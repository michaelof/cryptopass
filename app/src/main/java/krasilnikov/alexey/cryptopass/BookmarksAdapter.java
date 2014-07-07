package krasilnikov.alexey.cryptopass;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class BookmarksAdapter extends ResourceCursorAdapter {
	public BookmarksAdapter(Context context, Cursor c) {
		super(context, R.layout.row_bookmark, c, true);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView tv = (TextView) view;

		String url = cursor.getString(Data.URL_COLUMN);
		String username = cursor.getString(Data.USERNAME_COLUMN);

		String text = String.format("%s @ %s", username, url);

		tv.setText(text);
	}
}
