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
import com.braeco.braecowaiter.UIs.TitleLayout;
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
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;

public class MeFragmentActivityThemeEdit extends BraecoAppCompatActivity
        implements
        View.OnClickListener,
        com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener,
        TitleLayout.OnTitleActionListener {

    private static final String[] ALERT_STRING = new String[]{"是否保存您新建的活动？", "是否保存您对该活动的修改？"};
    private static final int CHANGE_PHOTO = 1;
    private static String SIZE = null;
    private static final int CHANGE_DETAIL = 0;

    private LinearLayout name;
    private LinearLayout time;
    private LinearLayout summary;
    private LinearLayout more;

    private TitleLayout title;
    private TextView emptyTip;
    private TextView nameTV;
    private TextView timeTV;
    private TextView summaryTV;
    private TextView moreTV;
    private TextView deleteTV;

    private KenBurnsView image;

    private int activityPosition = -1;
    private Context mContext;

    private boolean changed = false;
    private boolean pictureChange = false;
    private boolean pictureUploaded = false;

    private boolean noPicture = true;
    private String imageString = "";
    private String nameString = "";
    private String timeString = "";
    private String summaryString = "";
    private String moreString = "";

    private String token = "";
    private String key = "";

    private String oldNameString = "";
    private String oldTimeString = "";
    private String oldSummaryString = "";
    private String oldMoreString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_activity_theme_edit);

        mContext = this;

        activityPosition = getIntent().getIntExtra("position", -1);

        if (SIZE == null) {
            SIZE = "?imageView2/1/w/" + getResources().getDisplayMetrics().widthPixels
                    + "/h/" + BraecoWaiterUtils.dp2px(200, mContext);
        }

        name = (LinearLayout)findViewById(R.id.name);
        time = (LinearLayout)findViewById(R.id.time);
        summary = (LinearLayout)findViewById(R.id.summary);
        more = (LinearLayout)findViewById(R.id.more);

        title = (TitleLayout)findViewById(R.id.title_layout);
        image = (KenBurnsView)findViewById(R.id.image);
        emptyTip = (TextView)findViewById(R.id.empty_tip);
        nameTV = (TextView)findViewById(R.id.name_text);
        timeTV = (TextView)findViewById(R.id.time_text);
        summaryTV = (TextView)findViewById(R.id.summary_text);
        moreTV = (TextView)findViewById(R.id.more_text);
        deleteTV = (TextView)findViewById(R.id.delete);
        if (activityPosition == -1) deleteTV.setVisibility(View.GONE);

        name.setOnClickListener(this);
        time.setOnClickListener(this);
        summary.setOnClickListener(this);
        more.setOnClickListener(this);
        emptyTip.setOnClickListener(this);
        image.setOnClickListener(this);
        deleteTV.setOnClickListener(this);

        if (activityPosition != -1) {
            title.setTitle("活动修改");
            nameString = (String)BraecoWaiterData.activities.get(activityPosition).getTitle();
            nameTV.setText(nameString);
            timeString = (BraecoWaiterData.activities.get(activityPosition).getStartDate()) +
                    "~" + (BraecoWaiterData.activities.get(activityPosition).getEndDate());
            timeTV.setText(timeString);
            summaryString = (String)BraecoWaiterData.activities.get(activityPosition).getIntroduction();
            summaryTV.setText(summaryString);
            if (BraecoWaiterData.activities.get(activityPosition).getContent() == null
                    || "null".equals(BraecoWaiterData.activities.get(activityPosition).getContent())) {
                moreString = "";
            } else {
                moreString = (String)BraecoWaiterData.activities.get(activityPosition).getContent();
            }
            moreTV.setText(moreString);
            if (BraecoWaiterData.activities.get(activityPosition).getPicture() == null
                    || "null".equals(BraecoWaiterData.activities.get(activityPosition).getPicture())) {
                image.setVisibility(View.INVISIBLE);
                emptyTip.setVisibility(View.VISIBLE);
                noPicture = true;
            } else {
                Picasso.with(mContext)
                        .load(BraecoWaiterData.activities.get(activityPosition).getPicture() + SIZE)
                        .into(image);
                image.setVisibility(View.VISIBLE);
                emptyTip.setVisibility(View.INVISIBLE);
                noPicture = false;
            }

            oldNameString = nameString;
            oldTimeString = timeString;
            oldSummaryString = summaryString;
            oldMoreString = moreString;
        } else {
            title.setTitle(("新建活动"));
            nameTV.setText("");
            timeTV.setText("");
            summaryTV.setText("");
            moreString = "";
            moreTV.setText(moreString);
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
            case R.id.image:
            case R.id.empty_tip:
                changePicture();
                break;
            case R.id.name:
                changeName();
                break;
            case R.id.time:
                changeTime();
                break;
            case R.id.summary:
                changeSummary();
                break;
            case R.id.more:
                changeMore();
                break;
            case R.id.delete:
                deleteActivity();
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
        } else if (resultCode == RESULT_OK && requestCode == CHANGE_DETAIL) {
            moreString = data.getStringExtra("new");
            moreTV.setText(moreString);
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

    private void changeName() {
        String title = "修改活动标题";
        final int min = BraecoWaiterFinal.MIN_ACTIVITY_NAME;
        final int max = BraecoWaiterFinal.MAX_ACTIVITY_NAME;
        final String hint = "活动标题";
        final String fill = nameString;
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
                            nameString = materialDialog.getInputEditText().getText().toString();
                            nameTV.setText(nameString);
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

    private int startYear, startMonth, startDay;
    private int endYear, endMonth, endDay;
    private String startString = "";
    private boolean startTime = true;
    private void changeTime() {
        startTime = true;
        Calendar calendar = Calendar.getInstance();
        com.wdullaer.materialdatetimepicker.date.DatePickerDialog
                tpd = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        tpd.setAccentColor(BraecoWaiterUtils.getInstance()
                .getColorFromResource(this, R.color.colorPrimary));
        tpd.setYearRange(BraecoWaiterFinal.MIN_YEAR, BraecoWaiterFinal.MAX_YEAR);
        tpd.setTitle("开始时间");
        tpd.show(getFragmentManager(), "Timepickerdialog3");
    }

    @Override
    public void onDateSet(com.wdullaer.materialdatetimepicker.date.DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        if (startTime) {
            startString = String.format("%4d", year) + "-"
                    + String.format("%2d", (monthOfYear + 1)) + "-" + String.format("%2d", dayOfMonth);
            startTime = false;
            startYear = year;
            startMonth = monthOfYear;
            startDay = dayOfMonth;
            Calendar calendar = Calendar.getInstance();
            com.wdullaer.materialdatetimepicker.date.DatePickerDialog
                    tpd = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                    this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            tpd.setAccentColor(BraecoWaiterUtils.getInstance()
                    .getColorFromResource(this, R.color.colorPrimary));
            tpd.setYearRange(BraecoWaiterFinal.MIN_YEAR, BraecoWaiterFinal.MAX_YEAR);
            tpd.setTitle("结束时间");
            tpd.show(getFragmentManager(), "Timepickerdialog3");
        } else {
            // set the time
            endYear = year;
            endMonth = monthOfYear;
            endDay = dayOfMonth;
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.set(startYear, startMonth, startDay, 0, 0, 0);
            startCalendar.add(Calendar.SECOND, 0);
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.set(endYear, endMonth, endDay, 0, 0, 0);
            endCalendar.add(Calendar.SECOND, 0);
            if (startCalendar.after(endCalendar)) {
                BraecoWaiterUtils.showToast(mContext, "开始时间不可晚于结束时间");
                return;
            }
            timeString = startString + "~" + String.format("%4d", year) + "-"
                    + String.format("%2d", (monthOfYear + 1)) + "-" + String.format("%2d", dayOfMonth);
            timeTV.setText(timeString);
        }
    }

    private MaterialDialog inputDialog = null;
    private void changeSummary() {
        String title = "修改活动摘要";
        final int min = BraecoWaiterFinal.MIN_ACTIVITY_SUMMARY;
        final int max = BraecoWaiterFinal.MAX_ACTIVITY_SUMMARY;
        final String hint = "活动摘要";
        final String fill = summaryString;
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
                            summaryString = materialDialog.getInputEditText().getText().toString();
                            summaryTV.setText(summaryString);
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

    private void changeMore() {

        Intent intent = new Intent(mContext, EditDetailActivity.class);
        intent.putExtra("back", "");
        intent.putExtra("title", "详情介绍");
        intent.putExtra("dialog", "活动 " + BraecoWaiterUtils.cleanActivityParentheses(nameString) + " 详情介绍");
        intent.putExtra("edit", "完成");
        intent.putExtra("hint", "详情介绍");
        intent.putExtra("fill", moreString);
        intent.putExtra("old", moreString);
        intent.putExtra("help", "活动详情介绍能为顾客提供更多信息");
        intent.putExtra("max", BraecoWaiterFinal.MAX_ACTIVITY_DETAIL);
        intent.putExtra("count", true);

        startActivityForResult(intent, CHANGE_DETAIL);
    }

    public void update() {
        if (!oldNameString.equals(nameString)) changed = true;
        if (!oldSummaryString.equals(summaryString)) changed = true;
        if (!oldMoreString.equals(moreString)) changed = true;
        if (!oldTimeString.equals(timeString)) changed = true;

        if (!changed) onBackPressed();
        else save();
    }

    @Override
    public void onBackPressed() {
        if (!oldNameString.equals(nameString)) changed = true;
        if (!oldSummaryString.equals(summaryString)) changed = true;
        if (!oldMoreString.equals(moreString)) changed = true;
        if (!oldTimeString.equals(timeString)) changed = true;

        if (changed) {
            new MaterialDialog.Builder(mContext)
                    .title("尚未保存")
                    .content((activityPosition == -1 ? ALERT_STRING[0] : ALERT_STRING[1]))
                    .neutralText("取消")
                    .negativeText("不保存")
                    .positiveText("保存")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            if (dialogAction == DialogAction.POSITIVE) {
                                save();
                            }
                            if (dialogAction == DialogAction.NEGATIVE) {
                                MeFragmentActivityThemeEdit.super.onBackPressed();
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
        if (noPicture) {
            BraecoWaiterUtils.showToast(mContext, "活动图片不能为空");
            return;
        }
        if (null == nameString || "".equals(nameString)) {
            BraecoWaiterUtils.showToast(mContext, "活动标题不能为空");
            return;
        }
        if (null == timeString || "".equals(timeString)) {
            BraecoWaiterUtils.showToast(mContext, "持续时间不能为空");
            return;
        }
        if (null == summaryString || "".equals(summaryString)) {
            BraecoWaiterUtils.showToast(mContext, "活动摘要不能为空");
            return;
        }
        if (null == moreString || "".equals(moreString)) {
            BraecoWaiterUtils.showToast(mContext, "详情介绍不能为空");
            return;
        }

        if (activityPosition == -1) {
            addActivity();
        } else {
            updateActivity();
        }
    }

    private void addActivity() {
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add theme activity: " + nameString);
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add theme activity: " + timeString);
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add theme activity: " + summaryString);
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add theme activity: " + moreString);

        uploadPicture(false, -1);
    }

    private void updateActivity() {
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update theme activity: " + nameString);
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update theme activity: " + timeString);
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update theme activity: " + summaryString);
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update theme activity: " + moreString);

        uploadPicture(true, (Integer)BraecoWaiterData.activities.get(activityPosition).getId());
    }

    private void deleteActivity() {
        new MaterialDialog.Builder(mContext)
                .title("删除活动")
                .content("确认要删除活动 " + BraecoWaiterUtils.cleanActivityParentheses(nameString) + " 吗？")
                .positiveText("删除")
                .negativeText("取消")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            materialDialog.dismiss();
                            progressDialog = new MaterialDialog.Builder(mContext)
                                    .title("删除活动中")
                                    .content("请耐心等待")
                                    .cancelable(false)
                                    .progress(true, 0)
                                    .show();
                            new DeleteActivity()
                                    .execute("http://brae.co/Activity/Remove/"
                                            + (Integer) BraecoWaiterData.activities.get(activityPosition).getId());
                        }
                    }
                })
                .show();
    }

    @Override
    public void clickTitleBack() {
        onBackPressed();
    }

    @Override
    public void doubleClickTitle() {

    }

    @Override
    public void clickTitleEdit() {
        update();
    }


    private class DeleteActivity extends AsyncTask<String, Void, String> {

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
                Log.d("BraecoWaiter", "Delete activity: " + result);
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
                UrlEncodedFormEntity e = new UrlEncodedFormEntity(
                        pairList, "UTF-8");
                e.setContentEncoding(HTTP.UTF_8);
                HttpEntity requestHttpEntity = e;

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

            Log.d("BraecoWaiter", "Delete activity: " + result);

            progressDialog.dismiss();

            if (result != null) {
                JSONObject array;
                try {
                    array = new JSONObject(result);
                    if ("success".equals(array.getString("message"))) {
                        BraecoWaiterApplication.JUST_DELETE_ACTIVITY = true;
                        progressDialog.dismiss();
                        BraecoWaiterApplication.JUST_ACTIVITY_NAME
                                = BraecoWaiterUtils.cleanActivityParentheses(nameString);
                        finish();
                    } else {
                        BraecoWaiterUtils.showToast(mContext, "删除活动 "
                                + BraecoWaiterUtils.cleanActivityParentheses(nameString) + " 失败");
                    }
                } catch (JSONException e) {
                    BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Delete activity json error1");
                    e.printStackTrace();
                }
            } else {
                BraecoWaiterUtils.showToast(mContext, "网络连接失败");
            }
        }
    }

    private class AddActivity extends AsyncTask<String, Void, String> {

        private boolean isUpdated = false;

        public AddActivity(boolean isUpdated) {
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
                if (isUpdated) Log.d("BraecoWaiter", "Update activity: " + result);
                else Log.d("BraecoWaiter", "Add activity: " + result);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> pairList = new ArrayList<>();
            if (activityPosition != -1)
                pairList.add(new BasicNameValuePair("id", "" + BraecoWaiterData.activities.get(activityPosition).getId()));
            pairList.add(new BasicNameValuePair("date_begin", params[1]));
            pairList.add(new BasicNameValuePair("date_end", params[2]));
            pairList.add(new BasicNameValuePair("title", params[3]));
            pairList.add(new BasicNameValuePair("content", params[4]));
            pairList.add(new BasicNameValuePair("type", params[5]));
            pairList.add(new BasicNameValuePair("intro", params[6]));
            try {
                UrlEncodedFormEntity e = new UrlEncodedFormEntity(
                        pairList, "UTF-8");
                e.setContentEncoding(HTTP.UTF_8);
                HttpEntity requestHttpEntity = e;

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

            if (isUpdated) Log.d("BraecoWaiter", "Update activity: " + result);
            else Log.d("BraecoWaiter", "Add activity: " + result);

            progressDialog.dismiss();

            if (result != null) {
                JSONObject array;
                try {
                    array = new JSONObject(result);
                    if ("success".equals(array.getString("message"))) {
                        if (isUpdated) {
                            progressDialog.dismiss();
                            BraecoWaiterApplication.JUST_ACTIVITY_NAME
                                    = BraecoWaiterUtils.cleanActivityParentheses(nameString);
                            BraecoWaiterApplication.JUST_UPDATE_ACTIVITY = true;
                            finish();
                        } else {
                            progressDialog.dismiss();
                            BraecoWaiterApplication.JUST_ACTIVITY_NAME
                                    = BraecoWaiterUtils.cleanActivityParentheses(nameString);
                            BraecoWaiterApplication.JUST_ADD_ACTIVITY = true;
                            finish();
                        }
                    } else if ("Need to upload pic first".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "需要先上传活动图片");
                    } else if ("Every dinner can only has one of this type".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "餐品不存在");
                    } else if ("Invalid detail".equals(array.getString("message"))) {
                        BraecoWaiterUtils.showToast(mContext, "详情介绍不合法");
                    } else {
                        BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                    }
                } catch (JSONException e) {
                    BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add activity json error1");
                    e.printStackTrace();
                }
            } else {
                failToSave(isUpdated, "网络连接失败");
            }
        }
    }

    private int activityId = -1;
    private void uploadPicture(boolean isUpdated, int id) {
        activityId = id;
        if (pictureUploaded) {
            if (isUpdated) {
                
            }
            return;
        }
        if (!isUpdated) {
            progressDialog = new MaterialDialog.Builder(mContext)
                    .title("上传活动图片中")
                    .content("请耐心等待")
                    .progress(false, 100, true)
                    .cancelable(false)
                    .show();
            new GetToken(isUpdated)
                    .execute("http://brae.co/pic/upload/token/activityadd");
        } else {
            if (pictureChange) {
                progressDialog = new MaterialDialog.Builder(mContext)
                        .title("上传活动图片中")
                        .content("请耐心等待")
                        .progress(false, 100, true)
                        .cancelable(false)
                        .show();
                new GetToken(isUpdated)
                        .execute("http://brae.co/pic/upload/token/activityupdate/" + id);
            } else {
                progressDialog = new MaterialDialog.Builder(mContext)
                        .title("修改活动中")
                        .content("请耐心等待")
                        .progress(true, 0)
                        .cancelable(false)
                        .show();
                new AddActivity(isUpdated)
                        .execute(
                                "http://brae.co/Activity/Add",
                                timeString.substring(0, 10),
                                timeString.substring(11, 21),
                                nameString,
                                moreString,
                                "theme",
                                summaryString);
            }
        }

    }

    private class GetToken extends AsyncTask<String, Void, String> {

        private boolean isUpdate = false;

        public GetToken(boolean isUpdate) {
            this.isUpdate = isUpdate;
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
                                            pictureUploaded = true;
                                            new AddActivity(isUpdate)
                                                    .execute(
                                                            "http://brae.co/Activity/Add",
                                                            timeString.substring(0, 10),
                                                            timeString.substring(11, 21),
                                                            nameString,
                                                            moreString,
                                                            "theme",
                                                            summaryString);
                                            if (isUpdate) progressDialog.setContent("活动图片上传完成，正在修改活动，请耐心等待");
                                            else progressDialog.setContent("活动图片上传完成，正在新建活动，请耐心等待");
                                        } else {
                                            if (progressDialog != null) progressDialog.dismiss();
                                            failToUpload(isUpdate);
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
                        failToUpload(isUpdate);
                    }
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentActivityThemeEdit json error");
                    e.printStackTrace();
                    if (progressDialog != null) progressDialog.dismiss();
                    failToUpload(isUpdate);
                }
            } else {
                if (progressDialog != null) progressDialog.dismiss();
                failToUpload(isUpdate);
            }
        }
    }

    private void failToUpload(final boolean isUpdate) {
        new MaterialDialog.Builder(mContext)
                .title(isUpdate ? "修改活动失败" : "新建活动失败")
                .content("您的活动图片由于网络问题上传失败")
                .positiveText("确认")
                .negativeText("重试")
                .negativeColorRes(R.color.primaryBrown)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.NEGATIVE) {
                            uploadPicture(isUpdate, activityId);
                        }
                        if (dialogAction == DialogAction.POSITIVE) {
                        }
                    }
                })
                .show();
    }

    private void failToSave(final boolean isUpdate, String info) {
        new MaterialDialog.Builder(mContext)
                .title(isUpdate ? "修改活动失败" : "新建活动失败")
                .content(info)
                .positiveText("确认")
                .negativeText("重试")
                .negativeColorRes(R.color.primaryBrown)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.NEGATIVE) {
                            uploadPicture(isUpdate, activityId);
                        }
                        if (dialogAction == DialogAction.POSITIVE) {
                        }
                    }
                })
                .show();
    }
}
