package krasilnikov.alexey.cryptopass.data;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.Observable;
import android.net.Uri;
import android.os.Build;

import javax.inject.Inject;
import javax.inject.Singleton;

import krasilnikov.alexey.cryptopass.Data;

/**
 * Class for accessing bookmarks stored on the disk.
 * Use SQLite internally.
 */
@Singleton
@TargetApi(Build.VERSION_CODES.FROYO)
public class BookmarksStorage extends Observable<ContentObserver> {
    private final DatabaseHelper mDbHelper;

    @Inject
    public BookmarksStorage(Context context) {
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

        dispatchChange(false, null);
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

        int count = mDbHelper.getWritableDatabase().delete(DatabaseHelper.BOOKMARKS_TABLE, whereClause, whereArgs);
        if (count > 0) {
            dispatchChange(false, null);
        }

        return count;
    }

    private void dispatchChange(boolean selfChange, Uri uri) {
        synchronized(mObservers) {
            for (ContentObserver observer : mObservers) {
                if (!selfChange || observer.deliverSelfNotifications()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        observer.dispatchChange(selfChange, uri);
                    } else {
                        observer.dispatchChange(selfChange);
                    }
                }
            }
        }
    }

}
