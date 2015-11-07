package krasilnikov.alexey.cryptopass.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import krasilnikov.alexey.cryptopass.Data;

public class DataProvider extends ContentProvider {
    private BookmarksHelper mBookmarksHelper;

    @Override
    public boolean onCreate() {
        mBookmarksHelper = new BookmarksHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (uri.equals(Data.makeBookmarksUri(getContext()))) {
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
        Integer length = values.getAsInteger(Data.ARGS_LENGTH);

        mBookmarksHelper.saveBookmark(username, url, length != null ? length : Data.DEFAULT_LENGTH);

        getContext().getContentResolver().notifyChange(Data.makeBookmarksUri(getContext()), null);

        return Data.makeBookmarkUri(getContext(), username, url);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String username = Data.getUsername(uri);
        String url = Data.getUrl(uri);

        int deleted = mBookmarksHelper.deleteBookmark(username, url);

        getContext().getContentResolver().notifyChange(Data.makeBookmarksUri(getContext()), null);

        return deleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
