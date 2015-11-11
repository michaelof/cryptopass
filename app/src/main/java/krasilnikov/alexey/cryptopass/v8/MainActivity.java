package krasilnikov.alexey.cryptopass.v8;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

import krasilnikov.alexey.cryptopass.Bookmark;
import krasilnikov.alexey.cryptopass.Data;
import krasilnikov.alexey.cryptopass.PBKDF2Args;
import krasilnikov.alexey.cryptopass.R;

@TargetApi(Build.VERSION_CODES.FROYO)
public final class MainActivity extends Activity implements TextWatcher {
    private Loader mActiveLoader;

    void initLoader() {
        Object obj = getLastNonConfigurationInstance();
        if (obj instanceof Loader) {
            mActiveLoader = (Loader) obj;
        }

        if (mActiveLoader == null) {
            mActiveLoader = new Loader();
        }

        mActiveLoader.onInit(this);
    }

    private Button mResultButton;
    private EditText mSecretEdit;
    private EditText mUsernameEdit;
    private EditText mUrlEdit;
    private SeekBar mLengthSeek;
    private TextView mLengthText;

    private TextAppearanceSpan mSubTitleAppearance;

    private boolean mUserInput = true;
    private boolean mUserInteractive = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.main);

        mSubTitleAppearance = new TextAppearanceSpan(this, android.R.style.TextAppearance_Small_Inverse);

        mSecretEdit = (EditText) findViewById(R.id.secretEdit);
        mUsernameEdit = (EditText) findViewById(R.id.usernameEdit);
        mUrlEdit = (EditText) findViewById(R.id.urlEdit);

        mLengthSeek = (SeekBar) findViewById(R.id.lengthSeek);
        mLengthText = (TextView) findViewById(R.id.lengthText);

        mResultButton = (Button) findViewById(R.id.passBtn);

        Intent startIntent = getIntent();

        Uri data = startIntent.getData();
        if (data != null) {
            mUsernameEdit.setText(Data.getUsername(data));
            mUrlEdit.setText(Data.getUrl(data));
            mLengthSeek.setProgress(Data.getLength(data) - 8);
        }

        if (mUrlEdit.getText().length() == 0) {
            mUrlEdit.requestFocus();
        }

        mSecretEdit.addTextChangedListener(this);
        mUsernameEdit.addTextChangedListener(this);
        mUrlEdit.addTextChangedListener(this);

        mResultButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                resultButtonClicked();
            }
        });

        updateLength();

        mLengthSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        return mLengthSeek.getProgress() + 8;
    }

    protected void updateLength() {
        int length = getPasswordLength();
        String str = String.format(Locale.ENGLISH, "%02d", length);

        mLengthText.setText(str);

        if (activeResult != null) {
            resultButtonResult(activeResult, length);
        }
    }

    protected void resultButtonClicked() {
        Bookmark bookmark = mActiveLoader.lastBookmark();

        if (activeResult != null) {
            int length = getPasswordLength();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(activeResult.substring(0, length));

            Intent saveIntent = new Intent(Data.ACTION_SAVE, Data.makeBookmarksUri(this));

            saveIntent.putExtra(Data.ARGS_URL, bookmark.url);
            saveIntent.putExtra(Data.ARGS_USERNAME, bookmark.username);
            saveIntent.putExtra(Data.ARGS_LENGTH, length);

            startService(saveIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mActiveLoader.onActivityDestroy();
    }

    @Override
    protected void onPause() {
        mUserInteractive = false;

        mActiveLoader.onActivityPause();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mActiveLoader.onActivityResume();

        mUserInteractive = true;
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mActiveLoader.onActivityGetRetainState();
    }

    protected void updateResult() {
        updateResult(getInputs());
    }

    protected void updateResult(PBKDF2Args args) {
        mActiveLoader.restart(args);
    }

    public void working() {
        resultButtonWorking();
    }

    public void complete(String result) {
        resultButtonResult(result);
    }

    public void emptySecret() {
        mUserInput = false;
        try {
            mSecretEdit.setText(null);
            resultButtonEmpty();
        } finally {
            mUserInput = true;
        }
    }

    public void restoreSecret(String secret, String result) {
        mUserInput = false;
        try {
            mSecretEdit.setText(secret);
            resultButtonResult(result);
        } finally {
            mUserInput = true;
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
        mResultButton.setText(R.string.working);
        mResultButton.setEnabled(false);
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

    private PBKDF2Args getInputs() {
        PBKDF2Args args = new PBKDF2Args();

        args.password = mSecretEdit.getText().toString();
        args.username = mUsernameEdit.getText().toString();
        args.url = mUrlEdit.getText().toString();

        return args;
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mUserInput && mUserInteractive) {
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
