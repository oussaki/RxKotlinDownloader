package oussaki.libs.rxkotlindownloader

import java.io.File

/**
 * Created by oussama on 10/7/2017.
 */
data class FileContainer(
        var url: String,
        var filename: String,
        var progress: Int = 0,
        var isSucceed: Boolean = false,
        var isCanceled: Boolean = false) {

    lateinit var file: File
    lateinit var bytes: ByteArray

    fun Bytes(bytes: ByteArray) {
        this.isSucceed = true
        this.bytes = bytes
    }
}
