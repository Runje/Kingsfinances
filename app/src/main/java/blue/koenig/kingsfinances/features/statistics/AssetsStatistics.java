package blue.koenig.kingsfinances.features.statistics;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import blue.koenig.kingsfinances.model.calculation.StatisticEntry;

/**
 * Created by Thomas on 08.01.2018.
 */

public class AssetsStatistics {

    private final DateTime startDate;
    private final DateTime endDate;
    private final List<StatisticEntry> assets;

    private final int monthlyWin;

    private final int allWin;

    public AssetsStatistics(DateTime startDate, DateTime endDate, List<StatisticEntry> assets, int monthlyWin, int allWin) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.assets = assets;
        this.monthlyWin = monthlyWin;
        this.allWin = allWin;
    }

    public AssetsStatistics() {
        this(new DateTime(0), DateTime.now(), new ArrayList<>(), 0, 0);
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public List<StatisticEntry> getAssets() {
        return assets;
    }

    public int getMonthlyWin() {
        return monthlyWin;
    }

    public int getOverallWin() {
        return allWin;
    }
}
