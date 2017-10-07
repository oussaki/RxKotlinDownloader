package oussaki.rxkotlindownloader

import android.icu.util.TimeUnit
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import okhttp3.OkHttpClient
import oussaki.libs.rxkotlindownloader.*

class Main : AppCompatActivity() {
    lateinit var rxDownloader: RxDownloader;
    val TAG = "Main"
    lateinit var btnDownload: Button
    lateinit var progressBar: ProgressBar
    lateinit var txtStatus: TextView

    fun initUI() {
        btnDownload = findViewById(R.id.btnDownload) as Button
        progressBar = findViewById(R.id.progressBar) as ProgressBar
        txtStatus = findViewById(R.id.txtStatus) as TextView
    }

    fun example1(): Unit {
        rxDownloader = RxDownloader
                .Builder(applicationContext)
                .addFile("http://reactivex.io/assets/Rx_Logo_S.png")
                .build();

        rxDownloader.asList()
                .subscribe({ files, throwable ->
                    Log.d(TAG, "Just received ${files.size} file")
                    files.forEachIndexed { index, fileContainer ->
                        Log.d(TAG, "File in position: ${index} is ${fileContainer}")
                    }
                });
    }

    fun example2() {
        RxDownloader
                .Builder(applicationContext)
                .addFile("http://reactivex.io/assets/Rx_Logo_S.png")
                .build()
                .doOnProgress(action = object : OnProgress {
                    override fun run(progress: Int) {
                        progressBar.progress = progress
                    }
                })
                .doOnStart(action = object : OnStart {
                    override fun run() {
                        // do something useful here
                    }
                })
                .doOnEachSingleError(action = object : OnError {
                    override fun run(e: Throwable) {
                        // do something useful here also :D

                    }
                })
                .doOnCompleteWithError(action = object : OnCompleteWithError {
                    override fun run() {
                        // TODO("not implemented")
                    }
                })
                .doOnCompleteWithSuccess(action = object : OnCompleteWithSuccess {
                    override fun run() {
                        txtStatus.text = "Downloading ended successfully"
                    }
                })
                .asList()
                .subscribe({ files, error ->

                });
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
        btnDownload.setOnClickListener(View.OnClickListener { view ->
            example2()
        })
        Log.i(TAG, "This is an Example of how to use the library")
    }
}
