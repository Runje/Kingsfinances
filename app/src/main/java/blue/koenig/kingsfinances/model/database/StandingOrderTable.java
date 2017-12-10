package blue.koenig.kingsfinances.model.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.koenig.commonModel.Frequency;
import com.koenig.commonModel.finance.BookkeepingEntry;
import com.koenig.commonModel.finance.Expenses;
import com.koenig.commonModel.finance.StandingOrder;

import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Thomas on 30.11.2017.
 */

public class StandingOrderTable extends BookkeepingTable<StandingOrder> {
    public static final String NAME = "standing_order_table";
    private static final String FIRST_DATE = "first_date";
    private static final String END_DATE = "end_date";
    private static final String FREQUENCY = "frequency";
    private static final String FREQUENCY_FACTOR = "frequency_factor";
    private static final String EXECUTED_EXPENSES = "executed_expenses";

    public StandingOrderTable(SQLiteDatabase database) {
        super(database);
    }

    @Override
    protected String getBookkeepingTableSpecificCreateStatement() {
        return "," + FIRST_DATE + " LONG, "
                + END_DATE + " LONG, "
                + FREQUENCY + " TEXT,"
                + FREQUENCY_FACTOR + " INT, "
                + EXECUTED_EXPENSES + " TEXT ";
    }

    @Override
    protected void setBookkeepingItem(ContentValues values, StandingOrder item) {
        values.put(FIRST_DATE, dateTimeToValue(item.getFirstDate()));
        values.put(END_DATE, dateTimeToValue(item.getEndDate()));
        values.put(FREQUENCY, item.getFrequency().name());
        values.put(FREQUENCY_FACTOR, item.getFrequencyFactor());
        values.put(EXECUTED_EXPENSES, buildStringList(item.getExecutedExpenses()));
    }

    @Override
    protected StandingOrder getBookkeepingItem(BookkeepingEntry entry, Cursor cursor) {
        DateTime firstDate = getDateTime(cursor, FIRST_DATE);
        DateTime endDate = getDateTime(cursor, END_DATE);
        Frequency frequency = getEnum(cursor, FREQUENCY, Frequency.class);
        int frequencyFactor = getInt(cursor, FREQUENCY_FACTOR);
        List<String> executedExpenses = getStringList(cursor, EXECUTED_EXPENSES);
        return new StandingOrder(entry, firstDate, endDate, frequency, frequencyFactor, executedExpenses);
    }




    @Override
    protected Collection<? extends String> getBookkeepingColumnNames() {
        return Arrays.asList(FIRST_DATE, END_DATE, FREQUENCY, FREQUENCY_FACTOR, EXECUTED_EXPENSES);
    }

    @Override
    public String getTableName() {
        return NAME;
    }
}
