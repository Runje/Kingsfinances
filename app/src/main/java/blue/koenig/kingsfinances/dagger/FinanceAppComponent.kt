package blue.koenig.kingsfinances.dagger

import blue.koenig.kingsfamilylibrary.dagger.AppModule
import blue.koenig.kingsfamilylibrary.dagger.ConnectionModule
import blue.koenig.kingsfamilylibrary.dagger.FamilyAppComponent
import blue.koenig.kingsfamilylibrary.dagger.InstallationModule
import blue.koenig.kingsfinances.OverviewActivity
import blue.koenig.kingsfinances.view.BookkeepingItemActivity
import blue.koenig.kingsfinances.view.fragments.CategoryStatisticsFragment
import blue.koenig.kingsfinances.view.fragments.ExpensesFragment
import blue.koenig.kingsfinances.view.fragments.FinanceFragment
import blue.koenig.kingsfinances.view.fragments.StatisticsFragment
import dagger.Component
import javax.inject.Singleton

/**
 * Created by Thomas on 19.10.2017.
 */
@Singleton
@Component(modules = arrayOf(AppModule::class, ConnectionModule::class, InstallationModule::class, FinanceModelModule::class))
interface FinanceAppComponent : FamilyAppComponent {
    fun inject(target: OverviewActivity)

    fun inject(expensesFragment: FinanceFragment)

    fun inject(bookkeepingItemActivity: BookkeepingItemActivity)

    fun inject(statisticsFragment: StatisticsFragment)

    fun inject(categoryStatisticsFragment: CategoryStatisticsFragment)

    fun inject(expensesFragment: ExpensesFragment)
}
