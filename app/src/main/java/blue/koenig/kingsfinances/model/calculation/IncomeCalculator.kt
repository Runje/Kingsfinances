package blue.koenig.kingsfinances.model.calculation

import com.koenig.commonModel.Repository.IncomeRepository
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

class IncomeCalculator(expensesTable: ItemSubject<Expenses>, endDateObservable: Observable<YearMonth>, val incomeRepository: IncomeRepository) : MonthStatisticsCalculator(endDateObservable, incomeRepository.loadDeltaMap(), incomeRepository.loadAbsoluteMap()) {

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
        if (item.costs > 0) {
            updateStatistics(MonthStatistic.fromCostDistributionTakeTheory(item.day.yearMonth, item.costDistribution, negative = true))
        }
    }

    private fun addExpenses(item: Expenses) {
        if (item.costs > 0) {
            updateStatistics(MonthStatistic.fromCostDistributionTakeTheory(item.day.yearMonth, item.costDistribution))
        }
    }

    override fun onUpdateStatistics(delta: MonthStatistic) {
        incomeRepository.saveAbsoluteMap(absoluteMap)
        incomeRepository.saveDeltaMap(deltaMap)
    }
}
