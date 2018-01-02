package blue.koenig.kingsfinances;

import com.koenig.commonModel.finance.CostDistribution;
import com.koenig.commonModel.finance.Expenses;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import blue.koenig.kingsfinances.model.calculation.Debts;
import blue.koenig.kingsfinances.model.calculation.DebtsCalculator;
import blue.koenig.kingsfinances.model.calculation.DebtsCalculatorService;

import static blue.koenig.kingsfinances.TestHelper.makeCostDistribution;
import static blue.koenig.kingsfinances.TestHelper.milena;
import static blue.koenig.kingsfinances.TestHelper.thomas;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class CalculatorTests {

    public Expenses makeExpenses(CostDistribution costDistribution, DateTime dateTime) {
        return new Expenses("", "", "", costDistribution.sumReal(), costDistribution, dateTime, "");
    }


    public DateTime getDateTime(int year, int month, int day) {

        return new DateTime(year, month, day, 0, 0);
    }
    @Test
    public void debtsCalculation1() throws Exception {
        List<Expenses> expensesList = new ArrayList<>(1);
        expensesList.add(makeExpenses(makeCostDistribution(-10, 0, 0, -10), getDateTime(2017, 1, 1)));
        List<Debts> debts = DebtsCalculator.recalculateAll(expensesList, Period.months(1));
        Assert.assertEquals(2, debts.size());

        Debts debt = debts.get(0);
        Assert.assertEquals(getDateTime(2016, 12, 1), debt.getDate());
        Assert.assertEquals(0, debt.getDebtsFor(thomas));
        Assert.assertEquals(0, debt.getDebtsFor(milena));

        debt = debts.get(1);
        Assert.assertEquals(getDateTime(2017, 1, 1), debt.getDate());
        Assert.assertEquals(-10, debt.getDebtsFor(thomas));
        Assert.assertEquals(10, debt.getDebtsFor(milena));
    }

    @Test
    public void debtsCalculation2() throws Exception {
        List<Expenses> expensesList = new ArrayList<>(2);
        expensesList.add(makeExpenses(makeCostDistribution(-10, 0, 0, -10), getDateTime(2017, 1, 1)));
        expensesList.add(makeExpenses(makeCostDistribution(-5, -10, -5, 0), getDateTime(2017, 1, 31)));
        List<Debts> debts = DebtsCalculator.recalculateAll(expensesList, Period.months(1));
        Assert.assertEquals(3, debts.size());

        Debts debt = debts.get(0);
        Assert.assertEquals(getDateTime(2016, 12, 1), debt.getDate());
        Assert.assertEquals(0, debt.getDebtsFor(thomas));
        Assert.assertEquals(0, debt.getDebtsFor(milena));

        debt = debts.get(1);
        Assert.assertEquals(getDateTime(2017, 1, 1), debt.getDate());
        Assert.assertEquals(-10, debt.getDebtsFor(thomas));
        Assert.assertEquals(10, debt.getDebtsFor(milena));

        debt = debts.get(2);
        Assert.assertEquals(getDateTime(2017, 2, 1), debt.getDate());
        Assert.assertEquals(-5, debt.getDebtsFor(thomas));
        Assert.assertEquals(5, debt.getDebtsFor(milena));
    }

    @Test
    public void debtsCalculation3() throws Exception {
        List<Expenses> expensesList = new ArrayList<>(3);
        expensesList.add(makeExpenses(makeCostDistribution(-10, 0, 0, -10), getDateTime(2017, 1, 1)));
        expensesList.add(makeExpenses(makeCostDistribution(-10, 0, 0, -10), getDateTime(2017, 1, 1)));
        expensesList.add(makeExpenses(makeCostDistribution(-5, -10, -5, 0), getDateTime(2017, 1, 31)));
        expensesList.add(makeExpenses(makeCostDistribution(0, 300, 300, 0), getDateTime(2017, 3, 2)));
        List<Debts> debts = DebtsCalculator.recalculateAll(expensesList, Period.months(1));
        Assert.assertEquals(5, debts.size());

        Debts debt = debts.get(0);
        Assert.assertEquals(getDateTime(2016, 12, 1), debt.getDate());
        Assert.assertEquals(0, debt.getDebtsFor(thomas));
        Assert.assertEquals(0, debt.getDebtsFor(milena));

        debt = debts.get(1);
        Assert.assertEquals(getDateTime(2017, 1, 1), debt.getDate());
        Assert.assertEquals(-20, debt.getDebtsFor(thomas));
        Assert.assertEquals(20, debt.getDebtsFor(milena));

        debt = debts.get(2);
        Assert.assertEquals(getDateTime(2017, 2, 1), debt.getDate());
        Assert.assertEquals(-15, debt.getDebtsFor(thomas));
        Assert.assertEquals(15, debt.getDebtsFor(milena));

        debt = debts.get(3);
        Assert.assertEquals(getDateTime(2017, 3, 1), debt.getDate());
        Assert.assertEquals(-15, debt.getDebtsFor(thomas));
        Assert.assertEquals(15, debt.getDebtsFor(milena));

        debt = debts.get(4);
        Assert.assertEquals(getDateTime(2017, 4, 1), debt.getDate());
        Assert.assertEquals(-315, debt.getDebtsFor(thomas));
        Assert.assertEquals(315, debt.getDebtsFor(milena));
    }


    @Test
    public void emptyDebts() {
        List<Debts> debtsList = DebtsCalculator.updateDebts(makeDebts(-10, 17, 1, 2), Period.months(1), new ArrayList<>());
        Assert.assertEquals(2, debtsList.size());

        assertDebtsList(0, getDateTime(17, 1, 1), 0, debtsList);
        assertDebtsList(1, getDateTime(17, 2, 1), -10, debtsList);
    }

    @Test
    public void addOneBefore() {
        List<Debts> debtsList = DebtsCalculator.updateDebts(makeDebts(-10, 17, 1, 2), Period.months(1), makeDebtsList(getDateTime(17, 2, 1), new int[]{0, 20}));
        Assert.assertEquals(3, debtsList.size());

        assertDebtsList(0, getDateTime(17, 1, 1), 0, debtsList);
        assertDebtsList(1, getDateTime(17, 2, 1), -10, debtsList);
        assertDebtsList(2, getDateTime(17, 3, 1), 10, debtsList);
    }

    @Test
    public void addBefore() {
        List<Debts> debtsList = DebtsCalculator.updateDebts(makeDebts(-10, 17, 1, 2), Period.months(1), makeDebtsList(getDateTime(17, 1, 1), new int[]{0, 20}));
        Assert.assertEquals(2, debtsList.size());

        assertDebtsList(0, getDateTime(17, 1, 1), 0, debtsList);
        assertDebtsList(1, getDateTime(17, 2, 1), 10, debtsList);
    }

    private List<Debts> makeDebtsList(DateTime startDate, int[] debts) {
        List<Debts> debtsList = new ArrayList<>(debts.length);
        for (int i = 0; i < debts.length; i++) {
            debtsList.add(makeDebts(startDate, debts[i]));
            startDate = startDate.plus(Period.months(1));
        }

        return debtsList;
    }

    private Debts makeDebts(DateTime date, int debts) {
        return new Debts(date, makeCostDistribution(debts, 0, 0, debts));
    }

    @Test
    public void addWayBefore() {
        List<Debts> debtsList = DebtsCalculator.updateDebts(makeDebts(-10, 16, 6, 2), Period.months(1), makeDebtsList(getDateTime(17, 1, 1), new int[]{0, 20}));
        Assert.assertEquals(9, debtsList.size());

        assertDebtsList(0, getDateTime(16, 6, 1), 0, debtsList);
        assertDebtsList(1, getDateTime(16, 7, 1), -10, debtsList);
        assertDebtsList(2, getDateTime(16, 8, 1), -10, debtsList);
        assertDebtsList(3, getDateTime(16, 9, 1), -10, debtsList);
        assertDebtsList(4, getDateTime(16, 10, 1), -10, debtsList);
        assertDebtsList(5, getDateTime(16, 11, 1), -10, debtsList);
        assertDebtsList(6, getDateTime(16, 12, 1), -10, debtsList);
        assertDebtsList(7, getDateTime(17, 1, 1), -10, debtsList);
        assertDebtsList(8, getDateTime(17, 2, 1), 10, debtsList);
    }

    @Test
    public void addMiddle() {
        List<Debts> debtsList = DebtsCalculator.updateDebts(makeDebts(-10, 16, 12, 2), Period.months(1),
                makeDebtsList(getDateTime(16, 6, 1), new int[]{0, 20, 30, 40, 40, 40, 50, -20, 0}));
        Assert.assertEquals(9, debtsList.size());

        assertDebtsList(0, getDateTime(16, 6, 1), 0, debtsList);
        assertDebtsList(1, getDateTime(16, 7, 1), 20, debtsList);
        assertDebtsList(2, getDateTime(16, 8, 1), 30, debtsList);
        assertDebtsList(3, getDateTime(16, 9, 1), 40, debtsList);
        assertDebtsList(4, getDateTime(16, 10, 1), 40, debtsList);
        assertDebtsList(5, getDateTime(16, 11, 1), 40, debtsList);
        assertDebtsList(6, getDateTime(16, 12, 1), 50, debtsList);
        assertDebtsList(7, getDateTime(17, 1, 1), -30, debtsList);
        assertDebtsList(8, getDateTime(17, 2, 1), -10, debtsList);
    }

    @Test
    public void addOneEnd() {
        List<Debts> debtsList = DebtsCalculator.updateDebts(makeDebts(-10, 17, 2, 2), Period.months(1),
                makeDebtsList(getDateTime(17, 1, 1), new int[]{0, 20}));
        Assert.assertEquals(3, debtsList.size());

        assertDebtsList(0, getDateTime(17, 1, 1), 0, debtsList);
        assertDebtsList(1, getDateTime(17, 2, 1), 20, debtsList);
        assertDebtsList(2, getDateTime(17, 3, 1), 10, debtsList);
    }

    @Test
    public void addWayEnd() {
        List<Debts> debtsList = DebtsCalculator.updateDebts(makeDebts(-10, 17, 4, 2), Period.months(1),
                makeDebtsList(getDateTime(17, 1, 1), new int[]{0, 20}));
        Assert.assertEquals(5, debtsList.size());

        assertDebtsList(0, getDateTime(17, 1, 1), 0, debtsList);
        assertDebtsList(1, getDateTime(17, 2, 1), 20, debtsList);
        assertDebtsList(2, getDateTime(17, 3, 1), 20, debtsList);
        assertDebtsList(3, getDateTime(17, 4, 1), 20, debtsList);
        assertDebtsList(4, getDateTime(17, 5, 1), 10, debtsList);
    }


    private void assertDebtsList(int index, DateTime dateTime, int debts, List<Debts> debtsList) {
        Debts debt = debtsList.get(index);
        Assert.assertEquals(dateTime, debt.getDate());
        Assert.assertEquals(debts, debt.getDebtsFor(thomas));
        Assert.assertEquals(-debts, debt.getDebtsFor(milena));
    }

    private Debts makeDebts(int debts, int year, int month, int day) {
        return makeDebts(getDateTime(year, month, day), debts);
    }

    @Test
    public void editFirst() {
        TestExpensesSubject expensesItemSubject = new TestExpensesSubject();
        DebtsCalculator debtsCalculator = new DebtsCalculator(Period.months(1), expensesItemSubject,
                getCalculatorService(makeDebtsList(getDateTime(17, 1, 1), new int[]{0, 0, 0, -10})));
        expensesItemSubject.updateDebts(makeDebts(getDateTime(17, 1, 2), 0), makeDebts(getDateTime(17, 1, 2), 1));
        List<Debts> debtsList = debtsCalculator.getDebts();

        Assert.assertEquals(4, debtsList.size());

        assertDebtsList(0, getDateTime(17, 1, 1), 0, debtsList);
        assertDebtsList(1, getDateTime(17, 2, 1), 1, debtsList);
        assertDebtsList(2, getDateTime(17, 3, 1), 1, debtsList);
        assertDebtsList(3, getDateTime(17, 4, 1), -9, debtsList);
    }

    @Test
    public void editDateFromFirst() {
        TestExpensesSubject expensesItemSubject = new TestExpensesSubject();
        DebtsCalculator debtsCalculator = new DebtsCalculator(Period.months(1), expensesItemSubject,
                getCalculatorService(makeDebtsList(getDateTime(17, 1, 1), new int[]{0, 10, 0, -10})));
        expensesItemSubject.updateDebts(makeDebts(getDateTime(17, 1, 2), 10), makeDebts(getDateTime(17, 4, 2), 10));
        List<Debts> debtsList = debtsCalculator.getDebts();

        Assert.assertEquals(5, debtsList.size());

        assertDebtsList(0, getDateTime(17, 1, 1), 0, debtsList);
        assertDebtsList(1, getDateTime(17, 2, 1), 0, debtsList);
        assertDebtsList(2, getDateTime(17, 3, 1), -10, debtsList);
        assertDebtsList(3, getDateTime(17, 4, 1), -20, debtsList);
        assertDebtsList(4, getDateTime(17, 5, 1), -10, debtsList);
    }

    private DebtsCalculatorService getCalculatorService(List<Debts> debtsList) {
        return new DebtsCalculatorService() {
            @Override
            public List<Debts> getSavedSortedDebts() {
                return debtsList;
            }

            @Override
            public void saveDebts(List<Debts> debtsList) {

            }
        };
    }

}