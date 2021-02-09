package com.xu.weather.download;

/**
 * author : xujianbo
 * date : 2/9/21 10:39 AM
 * description :
 */
public interface DownloadListener {
    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    void onPause();

    void onCanceled();

}
