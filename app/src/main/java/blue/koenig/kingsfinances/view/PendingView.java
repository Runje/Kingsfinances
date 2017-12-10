package blue.koenig.kingsfinances.view;

import java.util.List;

import blue.koenig.kingsfinances.model.PendingOperation;

/**
 * Created by Thomas on 27.11.2017.
 */

public interface PendingView {

    void update(List<PendingOperation> pendingOperations);
}
