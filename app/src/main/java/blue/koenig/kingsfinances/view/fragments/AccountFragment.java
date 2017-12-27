package blue.koenig.kingsfinances.view.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.koenig.commonModel.finance.Balance;
import com.koenig.commonModel.finance.BankAccount;

import java.util.List;

import blue.koenig.kingsfamilylibrary.view.DeleteDialog;
import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.view.BalancesDialog;
import blue.koenig.kingsfinances.view.lists.AccountAdapter;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AccountFragment extends FinanceFragment {
    private AccountAdapter adapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        logger.info("Attaching account fragment");
        //model.attachPendingView(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        logger.info("Creating view account fragment");
        init(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        logger.info("Resume account fragment");
    }

    @Override
    protected void update() {
        update(model.getBankAccounts());
    }

    @Override
    protected void init(View view) {
        ListView listView = view.findViewById(R.id.list_accounts);
        adapter = new AccountAdapter(model.getBankAccounts(), new AccountAdapter.AccountInteractListener() {
            @Override
            public void onDelete(BankAccount account) {
                new DeleteDialog<>(getContext(), account.getName(), account, (i) -> model.deleteBankAccount(account)).show();
            }

            @Override
            public void onEdit(BankAccount account) {
                new BalancesDialog(getContext(), account, new BalancesDialog.BalanceDialogListener() {
                    @Override
                    public void onDelete(BankAccount account, Balance balance) {
                        model.deleteBalance(account, balance);
                        update();
                    }

                    @Override
                    public void onAdd(BankAccount account, Balance balance) {
                        model.addBalance(account, balance);
                        update();
                    }
                }).show();
            }
        });
        listView.setAdapter(adapter);
        initialized = true;
    }

    @Override
    public void onStop() {
        logger.info("Stop pending fragment");
        model.detachPendingView();
        super.onStop();
    }

    public void update(List<BankAccount> accounts) {
        getActivity().runOnUiThread(() -> adapter.update(accounts));
    }
}
