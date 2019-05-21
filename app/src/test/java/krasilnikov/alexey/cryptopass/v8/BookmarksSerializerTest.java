package krasilnikov.alexey.cryptopass.v8;

import android.database.MatrixCursor;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import krasilnikov.alexey.cryptopass.Data;
import krasilnikov.alexey.cryptopass.sync.BookmarksSerializer;

@RunWith(RobolectricTestRunner.class)
public class BookmarksSerializerTest {

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
    public void oneObject() throws JSONException {
        String data = mSerializer.serialize(makeOneRowData());
        JSONArray array = new JSONArray(data);

        Assert.assertEquals(1, array.length());
        Assert.assertEquals("url", array.getJSONObject(0).get("url"));
        Assert.assertEquals("user", array.getJSONObject(0).get("username"));
        Assert.assertEquals(10, array.getJSONObject(0).get("length"));
    }

    @Test
    public void twoRows() throws JSONException {
        String data = mSerializer.serialize(makeTwoRowsData());
        JSONArray array = new JSONArray(data);

        Assert.assertEquals(2, array.length());
        Assert.assertEquals("url", array.getJSONObject(0).get("url"));
        Assert.assertEquals("user", array.getJSONObject(0).get("username"));
        Assert.assertEquals(10, array.getJSONObject(0).get("length"));
        Assert.assertEquals("url2", array.getJSONObject(1).get("url"));
        Assert.assertEquals("user2", array.getJSONObject(1).get("username"));
        Assert.assertEquals(11, array.getJSONObject(1).get("length"));
    }

    @Test
    public void scrolledCursor() throws JSONException {
        MatrixCursor cursor = makeTwoRowsData();
        cursor.moveToLast();

        String data = mSerializer.serialize(cursor);
        JSONArray array = new JSONArray(data);

        Assert.assertEquals(2, array.length());
    }

    @Test
    public void onlyUrl() throws JSONException {
        String data = mSerializer.serialize(makeOneRowOnlyUrlData());
        JSONArray array = new JSONArray(data);

        Assert.assertEquals(1, array.length());
        Assert.assertEquals("url", array.getJSONObject(0).get("url"));
        Assert.assertTrue(array.getJSONObject(0).isNull("username"));
        Assert.assertEquals(16, array.getJSONObject(0).get("length"));
    }

    @Test
    public void fileFormatter() throws JSONException {
        String data = mSerializer.serialize(makeOneRowData());
        String lines[] = data.split("\\r?\\n");
        Assert.assertEquals(data, 7, lines.length);
    }
}
