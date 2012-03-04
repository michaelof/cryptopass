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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class StartActivity extends ListActivity implements OnItemClickListener
{
	private static class BookmarksAdapter extends ResourceCursorAdapter
	{
		public BookmarksAdapter(Context context, Cursor c)
		{
			super(context, R.layout.bookmark, c, true);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			TextView tv = (TextView) view;

			String url = cursor.getString(BookmarksHelper.URL_COLUMN);
			String username = cursor.getString(BookmarksHelper.USERNAME_COLUMN);

			String text = String.format("%s @ %s", username, url);

			tv.setText(text);
		}
	}

	private BookmarksHelper helper;
	private Cursor bookmarksCursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		helper = new BookmarksHelper(this);

		bookmarksCursor = helper.queryBookmarks();
		startManagingCursor(bookmarksCursor);

		if (bookmarksCursor.getCount() == 0)
		{
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}
		else
		{
			setContentView(R.layout.start);

			ListView listView = getListView();

			listView.setOnItemClickListener(this);
			registerForContextMenu(listView);

			setListAdapter(new BookmarksAdapter(this, bookmarksCursor));

			Button newButton = (Button) findViewById(R.id.newButton);
			newButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0)
				{
					startMainEmpty();
				}
			});
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		startMainBookmark(id);
	}

	void startMainEmpty()
	{
		startActivity(new Intent(StartActivity.this, MainActivity.class));
	}

	void startMainBookmark(long id)
	{
		Bookmark bookmark = helper.getBookmark(id);
		
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(MainActivity.EXTRA_URL, bookmark.url);
		intent.putExtra(MainActivity.EXTRA_USERNAME, bookmark.username);

		startActivity(intent);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v == getListView())
		{
			AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;

			getMenuInflater().inflate(R.menu.context, menu);

			TextView tv = (TextView) adapterMenuInfo.targetView;

			menu.setHeaderTitle(tv.getText());
		}
	}
	

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId())
		{
		case R.id.open:
			startMainBookmark(menuInfo.id);

			return true;
			
		case R.id.delete:
			helper.deleteBookmark(menuInfo.id);
			
			bookmarksCursor.requery();
			
			return true;
		}

		return super.onContextItemSelected(item);
	}
}