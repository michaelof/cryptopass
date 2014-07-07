package krasilnikov.alexey.cryptopass.v11;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import krasilnikov.alexey.cryptopass.OperationManager;
import krasilnikov.alexey.cryptopass.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StartActivity extends Activity implements BookmarksFragment.IListener, OperationManager.OperationListener {
	private static final String FIRST_START = "firstStart";

	private static final String BOOKMARKS_TAG = "bookmarks";
	private static final String MAIN_TAG = "main";

    private void handleIntent(Intent intent) {
        boolean firstStart = intent.getBooleanExtra(FIRST_START, false);

        if (firstStart) {
            getFragmentManager().beginTransaction().replace(R.id.rootView, new MainFragment(), MAIN_TAG).commit();
        } else {
            getFragmentManager().beginTransaction().replace(R.id.rootView, new BookmarksFragment(), BOOKMARKS_TAG).commit();
        }

    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.start);

		setProgressBarIndeterminateVisibility(false);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

		if (savedInstanceState == null) {
			handleIntent(getIntent());
		}
	}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
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
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
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
