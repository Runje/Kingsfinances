package blue.koenig.kingsfinances.features.expenses

import blue.koenig.kingsfamilylibrary.model.communication.ServerConnection
import blue.koenig.kingsfinances.features.FamilyPresenter
import blue.koenig.kingsfinances.features.FamilyState
import blue.koenig.kingsfinances.features.FamilyView
import blue.koenig.kingsfinances.features.pending_operations.OperationExecutor
import blue.koenig.kingsfinances.model.calculation.DebtsCalculator
import com.koenig.FamilyConstants
import com.koenig.commonModel.ItemType
import com.koenig.commonModel.Repository.ExpensesRepository
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.Expenses
import com.koenig.commonModel.finance.FinanceConfig
import com.koenig.commonModel.finance.statistics.MonthStatistic
import com.koenig.communication.messages.AskForUpdatesMessage
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.YearMonth
import java.util.concurrent.TimeUnit

/**
 * Created by Thomas on 04.02.2018.
 */
class ExpensesPresenter(val expensesRepository: ExpensesRepository, familyMembers: List<User>, val connection: ServerConnection, val config: FinanceConfig, val debtsCalculator: DebtsCalculator, val operationExecutor: OperationExecutor) : FamilyPresenter<ExpensesState, ExpensesView>() {

    init {
        state = ExpensesState(familyMembers = familyMembers, user = config.user)

        // TODO: listen to changes of family members and userId!
        expensesRepository.hasChanged.observeOn(AndroidSchedulers.mainThread()).subscribe { update(state.copy(hasChanged = it)) }

        // udpates from server are reflected in the expensesRepository
    }


    override fun start() {
        // subscribe to events from view
        view?.let {
            disposables.add(it.onRefresh.subscribe {
                update(state.copy(isLoading = true))


                lateinit var items: List<Expenses>
                Observable.fromCallable {
                    //mache dies hier und sorge dafür, dass auch die Schulden gescheit angezeigt werden(oder zeige nur die aktuellen Schulden an)
                    // TODO: make server requestor which handles it, requestor.askForExpenses()
                    // TODO: make answerTo ID in msg header to check for answer!
                    connection.sendFamilyMessage(AskForUpdatesMessage.askForExpenses(config.getLastSyncDate(ItemType.EXPENSES.name)))
                    items = expensesRepository.allItems
                }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    update(state.copy(expenses = items, debts = debtsCalculator.absoluteMap, hasChanged = false))
                }

                // disable timer after timeout of 3s, TODO: if not connected immediately come back
                Observable.timer(3, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    update(state.copy(isLoading = false))
                }
            })

            disposables.add(it.onDelete.subscribe {
                update(state.copy(isLoading = true))
                lateinit var items: List<Expenses>
                Observable.fromCallable {
                    // make pending operation and delete expenses
                    operationExecutor.deleteItem(it, expensesRepository)
                    items = expensesRepository.allItems
                }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    update(state.copy(expenses = items, debts = debtsCalculator.absoluteMap, hasChanged = false, isLoading = false))
                }
            })
        }


        Observable.fromCallable { update(state.copy(isLoading = true)) }.subscribeOn(AndroidSchedulers.mainThread()).subscribe()

        lateinit var items: List<Expenses>
        lateinit var debts: Map<YearMonth, MonthStatistic>
        Observable.fromCallable {
            logger.info("Calling all items")
            items = expensesRepository.allItems
            debts = debtsCalculator.absoluteMap
        }.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe {
            update(state.copy(expenses = items, debts = debts, isLoading = false))
        }
    }

    override fun stop() {
    }
}

interface ExpensesView : FamilyView<ExpensesState> {
    val onRefresh: Observable<Any>
    val onDelete: Observable<Expenses>
}

data class ExpensesState(val expenses: List<Expenses> = emptyList(), val isLoading: Boolean = false, val debts: Map<YearMonth, MonthStatistic> = emptyMap(), val familyMembers: List<User> = emptyList(), val hasChanged: Boolean = false, val user: User = FamilyConstants.NO_USER) : FamilyState