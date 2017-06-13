package com.example.yqx.servicebestpractice;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.health.PackageHealthStats;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder= (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startdwonload= (Button) findViewById(R.id.start_download);
        Button pausedownload= (Button) findViewById(R.id.pause_download);
        Button canceldownload= (Button) findViewById(R.id.cancel_dowanload);
        startdwonload.setOnClickListener(this);
        pausedownload.setOnClickListener(this);
        canceldownload.setOnClickListener(this);
        Intent intent=new Intent(this,DownloadService.class);
        startService(intent);
        bindService(intent,connection,BIND_AUTO_CREATE);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.
                WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new
                    String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }
        }

    @Override
    public void onClick(View v) {
        if (downloadBinder==null){
            return;
        }
        switch (v.getId()){
            case R.id.start_download:
                String url="http://xiazai.sogou.com/detail/34/1/" +
                        "-3080605666447722537.html?e=1970";
                downloadBinder.startDownload(url);
                break;
            case R.id.pause_download:
                downloadBinder.pauseDownload();
                break;
            case R.id.cancel_dowanload:
                downloadBinder.cancelDownload();
                break;
            default:
                break;
        }
    }
   public void onRequestPermissionsResult(int requestCode, String[] perissions,
              int[] grantResult){
       switch (requestCode){
           case 1:
               if (grantResult.length> 0 && grantResult[0]!=PackageManager.
                       PERMISSION_GRANTED){
                   Toast.makeText(this,"拒接权限将无法使用",Toast.LENGTH_SHORT).show();
                   finish();
               }
               break;
           default:
       }
   }
   protected void onDestroy(){
       super.onDestroy();
       unbindService(connection);
   }
}
