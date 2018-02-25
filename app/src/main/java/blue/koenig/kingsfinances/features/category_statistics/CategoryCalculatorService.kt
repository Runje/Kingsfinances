package blue.koenig.kingsfinances.features.category_statistics

import blue.koenig.kingsfinances.model.calculation.MonthStatistic
import org.joda.time.DateTime
import org.joda.time.YearMonth
import org.joda.time.Years

/**
 * Created by Thomas on 19.01.2018.
 */

interface CategoryCalculatorService {
    val absoluteCategoryMap: MutableMap<String, MutableMap<YearMonth, MonthStatistic>>
    val deltaCategoryMap: MutableMap<String, MutableMap<YearMonth, MonthStatistic>>

    val overallString: String

    val startDate: DateTime

    fun saveStatistics(deltaCategoryMap: Map<String, Map<YearMonth, MonthStatistic>>, absoluteCategoryMap: MutableMap<String, MutableMap<YearMonth, MonthStatistic>>)

    fun getGoalFor(category: String, month: YearMonth): Double
    fun getGoalFor(category: String, year: Years): Int
    val endDate: YearMonth
}
