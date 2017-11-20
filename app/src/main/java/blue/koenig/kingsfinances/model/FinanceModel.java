package blue.koenig.kingsfinances.model;

import android.content.Context;

import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.Expenses;
import com.koenig.communication.messages.AUDMessage;
import com.koenig.communication.messages.FamilyMessage;
import com.koenig.communication.messages.TextMessage;
import com.koenig.communication.messages.family.FamilyTextMessages;
import com.koenig.communication.messages.finance.CategorysMessage;
import com.koenig.communication.messages.finance.ExpensesMessage;
import com.koenig.communication.messages.finance.FinanceTextMessages;
import blue.koenig.kingsfamilylibrary.view.family.LoginHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

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

    private CategoryService categoryService;

    public FinanceModel(ServerConnection connection, Context context, LoginHandler handler) {
        super(connection, context, handler);
        categoryService = new FinanceCategoryService();
    }

    @Override
    public void start() {
        logger.info("Start");
        List<User> members = loginHandler.getMembers();
        if (members != null) {
            updateFamilymembers(members);
        }

        connection.sendFamilyMessage(FinanceTextMessages.getAllExpensesMessage());
    }

    @Override
    protected void updateFamilymembers(List<User> members) {
        logger.info("Setting family members...");
        getFinanceView().setFamilyMembers(members);
    }



    @Override
    public void onReceiveFinanceMessage(FamilyMessage message) {
            switch (message.getName()) {
                case ExpensesMessage.NAME:
                    ExpensesMessage expensesMessage = (ExpensesMessage) message;
                    getFinanceView().showExpenses(expensesMessage.getExpenses());
                    // update categorys
                    connection.sendFamilyMessage(FinanceTextMessages.getAllCategorysMessage());
                    break;
                case CategorysMessage.NAME:
                    CategorysMessage categorysMessage = (CategorysMessage) message;
                    categoryService.update(categorysMessage.getCategorys());

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

    public void deleteExpenses(Expenses expenses) {
        logger.info("Deleting expenses: " + expenses.getName());
        connection.sendFamilyMessage(AUDMessage.createDelete(expenses));
        // TODO: update expenses!
        // TODO: Transaction should have an id and then update on update on return success transaction id
    }

    public void updateExpenses(Expenses expenses) {
        logger.info("Updating expenses: " + expenses.getName());
        connection.sendFamilyMessage(AUDMessage.createUpdate(expenses));
    }

    public void editExpenses(Expenses expenses) {
        connection.sendFamilyMessage(AUDMessage.createUpdate(expenses));
    }

    public CategoryService getCategoryService() {
        return categoryService;
    }

    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
}
