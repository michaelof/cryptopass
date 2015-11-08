package krasilnikov.alexey.cryptopass;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import dagger.Component;
import krasilnikov.alexey.cryptopass.scope.ServiceScoped;

public class ActionService extends IntentService {
    private static final int NOTIFICATION_EXPORT = 1;
    private static final int NOTIFICATION_IMPORT = 2;

    @ServiceScoped
    @Component(dependencies = AppComponent.class)
    public interface ActionServiceComponent {
        OperationManager getOperationManager();
    }

    private ActionServiceComponent mActionServiceComponent;

    public ActionService() {
        super("cryptopass");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mActionServiceComponent = DaggerActionService_ActionServiceComponent.builder().
                appComponent(MainApplication.getComponent(this)).
                build();
    }

    private void saveBookmark(Intent intent) {
        ContentValues values = new ContentValues();

        String username = intent.getStringExtra(Data.ARGS_USERNAME);
        String url = intent.getStringExtra(Data.ARGS_URL);
        int length = intent.getIntExtra(Data.ARGS_LENGTH, Data.DEFAULT_LENGTH);

        values.put(Data.ARGS_USERNAME, username);
        values.put(Data.ARGS_URL, url);
        values.put(Data.ARGS_LENGTH, length);

        Uri bookmarksUri = Data.makeBookmarksUri(this);
        OperationManager operationManager = mActionServiceComponent.getOperationManager();
        Object obj = operationManager.operationStarted(bookmarksUri);
        try {
            getContentResolver().insert(bookmarksUri, values);
        } finally {
            operationManager.operationEnded(bookmarksUri, obj);
        }
    }

    private void deleteBookmark(Intent intent) {
        Uri uri = intent.getData();

        OperationManager operationManager = mActionServiceComponent.getOperationManager();
        Object obj = operationManager.operationStarted(uri);
        try {
            getContentResolver().delete(uri, null, null);
        } finally {
            operationManager.operationEnded(uri, obj);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void writeBookmarks(JsonWriter writer) throws IOException {
        writer.beginArray();
        Cursor c = getContentResolver().query(Data.makeBookmarksUri(this),
                Data.BOOKMARKS_PROJECTION, null, null, null);
        while (c.moveToNext()) {
            String url = c.getString(Data.URL_COLUMN);
            String username = c.getString(Data.USERNAME_COLUMN);
            int length = c.getInt(Data.LENGTH_COLUMN);

            writer.beginObject();
            if (!TextUtils.isEmpty(url))
                writer.name("url").value(url);
            if (!TextUtils.isEmpty(username))
                writer.name("username").value(username);
            writer.name("length").value(length);
            writer.endObject();
        }
        writer.endArray();
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

        OperationManager operationManager = mActionServiceComponent.getOperationManager();
        Object obj = operationManager.operationStarted(destination);
        try {
            String displayName = getFileDisplayName(destination);

            startForeground(NOTIFICATION_EXPORT,
                    makeProgressNotification(R.string.title_export_progress, displayName));

            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(destination, "w");
            FileOutputStream outputStream =
                    new ParcelFileDescriptor.AutoCloseOutputStream(pfd);
            Writer fileWriter = new OutputStreamWriter(outputStream);
            JsonWriter jsonWriter = new JsonWriter(fileWriter);
            try {
                writeBookmarks(jsonWriter);
            } finally {
                Utils.close(jsonWriter);
            }

            stopForeground(true);
            NotificationManager notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notifManager.notify(NOTIFICATION_EXPORT, makeCompleteExportNotification(displayName));

        } catch (Exception e) {
            stopForeground(true);
            Log.e("cryptopass", e.getMessage(), e);
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        } finally {
            operationManager.operationEnded(destination, obj);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void readBookmarks(JsonReader reader) throws IOException {
        reader.beginArray();

        Uri bookmarksUri = Data.makeBookmarksUri(this);
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

            ContentValues values = new ContentValues();

            values.put(Data.ARGS_USERNAME, username);
            values.put(Data.ARGS_URL, url);
            values.put(Data.ARGS_LENGTH, length);

            getContentResolver().insert(bookmarksUri, values);

            reader.endObject();
        }
        reader.endArray();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void importBookmarks(Intent intent) {
        Uri source = intent.getData();

        OperationManager operationManager = mActionServiceComponent.getOperationManager();
        Object obj = operationManager.operationStarted(source);
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
            operationManager.operationEnded(source, obj);
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
