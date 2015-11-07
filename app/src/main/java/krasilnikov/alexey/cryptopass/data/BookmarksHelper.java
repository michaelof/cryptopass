package krasilnikov.alexey.cryptopass.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import krasilnikov.alexey.cryptopass.Data;

public class BookmarksHelper {
    private final DatabaseHelper mDbHelper;

    public BookmarksHelper(Context context) {
        mDbHelper = new DatabaseHelper(context);
    }

    public Cursor queryBookmarks(String[] columns) {
        return mDbHelper.getReadableDatabase().query(DatabaseHelper.BOOKMARKS_TABLE, columns, null, null, null, null, "_ID DESC");
    }

    public void saveBookmark(String username, String url, int length) {
        ContentValues values = new ContentValues();

        values.put(Data.BOOKMARKS_USERNAME, username != null ? username : "");
        values.put(Data.BOOKMARKS_URL, url != null ? url : "");
        values.put(Data.BOOKMARKS_LENGTH, length);

        mDbHelper.getWritableDatabase().insert(DatabaseHelper.BOOKMARKS_TABLE, null, values);
    }

    public static Uri getBookmarkUri(Context context, final Cursor c, final int position) {
        if (c.moveToPosition(position)) {
            String username = c.getString(Data.USERNAME_COLUMN);
            String url = c.getString(Data.URL_COLUMN);
            int length = c.getInt(Data.LENGTH_COLUMN);
            return Data.makeBookmarkUri(context, username, url, length);
        }

        return null;
    }

    public int deleteBookmark(String username, String url) {
        assert username != null;
        assert url != null;

        String whereClause = Data.BOOKMARKS_USERNAME + " = ? AND " + Data.BOOKMARKS_URL + " = ?";
        String[] whereArgs = new String[]{username, url};

        return mDbHelper.getWritableDatabase().delete(DatabaseHelper.BOOKMARKS_TABLE, whereClause, whereArgs);
    }

}
