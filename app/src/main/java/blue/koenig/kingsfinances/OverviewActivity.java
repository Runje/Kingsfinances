package blue.koenig.kingsfinances;
import blue.koenig.kingsfamilylibrary.model.family.FamilyModel;
import blue.koenig.kingsfamilylibrary.view.family.FamilyActivity;
import blue.koenig.kingsfinances.dagger.FinanceApplication;
import blue.koenig.kingsfinances.model.FinanceModel;
import blue.koenig.kingsfinances.view.ExpensesFragment;
import blue.koenig.kingsfinances.view.FinanceFragmentPagerAdapter;
import blue.koenig.kingsfinances.view.FinanceView;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.Expenses;

import java.util.List;

import javax.inject.Inject;

public class OverviewActivity extends FamilyActivity implements FinanceView {


    private FinanceFragmentPagerAdapter pageAdapter;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance);
        ((FinanceApplication) getApplication()).getFinanceAppComponent().inject(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // TODO: move to base class
        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        pageAdapter = new FinanceFragmentPagerAdapter(this, getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.statistics_pager);
        pager.setAdapter(pageAdapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                logger.info("Page Selected: " + position);

                getSupportActionBar().setSelectedNavigationItem(position);
                getFinanceModel().onTabSelected(position);

            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });

        pager.setCurrentItem(0);
    }

    private FinanceModel getFinanceModel() {
        return (FinanceModel) model;
    }
    @Override
    protected FamilyModel createModel() {
        return model;
    }
    @Inject
    protected void provideModel(FinanceModel model) {
        this.model = model;
    }


    @Override
    public void showExpenses(List<Expenses> expenses) {
        ExpensesFragment expensesFragment = pageAdapter.getExpensesFragment();
        if (expensesFragment != null) {
            expensesFragment.updateExpenses(expenses);
        } else {
            logger.error("Couldn't update expensesFragment!");
        }

    }

    @Override
    public void setFamilyMembers(final List<User> members) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Expenses list view
                ExpensesFragment expensesFragment = pageAdapter.getExpensesFragment();
                if (expensesFragment == null) {
                    logger.error("Couldn't set members to expenses fragment!");
                } else {
                    expensesFragment.setFamilyMembers(members);
                }
                // overview
                // TODO
            }
        });

    }
}
