package krasilnikov.alexey.cryptopass.v11;

import android.content.Context;
import android.content.Loader;
import android.os.AsyncTask;
import krasilnikov.alexey.cryptopass.Bookmark;
import krasilnikov.alexey.cryptopass.IIterationsListener;
import krasilnikov.alexey.cryptopass.PBKDF2Args;
import krasilnikov.alexey.cryptopass.PasswordMaker;

class GenerateLoader extends Loader<GenerateLoaderResult> implements IIterationsListener {

    static class SuccessResult extends GenerateLoaderResult {
        private final Bookmark args;
        private final String result;

        public SuccessResult(Bookmark a, String res) {
            args = a;
            result = res;
        }

        void result(IResultHandler handler) {
            handler.complete(args, result);
        }
    }

    static class ExceptionResult extends GenerateLoaderResult {
        private final Exception exception;

        public ExceptionResult(Exception ex) {
            exception = ex;
        }

        void result(IResultHandler handler) {
            handler.exception(exception);
        }
    }

    static class EmptyResult extends GenerateLoaderResult {
        void result(IResultHandler handler) {
            handler.empty();
        }
    }

    private PBKDF2Args mChangedArgs;
    private GenerateTask mActiveTask;

    public GenerateLoader(Context context) {
        super(context);

        mChangedArgs = null;
    }

    protected void onForceLoad() {
        if (mActiveTask == null) {
            mActiveTask = new GenerateTask();
            mActiveTask.executeOnExecutor(GenerateTask.THREAD_POOL_EXECUTOR);
        }
    }

    class GenerateTask extends AsyncTask<Void, Void, GenerateLoaderResult> {

        @Override
        protected GenerateLoaderResult doInBackground(Void... voids) {
            try {
                GenerateLoaderResult result = null;

                while (result == null && !isCancelled()) {
                    PBKDF2Args args = mChangedArgs;
                    mChangedArgs = null;

                    if (args.isEmpty()) {
                        result = new EmptyResult();
                    } else {
                        result = make(args);
                    }
                }

                return result;
            } catch (Exception e) {
                return new ExceptionResult(e);
            }
        }

        protected void onPostExecute(GenerateLoaderResult result) {
            mActiveTask = null;
            if (mChangedArgs == null) {
                deliverResult(result);
            } else {
                forceLoad();
            }
        }
    }

    public void clearArgs() {
        setArgs(new PBKDF2Args());
    }

    public void setArgs(PBKDF2Args args) {
        mChangedArgs = args;

        if (args.isEmpty()) {
            deliverResult(new EmptyResult());
        } else {
            forceLoad();
        }
    }

    GenerateLoaderResult make(PBKDF2Args args) throws Exception {
        String secret = args.password;
        String username = args.username;
        String url = args.url;

        String result = PasswordMaker.make(this, secret, username, url);

        if (result != null) {
            Bookmark bookmark = new Bookmark(url, username);

            return new SuccessResult(bookmark, result);
        }

        return null;
    }

    @Override
    public boolean afterIteration() {
        if (isAbandoned()) {
            return false;
        }

        return mChangedArgs == null;
    }
}
