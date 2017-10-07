package oussaki.libs.rxkotlindownloader

import android.util.Log
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
    internal var onStart: OnStart? = null
    internal var mOError: OnError? = null
    internal var onCompleteWithSuccess: OnCompleteWithSuccess? = null
    internal var onCompleteWithError: OnCompleteWithError? = null
    internal var onProgress: OnProgress? = null

    fun onStart(onStart: OnStart) {
        this.onStart = onStart
    }

    fun onError(onError: OnError) {
        this.mOError = onError
    }

    fun onCompleteWithSuccess(onCompleteWithSuccess: OnCompleteWithSuccess) {
        this.onCompleteWithSuccess = onCompleteWithSuccess
    }

    fun onCompleteWithError(onCompleteWithError: OnCompleteWithError) {
        this.onCompleteWithError = onCompleteWithError
    }

    fun onProgress(onProgress: OnProgress) {
        this.onProgress = onProgress
    }


    override fun onSubscribe(d: Disposable) {
        filesContainer = ArrayList()
        // UI interaction and initialization
        onStart?.run()
        Log.d(TAG, "ItemsObserver onSubscribe")
    }

    override fun onNext(fileContainer: FileContainer) {
        Log.e(TAG, "Im inside on next")
        if (fileContainer.isSucceed) {
            try {
                rxStorage?.saveToFile(fileContainer?.bytes, fileContainer?.file) // save file
                Log.d(TAG, " First onNext value : " + fileContainer?.file.getName())
                filesContainer?.add(fileContainer)
                onProgress?.run(fileContainer.progress)
            } catch (e: IOException) {
                onError(IllegalStateException("Can't not save file:" + fileContainer.file.getName()))
            }

        } else
            onError(IllegalStateException("Can't not download the file:"))
    }

    override fun onError(e: Throwable) {
        Log.d(TAG, " ItemObserver onError : " + e?.message)
        if (e != null)
            this.mOError?.run(e)
    }

    fun CompleteWithError() {
        // TO-DO
        onCompleteWithError?.run()
        Log.d(TAG, "Download end-up with error")
        onComplete()
    }

    fun CompleteWithSuccess() {
        // TO-DO
        onCompleteWithSuccess?.run()
        Log.d(TAG, "Download Complete with success")
        onComplete()
    }

    override fun onComplete() {

    }
}
