package krasilnikov.alexey.cryptopass.v8;

import krasilnikov.alexey.cryptopass.Bookmark;
import krasilnikov.alexey.cryptopass.PBKDF2Args;

public interface ResultListener {
    void exception(Exception occuredException);

    void complete(Bookmark args, String result);

    void restart(PBKDF2Args args);

    void empty();
}
