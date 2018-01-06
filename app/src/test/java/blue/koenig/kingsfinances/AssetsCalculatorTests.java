package blue.koenig.kingsfinances;

import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.Balance;
import com.koenig.commonModel.finance.BankAccount;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blue.koenig.kingsfinances.model.calculation.AssetsCalculator;
import blue.koenig.kingsfinances.model.calculation.AssetsCalculatorService;
import blue.koenig.kingsfinances.model.calculation.StatisticEntry;

import static blue.koenig.kingsfinances.TestHelper.getAssetsCalculatorService;
import static blue.koenig.kingsfinances.TestHelper.getDay;
import static blue.koenig.kingsfinances.TestHelper.milena;
import static blue.koenig.kingsfinances.TestHelper.thomas;

/**
 * Created by Thomas on 03.01.2018.
 */

public class AssetsCalculatorTests {

    @Test
    public void linearEstimation() {
        Assert.assertEquals(15, AssetsCalculator.calcLinearEstimation(new Balance(10, getDay(17, 1, 31)), new Balance(20, getDay(17, 2, 2)), getDay(17, 2, 1)));
    }

    @Test
    public void add() {
        TestSubject itemSubject = new TestSubject();
        AssetsCalculatorService assetsCalculatorService = TestHelper.getAssetsCalculatorService(new HashMap<>(), getDay(17, 1, 1), getDay(17, 5, 1));
        AssetsCalculator assetsCalculator = new AssetsCalculator(Period.months(1), itemSubject, assetsCalculatorService);
        BankAccount bankAccountThomas = createBankAccountThomas(getDay(17, 1, 2), new int[]{10, 20, 30, -50});
        itemSubject.add(bankAccountThomas);
        List<StatisticEntry> statisticEntryList = assetsCalculator.getEntrysFor(bankAccountThomas);

        Assert.assertEquals(5, statisticEntryList.size());

        assertAssetOfList(0, getDay(17, 1, 1), 10, 0, statisticEntryList);
        assertAssetOfList(1, getDay(17, 2, 1), 19, 0, statisticEntryList);
        assertAssetOfList(2, getDay(17, 3, 1), 29, 0, statisticEntryList);
        assertAssetOfList(3, getDay(17, 4, 1), -47, 0, statisticEntryList);
        assertAssetOfList(4, getDay(17, 5, 1), -50, 0, statisticEntryList);

        statisticEntryList = assetsCalculator.getEntrysForAll();

        Assert.assertEquals(5, statisticEntryList.size());

        assertAssetOfList(0, getDay(17, 1, 1), 10, 0, statisticEntryList);
        assertAssetOfList(1, getDay(17, 2, 1), 19, 0, statisticEntryList);
        assertAssetOfList(2, getDay(17, 3, 1), 29, 0, statisticEntryList);
        assertAssetOfList(3, getDay(17, 4, 1), -47, 0, statisticEntryList);
        assertAssetOfList(4, getDay(17, 5, 1), -50, 0, statisticEntryList);
    }

    @Test
    public void addAll() {
        TestSubject itemSubject = new TestSubject();
        AssetsCalculatorService assetsCalculatorService = TestHelper.getAssetsCalculatorService(new HashMap<>(), getDay(17, 1, 1), getDay(17, 6, 1));
        AssetsCalculator assetsCalculator = new AssetsCalculator(Period.months(1), itemSubject, assetsCalculatorService);
        BankAccount bankAccountThomas = createBankAccountThomas(getDay(17, 1, 1), new int[]{10, 20, 30, -50});
        BankAccount bankAccountMilena = createBankAccountMilena(getDay(17, 2, 1), new int[]{10, 20, 30, -50});
        BankAccount bankAccountBoth = createBankAccountBoth(getDay(17, 3, 1), new int[]{10, 20, 30, -50});
        itemSubject.add(bankAccountThomas);
        itemSubject.add(bankAccountMilena);
        itemSubject.add(bankAccountBoth);
        List<StatisticEntry> statisticEntryList = assetsCalculator.getEntrysForAll();

        Assert.assertEquals(6, statisticEntryList.size());

        assertAssetOfList(0, getDay(17, 1, 1), 15, 15, statisticEntryList);
        assertAssetOfList(1, getDay(17, 2, 1), 25, 15, statisticEntryList);
        assertAssetOfList(2, getDay(17, 3, 1), 35, 25, statisticEntryList);
        assertAssetOfList(3, getDay(17, 4, 1), -40, 40, statisticEntryList);
        assertAssetOfList(4, getDay(17, 5, 1), -35, -35, statisticEntryList);
        assertAssetOfList(5, getDay(17, 6, 1), -75, -75, statisticEntryList);
    }


    @Test
    public void delete() {
        TestSubject itemSubject = new TestSubject();
        BankAccount bankAccountThomas = createBankAccountThomas(getDay(17, 1, 2), new int[]{10, 20, 30, -50});
        DateTime start = getDay(17, 1, 1);
        DateTime end = getDay(17, 5, 1);
        List<StatisticEntry> statisticEntryList = AssetsCalculator.calculateStatisticsOfBankAccount(bankAccountThomas, start, end, Period.months(1));
        Map<BankAccount, List<StatisticEntry>> listMap = new HashMap<>(1);
        listMap.put(bankAccountThomas, statisticEntryList);
        AssetsCalculator assetsCalculator = new AssetsCalculator(Period.months(1), itemSubject,
                getAssetsCalculatorService(new HashMap<>(), start, end));
        itemSubject.delete(bankAccountThomas);
        statisticEntryList = assetsCalculator.getEntrysFor(bankAccountThomas);

        Assert.assertEquals(null, statisticEntryList);

        statisticEntryList = assetsCalculator.getEntrysForAll();
        Assert.assertEquals(0, statisticEntryList.size());

    }
/*
    @Test
    public void updateDate() {
        // edit date
        TestSubject itemSubject = new TestSubject();
        AssetsCalculator assetsCalculator = new AssetsCalculator(Period.months(1), itemSubject,
                getCalculatorService(makeAssetsListThomas(getDay(17, 1, 1), new int[]{0, 10, 0, -10, 60})));
        itemSubject.update(createBankAccountThomas(getDay(17,1,2), new int[] {           10,20,  30,-50}),
                           createBankAccountThomas(getDay(17,2,2), new int[] {              10,   20,30,-50}));
        List<StatisticEntry> statisticEntryList = assetsCalculator.getEntrys();

        Assert.assertEquals(6, statisticEntryList.size());

        assertAssetOfList(0, getDay(17, 1, 1), 0, 0, statisticEntryList);
        assertAssetOfList(1, getDay(17, 2, 1), 0, 0, statisticEntryList);
        assertAssetOfList(2, getDay(17, 3, 1), -10,0, statisticEntryList);
        assertAssetOfList(3, getDay(17, 4, 1), -20, 0,statisticEntryList);
        assertAssetOfList(4, getDay(17, 5, 1), 140, 0,statisticEntryList);
        assertAssetOfList(5, getDay(17, 6, 1), 90, 0,statisticEntryList);
        // edit date & value
        // add and delete
    }

    @Test
    public void updateBetween() {
        // add entry between
        TestSubject itemSubject = new TestSubject();
        List<StatisticEntry> oldEntryList = makeAssetsListThomas(getDay(17, 1, 1), new int[]{0, 10, 10, 10, 60});
        // remove entry from month 3 and 4
        oldEntryList.remove(2);
        oldEntryList.remove(2);
        AssetsCalculator assetsCalculator = new AssetsCalculator(Period.months(1), itemSubject,
                getCalculatorService(oldEntryList));
        BankAccount oldBankAccount = createBankAccountThomas(getDay(17, 1, 2), new int[]{0});
        itemSubject.update(oldBankAccount,
                createBankAccountThomas(getDay(17, 1, 2), new int[]{10, 20, 30, -50}));
        List<StatisticEntry> statisticEntryList = assetsCalculator.getEntrys();

        Assert.assertEquals(5, statisticEntryList.size());

        assertAssetOfList(0, getDay(17, 1, 1), 0, 0, statisticEntryList);
        assertAssetOfList(1, getDay(17, 2, 1), 20, 0, statisticEntryList);
        assertAssetOfList(2, getDay(17, 3, 1), 30, 0, statisticEntryList);
        assertAssetOfList(3, getDay(17, 4, 1), 40, 0, statisticEntryList);
        assertAssetOfList(4, getDay(17, 5, 1), 10, 0, statisticEntryList);
    }

    @Test
    public void updateBetween2() {
        // add entry between
        TestSubject itemSubject = new TestSubject();
        List<StatisticEntry> oldEntryList = makeAssetsListThomas(getDay(17, 1, 1), new int[]{0, 10, 10, 10, 60});
        // remove entry from month 3 and 4
        oldEntryList.remove(2);
        oldEntryList.remove(3);
        AssetsCalculator assetsCalculator = new AssetsCalculator(Period.months(1), itemSubject,
                getCalculatorService(oldEntryList));
        BankAccount oldBankAccount = createBankAccountThomas(getDay(17, 1, 2), new int[]{0});
        itemSubject.update(oldBankAccount,
                createBankAccountThomas(getDay(17, 1, 2), new int[]{10, 20, 30}));
        List<StatisticEntry> statisticEntryList = assetsCalculator.getEntrys();

        Assert.assertEquals(5, statisticEntryList.size());

        assertAssetOfList(0, getDay(17, 1, 1), 0, 0, statisticEntryList);
        assertAssetOfList(1, getDay(17, 2, 1), 20, 0, statisticEntryList);
        assertAssetOfList(2, getDay(17, 3, 1), 30, 0, statisticEntryList);
        assertAssetOfList(3, getDay(17, 4, 1), 40, 0, statisticEntryList);
        assertAssetOfList(4, getDay(17, 5, 1), 90, 0, statisticEntryList);
    }

    @Test
    public void changeOwners() {
    }
*/


    private void assertAssetOfList(int index, DateTime day, int assetsThomas, int assetsMilena, List<StatisticEntry> newAssets) {
        StatisticEntry entry = newAssets.get(index);
        Assert.assertEquals(day, entry.getDate());
        Assert.assertEquals(assetsThomas, entry.getEntryFor(thomas));
        Assert.assertEquals(assetsMilena, entry.getEntryFor(milena));
    }

    private List<StatisticEntry> makeAssetsListThomas(DateTime day, int[] ints) {
        Map<User, Integer> userMap = new HashMap<>(1);
        List<StatisticEntry> statisticEntryList = new ArrayList<>(ints.length);
        for (int i = 0; i < ints.length; i++) {
            userMap.put(thomas, ints[i]);
            statisticEntryList.add(new StatisticEntry(new DateTime(day), userMap));
            day = day.plus(Period.months(1));
        }

        return statisticEntryList;
    }

    private BankAccount createBankAccountThomas(DateTime day, int[] balances) {
        List<User> owners = new ArrayList<>(1);
        owners.add(thomas);
        return createBankAccount(day, balances, owners);
    }

    private BankAccount createBankAccountMilena(DateTime day, int[] balances) {
        List<User> owners = new ArrayList<>(1);
        owners.add(milena);
        return createBankAccount(day, balances, owners);
    }

    private BankAccount createBankAccountBoth(DateTime day, int[] balances) {
        List<User> owners = new ArrayList<>(1);
        owners.add(thomas);
        owners.add(milena);
        return createBankAccount(day, balances, owners);
    }

    private BankAccount createBankAccount(DateTime day, int[] balances, List<User> owners) {

        List<Balance> balanceList = new ArrayList<>(balances.length);
        DateTime dateTime = day;
        for (int balance : balances) {
            balanceList.add(new Balance(balance, new DateTime(dateTime)));
            dateTime = dateTime.plus(Period.months(1));
        }

        return new BankAccount("", "", owners, balanceList);
    }

}