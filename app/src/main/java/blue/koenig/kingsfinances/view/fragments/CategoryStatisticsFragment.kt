package blue.koenig.kingsfinances.view.fragments


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import blue.koenig.kingsfinances.R
import blue.koenig.kingsfinances.dagger.FinanceApplication
import blue.koenig.kingsfinances.features.category_statistics.*
import blue.koenig.kingsfinances.view.lists.SpinnerAdapter
import com.jakewharton.rxbinding2.widget.RxAdapterView
import io.reactivex.Observable
import org.slf4j.LoggerFactory
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class CategoryStatisticsFragment : Fragment(), CategoryStatisticsView {
    private lateinit var _monthSelection: Observable<Int>
    private lateinit var _yearSelection: Observable<Int>
    private lateinit var _categoryGoals: Observable<CatGoal>
    override val monthSelection: Observable<Int>
        get() = _monthSelection
    override val yearSelection: Observable<Int>
        get() = _yearSelection
    override val categoryGoals: Observable<CatGoal>
        get() = _categoryGoals
    protected val logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    @Inject
    lateinit var presenter: CategoryStatisticsPresenter
    private var list: RecyclerView? = null
    private var adapter: CategoryStatisticsAdapter? = null
    private var dropdownYears: Spinner? = null
    private var dropdownMonths: Spinner? = null
    private var yearsAdapter: SpinnerAdapter? = null
    private var monthsAdapter: SpinnerAdapter? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        logger.info("Attaching statistics fragment")
        (activity?.application as FinanceApplication).financeAppComponent.inject(this)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.category_statistics, container, false)
        logger.info("Creating view cat statistics fragment")
        init(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        logger.info("Resume cat statistics fragment")
        presenter.attachView(this)
    }

    protected fun init(view: View) {

        list = view.findViewById(R.id.list_categorys)
        dropdownMonths = view.findViewById(R.id.dropdown_month)
        monthsAdapter = SpinnerAdapter()
        dropdownMonths?.adapter = monthsAdapter
        _monthSelection = RxAdapterView.itemSelections(dropdownMonths!!).hide()
        dropdownYears = view.findViewById(R.id.dropdown_years)
        yearsAdapter = SpinnerAdapter()
        dropdownYears?.adapter = yearsAdapter
        _yearSelection = RxAdapterView.itemSelections(dropdownYears!!).hide()
        list?.layoutManager = LinearLayoutManager(context)
        adapter = CategoryStatisticsAdapter()
        list?.adapter = adapter
        _categoryGoals = adapter?.goals!!
    }

    override fun onStop() {
        logger.info("Stop statistics fragment")
        presenter.detachView()
        super.onStop()
    }


    override fun render(state: CategoryStatisticsState) {
        adapter?.showGoal = state.yearsSelection != 0
        adapter?.update(state.categoryStatistics)
        yearsAdapter?.update(state.yearsList)
        monthsAdapter?.update(state.monthsList)
        dropdownYears?.setSelection(state.yearsSelection)
        dropdownMonths?.setSelection(state.monthsSelection)


    }
}
