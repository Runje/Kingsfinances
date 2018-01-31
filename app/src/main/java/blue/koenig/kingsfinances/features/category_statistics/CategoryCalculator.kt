package blue.koenig.kingsfinances.features.category_statistics

import blue.koenig.kingsfinances.model.StatisticsUtils
import blue.koenig.kingsfinances.model.calculation.AccumulativeStatisticsCalculator
import blue.koenig.kingsfinances.model.calculation.ItemSubject
import blue.koenig.kingsfinances.model.calculation.StatisticEntry
import com.google.common.collect.Lists
import com.koenig.commonModel.finance.Expenses
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 28.12.2017.
 */

class CategoryCalculator(protected var period: Period, expensesTable: ItemSubject<Expenses>, protected var service: CategoryCalculatorService) {
    private val categoryMapAsObservable: BehaviorSubject<Map<String, List<StatisticEntry>>>
    val yearsList: List<String>
    val monthsList: List<String>
    protected var lock = ReentrantLock()
    private val categoryMap: MutableMap<String, List<StatisticEntry>>
    val startDate: DateTime

    val allStatistics: Observable<Map<String, List<StatisticEntry>>>
        get() = categoryMapAsObservable.hide()

    val overallString: String
        get() = service.overallString

    init {
        categoryMap = service.categoryMap
        categoryMapAsObservable = BehaviorSubject.createDefault(HashMap())
        yearsList = generateYearsList()
        monthsList = generateMonthsList()
        startDate = service.startDate
        expensesTable.addAddListener { item -> addExpenses(item!!) }
        expensesTable.addDeleteListener { item -> deleteExpenses(item!!) }
        expensesTable.addUpdateListener { oldItem, newItem -> updateExpenses(oldItem!!, newItem!!) }
    }

    private fun updateExpenses(oldItem: Expenses, newItem: Expenses) {
        if (newItem.date == oldItem.date && newItem.category == oldItem.category) {
            val statisticEntry = StatisticEntry.fromTheoryCosts(newItem.date, newItem.getCostDistribution())
            statisticEntry.subtractEntry(StatisticEntry.fromTheoryCosts(oldItem.date, oldItem.getCostDistribution()))
            updateStatistics(statisticEntry, newItem.category)
        } else {
            // if date has changed, delete old one and add new item
            deleteExpenses(oldItem)
            addExpenses(newItem)
        }
    }

    private fun deleteExpenses(item: Expenses) {
        val statisticEntry = StatisticEntry(item.date)
        statisticEntry.subtractTheoryCosts(item.getCostDistribution())
        updateStatistics(statisticEntry, item.category)
    }

    protected fun updateStatistics(statisticEntry: StatisticEntry, category: String) {
        lock.lock()
        val statisticEntryList = AccumulativeStatisticsCalculator.updateStatistics(statisticEntry, period, getStatisticsFor(category))
        categoryMap[category] = statisticEntryList
        service.saveStatistics(categoryMap)
        lock.unlock()
    }

    fun getStatisticsFor(category: String): List<StatisticEntry> {
        val entries = categoryMap[category]
        return entries ?: ArrayList()
    }

    fun getCategoryMap(): Map<String, List<StatisticEntry>> {
        return categoryMap
    }

    private fun addExpenses(item: Expenses) {
        updateStatistics(StatisticEntry.fromTheoryCosts(item.date, item.getCostDistribution()), item.category)
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

    fun getCategoryStatistics(startDate: DateTime, endDate: DateTime): List<CategoryStatistics> {
        val categoryStatistics = ArrayList<CategoryStatistics>(categoryMap.size)
        for (category in categoryMap.keys) {
            val value = StatisticsUtils.calcDifferenceInPeriod(startDate, endDate, categoryMap[category])
            categoryStatistics.add(CategoryStatistics(category, value.sum, service.getGoalFor(category, startDate, endDate)))
        }

        return categoryStatistics
    }
}
