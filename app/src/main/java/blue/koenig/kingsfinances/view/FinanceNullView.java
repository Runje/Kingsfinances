package blue.koenig.kingsfinances.view;

import android.support.annotation.StringRes;

import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.Expenses;

import java.util.List;

import blue.koenig.kingsfamilylibrary.view.family.FamilyView;

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
}
