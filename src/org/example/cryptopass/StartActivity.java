package org.example.cryptopass;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class StartActivity extends ListActivity implements OnItemClickListener {
	private static final int INVALID_ID = -1;

	private class BookmarksAdapter extends ResourceCursorAdapter {
		public BookmarksAdapter(Cursor c) {
			super(StartActivity.this, R.layout.row_bookmark, c, true);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView tv = (TextView) view;

			String url = cursor.getString(BookmarksHelper.URL_COLUMN);
			String username = cursor.getString(BookmarksHelper.USERNAME_COLUMN);

			String text = String.format("%s @ %s", username, url);

			tv.setText(text);
		}
	}

	private BookmarksHelper helper;
	private Cursor bookmarksCursor;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		helper = new BookmarksHelper(this);

		bookmarksCursor = helper.queryBookmarks();
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

			setListAdapter(new BookmarksAdapter(bookmarksCursor));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (INVALID_ID != id) {
			startMainBookmark(id);
		} else {
			startMainEmpty();
		}
	}

	void startMainEmpty() {
		startActivity(new Intent(StartActivity.this, MainActivity.class));
	}

	void startMainBookmark(long id) {
		Bookmark bookmark = helper.getBookmark(id);

		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(MainActivity.EXTRA_URL, bookmark.url);
		intent.putExtra(MainActivity.EXTRA_USERNAME, bookmark.username);

		startActivity(intent);
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
					startMainBookmark(menuInfo.id);

					return true;

				case R.id.delete:
					helper.deleteBookmark(menuInfo.id);

					bookmarksCursor.requery();

					return true;
			}
		}

		return super.onContextItemSelected(item);
	}
}