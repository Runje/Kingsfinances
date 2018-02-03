package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.features.statistics.AssetsCalculatorService
import blue.koenig.kingsfinances.model.calculation.StatisticEntry
import blue.koenig.kingsfinances.model.calculation.StatisticsCalculatorService
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.BankAccount
import com.koenig.commonModel.finance.CostDistribution
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

    fun getCalculatorService(statisticEntryList: List<StatisticEntry>): StatisticsCalculatorService {
        return object : StatisticsCalculatorService {
            override fun getSavedSortedStatistics(): List<StatisticEntry> {
                return statisticEntryList
            }

            override fun saveStatistics(statisticEntryList: List<StatisticEntry>) {

            }
        }
    }

    fun getAssetsCalculatorService(map: Map<BankAccount, List<StatisticEntry>>, start: DateTime, end: DateTime): AssetsCalculatorService {
        return object : AssetsCalculatorService {
            override fun loadAllBankAccountStatistics(): Map<BankAccount, List<StatisticEntry>> {
                return map
            }

            override fun getStartDate(): DateTime {
                return start
            }

            override fun getEndDate(): DateTime {
                return end
            }

            override fun getOverallString(): String? {
                return null
            }

            override fun save(statisticEntryLists: Map<BankAccount, List<StatisticEntry>>) {

            }

            override fun getFutureString(): String? {
                return null
            }
        }
    }

    fun assertDebtsList(index: Int, dateTime: DateTime, debts: Int, statisticEntryList: List<StatisticEntry>) {
        val debt = statisticEntryList[index]
        Assert.assertEquals(dateTime, debt.date)
        Assert.assertEquals(debts, debt.getEntryFor(thomas))
        Assert.assertEquals(-debts, debt.getEntryFor(milena))
    }

    fun makeDebtsList(startDate: DateTime, debts: IntArray): List<StatisticEntry> {
        var startDate = startDate
        val statisticEntryList = ArrayList<StatisticEntry>(debts.size)
        for (i in debts.indices) {
            statisticEntryList.add(makeDebts(startDate, debts[i]))
            startDate = startDate.plus(Period.months(1))
        }

        return statisticEntryList
    }

    fun makeDebts(date: DateTime, debts: Int): StatisticEntry {
        return StatisticEntry(date, makeCostDistribution(debts, 0, 0, debts))
    }

    fun makeDebts(debts: Int, year: Int, month: Int, day: Int): StatisticEntry {
        return makeDebts(getDay(year, month, day), debts)
    }

    fun getStatisticsCalculatorService(list: List<StatisticEntry>): StatisticsCalculatorService {
        return object : StatisticsCalculatorService {
            override fun getSavedSortedStatistics(): List<StatisticEntry> {
                return list
            }

            override fun saveStatistics(statisticEntryList: List<StatisticEntry>) {

            }
        }
    }
}
