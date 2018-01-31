package blue.koenig.kingsfinances.features.standing_orders

import blue.koenig.kingsfinances.model.database.ExpensesTable
import blue.koenig.kingsfinances.model.database.StandingOrderTable
import com.koenig.commonModel.finance.StandingOrder

/**
 * Created by Thomas on 28.01.2018.
 */
class StandingOrderExecutor(private val standingOrderTable: StandingOrderTable, private val expensesTable: ExpensesTable) {
    public fun executeForAll() {
        val allItems = standingOrderTable.allItems
        allItems.forEach { order ->
            {
                executeOrdersFor(order)
            }
        }


    }

    private fun executeOrdersFor(order: StandingOrder) {
        order.executedExpenses
    }


}

