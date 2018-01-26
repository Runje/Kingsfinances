package blue.koenig.kingsfinances.features.category_statistics

import android.content.Context
import blue.koenig.kingsfamilylibrary.model.FamilyConfig
import blue.koenig.kingsfinances.model.database.GoalTable
import com.google.common.collect.Lists
import com.koenig.FamilyConstants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.Months
import org.joda.time.Period
import org.joda.time.Years
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory

/**
 * Created by Thomas on 16.01.2018.
 */

public class CategoryStatisticsPresenter(private val categoryCalculator: CategoryCalculator, private val goalTable: GoalTable, private val context: Context) {
    private var state: CategoryStatisticsState
    private var disposable: Disposable? = null
    private var view: CategoryStatisticsView? = null

    init {
        state = CategoryStatisticsState(calcStatistics(0, 0), categoryCalculator.yearsList, 0, 0, Lists.newArrayList(categoryCalculator.overallString))
    }

    fun attachView(view: CategoryStatisticsView) {
        this.view = view
        disposable = categoryCalculator.allStatistics.observeOn(AndroidSchedulers.mainThread()).subscribe(
                { statistics -> changeSelection(state.yearsSelection, state.monthsSelection) }
        ) { throwable -> logger.error("OnError: " + throwable.toString()) }

        view.monthSelection.observeOn(AndroidSchedulers.mainThread()).subscribe { pos ->
            changeMonthSelection(if (pos == -1) 0 else pos)
        }
        view.yearSelection.observeOn(AndroidSchedulers.mainThread()).subscribe { pos ->
            changeYearSelection(if (pos == -1) 0 else pos)
        }

        view.categoryGoals.observeOn(Schedulers.io()).subscribe { catGoal ->
            val year: Int = getYear()
            val goal: Int = calcYearGoal(catGoal.goal)
            // TODO: make goals for each user
            goalTable.saveGoal(catGoal.category, goal, year, statisticsUserId = FamilyConstants.ALL_USER.id, editFromUserId = FamilyConfig.getUserId(context))
            logger.info("Saved new goal: " + goal)
        }

        update()

    }

    private fun calcYearGoal(goal: Int): Int {
        // year or month
        if (state.monthsSelection == 0) return goal else return goal * 12
    }

    private fun getYear(yearsPos: Int = state.yearsSelection): Int {
        // cannot be overall
        check(yearsPos != 0)
        return Integer.parseInt(state.yearsList[yearsPos])
    }

    private fun changeYearSelection(pos: Int?) {
        logger.info("Change year: " + pos!!)
        if (pos == 0) {
            //if overall show only overall for months
            state = state.copy(monthsList = Lists.newArrayList(categoryCalculator.overallString), monthsSelection = 0)
        } else {
            state = state.copy(monthsList = categoryCalculator.monthsList)
        }

        changeSelection(pos, state.monthsSelection)
    }

    private fun changeMonthSelection(pos: Int?) {
        logger.info("Change month: " + pos!!)
        changeSelection(state.yearsSelection, pos)
    }

    private fun changeSelection(yearsPos: Int, monthPos: Int) {
        if (yearsPos == -1) return
        if (monthPos == -1) return
        if (yearsPos == state.yearsSelection && monthPos == state.monthsSelection) return
        val statistics = calcStatistics(yearsPos, monthPos)
        state = state.copy(categoryStatistics = statistics, yearsSelection = yearsPos, monthsSelection = monthPos)
        update();
    }

    private fun calcStatistics(yearsPos: Int, monthPos: Int): List<CategoryStatistics> {
        var startDate = categoryCalculator.startDate
        var endDate = DateTime.now().plus(Period.months(1)).withDayOfMonth(1)

        if (yearsPos != 0) {
            // not overall
            val year = getYear(yearsPos)
            if (monthPos != 0) {
                // not all month
                val date = DateTime.parse(state.monthsList[monthPos], DateTimeFormat.forPattern("MMM"))
                startDate = date.withYear(year).withDayOfMonth(1).withTimeAtStartOfDay();
                endDate = startDate.plus(Months.ONE);
            } else {
                // whole year
                startDate = DateTime(year, 1, 1, 0, 0)
                endDate = startDate.plus(Years.ONE)
            }
        }

        return categoryCalculator.getCategoryStatistics(startDate, endDate).filter { value -> value.winnings != 0 || value.goal != 0 }.sortedBy { value -> value.winnings }
    }


    private fun update() {
        view?.render(state)
    }


    fun detachView() {
        disposable?.dispose()
        view = null
    }

    companion object {
        protected var logger = LoggerFactory.getLogger("StatisticsPresenter")
    }
}
