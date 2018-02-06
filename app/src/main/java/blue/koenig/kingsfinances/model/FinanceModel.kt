package blue.koenig.kingsfinances.model

import android.content.Context
import android.graphics.Color
import blue.koenig.kingsfamilylibrary.model.FamilyConfig
import blue.koenig.kingsfamilylibrary.model.communication.ServerConnection
import blue.koenig.kingsfamilylibrary.model.family.FamilyModel
import blue.koenig.kingsfamilylibrary.view.family.FamilyView
import blue.koenig.kingsfamilylibrary.view.family.LoginHandler
import blue.koenig.kingsfinances.R
import blue.koenig.kingsfinances.features.category_statistics.CategoryCalculator
import blue.koenig.kingsfinances.features.standing_orders.StandingOrderExecutor
import blue.koenig.kingsfinances.features.statistics.AssetsCalculator
import blue.koenig.kingsfinances.model.calculation.DebtsCalculator
import blue.koenig.kingsfinances.model.calculation.IncomeCalculator
import blue.koenig.kingsfinances.model.calculation.StatisticEntry
import blue.koenig.kingsfinances.model.database.FinanceDatabase
import blue.koenig.kingsfinances.view.FinanceNullView
import blue.koenig.kingsfinances.view.FinanceView
import blue.koenig.kingsfinances.view.NullPendingView
import blue.koenig.kingsfinances.view.PendingView
import com.koenig.commonModel.*
import com.koenig.commonModel.database.DatabaseItem
import com.koenig.commonModel.finance.Balance
import com.koenig.commonModel.finance.BankAccount
import com.koenig.commonModel.finance.Expenses
import com.koenig.commonModel.finance.StandingOrder
import com.koenig.communication.messages.AUDMessage
import com.koenig.communication.messages.AskForUpdatesMessage
import com.koenig.communication.messages.FamilyMessage
import com.koenig.communication.messages.UpdatesMessage
import com.koenig.communication.messages.finance.FinanceTextMessages
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Thomas on 18.10.2017.
 */

class FinanceModel// TODO: Income Calculator as input that its tracking all changes from beginning. Put somewhere else but make sure it is tracking from startLoggedIn!
(connection: ServerConnection, context: Context, handler: LoginHandler, internal var database: FinanceDatabase, internal var userService: FinanceUserService, private val assetsCalculator: AssetsCalculator, incomeCalculator: IncomeCalculator, categoryCalculator: CategoryCalculator, val standingOrderExecutor: StandingOrderExecutor, val debtsCalculator: DebtsCalculator) : FamilyModel(connection, context, handler), FinanceCategoryService.CategoryServiceListener {

    private val categoryService: FinanceCategoryService
    private var pendingView: PendingView? = null
    private var succesMessages: Int = 0
    private var allExpenses: List<Expenses>? = null
    private var updateMessages: Int = 0
    private val financeView: FinanceView
        get() = view as FinanceView

    override fun start() {
        logger.info("Start")
        // start executing standing orders after while because at the beginning is so much work done
        Observable.timer(20, TimeUnit.SECONDS).observeOn(Schedulers.computation()).subscribe {
            logger.info("Executing standing orders...")
            standingOrderExecutor.executeForAll()
            if (standingOrderExecutor.consistencyCheck()) {
                logger.info("Consistency Check for standing orders passed")
            } else {
                logger.error("Consistency Check for standing orders failed")
                view.showText("Consistency Check for standing orders failed")
            }
        }
    }

    val pendingOperations: List<PendingOperation<out Item>>
        get() {
            try {
                return database.allPendingOperation
            } catch (e: SQLException) {
                logger.error("Couldn't get pending operations: " + e.message)
            }

            return ArrayList()
        }


    // TODO: dauert viel zu lange, im Hintergrund ausführen oder hier zwischenspeichern und nur ab und zu aktualisieren, oder nur die ersten 100 zurückliefern
    val expenses: List<Expenses>?
        get() {
            //if (allExpenses != null) return allExpenses
            try {
                allExpenses = database.allExpenses
                return allExpenses
            } catch (e: SQLException) {
                logger.error("Couldn't get expenses: " + e.message)
            }


            return ArrayList()
        }

    val standingOrders: List<StandingOrder>
        get() {
            try {
                return database.allStandingOrders
            } catch (e: SQLException) {
                logger.error("Error getting standing orders")
                return ArrayList()
            }

        }

    val bankAccounts: List<BankAccount>
        get() {
            try {
                return database.allBankAccounts
            } catch (e: SQLException) {
                logger.error("Couldn't get bankaccounts: " + e.message)
                return ArrayList()
            }

        }

    //return debtsCalculator.recalculateAll();
    val debts: List<StatisticEntry>
        get() {
            val debts = debtsCalculator.entrys
            if (debts.size == 0) {
                logger.warn("Recalculating all debts!")
            }

            return debts
        }

    val allAssets: List<StatisticEntry>
        get() = assetsCalculator.entrysForAll

    init {
        pendingView = NullPendingView()
        categoryService = FinanceCategoryService()
        categoryService.setListener(this)

        try {
            categoryService.update(database.allCategorys)

        } catch (e: SQLException) {
            logger.error("Couldn't create database: " + e.message)
        }

    }

    public override fun startLoggedIn() {
        logger.info("Start")
        val members = loginHandler.members
        if (members != null) {
            updateFamilymembers(members)
        }

        // TODO: send only last edit from same item
        val operations = database.nonConfirmedPendingOperations
        clearAUDSuccesMessages()
        for (operation in operations) {
            connection.sendFamilyMessage(AUDMessage(Component.FINANCE, operation.operation))
        }

        // ask for update after sent my changes!
        waitForSuccessMessages(operations.size)
        updateMessages = 0
        askForAllUpdates()
        //waitForFinishUpdates()

    }

    private fun waitForFinishUpdates() {
        var i = 0
        val timeOutInS = 10
        val intervall = 100
        while (i < 1000 * timeOutInS / intervall) {
            if (updateMessages >= 5) return
            Thread.sleep(intervall.toLong())
            i++
        }

    }

    private fun waitForSuccessMessages(size: Int) {
        var i = 0
        val timeOutInS = 2
        val intervall = 100
        try {
            while (i < 1000 * timeOutInS / intervall) {
                if (succesMessages >= size) return
                Thread.sleep(intervall.toLong())
                i++
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    private fun clearAUDSuccesMessages() {
        succesMessages = 0
    }

    private fun askForAllUpdates() {
        askForUpdates(ItemType.EXPENSES)
        askForUpdates(ItemType.CATEGORY)
        askForUpdates(ItemType.STANDING_ORDER)
        askForUpdates(ItemType.BANKACCOUNT)
        askForUpdates(ItemType.GOAL)
    }

    private fun askForUpdates(itemType: ItemType) {
        sendMessageToServer(AskForUpdatesMessage(Component.FINANCE, FamilyConfig.getLastSyncDate(context, itemType.name), itemType))
    }

    override fun updateFamilymembers(members: List<User>) {
        logger.info("Setting family members...")
        financeView.setFamilyMembers(members)
        userService.setUser(members)
    }


    public override fun onReceiveFinanceMessage(message: FamilyMessage) {
        when (message.name) {
            UpdatesMessage.NAME -> {
                val updatesMessage = message as UpdatesMessage<*>
                update(updatesMessage.items)
                updateMessages++
            }
            else -> logger.error("Unknown Message: " + message.name)
        }
    }

    private fun update(items: List<DatabaseItem<*>>) {
        try {
            update(database, items)
            if (items.isEmpty()) return
            val itemType = ItemType.fromItem(items[0].item)

            when (itemType) {
                ItemType.EXPENSES -> updateAllExpensesAndDebts()
                ItemType.STANDING_ORDER -> updateAllStandingOrders()
                ItemType.CATEGORY -> updateAllCategorys()
                ItemType.BANKACCOUNT -> updateAllBankAccountsAndAssets()
                ItemType.GOAL -> updateGoals()
                else -> logger.error("Unknown item type $itemType")
            }

            FamilyConfig.saveLastSyncDate(DateTime.now(), context, itemType.name)

        } catch (ex: SQLException) {
            logger.error("Error while updating: " + ex.message)
        }

    }

    private fun updateGoals() {
        // TODO: update CategoryStatisticsFragment/Presenter --> It should receive messages on its own
    }


    private fun updateAllCategorys() {
        try {
            val categories = database.allCategorys
            categoryService.update(categories)
        } catch (e: SQLException) {
            logger.error("Error getting categoires: " + e.message)
        }

    }

    private fun updateAllStandingOrders() {
        try {
            val standingOrders = database.allStandingOrders
            financeView.showStandingOrders(standingOrders)
        } catch (e: SQLException) {
            logger.error("Error getting categoires: " + e.message)
        }

    }

    fun onTabSelected(position: Int) {
        logger.info("OnTabSelected: " + position)
        if (position == 1) {
            pendingView!!.update(pendingOperations)
        }
    }

    override fun processFinanceCommand(words: Array<String>) {
        try {

            when (words[0]) {
                FinanceTextMessages.GET_ALL_EXPENSES_FAIL -> view.showText(R.string.getExpensesFail)
                FinanceTextMessages.AUD_SUCCESS -> {
                    succesMessages++
                    database.setPendingOperation(PendingStatus.CONFIRMED, words[1])
                    pendingView!!.update(pendingOperations)
                }
                FinanceTextMessages.AUD_FAIL -> {
                    database.setPendingOperation(PendingStatus.ERROR, words[1])
                    pendingView!!.update(pendingOperations)
                }

                else -> {
                    logger.info("Unknown command")
                    view.showText(words[0])
                }
            }
        } catch (e: Exception) {
            logger.error("Error while processing finance command: " + e.message)
        }

    }

    public override fun createNullView(): FamilyView {
        return FinanceNullView()
    }

    fun deleteExpenses(expenses: Expenses) {
        makeDeleteOperation(expenses)

        try {
            database.deleteExpenses(expenses)
            updateAllExpensesAndDebts()
        } catch (e: SQLException) {
            logger.error("Error while deleting expenses")
        }

    }


    private fun makeUpdateOperation(item: Item) {
        makeOperation(Operator.UPDATE, item)
    }

    private fun makeAddOperation(item: Item) {
        makeOperation(Operator.ADD, item)
    }

    private fun makeDeleteOperation(item: Item) {
        makeOperation(Operator.DELETE, item)
    }

    private fun makeOperation(operator: Operator, item: Item) {
        val operation = Operation(operator, item)
        try {
            database.addPendingOperation(PendingOperation(operation, PendingStatus.PENDING, DateTime.now()))
        } catch (e: SQLException) {
            logger.error("Error on operation: " + e.message)
        }

        connection.sendFamilyMessage(AUDMessage(Component.FINANCE, operation))
        pendingView!!.update(pendingOperations)
    }

    fun editExpenses(expenses: Expenses) {
        makeUpdateOperation(expenses)
        try {
            database.updateExpenses(expenses)
            updateAllExpensesAndDebts()
        } catch (e: SQLException) {
            logger.error("Error while updating expenses: " + e.message)
        }

    }

    fun getCategoryService(): CategoryService {
        return categoryService
    }

    fun addExpenses(expenses: Expenses) {
        try {
            makeAddOperation(expenses)

            database.addExpenses(expenses)
            updateAllExpensesAndDebts()
        } catch (e: SQLException) {
            logger.error("Error while adding expenses: " + e.message)
        }

    }

    @Throws(SQLException::class)
    private fun updateAllExpensesAndDebts() {
        // TODO: notify expenses changed through database
    }

    @Throws(SQLException::class)
    private fun updateAllBankAccountsAndAssets() {
        financeView.updateBankAccounts(database.allBankAccounts)
        financeView.updateAssets(allAssets)
    }

    fun deletePending(operation: PendingOperation<Item>) {
        try {
            database.deletePendingOperation(operation.id)
            // TODO: revert database(memorize last state) or ask database for old state
            pendingView!!.update(pendingOperations)
        } catch (e: SQLException) {
            logger.error("Exception deleting operation: " + e.message)
        }

    }

    fun sendPending(pendingOperation: PendingOperation<Item>) {
        connection.sendFamilyMessage(AUDMessage(Component.FINANCE, pendingOperation.operation))
    }

    fun attachPendingView(pendingFragment: PendingView) {
        this.pendingView = pendingFragment
    }

    fun detachPendingView() {
        this.pendingView = NullPendingView()
    }

    override fun addMainCategory(newCategory: String) {
        try {
            val category = Category(newCategory)
            makeAddOperation(category)
            database.addCategory(category)
        } catch (e: SQLException) {
            logger.error("Error adding new Category")
        }

    }

    override fun addSubCategory(mainCategory: String, newCategory: String) {
        try {
            val category = database.getCategory(mainCategory)
            if (category != null) {
                category.addSub(newCategory)
                makeUpdateOperation(category)
                database.updateCategory(category)
            } else {
                logger.error("Couldn't find main category to add subcategory: " + mainCategory)
            }

        } catch (e: SQLException) {
            logger.error("Error adding new subcategory")
        }

    }

    fun addStandingOrder(standingOrder: StandingOrder) {
        try {
            makeAddOperation(standingOrder)
            database.addStandingOrder(standingOrder)
            updateAllStandingOrders()
        } catch (e: SQLException) {
            logger.error("Error while adding standingOrder: " + e.message)
        }

    }

    fun editStandingOrder(standingOrder: StandingOrder) {
        makeUpdateOperation(standingOrder)
        try {
            database.updateStandingOrder(standingOrder)
            updateAllStandingOrders()
        } catch (e: SQLException) {
            logger.error("Error while updating standingOrder: " + e.message)
        }

    }

    fun deleteStandingOrder(standingOrder: StandingOrder) {
        makeDeleteOperation(standingOrder)
        try {
            database.deleteStandingOrder(standingOrder)
            updateAllStandingOrders()
        } catch (e: SQLException) {
            logger.error("Error while deleting standingOrder")
        }

    }

    fun deleteBankAccount(account: BankAccount) {
        makeDeleteOperation(account)
        try {
            database.deleteBankAccount(account)
            updateAllBankAccountsAndAssets()
        } catch (e: SQLException) {
            logger.error("Error while deleting standingOrder")
        }

    }


    fun deleteBalance(account: BankAccount, balance: Balance) {
        makeUpdateOperation(account)
        try {
            database.deleteBalance(account, balance)
            updateAllBankAccountsAndAssets()
        } catch (e: SQLException) {
            logger.error("Couldn't delete balance: " + e.message)
        }

    }

    fun addBalance(account: BankAccount, balance: Balance) {
        makeUpdateOperation(account)
        try {
            database.addBalance(account, balance)
            updateAllBankAccountsAndAssets()
        } catch (e: SQLException) {
            logger.error("Couldn't add balance: " + e.message)
        }

    }

    fun addBankAccount(bankAccount: BankAccount) {
        makeAddOperation(bankAccount)
        try {
            database.addBankAccount(bankAccount)
            updateAllBankAccountsAndAssets()
        } catch (e: SQLException) {
            logger.error("Couldn't add bankAccount: " + e.message)
        }

    }

    fun getColorFor(user: User): Int {
        // TODO: make preferences
        return if (user.name == "Thomas")
            Color.BLUE
        else
            Color.MAGENTA
    }

    companion object {

        private val clogger = LoggerFactory.getLogger("FinanceModel")

        @Suppress("UNCHECKED_CAST")
        @Throws(SQLException::class)
        fun update(database: FinanceDatabase, items: List<DatabaseItem<*>>) {
            if (items.isEmpty()) return
            val itemType = ItemType.fromItem(items[0].item)

            when (itemType) {
                ItemType.EXPENSES -> database.updateExpensesChanges(items as List<DatabaseItem<Expenses>>)
                ItemType.STANDING_ORDER -> database.updateStandingOrderChanges(items as List<DatabaseItem<StandingOrder>>)
                ItemType.CATEGORY -> database.updateCategoryChanges(items as List<DatabaseItem<Category>>)
                ItemType.BANKACCOUNT -> database.updateBankAccountChanges(items as List<DatabaseItem<BankAccount>>)
                ItemType.GOAL -> database.updateGoalChanges(items as List<DatabaseItem<Goal>>)
                else -> clogger.error("Unknown ItemType $itemType")
            }
        }
    }

}
