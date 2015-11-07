package krasilnikov.alexey.cryptopass.v14;

import krasilnikov.alexey.cryptopass.Bookmark;

public interface ResultHandler {
    void exception(Exception occurredException);

    void complete(Bookmark args, String result);

    void empty();
}
