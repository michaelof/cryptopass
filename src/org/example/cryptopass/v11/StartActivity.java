package org.example.cryptopass.v11;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import org.example.cryptopass.R;

public class StartActivity extends Activity implements BookmarksFragment.IListener {
	private static final String FIRST_START = "firstStart";
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.start);

		if (savedInstanceState == null) {
			Intent intent = getIntent();

			boolean firstStart = intent.getBooleanExtra(FIRST_START, false);

			if (firstStart) {
				getFragmentManager().beginTransaction().add(R.id.rootView, new MainFragment()).commit();
			} else {
				getFragmentManager().beginTransaction().add(R.id.rootView, new BookmarksFragment()).commit();
			}
		}
	}

	@Override
	public void noBookmarks() {
		Intent intent = new Intent(this, StartActivity.class);
		intent.putExtra(FIRST_START, true);
		startActivity(intent);
		finish();
	}
}
