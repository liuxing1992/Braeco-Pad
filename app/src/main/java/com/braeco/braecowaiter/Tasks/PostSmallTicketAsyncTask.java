package com.braeco.braecowaiter.Tasks;

import android.os.AsyncTask;

import com.braeco.braecowaiter.BraecoWaiterApplication;
import com.braeco.braecowaiter.BraecoWaiterData;
import com.braeco.braecowaiter.BraecoWaiterUtils;
import com.braeco.braecowaiter.Interfaces.OnPostSmallTicketAsyncTaskListener;
import com.braeco.braecowaiter.Model.Waiter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by Weiping on 2016/5/12.
 */

public class PostSmallTicketAsyncTask extends AsyncTask<String, Void, JSONObject> {

    public static final String URL_POST_SMALL_TICKET = BraecoWaiterData.BRAECO_PREFIX + "/Dinner/Manage/Statistic/Print";

    private OnPostSmallTicketAsyncTaskListener mListener;

    private int year;
    private int month;
    private int day;
    private boolean isToday;

    public PostSmallTicketAsyncTask(OnPostSmallTicketAsyncTaskListener mListener, int year, int month, int day) {
        this.mListener = mListener;
        this.year = year;
        this.month = month;
        this.day = day;

        Calendar calendar = Calendar.getInstance();
        if (year == calendar.get(Calendar.YEAR) && month == calendar.get(Calendar.MONTH) + 1 && day == calendar.get(Calendar.DAY_OF_MONTH)) {
            isToday = true;
        }
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        HttpURLConnection httpURLConnection;
        URL url;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            url = new URL(URL_POST_SMALL_TICKET);
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.addRequestProperty("Cookie" , "sid=" + Waiter.getInstance().getSid());
            httpURLConnection.addRequestProperty("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.connect();

            List<NameValuePair> pairs = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, day, 0, 0, 0);
            calendar.add(Calendar.SECOND, 0);
            if (!isToday) pairs.add(new BasicNameValuePair("st", calendar.getTime().getTime() / 1000 + ""));

            DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());
            String paramsString = BraecoWaiterUtils.getParams(pairs);
            BraecoWaiterUtils.log("Small Ticket " + paramsString);
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
            if (byteArrayOutputStream != null) BraecoWaiterUtils.log("PostSmallTicketAsyncTask: " + byteArrayOutputStream.toString());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        BraecoWaiterUtils.log("PostSmallTicketAsyncTask: " + (result == null ? "Null" : result.toString()));
        if (result == null) {
            if (mListener != null) mListener.fail("打印小票失败（网络异常）");
        } else {
            String message;
            try {
                message = result.getString("message");
                if (BraecoWaiterUtils.STRING_401.equals(message)) {
                    if (mListener != null) mListener.signOut();
                } else if (!"success".equals(message)) {
                    if (mListener != null) mListener.fail("打印小票失败（网络异常）");
                } else {
                    // successful
                    if (mListener != null) mListener.success();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (mListener != null) mListener.fail("打印小票失败（网络异常）");
            }
        }
    }
}
