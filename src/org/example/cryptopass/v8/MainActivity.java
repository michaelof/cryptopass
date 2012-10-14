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
import android.widget.SeekBar;
import android.widget.TextView;
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
	private SeekBar lengthSeek;
	private TextView lengthText;

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

		lengthSeek = (SeekBar) findViewById(R.id.lengthSeek);
		lengthText = (TextView) findViewById(R.id.lengthText);

		resultButton = (Button) findViewById(R.id.passBtn);

		Intent startIntent = getIntent();

		Uri data = startIntent.getData();
		if (data != null) {
			usernameEdit.setText(Data.getUsername(data));
			urlEdit.setText(Data.getUrl(data));
			lengthSeek.setProgress(Data.getLength(data) - 8);
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

		updateLength();

		lengthSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					updateLength();
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		initLoader();
	}

	protected int getPasswordLength() {
		return lengthSeek.getProgress() + 8;
	}

	protected void updateLength() {
		int length = getPasswordLength();
		String str = String.format("%02d", length);

		lengthText.setText(str);

		if (activeResult != null) {
			resultButtonResult(activeResult, length);
		}
	}

	protected void resultButtonClicked() {
		Bookmark bookmark = activeLoader.lastBookmark();

		if (activeResult != null) {
			int length = getPasswordLength();
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(activeResult.substring(0, length));

			Intent saveIntent = new Intent(Data.ACTION_SAVE, Data.URI_BOOKMARKS);

			saveIntent.putExtra(Data.ARGS_URL, bookmark.url);
			saveIntent.putExtra(Data.ARGS_USERNAME, bookmark.username);
			saveIntent.putExtra(Data.ARGS_LENGTH, length);

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

	protected void updateResult() {
		updateResult(getInputs());
	}

	protected void updateResult(PBKDF2Args args) {
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

	private void resultButtonWorking() {
		activeResult = null;
		resultButton.setText(R.string.working);
		resultButton.setEnabled(false);
	}

	private String activeResult;

	private void resultButtonResult(String result) {
		resultButtonResult(result, getPasswordLength());
	}

	private void resultButtonResult(String result, int length) {
		activeResult = result;

		String str = getString(R.string.result, result.substring(0, length));

		final int start = str.indexOf("\n");
		final int end = str.length();

		SpannableString span = new SpannableString(str);
		span.setSpan(subTitleAppearance, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		resultButton.setText(span);
		resultButton.setEnabled(true);
	}

	private void resultButtonEmpty() {
		activeResult = null;
		resultButton.setText(R.string.working_secret_empty);
		resultButton.setEnabled(false);
	}

	private void resultButtonError(String msg) {
		activeResult = null;
		resultButton.setText(msg);
		resultButton.setEnabled(false);
	}

	private PBKDF2Args getInputs() {
		PBKDF2Args args = new PBKDF2Args();

		args.password = secretEdit.getText().toString();
		args.username = usernameEdit.getText().toString();
		args.url = urlEdit.getText().toString();

		return args;
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (userInput && userInteractive) {
			updateResult();
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
}
