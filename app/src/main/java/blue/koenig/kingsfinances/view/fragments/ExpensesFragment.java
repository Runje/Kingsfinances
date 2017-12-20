package blue.koenig.kingsfinances.view.fragments;


import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.Expenses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.inject.Inject;

import blue.koenig.kingsfamilylibrary.view.DeleteDialog;
import blue.koenig.kingsfamilylibrary.view.EditDialog;
import blue.koenig.kingsfamilylibrary.view.ViewUtils;
import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.dagger.FinanceApplication;
import blue.koenig.kingsfinances.model.FinanceModel;
import blue.koenig.kingsfinances.view.EditExpensesDialog;
import blue.koenig.kingsfinances.view.FinanceViewUtils;
import blue.koenig.kingsfinances.view.lists.ExpensesAdapter;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ExpensesFragment extends FinanceFragment
{
    private ExpensesAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);
        logger.info("Creating view expenses fragment");
        init(view);
        return view;
    }

    private void initAdapter(View view)
    {
        logger.info("Init adapter");
        if (view == null) {
            return;
        }
        LinearLayout linearLayout = view.findViewById(R.id.persons_container);
        linearLayout.removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, familyMembers.size());
        linearLayout.setLayoutParams(layoutParams);
        boolean bigWidth = ViewUtils.getScreenWidth(getActivity()) > 1300;
        if (bigWidth) {
            logger.info("BIG WIDTH SCREEN");
            for (User member : familyMembers) {
                TextView person = (TextView) getActivity().getLayoutInflater().inflate(R.layout.expenses_person, null);
                person.setText(member.getName());
                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                linearLayout.addView(person, layoutParams2);
            }
        } else {
            logger.info("SMALL WIDTH SCREEN");
            linearLayout.setVisibility(View.GONE);
        }

        ListView listView = view.findViewById(R.id.list_expenses);
        adapter = new ExpensesAdapter(model.getExpenses(), bigWidth, new ExpensesAdapter.ExpensesInteractListener() {
            @Override
            public void onDelete(Expenses expenses) {
                new DeleteDialog<>(getActivity(), expenses.getName(), expenses, (e) -> model.deleteExpenses(e)).show();
            }

            @Override
            public void onEdit(Expenses expenses) {
                FinanceViewUtils.startEditExpensesActivity(getContext(), expenses);
            }
        }, familyMembers);
        listView.setAdapter(adapter);
        initialized = true;
    }




    public void updateExpenses(List<Expenses> expenses) {
        if (adapter == null) {
            logger.error("Adapter is null");
            init(getView());
        }

        adapter.update(expenses);
    }

    @Override
    protected void update() {
        updateExpenses(model.getExpenses());
    }

    protected void init(View view) {

        familyMembers = model.getFamilyMembers();
        if (familyMembers != null && familyMembers.size() > 0) {
            initAdapter(view);

            logger.info("Initialized");
        } else {
            logger.debug("Family members null");
        }
    }


}
