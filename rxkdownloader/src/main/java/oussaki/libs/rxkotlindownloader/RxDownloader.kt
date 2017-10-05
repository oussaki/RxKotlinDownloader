package oussaki.libs.rxkotlindownloader

import android.content.Context
import java.io.File

/**
 * Created by oussama abdallah , AKA oussaki on 10/5/2017 , 3:28 PM.
 */
class RxDownloader {
    val TAG = "RxDownloader"
    private val context: Context
    private var errors = 0
    private var size: Int = 0
    private var remains: Int = 0
    //    private val subject: ReplaySubject<FileContainer>
//    private val itemsObserver: ItemsObserver
//    private val rxStorage: RxStorage
    /* Actions */
//    private var onStart: OnStart? = null
//    private var onError: OnError? = null
//    private var onCompleteWithSuccess: OnCompleteWithSuccess? = null
//    private var onCompleteWithError: OnCompleteWithError? = null
//    private var onProgress: OnProgress? = null
//    private val client: OkHttpClient
    private var STRATEGY: Int = 0
    private lateinit var STORAGE: File
    //    private val files: MutableList<FileContainer>
    private var downloaded = 0
    private var canceled = false

    constructor(context: Context) {
        this.context = context
    }

    fun Storage(storage: File): Unit {
        this.STORAGE = storage
    }


}