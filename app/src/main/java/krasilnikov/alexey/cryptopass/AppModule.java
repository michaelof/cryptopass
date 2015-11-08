package krasilnikov.alexey.cryptopass;

import android.content.Context;

import dagger.Module;

@Module
public class AppModule {
    private final Context mApplicationContext;

    public AppModule(Context context) {
        mApplicationContext = context;
    }
}
