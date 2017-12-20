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
import com.koenig.commonModel.finance.StandingOrder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import blue.koenig.kingsfinances.R;

/**
 * Created by Thomas on 08.09.2015.
 */
public class StandingOrderAdapter extends ListAdapter<StandingOrder>
{
    private HashMap<User, Integer> usersId;
    protected StandingOrderInteractListener listener;
    private List<User> users;

    public StandingOrderAdapter(List<StandingOrder> standingOrders, StandingOrderInteractListener listener, List<User> users)
    {
        super(standingOrders);
        this.listener = listener;
        this.users = users;
        this.usersId = new HashMap<User, Integer>();
        for (User user : users) {
            usersId.put(user, View.generateViewId());
        }
    }

    @Override
    protected Comparator<StandingOrder> getComparator() {
        return (lhs, rhs) -> rhs.getFirstDate().compareTo(lhs.getFirstDate());
    }

    @Override
    protected void initView(View convertView, StandingOrder ex) {
        Context context = convertView.getContext();
        convertView.setLongClickable(true);

        final ImageButton delete = convertView.findViewById(R.id.button_delete);
        delete.setOnClickListener(view -> {
            if (listener != null) listener.onDelete(ex);
        });

        final ImageButton edit = convertView.findViewById(R.id.button_edit);
        edit.setOnClickListener(view -> {
            if (listener != null) listener.onEdit(ex);


        });

        TextView name = (TextView) convertView.findViewById(R.id.text_name);
        TextView costs = (TextView) convertView.findViewById(R.id.text_costs);
        TextView date = (TextView) convertView.findViewById(R.id.text_first_date);




        name.setText(ex.getName());
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

        date.setText(ex.getFirstDate().toString("dd.MM.yy"));

    }

    @Override
    protected int getItemLayout() {
        return R.layout.standing_order_item;
    }

    public interface StandingOrderInteractListener
    {
        void onDelete(StandingOrder standingOrder);
        void onEdit(StandingOrder standingOrder);
    }
}
