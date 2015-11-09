package krasilnikov.alexey.cryptopass.v8;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import krasilnikov.alexey.cryptopass.ActionService;
import krasilnikov.alexey.cryptopass.AppComponent;
import krasilnikov.alexey.cryptopass.BookmarksAdapter;
import krasilnikov.alexey.cryptopass.Data;
import krasilnikov.alexey.cryptopass.MainApplication;
import krasilnikov.alexey.cryptopass.OperationManager;
import krasilnikov.alexey.cryptopass.R;
import krasilnikov.alexey.cryptopass.data.BookmarksHelper;
import krasilnikov.alexey.cryptopass.oi.OpenIntents;
import krasilnikov.alexey.cryptopass.oi.OpenIntentsActivityHelper;
import krasilnikov.alexey.cryptopass.scope.ActivityModule;
import krasilnikov.alexey.cryptopass.scope.ActivityScoped;

public class StartActivity extends ListActivity implements OnItemClickListener, OperationManager.OperationListener {
    private static final int INVALID_ID = -1;

    @ActivityScoped
    @dagger.Component(dependencies = AppComponent.class, modules = ActivityModule.class)
    public interface Component {
        OperationManager getOperationManager();

        OpenIntentsActivityHelper getOIHelper();

        BookmarksSerializer getBookmarksSerializer();
    }

    private Component mComponent;
    private Cursor mBookmarksCursor;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mComponent = DaggerStartActivity_Component.builder().
                appComponent(MainApplication.getComponent(this)).
                activityModule(new ActivityModule(this)).
                build();

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setProgressBarIndeterminateVisibility(false);

        mBookmarksCursor = getContentResolver().query(Data.makeBookmarksUri(this), Data.BOOKMARKS_PROJECTION, null, null, null);
        startManagingCursor(mBookmarksCursor);

        if (mBookmarksCursor.getCount() == 0) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            ListView listView = getListView();

            TextView headerView = (TextView) getLayoutInflater().inflate(R.layout.row_empty, listView, false);

            listView.addHeaderView(headerView);
            listView.setOnItemClickListener(this);
            registerForContextMenu(listView);

            setListAdapter(new BookmarksAdapter(this, mBookmarksCursor));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send_bookmarks:
                sendBookmarks();
                return true;

            case R.id.oi_import_bookmarks:
                mComponent.getOIHelper().startImport();
                return true;
            case R.id.oi_export_bookmarks:
                mComponent.getOIHelper().startExport();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final ContentObserver mBookmarksObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (mBookmarksCursor != null && !mBookmarksCursor.isClosed()) {
                mBookmarksCursor.requery();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        mComponent.getOperationManager().subscribe(this);
        getContentResolver().registerContentObserver(Data.makeBookmarksUri(this), false, mBookmarksObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();

        getContentResolver().unregisterContentObserver(mBookmarksObserver);
        mComponent.getOperationManager().unsubscribe(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBookmarksCursor = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (INVALID_ID != id) {
            startMainBookmark(position);
        } else {
            startMainEmpty();
        }
    }

    private void sendBookmarks() {
        try {
            String data = mComponent.getBookmarksSerializer().serialize(mBookmarksCursor);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, data);
            intent.putExtra(Intent.EXTRA_SUBJECT, "CryptopassBookmarks.txt");
            startActivity(intent);
        } catch (JSONException e) {
            throw new RuntimeException("Send bookmarks failed",e);
        } catch (ActivityNotFoundException e) {
            // If there are no apps that can handle SEND intent,
            // suggest user to install Google Drive.
            try {
                Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                marketIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                marketIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.docs"));
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(marketIntent);
            } catch (ActivityNotFoundException ignored) {
                // No Google Play and no web browser? Hm.
            }
        }
    }

    private void startMainEmpty() {
        startActivity(new Intent(StartActivity.this, MainActivity.class));
    }

    private void startMainBookmark(final int listPosition) {
        if (listPosition > 0) {
            Uri uri = BookmarksHelper.getBookmarkUri(this, mBookmarksCursor, listPosition - 1);
            Intent intent = new Intent(Data.ACTION_SHOW, uri);

            startActivity(intent);
        }
    }

    private void deleteBookmark(final int listPosition) {
        if (listPosition > 0) {
            Uri uri = BookmarksHelper.getBookmarkUri(this, mBookmarksCursor, listPosition - 1);
            Intent intent = new Intent(Data.ACTION_DELETE, uri);
            intent.setClass(this, ActionService.class);

            startService(intent);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v == getListView()) {
            AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;

            if (INVALID_ID != adapterMenuInfo.id) {
                getMenuInflater().inflate(R.menu.context, menu);

                TextView tv = (TextView) adapterMenuInfo.targetView;

                menu.setHeaderTitle(tv.getText());
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();

        if (INVALID_ID != menuInfo.id) {
            switch (item.getItemId()) {
                case R.id.open:
                    startMainBookmark(menuInfo.position);

                    return true;

                case R.id.delete:
                    deleteBookmark(menuInfo.position);

                    return true;
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mComponent.getOIHelper().onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    private final Handler mHandler = new Handler();

    private final Runnable mUpdateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            setProgressBarIndeterminateVisibility(mComponent.getOperationManager().isInOperation());
        }
    };

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