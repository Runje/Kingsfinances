package blue.koenig.kingsfinances.view.fragments


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import blue.koenig.kingsfamilylibrary.model.FamilyConfig
import blue.koenig.kingsfamilylibrary.view.DeleteDialog
import blue.koenig.kingsfamilylibrary.view.ViewUtils
import blue.koenig.kingsfinances.R
import blue.koenig.kingsfinances.dagger.FinanceApplication
import blue.koenig.kingsfinances.features.expenses.ExpensesPresenter
import blue.koenig.kingsfinances.features.expenses.ExpensesState
import blue.koenig.kingsfinances.features.expenses.ExpensesView
import blue.koenig.kingsfinances.model.calculation.StatisticEntry
import blue.koenig.kingsfinances.view.ChartHelper
import blue.koenig.kingsfinances.view.ChartHelper.entrysToMonthXValues
import blue.koenig.kingsfinances.view.FinanceViewUtils
import blue.koenig.kingsfinances.view.lists.ExpensesAdapter
import com.github.mikephil.charting.charts.LineChart
import com.jakewharton.rxbinding2.support.v4.widget.RxSwipeRefreshLayout
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.Expenses
import io.reactivex.Observable


/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class ExpensesFragment : MvpFragment<ExpensesState, ExpensesView, ExpensesPresenter>(), ExpensesView {
    override var onRefresh: Observable<Any> = Observable.never()

    private var state = ExpensesState()
    override fun render(state: ExpensesState) {
        renderIsLoading(state.isLoading)
        renderMembersLayout(state.familyMembers)
        renderList(state.expenses)
        renderDebts(state.debts)
        this.state = state
    }

    private fun renderDebts(debts: List<StatisticEntry>) {
        updateDebts(debts)
    }

    private fun renderList(expenses: List<Expenses>) {
        updateExpenses(expenses)
    }

    private fun renderMembersLayout(familyMembers: List<User>) {
        view?.let { view ->
            // change layout only if it has changed
            if (familyMembers != state.familyMembers) {
                val linearLayout = view.findViewById<LinearLayout>(R.id.persons_container)
                linearLayout.removeAllViews()
                val layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, familyMembers.size.toFloat())
                linearLayout.layoutParams = layoutParams
                val bigWidth = ViewUtils.getScreenWidth(activity) > 1300
                if (bigWidth) {
                    logger.info("BIG WIDTH SCREEN")
                    for (member in familyMembers) {
                        val person = activity!!.layoutInflater.inflate(R.layout.expenses_person, null) as TextView
                        person.text = member.name
                        val layoutParams2 = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        linearLayout.addView(person, layoutParams2)
                    }
                } else {
                    logger.info("SMALL WIDTH SCREEN")
                    linearLayout.visibility = View.GONE
                }

                val listView = view.findViewById<ListView>(R.id.list_expenses)

                // TODO: make observable for delete and edit

                adapter = ExpensesAdapter(arrayListOf(), bigWidth, object : ExpensesAdapter.ExpensesInteractListener {
                    override fun onDelete(expenses: Expenses) {
                        DeleteDialog(activity, expenses.name, expenses) { e -> /*model.deleteExpenses(e)*/ }.show()
                    }

                    override fun onEdit(expenses: Expenses) {
                        FinanceViewUtils.startEditExpensesActivity(context, expenses)
                    }
                }, familyMembers)
                listView.adapter = adapter
            }
        }
    }

    private fun renderIsLoading(loading: Boolean) {
        refreshLayout?.isRefreshing = loading
        refreshLayout?.invalidate()
    }

    private var adapter: ExpensesAdapter? = null
    private var lineChart: LineChart? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_expenses, container, false)
        logger.info("Creating view expenses fragment")
        init(view)
        return view
    }

    fun updateExpenses(expenses: List<Expenses>?) {
        if (adapter == null) {
            logger.error("Adapter is null")
            init(view)
        }

        if (adapter != null) {
            adapter!!.update(expenses!!)
        }
    }


    private var refreshLayout: SwipeRefreshLayout? = null

    fun init(view: View?) {
        //bar chart
        lineChart = view!!.findViewById(R.id.linechart)
        lineChart!!.axisRight.textColor = Color.WHITE
        lineChart!!.axisLeft.textColor = Color.WHITE
        lineChart!!.xAxis.textColor = Color.WHITE
        val legend = lineChart!!.legend
        legend.textColor = Color.WHITE
        legend.isEnabled = false

        lineChart!!.setGridBackgroundColor(Color.BLACK)
        lineChart!!.setVisibleXRangeMaximum(12f)
        refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        onRefresh = RxSwipeRefreshLayout.refreshes(refreshLayout!!)
    }


    fun updateDebts(debts: List<StatisticEntry>) {
        if (lineChart == null) {
            logger.error("Adapter is null")
            init(view)
        }

        if (lineChart != null) {
            updateLinechart(debts)
        }
    }

    @Synchronized
    private fun updateLinechart(statisticEntryList: List<StatisticEntry>) {
        // TODO: after long abstinence(probably destroy was called) and then resume it fails with a NPE on lineChart
        val lineData = ChartHelper.entrysToLineData(statisticEntryList, FamilyConfig.getUserId(context), Color.GREEN)
        lineChart?.data = lineData

        val xValues = entrysToMonthXValues(statisticEntryList)
        //convert x values to date string
        lineChart?.xAxis?.setValueFormatter { value, _ ->
            val intValue = value.toInt()
            if (intValue > xValues.size - 1) {
                logger.error("intvalue: " + intValue + ", xValues.size(): " + xValues.size)
                return@setValueFormatter "Error"
            }

            xValues[intValue]
        }

        // show last 12 month
        //lineChart.setVisibleXRangeMaximum(12);
        //lineChart.moveViewToX(Math.max(0, statisticEntryList.size() - 12));
        lineChart?.invalidate()
    }

    fun showLoading(loading: Boolean) {
        refreshLayout?.isRefreshing = loading
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        (context?.applicationContext as FinanceApplication).financeAppComponent.inject(this)
    }
}
