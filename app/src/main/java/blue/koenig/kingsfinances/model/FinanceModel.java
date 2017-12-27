package blue.koenig.kingsfinances.model;

import android.content.Context;

import com.koenig.commonModel.Category;
import com.koenig.commonModel.Component;
import com.koenig.commonModel.Item;
import com.koenig.commonModel.ItemType;
import com.koenig.commonModel.Operation;
import com.koenig.commonModel.Operator;
import com.koenig.commonModel.User;
import com.koenig.commonModel.database.DatabaseItem;
import com.koenig.commonModel.finance.Balance;
import com.koenig.commonModel.finance.BankAccount;
import com.koenig.commonModel.finance.Expenses;
import com.koenig.commonModel.finance.StandingOrder;
import com.koenig.communication.messages.AUDMessage;
import com.koenig.communication.messages.AskForUpdatesMessage;
import com.koenig.communication.messages.FamilyMessage;
import com.koenig.communication.messages.UpdatesMessage;
import com.koenig.communication.messages.finance.FinanceTextMessages;

import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import blue.koenig.kingsfamilylibrary.model.FamilyConfig;
import blue.koenig.kingsfamilylibrary.model.communication.ServerConnection;
import blue.koenig.kingsfamilylibrary.model.family.FamilyModel;
import blue.koenig.kingsfamilylibrary.view.family.FamilyView;
import blue.koenig.kingsfamilylibrary.view.family.LoginHandler;
import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.model.database.FinanceDatabase;
import blue.koenig.kingsfinances.view.FinanceNullView;
import blue.koenig.kingsfinances.view.FinanceView;
import blue.koenig.kingsfinances.view.NullPendingView;
import blue.koenig.kingsfinances.view.PendingView;

/**
 * Created by Thomas on 18.10.2017.
 */

public class FinanceModel extends FamilyModel implements FinanceCategoryService.CategoryServiceListener {

    private final FinanceUserService userService;
    private FinanceDatabase database;
    private FinanceCategoryService categoryService;
    private PendingView pendingView;
    private int succesMessages;

    public FinanceModel(ServerConnection connection, Context context, LoginHandler handler) {
        super(connection, context, handler);
        pendingView = new NullPendingView();
        categoryService = new FinanceCategoryService();
        categoryService.setListener(this);
        userService = new FinanceUserService(loginHandler.getMembers());

        try {
            database = new FinanceDatabase(context, userService);
            categoryService.update(database.getAllCategorys());
        } catch (SQLException e) {
            logger.error("Couldn't create database: " + e.getMessage());
        }
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
            case BANKACCOUNT:
                database.updateBankAccountChanges(items);
                break;
        }
    }

    @Override
    public void start() {
        logger.info("Start");
        List<User> members = loginHandler.getMembers();
        if (members != null) {
            updateFamilymembers(members);
        }



        List<PendingOperation> operations = database.getNonConfiremdPendingOperations();
        clearAUDSuccesMessages();
        for (PendingOperation operation : operations) {
            connection.sendFamilyMessage(new AUDMessage(Component.FINANCE,operation.getOperation()));
        }

        // ask for update after sent my changes!
        waitForSuccessMessages(operations.size());
        askForAllUpdates();
    }

    private void waitForSuccessMessages(int size) {
        int i = 0;
        int timeOutInS = 2;
        int intervall = 100;
        try {
            while (i < 1000 * timeOutInS / intervall) {
            if (succesMessages >= size) return;
            Thread.sleep(intervall);
            i++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void clearAUDSuccesMessages() {
        succesMessages = 0;
    }

    private void askForAllUpdates() {
        askForUpdates(ItemType.EXPENSES);
        askForUpdates(ItemType.CATEGORY);
        askForUpdates(ItemType.STANDING_ORDER);
        askForUpdates(ItemType.BANKACCOUNT);
    }

    private void askForUpdates(ItemType itemType) {
        sendMessageToServer(new AskForUpdatesMessage(Component.FINANCE, FamilyConfig.getLastSyncDate(context, itemType.name()) , itemType));
    }

    @Override
    protected void updateFamilymembers(List<User> members) {
        logger.info("Setting family members...");
        getFinanceView().setFamilyMembers(members);
        userService.setUser(members);
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
                case BANKACCOUNT:
                    updateAllBankAccounts();
                    break;
            }

            FamilyConfig.saveLastSyncDate(DateTime.now(), context, itemType.name());

        } catch (SQLException ex) {
            logger.error("Error while updating: " + ex.getMessage());
        }
    }

    private void updateAllCategorys() {
        try {
            List<Category> categories = database.getAllCategorys();
            categoryService.update(categories);
        } catch (SQLException e) {
            logger.error("Error getting categoires: " + e.getMessage());
        }
    }

    private void updateAllStandingOrders() {
        try {
            List<StandingOrder> standingOrders = database.getAllStandingOrders();
            getFinanceView().showStandingOrders(standingOrders);
        } catch (SQLException e) {
            logger.error("Error getting categoires: " + e.getMessage());
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
                    succesMessages++;
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

    private void updateAllBankAccounts() throws SQLException {
        getFinanceView().updateBankAccounts(database.getAllBankAccounts());
    }

    public List<PendingOperation> getPendingOperations() {
        try {
            return database.getAllPendingOperation();
        } catch (SQLException e) {
            logger.error("Couldn't get pending operations: " + e.getMessage());
        }

        return new ArrayList<>();
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

    public List<Expenses> getExpenses() {
        try {
            return database.getAllExpenses();
        } catch (SQLException e) {
            logger.error("Couldn't get expenses: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    @Override
    public void addMainCategory(String newCategory) {
        try {
            Category category = new Category(newCategory);
            makeAddOperation(category);
            database.addCategory(category);
        } catch (SQLException e) {
            logger.error("Error adding new Category");
        }
    }

    @Override
    public void addSubCategory(String mainCategory, String newCategory) {
            try {
                Category category = database.getCategory(mainCategory);
                if (category != null) {
                    category.addSub(newCategory);
                    makeUpdateOperation(category);
                    database.updateCategory(category);
                } else {
                    logger.error("Couldn't find main category to add subcategory: " + mainCategory);
                }

            } catch (SQLException e) {
                logger.error("Error adding new subcategory");
            }
    }

    public List<StandingOrder> getStandingOrders()  {
        try {
            return database.getAllStandingOrders();
        } catch (SQLException e) {
            logger.error("Error getting standing orders");
            return new ArrayList<>();
        }
    }

    public void addStandingOrder(StandingOrder standingOrder) {
        try {
            makeAddOperation(standingOrder);
            database.addStandingOrder(standingOrder);
            updateAllStandingOrders();
        } catch (SQLException e) {
            logger.error("Error while adding standingOrder: " + e.getMessage());
        }
    }

    public void editStandingOrder(StandingOrder standingOrder) {
        makeUpdateOperation(standingOrder);
        try {
            database.updateStandingOrder(standingOrder);
            updateAllStandingOrders();
        } catch (SQLException e) {
            logger.error("Error while updating standingOrder: " + e.getMessage());
        }
    }

    public void deleteStandingOrder(StandingOrder standingOrder) {
        makeDeleteOperation(standingOrder);
        try {
            database.deleteStandingOrder(standingOrder);
            updateAllStandingOrders();
        } catch (SQLException e) {
            logger.error("Error while deleting standingOrder");
        }
    }

    public List<BankAccount> getBankAccounts() {
        try {
            return database.getAllBankAccounts();
        } catch (SQLException e) {
            logger.error("Couldn't get bankaccounts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void deleteBankAccount(BankAccount account) {
        makeDeleteOperation(account);
        try {
            database.deleteBankAccount(account);
            updateAllBankAccounts();
        } catch (SQLException e) {
            logger.error("Error while deleting standingOrder");
        }
    }


    public void deleteBalance(BankAccount account, Balance balance) {
        makeUpdateOperation(account);
        try {
            database.deleteBalance(account, balance);
        } catch (SQLException e) {
            logger.error("Couldn't delete balance: " + e.getMessage());
        }
    }

    public void addBalance(BankAccount account, Balance balance) {
        makeUpdateOperation(account);
        try {
            database.addBalance(account, balance);

        } catch (SQLException e) {
            logger.error("Couldn't add balance: " + e.getMessage());
        }
    }

    public void addBankAccount(BankAccount bankAccount) {
        makeAddOperation(bankAccount);
        try {
            database.addBankAccount(bankAccount);
        } catch (SQLException e) {
            logger.error("Couldn't add bankAccount: " + e.getMessage());
        }
    }
}
