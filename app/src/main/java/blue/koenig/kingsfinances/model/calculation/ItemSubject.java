package blue.koenig.kingsfinances.model.calculation;

import blue.koenig.kingsfinances.model.database.Table;

/**
 * Created by Thomas on 02.01.2018.
 */

public interface ItemSubject<T> {
    void addDeleteListener(Table.OnDeleteListener<T> listener);

    void addUpdateListener(Table.OnUpdateListener<T> listener);

    void addAddListener(Table.OnAddListener<T> listener);
}
