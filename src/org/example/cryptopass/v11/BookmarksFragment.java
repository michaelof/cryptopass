package org.example.cryptopass.v11;

import android.app.ActionBar;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.AbsListView;
import android.widget.ListView;
import org.example.cryptopass.*;

public class BookmarksFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int INVALID_ID = -1;
    
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

        if (Version.isHoneycomb()) {
            setRetainInstance(true);
        }

        getLoaderManager().getLoader(Loaders.BOOKMARKS_LOADER).onContentChanged();

        final ActionBar actionBar = getActivity().getActionBar();

        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mBookmarksAdapter = new BookmarksAdapter(getActivity(), null);

        getLoaderManager().initLoader(Loaders.BOOKMARKS_LOADER, null, this);

        final ListView listView = getListView();

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(mMultiChoiceModeListener);

        setListAdapter(mBookmarksAdapter);
        setListShown(false);
    }

    private final AbsListView.MultiChoiceModeListener mMultiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(final ActionMode actionMode, final int position, final long id, final boolean checked) {
            if (INVALID_ID == id && checked) {
                //prevent header item selection
                getListView().setItemChecked(0, false);
            } else {
                actionMode.invalidate();
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            final MenuInflater menuInflater = actionMode.getMenuInflater();

            menuInflater.inflate(R.menu.context, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            final int count = getListView().getCheckedItemCount();

            actionMode.getMenu().findItem(R.id.open).setEnabled(count == 1);

            actionMode.setTitle(getString(R.string.selected, count));

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.open:
                    final SparseBooleanArray array = getListView().getCheckedItemPositions();

                    final int listPosition = array.keyAt(array.indexOfValue(true));

                    showBookmark(listPosition);
                    
                    actionMode.finish();
                    
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
        }
    };
    
    private void showBookmark(final int listPosition) {
        Cursor cursor = mBookmarksAdapter.getCursor();

        Bookmark bookmark = null;
        if (listPosition > 0) {
            bookmark = BookmarksHelper.getBookmark(cursor, listPosition - 1);
        }

        getListener().showBookmark(bookmark);
    }

    public void onListItemClick(final ListView listView, final View rowView, final int position, final long id) {
        showBookmark(position);
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
