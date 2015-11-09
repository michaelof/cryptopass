package krasilnikov.alexey.cryptopass.sync;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.text.TextUtils;
import android.util.JsonWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.inject.Inject;

import krasilnikov.alexey.cryptopass.Data;
import krasilnikov.alexey.cryptopass.Utils;
import krasilnikov.alexey.cryptopass.data.BookmarksStorage;

/**
 * Helper class for writing bookmarks to given file.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class BookmarksWriter {
    private final BookmarksStorage mBookmarksStorage;
    @Inject
    public BookmarksWriter(BookmarksStorage storage) {
        mBookmarksStorage = storage;
    }

    /**
     * Write bookmarks to given output steam. Stream will be closed after that.
     */
    public void write(FileOutputStream outputStream) throws IOException {
        Writer fileWriter = new OutputStreamWriter(outputStream);
        JsonWriter jsonWriter = new JsonWriter(fileWriter);
        try {
            writeBookmarks(jsonWriter);
        } finally {
            Utils.close(jsonWriter);
        }
    }

    private void writeBookmarks(JsonWriter writer) throws IOException {
        writer.beginArray();
        Cursor c = mBookmarksStorage.queryBookmarks(Data.BOOKMARKS_PROJECTION);
        while (c.moveToNext()) {
            String url = c.getString(Data.URL_COLUMN);
            String username = c.getString(Data.USERNAME_COLUMN);
            int length = c.getInt(Data.LENGTH_COLUMN);

            writer.beginObject();
            if (!TextUtils.isEmpty(url))
                writer.name("url").value(url);
            if (!TextUtils.isEmpty(username))
                writer.name("username").value(username);
            writer.name("length").value(length);
            writer.endObject();
        }
        writer.endArray();
    }
}
