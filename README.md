#  Reactive Downloader Library for Android in Kotlin

[![Build Status](https://api.travis-ci.org/oussaki/RxKotlinDownloader.svg?branch=master)](https://travis-ci.org/oussaki/RxKotlinDownloader)
[![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=14)

This library provides a simple api to download files and handle data stream in a reactive way 
it's completly written in Kotlin Language 

# Usage
	

Example:

```kotlin

// Create one instance 

fun downloader():Unit{
 rxDownloader = RxDownloader
                .Builder(applicationContext)
                .addFile("http://reactivex.io/assets/Rx_Logo_S.png")
                .build();

// Subscribe to start downloading files 
     rxDownloader.asList()
                .subscribe({ files, throwable ->
                    Log.d(TAG, "Just received ${files.size} file")
                    files.forEachIndexed { index, fileContainer ->
                        Log.d(TAG, "File in position: ${index} is ${fileContainer}")
                    }
                });
}

```

# Hanling Progress & Error Events

There is 5 Events you can use in this library:
  
      * `doOnStart` : This event will be called before start downloading files.
      * `doOnProgress` : This event will publish the current progress each time a file downloaded.
      * `doOnEachSingleError` : Event called each time a file failed to download.
      * `doOnCompleteWithError` : Event called when finish all the downloads and some of the files failed to download.
      * `doOnCompleteWithSuccess` : Event called when finish downloading all the files successfully.

* Example:

```kotlin
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
```

# Options :

* Customize OkHttpClient  
```kotlin
	 var ok:OkHttpClient = OkHttpClient.Builder().connectTimeout(6,TimeUnit.SECONDS).build();
     RxDownloader
                .Builder(context)
                .client(ok)
                .build();
```
* Customize Directory to save your files
	By default files will be saved in cache directory.
	You can just customize the directory by calling : `storage(File storagePath)`

* Building list of urls methods:

	- `addFile(String url)`: add url directly to list of urls without renaming the downloaded file.
	- `addFile(String newName, String url)`: add url to list and renaming the file to newName, Ps: No need to write the file extension.
	- `addFiles(List<String> urls)`: add a bulk of URL's.



# Download Strategies
  
  	-MAX Strategy: will try to download all the files in case of errors it's will continue till the end.

```kotlin
 	 RxDownloader
                .Builder(context)
                .strategy(DownloadStrategy.MAX)
                

```

    
    -ALL Strategy: will try to download all the files but if it encountered an error it's will stop immediately.

```kotlin
	RxDownloader
                .Builder(context)
                .strategy(DownloadStrategy.ALL)
```

Sometimes you want your files to be in a certain order, for that you can achieve that by calling the `Order` Method in `Builder` this way:

  ```kotlin 
  .Order(DownloadStrategy.FLAG_SEQUENTIAL)
  ```

    * By default the downloader is using `FLAG_PARALLEL` to achieve more speed and performance 


# Setup

Add it in your root build.gradle at the end of repositories:

```groovy 
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Add the dependency to your `build.gradle`:

```groovy
dependencies {
    compile 'com.github.oussaki:RxDownloader:0.1'
}
```

# License

	Copyright 2016 Oussama Abdallah

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	    http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.