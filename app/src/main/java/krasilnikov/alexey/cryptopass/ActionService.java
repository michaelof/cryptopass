package krasilnikov.alexey.cryptopass;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.inject.Inject;

import dagger.Component;
import dagger.Lazy;
import krasilnikov.alexey.cryptopass.data.BookmarksStorage;
import krasilnikov.alexey.cryptopass.scope.ServiceScoped;
import krasilnikov.alexey.cryptopass.sync.BookmarksWriter;

public class ActionService extends IntentService {
    private static final int NOTIFICATION_EXPORT = 1;
    private static final int NOTIFICATION_IMPORT = 2;

    @ServiceScoped
    @Component(dependencies = AppComponent.class)
    public interface ActionServiceComponent {
        void inject(ActionService actionService);
    }

    @Inject
    OperationManager mOperationManager;

    @Inject
    Lazy<BookmarksWriter> mBookmarksWriter;

    @Inject
    BookmarksStorage mBookmarksStorage;

    public ActionService() {
        super("cryptopass");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ActionServiceComponent component = DaggerActionService_ActionServiceComponent.builder().
                appComponent(MainApplication.getComponent(this)).
                build();

        component.inject(this);
    }

    private void saveBookmark(Intent intent) {
        String username = intent.getStringExtra(Data.ARGS_USERNAME);
        String url = intent.getStringExtra(Data.ARGS_URL);
        int length = intent.getIntExtra(Data.ARGS_LENGTH, Data.DEFAULT_LENGTH);

        Uri bookmarksUri = Data.makeBookmarksUri(this);
        Object obj = mOperationManager.operationStarted(bookmarksUri);
        try {
            mBookmarksStorage.saveBookmark(username, url, length);
        } finally {
            mOperationManager.operationEnded(bookmarksUri, obj);
        }
    }

    private void deleteBookmark(Intent intent) {
        Uri uri = intent.getData();
        String username = Data.getUsername(uri);
        String url = Data.getUrl(uri);

        Object obj = mOperationManager.operationStarted(uri);
        try {
            mBookmarksStorage.deleteBookmark(username, url);
        } finally {
            mOperationManager.operationEnded(uri, obj);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private String getFileDisplayName(Uri destination) {
        String[] columns = new String[]{OpenableColumns.DISPLAY_NAME};
        Cursor c = getContentResolver().query(destination, columns, null, null, null);
        try {
            if (c != null && c.moveToFirst()) {
                return c.getString(0);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return null;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Notification makeProgressNotification(int title, String displayName) {
        Notification.Builder b = new Notification.Builder(this);
        b.setSmallIcon(R.drawable.icon);
        b.setContentTitle(getString(title));
        if (!TextUtils.isEmpty(displayName)) {
            b.setContentText(displayName);
        }
        b.setProgress(100, 0, true);
        return b.getNotification();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Notification makeCompleteExportNotification(String displayName) {
        Notification.Builder b = new Notification.Builder(this);
        b.setSmallIcon(R.drawable.icon);
        b.setContentTitle(getString(R.string.title_export_complete));
        if (!TextUtils.isEmpty(displayName)) {
            b.setContentText(displayName);
        }
        return b.getNotification();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void exportBookmarks(Intent intent) {
        Uri destination = intent.getData();

        Object obj = mOperationManager.operationStarted(destination);
        try {
            String displayName = getFileDisplayName(destination);

            startForeground(NOTIFICATION_EXPORT,
                    makeProgressNotification(R.string.title_export_progress, displayName));

            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(destination, "w");
            FileOutputStream outputStream =
                    new ParcelFileDescriptor.AutoCloseOutputStream(pfd);
            mBookmarksWriter.get().write(outputStream);

            stopForeground(true);
            NotificationManager notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notifManager.notify(NOTIFICATION_EXPORT, makeCompleteExportNotification(displayName));

        } catch (Exception e) {
            stopForeground(true);
            Log.e("cryptopass", e.getMessage(), e);
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        } finally {
            mOperationManager.operationEnded(destination, obj);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void readBookmarks(JsonReader reader) throws IOException {
        reader.beginArray();

        while (reader.hasNext()) {
            reader.beginObject();

            String url = "";
            String username = "";
            int length = Data.DEFAULT_LENGTH;

            while (reader.hasNext()) {
                String name = reader.nextName();
                if ("url".equals(name)) {
                    url = reader.nextString();
                } else if ("username".equals(name)) {
                    username = reader.nextString();
                } else if ("length".equals(name)) {
                    length = reader.nextInt();
                } else {
                    reader.nextString();
                }
            }

            mBookmarksStorage.saveBookmark(username, url, length);

            reader.endObject();
        }
        reader.endArray();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void importBookmarks(Intent intent) {
        Uri source = intent.getData();

        Object obj = mOperationManager.operationStarted(source);
        try {
            String displayName = getFileDisplayName(source);

            startForeground(NOTIFICATION_IMPORT,
                    makeProgressNotification(R.string.title_import_progress, displayName));

            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(source, "r");
            FileInputStream inputStream = new
                    ParcelFileDescriptor.AutoCloseInputStream(pfd);
            Reader fileReader = new InputStreamReader(inputStream);
            JsonReader jsonReader = new JsonReader(fileReader);
            try {
                readBookmarks(jsonReader);
            } finally {
                Utils.close(jsonReader);
            }
        } catch (Exception e) {
            Log.e("cryptopass", e.getMessage(), e);
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        } finally {
            stopForeground(true);
            mOperationManager.operationEnded(source, obj);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();

        if (Data.ACTION_SAVE.equals(action)) {
            saveBookmark(intent);
        } else if (Data.ACTION_DELETE.equals(action)) {
            deleteBookmark(intent);
        } else if (Data.ACTION_EXPORT.equals(action)) {
            exportBookmarks(intent);
        } else if (Data.ACTION_IMPORT.equals(action)) {
            importBookmarks(intent);
        }
    }
}
