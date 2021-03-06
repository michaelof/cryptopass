package krasilnikov.alexey.cryptopass.v14;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import javax.inject.Inject;

import dagger.BindsInstance;
import dagger.Component;
import krasilnikov.alexey.cryptopass.ActionService;
import krasilnikov.alexey.cryptopass.AppComponent;
import krasilnikov.alexey.cryptopass.Data;
import krasilnikov.alexey.cryptopass.MainApplication;
import krasilnikov.alexey.cryptopass.ProgressNotifier;
import krasilnikov.alexey.cryptopass.R;
import krasilnikov.alexey.cryptopass.scope.ActivityModule;
import krasilnikov.alexey.cryptopass.scope.ActivityScoped;
import krasilnikov.alexey.cryptopass.sync.SendHelper;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class StartActivity extends Activity implements BookmarksFragment.Listener, ProgressNotifier.OperationListener {
    private static final String FIRST_START = "firstStart";

    private static final String BOOKMARKS_TAG = "bookmarks";
    private static final String MAIN_TAG = "main";

    private static final int REQUEST_CODE_EXPORT = 0;
    private static final int REQUEST_CODE_IMPORT = 1;

    @ActivityScoped
    @dagger.Component(dependencies = AppComponent.class,
            modules = {ActivityModule.class, ProgressControllerModule.class})
    public interface Component {
        void inject(BookmarksFragment fragment);

        void inject(StartActivity startActivity);

        @dagger.Component.Builder
        interface Builder {
            Builder appComponent(AppComponent app);

            @BindsInstance
            Builder activity(Activity activity);

            Component build();
        }
    }

    private Component mComponent;
    private final Handler mHandler = new Handler();
    private final Runnable mUpdateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    @Inject
    ProgressNotifier mProgressNotifier;

    @Inject
    SendHelper mSendHelper;

    @Inject
    ProgressController mProgressController;

    private void handleIntent(Intent intent) {
        boolean firstStart = intent.getBooleanExtra(FIRST_START, false);

        if (firstStart) {
            getFragmentManager().beginTransaction().replace(android.R.id.content, new MainFragment(), MAIN_TAG).commit();
        } else {
            getFragmentManager().beginTransaction().replace(android.R.id.content, new BookmarksFragment(), BOOKMARKS_TAG).commit();
        }
    }

    private Component getComponent() {
        if (mComponent == null) {
            mComponent = DaggerStartActivity_Component.builder().
                    appComponent(MainApplication.getComponent(this)).
                    activity(this).
                    build();
        }
        return mComponent;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        if (fragment instanceof BookmarksFragment) {
            getComponent().inject((BookmarksFragment) fragment);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getComponent().inject(this);

        setContentView(R.layout.start);

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

        mProgressNotifier.subscribe(this);
        updateProgress();
    }

    void updateProgress() {
        mProgressController.setVisibility(mProgressNotifier.isInOperation());
    }

    @Override
    protected void onPause() {
        super.onPause();

        mProgressNotifier.unsubscribe(this);
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
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.export_file_name));
        intent.setType("text/plain");
        startActivityForResult(intent, REQUEST_CODE_EXPORT);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void startImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, REQUEST_CODE_IMPORT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                goHome();
                return true;

            case R.id.send_bookmarks:
                mSendHelper.sendByProvider();
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
                case REQUEST_CODE_IMPORT: {
                    Intent intent = new Intent(Data.ACTION_IMPORT, data.getData());
                    intent.setClass(this, ActionService.class);
                    startService(intent);
                }
                break;
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

        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment, MAIN_TAG).addToBackStack(null).commit();
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
