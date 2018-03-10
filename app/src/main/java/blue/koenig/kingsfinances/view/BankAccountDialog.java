package blue.koenig.kingsfinances.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.koenig.StringFormats;
import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.Balance;
import com.koenig.commonModel.finance.BankAccount;

import java.util.ArrayList;
import java.util.List;

import blue.koenig.kingsfamilylibrary.view.PickItemDialog;
import blue.koenig.kingsfamilylibrary.view.TextValidator;
import blue.koenig.kingsfamilylibrary.view.ViewUtils;
import blue.koenig.kingsfinances.R;

/**
 * Created by Thomas on 12.09.2015.
 */
public class BankAccountDialog {
    private final Context context;
    private BankAccount bankAccount;
    private List<User> users;
    private ConfirmListener confirmListener;
    private boolean backToOverview = false;

    public BankAccountDialog(Context context, List<User> users) {
        this.context = context;
        bankAccount = new BankAccount("", "", new ArrayList<>(), new ArrayList<>());
        this.users = users;
    }

    public BankAccountDialog(Context context, BankAccount bankAccount, List<User> users) {
        // Show overview
        this(context, users);
        this.bankAccount = bankAccount;
    }

    public void showEdit() {
        showOverview();
    }


    private void showBalance() {
        BalanceDialog balanceDialog = new BalanceDialog(context);
        balanceDialog.setConfirmListener((balance -> {
            List<Balance> balances = new ArrayList<>(1);
            balances.add(balance);
            bankAccount.setBalances(balances);
            if (backToOverview) showOverview();
            else showOwner();
        }));
        balanceDialog.showAdd();
    }


    private void showOwner() {
        int title = R.string.question_owner_bank_account;
        List<String> members = new ArrayList<>(users.size());
        for (User user : users) {
            members.add(user.getName() + " (" + user.getAbbreviation() + ")");
        }

        new PickItemDialog(context, context.getString(title), members, true, new PickItemDialog.PickListener() {
            @Override
            public void onPick(String item) {
                //not used
            }

            @Override
            public void onMultiPick(List<String> items) {
                List<User> owners = new ArrayList<>(items.size());
                for (String item : items) {
                    String[] strings = item.split("\\(");
                    // empty space at end
                    String name = strings[0].substring(0, strings[0].length() - 1);
                    // bracket at end
                    String abbreviation = strings[1].substring(0, strings[1].length() - 1);
                    for (User user : users) {
                        if (user.getName().equals(name) && user.getAbbreviation().equals(abbreviation)) {
                            owners.add(user);
                            break;
                        }
                    }
                }

                bankAccount.setOwners(owners);
                showOverview();
            }
        }).show();
    }

    private void showOverview() {
        backToOverview = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.bank_account_dialog_overview, null);
        builder.setView(layout);

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            updateFromLayout(layout);
            confirmListener.onConfirm(bankAccount);
        });
        builder.setNegativeButton(R.string.cancel, null);

        Dialog dialog = builder.create();
        dialog.show();

        updateLayout(layout, dialog);
    }

    private void updateFromLayout(View layout) {
        final EditText editBankName = layout.findViewById(R.id.editBank);
        final EditText editAccountName = layout.findViewById(R.id.edit_name);

        String name = editAccountName.getText().toString();
        bankAccount.setName(name);
        bankAccount.setBank(editBankName.getText().toString());
    }

    private void updateLayout(final View layout, final Dialog dialog) {
        final EditText editBankName = layout.findViewById(R.id.editBank);
        final EditText editAccountName = layout.findViewById(R.id.edit_name);
        final EditText editBalance = layout.findViewById(R.id.edit_balance);
        final EditText editOwner = layout.findViewById(R.id.edit_owner);
        final EditText editDate = layout.findViewById(R.id.edit_last_date);


        editOwner.setOnClickListener(v -> {
            dialog.cancel();
            updateFromLayout(layout);
            showOwner();
        });

        editDate.setOnClickListener(v -> {
            dialog.cancel();
            updateFromLayout(layout);
            showBalance();
        });


        editBankName.setText(bankAccount.getBank());
        editBalance.setText(StringFormats.INSTANCE.centsToCentString(bankAccount.getBalance()));
        editOwner.setText(StringFormats.INSTANCE.usersToAbbreviationString(bankAccount.getOwners()));
        editAccountName.setText(bankAccount.getName());
        editDate.setText(bankAccount.getDateTime().toString("dd.MM.yy"));

    }


    public void setConfirmListener(ConfirmListener confirmListener) {
        this.confirmListener = confirmListener;
    }

    public void showAdd() {
        bankName();
    }

    private void name(int titleId, final NameListener runnable) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleId);
        final View layout = LayoutInflater.from(context).inflate(R.layout.edit_name_dialog, null);
        final EditText editName = layout.findViewById(R.id.edit_name);
        ViewUtils.INSTANCE.clickOn(editName);
        builder.setView(layout);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String name = (editName).getText().toString();
            runnable.onNameSelected(name);
        });
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        button.setEnabled(false);
        editName.addTextChangedListener(new TextValidator(editName) {
            @Override
            public void validate(TextView textView, String text) {
                button.setEnabled(!text.trim().isEmpty());
            }
        });
    }

    private void bankName() {
        name(R.string.bank_name, name -> {
            bankAccount.setBank(name);
            if (backToOverview) showOverview();
            else accountName();
        });
    }

    private void accountName() {
        name(R.string.account_name, name -> {
            bankAccount.setName(name);
            if (backToOverview) showOverview();
            else showBalance();
        });
    }

    private interface NameListener {
        void onNameSelected(String name);
    }

    public interface ConfirmListener {
        void onConfirm(BankAccount bankAccount);
    }
}
