package org.example.cryptopass.v8;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.*;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import org.example.cryptopass.Bookmark;
import org.example.cryptopass.Data;
import org.example.cryptopass.PBKDF2Args;
import org.example.cryptopass.R;

public final class MainActivity extends Activity implements TextWatcher {
	private Loader activeLoader;

	void initLoader() {
		Object obj = getLastNonConfigurationInstance();
		if (obj instanceof Loader) {
			activeLoader = (Loader) obj;
		}

		if (activeLoader == null) {
			activeLoader = new Loader();
		}

		activeLoader.onInit(this);
	}

	private Button resultButton;
	private EditText secretEdit;
	private EditText usernameEdit;
	private EditText urlEdit;

	private TextAppearanceSpan subTitleAppearance;

	private boolean userInput = true;
	private boolean userInteractive = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

		setContentView(R.layout.main);

		subTitleAppearance = new TextAppearanceSpan(this, android.R.style.TextAppearance_Small);

		secretEdit = (EditText) findViewById(R.id.secretEdit);
		usernameEdit = (EditText) findViewById(R.id.usernameEdit);
		urlEdit = (EditText) findViewById(R.id.urlEdit);

		resultButton = (Button) findViewById(R.id.passBtn);

		Intent startIntent = getIntent();

		Uri data = startIntent.getData();
		if (data != null) {
			usernameEdit.setText(Data.getUsername(data));
			urlEdit.setText(Data.getUrl(data));
		}

		if (urlEdit.getText().length() == 0) {
			urlEdit.requestFocus();
		}

		secretEdit.addTextChangedListener(this);
		usernameEdit.addTextChangedListener(this);
		urlEdit.addTextChangedListener(this);

		resultButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				resultButtonClicked();
			}
		});

		initLoader();
	}

	void resultButtonClicked() {
		Bookmark bookmark = activeLoader.lastBookmark();

		if (activeResult != null) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(activeResult);

			Intent saveIntent = new Intent(Data.ACTION_SAVE, Data.URI_BOOKMARKS);

			saveIntent.putExtra(Data.ARGS_URL, bookmark.url);
			saveIntent.putExtra(Data.ARGS_USERNAME, bookmark.username);

			startService(saveIntent);
		}
	}

	protected void onDestroy() {
		super.onDestroy();

		activeLoader.onActivityDestroy();
	}

	protected void onPause() {
		userInteractive = false;

		activeLoader.onActivityPause();

		super.onPause();
	}

	protected void onResume() {
		super.onResume();

		activeLoader.onActivityResume();

		userInteractive = true;
	}

	public Object onRetainNonConfigurationInstance() {
		return activeLoader.onActivityGetRetainState();
	}

	void updateResult(PBKDF2Args args) {
		activeLoader.restart(args);
	}

	public void working() {
		resultButtonWorking();
	}

	public void complete(String result) {
		resultButtonResult(result);
	}

	public void emptySecret() {
		userInput = false;
		try {
			secretEdit.setText(null);
			resultButtonEmpty();
		} finally {
			userInput = true;
		}
	}

	public void restoreSecret(String secret, String result) {
		userInput = false;
		try {
			secretEdit.setText(secret);
			resultButtonResult(result);
		} finally {
			userInput = true;
		}
	}

	public void empty() {
		resultButtonEmpty();
	}

	public void exception(Exception ex) {
		resultButtonError(ex.getMessage());
	}

	void resultButtonWorking() {
		activeResult = null;
		resultButton.setText(R.string.working);
		resultButton.setEnabled(false);
	}

	private String activeResult;

	void resultButtonResult(String result) {
		activeResult = result;

		String str = getString(R.string.result, result);

		final int start = str.indexOf("\n");
		final int end = str.length();

		SpannableString span = new SpannableString(str);
		span.setSpan(subTitleAppearance, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		resultButton.setText(span);
		resultButton.setEnabled(true);
	}

	void resultButtonEmpty() {
		activeResult = null;
		resultButton.setText(R.string.working_secret_empty);
		resultButton.setEnabled(false);
	}

	void resultButtonError(String msg) {
		activeResult = null;
		resultButton.setText(msg);
		resultButton.setEnabled(false);
	}

	PBKDF2Args getInputs() {
		PBKDF2Args args = new PBKDF2Args();

		args.password = secretEdit.getText().toString();
		args.username = usernameEdit.getText().toString();
		args.url = urlEdit.getText().toString();

		return args;
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (userInput && userInteractive) {
			updateResult(getInputs());
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
}
