package blue.koenig.kingsfinances.model;

import android.content.Context;
import android.graphics.Color;

import com.koenig.commonModel.Byteable;
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
import org.joda.time.Period;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blue.koenig.kingsfamilylibrary.model.FamilyConfig;
import blue.koenig.kingsfamilylibrary.model.communication.ServerConnection;
import blue.koenig.kingsfamilylibrary.model.family.FamilyModel;
import blue.koenig.kingsfamilylibrary.view.family.FamilyView;
import blue.koenig.kingsfamilylibrary.view.family.LoginHandler;
import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.model.calculation.AssetsCalculator;
import blue.koenig.kingsfinances.model.calculation.AssetsCalculatorService;
import blue.koenig.kingsfinances.model.calculation.DebtsCalculator;
import blue.koenig.kingsfinances.model.calculation.StatisticEntry;
import blue.koenig.kingsfinances.model.calculation.StatisticsCalculatorService;
import blue.koenig.kingsfinances.model.database.FinanceDatabase;
import blue.koenig.kingsfinances.view.FinanceNullView;
import blue.koenig.kingsfinances.view.FinanceView;
import blue.koenig.kingsfinances.view.NullPendingView;
import blue.koenig.kingsfinances.view.PendingView;

/**
 * Created by Thomas on 18.10.2017.
 */

public class FinanceModel extends FamilyModel implements FinanceCategoryService.CategoryServiceListener {

    private static final String DEBTS = "DEBTS";
    private static final String ASSETS = "ASSETS";
    private final FinanceUserService userService;
    private DebtsCalculator debtsCalculator;
    private FinanceDatabase database;
    private FinanceCategoryService categoryService;
    private PendingView pendingView;
    private int succesMessages;
    private List<Expenses> allExpenses;
    private AssetsCalculator assetsCalculator;

    public FinanceModel(ServerConnection connection, Context context, LoginHandler handler) {
        super(connection, context, handler);
        pendingView = new NullPendingView();
        categoryService = new FinanceCategoryService();
        categoryService.setListener(this);
        userService = new FinanceUserService(loginHandler.getMembers());

        try {
            database = new FinanceDatabase(context, userService);
            categoryService.update(database.getAllCategorys());
            debtsCalculator = new DebtsCalculator(Period.months(1), database.getExpensesTable(), new StatisticsCalculatorService() {
                @Override
                public List<StatisticEntry> getSavedSortedDebts() {
                    ByteBuffer buffer = FamilyConfig.getBytesFromConfig(context, DEBTS);
                    if (buffer == null) return new ArrayList<>();

                    int size = buffer.getInt();
                    List<StatisticEntry> debts = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        debts.add(new StatisticEntry(buffer));
                    }

                    return debts;
                }

                @Override
                public void saveDebts(List<StatisticEntry> statisticEntryList) {
                    ByteBuffer buffer = ByteBuffer.allocate(Byteable.getBigListLength(statisticEntryList));
                    Byteable.writeBigList(statisticEntryList, buffer);
                    FamilyConfig.saveBytes(context, buffer.array(), DEBTS);
                }
            });
            assetsCalculator = new AssetsCalculator(Period.months(1), database.getBankAccountTable(), new AssetsCalculatorService() {
                @Override
                public Map<BankAccount, List<StatisticEntry>> getAllBankAccountStatistics() {
                    ByteBuffer buffer = FamilyConfig.getBytesFromConfig(context, ASSETS);
                    if (buffer == null) return new HashMap<>();

                    int size = buffer.getInt();
                    Map<BankAccount, List<StatisticEntry>> listMap = new HashMap<>(size);
                    for (int i = 0; i < size; i++) {
                        BankAccount bankAccount = new BankAccount(buffer);
                        int entries = buffer.getInt();
                        List<StatisticEntry> statistics = new ArrayList<>(entries);
                        for (int j = 0; j < entries; j++) {
                            statistics.add(new StatisticEntry(buffer));
                        }

                        listMap.put(bankAccount, statistics);
                    }

                    return listMap;
                }

                @Override
                public DateTime getStartDate() {
                    // TODO: preferences or somewhere
                    return new DateTime(2015, 1, 1, 0, 0);
                }

                @Override
                public DateTime getEndDate() {
                    return DateTime.now();
                }

                @Override
                public void save(Map<BankAccount, List<StatisticEntry>> statisticEntryLists) {
                    int size = 4;
                    for (BankAccount bankAccount : statisticEntryLists.keySet()) {
                        size += bankAccount.getByteLength();
                        size += Byteable.getBigListLength(statisticEntryLists.get(bankAccount));
                    }

                    ByteBuffer buffer = ByteBuffer.allocate(size);
                    buffer.putInt(statisticEntryLists.size());
                    for (BankAccount bankAccount : statisticEntryLists.keySet()) {
                        bankAccount.writeBytes(buffer);
                        Byteable.writeBigList(statisticEntryLists.get(bankAccount), buffer);
                    }

                    FamilyConfig.saveBytes(context, buffer.array(), ASSETS);
                }
            });
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

        List<PendingOperation> operations = database.getNonConfirmedPendingOperations();
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
                    updateAllExpensesAndDebts();
                    List<Expenses> expensesList = new ArrayList<>(items.size());
                    for (DatabaseItem item : items) {
                        expensesList.add((Expenses) item.getItem());
                    }

                    break;
                case STANDING_ORDER:
                    updateAllStandingOrders();
                    break;
                case CATEGORY:
                    updateAllCategorys();
                    break;
                case BANKACCOUNT:
                    updateAllBankAccountsAndAssets();
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
            updateAllExpensesAndDebts();
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
            updateAllExpensesAndDebts();
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
            updateAllExpensesAndDebts();
        } catch (SQLException e) {
            logger.error("Error while adding expenses: " + e.getMessage());
        }
    }

    private void updateAllExpensesAndDebts() throws SQLException {
        allExpenses = database.getAllExpenses();
        getFinanceView().showExpenses(allExpenses);
        getFinanceView().updateDebts(debtsCalculator.getEntrys());
    }

    private void updateAllBankAccountsAndAssets() throws SQLException {
        getFinanceView().updateBankAccounts(database.getAllBankAccounts());
        getFinanceView().updateAssets(getAllAssets());
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
        // TODO: dauert viel zu lange, im Hintergrund ausführen oder hier zwischenspeichern und nur ab und zu aktualisieren
        if (allExpenses != null) return allExpenses;
        try {
            allExpenses = database.getAllExpenses();
            return allExpenses;
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
            updateAllBankAccountsAndAssets();
        } catch (SQLException e) {
            logger.error("Error while deleting standingOrder");
        }
    }


    public void deleteBalance(BankAccount account, Balance balance) {
        makeUpdateOperation(account);
        try {
            database.deleteBalance(account, balance);
            updateAllBankAccountsAndAssets();
        } catch (SQLException e) {
            logger.error("Couldn't delete balance: " + e.getMessage());
        }
    }

    public void addBalance(BankAccount account, Balance balance) {
        makeUpdateOperation(account);
        try {
            database.addBalance(account, balance);
            updateAllBankAccountsAndAssets();
        } catch (SQLException e) {
            logger.error("Couldn't add balance: " + e.getMessage());
        }
    }

    public void addBankAccount(BankAccount bankAccount) {
        makeAddOperation(bankAccount);
        try {
            database.addBankAccount(bankAccount);
            updateAllBankAccountsAndAssets();
        } catch (SQLException e) {
            logger.error("Couldn't add bankAccount: " + e.getMessage());
        }
    }

    public List<StatisticEntry> getDebts() {
        List<StatisticEntry> debts = debtsCalculator.getEntrys();
        if (debts.size() == 0) {
            logger.warn("Recalculating all debts!");
            //return debtsCalculator.recalculateAll();
        }

        return debts;
    }

    public int getColorFor(User user) {
        // TODO: make preferences
        if (user.getName().equals("Thomas")) return Color.BLUE;
        else return Color.MAGENTA;
    }

    public List<StatisticEntry> getAllAssets() {
        return assetsCalculator.getEntrysForAll();
    }
}
