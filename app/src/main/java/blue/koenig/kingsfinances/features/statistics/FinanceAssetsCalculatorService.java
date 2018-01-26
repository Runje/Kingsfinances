package blue.koenig.kingsfinances.features.statistics;

import android.content.Context;

import com.koenig.commonModel.Byteable;
import com.koenig.commonModel.finance.BankAccount;

import org.joda.time.DateTime;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blue.koenig.kingsfamilylibrary.model.FamilyConfig;
import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.model.calculation.StatisticEntry;

/**
 * Created by Thomas on 07.01.2018.
 */

public class FinanceAssetsCalculatorService implements AssetsCalculatorService {
    private static final String ASSETS = "ASSETS";

    Context context;
    private DateTime startDate;

    public FinanceAssetsCalculatorService(Context context, DateTime startDate) {
        this.context = context;
        this.startDate = startDate;
    }

    @Override
    public Map<BankAccount, List<StatisticEntry>> loadAllBankAccountStatistics() {
        ByteBuffer buffer = FamilyConfig.getBytesFromConfig(context, ASSETS);
        if (buffer == null) return new HashMap<>();

        int size = buffer.getInt();
        Map<BankAccount, List<StatisticEntry>> listMap = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            BankAccount bankAccount = new BankAccount(buffer);
            int entries = buffer.getInt();
            List<StatisticEntry> statistics = new ArrayList<>(entries);
            for (int j = 0; j < entries; j++) {
                statistics.add(new StatisticEntry(buffer));
            }

            listMap.put(bankAccount, statistics);
        }

        return listMap;
    }

    @Override
    public DateTime getStartDate() {
        // TODO: preferences or somewhere
        return startDate;
    }

    @Override
    public DateTime getEndDate() {
        return DateTime.now();
    }

    @Override
    public String getOverallString() {
        return context.getResources().getString(R.string.overall);
    }

    @Override
    public void save(Map<BankAccount, List<StatisticEntry>> statisticEntryLists) {
        int size = 4;
        for (BankAccount bankAccount : statisticEntryLists.keySet()) {
            size += bankAccount.getByteLength();
            size += Byteable.getBigListLength(statisticEntryLists.get(bankAccount));
        }

        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(statisticEntryLists.size());
        for (BankAccount bankAccount : statisticEntryLists.keySet()) {
            bankAccount.writeBytes(buffer);
            Byteable.writeBigList(statisticEntryLists.get(bankAccount), buffer);
        }

        FamilyConfig.saveBytes(context, buffer.array(), ASSETS);
    }

    @Override
    public String getFutureString() {
        return context.getString(R.string.future);
    }
}
