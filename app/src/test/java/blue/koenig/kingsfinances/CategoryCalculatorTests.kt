package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.features.category_statistics.CategoryCalculator
import blue.koenig.kingsfinances.features.category_statistics.CategoryCalculatorService
import blue.koenig.kingsfinances.model.calculation.StatisticEntry
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
class CategoryCalculatorTests {

    fun makeExpenses(category: String, thomas: Int, milena: Int, dateTime: DateTime): Expenses {
        return Expenses("", category, "", thomas + milena, TestHelper.makeCostDistribution(thomas, thomas, milena, milena), dateTime, "")
    }


    @Test
    @Throws(Exception::class)
    fun categoryCalculation1() {
        val expensesItemSubject = TestExpensesSubject()
        val calculator = CategoryCalculator(Period.months(1), expensesItemSubject,
                getCategoryCalcService(HashMap()))
        expensesItemSubject.add(makeExpenses(category1, 10, 20, TestHelper.getDay(2017, 1, 2)))
        val statisticEntryList = calculator.getStatisticsFor(category1)
        Assert.assertEquals(2, statisticEntryList.size)

        var entry = statisticEntryList[0]
        Assert.assertEquals(TestHelper.getDay(2017, 1, 1), entry.date)
        Assert.assertEquals(0, entry.getEntryFor(TestHelper.thomas))
        Assert.assertEquals(0, entry.getEntryFor(TestHelper.milena))

        entry = statisticEntryList[1]
        Assert.assertEquals(TestHelper.getDay(2017, 2, 1), entry.date)
        Assert.assertEquals(10, entry.getEntryFor(TestHelper.thomas))
        Assert.assertEquals(20, entry.getEntryFor(TestHelper.milena))

        // calculate statistics for overall
        var statistics = calculator.getCategoryStatistics(TestHelper.getDay(2017, 1, 1), TestHelper.getDay(2017, 2, 1))
        Assert.assertEquals(1, statistics.size)
        var categoryStatistics = statistics[0]
        Assert.assertEquals(category1, categoryStatistics.name)
        Assert.assertEquals(30, categoryStatistics.winnings)

        // calculate statistics for year before
        statistics = calculator.getCategoryStatistics(TestHelper.getDay(2015, 1, 1), TestHelper.getDay(2016, 1, 1))
        Assert.assertEquals(1, statistics.size)
        categoryStatistics = statistics[0]
        Assert.assertEquals(category1, categoryStatistics.name)
        Assert.assertEquals(0, categoryStatistics.winnings)
    }

    private fun getCategoryCalcService(map: HashMap<String, List<StatisticEntry>>): CategoryCalculatorService {
        return object : CategoryCalculatorService {
            override fun getCategoryMap(): Map<String, List<StatisticEntry>> {
                return map
            }

            override fun saveStatistics(categoryMap: Map<String, List<StatisticEntry>>) {

            }

            override fun getOverallString(): String {
                return "ALL"
            }

            override fun getStartDate(): DateTime {
                return DateTime(2015, 1, 1, 0, 0)
            }

            override fun getGoalFor(category: String, startDate: DateTime, endDate: DateTime): Int {
                return 0
            }
        }
    }

    companion object {
        var category1 = "Category1"
    }

}