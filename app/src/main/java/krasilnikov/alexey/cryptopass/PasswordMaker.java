package krasilnikov.alexey.cryptopass;

import android.util.Base64;

public final class PasswordMaker {
    public static String make(IterationsListener listener, String password, String username, String url) throws Exception {
        String salt = username + "@" + url;

        PBKDF2KeyGenerator generator = new PBKDF2KeyGenerator(32, 5000, "HmacSHA256");
        byte[] digest = generator.generateKey(listener, password, salt.getBytes("UTF-8"));

        if (digest != null) {
            return Base64.encodeToString(digest, Base64.DEFAULT);
        }

        return null;
    }
}
