package blue.koenig.kingsfinances.features.category_statistics


import io.reactivex.Observable

/**
 * Created by Thomas on 16.01.2018.
 */

interface CategoryStatisticsView {

    val monthSelection: Observable<Int>
    val yearSelection: Observable<Int>
    val categoryGoals: Observable<CatGoal>
    fun render(state: CategoryStatisticsState)
}
