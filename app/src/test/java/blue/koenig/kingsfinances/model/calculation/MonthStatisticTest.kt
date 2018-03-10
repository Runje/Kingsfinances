package blue.koenig.kingsfinances.model.calculation

import blue.koenig.kingsfinances.TestHelper.milena
import blue.koenig.kingsfinances.TestHelper.thomas
import com.koenig.commonModel.finance.statistics.MonthStatistic
import org.joda.time.YearMonth
import org.junit.Assert
import org.junit.Test

/**
 * Created by Thomas on 24.02.2018.
 */
class MonthStatisticTest {
    @Test
    fun plus() {
        val a = MonthStatistic(YearMonth(17, 1), mapOf(thomas to 10))
        val b = MonthStatistic(YearMonth(7, 5), mapOf(thomas to 10, milena to 20))
        val expected = MonthStatistic(YearMonth(17, 1), mapOf(thomas to 20, milena to 20))
        Assert.assertEquals(expected, a + b)
    }

    @Test
    fun minus() {
        val a = MonthStatistic(YearMonth(17, 1), mapOf())
        val b = MonthStatistic(YearMonth(7, 5), mapOf(thomas to 10, milena to 20))
        val expected = MonthStatistic(YearMonth(17, 1), mapOf(thomas to -10, milena to -20))
        Assert.assertEquals(expected, a - b)
    }

}