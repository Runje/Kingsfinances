package blue.koenig.kingsfinances.model.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.koenig.commonModel.finance.BookkeepingEntry;
import com.koenig.commonModel.finance.Expenses;

import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Thomas on 30.11.2017.
 */

public class ExpensesTable extends BookkeepingTable<Expenses> {
    public final String NAME = "ExpensesTable";
    private static final String DATE = "date";
    private static final String STANDING_ORDER = "standing_order";
    public ExpensesTable(SQLiteDatabase database) {
        super(database);
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
}
