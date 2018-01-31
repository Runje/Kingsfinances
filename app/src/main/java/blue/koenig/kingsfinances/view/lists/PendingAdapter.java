package blue.koenig.kingsfinances.view.lists;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.koenig.commonModel.Item;

import java.util.Comparator;
import java.util.List;

import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.model.PendingOperation;
import blue.koenig.kingsfinances.model.PendingStatus;

/**
 * Created by Thomas on 08.09.2015.
 */
public class PendingAdapter extends ListAdapter<PendingOperation> {
    protected PendingInteractListener listener;

    public PendingAdapter(List<PendingOperation<? extends Item>> pendingOperations, PendingInteractListener listener)
    {
        super(pendingOperations);
        this.listener = listener;
    }

    @Override
    protected void updateView(View convertView, PendingOperation operation, int pos) {
        final ImageButton delete = convertView.findViewById(R.id.button_delete);
        delete.setOnClickListener(view -> {
            if (listener != null) listener.onDelete(operation);
        });

        final ImageButton send = convertView.findViewById(R.id.button_send);
        if (operation.getStatus().equals(PendingStatus.CONFIRMED)) {
            send.setEnabled(false);
        } else {
            send.setEnabled(true);
        }

        send.setOnClickListener(view -> {
            if (listener != null) listener.onSend(operation);


        });

        TextView name = (TextView) convertView.findViewById(R.id.text_name);
        TextView status = (TextView) convertView.findViewById(R.id.text_status);
        TextView date = (TextView) convertView.findViewById(R.id.text_date);
        TextView function = (TextView) convertView.findViewById(R.id.text_operation);

        function.setText(operation.getOperation().getOperator().name());
        name.setText(operation.getName());
        status.setText(operation.getStatus().name());


        date.setText(operation.getDateTime().toString("dd.MM.yy"));
    }

    @Override
    protected Comparator<PendingOperation> getComparator() {
        return (lhs, rhs) -> rhs.getDateTime().compareTo(lhs.getDateTime());
    }

    @Override
    protected int getItemLayout() {
        return R.layout.pending_item;
    }


    public interface PendingInteractListener
    {
        void onDelete(PendingOperation operation);
        void onSend(PendingOperation operation);
    }
}
