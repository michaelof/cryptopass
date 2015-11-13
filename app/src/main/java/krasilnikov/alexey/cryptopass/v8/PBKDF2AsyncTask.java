package krasilnikov.alexey.cryptopass.v8;

import android.os.AsyncTask;
import android.util.Base64;

import krasilnikov.alexey.cryptopass.Bookmark;
import krasilnikov.alexey.cryptopass.IterationsListener;
import krasilnikov.alexey.cryptopass.PBKDF2Args;
import krasilnikov.alexey.cryptopass.PBKDF2KeyGenerator;

public class PBKDF2AsyncTask extends AsyncTask<Void, Void, PBKDF2AsyncTaskResult> implements IterationsListener {

    static class SuccessResult extends PBKDF2AsyncTaskResult {
        private final Bookmark mArgs;
        private final String mResult;

        public SuccessResult(Bookmark a, String res) {
            mArgs = a;
            mResult = res;
        }

        public void result(ResultListener listener) {
            listener.complete(mArgs, mResult);
        }
    }

    static class ExceptionResult extends PBKDF2AsyncTaskResult {
        private final Exception mException;

        public ExceptionResult(Exception ex) {
            mException = ex;
        }

        public void result(ResultListener listener) {
            listener.exception(mException);
        }
    }

    static class EmptyResult extends PBKDF2AsyncTaskResult {
        public void result(ResultListener listener) {
            listener.empty();
        }
    }

    private final ResultListener mResultListener;
    private PBKDF2Args mChangedGenerateArgs;

    public PBKDF2AsyncTask(ResultListener listener, PBKDF2Args args) {
        mResultListener = listener;
        mChangedGenerateArgs = args;
    }

    public boolean afterIteration() {
        return !isCancelled() && mChangedGenerateArgs == null;
    }

    public void inputChanged(PBKDF2Args args) {
        assert args != null;

        mChangedGenerateArgs = args;
    }

    private PBKDF2AsyncTaskResult make(PBKDF2Args args) throws Exception {
        String password = args.password;
        String username = args.username;
        String url = args.url;

        if (password == null || password.length() == 0) {
            return new EmptyResult();
        } else {
            String salt = username + "@" + url;

            PBKDF2KeyGenerator generator = new PBKDF2KeyGenerator(32, 5000, "HmacSHA256");
            byte[] digest = generator.generateKey(this, password, salt.getBytes("UTF-8"));

            if (digest != null) {
                String result = Base64.encodeToString(digest, Base64.DEFAULT);
                Bookmark bookmark = new Bookmark(url, username);

                return new SuccessResult(bookmark, result);
            }

            return null;
        }
    }

    @Override
    protected PBKDF2AsyncTaskResult doInBackground(Void... arg0) {
        try {
            PBKDF2AsyncTaskResult result = null;

            while (result == null && !isCancelled()) {
                PBKDF2Args args = mChangedGenerateArgs;
                mChangedGenerateArgs = null;

                result = make(args);
            }

            return result;
        } catch (Exception e) {
            return new ExceptionResult(e);
        }
    }

    protected void onPostExecute(PBKDF2AsyncTaskResult result) {
        if (mChangedGenerateArgs == null) {
            result.result(mResultListener);
        } else {
            mResultListener.restart(mChangedGenerateArgs);
        }
    }
}
