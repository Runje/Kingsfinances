package blue.koenig.kingsfinances.model.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.koenig.commonModel.Byteable.Companion.bytesToGoals
import com.koenig.commonModel.Byteable.Companion.goalsToByte
import com.koenig.commonModel.Goal
import com.koenig.commonModel.database.DatabaseItemTable.Companion.COLUMN_NAME
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 25.11.2017.
 */

class GoalTable(database: SQLiteDatabase, lock: ReentrantLock) : ItemTable<Goal>(database, lock) {

    override val tableName: String
        get() = NAME
    override val columnNames: MutableList<String>
        get () = listOf<String>(COLUMN_GOALS, COLUMN_USER_ID).toMutableList()

    override val tableSpecificCreateStatement: String
        get() = ", $COLUMN_GOALS BLOB, $COLUMN_USER_ID TEXT"

    override fun setItem(values: ContentValues, item: Goal) {
        values.put(COLUMN_GOALS, goalsToByte(item.goals))
        values.put(COLUMN_USER_ID, item.userId)
    }


    override fun getItem(cursor: Cursor): Goal {
        val name = getString(cursor, COLUMN_NAME)
        val goals = bytesToGoals(cursor.getBlob(cursor.getColumnIndex(COLUMN_GOALS)))
        val userId = getString(cursor, COLUMN_USER_ID)
        return Goal(name, goals, userId)
    }


    override fun bindItem(statement: SQLiteStatement, map: Map<String, Int>, item: Goal) {
        statement.bindBlob(map[COLUMN_GOALS]!!, goalsToByte(item.goals))
        statement.bindString(map[COLUMN_USER_ID]!!, item.userId)
    }

    companion object {
        val NAME = "goals"
        val COLUMN_GOALS = "value"
        val COLUMN_USER_ID = "userId"
    }

    fun saveGoal(category: String, goal: Int, year: Int, statisticsUserId: String, editFromUserId: String): Goal {
        val databaseItemFromName = getDatabaseItemFromName(category)
        databaseItemFromName?.let {
            val item = databaseItemFromName.item
            item.setGoal(year, goal)
            updateFrom(item, editFromUserId)
            return item
        } ?: run {
            val newGoal = Goal(category, mutableMapOf(Pair(year, goal)), statisticsUserId)
            addFrom(newGoal, editFromUserId)
            return newGoal
        }
    }

}
