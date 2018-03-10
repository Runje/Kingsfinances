package blue.koenig.kingsfinances.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import blue.koenig.kingsfamilylibrary.view.ViewUtils
import blue.koenig.kingsfinances.R
import com.koenig.StringFormats
import com.koenig.commonModel.finance.Balance
import org.joda.time.LocalDate

/**
 * Created by Thomas on 12.09.2015.
 */
class BalanceDialog {
    private val context: Context
    private var balance: Balance? = null
    private var confirmListener: ConfirmListener? = null
    private var backToOverview = false

    constructor(context: Context, balance: Balance) {
        // Show overview
        this.context = context
        this.balance = balance
    }

    constructor(context: Context) {
        this.context = context
        balance = Balance(0, LocalDate.now())
    }

    fun showEdit() {
        showOverview()
    }

    private fun showBalance() {
        val builder = AlertDialog.Builder(context)
        val layout = LayoutInflater.from(context).inflate(R.layout.edit_costs_dialog, null)
        val editCosts = layout.findViewById<View>(R.id.edit_costs) as EditText
        if (balance!!.balance != 0) {
            editCosts.setText(java.lang.Float.toString(balance!!.balance.toFloat()))
        }
        ViewUtils.clickOn(editCosts)
        builder.setView(layout)
        builder.setTitle(R.string.balance)
        builder.setPositiveButton("OK") { dialog, which ->
            try {
                val c = java.lang.Float.parseFloat(editCosts.text.toString())
                balance!!.balance = (c * 100).toInt()
                if (backToOverview) {
                    showOverview()
                } else {
                    showDate()
                }
            } catch (e: Exception) {
                Toast.makeText(context, R.string.empty_not_allowed, Toast.LENGTH_SHORT).show()
                showBalance()
            }
        }
        builder.setNegativeButton(R.string.cancel, null)
        builder.create().show()
    }


    private fun showDate() {
        val builder = AlertDialog.Builder(context)
        val layout = LayoutInflater.from(context).inflate(R.layout.date_picker, null)
        builder.setView(layout)
        builder.setTitle(R.string.date)
        builder.setNegativeButton(R.string.cancel, null)
        val datePicker = layout.findViewById<DatePicker>(R.id.datePicker)
        if (balance!!.day != null) {
            ViewUtils.setDateToDatePicker(datePicker, balance!!.day)
        }
        builder.setPositiveButton("OK") { dialog, which ->
            val date = ViewUtils.getDateFromDatePicker(datePicker)
            balance!!.day = date
            showOverview()
        }

        builder.create().show()
    }


    private fun showOverview() {
        backToOverview = true
        if (balance!!.day == null) {
            balance!!.day = LocalDate()
        }
        val builder = AlertDialog.Builder(context)
        val layout = LayoutInflater.from(context).inflate(R.layout.balance_dialog_overview, null)
        builder.setView(layout)

        builder.setPositiveButton(R.string.save) { _, _ ->
            updateFromLayout(layout)
            confirmListener!!.onConfirm(balance!!)
        }
        builder.setNegativeButton(R.string.cancel, null)

        val dialog = builder.create()
        dialog.show()

        updateLayout(layout, dialog)
    }

    private fun updateFromLayout(layout: View) {
        val editBalance = layout.findViewById<View>(R.id.edit_balance) as EditText
        val balance = java.lang.Float.parseFloat(editBalance.text.toString())
        this.balance!!.balance = (100 * balance).toInt()
    }

    private fun updateLayout(layout: View, dialog: Dialog) {
        val editBalance = layout.findViewById<View>(R.id.edit_balance) as EditText
        val editDate = layout.findViewById<View>(R.id.edit_last_date) as EditText


        editDate.setOnClickListener { v ->
            dialog.cancel()
            updateFromLayout(layout)
            showDate()
        }


        editBalance.setText(StringFormats.centsToCentString(balance!!.balance))
        editDate.setText(balance!!.day.toString("dd.MM.yy"))
    }

    fun setConfirmListener(confirmListener: ConfirmListener) {
        this.confirmListener = confirmListener
    }

    fun showAdd() {
        showBalance()
    }

    interface ConfirmListener {
        fun onConfirm(balance: Balance)
    }
}
