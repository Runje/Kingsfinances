package blue.koenig.kingsfinances.model.database

/**
 * Created by Thomas on 06.09.2015.
 */

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import blue.koenig.kingsfamilylibrary.model.FamilyConfig
import blue.koenig.kingsfinances.model.PendingOperation
import blue.koenig.kingsfinances.model.PendingStatus
import com.koenig.commonModel.Category
import com.koenig.commonModel.Goal
import com.koenig.commonModel.Item
import com.koenig.commonModel.database.DatabaseItem
import com.koenig.commonModel.database.UserService
import com.koenig.commonModel.finance.Balance
import com.koenig.commonModel.finance.BankAccount
import com.koenig.commonModel.finance.Expenses
import com.koenig.commonModel.finance.StandingOrder
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*
import java.util.concurrent.locks.ReentrantLock

class FinanceDatabase @Throws(SQLException::class)
constructor(private val context: Context, name: String, factory: SQLiteDatabase.CursorFactory?, version: Int, userService: UserService) : SQLiteOpenHelper(context, name, factory, version) {
    val expensesTable: ExpensesTable
    private val standingOrderTable: StandingOrderTable
    private val categoryTable: CategoryTable
    val bankAccountTable: BankAccountTable
    val goalTable: GoalTable
    protected var logger = LoggerFactory.getLogger(this.javaClass.simpleName)
    protected var lock = ReentrantLock()
    internal var tables: MutableList<Table<*>> = ArrayList()
    val pendingTable: PendingTable

    val allPendingOperation: List<PendingOperation<out Item>>
        @Throws(SQLException::class)
        get() = pendingTable.allItems

    val nonConfirmedPendingOperations: List<PendingOperation<out Item>>
        get() = pendingTable.nonConfirmedOperations

    val allExpenses: List<Expenses>
        @Throws(SQLException::class)
        get() = expensesTable.allItems

    private val userId: String
        get() = FamilyConfig.getUserId(context)

    val allCategorys: List<Category>
        @Throws(SQLException::class)
        get() = categoryTable.allItems

    val allStandingOrders: List<StandingOrder>
        @Throws(SQLException::class)
        get() = standingOrderTable.allItems

    val allBankAccounts: List<BankAccount>
        @Throws(SQLException::class)
        get() = bankAccountTable.allItems

    @Throws(SQLException::class)
    constructor(context: Context, databaseName: String, userService: UserService) : this(context, databaseName, null, DATABASE_VERSION, userService) {
    }

    @Throws(SQLException::class)
    constructor(context: Context, userService: UserService) : this(context, DATABASE_NAME, userService) {
    }

    init {
        pendingTable = PendingTable(writableDatabase, lock)
        expensesTable = ExpensesTable(writableDatabase, lock)
        standingOrderTable = StandingOrderTable(writableDatabase, lock)
        categoryTable = CategoryTable(writableDatabase, lock)
        goalTable = GoalTable(writableDatabase, lock)
        bankAccountTable = BankAccountTable(writableDatabase, userService, lock)
        tables.add(pendingTable)
        tables.add(expensesTable)
        tables.add(standingOrderTable)
        tables.add(categoryTable)
        tables.add(bankAccountTable)
        tables.add(goalTable)
        createAllTables()
        //pendingTable.drop();
        //pendingTable.create();
    }

    @Throws(SQLException::class)
    fun createAllTables() {
        for (table in tables) {
            if (!table.isExisting()) {
                table.create()
                logger.info("Table created: " + table.tableName)
            }
        }
    }

    @Throws(SQLException::class)
    fun deleteAllEntrys() {
        for (table in tables) {
            table.deleteAllEntrys()
        }
    }


    override fun onCreate(db: SQLiteDatabase) {
        Log.d("DB", "On Create")
        try {
            for (table in tables) {
                table.create()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // TODO
    }

    @Throws(SQLException::class)
    fun addPendingOperation(pendingOperation: PendingOperation<*>) {
        pendingTable.addFrom(pendingOperation, "USERID")
    }

    @Throws(SQLException::class)
    fun updatePendingOperation(pendingOperation: PendingOperation<*>, userId: String) {
        pendingTable.updateFrom(pendingOperation, userId)
    }

    @Throws(SQLException::class)
    fun getPendingOperationFromId(id: String): PendingOperation<*>? {
        return pendingTable.getFromId(id)
    }

    @Throws(SQLException::class)
    fun deletePendingOperation(id: String) {
        pendingTable.deleteFrom(id, userId)
    }

    @Throws(SQLException::class)
    fun setPendingOperation(status: PendingStatus, id: String) {
        pendingTable.updateStatus(status, id)
    }

    @Throws(SQLException::class)
    fun updateExpensesChanges(items: List<DatabaseItem<Expenses>>) {
        expensesTable.updateFromServer(items)
    }

    @Throws(SQLException::class)
    fun updateStandingOrderChanges(items: List<DatabaseItem<StandingOrder>>) {
        standingOrderTable.updateFromServer(items)
    }

    @Throws(SQLException::class)
    fun updateCategoryChanges(items: List<DatabaseItem<Category>>) {
        categoryTable.updateFromServer(items)
    }

    @Throws(SQLException::class)
    fun updateExpenses(expenses: Expenses) {
        expensesTable.updateFrom(expenses, userId)
    }

    @Throws(SQLException::class)
    fun deleteExpenses(expenses: Expenses) {
        expensesTable.deleteFrom(expenses.id, userId)
    }

    @Throws(SQLException::class)
    fun addExpenses(expenses: Expenses) {
        expensesTable.addFrom(expenses, userId)
    }

    @Throws(SQLException::class)
    fun addCategory(category: Category) {
        categoryTable.addFrom(category, userId)
    }

    fun getCategory(mainCategory: String): Category? {
        return categoryTable.getFromName(mainCategory)
    }

    @Throws(SQLException::class)
    fun updateCategory(category: Category) {
        categoryTable.updateFrom(category, userId)
    }

    @Throws(SQLException::class)
    fun addStandingOrder(standingOrder: StandingOrder) {
        standingOrderTable.addFrom(standingOrder, userId)
    }

    @Throws(SQLException::class)
    fun updateStandingOrder(standingOrder: StandingOrder) {
        standingOrderTable.updateFrom(standingOrder, userId)
    }

    @Throws(SQLException::class)
    fun deleteStandingOrder(standingOrder: StandingOrder) {
        standingOrderTable.deleteFrom(standingOrder.id, userId)
    }

    @Throws(SQLException::class)
    fun deleteBankAccount(account: BankAccount) {
        bankAccountTable.deleteFrom(account.id, userId)
    }

    @Throws(SQLException::class)
    fun updateBankAccountChanges(bankAccounts: List<DatabaseItem<BankAccount>>) {
        bankAccountTable.updateFromServer(bankAccounts)
    }

    @Throws(SQLException::class)
    fun deleteBalance(account: BankAccount, balance: Balance) {
        val bankAccount = bankAccountTable.getFromId(account.id)
        bankAccount!!.deleteBalance(balance)
        bankAccountTable.updateFrom(bankAccount, userId)
    }

    @Throws(SQLException::class)
    fun addBalance(account: BankAccount, balance: Balance) {
        val bankAccount = bankAccountTable.getFromId(account.id)
        bankAccount!!.addBalance(balance)
        bankAccountTable.updateFrom(bankAccount, userId)
    }

    @Throws(SQLException::class)
    fun addBankAccount(bankAccount: BankAccount) {
        bankAccountTable.addFrom(bankAccount, userId)
    }

    @Throws(SQLException::class)
    fun getAllExpensesSince(updateSince: DateTime): List<Expenses> {
        return expensesTable.getAllSince(updateSince)
    }

    companion object {
        // Database Name
        val DATABASE_NAME = "family_finance.sqlite"
        // Database Version
        private val DATABASE_VERSION = 1
    }

    fun updateGoalChanges(list: List<DatabaseItem<Goal>>) {
        goalTable.updateFromServer(list)
    }
}
