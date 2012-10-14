package krasilnikov.alexey.cryptopass.v8;

import krasilnikov.alexey.cryptopass.Bookmark;
import krasilnikov.alexey.cryptopass.PBKDF2Args;

import android.util.Log;

class Loader
{
	private static final boolean DEBUG = false;
	private static final String DEBUG_LOG_TAG = "cryptopass";
	
	private MainActivity mainActivity;

	private PBKDF2AsyncTask activeTask = null;

	private PBKDF2Args lastArgs = new PBKDF2Args();
	private String lastResult;
	
	private boolean byConfigChange = false;
	
	public void restart(PBKDF2Args args) {
		assert args != null;
		
		if (DEBUG) Log.d(DEBUG_LOG_TAG, String.format("Loader.restart %1$s", args));
		
		lastArgs = args;
		lastResult = null;
		
		if (activeTask == null)
		{
			if (!args.isEmpty())
			{
				mainActivity.working();
				activeTask = new PBKDF2AsyncTask(taskListener, args);
				activeTask.execute();				
			}
			else
			{
				mainActivity.empty();
			}
		}
		else
		{
			activeTask.inputChanged(args);
		}
	}
	

	public Bookmark lastBookmark()
	{
		if (lastArgs != null) {
			return lastArgs.getBookmark();
		}
		return null;
	}
	
	public void onActivityPause() {
		if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.onActivityPause");
		
		byConfigChange = false;
		
		mainActivity.emptySecret();
	}
	
	public Loader onActivityGetRetainState() {
		if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.onActivityGetRetainState");
		
		byConfigChange = true;
		
		return this;
	}
	
	public void onInit(MainActivity activity) {
		if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.onInit");
		
		mainActivity = activity;
	}
	
	public void onActivityResume() {
		if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.onActivityResume");
		
		if (byConfigChange) {
			if (lastResult != null) {
				mainActivity.restoreSecret(lastArgs.password, lastResult);
			} else if (lastArgs.isEmpty()) {
				mainActivity.empty();
			} else {
				mainActivity.working();
			}
		} else {
			restart(lastArgs.dropSecret());
		}
		byConfigChange = false;
	}
	
	public void onActivityDestroy() {
		if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.onActivityDestroy");
		
		if (!byConfigChange) {
			if (activeTask != null)
			{
				activeTask.cancel(false);
			}
		}
	}
	
	private final PBKDF2AsyncTask.ResultListener taskListener = new PBKDF2AsyncTask.ResultListener() {
		@Override
		public void restart(PBKDF2Args args)
		{
			if (DEBUG) Log.d(DEBUG_LOG_TAG, String.format("Loader.listener.restart %1$s", args));
			
			activeTask = null;
			
			Loader.this.restart(args);			
		}
		
		@Override
		public void exception(Exception occuredException)
		{
			if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.listener.exception", occuredException);
			
			activeTask = null;
			mainActivity.exception(occuredException);			
		}
		
		@Override
		public void empty()
		{
			if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.listener.empty");
			
			activeTask = null;
			mainActivity.empty();			
		}
		
		@Override
		public void complete(Bookmark args, String result)
		{
			if (DEBUG) Log.d(DEBUG_LOG_TAG, "Loader.listener.complete");
			
			lastResult = result;
			activeTask = null;
			mainActivity.complete(result);
		}
	};

}
