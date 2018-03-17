package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.features.statistics.StatisticsPresenter
import com.koenig.FamilyConstants
import com.koenig.commonModel.Goal
import com.koenig.commonModel.finance.statistics.MonthStatistic
import junit.framework.Assert
import org.joda.time.YearMonth
import org.junit.Test

/**
 * Created by Thomas on 28.01.2018.
 */
class GoalsTest {

    @Test
    fun sumUntilTest() {
        val goals = StatisticsPresenter.calcGoals(YearMonth(2016, 1), YearMonth(2017, 1), listOf(Goal("", mutableMapOf(2016 to 12000), ""), Goal("", mutableMapOf(2015 to 1000), "")))
        Assert.assertEquals(13, goals.size)
        testGoal(YearMonth(2016, 1), 1000, goals)
        testGoal(YearMonth(2016, 2), 2000, goals)
        testGoal(YearMonth(2016, 3), 3000, goals)
        testGoal(YearMonth(2016, 4), 4000, goals)
        testGoal(YearMonth(2016, 5), 5000, goals)
        testGoal(YearMonth(2016, 6), 6000, goals)
        testGoal(YearMonth(2016, 7), 7000, goals)
        testGoal(YearMonth(2016, 8), 8000, goals)
        testGoal(YearMonth(2016, 9), 9000, goals)
        testGoal(YearMonth(2016, 10), 10000, goals)
        testGoal(YearMonth(2016, 11), 11000, goals)
        testGoal(YearMonth(2016, 12), 12000, goals)
        testGoal(YearMonth(2017, 1), 13000, goals)
    }

    @Test
    fun noGoalsTest() {
        val goals = StatisticsPresenter.calcGoals(YearMonth(2016, 1), YearMonth(2017, 1), listOf(Goal("", mutableMapOf(2015 to 1000), "")))
        Assert.assertEquals(13, goals.size)
        testGoal(YearMonth(2016, 1), 1000, goals)
        testGoal(YearMonth(2016, 2), 1000, goals)
        testGoal(YearMonth(2016, 3), 1000, goals)
        testGoal(YearMonth(2016, 4), 1000, goals)
        testGoal(YearMonth(2016, 5), 1000, goals)
        testGoal(YearMonth(2016, 6), 1000, goals)
        testGoal(YearMonth(2016, 7), 1000, goals)
        testGoal(YearMonth(2016, 8), 1000, goals)
        testGoal(YearMonth(2016, 9), 1000, goals)
        testGoal(YearMonth(2016, 10), 1000, goals)
        testGoal(YearMonth(2016, 11), 1000, goals)
        testGoal(YearMonth(2016, 12), 1000, goals)
        testGoal(YearMonth(2017, 1), 1000, goals)
    }

    private fun testGoal(month: YearMonth, value: Int, goals: Map<YearMonth, MonthStatistic>) {
        val statisticEntry = goals[month]!!
        Assert.assertEquals(month, statisticEntry.month)
        Assert.assertEquals(value, statisticEntry[FamilyConstants.GOAL_ALL_USER])
    }

}