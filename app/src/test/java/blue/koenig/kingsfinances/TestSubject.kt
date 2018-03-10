package blue.koenig.kingsfinances

import blue.koenig.kingsfinances.model.database.ItemTable
import com.koenig.commonModel.finance.statistics.ItemSubject
import java.util.*

/**
 * Created by Thomas on 02.01.2018.
 */

open class TestSubject<T> : ItemSubject<T> {
    private val deleteListeners = ArrayList<ItemTable.OnDeleteListener<T>>()
    private val updateListeners = ArrayList<ItemTable.OnUpdateListener<T>>()
    private val addListeners = ArrayList<ItemTable.OnAddListener<T>>()


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

    override fun addDeleteListener(listener: ItemTable.OnDeleteListener<T>) {
        deleteListeners.add(listener)
    }

    override fun addUpdateListener(listener: ItemTable.OnUpdateListener<T>) {
        updateListeners.add(listener)
    }

    override fun addAddListener(listener: ItemTable.OnAddListener<T>) {
        addListeners.add(listener)
    }
}
