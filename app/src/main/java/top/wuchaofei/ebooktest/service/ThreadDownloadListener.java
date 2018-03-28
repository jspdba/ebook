package top.wuchaofei.ebooktest.service;

/**
 * Created by Administrator on 2018/3/28.
 */
public interface ThreadDownloadListener {
    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    void onPaused();

    void onCanceled();
}
