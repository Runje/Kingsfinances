package blue.koenig.kingsfinances.model.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.koenig.commonModel.Byteable;
import com.koenig.commonModel.Item;
import com.koenig.commonModel.Operation;

import org.joda.time.DateTime;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import blue.koenig.kingsfinances.model.PendingOperation;
import blue.koenig.kingsfinances.model.PendingStatus;

/**
 * Created by Thomas on 25.11.2017.
 */

public class PendingTable extends Table<PendingOperation> {
    public static final String NAME = "pendings";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_OPERATION = "operation";
    public static final String COLUMN_STATUS = "status";
    private List<String> columnNames;

    public PendingTable(SQLiteDatabase database, ReentrantLock lock) {
        super(database, lock);
        columnNames = new ArrayList<>(3);
        columnNames.add(COLUMN_DATE);
        columnNames.add(COLUMN_OPERATION);
        columnNames.add(COLUMN_STATUS);
    }

    @Override
    protected void setItem(ContentValues values, PendingOperation item) {
        values.put(COLUMN_DATE, dateTimeToValue(item.getDateTime()));
        values.put(COLUMN_OPERATION, byteableToValue(item.getOperation()));
        values.put(COLUMN_STATUS, item.getStatus().name());
    }

    @Override
    protected String getTableSpecificCreateStatement() {
        return "," + COLUMN_DATE + " LONG, " + COLUMN_OPERATION + " BLOB, " + COLUMN_STATUS + " TEXT";
    }

    @Override
    protected PendingOperation getItem(Cursor cursor) {
        Operation operation = getOperation(cursor, COLUMN_OPERATION);
        DateTime dateTime = getDateTime(cursor, COLUMN_DATE);
        PendingStatus status = PendingStatus.valueOf(getString(cursor, COLUMN_STATUS));
        return new PendingOperation(operation, status, dateTime);
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
        return columnNames;
    }

    @Override
    protected void bindItem(SQLiteStatement statement, Map<String, Integer> map, PendingOperation item) {
        statement.bindLong(map.get(COLUMN_DATE), dateTimeToValue(item.getDateTime()));
        statement.bindBlob(map.get(COLUMN_OPERATION), byteableToValue(item.getOperation()));
        statement.bindString(map.get(COLUMN_STATUS), item.getStatus().name());
    }

    public void updateStatus(PendingStatus status, String id) throws SQLException {
        List<String> columns = new ArrayList<>(1);
        columns.add(COLUMN_STATUS);
        update(id, columns, (statement, columnsMap) -> {
            statement.bindString(columnsMap.get(COLUMN_STATUS), status.name());
        });
    }

    public List<PendingOperation> getNonConfirmedOperations() {
        String query = COLUMN_STATUS + " != ?";
        String value = PendingStatus.CONFIRMED.name();
        return queryWithOneValue(query, value);
    }


}
