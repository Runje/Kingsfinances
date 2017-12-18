package blue.koenig.kingsfinances.view.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.Expenses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.inject.Inject;

import blue.koenig.kingsfamilylibrary.view.DeleteDialog;
import blue.koenig.kingsfamilylibrary.view.EditDialog;
import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.dagger.FinanceApplication;
import blue.koenig.kingsfinances.model.FinanceModel;
import blue.koenig.kingsfinances.model.PendingOperation;
import blue.koenig.kingsfinances.view.EditExpensesDialog;
import blue.koenig.kingsfinances.view.PendingView;
import blue.koenig.kingsfinances.view.lists.ExpensesAdapter;
import blue.koenig.kingsfinances.view.lists.PendingAdapter;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class PendingFragment extends Fragment implements PendingView
{
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private PendingAdapter adapter;

    @Inject
    FinanceModel model;

    public PendingFragment()
    {
        // Required empty public constructor
        logger.info("Constructor expenses fragment");
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        logger.info("Attaching pending fragment");
        ((FinanceApplication) getActivity().getApplication()).getFinanceAppComponent().inject(this);
        model.attachPendingView(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        logger.info("Creating pending fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_pending, container, false);
        logger.info("Creating view pending fragment");
        ListView listView = view.findViewById(R.id.list_pendings);
        adapter = new PendingAdapter(model.getPendingOperations(), new PendingAdapter.PendingInteractListener() {
            @Override
            public void onDelete(PendingOperation operation) {
                new DeleteDialog<>(getContext(), operation.getName(), operation, (i) -> model.deletePending(operation)).show();
            }

            @Override
            public void onSend(PendingOperation operation) {
                model.sendPending(operation);
            }
        });
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        logger.info("Resume pending fragment");
        model.attachPendingView(this);
    }

    @Override
    public void onStop() {
        logger.info("Stop pending fragment");
        model.detachPendingView();
        super.onStop();
    }

    @Override
    public void update(List<PendingOperation> pendingOperations) {
        getActivity().runOnUiThread(() -> adapter.update(pendingOperations));
    }
}