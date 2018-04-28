package krasilnikov.alexey.cryptopass.v14;

import android.os.Build;

import javax.inject.Provider;

import dagger.Module;
import dagger.Provides;
import krasilnikov.alexey.cryptopass.scope.ActivityScoped;
import krasilnikov.alexey.cryptopass.v21.ProgressControllerLollipop;

@Module
public class ProgressControllerModule {
    @Provides
    @ActivityScoped
    ProgressController makeProgressController(Provider<ProgressControllerSandwitch> sandwitch,
                                              Provider<ProgressControllerLollipop> lollipop) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return lollipop.get();
        }
        return sandwitch.get();
    }
}
