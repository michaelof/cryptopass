package krasilnikov.alexey.cryptopass.v8;

import android.util.Log;

import krasilnikov.alexey.cryptopass.Bookmark;
import krasilnikov.alexey.cryptopass.PBKDF2Args;

class Loader {
    private static final boolean DEBUG = false;
    private static final String DEBUG_LOG_TAG = "cryptopass";

    private MainActivity mMainActivity;

    private PBKDF2AsyncTask mActiveTask = null;

    private PBKDF2Args mLastArgs = new PBKDF2Args();
    private String mLastResult;
    private final ResultListener mTaskListener = new ResultListener() {
        @Override
        public void restart(PBKDF2Args args) {
            if (DEBUG) Log.d(DEBUG_LOG_TAG, String.format("Loader.listener.restart %1$s", args));

            mActiveTask = null;

            Loader.this.restart(args);
        }

        @Override
        public void exception(Exception occuredException) {
            if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.listener.exception", occuredException);

            mActiveTask = null;
            mMainActivity.exception(occuredException);
        }

        @Override
        public void empty() {
            if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.listener.empty");

            mActiveTask = null;
            mMainActivity.empty();
        }

        @Override
        public void complete(Bookmark args, String result) {
            if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.listener.complete");

            mLastResult = result;
            mActiveTask = null;
            mMainActivity.complete(result);
        }
    };
    private boolean mByConfigChange = false;

    public void restart(PBKDF2Args args) {
        assert args != null;

        if (DEBUG) Log.d(DEBUG_LOG_TAG, String.format("Loader.restart %1$s", args));

        mLastArgs = args;
        mLastResult = null;

        if (mActiveTask == null) {
            if (!args.isEmpty()) {
                mMainActivity.working();
                mActiveTask = new PBKDF2AsyncTask(mTaskListener, args);
                mActiveTask.execute();
            } else {
                mMainActivity.empty();
            }
        } else {
            mActiveTask.inputChanged(args);
        }
    }

    public Bookmark lastBookmark() {
        if (mLastArgs != null) {
            return mLastArgs.getBookmark();
        }
        return null;
    }

    public void onActivityPause() {
        if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.onActivityPause");

        mByConfigChange = false;

        mMainActivity.emptySecret();
    }

    public Loader onActivityGetRetainState() {
        if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.onActivityGetRetainState");

        mByConfigChange = true;

        return this;
    }

    public void onInit(MainActivity activity) {
        if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.onInit");

        mMainActivity = activity;
    }

    public void onActivityResume() {
        if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.onActivityResume");

        if (mByConfigChange) {
            if (mLastResult != null) {
                mMainActivity.restoreSecret(mLastArgs.password, mLastResult);
            } else if (mLastArgs.isEmpty()) {
                mMainActivity.empty();
            } else {
                mMainActivity.working();
            }
        } else {
            restart(mLastArgs.dropSecret());
        }
        mByConfigChange = false;
    }

    public void onActivityDestroy() {
        if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.onActivityDestroy");

        if (!mByConfigChange) {
            if (mActiveTask != null) {
                mActiveTask.cancel(false);
            }
        }
    }

}
