package krasilnikov.alexey.cryptopass.sync

import android.annotation.TargetApi
import android.database.Cursor
import android.os.Build
import android.text.TextUtils
import krasilnikov.alexey.cryptopass.Data
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

/**
 * Helper class for bookmarks serialization.
 */
@TargetApi(Build.VERSION_CODES.FROYO)
class BookmarksSerializer @Inject
constructor() {

    @Throws(JSONException::class)
    fun serialize(c: Cursor): String {
        c.moveToPosition(-1)
        if (!c.isBeforeFirst)
            throw RuntimeException("Unable to rewind the cursor")

        val array = JSONArray()

        while (c.moveToNext()) {
            val url = c.getString(Data.URL_COLUMN)
            val username = c.getString(Data.USERNAME_COLUMN)
            val length = c.getInt(Data.LENGTH_COLUMN)

            val obj = JSONObject()

            if (!TextUtils.isEmpty(url))
                obj.put("url", url)
            if (!TextUtils.isEmpty(username))
                obj.put("username", username)
            obj.put("length", length)
            array.put(obj)
        }

        return array.toString(2)
    }
}
