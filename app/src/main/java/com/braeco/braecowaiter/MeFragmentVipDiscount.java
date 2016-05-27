package com.braeco.braecowaiter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MeFragmentVipDiscount extends BraecoAppCompatActivity
        implements View.OnClickListener {

    private String[] LEVEL_NAME = new String[]{"LV.0 黑铁级", "LV.1 青铜级", "LV.2 白银级", "LV.3 黄金级", "LV.4 白金级", "LV.5 钻石级"};
    private String[] LEVEL_JSON_NAME = new String[]{"黑铁", "青铜", "白银", "黄金", "白金", "钻石"};

    private LinearLayout back;

    private LinearLayout lv1;
    private LinearLayout lv2;
    private LinearLayout lv3;
    private LinearLayout lv4;
    private LinearLayout lv5;

    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private TextView tv4;
    private TextView tv5;

    private LinearLayout over;

    private EditText exp;
    private EditText discount;

    private Context mContext;

    private int[] newLadderExp = new int[6];
    private int[] newLadderDiscount = new int[6];

    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_vip_discount);

        mContext = this;

        back = (LinearLayout)findViewById(R.id.back);
        back.setOnClickListener(this);

        lv1 = (LinearLayout)findViewById(R.id.level_1);
        lv1.setOnClickListener(this);
        lv2 = (LinearLayout)findViewById(R.id.level_2);
        lv2.setOnClickListener(this);
        lv3 = (LinearLayout)findViewById(R.id.level_3);
        lv3.setOnClickListener(this);
        lv4 = (LinearLayout)findViewById(R.id.level_4);
        lv4.setOnClickListener(this);
        lv5 = (LinearLayout)findViewById(R.id.level_5);
        lv5.setOnClickListener(this);
        over = (LinearLayout)findViewById(R.id.over);
        over.setOnClickListener(this);

        tv1 = (TextView)findViewById(R.id.level_1_info);
        tv2 = (TextView)findViewById(R.id.level_2_info);
        tv3 = (TextView)findViewById(R.id.level_3_info);
        tv4 = (TextView)findViewById(R.id.level_4_info);
        tv5 = (TextView)findViewById(R.id.level_5_info);

        BraecoWaiterApplication.ladderExp[6] = BraecoWaiterUtils.MAX_EXP + 1;
        BraecoWaiterApplication.ladderDiscount[6] = 9;

        getData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.level_1:
                changeLevel(1);
                break;
            case R.id.level_2:
                changeLevel(2);
                break;
            case R.id.level_3:
                changeLevel(3);
                break;
            case R.id.level_4:
                changeLevel(4);
                break;
            case R.id.level_5:
                changeLevel(5);
                break;
            case R.id.over:
                startActivity(new Intent(mContext, MeFragmentVipDiscountOver.class));
                break;
            case R.id.back:
                finish();
                break;
        }
    }

    private void getData() {
        progressDialog = new MaterialDialog.Builder(mContext)
                .title("正在获取当前折扣信息")
                .content("请稍候")
                .cancelable(false)
                .negativeText("取消")
                .progress(true, 0)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.NEGATIVE) {
                            BraecoWaiterApplication.ME_FRAGMENT_VIP_DISCOUNT_TASK_NUM++;
                            progressDialog.dismiss();
                            finish();
                        }
                    }
                })
                .show();
        BraecoWaiterApplication.ME_FRAGMENT_VIP_DISCOUNT_TASK_NUM++;
        new GetLevel(BraecoWaiterApplication.ME_FRAGMENT_VIP_DISCOUNT_TASK_NUM)
                .execute("http://brae.co/Membership/Rule/Get");
    }

    private void setText() {
        tv1.setText("积分" + BraecoWaiterApplication.ladderExp[1] + "，折扣" + BraecoWaiterApplication.ladderDiscount[1] + "%");
        tv2.setText("积分" + BraecoWaiterApplication.ladderExp[2] + "，折扣" + BraecoWaiterApplication.ladderDiscount[2] + "%");
        tv3.setText("积分" + BraecoWaiterApplication.ladderExp[3] + "，折扣" + BraecoWaiterApplication.ladderDiscount[3] + "%");
        tv4.setText("积分" + BraecoWaiterApplication.ladderExp[4] + "，折扣" + BraecoWaiterApplication.ladderDiscount[4] + "%");
        tv5.setText("积分" + BraecoWaiterApplication.ladderExp[5] + "，折扣" + BraecoWaiterApplication.ladderDiscount[5] + "%");
    }

    private void changeLevel(final int i) {
        MaterialDialog materialDialog = new MaterialDialog.Builder(this)
                .title(LEVEL_NAME[i])
                .customView(R.layout.dialog_level, true)
                .positiveText("确认")
                .negativeText("取消")
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        InputMethodManager keyboard
                                = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        keyboard.showSoftInput(exp, InputMethodManager.SHOW_IMPLICIT);
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        View view = ((Activity)mContext).getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    }
                })
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            // new the post
                            int newExp = 0;
                            if (exp.getText().toString().length() == 0) {
                                newExp = 0;
                            } else {
                                try {
                                    newExp = Integer.parseInt(exp.getText().toString());
                                } catch (NumberFormatException n) {
                                    newExp = -1;
                                }
                            }
                            int newDiscount = 0;
                            if (discount.getText().toString().length() == 0) {
                                newDiscount = 0;
                            } else {
                                try {
                                    newDiscount = Integer.parseInt(discount.getText().toString());
                                } catch (NumberFormatException n) {
                                    newDiscount = -1;
                                }
                            }
                            if (!(0 <= newExp && newExp <= BraecoWaiterUtils.MAX_EXP)) {
                                YoYo.with(Techniques.Shake)
                                        .duration(700)
                                        .playOn(exp);
                                BraecoWaiterUtils.showToast(mContext, "积分必须是0到" + BraecoWaiterUtils.MAX_EXP + "的整数");
                                return;
                            }
                            if (!(10 <= newDiscount && newDiscount <= 100)) {
                                YoYo.with(Techniques.Shake)
                                        .duration(700)
                                        .playOn(discount);
                                BraecoWaiterUtils.showToast(mContext, "折扣必须是10到100的整数");
                                return;
                            }
                            if (!(BraecoWaiterApplication.ladderExp[i - 1] < newExp && newExp < BraecoWaiterApplication.ladderExp[i + 1])) {
                                YoYo.with(Techniques.Shake)
                                        .duration(700)
                                        .playOn(exp);
                                BraecoWaiterUtils.showToast(mContext, "升级所需积分必须大于前一等级，小于后一等级");
                                return;
                            }
                            if (!(BraecoWaiterApplication.ladderDiscount[i - 1] > newDiscount && newDiscount > BraecoWaiterApplication.ladderDiscount[i + 1])) {
                                YoYo.with(Techniques.Shake)
                                        .duration(700)
                                        .playOn(discount);
                                BraecoWaiterUtils.showToast(mContext, "折扣必须小于前一等级，大于后一等级");
                                return;
                            }
                            newLadderExp[i] = newExp;
                            newLadderDiscount[i] = newDiscount;
                            try {
                                JSONObject param = new JSONObject();
                                param.put("compatible", BraecoWaiterApplication.compatible);
                                JSONArray levels = new JSONArray();
                                for (int i = 0; i < 6; i++) {
                                    JSONObject level = new JSONObject();
                                    level.put("name", BraecoWaiterUtils.toUnicode(LEVEL_JSON_NAME[i]));
                                    level.put("EXP", newLadderExp[i]);
                                    level.put("discount", newLadderDiscount[i]);
                                    levels.put(i, level);
                                }
                                param.put("ladder", levels);
                                progressDialog = new MaterialDialog.Builder(mContext)
                                        .title("修改折扣设置中")
                                        .content("请稍候")
                                        .cancelable(false)
                                        .progress(true, 0)
                                        .show();
                                String paramString = param.toString();
                                String toString = "{";
                                for (int i = 1; i < paramString.length(); i++) {
                                    if (paramString.charAt(i - 1) == '\\' && paramString.charAt(i) == '\\') {

                                    } else {
                                        toString += paramString.charAt(i);
                                    }
                                }
                                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", toString);
                                new GiveLevel()
                                        .execute("http://brae.co/Membership/Rule/Set",
                                                toString);
                            } catch (JSONException j) {
                                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentVipDiscount json error.");
                                j.printStackTrace();
                            }
                            materialDialog.dismiss();
                        } else if (dialogAction == DialogAction.NEGATIVE) {
                            materialDialog.dismiss();
                        }
                    }
                })
                .autoDismiss(false)
                .show();
        View view = materialDialog.getCustomView();
        exp = (EditText) view.findViewById(R.id.exp);
        discount = (EditText) view.findViewById(R.id.discount);
    }

    private class GetLevel extends AsyncTask<String, Void, String> {

        private int task;

        public GetLevel(int task) {
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

            if (task != BraecoWaiterApplication.ME_FRAGMENT_VIP_DISCOUNT_TASK_NUM) return;

            if (progressDialog != null) progressDialog.dismiss();

            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "GetLevel:" + result);

            if (result != null) {
                JSONObject array;
                try {
                    array = new JSONObject(result);
                    if ("success".equals(array.getString("message"))) {
                        BraecoWaiterApplication.compatible = array.getInt("compatible");
                        JSONArray levels = new JSONArray();
                        levels = array.getJSONArray("ladder");
                        for (int i = 0; i < 6; i++) {
                            BraecoWaiterApplication.ladderExp[i] = levels.getJSONObject(i).getInt("EXP");
                            BraecoWaiterApplication.ladderDiscount[i] = levels.getJSONObject(i).getInt("discount");
                        }
                        setText();
                        for (int i = 0; i < 6; i++) {
                            newLadderExp[i] = BraecoWaiterApplication.ladderExp[i];
                            newLadderDiscount[i] = BraecoWaiterApplication.ladderDiscount[i];
                        }
                    } else {
                        fail();
                    }
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentVipDiscount json error");
                    e.printStackTrace();
                    fail();
                }
            } else {
                fail();
            }
        }
    }

    private void fail() {
        new MaterialDialog.Builder(mContext)
                .title("获取当前折扣信息失败")
                .content("网络连接失败")
                .positiveText("确认")
                .negativeText("重试")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.NEGATIVE) {
                            getData();
                        } else if (dialogAction == DialogAction.POSITIVE) {
                            finish();
                        }
                    }
                })
                .show();
    }

    private class GiveLevel extends AsyncTask<String, Void, String> {

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
            pairList.add(new BasicNameValuePair("data", params[1]));
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(
                        pairList);
                HttpPost httpPost = new HttpPost(params[0]);
                httpPost.addHeader("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
                httpPost.addHeader("Cookie" , "sid=" + BraecoWaiterApplication.sid);
//                httpPost.setEntity(requestHttpEntity);
                httpPost.setEntity(new StringEntity(params[1]));
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

            progressDialog.dismiss();

            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "SetLevel:" + result);

            if (result != null) {
                JSONObject array;
                try {
                    array = new JSONObject(result);
                    if ("success".equals(array.getString("message"))) {
                        for (int i = 0; i < 6; i++) {
                            BraecoWaiterApplication.ladderExp[i] = newLadderExp[i];
                            BraecoWaiterApplication.ladderDiscount[i] = newLadderDiscount[i];
                        }
                        setText();
                        new MaterialDialog.Builder(mContext)
                                .title("修改折扣设置成功")
                                .content("修改折扣设置成功")
                                .positiveText("确认")
                                .show();
                    } else {
                        new MaterialDialog.Builder(mContext)
                                .title("修改折扣设置失败")
                                .content("网络连接失败")
                                .positiveText("确认")
                                .show();
                    }
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentVipDiscount json error");
                    e.printStackTrace();
                    new MaterialDialog.Builder(mContext)
                            .title("修改折扣设置失败")
                            .content("网络连接失败")
                            .positiveText("确认")
                            .show();
                }
            } else {
                new MaterialDialog.Builder(mContext)
                        .title("修改折扣设置失败")
                        .content("网络连接失败")
                        .positiveText("确认")
                        .show();
            }
        }
    }

}
