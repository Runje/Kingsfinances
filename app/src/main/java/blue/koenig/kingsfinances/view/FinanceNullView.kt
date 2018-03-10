package blue.koenig.kingsfinances.view

import android.support.annotation.StringRes

import com.koenig.commonModel.User
import com.koenig.commonModel.finance.BankAccount
import com.koenig.commonModel.finance.StandingOrder
import com.koenig.commonModel.finance.statistics.MonthStatistic

import com.koenig.commonModel.finance.statistics.StatisticEntryDeprecated
import org.joda.time.YearMonth

/**
 * Created by Thomas on 19.10.2017.
 */

class FinanceNullView : FinanceView {
    override fun updateAssets(assets: Map<YearMonth, MonthStatistic>) {

    }

    override fun askForNameOrImport() {

    }

    override fun askJoinOrCreateFamily() {

    }

    override fun showText(text: String) {

    }

    override fun showText(@StringRes stringResource: Int) {

    }

    override fun showConnectionStatus(connected: Boolean) {

    }


    override fun setFamilyMembers(members: List<User>) {

    }

    override fun showStandingOrders(standingOrders: List<StandingOrder>) {

    }

    override fun updateBankAccounts(bankAccounts: List<BankAccount>) {

    }

    fun updateAssets(assets: List<StatisticEntryDeprecated>) {

    }

}
