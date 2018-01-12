package blue.koenig.kingsfinances.model.calculation;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Thomas on 04.01.2018.
 */

public abstract class StatisticsCalculator {
    protected Period period;
    protected StatisticsCalculatorService service;
    protected List<StatisticEntry> statisticEntryList;
    protected ReentrantLock lock = new ReentrantLock();

    public StatisticsCalculator(Period period, StatisticsCalculatorService service) {
        this.period = period;
        this.service = service;
        statisticEntryList = service.getSavedSortedStatistics();
    }

    @NonNull
    public static DateTime getStartDate(DateTime date) {
        return date.dayOfMonth().get() == 1 ? date.minus(Period.months(1)) : date.withDayOfMonth(1);
    }

    protected void updateStatistics(StatisticEntry statisticEntry) {
        lock.lock();
        statisticEntryList = calculateNewStatistics(statisticEntry, period, statisticEntryList);
        service.saveStatistics(statisticEntryList);
        lock.unlock();
    }

    protected abstract List<StatisticEntry> calculateNewStatistics(StatisticEntry statisticEntry, Period period, List<StatisticEntry> statisticEntryList);

    public List<StatisticEntry> getEntrys() {
        return statisticEntryList;
    }
}
