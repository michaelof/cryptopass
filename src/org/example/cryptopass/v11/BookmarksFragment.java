package org.example.cryptopass.v11;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import org.example.cryptopass.BookmarksAdapter;
import org.example.cryptopass.BookmarksHelper;
import org.example.cryptopass.SimpleCursorLoader;

public class BookmarksFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	public interface IListener {
		void noBookmarks();
	}

	private static int BOOKMARKS_LOADER = 1;

	private BookmarksAdapter mBookmarksAdapter;

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mBookmarksAdapter = new BookmarksAdapter(getActivity(), null);
		setListAdapter(mBookmarksAdapter);
		setListShown(false);

		getLoaderManager().initLoader(BOOKMARKS_LOADER, null, this);
	}

	static class BookmarkLoader extends SimpleCursorLoader {

		public BookmarkLoader(Context context) {
			super(context);
		}

		@Override
		public Cursor loadInBackground() {
			BookmarksHelper helper = new BookmarksHelper(getContext());

			return helper.queryBookmarks();
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return new BookmarkLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		if (cursor.getCount() == 0) {
			getListener().noBookmarks();
		} else {
			mBookmarksAdapter.swapCursor(cursor);
			setListShown(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		mBookmarksAdapter.swapCursor(null);
	}

	IListener getListener() {
		return (IListener) getActivity();
	}
}
