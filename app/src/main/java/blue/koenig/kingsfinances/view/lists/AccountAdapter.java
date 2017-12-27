package blue.koenig.kingsfinances.view.lists;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.koenig.StringFormats;
import com.koenig.commonModel.finance.BankAccount;

import java.util.Comparator;
import java.util.List;

import blue.koenig.kingsfinances.R;

/**
 * Created by Thomas on 08.09.2015.
 */
public class AccountAdapter extends ListAdapter<BankAccount> {
    protected AccountInteractListener listener;

    public AccountAdapter(List<BankAccount> accounts, AccountInteractListener listener) {
        super(accounts);
        this.listener = listener;
    }

    @Override
    protected void initView(View convertView, BankAccount account) {
        final ImageButton delete = convertView.findViewById(R.id.button_delete);
        delete.setOnClickListener(view -> {
            if (listener != null) listener.onDelete(account);
        });

        final ImageButton edit = convertView.findViewById(R.id.button_edit);
        edit.setOnClickListener(view -> {
            if (listener != null) listener.onEdit(account);
        });

        TextView bank = (TextView) convertView.findViewById(R.id.text_bankname);
        TextView accountName = (TextView) convertView.findViewById(R.id.text_name);
        TextView date = (TextView) convertView.findViewById(R.id.text_last_update);
        TextView balance = (TextView) convertView.findViewById(R.id.text_balance);
        TextView owner = (TextView) convertView.findViewById(R.id.text_owner);

        balance.setText(StringFormats.centsToEuroString(account.getBalance()));
        bank.setText(account.getBank());
        accountName.setText(account.getName());
        owner.setText(StringFormats.usersToAbbreviationString(account.getOwners()));

        date.setText(account.getDateTime().toString("dd.MM.yy"));
    }

    @Override
    protected Comparator<BankAccount> getComparator() {
        return (lhs, rhs) -> rhs.getDateTime().compareTo(lhs.getDateTime());
    }

    @Override
    protected int getItemLayout() {
        return R.layout.bank_account_item;
    }


    public interface AccountInteractListener {
        void onDelete(BankAccount account);

        void onEdit(BankAccount account);
    }
}
