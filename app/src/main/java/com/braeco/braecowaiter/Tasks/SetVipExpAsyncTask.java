package com.braeco.braecowaiter.Tasks;

import android.os.AsyncTask;

import com.braeco.braecowaiter.BraecoWaiterApplication;
import com.braeco.braecowaiter.BraecoWaiterData;
import com.braeco.braecowaiter.BraecoWaiterUtils;
import com.braeco.braecowaiter.Interfaces.OnSetVipExpAsyncTaskListener;
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

public class SetVipExpAsyncTask extends AsyncTask<String, Void, JSONObject> {

    public static final String URL_SET_VIP_EXP = BraecoWaiterData.BRAECO_PREFIX + "/Membership/Card/Setexp/";

    private OnSetVipExpAsyncTaskListener mListener;
    private int mId;
    private int mExp;

    public SetVipExpAsyncTask(OnSetVipExpAsyncTaskListener mListener, int id, int amount) {
        this.mListener = mListener;
        this.mId = id;
        this.mExp = amount;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        HttpURLConnection httpURLConnection;
        URL url;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            url = new URL(URL_SET_VIP_EXP + mId);
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setDoInput (true);
            httpURLConnection.setDoOutput (true);
            httpURLConnection.setUseCaches (false);
            httpURLConnection.setRequestProperty("Cookie" , "sid=" + Waiter.getInstance().getSid());
            httpURLConnection.setRequestProperty("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.connect();

            List<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("exp", mExp + ""));

            DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());
            String paramsString = BraecoWaiterUtils.getParams(pairs);
            BraecoWaiterUtils.log("Vip Exp " + paramsString);
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
            if (byteArrayOutputStream != null) BraecoWaiterUtils.log("SetVipExpAsyncTask: " + byteArrayOutputStream.toString());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        BraecoWaiterUtils.log("SetVipExpAsyncTask: " + (result == null ? "Null" : result.toString()));
        if (result == null) {
            if (mListener != null) mListener.fail("网络异常");
        } else {
            String message;
            try {
                message = result.getString("message");
                if (BraecoWaiterUtils.STRING_401.equals(message)) {
                    if (mListener != null) mListener.signOut();
                } else if ("success".equals(message)) {
                    if (mListener != null) mListener.success();
                } else {
                    switch (message) {
                        case "User not found":
                            if (mListener != null) mListener.fail("用户不存在，请确认用户信息");
                            break;
                        case "Membership card not exists":
                            if (mListener != null) mListener.fail("用户不存在或不是当前餐厅会员，请确认会员信息");
                            break;
                        default:
                            if (mListener != null) mListener.fail("网络异常");
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (mListener != null) mListener.fail("网络异常");
            }
        }
    }
}
