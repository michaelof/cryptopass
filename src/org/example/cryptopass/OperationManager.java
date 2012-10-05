package org.example.cryptopass;

import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;

public class OperationManager {
	public interface OperationListener {
		void onOperationStarted(Uri uri);

		void onOperationEnded(Uri uri);
	}

	public abstract static class AbstractOperationListener implements OperationListener {
		protected abstract boolean isInterest(Uri uri);

		protected abstract void onOperationStarted();

		protected abstract void onOperationEnded();

		@Override
		public void onOperationStarted(Uri uri) {
			if (isInterest(uri)) {
				onOperationStarted();
			}
		}

		@Override
		public void onOperationEnded(Uri uri) {
			if (isInterest(uri)) {
				onOperationEnded();
			}
		}
	}

	private HashMap<Uri, Object> mOperationMap = new HashMap<Uri, Object>();

	private ArrayList<OperationListener> mListenersArray = new ArrayList<OperationListener>();

	public synchronized boolean isInOperation() {
		return !mOperationMap.isEmpty();
	}

	public synchronized void subscribe(OperationListener listener) {
		mListenersArray.add(listener);
	}

	public synchronized void unsubscribe(OperationListener listener) {
		while (mListenersArray.remove(listener)) ;
	}

	public synchronized Object operationStarted(Uri uri) {
		final Object obj;
		synchronized (this) {
			if (mOperationMap.containsKey(uri)) {
				throw new RuntimeException();
			}

			obj = new Object();

			mOperationMap.put(uri, obj);
		}

		for (OperationListener listener : mListenersArray) {
			listener.onOperationStarted(uri);
		}

		return obj;
	}

	public void operationEnded(Uri uri, Object obj) {
		synchronized (this) {
			Object existing = mOperationMap.get(uri);
			if (existing != obj) {
				throw new RuntimeException();
			}

			mOperationMap.remove(uri);
		}

		for (OperationListener listener : mListenersArray) {
			listener.onOperationEnded(uri);
		}
	}

	private OperationManager() {
	}

	public static OperationManager getInstance() {
		return InstanceHolder.mInstance;
	}

	public static final class InstanceHolder {
		public static final OperationManager mInstance = new OperationManager();
	}
}
