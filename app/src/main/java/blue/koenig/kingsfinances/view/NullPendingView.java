package blue.koenig.kingsfinances.view;

import com.koenig.commonModel.Item;

import java.util.List;

import blue.koenig.kingsfinances.model.PendingOperation;

/**
 * Created by Thomas on 27.11.2017.
 */

public class NullPendingView implements PendingView {
    @Override
    public void update(List<PendingOperation<? extends Item>> pendingOperations) {

    }
}
