package com.braeco.braecowaiter.Tasks;

import android.os.AsyncTask;

import com.braeco.braecowaiter.BraecoWaiterApplication;
import com.braeco.braecowaiter.BraecoWaiterData;
import com.braeco.braecowaiter.BraecoWaiterUtils;
import com.braeco.braecowaiter.Interfaces.OnGet401AsyncTaskListener;
import com.braeco.braecowaiter.Model.Waiter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Weiping on 2016/5/20.
 */

public class Get401AsyncTask extends AsyncTask<String, Void, JSONObject> {

    public static final String URL_GET_401 = BraecoWaiterData.BRAECO_PREFIX + "/test/ex1";

    private OnGet401AsyncTaskListener mListener;

    public Get401AsyncTask(OnGet401AsyncTaskListener mListener) {
        this.mListener = mListener;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        HttpURLConnection httpURLConnection;
        URL url;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            url = new URL(URL_GET_401);
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.addRequestProperty("Cookie" , "sid=" + Waiter.getInstance().getSid());
            httpURLConnection.addRequestProperty("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                BraecoWaiterUtils.updateSid(httpURLConnection.getHeaderFields());

                InputStream in = httpURLConnection.getInputStream();
                byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while((len = in.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer, 0, len);
                }
//                return new JSONObject(byteArrayOutputStream.toString());
                return BraecoWaiterUtils.get401Json();
            } else if (httpURLConnection.getResponseCode() == 401) {
                return BraecoWaiterUtils.get401Json();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (byteArrayOutputStream != null) BraecoWaiterUtils.log("GetActivityAsyncTask: " + byteArrayOutputStream.toString());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        BraecoWaiterUtils.log("Get401AsyncTask: " + (result == null ? "Null" : result.toString()));
        if (result == null) {
            if (mListener != null) mListener.fail("获取401失败（网络异常）");
        } else {
            String message;
            try {
                message = result.getString("message");
                if (BraecoWaiterUtils.STRING_401.equals(message)) {
                    if (mListener != null) mListener.signOut();
                } else if (!"success".equals(message)) {
                    if (mListener != null) mListener.fail("获取401失败（网络异常）");
                } else {
                    // successful
                    if (mListener != null) mListener.success();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (mListener != null) mListener.fail("获取401失败（网络异常）");
            }
        }
    }
}
