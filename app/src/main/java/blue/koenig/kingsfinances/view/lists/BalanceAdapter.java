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
    protected void initView(View convertView, Balance balance) {
        final ImageButton delete = convertView.findViewById(R.id.button_delete);
        delete.setOnClickListener(view -> {
            if (listener != null) listener.onDelete(balance);
        });

        convertView.setLongClickable(true);
        convertView.setClickable(true);

        TextView textBalance = (TextView) convertView.findViewById(R.id.text_balance);
        TextView date = (TextView) convertView.findViewById(R.id.text_date);

        textBalance.setText(StringFormats.centsToCentString(balance.getBalance()));
        if (balance.getBalance() >= 0) {
            textBalance.setTextColor(ContextCompat.getColor(convertView.getContext(), R.color.positive_highlight));
        } else {
            textBalance.setTextColor(ContextCompat.getColor(convertView.getContext(), R.color.negative_highlight));
        }

        date.setText(balance.getDate().toString("dd.MM.yy"));
    }

    @Override
    protected Comparator<Balance> getComparator() {
        return (lhs, rhs) -> rhs.getDate().compareTo(lhs.getDate());
    }

    @Override
    protected int getItemLayout() {
        return R.layout.balance_item;
    }


    public interface BalanceInteractListener {
        void onDelete(Balance balance);
    }
}
