package blue.koenig.kingsfinances.model.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.koenig.commonModel.User;
import com.koenig.commonModel.database.UserService;
import com.koenig.commonModel.finance.Balance;
import com.koenig.commonModel.finance.BankAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Thomas on 25.11.2017.
 */

public class BankAccountTable extends Table<BankAccount> {
    public static final String NAME = "bankaccount_table";
    private static final String BANK = "bank";
    private static final String BALANCES = "balances";
    private static final String OWNERS = "owners";
    private List<String> columnNames;
    private UserService userService;

    public BankAccountTable(SQLiteDatabase database, UserService userService, ReentrantLock lock) {
        super(database, lock);
        this.userService = userService;
        columnNames = new ArrayList<>(3);
        columnNames.add(BANK);
        columnNames.add(BALANCES);
        columnNames.add(OWNERS);
    }

    @Override
    protected void setItem(ContentValues values, BankAccount item) {
        values.put(BANK, item.getBank());
        values.put(BALANCES, Balance.Companion.listToBytes(item.getBalances()));
        values.put(OWNERS, usersToId(item.getOwners()));
    }

    @Override
    protected String getTableSpecificCreateStatement() {
        return ", " + BANK + " TEXT, " + BALANCES + " BLOB, " + OWNERS + " TEXT";
    }

    @Override
    protected BankAccount getItem(Cursor cursor) {
        String name = getString(cursor, Companion.getCOLUMN_NAME());
        String bank = getString(cursor, BANK);
        List<Balance> balances = getBalances(cursor, BALANCES);
        List<User> users = getUsers(userService, getString(cursor, OWNERS));
        return new BankAccount(name, bank, users, balances);
    }

    private List<Balance> getBalances(Cursor cursor, String column) {
        byte[] bytes = cursor.getBlob(cursor.getColumnIndex(column));
        return Balance.Companion.getBalances(bytes);
    }


    @Override
    public String getTableName() {
        return NAME;
    }

    @Override
    protected List<String> getColumnNames() {
        return columnNames;
    }

    @Override
    protected void bindItem(SQLiteStatement statement, Map<String, Integer> map, BankAccount item) {
        statement.bindString(map.get(BANK), item.getBank());
        statement.bindBlob(map.get(BALANCES), Balance.Companion.listToBytes(item.getBalances()));
        statement.bindString(map.get(OWNERS), usersToId(item.getOwners()));
    }

}
