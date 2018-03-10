package blue.koenig.kingsfinances.model.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.koenig.commonModel.Repository.AssetsDbRepository
import com.koenig.commonModel.Repository.BankAccountRepository
import com.koenig.commonModel.database.MonthStatisticTable
import com.koenig.commonModel.database.MonthStatisticTable.Companion.ENTRY_MAP
import com.koenig.commonModel.database.MonthStatisticTable.Companion.MONTH
import com.koenig.commonModel.finance.statistics.MonthStatistic
import com.koenig.commonModel.finance.statistics.StatisticEntry
import com.koenig.commonModel.toInt
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 06.03.2018.
 */
class MonthStatisticAndroidTable(override val tableName: String, db: SQLiteDatabase, lock: ReentrantLock = ReentrantLock()) : AndroidTable<MonthStatistic>(db, lock), MonthStatisticTable {
    override fun getDatabaseItem(cursor: Cursor): MonthStatistic {
        val month = getYearMonth(cursor, MONTH)
        val entryMap = StatisticEntry.bytesToEntryMap(getBytes(cursor, ENTRY_MAP))
        return MonthStatistic(month, entryMap)
    }

    override fun setDatabaseItem(item: MonthStatistic): ContentValues {
        val values = ContentValues()
        values.put(MONTH, item.month.toInt())
        values.put(ENTRY_MAP, StatisticEntry.entryMapToBytes(item.entryMap))
        return values
    }
}

class AssetsAndroidRepository(val db: SQLiteDatabase, override val bankAccountRepository: BankAccountRepository) : AssetsDbRepository {
    override val tableCreator: (name: String) -> MonthStatisticTable
        get() = { name -> MonthStatisticAndroidTable(name, db, ReentrantLock()) }

}




