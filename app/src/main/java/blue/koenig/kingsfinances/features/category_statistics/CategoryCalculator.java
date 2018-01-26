package blue.koenig.kingsfinances.features.category_statistics;

import com.google.common.collect.Lists;
import com.koenig.commonModel.finance.Expenses;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import blue.koenig.kingsfinances.model.StatisticsUtils;
import blue.koenig.kingsfinances.model.calculation.AccumulativeStatisticsCalculator;
import blue.koenig.kingsfinances.model.calculation.ItemSubject;
import blue.koenig.kingsfinances.model.calculation.StatisticEntry;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by Thomas on 28.12.2017.
 */

public class CategoryCalculator {
    private final BehaviorSubject<Map<String, List<StatisticEntry>>> categoryMapAsObservable;
    private final List<String> yearsList;
    private final List<String> monthsList;
    protected Period period;
    protected CategoryCalculatorService service;
    protected ReentrantLock lock = new ReentrantLock();
    private Map<String, List<StatisticEntry>> categoryMap;
    private DateTime startDate;

    public CategoryCalculator(Period period, ItemSubject<Expenses> expensesTable, CategoryCalculatorService service) {
        this.period = period;
        categoryMap = service.getCategoryMap();
        this.service = service;
        categoryMapAsObservable = BehaviorSubject.createDefault(new HashMap<>());
        yearsList = generateYearsList();
        monthsList = generateMonthsList();
        startDate = service.getStartDate();
        expensesTable.addAddListener(item -> addExpenses(item));
        expensesTable.addDeleteListener(item -> deleteExpenses(item));
        expensesTable.addUpdateListener((oldItem, newItem) -> updateExpenses(oldItem, newItem));
    }

    private void updateExpenses(Expenses oldItem, Expenses newItem) {
        if (newItem.getDate().equals(oldItem.getDate()) && newItem.getCategory().equals(oldItem.getCategory())) {
            StatisticEntry statisticEntry = StatisticEntry.fromTheoryCosts(newItem.getDate(), newItem.getCostDistribution());
            statisticEntry.subtractEntry(StatisticEntry.fromTheoryCosts(oldItem.getDate(), oldItem.getCostDistribution()));
            updateStatistics(statisticEntry, newItem.getCategory());
        } else {
            // if date has changed, delete old one and add new item
            deleteExpenses(oldItem);
            addExpenses(newItem);
        }
    }

    public List<String> getYearsList() {
        return yearsList;
    }

    public List<String> getMonthsList() {
        return monthsList;
    }

    private void deleteExpenses(Expenses item) {
        StatisticEntry statisticEntry = new StatisticEntry(item.getDate());
        statisticEntry.subtractTheoryCosts(item.getCostDistribution());
        updateStatistics(statisticEntry, item.getCategory());
    }

    protected void updateStatistics(StatisticEntry statisticEntry, String category) {
        lock.lock();
        List<StatisticEntry> statisticEntryList = AccumulativeStatisticsCalculator.updateStatistics(statisticEntry, period, getStatisticsFor(category));
        categoryMap.put(category, statisticEntryList);
        service.saveStatistics(categoryMap);
        lock.unlock();
    }

    public List<StatisticEntry> getStatisticsFor(String category) {
        List<StatisticEntry> entries = categoryMap.get(category);
        return entries == null ? new ArrayList<>() : entries;
    }

    public Map<String, List<StatisticEntry>> getCategoryMap() {
        return categoryMap;
    }

    private void addExpenses(Expenses item) {
        updateStatistics(StatisticEntry.fromTheoryCosts(item.getDate(), item.getCostDistribution()), item.getCategory());
    }

    public Observable<Map<String, List<StatisticEntry>>> getAllStatistics() {
        return categoryMapAsObservable.hide();
    }

    private List<String> generateYearsList() {
        ArrayList<String> list = Lists.newArrayList(service.getOverallString());
        list.addAll(StatisticsUtils.yearsList(service.getStartDate(), DateTime.now()));
        return list;
    }

    private List<String> generateMonthsList() {
        ArrayList<String> list = Lists.newArrayList(service.getOverallString());
        list.addAll(StatisticsUtils.allMonthsList());
        return list;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public String getOverallString() {
        return service.getOverallString();
    }

    public List<CategoryStatistics> getCategoryStatistics(DateTime startDate, DateTime endDate) {
        List<CategoryStatistics> categoryStatistics = new ArrayList<>(categoryMap.size());
        for (String category : categoryMap.keySet()) {
            StatisticEntry value = StatisticsUtils.calcDifferenceInPeriod(startDate, endDate, categoryMap.get(category));
            categoryStatistics.add(new CategoryStatistics(category, value.getSum(), service.getGoalFor(category, startDate, endDate)));
        }

        return categoryStatistics;
    }
}
