package blue.koenig.kingsfinances.dagger;

import android.app.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import blue.koenig.kingsfamilylibrary.FamilyApplication;
import blue.koenig.kingsfamilylibrary.dagger.AppModule;

/**
 * Created by Thomas on 17.09.2017.
 */

public class FinanceApplication extends FamilyApplication {
    private Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    public FinanceAppComponent getFinanceAppComponent() {
        return (FinanceAppComponent) familyAppComponent;
    }

    @Override
    protected void initDagger() {
        familyAppComponent = initDagger(this);
    }

    protected FinanceAppComponent initDagger(FinanceApplication application) {
        return DaggerFinanceAppComponent.builder().appModule(new AppModule(application)).build();
    }
}
