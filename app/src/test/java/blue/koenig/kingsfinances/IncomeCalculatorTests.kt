package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.features.statistics.StatisticsPresenter
import blue.koenig.kingsfinances.model.calculation.IncomeCalculator
import blue.koenig.kingsfinances.model.calculation.StatisticEntryDeprecated
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.Expenses
import junit.framework.Assert
import org.joda.time.DateTime
import org.joda.time.Period
import org.junit.Test
import java.util.*

/**
 * Created by Thomas on 09.01.2018.
 */

class IncomeCalculatorTests {

    @Test
    fun add() {
        val itemSubject = TestSubject<Expenses>()
        val service = TestHelper.getStatisticsCalculatorService(ArrayList())
        val assetsCalculator = IncomeCalculator(Period.months(1), itemSubject, service)
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 1, 2), 200))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 1, 3), 300))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 1, 4), 400))

        val entrys = assetsCalculator.entrys
        Assert.assertEquals(2, entrys.size)

        assertIncomeList(0, TestHelper.getDay(2017, 1, 1), 0, 0, entrys)
        assertIncomeList(1, TestHelper.getDay(2017, 2, 1), 900, 0, entrys)
        val savingRate = StatisticsPresenter.calcSavingRate(TestHelper.getDay(2017, 1, 1), TestHelper.getDay(2018, 1, 1), 450, entrys)
        Assert.assertEquals(0.5f, savingRate)
    }

    // TODO: make tests for update deleted items through server update

    @Test
    fun addOneYear() {
        val itemSubject = TestSubject<Expenses>()
        val service = TestHelper.getStatisticsCalculatorService(ArrayList())
        val assetsCalculator = IncomeCalculator(Period.months(1), itemSubject, service)
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 1, 2), 200))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 2, 3), 300))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 3, 4), 400))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 4, 4), 400))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 5, 4), 400))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 6, 4), 400))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 7, 4), 400))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 8, 4), 400))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 9, 4), 400))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 10, 4), 400))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 11, 4), 400))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 12, 4), 400))

        val entrys = assetsCalculator.entrys
        Assert.assertEquals(13, entrys.size)

        assertIncomeList(0, TestHelper.getDay(2017, 1, 1), 0, 0, entrys)
        assertIncomeList(1, TestHelper.getDay(2017, 2, 1), 200, 0, entrys)
        assertIncomeList(2, TestHelper.getDay(2017, 3, 1), 500, 0, entrys)
        assertIncomeList(3, TestHelper.getDay(2017, 4, 1), 900, 0, entrys)
        assertIncomeList(4, TestHelper.getDay(2017, 5, 1), 1300, 0, entrys)
        assertIncomeList(5, TestHelper.getDay(2017, 6, 1), 1700, 0, entrys)
        assertIncomeList(6, TestHelper.getDay(2017, 7, 1), 2100, 0, entrys)
        assertIncomeList(7, TestHelper.getDay(2017, 8, 1), 2500, 0, entrys)
        assertIncomeList(8, TestHelper.getDay(2017, 9, 1), 2900, 0, entrys)
        assertIncomeList(9, TestHelper.getDay(2017, 10, 1), 3300, 0, entrys)
        assertIncomeList(10, TestHelper.getDay(2017, 11, 1), 3700, 0, entrys)
        assertIncomeList(11, TestHelper.getDay(2017, 12, 1), 4100, 0, entrys)
        assertIncomeList(12, TestHelper.getDay(2018, 1, 1), 4500, 0, entrys)
        val savingRate = StatisticsPresenter.calcSavingRate(TestHelper.getDay(2017, 1, 1), TestHelper.getDay(2018, 1, 1), 450, entrys)
        Assert.assertEquals(0.1f, savingRate)
    }

    @Test
    fun updateDelete() {
        val itemSubject = TestSubject<Expenses>()
        val service = TestHelper.getStatisticsCalculatorService(ArrayList())
        val assetsCalculator = IncomeCalculator(Period.months(1), itemSubject, service)
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2016, 1, 2), 200))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 2, 3), 300))
        val expensesForThomas = createExpensesForThomas(TestHelper.getDay(2017, 3, 4), 400)
        itemSubject.add(expensesForThomas)
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 4, 4), 400))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 5, 4), 400))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 6, 4), 400))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 7, 4), 400))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 8, 4), 400))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 9, 4), 400))
        val expensesForThomas1 = createExpensesForThomas(TestHelper.getDay(2017, 10, 4), 400)
        itemSubject.add(expensesForThomas1)
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2017, 11, 4), 400))
        itemSubject.add(createExpensesForThomas(TestHelper.getDay(2018, 12, 4), 400))


        itemSubject.update(expensesForThomas, createExpensesForThomas(TestHelper.getDay(2017, 3, 4), 200))
        itemSubject.delete(expensesForThomas1)
        val entrys = assetsCalculator.entrys
        /**Assert.assertEquals(13, entrys.size());
         *
         * assertIncomeList(0, getDay(2017, 1, 1), 0, 0, entrys);
         * assertIncomeList(1, getDay(2017, 2, 1), 200, 0,entrys);
         * assertIncomeList(2, getDay(2017, 3, 1), 500, 0,entrys);
         * assertIncomeList(3, getDay(2017, 4, 1), 900, 0,entrys);
         * assertIncomeList(4, getDay(2017, 5, 1), 1300, 0,entrys);
         * assertIncomeList(5, getDay(2017, 6, 1), 1700, 0,entrys);
         * assertIncomeList(6, getDay(2017, 7, 1), 2100, 0,entrys);
         * assertIncomeList(7, getDay(2017, 8, 1), 2500, 0,entrys);
         * assertIncomeList(8, getDay(2017, 9, 1), 2900, 0,entrys);
         * assertIncomeList(9, getDay(2017, 10, 1), 3300, 0,entrys);
         * assertIncomeList(10, getDay(2017, 11, 1), 3700, 0,entrys);
         * assertIncomeList(11, getDay(2017, 12, 1), 4100, 0,entrys);
         * assertIncomeList(12, getDay(2018, 1, 1), 4500, 0,entrys); */
        val savingRate = StatisticsPresenter.calcSavingRate(TestHelper.getDay(2017, 1, 1), TestHelper.getDay(2018, 1, 1), 330, entrys)
        val savingRate2 = StatisticsPresenter.calcSavingRate(TestHelper.getDay(2016, 1, 1), TestHelper.getDay(2017, 1, 1), 200, entrys)
        val savingRate3 = StatisticsPresenter.calcSavingRate(TestHelper.getDay(2018, 1, 1), TestHelper.getDay(2019, 1, 1), 200, entrys)
        Assert.assertEquals(0.1f, savingRate)
        Assert.assertEquals(1f, savingRate2)
        Assert.assertEquals(0.5f, savingRate3)
    }

    @Test
    fun entrysMissing() {
        // wenn vorne welche fehlen gibt es ein fehler, auch wenn hinten welche fehlen, könnte es fehler geben
        val entries = ArrayList<StatisticEntryDeprecated>()
        entries.add(createStatisticsEntry(TestHelper.getDay(2017, 6, 1), 100, 100))
        entries.add(createStatisticsEntry(TestHelper.getDay(2017, 7, 1), 200, 200))

        val savingRate = StatisticsPresenter.calcSavingRate(TestHelper.getDay(2017, 1, 1), TestHelper.getDay(2018, 1, 1), 100, entries)
        Assert.assertEquals(0.5f, savingRate)
    }

    private fun createStatisticsEntry(day: DateTime, thomas: Int, milena: Int): StatisticEntryDeprecated {
        val map = HashMap<User, Int>(2)
        map[TestHelper.thomas] = thomas
        map[TestHelper.milena] = milena
        return StatisticEntryDeprecated(day, map)
    }

    private fun createExpensesForThomas(day: DateTime, costs: Int): Expenses {
        return Expenses("", "", "", costs, TestHelper.makeCostDistribution(costs, costs, 0, 0), day, "")
    }

    companion object {
        fun assertIncomeList(index: Int, dateTime: DateTime, thomas: Int, milena: Int, statisticEntryList: List<StatisticEntryDeprecated>) {
            val debt = statisticEntryList[index]
            Assert.assertEquals(dateTime, debt.date)
            Assert.assertEquals(thomas, debt.getEntryFor(TestHelper.thomas))
            Assert.assertEquals(milena, debt.getEntryFor(TestHelper.milena))
        }
    }
}
