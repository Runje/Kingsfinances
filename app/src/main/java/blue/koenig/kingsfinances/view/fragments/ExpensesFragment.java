package blue.koenig.kingsfinances.view.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.dagger.FinanceApplication;
import blue.koenig.kingsfinances.model.FinanceModel;
import blue.koenig.kingsfinances.view.EditExpensesDialog;
import blue.koenig.kingsfinances.view.lists.ExpensesAdapter;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ExpensesFragment extends Fragment
{
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private String LogKey = "ExpensesFragment";
    private ExpensesAdapter adapter;

    @Inject
    FinanceModel model;
    private boolean initialized;
    private List<User> familyMembers;

    public ExpensesFragment()
    {
        // Required empty public constructor
        logger.info("Constructor expenses fragment");
    }

    @Override
    public void onAttach(Context context)
    {
        Log.d(LogKey, "On Attach Context");
        super.onAttach(context);
        ((FinanceApplication) getActivity().getApplication()).getFinanceAppComponent().inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        logger.info("Creating expenses fragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);
        logger.info("Creating view expenses fragment");
        init(view);
        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        logger.info("Resume expenses fragment");
    }

    private void initAdapter(View view)
    {
        if (initialized) {
            logger.debug("Adapter already initialized");
            return;
        }
        logger.info("Init adapter");
        if (view == null) {
            return;
        }
        LinearLayout linearLayout = view.findViewById(R.id.persons_container);
        linearLayout.removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, familyMembers.size());
        linearLayout.setLayoutParams(layoutParams);
        for (User member : familyMembers) {
            TextView person = (TextView) getActivity().getLayoutInflater().inflate(R.layout.expenses_person, null);
            person.setText(member.getName());
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            linearLayout.addView(person, layoutParams2);
        }

        ListView listView = view.findViewById(R.id.list_expenses);
        adapter = new ExpensesAdapter(model.getExpenses(), new ExpensesAdapter.ExpensesInteractListener() {
            @Override
            public void onDelete(Expenses expenses) {
                new DeleteDialog<>(getActivity(), expenses.getName(), expenses, (e) -> model.deleteExpenses(e)).show();
            }

            @Override
            public void onEdit(Expenses expenses) {
                new EditExpensesDialog(getContext(), expenses, model.getCategoryService(), model.getFamilyMembers(), new EditDialog.EditListener<Expenses>() {
                    @Override
                    public void onEdit(Expenses expenses) {
                        model.editExpenses(expenses);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public boolean validate(Expenses expenses) {

                        return expenses.isValid();
                    }

                    @Override
                    public int getErrorMessage() {
                        return R.string.invalid_entry;
                    }
                }).show();
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

    private void init(View view) {
        if (initialized) {
            logger.debug("Already initialized");
            return;
        }

        familyMembers = model.getFamilyMembers();
        if (familyMembers != null && familyMembers.size() > 0) {
            initAdapter(view);

            logger.info("Initialized");
        } else {
            logger.debug("Family members null");
        }
    }

    public void updateFamilyMembers(List<User> members) {
        familyMembers = members;
        init(getView());
    }
}
