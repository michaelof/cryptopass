package org.example.cryptopass.v11;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import org.example.cryptopass.Bookmark;
import org.example.cryptopass.R;

public class StartActivity extends Activity implements BookmarksFragment.IListener {
	private static final String FIRST_START = "firstStart";
	
	private static final String BOOKMARKS_TAG = "bookmarks";
	private static final String MAIN_TAG = "main";
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.start);

		if (savedInstanceState == null) {
			Intent intent = getIntent();

			boolean firstStart = intent.getBooleanExtra(FIRST_START, false);

			if (firstStart) {
				getFragmentManager().beginTransaction().add(R.id.rootView, new MainFragment(), MAIN_TAG).commit();
			} else {
				getFragmentManager().beginTransaction().add(R.id.rootView, new BookmarksFragment(), BOOKMARKS_TAG).commit();
			}
		}
	}

	void showHome() {
		final FragmentManager fragmentManager = getFragmentManager();
		Fragment bookmarksFragment = fragmentManager.findFragmentByTag(BOOKMARKS_TAG);
		if (bookmarksFragment == null) {
			fragmentManager.beginTransaction().replace(R.id.rootView, new BookmarksFragment(), BOOKMARKS_TAG).commit();
		} else {
			fragmentManager.popBackStack();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// app icon in action bar clicked; go home
				showHome();

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void noBookmarks() {
		Intent intent = new Intent(this, StartActivity.class);
		intent.putExtra(FIRST_START, true);
		startActivity(intent);
		finish();
	}

	@Override
	public void showBookmark(Bookmark bookmark) {
		final MainFragment fragment = MainFragment.instantiate(bookmark);
		
		getFragmentManager().beginTransaction().add(R.id.rootView, fragment, MAIN_TAG).addToBackStack(null).commit();
	}
}
