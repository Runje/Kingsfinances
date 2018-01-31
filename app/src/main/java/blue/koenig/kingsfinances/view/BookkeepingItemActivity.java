package blue.koenig.kingsfinances.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.koenig.FamilyConstants;
import com.koenig.StringFormats;
import com.koenig.commonModel.Frequency;
import com.koenig.commonModel.Validator;
import com.koenig.commonModel.finance.BookkeepingEntry;
import com.koenig.commonModel.finance.CostDistribution;
import com.koenig.commonModel.finance.Expenses;
import com.koenig.commonModel.finance.StandingOrder;

import org.joda.time.DateTime;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import blue.koenig.kingsfamilylibrary.view.TextValidator;
import blue.koenig.kingsfamilylibrary.view.ViewUtils;
import blue.koenig.kingsfinances.LocalizedStrings;
import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.dagger.FinanceApplication;
import blue.koenig.kingsfinances.model.CategoryService;
import blue.koenig.kingsfinances.model.FinanceModel;

/**
 * Created by Thomas on 19.12.2017.
 */

public class BookkeepingItemActivity extends AppCompatActivity {
    public static final String TYPE = "TYPE";
    public static final String EXPENSES = "EXPENSES";
    public static final String STANDINGORDER = "STANDINGORDER";
    public static final String ITEM = "ITEM";
    @Inject
    FinanceModel model;
    private boolean isExpenses = false;
    private CostDistributionView costDistributionView;
    private Expenses expenses;
    private BookkeepingEntry bookkeepingItem;
    private StandingOrder standingOrder;
    private CategoryService categoryService;
    private Button confirmButton;
    private boolean isEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FinanceApplication) getApplication()).getFinanceAppComponent().inject(this);
        categoryService = model.getCategoryService();

        if (getIntent() != null) {
            String type = getIntent().getStringExtra(TYPE);
            byte[] bytes = getIntent().getByteArrayExtra(ITEM);
            if (type.equals(EXPENSES)) {
                isExpenses = true;
            }

            if (bytes != null) {
                isEdit = true;
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                if (isExpenses) {
                    expenses = new Expenses(buffer);
                    bookkeepingItem = expenses;
                } else {
                    standingOrder = new StandingOrder(buffer);
                    bookkeepingItem = standingOrder;
                }
            }
        }

        if (bookkeepingItem == null) {
            if (isExpenses) {
                expenses = new Expenses("", "", "", 0, new CostDistribution(), DateTime.now(),"");
                bookkeepingItem = expenses;
            } else {
                standingOrder = new StandingOrder("", "", "", 0, new CostDistribution(), DateTime.now(), FamilyConstants.UNLIMITED, Frequency.Monthly, 1, new HashMap<>());
                bookkeepingItem = standingOrder;
            }
        }

        setContentView(R.layout.expenses_dialog_overview);

        confirmButton = findViewById(R.id.button_confirm);
        confirmButton.setText(isEdit ? getString(R.string.edit) : getString(R.string.add));
        confirmButton.setOnClickListener((v) -> {
            //TODO move logic to presenter
            if (!bookkeepingItem.isValid()) {
                Toast.makeText(this, R.string.invalid_entry, Toast.LENGTH_SHORT).show();
                return;
            }
            if (isEdit) {
                if (isExpenses) {
                    model.editExpenses(expenses);
                } else {
                    model.editStandingOrder(standingOrder);
                }
            } else {
                if (isExpenses) {
                    model.addExpenses(expenses);
                } else {
                    model.addStandingOrder(standingOrder);
                }
            }

            finish();

        });
        Button cancelButton = findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener((v) -> finish());
        costDistributionView = new CostDistributionView(this, bookkeepingItem.getCostDistribution(), model.getFamilyMembers());
        ((LinearLayout) findViewById(R.id.costdistribution)).addView(costDistributionView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        initLayout();
    }

    private void initLayout() {

        final EditText editName = (EditText) findViewById(R.id.edit_name);
        final EditText editCosts = (EditText) findViewById(R.id.edit_costs);
        final EditText editCategory = (EditText) findViewById(R.id.edit_sub_category);
        final EditText editFirstDate = (EditText) findViewById(R.id.edit_first_date);

        editName.addTextChangedListener(new TextValidator(editName) {
            @Override
            public void validate(TextView textView, String s) {
                bookkeepingItem.setName(s);
                if (!Validator.isNotEmpty(s)) {
                    textView.setError(getString(R.string.may_not_be_empty));
                    setEnabledConfirmButton(false);
                } else {
                    textView.setError(null);
                    setEnabledConfirmButton(true);
                }
            }
        });

        editCosts.addTextChangedListener(new TextValidator(editCosts) {
            @Override
            public void validate(TextView textView, String s) {
                try {
                    double costs = Double.parseDouble(s);
                    bookkeepingItem.setCosts((int) (costs * 100));
                    textView.setError(null);
                    setEnabledConfirmButton(true);
                    costDistributionView.setCosts(bookkeepingItem.getCosts());

                } catch (NumberFormatException e) {
                    textView.setError(getString(R.string.wrong_format_use_point_as_separator));
                    setEnabledConfirmButton(false);
                }
            }
        });
        editCategory.setOnClickListener(v -> {
            new PickCategoryDialog(this, new PickCategoryInteractor() {
                @Override
                public void onPick(String mainCategory, String subCategory) {
                    editCategory.setText(FinanceViewUtils.CategoryToString(mainCategory ,subCategory));
                    bookkeepingItem.setSubCategory(subCategory);
                    bookkeepingItem.setCategory(mainCategory);
                }

                @Override
                public List<String> getSubs(String mainCategory) {
                    return categoryService.getSubCategories(mainCategory);
                }

                @Override
                public boolean isValid(String category) {
                    return categoryService.isValid(category);
                }

                @Override
                public void addMainCategory(String newCategory) {
                    categoryService.addMainCategory(newCategory);
                }

                @Override
                public void addSubCategory(String mainCategory, String newCategory) {
                    categoryService.addSubCategory(mainCategory, newCategory);
                }

                @Override
                public List<String> getMainCategorys() {
                    return categoryService.getMainCategories();
                }
            }).show();
        });


        editFirstDate.setOnClickListener(v -> {
            ViewUtils.getDateFromDialog(this, (dateTime -> {
                editFirstDate.setText(StringFormats.dateTimeToDayString(dateTime));
                if (isExpenses) expenses.setDate(dateTime);
                else standingOrder.setFirstDate(dateTime);
            }));
        });


        editName.setText(bookkeepingItem.getName());
        editCosts.setText(StringFormats.centsToCentString(bookkeepingItem.getCosts()));
        editCategory.setText(FinanceViewUtils.CategoryToString(bookkeepingItem.getCategory(), bookkeepingItem.getSubCategory()));

        TextView textDate = (TextView) findViewById(R.id.text_date);

        textDate.setText(R.string.date);
        editFirstDate.setText(LocalizedStrings.dateTimeToUnlimitedDayString(this, isExpenses ? expenses.getDate(): standingOrder.getFirstDate()));

        if (isExpenses) {
            findViewById(R.id.standing_order_addition).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.text_date)).setText(R.string.firstDate);
            EditText editLastDate = findViewById(R.id.edit_last_date);
            editLastDate.setOnClickListener(v -> {
                ViewUtils.getUnlimitedDateFromDialog(this, (dateTime -> {
                    editLastDate.setText(LocalizedStrings.dateTimeToUnlimitedDayString(this, dateTime));
                    standingOrder.setEndDate(dateTime);
                }));
            });
            EditText editFrequency = findViewById(R.id.edit_frequency);
            editLastDate.setText(LocalizedStrings.dateTimeToUnlimitedDayString(this, standingOrder.getEndDate()));
            String frequency = LocalizedStrings.frequencyToString(this, standingOrder.getFrequencyFactor(), standingOrder.getFrequency());
            editFrequency.setText(frequency);
            editFrequency.setOnClickListener((v) -> new ChooseFrequencyDialog(this,standingOrder.getFrequencyFactor(), standingOrder.getFrequency(), (factor, freq) -> {
                editFrequency.setText(LocalizedStrings.frequencyToString(this, factor, freq));
                standingOrder.setFrequency(freq);
                standingOrder.setFrequencyFactor(factor);
            }).show());
            // TODO: show first execution dates
            // TODO: show preview on change standing orders, which entrys would be deleted,which would be added?
        }


}

    private void setEnabledConfirmButton(boolean enabled) {
        confirmButton.setEnabled(enabled);
    }
}
