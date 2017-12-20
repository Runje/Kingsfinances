package blue.koenig.kingsfinances.view.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.koenig.commonModel.finance.StandingOrder;

import java.util.List;

import blue.koenig.kingsfamilylibrary.view.DeleteDialog;
import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.model.PendingOperation;
import blue.koenig.kingsfinances.view.FinanceViewUtils;
import blue.koenig.kingsfinances.view.lists.PendingAdapter;
import blue.koenig.kingsfinances.view.lists.StandingOrderAdapter;

/**
 * Created by Thomas on 19.12.2017.
 */

public class StandingOrderFragment extends FinanceFragment {
    private StandingOrderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_standing_order, null);
        init(view);
        return view;
    }

    @Override
    protected void update() {
        update(model.getStandingOrders());
    }

    @Override
    protected void init(View view) {

        ListView listView = view.findViewById(R.id.list_standing_order);
        adapter = new StandingOrderAdapter(model.getStandingOrders(), new StandingOrderAdapter.StandingOrderInteractListener() {
            @Override
            public void onDelete(StandingOrder standingOrder) {
                new DeleteDialog<>(getActivity(), standingOrder.getName(), standingOrder, (e) -> model.deleteStandingOrder(e)).show();
            }

            @Override
            public void onEdit(StandingOrder standingOrder) {
                FinanceViewUtils.startEditStandingOrderActivity(getContext(), standingOrder);
            }
        }, model.getFamilyMembers());
        listView.setAdapter(adapter);
        initialized = true;
    }

    public void update(List<StandingOrder> standingOrders) {
        if (adapter == null) {
            logger.error("Adapter is null");
            init(getView());
        }

        adapter.update(standingOrders);
    }
}
