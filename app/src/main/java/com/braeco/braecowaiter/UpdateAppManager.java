package com.braeco.braecowaiter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateAppManager {  
    // 文件分隔符  
    private static final String FILE_SEPARATOR = "/";  
    // 外存sdcard存放路径  
    private static final String FILE_PATH = Environment.getExternalStorageDirectory() + FILE_SEPARATOR +"autoupdate" + FILE_SEPARATOR;  
    // 下载应用存放全路径  
    private static final String FILE_NAME = FILE_PATH + "autoupdate.apk";  
    // 更新应用版本标记  
    private static final int UPDARE_TOKEN = 0x29;  
    // 准备安装新版本应用标记  
    private static final int INSTALL_TOKEN = 0x31;  
      
    private Context context;  
    public static String message = "您的版本过于陈旧，请更新后再使用";  
    // 以华为天天聊hotalk.apk为例  
    public static String spec = "";  
    // 下载应用的对话框  
    private Dialog dialog;  
    // 下载应用的进度条  
    private ProgressBar progressBar;  
    // 进度条的当前刻度值  
    private int curProgress;  
    // 用户是否取消下载  
    private boolean isCancel;

    public static boolean mustUpdate = false;

    private MaterialDialog progressDialog;
      
    public UpdateAppManager(Context context) {  
        this.context = context;  
    }  
      
    private final Handler handler = new Handler(){  
        @Override  
        public void handleMessage(Message msg) {  
            switch (msg.what) {  
            case UPDARE_TOKEN:
                progressDialog.setProgress(curProgress);
                break;
            case INSTALL_TOKEN:  
                installApp();  
                break;  
            }  
        }  
    };  
      
    /** 
     * 检测应用更新信息 
     */  
    public void checkUpdateInfo() {  
        showNoticeDialog();  
    }  
  
    /** 
     * 显示提示更新对话框 
     */  
    private void showNoticeDialog() {
        if (mustUpdate) {
            new MaterialDialog.Builder(context)
                    .title("软件版本更新")
                    .content("您的版本过旧，可能无法使用。")
                    .positiveText("安装新版本")
                    .cancelable(false)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                showDownloadDialog();
                                materialDialog.dismiss();
                            }
                        }
                    })
                    .show();
        } else {
            new MaterialDialog.Builder(context)
                    .title("软件版本更新")
                    .content("我们将为您带来更多的功能。\n您也可以在设置界面中手动更新。")
                    .positiveText("安装新版本")
                    .negativeText("不必")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                showDownloadDialog();
                                materialDialog.dismiss();
                            }
                        }
                    })
                    .show();
        }
    }  
  
    /** 
     * 显示下载进度对话框 
     */  
    private void showDownloadDialog() {

        if (mustUpdate) {
            progressDialog = new MaterialDialog.Builder(context)
                    .title("下载最新安装包中……")
                    .content("请耐心等待")
                    .progress(false, 100, true)
                    .cancelable(false)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.NEGATIVE) {
                                materialDialog.dismiss();
                            }
                        }
                    })
                    .show();
        } else {
            progressDialog = new MaterialDialog.Builder(context)
                    .title("下载最新安装包中……")
                    .content("请耐心等待")
                    .progress(false, 100, true)
                    .negativeText("取消")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.NEGATIVE) {
                                materialDialog.dismiss();
                            }
                        }
                    })
                    .show();
        }

        downloadApp();  
    }  
  
    /** 
     * 下载新版本应用 
     */  
    private void downloadApp() {  
        new Thread(new Runnable() {  
            @Override  
            public void run() {  
                URL url = null;  
                InputStream in = null;  
                FileOutputStream out = null;  
                HttpURLConnection conn = null;  
                try {  
                    url = new URL(spec);  
                    conn = (HttpURLConnection) url.openConnection();  
                    conn.connect();  
                    long fileLength = conn.getContentLength();  
                    in = conn.getInputStream();  
                    File filePath = new File(FILE_PATH);  
                    if(!filePath.exists()) {  
                        filePath.mkdir();  
                    }  
                    out = new FileOutputStream(new File(FILE_NAME));  
                    byte[] buffer = new byte[1024];  
                    int len = 0;  
                    long readedLength = 0l;  
                    while((len = in.read(buffer)) != -1) {  
                        // 用户点击“取消”按钮，下载中断  
                        if(isCancel) {  
                            break;  
                        }  
                        out.write(buffer, 0, len);  
                        readedLength += len;  
                        curProgress = (int) (((float) readedLength / fileLength) * 100);  
                        handler.sendEmptyMessage(UPDARE_TOKEN);  
                        if(readedLength >= fileLength) {  
                            progressDialog.dismiss();
                            // 下载完毕，通知安装  
                            handler.sendEmptyMessage(INSTALL_TOKEN);  
                            break;  
                        }  
                    }  
                    out.flush();  
                } catch (Exception e) {  
                    e.printStackTrace();  
                } finally {  
                    if(out != null) {  
                        try {  
                            out.close();  
                        } catch (IOException e) {  
                            e.printStackTrace();  
                        }  
                    }  
                    if(in != null) {  
                        try {  
                            in.close();  
                        } catch (IOException e) {  
                            e.printStackTrace();  
                        }  
                    }  
                    if(conn != null) {  
                        conn.disconnect();  
                    }  
                }  
            }  
        }).start();  
    }  
      
    /** 
     * 安装新版本应用 
     */  
    private void installApp() {  
        File appFile = new File(FILE_NAME);  
        if(!appFile.exists()) {  
            return;  
        }  
        // 跳转到新版本应用安装页面  
        Intent intent = new Intent(Intent.ACTION_VIEW);  
        intent.setDataAndType(Uri.parse("file://" + appFile.toString()), "application/vnd.android.package-archive");  
        context.startActivity(intent);  
    }  
}  