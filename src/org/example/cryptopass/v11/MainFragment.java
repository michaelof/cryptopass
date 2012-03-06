package org.example.cryptopass.v11;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import org.example.cryptopass.Bookmark;
import org.example.cryptopass.PBKDF2Args;
import org.example.cryptopass.R;

public class MainFragment extends Fragment implements TextWatcher, IResultHandler, LoaderManager.LoaderCallbacks<GenerateLoaderResult> {
	private static int RESULT_GENERATE_LOADER = 2;

	boolean wasPaused;

	private Button resultButton;
	private EditText secretEdit;
	private EditText usernameEdit;
	private EditText urlEdit;

	private Bookmark argsForKeyGenerated = null;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main, container, false);

		secretEdit = (EditText) view.findViewById(R.id.secretEdit);
		usernameEdit = (EditText) view.findViewById(R.id.usernameEdit);
		urlEdit = (EditText) view.findViewById(R.id.urlEdit);

		resultButton = (Button) view.findViewById(R.id.passBtn);

		secretEdit.addTextChangedListener(this);
		usernameEdit.addTextChangedListener(this);
		urlEdit.addTextChangedListener(this);

		resultButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0)
			{
				resultButtonClicked();
			}
		});

		getLoaderManager().initLoader(RESULT_GENERATE_LOADER, null, this);

		return view;
	}

	void resultButtonClicked()
	{
	}

	public void onResume() {
		super.onResume();

		wasPaused = false;
	}

	public void onPause() {
		wasPaused = true;

		super.onPause();

		secretEdit.setText(null);

		updateResult();
	}

	void updateResult() {
		argsForKeyGenerated = null;

		PBKDF2Args args = getInputs();

		if (args.isEmpty()) {
			getLoaderManager().destroyLoader(RESULT_GENERATE_LOADER);
			resultButtonEmpty();
		} else {
			GenerateLoader loader = (GenerateLoader) getLoaderManager().initLoader(RESULT_GENERATE_LOADER, null, this);
			argsForKeyGenerated = null;

			resultButtonWorking();

			loader.setArgs(args);
		}
	}

	PBKDF2Args getInputs() {
		PBKDF2Args args = new PBKDF2Args();

		args.password = secretEdit.getText().toString();
		args.username = usernameEdit.getText().toString();
		args.url = urlEdit.getText().toString();

		return args;
	}

	void resultButtonWorking() {
		resultButton.setText(R.string.working);
		resultButton.setEnabled(false);
	}

	void resultButtonResult(String result) {
		resultButton.setText(result);
		resultButton.setEnabled(true);
	}

	void resultButtonEmpty() {
		resultButton.setText(R.string.working_secret_empty);
		resultButton.setEnabled(false);
	}

	void resultButtonError(String msg) {
		resultButton.setText(msg);
		resultButton.setEnabled(false);
	}

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
	}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
	}

	@Override
	public void afterTextChanged(Editable editable) {
		if (!wasPaused) {
			updateResult();
		}
	}

	@Override
	public Loader<GenerateLoaderResult> onCreateLoader(int id, Bundle bundle) {
		return new GenerateLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<GenerateLoaderResult> generateLoaderResultLoader, GenerateLoaderResult generateLoaderResult) {
		generateLoaderResult.result(this);
	}

	@Override
	public void onLoaderReset(Loader<GenerateLoaderResult> generateLoaderResultLoader) {
		resultButtonEmpty();
	}

	@Override
	public void exception(Exception occurredException) {
		resultButtonError(occurredException.getMessage());
	}

	@Override
	public void complete(Bookmark args, String result) {
		resultButtonResult(result);

		argsForKeyGenerated = args;
	}

	@Override
	public void empty() {
	}
}
