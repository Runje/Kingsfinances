package blue.koenig.kingsfinances

import android.support.test.runner.AndroidJUnit4
import blue.koenig.kingsfinances.model.FinanceModel
import blue.koenig.kingsfinances.model.PendingOperation
import blue.koenig.kingsfinances.model.PendingStatus
import com.koenig.commonModel.Operation
import com.koenig.commonModel.Operator
import com.koenig.commonModel.database.DatabaseItem
import com.koenig.commonModel.finance.CostDistribution
import com.koenig.commonModel.finance.Expenses
import com.koenig.communication.Parser
import com.koenig.communication.messages.UpdatesMessage
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.sql.SQLException
import java.util.*

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ConflictTests : DatabaseTests() {



    @Test
    @Throws(Exception::class)
    fun PendingTable() {

        val expenses = Expenses("Test", "Category", "", 506, CostDistribution(), DateTime.now(), "")
        val operation = Operation(Operator.ADD, expenses)
        val pendingOperation = PendingOperation(operation, PendingStatus.PENDING, DateTime.now())
        val id = pendingOperation.id

        // insert
        financeDatabase.addPendingOperation(pendingOperation)
        val pendingOperationFromId = financeDatabase.getPendingOperationFromId(id)
        assertEquals(pendingOperationFromId!!.id, pendingOperation.id)

        // update
        val userId = "THOMAS"
        pendingOperation.copy(status = PendingStatus.CONFIRMED)
        financeDatabase.updatePendingOperation(pendingOperation, userId)
        val updatedOperation = financeDatabase.getPendingOperationFromId(id)
        assertEquals(pendingOperation.status, updatedOperation!!.status)

        // delete
        financeDatabase.deletePendingOperation(id)
        val allPendingOperation = financeDatabase.allPendingOperation
        for (operation1 in allPendingOperation) {
            assertTrue(operation1.id !== id)
        }

    }

    @Test
    @Throws(SQLException::class)
    fun addExpensesFromServer() {
        val items = ArrayList<DatabaseItem<Expenses>>()
        val expenses = Expenses("Test", "Category", "", 506, CostDistribution(), DateTime.now(), "")
        val databaseItem = DatabaseItem(expenses, "userId")
        var expensesUpdatesMessage = UpdatesMessage(items)
        expensesUpdatesMessage.fromId = "FromId"
        expensesUpdatesMessage.toId = "ToId"
        var buffer = expensesUpdatesMessage.buffer
        buffer.position(4)
        var updatesMessage: UpdatesMessage<*> = Parser.parse(buffer) as UpdatesMessage<*>
        FinanceModel.update(financeDatabase, updatesMessage.items)
        var allExpenses = financeDatabase.allExpenses
        assertEquals(0, allExpenses.size.toLong())

        expensesUpdatesMessage = UpdatesMessage(items)
        expensesUpdatesMessage.fromId = "FromId"
        expensesUpdatesMessage.toId = "ToId"
        items.add(databaseItem)
        buffer = expensesUpdatesMessage.buffer
        buffer.position(4)
        updatesMessage = Parser.parse(buffer) as UpdatesMessage<*>
        FinanceModel.update(financeDatabase, updatesMessage.items)
        allExpenses = financeDatabase.allExpenses
        assertEquals(1, allExpenses.size.toLong())
    }
}
