package krasilnikov.alexey.cryptopass.v14;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import javax.inject.Inject;
import javax.inject.Provider;

import krasilnikov.alexey.cryptopass.ActionService;
import krasilnikov.alexey.cryptopass.BookmarksAdapter;
import krasilnikov.alexey.cryptopass.Data;
import krasilnikov.alexey.cryptopass.R;
import krasilnikov.alexey.cryptopass.Version;
import krasilnikov.alexey.cryptopass.data.BookmarksStorage;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class BookmarksFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int INVALID_ID = -1;

    public interface IListener {
        void noBookmarks();

        void showBookmark(Uri data);
    }

    @Inject
    public Provider<BookmarkCursorLoader> mBookmarksLoaderProvider;

    private BookmarksAdapter mBookmarksAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBookmarksAdapter = new BookmarksAdapter(getActivity(), null);

        getLoaderManager().initLoader(Loaders.BOOKMARKS_LOADER, null, this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        final ListView listView = (ListView) view.findViewById(android.R.id.list);

        final View headerView = inflater.inflate(R.layout.row_empty, listView, false);

        listView.addHeaderView(headerView);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ListView listView = getListView();

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(mMultiChoiceModeListener);

        setListAdapter(mBookmarksAdapter);
        setListShownNoAnimation(false);
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
        actionBar.setHomeButtonEnabled(false);
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
            final SparseBooleanArray array;

            switch (menuItem.getItemId()) {
                case R.id.open:
                    array = getListView().getCheckedItemPositions();

                    final int listPosition = array.keyAt(array.indexOfValue(true));

                    showBookmark(listPosition);

                    actionMode.finish();

                    return true;

                case R.id.delete:
                    array = getListView().getCheckedItemPositions();

                    for (int i = 0; i < array.size(); i++) {
                        if (array.valueAt(i)) {
                            deleteBookmark(array.keyAt(i));
                        }
                    }

                    actionMode.finish();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
        }
    };

    private void deleteBookmark(final int listPosition) {
        Cursor cursor = mBookmarksAdapter.getCursor();

        if (listPosition > 0) {
            Uri uri = BookmarksStorage.getBookmarkUri(getActivity(), cursor, listPosition - 1);
            Intent intent = new Intent(Data.ACTION_DELETE, uri);
            intent.setClass(getActivity(), ActionService.class);

            getActivity().startService(intent);
        }
    }

    private void showBookmark(final int listPosition) {
        Cursor cursor = mBookmarksAdapter.getCursor();

        if (listPosition > 0) {
            Uri data = BookmarksStorage.getBookmarkUri(getActivity(), cursor, listPosition - 1);

            getListener().showBookmark(data);
        } else {

            getListener().showBookmark(null);
        }
    }

    public void onListItemClick(final ListView listView, final View rowView, final int position, final long id) {
        showBookmark(position);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return mBookmarksLoaderProvider.get();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor.getCount() == 0) {
            getListener().noBookmarks();
        } else {
            mBookmarksAdapter.swapCursor(cursor);
            setListShownNoAnimation(true);
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
