package blue.koenig.kingsfinances

import com.koenig.commonModel.database.DatabaseItemTable
import com.koenig.commonModel.finance.statistics.ItemSubject
import java.util.*

/**
 * Created by Thomas on 02.01.2018.
 */

open class TestSubject<T> : ItemSubject<T> {
    private val deleteListeners = ArrayList<DatabaseItemTable.OnDeleteListener<T>>()
    private val updateListeners = ArrayList<DatabaseItemTable.OnUpdateListener<T>>()
    private val addListeners = ArrayList<DatabaseItemTable.OnAddListener<T>>()


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

    override fun addDeleteListener(listener: DatabaseItemTable.OnDeleteListener<T>) {
        deleteListeners.add(listener)
    }

    override fun addUpdateListener(listener: DatabaseItemTable.OnUpdateListener<T>) {
        updateListeners.add(listener)
    }

    override fun addAddListener(listener: DatabaseItemTable.OnAddListener<T>) {
        addListeners.add(listener)
    }
}
