package krasilnikov.alexey.cryptopass;

import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This class intended to link UI progress and actual operations.
 */
@Singleton
public class ProgressNotifier {
    @Inject
    public ProgressNotifier() {
    }

    /**
     * Listener to be notified about the beginning and the end of the operations.
     * Please be aware, methods can be called from any thread.
     */
    public interface OperationListener {
        void onOperationStarted(Uri uri);

        void onOperationEnded(Uri uri);
    }

    private final HashMap<Uri, Object> mOperationMap = new HashMap<>();

    private final ArrayList<OperationListener> mListenersArray = new ArrayList<>();

    public synchronized boolean isInOperation() {
        return !mOperationMap.isEmpty();
    }

    public synchronized void subscribe(OperationListener listener) {
        mListenersArray.add(listener);
    }

    public synchronized void unsubscribe(OperationListener listener) {
        while (mListenersArray.remove(listener)) ;
    }

    /**
     * This method should be called when long operation started.
     * Can be called from any thread.
     * @return the operation id.
     */
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
}
