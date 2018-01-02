package blue.koenig.kingsfinances.model.calculation;

import android.support.annotation.NonNull;

import com.koenig.commonModel.finance.Expenses;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Thomas on 28.12.2017.
 */

public class DebtsCalculator {
    private Period period;
    private DebtsCalculatorService service;
    private List<Debts> debtsList;
    private ReentrantLock lock = new ReentrantLock();

    public DebtsCalculator(Period period, ItemSubject<Expenses> expensesTable, DebtsCalculatorService service) {
        this.period = period;
        this.service = service;
        debtsList = service.getSavedSortedDebts();
        expensesTable.addAddListener(item -> addExpenses(item));
        expensesTable.addDeleteListener(item -> deleteExpenses(item));
        expensesTable.addUpdateListener((oldItem, newItem) -> updateExpenses(oldItem, newItem));
    }

    public static List<Debts> updateDebts(Debts debtsDelta, Period period, List<Debts> debtsList) {
        DateTime date = debtsDelta.getDate();
        DateTime startDate = getStartDate(date);
        DateTime nextDate = startDate.plus(period);

        if (debtsList.size() == 0) {
            debtsList.add(new Debts(startDate));
            debtsDelta.setDate(nextDate);
            debtsList.add(debtsDelta);
            return debtsList;
        } else {
            for (Debts debts : debtsList) {
                // change all debts after the delta
                if (!debts.getDate().isBefore(date)) {
                    debts.addDebts(debtsDelta);
                }
            }

            DateTime firstDate = debtsList.get(0).getDate();
            // date of last debt + 1
            Debts lastDebts = debtsList.get(debtsList.size() - 1);
            if (lastDebts.getDate().isBefore(debtsDelta.getDate())) {
                nextDate = lastDebts.getDate().plus(period);

                // add debts after list
                while (date.isAfter(nextDate)) {
                    Debts debts = new Debts(lastDebts);
                    // add delta only if it next date is after date of delta
                    if (nextDate.isAfter(debtsDelta.getDate())) {
                        debts.addDebts(debtsDelta);
                    }

                    debts.setDate(nextDate);
                    debtsList.add(debts);
                    nextDate = nextDate.plus(period);
                }
                Debts debts = new Debts(lastDebts);
                debts.addDebts(debtsDelta);
                debts.setDate(nextDate);
                debtsList.add(new Debts(debts));
            } else if (firstDate.isAfter(debtsDelta.getDate())) {

                // add missing debts before all

                List<Debts> newDebtsAtBeginning = new ArrayList<>(Months.monthsBetween(startDate, firstDate).getMonths() + debtsList.size());
                if (firstDate.isAfter(startDate)) {
                    newDebtsAtBeginning.add(new Debts(startDate));
                }

                while (firstDate.isAfter(nextDate)) {
                    newDebtsAtBeginning.add(new Debts(nextDate, debtsDelta.getDebts()));
                    nextDate = nextDate.plus(period);
                }

                // concatenate both lists
                newDebtsAtBeginning.addAll(debtsList);
                return newDebtsAtBeginning;
            }

            return debtsList;
        }

    }

    @NonNull
    private static DateTime getStartDate(DateTime date) {
        return date.dayOfMonth().get() == 1 ? date.minus(Period.months(1)) : date.withDayOfMonth(1);
    }

    @Deprecated
    public static List<Debts> recalculateAll(List<Expenses> expensesList, Period period) {
        if (expensesList.size() == 0) return new ArrayList<>();

        DateTime date = expensesList.get(0).getDate();
        // if it is already the first of the month take the month before as start date
        DateTime startDate = getStartDate(date);
        DateTime nextDate = startDate.plus(period);
        int size = Months.monthsBetween(startDate, expensesList.get(expensesList.size() - 1).getDate()).getMonths() + 2;
        List<Debts> debtsList = new ArrayList<>(size);
        Debts debts = new Debts();
        debts.setDate(startDate);
        debtsList.add(new Debts(debts));
        for (Expenses expenses : expensesList) {
            while (expenses.getDate().isAfter(nextDate)) {
                debts.setDate(nextDate);
                // add copy
                debtsList.add(new Debts(debts));
                nextDate = nextDate.plus(period);
            }

            debts.addCostDistribution(expenses.getCostDistribution());
        }

        debts.setDate(nextDate);
        debtsList.add(debts);
        return debtsList;
    }

    private void updateExpenses(Expenses oldItem, Expenses newItem) {
        if (newItem.getDate().equals(oldItem.getDate())) {
            Debts debts = new Debts(newItem.getDate(), newItem.getCostDistribution());
            debts.subtractDebts(new Debts(oldItem.getDate(), oldItem.getCostDistribution()));
            updateDebts(debts);
        } else {
            // if date has changed, delete old one and add new item
            deleteExpenses(oldItem);
            addExpenses(newItem);
        }
    }

    private void deleteExpenses(Expenses item) {
        Debts debts = new Debts(item.getDate());
        debts.subtractCostDistribution(item.getCostDistribution());
        updateDebts(debts);
    }

    private void addExpenses(Expenses item) {
        updateDebts(new Debts(item.getDate(), item.getCostDistribution()));
    }

    private void updateDebts(Debts debtsDelta) {
        lock.lock();
        debtsList = updateDebts(debtsDelta, period, debtsList);
        service.saveDebts(debtsList);
        lock.unlock();

    }

    public List<Debts> getDebts() {
        return debtsList;
    }
}
