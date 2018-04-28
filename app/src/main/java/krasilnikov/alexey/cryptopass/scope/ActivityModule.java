package krasilnikov.alexey.cryptopass.scope;

import android.app.Activity;
import android.content.Context;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class ActivityModule {
    @Binds
    public abstract Context bindContext(Activity startActivity);
}
