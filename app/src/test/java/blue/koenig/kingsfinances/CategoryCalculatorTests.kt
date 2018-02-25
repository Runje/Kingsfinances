package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.TestHelper.milena
import blue.koenig.kingsfinances.TestHelper.thomas
import blue.koenig.kingsfinances.features.category_statistics.CategoryCalculator
import blue.koenig.kingsfinances.features.category_statistics.CategoryCalculatorService
import blue.koenig.kingsfinances.model.calculation.MonthStatistic
import com.koenig.commonModel.finance.Expenses
import junit.framework.Assert
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.YearMonth
import org.joda.time.Years
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class CategoryCalculatorTests {
    fun makeExpenses(category: String, thomas: Int, milena: Int, dateTime: DateTime): Expenses {
        return Expenses("", category, "", thomas + milena, TestHelper.makeCostDistribution(thomas, thomas, milena, milena), dateTime, "")
    }


    @Test
    @Throws(Exception::class)
    fun categoryCalculation1() {
        val expensesItemSubject = TestExpensesSubject()
        val calculator = CategoryCalculator(Period.months(1), expensesItemSubject,
                getCategoryCalcService())
        var deltaExpenses: Map<YearMonth, MonthStatistic> = mutableMapOf()
        calculator.deltaStatisticsForAll.subscribe { deltaExpenses = it }
        expensesItemSubject.add(makeExpenses(category1, 10, 20, TestHelper.getDay(2017, 1, 2)))
        val statisticEntryList = calculator.getAbsoluteStatisticsFor(category1)
        Assert.assertEquals(1, statisticEntryList.size)

        val jan = YearMonth(2017, 1)
        val entry = statisticEntryList[jan]!!
        Assert.assertEquals(jan, entry.month)
        Assert.assertEquals(10, entry[TestHelper.thomas])
        Assert.assertEquals(20, entry[TestHelper.milena])

        // calculate statistics for overall
        var statistics = calculator.getCategoryStatistics(jan)
        Assert.assertEquals(2, statistics.size)
        var categoryStatistics = statistics[0]
        Assert.assertEquals(category1, categoryStatistics.name)
        Assert.assertEquals(30, categoryStatistics.winnings)

        categoryStatistics = statistics[1]
        Assert.assertEquals(calculator.allCategory, categoryStatistics.name)
        Assert.assertEquals(30, categoryStatistics.winnings)

        // calculate statistics for year before
        statistics = calculator.getCategoryStatistics(Years.years(2016))
        Assert.assertEquals(2, statistics.size)
        categoryStatistics = statistics[0]
        Assert.assertEquals(category1, categoryStatistics.name)
        Assert.assertEquals(0, categoryStatistics.winnings)

        categoryStatistics = statistics[1]
        Assert.assertEquals(calculator.allCategory, categoryStatistics.name)
        Assert.assertEquals(0, categoryStatistics.winnings)

        Assert.assertEquals(10, deltaExpenses[jan]!![thomas])
        Assert.assertEquals(20, deltaExpenses[jan]!![milena])

        expensesItemSubject.add(makeExpenses(category1, 10, 20, TestHelper.getDay(2015, 1, 31)))


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
        expensesItemSubject.add(makeExpenses(category1, 10, 20, TestHelper.getDay(2015, 4, 30)))
        val april15 = YearMonth(2015, 4)

        // calculate statistics for overall
        var monthStatistic = calculator.getAbsoluteStatisticsFor(category1)[jan]!!

        Assert.assertEquals(30, monthStatistic[thomas])
        Assert.assertEquals(60, monthStatistic[milena])

        monthStatistic = calculator.getAbsoluteStatisticsForAll()[jan]!!
        Assert.assertEquals(30, monthStatistic[thomas])
        Assert.assertEquals(60, monthStatistic[milena])


    }

    private fun getCategoryCalcService(): CategoryCalculatorService {
        return object : CategoryCalculatorService {
            override val absoluteCategoryMap: MutableMap<String, MutableMap<YearMonth, MonthStatistic>>
                get() = mutableMapOf()
            override val deltaCategoryMap: MutableMap<String, MutableMap<YearMonth, MonthStatistic>>
                get() = mutableMapOf()
            override val overallString: String
                get() = "All"
            override val startDate: DateTime
                get() = DateTime(2015, 1, 1, 0, 0)

            override fun saveStatistics(deltaCategoryMap: Map<String, Map<YearMonth, MonthStatistic>>, absoluteCategoryMap: MutableMap<String, MutableMap<YearMonth, MonthStatistic>>) {

            }

            override fun getGoalFor(category: String, month: YearMonth): Double {
                return 0.0
            }

            override fun getGoalFor(category: String, year: Years): Int {
                return 0
            }

            override val endDate = YearMonth(2017, 1)
        }
    }

    companion object {
        var category1 = "Category1"
    }

}