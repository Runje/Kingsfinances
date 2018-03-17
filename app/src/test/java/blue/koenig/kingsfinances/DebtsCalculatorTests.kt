package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.model.calculation.DebtsCalculator
import com.koenig.commonModel.Repository.MonthStatisticsRepository
import com.koenig.commonModel.finance.CostDistribution
import com.koenig.commonModel.finance.Expenses
import io.reactivex.Observable
import org.joda.time.LocalDate
import org.joda.time.YearMonth
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */

class DebtsCalculatorTests {

    val itemSubject: TestExpensesSubject = TestExpensesSubject()
    lateinit var calculator: DebtsCalculator
    var endDate: YearMonth = YearMonth(2017, 12)

    @Mock
    lateinit var deltaMonthStatisticsRepository: MonthStatisticsRepository
    @Mock
    lateinit var absoluteMonthStatisticsRepository: MonthStatisticsRepository

    fun makeExpenses(costDistribution: CostDistribution, dateTime: LocalDate): Expenses {
        return Expenses("", "", "", costDistribution.sumReal(), costDistribution, dateTime, "")
    }

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

    }

    fun initTest() {
        calculator = DebtsCalculator(itemSubject, deltaMonthStatisticsRepository, absoluteMonthStatisticsRepository, Observable.just(endDate))
    }


    @Test
    @Throws(Exception::class)
    fun debtsCalculation1() {
        endDate = YearMonth(2017, 1)
        initTest()
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 1, 1)))
        val debts = calculator.absoluteMap
        Assert.assertEquals(2, debts.size)

        var debt = debts[YearMonth(2016, 12)]!!
        Assert.assertEquals(YearMonth(2016, 12), debt.month)
        Assert.assertEquals(0, debt[TestHelper.thomas])
        Assert.assertEquals(0, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 1)]!!
        Assert.assertEquals(YearMonth(2017, 1), debt.month)
        Assert.assertEquals(-10, debt[TestHelper.thomas])
        Assert.assertEquals(10, debt[TestHelper.milena])
    }

    @Test
    @Throws(Exception::class)
    fun debtsCalculation2() {
        endDate = YearMonth(2017, 1)
        initTest()
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 1, 1)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), LocalDate(2017, 1, 31)))
        val debts = calculator.absoluteMap
        Assert.assertEquals(2, debts.size)

        var debt = debts[YearMonth(2016, 12)]!!
        Assert.assertEquals(YearMonth(2016, 12), debt.month)
        Assert.assertEquals(0, debt[TestHelper.thomas])
        Assert.assertEquals(0, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 1)]!!
        Assert.assertEquals(YearMonth(2017, 1), debt.month)
        Assert.assertEquals(-5, debt[TestHelper.thomas])
        Assert.assertEquals(5, debt[TestHelper.milena])


    }

    @Test
    @Throws(Exception::class)
    fun debtsCalculation3() {
        endDate = YearMonth(2017, 3)
        initTest()
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 1, 1)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 1, 1)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), LocalDate(2017, 1, 31)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(0, 300, 300, 0), LocalDate(2017, 3, 2)))
        val debts = calculator.absoluteMap
        Assert.assertEquals(4, debts.size)


        var debt = debts[YearMonth(2016, 12)]!!
        Assert.assertEquals(YearMonth(2016, 12), debt.month)
        Assert.assertEquals(0, debt[TestHelper.thomas])
        Assert.assertEquals(0, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 1)]!!
        Assert.assertEquals(YearMonth(2017, 1), debt.month)
        Assert.assertEquals(-15, debt[TestHelper.thomas])
        Assert.assertEquals(15, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 2)]!!
        Assert.assertEquals(YearMonth(2017, 2), debt.month)
        Assert.assertEquals(-15, debt[TestHelper.thomas])
        Assert.assertEquals(15, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 3)]!!
        Assert.assertEquals(YearMonth(2017, 3), debt.month)
        Assert.assertEquals(-315, debt[TestHelper.thomas])
        Assert.assertEquals(315, debt[TestHelper.milena])


    }


    @Test
    fun addOneBefore() {
        endDate = YearMonth(2017, 1)
        initTest()
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 1, 1)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 1, 1)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), LocalDate(2017, 1, 31)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(0, -25, -25, 0), LocalDate(2016, 12, 2)))
        val debts = calculator.absoluteMap
        Assert.assertEquals(3, debts.size)


        var debt = debts[YearMonth(2016, 12)]!!
        Assert.assertEquals(YearMonth(2016, 12), debt.month)
        Assert.assertEquals(25, debt[TestHelper.thomas])
        Assert.assertEquals(-25, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 1)]!!
        Assert.assertEquals(YearMonth(2017, 1), debt.month)
        Assert.assertEquals(10, debt[TestHelper.thomas])
        Assert.assertEquals(-10, debt[TestHelper.milena])



        debt = debts[YearMonth(2016, 11)]!!
        Assert.assertEquals(YearMonth(2016, 11), debt.month)
        Assert.assertEquals(0, debt[TestHelper.thomas])
        Assert.assertEquals(0, debt[TestHelper.milena])
    }

    @Test
    fun addBefore() {

        endDate = YearMonth(2017, 1)
        initTest()
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 1, 1)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 1, 1)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), LocalDate(2017, 1, 31)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(0, -25, -25, 0), LocalDate(2016, 11, 2)))
        val debts = calculator.absoluteMap
        Assert.assertEquals(4, debts.size)


        var debt = debts[YearMonth(2016, 12)]!!
        Assert.assertEquals(YearMonth(2016, 12), debt.month)
        Assert.assertEquals(25, debt[TestHelper.thomas])
        Assert.assertEquals(-25, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 1)]!!
        Assert.assertEquals(YearMonth(2017, 1), debt.month)
        Assert.assertEquals(10, debt[TestHelper.thomas])
        Assert.assertEquals(-10, debt[TestHelper.milena])



        debt = debts[YearMonth(2016, 11)]!!
        Assert.assertEquals(YearMonth(2016, 11), debt.month)
        Assert.assertEquals(25, debt[TestHelper.thomas])
        Assert.assertEquals(-25, debt[TestHelper.milena])

        debt = debts[YearMonth(2016, 10)]!!
        Assert.assertEquals(YearMonth(2016, 10), debt.month)
        Assert.assertEquals(0, debt[TestHelper.thomas])
        Assert.assertEquals(0, debt[TestHelper.milena])


    }


    @Test
    fun addWayBefore() {

        endDate = YearMonth(2017, 1)
        initTest()
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 1, 1)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 1, 1)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), LocalDate(2017, 1, 31)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(0, -25, -25, 0), LocalDate(2016, 9, 2)))
        val debts = calculator.absoluteMap
        Assert.assertEquals(6, debts.size)


        var debt = debts[YearMonth(2016, 12)]!!
        Assert.assertEquals(YearMonth(2016, 12), debt.month)
        Assert.assertEquals(25, debt[TestHelper.thomas])
        Assert.assertEquals(-25, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 1)]!!
        Assert.assertEquals(YearMonth(2017, 1), debt.month)
        Assert.assertEquals(10, debt[TestHelper.thomas])
        Assert.assertEquals(-10, debt[TestHelper.milena])



        debt = debts[YearMonth(2016, 11)]!!
        Assert.assertEquals(YearMonth(2016, 11), debt.month)
        Assert.assertEquals(25, debt[TestHelper.thomas])
        Assert.assertEquals(-25, debt[TestHelper.milena])

        debt = debts[YearMonth(2016, 10)]!!
        Assert.assertEquals(YearMonth(2016, 10), debt.month)
        Assert.assertEquals(25, debt[TestHelper.thomas])
        Assert.assertEquals(-25, debt[TestHelper.milena])

        debt = debts[YearMonth(2016, 9)]!!
        Assert.assertEquals(YearMonth(2016, 9), debt.month)
        Assert.assertEquals(25, debt[TestHelper.thomas])
        Assert.assertEquals(-25, debt[TestHelper.milena])

        debt = debts[YearMonth(2016, 8)]!!
        Assert.assertEquals(YearMonth(2016, 8), debt.month)
        Assert.assertEquals(0, debt[TestHelper.thomas])
        Assert.assertEquals(0, debt[TestHelper.milena])

    }

    @Test
    fun addMiddle() {
        endDate = YearMonth(2017, 3)
        initTest()
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 1, 1)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 3, 1)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), LocalDate(2017, 2, 28)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(0, -25, -25, 0), LocalDate(2016, 12, 2)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(0, -25, -25, 0), LocalDate(2017, 1, 2)))
        val debts = calculator.absoluteMap
        Assert.assertEquals(5, debts.size)

        var debt = debts[YearMonth(2016, 11)]!!
        Assert.assertEquals(YearMonth(2016, 11), debt.month)
        Assert.assertEquals(0, debt[TestHelper.thomas])
        Assert.assertEquals(0, debt[TestHelper.milena])

        debt = debts[YearMonth(2016, 12)]!!
        Assert.assertEquals(YearMonth(2016, 12), debt.month)
        Assert.assertEquals(25, debt[TestHelper.thomas])
        Assert.assertEquals(-25, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 1)]!!
        Assert.assertEquals(YearMonth(2017, 1), debt.month)
        Assert.assertEquals(40, debt[TestHelper.thomas])
        Assert.assertEquals(-40, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 2)]!!
        Assert.assertEquals(YearMonth(2017, 2), debt.month)
        Assert.assertEquals(45, debt[TestHelper.thomas])
        Assert.assertEquals(-45, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 3)]!!
        Assert.assertEquals(YearMonth(2017, 3), debt.month)
        Assert.assertEquals(35, debt[TestHelper.thomas])
        Assert.assertEquals(-35, debt[TestHelper.milena])
    }

    @Test
    fun addOneEnd() {
        endDate = YearMonth(2017, 2)
        initTest()
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 1, 1)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), LocalDate(2017, 1, 31)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), LocalDate(2017, 2, 1)))
        val debts = calculator.absoluteMap
        Assert.assertEquals(3, debts.size)

        var debt = debts[YearMonth(2016, 12)]!!
        Assert.assertEquals(YearMonth(2016, 12), debt.month)
        Assert.assertEquals(0, debt[TestHelper.thomas])
        Assert.assertEquals(0, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 1)]!!
        Assert.assertEquals(YearMonth(2017, 1), debt.month)
        Assert.assertEquals(-5, debt[TestHelper.thomas])
        Assert.assertEquals(5, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 2)]!!
        Assert.assertEquals(YearMonth(2017, 2), debt.month)
        Assert.assertEquals(0, debt[TestHelper.thomas])
        Assert.assertEquals(0, debt[TestHelper.milena])

    }

    @Test
    fun addWayEnd() {
        endDate = YearMonth(2017, 4)
        initTest()
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 1, 1)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), LocalDate(2017, 1, 31)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), LocalDate(2017, 4, 1)))
        val debts = calculator.absoluteMap
        Assert.assertEquals(5, debts.size)

        var debt = debts[YearMonth(2016, 12)]!!
        Assert.assertEquals(YearMonth(2016, 12), debt.month)
        Assert.assertEquals(0, debt[TestHelper.thomas])
        Assert.assertEquals(0, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 1)]!!
        Assert.assertEquals(YearMonth(2017, 1), debt.month)
        Assert.assertEquals(-5, debt[TestHelper.thomas])
        Assert.assertEquals(5, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 2)]!!
        Assert.assertEquals(YearMonth(2017, 2), debt.month)
        Assert.assertEquals(-5, debt[TestHelper.thomas])
        Assert.assertEquals(5, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 3)]!!
        Assert.assertEquals(YearMonth(2017, 3), debt.month)
        Assert.assertEquals(-5, debt[TestHelper.thomas])
        Assert.assertEquals(5, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 4)]!!
        Assert.assertEquals(YearMonth(2017, 4), debt.month)
        Assert.assertEquals(0, debt[TestHelper.thomas])
        Assert.assertEquals(0, debt[TestHelper.milena])

    }


    @Test
    fun editFirst() {
        endDate = YearMonth(2017, 2)
        initTest()
        val first = makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 1, 1))
        itemSubject.add(first)
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), LocalDate(2017, 1, 31)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), LocalDate(2017, 2, 1)))
        itemSubject.update(first, makeExpenses(TestHelper.makeCostDistribution(-10, -10, 0, 0), LocalDate(2017, 1, 1)))
        val debts = calculator.absoluteMap
        Assert.assertEquals(3, debts.size)

        var debt = debts[YearMonth(2016, 12)]!!
        Assert.assertEquals(YearMonth(2016, 12), debt.month)
        Assert.assertEquals(0, debt[TestHelper.thomas])
        Assert.assertEquals(0, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 1)]!!
        Assert.assertEquals(YearMonth(2017, 1), debt.month)
        Assert.assertEquals(5, debt[TestHelper.thomas])
        Assert.assertEquals(-5, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 2)]!!
        Assert.assertEquals(YearMonth(2017, 2), debt.month)
        Assert.assertEquals(10, debt[TestHelper.thomas])
        Assert.assertEquals(-10, debt[TestHelper.milena])
    }

    @Test
    fun editDateFromFirst() {
        endDate = YearMonth(2017, 2)
        initTest()
        val first = makeExpenses(TestHelper.makeCostDistribution(-10, 0, 0, -10), LocalDate(2017, 1, 1))
        itemSubject.add(first)
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), LocalDate(2017, 1, 31)))
        itemSubject.add(makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), LocalDate(2017, 2, 1)))
        itemSubject.update(first, makeExpenses(TestHelper.makeCostDistribution(-5, -10, -5, 0), LocalDate(2016, 12, 1)))
        val debts = calculator.absoluteMap
        Assert.assertEquals(4, debts.size)

        var debt = debts[YearMonth(2016, 11)]!!
        Assert.assertEquals(YearMonth(2016, 11), debt.month)
        Assert.assertEquals(0, debt[TestHelper.thomas])
        Assert.assertEquals(0, debt[TestHelper.milena])

        debt = debts[YearMonth(2016, 12)]!!
        Assert.assertEquals(YearMonth(2016, 12), debt.month)
        Assert.assertEquals(5, debt[TestHelper.thomas])
        Assert.assertEquals(-5, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 1)]!!
        Assert.assertEquals(YearMonth(2017, 1), debt.month)
        Assert.assertEquals(10, debt[TestHelper.thomas])
        Assert.assertEquals(-10, debt[TestHelper.milena])

        debt = debts[YearMonth(2017, 2)]!!
        Assert.assertEquals(YearMonth(2017, 2), debt.month)
        Assert.assertEquals(15, debt[TestHelper.thomas])
        Assert.assertEquals(-15, debt[TestHelper.milena])
    }

}