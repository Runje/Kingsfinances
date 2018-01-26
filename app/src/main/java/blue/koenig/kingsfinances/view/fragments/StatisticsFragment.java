package blue.koenig.kingsfinances.view.fragments;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.LineData;
import com.koenig.StringFormats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.inject.Inject;

import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.dagger.FinanceApplication;
import blue.koenig.kingsfinances.features.statistics.StatisticsPresenter;
import blue.koenig.kingsfinances.features.statistics.StatisticsState;
import blue.koenig.kingsfinances.features.statistics.StatisticsView;
import blue.koenig.kingsfinances.model.calculation.StatisticEntry;
import blue.koenig.kingsfinances.view.ChartHelper;
import blue.koenig.kingsfinances.view.lists.ListAdapter;

import static blue.koenig.kingsfinances.view.ChartHelper.entrysToMonthXValues;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class StatisticsFragment extends Fragment implements StatisticsView {
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Inject
    StatisticsPresenter presenter;
    private LineChart lineChart;
    private ListView list;
    private ListAdapter<String> adapter;
    private TextView savingRate;
    private TextView monthly;
    private TextView overall;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        logger.info("Attaching statistics fragment");
        ((FinanceApplication) getActivity().getApplication()).getFinanceAppComponent().inject(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        logger.info("Creating view statistics fragment");
        init(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        logger.info("Resume statistics fragment");
        presenter.attachView(this);
    }

    protected void init(View view) {
        savingRate = view.findViewById(R.id.text_saving_rate);
        overall = view.findViewById(R.id.text_overall);
        monthly = view.findViewById(R.id.text_monthly);
        lineChart = view.findViewById(R.id.linechart);
        lineChart.getAxisRight().setTextColor(Color.WHITE);
        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getXAxis().setTextColor(Color.WHITE);
        Legend legend = lineChart.getLegend();
        legend.setTextColor(Color.WHITE);
        lineChart.setGridBackgroundColor(Color.BLACK);
        lineChart.setVisibleXRangeMaximum(12);

        list = view.findViewById(R.id.yearList);
        adapter = new ListAdapter<String>() {
            @Override
            protected void updateView(View convertView, String item, int pos) {
                ((TextView) (convertView.findViewById(android.R.id.text1))).setText(item);
            }

            @Override
            protected int getItemLayout() {
                return android.R.layout.simple_list_item_1;
            }
        };
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view1, position, id) -> {
            presenter.clickYear(position);
        });
    }

    private void updateLinechart(List<StatisticEntry> statisticEntryList) {
        LineData lineData = ChartHelper.entrysToLineData(statisticEntryList, new int[]{Color.BLUE, Color.RED, Color.GREEN, Color.GRAY});
        lineChart.setData(lineData);

        List<String> xValues = entrysToMonthXValues(statisticEntryList);
        //convert x values to date string
        lineChart.getXAxis().setValueFormatter((value, axis) -> {
            int intValue = (int) value;
            if (intValue > xValues.size() - 1) {
                logger.error("intvalue: " + intValue + ", xValues.size(): " + xValues.size());
                return Integer.toString(intValue);
            }

            return xValues.get(intValue);
        });

        // show last 12 month
        //lineChart.setVisibleXRangeMaximum(12);
        //lineChart.moveViewToX(Math.max(0, statisticEntryList.size() - 12));
        lineChart.invalidate();
    }

    @Override
    public void onStop() {
        logger.info("Stop statistics fragment");
        presenter.detachView();
        super.onStop();
    }


    @Override
    public void render(StatisticsState state) {
        updateLinechart(state.getStatistics().getAssets());
        adapter.update(state.getYearsList());

        monthly.setText(StringFormats.centsToEuroString(state.getStatistics().getMonthlyWin()) + " €");
        overall.setText(StringFormats.centsToEuroString(state.getStatistics().getOverallWin()) + " €");
        savingRate.setText(StringFormats.floatToPercentString(state.getSavingRate()) + " %");
    }
}
