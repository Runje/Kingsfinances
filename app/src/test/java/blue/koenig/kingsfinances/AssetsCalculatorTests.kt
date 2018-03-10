package blue.koenig.kingsfinances

import com.koenig.commonModel.User
import com.koenig.commonModel.finance.Balance
import com.koenig.commonModel.finance.BankAccount
import com.koenig.commonModel.finance.statistics.AssetsCalculator
import com.koenig.commonModel.finance.statistics.StatisticEntryDeprecated
import junit.framework.Assert
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.Period
import org.junit.Test
import java.util.*

/**
 * Created by Thomas on 03.01.2018.
 */

class AssetsCalculatorTests {

    @Test
    fun linearEstimation() {
        Assert.assertEquals(15, AssetsCalculator.calcLinearEstimation(Balance(10, LocalDate(17, 1, 31)), Balance(20, LocalDate(17, 2, 2)), LocalDate(17, 2, 1)))
    }

    @Test
    fun add() {
        val itemSubject = TestSubject<BankAccount>()
        val assetsCalculatorService = TestHelper.getAssetsCalculatorService(hashMapOf(), LocalDate(17, 1, 1), LocalDate(17, 5, 1))
        val assetsCalculator = AssetsCalculator(Period.months(1), itemSubject, assetsCalculatorService)
        val bankAccountThomas = createBankAccountThomas(LocalDate(17, 1, 2), intArrayOf(10, 20, 30, -50))
        itemSubject.add(bankAccountThomas)
        var statisticEntryList = assetsCalculator.getEntrysFor(bankAccountThomas)

        Assert.assertEquals(5, statisticEntryList!!.size)

        assertAssetOfList(0, LocalDate(17, 1, 1), 10, 0, statisticEntryList)
        assertAssetOfList(1, LocalDate(17, 2, 1), 19, 0, statisticEntryList)
        assertAssetOfList(2, LocalDate(17, 3, 1), 29, 0, statisticEntryList)
        assertAssetOfList(3, LocalDate(17, 4, 1), -47, 0, statisticEntryList)
        assertAssetOfList(4, LocalDate(17, 5, 1), -50, 0, statisticEntryList)

        statisticEntryList = assetsCalculator.entrysForAll

        Assert.assertEquals(5, statisticEntryList.size)

        assertAssetOfList(0, LocalDate(17, 1, 1), 10, 0, statisticEntryList)
        assertAssetOfList(1, LocalDate(17, 2, 1), 19, 0, statisticEntryList)
        assertAssetOfList(2, LocalDate(17, 3, 1), 29, 0, statisticEntryList)
        assertAssetOfList(3, LocalDate(17, 4, 1), -47, 0, statisticEntryList)
        assertAssetOfList(4, LocalDate(17, 5, 1), -50, 0, statisticEntryList)
    }

    @Test
    fun addAll() {
        val itemSubject = TestSubject<BankAccount>()
        val assetsCalculatorService = TestHelper.getAssetsCalculatorService(hashMapOf(), LocalDate(17, 1, 1), LocalDate(17, 6, 1))
        val assetsCalculator = AssetsCalculator(Period.months(1), itemSubject, assetsCalculatorService)
        val bankAccountThomas = createBankAccountThomas(LocalDate(17, 1, 1), intArrayOf(10, 20, 30, -50))
        val bankAccountMilena = createBankAccountMilena(LocalDate(17, 2, 1), intArrayOf(10, 20, 30, -50))
        val bankAccountBoth = createBankAccountBoth(LocalDate(17, 3, 1), intArrayOf(10, 20, 30, -50))
        itemSubject.add(bankAccountThomas)
        itemSubject.add(bankAccountMilena)
        itemSubject.add(bankAccountBoth)
        val statisticEntryList = assetsCalculator.entrysForAll

        Assert.assertEquals(6, statisticEntryList.size)

        assertAssetOfList(0, LocalDate(17, 1, 1), 15, 15, statisticEntryList)
        assertAssetOfList(1, LocalDate(17, 2, 1), 25, 15, statisticEntryList)
        assertAssetOfList(2, LocalDate(17, 3, 1), 35, 25, statisticEntryList)
        assertAssetOfList(3, LocalDate(17, 4, 1), -40, 40, statisticEntryList)
        assertAssetOfList(4, LocalDate(17, 5, 1), -35, -35, statisticEntryList)
        assertAssetOfList(5, LocalDate(17, 6, 1), -75, -75, statisticEntryList)
    }


    @Test
    fun delete() {
        val itemSubject = TestSubject<BankAccount>()
        val bankAccountThomas = createBankAccountThomas(LocalDate(17, 1, 2), intArrayOf(10, 20, 30, -50))
        val start = LocalDate(17, 1, 1)
        val end = LocalDate(17, 5, 1)
        var statisticEntryList: List<StatisticEntryDeprecated>? = AssetsCalculator.calculateStatisticsOfBankAccount(bankAccountThomas, start, end, Period.months(1))
        val listMap = HashMap<BankAccount, List<StatisticEntryDeprecated>>(1)
        listMap[bankAccountThomas] = statisticEntryList!!
        val assetsCalculator = AssetsCalculator(Period.months(1), itemSubject,
                TestHelper.getAssetsCalculatorService(hashMapOf(), start, end))
        itemSubject.delete(bankAccountThomas)
        statisticEntryList = assetsCalculator.getEntrysFor(bankAccountThomas)

        Assert.assertEquals(null, statisticEntryList)

        statisticEntryList = assetsCalculator.entrysForAll
        Assert.assertEquals(0, statisticEntryList.size)

    }
    /*
    @Test
    public void updateDate() {
        // edit day
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
        // edit day & value
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


    private fun assertAssetOfList(index: Int, day: DateTime, assetsThomas: Int, assetsMilena: Int, newAssets: List<StatisticEntryDeprecated>) {
        val entry = newAssets[index]
        Assert.assertEquals(day, entry.date)
        Assert.assertEquals(assetsThomas, entry.getEntryFor(TestHelper.thomas))
        Assert.assertEquals(assetsMilena, entry.getEntryFor(TestHelper.milena))
    }

    private fun makeAssetsListThomas(day: DateTime, ints: IntArray): List<StatisticEntryDeprecated> {
        var day = day
        val userMap = HashMap<User, Int>(1)
        val statisticEntryList = ArrayList<StatisticEntryDeprecated>(ints.size)
        for (i in ints.indices) {
            userMap[TestHelper.thomas] = ints[i]
            statisticEntryList.add(StatisticEntryDeprecated(DateTime(day), userMap))
            day = day.plus(Period.months(1))
        }

        return statisticEntryList
    }

    private fun createBankAccountThomas(day: DateTime, balances: IntArray): BankAccount {
        val owners = ArrayList<User>(1)
        owners.add(TestHelper.thomas)
        return createBankAccount(day, balances, owners)
    }

    private fun createBankAccountMilena(day: DateTime, balances: IntArray): BankAccount {
        val owners = ArrayList<User>(1)
        owners.add(TestHelper.milena)
        return createBankAccount(day, balances, owners)
    }

    private fun createBankAccountBoth(day: DateTime, balances: IntArray): BankAccount {
        val owners = ArrayList<User>(1)
        owners.add(TestHelper.thomas)
        owners.add(TestHelper.milena)
        return createBankAccount(day, balances, owners)
    }

    private fun createBankAccount(day: DateTime, balances: IntArray, owners: List<User>): BankAccount {

        val balanceList = ArrayList<Balance>(balances.size)
        var dateTime = day
        for (balance in balances) {
            balanceList.add(Balance(balance, DateTime(dateTime)))
            dateTime = dateTime.plus(Period.months(1))
        }

        return BankAccount("", "", owners.toMutableList(), balanceList)
    }

}