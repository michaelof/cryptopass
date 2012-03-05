package org.example.cryptopass.v11;

import android.app.Activity;
import android.os.Bundle;
import org.example.cryptopass.R;

public class StartActivity extends Activity implements BookmarksFragment.IListener{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.start);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.rootView, new BookmarksFragment()).commit();
		}
	}

	@Override
	public void noBookmarks() {
		//getFragmentManager().beginTransaction().add(R.id.rootView, new MainFragment()).commit();
	}
}
