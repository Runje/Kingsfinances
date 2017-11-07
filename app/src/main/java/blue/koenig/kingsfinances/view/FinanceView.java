package blue.koenig.kingsfinances.view;

import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.Expenses;

import java.util.List;

import blue.koenig.kingsfamilylibrary.view.family.FamilyView;

/**
 * Created by Thomas on 18.10.2017.
 */

public interface FinanceView extends FamilyView {

    void showExpenses(List<Expenses> expenses);

    void setFamilyMembers(List<User> members);
}