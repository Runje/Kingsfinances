package blue.koenig.kingsfinances;

import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.Expenses;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blue.koenig.kingsfinances.features.statistics.StatisticsPresenter;
import blue.koenig.kingsfinances.model.calculation.IncomeCalculator;
import blue.koenig.kingsfinances.model.calculation.StatisticEntry;
import blue.koenig.kingsfinances.model.calculation.StatisticsCalculatorService;

import static blue.koenig.kingsfinances.TestHelper.getDay;

/**
 * Created by Thomas on 09.01.2018.
 */

public class IncomeCalculatorTests {
    public static void assertIncomeList(int index, DateTime dateTime, int thomas, int milena, List<StatisticEntry> statisticEntryList) {
        StatisticEntry debt = statisticEntryList.get(index);
        Assert.assertEquals(dateTime, debt.getDate());
        Assert.assertEquals(thomas, debt.getEntryFor(TestHelper.thomas));
        Assert.assertEquals(milena, debt.getEntryFor(TestHelper.milena));
    }

    @Test
    public void add() {
        TestSubject<Expenses> itemSubject = new TestSubject();
        StatisticsCalculatorService service = TestHelper.getStatisticsCalculatorService(new ArrayList<>());
        IncomeCalculator assetsCalculator = new IncomeCalculator(Period.months(1), itemSubject, service);
        itemSubject.add(createExpensesForThomas(getDay(2017, 1, 2), 200));
        itemSubject.add(createExpensesForThomas(getDay(2017, 1, 3), 300));
        itemSubject.add(createExpensesForThomas(getDay(2017, 1, 4), 400));

        List<StatisticEntry> entrys = assetsCalculator.getEntrys();
        Assert.assertEquals(2, entrys.size());

        assertIncomeList(0, getDay(2017, 1, 1), 0, 0, entrys);
        assertIncomeList(1, getDay(2017, 2, 1), 900, 0, entrys);
        float savingRate = StatisticsPresenter.Companion.calcSavingRate(getDay(2017, 1, 1), getDay(2018, 1, 1), 450, entrys);
        Assert.assertEquals(0.5f, savingRate);
    }

    // TODO: make tests for update deleted items through server update

    @Test
    public void addOneYear() {
        TestSubject<Expenses> itemSubject = new TestSubject();
        StatisticsCalculatorService service = TestHelper.getStatisticsCalculatorService(new ArrayList<>());
        IncomeCalculator assetsCalculator = new IncomeCalculator(Period.months(1), itemSubject, service);
        itemSubject.add(createExpensesForThomas(getDay(2017, 1, 2), 200));
        itemSubject.add(createExpensesForThomas(getDay(2017, 2, 3), 300));
        itemSubject.add(createExpensesForThomas(getDay(2017, 3, 4), 400));
        itemSubject.add(createExpensesForThomas(getDay(2017, 4, 4), 400));
        itemSubject.add(createExpensesForThomas(getDay(2017, 5, 4), 400));
        itemSubject.add(createExpensesForThomas(getDay(2017, 6, 4), 400));
        itemSubject.add(createExpensesForThomas(getDay(2017, 7, 4), 400));
        itemSubject.add(createExpensesForThomas(getDay(2017, 8, 4), 400));
        itemSubject.add(createExpensesForThomas(getDay(2017, 9, 4), 400));
        itemSubject.add(createExpensesForThomas(getDay(2017, 10, 4), 400));
        itemSubject.add(createExpensesForThomas(getDay(2017, 11, 4), 400));
        itemSubject.add(createExpensesForThomas(getDay(2017, 12, 4), 400));

        List<StatisticEntry> entrys = assetsCalculator.getEntrys();
        Assert.assertEquals(13, entrys.size());

        assertIncomeList(0, getDay(2017, 1, 1), 0, 0, entrys);
        assertIncomeList(1, getDay(2017, 2, 1), 200, 0, entrys);
        assertIncomeList(2, getDay(2017, 3, 1), 500, 0, entrys);
        assertIncomeList(3, getDay(2017, 4, 1), 900, 0, entrys);
        assertIncomeList(4, getDay(2017, 5, 1), 1300, 0, entrys);
        assertIncomeList(5, getDay(2017, 6, 1), 1700, 0, entrys);
        assertIncomeList(6, getDay(2017, 7, 1), 2100, 0, entrys);
        assertIncomeList(7, getDay(2017, 8, 1), 2500, 0, entrys);
        assertIncomeList(8, getDay(2017, 9, 1), 2900, 0, entrys);
        assertIncomeList(9, getDay(2017, 10, 1), 3300, 0, entrys);
        assertIncomeList(10, getDay(2017, 11, 1), 3700, 0, entrys);
        assertIncomeList(11, getDay(2017, 12, 1), 4100, 0, entrys);
        assertIncomeList(12, getDay(2018, 1, 1), 4500, 0, entrys);
        float savingRate = StatisticsPresenter.Companion.calcSavingRate(getDay(2017, 1, 1), getDay(2018, 1, 1), 450, entrys);
        Assert.assertEquals(0.1f, savingRate);
    }

    @Test
    public void updateDelete() {
        TestSubject<Expenses> itemSubject = new TestSubject();
        StatisticsCalculatorService service = TestHelper.getStatisticsCalculatorService(new ArrayList<>());
        IncomeCalculator assetsCalculator = new IncomeCalculator(Period.months(1), itemSubject, service);
        itemSubject.add(createExpensesForThomas(getDay(2016, 1, 2), 200));
        itemSubject.add(createExpensesForThomas(getDay(2017, 2, 3), 300));
        Expenses expensesForThomas = createExpensesForThomas(getDay(2017, 3, 4), 400);
        itemSubject.add(expensesForThomas);
        itemSubject.add(createExpensesForThomas(getDay(2017, 4, 4), 400));
        itemSubject.add(createExpensesForThomas(getDay(2017, 5, 4), 400));
        itemSubject.add(createExpensesForThomas(getDay(2017, 6, 4), 400));
        itemSubject.add(createExpensesForThomas(getDay(2017, 7, 4), 400));
        itemSubject.add(createExpensesForThomas(getDay(2017, 8, 4), 400));
        itemSubject.add(createExpensesForThomas(getDay(2017, 9, 4), 400));
        Expenses expensesForThomas1 = createExpensesForThomas(getDay(2017, 10, 4), 400);
        itemSubject.add(expensesForThomas1);
        itemSubject.add(createExpensesForThomas(getDay(2017, 11, 4), 400));
        itemSubject.add(createExpensesForThomas(getDay(2018, 12, 4), 400));


        itemSubject.update(expensesForThomas, createExpensesForThomas(getDay(2017, 3, 4), 200));
        itemSubject.delete(expensesForThomas1);
        List<StatisticEntry> entrys = assetsCalculator.getEntrys();
        /**Assert.assertEquals(13, entrys.size());

         assertIncomeList(0, getDay(2017, 1, 1), 0, 0, entrys);
         assertIncomeList(1, getDay(2017, 2, 1), 200, 0,entrys);
         assertIncomeList(2, getDay(2017, 3, 1), 500, 0,entrys);
         assertIncomeList(3, getDay(2017, 4, 1), 900, 0,entrys);
         assertIncomeList(4, getDay(2017, 5, 1), 1300, 0,entrys);
         assertIncomeList(5, getDay(2017, 6, 1), 1700, 0,entrys);
         assertIncomeList(6, getDay(2017, 7, 1), 2100, 0,entrys);
         assertIncomeList(7, getDay(2017, 8, 1), 2500, 0,entrys);
         assertIncomeList(8, getDay(2017, 9, 1), 2900, 0,entrys);
         assertIncomeList(9, getDay(2017, 10, 1), 3300, 0,entrys);
         assertIncomeList(10, getDay(2017, 11, 1), 3700, 0,entrys);
         assertIncomeList(11, getDay(2017, 12, 1), 4100, 0,entrys);
         assertIncomeList(12, getDay(2018, 1, 1), 4500, 0,entrys);**/
        float savingRate = StatisticsPresenter.Companion.calcSavingRate(getDay(2017, 1, 1), getDay(2018, 1, 1), 330, entrys);
        float savingRate2 = StatisticsPresenter.Companion.calcSavingRate(getDay(2016, 1, 1), getDay(2017, 1, 1), 200, entrys);
        float savingRate3 = StatisticsPresenter.Companion.calcSavingRate(getDay(2018, 1, 1), getDay(2019, 1, 1), 200, entrys);
        Assert.assertEquals(0.1f, savingRate);
        Assert.assertEquals(1f, savingRate2);
        Assert.assertEquals(0.5f, savingRate3);
    }

    @Test
    public void entrysMissing() {
        // wenn vorne welche fehlen gibt es ein fehler, auch wenn hinten welche fehlen, könnte es fehler geben
        List<StatisticEntry> entries = new ArrayList<>();
        entries.add(createStatisticsEntry(getDay(2017, 6, 1), 100, 100));
        entries.add(createStatisticsEntry(getDay(2017, 7, 1), 200, 200));

        float savingRate = StatisticsPresenter.Companion.calcSavingRate(getDay(2017, 1, 1), getDay(2018, 1, 1), 100, entries);
        Assert.assertEquals(0.5f, savingRate);
    }

    private StatisticEntry createStatisticsEntry(DateTime day, int thomas, int milena) {
        Map<User, Integer> map = new HashMap<>(2);
        map.put(TestHelper.thomas, thomas);
        map.put(TestHelper.milena, milena);
        return new StatisticEntry(day, map);
    }

    private Expenses createExpensesForThomas(DateTime day, int costs) {
        return new Expenses("", "", "", costs, TestHelper.makeCostDistribution(costs, costs, 0, 0), day, "");
    }
}
