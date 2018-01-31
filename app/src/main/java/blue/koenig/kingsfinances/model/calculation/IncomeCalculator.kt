package blue.koenig.kingsfinances.model.calculation

import com.koenig.commonModel.finance.Expenses

import org.joda.time.Period

/**
 * Created by Thomas on 28.12.2017.
 */

class IncomeCalculator(period: Period, expensesTable: ItemSubject<Expenses>, service: StatisticsCalculatorService) : AccumulativeStatisticsCalculator(period, service) {

    init {
        expensesTable.addAddListener { item -> addExpenses(item!!) }
        expensesTable.addDeleteListener { item -> deleteExpenses(item!!) }
        expensesTable.addUpdateListener { oldItem, newItem -> updateExpenses(oldItem!!, newItem!!) }
    }

    private fun updateExpenses(oldItem: Expenses, newItem: Expenses) {
        if (newItem.date == oldItem.date) {
            if (oldItem.costs > 0 || newItem.costs > 0) {
                val statisticEntry = StatisticEntry(newItem.date)
                statisticEntry.addTheoryCosts(newItem.getCostDistribution())
                statisticEntry.subtractTheoryCosts(oldItem.getCostDistribution())
                updateStatistics(statisticEntry)
            }
        } else {
            // if date has changed, delete old one and add new item
            deleteExpenses(oldItem)
            addExpenses(newItem)
        }
    }

    private fun deleteExpenses(item: Expenses) {
        if (item.costs > 0) {
            val statisticEntry = StatisticEntry(item.date)
            statisticEntry.subtractTheoryCosts(item.getCostDistribution())
            updateStatistics(statisticEntry)
        }
    }

    private fun addExpenses(item: Expenses) {
        if (item.costs > 0) {
            val statisticEntry = StatisticEntry(item.date)
            statisticEntry.addTheoryCosts(item.getCostDistribution())
            updateStatistics(statisticEntry)
        }
    }
}
