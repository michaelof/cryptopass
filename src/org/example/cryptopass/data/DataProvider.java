package org.example.cryptopass.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import org.example.cryptopass.BookmarksHelper;
import org.example.cryptopass.Data;

public class DataProvider extends ContentProvider {
	private BookmarksHelper mBookmarksHelper;

	@Override
	public boolean onCreate() {
		mBookmarksHelper = new BookmarksHelper(getContext());

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if (uri.equals(Data.URI_BOOKMARKS)) {
			return mBookmarksHelper.queryBookmarks(projection);
		}

		return null;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String username = values.getAsString(Data.ARGS_USERNAME);
		String url = values.getAsString(Data.ARGS_URL);

		mBookmarksHelper.saveBookmark(username, url);

		getContext().getContentResolver().notifyChange(Data.URI_BOOKMARKS, null);

		return Data.makeBookmarkUri(username, url);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String username = Data.getUsername(uri);
		String url = Data.getUrl(uri);

		int deleted = mBookmarksHelper.deleteBookmark(username, url);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		getContext().getContentResolver().notifyChange(Data.URI_BOOKMARKS, null);

		return deleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
}
