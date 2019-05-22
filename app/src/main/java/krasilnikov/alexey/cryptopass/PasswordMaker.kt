package krasilnikov.alexey.cryptopass

import android.util.Base64

object PasswordMaker {
    fun make(listener: IterationsListener, password: String, username: String, url: String): String? {
        val salt = "$username@$url"

        val generator = PBKDF2KeyGenerator(32, 5000, "HmacSHA256")
        val digest = generator.generateKey(listener, password, salt.toByteArray())

        return Base64.encodeToString(digest!!, Base64.DEFAULT)
    }
}
