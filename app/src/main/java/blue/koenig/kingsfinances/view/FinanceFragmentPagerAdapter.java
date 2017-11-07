package blue.koenig.kingsfinances.view;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import blue.koenig.kingsfinances.R;

/**
 * Created by Thomas on 20.08.2015.
 */
public class FinanceFragmentPagerAdapter extends FragmentPagerAdapter
{
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private final Context context;
    SparseArray<Fragment> fragments = new SparseArray<>();

    public FinanceFragmentPagerAdapter(Context context, FragmentManager fm)
    {
        super(fm);
        logger.info("Constructor adapter");
        this.context = context;
        this.fragments = fragments;
        //fragments.put(0, new ExpensesFragment());
/**
        fragments.add(new StandingOrderFragment());
        fragments.add(new BankAccountFragment());
        fragments.add(new StatisticsFragment_());**/

    }

    @Override
    public Fragment getItem(int position)
    {
        logger.info("Get item " + position);
        if (fragments.get(0) == null) {
            logger.info("Fragment is null");
            switch (position) {
                case 0:
                    ExpensesFragment expensesFragment = new ExpensesFragment();
                    fragments.put(0, expensesFragment);
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
        return 1;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return context.getResources().getString(R.string.expenses);
            case 1:
                return context.getResources().getString(R.string.standing_order);
            case 2:
                return context.getResources().getString(R.string.bank_account);
            case 3:
                return context.getResources().getString(R.string.statistics);
        }

        return super.getPageTitle(position);
    }

    public ExpensesFragment getExpensesFragment() {
        return (ExpensesFragment) fragments.get(0);
    }


}