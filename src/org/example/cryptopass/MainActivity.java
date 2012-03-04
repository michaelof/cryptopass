package org.example.cryptopass;

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

public final class MainActivity extends Activity implements TextWatcher, PBKDF2AsyncTask.ResultListener
{
	public final static String EXTRA_USERNAME = "org.example.cryptopass.MainActivity.username";
	public final static String EXTRA_URL = "org.example.cryptopass.MainActivity.url";

	private PBKDF2AsyncTask activeTask = null;

	private Button resultButton;
	private EditText secretEdit;
	private EditText usernameEdit;
	private EditText urlEdit;

	private Bookmark argsForKeyGenerated = null;

	private boolean userActive = false;
	private boolean wasPaused = false;

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

		Object obj = getLastNonConfigurationInstance();

		if (obj instanceof PBKDF2AsyncTask)
		{
			PBKDF2AsyncTask task = (PBKDF2AsyncTask) obj;
			activeTask = task;
			activeTask.activityChanged(this);

			resultButtonWorking();
		}
		else
		{
			resultButtonEmpty();
		}
	}

	void resultButtonClicked()
	{
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setText(resultButton.getText());

		BookmarksHelper.saveBookmark(this, argsForKeyGenerated.username, argsForKeyGenerated.url);
	}

	protected void onDestroy()
	{
		super.onDestroy();

		if (activeTask != null)
		{
			activeTask.cancel(false);
		}
	}

	protected void onPause()
	{
		userActive = false;
		wasPaused = true;

		super.onPause();

		secretEdit.setText(null);
		resultButtonEmpty();
	}

	protected void onResume()
	{
		super.onResume();

		userActive = true;
	}

	public Object onRetainNonConfigurationInstance()
	{
		if (activeTask != null)
		{
			PBKDF2AsyncTask task = activeTask;

			activeTask = null;

			return task;
		}

		return null;
	}

	void updateResult(PBKDF2Args args)
	{
		argsForKeyGenerated = null;

		resultButtonWorking();

		if (activeTask == null)
		{
			if (!args.isEmpty())
			{
				activeTask = new PBKDF2AsyncTask(this, args);
				activeTask.execute();
			}
			else
			{
				empty();
			}
		}
		else
		{
			activeTask.inputChanged(args);
		}

		wasPaused = false;
	}

	public void restart(PBKDF2Args args)
	{
		activeTask = null;
		updateResult(args);
	}

	public void complete(Bookmark args, String result)
	{
		if (!wasPaused)
		{
			resultButtonResult(result);

			argsForKeyGenerated = args;
		}

		activeTask = null;
	}

	public void empty()
	{
		resultButtonEmpty();

		activeTask = null;
	}

	public void exception(Exception ex)
	{
		resultButtonError(ex.getMessage());

		activeTask = null;
	}

	void resultButtonWorking()
	{
		resultButton.setText("Working...");
		resultButton.setEnabled(false);
	}

	void resultButtonResult(String result)
	{
		resultButton.setText(result);
		resultButton.setEnabled(true);
	}

	void resultButtonEmpty()
	{
		resultButton.setText("Secret is empty");
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
		if (userActive)
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
