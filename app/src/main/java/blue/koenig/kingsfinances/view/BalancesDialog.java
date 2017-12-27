package blue.koenig.kingsfinances.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.koenig.commonModel.finance.Balance;
import com.koenig.commonModel.finance.BankAccount;

import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.view.lists.BalanceAdapter;


/**
 * Created by Thomas on 02.11.2015.
 */
public class BalancesDialog {
    private final Context context;
    private final BalanceDialogListener listener;
    private BankAccount account;
    private String LogKey = "BalancesDialog";
    private BalanceAdapter adapter;

    public BalancesDialog(Context context, BankAccount account, BalanceDialogListener listener) {
        this.account = account;
        this.context = context;
        this.listener = listener;
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(account.toReadableString());
        final View layout = LayoutInflater.from(context).inflate(R.layout.balances_dialog, null);
        builder.setView(layout);

        builder.setPositiveButton(R.string.close, null);

        ListView listView = (ListView) layout.findViewById(R.id.list_expenses);
        adapter = new BalanceAdapter(account.getBalances(), balance -> {
            account.deleteBalance(balance);
            listener.onDelete(account, balance);
            updateAdapter(account);
        });
        listView.setAdapter(adapter);
        layout.findViewById(R.id.button_add).setOnClickListener(v -> {
            BalanceDialog addDialog = new BalanceDialog(context);
            addDialog.setConfirmListener(balance -> {
                account.addBalance(balance);
                listener.onAdd(account, balance);
                adapter.update(account.getBalances());
            });
            addDialog.showAdd();
        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    private void updateAdapter(BankAccount account) {
        adapter.update(account.getBalances());
    }

    public interface BalanceDialogListener {
        void onDelete(BankAccount account, Balance balance);

        void onAdd(BankAccount account, Balance balance);
    }
}
