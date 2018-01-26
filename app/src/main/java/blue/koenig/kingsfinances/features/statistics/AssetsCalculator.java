package blue.koenig.kingsfinances.features.statistics;

import com.google.common.collect.Lists;
import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.Balance;
import com.koenig.commonModel.finance.BankAccount;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.Years;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import blue.koenig.kingsfinances.model.StatisticsUtils;
import blue.koenig.kingsfinances.model.calculation.ItemSubject;
import blue.koenig.kingsfinances.model.calculation.StatisticEntry;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.koenig.FamilyConstants.ALL_USER;

/**
 * Created by Thomas on 03.01.2018.
 */

public class AssetsCalculator {

    private static BankAccount ALL_ASSETS = new BankAccount("ALL_ASSETS", "ALL_ASSETS", "ALL", ALL_USER, new ArrayList<>());
    private static User FORECAST_USER = new User("YEAR_FORECAST", "YEAR_FORECAST", "F", new DateTime());
    private static BankAccount YEAR_FORECAST = new BankAccount("YEAR_FORECAST", "YEAR_FORECAST", "YEAR_FORECAST", FORECAST_USER, new ArrayList<>());
    private static BankAccount FUTURE_FORECAST = new BankAccount("FUTURE_FORECAST", "FUTURE_FORECAST", "FUTURE_FORECAST", FORECAST_USER, new ArrayList<>());
    private final DateTime startDate;
    private final DateTime endDate;
    private final List<String> yearsList;
    protected Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());
    protected Period period;
    protected AssetsCalculatorService service;
    protected Map<BankAccount, List<StatisticEntry>> statisticEntryLists;
    protected ReentrantLock lock = new ReentrantLock();
    private BehaviorSubject<List<StatisticEntry>> allAssets;

    public AssetsCalculator(Period period, ItemSubject<BankAccount> bankSubject, AssetsCalculatorService service) {
        this.period = period;
        this.service = service;
        statisticEntryLists = service.loadAllBankAccountStatistics();
        List<StatisticEntry> allAssetsStartValue = statisticEntryLists.get(ALL_ASSETS);
        if (allAssetsStartValue == null) allAssetsStartValue = new ArrayList<>();
        allAssets = BehaviorSubject.createDefault(allAssetsStartValue);
        startDate = service.getStartDate();
        endDate = service.getEndDate();
        bankSubject.addAddListener(bankAccount -> addBankAccount(bankAccount));
        bankSubject.addDeleteListener(bankAccount -> deleteBankAccount(bankAccount));
        bankSubject.addUpdateListener((oldBankAccount, newBankAccount) -> updateBankAccount(oldBankAccount, newBankAccount));
        yearsList = generateYearsList();
    }

    public static List<StatisticEntry> calculateStatisticsOfBankAccount(BankAccount bankAccount, DateTime start, DateTime end, Period period) {
        start = start.dayOfMonth().get() == 1 ? start : start.withDayOfMonth(1);
        end = end.dayOfMonth().get() == 1 ? end : end.plus(Months.ONE).withDayOfMonth(1);
        List<StatisticEntry> entries = new ArrayList<>(Months.monthsBetween(start, end).getMonths() + 1);
        // balances are sorted with newest at top
        List<Balance> balances = bankAccount.getBalances();
        DateTime nextDate = start;
        if (balances.size() == 0) {
            while (!nextDate.isAfter(end)) {
                entries.add(new StatisticEntry(nextDate));
                nextDate = nextDate.plus(period);
            }

            return entries;
        }

        // first balance is last (sorted)
        Balance firstBalance = balances.get(balances.size() - 1);
        DateTime firstDate = firstBalance.getDate();
        // fill all entrys before first entry with value of first entry
        while (nextDate.isBefore(firstDate)) {
            StatisticEntry entry = new StatisticEntry(nextDate);
            entry.addEntry(new StatisticEntry(firstBalance, bankAccount.getOwners()));
            entries.add(entry);
            nextDate = nextDate.plus(period);
        }

        Balance lastBalance = firstBalance;
        for (int i = balances.size() - 1; i >= 0; i--) {
            Balance balance = balances.get(i);
            DateTime date = balance.getDate();
            if (nextDate.equals(date)) {
                entries.add(new StatisticEntry(balance, bankAccount.getOwners()));
                nextDate = nextDate.plus(period);
            } else if (!nextDate.isAfter(date)) {
                while (balance.getDate().isAfter(nextDate)) {
                    entries.add(new StatisticEntry(new Balance(calcLinearEstimation(lastBalance, balance, nextDate), nextDate), bankAccount.getOwners()));
                    nextDate = nextDate.plus(period);
                }
            }

            lastBalance = balance;
        }

        // add value of last balance until end
        while (!nextDate.isAfter(end)) {
            StatisticEntry statisticEntry = new StatisticEntry(nextDate);
            statisticEntry.addEntry(new StatisticEntry(lastBalance, bankAccount.getOwners()));
            entries.add(statisticEntry);
            nextDate = nextDate.plus(period);
        }

        return entries;
    }

    public static int calcLinearEstimation(Balance lastBalance, Balance balance, DateTime nextDate) {
        int days = Days.daysBetween(lastBalance.getDate(), balance.getDate()).getDays();
        int daysUntil = Days.daysBetween(lastBalance.getDate(), nextDate).getDays();

        return lastBalance.getBalance() + daysUntil * (balance.getBalance() - lastBalance.getBalance()) / days;
    }

    public List<String> getYearsList() {
        return yearsList;
    }

    public AssetsStatistics calcStatisticsFor(DateTime startDate, DateTime endDate) {
        List<StatisticEntry> allAssets = getEntrysForAll();
        List<StatisticEntry> filtered = new ArrayList<>();

        for (StatisticEntry statisticEntry : allAssets) {
            DateTime statisticEntryDate = statisticEntry.getDate();
            if (!statisticEntryDate.isBefore(startDate) && !statisticEntryDate.isAfter(endDate)) {
                filtered.add(statisticEntry);
            }
        }
        int overallWin = 0;
        int monthlyWin = 0;
        if (filtered.size() > 0) {
            StatisticEntry last = filtered.get(filtered.size() - 1);
            StatisticEntry first = filtered.get(0);
            overallWin = last.getEntryFor(ALL_USER) - first.getEntryFor(ALL_USER);
            monthlyWin = overallWin / Months.monthsBetween(first.getDate(), last.getDate()).getMonths();
        }

        // add forecast to filtered if it is the actual year
        if (DateTime.now().getYear() == startDate.getYear()) {
            List<StatisticEntry> forecast = getEntrysForForecast();
            if (forecast != null) {
                for (int i = 0; i < forecast.size(); i++) {
                    StatisticEntry entry = forecast.get(i);
                    if (i == 0 && filtered.size() > 0) {
                        StatisticEntry lastFiltered = filtered.get(filtered.size() - 1);
                        assert entry.getDate().equals(lastFiltered);
                        // first entry is overlapping, add to entry
                        lastFiltered.putEntry(FORECAST_USER, entry.getEntryFor(FORECAST_USER));
                    } else {
                        // new entry
                        filtered.add(entry);
                    }
                }
            }
        }


        return new AssetsStatistics(startDate, endDate, filtered, monthlyWin, overallWin);
    }

    private List<String> generateYearsList() {
        ArrayList<String> list = Lists.newArrayList(service.getOverallString(), service.getFutureString());
        list.addAll(StatisticsUtils.yearsList(startDate, DateTime.now()));
        return list;
    }

    public Observable<List<StatisticEntry>> getAllAssets() {
        return allAssets;
    }

    private void addBankAccount(BankAccount bankAccount) {
        updateStatisticsFor(bankAccount);

    }

    private void updateStatisticsFor(BankAccount bankAccount) {
        lock.lock();
        statisticEntryLists.put(bankAccount, calculateStatisticsOfBankAccount(bankAccount, startDate, endDate, period));
        updateAllAssets();
        updateForecast();
        service.save(statisticEntryLists);
        lock.unlock();
    }

    private void updateForecast() {
        List<StatisticEntry> all = getEntrysForAll();
        StatisticEntry lastEntry = all.get(all.size() - 1);
        DateTime lastDate = lastEntry.getDate().plusYears(1).withMonthOfYear(1);
        int numberEntrys = Months.monthsBetween(lastEntry.getDate(), lastDate).getMonths() + 1;
        List<StatisticEntry> forecast = new ArrayList<>(numberEntrys);
        int sum = lastEntry.getEntryFor(ALL_USER);
        Map<User, Integer> entryMap = new HashMap<>(1);
        entryMap.put(FORECAST_USER, sum);
        forecast.add(new StatisticEntry(lastEntry.getDate(), entryMap));

        // get average win of last 12 month
        StatisticEntry overallWin = new StatisticEntry(lastEntry);
        overallWin.subtractEntry(all.get(Math.max(0, all.size() - 12)));
        int averageWin = overallWin.getSum() / 12;

        for (int i = 1; i < numberEntrys; i++) {
            int prognose = sum + averageWin * i;
            Map<User, Integer> map = new HashMap<>(1);
            map.put(FORECAST_USER, prognose);
            forecast.add(new StatisticEntry(lastEntry.getDate().plus(Months.months(i)), map));
        }

        statisticEntryLists.put(YEAR_FORECAST, forecast);

        int years = 20;
        List<StatisticEntry> futureForecast = new ArrayList<>(years);
        for (int i = 0; i < years; i++) {
            int prognose = sum + overallWin.getSum() * i;
            Map<User, Integer> map = new HashMap<>(1);
            map.put(FORECAST_USER, prognose);
            futureForecast.add(new StatisticEntry(lastEntry.getDate().plus(Years.years(i)), map));
        }

        statisticEntryLists.put(FUTURE_FORECAST, futureForecast);

    }



    private void deleteBankAccount(BankAccount bankAccount) {
        lock.lock();
        statisticEntryLists.remove(bankAccount);
        updateAllAssets();
        updateForecast();
        service.save(statisticEntryLists);
        lock.unlock();
    }

    private void updateAllAssets() {
        List<StatisticEntry> allEntries = new ArrayList<>();
        for (BankAccount bankAccount : statisticEntryLists.keySet()) {
            if (bankAccount.equals(ALL_ASSETS) || bankAccount.equals(YEAR_FORECAST) || bankAccount.equals(FUTURE_FORECAST))
                continue;
            List<StatisticEntry> entries = statisticEntryLists.get(bankAccount);
            for (int i = 0; i < entries.size(); i++) {
                StatisticEntry entry = entries.get(i);
                if (allEntries.size() <= i) {
                    // add new entry if no entry
                    allEntries.add(new StatisticEntry(entry));
                } else {
                    allEntries.get(i).addEntry(entry);
                }

                // calculate sum for ALL
                Map<User, Integer> entryMap = allEntries.get(i).getEntryMap();
                int sum = 0;
                for (User user : entryMap.keySet()) {
                    if (user.equals(ALL_USER)) continue;
                    sum += entryMap.get(user);
                }

                entryMap.put(ALL_USER, sum);
            }
        }

        statisticEntryLists.put(ALL_ASSETS, allEntries);
        logger.info("On Next");
        allAssets.onNext(allEntries);
    }

    private void updateBankAccount(BankAccount oldBankAccount, BankAccount newBankAccount) {
        updateStatisticsFor(newBankAccount);
    }

    public List<StatisticEntry> getEntrysFor(BankAccount bankAccount) {
        return statisticEntryLists.get(bankAccount);
    }

    public List<StatisticEntry> getEntrysForAll() {
        List<StatisticEntry> entries = statisticEntryLists.get(ALL_ASSETS);
        if (entries == null) return new ArrayList<>();
        return entries;
    }

    public List<StatisticEntry> getEntrysForForecast() {
        List<StatisticEntry> entries = statisticEntryLists.get(YEAR_FORECAST);
        if (entries == null) return new ArrayList<>();
        return entries;
    }

    public List<StatisticEntry> getEntrysForFutureForecast() {
        List<StatisticEntry> entries = statisticEntryLists.get(FUTURE_FORECAST);
        if (entries == null) return new ArrayList<>();
        return entries;
    }
}
