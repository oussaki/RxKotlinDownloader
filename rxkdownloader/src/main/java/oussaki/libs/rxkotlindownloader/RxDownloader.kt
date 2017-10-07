package oussaki.libs.rxkotlindownloader

import android.content.Context
import android.util.Log
import android.util.Patterns
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by oussama abdallah , AKA oussaki on 10/5/2017 , 3:28 PM.
 */
class RxDownloader(
        var builder: Builder
) {
    val TAG: String = "RxDownloader"
    private var canceled: Boolean = false
    var ORDER: Int = DownloadStrategy.FLAG_PARALLEL
    private var errors: Int = 0
    private var size: Int = 0
    private var remains: Int = 0
    private var downloaded: Int = 0
    lateinit var context: Context
    lateinit var client: OkHttpClient
    lateinit var STORAGE: File
    var STRATEGY: Int = DownloadStrategy.DEFAULT

    lateinit private var subject: ReplaySubject<FileContainer>
    private lateinit var itemsObserver: ItemsObserver
    private lateinit var rxStorage: RxStorage
    /* Actions */
    private var onStart: OnStart? = null
    private var onError: OnError? = null
    private var onCompleteWithSuccess: OnCompleteWithSuccess? = null
    private var onCompleteWithError: OnCompleteWithError? = null
    private var onProgress: OnProgress? = null
    private lateinit var files: MutableList<FileContainer>

    /**
     * Action to be taken when error thrown for one single file
     *
     * @param action
     * @return RxDownloader
     */
    fun doOnEachSingleError(action: OnError): RxDownloader {
        this.onError = action
        this.itemsObserver.onError(action)
        return this
    }

    /**
     * doOnStart : Action to be taken before start downloading
     *
     * @param action
     * @return RxDownloader
     */
    fun doOnStart(action: OnStart): RxDownloader {
        this.onStart = action
        this.itemsObserver.onStart(action)
        return this
    }

    /**
     * doOnCompleteWithSuccess : Action to be taken when successfully finish downloading all the files
     *
     * @param action
     * @return RxDownloader
     */
    fun doOnCompleteWithSuccess(action: OnCompleteWithSuccess): RxDownloader {
        this.onCompleteWithSuccess = action
        this.itemsObserver.onCompleteWithSuccess(action)
        return this
    }

    /**
     * doOnCompleteWithError : Action to be taken when downloading ends with an error
     *
     * @param action
     * @return RxDownloader
     */
    fun doOnCompleteWithError(action: OnCompleteWithError): RxDownloader {
        this.onCompleteWithError = action
        this.itemsObserver.onCompleteWithError(action)
        return this
    }


    /**
     * doOnProgress(int progress) : On downloading files
     *
     * @param action
     * @return RxDownloader
     */
    fun doOnProgress(action: OnProgress): RxDownloader {
        this.onProgress = action
        this.itemsObserver.onProgress(action)
        return this
    }

    /**
     * Check if Object is null ot not
     *
     * @param obj
     * @return boolean
     */
    internal fun isNull(obj: Any?): Boolean {
        if (obj == null)
            Log.i(TAG, "Object is null")
        else
            Log.i(TAG, "Object " + obj.javaClass.toString() + " is not null")
        return obj == null
    }

    /*
    * Print the current thread name.
    * */
    private fun current_thread() {
        Log.e(TAG, "Thread:" + Thread.currentThread().name)
    }

    /**
     * Download a file from using an HTTP client
     *
     * @param url
     * @return byte[]
     */
    @Throws(IOException::class)
    internal fun downloadFile(url: String): ByteArray {
        return client.newCall(Request.Builder().url(url).build()).execute().body().bytes()
    }

    /**
     * @param bytes
     * @param emptyContainer
     * @return FileContainer
     */
    internal fun produceFileContainerFromBytes(bytes: ByteArray, emptyContainer: FileContainer): FileContainer {
        current_thread()
        Log.d(TAG, "fileContainer success" + emptyContainer.isSucceed)
        if (canceled && emptyContainer.isSucceed) {
            /*
            * Canceled file want be considered as downloaded files ( Ignored )
            * */
            Log.d(TAG, "emptyContainer.setCanceled")
            emptyContainer.isCanceled = true // to help filtration in ALL Strategy
        } else if (emptyContainer.isSucceed && !canceled) {
            val filename = emptyContainer.filename
            val file = File(STORAGE.toString() + File.separator + filename)
            var progress = 0
            if (size > 0)
                progress = Math.abs(remains * 100 / size - 100)

            emptyContainer.Bytes(bytes)
            emptyContainer.progress = progress;
            emptyContainer.file = file;
        }
        Log.e(TAG, "Empty container return")
        return emptyContainer
    }

    /**
     * @param fileContainer
     */
    private fun publishContainer(fileContainer: FileContainer) {
        current_thread()
        if (fileContainer.isSucceed)
            subject.onNext(fileContainer)

        Log.e(TAG, "publishContainer on next subject")
        if (remains == 0) {
            if (errors == 0)
                this.itemsObserver.CompleteWithSuccess()
            else
                this.itemsObserver.CompleteWithError()
            Log.i(TAG, remains.toString() + " i will throw on complete")
            //          subject.onCompleteWithSuccess(); // it was like this
        }
    }

    /**
     * @param bytes
     */
    private fun catchCanceling(bytes: ByteArray) {
        // cancel only if the strategy is ALL strategy
        Log.d(TAG, "catchCanceling  " + (bytes.size == 1 && STRATEGY == DownloadStrategy.ALL))
        if (bytes.size == 1 && STRATEGY == DownloadStrategy.ALL)
            canceled = true
    }

    /**
     * @param bytes
     * @param fileContainer
     */
    private fun catchDownloadError(bytes: ByteArray, fileContainer: FileContainer) {
        if (bytes.size == 1) {
            errors++
            fileContainer.isSucceed = false

        } else {
            downloaded++ // this variable is only for testing
            fileContainer.isSucceed = true
        }

        remains--
    }

    /**
     * @param fileContainer
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun handleDownloadError(fileContainer: FileContainer) {
        if (!fileContainer.isSucceed)
            throw IOException("Can not download the file " + fileContainer.filename)
    }


    /**
     * Get an observable of one file downloader
     *
     * @param fileContainer
     * @return
     */
    private fun ObservableFileDownloader(fileContainer: FileContainer): Observable<FileContainer> {
        var observable = Observable
                .fromCallable({ downloadFile(fileContainer.url) })
                .onErrorReturn({ throwable ->
                    Log.e(TAG, "throwable")
                    val b = ByteArray(1)
                    Log.e(TAG, "b.length:" + b.size)
                    b
                })
                .subscribeOn(Schedulers.io())
                .doOnNext({ bytes -> catchDownloadError(bytes, fileContainer) })
                .doOnNext({ bytes -> catchCanceling(bytes) })
                .map({ bytes -> produceFileContainerFromBytes(bytes, fileContainer) })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext({ fileContainerError -> handleDownloadError(fileContainerError) })

        if (STRATEGY == DownloadStrategy.ALL)
            observable = allStrategy(observable)
        else
            observable = maxStrategy(observable, fileContainer)

        return observable.doOnNext({ fileContainerOnNext -> publishContainer(fileContainerOnNext) })
                .filter({ fileContainer1 -> fileContainer1.isSucceed && !fileContainer1.isCanceled })
                .filter({ fileContainer1 ->
                    Log.d(TAG, "Filter 2" + fileContainer1.isCanceled)
                    //                    if(canceled && )
                    true
                })
    }

    /**
     * @param observable
     * @param fileContainer
     * @return
     */
    private fun maxStrategy(observable: Observable<FileContainer>, fileContainer: FileContainer): Observable<FileContainer> {
        Log.d(TAG, "Going to use max strategy")
        return observable
                .doOnError({ throwable ->
                    Log.d(TAG, "doOnError")
                    this.itemsObserver.onError(throwable)
                })
                .onErrorReturn({ error ->
                    Log.d(TAG, "onErrorReturn")
                    fileContainer
                })
    }

    /**
     * @param observable
     * @return
     */
    private fun allStrategy(observable: Observable<FileContainer>): Observable<FileContainer> {
        Log.d(TAG, "Going to use all strategy")
        return observable
                .doOnError({ throwable ->
                    Log.d(TAG, "doOnError")
                    this.itemsObserver.onError(throwable)
                })
                .onErrorResumeNext { observer: Observer<in FileContainer> ->
                    Log.d(TAG, "onErrorResumeNext all strategy")
                    observer.onComplete()
                    subject.onComplete()
                    this.itemsObserver.CompleteWithError()
                }
//                .onErrorResumeNext({ throwable ->
//
//                })
    }

    /**
     * Downloading files sequentially using concatMap
     *
     * @param observable
     * @return
     */
    private fun sequentialDownloading(observable: Observable<FileContainer>): Observable<FileContainer> {
        return observable.concatMap({ fileContainer -> ObservableFileDownloader(fileContainer) })
    }

    /**
     * Downloading the files in Parallel using FlatMap
     *
     * @param observable
     * @return
     */
    private fun parallelDownloading(observable: Observable<FileContainer>): Observable<FileContainer> {
        return observable.flatMap({ fileContainer -> ObservableFileDownloader(fileContainer) })
    }


    /**
     * Converts the downloaded files to be observable and consumed Reactively
     *
     * @return
     */
    fun asList(): Single<List<FileContainer>> {
        this.subject.subscribe(this.itemsObserver)
        this.size = this.files.size
        this.remains = this.size
        var observable = Observable
                .fromIterable(this.files)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())

        if (this.ORDER == DownloadStrategy.FLAG_PARALLEL)
            observable = parallelDownloading(observable)
        else if (this.ORDER == DownloadStrategy.FLAG_SEQUENTIAL)
            observable = sequentialDownloading(observable)

        return observable.toList()
    }


    /**
     * Builder Class
     */
    class Builder
    /**
     * @param context The context
     */
    (internal var context: Context) {
        internal var client: OkHttpClient
        internal var STRATEGY: Int = 0
        internal var ORDER: Int = 0
        internal var STORAGE: File
        internal var rxStorage: RxStorage
        /**
         * HashMap of files to be downloaded
         */
        internal var files: MutableList<FileContainer>

        init {
            STRATEGY = DownloadStrategy.DEFAULT
            ORDER = DownloadStrategy.FLAG_PARALLEL // default value
            client = OkHttpClient.Builder()
                    .connectTimeout(500, TimeUnit.MILLISECONDS)
                    .build()
            files = ArrayList<FileContainer>()
            this.STORAGE = context.cacheDir
            this.rxStorage = RxStorage(context)
            Log.i("RxDownloader", "Builder Constructor called")
        }


        /**
         * Set a custom Http Client (OkHttpClient )
         *
         * @param client and OkHttp instance
         * @return Builder
         */
        fun client(client: OkHttpClient): Builder {
            if (client != null)
                this.client = client
            return this
        }


        /**
         * Set the order of downloading files
         * it could be parallel or sequential
         *
         * @param order
         * @return Builder
         */
        fun Order(order: Int): Builder {
            ORDER = order
            return this
        }

        /**
         * Set strategy for downloading files
         * (MAX or ALL)
         *
         * @param strategy
         * @return Builder
         */
        fun strategy(strategy: Int): Builder {
            STRATEGY = strategy
            return this
        }

        /**
         * Add a URL of a file to the list of downloading
         * and rename it to the given name
         *
         * @param newName
         * @param url
         * @return Builder
         */
        fun addFile(newName: String, url: String): Builder {
            if (isUrl(url)) {
                var extesion = ""
                if (newName.indexOf(".") < 0)
                    extesion = ExtractExtension(url)
                files.add(FileContainer(url, newName + extesion))
            }
            return this
        }

        private fun isUrl(url: String): Boolean {
            return Patterns.WEB_URL.matcher(url.toLowerCase()).matches()
        }

        /**
         * Extract the extension of file from a given URL
         *
         * @param url
         * @return Builder
         */
        protected fun ExtractExtension(url: String): String {
            return url.substring(url.lastIndexOf("."))
        }

        /**
         * ُExtract the Name and extension of a given file URL
         *
         * @param url
         * @return Builder
         */
        protected fun ExtractNameAndExtension(url: String): String {
            return url.substring(url.lastIndexOf("/") + 1)
        }


        /**
         * Add file to downloading list
         *
         * @param url
         * @return Builder
         */
        fun addFile(url: String): Builder {
            if (isUrl(url)) {
                val name = ExtractNameAndExtension(url)
                files.add(FileContainer(url = url, filename = name))
            }
            return this
        }

        /**
         * Set the storage type to save files in
         *
         * @param storagePath
         * @return Builder
         */
        fun storage(storagePath: File): Builder {
            if (storagePath != null)
                this.STORAGE = storagePath
            return this
        }

        /**
         * Add Bulk of files to the List of files
         *
         * @param urls
         * @return Builder
         */
        fun addFiles(urls: List<String>): Builder {
            for (url in urls) {
                val newFileContainer = FileContainer(url, ExtractNameAndExtension(url))
                this.files.add(newFileContainer)
            }
            return this
        }

        fun build(): RxDownloader {
            return RxDownloader(builder = this)
        }
    }


}