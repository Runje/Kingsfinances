package blue.koenig.kingsfinances.features.category_statistics

import blue.koenig.kingsfinances.model.StatisticsUtils
import blue.koenig.kingsfinances.model.calculation.*
import com.google.common.collect.Lists
import com.koenig.commonModel.finance.Expenses
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.YearMonth
import org.joda.time.Years
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 28.12.2017.
 */
// Only save delta statistics and calculate the rest
class CategoryCalculator(protected var period: Period, expensesTable: ItemSubject<Expenses>, protected var service: CategoryCalculatorService) {
    private val deltaCategoryMapForAllObservable: BehaviorSubject<Map<YearMonth, MonthStatistic>> = BehaviorSubject.createDefault(HashMap())
    val allCategory = "ALL_CATEGORYS"
    val yearsList: List<String>
    val monthsList: List<String>
    private var lock = ReentrantLock()

    private val deltaCategoryMap: MutableMap<String, MutableMap<YearMonth, MonthStatistic>> = service.deltaCategoryMap

    private val absoluteCategoryMap: MutableMap<String, MutableMap<YearMonth, MonthStatistic>> = service.absoluteCategoryMap

    val startDate: DateTime

    val deltaStatisticsForAll: Observable<Map<YearMonth, MonthStatistic>>
        get() = deltaCategoryMapForAllObservable.hide()

    val overallString: String
        get() = service.overallString

    init {
        yearsList = generateYearsList()
        monthsList = generateMonthsList()
        startDate = service.startDate
        expensesTable.addAddListener { item -> addExpenses(item!!) }
        expensesTable.addDeleteListener { item -> deleteExpenses(item!!) }
        expensesTable.addUpdateListener { oldItem, newItem -> updateExpenses(oldItem!!, newItem!!) }
    }

    private fun updateExpenses(oldItem: Expenses, newItem: Expenses) {
        deleteExpenses(oldItem)
        addExpenses(newItem)
    }

    private fun deleteExpenses(item: Expenses) {
        val statisticEntry = StatisticEntryDeprecated(item.date)
        statisticEntry.subtractTheoryCosts(item.costDistribution)
        updateStatistics(MonthStatistic.fromCostDistributionTakeTheory(item.date.yearMonth, item.costDistribution, negative = true), item.category)
    }

    private fun updateStatistics(deltaEntry: MonthStatistic, category: String) {
        lock.lock()


        // update deltamap: only update the according month
        deltaCategoryMap[category]?.let {
            it[deltaEntry.month] = (it[deltaEntry.month]
                    ?: MonthStatistic(deltaEntry.month)) + deltaEntry
        } ?: kotlin.run {
            // create new map
            deltaCategoryMap[category] = mutableMapOf(deltaEntry.month to deltaEntry)
        }

        // update all category
        deltaCategoryMap[allCategory]?.let {
            it[deltaEntry.month] = (it[deltaEntry.month]
                    ?: MonthStatistic(deltaEntry.month)) + deltaEntry
        } ?: kotlin.run {
            // create new map
            deltaCategoryMap[allCategory] = mutableMapOf(deltaEntry.month to deltaEntry)
        }

        // fill up month -1 to calculate correct statistics depending on this value
        val lastMonth = deltaEntry.month.minusMonths(1)
        absoluteCategoryMap[category]?.let {
            if (it[lastMonth] == null) {
                it[lastMonth] = lastEntryBefore(lastMonth, it).withMonth(lastMonth)
            }
        }

        absoluteCategoryMap[allCategory]?.let {
            if (it[lastMonth] == null) {
                it[lastMonth] = lastEntryBefore(lastMonth, it).withMonth(lastMonth)
            }
        }
        // update absolute map: update according and following months
        for (month in yearMonthRange(deltaEntry.month, service.endDate)) {
            absoluteCategoryMap[category]?.let {
                // take value from last month if not there
                it[month] = (it[month] ?: it[month.minusMonths(1)]?.withMonth(month)
                ?: MonthStatistic(month)) + deltaEntry
            } ?: kotlin.run {
                // create new map
                absoluteCategoryMap[category] = mutableMapOf(month to deltaEntry)
            }

            absoluteCategoryMap[allCategory]?.let {
                // take value from last month if not there
                it[month] = (it[month] ?: it[month.minusMonths(1)]?.withMonth(month)
                ?: MonthStatistic(month)) + deltaEntry
            } ?: kotlin.run {
                // create new map
                absoluteCategoryMap[allCategory] = mutableMapOf(month to deltaEntry)
            }
        }

        service.saveStatistics(deltaCategoryMap, absoluteCategoryMap)
        deltaCategoryMapForAllObservable.onNext(deltaCategoryMap[allCategory] ?: emptyMap())
        lock.unlock()
    }

    private fun lastEntryBefore(month: YearMonth, map: Map<YearMonth, MonthStatistic>): MonthStatistic {
        var lastValue = MonthStatistic(YearMonth(0, 1))

        map.values.forEach {
            if (it.month < month && it.month > lastValue.month) lastValue = it
        }

        return lastValue
    }


    fun getDeltaStatisticsFor(category: String): Map<YearMonth, MonthStatistic> {
        val entries = deltaCategoryMap[category]
        return entries ?: emptyMap<YearMonth, MonthStatistic>()
    }

    fun getAbsoluteStatisticsFor(category: String): Map<YearMonth, MonthStatistic> {
        val entries = absoluteCategoryMap[category]
        return entries ?: emptyMap<YearMonth, MonthStatistic>()
    }


    private fun addExpenses(item: Expenses) {
        updateStatistics(MonthStatistic.fromCostDistributionTakeTheory(item.date.yearMonth, item.costDistribution), item.category)
    }

    private fun generateYearsList(): List<String> {
        val list = Lists.newArrayList(service.overallString)
        list.addAll(StatisticsUtils.yearsList(service.startDate, DateTime.now()))
        return list
    }

    private fun generateMonthsList(): List<String> {
        val list = Lists.newArrayList(service.overallString)
        list.addAll(StatisticsUtils.allMonthsList())
        return list
    }

    fun getCategoryStatistics(month: YearMonth): List<CategoryStatistics> {
        val result = mutableListOf<CategoryStatistics>()
        for ((category, map) in deltaCategoryMap) {
            result.add(CategoryStatistics(category, map[month]?.sum
                    ?: 0, service.getGoalFor(category, month).toInt()))
        }

        return result
    }

    fun getCategoryStatistics(year: Years): List<CategoryStatistics> {
        return getCategoryStatistics(YearMonth(year.years, 1), YearMonth(year.years, 12))
    }

    fun getCategoryStatistics(start: YearMonth, end: YearMonth): List<CategoryStatistics> {
        val result = mutableListOf<CategoryStatistics>()

        for ((category, map) in deltaCategoryMap) {
            // sum values for each month of the year
            var sum = 0
            var goal = 0.0
            yearMonthRange(start, end).forEach {
                sum += map[it]?.sum ?: 0
                goal += service.getGoalFor(category, it)
            }
            result.add(CategoryStatistics(category, sum, goal.toInt()))
        }

        return result
    }

    fun getAbsoluteStatisticsForAll(): Map<YearMonth, MonthStatistic> {
        return absoluteCategoryMap[allCategory] ?: emptyMap()
    }
}

val Years.months: List<YearMonth>
    get() {
        return yearMonthRange(YearMonth(years, 1), YearMonth(years, 12))
    }


