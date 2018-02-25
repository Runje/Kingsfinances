package blue.koenig.kingsfinances.model.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.koenig.commonModel.database.DatabaseItem
import com.koenig.commonModel.database.DatabaseTable
import com.koenig.commonModel.finance.BookkeepingEntry
import com.koenig.commonModel.finance.Expenses
import org.joda.time.DateTime
import java.sql.SQLException
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 30.11.2017.
 */

class ExpensesTable(database: SQLiteDatabase, lock: ReentrantLock) : BookkeepingTable<Expenses>(database, lock) {

    override val tableName = "ExpensesTable"

    override fun getBookkeepingTableSpecificCreateStatement(): String {
        return ",$DATE LONG, $STANDING_ORDER TEXT, $COMPENSATION INT"
    }

    override fun setBookkeepingItem(values: ContentValues, item: Expenses) {
        values.put(DATE, dateTimeToValue(item.date))
        values.put(STANDING_ORDER, item.standingOrder)
        values.put(COMPENSATION, item.isCompensation)
    }

    override fun bindBookkeepingItem(statement: SQLiteStatement, map: Map<String, Int>, item: Expenses) {
        statement.bindLong(map[DATE]!!, dateTimeToValue(item.date))
        statement.bindString(map[STANDING_ORDER]!!, item.standingOrder)
        statement.bindLong(map[COMPENSATION]!!, boolToValue(item.isCompensation).toLong())
    }

    override fun getBookkeepingItem(entry: BookkeepingEntry, cursor: Cursor): Expenses {
        val date = getDateTime(cursor, DATE)
        val standingOrder = getString(cursor, STANDING_ORDER)
        val isCompensation = getBool(cursor, COMPENSATION)
        return Expenses(entry, date, standingOrder, isCompensation)
    }

    override fun getBookkeepingColumnNames(): Collection<String> {
        return Arrays.asList(DATE, STANDING_ORDER, COMPENSATION)
    }

    @Throws(SQLException::class)
    fun getAllSince(updateSince: DateTime): List<Expenses> {
        return runInLockWithResult({
            val items = ArrayList<DatabaseItem<Expenses>>()

            val selectQuery = "SELECT * FROM " + tableName + " WHERE " + DATE + " >= ? AND " + DatabaseTable.COLUMN_DELETED + " != ?"

            val cursor = db.rawQuery(selectQuery, arrayOf(java.lang.Long.toString(updateSince.millis), DatabaseTable.TRUE_STRING))

            if (cursor.moveToFirst()) {
                do {
                    val databaseItem = createDatabaseItemFromCursor(cursor)
                    items.add(databaseItem)
                } while (cursor.moveToNext())
            }

            cursor.close()

            toItemList(items)
        })
    }

    companion object {
        private const val DATE = "date"
        private const val STANDING_ORDER = "standing_order"
        private const val COMPENSATION = "compensation"
    }

    val compensations: Map<DateTime, Expenses>
        get() {
            val list = getWith("$COMPENSATION = ?", arrayListOf(TRUE_STRING))
            val map = mutableMapOf<DateTime, Expenses>()
            list.forEach { map[it.item.date] = it.item }
            return map
        }


}


