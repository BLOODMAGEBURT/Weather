package com.xu.weather.download;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.FileObserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * author : xujianbo
 * date : 2/9/21 10:40 AM
 * description :
 */
public class DownloadTask extends AsyncTask<String, Integer, Integer> {


    final int TYPE_SUCCESS = 0;
    final int TYPE_FAILED = 1;
    final int TYPE_PAUSED = 2;
    final int TYPE_CANCELED = 3;


    private DownloadListener downloadListener;
    private boolean isCanceled = false;
    private boolean isPaused = false;


    public DownloadTask(DownloadListener downloadListener) {

        this.downloadListener = downloadListener;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        // 创建文件｜找文件
        String downloadUrl = strings[0];
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));

        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

        File file = new File(directory + fileName);
        InputStream inputStream = null;
        RandomAccessFile savedFile = null;

        long downloadedLength = 0;
        if (file.exists()) {
            downloadedLength = file.length();
        }
        // 根据本地文件大小和网络文件大小判断  是断点续下载， 还是已经下载完毕
        try {
            long contentLength = getContentLength(downloadUrl);

            if (downloadedLength < contentLength) {
                // 下载
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder().url(downloadUrl)
                        .addHeader("Range", "bytes=" + downloadedLength + "-")
                        .build();
                Response response = okHttpClient.newCall(request).execute();

                if (response != null) {
                    inputStream = response.body().byteStream();
                    byte[] bytes = new byte[1024];
                    int len = 0;
                    // 合并文件
                    savedFile = new RandomAccessFile(file, "rw");
                    savedFile.seek(downloadedLength);
                    int total = 0;
                    while ((len = inputStream.read(bytes)) != -1) {
                        if (isCanceled) {
                            return TYPE_CANCELED;
                        }
                        if (isPaused) {
                            return TYPE_PAUSED;
                        }

                        savedFile.write(bytes, 0, len);
                        // 计算下载的百分比
                        // 下载的过程中，回调progress进度
                        total += len;
                        publishProgress((int) ((total + downloadedLength) * 100 / contentLength));
                    }
                }
                //全部循环完毕, 下载成功
                response.body().close();
                return TYPE_SUCCESS;

            } else {
                // 已经下载完毕
                return TYPE_SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            try {

                if (inputStream != null) {
                    inputStream.close();
                }

                if (savedFile != null) {
                    savedFile.close();
                }

                if (isCanceled && file != null) {
                    file.delete();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return TYPE_FAILED;
    }

    private long getContentLength(String downloadUrl) throws IOException {

        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder().url(downloadUrl).build();

        Response response = okHttpClient.newCall(request).execute();

        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            // 关闭流
            response.body().close();

            return contentLength;
        }
        return 0;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        downloadListener.onProgress(values[0]);
    }


    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer) {
            case TYPE_SUCCESS:
                downloadListener.onSuccess();
                break;
            case TYPE_FAILED:
                downloadListener.onFailed();
                break;
            case TYPE_PAUSED:
                downloadListener.onPause();
                break;
            case TYPE_CANCELED:
                downloadListener.onCanceled();
                break;
            default:
                break;
        }
    }

    public void setCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }
}
