package krasilnikov.alexey.cryptopass.sync;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;

import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.inject.Inject;

import krasilnikov.alexey.cryptopass.Data;
import krasilnikov.alexey.cryptopass.data.BookmarksStorage;

/**
 * Helper class for writing bookmarks to given file.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class BookmarksWriter {
    private final BookmarksStorage mBookmarksStorage;
    private final BookmarksSerializer mBookmarksSerializer;

    @Inject
    public BookmarksWriter(BookmarksStorage storage, BookmarksSerializer serializer) {
        mBookmarksStorage = storage;
        mBookmarksSerializer = serializer;
    }

    /**
     * Write bookmarks to given output steam. Stream will be closed after that.
     */
    public void write(OutputStream outputStream) throws IOException, JSONException {
        try (Writer fileWriter = new OutputStreamWriter(outputStream)) {
            try (Cursor c = mBookmarksStorage.queryBookmarks(Data.BOOKMARKS_PROJECTION)) {
                fileWriter.write(mBookmarksSerializer.serialize(c));
            }
        }
    }

}
