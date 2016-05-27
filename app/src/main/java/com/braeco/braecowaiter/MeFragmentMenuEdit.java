package com.braeco.braecowaiter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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

public class MeFragmentMenuEdit extends BraecoAppCompatActivity
        implements View.OnClickListener {

    private static final int CHANGE_PHOTO = 1;
    private static final String[] ALERT_STRING = new String[]{"是否保存您新建的品类？", "是否保存您对该品类的修改？"};
    private static String SIZE = null;

    private LinearLayout back;
    private LinearLayout cName;

    private TextView backTV;
    private TextView title;
    private TextView finish;
    private TextView emptyTip;
    private TextView cNameTV;

    private KenBurnsView image;

    private int buttonPosition = -1;
    private Context mContext;

    private boolean changed = false;
    private boolean pictureChange = false;

    private boolean noPicture = true;
    private String imageString = "";
    private String cNameString = "";

    private String token = "";
    private String key = "";

    private String oldCNameString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_menu_edit);

        mContext = this;

        buttonPosition = getIntent().getIntExtra("position", -1);

        if (SIZE == null) {
            SIZE = "?imageView2/1/w/" + getResources().getDisplayMetrics().widthPixels
                    + "/h/" + BraecoWaiterUtils.dp2px(200, mContext);
        }

        BraecoWaiterData.attributes = new ArrayList<>();

        back = (LinearLayout)findViewById(R.id.back);
        cName = (LinearLayout)findViewById(R.id.c_name);

        image = (KenBurnsView)findViewById(R.id.image);
        backTV = (TextView)findViewById(R.id.back_text);
        title = (TextView)findViewById(R.id.title);
        finish = (TextView)findViewById(R.id.edit);
        emptyTip = (TextView)findViewById(R.id.empty_tip);
        cNameTV = (TextView)findViewById(R.id.c_name_text);

        back.setOnClickListener(this);
        finish.setOnClickListener(this);
        cName.setOnClickListener(this);
        emptyTip.setOnClickListener(this);
        image.setOnClickListener(this);

        backTV.setVisibility(View.INVISIBLE);
        finish.setText("完成");
        if (buttonPosition != -1) {
            title.setText("品类修改");
            cNameString = (String) BraecoWaiterApplication.mButton.get(buttonPosition).get("button");
            cNameTV.setText(cNameString);
            if (BraecoWaiterApplication.mButton.get(buttonPosition).get("categorypic") == null
                    || "null".equals(BraecoWaiterApplication.mButton.get(buttonPosition).get("categorypic"))) {
                image.setVisibility(View.INVISIBLE);
                emptyTip.setVisibility(View.VISIBLE);
                noPicture = true;
            } else {
                Picasso.with(mContext)
                        .load(BraecoWaiterApplication.mButton.get(buttonPosition).get("categorypic") + SIZE)
                        .into(image);
                image.setVisibility(View.VISIBLE);
                emptyTip.setVisibility(View.INVISIBLE);
                noPicture = false;
            }

            oldCNameString = cNameString;
        } else {
            title.setText(("新建品类"));
            cNameTV.setText("");
            image.setVisibility(View.INVISIBLE);
            emptyTip.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.image:
            case R.id.empty_tip:
                changePicture();
                break;
            case R.id.c_name:
                changeCName();
                break;
            case R.id.edit:
                update();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CHANGE_PHOTO) {
            if (data != null) {
                imageString = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS).get(0);
                File imgFile = new File(imageString);
                if(imgFile.exists()) {
                    noPicture = false;
                    pictureChange = true;
                    changed = true;
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    int width = mContext.getResources().getDisplayMetrics().widthPixels;
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(myBitmap, width, myBitmap.getHeight() * width / myBitmap.getWidth(), true);
                    image.setImageBitmap(scaledBitmap);
                    image.setVisibility(View.VISIBLE);
                    emptyTip.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void changePicture() {
        if (noPicture) {
            PhotoPickerIntent intent = new PhotoPickerIntent(mContext);
            intent.setPhotoCount(1);
            intent.setShowCamera(true);
            startActivityForResult(intent, CHANGE_PHOTO);
        } else {
            new MaterialDialog.Builder(mContext)
                    .title("操作")
                    .positiveText("换张新照片")
                    .positiveColorRes(R.color.primaryBrown)
//                    .negativeText("删除该照片")
//                    .negativeColorRes(R.color.primaryBrown)
                    .neutralText("取消")
                    .neutralColorRes(R.color.refund_button)
                    .forceStacking(true)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                PhotoPickerIntent intent = new PhotoPickerIntent(mContext);
                                intent.setPhotoCount(1);
                                intent.setShowCamera(true);
                                startActivityForResult(intent, CHANGE_PHOTO);
                            } else if (dialogAction == DialogAction.NEGATIVE) {
                                // delete this picture
                                image.setVisibility(View.INVISIBLE);
                                emptyTip.setVisibility(View.VISIBLE);
                            }
                        }
                    })
                    .show();
        }
    }

    private MaterialDialog inputDialog;
    private void changeCName() {
        String title = "修改品类名";
        final int min = BraecoWaiterFinal.MIN_CATEGORY_NAME;
        final int max = BraecoWaiterFinal.MAX_CATEGORY_NAME;
        final String hint = "品类名";
        final String fill = cNameString;
        inputDialog = new MaterialDialog.Builder(mContext)
                .title(title)
                .negativeText("取消")
                .positiveText("确认")
                .content("")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(hint, fill, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        int count = BraecoWaiterUtils.textCounter(String.valueOf(String.valueOf(input)));
                        String pre = BraecoWaiterUtils.invalidString(input);
                        if (!"".equals(pre)) BraecoWaiterUtils.showToast(mContext, pre);
                        dialog.setContent(
                                BraecoWaiterUtils.getDialogContent(mContext,
                                        "",
                                        count + "/" + min + "-" + max,
                                        (min <= count && count <= max)));
                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                        if (!(min <= count && count <= max) || pre.length() != 0) {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                })
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            cNameString = materialDialog.getInputEditText().getText().toString();
                            cNameTV.setText(cNameString);
                        }
                    }
                })
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        int count = BraecoWaiterUtils.textCounter(String.valueOf(fill));
                        String pre = BraecoWaiterUtils.invalidString(fill);
                        inputDialog.setContent(
                                BraecoWaiterUtils.getDialogContent(mContext,
                                        pre,
                                        count + "/" + min + "-" + max,
                                        (min <= count && count <= max)));
                        inputDialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                        if (!(min <= count && count <= max) || pre.length() != 0) {
                            inputDialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                })
                .alwaysCallInputCallback()
                .show();
    }

    public void update() {
        if (!oldCNameString.equals(cNameString)) changed = true;

        if (!changed) onBackPressed();
        else save();
    }

    @Override
    public void onBackPressed() {
        if (!oldCNameString.equals(cNameString)) changed = true;

        if (changed) {
            new MaterialDialog.Builder(mContext)
                    .title("尚未保存")
                    .content((buttonPosition == -1 ? ALERT_STRING[0] : ALERT_STRING[1]))
                    .neutralText("取消")
                    .negativeText("不保存")
                    .positiveText("保存")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                save();
//                                MeFragmentMenuMenuEdit.super.onBackPressed();
                            }
                            if (dialogAction == DialogAction.NEGATIVE) {
                                MeFragmentMenuEdit.super.onBackPressed();
                            }
                            if (dialogAction == DialogAction.NEUTRAL) {
                                return;
                            }
                        }
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private MaterialDialog progressDialog;
    private void save() {
        if (null == cNameString || "".equals(cNameString)) {
            BraecoWaiterUtils.showToast(mContext, "品类名不能为空");
            return;
        }
        progressDialog = new MaterialDialog.Builder(mContext)
                .title("保存中")
                .content("请稍候")
                .cancelable(false)
                .progress(true, 0)
                .show();
        // we have to upload the data of the category to get the id
        // then use the id to upload picture
        if (buttonPosition == -1) {
            addCategory();
        } else {
            updateCategory();
        }
    }

    private void addCategory() {
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add category: " + cNameString);

        // property, dc_type, dc, tag, category, detail
        new AddCategory(false)
                .execute(
                        "http://brae.co/Category/Add",
                        cNameString);
    }

    private void updateCategory() {
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update category: " + cNameString + " " + BraecoWaiterApplication.mButton.get(buttonPosition).get("id"));

        // property, dc_type, dc, tag, category, detail
        new AddCategory(true)
                .execute(
                        "http://brae.co/Category/Update/Profile/" + BraecoWaiterApplication.mButton.get(buttonPosition).get("id"),
                        cNameString);
    }

    private class AddCategory extends AsyncTask<String, Void, String> {

        private boolean isUpdated = false;

        public AddCategory(boolean isUpdated) {
            this.isUpdated = isUpdated;
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
                if (isUpdated) Log.d("BraecoWaiter", "Update category: " + result);
                else Log.d("BraecoWaiter", "Add category: " + result);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            pairList.add(new BasicNameValuePair("name", params[1]));
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList, "UTF-8");
                HttpPost httpPost = new HttpPost(params[0]);
                httpPost.addHeader("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
                httpPost.addHeader("Cookie" , "sid=" + BraecoWaiterApplication.sid);
                httpPost.setEntity(requestHttpEntity);
//                httpPost.setEntity(new StringEntity(params[1]));
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

            if (isUpdated) Log.d("BraecoWaiter", "Update category: " + result);
            else Log.d("BraecoWaiter", "Add category: " + result);

            progressDialog.dismiss();

            if (result != null) {
                JSONObject array;
                try {
                    array = new JSONObject(result);
                    if ("success".equals(array.getString("message"))) {
                        if (pictureChange) {
                            if (buttonPosition == -1) {
                                uploadPicture(true, array.getInt("id"));
                            } else {
                                uploadPicture(true, (Integer) BraecoWaiterApplication.mButton.get(buttonPosition).get("id"));
                            }
                        } else {
                            if (buttonPosition == -1) {
                                BraecoWaiterUtils.showToast(mContext, "添加品类 " + cNameString + " 成功");
                                BraecoWaiterData.JUST_ADD_CATEGORY = true;
                            } else {
                                BraecoWaiterUtils.showToast(mContext, "修改品类 " + cNameString + " 成功");
                                BraecoWaiterData.JUST_UPDATE_CATEGORY = true;
                            }
                            finish();
                        }
                    } else if ("Category not found".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品种类不存在");
                    } else if ("Dish not found".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品不存在");
                    } else if ("Invalid name".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品中文名不合法");
                    } else if ("Invalid name2".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品英文名不合法");
                    } else if ("Invalid price".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品基础价格不合法");
                    } else if ("Invalid tag".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品标签提醒不合法");
                    } else if ("Invalid detail".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品详情介绍不合法");
                    } else if ("Invalid group name".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品属性组名不合法");
                    } else if ("Invalid property name".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品属性名不合法");
                    } else if ("Invalid property price".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品价差值不合法");
                    } else if ("Conflicting group name".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品属性组名冲突");
                    } else if ("Conflicting property name".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品属性名冲突");
                    } else if ("Too many properties in a group".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品属性数量超出限制");
                    } else if ("Too many groups".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品属性组数量超出限制");
                    } else {
                        BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                    }
                } catch (JSONException e) {
                    BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add menu json error1");
                    e.printStackTrace();
                }
            } else {
                BraecoWaiterUtils.showToast(mContext, "网络连接失败");
            }
        }
    }

    private int categoryId = -1;
    private void uploadPicture(boolean isUpdated, int id) {
        categoryId = id;
        progressDialog = new MaterialDialog.Builder(mContext)
                .title("上传品类图片中")
                .content("请耐心等待")
                .progress(false, 100, true)
                .cancelable(false)
                .show();
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "http://brae.co/pic/upload/token/category/" + id);
        if (!isUpdated)
            new GetToken(isUpdated)
                    .execute("http://brae.co/pic/upload/token/category/" + id);
        else
            new GetToken(isUpdated)
                    .execute("http://brae.co/pic/upload/token/category/" + id);
    }

    private class GetToken extends AsyncTask<String, Void, String> {

        private boolean isUpdated;

        public GetToken(boolean isUpdated) {
            this.isUpdated = isUpdated;
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
                        uploadManager.put(imageString, key, token,
                                new UpCompletionHandler() {
                                    @Override
                                    public void complete(String key, ResponseInfo info, JSONObject res) {
                                        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Upload picture: " + "key: " + key + " res: " + res);
                                        // finish uploading
                                        if (info.isOK()) {
                                            progressDialog.dismiss();
                                            BraecoWaiterUtils.showToast(mContext, "上传品类图片成功");
                                            if (!isUpdated) BraecoWaiterData.JUST_ADD_CATEGORY = true;
                                            else BraecoWaiterData.JUST_UPDATE_CATEGORY = true;
                                            finish();
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
                                                return false;
                                            }
                                        }));

                    } else {
                        if (progressDialog != null) progressDialog.dismiss();
                        failToUpload();
                    }
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentMenuEdit json error");
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

    private void failToUpload() {
        new MaterialDialog.Builder(mContext)
                .title("上传品类图片失败")
                .content("您新建的品类已经保存，但是品类图片由于网络问题上传失败")
                .positiveText("确认")
                .negativeText("重试")
                .negativeColorRes(R.color.primaryBrown)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.NEGATIVE) {
                            uploadPicture((buttonPosition != -1), categoryId);
                        }
                        if (dialogAction == DialogAction.POSITIVE) {
                            BraecoWaiterData.JUST_ADD_CATEGORY = true;
                            finish();
                        }
                    }
                })
                .show();
    }
}
