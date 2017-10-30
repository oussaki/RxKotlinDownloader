package oussaki.libs.rxkotlindownloader

import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.io.IOException
import java.util.*

/**
 * Created by oussama on 10/7/2017.
 */

class ItemsObserver(internal var rxStorage: RxStorage?) : Observer<FileContainer> {

    internal lateinit var filesContainer: MutableList<FileContainer>
    internal var TAG = "ItemsObserver"

    internal lateinit var onStart: () -> Unit
    internal lateinit var omError: (e: Throwable) -> Unit
    internal lateinit var onProgress: (progress: Int) -> Unit

    internal lateinit var onCompleteWithSuccess: () -> Unit
    internal lateinit var onCompleteWithError: () -> Unit

    fun onStart(onStart: () -> Unit) {
        this.onStart = onStart
    }

    fun onError(onError: (e: Throwable) -> Unit) {
        this.omError = onError
    }

    fun onCompleteWithSuccess(onCompleteWithSuccess: () -> Unit) {
        this.onCompleteWithSuccess = onCompleteWithSuccess
    }

    fun onCompleteWithError(onCompleteWithError: () -> Unit) {
        this.onCompleteWithError = onCompleteWithError
    }

    fun onProgress(onProgress: (prog: Int) -> Unit) {
        this.onProgress = onProgress
    }


    override fun onSubscribe(d: Disposable) {
        filesContainer = ArrayList()
        // UI interaction and initialization
        onStart?.invoke()
//        Log.d(TAG, "ItemsObserver onSubscribe")
    }

    override fun onNext(fileContainer: FileContainer) {
//        Log.e(TAG, "Im inside on next")
        if (fileContainer.isSucceed) {
            try {
                rxStorage?.saveToFile(fileContainer?.bytes, fileContainer?.file) // save file
//                Log.d(TAG, " First onNext value : " + fileContainer?.file.getName())
                filesContainer?.add(fileContainer)
                onProgress?.invoke(fileContainer.progress)
            } catch (e: IOException) {
                onError(IllegalStateException("Can't not save file:" + fileContainer.file.getName()))
            }

        } else
            onError(IllegalStateException("Can't not download the file:"))
    }

    override fun onError(e: Throwable) {
//        Log.d(TAG, " ItemObserver onError : " + e?.message)
        this.omError?.invoke(e)
    }

    fun CompleteWithError() {
        onCompleteWithError?.invoke()
//        Log.d(TAG, "Download end-up with error")
        onComplete()
    }

    fun CompleteWithSuccess() {
        onCompleteWithSuccess?.invoke()
//        Log.d(TAG, "Download Complete with success")
        onComplete()
    }

    override fun onComplete() {

    }
}
