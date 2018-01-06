package blue.koenig.kingsfinances.model.calculation;

import com.koenig.commonModel.finance.Expenses;

import org.joda.time.Period;

/**
 * Created by Thomas on 28.12.2017.
 */

public class DebtsCalculator extends AccumulativeStatisticsCalculator {

    public DebtsCalculator(Period period, ItemSubject<Expenses> expensesTable, StatisticsCalculatorService service) {
        super(period, service);
        expensesTable.addAddListener(item -> addExpenses(item));
        expensesTable.addDeleteListener(item -> deleteExpenses(item));
        expensesTable.addUpdateListener((oldItem, newItem) -> updateExpenses(oldItem, newItem));
    }

    private void updateExpenses(Expenses oldItem, Expenses newItem) {
        if (newItem.getDate().equals(oldItem.getDate())) {
            StatisticEntry statisticEntry = new StatisticEntry(newItem.getDate(), newItem.getCostDistribution());
            statisticEntry.subtractEntry(new StatisticEntry(oldItem.getDate(), oldItem.getCostDistribution()));
            updateStatistics(statisticEntry);
        } else {
            // if date has changed, delete old one and add new item
            deleteExpenses(oldItem);
            addExpenses(newItem);
        }
    }

    private void deleteExpenses(Expenses item) {
        StatisticEntry statisticEntry = new StatisticEntry(item.getDate());
        statisticEntry.subtractCostDistribution(item.getCostDistribution());
        updateStatistics(statisticEntry);
    }

    private void addExpenses(Expenses item) {
        updateStatistics(new StatisticEntry(item.getDate(), item.getCostDistribution()));
    }
}
