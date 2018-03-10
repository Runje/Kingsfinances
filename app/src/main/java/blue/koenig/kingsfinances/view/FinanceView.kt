package blue.koenig.kingsfinances.view

import blue.koenig.kingsfamilylibrary.view.family.FamilyView
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.BankAccount
import com.koenig.commonModel.finance.StandingOrder
import com.koenig.commonModel.finance.statistics.MonthStatistic
import org.joda.time.YearMonth

/**
 * Created by Thomas on 18.10.2017.
 */

interface FinanceView : FamilyView {

    fun setFamilyMembers(members: List<User>)

    fun showStandingOrders(standingOrders: List<StandingOrder>)

    fun updateBankAccounts(bankAccounts: List<BankAccount>)

    fun updateAssets(assets: Map<YearMonth, MonthStatistic>)

}
