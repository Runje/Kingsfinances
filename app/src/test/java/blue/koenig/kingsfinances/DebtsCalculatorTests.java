package blue.koenig.kingsfinances;

import com.koenig.commonModel.finance.CostDistribution;
import com.koenig.commonModel.finance.Expenses;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import blue.koenig.kingsfinances.model.calculation.DebtsCalculator;
import blue.koenig.kingsfinances.model.calculation.StatisticEntry;

import static blue.koenig.kingsfinances.TestHelper.assertDebtsList;
import static blue.koenig.kingsfinances.TestHelper.getCalculatorService;
import static blue.koenig.kingsfinances.TestHelper.getDay;
import static blue.koenig.kingsfinances.TestHelper.makeCostDistribution;
import static blue.koenig.kingsfinances.TestHelper.makeDebts;
import static blue.koenig.kingsfinances.TestHelper.makeDebtsList;
import static blue.koenig.kingsfinances.TestHelper.milena;
import static blue.koenig.kingsfinances.TestHelper.thomas;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class DebtsCalculatorTests {

    public Expenses makeExpenses(CostDistribution costDistribution, DateTime dateTime) {
        return new Expenses("", "", "", costDistribution.sumReal(), costDistribution, dateTime, "");
    }


    @Test
    public void debtsCalculation1() throws Exception {
        List<Expenses> expensesList = new ArrayList<>(1);
        expensesList.add(makeExpenses(makeCostDistribution(-10, 0, 0, -10), getDay(2017, 1, 1)));
        List<StatisticEntry> debts = DebtsCalculator.recalculateAll(expensesList, Period.months(1));
        Assert.assertEquals(2, debts.size());

        StatisticEntry debt = debts.get(0);
        Assert.assertEquals(getDay(2016, 12, 1), debt.getDate());
        Assert.assertEquals(0, debt.getEntryFor(thomas));
        Assert.assertEquals(0, debt.getEntryFor(milena));

        debt = debts.get(1);
        Assert.assertEquals(getDay(2017, 1, 1), debt.getDate());
        Assert.assertEquals(-10, debt.getEntryFor(thomas));
        Assert.assertEquals(10, debt.getEntryFor(milena));
    }

    @Test
    public void debtsCalculation2() throws Exception {
        List<Expenses> expensesList = new ArrayList<>(2);
        expensesList.add(makeExpenses(makeCostDistribution(-10, 0, 0, -10), getDay(2017, 1, 1)));
        expensesList.add(makeExpenses(makeCostDistribution(-5, -10, -5, 0), getDay(2017, 1, 31)));
        List<StatisticEntry> debts = DebtsCalculator.recalculateAll(expensesList, Period.months(1));
        Assert.assertEquals(3, debts.size());

        StatisticEntry debt = debts.get(0);
        Assert.assertEquals(getDay(2016, 12, 1), debt.getDate());
        Assert.assertEquals(0, debt.getEntryFor(thomas));
        Assert.assertEquals(0, debt.getEntryFor(milena));

        debt = debts.get(1);
        Assert.assertEquals(getDay(2017, 1, 1), debt.getDate());
        Assert.assertEquals(-10, debt.getEntryFor(thomas));
        Assert.assertEquals(10, debt.getEntryFor(milena));

        debt = debts.get(2);
        Assert.assertEquals(getDay(2017, 2, 1), debt.getDate());
        Assert.assertEquals(-5, debt.getEntryFor(thomas));
        Assert.assertEquals(5, debt.getEntryFor(milena));
    }

    @Test
    public void debtsCalculation3() throws Exception {
        List<Expenses> expensesList = new ArrayList<>(3);
        expensesList.add(makeExpenses(makeCostDistribution(-10, 0, 0, -10), getDay(2017, 1, 1)));
        expensesList.add(makeExpenses(makeCostDistribution(-10, 0, 0, -10), getDay(2017, 1, 1)));
        expensesList.add(makeExpenses(makeCostDistribution(-5, -10, -5, 0), getDay(2017, 1, 31)));
        expensesList.add(makeExpenses(makeCostDistribution(0, 300, 300, 0), getDay(2017, 3, 2)));
        List<StatisticEntry> debts = DebtsCalculator.recalculateAll(expensesList, Period.months(1));
        Assert.assertEquals(5, debts.size());

        StatisticEntry debt = debts.get(0);
        Assert.assertEquals(getDay(2016, 12, 1), debt.getDate());
        Assert.assertEquals(0, debt.getEntryFor(thomas));
        Assert.assertEquals(0, debt.getEntryFor(milena));

        debt = debts.get(1);
        Assert.assertEquals(getDay(2017, 1, 1), debt.getDate());
        Assert.assertEquals(-20, debt.getEntryFor(thomas));
        Assert.assertEquals(20, debt.getEntryFor(milena));

        debt = debts.get(2);
        Assert.assertEquals(getDay(2017, 2, 1), debt.getDate());
        Assert.assertEquals(-15, debt.getEntryFor(thomas));
        Assert.assertEquals(15, debt.getEntryFor(milena));

        debt = debts.get(3);
        Assert.assertEquals(getDay(2017, 3, 1), debt.getDate());
        Assert.assertEquals(-15, debt.getEntryFor(thomas));
        Assert.assertEquals(15, debt.getEntryFor(milena));

        debt = debts.get(4);
        Assert.assertEquals(getDay(2017, 4, 1), debt.getDate());
        Assert.assertEquals(-315, debt.getEntryFor(thomas));
        Assert.assertEquals(315, debt.getEntryFor(milena));
    }


    @Test
    public void emptyDebts() {
        List<StatisticEntry> statisticEntryList = DebtsCalculator.updateStatistics(makeDebts(-10, 17, 1, 2), Period.months(1), new ArrayList<>());
        Assert.assertEquals(2, statisticEntryList.size());

        assertDebtsList(0, getDay(17, 1, 1), 0, statisticEntryList);
        assertDebtsList(1, getDay(17, 2, 1), -10, statisticEntryList);
    }

    @Test
    public void addOneBefore() {
        List<StatisticEntry> statisticEntryList = DebtsCalculator.updateStatistics(makeDebts(-10, 17, 1, 2), Period.months(1), makeDebtsList(getDay(17, 2, 1), new int[]{0, 20}));
        Assert.assertEquals(3, statisticEntryList.size());

        assertDebtsList(0, getDay(17, 1, 1), 0, statisticEntryList);
        assertDebtsList(1, getDay(17, 2, 1), -10, statisticEntryList);
        assertDebtsList(2, getDay(17, 3, 1), 10, statisticEntryList);
    }

    @Test
    public void addBefore() {
        List<StatisticEntry> statisticEntryList = DebtsCalculator.updateStatistics(makeDebts(-10, 17, 1, 2), Period.months(1), makeDebtsList(getDay(17, 1, 1), new int[]{0, 20}));
        Assert.assertEquals(2, statisticEntryList.size());

        assertDebtsList(0, getDay(17, 1, 1), 0, statisticEntryList);
        assertDebtsList(1, getDay(17, 2, 1), 10, statisticEntryList);
    }


    @Test
    public void addWayBefore() {
        List<StatisticEntry> statisticEntryList = DebtsCalculator.updateStatistics(makeDebts(-10, 16, 6, 2), Period.months(1), makeDebtsList(getDay(17, 1, 1), new int[]{0, 20}));
        Assert.assertEquals(9, statisticEntryList.size());

        assertDebtsList(0, getDay(16, 6, 1), 0, statisticEntryList);
        assertDebtsList(1, getDay(16, 7, 1), -10, statisticEntryList);
        assertDebtsList(2, getDay(16, 8, 1), -10, statisticEntryList);
        assertDebtsList(3, getDay(16, 9, 1), -10, statisticEntryList);
        assertDebtsList(4, getDay(16, 10, 1), -10, statisticEntryList);
        assertDebtsList(5, getDay(16, 11, 1), -10, statisticEntryList);
        assertDebtsList(6, getDay(16, 12, 1), -10, statisticEntryList);
        assertDebtsList(7, getDay(17, 1, 1), -10, statisticEntryList);
        assertDebtsList(8, getDay(17, 2, 1), 10, statisticEntryList);
    }

    @Test
    public void addMiddle() {
        List<StatisticEntry> statisticEntryList = DebtsCalculator.updateStatistics(makeDebts(-10, 16, 12, 2), Period.months(1),
                makeDebtsList(getDay(16, 6, 1), new int[]{0, 20, 30, 40, 40, 40, 50, -20, 0}));
        Assert.assertEquals(9, statisticEntryList.size());

        assertDebtsList(0, getDay(16, 6, 1), 0, statisticEntryList);
        assertDebtsList(1, getDay(16, 7, 1), 20, statisticEntryList);
        assertDebtsList(2, getDay(16, 8, 1), 30, statisticEntryList);
        assertDebtsList(3, getDay(16, 9, 1), 40, statisticEntryList);
        assertDebtsList(4, getDay(16, 10, 1), 40, statisticEntryList);
        assertDebtsList(5, getDay(16, 11, 1), 40, statisticEntryList);
        assertDebtsList(6, getDay(16, 12, 1), 50, statisticEntryList);
        assertDebtsList(7, getDay(17, 1, 1), -30, statisticEntryList);
        assertDebtsList(8, getDay(17, 2, 1), -10, statisticEntryList);
    }

    @Test
    public void addOneEnd() {
        List<StatisticEntry> statisticEntryList = DebtsCalculator.updateStatistics(makeDebts(-10, 17, 2, 2), Period.months(1),
                makeDebtsList(getDay(17, 1, 1), new int[]{0, 20}));
        Assert.assertEquals(3, statisticEntryList.size());

        assertDebtsList(0, getDay(17, 1, 1), 0, statisticEntryList);
        assertDebtsList(1, getDay(17, 2, 1), 20, statisticEntryList);
        assertDebtsList(2, getDay(17, 3, 1), 10, statisticEntryList);
    }

    @Test
    public void addWayEnd() {
        List<StatisticEntry> statisticEntryList = DebtsCalculator.updateStatistics(makeDebts(-10, 17, 4, 2), Period.months(1),
                makeDebtsList(getDay(17, 1, 1), new int[]{0, 20}));
        Assert.assertEquals(5, statisticEntryList.size());

        assertDebtsList(0, getDay(17, 1, 1), 0, statisticEntryList);
        assertDebtsList(1, getDay(17, 2, 1), 20, statisticEntryList);
        assertDebtsList(2, getDay(17, 3, 1), 20, statisticEntryList);
        assertDebtsList(3, getDay(17, 4, 1), 20, statisticEntryList);
        assertDebtsList(4, getDay(17, 5, 1), 10, statisticEntryList);
    }


    @Test
    public void editFirst() {
        TestExpensesSubject expensesItemSubject = new TestExpensesSubject();
        DebtsCalculator debtsCalculator = new DebtsCalculator(Period.months(1), expensesItemSubject,
                getCalculatorService(makeDebtsList(getDay(17, 1, 1), new int[]{0, 0, 0, -10})));
        expensesItemSubject.updateDebts(makeDebts(getDay(17, 1, 2), 0), makeDebts(getDay(17, 1, 2), 1));
        List<StatisticEntry> statisticEntryList = debtsCalculator.getEntrys();

        Assert.assertEquals(4, statisticEntryList.size());

        assertDebtsList(0, getDay(17, 1, 1), 0, statisticEntryList);
        assertDebtsList(1, getDay(17, 2, 1), 1, statisticEntryList);
        assertDebtsList(2, getDay(17, 3, 1), 1, statisticEntryList);
        assertDebtsList(3, getDay(17, 4, 1), -9, statisticEntryList);
    }

    @Test
    public void editDateFromFirst() {
        TestExpensesSubject expensesItemSubject = new TestExpensesSubject();
        DebtsCalculator debtsCalculator = new DebtsCalculator(Period.months(1), expensesItemSubject,
                getCalculatorService(makeDebtsList(getDay(17, 1, 1), new int[]{0, 10, 0, -10})));
        expensesItemSubject.updateDebts(makeDebts(getDay(17, 1, 2), 10), makeDebts(getDay(17, 4, 2), 10));
        List<StatisticEntry> statisticEntryList = debtsCalculator.getEntrys();

        Assert.assertEquals(5, statisticEntryList.size());

        assertDebtsList(0, getDay(17, 1, 1), 0, statisticEntryList);
        assertDebtsList(1, getDay(17, 2, 1), 0, statisticEntryList);
        assertDebtsList(2, getDay(17, 3, 1), -10, statisticEntryList);
        assertDebtsList(3, getDay(17, 4, 1), -20, statisticEntryList);
        assertDebtsList(4, getDay(17, 5, 1), -10, statisticEntryList);
    }


}