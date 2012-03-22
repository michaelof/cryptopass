package org.example.cryptopass.v11;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import org.example.cryptopass.*;

public class BookmarksFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	public interface IListener {
		void noBookmarks();

		void showBookmark(Bookmark bookmark);
	}

	private BookmarksAdapter mBookmarksAdapter;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = super.onCreateView(inflater, container, savedInstanceState);

		final ListView listView = (ListView) view.findViewById(android.R.id.list);

		final View headerView = inflater.inflate(R.layout.row_empty, listView, false);

		listView.addHeaderView(headerView);

		return view;
	}

    @Override
    public void onStart() {
        super.onStart();

        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
    }

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mBookmarksAdapter = new BookmarksAdapter(getActivity(), null);

		getLoaderManager().initLoader(Loaders.BOOKMARKS_LOADER, null, this);

		setListAdapter(mBookmarksAdapter);
		setListShown(false);
	}

	public void onListItemClick(final ListView listView, final View rowView, final int position, final long id) {
		Cursor cursor = mBookmarksAdapter.getCursor();
		
		Bookmark bookmark = null;
        if (position > 0) {
            bookmark = BookmarksHelper.getBookmark(cursor, position - 1);
        }

		getListener().showBookmark(bookmark);

		super.onListItemClick(listView, rowView, position, id);
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
