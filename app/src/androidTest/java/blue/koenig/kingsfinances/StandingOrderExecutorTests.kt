package blue.koenig.kingsfinances

import android.support.test.runner.AndroidJUnit4
import blue.koenig.kingsfinances.model.database.ExpensesDbRepository
import blue.koenig.kingsfinances.model.database.StandingOrderDbRepository
import com.koenig.commonModel.Frequency
import com.koenig.commonModel.finance.CostDistribution
import com.koenig.commonModel.finance.StandingOrder
import com.koenig.commonModel.finance.features.StandingOrderExecutor
import io.reactivex.subjects.BehaviorSubject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by Thomas on 03.02.2018.
 */
@RunWith(AndroidJUnit4::class)
class StandingOrderExecutorTests : DatabaseTests() {

    @Test
    fun executeOrder() {
        val standingOrderTable = financeDatabase.standingOrderTable
        val executor = StandingOrderExecutor(StandingOrderDbRepository(standingOrderTable, BehaviorSubject.createDefault("ID")), ExpensesDbRepository(financeDatabase.expensesTable, BehaviorSubject.createDefault("ID")))
        val order = StandingOrder("", "", "", 0, CostDistribution(), Helper.getDay(2015, 2, 2), Helper.getDay(2016, 2, 2), Frequency.Monthly, 1, mutableMapOf())
        standingOrderTable.addFrom(order, "USER")
        executor.executeForAll()
        Assert.assertTrue(executor.consistencyCheck())
    }
}