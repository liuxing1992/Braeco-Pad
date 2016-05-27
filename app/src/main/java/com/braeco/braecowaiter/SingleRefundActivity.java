package com.braeco.braecowaiter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SingleRefundActivity extends BraecoAppCompatActivity
        implements
        SingleRefundAdapter.OnOrderListener,
        View.OnClickListener {

    private ExpandedListView meals;
    private SingleRefundAdapter adapter;

    private ExpandedListView unRefundMeals;
    private SingleUnrefundAdapter unRefundAdapter;

    private TextView sum;
    private LinearLayout makeSure;
    private TextView all;
    private TextView discount;

    private Context mContext;

    private LinearLayout back;

    private MaterialDialog progressDialog;

    private LinearLayout remark;
    private TextView remarkText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_refund);

        mContext = this;

        back = (LinearLayout)findViewById(R.id.back);
        back.setOnClickListener(this);

        all = (TextView)findViewById(R.id.all);
        all.setOnClickListener(this);

        discount = (TextView)findViewById(R.id.discount);

        meals = (ExpandedListView)findViewById(R.id.list_view);
        adapter = new SingleRefundAdapter(this);
        meals.setAdapter(adapter);

        remark = (LinearLayout)findViewById(R.id.remark);
        remark.setOnClickListener(this);
        remarkText = (TextView)findViewById(R.id.remark_text);
        remarkText.setText(remarkString);

        if (BraecoWaiterData.unRefundMeals.size() != 0) {
            unRefundMeals = (ExpandedListView)findViewById(R.id.listview2);
            unRefundAdapter = new SingleUnrefundAdapter();
            unRefundMeals.setAdapter(unRefundAdapter);
        } else {
            discount.setVisibility(View.GONE);
        }

        if (unRefundMeals != null) unRefundMeals.setVisibility(View.GONE);
        discount.setVisibility(View.GONE);

        sum = (TextView)findViewById(R.id.sum);
        makeSure = (LinearLayout)findViewById(R.id.make_sure);
        makeSure.setOnClickListener(this);

        calculateSum();
    }

    private void refund() {
        int refundSum = 0;
        for (int i = 0; i < BraecoWaiterData.refundMeals.size(); i++) {
            refundSum += adapter.number.get(i);
        }
        if (refundSum == 0) {
            BraecoWaiterUtils.showToast(mContext, "请选择需要退款的餐品");
            return;
        }
        new MaterialDialog.Builder(mContext)
                .title("退款")
                .content(sum.getText().toString() + "，请输入退款密码\n（注意：一旦确认不可撤回）")
                .positiveText("确认")
                .negativeText("取消")
                .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            if (materialDialog.getInputEditText().getText().toString().equals(BraecoWaiterApplication.password)) {
                                progressDialog = new MaterialDialog.Builder(mContext)
                                        .title("退款中")
                                        .content("请稍候")
                                        .cancelable(false)
                                        .progress(true, 0)
                                        .show();
                                new Refund().execute(
                                        "http://brae.co/order/refund/" + BraecoWaiterData.refundId,
                                        getRefundString(),
                                        remarkString,
                                        BraecoWaiterApplication.password);
                            } else {
                                BraecoWaiterUtils.showToast(BraecoWaiterApplication.getAppContext(), "密码错误，请重新输入");
                            }
                        }
                        if (dialogAction == DialogAction.NEUTRAL) {
                            materialDialog.dismiss();
                        }
                    }
                })
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {

                    }
                })
                .show();
    }

    @Override
    public void OnOrderListen() {
        boolean selectAll = true;
        for (int i = adapter.number.size() - 1; i >= 0; i--) {
            if (!adapter.number.get(i).equals((Integer) BraecoWaiterData.refundMeals.get(i).get("sum"))) {
                selectAll = false;
                break;
            }
        }
        if (selectAll) all.setText("全不选");
        else all.setText("全选");
        calculateSum();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void calculateSum() {
        double priceSum = 0;
        for (int i = 0; i < BraecoWaiterData.refundMeals.size(); i++) {
            priceSum += Double.parseDouble("" + BraecoWaiterData.refundMeals.get(i).get("price")) * adapter.number.get(i);
        }
        sum.setText("退款总额：¥" + String.format("%.2f", priceSum));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.all:
                if ("全选".equals(all.getText().toString())) {
                    all.setText("全不选");
                    for (int i = 0; i < adapter.number.size(); i++)
                        adapter.number.set(i, (Integer) BraecoWaiterData.refundMeals.get(i).get("sum"));
                } else {
                    all.setText("全选");
                    for (int i = 0; i < adapter.number.size(); i++) adapter.number.set(i, 0);
                }
                adapter.notifyDataSetChanged();
                OnOrderListen();
                break;
            case R.id.make_sure:
                if ("".equals(remarkText.getText().toString())) {
                    BraecoWaiterUtils.showToast(mContext, "您尚未填写退款备注");
                    return;
                }
                boolean allZero = true;
                for (int i = 0; i < adapter.number.size(); i++) {
                    if (adapter.number.get(i) != 0) {
                        allZero = false;
                        break;
                    }
                }
                if (!allZero) refund();
                else BraecoWaiterUtils.showToast(mContext, "您尚未选择退款商品");
                break;
            case R.id.remark:
                addRemark();
                break;
        }
    }

    private String remarkString = "";
    private MaterialDialog inputDialog;
    private void addRemark() {
        String title = "填写退款备注";
        final int min = BraecoWaiterFinal.MIN_REFUNDED_REMARK;
        final int max = BraecoWaiterFinal.MAX_REFUNDED_REMARK;
        final String hint = "退款备注";
        final String fill = remarkString;
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
                            remarkString = materialDialog.getInputEditText().getText().toString();
                            remarkText.setText(remarkString);
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

    private class Refund extends AsyncTask<String, Void, String> {

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
                Log.d("BraecoWaiter", "Refund: " + result);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(new BasicNameValuePair("refund", params[1]));
            pairList.add(new BasicNameValuePair("description", params[2]));
            pairList.add(new BasicNameValuePair("password",  BraecoWaiterUtils.getInstance().MD5(params[3])));
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

            Log.d("BraecoWaiter", "Refund: " + result);

            progressDialog.dismiss();

            if (result != null) {
                JSONObject array;
                try {
                    array = new JSONObject(result);
                    if ("success".equals(array.getString("message"))) {
                        finishRefund(true, "退款成功", "退款成功");
                    } else if ("Order not found".equals(array.getString("message"))) {
                        finishRefund(false, "退款失败", "订单不存在");
                    } else if ("Invalid dish to refund".equals(array.getString("message"))) {
                        finishRefund(false, "退款失败", "含有不可退款的餐品");
                    } else if ("Need to upload cert of wx pay".equals(array.getString("message"))) {
                        finishRefund(false, "退款失败", "需要上传微信证书");
                    }
                } catch (JSONException e) {
                    finishRefund(false, "退款失败", "网络连接失败");
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Refund activity json error");
                    e.printStackTrace();
                }
            } else {
                finishRefund(false, "退款失败", "网络连接失败");
            }
        }
    }

    private String getRefundString() {
        try {
            JSONArray jsonMeals = new JSONArray();
            for (int i = 0; i < BraecoWaiterData.refundMeals.size(); i++) {
                if (adapter.number.get(i) != 0) {
                    JSONObject jsonMeal = new JSONObject();
                    jsonMeal.put("id", BraecoWaiterData.refundMeals.get(i).get("id"));
                    if (BraecoWaiterData.refundMeals.get(i).containsKey("isSet")
                            && (Boolean)BraecoWaiterData.refundMeals.get(i).get("isSet")) {
                        jsonMeal.put("property", BraecoWaiterData.refundMeals.get(i).get("refund_property"));
                    } else {
                        jsonMeal.put("property", getPropertyString((String[]) BraecoWaiterData.refundMeals.get(i).get("property")));
                    }
                    jsonMeal.put("sum", adapter.number.get(i));
                    jsonMeals.put(jsonMeals.length(), jsonMeal);
                }
            }
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Refund: " + jsonMeals.toString());
            return jsonMeals.toString();
        } catch (JSONException j) {
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Refund activity json error");
        }
        return "";
    }

    private JSONArray getPropertyString(String[] property) {
        JSONArray ja = new JSONArray();
        for (int i = 0; i < property.length; i++) {
            ja.put(property[i]);
        }
        return ja;
    }

    private void finishRefund(final boolean goBack, String title, String content) {
        progressDialog = new MaterialDialog.Builder(mContext)
                .title(title)
                .content(content)
                .cancelable(false)
                .positiveText("确认")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            if (goBack) {
                                BraecoWaiterData.JUST_REFRESH_RECORDS = true;
                                finish();
                            }
                        }
                    }
                })
                .show();
    }
}
