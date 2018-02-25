package blue.koenig.kingsfinances.model.calculation

import blue.koenig.kingsfinances.model.FinanceConfig
import com.koenig.commonModel.Byteable
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by Thomas on 09.01.2018.
 */

class FinanceStatisticsCalculatorService(private val config: FinanceConfig, private val key: String) : StatisticsCalculatorService {

    override fun getSavedSortedStatistics(): List<StatisticEntryDeprecated> {
        val buffer = config.loadBuffer(key) ?: return ArrayList()

        val size = buffer.int
        val debts = ArrayList<StatisticEntryDeprecated>(size)
        for (i in 0 until size) {
            debts.add(StatisticEntryDeprecated(buffer))
        }

        return debts
    }

    override fun saveStatistics(statisticEntryList: List<StatisticEntryDeprecated>) {
        val buffer = ByteBuffer.allocate(Byteable.getBigListLength(statisticEntryList))
        Byteable.writeBigList(statisticEntryList, buffer)
        config.saveBuffer(buffer, key)
    }
}
