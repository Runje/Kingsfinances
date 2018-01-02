package blue.koenig.kingsfinances.view.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.Expenses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import blue.koenig.kingsfamilylibrary.model.FamilyConfig;
import blue.koenig.kingsfamilylibrary.view.DeleteDialog;
import blue.koenig.kingsfamilylibrary.view.ViewUtils;
import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.model.calculation.Debts;
import blue.koenig.kingsfinances.view.FinanceViewUtils;
import blue.koenig.kingsfinances.view.lists.ExpensesAdapter;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ExpensesFragment extends FinanceFragment
{
    private ExpensesAdapter adapter;
    private LineChart lineChart;

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
        List<Expenses> expenses = model.getExpenses();
        adapter = new ExpensesAdapter(expenses, bigWidth, new ExpensesAdapter.ExpensesInteractListener() {
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

        //bar chart
        lineChart = view.findViewById(R.id.linechart);
        List<Debts> debtsList = model.getDebts();
        lineChart.getAxisRight().setTextColor(Color.WHITE);
        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getXAxis().setTextColor(Color.WHITE);
        Legend legend = lineChart.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setEnabled(false);

        lineChart.setGridBackgroundColor(Color.BLACK);
        lineChart.setVisibleXRangeMaximum(12);

        updateLinechart(debtsList);

        initialized = true;
    }

    private List<String> debtsToXValues(List<Debts> debtsList) {
        ArrayList<String> xEntrys = new ArrayList<>(debtsList.size());
        for (Debts debt : debtsList) {
            String dateString = debt.getDate().toString("MM/yy");
            xEntrys.add(dateString);
        }

        return xEntrys;

    }

    /**
     * Return only debts from this user
     *
     * @param debts
     * @return
     */
    private LineData debtsToLineData(List<Debts> debts) {
        // One debts not possible
        if (debts.size() <= 1) return new LineData();
        Map<User, Integer> debtsMap = debts.get(1).getDebts();
        List<Integer> colors = new ArrayList<>(debts.size());
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        for (User user : debtsMap.keySet()) {
            if (user.getId().equals(FamilyConfig.getUserId(getContext()))) {
                ArrayList<Entry> userEntrys = new ArrayList<>(debts.size());
                int i = 0;
                for (Debts debt : debts) {
                    int value = debt.getDebtsFor(user);
                    userEntrys.add(new Entry(i, value / 100f));
                    if (value < 0) {
                        colors.add(Color.RED);
                    } else colors.add(Color.GREEN);

                    i++;
                }
                LineDataSet barDataSet = new LineDataSet(userEntrys, getContext().getString(R.string.debts_of, user.getName()));
                //barDataSet.setColors(colors);
                barDataSet.setColor(model.getColorFor(user));
                //barDataSet.setBarBorderWidth(20);

                barDataSet.setValueTextColor(Color.WHITE);
                dataSets.add(barDataSet);
            }
        }

        LineData lineData = new LineData(dataSets);
        //lineData.setBarWidth(0.04f);

        return lineData;
    }


    public void updateExpenses(List<Expenses> expenses) {
        if (adapter == null) {
            logger.error("Adapter is null");
            init(getView());
        }

        if (adapter != null) {
            adapter.update(expenses);
        }
    }

    @Override
    protected void update() {
        updateExpenses(model.getExpenses());
        updateLinechart(model.getDebts());
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


    public void updateDebts(List<Debts> debts) {
        if (lineChart == null) {
            logger.error("Adapter is null");
            init(getView());
        }

        if (lineChart != null) {
            updateLinechart(debts);
        }
    }

    private synchronized void updateLinechart(List<Debts> debtsList) {
        LineData lineData = debtsToLineData(debtsList);
        lineChart.setData(lineData);

        List<String> xValues = debtsToXValues(debtsList);
        //convert x values to date string
        lineChart.getXAxis().setValueFormatter((value, axis) -> {
            int intValue = (int) value;
            if (intValue > xValues.size() - 1) {
                logger.error("intvalue: " + intValue + ", xValues.size(): " + xValues.size());
                return "Error";
            }

            return xValues.get(intValue);
        });

        // show last 12 month
        //lineChart.setVisibleXRangeMaximum(12);
        lineChart.moveViewToX(Math.max(0, debtsList.size() - 12));
        lineChart.invalidate();
    }
}
