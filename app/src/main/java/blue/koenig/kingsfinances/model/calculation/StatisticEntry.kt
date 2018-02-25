package blue.koenig.kingsfinances.model.calculation

import blue.koenig.kingsfinances.model.calculation.StatisticEntry.Companion.bytesToEntryMap
import com.koenig.commonModel.Byteable
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.CostDistribution
import com.koenig.commonModel.writeBytes
import com.koenig.commonModel.yearMonth
import org.joda.time.DateTime
import org.joda.time.YearMonth
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by Thomas on 18.02.2018.
 */
interface StatisticEntry : Byteable {
    val entryMap: Map<User, Int>
    val dateLength: Int
    val sum: Int
        get() = entryMap.values.sum()

    override val byteLength: Int
        get() = dateLength + getEntryMapLength(entryMap)

    override fun writeBytes(buffer: ByteBuffer) {
        writeDate(buffer)
        buffer.put(entryMapToBytes(entryMap))
    }

    fun writeDate(buffer: ByteBuffer)

    fun addEntryMap(otherMap: Map<User, Int>): Map<User, Int> {
        val map = entryMap.toMutableMap()
        for ((user, integer) in otherMap) {
            map[user] = (map[user] ?: 0) + integer
        }
        return map
    }

    fun subtractEntryMap(otherMap: Map<User, Int>): Map<User, Int> {
        val map = entryMap.toMutableMap()
        for ((user, integer) in otherMap) {
            map[user] = (map[user] ?: 0) - integer
        }
        return map
    }

    operator fun get(user: User): Int {
        return entryMap[user] ?: 0
    }

    companion object {

        fun entryMapToBytes(map: Map<User, Int>?): ByteArray {
            val buffer = ByteBuffer.allocate(getEntryMapLength(map).toInt())
            buffer.putShort(map!!.size.toShort())
            for ((user, integer) in map) {
                user.writeBytes(buffer)
                buffer.putInt(integer)
            }

            return buffer.array()
        }

        fun bytesToEntryMap(buffer: ByteBuffer): HashMap<User, Int> {
            val size = buffer.short
            val result = HashMap<User, Int>(size.toInt())
            for (i in 0 until size) {
                val user = User(buffer)
                val integer = buffer.int
                result[user] = integer
            }

            return result
        }

        private fun getEntryMapLength(map: Map<User, Int>?): Short {
            var size = 2
            for ((user) in map!!) {
                size += user.byteLength + 4
            }
            return size.toShort()
        }
    }
}

data class MonthStatistic(val month: YearMonth, override val entryMap: Map<User, Int> = mapOf()) : StatisticEntry {
    override val dateLength: Int = 3

    constructor(buffer: ByteBuffer) : this(buffer.yearMonth, bytesToEntryMap(buffer))

    override fun writeDate(buffer: ByteBuffer) {
        month.writeBytes(buffer)
    }

    operator fun plus(other: MonthStatistic): MonthStatistic {
        return MonthStatistic(month, addEntryMap(other.entryMap))
    }

    operator fun minus(other: MonthStatistic): MonthStatistic {
        return MonthStatistic(month, subtractEntryMap(other.entryMap))
    }

    companion object {
        fun fromCostDistributionTakeTheory(month: YearMonth, distribution: CostDistribution, negative: Boolean = false): MonthStatistic {
            val entryMap = mutableMapOf<User, Int>()
            for ((user, costs) in distribution.getDistribution()) {
                entryMap[user] = if (negative) -costs.Theory else costs.Theory
            }

            return MonthStatistic(month, entryMap)
        }
    }

    fun withMonth(month: YearMonth): MonthStatistic {
        return MonthStatistic(month, entryMap)
    }


}

val DateTime.yearMonth: YearMonth
    get() = YearMonth(year, monthOfYear)

val YearMonth.next: YearMonth
    get() {
        val month = monthOfYear + 1
        if (month == 13) return YearMonth(year + 1, 1)

        return YearMonth(year, month)
    }

/**
 * Including from and to
 */
fun yearMonthRange(from: YearMonth, to: YearMonth): List<YearMonth> {
    val result = mutableListOf<YearMonth>()
    var month = from
    while (month <= to) {
        result.add(month)
        month = month.plusMonths(1)
    }

    return result
}


