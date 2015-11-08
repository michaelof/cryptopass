package krasilnikov.alexey.cryptopass.scope;

import android.app.Activity;
import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {
    private final Activity mActivity;

    public ActivityModule(Activity activity) {
        mActivity = activity;
    }

    @Provides
    public Activity getAcitivity() {
        return mActivity;
    }

    @Provides
    public Context getContext() {
        return mActivity;
    }
}
