package org.example.cryptopass.v8;

import android.app.ListActivity;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import org.example.cryptopass.*;

public class StartActivity extends ListActivity implements OnItemClickListener, OperationManager.OperationListener {
	private static final int INVALID_ID = -1;

	private Cursor bookmarksCursor;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setProgressBarIndeterminateVisibility(false);

		bookmarksCursor = getContentResolver().query(Data.URI_BOOKMARKS, Data.BOOKMARKS_PROJECTION, null, null, null);
		startManagingCursor(bookmarksCursor);

		if (bookmarksCursor.getCount() == 0) {
			startActivity(new Intent(this, MainActivity.class));
			finish();
		} else {
			ListView listView = getListView();

			TextView headerView = (TextView) getLayoutInflater().inflate(R.layout.row_empty, listView, false);

			listView.addHeaderView(headerView);
			listView.setOnItemClickListener(this);
			registerForContextMenu(listView);

			setListAdapter(new BookmarksAdapter(this, bookmarksCursor));
		}
	}

	private final ContentObserver mBookmarksObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			if (bookmarksCursor != null && !bookmarksCursor.isClosed()) {
				bookmarksCursor.requery();
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		OperationManager.getInstance().subscribe(this);
		getContentResolver().registerContentObserver(Data.URI_BOOKMARKS, false, mBookmarksObserver);
	}

	@Override
	protected void onPause() {
		super.onPause();

		getContentResolver().unregisterContentObserver(mBookmarksObserver);
		OperationManager.getInstance().unsubscribe(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		bookmarksCursor = null;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (INVALID_ID != id) {
			startMainBookmark(position);
		} else {
			startMainEmpty();
		}
	}

	void startMainEmpty() {
		startActivity(new Intent(StartActivity.this, MainActivity.class));
	}


	void startMainBookmark(final int listPosition) {
		if (listPosition > 0) {
			Intent intent = new Intent(Data.ACTION_SHOW, BookmarksHelper.getBookmarkUri(bookmarksCursor, listPosition - 1));

			startActivity(intent);
		}
	}

	private void deleteBookmark(final int listPosition) {
		if (listPosition > 0) {
			Intent intent = new Intent(Data.ACTION_DELETE, BookmarksHelper.getBookmarkUri(bookmarksCursor, listPosition - 1));

			startService(intent);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v == getListView()) {
			AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;

			if (INVALID_ID != adapterMenuInfo.id) {
				getMenuInflater().inflate(R.menu.context, menu);

				TextView tv = (TextView) adapterMenuInfo.targetView;

				menu.setHeaderTitle(tv.getText());
			}
		}
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();

		if (INVALID_ID != menuInfo.id) {
			switch (item.getItemId()) {
				case R.id.open:
					startMainBookmark(menuInfo.position);

					return true;

				case R.id.delete:
					deleteBookmark(menuInfo.position);

					return true;
			}
		}

		return super.onContextItemSelected(item);
	}

	private final Handler mHandler = new Handler();

	private final Runnable mUpdateProgressRunnable = new Runnable() {
		@Override
		public void run() {
			setProgressBarIndeterminateVisibility(OperationManager.getInstance().isInOperation());
		}
	};

	@Override
	public void onOperationStarted(Uri uri) {
		mHandler.removeCallbacks(mUpdateProgressRunnable);
		mHandler.post(mUpdateProgressRunnable);
	}

	@Override
	public void onOperationEnded(Uri uri) {
		mHandler.removeCallbacks(mUpdateProgressRunnable);
		mHandler.post(mUpdateProgressRunnable);
	}
}