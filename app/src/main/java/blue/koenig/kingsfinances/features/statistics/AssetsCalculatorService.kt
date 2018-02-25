package blue.koenig.kingsfinances.features.statistics

import blue.koenig.kingsfinances.model.calculation.StatisticEntryDeprecated
import com.koenig.commonModel.finance.BankAccount
import org.joda.time.DateTime

/**
 * Created by Thomas on 05.01.2018.
 */

interface AssetsCalculatorService {

    val startDate: DateTime

    val endDate: DateTime

    val overallString: String

    val futureString: String
    fun loadAllBankAccountStatistics(): Map<BankAccount, List<StatisticEntryDeprecated>>

    fun save(statisticEntryLists: Map<BankAccount, List<StatisticEntryDeprecated>>)
}
