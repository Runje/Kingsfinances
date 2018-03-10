package blue.koenig.kingsfinances.features.category_statistics

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import blue.koenig.kingsfinances.R
import com.koenig.StringFormats
import io.reactivex.subjects.PublishSubject
import org.slf4j.LoggerFactory

/**
 * Created by Thomas on 16.01.2018.
 */

class CategoryStatisticsAdapter(var items: List<CategoryStatistics> = arrayListOf()) : RecyclerView.Adapter<CategoryStatisticsAdapter.CategoryStatisticsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryStatisticsViewHolder {
        logger.info("Create view holder $viewType")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cat_statistics_item, parent, false)
        return CategoryStatisticsViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryStatisticsViewHolder, position: Int) {
        holder.bind(items[position], showGoal, goals)
    }

    private var logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    var showGoal: Boolean = true



    override fun getItemCount(): Int {
        return items.size
    }

    val goals: PublishSubject<CatGoal> = PublishSubject.create<CatGoal>()


    fun update(items: List<CategoryStatistics>) {
        this.items = items
        notifyDataSetChanged()
    }


    class CategoryStatisticsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val logger = LoggerFactory.getLogger(this.javaClass.simpleName)
        private var goalTextWatcher: GoalTextWatcher? = null
        fun bind(item: CategoryStatistics, showGoal: Boolean, goals: PublishSubject<CatGoal>) {
            logger.info("update view $item")
            val category = itemView.findViewById<TextView>(R.id.text_category)
            val winnings = itemView.findViewById<TextView>(R.id.text_winnings)
            val goal = itemView.findViewById<TextView>(R.id.textGoal)
            category.text = item.name
            winnings.text = StringFormats.centsToEuroString(item.winnings)

            goal.visibility = if (showGoal) View.VISIBLE else View.INVISIBLE
            if (showGoal) {
                goal.removeTextChangedListener(goalTextWatcher)
                goal.text = StringFormats.centsToEuroString(item.goal)
                goalTextWatcher = GoalTextWatcher(goal, item, goals)
                goal.addTextChangedListener(goalTextWatcher)
            }
        }

    }


}

data class CatGoal(val category: String, val goal: Int)

class GoalTextWatcher(val textView: TextView, var item: CategoryStatistics, val goals: PublishSubject<CatGoal>) : TextWatcher {
    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        Log.d("Adapter", "Calling text Changes for " + item)
        // validate
        val value = s.toString().toFloatOrNull()
        value?.let {
            @Suppress("DEPRECATION")
            textView.setTextColor(textView.context.resources.getColor(R.color.normalText))
            val intValue = (value * 100).toInt()
            if (item.goal != intValue) {
                // only if changed
                goals.onNext(CatGoal(item.name, intValue))
            }
        } ?: run {
            textView.setTextColor(Color.RED)
        }
    }
}