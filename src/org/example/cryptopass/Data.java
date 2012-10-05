package org.example.cryptopass;

import android.net.Uri;

public class Data {
	public static final Uri URI_BOOKMARKS = Uri.parse("content://org.example.cryptopass/bookmarks");

	public static Uri makeBookmarkUri(String username, String url) {
		return URI_BOOKMARKS.buildUpon().appendQueryParameter("username", username).appendQueryParameter("url", url).build();
	}

	public static Uri makeBookmarkUri(Bookmark bookmark) {
		return makeBookmarkUri(bookmark.username, bookmark.url);
	}

	public static String getUsername(Uri uri) {
		String username = uri.getQueryParameter("username");

		return username != null ? username : "";
	}

	public static String getUrl(Uri uri) {
		String url = uri.getQueryParameter("url");

		return url != null ? url : "";
	}

	public static final String ACTION_SAVE = "org.example.cryptopass.SAVE";
	public static final String ACTION_DELETE = "org.example.cryptopass.DELETE";
	public static final String ACTION_SHOW = "org.example.cryptopass.SHOW";

	public static final String ARGS_URL = "url";
	public static final String ARGS_USERNAME = "username";

	public static final String BOOKMARKS_ID = "_id";
	public static final String BOOKMARKS_USERNAME = "username";
	public static final String BOOKMARKS_URL = "url";

	public static final String[] BOOKMARKS_PROJECTION = {BOOKMARKS_ID, BOOKMARKS_USERNAME, BOOKMARKS_URL};

	public static final int ID_COLUMN = 0;
	public static final int USERNAME_COLUMN = 1;
	public static final int URL_COLUMN = 2;
}
