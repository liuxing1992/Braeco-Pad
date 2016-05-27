package com.braeco.braecowaiter.Tasks;

import android.os.AsyncTask;

import com.braeco.braecowaiter.BraecoWaiterApplication;
import com.braeco.braecowaiter.BraecoWaiterData;
import com.braeco.braecowaiter.BraecoWaiterUtils;
import com.braeco.braecowaiter.Interfaces.OnSetDishUpdateCategoryAsyncTaskListener;
import com.braeco.braecowaiter.Model.Waiter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Weiping on 2016/5/12.
 */

public class SetDishUpdateCategoryAsyncTask extends AsyncTask<String, Void, JSONObject> {

    public static final String URL_SET_DISH_UPDATE_CATEGORY = BraecoWaiterData.BRAECO_PREFIX + "/Dish/Update/Category";

    private OnSetDishUpdateCategoryAsyncTaskListener mListener;
    private String mData;

    public SetDishUpdateCategoryAsyncTask(OnSetDishUpdateCategoryAsyncTaskListener mListener, String mData) {
        this.mListener = mListener;
        this.mData = mData;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        HttpURLConnection httpURLConnection;
        URL url;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            url = new URL(URL_SET_DISH_UPDATE_CATEGORY);
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setDoInput (true);
            httpURLConnection.setDoOutput (true);
            httpURLConnection.setUseCaches (false);
            httpURLConnection.setRequestProperty("Cookie" , "sid=" + Waiter.getInstance().getSid());
            httpURLConnection.setRequestProperty("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.connect();

            DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());
            BraecoWaiterUtils.log("Update Category " + mData);
            out.write(mData.getBytes("UTF-8"));
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
            if (byteArrayOutputStream != null) BraecoWaiterUtils.log("SetDishUpdateCategoryAsyncTask: " + byteArrayOutputStream.toString());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        BraecoWaiterUtils.log("SetDishUpdateCategoryAsyncTask: " + (result == null ? "Null" : result.toString()));
        if (result == null) {
            if (mListener != null) mListener.fail("网络异常");
        } else {
            String message = null;
            try {
                message = result.getString("message");
                if (BraecoWaiterUtils.STRING_401.equals(message)) {
                    if (mListener != null) mListener.signOut();
                } else if ("success".equals(message)) {
                    if (mListener != null) mListener.success();
                } else {
                    if (mListener != null) mListener.fail(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (mListener != null) mListener.fail("网络异常");
            }
        }
    }
}
