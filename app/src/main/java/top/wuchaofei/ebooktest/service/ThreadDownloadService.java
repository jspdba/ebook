package top.wuchaofei.ebooktest.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.io.File;

import top.wuchaofei.ebooktest.ChapterAdapter;
import top.wuchaofei.ebooktest.MainActivity;
import top.wuchaofei.ebooktest.R;

public class ThreadDownloadService extends Service {
    private ThreadDownloadTask threadDownloadTask;
    private DownloadBinder mBinder = new DownloadBinder();
    private Message message;
    private NotificationManager notificationManager;
    private ThreadDownloadListener threadDownloadListener = new ThreadDownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("Downloading...", progress));
        }

        @Override
        public void onSuccess() {
            threadDownloadTask = null;
            // 下载成功时将前台服务通知关闭，并创建一个下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Success", -1));
            Toast.makeText(ThreadDownloadService.this, "Download Success", Toast.LENGTH_SHORT).show();
            // 通知主线程更新Ui
            sendMessage();
            // 停止服务
            stopSelf();
        }

        @Override
        public void onFailed() {
            threadDownloadTask = null;
            // 下载失败时将前台服务通知关闭，并创建一个下载失败的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Failed", -1));
            Toast.makeText(ThreadDownloadService.this, "Download Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            threadDownloadTask = null;
            Toast.makeText(ThreadDownloadService.this, "Paused", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            threadDownloadTask = null;
            stopForeground(true);
            Toast.makeText(ThreadDownloadService.this, "Canceled", Toast.LENGTH_SHORT).show();
        }
    };
    public ThreadDownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class DownloadBinder extends Binder{
        public void startDownload(int threadCount) {
            if (threadDownloadTask == null) {
                threadDownloadTask = new ThreadDownloadTask(threadDownloadListener);
                threadDownloadTask.execute(threadCount);
                startForeground(1, getNotification("Downloading...", 0));
                Toast.makeText(ThreadDownloadService.this, "Downloading...", Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload() {
            if (threadDownloadTask != null) {
                threadDownloadTask.pauseDownload();
            }
        }

        public void cancelDownload() {
            if (threadDownloadTask != null) {
                threadDownloadTask.cancelDownload();
            } else {
                // 取消下载时需将通知关闭
                getNotificationManager().cancel(1);
                stopForeground(true);
                Toast.makeText(ThreadDownloadService.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress >= 0) {
            // 当progress大于或等于0时才需显示下载进度
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }
    private NotificationManager getNotificationManager() {
        if(notificationManager==null) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    private void sendMessage(){
        message = new Message();
        message.arg1 = 1;
        MainActivity.handler.sendMessage(message);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        threadDownloadTask = null;
    }
}
