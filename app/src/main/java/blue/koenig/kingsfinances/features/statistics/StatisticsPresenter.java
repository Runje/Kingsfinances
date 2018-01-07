package blue.koenig.kingsfinances.features.statistics;


import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.Years;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.model.calculation.StatisticEntry;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by Thomas on 07.01.2018.
 */

public class StatisticsPresenter {
    protected Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());
    StatisticsView view;
    AssetsCalculator assetsCalculator;
    private StatisticsState state;
    private Disposable disposable;

    public StatisticsPresenter(AssetsCalculator assetsCalculator, DateTime startDate, Context context) {
        this.assetsCalculator = assetsCalculator;

        state = new StatisticsState(new ArrayList<>(), 0, 0, 0, generateYearsList(startDate, context));
    }

    private List<String> generateYearsList(DateTime startDate, Context context) {
        int size = Years.yearsBetween(startDate, DateTime.now()).getYears() + 2;
        ArrayList<String> list = new ArrayList<>(size);
        list.add(context.getResources().getString(R.string.overall));
        while (startDate.isBefore(DateTime.now())) {
            list.add(Integer.toString(startDate.getYear()));
            startDate = startDate.plus(Years.ONE);
        }

        return list;
    }

    public void attachView(StatisticsView view) {
        this.view = view;
        disposable = assetsCalculator.getAllAssets().observeOn(AndroidSchedulers.mainThread()).subscribe(
                assets -> showAssets(assets), throwable -> logger.error("OnError: " + throwable.toString())
        );
    }

    private void showAssets(List<StatisticEntry> assets) {
        logger.info("Show assets");
        changeStateTo(state.toBuilder().assets(assets).build());
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
        List<StatisticEntry> statisticEntries = assetsCalculator.getEntrysForAll();
        if (position != 0) {
            // not overall
            int year = Integer.parseInt(state.getYearsList().get(position));
            DateTime beforeDate = new DateTime(year, 1, 1, 0, 0);
            DateTime afterDate = beforeDate.plus(Years.ONE);
            List<StatisticEntry> filtered = new ArrayList<>();
            for (StatisticEntry statisticEntry : statisticEntries) {
                DateTime statisticEntryDate = statisticEntry.getDate();
                if (!statisticEntryDate.isBefore(beforeDate) && !statisticEntryDate.isAfter(afterDate)) {
                    filtered.add(statisticEntry);
                }
            }

            statisticEntries = filtered;
        }

        changeStateTo(state.toBuilder().assets(statisticEntries).build());
    }
}
