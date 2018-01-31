package blue.koenig.kingsfinances.view.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.koenig.commonModel.Item;

import java.util.List;

import blue.koenig.kingsfamilylibrary.view.DeleteDialog;
import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.model.PendingOperation;
import blue.koenig.kingsfinances.view.PendingView;
import blue.koenig.kingsfinances.view.lists.PendingAdapter;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class PendingFragment extends FinanceFragment implements PendingView
{
    private PendingAdapter adapter;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        logger.info("Attaching pending fragment");
        model.attachPendingView(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_pending, container, false);
        logger.info("Creating view pending fragment");
        init(view);
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
    protected void update() {
        update(model.getPendingOperations());
    }

    @Override
    protected void init(View view) {
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
        initialized = true;
    }

    @Override
    public void onStop() {
        logger.info("Stop pending fragment");
        model.detachPendingView();
        super.onStop();
    }

    @Override
    public void update(List<PendingOperation<? extends Item>> pendingOperations) {
        getActivity().runOnUiThread(() -> adapter.update(pendingOperations));
    }
}
