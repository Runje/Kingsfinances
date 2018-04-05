package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.TestHelper.milena
import blue.koenig.kingsfinances.TestHelper.thomas
import blue.koenig.kingsfinances.features.category_statistics.CategoryStatistics
import blue.koenig.kingsfinances.features.category_statistics.CategoryStatisticsDbRepository
import com.koenig.commonModel.Repository.CategoryRepository
import com.koenig.commonModel.Repository.GoalRepository
import com.koenig.commonModel.finance.statistics.MonthStatistic
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import org.joda.time.YearMonth
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class CategoryStatisticsRepoTests {

    lateinit var categoryStatisticsRepo: CategoryStatisticsDbRepository

    @Mock
    lateinit var categoryRepo: CategoryRepository

    @Mock
    lateinit var goalRepo: GoalRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        categoryStatisticsRepo = CategoryStatisticsDbRepository(categoryRepo, goalRepo)
    }

    @Test
    fun bothEmpty() {
        whenever(goalRepo.getGoalFor(any<String>(), any<kotlin.Int>())).thenReturn(0)
        whenever(categoryRepo.getCategoryAbsoluteStatistics(any())).thenReturn(mutableMapOf<YearMonth, MonthStatistic>())

        val result = categoryStatisticsRepo.getCategoryStatistics(YearMonth(2015, 1), YearMonth(2016, 12))

        Assert.assertEquals(0, result.size)
    }


    @Test
    fun all() {
        whenever(goalRepo.getGoalFor(any(), any<YearMonth>())).thenAnswer {
            (it.arguments[1] as YearMonth).year * Integer.parseInt(it.arguments[0].toString())
        }
        whenever(categoryRepo.savedCategorys).thenReturn(listOf("1", "2", "3"))
        whenever(categoryRepo.getCategoryAbsoluteStatistics(any())).thenAnswer {
            val map = mutableMapOf<YearMonth, MonthStatistic>()
            map[YearMonth(2015, 12)] = MonthStatistic(YearMonth(2015, 12), mapOf(thomas to 0, milena to 10))
            map[YearMonth(2018, 12)] = MonthStatistic(YearMonth(2018, 12), mapOf(thomas to 30, milena to 20))
            map
        }

        val result = categoryStatisticsRepo.getCategoryStatistics(YearMonth(2016, 1), YearMonth(2018, 12))

        Assert.assertEquals(3, result.size)
        (0..2).forEach {
            Assert.assertEquals(CategoryStatistics((it + 1).toString(), 40, (it + 1) * 2017 * 12 * 3), result[it])
        }
    }

    @Test
    fun oneYear() {
        whenever(goalRepo.getGoalFor(any(), any<YearMonth>())).thenAnswer {
            (it.arguments[1] as YearMonth).year * Integer.parseInt(it.arguments[0].toString())
        }
        whenever(categoryRepo.savedCategorys).thenReturn(listOf("1", "2", "3"))
        whenever(categoryRepo.getCategoryAbsoluteStatistics(any())).thenAnswer {
            val map = mutableMapOf<YearMonth, MonthStatistic>()
            map[YearMonth(2017, 12)] = MonthStatistic(YearMonth(2017, 12), mapOf(thomas to 0, milena to 10))
            map[YearMonth(2018, 12)] = MonthStatistic(YearMonth(2018, 12), mapOf(thomas to 30, milena to 20))
            map
        }

        val result = categoryStatisticsRepo.getCategoryStatistics(YearMonth(2018, 1), YearMonth(2018, 12))

        Assert.assertEquals(3, result.size)
        (0..2).forEach {
            Assert.assertEquals(CategoryStatistics((it + 1).toString(), 40, (it + 1) * 2018 * 12), result[it])
        }
    }

    @Test
    fun oneMonth() {
        whenever(goalRepo.getGoalFor(any(), any<YearMonth>())).thenAnswer {
            (it.arguments[1] as YearMonth).year * Integer.parseInt(it.arguments[0].toString())
        }
        whenever(categoryRepo.savedCategorys).thenReturn(listOf("1", "2", "3"))
        whenever(categoryRepo.getCategoryAbsoluteStatistics(any())).thenAnswer {
            val map = mutableMapOf<YearMonth, MonthStatistic>()
            map[YearMonth(2018, 1)] = MonthStatistic(YearMonth(2018, 1), mapOf(thomas to 0, milena to 10))
            map[YearMonth(2018, 2)] = MonthStatistic(YearMonth(2018, 2), mapOf(thomas to 30, milena to 20))
            map
        }

        val result = categoryStatisticsRepo.getCategoryStatistics(YearMonth(2018, 2), YearMonth(2018, 2))

        Assert.assertEquals(3, result.size)
        (0..2).forEach {
            Assert.assertEquals(CategoryStatistics((it + 1).toString(), 40, (it + 1) * 2018), result[it])
        }
    }
}