package blue.koenig.kingsfinances.model.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import blue.koenig.kingsfinances.model.PendingOperation
import blue.koenig.kingsfinances.model.PendingStatus
import com.koenig.commonModel.Item
import com.koenig.commonModel.Operation
import com.koenig.commonModel.Operator
import java.nio.ByteBuffer
import java.sql.SQLException
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 25.11.2017.
 */

class PendingTable(database: SQLiteDatabase, lock: ReentrantLock) : ItemTable<PendingOperation<*>>(database, lock) {
    override val tableName: String
        get() = NAME
    override val tableSpecificCreateStatement: String
        get() = ",$COLUMN_DATE LONG, $COLUMN_OPERATION BLOB, $COLUMN_STATUS TEXT"
    override val columnNames: MutableList<String>
        get() = mutableListOf(COLUMN_DATE, COLUMN_OPERATION, COLUMN_STATUS)

    // oldest first
    val nonConfirmedOperations: List<PendingOperation<*>>
        get() {
            val query = COLUMN_STATUS + " != ? ORDER BY " + COLUMN_DATE
            val value = PendingStatus.CONFIRMED.name
            return queryWithOneValue(query, value)
        }

    override fun setItem(values: ContentValues, item: PendingOperation<*>) {
        values.put(COLUMN_DATE, dateTimeToValue(item.dateTime))
        values.put(COLUMN_OPERATION, byteableToValue(item.operation))
        values.put(COLUMN_STATUS, item.status.name)
    }


    override fun getItem(cursor: Cursor): PendingOperation<*> {
        val operation = getOperation(cursor, COLUMN_OPERATION)
        val dateTime = getDateTime(cursor, COLUMN_DATE)
        val status = PendingStatus.valueOf(getString(cursor, COLUMN_STATUS))
        return PendingOperation(operation, status, dateTime)
    }

    private fun getOperation(cursor: Cursor, columnOperation: String): Operation<*> {
        val bytes = cursor.getBlob(cursor.getColumnIndex(columnOperation))
        return Operation<Item>(ByteBuffer.wrap(bytes))
    }

    override fun bindItem(statement: SQLiteStatement, map: Map<String, Int>, item: PendingOperation<*>) {
        statement.bindLong(map[COLUMN_DATE]!!, dateTimeToValue(item.dateTime))
        statement.bindBlob(map[COLUMN_OPERATION]!!, byteableToValue(item.operation))
        statement.bindString(map[COLUMN_STATUS]!!, item.status.name)
    }

    @Throws(SQLException::class)
    fun updateStatus(status: PendingStatus, id: String) {
        val columns = ArrayList<String>(1)
        columns.add(COLUMN_STATUS)
        update(id, columns, { statement, columnsMap -> statement.bindString(columnsMap[COLUMN_STATUS]!!, status.name) })
    }


    fun addUpdate(item: Item, userId: String): Operation<Item> {
        val operation = Operation(Operator.UPDATE, item)
        addFrom(PendingOperation(operation), userId)
        return operation
    }

    companion object {
        val NAME = "pendings"
        val COLUMN_DATE = "day"
        val COLUMN_OPERATION = "operation"
        val COLUMN_STATUS = "status"
    }
}
