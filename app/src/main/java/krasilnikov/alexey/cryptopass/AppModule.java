package krasilnikov.alexey.cryptopass;

import android.app.Application;
import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private final Application mApplicationContext;

    public AppModule(Application context) {
        mApplicationContext = context;
    }

    @Provides
    public Context getAppContent() {
        return mApplicationContext;
    }
}
