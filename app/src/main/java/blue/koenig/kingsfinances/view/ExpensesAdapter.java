package blue.koenig.kingsfinances.view;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koenig.StringFormats;
import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.Costs;
import com.koenig.commonModel.finance.Expenses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import blue.koenig.kingsfinances.R;

/**
 * Created by Thomas on 08.09.2015.
 */
public class ExpensesAdapter extends BaseAdapter
{
    protected final Context context;
    private final HashMap<User, Integer> usersId;
    protected ExpensesInteractListener listener;
    private List<User> users;
    protected boolean showDeleteButton;
    private List<Expenses> expenses;



    public ExpensesAdapter(Context context, ExpensesInteractListener listener, List<User> users)
    {
        this.context = context;
        this.listener = listener;
        this.users = users;
        expenses = new ArrayList<>();
        this.usersId = new HashMap<User, Integer>();
        for (User user : users) {
            usersId.put(user, View.generateViewId());
        }
    }

    public void updateExpenses(List<Expenses> expenses)
    {
        this.expenses = expenses;

        Collections.sort(this.expenses, new Comparator<Expenses>()
        {
            @Override
            public int compare(Expenses lhs, Expenses rhs)
            {
                return rhs.getDate().compareTo(lhs.getDate());
            }
        });
        ((Activity) context).runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount()
    {
        return expenses.size();
    }

    @Override
    public Object getItem(int position)
    {
        return expenses.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return expenses.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final Expenses ex = expenses.get(position);
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expenses_item, null);
            convertView.setLongClickable(true);

            LinearLayout linearLayout = convertView.findViewById(R.id.persons_container);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, users.size());
            linearLayout.setLayoutParams(layoutParams);
            for (User member : users) {
                TextView person = (TextView) inflater.inflate(R.layout.expenses_person, null);
                person.setText(member.getName());
                person.setId(usersId.get(member));

                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                linearLayout.addView(person, layoutParams2);
            }
        }



        final ImageButton delete = convertView.findViewById(R.id.button_delete);
        delete.setOnClickListener(view -> {
            if (listener != null) listener.onDelete(ex);
        });

        final ImageButton edit = convertView.findViewById(R.id.button_edit);
        edit.setOnClickListener(view -> {
            if (listener != null) listener.onEdit(ex);


        });


        TextView name = (TextView) convertView.findViewById(R.id.text_name);
        TextView category = (TextView) convertView.findViewById(R.id.text_category);
        TextView costs = (TextView) convertView.findViewById(R.id.text_costs);
        TextView date = (TextView) convertView.findViewById(R.id.text_date);


        for (User user : users) {
            Costs costsFor = ex.getCostDistribution().getCostsFor(user);
            TextView costView = convertView.findViewById(usersId.get(user));
            costView.setText(costsFor.toEuroString());
            int colorRes;
            if (costsFor.Theory > 0)
            {
                colorRes = R.color.positive_highlight;
            } else if (costsFor.Theory < 0)
            {
                colorRes = R.color.negative_highlight;
            } else {
                colorRes = R.color.normalText;
            }

            costView.setTextColor(ContextCompat.getColor(context, colorRes));
        }

        name.setText(ex.getName());
        category.setText(ex.getCategory().toString());
        costs.setText(StringFormats.centsToEuroString(ex.getCosts()));
        if (ex.getCosts() > 0)
        {
            costs.setTextColor(ContextCompat.getColor(context, R.color.positive_highlight));
        } else if( ex.getCosts() < 0)
        {
            costs.setTextColor(ContextCompat.getColor(context, R.color.negative_highlight));
        } else {
            costs.setTextColor(ContextCompat.getColor(context, R.color.normalText));
        }

        date.setText(ex.getDate().toString("dd.MM.yy"));

        return convertView;
    }



    public void setExpenses(List<Expenses> expenses)
    {
        this.expenses = expenses;
    }

    public interface ExpensesInteractListener
    {
        void onDelete(Expenses expenses);
        void onEdit(Expenses expenses);
    }
}
