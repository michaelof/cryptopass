package org.example.cryptopass.v8;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import org.example.cryptopass.*;

public final class MainActivity extends Activity implements TextWatcher
{
	public final static String EXTRA_USERNAME = "org.example.cryptopass.v8.MainActivity.username";
	public final static String EXTRA_URL = "org.example.cryptopass.v8.MainActivity.url";

	private Loader activeLoader;
	
	void initLoader() {
		Object obj = getLastNonConfigurationInstance();
		if (obj instanceof Loader) {
			activeLoader = (Loader)obj;
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

	private boolean userInput = true;
	private boolean userInteractive = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		secretEdit = (EditText) findViewById(R.id.secretEdit);
		usernameEdit = (EditText) findViewById(R.id.usernameEdit);
		urlEdit = (EditText) findViewById(R.id.urlEdit);

		resultButton = (Button) findViewById(R.id.passBtn);

		Intent startIntent = getIntent();

		usernameEdit.setText(startIntent.getStringExtra(EXTRA_USERNAME));
		urlEdit.setText(startIntent.getStringExtra(EXTRA_URL));
		if (urlEdit.getText().length() == 0)
		{
			urlEdit.requestFocus();
		}

		secretEdit.addTextChangedListener(this);
		usernameEdit.addTextChangedListener(this);
		urlEdit.addTextChangedListener(this);

		resultButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0)
			{
				resultButtonClicked();
			}
		});

		initLoader();
	}

	void resultButtonClicked()
	{
		Bookmark bookmark = activeLoader.lastBookmark();
		
		if (bookmark != null && !bookmark.isEmpty()) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(resultButton.getText());
	
			BookmarksHelper.saveBookmark(this, bookmark);
		}
	}

	protected void onDestroy()
	{
		super.onDestroy();
		
		activeLoader.onActivityDestroy();
	}

	protected void onPause()
	{
		userInteractive = false;
		
		activeLoader.onActivityPause();
		
		super.onPause();
	}

	protected void onResume()
	{
		super.onResume();

		activeLoader.onActivityResume();
		
		userInteractive = true;
	}

	public Object onRetainNonConfigurationInstance()
	{
		return activeLoader.onActivityGetRetainState();
	}

	void updateResult(PBKDF2Args args)
	{		
		activeLoader.restart(args);
	}
	
	public void working() {
		resultButtonWorking();
	}

	public void complete(String result)
	{
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
	
	public void empty()
	{
		resultButtonEmpty();
	}

	public void exception(Exception ex)
	{
		resultButtonError(ex.getMessage());
	}

	void resultButtonWorking()
	{
		resultButton.setText(R.string.working);
		resultButton.setEnabled(false);
	}

	void resultButtonResult(String result)
	{
		resultButton.setText(result);
		resultButton.setEnabled(true);
	}

	void resultButtonEmpty()
	{
		resultButton.setText(R.string.working_secret_empty);
		resultButton.setEnabled(false);
	}

	void resultButtonError(String msg)
	{
		resultButton.setText(msg);
		resultButton.setEnabled(false);
	}

	PBKDF2Args getInputs()
	{
		PBKDF2Args args = new PBKDF2Args();

		args.password = secretEdit.getText().toString();
		args.username = usernameEdit.getText().toString();
		args.url = urlEdit.getText().toString();

		return args;
	}

	@Override
	public void afterTextChanged(Editable s)
	{
		if (userInput && userInteractive)
		{
			updateResult(getInputs());
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
	}
}