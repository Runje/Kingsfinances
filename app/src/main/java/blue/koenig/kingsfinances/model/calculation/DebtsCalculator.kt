package blue.koenig.kingsfinances.model.calculation

import com.koenig.commonModel.Repository.MonthStatisticsRepository
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.CostDistribution
import com.koenig.commonModel.finance.Expenses
import com.koenig.commonModel.finance.statistics.ItemSubject
import com.koenig.commonModel.finance.statistics.MonthStatistic
import com.koenig.commonModel.finance.statistics.MonthStatisticsCalculator
import com.koenig.commonModel.finance.statistics.yearMonth
import io.reactivex.Observable
import org.joda.time.YearMonth

/**
 * Created by Thomas on 28.12.2017.
 */

class DebtsCalculator(expensesTable: ItemSubject<Expenses>, val deltaMonthStatisticsRepository: MonthStatisticsRepository, val absoluteMonthStatisticsRepository: MonthStatisticsRepository, endDateObservable: Observable<YearMonth>) : MonthStatisticsCalculator(endDateObservable, deltaMonthStatisticsRepository.load(), absoluteMonthStatisticsRepository.load()) {

    init {
        expensesTable.addAddListener { item -> addExpenses(item) }
        expensesTable.addDeleteListener { item -> deleteExpenses(item!!) }
        expensesTable.addUpdateListener { oldItem, newItem -> updateExpenses(oldItem!!, newItem) }
    }

    private fun updateExpenses(oldItem: Expenses, newItem: Expenses) {
        deleteExpenses(oldItem)
        addExpenses(newItem)
    }

    private fun deleteExpenses(item: Expenses) {
        updateStatistics(MonthStatistic(item.day.yearMonth, calcDebtsFromCostDistribution(item.costDistribution, inverse = true)))
    }

    private fun calcDebtsFromCostDistribution(costDistribution: CostDistribution, inverse: Boolean = false): Map<User, Int> {
        val result = mutableMapOf<User, Int>()
        for ((user, costs) in costDistribution.getDistribution()) {
            val newDebts = costs.Theory - costs.Real
            val debts = if (inverse) -newDebts else newDebts
            result[user] = debts
        }

        return result
    }

    private fun addExpenses(item: Expenses) {
        updateStatistics(MonthStatistic(item.day.yearMonth, calcDebtsFromCostDistribution(item.costDistribution, inverse = false)))
    }
}
