package krasilnikov.alexey.cryptopass.v8;

import android.database.MatrixCursor;
import android.os.Build;

import junit.framework.Assert;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import krasilnikov.alexey.cryptopass.BuildConfig;
import krasilnikov.alexey.cryptopass.Data;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.JELLY_BEAN)
public class BookmarksSerializerTest {
    private final String EXPECTED_ONE_ROW = "[\n" +
            "  {\n" +
            "    \"username\": \"user\",\n" +
            "    \"length\": 10,\n" +
            "    \"url\": \"url\"\n" +
            "  }\n" +
            "]";

    private final String EXPECTED_TWO_ROWS = "[\n" +
            "  {\n" +
            "    \"username\": \"user\",\n" +
            "    \"length\": 10,\n" +
            "    \"url\": \"url\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"username\": \"user2\",\n" +
            "    \"length\": 11,\n" +
            "    \"url\": \"url2\"\n" +
            "  }\n" +
            "]";

    private final String EXPECTED_ONLY_URL = "[\n" +
            "  {\n" +
            "    \"length\": 16,\n" +
            "    \"url\": \"url\"\n" +
            "  }\n" +
            "]";

    private MatrixCursor makeMatrix() {
        return new MatrixCursor(Data.BOOKMARKS_PROJECTION);
    }

    private MatrixCursor makeOneRowData() {
        MatrixCursor cursor = makeMatrix();
        cursor.addRow(new Object[]{0, "user", "url", 10});
        return cursor;
    }

    private MatrixCursor makeTwoRowsData() {
        MatrixCursor cursor = makeOneRowData();
        cursor.addRow(new Object[]{0, "user2", "url2", 11});
        return cursor;
    }

    private MatrixCursor makeOneRowOnlyUrlData() {
        MatrixCursor cursor = makeMatrix();
        cursor.addRow(new Object[]{0, null, "url", 16});
        return cursor;
    }

    private BookmarksSerializer mSerializer = new BookmarksSerializer();

    @Test
    public void noRow() throws JSONException {
        Assert.assertEquals("[]", mSerializer.serialize(makeMatrix()));
    }

    @Test
    public void oneRow() throws JSONException {
        Assert.assertEquals(EXPECTED_ONE_ROW, mSerializer.serialize(makeOneRowData()));
    }

    @Test
    public void twoRows() throws JSONException {
        Assert.assertEquals(EXPECTED_TWO_ROWS, mSerializer.serialize(makeTwoRowsData()));
    }

    @Test
    public void scrolledCursor() throws JSONException {
        MatrixCursor cursor = makeTwoRowsData();
        cursor.moveToLast();
        Assert.assertEquals(EXPECTED_TWO_ROWS, mSerializer.serialize(cursor));
    }

    @Test
    public void onlyUrl() throws JSONException {
        Assert.assertEquals(EXPECTED_ONLY_URL, mSerializer.serialize(makeOneRowOnlyUrlData()));
    }
}
