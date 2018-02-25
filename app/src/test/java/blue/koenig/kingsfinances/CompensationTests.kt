package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.features.expenses.CompensationCalculator
import blue.koenig.kingsfinances.model.calculation.MonthStatistic
import blue.koenig.kingsfinances.model.calculation.StatisticEntryDeprecated
import junit.framework.Assert
import org.joda.time.DateTime
import org.joda.time.YearMonth
import org.junit.Test

/**
 * Created by Thomas on 14.02.2018.
 */
class CompensationTests {

    @Test
    fun calcCompensation() {
        val month = YearMonth(2017, 2)
        val deltaAssets = MonthStatistic(month, mapOf(TestHelper.thomas to 1000, TestHelper.milena to -1000))
        val expenses = MonthStatistic(month, mapOf(TestHelper.thomas to -1000, TestHelper.milena to 1000))

        val result = CompensationCalculator.calcCompensation(month, deltaAssets, expenses, listOf(TestHelper.milena, TestHelper.thomas), "Ausgleich", "Ausgleich")
        Assert.assertEquals(DateTime(2017, 2, 28, 0, 0, 0), result.date)
        Assert.assertEquals(2, result.costDistribution.getDistribution().size)
        Assert.assertEquals(2000, result.costDistribution.getCostsFor(TestHelper.thomas).Theory)
        Assert.assertEquals(-2000, result.costDistribution.getCostsFor(TestHelper.milena).Theory)
    }

    @Test
    fun calcCompensations1() {
        val startDate = YearMonth(2017, 1)
        val endDate = YearMonth(2017, 2)
        val allAssets = mapOf<YearMonth, MonthStatistic>(startDate to MonthStatistic(startDate, mapOf(TestHelper.thomas to 10)),
                endDate to MonthStatistic(endDate, mapOf(TestHelper.thomas to 20)))

        val expenses = mapOf<YearMonth, MonthStatistic>(startDate to MonthStatistic(startDate, mapOf(TestHelper.thomas to 0)),
                endDate to MonthStatistic(endDate, mapOf(TestHelper.thomas to 30)))
        val result = CompensationCalculator.calcCompensations(startDate, endDate, allAssets, expenses, listOf(TestHelper.thomas), "Ausgleich", "Ausgleich")
        Assert.assertEquals(2, result.size)
        var entry = result[0]
        Assert.assertEquals(TestHelper.getDay(2017, 1, 31), entry.date)
        Assert.assertEquals(1, entry.costDistribution.getDistribution().size)
        Assert.assertEquals(10, entry.costDistribution.getCostsFor(TestHelper.thomas).Theory)
        entry = result[1]
        Assert.assertEquals(TestHelper.getDay(2017, 2, 28), entry.date)
        Assert.assertEquals(1, entry.costDistribution.getDistribution().size)
        Assert.assertEquals(-10, entry.costDistribution.getCostsFor(TestHelper.thomas).Theory)
    }

    private fun createEntryForThomas(day: DateTime, value: Int): StatisticEntryDeprecated {
        return StatisticEntryDeprecated(day, mapOf(TestHelper.thomas to value))
    }
}