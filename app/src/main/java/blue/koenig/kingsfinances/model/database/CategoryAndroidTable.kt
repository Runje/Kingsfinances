package blue.koenig.kingsfinances.model.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.koenig.commonModel.Category
import com.koenig.commonModel.Repository.CategoryDbRepository
import com.koenig.commonModel.database.CategoryTable
import com.koenig.commonModel.database.CategoryTable.Companion.SUBS
import com.koenig.commonModel.database.DatabaseItemTable
import com.koenig.commonModel.database.MonthStatisticTable
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 25.11.2017.
 */

class CategoryAndroidTable(database: SQLiteDatabase, lock: ReentrantLock = ReentrantLock()) : ItemTable<Category>(database, lock), CategoryTable {

    override fun setItem(values: ContentValues, item: Category) {
        values.put(SUBS, DatabaseItemTable.Companion.buildStringList(item.getSubs()))
    }

    override fun getItem(cursor: Cursor): Category {
        val subs = getStringList(cursor, SUBS)
        val name = getString(cursor, DatabaseItemTable.Companion.COLUMN_NAME)
        return Category(name, subs)
    }

    override fun bindItem(statement: SQLiteStatement, map: Map<String, Int>, item: Category) {
        statement.bindString(map[SUBS]!!, DatabaseItemTable.Companion.buildStringList(item.getSubs()))
    }
}

class CategoryAndroidRepository(override val categoryTable: CategoryTable, val db: SQLiteDatabase) : CategoryDbRepository {

    override val allCategoryAbsoluteTable = MonthStatisticAndroidTable("all_categories_absolute", db).apply { create() }
    override val allCategoryDeltaTable = MonthStatisticAndroidTable("all_categories_delta", db).apply { create() }

    override fun getTable(name: String): MonthStatisticTable {
        val table = MonthStatisticAndroidTable(name, db)
        table.create()
        return table
    }
}
