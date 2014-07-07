package krasilnikov.alexey.cryptopass;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;

public class ActionService extends IntentService {
	public ActionService() {
		super("cryptopass");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();

		if (Data.ACTION_SAVE.equals(action)) {
			ContentValues values = new ContentValues();

			String username = intent.getStringExtra(Data.ARGS_USERNAME);
			String url = intent.getStringExtra(Data.ARGS_URL);
			int length = intent.getIntExtra(Data.ARGS_LENGTH, Data.DEFAULT_LENGTH);

			values.put(Data.ARGS_USERNAME, username);
			values.put(Data.ARGS_URL, url);
			values.put(Data.ARGS_LENGTH, length);

            Uri bookmarksUri = Data.makeBookmarksUri(this);
			Object obj = OperationManager.getInstance().operationStarted(bookmarksUri);
			try {
				getContentResolver().insert(bookmarksUri, values);
			} finally {
				OperationManager.getInstance().operationEnded(bookmarksUri, obj);
			}

		} else if (Data.ACTION_DELETE.equals(action)) {
			Uri uri = intent.getData();

			Object obj = OperationManager.getInstance().operationStarted(uri);
			try {
				getContentResolver().delete(uri, null, null);
			} finally {
				OperationManager.getInstance().operationEnded(uri, obj);
			}
		}
	}
}
