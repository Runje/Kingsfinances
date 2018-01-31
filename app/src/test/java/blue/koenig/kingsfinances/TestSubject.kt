package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.model.calculation.ItemSubject
import blue.koenig.kingsfinances.model.database.Table
import java.util.*

/**
 * Created by Thomas on 02.01.2018.
 */

open class TestSubject<T> : ItemSubject<T> {
    private val deleteListeners = ArrayList<Table.OnDeleteListener<T>>()
    private val updateListeners = ArrayList<Table.OnUpdateListener<T>>()
    private val addListeners = ArrayList<Table.OnAddListener<T>>()


    fun add(item: T) {
        for (addListener in addListeners) {
            addListener.onAdd(item)
        }
    }

    fun update(old: T, newItem: T) {
        for (updateListener in updateListeners) {
            updateListener.onUpdate(old, newItem)
        }
    }

    fun delete(item: T) {
        for (onDeleteListener in deleteListeners) {
            onDeleteListener.onDelete(item)
        }
    }

    override fun addDeleteListener(listener: Table.OnDeleteListener<T>) {
        deleteListeners.add(listener)
    }

    override fun addUpdateListener(listener: Table.OnUpdateListener<T>) {
        updateListeners.add(listener)
    }

    override fun addAddListener(listener: Table.OnAddListener<T>) {
        addListeners.add(listener)
    }
}
