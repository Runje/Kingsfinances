package blue.koenig.kingsfinances.features.standing_orders

import blue.koenig.kingsfamilylibrary.model.FamilyConfig
import blue.koenig.kingsfinances.model.database.ExpensesTable
import blue.koenig.kingsfinances.model.database.StandingOrderTable
import com.koenig.commonModel.Frequency
import com.koenig.commonModel.finance.Expenses
import com.koenig.commonModel.finance.StandingOrder
import com.koenig.commonModel.finance.calcUuidFrom
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

/**
 * Created by Thomas on 28.01.2018.
 */
class StandingOrderExecutor(private val standingOrderTable: StandingOrderTable, private val expensesTable: ExpensesTable) {
    private val logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    public fun executeForAll() {
        val allItems = standingOrderTable.allItems
        allItems.forEach { order ->
                executeOrdersFor(order)
        }
    }

    private fun executeOrdersFor(order: StandingOrder) {
        val until = if (DateTime.now().isBefore(order.endDate)) DateTime.now() else order.endDate
        val dates = order.getExecutionDatesUntil(until)
        dates.forEach {
            if (order.executedExpenses[it] == null) {
                logger.info("Executing $order at $it")
                executeOrder(order, it)
            }
        }
    }

    private fun executeOrder(order: StandingOrder, date: DateTime) {
        val expenses = Expenses(order.name, order.category, order.subCategory, order.costs, order.costDistribution, date, order.id)
        // calculate id from last standing order or standing order if it is the first
        expenses.id = calcUuidFrom(order.lastExecutedExpenses ?: order.id)
        expensesTable.addFrom(expenses, FamilyConfig.getUserId())
        order.executedExpenses[date] = expenses.id
        standingOrderTable.addExpensesToStandingOrders(order.id, expenses.id, date)
    }

    fun consistencyCheck(): Boolean {
        logger.info("Starting consistency check...")

        // check all expenses
        expensesTable.allItems.forEach {
            if (!it.standingOrder.isBlank()) {
                val order = standingOrderTable.getFromId(it.standingOrder)
                // check if standingorder exists from this expenses
                if (order?.executedExpenses?.containsValue(it.id) == false) {
                    logger.error("$order has not $it as executedExpenses!")
                    return false
                }
                // Check if the dates are equal
                else if (order?.executedExpenses?.get(it.date) == null) {
                    logger.warn("$order has not same date as $it!")
                }
            }
        }

        // check all standing orders
        standingOrderTable.allItems.forEach {
            // check due dates
            it.getExecutionDatesUntil(DateTime.now()).forEach { date ->
                if (!it.executedExpenses.containsKey(date)) {
                    logger.error("$date not in $it")
                    return false
                }
            }
            // check expenses
            it.executedExpenses.forEach { (date, id) ->
                val expenses = expensesTable.getFromId(id)
                if (!expenses?.standingOrder.equals(it.id)) {
                    logger.error("$expenses has wrong id from $it")
                    return false
                }
                // check if the dates are equal
                else if (expenses?.date?.equals(date) == false) {
                    logger.warn("$expenses has not same date as $it!")
                }
            }
        }


        logger.info("Consistency check finished...everything is ok")
        return true
    }
}


fun StandingOrder.getExecutionDatesUntil(until: DateTime): List<DateTime> {
    val times = arrayListOf<DateTime>()

    // set hour to 12 to avoid switching days because of different timezones(summer time)
    val oldHour = firstDate.hourOfDay
    val firstDate = firstDate.withHourOfDay(12)
    if (firstDate.isBefore(until.withHourOfDay(13))) {
        times.add(firstDate.withHourOfDay(oldHour))
    }

    var i = 1
    while (true) {
        if (frequencyFactor == 0) {
            break
        }

        var nextDate: DateTime = when (frequency) {
            Frequency.Daily -> firstDate.plusDays(i * frequencyFactor)
            Frequency.Weekly -> firstDate.plusWeeks(i * frequencyFactor)
            Frequency.Monthly -> firstDate.plusMonths(i * frequencyFactor)
            Frequency.Yearly -> firstDate.plusYears(i * frequencyFactor)
        }

        if (nextDate.isBefore(until.withHourOfDay(13)) && nextDate.isBefore(endDate.withHourOfDay(13))) {
            times.add(nextDate.withHourOfDay(oldHour))
        } else {
            break
        }

        i++
    }

    return times
}

