package krasilnikov.alexey.cryptopass.v14;

import android.os.Build;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import krasilnikov.alexey.cryptopass.scope.ActivityScoped;
import krasilnikov.alexey.cryptopass.v21.ProgressControllerLollipop;

@Module
public class ProgressControllerModule {
    @Provides
    @ActivityScoped
    ProgressController makeProgressController(Lazy<ProgressControllerSandwitch> sandwitchLazy,
                                              Lazy<ProgressControllerLollipop> lollipopLazy) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return lollipopLazy.get();
        }
        return sandwitchLazy.get();
    }
}
