package blue.koenig.kingsfinances.model;

import com.koenig.commonModel.Item;
import com.koenig.commonModel.Operation;

import org.joda.time.DateTime;

/**
 * Created by Thomas on 24.11.2017.
 */
public class PendingOperation extends Item {

    private Operation operation;
    private PendingStatus status;
    private DateTime dateTime;

    public PendingOperation(Operation operation, PendingStatus status, DateTime dateTime) {
        super(operation.getId(), operation.getName());
        this.operation = operation;
        this.status = status;
        this.dateTime = dateTime;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public PendingStatus getStatus() {
        return status;
    }

    public void setStatus(PendingStatus status) {
        this.status = status;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }
}
