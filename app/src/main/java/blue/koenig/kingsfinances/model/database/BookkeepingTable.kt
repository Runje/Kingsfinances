package blue.koenig.kingsfinances.model.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.koenig.commonModel.database.DatabaseItemTable.Companion.COLUMN_NAME
import com.koenig.commonModel.finance.BookkeepingEntry
import com.koenig.commonModel.finance.CostDistribution
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 29.11.2017.
 */

abstract class BookkeepingTable<T : BookkeepingEntry>(database: SQLiteDatabase, lock: ReentrantLock) : ItemTable<T>(database, lock) {

    override val itemSpecificCreateStatement: String
        get() = (", "
                + CATEGORY + " TEXT,"
                + SUBCATEGORY + " TEXT,"
                + COSTS + " INT,"
                + COSTDISTRIBUTION + " BLOB"
                + bookkeepingTableSpecificCreateStatement)

    protected abstract val bookkeepingTableSpecificCreateStatement: String


    override val columnNames: List<String>
        get() {
            val columnNames = ArrayList<String>()
            columnNames.add(CATEGORY)
            columnNames.add(SUBCATEGORY)
            columnNames.add(COSTS)
            columnNames.add(COSTDISTRIBUTION)
            columnNames.addAll(bookkeepingColumnNames)
            return columnNames
        }

    protected abstract val bookkeepingColumnNames: Collection<String>

    override fun setItem(values: ContentValues, item: T) {
        values.put(CATEGORY, item.category)
        values.put(SUBCATEGORY, item.subCategory)
        values.put(COSTS, item.costs)
        values.put(COSTDISTRIBUTION, item.costDistribution.bytes)
        setBookkeepingItem(values, item)
    }

    protected abstract fun setBookkeepingItem(values: ContentValues, item: T)

    override fun bindItem(statement: SQLiteStatement, map: Map<String, Int>, item: T) {
        statement.bindString(map[CATEGORY]!!, item.category)
        statement.bindString(map[SUBCATEGORY]!!, item.subCategory)
        statement.bindLong(map[COSTS]!!, item.costs.toLong())
        statement.bindBlob(map[COSTDISTRIBUTION]!!, byteableToValue(item.costDistribution))
        bindBookkeepingItem(statement, map, item)
    }

    protected abstract fun bindBookkeepingItem(statement: SQLiteStatement, map: Map<String, Int>, item: T)

    override fun getItem(cursor: Cursor): T {
        val name = getString(cursor, COLUMN_NAME)
        val category = getString(cursor, CATEGORY)
        val subcategory = getString(cursor, SUBCATEGORY)
        val costs = getInt(cursor, COSTS)
        val costDistribution = getCostDistribution(cursor, COSTDISTRIBUTION)
        val entry = BookkeepingEntry(name, category, subcategory, costs, costDistribution)

        return getBookkeepingItem(entry, cursor)
    }

    protected abstract fun getBookkeepingItem(entry: BookkeepingEntry, cursor: Cursor): T

    private fun getCostDistribution(cursor: Cursor, columnName: String): CostDistribution {
        val buffer = ByteBuffer.wrap(cursor.getBlob(cursor.getColumnIndex(columnName)))
        return CostDistribution(buffer)
    }

    companion object {
        private val CATEGORY = "category"
        private val SUBCATEGORY = "sub_category"
        private val COSTS = "costs"
        private val COSTDISTRIBUTION = "cost_distribution"
    }
}
