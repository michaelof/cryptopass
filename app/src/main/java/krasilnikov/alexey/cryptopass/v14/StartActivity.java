package krasilnikov.alexey.cryptopass.v14;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import krasilnikov.alexey.cryptopass.ActionService;
import krasilnikov.alexey.cryptopass.Data;
import krasilnikov.alexey.cryptopass.OpenIntents;
import krasilnikov.alexey.cryptopass.OperationManager;
import krasilnikov.alexey.cryptopass.R;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class StartActivity extends Activity implements BookmarksFragment.IListener, OperationManager.OperationListener {
    private static final String FIRST_START = "firstStart";

    private static final String BOOKMARKS_TAG = "bookmarks";
    private static final String MAIN_TAG = "main";

    private static final int REQUEST_CODE_EXPORT = 0;
    private static final int REQUEST_CODE_IMPORT = 1;
    private static final int REQUEST_CODE_OI_EXPORT = 2;
    private static final int REQUEST_CODE_OI_IMPORT = 3;
    private final Handler mHandler = new Handler();
    private final Runnable mUpdateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            setProgressBarIndeterminateVisibility(OperationManager.getInstance().isInOperation());
        }
    };

    private void handleIntent(Intent intent) {
        boolean firstStart = intent.getBooleanExtra(FIRST_START, false);

        if (firstStart) {
            getFragmentManager().beginTransaction().replace(R.id.rootView, new MainFragment(), MAIN_TAG).commit();
        } else {
            getFragmentManager().beginTransaction().replace(R.id.rootView, new BookmarksFragment(), BOOKMARKS_TAG).commit();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.start);

        setProgressBarIndeterminateVisibility(false);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        OperationManager.getInstance().subscribe(this);
        mUpdateProgressRunnable.run();
    }

    @Override
    protected void onPause() {
        super.onPause();

        OperationManager.getInstance().unsubscribe(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void goHome() {
        Intent intent = new Intent(this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void startExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_EXPORT);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void startImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_IMPORT);
    }

    private void tryStartOIFileManager(Intent intent, int requestCode) {
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException ex) {
            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
            marketIntent.addCategory(Intent.CATEGORY_BROWSABLE);
            marketIntent.setData(OpenIntents.FILEMANAGER_ON_MARKET);
            marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(marketIntent);
        }
    }

    private void startOIImport() {
        Intent intent = new Intent(OpenIntents.ACTION_PICK_FILE);
        intent.putExtra(OpenIntents.EXTRA_TITLE, "Cryptopass: Import bookmarks");
        intent.putExtra(OpenIntents.EXTRA_BUTTON_TEXT, "Open");
        tryStartOIFileManager(intent, REQUEST_CODE_OI_IMPORT);
    }

    private void startOIExport() {
        Intent intent = new Intent(OpenIntents.ACTION_PICK_FILE);
        intent.setData(Uri.parse("file:///bookmarks.json"));
        intent.putExtra(OpenIntents.EXTRA_TITLE, "Cryptopass: Export bookmarks");
        intent.putExtra(OpenIntents.EXTRA_BUTTON_TEXT, "Save");
        tryStartOIFileManager(intent, REQUEST_CODE_OI_EXPORT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                goHome();
                return true;

            case R.id.oi_import_bookmarks:
                startOIImport();
                return true;
            case R.id.oi_export_bookmarks:
                startOIExport();
                return true;

            case R.id.import_bookmarks:
                startImport();
                return true;
            case R.id.export_bookmarks:
                startExport();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case REQUEST_CODE_OI_IMPORT:
                case REQUEST_CODE_IMPORT: {
                    Intent intent = new Intent(Data.ACTION_IMPORT, data.getData());
                    intent.setClass(this, ActionService.class);
                    startService(intent);
                }
                break;
                case REQUEST_CODE_OI_EXPORT:
                case REQUEST_CODE_EXPORT: {
                    Intent intent = new Intent(Data.ACTION_EXPORT, data.getData());
                    intent.setClass(this, ActionService.class);
                    startService(intent);
                }

                break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void noBookmarks() {
        Intent intent = new Intent(this, StartActivity.class);
        intent.putExtra(FIRST_START, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public void showBookmark(Uri data) {
        final MainFragment fragment = MainFragment.instantiate(data);

        getFragmentManager().beginTransaction().replace(R.id.rootView, fragment, MAIN_TAG).addToBackStack(null).commit();
    }

    @Override
    public void onOperationStarted(Uri uri) {
        mHandler.removeCallbacks(mUpdateProgressRunnable);
        mHandler.post(mUpdateProgressRunnable);
    }

    @Override
    public void onOperationEnded(Uri uri) {
        mHandler.removeCallbacks(mUpdateProgressRunnable);
        mHandler.post(mUpdateProgressRunnable);
    }
}
