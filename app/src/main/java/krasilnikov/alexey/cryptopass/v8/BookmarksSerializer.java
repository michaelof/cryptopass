package krasilnikov.alexey.cryptopass.v8;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import krasilnikov.alexey.cryptopass.Data;

/**
 * Helper class for bookmarks serialization.
 * Designed for android 2.2, newer OS version uses another.
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public class BookmarksSerializer {
    @Inject
    public BookmarksSerializer() {
    }

    public String serialize(Cursor c) throws JSONException {
        c.moveToPosition(-1);
        if (!c.isBeforeFirst())
            throw new RuntimeException("Unable to rewind the cursor");

        JSONArray array = new JSONArray();

        while (c.moveToNext()) {
            String url = c.getString(Data.URL_COLUMN);
            String username = c.getString(Data.USERNAME_COLUMN);
            int length = c.getInt(Data.LENGTH_COLUMN);

            JSONObject obj = new JSONObject();

            if (!TextUtils.isEmpty(url))
                obj.put("url", url);
            if (!TextUtils.isEmpty(username))
                obj.put("username", username);
            obj.put("length", length);
            array.put(obj);
        }

        return array.toString(2);
    }
}
