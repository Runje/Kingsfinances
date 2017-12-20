package blue.koenig.kingsfinances;

import android.content.Context;

import com.koenig.FamilyConstants;
import com.koenig.StringFormats;
import com.koenig.commonModel.Frequency;

import org.joda.time.DateTime;

import blue.koenig.kingsfinances.view.FinanceViewUtils;

/**
 * Created by Thomas on 20.12.2017.
 */

public class LocalizedStrings {
    public static String dateTimeToUnlimitedDayString(Context context, DateTime dateTime) {
        if (dateTime.equals(FamilyConstants.UNLIMITED)) return context.getString(R.string.unlimited);

        return StringFormats.dateTimeToDayString(dateTime);
    }

    public static String frequencyToString(Context context, Frequency frequency)
    {
        switch (frequency)
        {
            case Daily:
                return context.getResources().getText(R.string.daily).toString();
            case Weekly:
                return context.getResources().getText(R.string.weekly).toString();
            case Monthly:
                return context.getResources().getText(R.string.monthly).toString();
            case Yearly:
                return context.getResources().getText(R.string.yearly).toString();
        }

        return null;
    }

    public static String frequencyToString(Context context, int frequencyFactor, Frequency frequency) {
        return frequencyFactor == 1 ? frequencyToString(context, frequency) : frequencyFactor + "-" + frequencyToString(context, frequency);
    }
}
