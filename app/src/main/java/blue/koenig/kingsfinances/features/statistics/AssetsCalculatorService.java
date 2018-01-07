package blue.koenig.kingsfinances.features.statistics;

import com.koenig.commonModel.finance.BankAccount;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

import blue.koenig.kingsfinances.model.calculation.StatisticEntry;

/**
 * Created by Thomas on 05.01.2018.
 */

public interface AssetsCalculatorService {
    Map<BankAccount, List<StatisticEntry>> loadAllBankAccountStatistics();

    DateTime getStartDate();

    DateTime getEndDate();

    void save(Map<BankAccount, List<StatisticEntry>> statisticEntryLists);
}
