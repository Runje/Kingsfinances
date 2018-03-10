package blue.koenig.kingsfinances.model.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.koenig.commonModel.Category
import com.koenig.commonModel.Operation
import com.koenig.commonModel.Repository.CategoryRepository
import com.koenig.commonModel.database.DatabaseItemTable
import com.koenig.commonModel.database.MonthStatisticTable
import com.koenig.commonModel.finance.statistics.MonthStatistic
import io.reactivex.Observable
import org.joda.time.YearMonth
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 25.11.2017.
 */

class CategoryTable(database: SQLiteDatabase, lock: ReentrantLock) : ItemTable<Category>(database, lock) {

    override val tableSpecificCreateStatement: String
        get() = ", $SUBS TEXT"

    override val tableName: String
        get() = NAME

    override val columnNames: List<String>
        get() {
            val columnNames = ArrayList<String>()
            columnNames.add(SUBS)
            return columnNames
        }

    override fun setItem(values: ContentValues, item: Category) {
        values.put(SUBS, DatabaseItemTable.Companion.buildStringList(item.getSubs()))
    }

    override fun getItem(cursor: Cursor): Category {
        val subs = getStringList(cursor, SUBS)
        val name = getString(cursor, DatabaseItemTable.Companion.COLUMN_NAME)
        return Category(name, subs)
    }

    private fun getOperation(cursor: Cursor, columnOperation: String): Operation<*> {
        val bytes = cursor.getBlob(cursor.getColumnIndex(columnOperation))
        return Operation<Category>(ByteBuffer.wrap(bytes))
    }

    override fun bindItem(statement: SQLiteStatement, map: Map<String, Int>, item: Category) {
        statement.bindString(map[SUBS]!!, DatabaseItemTable.Companion.buildStringList(item.getSubs()))
    }

    companion object {
        val NAME = "category_table"
        private val SUBS = "subs"
    }

}

class CategoryDbRepository(val categoryTable: CategoryTable, val db: SQLiteDatabase) : CategoryRepository {
    val allCategoryAbsoluteTable = MonthStatisticAndroidTable("all_categories_absolute", db).apply { create() }
    val allCategoryDeltaTable = MonthStatisticAndroidTable("all_categories_delta", db).apply { create() }
    override val allCategoryAbsoluteStatistics: MutableMap<YearMonth, MonthStatistic>
        get() = allCategoryAbsoluteTable.allAsMap.toMutableMap()
    override val allCategoryDeltaStatistics: MutableMap<YearMonth, MonthStatistic>
        get() = allCategoryDeltaTable.allAsMap.toMutableMap()

    override fun getCategoryAbsoluteStatistics(category: String): MutableMap<YearMonth, MonthStatistic> {
        return getAbsoluteTable(category).allAsMap.toMutableMap()
    }

    override fun getCategoryDeltaStatistics(category: String): MutableMap<YearMonth, MonthStatistic> {
        return getDeltaTable(category).allAsMap.toMutableMap()
    }

    private fun getDeltaTable(category: String): MonthStatisticTable {
        return getTable(category + "_delta")
    }

    private fun getAbsoluteTable(category: String): MonthStatisticTable {
        return getTable(category + "_absolute")
    }

    private fun getTable(name: String): MonthStatisticTable {
        val table = MonthStatisticAndroidTable(name, db)
        table.create()
        return table
    }

    override fun saveAllCategoryAbsoluteStatistics(map: MutableMap<YearMonth, MonthStatistic>) {
        allCategoryAbsoluteTable.overwriteAll(map.values)
    }

    override fun saveAllCategoryDeltaStatistics(map: MutableMap<YearMonth, MonthStatistic>) {
        allCategoryDeltaTable.overwriteAll(map.values)
    }

    override fun saveCategoryAbsoluteStatistics(category: String, map: MutableMap<YearMonth, MonthStatistic>) {
        getAbsoluteTable(category).overwriteAll(map.values)
    }

    override fun saveCategoryDeltaStatistics(category: String, deltaMap: MutableMap<YearMonth, MonthStatistic>) {
        getDeltaTable(category).overwriteAll(deltaMap.values)
    }

    override val savedCategorys: List<String>
    // TODO: what about sub categories?
        get() = categoryTable.allItems.map { it.name }
    override val hasChanged: Observable<Any>
        get() = categoryTable.hasChanged.cast(Any::class.java)
}
