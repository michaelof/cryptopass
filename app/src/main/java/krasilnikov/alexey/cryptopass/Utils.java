package krasilnikov.alexey.cryptopass;

import java.io.Closeable;
import java.io.IOException;

public class Utils {
    public static void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }
}
