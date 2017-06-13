package com.example.yqx.servicebestpractice;

/**
 * Created by yqx on 2017/6/5.
 */

public interface DownloadListenner {
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPaused();
    void onCanceled();


}
