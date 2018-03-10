package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.model.calculation.StatisticsCalculatorService
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.BankAccount
import com.koenig.commonModel.finance.CostDistribution
import com.koenig.commonModel.finance.statistics.StatisticEntryDeprecated
import org.joda.time.DateTime
import org.joda.time.Period
import org.junit.Assert
import java.util.*

/**
 * Created by Thomas on 02.01.2018.
 */

object TestHelper {
    var milena = User("Milena")
    var thomas = User("Thomas")

    fun makeCostDistribution(theoryThomas: Int, realThomas: Int, theoryMilena: Int, realMilena: Int): CostDistribution {
        val costDistribution = CostDistribution()
        costDistribution.putCosts(thomas, realThomas, theoryThomas)
        costDistribution.putCosts(milena, realMilena, theoryMilena)
        return costDistribution
    }

    fun getDay(year: Int, month: Int, day: Int): DateTime {
        return DateTime(year, month, day, 0, 0)
    }

    fun getCalculatorService(statisticEntryList: List<StatisticEntryDeprecated>): StatisticsCalculatorService {
        return object : StatisticsCalculatorService {
            override fun getSavedSortedStatistics(): List<StatisticEntryDeprecated> {
                return statisticEntryList
            }

            override fun saveStatistics(statisticEntryList: List<StatisticEntryDeprecated>) {

            }
        }
    }

    fun getAssetsCalculatorService(map: Map<BankAccount, List<StatisticEntryDeprecated>>, start: DateTime, end: DateTime): AssetsCalculatorService {
        return object : AssetsCalculatorService {
            override val startDate: DateTime
                get() = start
            override val endDate: DateTime
                get() = end
            override val overallString: String
                get() = ""
            override val futureString: String
                get() = ""

            override fun loadAllBankAccountStatistics(): Map<BankAccount, List<StatisticEntryDeprecated>> {
                return map
            }


            override fun save(statisticEntryLists: Map<BankAccount, List<StatisticEntryDeprecated>>) {

            }

        }
    }

    fun assertDebtsList(index: Int, dateTime: DateTime, debts: Int, statisticEntryList: List<StatisticEntryDeprecated>) {
        val debt = statisticEntryList[index]
        Assert.assertEquals(dateTime, debt.date)
        Assert.assertEquals(debts, debt.getEntryFor(thomas))
        Assert.assertEquals(-debts, debt.getEntryFor(milena))
    }

    fun makeDebtsList(startDate: DateTime, debts: IntArray): List<StatisticEntryDeprecated> {
        var startDate = startDate
        val statisticEntryList = ArrayList<StatisticEntryDeprecated>(debts.size)
        for (i in debts.indices) {
            statisticEntryList.add(makeDebts(startDate, debts[i]))
            startDate = startDate.plus(Period.months(1))
        }

        return statisticEntryList
    }

    fun makeDebts(date: DateTime, debts: Int): StatisticEntryDeprecated {
        return StatisticEntryDeprecated(date, makeCostDistribution(debts, 0, 0, debts))
    }

    fun makeDebts(debts: Int, year: Int, month: Int, day: Int): StatisticEntryDeprecated {
        return makeDebts(getDay(year, month, day), debts)
    }

    fun getStatisticsCalculatorService(list: List<StatisticEntryDeprecated>): StatisticsCalculatorService {
        return object : StatisticsCalculatorService {
            override fun getSavedSortedStatistics(): List<StatisticEntryDeprecated> {
                return list
            }

            override fun saveStatistics(statisticEntryList: List<StatisticEntryDeprecated>) {

            }
        }
    }
}
