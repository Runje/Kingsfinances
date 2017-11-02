package blue.koenig.kingsfinances.model;

import android.content.Context;

import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.Expenses;
import com.koenig.communication.messages.FamilyMessage;
import com.koenig.communication.messages.TextMessage;
import com.koenig.communication.messages.family.FamilyTextMessages;
import com.koenig.communication.messages.finance.ExpensesMessage;
import com.koenig.communication.messages.finance.FinanceTextMessages;
import blue.koenig.kingsfamilylibrary.view.family.LoginHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import blue.koenig.kingsfamilylibrary.model.communication.ServerConnection;
import blue.koenig.kingsfamilylibrary.model.family.FamilyModel;
import blue.koenig.kingsfamilylibrary.model.family.Plugin;
import blue.koenig.kingsfamilylibrary.view.family.FamilyView;
import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.view.FinanceNullView;
import blue.koenig.kingsfinances.view.FinanceView;

/**
 * Created by Thomas on 18.10.2017.
 */

public class FinanceModel extends FamilyModel {

    @Inject
    public FinanceModel(ServerConnection connection, Context context, LoginHandler handler) {
        super(connection, context, handler);
    }

    @Override
    public void start() {
        logger.info("Start");
        connection.sendFamilyMessage(FinanceTextMessages.getAllExpensesMessage());
    }

    @Override
    protected void updateFamilymembers(List<User> members) {
        getFinanceView().setFamilyMembers(members);
    }



    @Override
    public void onReceiveFinanceMessage(FamilyMessage message) {
            switch (message.getName()) {
                case ExpensesMessage.NAME:
                    ExpensesMessage expensesMessage = (ExpensesMessage) message;
                    getFinanceView().showExpenses(expensesMessage.getExpenses());
                    break;
                default:
                    logger.error("Unknown Message: " + message.getName());
            }
    }

    private FinanceView getFinanceView() {
        return (FinanceView) view;
    }

    public void onTabSelected(int position) {
        logger.info("OnTabSelected: " + position);
    }

    @Override
    protected void processFinanceCommand(String[] words) {
        switch (words[0]) {
            case FinanceTextMessages.GET_ALL_EXPENSES_FAIL:
                view.showText(R.string.getExpensesFail);
                break;
            default:
                logger.info("Unknown command");
                view.showText(words[0]);
        }
    }

    public FamilyView createNullView() {
        return new FinanceNullView();
    }
}
