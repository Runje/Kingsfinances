package blue.koenig.kingsfinances.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import blue.koenig.kingsfamilylibrary.view.EditDialog
import blue.koenig.kingsfamilylibrary.view.TextValidator
import blue.koenig.kingsfamilylibrary.view.ViewUtils
import blue.koenig.kingsfinances.R
import blue.koenig.kingsfinances.model.CategoryService
import com.koenig.StringFormats
import com.koenig.commonModel.User
import com.koenig.commonModel.Validator
import com.koenig.commonModel.finance.Expenses

/**
 * Created by Thomas on 08.11.2017.
 */

open class EditExpensesDialog(context: Context, item: Expenses, private val categoryService: CategoryService, private val users: List<User>, listener: EditDialog.EditListener<Expenses>) : EditDialog<Expenses>(context, item, listener) {
    private var costDistributionView: CostDistributionView? = null

    override fun makeView(inflater: LayoutInflater): View {
        val overview = inflater.inflate(R.layout.expenses_dialog_overview, null) as LinearLayout
        costDistributionView = CostDistributionView(context, item.costDistribution, users)
        overview.addView(costDistributionView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        initLayout(overview)
        return overview
    }


    private fun initLayout(layout: View) {
        val editName = layout.findViewById<View>(R.id.edit_name) as EditText
        val editCosts = layout.findViewById<View>(R.id.edit_costs) as EditText
        val editCategory = layout.findViewById<View>(R.id.edit_sub_category) as EditText
        val editFirstDate = layout.findViewById<View>(R.id.edit_first_date) as EditText

        editName.addTextChangedListener(object : TextValidator(editName) {
            override fun validate(textView: TextView, s: String) {
                item.name = s
                if (!Validator.isNotEmpty(s)) {
                    textView.error = context.getString(R.string.may_not_be_empty)
                    setEnabledConfirmButton(false)
                } else {
                    textView.error = null
                    setEnabledConfirmButton(true)
                }
            }
        })

        editCosts.addTextChangedListener(object : TextValidator(editCosts) {
            override fun validate(textView: TextView, s: String) {
                try {
                    val costs = java.lang.Double.parseDouble(s)
                    item.costs = (costs * 100).toInt()
                    textView.error = null
                    setEnabledConfirmButton(true)
                    costDistributionView!!.setCosts(item.costs)

                } catch (e: NumberFormatException) {
                    textView.error = context.getString(R.string.wrong_format_use_point_as_separator)
                    setEnabledConfirmButton(false)
                }

            }
        })
        editCategory.setOnClickListener {
            PickCategoryDialog(context, object : PickCategoryInteractor {
                override fun onPick(mainCategory: String, subCategory: String) {
                    editCategory.setText(FinanceViewUtils.CategoryToString(mainCategory, subCategory))
                    item.subCategory = subCategory
                    item.category = mainCategory
                }

                override fun getSubs(mainCategory: String): List<String> {
                    return categoryService.getSubCategories(mainCategory)
                }

                override fun isValid(category: String): Boolean {
                    return categoryService.isValid(category)
                }

                override fun addMainCategory(newCategory: String) {
                    categoryService.addMainCategory(newCategory)
                }

                override fun addSubCategory(mainCategory: String, newCategory: String) {
                    categoryService.addSubCategory(mainCategory, newCategory)
                }

                override fun getMainCategorys(): List<String> {
                    return categoryService.mainCategories
                }
            }).show()
        }


        editFirstDate.setOnClickListener { ViewUtils.getDateFromDialog(context) { dateTime -> editFirstDate.setText(StringFormats.localDateTimeToDayString(dateTime)) } }


        editName.setText(item.name)
        editCosts.setText(StringFormats.centsToCentString(item.costs))
        editCategory.setText(FinanceViewUtils.CategoryToString(item.category, item.subCategory))

        val textDate = layout.findViewById<View>(R.id.text_date) as TextView

        textDate.setText(R.string.date)
        editFirstDate.setText(StringFormats.localDateTimeToDayString(item.day))

    }


}
