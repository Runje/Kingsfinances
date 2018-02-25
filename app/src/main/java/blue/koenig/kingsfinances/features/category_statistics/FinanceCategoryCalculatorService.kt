package blue.koenig.kingsfinances.features.category_statistics

import blue.koenig.kingsfinances.model.FinanceConfig
import blue.koenig.kingsfinances.model.calculation.MonthStatistic
import blue.koenig.kingsfinances.model.calculation.yearMonth
import blue.koenig.kingsfinances.model.database.GoalTable
import com.koenig.commonModel.Byteable
import org.joda.time.DateTime
import org.joda.time.YearMonth
import org.joda.time.Years
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by Thomas on 20.01.2018.
 */

class FinanceCategoryCalculatorService(private val config: FinanceConfig, private val table: GoalTable) : CategoryCalculatorService {
    private val delta_key = "DELTA_CATEGORY_STATISTICS"
    private val absolute_key = "ABSOLUTE_CATEGORY_STATISTICS"

    override val deltaCategoryMap: MutableMap<String, MutableMap<YearMonth, MonthStatistic>>
        get() = loadMap(delta_key)

    override val absoluteCategoryMap: MutableMap<String, MutableMap<YearMonth, MonthStatistic>>
        get() = loadMap(absolute_key)


    private fun loadMap(key: String): MutableMap<String, MutableMap<YearMonth, MonthStatistic>> {
        val buffer = config.loadBuffer(key) ?: return HashMap()

        val size = buffer.int
        val listMap = mutableMapOf<String, MutableMap<YearMonth, MonthStatistic>>()
        for (i in 0 until size) {
            val category = Byteable.byteToString(buffer)
            val entries = buffer.int
            val statistics = mutableMapOf<YearMonth, MonthStatistic>()
            for (j in 0 until entries) {
                val entry = MonthStatistic(buffer)
                statistics[entry.month] = entry
            }

            listMap[category] = statistics
        }

        return listMap
    }

    override fun saveStatistics(deltaCategoryMap: Map<String, Map<YearMonth, MonthStatistic>>, absoluteCategoryMap: MutableMap<String, MutableMap<YearMonth, MonthStatistic>>) {
        saveMap(delta_key, deltaCategoryMap)
        saveMap(absolute_key, absoluteCategoryMap)
    }

    private fun saveMap(key: String, map: Map<String, Map<YearMonth, MonthStatistic>>) {
        var size = 4
        for ((category, sMap) in map) {
            size += Byteable.getStringLength(category)
            size += Byteable.getBigListLength(sMap.values)
        }

        val buffer = ByteBuffer.allocate(size)
        buffer.putInt(map.size)
        for ((category, sMap) in map) {
            Byteable.writeString(category, buffer)
            Byteable.writeBigList(sMap.values, buffer)
        }

        config.saveBuffer(buffer, key)
    }

    override val overallString: String
        get() = config.overallString
    override val startDate: DateTime
        get() = config.startDate

    override fun getGoalFor(category: String, month: YearMonth): Double {
        //ask database for value of year
        val goal = table.getFromName(category) ?: return 0.0
        val wholeYear = goal.goals[month.year] ?: return 0.0
        return (wholeYear * 12.0)
    }

    override fun getGoalFor(category: String, year: Years): Int {
        val goal = table.getFromName(category) ?: return 0
        return goal.goals[year.years] ?: 0
    }

    override val endDate: YearMonth
        get() = DateTime.now().yearMonth

}
