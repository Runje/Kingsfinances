package blue.koenig.kingsfinances.features.statistics;


import com.koenig.FamilyConstants;

import org.joda.time.DateTime;
import org.joda.time.Years;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import blue.koenig.kingsfinances.model.calculation.IncomeCalculator;
import blue.koenig.kingsfinances.model.calculation.StatisticEntry;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static blue.koenig.kingsfinances.model.StatisticsUtils.calcDifferenceInPeriod;

/**
 * Created by Thomas on 07.01.2018.
 */

public class StatisticsPresenter {
    protected static Logger logger = LoggerFactory.getLogger("StatisticsPresenter");
    StatisticsView view;

    AssetsCalculator assetsCalculator;
    IncomeCalculator incomeCalculator;
    private StatisticsState state;
    private Disposable disposable;

    public StatisticsPresenter(AssetsCalculator assetsCalculator, IncomeCalculator incomeCalculator) {
        this.assetsCalculator = assetsCalculator;
        this.incomeCalculator = incomeCalculator;
        state = new StatisticsState(new AssetsStatistics(), 0, assetsCalculator.getYearsList(), 0);
    }


    public static float calcSavingRate(DateTime startDate, DateTime endDate, int overallWin, List<StatisticEntry> incomes) {

        StatisticEntry allSavings = calcDifferenceInPeriod(startDate, endDate, incomes);
        if (allSavings.getSum() == 0) {
            return 0;
        }


        return overallWin / (float) allSavings.getSum();
    }

    public void attachView(StatisticsView view) {
        this.view = view;
        disposable = assetsCalculator.getAllAssets().observeOn(AndroidSchedulers.mainThread()).subscribe(
                assets -> clickYear(state.getPosition()), throwable -> logger.error("OnError: " + throwable.toString())
        );
    }

    private void changeStateTo(StatisticsState newState) {
        state = newState;
        if (view != null) view.render(state);
    }

    public void detachView() {
        disposable.dispose();
        view = null;
    }

    public void clickYear(int position) {
        DateTime beforeDate = new DateTime(0);
        DateTime afterDate = FamilyConstants.UNLIMITED;
        List<StatisticEntry> entrysForFutureForecast = null;
        if (position == 1) {
            entrysForFutureForecast = assetsCalculator.getEntrysForFutureForecast();
        } else if (position != 0) {
            // not overall
            int year = Integer.parseInt(state.getYearsList().get(position));
            beforeDate = new DateTime(year, 1, 1, 0, 0);
            afterDate = beforeDate.plus(Years.ONE);
        }

        AssetsStatistics statistics = assetsCalculator.calcStatisticsFor(beforeDate, afterDate);
        float savingRate = calcSavingRate(statistics.getStartDate(), statistics.getEndDate(), statistics.getOverallWin(), incomeCalculator.getEntrys());
        AssetsStatistics statisticsToShow = entrysForFutureForecast != null ? new AssetsStatistics(statistics.getStartDate(), statistics.getEndDate(), entrysForFutureForecast, statistics.getMonthlyWin(), statistics.getOverallWin()) : statistics;
        changeStateTo(state.toBuilder().statistics(statisticsToShow).savingRate(savingRate).build());
    }
}
