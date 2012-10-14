package org.example.cryptopass;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class BookmarksHelper {
	private final DatabaseHelper dbHelper;

	public BookmarksHelper(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public Cursor queryBookmarks(String[] columns) {
		return dbHelper.getReadableDatabase().query(DatabaseHelper.BOOKMARKS_TABLE, columns, null, null, null, null, "_ID DESC");
	}

	public void saveBookmark(String username, String url, int length) {
		ContentValues values = new ContentValues();

		values.put(Data.BOOKMARKS_USERNAME, username != null ? username : "");
		values.put(Data.BOOKMARKS_URL, url != null ? url : "");
		values.put(Data.BOOKMARKS_LENGTH, length);

		dbHelper.getWritableDatabase().insert(DatabaseHelper.BOOKMARKS_TABLE, null, values);
	}

	public static Uri getBookmarkUri(final Cursor c, final int position) {
		if (c.moveToPosition(position)) {
			return Data.makeBookmarkUri(c.getString(Data.USERNAME_COLUMN), c.getString(Data.URL_COLUMN), c.getInt(Data.LENGTH_COLUMN));
		}

		return null;
	}

	public int deleteBookmark(String username, String url) {
		assert username != null;
		assert url != null;

		String whereClause = Data.BOOKMARKS_USERNAME + " = ? AND " + Data.BOOKMARKS_URL + " = ?";
		String[] whereArgs = new String[]{username, url};

		return dbHelper.getWritableDatabase().delete(DatabaseHelper.BOOKMARKS_TABLE, whereClause, whereArgs);
	}

}
