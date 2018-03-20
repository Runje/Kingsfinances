package blue.koenig.kingsfinances.model.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.koenig.commonModel.Item
import com.koenig.commonModel.database.Database
import com.koenig.commonModel.database.DatabaseItem
import com.koenig.commonModel.database.DatabaseItemTable
import com.koenig.commonModel.database.DatabaseItemTable.Companion.COLUMN_DELETED
import com.koenig.commonModel.database.DatabaseItemTable.Companion.COLUMN_ID
import com.koenig.commonModel.database.DatabaseItemTable.Companion.COLUMN_INSERT_DATE
import com.koenig.commonModel.database.DatabaseItemTable.Companion.COLUMN_INSERT_ID
import com.koenig.commonModel.database.DatabaseItemTable.Companion.COLUMN_MODIFIED_DATE
import com.koenig.commonModel.database.DatabaseItemTable.Companion.COLUMN_MODIFIED_ID
import com.koenig.commonModel.database.DatabaseItemTable.Companion.COLUMN_NAME
import com.koenig.commonModel.database.DatabaseItemTable.Companion.FALSE_STRING
import com.koenig.commonModel.database.DatabaseItemTable.Companion.TRUE_STRING
import com.koenig.commonModel.toLocalDate
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.sql.SQLException
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 25.11.2017.
 */

abstract class ItemTable<T : Item>(db: SQLiteDatabase, lock: ReentrantLock) : DatabaseItemTable<T>, AndroidTable<DatabaseItem<T>>(db, lock) {
    override var onDeleteListeners: MutableList<DatabaseItemTable.OnDeleteListener<T>> = mutableListOf()
    override var onAddListeners: MutableList<DatabaseItemTable.OnAddListener<T>> = mutableListOf()
    override var onUpdateListeners: MutableList<DatabaseItemTable.OnUpdateListener<T>> = mutableListOf()
    override val hasChanged: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val allDeletedItems: List<T>
        @Throws(SQLException::class)
        get() = toItemList(allDeleted)

    val allDeleted: List<DatabaseItem<T>>
        @Throws(SQLException::class)
        get() = runInLockWithResult<ArrayList<DatabaseItem<T>>>(Database.ResultTransaction {

            val selectQuery = "SELECT * FROM $tableName WHERE $COLUMN_DELETED = ?"

            val cursor = db.rawQuery(selectQuery, arrayOf(TRUE_STRING))

            getListFromCursor(cursor)
        })

    override val all: List<DatabaseItem<T>>
        get() {
            return runInLockWithResult<ArrayList<DatabaseItem<T>>>(Database.ResultTransaction {
                val selectQuery = "SELECT * FROM $tableName WHERE ${DatabaseItemTable.COLUMN_DELETED} != ?"
                val cursor = db.rawQuery(selectQuery, arrayOf(DatabaseItemTable.TRUE_STRING))
                getListFromCursor(cursor)
            })

        }

    fun getWith(condition: String, argumentList: List<String>): List<DatabaseItem<T>> {
        return runInLockWithResult {
            val selectQuery = "SELECT * FROM $tableName WHERE $COLUMN_DELETED = ? AND $condition"
            val arguments = arrayListOf(FALSE_STRING)
            arguments.addAll(argumentList)
            val cursor = db.rawQuery(selectQuery, arguments.toTypedArray())

            getListFromCursor(cursor)
        }
    }
    protected val allColumnNames: MutableList<String>
        get() {
            val columns = ArrayList<String>()
            columns.addAll(baseColumnNames)
            columns.addAll(columnNames)
            return columns
        }

    override fun setDatabaseItem(item: DatabaseItem<T>): ContentValues {
        val values = ContentValues()
        values.put(COLUMN_ID, item.id)
        values.put(COLUMN_MODIFIED_ID, item.lastModifiedId)
        values.put(COLUMN_INSERT_ID, item.insertId)
        values.put(COLUMN_MODIFIED_DATE, dateTimeToValue(item.lastModifiedDate))
        values.put(COLUMN_INSERT_DATE, dateTimeToValue(item.insertDate))
        values.put(COLUMN_DELETED, boolToValue(item.isDeleted))
        values.put(COLUMN_NAME, item.name)
        setItem(values, item.item)
        return values

    }


    protected abstract fun setItem(values: ContentValues, item: T)


    @Throws(SQLException::class)
    override fun add(item: DatabaseItem<T>) {
        runInLock(Database.Transaction {
            super.add(item)
            for (onAddListener in onAddListeners) {
                // only add non deleted items to statistics
                if (!item.isDeleted) {
                    onAddListener.onAdd(item.item)
                }
            }


        })
    }

    @Throws(SQLException::class)
    override fun getDatabaseItemFromId(id: String): DatabaseItem<T>? {
        val selectQuery = "SELECT * FROM $tableName WHERE $COLUMN_ID = ?"

        val cursor = db.rawQuery(selectQuery, arrayOf(id))

        var result: DatabaseItem<T>? = null
        if (cursor.moveToFirst()) {
            result = getDatabaseItem(cursor)
        }

        cursor.close()
        return result
    }

    fun getLocalDate(cursor: Cursor, key: String): LocalDate {
        return getInt(cursor, key).toLocalDate()
    }

    override fun getDatabaseItem(cursor: Cursor): DatabaseItem<T> {
        val id = getString(cursor, COLUMN_ID)
        val lastModifiedId = getString(cursor, COLUMN_MODIFIED_ID)
        val insertId = getString(cursor, COLUMN_INSERT_ID)
        val name = getString(cursor, COLUMN_NAME)
        val deleted = getBool(cursor, COLUMN_DELETED)
        val modifiedDate = getDateTime(cursor, COLUMN_MODIFIED_DATE)
        val insertDate = getDateTime(cursor, COLUMN_INSERT_DATE)
        val item = getItem(cursor)
        item.id = id
        item.name = name
        return DatabaseItem(item, insertDate, modifiedDate, deleted, insertId, lastModifiedId)
    }


    @Throws(SQLException::class)
    fun deleteFrom(itemId: String, userId: String) {
        for (onDeleteListener in onDeleteListeners) {
            // ASSUMPTION: item was before in database else the statistics are corrupted!
            onDeleteListener.onDelete(getFromId(itemId))
        }
        val columns = ArrayList<String>()
        columns.add(COLUMN_MODIFIED_ID)
        columns.add(COLUMN_MODIFIED_DATE)
        columns.add(COLUMN_DELETED)
        val binder: StatementBinder = object : StatementBinder {
            override fun bind(statement: SQLiteStatement, columnsMap: Map<String, Int>) {
                statement.bindString(columnsMap.get(COLUMN_MODIFIED_ID)!!, userId)
                statement.bindLong(columnsMap.get(COLUMN_MODIFIED_DATE)!!, dateTimeToValue(DateTime.now()))
                statement.bindLong(columnsMap.get(COLUMN_DELETED)!!, boolToValue(true).toLong())
            }
        }

        update(itemId, columns, binder)

        hasChanged.onNext(true)
    }

    @Throws(SQLException::class)
    protected fun update(itemId: String, columns: MutableList<String>, binder: (statement: SQLiteStatement, map: Map<String, Int>) -> Unit) {
        update(itemId, columns, object : StatementBinder {
            override fun bind(statement: SQLiteStatement, columnsMap: Map<String, Int>) {
                binder(statement, columnsMap)
            }
        })
    }

    @Throws(SQLException::class)
    protected fun update(itemId: String, columns: MutableList<String>, binder: StatementBinder) {
        // don't update id!
        columns.remove(COLUMN_ID)
        val query = "UPDATE " + tableName + " SET " + getParameters(columns) + " WHERE " + COLUMN_ID + "= ?"
        // id of where clause
        columns.add(COLUMN_ID)
        val map = createMap(columns)
        val statement = db.compileStatement(query)
        statement.bindString(map[COLUMN_ID]!!, itemId)
        binder.bind(statement, map)
        val updates = statement.executeUpdateDelete()

        if (updates != 1) {
            throw SQLException("Update error: rows= " + updates)
        }
    }

    @Throws(SQLException::class)
    protected fun runTransaction(runnable: () -> Unit) {
        runTransaction(Database.Transaction(runnable))
    }

    @Throws(SQLException::class)
    protected fun runTransaction(runnable: Database.Transaction) {
        //db.beginTransaction();
        this.lock.lock()

        try {
            runnable.run()
            //db.setTransactionSuccessful();
        } finally {
            this.lock.unlock()
            //db.endTransaction();
        }
    }

    @Throws(SQLException::class)
    fun updateFromServer(items: List<DatabaseItem<T>>) {
        runTransaction({
            for (item in items) {
                val oldDatabaseItem = getDatabaseItemFromId(item.id)
                if (oldDatabaseItem == null) {
                    // new
                    add(item)
                    logger.info("Added new item: " + item.name)
                } else {
                    if (!oldDatabaseItem.isDeleted && item.isDeleted) {
                        // delete
                        for (onDeleteListener in onDeleteListeners) {
                            onDeleteListener.onDelete(oldDatabaseItem.item)
                        }
                    } else if (!oldDatabaseItem.isDeleted && !item.isDeleted) {
                        // regular update
                        for (onUpdateListener in onUpdateListeners) {
                            onUpdateListener.onUpdate(oldDatabaseItem.item, item.item)
                        }
                    }
                    // overwrite
                    overwrite(item)
                    logger.info("Overwritten item: " + item.name)
                }
            }

            hasChanged.onNext(true)
        })
    }

    @Throws(SQLException::class)
    protected fun overwrite(item: DatabaseItem<T>) {
        update(item.id, allColumnNames, { statement, map ->
            statement.bindString(map.get(COLUMN_MODIFIED_ID)!!, item.lastModifiedId)
            statement.bindString(map.get(COLUMN_INSERT_ID)!!, item.insertId)
            statement.bindString(map.get(COLUMN_NAME)!!, item.name)
            statement.bindLong(map.get(COLUMN_MODIFIED_DATE)!!, dateTimeToValue(item.lastModifiedDate))
            statement.bindLong(map.get(COLUMN_INSERT_DATE)!!, dateTimeToValue(item.insertDate))
            statement.bindLong(map.get(COLUMN_DELETED)!!, boolToValue(item.isDeleted).toLong())
            bindItem(statement, map, item.item)
        })
    }

    fun getDatabaseItemFromName(name: String): DatabaseItem<T>? {
        val selectQuery = "SELECT * FROM $tableName WHERE $COLUMN_NAME = ?"

        val cursor = db.rawQuery(selectQuery, arrayOf(name))

        var result: DatabaseItem<T>? = null
        if (cursor.moveToFirst()) {
            result = getDatabaseItem(cursor)
        }

        cursor.close()
        return result
    }

    fun getFromName(name: String): T? {
        val item = getDatabaseItemFromName(name) ?: return null

        return item.item
    }

    @Throws(SQLException::class)
    fun updateFrom(item: T, userId: String) {
        // ASSUMPTION: Updated item is not deleted!
        for (onUpdateListener in onUpdateListeners) {
            onUpdateListener.onUpdate(getFromId(item.id)!!, item)
        }

        val columns = ArrayList<String>()
        columns.add(COLUMN_MODIFIED_ID)
        columns.add(COLUMN_MODIFIED_DATE)
        columns.add(COLUMN_NAME)
        columns.addAll(columnNames)

        update(item.id, columns, { statement, map ->
            statement.bindString(map.get(COLUMN_MODIFIED_ID)!!, userId)
            statement.bindLong(map.get(COLUMN_MODIFIED_DATE)!!, dateTimeToValue(DateTime.now()))
            statement.bindString(map.get(COLUMN_NAME)!!, item.name)
            bindItem(statement, map, item)
        })

        hasChanged.onNext(true)
    }

    protected abstract fun bindItem(statement: SQLiteStatement, map: Map<String, Int>, item: T)

    protected fun dateTimeToStringValue(time: DateTime): String {
        return java.lang.Long.toString(dateTimeToValue(time))
    }

    private fun createMap(columns: List<String>): Map<String, Int> {
        val columnsMap = HashMap<String, Int>(columns.size)
        var i = 1
        for (columnName in columns) {
            columnsMap[columnName] = i
            i++
        }
        return columnsMap
    }

    private fun getParameters(columnNames: List<String>): String {
        val result = StringBuilder()
        for (name in columnNames) {
            result.append(name)
            result.append("=?, ")
        }

        return result.substring(0, result.length - 2)
    }

    protected abstract fun getItem(cursor: Cursor): T



    @Throws(SQLException::class)
    override fun getFromId(id: String): T? {
        val databaseItemFromId = getDatabaseItemFromId(id)
        return databaseItemFromId?.item
    }



    protected fun queryWithOneValueDatabaseItems(query: String, value: String): List<DatabaseItem<T>> {

        val databaseItems = ArrayList<DatabaseItem<T>>()


        val selectQuery = "SELECT * FROM $tableName WHERE $query"

        val cursor = db.rawQuery(selectQuery, arrayOf(value))

        if (cursor.moveToFirst()) {
            do {
                val b = getDatabaseItem(cursor)
                databaseItems.add(b)
            } while (cursor.moveToNext())
        }

        cursor.close()

        return databaseItems
    }

    protected fun queryWithOneValue(query: String, value: String): List<T> {
        return toItemList(queryWithOneValueDatabaseItems(query, value))
    }

}
