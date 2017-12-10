package blue.koenig.kingsfinances.model.database;

/**
 * Created by Thomas on 06.09.2015.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.koenig.commonModel.database.DatabaseItem;
import com.koenig.commonModel.finance.Expenses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import blue.koenig.kingsfamilylibrary.model.FamilyConfig;
import blue.koenig.kingsfinances.model.PendingOperation;
import blue.koenig.kingsfinances.model.PendingStatus;

public class FinanceDatabase extends SQLiteOpenHelper
{
    private final ExpensesTable expensesTable;
    private final StandingOrderTable standingOrderTable;
    private final CategoryTable categoryTable;
    private PendingTable pendingTable;
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    // Database Name
    public static final String DATABASE_NAME = "family_finance.sqlite";
    // Database Version
    private static final int DATABASE_VERSION = 1;
    private Context context;
    List<Table> tables = new ArrayList<>();

    public FinanceDatabase(Context context, String databaseName) throws SQLException {
        this(context,databaseName, null, DATABASE_VERSION);
    }

    public FinanceDatabase(Context context) throws SQLException {
        this(context, DATABASE_NAME);
    }

    public FinanceDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) throws SQLException {
        super(context, name, factory, version);
        this.context = context;
        pendingTable = new PendingTable(getWritableDatabase());
        expensesTable = new ExpensesTable(getWritableDatabase());
        standingOrderTable = new StandingOrderTable(getWritableDatabase());
        categoryTable = new CategoryTable(getWritableDatabase());
        tables.add(pendingTable);
        tables.add(expensesTable);
        tables.add(standingOrderTable);
        tables.add(categoryTable);
        createAllTables();
        //pendingTable.drop();
        //pendingTable.create();
    }

    public void createAllTables() throws SQLException {
        for (Table table : tables) {
            if (!table.isExisting()) {
                table.create();
                logger.info("Table created: " + table.getTableName());
            }
        }
    }

    public void deleteAllEntrys() throws SQLException {
        for (Table table : tables) {
            table.deleteAllEntrys();
        }
    }



    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.d("DB", "On Create");
        try {
            for (Table table : tables) {
                table.create();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        // TODO
    }

    public void addPendingOperation(PendingOperation pendingOperation) throws SQLException {
        pendingTable.addFrom(pendingOperation, "USERID");
    }

    public List<PendingOperation> getAllPendingOperation() throws SQLException{
        return pendingTable.getAllItems();
    }

    public void updatePendingOperation(PendingOperation pendingOperation, String userId) throws SQLException {
        pendingTable.updateFrom(pendingOperation, userId);
    }

    public PendingOperation getPendingOperationFromId(String id) throws SQLException {
        return pendingTable.getFromId(id);
    }

    public void deletePendingOperation(String id) throws SQLException {
        pendingTable.deleteFrom(id, getUserId());
    }

    public void setPendingOperation(PendingStatus status, String id) throws SQLException {
        pendingTable.updateStatus(status, id);
    }

    public List<PendingOperation> getNonConfiremdPendingOperations() {
        return pendingTable.getNonConfirmedOperations();
    }

    public void updateExpensesChanges(List<DatabaseItem> items) throws SQLException {
        expensesTable.updateFromServer(items);
    }

    public void updateStandingOrderChanges(List<DatabaseItem> items) throws SQLException {
        standingOrderTable.updateFromServer(items);
    }

    public void updateCategoryChanges(List<DatabaseItem> items) throws SQLException {
        categoryTable.updateFromServer(items);
    }

    public List<Expenses> getAllExpenses() throws SQLException {
        return expensesTable.getAllItems();
    }

    public void updateExpenses(Expenses expenses) throws SQLException {
        expensesTable.updateFrom(expenses, getUserId());
    }

    public void deleteExpenses(Expenses expenses) throws SQLException {
        expensesTable.deleteFrom(expenses.getId(), getUserId());
    }

    private String getUserId() {
        return FamilyConfig.getUserId(context);
    }

    public void addExpenses(Expenses expenses) throws SQLException {
        expensesTable.addFrom(expenses, getUserId());
    }
}
