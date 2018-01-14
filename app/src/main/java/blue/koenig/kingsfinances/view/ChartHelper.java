package blue.koenig.kingsfinances.view;

import android.graphics.Color;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.koenig.commonModel.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import blue.koenig.kingsfinances.model.calculation.StatisticEntry;

/**
 * Created by Thomas on 05.01.2018.
 */

public class ChartHelper {

    public static List<String> entrysToMonthXValues(List<StatisticEntry> statisticEntryList) {
        ArrayList<String> xEntrys = new ArrayList<>(statisticEntryList.size());
        for (StatisticEntry debt : statisticEntryList) {
            String dateString = debt.getDate().toString("MM/yy");
            xEntrys.add(dateString);
        }

        return xEntrys;

    }

    public static LineData entrysToLineData(List<StatisticEntry> debts, String userId, int color) {
        // One debts not possible
        if (debts.size() <= 1) return new LineData();
        Map<User, Integer> debtsMap = debts.get(1).getEntryMap();
        List<Integer> colors = new ArrayList<>(debts.size());
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        for (User user : debtsMap.keySet()) {
            if (user.getId().equals(userId)) {
                ArrayList<Entry> userEntrys = new ArrayList<>(debts.size());
                int i = 0;
                for (StatisticEntry debt : debts) {
                    Integer entryForNullabe = debt.getEntryForNullabe(user);
                    if (entryForNullabe != null) {
                        int value = entryForNullabe;
                        userEntrys.add(new Entry(i, value / 100f));
                        if (value < 0) {
                            colors.add(Color.RED);
                        } else colors.add(Color.GREEN);
                    }

                    i++;
                }
                LineDataSet barDataSet = new LineDataSet(userEntrys, user.getName());
                //barDataSet.setColors(colors);
                barDataSet.setColor(color);
                //barDataSet.setBarBorderWidth(20);

                barDataSet.setValueTextColor(Color.WHITE);
                dataSets.add(barDataSet);
            }
        }

        LineData lineData = new LineData(dataSets);
        //lineData.setBarWidth(0.04f);

        return lineData;
    }

    public static LineData entrysToLineData(List<StatisticEntry> entries, int[] colors) {
        // One entries not possible
        if (entries.size() <= 1) return new LineData();
        Map<User, Integer> debtsMap = entries.get(1).getEntryMap();
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        int userCount = 0;
        for (User user : debtsMap.keySet()) {
            int i = 0;
            ArrayList<Entry> userEntrys = new ArrayList<>(entries.size());
            for (StatisticEntry debt : entries) {
                Integer entryForNullabe = debt.getEntryForNullabe(user);
                if (entryForNullabe != null) {
                    int value = entryForNullabe;
                    userEntrys.add(new Entry(i, value / 100f));
                }
                i++;
            }
            LineDataSet barDataSet = new LineDataSet(userEntrys, user.getName());
            barDataSet.setColor(colors[userCount % colors.length]);
            barDataSet.setValueTextColor(Color.WHITE);
            dataSets.add(barDataSet);
            userCount++;
        }

        LineData lineData = new LineData(dataSets);
        return lineData;
    }
}
