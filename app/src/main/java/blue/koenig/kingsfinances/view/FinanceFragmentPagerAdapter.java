package blue.koenig.kingsfinances.view;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.view.fragments.AccountFragment;
import blue.koenig.kingsfinances.view.fragments.CategoryStatisticsFragment;
import blue.koenig.kingsfinances.view.fragments.ExpensesFragment;
import blue.koenig.kingsfinances.view.fragments.PendingFragment;
import blue.koenig.kingsfinances.view.fragments.StandingOrderFragment;
import blue.koenig.kingsfinances.view.fragments.StatisticsFragment;

/**
 * Created by Thomas on 20.08.2015.
 */
public class FinanceFragmentPagerAdapter extends FragmentPagerAdapter
{
    private final Context context;
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    SparseArray<Fragment> fragments = new SparseArray<>();

    public FinanceFragmentPagerAdapter(Context context, FragmentManager fm)
    {
        super(fm);
        logger.info("Constructor adapter");
        this.context = context;
    }

    @Override
    public Fragment getItem(int position)
    {
        logger.info("Get item " + position);
        if (fragments.get(position) == null) {
            logger.info("Fragment is null");
            switch (position) {
                case 0:
                    ExpensesFragment expensesFragment = new ExpensesFragment();
                    fragments.put(0, expensesFragment);
                    break;

                case 1:
                    fragments.put(position, new StandingOrderFragment());
                    break;
                case 2:
                    fragments.put(position, new AccountFragment());
                    break;
                case 3:
                    fragments.put(position, new PendingFragment());
                    break;
                case 4:
                    fragments.put(position, new StatisticsFragment());
                    break;
                case 5:
                    fragments.put(position, new CategoryStatisticsFragment());
                    break;
            }
        }

        return fragments.get(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        logger.info("instantiate item " + position);
        Object ret = super.instantiateItem(container, position);
        fragments.put(position, (Fragment) ret);
        return ret;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        logger.info("destroy item " + position);
        fragments.put(position, null);
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount()
    {
        return 6;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return context.getResources().getString(R.string.expenses);
            case 1:
                return context.getResources().getString(R.string.pending);
            case 2:
                return context.getResources().getString(R.string.bank_account);
            case 3:
                return context.getResources().getString(R.string.statistics);
            case 4:
                return context.getResources().getString(R.string.statistics);
            case 5:
                return context.getResources().getString(R.string.statistics);
        }

        return super.getPageTitle(position);
    }

    public ExpensesFragment getExpensesFragment() {
        return (ExpensesFragment) fragments.get(0);
    }


    public StandingOrderFragment getStandingOrderFragment() {
        return (StandingOrderFragment) fragments.get(1);
    }

    public AccountFragment getAccountFragment() {
        return (AccountFragment) fragments.get(2);
    }
}
