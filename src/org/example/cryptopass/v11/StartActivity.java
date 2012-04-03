package org.example.cryptopass.v11;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// app icon in action bar clicked; go home
                Intent intent = new Intent(this, StartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void noBookmarks() {
		Intent intent = new Intent(this, StartActivity.class);
		intent.putExtra(FIRST_START, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public void showBookmark(Bookmark bookmark) {
		final MainFragment fragment = MainFragment.instantiate(bookmark);
		
		getFragmentManager().beginTransaction().replace(R.id.rootView, fragment, MAIN_TAG).addToBackStack(null).commit();
	}
}
