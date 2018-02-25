package blue.koenig.kingsfinances.features.statistics


import blue.koenig.kingsfinances.model.StatisticsUtils.calcDifferenceInPeriod
import blue.koenig.kingsfinances.model.calculation.IncomeCalculator
import blue.koenig.kingsfinances.model.calculation.StatisticEntryDeprecated
import blue.koenig.kingsfinances.model.database.GoalTable
import com.koenig.FamilyConstants
import com.koenig.FamilyConstants.ALL_USER
import com.koenig.commonModel.Goal
import com.koenig.commonModel.User
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.Years
import org.slf4j.LoggerFactory
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Thomas on 07.01.2018.
 */

class StatisticsPresenter(private var assetsCalculator: AssetsCalculator, private var incomeCalculator: IncomeCalculator, private val goalTable: GoalTable, private val familyMembers: List<User>) {
    internal var view: StatisticsView? = null
    private var state: StatisticsState = StatisticsState(AssetsStatistics(), 0f, assetsCalculator.yearsList, 0, listOf())
    private var disposable: Disposable? = null
    private val membersWithForecastAndGoal by lazy {
        val list = familyMembers.toMutableList()
        list.add(FamilyConstants.ALL_USER)
        list.add(AssetsCalculator.FORECAST_USER)
        list.add(FamilyConstants.GOAL_ALL_USER)
        list
    }

    fun attachView(view: StatisticsView) {
        this.view = view
        disposable = assetsCalculator.allAssets.observeOn(AndroidSchedulers.mainThread()).subscribe(
                { clickYear(state.position) }
        ) { throwable -> logger.error("OnError: " + throwable.toString()) }
    }

    private fun changeStateTo(newState: StatisticsState) {
        state = newState
        if (view != null) view!!.render(state)
    }

    fun detachView() {
        disposable?.dispose()
        view = null
    }

    fun clickYear(position: Int) {
        var beforeDate = DateTime(0)
        var afterDate = FamilyConstants.UNLIMITED
        var entrysForFutureForecast: List<StatisticEntryDeprecated>? = null
        // get all users (familymembers)
        val statUsers = if (position == 1) arrayListOf(AssetsCalculator.FORECAST_USER) else membersWithForecastAndGoal
        if (position == 1) {
            entrysForFutureForecast = assetsCalculator.entrysForFutureForecast
        } else if (position != 0) {
            // not overall
            val year = Integer.parseInt(state.yearsList[position])
            beforeDate = DateTime(year, 1, 1, 0, 0)
            afterDate = beforeDate.plus(Years.ONE)
        }

        val statistics = assetsCalculator.calcStatisticsFor(beforeDate, afterDate)
        val savingRate = calcSavingRate(statistics.startDate, statistics.endDate, statistics.overallWin, incomeCalculator.entrys)
        val statisticsToShow = if (entrysForFutureForecast != null) AssetsStatistics(statistics.startDate, statistics.endDate, entrysForFutureForecast, statistics.monthlyWin, statistics.overallWin) else statistics
        val lastGoalDate = statistics.assets.last().date
        val goals = calcGoals(beforeDate, lastGoalDate, goalTable.allItems, assetsCalculator.getStartValueFor(ALL_USER))

        // remove all goals from statistics(shouldn't be there, but gets there because of mutability of statisticsEntry!
        statistics.assets.forEach { entry -> entry.putEntry(FamilyConstants.GOAL_ALL_USER, 0) }
        // add goal statistics to other statistics
        // find start of goals
        val startIndex = goals.indexOfFirst { statisticEntry ->
            statistics.assets[0]?.date?.equals(statisticEntry.date) ?: false
        }
        if (startIndex != -1) {
            for (i in 0 until min(statistics.assets.size, goals.size - startIndex)) {
                check(statistics.assets[i].date.equals(goals[i + startIndex].date)) { "dates are not aligned" }
                statistics.assets[i].addEntry(goals[i + startIndex])
            }
        } else {
            // goals beginning later
            val offset = statistics.assets.indexOfFirst { statisticEntry ->
                goals[0].date.equals(statisticEntry.date)
            }
            if (offset != -1) {
                for (i in 0 until min(goals.size, statistics.assets.size - offset)) {
                    check(statistics.assets[i + offset].date.equals(goals[i].date)) { "dates are not aligned" }
                    statistics.assets[i + offset].addEntry(goals[i])
                }
            }
        }



        changeStateTo(state.copy(statistics = statisticsToShow, savingRate = savingRate, users = statUsers))
    }


    companion object {
        protected var logger = LoggerFactory.getLogger("StatisticsPresenter")


        fun calcSavingRate(startDate: DateTime, endDate: DateTime, overallWin: Int, incomes: List<StatisticEntryDeprecated>): Float {

            val allSavings = calcDifferenceInPeriod(startDate, endDate, incomes)
            return if (allSavings.sum == 0) {
                0f
            } else overallWin / allSavings.sum.toFloat()


        }

        fun calcGoals(beforeDate: DateTime, afterDate: DateTime, allItems: List<Goal>, startValue: Int = 0): List<StatisticEntryDeprecated> {
            // first year of data
            val minYear: Int = allItems.minBy { it ->
                it.goals.keys.min() ?: 3000
            }?.goals?.keys?.min() ?: 0
            // last year of data
            val maxYear = allItems.maxBy { it -> it.goals.keys.min() ?: 0 }?.goals?.keys?.max()
                    ?: 3000
            val result = arrayListOf<StatisticEntryDeprecated>()
            // sum goals up until year of beforeDate
            val firstYear = max(beforeDate.year, minYear)
            val sumUntilYear = startValue + if (firstYear > minYear) allItems.sumBy { it ->
                var sum = 0
                for ((year, goal) in it.goals) {
                    if (year < firstYear) sum += goal
                }
                sum
            } else 0

            // last year is -1 if it is the first day of the year
            var lastYear = if (afterDate.monthOfYear == 12 && afterDate.dayOfMonth == 1) afterDate.year - 1 else afterDate.year
            // if it is unlimited make data until we have data
            if (lastYear == FamilyConstants.UNLIMITED.year) lastYear = maxYear
            var date = DateTime(firstYear, 1, 1, 0, 0, 0)
            var goal = sumUntilYear
            for (year in firstYear..lastYear) {
                // TODO: cents going lost due to rounding error
                val monthlyGoal = allItems.sumBy { it -> it.goals[year] ?: 0 } / 12.0
                for (month in 1..12) {
                    if (!date.isBefore(beforeDate) && !date.isAfter(afterDate)) {
                        result.add(StatisticEntryDeprecated(date, mutableMapOf(FamilyConstants.GOAL_ALL_USER to goal)))
                    }

                    date = date.plus(Period.months(1))
                    goal = (goal + monthlyGoal).toInt()
                }

            }

            // add last one
            if (!date.isBefore(beforeDate) && !date.isAfter(afterDate)) {
                result.add(StatisticEntryDeprecated(date, mutableMapOf(FamilyConstants.GOAL_ALL_USER to goal)))
            }


            return result
        }

    }
}
