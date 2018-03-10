package blue.koenig.kingsfinances.view.fragments


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import blue.koenig.kingsfinances.R
import blue.koenig.kingsfinances.dagger.FinanceApplication
import blue.koenig.kingsfinances.features.statistics.StatisticsPresenter
import blue.koenig.kingsfinances.features.statistics.StatisticsState
import blue.koenig.kingsfinances.features.statistics.StatisticsView
import blue.koenig.kingsfinances.view.ChartHelper
import blue.koenig.kingsfinances.view.lists.ListAdapter
import com.github.mikephil.charting.charts.LineChart
import com.koenig.StringFormats
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.statistics.MonthStatistic
import org.joda.time.YearMonth
import org.slf4j.LoggerFactory
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class StatisticsFragment : Fragment(), StatisticsView {
    protected var logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    @Inject
    lateinit var presenter: StatisticsPresenter
    private var lineChart: LineChart? = null
    private var list: ListView? = null
    private var adapter: ListAdapter<String>? = null
    private var savingRate: TextView? = null
    private var monthly: TextView? = null
    private var overall: TextView? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        logger.info("Attaching statistics fragment")
        (activity!!.application as FinanceApplication).financeAppComponent.inject(this)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_statistics, container, false)
        logger.info("Creating view statistics fragment")
        init(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        logger.info("Resume statistics fragment")
        presenter.attachView(this)
    }

    protected fun init(view: View) {
        savingRate = view.findViewById(R.id.text_saving_rate)
        overall = view.findViewById(R.id.text_overall)
        monthly = view.findViewById(R.id.text_monthly)
        lineChart = view.findViewById(R.id.linechart)
        lineChart!!.axisRight.textColor = Color.WHITE
        lineChart!!.axisLeft.textColor = Color.WHITE
        lineChart!!.xAxis.textColor = Color.WHITE
        val legend = lineChart!!.legend
        legend.textColor = Color.WHITE
        lineChart!!.setGridBackgroundColor(Color.BLACK)
        lineChart!!.setVisibleXRangeMaximum(12f)

        list = view.findViewById(R.id.yearList)
        adapter = object : ListAdapter<String>() {

            override val itemLayout: Int
                get() = android.R.layout.simple_list_item_1

            override fun updateView(convertView: View, item: String, pos: Int) {
                (convertView.findViewById<View>(android.R.id.text1) as TextView).text = item
            }
        }
        list!!.adapter = adapter
        list!!.setOnItemClickListener { parent, view1, position, id -> presenter.clickYear(position) }
    }

    private fun updateLinechart(statisticEntryList: Map<YearMonth, MonthStatistic>, users: List<User>) {
        val lineData = ChartHelper.mapToLineData(statisticEntryList.values.sortedBy { it.month }, intArrayOf(Color.BLUE, Color.RED, Color.GREEN, Color.GRAY, Color.WHITE), users)
        lineChart!!.data = lineData

        val xValues = ChartHelper.entrysToMonthXValues(statisticEntryList.keys.sorted())
        //convert x values to date string
        lineChart!!.xAxis.setValueFormatter { value, _ ->
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
        lineChart!!.invalidate()
    }

    override fun onStop() {
        logger.info("Stop statistics fragment")
        presenter.detachView()
        super.onStop()
    }


    override fun render(state: StatisticsState) {
        updateLinechart(state.statistics.assets, state.users)
        adapter!!.update(state.yearsList)

        monthly!!.text = StringFormats.centsToEuroString(state.statistics.monthlyWin) + " €"
        overall!!.text = StringFormats.centsToEuroString(state.statistics.overallWin) + " €"
        savingRate!!.text = StringFormats.floatToPercentString(state.savingRate) + " %"
    }
}
