package blue.koenig.kingsfinances.view;


import android.app.Activity;
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

import blue.koenig.kingsfinances.R;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ExpensesFragment extends Fragment
{
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private String LogKey = "ExpensesFragment";
    private ExpensesAdapter adapter;

    public ExpensesFragment()
    {
        // Required empty public constructor
        logger.info("Constructor expenses fragment");
    }

    @Override
    public void onAttach(Activity context)
    {
        Log.d(LogKey, "On Attach Activity");
        super.onAttach(context);
        //callback = (ExpensesListener) context;
    }

    @Override
    public void onAttach(Context context)
    {
        Log.d(LogKey, "On Attach Context");
        super.onAttach(context);
        //callback = (ExpensesListener) context;
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
        init(view);
        logger.info("Creating view expenses fragment");
        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        logger.info("Resume expenses fragment");
    }

    private void init(View view)
    {

    }


    public void updateExpenses(List<Expenses> expenses) {
        adapter.updateExpenses(expenses);
    }

    public void setFamilyMembers(List<User> members) {

        LinearLayout linearLayout = getView().findViewById(R.id.persons_container);
        linearLayout.removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, members.size());
        linearLayout.setLayoutParams(layoutParams);
        for (User member : members) {
            TextView person = (TextView) getActivity().getLayoutInflater().inflate(R.layout.expenses_person, null);
            person.setText(member.getName());
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            linearLayout.addView(person, layoutParams2);
        }

        ListView listView = getView().findViewById(R.id.list_expenses);
        adapter = new ExpensesAdapter(getContext(), null, members);
        listView.setAdapter(adapter);
    }
}
