package blue.koenig.kingsfinances;

import com.koenig.commonModel.finance.Expenses;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blue.koenig.kingsfinances.features.category_statistics.CategoryCalculator;
import blue.koenig.kingsfinances.features.category_statistics.CategoryCalculatorService;
import blue.koenig.kingsfinances.features.category_statistics.CategoryStatistics;
import blue.koenig.kingsfinances.model.calculation.StatisticEntry;

import static blue.koenig.kingsfinances.TestHelper.getDay;
import static blue.koenig.kingsfinances.TestHelper.makeCostDistribution;
import static blue.koenig.kingsfinances.TestHelper.milena;
import static blue.koenig.kingsfinances.TestHelper.thomas;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class CategoryCalculatorTests {
    public static String category1 = "Category1";

    public Expenses makeExpenses(String category, int thomas, int milena, DateTime dateTime) {
        return new Expenses("", category, "", thomas + milena, makeCostDistribution(thomas, thomas, milena, milena), dateTime, "");
    }


    @Test
    public void categoryCalculation1() throws Exception {
        TestExpensesSubject expensesItemSubject = new TestExpensesSubject();
        CategoryCalculator calculator = new CategoryCalculator(Period.months(1), expensesItemSubject,
                getCategoryCalcService(new HashMap<String, List<StatisticEntry>>()));
        expensesItemSubject.add(makeExpenses(category1, 10, 20, getDay(2017, 1, 2)));
        List<StatisticEntry> statisticEntryList = calculator.getStatisticsFor(category1);
        Assert.assertEquals(2, statisticEntryList.size());

        StatisticEntry entry = statisticEntryList.get(0);
        Assert.assertEquals(getDay(2017, 1, 1), entry.getDate());
        Assert.assertEquals(0, entry.getEntryFor(thomas));
        Assert.assertEquals(0, entry.getEntryFor(milena));

        entry = statisticEntryList.get(1);
        Assert.assertEquals(getDay(2017, 2, 1), entry.getDate());
        Assert.assertEquals(10, entry.getEntryFor(thomas));
        Assert.assertEquals(20, entry.getEntryFor(milena));

        // calculate statistics for overall
        List<CategoryStatistics> statistics = calculator.getCategoryStatistics(getDay(2017, 1, 1), getDay(2017, 2, 1));
        Assert.assertEquals(1, statistics.size());
        CategoryStatistics categoryStatistics = statistics.get(0);
        Assert.assertEquals(category1, categoryStatistics.getName());
        Assert.assertEquals(30, categoryStatistics.getWinnings());

        // calculate statistics for year before
        statistics = calculator.getCategoryStatistics(getDay(2015, 1, 1), getDay(2016, 1, 1));
        Assert.assertEquals(1, statistics.size());
        categoryStatistics = statistics.get(0);
        Assert.assertEquals(category1, categoryStatistics.getName());
        Assert.assertEquals(0, categoryStatistics.getWinnings());
    }

    private CategoryCalculatorService getCategoryCalcService(HashMap<String, List<StatisticEntry>> map) {
        return new CategoryCalculatorService() {
            @Override
            public Map<String, List<StatisticEntry>> getCategoryMap() {
                return map;
            }

            @Override
            public void saveStatistics(Map<String, List<StatisticEntry>> categoryMap) {

            }

            @Override
            public String getOverallString() {
                return "ALL";
            }

            @Override
            public DateTime getStartDate() {
                return new DateTime(2015, 1, 1, 0, 0);
            }

            @Override
            public int getGoalFor(String category, DateTime startDate, DateTime endDate) {
                return 0;
            }
        };
    }

}