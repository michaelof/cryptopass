package org.example.cryptopass.v11;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import org.example.cryptopass.Bookmark;
import org.example.cryptopass.Data;
import org.example.cryptopass.PBKDF2Args;
import org.example.cryptopass.R;

public class MainFragment extends Fragment implements TextWatcher, IResultHandler, LoaderManager.LoaderCallbacks<GenerateLoaderResult> {
	public static MainFragment instantiate(final Uri data) {
		MainFragment fragment = new MainFragment();

		if (data != null) {
			final Bundle args = new Bundle();

			args.putParcelable("data", data);

			fragment.setArguments(args);
		}

		return fragment;
	}

	public MainFragment() {
		super();

		//На самом деле в этом нет необходимости, но из-за некорректной работы Loaderов на
		//Honeycomb так лучше.
		setRetainInstance(true);
	}

	boolean wasPaused = false;

	private Button resultButton;
	private EditText secretEdit;
	private EditText usernameEdit;
	private EditText urlEdit;

	private TextAppearanceSpan subTitleAppearance;

	private Bookmark argsForKeyGenerated = null;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main, container, false);

		secretEdit = (EditText) view.findViewById(R.id.secretEdit);
		usernameEdit = (EditText) view.findViewById(R.id.usernameEdit);
		urlEdit = (EditText) view.findViewById(R.id.urlEdit);

		resultButton = (Button) view.findViewById(R.id.passBtn);

		subTitleAppearance = new TextAppearanceSpan(inflater.getContext(), android.R.style.TextAppearance_Small);

		if (savedInstanceState == null) {
			final Bundle args = getArguments();
			if (args != null) {
				Uri data = args.getParcelable("data");

				if (data != null) {
					usernameEdit.setText(Data.getUsername(data));
					urlEdit.setText(Data.getUrl(data));
				}
			}
		}

		if (urlEdit.getText().length() == 0) {
			urlEdit.requestFocus();
		}

		secretEdit.addTextChangedListener(this);
		usernameEdit.addTextChangedListener(this);
		urlEdit.addTextChangedListener(this);

		resultButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				resultButtonClicked();
			}
		});

		return view;
	}


	@Override
	public void onStart() {
		super.onStart();

		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(Loaders.RESULT_GENERATE_LOADER, null, this);
	}

	void resultButtonClicked() {
		if (activeResult != null) {
			ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
			clipboardManager.setPrimaryClip(ClipData.newPlainText("generated", activeResult));

			Intent saveIntent = new Intent(Data.ACTION_SAVE, Data.URI_BOOKMARKS);

			saveIntent.putExtra(Data.ARGS_URL, argsForKeyGenerated.url);
			saveIntent.putExtra(Data.ARGS_USERNAME, argsForKeyGenerated.username);

			getActivity().startService(saveIntent);
		}
	}

	public void onResume() {
		super.onResume();

		wasPaused = false;
	}

	public void onPause() {
		wasPaused = true;

		secretEdit.setText(null);
		getGenerator().clearArgs();

		super.onPause();
	}

	private GenerateLoader getGenerator() {
		final LoaderManager loaderManager = getLoaderManager();

		return (GenerateLoader) loaderManager.<GenerateLoaderResult>getLoader(Loaders.RESULT_GENERATE_LOADER);
	}

	private void updateResult() {
		argsForKeyGenerated = null;

		PBKDF2Args args = getInputs();

		GenerateLoader loader = getGenerator();

		loader.setArgs(args);

		if (args.isEmpty()) {
			resultButtonEmpty();
		} else {
			resultButtonWorking();
		}
	}

	private PBKDF2Args getInputs() {
		PBKDF2Args args = new PBKDF2Args();

		args.password = secretEdit.getText().toString();
		args.username = usernameEdit.getText().toString();
		args.url = urlEdit.getText().toString();

		return args;
	}

	private String activeResult;

	private void resultButtonWorking() {
		activeResult = null;
		resultButton.setText(R.string.working);
		resultButton.setEnabled(false);
	}

	private void resultButtonResult(String result) {
		activeResult = result;
		String str = getString(R.string.result, result);

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
		resultButtonEmpty();
		argsForKeyGenerated = null;
	}
}
