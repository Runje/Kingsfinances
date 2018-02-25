package blue.koenig.kingsfinances.model.calculation

import android.util.Log
import com.koenig.FamilyConstants
import com.koenig.commonModel.Byteable
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.Balance
import com.koenig.commonModel.finance.CostDistribution
import org.joda.time.DateTime
import org.joda.time.YearMonth
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by Thomas on 28.12.2017.
 */

class StatisticEntryDeprecated : Byteable {
    lateinit var date: DateTime
    /**
     * Negative value means debts and positive means credit
     */
    var entryMap: MutableMap<User, Int>

    override val byteLength: Int
        get() = Byteable.Companion.dateLength + getEntryMapLength(entryMap)

    val sum: Int
        get() {
            var sum = 0
            for (user in entryMap.keys) {
                if (user == FamilyConstants.ALL_USER) {
                    Log.d("Statistic Entry", "Summing with ALL_USER!!!")
                }
                sum += entryMap[user]!!
            }

            return sum
        }

    constructor(buffer: ByteBuffer) {
        this.date = Byteable.Companion.byteToDateTime(buffer)
        this.entryMap = bytesToEntryMap(buffer)
    }

    constructor() {
        this.entryMap = HashMap()
    }

    constructor(statisticEntry: StatisticEntryDeprecated) {
        date = statisticEntry.date
        entryMap = HashMap(statisticEntry.entryMap)
    }

    constructor(date: DateTime, costDistribution: CostDistribution) : this(date) {
        addCostDistribution(costDistribution)
    }

    constructor(date: DateTime) : this() {
        this.date = date
    }

    constructor(date: DateTime, entryMap: Map<User, Int>) {
        this.date = date
        this.entryMap = HashMap(entryMap)
    }

    constructor(balance: Balance, users: List<User>) : this(balance.date) {
        // distribute equally
        val n = users.size
        var distributed = 0
        var value = balance.balance / n
        for (i in 0 until n) {
            val user = users[i]
            if (i == n - 1) {
                // last one gets the rest
                value = balance.balance - distributed
            }

            entryMap[user] = value
            distributed += value
        }
    }

    fun addCostDistribution(costDistribution: CostDistribution) {
        addCostDistribution(costDistribution, false)
    }

    fun subtractCostDistribution(costDistribution: CostDistribution) {
        addCostDistribution(costDistribution, true)
    }

    private fun addCostDistribution(costDistribution: CostDistribution, inverse: Boolean) {
        val distribution = costDistribution.getDistribution()
        for (user in distribution.keys) {
            val integer = entryMap[user]
            val oldDebts = integer ?: 0
            val costs = distribution[user]!!
            val newDebts = costs.Theory - costs.Real
            val debts = if (inverse) oldDebts - newDebts else oldDebts + newDebts
            entryMap[user] = debts
        }
    }

    private fun addTheoryCosts(costDistribution: CostDistribution, inverse: Boolean) {
        val distribution = costDistribution.getDistribution()
        for (user in distribution.keys) {
            val integer = entryMap[user]
            val oldDebts = integer ?: 0
            val costs = distribution[user]!!
            val newDebts = costs.Theory
            val debts = if (inverse) oldDebts - newDebts else oldDebts + newDebts
            entryMap[user] = debts
        }
    }

    fun getEntryFor(user: User): Int {
        val integer = entryMap[user]
        return integer ?: 0
    }

    fun getEntryForNullabe(user: User): Int? {
        return entryMap[user]
    }

    override fun writeBytes(buffer: ByteBuffer) {
        Byteable.Companion.writeDateTime(date, buffer)
        buffer.put(entryMapToBytes(entryMap))
    }

    fun addEntry(statisticEntryDelta: StatisticEntryDeprecated) {
        addEntry(statisticEntryDelta, false)
    }

    private fun addEntry(statisticEntryDelta: StatisticEntryDeprecated, inverse: Boolean) {
        for (user in statisticEntryDelta.entryMap.keys) {
            val integer = entryMap[user]
            val oldDebts = integer ?: 0
            val deltaDebts = statisticEntryDelta.entryMap[user] ?: 0
            val newDebts = if (inverse) oldDebts - deltaDebts else oldDebts + deltaDebts
            entryMap[user] = newDebts
        }
    }

    fun subtractEntry(statisticEntry: StatisticEntryDeprecated) {
        addEntry(statisticEntry, true)
    }

    fun subtractTheoryCosts(costDistribution: CostDistribution) {
        addTheoryCosts(costDistribution, true)
    }

    fun addTheoryCosts(costDistribution: CostDistribution) {
        addTheoryCosts(costDistribution, false)
    }

    fun putEntry(user: User, value: Int) {
        entryMap[user] = value
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

        fun fromTheoryCosts(dateTime: DateTime, costDistribution: CostDistribution): StatisticEntryDeprecated {
            val entry = StatisticEntryDeprecated(dateTime)
            entry.addTheoryCosts(costDistribution)
            return entry
        }
    }

    fun toMonthStatistic(): MonthStatistic {
        return MonthStatistic(date.toYearMonth(), entryMap)
    }
}

private fun DateTime.toYearMonth(): YearMonth {
    return YearMonth(year, monthOfYear)
}
