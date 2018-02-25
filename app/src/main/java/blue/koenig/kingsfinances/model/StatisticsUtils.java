package blue.koenig.kingsfinances.model;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.Years;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import blue.koenig.kingsfinances.model.calculation.StatisticEntryDeprecated;

/**
 * Created by Thomas on 20.01.2018.
 */

public class StatisticsUtils {
    private final static Logger logger = LoggerFactory.getLogger("StatisticsUtils");

    public static List<String> yearsList(DateTime startDate, DateTime endDate) {
        ArrayList<String> list = new ArrayList<>();
        DateTime nextDate = startDate;
        while (nextDate.isBefore(endDate)) {
            list.add(0, Integer.toString(nextDate.getYear()));
            nextDate = nextDate.plus(Years.ONE);
        }

        return list;
    }

    public static List<String> allMonthsList() {
        List<String> months = new ArrayList<>(12);
        DateTime dateTime = new DateTime(2017, 1, 1, 0, 0);
        Period period = Period.months(1);
        for (int i = 0; i < 12; i++) {
            months.add(dateTime.toString("MMM"));
            dateTime = dateTime.plus(period);
        }

        return months;
    }

    public static StatisticEntryDeprecated calcDifferenceInPeriod(DateTime startDate, DateTime endDate, List<StatisticEntryDeprecated> entrys) {
        assert startDate.getDayOfMonth() == 1;
        assert endDate.getDayOfMonth() == 1;
        StatisticEntryDeprecated allSavings = new StatisticEntryDeprecated(endDate);


        StatisticEntryDeprecated first = entrys.size() > 0 ? entrys.get(0) : null;

        if (first == null || first.getDate().withTimeAtStartOfDay().isAfter(endDate.withTimeAtStartOfDay())) {
            return allSavings;
        }

        StatisticEntryDeprecated last = entrys.size() > 0 ? entrys.get(entrys.size() - 1) : null;
        for (StatisticEntryDeprecated entry : entrys) {
            if (!entry.getDate().withTimeAtStartOfDay().isAfter(startDate.withTimeAtStartOfDay())) {
                // it is nearer at the start date
                first = entry;
            } else if (!entry.getDate().withTimeAtStartOfDay().isAfter(endDate.withTimeAtStartOfDay())) {
                // nearer at end date
                last = entry;
            }
        }

        if (first != null && last != null) {
            allSavings.addEntry(last);
            allSavings.subtractEntry(first);
        } else {
            logger.error("first or last is null: " + first + ", last: " + last);
        }

        return allSavings;
    }
}
