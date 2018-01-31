package blue.koenig.kingsfinances.model.calculation;

import android.content.Context;

import com.koenig.commonModel.Byteable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import blue.koenig.kingsfamilylibrary.model.FamilyConfig;

/**
 * Created by Thomas on 09.01.2018.
 */

public class FinanceStatisticsCalculatorService implements StatisticsCalculatorService {

    private final Context context;
    private final String key;

    public FinanceStatisticsCalculatorService(Context context, String key) {
        this.context = context;
        this.key = key;
    }

    @Override
    public List<StatisticEntry> getSavedSortedStatistics() {
        ByteBuffer buffer = FamilyConfig.getBytesFromConfig(context, key);
        if (buffer == null) return new ArrayList<>();

        int size = buffer.getInt();
        List<StatisticEntry> debts = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            debts.add(new StatisticEntry(buffer));
        }

        return debts;
    }

    @Override
    public void saveStatistics(List<StatisticEntry> statisticEntryList) {
        ByteBuffer buffer = ByteBuffer.allocate(Byteable.Companion.getBigListLength(statisticEntryList));
        Byteable.Companion.writeBigList(statisticEntryList, buffer);
        FamilyConfig.saveBytes(context, buffer.array(), key);
    }
}
