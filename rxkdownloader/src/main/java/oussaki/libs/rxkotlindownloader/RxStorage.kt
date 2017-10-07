package oussaki.libs.rxkotlindownloader

import android.content.Context
import java.io.*

/**
 * Created by oussama on 10/7/2017.
 */
class RxStorage internal constructor(internal var context: Context) {

    /**
     * Save an InputStream into a file
     *
     * @param is
     * @param file
     * @throws IOException
     */
    @Throws(IOException::class)
    internal fun saveToFile(`is`: InputStream, file: File) {
        val input = BufferedInputStream(`is`)
        val output = FileOutputStream(file)
        val data = ByteArray(1024)
        var total: Long = 0
        var count: Int = -1
        while ({ count = input.read(data); count }() != -1){
            total += count.toLong()
            output.write(data, 0, count)
        }
        output.flush()
        output.close()
        input.close()
    }

    @Throws(IOException::class)
    internal fun saveToFile(`is`: ByteArray, file: File) {
        val outputStream: FileOutputStream
        try {
            outputStream = context.openFileOutput(file.name, Context.MODE_PRIVATE)
            outputStream.write(`is`)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    companion object {
        internal var DATA_DIRECTORY = 0 // unused for now maybe in next API version
        internal var EXTERNAL_CACHE_DIR = 1 // unused for now maybe in next API version
    }
}