package blue.koenig.kingsfinances;

import com.koenig.commonModel.finance.CostDistribution;
import com.koenig.commonModel.finance.Expenses;

import java.util.ArrayList;
import java.util.List;

import blue.koenig.kingsfinances.model.calculation.Debts;
import blue.koenig.kingsfinances.model.calculation.ItemSubject;
import blue.koenig.kingsfinances.model.database.Table;

import static blue.koenig.kingsfinances.TestHelper.thomas;

/**
 * Created by Thomas on 02.01.2018.
 */

public class TestExpensesSubject implements ItemSubject<Expenses> {
    private List<Table.OnDeleteListener> deleteListeners = new ArrayList<>();
    private List<Table.OnUpdateListener> updateListeners = new ArrayList<>();
    private List<Table.OnAddListener> addListeners = new ArrayList<>();

    public void addExpenses(Expenses expenses) {
        for (Table.OnAddListener addListener : addListeners) {
            addListener.onAdd(expenses);
        }
    }

    public void updateExpenses(Expenses old, Expenses newExpenses) {
        for (Table.OnUpdateListener updateListener : updateListeners) {
            updateListener.onUpdate(old, newExpenses);
        }
    }

    public void deleteExpenses(Expenses expenses) {
        for (Table.OnDeleteListener onDeleteListener : deleteListeners) {
            onDeleteListener.onDelete(expenses);
        }
    }

    @Override
    public void addDeleteListener(Table.OnDeleteListener<Expenses> listener) {
        deleteListeners.add(listener);
    }

    @Override
    public void addUpdateListener(Table.OnUpdateListener<Expenses> listener) {
        updateListeners.add(listener);
    }

    @Override
    public void addAddListener(Table.OnAddListener<Expenses> listener) {
        addListeners.add(listener);
    }

    public void updateDebts(Debts oldDebts, Debts newDebts) {
        updateExpenses(makeExpensesFromDebts(oldDebts), makeExpensesFromDebts(newDebts));
    }

    private Expenses makeExpensesFromDebts(Debts debts) {
        CostDistribution costDistribution = TestHelper.makeCostDistribution(debts.getDebtsFor(thomas), 0, 0, debts.getDebtsFor(thomas));
        return new Expenses("", "", "", costDistribution.sumReal(), costDistribution, debts.getDate(), "");
    }
}
