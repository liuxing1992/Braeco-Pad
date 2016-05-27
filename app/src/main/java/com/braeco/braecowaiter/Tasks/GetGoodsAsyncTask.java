package com.braeco.braecowaiter.Tasks;

import android.os.AsyncTask;

import com.braeco.braecowaiter.BraecoWaiterApplication;
import com.braeco.braecowaiter.BraecoWaiterData;
import com.braeco.braecowaiter.BraecoWaiterUtils;
import com.braeco.braecowaiter.Interfaces.OnGetGoodsAsyncTaskListener;
import com.braeco.braecowaiter.Model.Waiter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by Weiping on 2016/5/12.
 */

public class GetGoodsAsyncTask extends AsyncTask<String, Void, JSONObject> {

    public static final String URL_GET_GOODS = BraecoWaiterData.BRAECO_PREFIX + "/Dinner/Manage/Statistic/rank";

    public static int TASK_ID = 0;

    private OnGetGoodsAsyncTaskListener mListener;

    private int task;

    private int year;
    private int month;
    private int day;
    private int week;
    private int sum;

    public GetGoodsAsyncTask(OnGetGoodsAsyncTaskListener mListener, int task, int year, int month, int day, int week, int sum) {
        this.mListener = mListener;
        this.task = task;
        this.year = year;
        this.month = month;
        this.day = day;
        this.week = week;
        this.sum = sum;
        TASK_ID = task;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        HttpURLConnection httpURLConnection;
        URL url;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            url = new URL(URL_GET_GOODS);
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.addRequestProperty("Cookie" , "sid=" + Waiter.getInstance().getSid());
            httpURLConnection.addRequestProperty("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.connect();

            List<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("year", year + ""));
            pairs.add(new BasicNameValuePair("month", month + ""));
            pairs.add(new BasicNameValuePair("day", day + ""));
            if (week != -1) pairs.add(new BasicNameValuePair("week", week + ""));
            pairs.add(new BasicNameValuePair("sum", sum + ""));

            DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());
            String paramsString = BraecoWaiterUtils.getParams(pairs);
            BraecoWaiterUtils.log("Goods " + paramsString);
            out.write(paramsString.getBytes("UTF-8"));
            out.flush();
            out.close();

            if (httpURLConnection.getResponseCode() == 200) {
                BraecoWaiterUtils.updateSid(httpURLConnection.getHeaderFields());

                InputStream in = httpURLConnection.getInputStream();
                byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while((len = in.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer, 0, len);
                }
                return new JSONObject(byteArrayOutputStream.toString());
            } else if (httpURLConnection.getResponseCode() == 401) {
                return BraecoWaiterUtils.get401Json();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            if (byteArrayOutputStream != null) BraecoWaiterUtils.log("GetGoodsAsyncTask: " + byteArrayOutputStream.toString());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        if (task != TASK_ID) return;

        BraecoWaiterUtils.log("GetGoodsAsyncTask: " + (result == null ? "Null" : result.toString()));
        if (result == null) {
            if (mListener != null) mListener.fail("获取餐品信息失败（网络异常）");
        } else {
            String message;
            try {
                message = result.getString("message");
                if (BraecoWaiterUtils.STRING_401.equals(message)) {
                    if (mListener != null) mListener.signOut();
                } else if (!"success".equals(message)) {
                    if (mListener != null) mListener.fail("获取餐品信息失败（网络异常）");
                } else {
                    // successful
                    if (mListener != null) mListener.success(result);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (mListener != null) mListener.fail("获取餐品信息失败（网络异常）");
            }
        }
    }
}
