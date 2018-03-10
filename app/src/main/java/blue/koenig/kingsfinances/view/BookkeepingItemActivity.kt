package blue.koenig.kingsfinances.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import blue.koenig.kingsfamilylibrary.view.TextValidator
import blue.koenig.kingsfamilylibrary.view.ViewUtils
import blue.koenig.kingsfinances.LocalizedStrings
import blue.koenig.kingsfinances.R
import blue.koenig.kingsfinances.dagger.FinanceApplication
import blue.koenig.kingsfinances.model.CategoryService
import blue.koenig.kingsfinances.model.FinanceModel
import com.koenig.FamilyConstants
import com.koenig.StringFormats
import com.koenig.commonModel.Frequency
import com.koenig.commonModel.Validator
import com.koenig.commonModel.finance.BookkeepingEntry
import com.koenig.commonModel.finance.CostDistribution
import com.koenig.commonModel.finance.Expenses
import com.koenig.commonModel.finance.StandingOrder
import org.joda.time.LocalDate
import java.nio.ByteBuffer
import java.util.*
import javax.inject.Inject

/**
 * Created by Thomas on 19.12.2017.
 */

class BookkeepingItemActivity : AppCompatActivity() {
    @Inject
    lateinit var model: FinanceModel
    private var isExpenses = false
    private var costDistributionView: CostDistributionView? = null
    private var expenses: Expenses? = null
    private var bookkeepingItem: BookkeepingEntry? = null
    private var standingOrder: StandingOrder? = null
    private var categoryService: CategoryService? = null
    private var confirmButton: Button? = null
    private var isEdit: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as FinanceApplication).financeAppComponent.inject(this)
        categoryService = model.getCategoryService()

        if (intent != null) {
            val type = intent.getStringExtra(TYPE)
            val bytes = intent.getByteArrayExtra(ITEM)
            if (type == EXPENSES) {
                isExpenses = true
            }

            if (bytes != null) {
                isEdit = true
                val buffer = ByteBuffer.wrap(bytes)
                if (isExpenses) {
                    expenses = Expenses(buffer)
                    bookkeepingItem = expenses
                } else {
                    standingOrder = StandingOrder(buffer)
                    bookkeepingItem = standingOrder
                }
            }
        }

        if (bookkeepingItem == null) {
            if (isExpenses) {
                expenses = Expenses("", "", "", 0, CostDistribution(), LocalDate(), "")
                bookkeepingItem = expenses
            } else {
                standingOrder = StandingOrder("", "", "", 0, CostDistribution(), LocalDate(), FamilyConstants.UNLIMITED, Frequency.Monthly, 1, HashMap())
                bookkeepingItem = standingOrder
            }
        }

        setContentView(R.layout.expenses_dialog_overview)

        confirmButton = findViewById(R.id.button_confirm)
        confirmButton!!.text = if (isEdit) getString(R.string.edit) else getString(R.string.add)
        confirmButton!!.setOnClickListener {
            //TODO move logic to presenter
            if (!bookkeepingItem!!.isValid) {
                Toast.makeText(this, R.string.invalid_entry, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isEdit) {
                if (isExpenses) {
                    model.editExpenses(expenses!!)
                } else {
                    model.editStandingOrder(standingOrder!!)
                }
            } else {
                if (isExpenses) {
                    model.addExpenses(expenses!!)
                } else {
                    model.addStandingOrder(standingOrder!!)
                }
            }

            finish()

        }
        val cancelButton = findViewById<Button>(R.id.button_cancel)
        cancelButton.setOnClickListener { finish() }
        costDistributionView = CostDistributionView(this, bookkeepingItem!!.costDistribution, model.familyMembers)
        (findViewById<View>(R.id.costdistribution) as LinearLayout).addView(costDistributionView, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        initLayout()
    }

    private fun initLayout() {

        val editName = findViewById<View>(R.id.edit_name) as EditText
        val editCosts = findViewById<View>(R.id.edit_costs) as EditText
        val editCategory = findViewById<View>(R.id.edit_sub_category) as EditText
        val editFirstDate = findViewById<View>(R.id.edit_first_date) as EditText

        editName.addTextChangedListener(object : TextValidator(editName) {
            override fun validate(textView: TextView, s: String) {
                bookkeepingItem!!.name = s
                if (!Validator.isNotEmpty(s)) {
                    textView.error = getString(R.string.may_not_be_empty)
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
                    bookkeepingItem!!.costs = (costs * 100).toInt()
                    textView.error = null
                    setEnabledConfirmButton(true)
                    costDistributionView!!.setCosts(bookkeepingItem!!.costs)

                } catch (e: NumberFormatException) {
                    textView.error = getString(R.string.wrong_format_use_point_as_separator)
                    setEnabledConfirmButton(false)
                }

            }
        })
        editCategory.setOnClickListener {
            PickCategoryDialog(this, object : PickCategoryInteractor {
                override fun onPick(mainCategory: String, subCategory: String) {
                    editCategory.setText(FinanceViewUtils.CategoryToString(mainCategory, subCategory))
                    bookkeepingItem!!.subCategory = subCategory
                    bookkeepingItem!!.category = mainCategory
                }

                override fun getSubs(mainCategory: String): List<String> {
                    return categoryService!!.getSubCategories(mainCategory)
                }

                override fun isValid(category: String): Boolean {
                    return categoryService!!.isValid(category)
                }

                override fun addMainCategory(newCategory: String) {
                    categoryService!!.addMainCategory(newCategory)
                }

                override fun addSubCategory(mainCategory: String, newCategory: String) {
                    categoryService!!.addSubCategory(mainCategory, newCategory)
                }

                override fun getMainCategorys(): List<String> {
                    return categoryService!!.mainCategories
                }
            }).show()
        }


        editFirstDate.setOnClickListener {
            ViewUtils.getDateFromDialog(this) { day ->
                editFirstDate.setText(StringFormats.localDateTimeToDayString(day))
                if (isExpenses)
                    expenses!!.day = day
                else
                    standingOrder!!.firstDate = day
            }
        }


        editName.setText(bookkeepingItem!!.name)
        editCosts.setText(StringFormats.centsToCentString(bookkeepingItem!!.costs))
        editCategory.setText(FinanceViewUtils.CategoryToString(bookkeepingItem!!.category, bookkeepingItem!!.subCategory))

        val textDate = findViewById<View>(R.id.text_date) as TextView

        textDate.setText(R.string.date)
        editFirstDate.setText(LocalizedStrings.localDateToUnlimitedDayString(this, if (isExpenses) expenses!!.day else standingOrder!!.firstDate))

        if (isExpenses) {
            findViewById<View>(R.id.standing_order_addition).visibility = View.GONE
        } else {
            (findViewById<View>(R.id.text_date) as TextView).setText(R.string.firstDate)
            val editLastDate = findViewById<EditText>(R.id.edit_last_date)
            editLastDate.setOnClickListener {
                ViewUtils.getUnlimitedDateFromDialog(this) { dateTime ->
                    editLastDate.setText(LocalizedStrings.localDateToUnlimitedDayString(this, dateTime))
                    standingOrder!!.endDate = dateTime
                }
            }
            val editFrequency = findViewById<EditText>(R.id.edit_frequency)
            editLastDate.setText(LocalizedStrings.localDateToUnlimitedDayString(this, standingOrder!!.endDate))
            val frequency = LocalizedStrings.frequencyToString(this, standingOrder!!.frequencyFactor, standingOrder!!.frequency)
            editFrequency.setText(frequency)
            editFrequency.setOnClickListener {
                ChooseFrequencyDialog(this, standingOrder!!.frequencyFactor, standingOrder!!.frequency) { factor, freq ->
                    editFrequency.setText(LocalizedStrings.frequencyToString(this, factor, freq))
                    standingOrder!!.frequency = freq
                    standingOrder!!.frequencyFactor = factor
                }.show()
            }
            // TODO: show first execution dates
            // TODO: show preview on change standing orders, which entrys would be deleted,which would be added?
        }


    }

    private fun setEnabledConfirmButton(enabled: Boolean) {
        confirmButton!!.isEnabled = enabled
    }

    companion object {
        val TYPE = "TYPE"
        val EXPENSES = "EXPENSES"
        val STANDINGORDER = "STANDINGORDER"
        val ITEM = "ITEM"
    }
}
