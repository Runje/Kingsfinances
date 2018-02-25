package blue.koenig.kingsfinances.features.statistics

import blue.koenig.kingsfinances.model.FinanceConfig
import blue.koenig.kingsfinances.model.calculation.StatisticEntryDeprecated
import com.koenig.commonModel.Byteable
import com.koenig.commonModel.finance.BankAccount
import org.joda.time.DateTime
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by Thomas on 07.01.2018.
 */

class FinanceAssetsCalculatorService(private val config: FinanceConfig) : AssetsCalculatorService {
    override val overallString: String
        get() = config.overallString

    override val futureString: String
        get() = config.futureString
    override lateinit var startDate: DateTime
    override val endDate: DateTime = DateTime.now()


    init {
        config.startDateObservable.subscribe { startDate = it }
    }

    override fun loadAllBankAccountStatistics(): Map<BankAccount, List<StatisticEntryDeprecated>> {
        val buffer = config.loadBuffer(ASSETS) ?: return HashMap()

        val size = buffer.int
        val listMap = HashMap<BankAccount, List<StatisticEntryDeprecated>>(size)
        for (i in 0 until size) {
            val bankAccount = BankAccount(buffer)
            val entries = buffer.int
            val statistics = ArrayList<StatisticEntryDeprecated>(entries)
            for (j in 0 until entries) {
                statistics.add(StatisticEntryDeprecated(buffer))
            }

            listMap.put(bankAccount, statistics)
        }

        return listMap
    }


    override fun save(statisticEntryLists: Map<BankAccount, List<StatisticEntryDeprecated>>) {
        var size = 4
        for (bankAccount in statisticEntryLists.keys) {
            size += bankAccount.byteLength
            size += Byteable.getBigListLength(statisticEntryLists[bankAccount]!!)
        }

        val buffer = ByteBuffer.allocate(size)
        buffer.putInt(statisticEntryLists.size)
        for (bankAccount in statisticEntryLists.keys) {
            bankAccount.writeBytes(buffer)
            Byteable.writeBigList(statisticEntryLists[bankAccount]!!, buffer)
        }

        config.saveBuffer(buffer, ASSETS)
    }


    companion object {
        private val ASSETS = "ASSETS"
    }
}
