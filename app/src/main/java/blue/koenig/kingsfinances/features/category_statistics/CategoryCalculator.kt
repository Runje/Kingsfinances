package blue.koenig.kingsfinances.features.category_statistics

import blue.koenig.kingsfinances.model.calculation.MonthStatisticsCalculator
import com.koenig.commonModel.Repository.CategoryRepository
import com.koenig.commonModel.finance.Expenses
import com.koenig.commonModel.finance.statistics.ItemSubject
import com.koenig.commonModel.finance.statistics.MonthStatistic
import com.koenig.commonModel.finance.statistics.yearMonth
import com.koenig.commonModel.finance.statistics.yearMonthRange
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.YearMonth
import org.joda.time.Years
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Created by Thomas on 28.12.2017.
 */
// Only save delta statistics and calculate the rest
class CategoryCalculator(expensesTable: ItemSubject<Expenses>, val categoryRepository: CategoryRepository, val endDateObservable: Observable<YearMonth>) : MonthStatisticsCalculator(endDateObservable, categoryRepository.allCategoryDeltaStatistics, categoryRepository.allCategoryAbsoluteStatistics) {
    private val deltaCategoryMapForAllObservable: BehaviorSubject<Map<YearMonth, MonthStatistic>> = BehaviorSubject.createDefault(HashMap())
    private var lock = ReentrantLock()

    private val calculatorMap = mutableMapOf<String, MonthStatisticsCalculator>()

    val deltaStatisticsForAll: Observable<Map<YearMonth, MonthStatistic>>
        get() = deltaCategoryMapForAllObservable.hide()


    init {
        expensesTable.addAddListener { item -> addExpenses(item) }
        expensesTable.addDeleteListener { item -> deleteExpenses(item!!) }
        expensesTable.addUpdateListener { oldItem, newItem -> updateExpenses(oldItem!!, newItem) }
        categoryRepository.savedCategorys.forEach {
            calculatorMap[it] = createMonthStatisticsCalculator(it)
        }
    }

    private fun createMonthStatisticsCalculator(category: String): MonthStatisticsCalculator {
        return MonthStatisticsCalculator(endDateObservable, categoryRepository.getCategoryDeltaStatistics(category), categoryRepository.getCategoryAbsoluteStatistics(category))
    }

    private fun updateExpenses(oldItem: Expenses, newItem: Expenses) {
        deleteExpenses(oldItem)
        addExpenses(newItem)
    }

    private fun deleteExpenses(item: Expenses) {
        updateStatistics(MonthStatistic.fromCostDistributionTakeTheory(item.day.yearMonth, item.costDistribution, negative = true), item.category)
    }

    private fun updateStatistics(deltaEntry: MonthStatistic, category: String) {
        lock.withLock {
            if (calculatorMap[category] == null) {

                calculatorMap[category] = createMonthStatisticsCalculator(category)
            }

            calculatorMap[category]!!.updateStatistics(deltaEntry)

            updateStatistics(deltaEntry)

            // save allcategory maps
            categoryRepository.saveAllCategoryAbsoluteStatistics(absoluteMap)
            categoryRepository.saveAllCategoryDeltaStatistics(deltaMap)

            // save changed category maps
            categoryRepository.saveCategoryAbsoluteStatistics(category, calculatorMap[category]!!.absoluteMap)
            categoryRepository.saveCategoryDeltaStatistics(category, calculatorMap[category]!!.deltaMap)

            deltaCategoryMapForAllObservable.onNext(deltaMap)
        }


    }

    fun getDeltaStatisticsFor(category: String): Map<YearMonth, MonthStatistic> {
        val entries = calculatorMap[category]?.deltaMap
        return entries ?: emptyMap<YearMonth, MonthStatistic>()
    }

    fun getAbsoluteStatisticsFor(category: String): Map<YearMonth, MonthStatistic> {
        val entries = calculatorMap[category]?.absoluteMap
        return entries ?: emptyMap<YearMonth, MonthStatistic>()
    }


    private fun addExpenses(item: Expenses) {
        updateStatistics(MonthStatistic.fromCostDistributionTakeTheory(item.day.yearMonth, item.costDistribution), item.category)
    }



    fun getAbsoluteStatisticsForAll(): Map<YearMonth, MonthStatistic> {
        return absoluteMap
    }
}

val Years.months: List<YearMonth>
    get() {
        return yearMonthRange(YearMonth(years, 1), YearMonth(years, 12))
    }


