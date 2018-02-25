package blue.koenig.kingsfinances.model.calculation;

import java.util.List;

/**
 * Created by Thomas on 28.12.2017.
 */

public interface StatisticsCalculatorService {
    /**
     * Getting the saved debts, sorted in a manner that the first one is the oldest.
     *
     * @return sorted debts
     */
    List<StatisticEntryDeprecated> getSavedSortedStatistics();

    void saveStatistics(List<StatisticEntryDeprecated> statisticEntryList);
}
