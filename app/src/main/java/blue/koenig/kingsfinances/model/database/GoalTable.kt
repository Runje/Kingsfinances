package blue.koenig.kingsfinances.model.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import blue.koenig.kingsfinances.model.Goal
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 25.11.2017.
 */

class GoalTable(database: SQLiteDatabase, lock: ReentrantLock) : Table<Goal>(database, lock) {
    private val columnNames: MutableList<String>

    init {
        columnNames = listOf<String>(COLUMN_GOALS, COLUMN_USER_ID).toMutableList()
    }

    private fun goalsToByte(goals: Map<Int, Int>): ByteArray {
        val size = 2 + goals.size * 8

        val buffer = ByteBuffer.allocate(size)
        buffer.putShort(goals.size.toShort())
        for ((year, value) in goals) {
            buffer.putInt(year)
            buffer.putInt(value)
        }

        return buffer.array();
    }

    private fun bytesToGoals(bytes: ByteArray?): MutableMap<Int, Int> {
        val buffer = ByteBuffer.wrap(bytes)
        val size = buffer.short
        val goals = HashMap<Int, Int>(size.toInt())
        for (i in 1..size) {
            goals[buffer.int] = buffer.int
        }

        return goals
    }

    override fun setItem(values: ContentValues, item: Goal) {
        values.put(COLUMN_GOALS, goalsToByte(item.goals))
        values.put(COLUMN_USER_ID, item.userId)
    }

    override fun getTableSpecificCreateStatement(): String? {
        return ", $COLUMN_GOALS BLOB, $COLUMN_USER_ID TEXT"
    }

    override fun getItem(cursor: Cursor): Goal {
        val name = getString(cursor, COLUMN_NAME)
        val goals = bytesToGoals(cursor.getBlob(cursor.getColumnIndex(COLUMN_GOALS)))
        val userId = getString(cursor, COLUMN_USER_ID)
        return Goal(name, goals, userId)
    }


    override fun getTableName(): String {
        return NAME
    }

    override fun getColumnNames(): List<String> {
        return columnNames
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

    fun saveGoal(category: String, goal: Int, year: Int, statisticsUserId: String, editFromUserId: String) {
        val databaseItemFromName = getDatabaseItemFromName(category)
        databaseItemFromName?.let {
            val item = databaseItemFromName.item
            item.setGoal(year, goal)
            updateFrom(item, editFromUserId)
        } ?: run {
            val newGoal = Goal(category, mutableMapOf(Pair(year, goal)), statisticsUserId)
            addFrom(newGoal, editFromUserId)
        }
    }

}
