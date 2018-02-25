package blue.koenig.kingsfinances.view;

import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.BankAccount;
import com.koenig.commonModel.finance.StandingOrder;

import java.util.List;

import blue.koenig.kingsfamilylibrary.view.family.FamilyView;
import blue.koenig.kingsfinances.model.calculation.StatisticEntryDeprecated;

/**
 * Created by Thomas on 18.10.2017.
 */

public interface FinanceView extends FamilyView {

    void setFamilyMembers(List<User> members);

    void showStandingOrders(List<StandingOrder> standingOrders);

    void updateBankAccounts(List<BankAccount> bankAccounts);

    void updateAssets(List<StatisticEntryDeprecated> assets);

}
