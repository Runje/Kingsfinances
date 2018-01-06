package blue.koenig.kingsfinances;

import java.util.ArrayList;
import java.util.List;

import blue.koenig.kingsfinances.model.calculation.ItemSubject;
import blue.koenig.kingsfinances.model.database.Table;

/**
 * Created by Thomas on 02.01.2018.
 */

public class TestSubject<T> implements ItemSubject<T> {
    private List<Table.OnDeleteListener> deleteListeners = new ArrayList<>();
    private List<Table.OnUpdateListener> updateListeners = new ArrayList<>();
    private List<Table.OnAddListener> addListeners = new ArrayList<>();

    public void add(T item) {
        for (Table.OnAddListener addListener : addListeners) {
            addListener.onAdd(item);
        }
    }

    public void update(T old, T newItem) {
        for (Table.OnUpdateListener updateListener : updateListeners) {
            updateListener.onUpdate(old, newItem);
        }
    }

    public void delete(T item) {
        for (Table.OnDeleteListener onDeleteListener : deleteListeners) {
            onDeleteListener.onDelete(item);
        }
    }

    @Override
    public void addDeleteListener(Table.OnDeleteListener<T> listener) {
        deleteListeners.add(listener);
    }

    @Override
    public void addUpdateListener(Table.OnUpdateListener<T> listener) {
        updateListeners.add(listener);
    }

    @Override
    public void addAddListener(Table.OnAddListener<T> listener) {
        addListeners.add(listener);
    }
}
