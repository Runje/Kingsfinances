package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.model.calculation.StatisticEntryDeprecated
import com.koenig.commonModel.finance.Expenses

/**
 * Created by Thomas on 02.01.2018.
 */

class TestExpensesSubject : TestSubject<Expenses>() {
    fun updateDebts(oldStatisticEntry: StatisticEntryDeprecated, newStatisticEntry: StatisticEntryDeprecated) {
        update(makeExpensesFromDebts(oldStatisticEntry), makeExpensesFromDebts(newStatisticEntry))
    }

    private fun makeExpensesFromDebts(statisticEntry: StatisticEntryDeprecated): Expenses {
        val costDistribution = TestHelper.makeCostDistribution(statisticEntry.getEntryFor(TestHelper.thomas), 0, 0, statisticEntry.getEntryFor(TestHelper.thomas))
        return Expenses("", "", "", costDistribution.sumReal(), costDistribution, statisticEntry.date, "")
    }
}
