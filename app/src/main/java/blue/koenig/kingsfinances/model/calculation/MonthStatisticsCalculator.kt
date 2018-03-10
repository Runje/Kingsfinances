package blue.koenig.kingsfinances.model.calculation

import com.koenig.commonModel.finance.statistics.MonthStatistic
import com.koenig.commonModel.finance.statistics.yearMonthRange
import io.reactivex.Observable
import org.joda.time.YearMonth

/**
 * Created by Thomas on 07.03.2018.
 */
open class MonthStatisticsCalculator(endDateObservable: Observable<YearMonth>, val deltaMap: MutableMap<YearMonth, MonthStatistic>, val absoluteMap: MutableMap<YearMonth, MonthStatistic>) {
    lateinit var endDate: YearMonth

    init {
        endDateObservable.subscribe { endDate = it }
    }

    fun updateStatistics(delta: MonthStatistic) {
        updateDeltaMap(deltaMap, delta)
        updateAbsoluteMap(absoluteMap, delta)
        onUpdateStatistics(delta)
    }

    protected open fun onUpdateStatistics(delta: MonthStatistic) {
        // to be overidden in subclasses
    }

    private fun updateAbsoluteMap(absoluteMap: MutableMap<YearMonth, MonthStatistic>, deltaEntry: MonthStatistic) {
        // fill up month -1 to calculate correct statistics depending on this value
        val lastMonth = deltaEntry.month.minusMonths(1)

        if (absoluteMap[lastMonth] == null) {
            absoluteMap[lastMonth] = lastEntryBefore(lastMonth, absoluteMap).withMonth(lastMonth)
        }

        // update absolute map: update according and following months
        for (month in yearMonthRange(deltaEntry.month, endDate)) {
            // take value from last month if not there
            absoluteMap[month] = (absoluteMap[month]
                    ?: absoluteMap[month.minusMonths(1)]?.withMonth(month)
                    ?: MonthStatistic(month)) + deltaEntry
        }
    }


    private fun updateDeltaMap(deltaMap: MutableMap<YearMonth, MonthStatistic>, delta: MonthStatistic) {
        deltaMap[delta.month] = (deltaMap[delta.month] ?: MonthStatistic(delta.month)) + delta
    }
}

fun lastEntryBefore(month: YearMonth, map: Map<YearMonth, MonthStatistic>): MonthStatistic {
    var lastValue = MonthStatistic(YearMonth(0, 1))

    map.values.forEach {
        if (it.month < month && it.month > lastValue.month) lastValue = it
    }

    return lastValue
}