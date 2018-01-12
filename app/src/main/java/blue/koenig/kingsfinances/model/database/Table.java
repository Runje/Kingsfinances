package blue.koenig.kingsfinances.model.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.koenig.commonModel.Byteable;
import com.koenig.commonModel.Item;
import com.koenig.commonModel.database.Database;
import com.koenig.commonModel.database.DatabaseItem;
import com.koenig.commonModel.database.DatabaseTable;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import blue.koenig.kingsfinances.model.calculation.ItemSubject;

/**
 * Created by Thomas on 25.11.2017.
 */

public abstract class Table<T extends Item> extends DatabaseTable<T> implements ItemSubject<T> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    protected List<OnDeleteListener> onDeleteListeners = new ArrayList<>();
    protected List<OnAddListener> onAddListeners = new ArrayList<>();
    protected List<OnUpdateListener> onUpdateListeners = new ArrayList<>();
    SQLiteDatabase db;

    public Table(SQLiteDatabase database, ReentrantLock lock) {
        db = database;
        //share locks between all tables of one database
        this.lock = lock;
    }

    @Override
    public void create() throws SQLException {
        runInLock(() -> {
            db.execSQL(buildCreateStatement());
        });
    }

    @Override
    public List<DatabaseItem<T>> getAll() throws SQLException {
        return runInLock(() -> {
            ArrayList<DatabaseItem<T>> items = new ArrayList<>();

            String selectQuery = "SELECT * FROM " + getTableName() + " WHERE " + COLUMN_DELETED + " != ?";

            Cursor cursor = db.rawQuery(selectQuery, new String[]{TRUE_STRING});

            if (cursor.moveToFirst())
            {
                do
                {
                    DatabaseItem<T> databaseItem = createDatabaseItemFromCursor(cursor);
                    items.add(databaseItem);
                } while (cursor.moveToNext());
            }

            cursor.close();

            return items;
        });

    }

    @Override
    public boolean isExisting() throws SQLException {
        // is ok to always return false, because the table will only be created if it does not exist
        return false;
    }

    protected ContentValues itemToValues(DatabaseItem<T> item) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, item.getId());
        values.put(COLUMN_MODIFIED_ID, item.getLastModifiedId());
        values.put(COLUMN_INSERT_ID, item.getInsertId());
        values.put(COLUMN_MODIFIED_DATE, dateTimeToValue(item.getLastModifiedDate()));
        values.put(COLUMN_INSERT_DATE, dateTimeToValue(item.getInsertDate()));
        values.put(COLUMN_DELETED, boolToValue(item.isDeleted()));
        values.put(COLUMN_NAME, item.getName());
        setItem(values, item.getItem());
        return  values;

    }

    protected abstract void setItem(ContentValues values, T item);

    protected short boolToValue(boolean bool) {
        return (short) (bool ? 1 : 0);
    }

    protected long dateTimeToValue(DateTime date) {
        return date.getMillis();
    }

    protected byte[] byteableToValue(Byteable byteable) {
        return byteable.getBytes();
    }

    @Override
    public void add(DatabaseItem<T> databaseItem) throws SQLException {
        runInLock(() -> {
            for (OnAddListener onAddListener : onAddListeners) {
                // only add non deleted items to statistics
                if (!databaseItem.isDeleted()) {
                    onAddListener.onAdd(databaseItem.getItem());
                }
            }

            db.insert(getTableName(), null, itemToValues(databaseItem));
        });
    }

    @Override
    public DatabaseItem<T> getDatabaseItemFromId(String id) throws SQLException {
        String selectQuery = "SELECT * FROM " + getTableName() + " WHERE " + COLUMN_ID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{id});

        DatabaseItem<T> result = null;
        if (cursor.moveToFirst())
        {
            result = createDatabaseItemFromCursor(cursor);
        }

        cursor.close();
        return result;
    }

    protected DatabaseItem<T> createDatabaseItemFromCursor(Cursor cursor) {
        String id = getString(cursor, COLUMN_ID);
        String lastModifiedId = getString(cursor, COLUMN_MODIFIED_ID);
        String insertId = getString(cursor, COLUMN_INSERT_ID);
        String name = getString(cursor, COLUMN_NAME);
        boolean deleted = getBool(cursor, COLUMN_DELETED);
        DateTime modifiedDate = getDateTime(cursor, COLUMN_MODIFIED_DATE);
        DateTime insertDate = getDateTime(cursor, COLUMN_INSERT_DATE);
        T item = getItem(cursor);
        item.setId(id);
        item.setName(name);
        return new DatabaseItem<T>(item, insertDate, modifiedDate, deleted, insertId, lastModifiedId);
    }



    @Override
    public void deleteFrom(String itemId, String userId) throws SQLException {
        for (OnDeleteListener onDeleteListener : onDeleteListeners) {
            // ASSUMPTION: item was before in database else the statistics are corrupted!
            onDeleteListener.onDelete(getFromId(itemId));
        }
        List<String> columns = new ArrayList<>();
        columns.add(COLUMN_MODIFIED_ID);
        columns.add(COLUMN_MODIFIED_DATE);
        columns.add(COLUMN_DELETED);
        update(itemId, columns, (statement, map) -> {
            statement.bindString(map.get(COLUMN_MODIFIED_ID), userId);
            statement.bindLong(map.get(COLUMN_MODIFIED_DATE), dateTimeToValue(DateTime.now()));
            statement.bindLong(map.get(COLUMN_DELETED), boolToValue(true));
        });
    }

    protected void update(String itemId, List<String> columns, StatementBinder binder) throws SQLException {
        // don't update id!
        columns.remove(COLUMN_ID);
        String query = "UPDATE " + getTableName() + " SET " + getParameters(columns) + " WHERE " + COLUMN_ID + "= ?";
        // id of where clause
        columns.add(COLUMN_ID);
        Map<String, Integer> map = createMap(columns);
        SQLiteStatement statement = db.compileStatement(query);
        statement.bindString(map.get(COLUMN_ID), itemId);
        binder.bind(statement, map);
        int updates = statement.executeUpdateDelete();

        if (updates != 1) {
            throw new SQLException("Update error: rows= " + updates);
        }
    }

    protected void runTransaction(Database.Transaction runnable) throws SQLException {
        //db.beginTransaction();
        this.lock.lock();

        try {
            runnable.run();
            //db.setTransactionSuccessful();
        } finally {
            this.lock.unlock();
            //db.endTransaction();
        }
    }

    public void updateFromServer(List<DatabaseItem> items) throws SQLException {
        runTransaction(() -> {
            for (DatabaseItem<T> item : items) {
                DatabaseItem<T> oldDatabaseItem = getDatabaseItemFromId(item.getId());
                if (oldDatabaseItem == null) {
                    // new
                    add(item);
                    logger.info("Added new item: " + item.getName());
                } else {
                    if (!oldDatabaseItem.isDeleted() && item.isDeleted()) {
                        // delete
                        for (OnDeleteListener onDeleteListener : onDeleteListeners) {
                            onDeleteListener.onDelete(oldDatabaseItem.getItem());
                        }
                    } else if (!oldDatabaseItem.isDeleted() && !item.isDeleted()) {
                        // regular update
                        for (OnUpdateListener onUpdateListener : onUpdateListeners) {
                            onUpdateListener.onUpdate(oldDatabaseItem.getItem(), item.getItem());
                        }
                    }
                    // overwrite
                    overwrite(item);
                    logger.info("Overwritten item: " + item.getName());
                }
            }
        });
    }

    protected void overwrite(DatabaseItem<T> item) throws SQLException {
        update(item.getId(), getAllColumnNames(), (statement, map) -> {
            statement.bindString(map.get(COLUMN_MODIFIED_ID), item.getLastModifiedId());
            statement.bindString(map.get(COLUMN_INSERT_ID), item.getInsertId());
            statement.bindString(map.get(COLUMN_NAME), item.getName());
            statement.bindLong(map.get(COLUMN_MODIFIED_DATE), dateTimeToValue(item.getLastModifiedDate()));
            statement.bindLong(map.get(COLUMN_INSERT_DATE), dateTimeToValue(item.getInsertDate()));
            statement.bindLong(map.get(COLUMN_DELETED), boolToValue(item.isDeleted()));
            bindItem(statement, map, item.getItem());
        });
    }

    public DatabaseItem<T> getDatabaseItemFromName(String name) {
        String selectQuery = "SELECT * FROM " + getTableName() + " WHERE " + COLUMN_NAME + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{name});

        DatabaseItem<T> result = null;
        if (cursor.moveToFirst())
        {
            result = createDatabaseItemFromCursor(cursor);
        }

        cursor.close();
        return result;
    }

    public T getFromName(String name) {
        DatabaseItem<T> item = getDatabaseItemFromName(name);
        if (item == null) {
            return null;
        }

        return item.getItem();
    }

    public List<T> getAllDeletedItems() throws SQLException {
        return toItemList(getAllDeleted());
    }

    public List<DatabaseItem<T>> getAllDeleted() throws SQLException {
        return runInLock(() -> {
            ArrayList<DatabaseItem<T>> items = new ArrayList<>();

            String selectQuery = "SELECT * FROM " + getTableName() + " WHERE " + COLUMN_DELETED + " = ?";

            Cursor cursor = db.rawQuery(selectQuery, new String[]{TRUE_STRING});

            if (cursor.moveToFirst())
            {
                do
                {
                    DatabaseItem<T> databaseItem = createDatabaseItemFromCursor(cursor);
                    items.add(databaseItem);
                } while (cursor.moveToNext());
            }

            cursor.close();

            return items;
        });

    }

    @Override
    public void updateFrom(T item, String userId) throws SQLException {
        // ASSUMPTION: Updated item is not deleted!
        for (OnUpdateListener onUpdateListener : onUpdateListeners) {
            onUpdateListener.onUpdate(getFromId(item.getId()), item);
        }

        List<String> columns = new ArrayList<>();
        columns.add(COLUMN_MODIFIED_ID);
        columns.add(COLUMN_MODIFIED_DATE);
        columns.add(COLUMN_NAME);
        columns.addAll(getColumnNames());

        update(item.getId(), columns, (statement, map) -> {
            statement.bindString(map.get(COLUMN_MODIFIED_ID), userId);
            statement.bindLong(map.get(COLUMN_MODIFIED_DATE), dateTimeToValue(DateTime.now()));
            statement.bindString(map.get(COLUMN_NAME), item.getName());
            bindItem(statement, map, item);
        });

    }

    protected abstract void bindItem(SQLiteStatement statement, Map<String, Integer> map, T item);

    protected String dateTimeToStringValue(DateTime time) {
        return Long.toString(dateTimeToValue(time));
    }

    private Map<String, Integer> createMap(List<String> columns) {
        Map<String, Integer> columnsMap = new HashMap<>(columns.size());
        int i = 1;
        for (String columnName : columns) {
            columnsMap.put(columnName, i);
            i++;
        }
        return columnsMap;
    }

    private String getParameters(List<String> columnNames) {
        StringBuilder result = new StringBuilder();
        for (String name : columnNames) {
            result.append(name);
            result.append("=?, ");
        }

        return result.substring(0, result.length() - 2);
    }

    protected abstract T getItem(Cursor cursor);

    protected DateTime getDateTime(Cursor cursor, String columnName) {
        return new DateTime(cursor.getLong(cursor.getColumnIndex(columnName)));
    }

    protected boolean getBool(Cursor cursor, String columnName) {
        return cursor.getShort(cursor.getColumnIndex(columnName)) != 0;
    }

    protected String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    protected  <T extends Enum<T>> T getEnum(Cursor cursor, String name, Class<T> className) {
        return Enum.valueOf(className, getString(cursor, name));
    }

    protected int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    protected List<String> getStringList(Cursor cursor, String name) {
        return getStringList(getString(cursor, name));
    }

    @Override
    public T getFromId(String id) throws SQLException {
        DatabaseItem<T> databaseItemFromId = getDatabaseItemFromId(id);
        return databaseItemFromId == null ? null : databaseItemFromId.getItem();
    }

    @Override
    public void deleteAllEntrys() throws SQLException {
        db.execSQL("DELETE FROM " + getTableName());
    }

    public void drop() throws SQLException {
        db.execSQL("DROP TABLE IF EXISTS " + getTableName());
    }

    protected List<String> getAllColumnNames() {
        List<String> columns = new ArrayList<>();
        columns.addAll(getBaseColumnNames());
        columns.addAll(getColumnNames());
        return columns;
    }

    @Override
    protected String getTableSpecificCreateStatement() {
        return null;
    }

    public void create(SQLiteDatabase db) throws SQLException {
        this.db = db;
        create();
    }

    protected List<DatabaseItem<T>> queryWithOneValueDatabaseItems(String query, String value) {

        ArrayList<DatabaseItem<T>> databaseItems = new ArrayList<>();


        String selectQuery = "SELECT * FROM " + getTableName() + " WHERE " + query;

        Cursor cursor = db.rawQuery(selectQuery, new String[]{value});

        if (cursor.moveToFirst())
        {
            do
            {
                DatabaseItem<T> b = createDatabaseItemFromCursor(cursor);
                databaseItems.add(b);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return databaseItems;
    }

    protected List<T> queryWithOneValue(String query, String value) {
        return toItemList(queryWithOneValueDatabaseItems(query, value));
    }

    public void addDeleteListener(OnDeleteListener<T> deleteListener) {
        onDeleteListeners.add(deleteListener);
    }

    public void addAddListener(OnAddListener<T> addListener) {
        onAddListeners.add(addListener);
    }

    public void addUpdateListener(OnUpdateListener<T> updateListener) {
        onUpdateListeners.add(updateListener);
    }

    public void removeDeleteListener(OnDeleteListener deleteListener) {
        onDeleteListeners.remove(deleteListener);
    }

    public void removeAddListener(OnAddListener addListener) {
        onAddListeners.remove(addListener);
    }

    public void removeUpdateListener(OnUpdateListener updateListener) {
        onUpdateListeners.remove(updateListener);
    }

    protected interface StatementBinder {
        void bind(SQLiteStatement statement, Map<String, Integer> columnsMap);
    }

    public interface OnDeleteListener<T> {
        void onDelete(T item);
    }

    public interface OnUpdateListener<T> {
        void onUpdate(T oldItem, T newItem);
    }

    public interface OnAddListener<T> {
        void onAdd(T item);
    }
}
