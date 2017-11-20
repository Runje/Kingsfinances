package blue.koenig.kingsfinances.view;

import android.widget.TextView;

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
}