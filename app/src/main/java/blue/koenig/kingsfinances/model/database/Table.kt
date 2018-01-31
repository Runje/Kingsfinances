package blue.koenig.kingsfinances.model.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import blue.koenig.kingsfinances.model.calculation.ItemSubject
import com.koenig.commonModel.Byteable
import com.koenig.commonModel.Item
import com.koenig.commonModel.database.Database
import com.koenig.commonModel.database.DatabaseItem
import com.koenig.commonModel.database.DatabaseTable
import org.joda.time.DateTime
import java.sql.SQLException
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 25.11.2017.
 */

abstract class Table<T : Item>(protected var db: SQLiteDatabase, lock: ReentrantLock) : DatabaseTable<T>(), ItemSubject<T> {
    protected var onDeleteListeners: MutableList<OnDeleteListener<T>> = ArrayList()
    protected var onAddListeners: MutableList<OnAddListener<T>> = ArrayList()
    protected var onUpdateListeners: MutableList<OnUpdateListener<T>> = ArrayList()

    val allDeletedItems: List<T>
        @Throws(SQLException::class)
        get() = toItemList(allDeleted)

    val allDeleted: List<DatabaseItem<T>>
        @Throws(SQLException::class)
        get() = runInLockWithResult<ArrayList<DatabaseItem<T>>>(Database.ResultTransaction {
            val items = ArrayList<DatabaseItem<T>>()

            val selectQuery = "SELECT * FROM " + tableName + " WHERE " + DatabaseTable.COLUMN_DELETED + " = ?"

            val cursor = db.rawQuery(selectQuery, arrayOf(DatabaseTable.TRUE_STRING))

            if (cursor.moveToFirst()) {
                do {
                    val databaseItem = createDatabaseItemFromCursor(cursor)
                    items.add(databaseItem)
                } while (cursor.moveToNext())
            }

            cursor.close()

            items
        })

    protected val allColumnNames: MutableList<String>
        get() {
            val columns = ArrayList<String>()
            columns.addAll(baseColumnNames)
            columns.addAll(columnNames)
            return columns
        }

    override val tableSpecificCreateStatement: String
        get() = ""

    init {
        //share locks between all tables of one database
        this.lock = lock
    }

    @Throws(SQLException::class)
    override fun create() {
        runInLock(Database.Transaction { db.execSQL(buildCreateStatement()) })
    }

    @Throws(SQLException::class)
    override fun getAll(): List<DatabaseItem<T>> {
        return runInLockWithResult<ArrayList<DatabaseItem<T>>>(Database.ResultTransaction {
            val items = ArrayList<DatabaseItem<T>>()

            val selectQuery = "SELECT * FROM " + tableName + " WHERE " + DatabaseTable.Companion.COLUMN_DELETED + " != ?"

            val cursor = db.rawQuery(selectQuery, arrayOf(DatabaseTable.Companion.TRUE_STRING))

            if (cursor.moveToFirst()) {
                do {
                    val databaseItem = createDatabaseItemFromCursor(cursor)
                    items.add(databaseItem)
                } while (cursor.moveToNext())
            }

            cursor.close()

            items
        })

    }

    override fun isExisting(): Boolean {
        // is ok to always return false, because the table will only be created if it does not exist
        return false
    }

    protected fun itemToValues(item: DatabaseItem<T>): ContentValues {
        val values = ContentValues()
        values.put(DatabaseTable.Companion.COLUMN_ID, item.id)
        values.put(DatabaseTable.Companion.COLUMN_MODIFIED_ID, item.lastModifiedId)
        values.put(DatabaseTable.Companion.COLUMN_INSERT_ID, item.insertId)
        values.put(DatabaseTable.Companion.COLUMN_MODIFIED_DATE, dateTimeToValue(item.lastModifiedDate))
        values.put(DatabaseTable.Companion.COLUMN_INSERT_DATE, dateTimeToValue(item.insertDate))
        values.put(DatabaseTable.Companion.COLUMN_DELETED, boolToValue(item.isDeleted))
        values.put(DatabaseTable.Companion.COLUMN_NAME, item.name)
        setItem(values, item.item)
        return values

    }

    protected abstract fun setItem(values: ContentValues, item: T)

    protected fun boolToValue(bool: Boolean): Short {
        return (if (bool) 1 else 0).toShort()
    }

    protected fun dateTimeToValue(date: DateTime): Long {
        return date.millis
    }

    protected fun byteableToValue(byteable: Byteable): ByteArray {
        return byteable.bytes
    }

    @Throws(SQLException::class)
    override fun add(databaseItem: DatabaseItem<T>) {
        runInLock(Database.Transaction {
            for (onAddListener in onAddListeners) {
                // only add non deleted items to statistics
                if (!databaseItem.isDeleted) {
                    onAddListener.onAdd(databaseItem.getItem())
                }
            }

            db.insert(tableName, null, itemToValues(databaseItem))
        })
    }

    @Throws(SQLException::class)
    override fun getDatabaseItemFromId(id: String): DatabaseItem<T>? {
        val selectQuery = "SELECT * FROM " + tableName + " WHERE " + DatabaseTable.Companion.COLUMN_ID + " = ?"

        val cursor = db.rawQuery(selectQuery, arrayOf(id))

        var result: DatabaseItem<T>? = null
        if (cursor.moveToFirst()) {
            result = createDatabaseItemFromCursor(cursor)
        }

        cursor.close()
        return result
    }

    protected fun createDatabaseItemFromCursor(cursor: Cursor): DatabaseItem<T> {
        val id = getString(cursor, DatabaseTable.Companion.COLUMN_ID)
        val lastModifiedId = getString(cursor, DatabaseTable.Companion.COLUMN_MODIFIED_ID)
        val insertId = getString(cursor, DatabaseTable.Companion.COLUMN_INSERT_ID)
        val name = getString(cursor, DatabaseTable.Companion.COLUMN_NAME)
        val deleted = getBool(cursor, DatabaseTable.Companion.COLUMN_DELETED)
        val modifiedDate = getDateTime(cursor, DatabaseTable.Companion.COLUMN_MODIFIED_DATE)
        val insertDate = getDateTime(cursor, DatabaseTable.Companion.COLUMN_INSERT_DATE)
        val item = getItem(cursor)
        item.id = id
        item.name = name
        return DatabaseItem(item, insertDate, modifiedDate, deleted, insertId, lastModifiedId)
    }


    @Throws(SQLException::class)
    override fun deleteFrom(itemId: String, userId: String) {
        for (onDeleteListener in onDeleteListeners) {
            // ASSUMPTION: item was before in database else the statistics are corrupted!
            onDeleteListener.onDelete(getFromId(itemId))
        }
        val columns = ArrayList<String>()
        columns.add(DatabaseTable.Companion.COLUMN_MODIFIED_ID)
        columns.add(DatabaseTable.Companion.COLUMN_MODIFIED_DATE)
        columns.add(DatabaseTable.Companion.COLUMN_DELETED)
        val binder: Table.StatementBinder = object : Table.StatementBinder {
            override fun bind(statement: SQLiteStatement, columnsMap: Map<String, Int>) {
                statement.bindString(columnsMap.get(DatabaseTable.Companion.COLUMN_MODIFIED_ID)!!, userId)
                statement.bindLong(columnsMap.get(DatabaseTable.Companion.COLUMN_MODIFIED_DATE)!!, dateTimeToValue(DateTime.now()))
                statement.bindLong(columnsMap.get(DatabaseTable.Companion.COLUMN_DELETED)!!, boolToValue(true).toLong())
            }
        }

        update(itemId, columns, binder)
    }

    @Throws(SQLException::class)
    protected fun update(itemId: String, columns: MutableList<String>, binder: (statement: SQLiteStatement, map: Map<String, Int>) -> Unit) {
        update(itemId, columns, object : Table.StatementBinder {
            override fun bind(statement: SQLiteStatement, columnsMap: Map<String, Int>) {
                binder(statement, columnsMap)
            }
        })
    }

    @Throws(SQLException::class)
    protected fun update(itemId: String, columns: MutableList<String>, binder: StatementBinder) {
        // don't update id!
        columns.remove(DatabaseTable.Companion.COLUMN_ID)
        val query = "UPDATE " + tableName + " SET " + getParameters(columns) + " WHERE " + DatabaseTable.Companion.COLUMN_ID + "= ?"
        // id of where clause
        columns.add(DatabaseTable.Companion.COLUMN_ID)
        val map = createMap(columns)
        val statement = db.compileStatement(query)
        statement.bindString(map[DatabaseTable.Companion.COLUMN_ID]!!, itemId)
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
                            onDeleteListener.onDelete(oldDatabaseItem.getItem())
                        }
                    } else if (!oldDatabaseItem.isDeleted && !item.isDeleted) {
                        // regular update
                        for (onUpdateListener in onUpdateListeners) {
                            onUpdateListener.onUpdate(oldDatabaseItem.getItem(), item.getItem())
                        }
                    }
                    // overwrite
                    overwrite(item)
                    logger.info("Overwritten item: " + item.name)
                }
            }
        })
    }

    @Throws(SQLException::class)
    protected fun overwrite(item: DatabaseItem<T>) {
        update(item.id, allColumnNames, { statement, map ->
            statement.bindString(map.get(DatabaseTable.Companion.COLUMN_MODIFIED_ID)!!, item.lastModifiedId)
            statement.bindString(map.get(DatabaseTable.Companion.COLUMN_INSERT_ID)!!, item.insertId)
            statement.bindString(map.get(DatabaseTable.Companion.COLUMN_NAME)!!, item.name)
            statement.bindLong(map.get(DatabaseTable.Companion.COLUMN_MODIFIED_DATE)!!, dateTimeToValue(item.lastModifiedDate))
            statement.bindLong(map.get(DatabaseTable.Companion.COLUMN_INSERT_DATE)!!, dateTimeToValue(item.insertDate))
            statement.bindLong(map.get(DatabaseTable.Companion.COLUMN_DELETED)!!, boolToValue(item.isDeleted).toLong())
            bindItem(statement, map, item.item)
        })
    }

    fun getDatabaseItemFromName(name: String): DatabaseItem<T>? {
        val selectQuery = "SELECT * FROM " + tableName + " WHERE " + DatabaseTable.Companion.COLUMN_NAME + " = ?"

        val cursor = db.rawQuery(selectQuery, arrayOf(name))

        var result: DatabaseItem<T>? = null
        if (cursor.moveToFirst()) {
            result = createDatabaseItemFromCursor(cursor)
        }

        cursor.close()
        return result
    }

    fun getFromName(name: String): T? {
        val item = getDatabaseItemFromName(name) ?: return null

        return item.item
    }

    @Throws(SQLException::class)
    override fun updateFrom(item: T, userId: String) {
        // ASSUMPTION: Updated item is not deleted!
        for (onUpdateListener in onUpdateListeners) {
            onUpdateListener.onUpdate(getFromId(item.id)!!, item)
        }

        val columns = ArrayList<String>()
        columns.add(DatabaseTable.Companion.COLUMN_MODIFIED_ID)
        columns.add(DatabaseTable.Companion.COLUMN_MODIFIED_DATE)
        columns.add(DatabaseTable.Companion.COLUMN_NAME)
        columns.addAll(columnNames)

        update(item.id, columns, { statement, map ->
            statement.bindString(map.get(DatabaseTable.Companion.COLUMN_MODIFIED_ID)!!, userId)
            statement.bindLong(map.get(DatabaseTable.Companion.COLUMN_MODIFIED_DATE)!!, dateTimeToValue(DateTime.now()))
            statement.bindString(map.get(DatabaseTable.Companion.COLUMN_NAME)!!, item.name)
            bindItem(statement, map, item)
        })

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

    protected fun getDateTime(cursor: Cursor, columnName: String): DateTime {
        return DateTime(cursor.getLong(cursor.getColumnIndex(columnName)))
    }

    protected fun getBool(cursor: Cursor, columnName: String): Boolean {
        return cursor.getShort(cursor.getColumnIndex(columnName)).toInt() != 0
    }

    protected fun getString(cursor: Cursor, columnName: String): String {
        return cursor.getString(cursor.getColumnIndex(columnName))
    }

    protected fun <T : Enum<T>> getEnum(cursor: Cursor, name: String, className: Class<T>): T {
        return java.lang.Enum.valueOf<T>(className, getString(cursor, name))
    }

    protected fun getInt(cursor: Cursor, columnName: String): Int {
        return cursor.getInt(cursor.getColumnIndex(columnName))
    }

    protected fun getStringList(cursor: Cursor, name: String): List<String> {
        return DatabaseTable.Companion.getStringList(getString(cursor, name))
    }

    @Throws(SQLException::class)
    override fun getFromId(id: String): T? {
        val databaseItemFromId = getDatabaseItemFromId(id)
        return databaseItemFromId?.item
    }

    @Throws(SQLException::class)
    override fun deleteAllEntrys() {
        db.execSQL("DELETE FROM " + tableName)
    }

    @Throws(SQLException::class)
    fun drop() {
        db.execSQL("DROP TABLE IF EXISTS " + tableName)
    }

    @Throws(SQLException::class)
    fun create(db: SQLiteDatabase) {
        this.db = db
        create()
    }

    protected fun queryWithOneValueDatabaseItems(query: String, value: String): List<DatabaseItem<T>> {

        val databaseItems = ArrayList<DatabaseItem<T>>()


        val selectQuery = "SELECT * FROM $tableName WHERE $query"

        val cursor = db.rawQuery(selectQuery, arrayOf(value))

        if (cursor.moveToFirst()) {
            do {
                val b = createDatabaseItemFromCursor(cursor)
                databaseItems.add(b)
            } while (cursor.moveToNext())
        }

        cursor.close()

        return databaseItems
    }

    protected fun queryWithOneValue(query: String, value: String): List<T> {
        return toItemList(queryWithOneValueDatabaseItems(query, value))
    }

    override fun addDeleteListener(deleteListener: OnDeleteListener<T>) {
        onDeleteListeners.add(deleteListener)
    }

    override fun addAddListener(addListener: OnAddListener<T>) {
        onAddListeners.add(addListener)
    }

    override fun addUpdateListener(updateListener: OnUpdateListener<T>) {
        onUpdateListeners.add(updateListener)
    }

    fun removeDeleteListener(deleteListener: OnDeleteListener<T>) {
        onDeleteListeners.remove(deleteListener)
    }

    fun removeAddListener(addListener: OnAddListener<T>) {
        onAddListeners.remove(addListener)
    }

    fun removeUpdateListener(updateListener: OnUpdateListener<T>) {
        onUpdateListeners.remove(updateListener)
    }

    protected interface StatementBinder {
        fun bind(statement: SQLiteStatement, columnsMap: Map<String, Int>)
    }

    interface OnDeleteListener<T> {
        fun onDelete(item: T?)
    }

    interface OnUpdateListener<T> {
        fun onUpdate(oldItem: T?, newItem: T)
    }

    interface OnAddListener<T> {
        fun onAdd(item: T?)
    }
}
