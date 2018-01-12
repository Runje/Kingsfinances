package blue.koenig.kingsfinances.model.calculation;

import com.koenig.commonModel.finance.Expenses;

import org.joda.time.Period;

/**
 * Created by Thomas on 28.12.2017.
 */

public class IncomeCalculator extends AccumulativeStatisticsCalculator {

    public IncomeCalculator(Period period, ItemSubject<Expenses> expensesTable, StatisticsCalculatorService service) {
        super(period, service);
        expensesTable.addAddListener(item -> addExpenses(item));
        expensesTable.addDeleteListener(item -> deleteExpenses(item));
        expensesTable.addUpdateListener((oldItem, newItem) -> updateExpenses(oldItem, newItem));
    }

    private void updateExpenses(Expenses oldItem, Expenses newItem) {
        if (newItem.getDate().equals(oldItem.getDate())) {
            if (oldItem.getCosts() > 0 || newItem.getCosts() > 0) {
                StatisticEntry statisticEntry = new StatisticEntry(newItem.getDate());
                statisticEntry.addTheoryCosts(newItem.getCostDistribution());
                statisticEntry.subtractTheoryCosts(oldItem.getCostDistribution());
                updateStatistics(statisticEntry);
            }
        } else {
            // if date has changed, delete old one and add new item
            deleteExpenses(oldItem);
            addExpenses(newItem);
        }
    }

    private void deleteExpenses(Expenses item) {
        if (item.getCosts() > 0) {
            StatisticEntry statisticEntry = new StatisticEntry(item.getDate());
            statisticEntry.subtractTheoryCosts(item.getCostDistribution());
            updateStatistics(statisticEntry);
        }
    }

    private void addExpenses(Expenses item) {
        if (item.getCosts() > 0) {
            StatisticEntry statisticEntry = new StatisticEntry(item.getDate());
            statisticEntry.addTheoryCosts(item.getCostDistribution());
            updateStatistics(statisticEntry);
        }
    }
}
