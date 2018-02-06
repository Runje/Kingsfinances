package blue.koenig.kingsfinances

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.TextView
import blue.koenig.kingsfamilylibrary.model.family.FamilyModel
import blue.koenig.kingsfamilylibrary.view.family.FamilyActivity
import blue.koenig.kingsfinances.dagger.FinanceApplication
import blue.koenig.kingsfinances.model.FinanceModel
import blue.koenig.kingsfinances.model.calculation.StatisticEntry
import blue.koenig.kingsfinances.view.BankAccountDialog
import blue.koenig.kingsfinances.view.FinanceFragmentPagerAdapter
import blue.koenig.kingsfinances.view.FinanceView
import blue.koenig.kingsfinances.view.FinanceViewUtils
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.BankAccount
import com.koenig.commonModel.finance.StandingOrder
import javax.inject.Inject

class OverviewActivity : FamilyActivity(), FinanceView, NavigationView.OnNavigationItemSelectedListener {


    private var pageAdapter: FinanceFragmentPagerAdapter? = null
    private var pager: ViewPager? = null
    private var fab: FloatingActionButton? = null

    private val financeModel: FinanceModel
        get() = model as FinanceModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawer_layout)
        (application as FinanceApplication).financeAppComponent.inject(this)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        // TODO: move to base class
        connectionStatus = findViewById<TextView>(R.id.connectionStatus)
        fab = findViewById(R.id.fab_add)
        fab!!.setOnClickListener { clickFab() }
        pageAdapter = FinanceFragmentPagerAdapter(this, supportFragmentManager)
        pager = findViewById<ViewPager>(R.id.statistics_pager)
        pager!!.adapter = pageAdapter
        pager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                //logger.info("Page scrolled, position: " + position);
            }

            override fun onPageSelected(position: Int) {
                logger.info("Page Selected: " + position)

                //getSupportActionBar().setSelectedNavigationItem(position);
                financeModel.onTabSelected(position)

            }

            override fun onPageScrollStateChanged(state: Int) {
                logger.info("PageScrollStateChanged: " + state)
            }
        })

        pager!!.currentItem = 0

        initDrawer(toolbar)


    }

    private fun initDrawer(toolbar: Toolbar) {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun clickFab() {
        val currentItem = pager!!.currentItem
        if (currentItem == 0) {
            FinanceViewUtils.startAddExpensesActivity(this)

        } else if (currentItem == 1) {
            // standing order
            FinanceViewUtils.startAddStandingOrderActivity(this)
        } else if (currentItem == 2) {
            val bankAccountDialog = BankAccountDialog(this, financeModel.familyMembers)
            bankAccountDialog.setConfirmListener { bankAccount ->
                financeModel.addBankAccount(bankAccount)
                updateBankAccounts(financeModel.bankAccounts)
            }
            bankAccountDialog.showAdd()
        } else {
            logger.error("FAB should not be visible!")
        }
    }

    override fun createModel(): FamilyModel {
        return model
    }

    @Inject
    fun provideModel(model: FinanceModel) {
        this.model = model
    }


    override fun setFamilyMembers(members: List<User>) {
        runOnUiThread {


            val standingOrderFragment = pageAdapter!!.standingOrderFragment
            if (standingOrderFragment == null) {
                logger.error("Couldn't set members to expenses fragment!")
            } else {
                standingOrderFragment.updateFamilyMembers(members)
            }
            // overview
            // TODO
        }

    }

    override fun showStandingOrders(standingOrders: List<StandingOrder>) {
        val standingOrderFragment = pageAdapter!!.standingOrderFragment
        if (standingOrderFragment != null) {
            runOnUiThread { standingOrderFragment.update(standingOrders) }
        } else {
            logger.error("Couldn't update standingOrderFragment!")
        }
    }

    override fun updateBankAccounts(bankAccounts: List<BankAccount>) {
        val accountFragment = pageAdapter!!.accountFragment
        if (accountFragment != null) {
            runOnUiThread { accountFragment.update(bankAccounts) }
        } else {
            logger.error("Couldn't update accountFragment!")
        }
    }

    override fun updateAssets(assets: List<StatisticEntry>) {
        val accountFragment = pageAdapter!!.accountFragment
        if (accountFragment != null) {
            runOnUiThread { accountFragment.updateAssets(assets) }
        } else {
            logger.error("Couldn't update accountFragment!")
        }
    }


    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        if (id == R.id.nav_expenses) {
            // Handle the camera action
            logger.info("Click on expenses")
            pager!!.currentItem = 0
            //fragment = new ExpensesFragment();
        } else if (id == R.id.nav_pending) {
            logger.info("Click on pending")
            pager!!.currentItem = 1
            //fragment = new PendingFragment();
        } else if (id == R.id.nav_statistics) {
            logger.info("Click on statistics")
            pager!!.currentItem = 4
            //fragment = new PendingFragment();
        } else {
            logger.error("Unknown navigation item")
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        // Insert the fragment by replacing any existing fragment
        //FragmentManager fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        item.isChecked = true
        // Set action bar title
        title = item.title
        // Close the navigation drawer
        drawer.closeDrawers()


        drawer.closeDrawer(GravityCompat.START)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        // Check if user triggered a refresh:
            R.id.menu_refresh -> {
                // Start the refresh background task.
                // This method calls setRefreshing(false) when it's finished.
                if (pager?.currentItem == 0) {
                    logger.info("Trigger refreseh expenses")
                    // TODO: proxy to presenter of expenses
                    //financeModel.refreshExpenses()
                }
                return true
            }
        }

        // User didn't trigger a refreshExpenses, let the superclass handle this action
        return super.onOptionsItemSelected(item)

    }
}
