package blue.koenig.kingsfinances.model.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.koenig.commonModel.Byteable
import com.koenig.commonModel.database.Database
import com.koenig.commonModel.database.DatabaseItemTable
import com.koenig.commonModel.database.DatabaseTable
import com.koenig.commonModel.database.yearMonthFromInt
import org.joda.time.DateTime
import org.joda.time.YearMonth
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*
import java.util.concurrent.locks.Lock

/**
 * Created by Thomas on 03.03.2018.
 */
abstract class AndroidTable<T>(protected var db: SQLiteDatabase, override var lock: Lock) : DatabaseTable<T> {
    protected val logger = LoggerFactory.getLogger(javaClass.simpleName)


    @Throws(SQLException::class)
    override fun create() {
        runInLock(Database.Transaction { db.execSQL(buildCreateStatement()) })
    }

    override val all: List<T>
        get() {
            return runInLockWithResult<ArrayList<T>>(Database.ResultTransaction {
                val selectQuery = "SELECT * FROM $tableName"
                val cursor = db.rawQuery(selectQuery, arrayOf())
                getListFromCursor(cursor)
            })

        }

    @Throws(SQLException::class)
    override fun add(item: T) {
        runInLock(Database.Transaction {
            db.insert(tableName, null, setDatabaseItem(item))
        })
    }

    abstract fun getDatabaseItem(cursor: Cursor): T
    abstract fun setDatabaseItem(item: T): ContentValues
    protected fun getListFromCursor(cursor: Cursor): ArrayList<T> {
        val items = ArrayList<T>()
        if (cursor.moveToFirst()) {
            do {
                val databaseItem = getDatabaseItem(cursor)
                items.add(databaseItem)
            } while (cursor.moveToNext())
        }

        cursor.close()

        return items
    }

    override fun addAll(items: Collection<T>) {
        runInTransaction({
            items.forEach {
                db.insert(tableName, null, setDatabaseItem(it))
            }
        })
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

    protected fun getDateTime(cursor: Cursor, columnName: String): DateTime {
        return DateTime(cursor.getLong(cursor.getColumnIndex(columnName)))
    }

    protected fun getYearMonth(cursor: Cursor, key: String): YearMonth {
        return yearMonthFromInt(getInt(cursor, key))
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

    protected fun getBytes(cursor: Cursor, columnName: String): ByteArray {
        return cursor.getBlob(cursor.getColumnIndex(columnName))
    }

    protected fun getStringList(cursor: Cursor, name: String): MutableList<String> {
        return DatabaseItemTable.getStringList(getString(cursor, name))
    }

    protected fun boolToValue(bool: Boolean): Short {
        return (if (bool) 1 else 0).toShort()
    }

    protected fun dateTimeToValue(date: DateTime): Long {
        return date.millis
    }

    protected fun byteableToValue(byteable: Byteable): ByteArray {
        return byteable.bytes
    }

    override fun isExisting(): Boolean {
        // is ok to always return false, because the table will only be created if it does not exist
        return false
    }

    protected interface StatementBinder {
        fun bind(statement: SQLiteStatement, columnsMap: Map<String, Int>)
    }

    fun runInTransaction(transaction: () -> Unit) {
        lock.lock()
        try {
            db.beginTransaction()
            transaction.invoke()
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            lock.unlock()
        }
    }
}