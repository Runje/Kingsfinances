package blue.koenig.kingsfinances.model

import com.koenig.commonModel.Item
import com.koenig.commonModel.Operation

import org.joda.time.DateTime

/**
 * Created by Thomas on 24.11.2017.
 */
data class PendingOperation<T : Item>(val operation: Operation<T>, val status: PendingStatus = PendingStatus.PENDING, val dateTime: DateTime = DateTime.now()) : Item(operation.id, operation.name)
