package blue.koenig.kingsfinances.dagger;

import javax.inject.Singleton;
import blue.koenig.kingsfamilylibrary.dagger.FamilyAppComponent;
import blue.koenig.kingsfamilylibrary.dagger.AppModule;
import blue.koenig.kingsfamilylibrary.dagger.ConnectionModule;
import blue.koenig.kingsfamilylibrary.dagger.InstallationModule;
import blue.koenig.kingsfinances.OverviewActivity;
import blue.koenig.kingsfinances.view.ExpensesFragment;
import dagger.Component;

/**
 * Created by Thomas on 19.10.2017.
 */
@Singleton
@Component(modules = {AppModule.class, ConnectionModule.class, InstallationModule.class, ModelModule.class})
public interface FinanceAppComponent extends FamilyAppComponent{
    void inject(OverviewActivity target);

    void inject(ExpensesFragment expensesFragment);
}
