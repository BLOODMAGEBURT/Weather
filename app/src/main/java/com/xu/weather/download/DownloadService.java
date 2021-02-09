package com.xu.weather.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.load.resource.bitmap.Downsampler;
import com.xu.weather.DownloadActivity;
import com.xu.weather.MainActivity;
import com.xu.weather.R;

/**
 * author : xujianbo
 * date : 2/9/21 12:29 PM
 * description :
 */
public class DownloadService extends Service {

    private DownloadTask downloadTask;


    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            // 将进度更新到通知上

            Notification notification = getNotification("下载中", progress);
            getNotificationManager().notify(0, notification);
        }

        @Override
        public void onSuccess() {
            // 先关闭前台服务通知
            stopForeground(true);
            // 再创建一个完成的通知
            Notification notification = getNotification("下载完成", 100);
            getNotificationManager().notify(0, notification);
        }

        @Override
        public void onFailed() {

        }

        @Override
        public void onPause() {
            downloadTask = null;
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadBinder();
    }

    public class DownloadBinder extends Binder {
        public void startDownload(String url) {
            if (downloadTask == null) {
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(url);

                // 创建通知
                Notification notification = getNotification("开始下载", 0);
                // 启用前台服务
                startForeground(0, notification);
                Toast.makeText(getApplication(), "kaisihxiaz", Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload() {
            if (downloadTask != null) {
                downloadTask.setPaused(true);
            }
        }

        public void cancelDownload() {
            if (downloadTask != null) {
                downloadTask.setCanceled(true);
            }
        }

    }

    private NotificationManagerCompat getNotificationManager() {
        return NotificationManagerCompat.from(this);

    }

    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(DownloadService.this, DownloadActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(DownloadService.this, 0, intent, 0);

        return new NotificationCompat.Builder(DownloadService.this, "uniqueId")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setContentText(progress + "%")
                .setProgress(100, progress, false)
                .setAutoCancel(true) // 点击之后自动取消
                .build();
    }

}
