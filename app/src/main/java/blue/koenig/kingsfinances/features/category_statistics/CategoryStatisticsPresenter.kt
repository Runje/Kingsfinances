package blue.koenig.kingsfinances.features.category_statistics

import blue.koenig.kingsfamilylibrary.model.communication.ServerConnection
import blue.koenig.kingsfinances.model.StatisticsUtils
import blue.koenig.kingsfinances.model.database.GoalTable
import blue.koenig.kingsfinances.model.database.PendingTable
import com.google.common.collect.Lists
import com.koenig.FamilyConstants
import com.koenig.commonModel.Component
import com.koenig.commonModel.finance.FinanceConfig
import com.koenig.commonModel.finance.statistics.createYearsList
import com.koenig.communication.messages.AUDMessage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.YearMonth
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory

/**
 * Created by Thomas on 16.01.2018.
 */

class CategoryStatisticsPresenter(private val categoryStatisticsRepository: CategoryStatisticsRepository, private val goalTable: GoalTable, private val config: FinanceConfig, private val pendingTable: PendingTable, private val connection: ServerConnection) {
    private var state: CategoryStatisticsState
    private var disposable: Disposable? = null
    private var view: CategoryStatisticsView? = null
    private fun generateYearsList(): List<String> {
        val list = Lists.newArrayList(FamilyConstants.OVERALL_STRING)
        list.addAll(createYearsList(config.startMonth, YearMonth()))
        return list
    }

    private fun generateMonthsList(): List<String> {
        val list = Lists.newArrayList(FamilyConstants.OVERALL_STRING)
        list.addAll(StatisticsUtils.allMonthsList())
        return list
    }
    init {
        state = CategoryStatisticsState(calcStatistics(0, 0), generateYearsList(), 0, 0, Lists.newArrayList(FamilyConstants.OVERALL_STRING))
    }

    fun attachView(view: CategoryStatisticsView) {
        this.view = view
        disposable = categoryStatisticsRepository.statisticsChanged.observeOn(AndroidSchedulers.mainThread()).subscribe(
                { changeSelection(state.yearsSelection, state.monthsSelection) }
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
            val updatedGoal = goalTable.saveGoal(catGoal.category, goal, year, statisticsUserId = FamilyConstants.ALL_USER.id, editFromUserId = config.userId)

            // add to pending operations
            val operation = pendingTable.addUpdate(updatedGoal, config.userId)
            // send to server
            connection.sendFamilyMessage(AUDMessage(Component.FINANCE, operation))
            logger.info("Saved new goal: $goal")
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
            state = state.copy(monthsList = Lists.newArrayList(FamilyConstants.OVERALL_STRING), monthsSelection = 0)
        } else {
            state = state.copy(monthsList = generateMonthsList())
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
        update()
    }

    private fun calcStatistics(yearsPos: Int, monthPos: Int): List<CategoryStatistics> {
        val result = if (yearsPos != 0) {
            // not overall
            val year = getYear(yearsPos)
            if (monthPos != 0) {
                // not all month
                val date = DateTime.parse(state.monthsList[monthPos], DateTimeFormat.forPattern("MMM"))
                val yearMonth = YearMonth(year, date.monthOfYear)
                categoryStatisticsRepository.getCategoryStatistics(yearMonth)
            } else {
                // whole year
                categoryStatisticsRepository.getCategoryStatistics(YearMonth(year, 1), YearMonth(year, 12))
            }
        } else {
            // overall
            categoryStatisticsRepository.getCategoryStatistics(config.startMonth, YearMonth())
        }

        return result.sortedBy { value -> value.winnings }
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
