package blue.koenig.kingsfinances;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.BankAccount;
import com.koenig.commonModel.finance.Expenses;
import com.koenig.commonModel.finance.StandingOrder;

import java.util.List;

import javax.inject.Inject;

import blue.koenig.kingsfamilylibrary.model.family.FamilyModel;
import blue.koenig.kingsfamilylibrary.view.family.FamilyActivity;
import blue.koenig.kingsfinances.dagger.FinanceApplication;
import blue.koenig.kingsfinances.model.FinanceModel;
import blue.koenig.kingsfinances.model.calculation.Debts;
import blue.koenig.kingsfinances.view.BankAccountDialog;
import blue.koenig.kingsfinances.view.FinanceFragmentPagerAdapter;
import blue.koenig.kingsfinances.view.FinanceView;
import blue.koenig.kingsfinances.view.FinanceViewUtils;
import blue.koenig.kingsfinances.view.fragments.AccountFragment;
import blue.koenig.kingsfinances.view.fragments.ExpensesFragment;
import blue.koenig.kingsfinances.view.fragments.StandingOrderFragment;

public class OverviewActivity extends FamilyActivity implements FinanceView, NavigationView.OnNavigationItemSelectedListener {


    private FinanceFragmentPagerAdapter pageAdapter;
    private ViewPager pager;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);
        ((FinanceApplication) getApplication()).getFinanceAppComponent().inject(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // TODO: move to base class
        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(ignored -> clickFab());
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

                //getSupportActionBar().setSelectedNavigationItem(position);
                getFinanceModel().onTabSelected(position);

            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });

        pager.setCurrentItem(0);

        initDrawer(toolbar);


    }

    private void initDrawer(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void clickFab() {
        int currentItem = pager.getCurrentItem();
        if (currentItem == 0) {
            FinanceViewUtils.startAddExpensesActivity(this);

        } else if (currentItem == 1) {
            // standing order
            FinanceViewUtils.startAddStandingOrderActivity(this);
        } else if (currentItem == 2) {
            BankAccountDialog bankAccountDialog = new BankAccountDialog(this, getFinanceModel().getFamilyMembers());
            bankAccountDialog.setConfirmListener(bankAccount -> {
                getFinanceModel().addBankAccount(bankAccount);
                updateBankAccounts(getFinanceModel().getBankAccounts());
            });
            bankAccountDialog.showAdd();
        } else {
            logger.error("FAB should not be visible!");
        }
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
            runOnUiThread(() -> expensesFragment.updateExpenses(expenses));
        } else {
            logger.error("Couldn't update expensesFragment!");
        }

    }

    @Override
    public void setFamilyMembers(final List<User> members) {
        runOnUiThread(() -> {
            // Expenses list view
            ExpensesFragment expensesFragment = pageAdapter.getExpensesFragment();
            if (expensesFragment == null) {
                logger.error("Couldn't set members to expenses fragment!");
            } else {
                expensesFragment.updateFamilyMembers(members);
            }

            StandingOrderFragment standingOrderFragment = pageAdapter.getStandingOrderFragment();
            if (standingOrderFragment == null) {
                logger.error("Couldn't set members to expenses fragment!");
            } else {
                standingOrderFragment.updateFamilyMembers(members);
            }
            // overview
            // TODO
        });

    }

    @Override
    public void showStandingOrders(List<StandingOrder> standingOrders) {
        StandingOrderFragment standingOrderFragment = pageAdapter.getStandingOrderFragment();
        if (standingOrderFragment != null) {
            runOnUiThread(() -> standingOrderFragment.update(standingOrders));
        } else {
            logger.error("Couldn't update standingOrderFragment!");
        }
    }

    @Override
    public void updateBankAccounts(List<BankAccount> bankAccounts) {
        AccountFragment accountFragment = pageAdapter.getAccountFragment();
        if (accountFragment != null) {
            runOnUiThread(() -> accountFragment.update(bankAccounts));
        } else {
            logger.error("Couldn't update accountFragment!");
        }
    }

    @Override
    public void updateDebts(List<Debts> debts) {
        ExpensesFragment expensesFragment = pageAdapter.getExpensesFragment();
        if (expensesFragment != null) {
            runOnUiThread(() -> expensesFragment.updateDebts(debts));
        } else {
            logger.error("Couldn't update debts!");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        if (id == R.id.nav_expenses) {
            // Handle the camera action
            logger.info("Click on expenses");
            pager.setCurrentItem(0);
            //fragment = new ExpensesFragment();
        } else if (id == R.id.nav_pending) {
            logger.info("Click on pending");
            pager.setCurrentItem(1);
            //fragment = new PendingFragment();
        } else {
            logger.error("Unknown navigation item");
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Insert the fragment by replacing any existing fragment
        //FragmentManager fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        item.setChecked(true);
        // Set action bar title
        setTitle(item.getTitle());
        // Close the navigation drawer
        drawer.closeDrawers();


        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
