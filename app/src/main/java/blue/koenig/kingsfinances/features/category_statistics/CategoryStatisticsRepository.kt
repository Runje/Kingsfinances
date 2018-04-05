package blue.koenig.kingsfinances.features.category_statistics

import com.koenig.commonModel.Repository.CategoryRepository
import com.koenig.commonModel.Repository.GoalRepository
import com.koenig.commonModel.finance.statistics.lastEntryBefore
import com.koenig.commonModel.finance.statistics.yearMonthRange
import io.reactivex.Observable
import org.joda.time.YearMonth

/**
 * Created by Thomas on 09.03.2018.
 */
interface CategoryStatisticsRepository {
    fun getCategoryStatistics(start: YearMonth, end: YearMonth = start): List<CategoryStatistics>
    val statisticsChanged: Observable<Any>
}

class CategoryStatisticsDbRepository(val categoryRepository: CategoryRepository, val goalRepository: GoalRepository) : CategoryStatisticsRepository {
    override fun getCategoryStatistics(start: YearMonth, end: YearMonth): List<CategoryStatistics> {
        return categoryRepository.savedCategorys.map { category ->
            val map = categoryRepository.getCategoryAbsoluteStatistics(category)
            // end - (start - 1)

            val sum = lastEntryBefore(end.plusMonths(1), map).sum - (map[start.minusMonths(1)]?.sum
                    ?: 0)
            var goal = 0.0
            yearMonthRange(start, end).forEach { month ->
                goal += goalRepository.getGoalFor(category, month)
            }

            CategoryStatistics(category, sum, goal.toInt())
        }
    }

    override val statisticsChanged: Observable<Any>
        get() = categoryRepository.hasChanged.mergeWith(goalRepository.hasChanged)
}