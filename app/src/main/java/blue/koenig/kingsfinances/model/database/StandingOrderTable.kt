package blue.koenig.kingsfinances.model.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.koenig.commonModel.Byteable
import com.koenig.commonModel.Frequency
import com.koenig.commonModel.finance.BookkeepingEntry
import com.koenig.commonModel.finance.StandingOrder
import com.koenig.commonModel.toInt
import org.joda.time.LocalDate
import java.sql.SQLException
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 30.11.2017.
 */

class StandingOrderTable(database: SQLiteDatabase, lock: ReentrantLock) : BookkeepingTable<StandingOrder>(database, lock) {

    override val tableName: String
        get() = NAME

    override val bookkeepingTableSpecificCreateStatement: String = ",$FIRST_DAY INT, $END_DAY INT, $FREQUENCY TEXT,$FREQUENCY_FACTOR INT, $EXECUTED_EXPENSES BLOB "

    override fun setBookkeepingItem(values: ContentValues, item: StandingOrder) {
        values.put(FIRST_DAY, item.firstDate.toInt())
        values.put(END_DAY, item.endDate.toInt())
        values.put(FREQUENCY, item.frequency.name)
        values.put(FREQUENCY_FACTOR, item.frequencyFactor)
        values.put(EXECUTED_EXPENSES, Byteable.getBytesShortMap(item.executedExpenses))
    }

    override fun bindBookkeepingItem(statement: SQLiteStatement, map: Map<String, Int>, item: StandingOrder) {
        statement.bindLong(map[FIRST_DAY]!!, item.firstDate.toInt().toLong())
        statement.bindLong(map[END_DAY]!!, item.endDate.toInt().toLong())
        statement.bindString(map[FREQUENCY]!!, item.frequency.name)
        statement.bindLong(map[FREQUENCY_FACTOR]!!, item.frequencyFactor.toLong())
        statement.bindBlob(map[EXECUTED_EXPENSES]!!, Byteable.getBytesShortMap(item.executedExpenses))
    }

    override fun getBookkeepingItem(entry: BookkeepingEntry, cursor: Cursor): StandingOrder {
        val firstDate = getLocalDate(cursor, FIRST_DAY)
        val endDate = getLocalDate(cursor, END_DAY)
        val frequency = getEnum(cursor, FREQUENCY, Frequency::class.java)
        val frequencyFactor = getInt(cursor, FREQUENCY_FACTOR)
        val executedExpenses = getExecutedExpenses(cursor, EXECUTED_EXPENSES)
        return StandingOrder(entry, firstDate, endDate, frequency, frequencyFactor, executedExpenses)
    }

    fun getExecutedExpenses(cursor: Cursor, expenses: String): MutableMap<LocalDate, String> {
        return Byteable.byteToShortMap(cursor.getBlob(cursor.getColumnIndex(expenses)))
    }


    override val bookkeepingColumnNames: Collection<String> = Arrays.asList(FIRST_DAY, END_DAY, FREQUENCY, FREQUENCY_FACTOR, EXECUTED_EXPENSES)

    @Throws(SQLException::class)
    fun addExpensesToStandingOrders(standingOrderId: String, expensesId: String, day: LocalDate) {
        val standingOrder = getFromId(standingOrderId)!!
        standingOrder.executedExpenses.set(day, expensesId)
        update(standingOrderId, mutableListOf(EXECUTED_EXPENSES), { statement, columnsMap -> statement.bindBlob(columnsMap[EXECUTED_EXPENSES]!!, Byteable.getBytesShortMap(standingOrder.executedExpenses)) })
    }

    companion object {
        val NAME = "standing_order_table"
        private val FIRST_DAY = "first_day"
        private val END_DAY = "end_day"
        private val FREQUENCY = "frequency"
        private val FREQUENCY_FACTOR = "frequency_factor"
        private val EXECUTED_EXPENSES = "executed_expenses"
    }
}

