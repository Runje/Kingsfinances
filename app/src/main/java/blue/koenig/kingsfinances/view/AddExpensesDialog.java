package blue.koenig.kingsfinances.view;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.StringRes;

import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.CostDistribution;
import com.koenig.commonModel.finance.Expenses;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.List;

import blue.koenig.kingsfamilylibrary.view.EditDialog;
import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.model.CategoryService;

/**
 * Created by Thomas on 20.11.2017.
 */

public class AddExpensesDialog extends EditExpensesDialog {
    public AddExpensesDialog(Context context, CategoryService categoryService, List<User> users, @NotNull AddExpensesListener listener) {
        super(context, new Expenses("", "", "", 0, new CostDistribution(), DateTime.now(),""), categoryService, users, new EditListener<Expenses>() {
            @Override
            public void onEdit(Expenses expenses) {
                listener.add(expenses);
            }

            @Override
            public boolean validate(Expenses expenses) {
                return expenses.isValid();
            }

            @Override
            public int getErrorMessage() {
                return R.string.invalid_entry;
            }
        });

    }

    @Override
    protected void adaptBuilder(AlertDialog.Builder builder) {
        builder.setPositiveButton(R.string.add, null);
    }


    public interface AddExpensesListener {
        void add(Expenses expenses);
    }
}
