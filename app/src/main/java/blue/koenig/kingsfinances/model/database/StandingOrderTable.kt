package blue.koenig.kingsfinances.model.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.koenig.commonModel.Byteable
import com.koenig.commonModel.Frequency
import com.koenig.commonModel.finance.BookkeepingEntry
import com.koenig.commonModel.finance.StandingOrder
import org.joda.time.DateTime
import java.sql.SQLException
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 30.11.2017.
 */

class StandingOrderTable(database: SQLiteDatabase, lock: ReentrantLock) : BookkeepingTable<StandingOrder>(database, lock) {

    override val tableName: String
        get() = NAME

    override fun getBookkeepingTableSpecificCreateStatement(): String {
        return ("," + FIRST_DATE + " LONG, "
                + END_DATE + " LONG, "
                + FREQUENCY + " TEXT,"
                + FREQUENCY_FACTOR + " INT, "
                + EXECUTED_EXPENSES + " BLOB ")
    }

    override fun setBookkeepingItem(values: ContentValues, item: StandingOrder) {
        values.put(FIRST_DATE, dateTimeToValue(item.firstDate))
        values.put(END_DATE, dateTimeToValue(item.endDate))
        values.put(FREQUENCY, item.frequency.name)
        values.put(FREQUENCY_FACTOR, item.frequencyFactor)
        values.put(EXECUTED_EXPENSES, Byteable.getBytes(item.executedExpenses))
    }

    override fun bindBookkeepingItem(statement: SQLiteStatement, map: Map<String, Int>, item: StandingOrder) {
        statement.bindLong(map[FIRST_DATE]!!, dateTimeToValue(item.firstDate))
        statement.bindLong(map[END_DATE]!!, dateTimeToValue(item.endDate))
        statement.bindString(map[FREQUENCY]!!, item.frequency.name)
        statement.bindLong(map[FREQUENCY_FACTOR]!!, item.frequencyFactor.toLong())
        statement.bindBlob(map[EXECUTED_EXPENSES]!!, Byteable.getBytes(item.executedExpenses))
    }

    override fun getBookkeepingItem(entry: BookkeepingEntry, cursor: Cursor): StandingOrder {
        val firstDate = getDateTime(cursor, FIRST_DATE)
        val endDate = getDateTime(cursor, END_DATE)
        val frequency = getEnum(cursor, FREQUENCY, Frequency::class.java)
        val frequencyFactor = getInt(cursor, FREQUENCY_FACTOR)
        val executedExpenses = getExecutedExpenses(cursor, EXECUTED_EXPENSES)
        return StandingOrder(entry, firstDate, endDate, frequency, frequencyFactor, executedExpenses)
    }

    fun getExecutedExpenses(cursor: Cursor, expenses: String): MutableMap<DateTime, String> {
        return Byteable.byteToShortMap(cursor.getBlob(cursor.getColumnIndex(expenses)))
    }


    override fun getBookkeepingColumnNames(): Collection<String> {
        return Arrays.asList(FIRST_DATE, END_DATE, FREQUENCY, FREQUENCY_FACTOR, EXECUTED_EXPENSES)
    }

    @Throws(SQLException::class)
    fun addExpensesToStandingOrders(standingOrderId: String, expensesId: String, dateTime: DateTime) {
        val standingOrder = getFromId(standingOrderId)!!
        standingOrder.executedExpenses.set(dateTime, expensesId)
        update(standingOrderId, mutableListOf(EXECUTED_EXPENSES), { statement, columnsMap -> statement.bindBlob(columnsMap[EXECUTED_EXPENSES]!!, Byteable.getBytes(standingOrder.executedExpenses)) })
    }

    companion object {
        val NAME = "standing_order_table"
        private val FIRST_DATE = "first_date"
        private val END_DATE = "end_date"
        private val FREQUENCY = "frequency"
        private val FREQUENCY_FACTOR = "frequency_factor"
        private val EXECUTED_EXPENSES = "executed_expenses"
    }
}

