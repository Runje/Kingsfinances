package blue.koenig.kingsfinances;

import com.koenig.commonModel.finance.CostDistribution;
import com.koenig.commonModel.finance.Expenses;

import blue.koenig.kingsfinances.model.calculation.StatisticEntry;

import static blue.koenig.kingsfinances.TestHelper.thomas;

/**
 * Created by Thomas on 02.01.2018.
 */

public class TestExpensesSubject extends TestSubject<Expenses> {
    public void updateDebts(StatisticEntry oldStatisticEntry, StatisticEntry newStatisticEntry) {
        update(makeExpensesFromDebts(oldStatisticEntry), makeExpensesFromDebts(newStatisticEntry));
    }

    private Expenses makeExpensesFromDebts(StatisticEntry statisticEntry) {
        CostDistribution costDistribution = TestHelper.makeCostDistribution(statisticEntry.getEntryFor(thomas), 0, 0, statisticEntry.getEntryFor(thomas));
        return new Expenses("", "", "", costDistribution.sumReal(), costDistribution, statisticEntry.getDate(), "");
    }
}
