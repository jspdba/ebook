package top.wuchaofei.ebooktest.service;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import top.wuchaofei.ebooktest.MainActivity;
import top.wuchaofei.ebooktest.db.Chapter;

/**
 * Created by Administrator on 2018/3/28.
 */

public class ThreadDownloadTask extends AsyncTask<Integer,Integer,Integer>{
    private int count = 5;
    private ExecutorService executorService;
    private ThreadDownloadListener threadDownloadListener;

    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCELED = 3;

    private boolean isCanceled = false;
    private boolean isPaused = false;
    private CountDownLatch countDownLatch;

    private int lastProgress;

    public ThreadDownloadTask(ThreadDownloadListener threadDownloadListener) {
        this.threadDownloadListener = threadDownloadListener;
    }

    @Override
    protected Integer doInBackground(Integer... threadCount) {

        count = threadCount[0];
        if(executorService==null){
            executorService= Executors.newFixedThreadPool(count);
        }
        final List<Chapter> chapterList = DataSupport.where("content is null").find(Chapter.class);
        if(chapterList!=null && chapterList.size()>0){
            countDownLatch = new CountDownLatch(chapterList.size());
            final int total = chapterList.size();

            for (int i = 0; i < total; i++) {
                final int finalI = i;
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpClient client = new OkHttpClient();
                        Chapter chapter = chapterList.get(finalI);
                        Log.i("download===", chapter.getLink());
                        Request request = new Request.Builder().url(chapter.getLink()).build();

                        Response res = null;
                        try {
                            res = client.newCall(request).execute();
                            String content = MainActivity.parseContent(res.body().string());
                            if(!TextUtils.isEmpty(content)){
                                chapter.setContent(content);
                                chapter.save();
                                chapter = null;
                                content = null;
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("threadDownloadTask", e.getMessage());
                        }finally {
                            if(res!=null){
                                res.body().close();
                            }
                            countDownLatch.countDown();
                            // 计算已下载的百分比
                            int progress = (int) ((finalI+1) * 100 / total);
                            Log.d("progress===",""+progress);
                            publishProgress(progress);
                        }
                    }
                });
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                if(executorService!=null){
                    executorService.shutdown();
                    executorService = null;
                }
            }
        }
        return TYPE_SUCCESS;
    }
    @Override
    protected void onPostExecute(Integer status) {
        switch (status) {
            case TYPE_SUCCESS:
                threadDownloadListener.onSuccess();
                break;
            case TYPE_FAILED:
                threadDownloadListener.onFailed();
                break;
            case TYPE_PAUSED:
                threadDownloadListener.onPaused();
                break;
            case TYPE_CANCELED:
                threadDownloadListener.onCanceled();
            default:
                break;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];

        if (progress > lastProgress) {
            threadDownloadListener.onProgress(progress);
            lastProgress = progress;
        }
    }

    public void pauseDownload() {
        isPaused = true;
    }
    public void cancelDownload() {
        isCanceled = true;
    }
}
