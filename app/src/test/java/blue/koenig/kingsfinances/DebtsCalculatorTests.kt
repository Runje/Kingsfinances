package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.model.calculation.AccumulativeStatisticsCalculator
import blue.koenig.kingsfinances.model.calculation.DebtsCalculator
import blue.koenig.kingsfinances.model.calculation.StatisticEntry
import com.koenig.commonModel.finance.CostDistribution
import com.koenig.commonModel.finance.Expenses
import junit.framework.Assert
import org.joda.time.DateTime
import org.joda.time.Period
import org.junit.Test
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class DebtsCalculatorTests {

    fun makeExpenses(costDistribution: CostDistribution, dateTime: DateTime): Expenses {
        return Expenses("", "", "", costDistribution.sumReal(), costDistribution, dateTime, "")
    }


    @Test
    @Throws(Exception::class)
    fun debtsCalculation1() {
        val expensesList = ArrayList<Expenses>(1)
        expensesList.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), TestHelper.getDay(2017, 1, 1)))
        val debts = AccumulativeStatisticsCalculator.recalculateAll(expensesList, Period.months(1))
        Assert.assertEquals(2, debts.size)

        var debt = debts.get(0)
        Assert.assertEquals(TestHelper.getDay(2016, 12, 1), debt.getDate())
        Assert.assertEquals(0, debt.getEntryFor(TestHelper.thomas))
        Assert.assertEquals(0, debt.getEntryFor(TestHelper.milena))

        debt = debts.get(1)
        Assert.assertEquals(TestHelper.getDay(2017, 1, 1), debt.getDate())
        Assert.assertEquals(-10, debt.getEntryFor(TestHelper.thomas))
        Assert.assertEquals(10, debt.getEntryFor(TestHelper.milena))
    }

    @Test
    @Throws(Exception::class)
    fun debtsCalculation2() {
        val expensesList = ArrayList<Expenses>(2)
        expensesList.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), TestHelper.getDay(2017, 1, 1)))
        expensesList.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), TestHelper.getDay(2017, 1, 31)))
        val debts = AccumulativeStatisticsCalculator.recalculateAll(expensesList, Period.months(1))
        Assert.assertEquals(3, debts.size)

        var debt = debts.get(0)
        Assert.assertEquals(TestHelper.getDay(2016, 12, 1), debt.getDate())
        Assert.assertEquals(0, debt.getEntryFor(TestHelper.thomas))
        Assert.assertEquals(0, debt.getEntryFor(TestHelper.milena))

        debt = debts.get(1)
        Assert.assertEquals(TestHelper.getDay(2017, 1, 1), debt.getDate())
        Assert.assertEquals(-10, debt.getEntryFor(TestHelper.thomas))
        Assert.assertEquals(10, debt.getEntryFor(TestHelper.milena))

        debt = debts.get(2)
        Assert.assertEquals(TestHelper.getDay(2017, 2, 1), debt.getDate())
        Assert.assertEquals(-5, debt.getEntryFor(TestHelper.thomas))
        Assert.assertEquals(5, debt.getEntryFor(TestHelper.milena))
    }

    @Test
    @Throws(Exception::class)
    fun debtsCalculation3() {
        val expensesList = ArrayList<Expenses>(3)
        expensesList.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), TestHelper.getDay(2017, 1, 1)))
        expensesList.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), TestHelper.getDay(2017, 1, 1)))
        expensesList.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), TestHelper.getDay(2017, 1, 31)))
        expensesList.add(makeExpenses(TestHelper.makeCostDistribution(0, 300, 300, 0), TestHelper.getDay(2017, 3, 2)))
        val debts = AccumulativeStatisticsCalculator.recalculateAll(expensesList, Period.months(1))
        Assert.assertEquals(5, debts.size)

        var debt = debts.get(0)
        Assert.assertEquals(TestHelper.getDay(2016, 12, 1), debt.getDate())
        Assert.assertEquals(0, debt.getEntryFor(TestHelper.thomas))
        Assert.assertEquals(0, debt.getEntryFor(TestHelper.milena))

        debt = debts.get(1)
        Assert.assertEquals(TestHelper.getDay(2017, 1, 1), debt.getDate())
        Assert.assertEquals(-20, debt.getEntryFor(TestHelper.thomas))
        Assert.assertEquals(20, debt.getEntryFor(TestHelper.milena))

        debt = debts.get(2)
        Assert.assertEquals(TestHelper.getDay(2017, 2, 1), debt.getDate())
        Assert.assertEquals(-15, debt.getEntryFor(TestHelper.thomas))
        Assert.assertEquals(15, debt.getEntryFor(TestHelper.milena))

        debt = debts.get(3)
        Assert.assertEquals(TestHelper.getDay(2017, 3, 1), debt.getDate())
        Assert.assertEquals(-15, debt.getEntryFor(TestHelper.thomas))
        Assert.assertEquals(15, debt.getEntryFor(TestHelper.milena))

        debt = debts.get(4)
        Assert.assertEquals(TestHelper.getDay(2017, 4, 1), debt.getDate())
        Assert.assertEquals(-315, debt.getEntryFor(TestHelper.thomas))
        Assert.assertEquals(315, debt.getEntryFor(TestHelper.milena))
    }


    @Test
    fun emptyDebts() {
        val statisticEntryList = AccumulativeStatisticsCalculator.updateStatistics(TestHelper.makeDebts(-10, 17, 1, 2), Period.months(1), ArrayList<StatisticEntry>())
        Assert.assertEquals(2, statisticEntryList.size)

        TestHelper.assertDebtsList(0, TestHelper.getDay(17, 1, 1), 0, statisticEntryList)
        TestHelper.assertDebtsList(1, TestHelper.getDay(17, 2, 1), -10, statisticEntryList)
    }

    @Test
    fun addOneBefore() {
        val statisticEntryList = AccumulativeStatisticsCalculator.updateStatistics(TestHelper.makeDebts(-10, 17, 1, 2), Period.months(1), TestHelper.makeDebtsList(TestHelper.getDay(17, 2, 1), intArrayOf(0, 20)))
        Assert.assertEquals(3, statisticEntryList.size)

        TestHelper.assertDebtsList(0, TestHelper.getDay(17, 1, 1), 0, statisticEntryList)
        TestHelper.assertDebtsList(1, TestHelper.getDay(17, 2, 1), -10, statisticEntryList)
        TestHelper.assertDebtsList(2, TestHelper.getDay(17, 3, 1), 10, statisticEntryList)
    }

    @Test
    fun addBefore() {
        val statisticEntryList = AccumulativeStatisticsCalculator.updateStatistics(TestHelper.makeDebts(-10, 17, 1, 2), Period.months(1), TestHelper.makeDebtsList(TestHelper.getDay(17, 1, 1), intArrayOf(0, 20)))
        Assert.assertEquals(2, statisticEntryList.size)

        TestHelper.assertDebtsList(0, TestHelper.getDay(17, 1, 1), 0, statisticEntryList)
        TestHelper.assertDebtsList(1, TestHelper.getDay(17, 2, 1), 10, statisticEntryList)
    }


    @Test
    fun addWayBefore() {
        val statisticEntryList = AccumulativeStatisticsCalculator.updateStatistics(TestHelper.makeDebts(-10, 16, 6, 2), Period.months(1), TestHelper.makeDebtsList(TestHelper.getDay(17, 1, 1), intArrayOf(0, 20)))
        Assert.assertEquals(9, statisticEntryList.size)

        TestHelper.assertDebtsList(0, TestHelper.getDay(16, 6, 1), 0, statisticEntryList)
        TestHelper.assertDebtsList(1, TestHelper.getDay(16, 7, 1), -10, statisticEntryList)
        TestHelper.assertDebtsList(2, TestHelper.getDay(16, 8, 1), -10, statisticEntryList)
        TestHelper.assertDebtsList(3, TestHelper.getDay(16, 9, 1), -10, statisticEntryList)
        TestHelper.assertDebtsList(4, TestHelper.getDay(16, 10, 1), -10, statisticEntryList)
        TestHelper.assertDebtsList(5, TestHelper.getDay(16, 11, 1), -10, statisticEntryList)
        TestHelper.assertDebtsList(6, TestHelper.getDay(16, 12, 1), -10, statisticEntryList)
        TestHelper.assertDebtsList(7, TestHelper.getDay(17, 1, 1), -10, statisticEntryList)
        TestHelper.assertDebtsList(8, TestHelper.getDay(17, 2, 1), 10, statisticEntryList)
    }

    @Test
    fun addMiddle() {
        val statisticEntryList = AccumulativeStatisticsCalculator.updateStatistics(TestHelper.makeDebts(-10, 16, 12, 2), Period.months(1),
                TestHelper.makeDebtsList(TestHelper.getDay(16, 6, 1), intArrayOf(0, 20, 30, 40, 40, 40, 50, -20, 0)))
        Assert.assertEquals(9, statisticEntryList.size)

        TestHelper.assertDebtsList(0, TestHelper.getDay(16, 6, 1), 0, statisticEntryList)
        TestHelper.assertDebtsList(1, TestHelper.getDay(16, 7, 1), 20, statisticEntryList)
        TestHelper.assertDebtsList(2, TestHelper.getDay(16, 8, 1), 30, statisticEntryList)
        TestHelper.assertDebtsList(3, TestHelper.getDay(16, 9, 1), 40, statisticEntryList)
        TestHelper.assertDebtsList(4, TestHelper.getDay(16, 10, 1), 40, statisticEntryList)
        TestHelper.assertDebtsList(5, TestHelper.getDay(16, 11, 1), 40, statisticEntryList)
        TestHelper.assertDebtsList(6, TestHelper.getDay(16, 12, 1), 50, statisticEntryList)
        TestHelper.assertDebtsList(7, TestHelper.getDay(17, 1, 1), -30, statisticEntryList)
        TestHelper.assertDebtsList(8, TestHelper.getDay(17, 2, 1), -10, statisticEntryList)
    }

    @Test
    fun addOneEnd() {
        val statisticEntryList = AccumulativeStatisticsCalculator.updateStatistics(TestHelper.makeDebts(-10, 17, 2, 2), Period.months(1),
                TestHelper.makeDebtsList(TestHelper.getDay(17, 1, 1), intArrayOf(0, 20)))
        Assert.assertEquals(3, statisticEntryList.size)

        TestHelper.assertDebtsList(0, TestHelper.getDay(17, 1, 1), 0, statisticEntryList)
        TestHelper.assertDebtsList(1, TestHelper.getDay(17, 2, 1), 20, statisticEntryList)
        TestHelper.assertDebtsList(2, TestHelper.getDay(17, 3, 1), 10, statisticEntryList)
    }

    @Test
    fun addWayEnd() {
        val statisticEntryList = AccumulativeStatisticsCalculator.updateStatistics(TestHelper.makeDebts(-10, 17, 4, 2), Period.months(1),
                TestHelper.makeDebtsList(TestHelper.getDay(17, 1, 1), intArrayOf(0, 20)))
        Assert.assertEquals(5, statisticEntryList.size)

        TestHelper.assertDebtsList(0, TestHelper.getDay(17, 1, 1), 0, statisticEntryList)
        TestHelper.assertDebtsList(1, TestHelper.getDay(17, 2, 1), 20, statisticEntryList)
        TestHelper.assertDebtsList(2, TestHelper.getDay(17, 3, 1), 20, statisticEntryList)
        TestHelper.assertDebtsList(3, TestHelper.getDay(17, 4, 1), 20, statisticEntryList)
        TestHelper.assertDebtsList(4, TestHelper.getDay(17, 5, 1), 10, statisticEntryList)
    }


    @Test
    fun editFirst() {
        val expensesItemSubject = TestExpensesSubject()
        val calculator = DebtsCalculator(Period.months(1), expensesItemSubject,
                TestHelper.getCalculatorService(TestHelper.makeDebtsList(TestHelper.getDay(17, 1, 1), intArrayOf(0, 0, 0, -10))))
        expensesItemSubject.updateDebts(TestHelper.makeDebts(TestHelper.getDay(17, 1, 2), 0), TestHelper.makeDebts(TestHelper.getDay(17, 1, 2), 1))
        val statisticEntryList = calculator.entrys

        Assert.assertEquals(4, statisticEntryList.size)

        TestHelper.assertDebtsList(0, TestHelper.getDay(17, 1, 1), 0, statisticEntryList)
        TestHelper.assertDebtsList(1, TestHelper.getDay(17, 2, 1), 1, statisticEntryList)
        TestHelper.assertDebtsList(2, TestHelper.getDay(17, 3, 1), 1, statisticEntryList)
        TestHelper.assertDebtsList(3, TestHelper.getDay(17, 4, 1), -9, statisticEntryList)
    }

    @Test
    fun editDateFromFirst() {
        val expensesItemSubject = TestExpensesSubject()
        val calculator = DebtsCalculator(Period.months(1), expensesItemSubject,
                TestHelper.getCalculatorService(TestHelper.makeDebtsList(TestHelper.getDay(17, 1, 1), intArrayOf(0, 10, 0, -10))))
        expensesItemSubject.updateDebts(TestHelper.makeDebts(TestHelper.getDay(17, 1, 2), 10), TestHelper.makeDebts(TestHelper.getDay(17, 4, 2), 10))
        val statisticEntryList = calculator.entrys

        Assert.assertEquals(5, statisticEntryList.size)

        TestHelper.assertDebtsList(0, TestHelper.getDay(17, 1, 1), 0, statisticEntryList)
        TestHelper.assertDebtsList(1, TestHelper.getDay(17, 2, 1), 0, statisticEntryList)
        TestHelper.assertDebtsList(2, TestHelper.getDay(17, 3, 1), -10, statisticEntryList)
        TestHelper.assertDebtsList(3, TestHelper.getDay(17, 4, 1), -20, statisticEntryList)
        TestHelper.assertDebtsList(4, TestHelper.getDay(17, 5, 1), -10, statisticEntryList)
    }


}