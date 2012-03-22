package org.example.cryptopass;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

public abstract class SimpleCursorLoader extends AsyncTaskLoader<Cursor> {
    final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();
    private Cursor mLoadedCursor;

    public SimpleCursorLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mLoadedCursor;
        mLoadedCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     *
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mLoadedCursor != null) {
            deliverResult(mLoadedCursor);
        }
        if (takeContentChanged() || mLoadedCursor == null) {
            forceLoad();
        }
    }

    protected Cursor onLoadInBackground(){
        Cursor cursor = super.onLoadInBackground();

        if (cursor != null) {
            cursor.getCount();
            cursor.registerContentObserver(mObserver);
        }

        return cursor;
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        cancelLoad();

        if (mLoadedCursor != null && !mLoadedCursor.isClosed()) {
            mLoadedCursor.close();
        }
        mLoadedCursor = null;
    }
}
