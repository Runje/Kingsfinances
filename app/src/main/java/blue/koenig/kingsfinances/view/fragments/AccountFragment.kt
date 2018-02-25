package blue.koenig.kingsfinances.view.fragments


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import blue.koenig.kingsfamilylibrary.view.DeleteDialog
import blue.koenig.kingsfinances.R
import blue.koenig.kingsfinances.model.calculation.StatisticEntryDeprecated
import blue.koenig.kingsfinances.view.BalancesDialog
import blue.koenig.kingsfinances.view.ChartHelper
import blue.koenig.kingsfinances.view.ChartHelper.entrysToMonthXValues
import blue.koenig.kingsfinances.view.lists.AccountAdapter
import com.github.mikephil.charting.charts.LineChart
import com.koenig.FamilyConstants
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.Balance
import com.koenig.commonModel.finance.BankAccount


/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
class AccountFragment : FinanceFragment() {
    private var adapter: AccountAdapter? = null
    private var lineChart: LineChart? = null
    private lateinit var statUsers: MutableList<User>

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        logger.info("Attaching account fragment")
        statUsers = model.config.familyMembers.toMutableList()
        statUsers.add(FamilyConstants.ALL_USER)
        //model.attachPendingView(this);
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        logger.info("Creating view account fragment")
        init(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        logger.info("Resume account fragment")
    }

    override fun update() {
        update(model.bankAccounts)
        updateLinechart(model.allAssets)
    }

    override fun init(view: View) {

        val listView = view.findViewById<ListView>(R.id.list_accounts)
        adapter = AccountAdapter(model.bankAccounts, object : AccountAdapter.AccountInteractListener {
            override fun onDelete(account: BankAccount) {
                DeleteDialog(context, account.name, account) { model.deleteBankAccount(account) }.show()
            }

            override fun onEdit(account: BankAccount) {
                BalancesDialog(context, account, object : BalancesDialog.BalanceDialogListener {
                    override fun onDelete(account: BankAccount, balance: Balance) {
                        model.deleteBalance(account, balance)
                        update()
                    }

                    override fun onAdd(account: BankAccount, balance: Balance) {
                        model.addBalance(account, balance)
                        update()
                    }
                }).show()
            }
        })
        listView.adapter = adapter
        lineChart = view.findViewById(R.id.linechart)
        val statisticEntryList = model.allAssets
        lineChart!!.axisRight.textColor = Color.WHITE
        lineChart!!.axisLeft.textColor = Color.WHITE
        lineChart!!.xAxis.textColor = Color.WHITE
        val legend = lineChart!!.legend
        legend.textColor = Color.WHITE


        lineChart!!.setGridBackgroundColor(Color.BLACK)
        lineChart!!.setVisibleXRangeMaximum(12f)

        updateLinechart(statisticEntryList)
        initialized = true
    }

    private fun updateLinechart(statisticEntryList: List<StatisticEntryDeprecated>) {
        val lineData = ChartHelper.entrysToLineData(statisticEntryList, intArrayOf(Color.BLUE, Color.RED, Color.GREEN, Color.GRAY, Color.WHITE), statUsers)
        lineChart!!.data = lineData

        val xValues = entrysToMonthXValues(statisticEntryList)
        //convert x values to date string
        lineChart!!.xAxis.setValueFormatter { value, _ ->
            val intValue = value.toInt()
            if (intValue > xValues.size - 1) {
                logger.error("intvalue: " + intValue + ", xValues.size(): " + xValues.size)
                intValue.toString()
            } else xValues[intValue]
        }

        // show last 12 month
        //lineChart.setVisibleXRangeMaximum(12);
        //lineChart.moveViewToX(Math.max(0, statisticEntryList.size() - 12));
        lineChart!!.invalidate()
    }

    override fun onStop() {
        logger.info("Stop pending fragment")
        model.detachPendingView()
        super.onStop()
    }

    fun update(accounts: List<BankAccount>) {
        activity!!.runOnUiThread { adapter!!.update(accounts) }
    }

    fun updateAssets(assets: List<StatisticEntryDeprecated>) {
        activity!!.runOnUiThread { updateLinechart(assets) }
    }
}
