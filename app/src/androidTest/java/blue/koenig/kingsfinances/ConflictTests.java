package blue.koenig.kingsfinances;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.koenig.commonModel.Operation;
import com.koenig.commonModel.Operator;
import com.koenig.commonModel.database.DatabaseItem;
import com.koenig.commonModel.finance.CostDistribution;
import com.koenig.commonModel.finance.Expenses;
import com.koenig.communication.Parser;
import com.koenig.communication.messages.UpdatesMessage;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import blue.koenig.kingsfinances.model.FinanceModel;
import blue.koenig.kingsfinances.model.PendingOperation;
import blue.koenig.kingsfinances.model.PendingStatus;
import blue.koenig.kingsfinances.model.database.FinanceDatabase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ConflictTests {

    private FinanceDatabase financeDatabase;

    @Before
    public void setup() throws SQLException {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("blue.koenig.kingsfinances", appContext.getPackageName());

        financeDatabase = new FinanceDatabase(appContext, "TestDatabase.sqlite", null);
        financeDatabase.deleteAllEntrys();
    }

    @Test
    public void PendingTable() throws Exception {

        Expenses expenses = new Expenses("Test", "Category", "", 506, new CostDistribution(), DateTime.now(),"");
        Operation operation = new Operation(Operator.ADD, expenses);
        PendingOperation pendingOperation = new PendingOperation(operation, PendingStatus.PENDING, DateTime.now());
        String id = pendingOperation.getId();

        // insert
        financeDatabase.addPendingOperation(pendingOperation);
        PendingOperation pendingOperationFromId = financeDatabase.getPendingOperationFromId(id);
        assertEquals(pendingOperationFromId.getId(), pendingOperation.getId());

        // update
        String userId = "THOMAS";
        pendingOperation.setStatus(PendingStatus.CONFIRMED);
        financeDatabase.updatePendingOperation(pendingOperation, userId);
        PendingOperation updatedOperation = financeDatabase.getPendingOperationFromId(id);
        assertEquals(pendingOperation.getStatus(), updatedOperation.getStatus());

        // delete
        financeDatabase.deletePendingOperation(id);
        List<PendingOperation> allPendingOperation = financeDatabase.getAllPendingOperation();
        for (PendingOperation operation1 : allPendingOperation) {
            assertTrue(operation1.getId() != id);
        }

    }

    @Test
    public void addExpensesFromServer() throws SQLException {
        List<DatabaseItem<Expenses>> items = new ArrayList<>();
        Expenses expenses = new Expenses("Test", "Category", "", 506, new CostDistribution(), DateTime.now(),"");
        DatabaseItem<Expenses> databaseItem = new DatabaseItem<Expenses>(expenses, "userId");
        UpdatesMessage<Expenses> expensesUpdatesMessage = new UpdatesMessage<Expenses>(items);
        expensesUpdatesMessage.setFromId("FromId");
        expensesUpdatesMessage.setToId("ToId");
        ByteBuffer buffer = expensesUpdatesMessage.getBuffer();
        buffer.position(4);
        UpdatesMessage updatesMessage = (UpdatesMessage) Parser.parse(buffer);
        FinanceModel.update(financeDatabase, updatesMessage.getItems());
        List<Expenses> allExpenses = financeDatabase.getAllExpenses();
        assertEquals(0, allExpenses.size());

        expensesUpdatesMessage = new UpdatesMessage<Expenses>(items);
        expensesUpdatesMessage.setFromId("FromId");
        expensesUpdatesMessage.setToId("ToId");
        items.add(databaseItem);
        buffer = expensesUpdatesMessage.getBuffer();
        buffer.position(4);
        updatesMessage = (UpdatesMessage) Parser.parse(buffer);
        FinanceModel.update(financeDatabase, updatesMessage.getItems());
        allExpenses = financeDatabase.getAllExpenses();
        assertEquals(1, allExpenses.size());
    }
}
