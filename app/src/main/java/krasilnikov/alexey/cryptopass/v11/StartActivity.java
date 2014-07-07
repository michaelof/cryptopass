package krasilnikov.alexey.cryptopass.v11;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import krasilnikov.alexey.cryptopass.OperationManager;
import krasilnikov.alexey.cryptopass.R;

public class StartActivity extends Activity implements BookmarksFragment.IListener, OperationManager.OperationListener {
	private static final String FIRST_START = "firstStart";

	private static final String BOOKMARKS_TAG = "bookmarks";
	private static final String MAIN_TAG = "main";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.start);

		setProgressBarIndeterminateVisibility(false);

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
	protected void onResume() {
		super.onResume();

		OperationManager.getInstance().subscribe(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		OperationManager.getInstance().unsubscribe(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// app icon in action bar clicked; go home
				Intent intent = new Intent(this, StartActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				overridePendingTransition(0, 0);

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
		overridePendingTransition(0, 0);
	}

	@Override
	public void showBookmark(Uri data) {
		final MainFragment fragment = MainFragment.instantiate(data);

		getFragmentManager().beginTransaction().replace(R.id.rootView, fragment, MAIN_TAG).addToBackStack(null).commit();
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
