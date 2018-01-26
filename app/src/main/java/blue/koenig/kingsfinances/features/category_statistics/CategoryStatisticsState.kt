package blue.koenig.kingsfinances.features.category_statistics

/**
 * Created by Thomas on 16.01.2018.
 */

data public class CategoryStatisticsState(val categoryStatistics: List<CategoryStatistics>, val yearsList: List<String>, val yearsSelection: Int, val monthsSelection: Int, val monthsList: List<String>)
