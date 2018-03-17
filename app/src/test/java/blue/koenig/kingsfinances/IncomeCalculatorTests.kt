package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.features.statistics.StatisticsPresenter
import blue.koenig.kingsfinances.model.calculation.IncomeCalculator
import com.koenig.commonModel.Repository.IncomeRepository
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.Expenses
import com.koenig.commonModel.finance.statistics.MonthStatistic
import io.reactivex.Observable
import junit.framework.Assert
import org.joda.time.LocalDate
import org.joda.time.YearMonth
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Created by Thomas on 09.01.2018.
 */
class IncomeCalculatorTests {

    @Mock
    lateinit var incomeRepository: IncomeRepository
    lateinit var itemSubject: TestSubject<Expenses>
    lateinit var incomeCalculator: IncomeCalculator

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        itemSubject = TestSubject<Expenses>()
        incomeCalculator = IncomeCalculator(itemSubject, Observable.just(YearMonth(2018, 12)), incomeRepository)
    }
    
    @Test
    fun add() {

        itemSubject.add(createExpensesForThomas(LocalDate(2017, 2, 2), 200))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 2, 3), 300))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 2, 4), 400))

        val entrys = incomeCalculator.absoluteMap

        assertIncomeList(YearMonth(2017, 1), 0, 0, entrys)
        assertIncomeList(YearMonth(2017, 2), 900, 0, entrys)
        assertIncomeList(YearMonth(2018, 2), 900, 0, entrys)
        val savingRate = StatisticsPresenter.calcSavingRate(YearMonth(2017, 1), YearMonth(2018, 1), 450, entrys)
        Assert.assertEquals(0.5f, savingRate)
    }

    // TODO: make tests for update deleted items through server update


    @Test
    fun addOneYear() {
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 2, 2), 200))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 3, 3), 300))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 4, 4), 400))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 5, 4), 400))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 6, 4), 400))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 7, 4), 400))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 8, 4), 400))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 9, 4), 400))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 10, 4), 400))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 11, 4), 400))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 12, 4), 800))

        val entrys = incomeCalculator.absoluteMap

        assertIncomeList(YearMonth(2017, 1), 0, 0, entrys)
        assertIncomeList(YearMonth(2017, 2), 200, 0, entrys)
        assertIncomeList(YearMonth(2017, 3), 500, 0, entrys)
        assertIncomeList(YearMonth(2017, 4), 900, 0, entrys)
        assertIncomeList(YearMonth(2017, 5), 1300, 0, entrys)
        assertIncomeList(YearMonth(2017, 6), 1700, 0, entrys)
        assertIncomeList(YearMonth(2017, 7), 2100, 0, entrys)
        assertIncomeList(YearMonth(2017, 8), 2500, 0, entrys)
        assertIncomeList(YearMonth(2017, 9), 2900, 0, entrys)
        assertIncomeList(YearMonth(2017, 10), 3300, 0, entrys)
        assertIncomeList(YearMonth(2017, 11), 3700, 0, entrys)
        assertIncomeList(YearMonth(2017, 12), 4500, 0, entrys)
        val savingRate = StatisticsPresenter.calcSavingRate(YearMonth(2017, 1), YearMonth(2017, 12), 450, entrys)
        Assert.assertEquals(0.1f, savingRate)
    }

    @Test
    fun updateDelete() {

        itemSubject.add(createExpensesForThomas(LocalDate(2016, 1, 2), 200))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 2, 3), 300))
        val expensesForThomas = createExpensesForThomas(LocalDate(2017, 3, 4), 400)
        itemSubject.add(expensesForThomas)
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 4, 4), 400))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 5, 4), 400))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 6, 4), 400))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 7, 4), 400))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 8, 4), 400))
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 9, 4), 400))
        val expensesForThomas1 = createExpensesForThomas(LocalDate(2017, 10, 4), 400)
        itemSubject.add(expensesForThomas1)
        itemSubject.add(createExpensesForThomas(LocalDate(2017, 11, 4), 400))
        itemSubject.add(createExpensesForThomas(LocalDate(2018, 12, 4), 400))


        itemSubject.update(expensesForThomas, createExpensesForThomas(LocalDate(2017, 3, 4), 200))
        itemSubject.delete(expensesForThomas1)
        val entrys = incomeCalculator.absoluteMap

        val savingRate = StatisticsPresenter.calcSavingRate(YearMonth(2017, 1), YearMonth(2017, 12), 330, entrys)
        val savingRate2 = StatisticsPresenter.calcSavingRate(YearMonth(2016, 1), YearMonth(2016, 12), 200, entrys)
        val savingRate3 = StatisticsPresenter.calcSavingRate(YearMonth(2018, 1), YearMonth(2018, 12), 200, entrys)
        Assert.assertEquals(0.1f, savingRate)
        Assert.assertEquals(1f, savingRate2)
        Assert.assertEquals(0.5f, savingRate3)
    }

    @Test
    fun entrysMissing() {
        // wenn vorne welche fehlen gibt es ein fehler, auch wenn hinten welche fehlen, könnte es fehler geben
        val entries = mutableMapOf<YearMonth, MonthStatistic>()
        entries[YearMonth(2017, 6)] = createStatisticsEntry(YearMonth(2017, 6), 100, 100)
        entries[YearMonth(2017, 7)] = createStatisticsEntry(YearMonth(2017, 7), 200, 200)

        val savingRate = StatisticsPresenter.calcSavingRate(YearMonth(2017, 1), YearMonth(2018, 1), 100, entries)
        Assert.assertEquals(0.25f, savingRate)
    }

    private fun createStatisticsEntry(day: YearMonth, thomas: Int, milena: Int): MonthStatistic {
        val map = HashMap<User, Int>(2)
        map[TestHelper.thomas] = thomas
        map[TestHelper.milena] = milena
        return MonthStatistic(day, map)
    }

    private fun createExpensesForThomas(day: LocalDate, costs: Int): Expenses {
        return Expenses("", "", "", costs, TestHelper.makeCostDistribution(costs, costs, 0, 0), day, "")
    }

    companion object {
        fun assertIncomeList(month: YearMonth, thomas: Int, milena: Int, statisticEntryList: Map<YearMonth, MonthStatistic>) {
            val debt = statisticEntryList[month]!!
            Assert.assertEquals(month, debt.month)
            Assert.assertEquals(thomas, debt[TestHelper.thomas])
            Assert.assertEquals(milena, debt[TestHelper.milena])
        }
    }

}
