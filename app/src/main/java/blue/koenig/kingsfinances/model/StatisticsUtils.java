package blue.koenig.kingsfinances.model;


import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 20.01.2018.
 */

public class StatisticsUtils {
    private final static Logger logger = LoggerFactory.getLogger("StatisticsUtils");



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


}
