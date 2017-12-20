package blue.koenig.kingsfinances.model.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.koenig.commonModel.Item;
import com.koenig.commonModel.finance.BookkeepingEntry;
import com.koenig.commonModel.finance.CostDistribution;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Thomas on 29.11.2017.
 */

abstract class BookkeepingTable<T extends BookkeepingEntry> extends Table<T> {
    private static final String CATEGORY = "category";
    private static final String SUBCATEGORY = "sub_category";
    private static final String COSTS = "costs";
    private static final String COSTDISTRIBUTION = "cost_distribution";

    public BookkeepingTable(SQLiteDatabase database, ReentrantLock lock) {
        super(database, lock);
    }

    @Override
    protected String getTableSpecificCreateStatement() {
        return ", "
                + CATEGORY + " TEXT,"
                + SUBCATEGORY + " TEXT,"
                + COSTS + " INT,"
                + COSTDISTRIBUTION + " BLOB"
                + getBookkeepingTableSpecificCreateStatement();
    }

    protected abstract String getBookkeepingTableSpecificCreateStatement();

    @Override
    protected void setItem(ContentValues values, T item) {
        values.put(CATEGORY, item.getCategory());
        values.put(SUBCATEGORY, item.getSubCategory());
        values.put(COSTS, item.getCosts());
        values.put(COSTDISTRIBUTION, item.getCostDistribution().getBytes());
        setBookkeepingItem(values, (T) item);
    }

    protected abstract void setBookkeepingItem(ContentValues values, T item);

    @Override
    protected void bindItem(SQLiteStatement statement, Map<String, Integer> map, T item) {
        statement.bindString(map.get(CATEGORY), item.getCategory());
        statement.bindString(map.get(SUBCATEGORY), item.getSubCategory());
        statement.bindLong(map.get(COSTS), item.getCosts());
        statement.bindBlob(map.get(COSTDISTRIBUTION), byteableToValue(item.getCostDistribution()));
        bindBookkeepingItem(statement, map, item);
    }

    protected abstract void bindBookkeepingItem(SQLiteStatement statement, Map<String, Integer> map, T item);

    @Override
    protected T getItem(Cursor cursor) {
        String name = getString(cursor, COLUMN_NAME);
        String category = getString(cursor, CATEGORY);
        String subcategory = getString(cursor, SUBCATEGORY);
        int costs = getInt(cursor, COSTS);
        CostDistribution costDistribution = getCostDistribution(cursor, COSTDISTRIBUTION);
        BookkeepingEntry entry = new BookkeepingEntry(name, category, subcategory, costs, costDistribution);

        return getBookkeepingItem(entry, cursor);
    }

    protected abstract T getBookkeepingItem(BookkeepingEntry entry, Cursor cursor);

    private CostDistribution getCostDistribution(Cursor cursor, String columnName) {
        ByteBuffer buffer = ByteBuffer.wrap(cursor.getBlob(cursor.getColumnIndex(columnName)));
        return new CostDistribution(buffer);
    }



    @Override
    protected List<String> getColumnNames() {
        ArrayList<String> columnNames = new ArrayList<>();
        columnNames.add(CATEGORY);
        columnNames.add(SUBCATEGORY);
        columnNames.add(COSTS);
        columnNames.add(COSTDISTRIBUTION);
        columnNames.addAll(getBookkeepingColumnNames());
        return columnNames;
    }

    protected abstract Collection<? extends String> getBookkeepingColumnNames();
}
