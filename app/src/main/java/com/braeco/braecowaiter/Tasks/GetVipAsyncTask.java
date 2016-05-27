package com.braeco.braecowaiter.Tasks;

import android.os.AsyncTask;

import com.braeco.braecowaiter.BraecoWaiterApplication;
import com.braeco.braecowaiter.BraecoWaiterData;
import com.braeco.braecowaiter.BraecoWaiterUtils;
import com.braeco.braecowaiter.Interfaces.OnGetVipAsyncTaskListener;
import com.braeco.braecowaiter.Model.Vip;
import com.braeco.braecowaiter.Model.Waiter;

import org.json.JSONArray;
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
public class GetVipAsyncTask extends AsyncTask<String, Void, JSONObject> {

    public static final String URL_GET_VIP =  BraecoWaiterData.BRAECO_PREFIX + "/Dinner/Manage/Membership";

    public static final int VIP_PER_PAGE = 10;

    public static int TASK_ID = 0;

    private OnGetVipAsyncTaskListener mListener;

    private int page;
    private int taskId;

    public GetVipAsyncTask(OnGetVipAsyncTaskListener mListener, int page, int taskId) {
        this.mListener = mListener;
        this.page = page - 1;
        this.taskId = taskId;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        HttpURLConnection httpURLConnection;
        URL url;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            url = new URL(URL_GET_VIP);
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.addRequestProperty("Cookie" , "sid=" + Waiter.getInstance().getSid());
            httpURLConnection.addRequestProperty("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.connect();

            List<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("pn", params[0]));
            pairs.add(new BasicNameValuePair("pc", params[1]));
            pairs.add(new BasicNameValuePair("by", params[2]));
            pairs.add(new BasicNameValuePair("in", params[3]));
            if (params[4] != null) pairs.add(new BasicNameValuePair("search", params[4]));

            DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());
            String paramsString = BraecoWaiterUtils.getParams(pairs);
            BraecoWaiterUtils.log("Vip " + paramsString);
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
            if (byteArrayOutputStream != null) BraecoWaiterUtils.log("GetVipAsyncTask: " + byteArrayOutputStream.toString());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        if (taskId != TASK_ID) return;

        BraecoWaiterUtils.log("GetVipAsyncTask: " + (result == null ? "Null" : result.toString()));
        if (result == null) {
            if (mListener != null) mListener.fail("网络连接失败");
        } else {
            String message = null;
            try {
                message = result.getString("message");
                if (BraecoWaiterUtils.STRING_401.equals(message)) {
                    if (mListener != null) mListener.signOut();
                } else if (!"success".equals(message)) {
                    if (mListener != null) mListener.fail("网络连接失败");
                } else {
                    // successful
                    BraecoWaiterApplication.maxVips = Integer.parseInt(result.getString("sum"));

                    JSONArray jsonVips = result.getJSONArray("membership");
                    for (int i = 0; i < jsonVips.length(); i++) {
                        JSONObject jsonVip = jsonVips.getJSONObject(i);
                        Vip vip = new Vip(
                                jsonVip.getInt("id"),
                                jsonVip.isNull("phone") ? null : jsonVip.getString("phone"),
                                jsonVip.isNull("nick") ? null : jsonVip.getString("nick"),
                                jsonVip.getString("level"),
                                jsonVip.getInt("EXP"),
                                jsonVip.getDouble("balance"),
                                jsonVip.getLong("date"),
                                jsonVip.getInt("id_of_dinner"));
                        if (BraecoWaiterApplication.vips.size() < BraecoWaiterApplication.maxVips) {
                            if (BraecoWaiterApplication.vips.size() > page * VIP_PER_PAGE + i) {
                                // another page has arrived the position
                                BraecoWaiterApplication.vips.add(page * VIP_PER_PAGE + i, vip);
                            } else {
                                BraecoWaiterApplication.vips.add(vip);
                            }
                        }
                    }

                    mListener.success();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (mListener != null) mListener.fail("网络连接失败");
            }
        }
    }
}
