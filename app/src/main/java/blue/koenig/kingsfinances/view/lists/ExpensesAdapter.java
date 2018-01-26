package blue.koenig.kingsfinances.view.lists;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koenig.StringFormats;
import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.Costs;
import com.koenig.commonModel.finance.Expenses;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import blue.koenig.kingsfinances.R;

/**
 * Created by Thomas on 08.09.2015.
 */
public class ExpensesAdapter extends ListAdapter<Expenses>
{
    private final boolean bigWidth;
    protected ExpensesInteractListener listener;
    private HashMap<User, Integer> usersId;
    private List<User> users;

    public ExpensesAdapter(List<Expenses> expenses, boolean bigWidth, ExpensesInteractListener listener, List<User> users)
    {
        super(expenses);
        this.bigWidth = bigWidth;
        this.listener = listener;
        this.users = users;
        this.usersId = new HashMap<User, Integer>();
        for (User user : users) {
            usersId.put(user, View.generateViewId());
        }
    }

    @Override
    protected Comparator<Expenses> getComparator() {
        return (lhs, rhs) -> rhs.getDate().compareTo(lhs.getDate());
    }

    @Override
    protected void updateView(View convertView, Expenses ex, int pos) {
        Context context = convertView.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        convertView.setLongClickable(true);

        LinearLayout linearLayout = convertView.findViewById(R.id.persons_container);
        linearLayout.removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, users.size());
        linearLayout.setLayoutParams(layoutParams);
        if (bigWidth) {
            for (User member : users) {
                TextView person = (TextView) inflater.inflate(R.layout.expenses_person, null);
                person.setText(member.getName());
                person.setId(usersId.get(member));

                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                linearLayout.addView(person, layoutParams2);
            }
        }
        else {
            linearLayout.setVisibility(View.GONE);
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


        if (bigWidth) {
            for (User user : users) {
                Costs costsFor = ex.getCostDistribution().getCostsFor(user);
                TextView costView = convertView.findViewById(usersId.get(user));
                costView.setText(costsFor.toEuroString());
                int colorRes;
                if (costsFor.Theory > 0) {
                    colorRes = R.color.positive_highlight;
                } else if (costsFor.Theory < 0) {
                    colorRes = R.color.negative_highlight;
                } else {
                    colorRes = R.color.normalText;
                }

                costView.setTextColor(ContextCompat.getColor(context, colorRes));
            }
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

    }

    @Override
    protected int getItemLayout() {
        return R.layout.expenses_item;
    }

    public interface ExpensesInteractListener
    {
        void onDelete(Expenses expenses);
        void onEdit(Expenses expenses);
    }
}
