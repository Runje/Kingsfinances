package blue.koenig.kingsfinances.model.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.koenig.commonModel.User
import com.koenig.commonModel.database.DatabaseItemTable
import com.koenig.commonModel.finance.Balance
import com.koenig.commonModel.finance.BankAccount
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 25.11.2017.
 */

class BankAccountTable(database: SQLiteDatabase, private val userService: (String) -> User?, lock: ReentrantLock) : ItemTable<BankAccount>(database, lock) {
    override val columnNames: MutableList<String>

    override val itemSpecificCreateStatement: String
        get() = ", $BANK TEXT, $BALANCES BLOB, $OWNERS TEXT"


    override val tableName: String
        get() = NAME

    init {
        columnNames = ArrayList(3)
        columnNames.add(BANK)
        columnNames.add(BALANCES)
        columnNames.add(OWNERS)
    }

    override fun setItem(values: ContentValues, item: BankAccount) {
        values.put(BANK, item.bank)
        values.put(BALANCES, Balance.listToBytes(item.balances))
        values.put(OWNERS, usersToId(item.owners))
    }

    override fun getItem(cursor: Cursor): BankAccount {
        val name = getString(cursor, DatabaseItemTable.COLUMN_NAME)
        val bank = getString(cursor, BANK)
        val balances = getBalances(cursor, BALANCES)
        val users = getUsers(userService, getString(cursor, OWNERS))
        return BankAccount(name, bank, users, balances)
    }

    private fun getBalances(cursor: Cursor, column: String): MutableList<Balance> {
        val bytes = cursor.getBlob(cursor.getColumnIndex(column))
        return Balance.getBalances(bytes)
    }


    override fun bindItem(statement: SQLiteStatement, map: Map<String, Int>, item: BankAccount) {
        statement.bindString(map[BANK]!!, item.bank)
        statement.bindBlob(map[BALANCES]!!, Balance.listToBytes(item.balances))
        statement.bindString(map[OWNERS]!!, usersToId(item.owners))
    }

    companion object {
        val NAME = "bankaccount_table"
        private val BANK = "bank"
        private val BALANCES = "balances"
        private val OWNERS = "owners"
    }

}
