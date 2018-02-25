package blue.koenig.kingsfinances.model.calculation;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;

/**
 * Created by Thomas on 28.12.2017.
 */

public class LinearStatisticsCalculator extends StatisticsCalculator {


    public LinearStatisticsCalculator(Period period, StatisticsCalculatorService service) {
        super(period, service);
    }

    /**
     * Calculates the next statistical relevant date(1. of next month).
     *
     * @param dateTime date of change
     * @return date of statistical relevance
     */
    public static DateTime calcEntryDate(DateTime dateTime) {
        return dateTime.plus(Period.months(1)).withDayOfMonth(1);
    }

    public static List<StatisticEntryDeprecated> updateStatistics(StatisticEntryDeprecated delta, Period period, List<StatisticEntryDeprecated> statisticEntryList) {
        DateTime date = delta.getDate();
        DateTime relevantDate = calcEntryDate(date);
        delta.setDate(relevantDate);

        if (statisticEntryList.size() == 0) {
            statisticEntryList.add(delta);
            return statisticEntryList;
        } else {
            // TODO: make hashmap to index date for faster search
            int i = 0;
            for (StatisticEntryDeprecated statisticEntry : statisticEntryList) {
                // add delta to current entry
                if (statisticEntry.getDate().equals(relevantDate)) {
                    statisticEntry.addEntry(delta);
                    return statisticEntryList;
                } else if (statisticEntry.getDate().isAfter(relevantDate)) {
                    // insert delta + lastEntry here
                    StatisticEntryDeprecated newEntry = new StatisticEntryDeprecated(delta);
                    if (i > 0) newEntry.addEntry(statisticEntryList.get(i - 1));
                    statisticEntryList.add(i, newEntry);
                    return statisticEntryList;
                }
                i++;
            }

            // entry was not added so far, so it must be at the end
            // last entry + delta
            delta.addEntry(statisticEntryList.get(statisticEntryList.size() - 1));
            statisticEntryList.add(delta);
            return statisticEntryList;
        }
    }

    @Override
    protected List<StatisticEntryDeprecated> calculateNewStatistics(StatisticEntryDeprecated delta, Period period, List<StatisticEntryDeprecated> statisticEntryList) {
        return updateStatistics(delta, period, statisticEntryList);
    }
}
