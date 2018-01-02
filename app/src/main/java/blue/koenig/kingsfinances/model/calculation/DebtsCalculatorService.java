package blue.koenig.kingsfinances.model.calculation;

import java.util.List;

/**
 * Created by Thomas on 28.12.2017.
 */

public interface DebtsCalculatorService {
    /**
     * Getting the saved debts, sorted in a manner that the first one is the oldest.
     *
     * @return sorted debts
     */
    List<Debts> getSavedSortedDebts();

    void saveDebts(List<Debts> debtsList);
}
