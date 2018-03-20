package blue.koenig.kingsfinances.features.statistics


import blue.koenig.kingsfinances.model.calculation.IncomeCalculator
import blue.koenig.kingsfinances.model.database.GoalTable
import com.koenig.FamilyConstants
import com.koenig.FamilyConstants.ALL_USER
import com.koenig.commonModel.Goal
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.statistics.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.YearMonth
import org.joda.time.Years
import org.slf4j.LoggerFactory
import kotlin.math.max

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
        var startMonth = assetsCalculator.startDate
        var endMonth = FamilyConstants.UNLIMITED.yearMonth
        var entrysForFutureForecast: Map<YearMonth, MonthStatistic>? = null
        // get all users (familymembers)
        val statUsers = if (position == 1) arrayListOf(AssetsCalculator.FORECAST_USER) else membersWithForecastAndGoal
        if (position == 1) {
            // overall, include future forecast
            entrysForFutureForecast = assetsCalculator.entrysForFutureForecast
        } else if (position != 0) {
            // not overall
            val year = Integer.parseInt(state.yearsList[position])
            startMonth = YearMonth(year, 1)
            endMonth = startMonth.plus(Years.ONE)
        }

        val statistics = assetsCalculator.calcStatisticsFor(startMonth, endMonth, withYearForecast = LocalDate().year == startMonth.year)
        val savingRate = calcSavingRate(statistics.startDate, statistics.endDate, statistics.overallWin, incomeCalculator.absoluteMap)
        val statisticsToShow: AssetsStatistics = if (entrysForFutureForecast != null) AssetsStatistics(statistics.startDate, statistics.endDate, entrysForFutureForecast, statistics.monthlyWin, statistics.overallWin) else statistics
        val lastGoalDate = statistics.assets.maxBy { it.key }!!.key
        val goals = calcGoals(startMonth, lastGoalDate, goalTable.allItems, assetsCalculator.getStartValueFor(ALL_USER))

        // remove all goals from statistics(shouldn't be there, but gets there because of mutability of statisticsEntry!
        //statistics.assets.forEach { entry -> entry.putEntry(FamilyConstants.GOAL_ALL_USER, 0) }
        // add goal statistics to other statistics

        val assets = statisticsToShow.assets.toMutableMap()
        for ((month, goal) in goals) {
            assets[month] = (statistics.assets[month] ?: MonthStatistic(month)) + goal
        }

        changeStateTo(state.copy(statistics = statisticsToShow.copy(assets = assets), savingRate = savingRate, users = statUsers))
    }


    companion object {
        protected var logger = LoggerFactory.getLogger("StatisticsPresenter")


        fun calcSavingRate(startDate: YearMonth, endDate: YearMonth, overallWin: Int, incomes: Map<YearMonth, MonthStatistic>): Float {
            val allSavings = lastEntryBefore(endDate.plusMonths(1), incomes).sum - (incomes[startDate.minusMonths(1)]?.sum
                    ?: 0)
            return if (allSavings == 0) {
                0f
            } else overallWin / allSavings.toFloat()
        }

        fun calcGoals(beforeDate: YearMonth, afterDate: YearMonth, allItems: List<Goal>, startValue: Int = 0): Map<YearMonth, MonthStatistic> {
            // first year of data
            val minYear: Int = allItems.minBy { it ->
                it.goals.keys.min() ?: 3000
            }?.goals?.keys?.min() ?: 0
            // last year of data
            val maxYear = allItems.maxBy { it -> it.goals.keys.min() ?: 0 }?.goals?.keys?.max()
                    ?: FamilyConstants.UNLIMITED.year
            val result = mutableMapOf<YearMonth, MonthStatistic>()
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
            var lastYear = afterDate.year
            // if it is unlimited make data until we have data
            if (lastYear == FamilyConstants.UNLIMITED.year) lastYear = maxYear
            var date = YearMonth(firstYear, 1)
            var goal = sumUntilYear
            for (year in firstYear..lastYear) {
                // TODO: cents going lost due to rounding error
                val monthlyGoal = allItems.sumBy { it -> it.goals[year] ?: 0 } / 12.0
                for (month in 1..12) {
                    if (date in beforeDate..afterDate) {
                        result[date] = MonthStatistic(date, mutableMapOf(FamilyConstants.GOAL_ALL_USER to goal))
                    }

                    date = date.plus(Period.months(1))
                    goal = (goal + monthlyGoal).toInt()
                }

            }

            // add last one
            if (date in beforeDate..afterDate) {
                result[date] = MonthStatistic(date, mutableMapOf(FamilyConstants.GOAL_ALL_USER to goal))
            }


            return result
        }

    }
}
