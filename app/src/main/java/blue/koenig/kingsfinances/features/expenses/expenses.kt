package blue.koenig.kingsfinances.features.expenses

import android.content.Context
import blue.koenig.kingsfamilylibrary.model.FamilyConfig
import blue.koenig.kingsfamilylibrary.model.communication.ServerConnection
import blue.koenig.kingsfinances.features.FamilyPresenter
import blue.koenig.kingsfinances.features.FamilyState
import blue.koenig.kingsfinances.features.FamilyView
import blue.koenig.kingsfinances.model.calculation.DebtsCalculator
import blue.koenig.kingsfinances.model.calculation.StatisticEntry
import blue.koenig.kingsfinances.model.database.ExpensesTable
import com.koenig.commonModel.ItemType
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.Expenses
import com.koenig.communication.messages.AskForUpdatesMessage
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by Thomas on 04.02.2018.
 */
class ExpensesPresenter(val expensesTable: ExpensesTable, familyMembers: List<User>, val connection: ServerConnection, val context: Context, val debtsCalculator: DebtsCalculator) : FamilyPresenter<ExpensesState, ExpensesView>() {

    init {
        state = ExpensesState(familyMembers = familyMembers)

        // TODO: listen to changes of family members!
        // TODO: listen to changes of expenses (Make expensesRepository)


    }

    private var disposable: Disposable? = null

    override fun start() {
        disposable = view?.onRefresh?.subscribe {
            update(state.copy(isLoading = true))

            Observable.fromCallable {
                // TODO: make server requestor which handles it, requestor.askForExpenses()
                connection.sendFamilyMessage(AskForUpdatesMessage.askForExpenses(FamilyConfig.getLastSyncDate(context, ItemType.EXPENSES.name)))
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                update(state.copy(expenses = expensesTable.allItems, debts = debtsCalculator.entrys))
            }

            // disable timer after timeout of 3s
            Observable.timer(3, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
                update(state.copy(isLoading = false))
            }
        }

        Observable.fromCallable { update(state.copy(isLoading = true)) }.subscribeOn(AndroidSchedulers.mainThread()).subscribe()

        lateinit var items: List<Expenses>
        lateinit var debts: List<StatisticEntry>
        Observable.fromCallable {
            items = expensesTable.allItems
            debts = debtsCalculator.entrys
        }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe {
            update(state.copy(expenses = items, debts = debts, isLoading = false))
        }
    }

    override fun stop() {
        disposable?.dispose()
    }
}

interface ExpensesView : FamilyView<ExpensesState> {
    var onRefresh: Observable<Any>
}

data class ExpensesState(val expenses: List<Expenses> = emptyList(), val isLoading: Boolean = false, val debts: List<StatisticEntry> = emptyList(), val familyMembers: List<User> = emptyList()) : FamilyState