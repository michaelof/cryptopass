package org.example.cryptopass.v8;

import android.os.AsyncTask;
import android.util.Base64;
import org.example.cryptopass.Bookmark;
import org.example.cryptopass.IIterationsListener;
import org.example.cryptopass.PBKDF2Args;
import org.example.cryptopass.PBKDF2KeyGenerator;

public class PBKDF2AsyncTask extends AsyncTask<Void, Void, PBKDF2AsyncTaskResult> implements IIterationsListener
{
	public interface ResultListener
	{
		void exception(Exception occuredException);

		void complete(Bookmark args, String result);

		void restart(PBKDF2Args args);

		void empty();
	}

	static class SuccessResult extends PBKDF2AsyncTaskResult
	{
		private final Bookmark args;
		private final String result;

		public SuccessResult(Bookmark a, String res)
		{
			args = a;
			result = res;
		}

		public void result(PBKDF2AsyncTask task)
		{
			task.resultListener.complete(args, result);
		}
	}

	static class ExceptionResult extends PBKDF2AsyncTaskResult
	{
		private final Exception exception;

		public ExceptionResult(Exception ex)
		{
			exception = ex;
		}

		public void result(PBKDF2AsyncTask task)
		{
			task.resultListener.exception(exception);
		}
	}

	static class EmptyResult extends PBKDF2AsyncTaskResult
	{
		public void result(PBKDF2AsyncTask task)
		{
			task.resultListener.empty();
		}
	}

	private ResultListener resultListener;
	private PBKDF2Args changedGenerateArgs;

	public PBKDF2AsyncTask(ResultListener listener, PBKDF2Args args)
	{
		resultListener = listener;
		changedGenerateArgs = args;
	}

	public boolean afterIteration()
	{
		return !isCancelled() && changedGenerateArgs == null;
	}

	public void inputChanged(PBKDF2Args args)
	{
		assert args != null;

		changedGenerateArgs = args;
	}

	private PBKDF2AsyncTaskResult make(PBKDF2Args args) throws Exception
	{
		String password = args.password;
		String username = args.username;
		String url = args.url;

		if (password == null || password.length() == 0)
		{
			return new EmptyResult();
		}
		else
		{
			String salt = username + "@" + url;

			PBKDF2KeyGenerator generator = new PBKDF2KeyGenerator(32, 5000, "HmacSHA256");
			byte[] digest = generator.generateKey(this, password, salt.getBytes("UTF-8"));

			if (digest != null)
			{
				String result = Base64.encodeToString(digest, Base64.DEFAULT);
				Bookmark bookmark = new Bookmark(url, username);

				return new SuccessResult(bookmark, result);
			}

			return null;
		}
	}

	@Override
	protected PBKDF2AsyncTaskResult doInBackground(Void... arg0)
	{
		try
		{
			PBKDF2AsyncTaskResult result = null;

			while (result == null && !isCancelled())
			{
				PBKDF2Args args = changedGenerateArgs;
				changedGenerateArgs = null;

				result = make(args);
			}

			return result;
		}
		catch (Exception e)
		{
			return new ExceptionResult(e);
		}
	}

	protected void onPostExecute(PBKDF2AsyncTaskResult result)
	{
		if (changedGenerateArgs == null)
		{
			result.result(this);
		}
		else
		{
			resultListener.restart(changedGenerateArgs);
		}
	}
}
