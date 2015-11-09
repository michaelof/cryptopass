package krasilnikov.alexey.cryptopass.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import dagger.Component;
import krasilnikov.alexey.cryptopass.AppComponent;
import krasilnikov.alexey.cryptopass.Data;
import krasilnikov.alexey.cryptopass.MainApplication;
import krasilnikov.alexey.cryptopass.OperationManager;
import krasilnikov.alexey.cryptopass.R;
import krasilnikov.alexey.cryptopass.scope.ContentProviderScoped;
import krasilnikov.alexey.cryptopass.scope.ServiceScoped;
import krasilnikov.alexey.cryptopass.sync.BookmarksWriter;

/**
 * This ContentProvider provides files for Intent.ACTION_SEND intent.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class DataProvider extends ContentProvider {
    private Component mComponent;

    @ContentProviderScoped
    @dagger.Component(dependencies = AppComponent.class)
    public interface Component {
        BookmarksWriter getBookmarksWriter();
    }

    @Override
    public boolean onCreate() {
        mComponent = DaggerDataProvider_Component.builder().
                appComponent(MainApplication.getComponent(getContext())).
                build();

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Context context = getContext();
        if (context == null) {
            throw new IllegalStateException();
        }

        context.enforceCallingUriPermission(uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION, "Access denied");

        if (!Data.makeExportUri(context).equals(uri)) {
            return null;
        }

        Object[] row = new Object[projection.length];
        for (int i = 0; i < projection.length; i++) {
            final String column = projection[i];
            switch (column) {
                case MediaStore.MediaColumns.DISPLAY_NAME:
                    row[i] = context.getString(R.string.export_file_name);
                    break;

                default:
                    row[i] = null;
            }
        }

        MatrixCursor m = new MatrixCursor(projection);
        m.addRow(row);
        return m;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        Context context = getContext();
        if (context == null) {
            throw new IllegalStateException();
        }

        if (!Data.makeExportUri(context).equals(uri)) {
            return super.openFile(uri, mode);
        }

        if (!"r".equals(mode)) {
            throw new UnsupportedOperationException();
        }

        try {
            File tmpFile = File.createTempFile("export", String.valueOf(SystemClock.uptimeMillis()));

            mComponent.getBookmarksWriter().write(new FileOutputStream(tmpFile));

            return ParcelFileDescriptor.open(tmpFile, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getType(Uri uri) {
        Context context = getContext();
        if (context == null) {
            throw new IllegalStateException();
        }

        if (!Data.makeExportUri(context).equals(uri)) {
            return "text/plain";
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
