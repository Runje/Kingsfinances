package blue.koenig.kingsfinances.features.category_statistics

import android.content.Context
import blue.koenig.kingsfamilylibrary.model.FamilyConfig
import blue.koenig.kingsfinances.R
import blue.koenig.kingsfinances.model.calculation.StatisticEntry
import blue.koenig.kingsfinances.model.database.GoalTable
import com.koenig.commonModel.Byteable
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.PeriodType
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by Thomas on 20.01.2018.
 */

class FinanceCategoryCalculatorService(private val context: Context, private val startDate: DateTime, private val table: GoalTable) : CategoryCalculatorService {
    private val key = "CATEGORY_STATISTICS"

    override fun getCategoryMap(): Map<String, List<StatisticEntry>> {
        val buffer = FamilyConfig.getBytesFromConfig(context, key) ?: return HashMap()

        val size = buffer.int
        val listMap = HashMap<String, List<StatisticEntry>>(size)
        for (i in 0 until size) {
            val category = Byteable.byteToString(buffer)
            val entries = buffer.int
            val statistics = ArrayList<StatisticEntry>(entries)
            for (j in 0 until entries) {
                statistics.add(StatisticEntry(buffer))
            }

            listMap[category] = statistics
        }

        return listMap
    }

    override fun saveStatistics(categoryMap: Map<String, List<StatisticEntry>>) {
        var size = 4
        for (category in categoryMap.keys) {
            size += Byteable.getStringLength(category)
            size += Byteable.getBigListLength(categoryMap[category])
        }

        val buffer = ByteBuffer.allocate(size)
        buffer.putInt(categoryMap.size)
        for (category in categoryMap.keys) {
            Byteable.writeString(category, buffer)
            Byteable.writeBigList(categoryMap[category], buffer)
        }

        FamilyConfig.saveBytes(context, buffer.array(), key)
    }

    override fun getOverallString(): String {
        return context.getString(R.string.overall)
    }

    override fun getStartDate(): DateTime {
        return startDate
    }

    override fun getGoalFor(category: String, startDate: DateTime, endDate: DateTime): Int {
        // only in the same year(period can go to first of january of next year
        assert(startDate.year == endDate.year || startDate.year + 1 == endDate.year && endDate.monthOfYear == 1 && endDate.dayOfMonth == 1)
        // which year is it?
        val year = startDate.year
        //ask database for value of year (in case of one month divide through number of month)
        val goal = table.getFromName(category) ?: return 0
        val wholeYear = goal.goals[year]
        val numberMonths = Period(startDate, endDate, PeriodType.months()).months
        if (wholeYear == null) return 0
        return (wholeYear * (numberMonths / 12.0)).toInt()
    }
}
