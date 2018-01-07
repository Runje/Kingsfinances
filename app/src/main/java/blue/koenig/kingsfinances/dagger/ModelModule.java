package blue.koenig.kingsfinances.dagger;

import android.content.Context;

import com.koenig.commonModel.User;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Singleton;

import blue.koenig.kingsfamilylibrary.model.FamilyConfig;
import blue.koenig.kingsfamilylibrary.model.communication.ServerConnection;
import blue.koenig.kingsfamilylibrary.view.family.LoginHandler;
import blue.koenig.kingsfinances.features.statistics.AssetsCalculator;
import blue.koenig.kingsfinances.features.statistics.AssetsCalculatorService;
import blue.koenig.kingsfinances.features.statistics.FinanceAssetsCalculatorService;
import blue.koenig.kingsfinances.features.statistics.StatisticsPresenter;
import blue.koenig.kingsfinances.model.FinanceModel;
import blue.koenig.kingsfinances.model.FinanceUserService;
import blue.koenig.kingsfinances.model.database.FinanceDatabase;
import dagger.Module;
import dagger.Provides;

/**
 * Created by Thomas on 19.11.2017.
 */
@Module
public class ModelModule {
    @Provides
    @Singleton
    FinanceModel provideFinanceModel(ServerConnection connection, Context context, LoginHandler handler, FinanceDatabase database, FinanceUserService service, AssetsCalculator assetsCalculator) {
        return new FinanceModel(connection, context, handler, database, service, assetsCalculator);
    }

    @Provides
    @Singleton
    FinanceDatabase provideDatabase(Context context, FinanceUserService userService) {
        try {
            return new FinanceDatabase(context, userService);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Provides
    FinanceUserService provideUserService(List<User> members) {
        return new FinanceUserService(members);
    }

    @Provides
    List<User> provideFamilyMembers(Context context) {
        return FamilyConfig.getFamilyMembers(context);
    }

    @Provides
    DateTime provideStartDate() {
        // TODO:
        return new DateTime(2015, 1, 1, 0, 0);
    }

    @Provides
    StatisticsPresenter provideStatisticsPresenter(AssetsCalculator assetsCalculator, DateTime startDate, Context context) {
        return new StatisticsPresenter(assetsCalculator, startDate, context);
    }

    @Provides
    @Singleton
    AssetsCalculator provideAssetsCalculator(FinanceDatabase database, AssetsCalculatorService service) {
        return new AssetsCalculator(Period.months(1), database.getBankAccountTable(), service);
    }

    @Provides
    @Singleton
    AssetsCalculatorService provideAssetsCalculatorService(Context context) {
        return new FinanceAssetsCalculatorService(context);
    }
}
