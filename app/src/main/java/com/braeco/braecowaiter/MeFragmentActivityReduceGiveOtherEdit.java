package com.braeco.braecowaiter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Enums.ActivityType;
import com.braeco.braecowaiter.Model.Authority;
import com.braeco.braecowaiter.Model.AuthorityManager;
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
import org.json.JSONArray;
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

public class MeFragmentActivityReduceGiveOtherEdit extends BraecoAppCompatActivity
        implements
        View.OnClickListener,
        com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener,
        AdapterView.OnItemClickListener,
        TitleLayout.OnTitleActionListener {

    private static final String[] ALERT_STRING = new String[]{"是否保存您新建的活动？", "是否保存您对该活动的修改？"};
    private static final int CHANGE_PHOTO = 1;
    private static String SIZE = null;
    private static final int CHANGE_MORE = 0;
    private static final int CHANGE_DETAIL = 2;

    private LinearLayout name;
    private LinearLayout time;
    private LinearLayout summary;
    private LinearLayout more;

    private TitleLayout title;
    private ScrollView scrollView;
    private TextView emptyTip;
    private TextView nameTV;
    private TextView timeTV;
    private TextView summaryTV;
    private TextView detailTV;
    private ExpandedListView listView;
    private MeFragmentActivityDetailAdapter adapter;
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
    private ArrayList<Object> detail;

    private String token = "";
    private String key = "";

    private String oldNameString = "";
    private String oldTimeString = "";
    private String oldSummaryString = "";
    private ArrayList<Object> oldDetail;

    private ActivityType type = ActivityType.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_activity_reduce_give_edit);

        mContext = this;

        activityPosition = getIntent().getIntExtra("position", -1);
        if (activityPosition != -1) type = BraecoWaiterData.activities.get(activityPosition).getType();

        if (SIZE == null) {
            SIZE = "?imageView2/1/w/" + getResources().getDisplayMetrics().widthPixels
                    + "/h/" + BraecoWaiterUtils.dp2px(200, mContext);
        }

        name = (LinearLayout) findViewById(R.id.name);
        time = (LinearLayout) findViewById(R.id.time);
        summary = (LinearLayout) findViewById(R.id.more);
        more = (LinearLayout) findViewById(R.id.detail);

        image = (KenBurnsView) findViewById(R.id.image);
        title = (TitleLayout) findViewById(R.id.title_layout);
        scrollView = (ScrollView)findViewById(R.id.scroll_view);
        emptyTip = (TextView) findViewById(R.id.empty_tip);
        nameTV = (TextView) findViewById(R.id.name_text);
        timeTV = (TextView) findViewById(R.id.time_text);
        summaryTV = (TextView) findViewById(R.id.more_text);
        detailTV = (TextView)findViewById(R.id.detail_type);
        deleteTV = (TextView) findViewById(R.id.delete);
        if (activityPosition == -1) deleteTV.setVisibility(View.GONE);

        title.setOnTitleActionListener(this);
        name.setOnClickListener(this);
        time.setOnClickListener(this);
        summary.setOnClickListener(this);
        more.setOnClickListener(this);
        emptyTip.setOnClickListener(this);
        image.setOnClickListener(this);
        deleteTV.setOnClickListener(this);

        if (activityPosition != -1) {
            title.setTitle("活动修改");
            nameString = BraecoWaiterData.activities.get(activityPosition).getTitle();
            nameTV.setText(nameString);
            timeString = (BraecoWaiterData.activities.get(activityPosition).getStartDate()) +
                    "~" + (BraecoWaiterData.activities.get(activityPosition).getEndDate());
            timeTV.setText(timeString);
            if (BraecoWaiterData.activities.get(activityPosition).getContent() == null
                    || "null".equals(BraecoWaiterData.activities.get(activityPosition).getContent())) {
                summaryString = "";
            } else {
                summaryString = BraecoWaiterData.activities.get(activityPosition).getContent();
            }
            summaryTV.setText(summaryString);
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

            detail = BraecoWaiterData.activities.get(activityPosition).getDetails();
            oldDetail = (ArrayList<Object>) detail.clone();
            detail = (ArrayList<Object>)oldDetail.clone();

            setTypeText();

            adapter = new MeFragmentActivityDetailAdapter(BraecoWaiterData.activities.get(activityPosition).getType(), detail);
            adapter.setType(type);
            listView = (ExpandedListView) findViewById(R.id.detail_list);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);

            oldNameString = nameString;
            oldTimeString = timeString;
            oldSummaryString = summaryString;
        } else {
            title.setTitle(("新建活动"));
            nameString = "";
            nameTV.setText("");
            timeString = "";
            timeTV.setText("");
            summaryString = "";
            summaryTV.setText("");
            detailTV.setText("");
            image.setVisibility(View.INVISIBLE);
            emptyTip.setVisibility(View.VISIBLE);

            detail = new ArrayList<>();
            oldDetail = new ArrayList<>();

            detailTV.setText("");

            adapter = new MeFragmentActivityDetailAdapter(type, detail);
            adapter.setType(type);
            listView = (ExpandedListView) findViewById(R.id.detail_list);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);

            oldNameString = nameString;
            oldTimeString = timeString;
            oldSummaryString = summaryString;
        }

    }

    private void setTypeText() {
        switch (type) {
            case REDUCE:
                detailTV.setText("满减活动");
                break;
            case GIVE:
                detailTV.setText("满送活动");
                break;
            case OTHER:
                detailTV.setText("其他活动");
                break;
        }
    }

    private String getTypeParam() {
        switch (type) {
            case REDUCE:
                return "reduce";
            case GIVE:
                return "give";
            case OTHER:
                return "other";
        }
        return "";
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private MaterialDialog typeDialog = null;
    private boolean hasReduce = false;
    private boolean hasGive = false;
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
            case R.id.name:
                changeName();
                break;
            case R.id.time:
                changeTime();
                break;
            case R.id.more:
                changeSummary();
                break;
            case R.id.detail:
                if (detail.size() == 0) {
                    hasReduce = false;
                    for (int i = 0; i < BraecoWaiterData.activities.size(); i++) {
                        if (BraecoWaiterData.activities.get(i).getType().equals(ActivityType.REDUCE)) {
                            hasReduce = true;
                            break;
                        }
                    }
                    hasGive = false;
                    for (int i = 0; i < BraecoWaiterData.activities.size(); i++) {
                        if (BraecoWaiterData.activities.get(i).getType().equals(ActivityType.GIVE)) {
                            hasGive = true;
                            break;
                        }
                    }
                    String positiveText = (hasReduce ? "已存在满减活动" : "满减活动");
                    String negativeText = (hasGive ? "已存在满送活动" : "满送活动");
                    typeDialog = new MaterialDialog.Builder(mContext)
                            .title("活动类型")
                            .positiveText(positiveText)
                            .positiveColorRes(R.color.primaryBrown)
                            .negativeText(negativeText)
                            .negativeColorRes(R.color.primaryBrown)
                            .neutralText("其他活动")
                            .neutralColorRes(R.color.primaryBrown)
                            .forceStacking(true)
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    if (dialogAction == DialogAction.POSITIVE) {
                                        type = ActivityType.REDUCE;
                                        adapter.setType(type);
                                        setTypeText();
                                        addDetail(detail.size(), null, null, null);
                                    } else if (dialogAction == DialogAction.NEGATIVE) {
                                        type = ActivityType.GIVE;
                                        adapter.setType(type);
                                        setTypeText();
                                        addDetail(detail.size(), null, null, null);
                                    } else if (dialogAction == DialogAction.NEUTRAL) {
                                        type = ActivityType.OTHER;
                                        adapter.setType(type);
                                        setTypeText();
                                        Intent intent = new Intent(mContext, EditDetailActivity.class);
                                        intent.putExtra("back", "");
                                        intent.putExtra("title", "活动详情");
                                        intent.putExtra("dialog", "活动 " + BraecoWaiterUtils.cleanActivityParentheses(nameString) + " 活动详情");
                                        intent.putExtra("edit", "完成");
                                        intent.putExtra("hint", "活动详情");
                                        intent.putExtra("fill", "");
                                        intent.putExtra("old", "");
                                        intent.putExtra("help", "活动详情介绍能为顾客提供更多信息");
                                        intent.putExtra("min", BraecoWaiterFinal.MIN_ACTIVITY_DETAIL);
                                        intent.putExtra("max", BraecoWaiterFinal.MAX_ACTIVITY_DETAIL);
                                        intent.putExtra("count", true);

                                        startActivityForResult(intent, CHANGE_DETAIL);
                                    }
                                }
                            })
                            .showListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    typeDialog.getActionButton(DialogAction.POSITIVE).setEnabled(!hasReduce);
                                    typeDialog.getActionButton(DialogAction.NEGATIVE).setEnabled(!hasGive);
                                }
                            })
                            .show();
                } else {
                    if (type.equals(ActivityType.OTHER)) {
                        listView.performItemClick(listView.getChildAt(0), 0, listView.getItemIdAtPosition(0));
                    } else {
                        addDetail(detail.size(), null, null, null);
                    }
                }
                break;
            case R.id.edit:
                update();
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
                if (imgFile.exists()) {
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
        } else if (resultCode == RESULT_OK && requestCode == CHANGE_MORE) {
            boolean changed = data.getBooleanExtra("changed", false);
            summaryString = data.getStringExtra("new");
            summaryTV.setText(summaryString);
        } else if (resultCode == RESULT_OK && requestCode == CHANGE_DETAIL) {
            boolean changed = data.getBooleanExtra("changed", false);
            String backString = data.getStringExtra("new");
            if ("".equals(backString)) return;
            if (detail.size() == 0) detail.add(backString);
            else detail.set(0, backString);
            adapter.notifyDataSetChanged();
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

    private MaterialDialog inputDialog = null;
    private void changeName() {
        String title = "修改活动名";
        final int min = BraecoWaiterFinal.MIN_ACTIVITY_NAME;
        final int max = BraecoWaiterFinal.MAX_ACTIVITY_NAME;
        final String hint = "活动名";
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

    private void changeSummary() {
        Intent intent = new Intent(mContext, EditDetailActivity.class);
        intent.putExtra("back", "");
        intent.putExtra("title", "详情介绍");
        intent.putExtra("dialog", "活动 " + BraecoWaiterUtils.cleanActivityParentheses(nameString) + " 详情介绍");
        intent.putExtra("edit", "完成");
        intent.putExtra("hint", "详情介绍");
        intent.putExtra("fill", summaryString);
        intent.putExtra("old", summaryString);
        intent.putExtra("help", "活动详情介绍能为顾客提供更多信息");
        intent.putExtra("max", BraecoWaiterFinal.MAX_ACTIVITY_DETAIL);
        intent.putExtra("count", true);

        startActivityForResult(intent, CHANGE_MORE);
    }

    private int least = -1;
    private int reduce = -1;
    private void addDetail(
            final int position,
            @Nullable final String leastFill,
            @Nullable final String giveFill,
            @Nullable final String reduceFill) {
        String content = "";
        if (type.equals(ActivityType.REDUCE)) {
            if (detail.size() > 0) {
                content = "满足满减条件的金额必须大于前一金额（" + BraecoWaiterUtils.money2Digit((Integer)detail.get(position - 2) * 1.0) + "）";
            } else {
                content = "";
            }
        } else if (type.equals(ActivityType.GIVE)) {
            if (detail.size() > 0) {
                content = "满足满送条件的金额必须大于前一金额（" + BraecoWaiterUtils.money2Digit((Integer)detail.get(position - 2) * 1.0) + "）";
            } else {
                content = "";
            }
        }
        new MaterialDialog.Builder(mContext)
                .title("满")
                .content(content)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .positiveText("确定")
                .negativeText("取消")
                .input("", (leastFill == null ? "" : leastFill), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                        least = -1;
                        try {
                            least = Integer.parseInt(String.valueOf(input));
                        } catch (NumberFormatException n) {
                            least = -1;
                        }
                        if (detail.size() > 0) {
                            if (least <= (Integer)detail.get(position - 2)) {
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                            }
                        } else {
                            if (least == -1) {
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                            }
                        }
                    }
                })
                .alwaysCallInputCallback()
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            materialDialog.dismiss();

                            if (type.equals(ActivityType.REDUCE)) {
                                String subContent = "";
                                if (detail.size() > 0) {
                                    subContent = "减免金额必须大于前一金额（" + BraecoWaiterUtils.money2Digit((Integer)detail.get(position - 1) * 1.0) + "）";
                                } else {
                                    subContent = "";
                                }
                                new MaterialDialog.Builder(mContext)
                                        .title("减")
                                        .content(subContent)
                                        .inputType(InputType.TYPE_CLASS_NUMBER)
                                        .positiveText("确定")
                                        .negativeText("取消")
                                        .input("", (reduceFill == null ? "" : reduceFill), new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                                reduce = -1;
                                                try {
                                                    reduce = Integer.parseInt(String.valueOf(input));
                                                } catch (NumberFormatException n) {
                                                    reduce = -1;
                                                }
                                                if (detail.size() > 0) {
                                                    if (reduce <= (Integer)detail.get(position - 1)) {
                                                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                                    }
                                                } else {
                                                    if (reduce == -1) {
                                                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                                    }
                                                }
                                            }
                                        })
                                        .alwaysCallInputCallback()
                                        .onAny(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                                if (dialogAction == DialogAction.POSITIVE) {
                                                    materialDialog.dismiss();
                                                    if (type.equals(ActivityType.REDUCE)) {
                                                        if (position < detail.size()) {
                                                            detail.set(position, least);
                                                            detail.set(position + 1, reduce);
                                                        } else {
                                                            detail.add(least);
                                                            detail.add(reduce);
                                                        }
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        })
                                        .show();
                            } else if (type.equals(ActivityType.GIVE)) {
                                String title = "送";
                                final int min = BraecoWaiterFinal.MIN_ACTIVITY_GIVE;
                                final int max = BraecoWaiterFinal.MAX_ACTIVITY_GIVE;
                                final String hint = "赠品";
                                final String fill = (giveFill == null ? "" : giveFill);
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
                                                    if (position < detail.size()) {
                                                        detail.set(position, least);
                                                        detail.set(position + 1, materialDialog.getInputEditText().getText().toString());
                                                    } else {
                                                        detail.add(least);
                                                        detail.add(materialDialog.getInputEditText().getText().toString());
                                                    }
                                                    adapter.notifyDataSetChanged();
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
                        }
                    }
                })
                .show();
    }

    public void update() {
        if (!oldNameString.equals(nameString)) changed = true;
        if (!oldSummaryString.equals(summaryString)) changed = true;
        if (!BraecoWaiterUtils.isSameListObject(oldDetail, detail)) changed = true;
        if (!oldTimeString.equals(timeString)) changed = true;

        if (!changed) onBackPressed();
        else save();
    }

    @Override
    public void onBackPressed() {
        if (!oldNameString.equals(nameString)) changed = true;
        if (!oldSummaryString.equals(summaryString)) changed = true;
        if (!BraecoWaiterUtils.isSameListObject(oldDetail, detail)) changed = true;
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
                                MeFragmentActivityReduceGiveOtherEdit.super.onBackPressed();
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
        if (type.equals(ActivityType.OTHER) && !AuthorityManager.ableTo(Authority.MANAGER_ACTIVITY)) {
            AuthorityManager.showDialog(mContext, "编辑其他促销活动");
            return;
        }
        if ((type.equals(ActivityType.GIVE) || type.equals(ActivityType.REDUCE)) && !AuthorityManager.ableTo(Authority.MANAGER_DISCOUNT)) {
            AuthorityManager.showDialog(mContext, "编辑促销活动");
            return;
        }
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
            BraecoWaiterUtils.showToast(mContext, "详情介绍不能为空");
            return;
        }
        if (detail.size() == 0) {
            BraecoWaiterUtils.showToast(mContext, "活动详情不能为空");
            return;
        }

        if (activityPosition == -1) {
            addActivity();
        } else {
            updateActivity();
        }
    }

    private void addActivity() {
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add RG activity: " + nameString);
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add RG activity: " + timeString);
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add RG activity: " + summaryString);
        if (BuildConfig.DEBUG) {
            String detailString = "";
            if (type.equals(ActivityType.REDUCE)) {
                for (int i = 0; i < detail.size(); i++) {
                    detailString += " " + (Integer)detail.get(i);
                }
            } else if (type.equals(ActivityType.GIVE)) {
                for (int i = 0; i < detail.size(); i += 2) {
                    detailString += (Integer)detail.get(i) + (String)detail.get(i + 1);
                }
            }
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Add RG activity: " + detailString);

        }

        uploadPicture(false, -1);
    }

    private void updateActivity() {
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update RG activity: " + nameString);
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update RG activity: " + timeString);
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update RG activity: " + summaryString);
        if (BuildConfig.DEBUG) {
            String detailString = "";
            if (type.equals(ActivityType.REDUCE)) {
                for (int i = 0; i < detail.size(); i++) {
                    detailString += " " + (Integer)detail.get(i);
                }
            } else if (type.equals(ActivityType.GIVE)) {
                for (int i = 0; i < detail.size(); i += 2) {
                    detailString += (Integer)detail.get(i) + (String)detail.get(i + 1);
                }
            } else if (type.equals(ActivityType.OTHER)) {
                detailString = detail.get(0) + "";
            }
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Update RG activity: " + detailString);
        }

        uploadPicture(true, BraecoWaiterData.activities.get(activityPosition).getId());
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
                                            + BraecoWaiterData.activities.get(activityPosition).getId());
                        }
                    }
                })
                .show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        String content = "";
        if (!type.equals(ActivityType.OTHER)) {
            content = "消费满 ";
            content += "¥" + String.format("%.2f", (Integer)detail.get(position * 2) * 1.0);
            if (type.equals(ActivityType.REDUCE)) {
                content += " 减 ";
                content += "¥" + String.format("%.2f", (Integer)detail.get(position * 2 + 1) * 1.0);
            }
            if (type.equals(ActivityType.GIVE)) {
                content += " 送 ";
                content += detail.get(position * 2 + 1);
            }
        }

        new MaterialDialog.Builder(mContext)
                .title("操作")
                .content(content)
                .positiveText("修改")
                .positiveColorRes(R.color.primaryBrown)
                .negativeText("删除")
                .negativeColorRes(R.color.primaryBrown)
                .neutralText("取消")
                .neutralColorRes(R.color.refund_button)
                .forceStacking(true)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            if (type.equals(ActivityType.OTHER)) {
                                Intent intent = new Intent(mContext, EditDetailActivity.class);
                                intent.putExtra("back", "");
                                intent.putExtra("title", "活动详情");
                                intent.putExtra("dialog", "活动 " + BraecoWaiterUtils.cleanActivityParentheses(nameString) + " 活动详情");
                                intent.putExtra("edit", "完成");
                                intent.putExtra("hint", "活动详情");
                                intent.putExtra("fill", detail.get(0) + "");
                                intent.putExtra("old", detail.get(0) + "");
                                intent.putExtra("help", "活动详情介绍能为顾客提供更多信息");
                                intent.putExtra("min", BraecoWaiterFinal.MIN_ACTIVITY_DETAIL);
                                intent.putExtra("max", BraecoWaiterFinal.MAX_ACTIVITY_DETAIL);
                                intent.putExtra("count", true);

                                startActivityForResult(intent, CHANGE_DETAIL);
                                return;
                            }
                            addDetail(position * 2, detail.get(position * 2) + "", detail.get(position * 2 + 1) + "", detail.get(position * 2 + 1) + "");
                        } else if (dialogAction == DialogAction.NEGATIVE) {
                            if (type.equals(ActivityType.OTHER)) {
                                detail.remove(0);
                                adapter.notifyDataSetChanged();
                                detailTV.setText("");
                                return;
                            }
                            detail.remove(position * 2);
                            detail.remove(position * 2);
                            adapter.notifyDataSetChanged();
                            if (detail.size() == 0) {
                                detailTV.setText("");
                            }
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
        scrollView.fullScroll(ScrollView.FOCUS_UP);
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
                httpPost.addHeader("Cookie", "sid=" + BraecoWaiterApplication.sid);
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
            pairList.add(new BasicNameValuePair("detail", params[6]));
            try {
                UrlEncodedFormEntity e = new UrlEncodedFormEntity(
                        pairList, "UTF-8");
                e.setContentEncoding(HTTP.UTF_8);
                HttpEntity requestHttpEntity = e;

                HttpPost httpPost = new HttpPost(params[0]);
                httpPost.addHeader("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
                httpPost.addHeader("Cookie", "sid=" + BraecoWaiterApplication.sid);
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
                String json = getDetailJsonString();
                new AddActivity(isUpdated)
                        .execute(
                                "http://brae.co/Activity/Add",
                                timeString.substring(0, 10),
                                timeString.substring(11, 21),
                                nameString,
                                summaryString,
                                getTypeParam(),
                                json);
            }
        }

    }


    private String getDetailJsonString() {
        try {
            JSONArray jsonArray = new JSONArray();
            if (type.equals(ActivityType.REDUCE)) {
                for (int i = 0; i < detail.size(); i += 2) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("least", detail.get(i));
                    jsonObject.put("reduce", detail.get(i + 1));
                    jsonArray.put(jsonArray.length(), jsonObject);
                }
            } else if (type.equals(ActivityType.GIVE)) {
                for (int i = 0; i < detail.size(); i += 2) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("least", detail.get(i));
                    jsonObject.put("dish", detail.get(i + 1));
                    jsonArray.put(jsonArray.length(), jsonObject);
                }
            } else if (type.equals(ActivityType.OTHER)) {
                return "\"" + detail.get(0) + "\"";
            }
            return jsonArray.toString();
        } catch (JSONException j) {
            j.printStackTrace();
        }
        return "";
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
                httpPost.addHeader("Cookie", "sid=" + BraecoWaiterApplication.sid);
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
                                        if (BuildConfig.DEBUG)
                                            Log.d("BraecoWaiter", "Upload picture: " + "key: " + key + " res: " + res);
                                        // finish uploading
                                        if (info.isOK()) {
                                            pictureUploaded = true;
                                            String json = getDetailJsonString();
                                            new AddActivity(isUpdate)
                                                    .execute(
                                                            "http://brae.co/Activity/Add",
                                                            timeString.substring(0, 10),
                                                            timeString.substring(11, 21),
                                                            nameString,
                                                            summaryString,
                                                            getTypeParam(),
                                                            json);
                                            if (isUpdate)
                                                progressDialog.setContent("活动图片上传完成，正在修改活动，请耐心等待");
                                            else progressDialog.setContent("活动图片上传完成，正在新建活动，请耐心等待");
                                        } else {
                                            if (progressDialog != null) progressDialog.dismiss();
                                            failToUpload(isUpdate);
                                        }
                                    }
                                },
                                new UploadOptions(null, null, false,
                                        new UpProgressHandler() {
                                            public void progress(String key, double percent) {
                                                progressDialog.setProgress((int) (percent * 100));
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
                    if (BuildConfig.DEBUG)
                        Log.d("BraecoWaiter", "MeFragmentActivityThemeEdit json error");
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
