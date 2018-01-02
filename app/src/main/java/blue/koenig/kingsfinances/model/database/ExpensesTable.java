package blue.koenig.kingsfinances.model.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.koenig.commonModel.database.DatabaseItem;
import com.koenig.commonModel.finance.BookkeepingEntry;
import com.koenig.commonModel.finance.Expenses;

import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Thomas on 30.11.2017.
 */

public class ExpensesTable extends BookkeepingTable<Expenses> {
    private static final String DATE = "date";
    private static final String STANDING_ORDER = "standing_order";
    public final String NAME = "ExpensesTable";
    public ExpensesTable(SQLiteDatabase database, ReentrantLock lock) {
        super(database, lock);
    }

    @Override
    protected String getBookkeepingTableSpecificCreateStatement() {
        return "," + DATE + " LONG, " + STANDING_ORDER + " TEXT";
    }

    @Override
    protected void setBookkeepingItem(ContentValues values, Expenses item) {
        values.put(DATE, dateTimeToValue(item.getDate()));
        values.put(STANDING_ORDER, item.getStandingOrder());
    }

    @Override
    protected void bindBookkeepingItem(SQLiteStatement statement, Map<String, Integer> map, Expenses item) {
        statement.bindLong(map.get(DATE), dateTimeToValue(item.getDate()));
        statement.bindString(map.get(STANDING_ORDER), item.getStandingOrder());
    }

    @Override
    protected Expenses getBookkeepingItem(BookkeepingEntry entry, Cursor cursor) {
        DateTime date = getDateTime(cursor, DATE);
        String standingOrder = getString(cursor, STANDING_ORDER);
        return new Expenses(entry, date, standingOrder);
    }

    @Override
    protected Collection<? extends String> getBookkeepingColumnNames() {
        return Arrays.asList(DATE, STANDING_ORDER);
    }

    @Override
    public String getTableName() {
        return NAME;
    }

    public List<Expenses> getAllSince(DateTime updateSince) throws SQLException {
        return runInLock(() -> {
            ArrayList<DatabaseItem<Expenses>> items = new ArrayList<>();

            String selectQuery = "SELECT * FROM " + getTableName() + " WHERE " + DATE + " >= ? AND " + COLUMN_DELETED + " != ?";

            Cursor cursor = db.rawQuery(selectQuery, new String[]{Long.toString(updateSince.getMillis()), TRUE_STRING});

            if (cursor.moveToFirst()) {
                do {
                    DatabaseItem<Expenses> databaseItem = createDatabaseItemFromCursor(cursor);
                    items.add(databaseItem);
                } while (cursor.moveToNext());
            }

            cursor.close();

            return toItemList(items);
        });
    }
}
