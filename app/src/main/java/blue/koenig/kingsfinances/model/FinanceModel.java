package blue.koenig.kingsfinances.model;

import android.content.Context;
import android.provider.ContactsContract;

import com.koenig.commonModel.Component;
import com.koenig.commonModel.Item;
import com.koenig.commonModel.ItemType;
import com.koenig.commonModel.Operation;
import com.koenig.commonModel.Operator;
import com.koenig.commonModel.User;
import com.koenig.commonModel.database.DatabaseItem;
import com.koenig.commonModel.finance.Expenses;
import com.koenig.communication.messages.AUDMessage;
import com.koenig.communication.messages.AskForUpdatesMessage;
import com.koenig.communication.messages.FamilyMessage;
import com.koenig.communication.messages.UpdatesMessage;
import com.koenig.communication.messages.finance.FinanceTextMessages;

import org.joda.time.DateTime;

import blue.koenig.kingsfamilylibrary.model.FamilyConfig;
import blue.koenig.kingsfamilylibrary.view.family.LoginHandler;

import java.sql.SQLException;
import java.util.List;

import blue.koenig.kingsfamilylibrary.model.communication.ServerConnection;
import blue.koenig.kingsfamilylibrary.model.family.FamilyModel;
import blue.koenig.kingsfamilylibrary.view.family.FamilyView;
import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.model.database.FinanceDatabase;
import blue.koenig.kingsfinances.view.FinanceNullView;
import blue.koenig.kingsfinances.view.FinanceView;
import blue.koenig.kingsfinances.view.NullPendingView;
import blue.koenig.kingsfinances.view.PendingView;

/**
 * Created by Thomas on 18.10.2017.
 */

public class FinanceModel extends FamilyModel {

    private FinanceDatabase database;
    private CategoryService categoryService;
    private PendingView pendingView;

    public FinanceModel(ServerConnection connection, Context context, LoginHandler handler) {
        super(connection, context, handler);
        pendingView = new NullPendingView();
        categoryService = new FinanceCategoryService();
        try {
            database = new FinanceDatabase(context);
        } catch (SQLException e) {
            logger.error("Couldn't create database: " + e.getMessage());
        }
    }

    @Override
    public void start() {
        logger.info("Start");
        List<User> members = loginHandler.getMembers();
        if (members != null) {
            updateFamilymembers(members);
        }

        askForAllUpdates();

        List<PendingOperation> operations = database.getNonConfiremdPendingOperations();
        for (PendingOperation operation : operations) {
            connection.sendFamilyMessage(new AUDMessage(Component.FINANCE,operation.getOperation()));
        }
    }

    private void askForAllUpdates() {
        askForUpdates(ItemType.EXPENSES);
    }

    private void askForUpdates(ItemType itemType) {
        sendMessageToServer(new AskForUpdatesMessage(Component.FINANCE, FamilyConfig.getLastSyncDate(context, itemType.name()) , itemType));
    }

    @Override
    protected void updateFamilymembers(List<User> members) {
        logger.info("Setting family members...");
        getFinanceView().setFamilyMembers(members);
    }



    @Override
    public void onReceiveFinanceMessage(FamilyMessage message) {
            switch (message.getName()) {
                case UpdatesMessage.NAME:
                    UpdatesMessage updatesMessage = (UpdatesMessage) message;
                    update(updatesMessage.getItems());
                    break;
                default:
                    logger.error("Unknown Message: " + message.getName());
            }
    }

    private void update(List<DatabaseItem> items) {
        try {
            update(database, items);
            if (items.size() == 0) return;
            ItemType itemType = ItemType.fromItem(items.get(0).getItem());

            switch (itemType) {
                case EXPENSES:
                    updateAllExpenses();
                    break;
                case STANDING_ORDER:
                    updateAllStandingOrders();
                    break;
                case CATEGORY:
                    updateAllCategorys();
                    break;
            }

            FamilyConfig.saveLastSyncDate(DateTime.now(), context, itemType.name());

        } catch (SQLException ex) {
            logger.error("Error while updating: " + ex.getMessage());
        }
    }

    private void updateAllCategorys() {
        // TODO:
    }

    private void updateAllStandingOrders() {
        // TODO:
    }

    public static void update(FinanceDatabase database, List<DatabaseItem> items) throws SQLException {
        if (items.size() == 0) return;
        ItemType itemType = ItemType.fromItem(items.get(0).getItem());

        switch (itemType) {
            case EXPENSES:
                database.updateExpensesChanges(items);

                break;
            case STANDING_ORDER:
                database.updateStandingOrderChanges(items);
                break;
            case CATEGORY:
                database.updateCategoryChanges(items);
                break;
        }
    }


    private FinanceView getFinanceView() {
        return (FinanceView) view;
    }

    public void onTabSelected(int position) {
        logger.info("OnTabSelected: " + position);
        if (position == 1) {
            pendingView.update(getPendingOperations());
        }
    }

    @Override
    protected void processFinanceCommand(String[] words) {
        try {

            switch (words[0]) {
                case FinanceTextMessages.GET_ALL_EXPENSES_FAIL:
                    view.showText(R.string.getExpensesFail);
                    break;
                case FinanceTextMessages.AUD_SUCCESS:
                    database.setPendingOperation(PendingStatus.CONFIRMED, words[1]);
                    pendingView.update(getPendingOperations());
                    break;
                case FinanceTextMessages.AUD_FAIL:
                    database.setPendingOperation(PendingStatus.ERROR, words[1]);
                    pendingView.update(getPendingOperations());
                    break;

                default:
                    logger.info("Unknown command");
                    view.showText(words[0]);
            }
        } catch (Exception e) {
            logger.error("Error while processing finance command: " + e.getMessage());
        }
    }

    public FamilyView createNullView() {
        return new FinanceNullView();
    }

    public void deleteExpenses(Expenses expenses) {
        makeDeleteOperation(expenses);
        try {
            database.deleteExpenses(expenses);
            updateAllExpenses();
        } catch (SQLException e) {
            logger.error("Error while deleting expenses");
        }
    }

    public void updateExpenses(Expenses expenses) {
        // TODO: warum wird es nicht aufgerufen?
        logger.info("Updating expenses: " + expenses.getName());
        makeUpdateOperation(expenses);
        try {
            database.updateExpenses(expenses);
            updateAllExpenses();
        } catch (SQLException e) {
            logger.error("Error while updating expenses: " + e.getMessage());
        }
    }

    private void makeUpdateOperation(Item item) {
        makeOperation(Operator.UPDATE, item);
    }

    private void makeAddOperation(Item item) {
        makeOperation(Operator.ADD, item);
    }

    private void makeDeleteOperation(Item item) {
        makeOperation(Operator.DELETE, item);
    }
    private void makeOperation(Operator operator, Item item) {
        Operation operation = new Operation(operator, item);
        try {
            database.addPendingOperation(new PendingOperation(operation, PendingStatus.PENDING, DateTime.now()));
        } catch (SQLException e) {
            logger.error("Error on operation: " + e.getMessage());
        }

        connection.sendFamilyMessage(new AUDMessage(Component.FINANCE, operation));
        pendingView.update(getPendingOperations());
    }

    public void editExpenses(Expenses expenses) {
        makeUpdateOperation(expenses);
        try {
            database.updateExpenses(expenses);
            getFinanceView().showExpenses(database.getAllExpenses());
        } catch (SQLException e) {
            logger.error("Error while updating expenses: " + e.getMessage());
        }
    }

    public CategoryService getCategoryService() {
        return categoryService;
    }

    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void addExpenses(Expenses expenses) {
        try {
            makeAddOperation(expenses);
            database.addExpenses(expenses);
            updateAllExpenses();
        } catch (SQLException e) {
            logger.error("Error while adding expenses: " + e.getMessage());
        }
    }

    private void updateAllExpenses() throws SQLException {
        getFinanceView().showExpenses(database.getAllExpenses());
    }

    public List<PendingOperation> getPendingOperations() {
        try {
            return database.getAllPendingOperation();
        } catch (SQLException e) {
            logger.error("Couldn't get pending operations: " + e.getMessage());
        }

        return null;
    }

    public void deletePending(PendingOperation operation) {
        try {
            database.deletePendingOperation(operation.getId());
            // TODO: revert database(memorize last state) or ask database for old state
            pendingView.update(getPendingOperations());
        } catch (SQLException e) {
            logger.error("Exception deleting operation: " + e.getMessage());
        }
    }

    public void sendPending(PendingOperation pendingOperation) {
        connection.sendFamilyMessage(new AUDMessage(Component.FINANCE, pendingOperation.getOperation()));
    }

    public void attachPendingView(PendingView pendingFragment) {
        this.pendingView = pendingFragment;
    }

    public void detachPendingView() {
        this.pendingView = new NullPendingView();
    }
}
