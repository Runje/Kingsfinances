package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.features.category_statistics.CategoryCalculator
import blue.koenig.kingsfinances.features.category_statistics.CategoryCalculatorService
import blue.koenig.kingsfinances.features.expenses.CompensationCalculator
import blue.koenig.kingsfinances.model.calculation.MonthStatistic
import blue.koenig.kingsfinances.model.database.ExpensesDbRepository
import com.koenig.commonModel.finance.CostDistribution
import com.koenig.commonModel.finance.Costs
import com.koenig.commonModel.finance.Expenses
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.YearMonth
import org.joda.time.Years
import org.junit.Assert
import org.junit.Test

/**
 * Created by Thomas on 18.02.2018.
 */
class CompensationDatabaseTests : DatabaseTests() {
    @Test
    fun getCompensations() {
        val expenses1 = Expenses("Test1", "Category", "", 506, CostDistribution(), Helper.getDay(2017, 2, 28), "", true)
        val expenses2 = Expenses("Test2", "Category", "", 506, CostDistribution(), Helper.getDay(2017, 1, 31), "", false)
        val expenses3 = Expenses("Test3", "Category", "", 506, CostDistribution(), Helper.getDay(2017, 4, 30), "", true)
        financeDatabase.addExpenses(expenses1)
        financeDatabase.addExpenses(expenses2)
        financeDatabase.addExpenses(expenses3)

        val compensations = financeDatabase.expensesTable.compensations
        Assert.assertEquals(2, compensations.size)
        Assert.assertEquals(compensations[expenses1.date]!!.name, expenses1.name)
        Assert.assertEquals(compensations[expenses3.date]!!.name, expenses3.name)
    }

    @Test
    fun calcCompensations() {
        val categoryCalculator = CategoryCalculator(Period.months(1), financeDatabase.expensesTable,
                getCategoryCalcService())
        val jan = YearMonth(2015, 1)
        val feb = YearMonth(2015, 2)
        val allAssets = Observable.just(mapOf(jan to MonthStatistic(jan, mapOf(Helper.thomas to 20))))
        val calculator = CompensationCalculator(ExpensesDbRepository(financeDatabase.expensesTable, config.userIdObservable), categoryCalculator.deltaStatisticsForAll, allAssets, config)
        val expenses1 = Expenses("Test1", "Category", "", 5, CostDistribution(mutableMapOf(Helper.thomas to Costs(5, 5))), Helper.getDay(2015, 1, 28), "")
        financeDatabase.addExpenses(expenses1)
        calculator.calcCompensations()
        var compensations = financeDatabase.expensesTable.compensations
        val day = Helper.getDay(2015, 1, 31)
        Assert.assertEquals(15, compensations[day]!!.costDistribution[Helper.thomas].Theory)
        // everything else should be 0
        compensations.values.forEach {
            if (it.date != day) {
                Assert.assertEquals(0, it.costDistribution[Helper.thomas].Theory)
            }
        }

        // second run, nothing should change
        calculator.calcCompensations()
        compensations = financeDatabase.expensesTable.compensations
        Assert.assertEquals(0, compensations[day]!!.costDistribution[Helper.thomas].Theory)
        compensations.values.forEach {
            if (it.date != day) {
                Assert.assertEquals(0, it.costDistribution[Helper.thomas].Theory)
            }
        }
    }

    private fun getCategoryCalcService(): CategoryCalculatorService {
        return object : CategoryCalculatorService {
            override val absoluteCategoryMap: MutableMap<String, MutableMap<YearMonth, MonthStatistic>>
                get() = mutableMapOf()
            override val deltaCategoryMap: MutableMap<String, MutableMap<YearMonth, MonthStatistic>>
                get() = mutableMapOf()
            override val overallString: String
                get() = "All"
            override val startDate: DateTime
                get() = DateTime(2015, 1, 1, 0, 0)

            override fun saveStatistics(deltaCategoryMap: Map<String, Map<YearMonth, MonthStatistic>>, absoluteCategoryMap: MutableMap<String, MutableMap<YearMonth, MonthStatistic>>) {

            }

            override fun getGoalFor(category: String, month: YearMonth): Double {
                return 0.0
            }

            override fun getGoalFor(category: String, year: Years): Int {
                return 0
            }

            override val endDate = YearMonth(2017, 1)
        }
    }
}