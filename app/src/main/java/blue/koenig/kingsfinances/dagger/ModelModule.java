package blue.koenig.kingsfinances.dagger;

import android.content.Context;

import javax.inject.Singleton;

import blue.koenig.kingsfamilylibrary.model.communication.ServerConnection;
import blue.koenig.kingsfamilylibrary.view.family.LoginHandler;
import blue.koenig.kingsfinances.model.FinanceModel;
import dagger.Module;
import dagger.Provides;

/**
 * Created by Thomas on 19.11.2017.
 */
@Module
public class ModelModule {
    @Provides
    @Singleton
    FinanceModel provideFinanceModel(ServerConnection connection, Context context, LoginHandler handler) {
        return new FinanceModel(connection, context, handler);
    }
}
