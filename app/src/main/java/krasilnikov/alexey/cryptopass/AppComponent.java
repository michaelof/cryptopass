package krasilnikov.alexey.cryptopass;

import javax.inject.Singleton;

import dagger.Component;
import krasilnikov.alexey.cryptopass.data.BookmarksStorage;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    ProgressNotifier getProgressNotifier();

    BookmarksStorage getBookmarksStorage();
}
