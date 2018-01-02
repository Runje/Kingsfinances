package blue.koenig.kingsfinances.view;

import android.support.annotation.StringRes;

import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.BankAccount;
import com.koenig.commonModel.finance.Expenses;
import com.koenig.commonModel.finance.StandingOrder;

import java.util.List;

import blue.koenig.kingsfinances.model.calculation.Debts;

/**
 * Created by Thomas on 19.10.2017.
 */

public class FinanceNullView implements FinanceView {
    @Override
    public void askForNameOrImport() {

    }

    @Override
    public void askJoinOrCreateFamily() {

    }

    @Override
    public void showText(String text) {

    }

    @Override
    public void showText(@StringRes int stringResource) {

    }

    @Override
    public void showConnectionStatus(boolean connected) {

    }

    @Override
    public void showExpenses(List<Expenses> expenses) {

    }

    @Override
    public void setFamilyMembers(List<User> members) {

    }

    @Override
    public void showStandingOrders(List<StandingOrder> standingOrders) {

    }

    @Override
    public void updateBankAccounts(List<BankAccount> bankAccounts) {

    }

    @Override
    public void updateDebts(List<Debts> debts) {

    }
}
