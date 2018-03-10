package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.TestHelper.getDay
import blue.koenig.kingsfinances.features.statistics.StatisticsPresenter
import com.koenig.FamilyConstants
import com.koenig.commonModel.Goal
import com.koenig.commonModel.finance.statistics.StatisticEntryDeprecated
import junit.framework.Assert
import org.joda.time.DateTime
import org.junit.Test

/**
 * Created by Thomas on 28.01.2018.
 */
class GoalsTest {

    @Test
    fun sumUntilTest() {
        val goals = StatisticsPresenter.calcGoals(getDay(2016, 1, 1), getDay(2017, 1, 1), listOf(Goal("", mutableMapOf(2016 to 12000), ""), Goal("", mutableMapOf(2015 to 1000), "")))
        Assert.assertEquals(13, goals.size)
        testGoal(0, getDay(2016, 1, 1), 1000, goals)
        testGoal(1, getDay(2016, 2, 1), 2000, goals)
        testGoal(2, getDay(2016, 3, 1), 3000, goals)
        testGoal(3, getDay(2016, 4, 1), 4000, goals)
        testGoal(4, getDay(2016, 5, 1), 5000, goals)
        testGoal(5, getDay(2016, 6, 1), 6000, goals)
        testGoal(6, getDay(2016, 7, 1), 7000, goals)
        testGoal(7, getDay(2016, 8, 1), 8000, goals)
        testGoal(8, getDay(2016, 9, 1), 9000, goals)
        testGoal(9, getDay(2016, 10, 1), 10000, goals)
        testGoal(10, getDay(2016, 11, 1), 11000, goals)
        testGoal(11, getDay(2016, 12, 1), 12000, goals)
        testGoal(12, getDay(2017, 1, 1), 13000, goals)
    }

    @Test
    fun noGoalsTest() {
        val goals = StatisticsPresenter.calcGoals(getDay(2016, 1, 1), getDay(2017, 1, 1), listOf(Goal("", mutableMapOf(2015 to 1000), "")))
        Assert.assertEquals(13, goals.size)
        testGoal(0, getDay(2016, 1, 1), 1000, goals)
        testGoal(1, getDay(2016, 2, 1), 1000, goals)
        testGoal(2, getDay(2016, 3, 1), 1000, goals)
        testGoal(3, getDay(2016, 4, 1), 1000, goals)
        testGoal(4, getDay(2016, 5, 1), 1000, goals)
        testGoal(5, getDay(2016, 6, 1), 1000, goals)
        testGoal(6, getDay(2016, 7, 1), 1000, goals)
        testGoal(7, getDay(2016, 8, 1), 1000, goals)
        testGoal(8, getDay(2016, 9, 1), 1000, goals)
        testGoal(9, getDay(2016, 10, 1), 1000, goals)
        testGoal(10, getDay(2016, 11, 1), 1000, goals)
        testGoal(11, getDay(2016, 12, 1), 1000, goals)
        testGoal(12, getDay(2017, 1, 1), 1000, goals)
    }

    private fun testGoal(index: Int, day: DateTime, value: Int, goals: List<StatisticEntryDeprecated>) {
        val statisticEntry = goals[index]
        Assert.assertEquals(day, statisticEntry.date)
        Assert.assertEquals(value, statisticEntry.getEntryFor(FamilyConstants.GOAL_ALL_USER))
    }
}