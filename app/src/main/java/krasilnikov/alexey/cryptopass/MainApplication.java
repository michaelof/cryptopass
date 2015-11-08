package krasilnikov.alexey.cryptopass;

import android.app.Application;
import android.content.Context;

public class MainApplication extends Application {
    private AppComponent mAppComponent;

    public AppComponent getAppComponent() {
        if (mAppComponent == null) {
            mAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        }
        return mAppComponent;
    }

    public static AppComponent getComponent(Context context) {
        MainApplication application = (MainApplication) context.getApplicationContext();

        return application.getAppComponent();
    }
}
