package krasilnikov.alexey.cryptopass.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "bookmarks.sqlite";
    private static final int DATABASE_VERSION = 2;

    public static final String BOOKMARKS_TABLE = "bookmarks";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE bookmarks (_id INTEGER PRIMARY KEY, username TEXT, url TEXT, length INT, UNIQUE (username,url) ON CONFLICT REPLACE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            db.execSQL("ALTER TABLE bookmarks ADD COLUMN length INT");
        }
    }
}
