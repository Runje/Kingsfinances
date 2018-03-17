package blue.koenig.kingsfinances

import com.koenig.commonModel.Repository.AssetsRepository
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.Balance
import com.koenig.commonModel.finance.BankAccount
import com.koenig.commonModel.finance.statistics.AssetsCalculator
import com.koenig.commonModel.finance.statistics.MonthStatistic
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import junit.framework.Assert
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.YearMonth
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Created by Thomas on 03.01.2018.
 */

class AssetsCalculatorTests {

    val itemSubject = TestSubject<BankAccount>()
    var startMonth = YearMonth(2015, 1)
    var endMonth = YearMonth(2016, 12)

    val startMonthObservable
        get() = Observable.just(startMonth)
    val endMonthObservable
        get() = Observable.just(endMonth)
    @Mock
    lateinit var assetsRepository: AssetsRepository

    fun initAssetCalculator() {
        assetsCalculator = AssetsCalculator(itemSubject, startMonthObservable, endMonthObservable, assetsRepository)
    }

    lateinit var assetsCalculator: AssetsCalculator

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }
    @Test
    fun linearEstimation() {
        Assert.assertEquals(15, AssetsCalculator.calcLinearEstimation(Balance(10, LocalDate(17, 1, 31)), Balance(20, LocalDate(17, 2, 2)), LocalDate(17, 2, 1)))
    }

    @Test
    fun add() {
        // given
        initTest(startMonth = YearMonth(17, 1), endMonth = YearMonth(17, 5))

        // when adding new bank account
        val bankAccountThomas = createBankAccountThomas(LocalDate(17, 1, 2), intArrayOf(10, 20, 30, -50))
        itemSubject.add(bankAccountThomas)

        // then
        var statisticEntryList = assetsCalculator.getEntrysFor(bankAccountThomas)!!

        Assert.assertEquals(5, statisticEntryList.size)

        assertAssetOfList(YearMonth(17, 1), 10, 0, statisticEntryList)
        assertAssetOfList(YearMonth(17, 2), 19, 0, statisticEntryList)
        assertAssetOfList(YearMonth(17, 3), 29, 0, statisticEntryList)
        assertAssetOfList(YearMonth(17, 4), -47, 0, statisticEntryList)
        assertAssetOfList(YearMonth(17, 5), -50, 0, statisticEntryList)

        statisticEntryList = assetsCalculator.entrysForAll

        Assert.assertEquals(5, statisticEntryList.size)

        assertAssetOfList(YearMonth(17, 1), 10, 0, statisticEntryList)
        assertAssetOfList(YearMonth(17, 2), 19, 0, statisticEntryList)
        assertAssetOfList(YearMonth(17, 3), 29, 0, statisticEntryList)
        assertAssetOfList(YearMonth(17, 4), -47, 0, statisticEntryList)
        assertAssetOfList(YearMonth(17, 5), -50, 0, statisticEntryList)
    }

    private fun initTest(startMonth: YearMonth, endMonth: YearMonth, assets: MutableMap<BankAccount, MutableMap<YearMonth, MonthStatistic>> = mutableMapOf<BankAccount, MutableMap<YearMonth, MonthStatistic>>()) {
        this.startMonth = startMonth
        this.endMonth = endMonth
        whenever(assetsRepository.load()).thenReturn(assets)
        initAssetCalculator()
    }


    @Test
    fun addAll() {


        // given
        initTest(startMonth = YearMonth(17, 1), endMonth = YearMonth(17, 6))

        // when adding multiple bankAccounts
        val bankAccountThomas = createBankAccountThomas(LocalDate(17, 1, 1), intArrayOf(10, 20, 30, -50))
        val bankAccountMilena = createBankAccountMilena(LocalDate(17, 2, 1), intArrayOf(10, 20, 30, -50))
        val bankAccountBoth = createBankAccountBoth(LocalDate(17, 3, 1), intArrayOf(10, 20, 30, -50))
        itemSubject.add(bankAccountThomas)
        itemSubject.add(bankAccountMilena)
        itemSubject.add(bankAccountBoth)
        val statisticEntryList = assetsCalculator.entrysForAll

        // then
        Assert.assertEquals(6, statisticEntryList.size)

        assertAssetOfList(YearMonth(17, 1), 15, 15, statisticEntryList)
        assertAssetOfList(YearMonth(17, 2), 25, 15, statisticEntryList)
        assertAssetOfList(YearMonth(17, 3), 35, 25, statisticEntryList)
        assertAssetOfList(YearMonth(17, 4), -40, 40, statisticEntryList)
        assertAssetOfList(YearMonth(17, 5), -35, -35, statisticEntryList)
        assertAssetOfList(YearMonth(17, 6), -75, -75, statisticEntryList)
    }

    @Test
    fun delete() {
        // given
        val start = YearMonth(17, 1)
        val end = YearMonth(17, 5)
        val bankAccountThomas = createBankAccountThomas(LocalDate(17, 1, 2), intArrayOf(10, 20, 30, -50))
        initTest(startMonth = start, endMonth = end)
        itemSubject.add(bankAccountThomas)

        var statisticEntryList = assetsCalculator.getEntrysFor(bankAccountThomas)
        Assert.assertNotNull(statisticEntryList)

        // when delete bank account
        itemSubject.delete(bankAccountThomas)
        statisticEntryList = assetsCalculator.getEntrysFor(bankAccountThomas)

        Assert.assertEquals(null, statisticEntryList)

        statisticEntryList = assetsCalculator.entrysForAll
        Assert.assertEquals(0, statisticEntryList.size)

    }


    private fun assertAssetOfList(month: YearMonth, assetsThomas: Int, assetsMilena: Int, newAssets: Map<YearMonth, MonthStatistic>) {
        val entry = newAssets[month]!!
        Assert.assertEquals(month, entry.month)
        Assert.assertEquals(assetsThomas, entry[TestHelper.thomas])
        Assert.assertEquals(assetsMilena, entry[TestHelper.milena])
    }


    private fun createBankAccountThomas(day: LocalDate, balances: IntArray): BankAccount {
        val owners = ArrayList<User>(1)
        owners.add(TestHelper.thomas)
        return createBankAccount(day, balances, owners)
    }

    private fun createBankAccountMilena(day: LocalDate, balances: IntArray): BankAccount {
        val owners = ArrayList<User>(1)
        owners.add(TestHelper.milena)
        return createBankAccount(day, balances, owners)
    }

    private fun createBankAccountBoth(day: LocalDate, balances: IntArray): BankAccount {
        val owners = ArrayList<User>(1)
        owners.add(TestHelper.thomas)
        owners.add(TestHelper.milena)
        return createBankAccount(day, balances, owners)
    }

    private fun createBankAccount(day: LocalDate, balances: IntArray, owners: List<User>): BankAccount {

        val balanceList = ArrayList<Balance>(balances.size)
        var dateTime = day
        for (balance in balances) {
            balanceList.add(Balance(balance, LocalDate(dateTime)))
            dateTime = dateTime.plus(Period.months(1))
        }

        return BankAccount("", "", owners.toMutableList(), balanceList)
    }

}