package blue.koenig.kingsfinances.model.calculation

import blue.koenig.kingsfinances.model.database.Table

/**
 * Created by Thomas on 02.01.2018.
 */

interface ItemSubject<T> {
    fun addDeleteListener(listener: Table.OnDeleteListener<T>)
    fun addDeleteListener(listener: (T?) -> Unit) {
        addDeleteListener(object : Table.OnDeleteListener<T> {
            override fun onDelete(item: T?) {
                listener(item)
            }
        })
    }

    fun addUpdateListener(listener: Table.OnUpdateListener<T>)

    fun addUpdateListener(listener: (T?, T?) -> Unit) {
        addUpdateListener(object : Table.OnUpdateListener<T> {
            override fun onUpdate(oldItem: T?, newItem: T) {
                listener(oldItem, newItem)
            }
        })
    }

    fun addAddListener(listener: Table.OnAddListener<T>)
    fun addAddListener(listener: (T?) -> Unit) {
        addAddListener(object : Table.OnAddListener<T> {
            override fun onAdd(item: T?) {
                listener(item)
            }
        })
    }
}
