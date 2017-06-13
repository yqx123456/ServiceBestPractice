package com.example.yqx.servicebestpractice;

import android.app.DownloadManager;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yqx on 2017/6/5.
 */

public class DownloadTask extends AsyncTask<String ,Integer,Integer> {
    public static final int TYPE_SUCCESS=0;
    public static final int TYPE_FAILED=1;
    public static final int TYPE_PAUSED=2;
    public static final int TYPE_CANCELED=3;
    private DownloadListenner listener;
    private boolean isCanceled=false;
    private boolean isPause=false;
    private int lastProgress;
    public DownloadTask(DownloadListenner listener){
        this.listener=listener;
    }
    protected Integer doInBackground(String... params) {
        InputStream is=null;
        RandomAccessFile saveFile=null;
        File file=null;
        try{
            long downloadedLength=0;
            String downloadUrl=params[0];
            String fileName=downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            String directory= Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DOWNLOADS).getPath();
            file=new File(directory+fileName);
            if (file.exists()){
                downloadedLength=file.length();
            }
            long contentLength=getContentLength(downloadUrl);
            if (contentLength==0){
                return TYPE_FAILED;
            }else if (contentLength==downloadedLength){
                return TYPE_SUCCESS;
            }
            OkHttpClient client=new OkHttpClient();
            Request request=new Request.Builder().
                    addHeader("RANGE","bytes="+downloadedLength+"-").
                    url(downloadUrl).build();
            Response response=client.newCall(request).execute();
            saveFile=new RandomAccessFile(file,"rw");
            saveFile.seek(downloadedLength);
            byte[] b=new byte[1024];
            int total=0;
            int len;
            while((len=is.read(b))!=-1){
                if (isCanceled){
                    return TYPE_CANCELED;
                }
                else {
                    if (isPause) {
                        return TYPE_PAUSED;
                    } else {
                        total += len;
                        saveFile.write(b, 0, len);
                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
            }
           response.body().close();
            return TYPE_SUCCESS;
        }
        catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if (is !=null){
                    is.close();
                }
                if (saveFile!=null){
                    saveFile.close();
                }
                if (isCanceled && file!=null){
                    file.delete();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress=values[0];
        if (progress>lastProgress){
            listener.onProgress(progress);
            lastProgress=progress;
        }
    }

    @Override
    protected void onPostExecute(Integer status) {
        switch (status){
                case  TYPE_SUCCESS:
                listener.onSuccess();
            case TYPE_FAILED:
                listener.onFailed();
            case TYPE_PAUSED:
                listener.onPaused();
            case TYPE_CANCELED:
                listener.onCanceled();
                default:
                    break;
        }
    }
    public void pauseDownLoad(){
        isPause=true;
    }
    public void cancelDownload(){
        isCanceled=true;
    }
    private long getContentLength(String downloadUrl)throws IOException{
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(downloadUrl).build();
        Response response=client.newCall(request).execute();
        if (response!=null&& response.isSuccessful()){
            long contentLength=response.body().contentLength();
            response.body().close();
            return contentLength;
        }
        return 0;
    }
}
