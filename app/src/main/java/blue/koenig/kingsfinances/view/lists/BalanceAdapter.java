package blue.koenig.kingsfinances.view.lists;

import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.koenig.StringFormats;
import com.koenig.commonModel.finance.Balance;

import java.util.Comparator;
import java.util.List;

import blue.koenig.kingsfinances.R;

/**
 * Created by Thomas on 08.09.2015.
 */
public class BalanceAdapter extends ListAdapter<Balance> {
    protected BalanceInteractListener listener;

    public BalanceAdapter(List<Balance> balances, BalanceInteractListener listener) {
        super(balances);
        this.listener = listener;
    }

    @Override
    protected void updateView(View convertView, Balance balance, int pos) {
        final ImageButton delete = convertView.findViewById(R.id.button_delete);
        delete.setOnClickListener(view -> {
            if (listener != null) listener.onDelete(balance);
        });

        convertView.setLongClickable(true);
        convertView.setClickable(true);

        TextView textBalance = convertView.findViewById(R.id.text_balance);
        TextView date = convertView.findViewById(R.id.text_date);

        textBalance.setText(StringFormats.INSTANCE.centsToCentString(balance.getBalance()));
        if (balance.getBalance() >= 0) {
            textBalance.setTextColor(ContextCompat.getColor(convertView.getContext(), R.color.positive_highlight));
        } else {
            textBalance.setTextColor(ContextCompat.getColor(convertView.getContext(), R.color.negative_highlight));
        }

        date.setText(balance.getDay().toString("dd.MM.yy"));
    }

    @Override
    protected Comparator<Balance> getComparator() {
        return (lhs, rhs) -> rhs.getDay().compareTo(lhs.getDay());
    }

    @Override
    protected int getItemLayout() {
        return R.layout.balance_item;
    }


    public interface BalanceInteractListener {
        void onDelete(Balance balance);
    }
}
