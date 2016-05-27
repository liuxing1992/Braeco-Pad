package com.braeco.braecowaiter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.github.lguipeng.library.animcheckbox.AnimCheckBox;

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

public class MeFragmentVipDiscountOver extends BraecoAppCompatActivity
        implements View.OnClickListener {

    private String[] LEVEL_NAME = new String[]{"LV.1 青铜级", "LV.2 白银级", "LV.3 黄金级", "LV.4 白金级", "LV.5 钻石级"};
    private String[] LEVEL_JSON_NAME = new String[]{"黑铁", "青铜", "白银", "黄金", "白金", "钻石"};

    private LinearLayout back;

    private LinearLayout discount;
    private LinearLayout minus;
    private LinearLayout half;
    private LinearLayout limit;

    private AnimCheckBox discountCheck;
    private AnimCheckBox minusCheck;
    private AnimCheckBox halfCheck;
    private AnimCheckBox limitCheck;

    private TextView sure;

    private Context mContext;

    private int newCompatible = BraecoWaiterApplication.compatible;

    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_vip_discount_over);

        mContext = this;

        back = (LinearLayout)findViewById(R.id.back);
        back.setOnClickListener(this);

        discount = (LinearLayout)findViewById(R.id.discount);
        discount.setOnClickListener(this);
        minus = (LinearLayout)findViewById(R.id.minus);
        minus.setOnClickListener(this);
        half = (LinearLayout)findViewById(R.id.half);
        half.setOnClickListener(this);
        limit = (LinearLayout)findViewById(R.id.limit);
        limit.setOnClickListener(this);

        discountCheck = (AnimCheckBox)findViewById(R.id.discount_check);
        discountCheck.setOnClickListener(this);
        minusCheck = (AnimCheckBox)findViewById(R.id.minus_check);
        minusCheck.setOnClickListener(this);
        halfCheck = (AnimCheckBox)findViewById(R.id.half_check);
        halfCheck.setOnClickListener(this);
        limitCheck = (AnimCheckBox)findViewById(R.id.limit_check);
        limitCheck.setOnClickListener(this);

        sure = (TextView)findViewById(R.id.sure);
        sure.setOnClickListener(this);

        setCheck();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.discount:
            case R.id.discount_check:
                if (discountCheck.isChecked()) discountCheck.setChecked(false);
                else discountCheck.setChecked(true);
                break;
            case R.id.minus:
            case R.id.minus_check:
                if (minusCheck.isChecked()) minusCheck.setChecked(false);
                else minusCheck.setChecked(true);
                break;
            case R.id.half:
            case R.id.half_check:
                if (halfCheck.isChecked()) halfCheck.setChecked(false);
                else halfCheck.setChecked(true);
                break;
            case R.id.limit:
            case R.id.limit_check:
                if (limitCheck.isChecked()) limitCheck.setChecked(false);
                else limitCheck.setChecked(true);
                break;
            case R.id.sure:
                try {
                    newCompatible = 0;
                    newCompatible += (discountCheck.isChecked() ? 1 : 0);
                    newCompatible += (minusCheck.isChecked() ? 2 : 0);
                    newCompatible += (halfCheck.isChecked() ? 4 : 0);
                    newCompatible += (limitCheck.isChecked() ? 8 : 0);
                    JSONObject param = new JSONObject();
                    param.put("compatible", newCompatible);
                    JSONArray levels = new JSONArray();
                    for (int i = 0; i < 6; i++) {
                        JSONObject level = new JSONObject();
                        level.put("name", BraecoWaiterUtils.chinaToUnicode(LEVEL_JSON_NAME[i]));
                        level.put("EXP", BraecoWaiterApplication.ladderExp[i]);
                        level.put("discount", BraecoWaiterApplication.ladderDiscount[i]);
                        levels.put(i, level);
                    }
                    param.put("ladder", levels);
                    progressDialog = new MaterialDialog.Builder(mContext)
                            .title("修改优惠叠加中")
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
                break;
            case R.id.back:
                finish();
                return;
        }
    }

    private void setCheck() {
        int t = BraecoWaiterApplication.compatible;
        if (t % 2 == 1) discountCheck.setChecked(true);
        else discountCheck.setChecked(false);
        t /= 2;
        if (t % 2 == 1) minusCheck.setChecked(true);
        else minusCheck.setChecked(false);
        t /= 2;
        if (t % 2 == 1) halfCheck.setChecked(true);
        else halfCheck.setChecked(false);
        t /= 2;
        if (t % 2 == 1) limitCheck.setChecked(true);
        else limitCheck.setChecked(false);
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

            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", result);

            if (result != null) {
                JSONObject array;
                try {
                    array = new JSONObject(result);
                    if ("success".equals(array.getString("message"))) {
                        BraecoWaiterApplication.compatible = newCompatible;
                        setCheck();
                        new MaterialDialog.Builder(mContext)
                                .title("修改优惠叠加成功")
                                .content("修改优惠叠加成功")
                                .positiveText("确认")
                                .show();
                    } else {
                        new MaterialDialog.Builder(mContext)
                                .title("修改优惠叠加失败")
                                .content("网络连接失败")
                                .positiveText("确认")
                                .show();
                    }
                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentVipDiscountOver json error");
                    e.printStackTrace();
                    new MaterialDialog.Builder(mContext)
                            .title("修改优惠叠加失败")
                            .content("网络连接失败")
                            .positiveText("确认")
                            .show();
                }
            } else {
                new MaterialDialog.Builder(mContext)
                        .title("修改优惠叠加失败")
                        .content("网络连接失败")
                        .positiveText("确认")
                        .show();
            }
        }
    }
}
