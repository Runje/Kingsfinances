package blue.koenig.kingsfinances.model.calculation;

import com.koenig.commonModel.finance.BankAccount;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

/**
 * Created by Thomas on 05.01.2018.
 */

public interface AssetsCalculatorService {
    Map<BankAccount, List<StatisticEntry>> getAllBankAccountStatistics();

    DateTime getStartDate();

    DateTime getEndDate();

    void save(Map<BankAccount, List<StatisticEntry>> statisticEntryLists);
}
