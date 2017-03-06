package krasilnikov.alexey.cryptopass.sync;

import android.database.MatrixCursor;

import junit.framework.Assert;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import krasilnikov.alexey.cryptopass.BuildConfig;
import krasilnikov.alexey.cryptopass.Data;
import krasilnikov.alexey.cryptopass.data.BookmarksStorage;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class BookmarksWriterTest {
    private MatrixCursor makeMatrix() {
        return new MatrixCursor(Data.BOOKMARKS_PROJECTION);
    }

    private MatrixCursor makeOneRowData() {
        MatrixCursor cursor = makeMatrix();
        cursor.addRow(new Object[]{0, "user", "url", 10});
        return cursor;
    }
    @Test
    public void outputFormatted() throws IOException, JSONException {
        BookmarksStorage storage = Mockito.mock(BookmarksStorage.class);
        Mockito.when(storage.queryBookmarks(Mockito.<String[]>any())).thenReturn(makeOneRowData());

        BookmarksWriter writer = new BookmarksWriter(storage, new BookmarksSerializer());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        writer.write(outputStream);

        String data = outputStream.toString();
        String lines[] = data.split("\\r?\\n");
        Assert.assertEquals(data, 7, lines.length);

    }
}
