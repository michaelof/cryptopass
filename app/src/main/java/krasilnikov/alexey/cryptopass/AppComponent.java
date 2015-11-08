package krasilnikov.alexey.cryptopass;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Provides;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    OperationManager getOperationManager();
}
