package krasilnikov.alexey.cryptopass.v14;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

import krasilnikov.alexey.cryptopass.ActionService;
import krasilnikov.alexey.cryptopass.Bookmark;
import krasilnikov.alexey.cryptopass.Data;
import krasilnikov.alexey.cryptopass.PBKDF2Args;
import krasilnikov.alexey.cryptopass.R;
import krasilnikov.alexey.cryptopass.Version;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MainFragment extends Fragment implements TextWatcher, ResultHandler, LoaderManager.LoaderCallbacks<GenerateLoaderResult> {
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

        if (Version.isHoneycomb()) {
            setRetainInstance(true);
        }
    }

    boolean mWasPaused = false;

    private View mContainerView;
    private Button mResultButton;
    private EditText mSecretEdit;
    private EditText mUsernameEdit;
    private EditText mUrlEdit;
    private SeekBar mLengthSeek;
    private TextView mLengthText;

    private TextAppearanceSpan mSubTitleAppearance;

    private Bookmark mArgsForKeyGenerated = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContainerView = view;

        mSecretEdit = (EditText) view.findViewById(R.id.secretEdit);
        mUsernameEdit = (EditText) view.findViewById(R.id.usernameEdit);
        mUrlEdit = (EditText) view.findViewById(R.id.urlEdit);

        mLengthSeek = (SeekBar) view.findViewById(R.id.lengthSeek);
        mLengthText = (TextView) view.findViewById(R.id.lengthText);

        mResultButton = (Button) view.findViewById(R.id.passBtn);

        mSubTitleAppearance = new TextAppearanceSpan(getActivity(), android.R.style.TextAppearance_Small);

        if (savedInstanceState == null) {
            final Bundle args = getArguments();
            if (args != null) {
                Uri data = args.getParcelable("data");

                if (data != null) {
                    mUsernameEdit.setText(Data.getUsername(data));
                    mUrlEdit.setText(Data.getUrl(data));
                    mLengthSeek.setProgress(Data.getLength(data) - 8);
                }
            }
        }

        mSecretEdit.addTextChangedListener(this);
        mUsernameEdit.addTextChangedListener(this);
        mUrlEdit.addTextChangedListener(this);

        mResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                resultButtonClicked();
            }
        });

        mLengthSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !mWasPaused) {
                    updateLengthText();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        updateLengthText();
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

    protected int getPasswordLength() {
        return mLengthSeek.getProgress() + 8;
    }

    protected void updateLengthText() {
        int length = getPasswordLength();
        String str = String.format(Locale.ENGLISH, "%02d", length);

        mLengthText.setText(str);
        if (activeResult != null) {
            resultButtonResult(activeResult, length);
        }
    }

    protected void resultButtonClicked() {
        if (activeResult != null) {
            int length = getPasswordLength();

            ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText("generated", activeResult.substring(0, length)));

            Intent saveIntent = new Intent(Data.ACTION_SAVE, Data.makeBookmarksUri(getActivity()));

            saveIntent.setClass(getActivity(), ActionService.class);
            saveIntent.putExtra(Data.ARGS_URL, mArgsForKeyGenerated.url);
            saveIntent.putExtra(Data.ARGS_USERNAME, mArgsForKeyGenerated.username);
            saveIntent.putExtra(Data.ARGS_LENGTH, length);

            getActivity().startService(saveIntent);
        }
    }

    public void onResume() {
        super.onResume();

        mWasPaused = false;

        View focused = mContainerView.findFocus();
        if (focused == null) {
            if (mUrlEdit.getText().length() == 0) {
                mUrlEdit.requestFocus();
                focused = mUrlEdit;
            } else {
                mSecretEdit.requestFocus();
                focused = mSecretEdit;
            }
        }

        if (focused instanceof EditText) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(focused, 0);
        }
    }

    public void onPause() {
        mWasPaused = true;

        mSecretEdit.setText(null);
        getGenerator().clearArgs();

        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mContainerView = null;
        mSecretEdit = null;
        mUsernameEdit = null;
        mUrlEdit = null;

        mLengthSeek = null;
        mLengthText = null;

        mResultButton = null;
    }

    private GenerateLoader getGenerator() {
        final LoaderManager loaderManager = getLoaderManager();

        return (GenerateLoader) loaderManager.<GenerateLoaderResult>getLoader(Loaders.RESULT_GENERATE_LOADER);
    }

    private void updateResult() {
        mArgsForKeyGenerated = null;

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

        args.password = mSecretEdit.getText().toString();
        args.username = mUsernameEdit.getText().toString();
        args.url = mUrlEdit.getText().toString();

        return args;
    }

    private String activeResult;

    private void resultButtonWorking() {
        activeResult = null;
        mResultButton.setText(R.string.working);
        mResultButton.setEnabled(false);
    }

    private void resultButtonResult(String result) {
        resultButtonResult(result, getPasswordLength());
    }

    private void resultButtonResult(String result, int length) {
        activeResult = result;
        String str = getString(R.string.result, result.substring(0, length));

        final int start = str.indexOf("\n");
        final int end = str.length();

        SpannableString span = new SpannableString(str);
        span.setSpan(mSubTitleAppearance, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mResultButton.setText(span);
        mResultButton.setEnabled(true);
    }

    private void resultButtonEmpty() {
        activeResult = null;
        mResultButton.setText(R.string.working_secret_empty);
        mResultButton.setEnabled(false);
    }

    private void resultButtonError(String msg) {
        activeResult = null;
        mResultButton.setText(msg);
        mResultButton.setEnabled(false);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (!mWasPaused) {
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

        mArgsForKeyGenerated = args;
    }

    @Override
    public void empty() {
        resultButtonEmpty();
        mArgsForKeyGenerated = null;
    }
}
