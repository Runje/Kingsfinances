package blue.koenig.kingsfinances.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koenig.StringFormats;
import com.koenig.commonModel.User;
import com.koenig.commonModel.Validator;
import com.koenig.commonModel.finance.Expenses;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.time.DateTimeException;
import java.util.List;

import blue.koenig.kingsfamilylibrary.view.EditDialog;
import blue.koenig.kingsfamilylibrary.view.TextValidator;
import blue.koenig.kingsfamilylibrary.view.ViewUtils;
import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.model.CategoryService;

/**
 * Created by Thomas on 08.11.2017.
 */

public class EditExpensesDialog extends EditDialog<Expenses> {


    private CategoryService categoryService;
    private List<User> users;
    private CostDistributionView costDistributionView;

    public EditExpensesDialog(Context context, Expenses item, CategoryService categoryService, List<User> users, @NotNull EditListener<Expenses> listener) {
        super(context, item, listener);
        this.categoryService = categoryService;
        this.users = users;
    }

    protected View makeView(LayoutInflater inflater) {
        LinearLayout overview = (LinearLayout) inflater.inflate(R.layout.expenses_dialog_overview, null);
        costDistributionView = new CostDistributionView(context, item.getCostDistribution(), users);
        overview.addView(costDistributionView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        initLayout(overview);
        return overview;
    }


    private void initLayout(final View layout)
    {
        final EditText editName = (EditText) layout.findViewById(R.id.edit_name);
        final EditText editCosts = (EditText) layout.findViewById(R.id.edit_costs);
        final EditText editCategory = (EditText) layout.findViewById(R.id.edit_sub_category);
        final EditText editFirstDate = (EditText) layout.findViewById(R.id.edit_first_date);

        editName.addTextChangedListener(new TextValidator(editName) {
            @Override
            public void validate(TextView textView, String s) {
                item.setName(s);
                if (!Validator.isNotEmpty(s)) {
                    textView.setError(context.getString(R.string.may_not_be_empty));
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
                    item.setCosts((int) (costs * 100));
                    textView.setError(null);
                    setEnabledConfirmButton(true);
                    costDistributionView.setCosts(item.getCosts());

                } catch (NumberFormatException e) {
                    textView.setError(context.getString(R.string.wrong_format_use_point_as_separator));
                    setEnabledConfirmButton(false);
                }
            }
        });
        editCategory.setOnClickListener(v -> {
            new PickCategoryDialog(context, new PickCategoryInteractor() {
                @Override
                public void onPick(String mainCategory, String subCategory) {
                    editCategory.setText(FinanceViewUtils.CategoryToString(mainCategory ,subCategory));
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
            ViewUtils.getDateFromDialog(context, (dateTime -> editFirstDate.setText(StringFormats.dateTimeToDayString(dateTime))));
        });


        editName.setText(item.getName());
        editCosts.setText(StringFormats.centsToCentString(item.getCosts()));
        editCategory.setText(FinanceViewUtils.CategoryToString(item.getCategory(), item.getSubCategory()));

        TextView textDate = (TextView) layout.findViewById(R.id.text_date);

            textDate.setText(R.string.date);
            editFirstDate.setText(StringFormats.dateTimeToDayString(item.getDate()));

    }


}
