package blue.koenig.kingsfinances.view.lists

import android.app.Activity
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Created by Thomas on 26.11.2017.
 */

abstract class ListAdapter<T> @JvmOverloads constructor(var items: List<T> = ArrayList()) : BaseAdapter() {
    protected var logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    protected open val comparator: Comparator<T>?
        get() = null

    @get:LayoutRes
    protected abstract val itemLayout: Int

    init {
        update(items)
    }

    open fun update(items: List<T>) {
        this.items = items
        val comparator = comparator
        if (comparator != null) Collections.sort(this.items, comparator)
        notifyDataSetChanged()

    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): T {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return items.indexOf(getItem(position)).toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var resultView = convertView
        val item = items[position]
        if (resultView == null) {
            val inflater = parent.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            resultView = inflater.inflate(itemLayout, null)
            initView(resultView, item, position)
        }

        updateView(resultView!!, item, position)
        return resultView
    }

    protected open fun initView(convertView: View, item: T, position: Int) {}

    protected abstract fun updateView(convertView: View, item: T, pos: Int)
}
