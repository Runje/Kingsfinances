package blue.koenig.kingsfinances.features.statistics

import blue.koenig.kingsfinances.model.StatisticsUtils
import blue.koenig.kingsfinances.model.calculation.ItemSubject
import blue.koenig.kingsfinances.model.calculation.MonthStatistic
import blue.koenig.kingsfinances.model.calculation.StatisticEntryDeprecated
import blue.koenig.kingsfinances.model.calculation.yearMonth
import com.google.common.collect.Lists
import com.koenig.FamilyConstants.ALL_USER
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.Balance
import com.koenig.commonModel.finance.BankAccount
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.*
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by Thomas on 03.01.2018.
 */

class AssetsCalculator(protected var period: Period, bankSubject: ItemSubject<BankAccount>, protected var service: AssetsCalculatorService) {
    private val startDate: DateTime
    private val endDate: DateTime
    val yearsList: List<String>
    private var logger = LoggerFactory.getLogger(javaClass.simpleName)
    private var statisticEntryLists: MutableMap<BankAccount, List<StatisticEntryDeprecated>> = service.loadAllBankAccountStatistics().toMutableMap()
    private var lock = ReentrantLock()
    val allAssets: BehaviorSubject<List<StatisticEntryDeprecated>>
    val deltaAssetsForAll: BehaviorSubject<Map<YearMonth, MonthStatistic>>

    val entrysForAll: List<StatisticEntryDeprecated>
        get() {
            return statisticEntryLists[ALL_ASSETS] ?: return ArrayList()
        }

    val entrysForForecast: List<StatisticEntryDeprecated>
        get() {
            return statisticEntryLists[YEAR_FORECAST] ?: return ArrayList()
        }

    val entrysForFutureForecast: List<StatisticEntryDeprecated>
        get() {
            return statisticEntryLists[FUTURE_FORECAST] ?: return ArrayList()
        }

    init {
        var allAssetsStartValue: List<StatisticEntryDeprecated>? = statisticEntryLists[ALL_ASSETS]
        if (allAssetsStartValue == null) allAssetsStartValue = ArrayList()
        allAssets = BehaviorSubject.createDefault(allAssetsStartValue)
        deltaAssetsForAll = BehaviorSubject.create()
        calcDeltaMapFromAll(statisticEntryLists[ALL_ASSETS]
                ?: emptyList<StatisticEntryDeprecated>())
        startDate = service.startDate
        endDate = service.endDate
        bankSubject.addAddListener({ bankAccount -> addBankAccount(bankAccount!!) })
        bankSubject.addDeleteListener({ bankAccount -> deleteBankAccount(bankAccount!!) })
        bankSubject.addUpdateListener({ _, newBankAccount -> updateBankAccount(newBankAccount!!) })
        yearsList = generateYearsList()
    }

    fun calcStatisticsFor(startDate: DateTime, endDate: DateTime): AssetsStatistics {
        val allAssets = entrysForAll
        val filtered = ArrayList<StatisticEntryDeprecated>()

        for (statisticEntry in allAssets) {
            val statisticEntryDate = statisticEntry.date
            if (!statisticEntryDate.isBefore(startDate) && !statisticEntryDate.isAfter(endDate)) {
                filtered.add(statisticEntry)
            }
        }
        var overallWin = 0
        var monthlyWin = 0
        if (filtered.size > 0) {
            val last = filtered[filtered.size - 1]
            val first = filtered[0]
            overallWin = last.getEntryFor(ALL_USER) - first.getEntryFor(ALL_USER)
            monthlyWin = overallWin / Months.monthsBetween(first.date, last.date).months
        }

        // add forecast to filtered if it is the actual year
        if (DateTime.now().year == startDate.year) {
            val forecast = entrysForForecast
            for (i in forecast.indices) {
                val entry = forecast[i]
                if (i == 0 && filtered.size > 0) {
                    val lastFiltered = filtered[filtered.size - 1]
                    check(entry.date == lastFiltered.date)
                    // first entry is overlapping, add to entry
                    lastFiltered.putEntry(FORECAST_USER, entry.getEntryFor(FORECAST_USER))
                } else {
                    // new entry
                    filtered.add(entry)
                }
            }
        }

        return AssetsStatistics(startDate, endDate, filtered.toList(), monthlyWin, overallWin)
    }

    private fun generateYearsList(): List<String> {
        val list = Lists.newArrayList(service.overallString, service.futureString)
        list.addAll(StatisticsUtils.yearsList(startDate, DateTime.now()))
        return list
    }

    fun getAllAssets(): Observable<List<StatisticEntryDeprecated>> {
        return allAssets
    }

    private fun addBankAccount(bankAccount: BankAccount) {
        updateStatisticsFor(bankAccount)

    }

    private fun updateStatisticsFor(bankAccount: BankAccount) {
        lock.lock()
        statisticEntryLists[bankAccount] = calculateStatisticsOfBankAccount(bankAccount, startDate, endDate, period)
        updateAllAssets()
        updateForecast()
        service.save(statisticEntryLists)
        lock.unlock()
    }

    private fun updateForecast() {
        if (entrysForAll.isEmpty()) return
        val all = entrysForAll
        val lastEntry = all[all.size - 1]
        val lastDate = lastEntry.date.plusYears(1).withMonthOfYear(1)
        val numberEntrys = Months.monthsBetween(lastEntry.date, lastDate).months + 1
        val forecast = ArrayList<StatisticEntryDeprecated>(numberEntrys)
        val sum = lastEntry.getEntryFor(ALL_USER)
        val entryMap = HashMap<User, Int>(1)
        entryMap[FORECAST_USER] = sum
        forecast.add(StatisticEntryDeprecated(lastEntry.date, entryMap))

        // get average win of last 12 month
        val overallWin = StatisticEntryDeprecated(lastEntry)
        overallWin.subtractEntry(all[Math.max(0, all.size - 12)])
        val averageWin = overallWin.getEntryFor(ALL_USER) / 12

        for (i in 1 until numberEntrys) {
            val prognose = sum + averageWin * i
            val map = HashMap<User, Int>(1)
            map[FORECAST_USER] = prognose
            forecast.add(StatisticEntryDeprecated(lastEntry.date.plus(Months.months(i)), map))
        }

        statisticEntryLists[YEAR_FORECAST] = forecast

        val years = 20
        val futureForecast = ArrayList<StatisticEntryDeprecated>(years)
        for (i in 0 until years) {
            val prognose = sum + overallWin.getEntryFor(ALL_USER) * i
            val map = HashMap<User, Int>(1)
            map[FORECAST_USER] = prognose
            futureForecast.add(StatisticEntryDeprecated(lastEntry.date.plus(Years.years(i)), map))
        }

        statisticEntryLists[FUTURE_FORECAST] = futureForecast

    }


    private fun deleteBankAccount(bankAccount: BankAccount) {
        lock.lock()
        statisticEntryLists.remove(bankAccount)
        updateAllAssets()
        updateForecast()
        service.save(statisticEntryLists)
        lock.unlock()
    }

    private fun updateAllAssets() {
        val allEntries = ArrayList<StatisticEntryDeprecated>()
        for (bankAccount in statisticEntryLists.keys) {
            if (bankAccount == ALL_ASSETS || bankAccount == YEAR_FORECAST || bankAccount == FUTURE_FORECAST)
                continue
            val entries = statisticEntryLists[bankAccount]
            for (i in entries!!.indices) {
                val entry = entries.get(i)
                if (allEntries.size <= i) {
                    // add new entry if no entry
                    allEntries.add(StatisticEntryDeprecated(entry))
                } else {
                    allEntries[i].addEntry(entry)
                }

                // calculate sum for ALL
                val entryMap = allEntries[i].entryMap
                var sum = 0
                for (user in entryMap.keys) {
                    if (user == ALL_USER) continue
                    sum += entryMap[user]!!
                }

                entryMap[ALL_USER] = sum
            }
        }

        statisticEntryLists[ALL_ASSETS] = allEntries
        logger.info("On Next")
        allAssets.onNext(allEntries)

        calcDeltaMapFromAll(allEntries)
    }

    private fun calcDeltaMapFromAll(allEntries: List<StatisticEntryDeprecated>) {
        // convert to year month delta map
        val deltaMap = mutableMapOf<YearMonth, MonthStatistic>()
        allEntries.forEachIndexed { index, entryDeprecated ->
            if (index + 1 < allEntries.size) {
                deltaMap[entryDeprecated.date.yearMonth] = allEntries[index + 1].toMonthStatistic() - entryDeprecated.toMonthStatistic()
            }
        }

        deltaAssetsForAll.onNext(deltaMap)
    }

    private fun updateBankAccount(newBankAccount: BankAccount) {
        updateStatisticsFor(newBankAccount)
    }

    fun getEntrysFor(bankAccount: BankAccount): List<StatisticEntryDeprecated>? {
        return statisticEntryLists[bankAccount]
    }

    companion object {

        private val ALL_ASSETS = BankAccount("ALL_ASSETS", "ALL_ASSETS", "ALL", ALL_USER, ArrayList())
        var FORECAST_USER = User("YEAR_FORECAST", "YEAR_FORECAST", "F", DateTime())
        var YEAR_FORECAST = BankAccount("YEAR_FORECAST", "YEAR_FORECAST", "YEAR_FORECAST", FORECAST_USER, ArrayList())
        var FUTURE_FORECAST = BankAccount("FUTURE_FORECAST", "FUTURE_FORECAST", "FUTURE_FORECAST", FORECAST_USER, ArrayList())

        fun calculateStatisticsOfBankAccount(bankAccount: BankAccount, start: DateTime, end: DateTime, period: Period): List<StatisticEntryDeprecated> {
            var startDate = start
            var endDate = end
            startDate = if (startDate.dayOfMonth().get() == 1) startDate else startDate.withDayOfMonth(1)
            endDate = if (endDate.dayOfMonth().get() == 1) endDate else endDate.plus(Months.ONE).withDayOfMonth(1)
            val entries = ArrayList<StatisticEntryDeprecated>(Months.monthsBetween(startDate, endDate).months + 1)
            // balances are sorted with newest at top
            val balances = bankAccount.balances
            var nextDate = startDate
            if (balances.size == 0) {
                while (!nextDate.isAfter(endDate)) {
                    entries.add(StatisticEntryDeprecated(nextDate))
                    nextDate = nextDate.plus(period)
                }

                return entries
            }

            // first balance is last (sorted)
            val firstBalance = balances[balances.size - 1]
            val firstDate = firstBalance.date
            // fill all entrys before first entry with value of first entry
            while (nextDate.isBefore(firstDate)) {
                val entry = StatisticEntryDeprecated(nextDate)
                entry.addEntry(StatisticEntryDeprecated(firstBalance, bankAccount.owners))
                entries.add(entry)
                nextDate = nextDate.plus(period)
            }

            var lastBalance = firstBalance
            for (i in balances.indices.reversed()) {
                val balance = balances[i]
                val date = balance.date
                if (nextDate == date) {
                    entries.add(StatisticEntryDeprecated(balance, bankAccount.owners))
                    nextDate = nextDate.plus(period)
                } else if (!nextDate.isAfter(date)) {
                    while (balance.date.isAfter(nextDate)) {
                        entries.add(StatisticEntryDeprecated(Balance(calcLinearEstimation(lastBalance, balance, nextDate), nextDate), bankAccount.owners))
                        nextDate = nextDate.plus(period)
                    }
                }

                lastBalance = balance
            }

            // add value of last balance until end
            while (!nextDate.isAfter(endDate)) {
                val statisticEntry = StatisticEntryDeprecated(nextDate)
                statisticEntry.addEntry(StatisticEntryDeprecated(lastBalance, bankAccount.owners))
                entries.add(statisticEntry)
                nextDate = nextDate.plus(period)
            }

            return entries
        }

        fun calcLinearEstimation(lastBalance: Balance, balance: Balance, nextDate: DateTime): Int {
            val days = Days.daysBetween(lastBalance.date, balance.date).days
            val daysUntil = Days.daysBetween(lastBalance.date, nextDate).days

            return lastBalance.balance + daysUntil * (balance.balance - lastBalance.balance) / days
        }
    }

    fun getStartValueFor(user: User): Int {
        return getEntrysFor(ALL_ASSETS)?.first()?.getEntryFor(user) ?: 0
    }


}
