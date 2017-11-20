package blue.koenig.kingsfinances.view;

import android.content.Context;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koenig.FamilyUtils;
import com.koenig.StringFormats;
import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.CostDistribution;
import com.koenig.commonModel.finance.Costs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import blue.koenig.kingsfamilylibrary.view.TextValidator;
import blue.koenig.kingsfinances.R;

/**
 * Created by Thomas on 10.11.2017.
 */

public class CostDistributionView extends LinearLayout {
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private CostDistribution costDistribution;
    private List<User> users;
    private Map<User, Integer> userRealViewIdMap = new HashMap<>();
    private Map<User, Integer> userTheoryViewIdMap = new HashMap<>();
    LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    LayoutParams buttonLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    ReentrantLock lock = new ReentrantLock();
    private int itemCosts;

    public CostDistributionView(Context context, CostDistribution costDistribution, List<User> users) {
        super(context);
        this.costDistribution = costDistribution;
        this.itemCosts = costDistribution.sumReal();
        this.users = users;
        setOrientation(VERTICAL);

        TextView real = makeHeader(R.string.real);
        addView(real, layoutParams);

        for (User user : users) {
            Costs costs = costDistribution.getCostsFor(user);
            View distributorView = makeDistributor(user, costs.Real, costDistribution.getRealPercent(user), userRealViewIdMap, true);
            addView(distributorView, layoutParams);
        }

        TextView theory = makeHeader(R.string.theory);
        addView(theory, layoutParams);

        for (User user : users) {
            Costs costs = costDistribution.getCostsFor(user);
            View distributorView = makeDistributor(user, costs.Theory, costDistribution.getTheoryPercent(user), userTheoryViewIdMap, false);
            addView(distributorView, layoutParams);
        }
    }

    private View makeDistributor(User user, int costs, float percent, Map<User, Integer> userViewIdMap, boolean real) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.cost_distributor, null);
        int id = View.generateViewId();
        view.setId(id);
        userViewIdMap.put(user, id);

        setCosts(user.getName(), StringFormats.centsToCentString(costs), StringFormats.floatToPercentString(percent), view);
        User otherUser = getOtherUser(user);

        // add text watcher
        EditText editCosts = view.findViewById(R.id.editEuro);
        EditText editPercent = view.findViewById(R.id.editPercent);

        editCosts.addTextChangedListener(new TextValidator(editCosts) {
            @Override
            public void validate(TextView textView, String s) {
                if (lock.isLocked()) {
                    return;
                }

                lock.lock();
                try {
                    logger.info("Starting to change text");
                    float euro = FinanceViewUtils.getCostsFromTextView(textView);
                    View otherUsersView = findViewById(userViewIdMap.get(otherUser));
                    EditText otherEditCosts = otherUsersView.findViewById(R.id.editEuro);
                    EditText otherEditPercent = otherUsersView.findViewById(R.id.editPercent);
                    textView.setError(null);
                    Costs c = costDistribution.getCostsFor(user);
                    int cents = (int) (euro * 100);
                    otherEditCosts.setText(StringFormats.centsToCentString(itemCosts - cents));
                    if (itemCosts != 0) {
                        float percent = (float) cents / itemCosts;
                        float otherPercent = 1 - percent;
                        editPercent.setText(StringFormats.floatToPercentString(percent));
                        otherEditPercent.setText(StringFormats.floatToPercentString(otherPercent));
                    }
                    updateCostDistribution();

                } catch (NumberFormatException e) {
                    textView.setError(getContext().getString(R.string.invalid_costs));
                } finally {
                    lock.unlock();

                    logger.info("Stopped changing text");
                }
            }


        });

        editPercent.addTextChangedListener(new TextValidator(editPercent) {

                                               @Override
                                               public void validate(TextView textView, String s) {
                                                   if (lock.isLocked()) {
                                                       return;
                                                   }
                                                   try {
                                                       lock.lock();
                                                       logger.info("Starting to change text");
                                                       float percent = FinanceViewUtils.getPercentFromTextView(textView);
                                                       View otherUsersView = findViewById(userViewIdMap.get(otherUser));
                                                       EditText otherEditCosts = otherUsersView.findViewById(R.id.editEuro);
                                                       EditText otherEditPercent = otherUsersView.findViewById(R.id.editPercent);
                                                       textView.setError(null);
                                                       Costs c = costDistribution.getCostsFor(user);
                                                       int cents = (int) (percent * itemCosts);
                                                       editCosts.setText(StringFormats.centsToCentString(cents));
                                                       otherEditCosts.setText(StringFormats.centsToCentString(itemCosts - cents));

                                                       float otherPercent = 1 - percent;
                                                       otherEditPercent.setText(StringFormats.floatToPercentString(otherPercent));

                                                       updateCostDistribution();

                                                   } catch (NumberFormatException e) {
                                                       textView.setError(getContext().getString(R.string.invalid_costs));
                                                   } finally {
                                                       lock.unlock();
                                                       logger.info("Stopped changing text");
                                                   }
                                               }
                                           });
        // add buttons
        LinearLayout buttons = view.findViewById(R.id.buttons);
        int n = costDistribution.getDistribution().size();
        if (n == 2) {
            Button bu100 = new Button(getContext());
            bu100.setText("100%");

            bu100.setOnClickListener((b) -> {
                editCosts.setText(StringFormats.centsToCentString(itemCosts));
                updateCostDistribution();
            });
            buttons.addView(bu100, buttonLayoutParams);

            Button bu50 = new Button(getContext());
            bu50.setText("50%");
            bu50.setOnClickListener((b) -> {
                editCosts.setText(StringFormats.centsToCentString(FamilyUtils.getHalfRoundDown(itemCosts)));
                updateCostDistribution();
            });
            buttons.addView(bu50, buttonLayoutParams);
        }
        return view;
    }

    private User getOtherUser(User user) {
        if (users.size() != 2) {
            throw new RuntimeException("Not 2 Users: " + users.size());
        }
        for (User user1 : users) {
            if (!user1.equals(user)) {
                return user1;
            }
        }

        throw new RuntimeException("There is no other user");

    }

    private void updateCostDistribution() {
        for (User user : costDistribution.getDistribution().keySet()) {
            Costs oldCosts = costDistribution.getCostsFor(user);
            // Theory
            View distributorView = findViewById(userTheoryViewIdMap.get(user));

            EditText editCosts = distributorView.findViewById(R.id.editEuro);
            try {
                float costs = FinanceViewUtils.getCostsFromTextView(editCosts);
                editCosts.setError(null);
                oldCosts.Theory = (int)(costs * 100);
            } catch (NumberFormatException e) {
                editCosts.setError(getContext().getString(R.string.invalid_costs));
            }

            // Real
            distributorView = findViewById(userRealViewIdMap.get(user));

            editCosts = distributorView.findViewById(R.id.editEuro);
            try {
                float costs = FinanceViewUtils.getCostsFromTextView(editCosts);
                editCosts.setError(null);
                oldCosts.Real = (int)(costs * 100);
            } catch (NumberFormatException e) {
                editCosts.setError(getContext().getString(R.string.invalid_costs));
            }


        }
    }

    private void setCosts(String user, String costs, String percent, View view) {

        if (view == null) {
            logger.error("View not found");
            return;
        }

        EditText editEuro = view.findViewById(R.id.editEuro);
        editEuro.setText(costs);

        EditText editPercent = view.findViewById(R.id.editPercent);
        editPercent.setText(percent);

        TextView textName = view.findViewById(R.id.textName);
        textName.setText(user);
    }

    private TextView makeHeader(@StringRes int real) {
        TextView view = new TextView(getContext());
        view.setText(real);
        return view;
    }

    public void setCosts(int costs) {
        itemCosts = costs;
        User user = users.get(0);
        View view = findViewById(userTheoryViewIdMap.get(user));
        View view2 = findViewById(userRealViewIdMap.get(user));
        // just trigger text watcher to recalculate
        TextView edit = view.findViewById(R.id.editPercent);
        TextView edit2 = view2.findViewById(R.id.editPercent);
        edit.setText(edit.getText().toString());
        edit2.setText(edit2.getText().toString());
    }
}
