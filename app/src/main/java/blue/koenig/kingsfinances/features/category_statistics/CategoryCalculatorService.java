package blue.koenig.kingsfinances.features.category_statistics;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

import blue.koenig.kingsfinances.model.calculation.StatisticEntry;

/**
 * Created by Thomas on 19.01.2018.
 */

public interface CategoryCalculatorService {
    Map<String, List<StatisticEntry>> getCategoryMap();

    void saveStatistics(Map<String, List<StatisticEntry>> categoryMap);

    String getOverallString();

    DateTime getStartDate();

    int getGoalFor(String category, DateTime startDate, DateTime endDate);
}
