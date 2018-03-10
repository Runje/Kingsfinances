package blue.koenig.kingsfinances.model.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.koenig.commonModel.database.DatabaseItem
import com.koenig.commonModel.database.DatabaseItemTable.Companion.COLUMN_DELETED
import com.koenig.commonModel.database.DatabaseItemTable.Companion.TRUE_STRING
import com.koenig.commonModel.finance.BookkeepingEntry
import com.koenig.commonModel.finance.Expenses
import com.koenig.commonModel.toInt
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.sql.SQLException
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 30.11.2017.
 */

class ExpensesTable(database: SQLiteDatabase, lock: ReentrantLock) : BookkeepingTable<Expenses>(database, lock) {

    override val tableName = "ExpensesTable"

    override val bookkeepingTableSpecificCreateStatement: String
        get() = ",$DAY LONG, $STANDING_ORDER TEXT, $COMPENSATION INT"


    override fun setBookkeepingItem(values: ContentValues, item: Expenses) {
        values.put(DAY, item.day.toInt())
        values.put(STANDING_ORDER, item.standingOrder)
        values.put(COMPENSATION, item.isCompensation)
    }

    override fun bindBookkeepingItem(statement: SQLiteStatement, map: Map<String, Int>, item: Expenses) {
        statement.bindLong(map[DAY]!!, item.day.toInt().toLong())
        statement.bindString(map[STANDING_ORDER]!!, item.standingOrder)
        statement.bindLong(map[COMPENSATION]!!, boolToValue(item.isCompensation).toLong())
    }

    override fun getBookkeepingItem(entry: BookkeepingEntry, cursor: Cursor): Expenses {
        val date = getLocalDate(cursor, DAY)
        val standingOrder = getString(cursor, STANDING_ORDER)
        val isCompensation = getBool(cursor, COMPENSATION)
        return Expenses(entry, date, standingOrder, isCompensation)
    }


    override val bookkeepingColumnNames: Collection<String> = Arrays.asList(DAY, STANDING_ORDER, COMPENSATION)

    @Throws(SQLException::class)
    fun getAllSince(updateSince: DateTime): List<Expenses> {
        return runInLockWithResult({
            val items = ArrayList<DatabaseItem<Expenses>>()

            val selectQuery = "SELECT * FROM $tableName WHERE $DAY >= ? AND $COLUMN_DELETED != ?"

            val cursor = db.rawQuery(selectQuery, arrayOf(java.lang.Long.toString(updateSince.millis), TRUE_STRING))

            if (cursor.moveToFirst()) {
                do {
                    val databaseItem = getDatabaseItem(cursor)
                    items.add(databaseItem)
                } while (cursor.moveToNext())
            }

            cursor.close()

            toItemList(items)
        })
    }

    companion object {
        private const val DAY = "DAY"
        private const val STANDING_ORDER = "standing_order"
        private const val COMPENSATION = "compensation"
    }

    val compensations: Map<LocalDate, Expenses>
        get() {
            val list = getWith("$COMPENSATION = ?", arrayListOf(TRUE_STRING))
            val map = mutableMapOf<LocalDate, Expenses>()
            list.forEach { map[it.item.day] = it.item }
            return map
        }


}


