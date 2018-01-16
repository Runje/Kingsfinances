package blue.koenig.kingsfinances;

import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.BankAccount;
import com.koenig.commonModel.finance.CostDistribution;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import blue.koenig.kingsfinances.features.statistics.AssetsCalculatorService;
import blue.koenig.kingsfinances.model.calculation.StatisticEntry;
import blue.koenig.kingsfinances.model.calculation.StatisticsCalculatorService;

/**
 * Created by Thomas on 02.01.2018.
 */

public class TestHelper {
    public static User milena = new User("Milena");
    public static User thomas = new User("Thomas");

    public static CostDistribution makeCostDistribution(int theoryThomas, int realThomas, int theoryMilena, int realMilena) {
        CostDistribution costDistribution = new CostDistribution();
        costDistribution.putCosts(thomas, realThomas, theoryThomas);
        costDistribution.putCosts(milena, realMilena, theoryMilena);
        return costDistribution;
    }

    public static DateTime getDay(int year, int month, int day) {
        return new DateTime(year, month, day, 0, 0);
    }

    public static StatisticsCalculatorService getCalculatorService(List<StatisticEntry> statisticEntryList) {
        return new StatisticsCalculatorService() {
            @Override
            public List<StatisticEntry> getSavedSortedStatistics() {
                return statisticEntryList;
            }

            @Override
            public void saveStatistics(List<StatisticEntry> statisticEntryList) {

            }
        };
    }

    public static AssetsCalculatorService getAssetsCalculatorService(Map<BankAccount, List<StatisticEntry>> map, DateTime start, DateTime end) {
        return new AssetsCalculatorService() {
            @Override
            public Map<BankAccount, List<StatisticEntry>> loadAllBankAccountStatistics() {
                return map;
            }

            @Override
            public DateTime getStartDate() {
                return start;
            }

            @Override
            public DateTime getEndDate() {
                return end;
            }

            @Override
            public String getOverallString() {
                return null;
            }

            @Override
            public void save(Map<BankAccount, List<StatisticEntry>> statisticEntryLists) {

            }

            @Override
            public String getFutureString() {
                return null;
            }
        };
    }

    public static void assertDebtsList(int index, DateTime dateTime, int debts, List<StatisticEntry> statisticEntryList) {
        StatisticEntry debt = statisticEntryList.get(index);
        Assert.assertEquals(dateTime, debt.getDate());
        Assert.assertEquals(debts, debt.getEntryFor(thomas));
        Assert.assertEquals(-debts, debt.getEntryFor(milena));
    }

    public static List<StatisticEntry> makeDebtsList(DateTime startDate, int[] debts) {
        List<StatisticEntry> statisticEntryList = new ArrayList<>(debts.length);
        for (int i = 0; i < debts.length; i++) {
            statisticEntryList.add(makeDebts(startDate, debts[i]));
            startDate = startDate.plus(Period.months(1));
        }

        return statisticEntryList;
    }

    public static StatisticEntry makeDebts(DateTime date, int debts) {
        return new StatisticEntry(date, makeCostDistribution(debts, 0, 0, debts));
    }

    public static StatisticEntry makeDebts(int debts, int year, int month, int day) {
        return makeDebts(getDay(year, month, day), debts);
    }

    public static StatisticsCalculatorService getStatisticsCalculatorService(List<StatisticEntry> list) {
        return new StatisticsCalculatorService() {
            @Override
            public List<StatisticEntry> getSavedSortedStatistics() {
                return list;
            }

            @Override
            public void saveStatistics(List<StatisticEntry> statisticEntryList) {

            }
        };
    }
}
