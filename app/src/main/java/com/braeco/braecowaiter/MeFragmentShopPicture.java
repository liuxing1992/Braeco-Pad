package com.braeco.braecowaiter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;

public class MeFragmentShopPicture extends BraecoAppCompatActivity
        implements AdapterView.OnItemClickListener {

    private LinearLayout back;

    private GridView grid;
    private UpdatePictureAdapter adapter;

    private Context mContext;

    private static final int CHANGE_PHOTO = 1;
    private static final int ADD_PHOTO = 2;

    private String lastPicture;
    private int lastPosition = -1;

    private MaterialDialog progressDialog;

    private String token;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_shop_picture);

        mContext = this;

        back = (LinearLayout)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        grid = (GridView)findViewById(R.id.gridview);
        adapter = new UpdatePictureAdapter();
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if (position == BraecoWaiterApplication.MAX_SHOP_PICTURE) {
            new MaterialDialog.Builder(mContext)
                    .title("已达到上限")
                    .content("您最多只能上传" + BraecoWaiterApplication.MAX_SHOP_PICTURE + "张门店照片")
                    .positiveText("确认")
                    .show();
            return;
        }
        if ("".equals(BraecoWaiterApplication.pictureAddress[position])) {
            // add some picture
            lastPosition = position;
            PhotoPickerIntent intent = new PhotoPickerIntent(mContext);
            intent.setPhotoCount(1);
            intent.setShowCamera(true);
            startActivityForResult(intent, ADD_PHOTO);
        } else {
            // change a picture
            new MaterialDialog.Builder(mContext)
                    .title("操作")
                    .positiveText("上传新照片")
                    .positiveColorRes(R.color.primaryBrown)
                    .negativeText("删除该照片")
                    .negativeColorRes(R.color.primaryBrown)
                    .neutralText("取消")
                    .neutralColorRes(R.color.refund_button)
                    .forceStacking(true)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                lastPosition = position;
                                PhotoPickerIntent intent = new PhotoPickerIntent(mContext);
                                intent.setPhotoCount(1);
                                intent.setShowCamera(true);
                                startActivityForResult(intent, CHANGE_PHOTO);
                            } else if (dialogAction == DialogAction.NEGATIVE) {
                                lastPosition = position;
                                progressDialog = new MaterialDialog.Builder(mContext)
                                        .title("正在从服务器删除该照片")
                                        .content("请耐心等待")
                                        .cancelable(false)
                                        .negativeText("取消")
                                        .onAny(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                                if (dialogAction == DialogAction.NEGATIVE) {
                                                    materialDialog.dismiss();
                                                    BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_TASK_NUM++;
                                                }
                                            }
                                        })
                                        .show();
                                BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_TASK_NUM++;
                                new DeletePicture(BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_TASK_NUM)
                                        .execute("http://brae.co/Dinner/Cover/Remove/" + lastPosition);
                            }
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CHANGE_PHOTO) {
            if (data != null) {
                lastPicture = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS).get(0);
                changePicture();
            }
        }

        if (resultCode == RESULT_OK && requestCode == ADD_PHOTO) {
            if (data != null) {
                lastPicture = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS).get(0);
                addPicture();
            }
        }
    }

    private void addPicture() {
        changePicture();
    }

    private void changePicture() {
        progressDialog = new MaterialDialog.Builder(mContext)
                .title("上传图片中")
                .content("请耐心等待")
                .progress(false, 100, true)
                .cancelable(false)
                .negativeText("取消")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.NEGATIVE) {
                            materialDialog.dismiss();
                            BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_TASK_NUM++;
                        }
                    }
                })
                .show();
        BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_TASK_NUM++;
        new GetToken(BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_TASK_NUM)
                .execute("http://brae.co/pic/upload/token/cover/" + lastPosition);
    }

    private class GetToken extends AsyncTask<String, Void, String> {

        private int task;

        public GetToken(int task) {
            this.task = task;
        }

        protected String showResponseResult(HttpResponse response) {
            if (null == response) return null;

            HttpEntity httpEntity = response.getEntity();

            try {
                InputStream inputStream = httpEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String result = "";
                String line = "";
                while (null != (line = reader.readLine())) {
                    result += line;
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> pairList = new ArrayList<>();
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(
                        pairList);
                HttpPost httpPost = new HttpPost(params[0]);
                httpPost.addHeader("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
                httpPost.addHeader("Cookie" , "sid=" + BraecoWaiterApplication.sid);
                httpPost.setEntity(requestHttpEntity);
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 401) return BraecoWaiterUtils.STRING_401;
                return showResponseResult(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            if (BraecoWaiterUtils.STRING_401.equals(result)) {
                BraecoWaiterUtils.forceToLoginFor401(mContext);
                return;
            }

            if (task != BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_TASK_NUM) return;

            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "GetToken:" + result);

            if (result != null) {
                JSONObject array;
                try {
                    array = new JSONObject(result);
                    if ("success".equals(array.getString("message"))) {
                        token = array.getString("token");
                        key = array.getString("key");

                        // true update
                        UploadManager uploadManager = new UploadManager();
                        uploadManager.put(lastPicture, key, token,
                                new UpCompletionHandler() {
                                    @Override
                                    public void complete(String key, ResponseInfo info, JSONObject res) {
                                        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Updated picture: " + "key: " + key + " res: " + res);
                                        // finish uploading
                                        if (info.isOK()) {
                                            BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_CHANGE = true;
                                            View view = grid.getChildAt(lastPosition);
                                            ImageView image = (ImageView)view.findViewById(R.id.image);
                                            File imgFile = new File(lastPicture);
                                            if(imgFile.exists()) {
                                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                                Bitmap scaleBitmap = Bitmap.createScaledBitmap(myBitmap, 200, myBitmap.getHeight() * 200 / myBitmap.getWidth(), true);
                                                image.setImageBitmap(scaleBitmap);
                                            }
                                            if (lastPosition < BraecoWaiterApplication.MAX_SHOP_PICTURE) {
                                                BraecoWaiterApplication.pictureAddress[lastPosition]
                                                        = "http://static.brae.co/" + key + BraecoWaiterApplication.pictureSize;
                                            } else {
                                                for (int i = 0; i < BraecoWaiterApplication.MAX_SHOP_PICTURE; i++) {
                                                    if ("".equals(BraecoWaiterApplication.pictureAddress[i]))
                                                        BraecoWaiterApplication.pictureAddress[i]
                                                                = "http://static.brae.co/" + key + BraecoWaiterApplication.pictureSize;
                                                }
                                            }
                                            BraecoWaiterApplication.writePreferenceArray(mContext, "PICTURE_ADDRESS", BraecoWaiterApplication.pictureAddress);
                                            grid.invalidate();
                                            progressDialog.dismiss();
                                            BraecoWaiterUtils.showToast(mContext, "上传门店图片成功");
                                        } else {
                                            if (progressDialog != null) progressDialog.dismiss();
                                            failToUpload();
                                        }
                                    }
                                },
                                new UploadOptions(null, null, false,
                                        new UpProgressHandler(){
                                            public void progress(String key, double percent){
                                                progressDialog.setProgress((int)(percent * 100));
                                            }
                                        },
                                        new UpCancellationSignal() {
                                            public boolean isCancelled() {
                                            return task != BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_TASK_NUM;
                                    }
                                }));

                    } else {
                        if (progressDialog != null) progressDialog.dismiss();
                        failToUpload();
                    }
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentVipDiscount json error");
                    e.printStackTrace();
                    if (progressDialog != null) progressDialog.dismiss();
                    failToUpload();
                }
            } else {
                if (progressDialog != null) progressDialog.dismiss();
                failToUpload();
            }
        }
    }

    private class DeletePicture extends AsyncTask<String, Void, String> {

        private int task;

        public DeletePicture(int task) {
            this.task = task;
        }

        protected String showResponseResult(HttpResponse response) {
            if (null == response) return null;

            HttpEntity httpEntity = response.getEntity();

            try {
                InputStream inputStream = httpEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String result = "";
                String line = "";
                while (null != (line = reader.readLine())) {
                    result += line;
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> pairList = new ArrayList<>();
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(
                        pairList);
                HttpPost httpPost = new HttpPost(params[0]);
                httpPost.addHeader("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
                httpPost.addHeader("Cookie" , "sid=" + BraecoWaiterApplication.sid);
                httpPost.setEntity(requestHttpEntity);
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 401) return BraecoWaiterUtils.STRING_401;
                return showResponseResult(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            if (BraecoWaiterUtils.STRING_401.equals(result)) {
                BraecoWaiterUtils.forceToLoginFor401(mContext);
                return;
            }

            if (task != BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_TASK_NUM) return;

            if (progressDialog != null) progressDialog.dismiss();

            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "GetToken:" + result);

            if (result != null) {
                JSONObject array;
                try {
                    array = new JSONObject(result);
                    if ("success".equals(array.getString("message"))) {
                        BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_CHANGE = true;
                        BraecoWaiterApplication.pictureAddress[lastPosition] = "";
                        String[] newPictures = new String[BraecoWaiterApplication.MAX_SHOP_PICTURE];
                        int count = 0;
                        for (int i = 0; i < newPictures.length; i++) newPictures[i] = "";
                        for (int i = 0; i < BraecoWaiterApplication.pictureAddress.length; i++) {
                            if (!"".equals(BraecoWaiterApplication.pictureAddress[i]))
                                newPictures[count++] = BraecoWaiterApplication.pictureAddress[i];
                        }
                        BraecoWaiterApplication.pictureAddress = newPictures;
                        BraecoWaiterApplication.writePreferenceArray(mContext, "PICTURE_ADDRESS", BraecoWaiterApplication.pictureAddress);
                        adapter.notifyDataSetChanged();
                        grid.invalidate();
                        new MaterialDialog.Builder(mContext)
                                .title("删除图片成功")
                                .positiveText("确认")
                                .show();
                    } else {
                        if (progressDialog != null) progressDialog.dismiss();
                        failToDelete();
                    }
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentVipDiscount json error");
                    e.printStackTrace();
                    if (progressDialog != null) progressDialog.dismiss();
                    failToDelete();
                }
            } else {
                if (progressDialog != null) progressDialog.dismiss();
                failToDelete();
            }
        }
    }

    private void failToUpload() {
        new MaterialDialog.Builder(mContext)
                .title("上传门店图片失败")
                .content("网络连接失败")
                .positiveText("确认")
                .negativeText("重试")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.NEGATIVE) {
                            changePicture();
                        }
                    }
                })
                .show();
    }

    private void failToDelete() {
        new MaterialDialog.Builder(mContext)
                .title("删除图片失败")
                .content("网络连接失败")
                .positiveText("确认")
                .negativeText("重试")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.NEGATIVE) {
                            progressDialog = new MaterialDialog.Builder(mContext)
                                    .title("正在从服务器删除该照片")
                                    .content("请耐心等待")
                                    .cancelable(false)
                                    .negativeText("取消")
                                    .onAny(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                            if (dialogAction == DialogAction.NEGATIVE) {
                                                materialDialog.dismiss();
                                                BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_TASK_NUM++;
                                            }
                                        }
                                    })
                                    .show();
                            new DeletePicture(BraecoWaiterApplication.ME_FRAGMENT_SHOP_PICTURE_UPDATE_TASK_NUM)
                                    .execute("http://brae.co/Dinner/Cover/Remove/" + lastPosition);
                        }
                    }
                })
                .show();
    }
}
