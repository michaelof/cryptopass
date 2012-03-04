package org.example.cryptopass;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class BookmarksHelper
{
	private final DatabaseHelper dbHelper;

	public BookmarksHelper(Context context)
	{
		dbHelper = new DatabaseHelper(context);
	}

	public Cursor queryBookmarks()
	{
		String[] columns = new String[] { DatabaseHelper.BOOKMARKS_ID, DatabaseHelper.BOOKMARKS_USERNAME, DatabaseHelper.BOOKMARKS_URL };

		return dbHelper.getReadableDatabase().query(DatabaseHelper.BOOKMARKS_TABLE, columns, null, null, null, null, null);
	}

	public static void saveBookmark(Context context, String username, String url)
	{
		assert url != null;
		assert username != null;
		assert context != null;
		
		DatabaseHelper dbHelper = new DatabaseHelper(context);

		ContentValues values = new ContentValues();

		values.put(DatabaseHelper.BOOKMARKS_USERNAME, username);
		values.put(DatabaseHelper.BOOKMARKS_URL, url);

		dbHelper.getWritableDatabase().insert(DatabaseHelper.BOOKMARKS_TABLE, null, values);
	}

	public Bookmark getBookmark(long id)
	{
		String[] columns = new String[] { DatabaseHelper.BOOKMARKS_USERNAME, DatabaseHelper.BOOKMARKS_URL };
		String selection = DatabaseHelper.BOOKMARKS_ID + " = ?";
		String[] selectionArgs = new String[] { Long.toString(id) };
		
		Cursor c = dbHelper.getReadableDatabase()
				.query(DatabaseHelper.BOOKMARKS_TABLE, columns, selection, selectionArgs, null, null, null);
		try
		{
			if (c.moveToFirst())
			{
				return new Bookmark(c.getString(1), c.getString(0));
			}

			return null;
		}
		finally
		{
			c.close();
		}
	}
	
	public void deleteBookmark(long id)
	{
		String whereClause = DatabaseHelper.BOOKMARKS_ID + " = ?";
		String[] whereArgs = new String[] { Long.toString(id) };

		dbHelper.getWritableDatabase().delete(DatabaseHelper.BOOKMARKS_TABLE, whereClause, whereArgs);		
	}

	public static final int ID_COLUMN = 0;
	public static final int USERNAME_COLUMN = 1;
	public static final int URL_COLUMN = 2;

}
