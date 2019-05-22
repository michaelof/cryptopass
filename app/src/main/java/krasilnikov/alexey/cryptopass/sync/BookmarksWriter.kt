package krasilnikov.alexey.cryptopass.sync

import android.annotation.TargetApi
import android.os.Build
import krasilnikov.alexey.cryptopass.Data
import krasilnikov.alexey.cryptopass.data.BookmarksStorage
import org.json.JSONException
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import javax.inject.Inject

/**
 * Helper class for writing bookmarks to given file.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class BookmarksWriter @Inject
constructor(private val bookmarksStorage: BookmarksStorage, private val bookmarksSerializer: BookmarksSerializer) {

    /**
     * Write bookmarks to given output steam. Stream will be closed after that.
     */
    @Throws(IOException::class, JSONException::class)
    fun write(outputStream: OutputStream) {
        OutputStreamWriter(outputStream).use { fileWriter ->

            bookmarksStorage.queryBookmarks(Data.BOOKMARKS_PROJECTION).use { c ->
                fileWriter.write(bookmarksSerializer.serialize(c))
            }
        }
    }

}
