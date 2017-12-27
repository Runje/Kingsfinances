package blue.koenig.kingsfinances.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.koenig.StringFormats;
import com.koenig.commonModel.finance.Balance;

import org.joda.time.DateTime;

import blue.koenig.kingsfamilylibrary.view.ViewUtils;
import blue.koenig.kingsfinances.R;

/**
 * Created by Thomas on 12.09.2015.
 */
public class BalanceDialog {
    private final Context context;
    private Balance balance;
    private ConfirmListener confirmListener;
    private boolean backToOverview = false;

    public BalanceDialog(Context context, Balance balance) {
        // Show overview
        this.context = context;
        this.balance = balance;
    }

    public BalanceDialog(Context context) {
        this.context = context;
        balance = new Balance(0, DateTime.now());
    }

    public void showEdit() {
        showOverview();
    }

    private void showBalance() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.edit_costs_dialog, null);
        final EditText editCosts = (EditText) layout.findViewById(R.id.edit_costs);
        if (balance.getBalance() != 0) {
            editCosts.setText(Float.toString(balance.getBalance()));
        }
        ViewUtils.clickOn(editCosts);
        builder.setView(layout);
        builder.setTitle(R.string.balance);
        builder.setPositiveButton("OK", (dialog, which) -> {
            try {
                float c = Float.parseFloat((editCosts).getText().toString());
                balance.setBalance((int) (c * 100));
                if (backToOverview) {
                    showOverview();
                } else {
                    showDate();
                }
            } catch (Exception e) {
                Toast.makeText(context, R.string.empty_not_allowed, Toast.LENGTH_SHORT).show();
                showBalance();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }


    private void showDate() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.date_picker, null);
        builder.setView(layout);
        builder.setTitle(R.string.date);
        builder.setNegativeButton(R.string.cancel, null);
        final DatePicker datePicker = layout.findViewById(R.id.datePicker);
        if (balance.getDate() != null) {
            ViewUtils.setDateToDatePicker(datePicker, balance.getDate());
        }
        builder.setPositiveButton("OK", (dialog, which) -> {
            DateTime date = ViewUtils.getDateFromDatePicker(datePicker);
            balance.setDate(date);
            showOverview();
        });

        builder.create().show();
    }


    private void showOverview() {
        backToOverview = true;
        if (balance.getDate() == null) {
            balance.setDate(DateTime.now());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.balance_dialog_overview, null);
        builder.setView(layout);

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            updateFromLayout(layout);
            confirmListener.onConfirm(balance);
        });
        builder.setNegativeButton(R.string.cancel, null);

        Dialog dialog = builder.create();
        dialog.show();

        updateLayout(layout, dialog);
    }

    private void updateFromLayout(View layout) {
        final EditText editBalance = (EditText) layout.findViewById(R.id.edit_balance);
        float balance = Float.parseFloat(editBalance.getText().toString());
        this.balance.setBalance((int) (100 * balance));
    }

    private void updateLayout(final View layout, final Dialog dialog) {
        final EditText editBalance = (EditText) layout.findViewById(R.id.edit_balance);
        final EditText editDate = (EditText) layout.findViewById(R.id.edit_last_date);


        editDate.setOnClickListener(v -> {
            dialog.cancel();
            updateFromLayout(layout);
            showDate();
        });


        editBalance.setText(StringFormats.centsToCentString(balance.getBalance()));
        editDate.setText(balance.getDate().toString("dd.MM.yy"));
    }

    public void setConfirmListener(ConfirmListener confirmListener) {
        this.confirmListener = confirmListener;
    }

    public void showAdd() {
        showBalance();
    }

    public interface ConfirmListener {
        void onConfirm(Balance balance);
    }
}
