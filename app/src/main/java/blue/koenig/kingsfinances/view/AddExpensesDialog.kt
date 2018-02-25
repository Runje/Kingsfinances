package blue.koenig.kingsfinances.view

import android.app.AlertDialog
import android.content.Context
import blue.koenig.kingsfamilylibrary.view.EditDialog
import blue.koenig.kingsfinances.R
import blue.koenig.kingsfinances.model.CategoryService
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.CostDistribution
import com.koenig.commonModel.finance.Expenses
import org.joda.time.DateTime

/**
 * Created by Thomas on 20.11.2017.
 */

class AddExpensesDialog(context: Context, categoryService: CategoryService, users: List<User>, listener: AddExpensesListener) : EditExpensesDialog(context, Expenses("", "", "", 0, CostDistribution(), DateTime.now(), ""), categoryService, users, object : EditDialog.EditListener<Expenses> {
    override fun onEdit(expenses: Expenses) {
        listener.add(expenses)
    }

    override fun validate(expenses: Expenses): Boolean {
        return expenses.isValid
    }

    override fun getErrorMessage(): Int {
        return R.string.invalid_entry
    }
}) {

    override fun adaptBuilder(builder: AlertDialog.Builder) {
        builder.setPositiveButton(R.string.add, null)
    }


    interface AddExpensesListener {
        fun add(expenses: Expenses)
    }
}
