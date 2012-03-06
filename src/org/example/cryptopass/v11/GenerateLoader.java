package org.example.cryptopass.v11;

import android.content.Context;
import android.content.Loader;
import android.os.AsyncTask;
import org.example.cryptopass.Bookmark;
import org.example.cryptopass.IIterationsListener;
import org.example.cryptopass.PBKDF2Args;
import org.example.cryptopass.PasswordMaker;

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

	private PBKDF2Args mChangedArgs;
	private GenerateTask mActiveTask;

	public GenerateLoader(Context context) {
		super(context);

		mChangedArgs = null;
	}

	protected void onAbandon() {
		super.onAbandon();

		if (mActiveTask != null) {
			mActiveTask.cancel(false);
		}
	}

	protected void onReset() {
		super.onReset();

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

					result = make(args);
				}

				return result;
			} catch (Exception e) {
				return new ExceptionResult(e);
			}
		}

		protected void onPostExecute(GenerateLoaderResult result) {
			mActiveTask = null;
			if (mChangedArgs == null)
			{
				deliverResult(result);
			}
			else
			{
				forceLoad();
			}
		}
	}

	public void setArgs(PBKDF2Args args) {
		mChangedArgs = args;
		forceLoad();
	}

	GenerateLoaderResult make(PBKDF2Args args) throws Exception {
		String secret = args.password;
		String username = args.username;
		String url = args.url;

		String result = PasswordMaker.make(this, secret, username, url, 25);

		if (result != null) {
			Bookmark bookmark = new Bookmark(url, username);

			return new SuccessResult(bookmark, result);
		}

		return null;
	}

	@Override
	public boolean afterIteration() {
		if (isReset()) {
			return false;
		}

		return mChangedArgs == null;
	}
}
