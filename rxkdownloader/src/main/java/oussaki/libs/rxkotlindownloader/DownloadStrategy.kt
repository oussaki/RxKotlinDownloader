package oussaki.libs.rxkotlindownloader

/**
 * Created by oussama on 10/7/2017.
 */
class DownloadStrategy {

    /*
   * Strategies to download files
   * 1 - Try to download the maximum you can
   * 2 - Try to download all of them
   *
   * */

    companion object {
        /*
* MAX: Flag means that the downloader will try to download all the files
* in case of errors it's will continue till the end
* */
        var MAX: Int = 1
        /*
* ALL: Flag means that the downloader will try to download all the files
* but if it encountered an error it's will stop immediately
* */
        var ALL: Int = 2

        /**
         * Flag indicate that all the files will be downloaded in parallel with each others
         */
        var FLAG_PARALLEL: Int = 3

        /**
         * Flag indicate that the files will respect the order and download sequentially
         */
        var FLAG_SEQUENTIAL: Int = 4

        /*
    * Definition: <p> Default Flag </p>
    * */
        var DEFAULT: Int = MAX

    }


}