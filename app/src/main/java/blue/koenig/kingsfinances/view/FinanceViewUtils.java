package blue.koenig.kingsfinances.view;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import com.koenig.commonModel.finance.Expenses;
import com.koenig.commonModel.finance.StandingOrder;

/**
 * Created by Thomas on 14.11.2017.
 */

public class FinanceViewUtils {

    public static String CategoryToString(String main, String sub) {
        if (sub.equals("")) {
            return main;
        }

        return main + " / " + sub;
    }

    public static String CategoryToOneString(String main, String sub) {
        if (sub.equals("")) {
            return main;
        }

        return sub;
    }

    public static float getCostsFromTextView(TextView textView) {
            return Float.parseFloat(textView.getText().toString());
    }

    public static float getPercentFromTextView(TextView textView) {
        return Float.parseFloat(textView.getText().toString()) / 100f;
    }

    public static void startAddExpensesActivity(Context context) {
        Intent intent = new Intent(context, BookkeepingItemActivity.class);
        intent.putExtra(BookkeepingItemActivity.Companion.getTYPE(), BookkeepingItemActivity.Companion.getEXPENSES());
        context.startActivity(intent);
    }

    public static void startAddStandingOrderActivity(Context context) {
        Intent intent = new Intent(context, BookkeepingItemActivity.class);
        intent.putExtra(BookkeepingItemActivity.Companion.getTYPE(), BookkeepingItemActivity.Companion.getSTANDINGORDER());
        context.startActivity(intent);
    }

    public static void startEditExpensesActivity(Context context, Expenses expenses) {
        Intent intent = new Intent(context, BookkeepingItemActivity.class);
        intent.putExtra(BookkeepingItemActivity.Companion.getTYPE(), BookkeepingItemActivity.Companion.getEXPENSES());
        intent.putExtra(BookkeepingItemActivity.Companion.getITEM(), expenses.getBytes());
        context.startActivity(intent);
    }

    public static void startEditStandingOrderActivity(Context context, StandingOrder standingOrder) {
        Intent intent = new Intent(context, BookkeepingItemActivity.class);
        intent.putExtra(BookkeepingItemActivity.Companion.getTYPE(), BookkeepingItemActivity.Companion.getSTANDINGORDER());
        intent.putExtra(BookkeepingItemActivity.Companion.getITEM(), standingOrder.getBytes());
        context.startActivity(intent);
    }
}