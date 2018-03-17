package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.TestHelper.milena
import blue.koenig.kingsfinances.TestHelper.thomas
import com.koenig.commonModel.Repository.CategoryRepository
import com.koenig.commonModel.finance.Expenses
import com.koenig.commonModel.finance.statistics.CategoryCalculator
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
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class CategoryCalculatorTests {

    @Mock
    lateinit var categoryRepository: CategoryRepository


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

    }

    @Test
    @Throws(Exception::class)
    fun categoryCalculation1() {
        val expensesItemSubject = TestExpensesSubject()
        val endDate = YearMonth(2017, 1)

        val calculator = CategoryCalculator(expensesItemSubject, categoryRepository, Observable.just(endDate))
        var deltaExpenses: Map<YearMonth, MonthStatistic> = mutableMapOf()
        val category1 = "Category1"
        calculator.deltaStatisticsForAll.subscribe { deltaExpenses = it }
        expensesItemSubject.add(makeExpenses(category1, 10, 20, LocalDate(2017, 1, 2)))
        val statisticEntryList = calculator.getAbsoluteStatisticsFor(category1)
        Assert.assertEquals(2, statisticEntryList.size)

        val jan = YearMonth(2017, 1)
        val entry = statisticEntryList[jan]!!
        Assert.assertEquals(jan, entry.month)
        Assert.assertEquals(10, entry[TestHelper.thomas])
        Assert.assertEquals(20, entry[TestHelper.milena])

        // calculate statistics for overall
        val statistics = calculator.deltaStatisticsForAll.blockingFirst()
        var categoryStatistics = statistics[jan]!!
        Assert.assertEquals(30, categoryStatistics.entryMap.values.sum())



        expensesItemSubject.add(makeExpenses(category1, 10, 20, LocalDate(2015, 1, 31)))


        Assert.assertEquals(10, deltaExpenses[jan]!![thomas])
        Assert.assertEquals(20, deltaExpenses[jan]!![milena])

        val jan15 = YearMonth(2015, 1)
        Assert.assertEquals(10, deltaExpenses[jan15]!![thomas])
        Assert.assertEquals(20, deltaExpenses[jan15]!![milena])

        deltaExpenses.values.forEach {
            if (it.month != jan && it.month != jan15) {
                Assert.assertEquals(0, deltaExpenses[jan]!![thomas])
                Assert.assertEquals(0, deltaExpenses[jan]!![milena])
            }
        }

        // test missing month
        expensesItemSubject.add(makeExpenses(category1, 10, 20, LocalDate(2015, 4, 30)))
        val april15 = YearMonth(2015, 4)

        // calculate statistics for overall
        var monthStatistic = calculator.getAbsoluteStatisticsFor(category1)[jan]!!

        Assert.assertEquals(30, monthStatistic[thomas])
        Assert.assertEquals(60, monthStatistic[milena])

        monthStatistic = calculator.getAbsoluteStatisticsForAll()[jan]!!
        Assert.assertEquals(30, monthStatistic[thomas])
        Assert.assertEquals(60, monthStatistic[milena])

    }

    fun makeExpenses(category: String, thomas: Int, milena: Int, dateTime: LocalDate): Expenses {
        return Expenses("", category, "", thomas + milena, TestHelper.makeCostDistribution(thomas, thomas, milena, milena), dateTime, "")
    }

}