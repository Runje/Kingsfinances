package blue.koenig.kingsfinances.features.pending_operations

import blue.koenig.kingsfamilylibrary.model.communication.ServerConnection
import blue.koenig.kingsfinances.model.PendingOperation
import blue.koenig.kingsfinances.model.PendingStatus
import blue.koenig.kingsfinances.model.database.PendingTable
import com.koenig.commonModel.Component
import com.koenig.commonModel.Item
import com.koenig.commonModel.Operation
import com.koenig.commonModel.Operator
import com.koenig.commonModel.Repository.Repository
import com.koenig.communication.messages.AUDMessage
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.sql.SQLException

/**
 * Created by Thomas on 13.02.2018.
 */
class OperationExecutor(private val connection: ServerConnection, private val pendingTable: PendingTable) {
    private val logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    fun <T : Item> deleteItem(item: T, repository: Repository<T>) {
        makeDeleteOperation(item)
        repository.delete(item)
    }

    fun <T : Item> udpateItem(item: T, repository: Repository<T>) {
        makeUpdateOperation(item)
        repository.update(item)
    }

    fun <T : Item> addItem(item: T, repository: Repository<T>) {
        makeAddOperation(item)
        repository.add(item)
    }

    private fun makeUpdateOperation(item: Item) {
        makeOperation(Operator.UPDATE, item)
    }

    private fun makeAddOperation(item: Item) {
        makeOperation(Operator.ADD, item)
    }

    private fun <T : Item> makeDeleteOperation(item: T) {
        makeOperation(Operator.DELETE, item)
    }

    private fun makeOperation(operator: Operator, item: Item) {
        val operation = Operation(operator, item)
        try {
            // UserId doesn't matter
            pendingTable.addFrom(PendingOperation(operation, PendingStatus.PENDING, DateTime.now()), "USERID")
        } catch (e: SQLException) {
            logger.error("Error on operation: " + e.message)
        }

        connection.sendFamilyMessage(AUDMessage(Component.FINANCE, operation))
    }
}