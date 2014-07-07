package krasilnikov.alexey.cryptopass;

import android.net.Uri;
import android.text.TextUtils;

public class Data {
	public static final Uri URI_BOOKMARKS = Uri.parse("content://krasilnikov.alexey.cryptopass/bookmarks");

	public static Uri makeBookmarkUri(String username, String url) {
		return URI_BOOKMARKS.buildUpon().
				appendQueryParameter("username", username).
				appendQueryParameter("url", url).build();
	}

	public static Uri makeBookmarkUri(String username, String url, int length) {
		return URI_BOOKMARKS.buildUpon().
				appendQueryParameter("username", username).
				appendQueryParameter("url", url).
				appendQueryParameter("length", String.valueOf(length)).build();
	}

	public static String getUsername(Uri uri) {
		String username = uri.getQueryParameter("username");

		return username != null ? username : "";
	}

	public static String getUrl(Uri uri) {
		String url = uri.getQueryParameter("url");

		return url != null ? url : "";
	}

	public static int getLength(Uri uri) {
		try {
			String url = uri.getQueryParameter("length");

			if (TextUtils.isEmpty(url)) {
				return DEFAULT_LENGTH;
			}
			return Integer.parseInt(url);
		} catch (NumberFormatException ignored) {
			return DEFAULT_LENGTH;
		}
	}

	public static final int DEFAULT_LENGTH = 25;

	public static final String ACTION_SAVE = "krasilnikov.alexey.cryptopass.SAVE";
	public static final String ACTION_DELETE = "krasilnikov.alexey.cryptopass.DELETE";
	public static final String ACTION_SHOW = "krasilnikov.alexey.cryptopass.SHOW";

	public static final String ARGS_URL = "url";
	public static final String ARGS_USERNAME = "username";
	public static final String ARGS_LENGTH = "length";

	public static final String BOOKMARKS_ID = "_id";
	public static final String BOOKMARKS_USERNAME = "username";
	public static final String BOOKMARKS_URL = "url";
	public static final String BOOKMARKS_LENGTH = "length";

	public static final String[] BOOKMARKS_PROJECTION = {BOOKMARKS_ID, BOOKMARKS_USERNAME, BOOKMARKS_URL, BOOKMARKS_LENGTH};

	public static final int ID_COLUMN = 0;
	public static final int USERNAME_COLUMN = 1;
	public static final int URL_COLUMN = 2;
	public static final int LENGTH_COLUMN = 3;
}
