package oussaki.rxkotlindownloader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import oussaki.libs.rxkotlindownloader.RxDownloader

class Main : AppCompatActivity() {
    lateinit var rxDownloader: RxDownloader;
    val TAG = "Main"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(TAG, "This is an Example of how to use the library")
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
}
