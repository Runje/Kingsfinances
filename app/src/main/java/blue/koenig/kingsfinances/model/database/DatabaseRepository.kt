package blue.koenig.kingsfinances.model.database

import com.koenig.commonModel.Goal
import com.koenig.commonModel.Item
import com.koenig.commonModel.Repository.*
import com.koenig.commonModel.database.DatabaseItem
import com.koenig.commonModel.finance.BankAccount
import com.koenig.commonModel.finance.Expenses
import com.koenig.commonModel.finance.StandingOrder
import io.reactivex.Observable
import org.joda.time.LocalDate
import org.joda.time.YearMonth

/**
 * Created by Thomas on 13.02.2018.
 */
abstract class DbRepository<T : Item>(protected val table: ItemTable<T>, userIdObservable: Observable<String>) : Repository<T> {
    private var userId: String = ""

    init {
        userIdObservable.subscribe { userId = it }
    }

    override val hasChanged: Observable<Boolean>
        get() = table.hasChanged

    override val allItems: List<T>
        get() = table.allItems

    override fun updateFromServer(items: List<DatabaseItem<T>>) {
        table.updateFromServer(items)
    }

    override fun delete(item: T) {
        table.deleteFrom(item.id, userId)
    }

    override fun add(item: T) {
        table.addFrom(item, userId)
    }

    override fun update(item: T) {
        table.updateFrom(item, userId)
    }

    override fun getFromId(id: String): T? {
        return table.getFromId(id)
    }
}

class ExpensesDbRepository(private val expensesTable: ExpensesTable, userIdObservable: Observable<String>) : DbRepository<Expenses>(expensesTable, userIdObservable), ExpensesRepository {
    override val compensations: Map<LocalDate, Expenses>
        get() = expensesTable.compensations
}

class StandingOrderDbRepository(val standingOrderTable: StandingOrderTable, userIdObservable: Observable<String>) : DbRepository<StandingOrder>(standingOrderTable, userIdObservable), StandingOrderRepository {
    // TODO: move to other file
    override fun addExpensesToStandingOrders(standingOrderId: String, expensesId: String, dateTime: LocalDate) {
        standingOrderTable.addExpensesToStandingOrders(standingOrderId, expensesId, dateTime)
    }

}

class BankAccountDbRepository(bankAccountTable: BankAccountTable, userIdObservable: Observable<String>) : DbRepository<BankAccount>(bankAccountTable, userIdObservable), BankAccountRepository
class GoalDbRepository(goalTable: GoalTable, userIdObservable: Observable<String>) : DbRepository<Goal>(goalTable, userIdObservable), GoalRepository {
    override fun getGoalFor(category: String, year: Int): Int {
        return table.getFromName(category)?.goals?.get(year) ?: 0
    }

    override fun getGoalFor(category: String, month: YearMonth): Double {
        //ask database for value of year
        val goal = table.getFromName(category) ?: return 0.0
        val wholeYear = goal.goals[month.year] ?: return 0.0
        return (wholeYear * 12.0)

    }
}