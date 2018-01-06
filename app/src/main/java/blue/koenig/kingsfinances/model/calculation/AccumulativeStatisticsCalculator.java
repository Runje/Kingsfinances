package blue.koenig.kingsfinances.model.calculation;

import com.koenig.commonModel.finance.Expenses;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 28.12.2017.
 */

public class AccumulativeStatisticsCalculator extends StatisticsCalculator {

    public AccumulativeStatisticsCalculator(Period period, StatisticsCalculatorService service) {
        super(period, service);
        this.period = period;
        this.service = service;
        statisticEntryList = service.getSavedSortedDebts();
    }

    public static List<StatisticEntry> updateStatistics(StatisticEntry statisticEntryDelta, Period period, List<StatisticEntry> statisticEntryList) {
        DateTime date = statisticEntryDelta.getDate();
        DateTime startDate = getStartDate(date);
        DateTime nextDate = startDate.plus(period);

        if (statisticEntryList.size() == 0) {
            statisticEntryList.add(new StatisticEntry(startDate));
            statisticEntryDelta.setDate(nextDate);
            statisticEntryList.add(statisticEntryDelta);
            return statisticEntryList;
        } else {
            for (StatisticEntry statisticEntry : statisticEntryList) {
                // change all debts after the delta
                if (!statisticEntry.getDate().isBefore(date)) {
                    statisticEntry.addEntry(statisticEntryDelta);
                }
            }

            DateTime firstDate = statisticEntryList.get(0).getDate();
            // date of last debt + 1
            StatisticEntry lastStatisticEntry = statisticEntryList.get(statisticEntryList.size() - 1);
            if (lastStatisticEntry.getDate().isBefore(statisticEntryDelta.getDate())) {
                nextDate = lastStatisticEntry.getDate().plus(period);

                // add debts after list
                while (date.isAfter(nextDate)) {
                    StatisticEntry statisticEntry = new StatisticEntry(lastStatisticEntry);
                    // add delta only if it next date is after date of delta
                    if (nextDate.isAfter(statisticEntryDelta.getDate())) {
                        statisticEntry.addEntry(statisticEntryDelta);
                    }

                    statisticEntry.setDate(nextDate);
                    statisticEntryList.add(statisticEntry);
                    nextDate = nextDate.plus(period);
                }
                StatisticEntry statisticEntry = new StatisticEntry(lastStatisticEntry);
                statisticEntry.addEntry(statisticEntryDelta);
                statisticEntry.setDate(nextDate);
                statisticEntryList.add(new StatisticEntry(statisticEntry));
            } else if (firstDate.isAfter(statisticEntryDelta.getDate())) {

                // add missing debts before all
                // TODO: instead of months between, period.between
                List<StatisticEntry> newStatisticEntryAtBeginning = new ArrayList<>(Months.monthsBetween(startDate, firstDate).getMonths() + statisticEntryList.size());
                if (firstDate.isAfter(startDate)) {
                    newStatisticEntryAtBeginning.add(new StatisticEntry(startDate));
                }

                while (firstDate.isAfter(nextDate)) {
                    newStatisticEntryAtBeginning.add(new StatisticEntry(nextDate, statisticEntryDelta.getEntryMap()));
                    nextDate = nextDate.plus(period);
                }

                // concatenate both lists
                newStatisticEntryAtBeginning.addAll(statisticEntryList);
                return newStatisticEntryAtBeginning;
            }

            return statisticEntryList;
        }
    }

    @Deprecated
    public static List<StatisticEntry> recalculateAll(List<Expenses> expensesList, Period period) {
        if (expensesList.size() == 0) return new ArrayList<>();

        DateTime date = expensesList.get(0).getDate();
        // if it is already the first of the month take the month before as start date
        DateTime startDate = getStartDate(date);
        DateTime nextDate = startDate.plus(period);
        int size = Months.monthsBetween(startDate, expensesList.get(expensesList.size() - 1).getDate()).getMonths() + 2;
        List<StatisticEntry> statisticEntryList = new ArrayList<>(size);
        StatisticEntry statisticEntry = new StatisticEntry();
        statisticEntry.setDate(startDate);
        statisticEntryList.add(new StatisticEntry(statisticEntry));
        for (Expenses expenses : expensesList) {
            while (expenses.getDate().isAfter(nextDate)) {
                statisticEntry.setDate(nextDate);
                // add copy
                statisticEntryList.add(new StatisticEntry(statisticEntry));
                nextDate = nextDate.plus(period);
            }

            statisticEntry.addCostDistribution(expenses.getCostDistribution());
        }

        statisticEntry.setDate(nextDate);
        statisticEntryList.add(statisticEntry);
        return statisticEntryList;
    }

    @Override
    protected List<StatisticEntry> calculateNewStatistics(StatisticEntry statisticEntry, Period period, List<StatisticEntry> statisticEntryList) {
        return updateStatistics(statisticEntry, period, statisticEntryList);
    }


}
