package blue.koenig.kingsfinances.view

import android.graphics.Color
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.statistics.MonthStatistic
import org.joda.time.YearMonth
import java.util.*

/**
 * Created by Thomas on 05.01.2018.
 */

object ChartHelper {

    fun entrysToMonthXValues(statisticEntryList: List<YearMonth>): List<String> {
        val xEntrys = ArrayList<String>(statisticEntryList.size)
        for (debt in statisticEntryList) {
            val dateString = debt.toString("MM/yy")
            xEntrys.add(dateString)
        }

        return xEntrys

    }


    fun mapToLineData(entries: List<MonthStatistic>, colors: IntArray, users: List<User>): LineData {
        // One entries not possible
        if (entries.size <= 1) return LineData()
        val dataSets = ArrayList<ILineDataSet>()

        for ((userCount, user) in users.withIndex()) {
            val userEntrys = ArrayList<Entry>(entries.size)
            entries.forEachIndexed { index, monthStatistic ->
                monthStatistic.entryMap[user]?.let {
                    userEntrys.add(Entry(index.toFloat(), it / 100f))
                }
            }

            val barDataSet = LineDataSet(userEntrys, user.name)
            barDataSet.color = colors[userCount % colors.size]
            barDataSet.valueTextColor = Color.WHITE
            if (barDataSet.entryCount > 0) dataSets.add(barDataSet)
        }

        return LineData(dataSets)
    }
}
