package blue.koenig.kingsfinances.model.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.koenig.commonModel.Category;
import com.koenig.commonModel.Operation;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Thomas on 25.11.2017.
 */

public class CategoryTable extends Table<Category> {
    public static final String NAME = "category_table";
    private static final String SUBS = "subs";

    public CategoryTable(SQLiteDatabase database, ReentrantLock lock) {
        super(database, lock);
    }

    @Override
    protected void setItem(ContentValues values, Category item) {
        values.put(SUBS, Companion.buildStringList(item.getSubs()));
    }

    @Override
    protected String getTableSpecificCreateStatement() {
        return ", " + SUBS + " TEXT";
    }

    @Override
    protected Category getItem(Cursor cursor) {
        List<String> subs = getStringList(cursor, SUBS);
        String name = getString(cursor, Companion.getCOLUMN_NAME());
        return new Category(name, subs);
    }

    private Operation getOperation(Cursor cursor, String columnOperation) {
        byte[] bytes = cursor.getBlob(cursor.getColumnIndex(columnOperation));
        return new Operation(ByteBuffer.wrap(bytes));
    }

    @Override
    public String getTableName() {
        return NAME;
    }

    @Override
    protected List<String> getColumnNames() {
        ArrayList<String> columnNames = new ArrayList<>();
        columnNames.add(SUBS);
        return columnNames;
    }

    @Override
    protected void bindItem(SQLiteStatement statement, Map<String, Integer> map, Category item) {
        statement.bindString(map.get(SUBS), Companion.buildStringList(item.getSubs()));
    }

}
