package com.braeco.braecowaiter.Tasks;

import android.os.AsyncTask;

import com.braeco.braecowaiter.BraecoWaiterApplication;
import com.braeco.braecowaiter.BraecoWaiterData;
import com.braeco.braecowaiter.BraecoWaiterUtils;
import com.braeco.braecowaiter.Enums.PayType;
import com.braeco.braecowaiter.Interfaces.OnSetOrderAsyncTaskListener;
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

/**
 * Created by Weiping on 2016/5/12.
 */

public class SetOrderAsyncTask extends AsyncTask<String, Void, JSONObject> {

    public static final String URL_SET_ORDER = BraecoWaiterData.BRAECO_PREFIX + "/order/add";

    private OnSetOrderAsyncTaskListener mListener;
    private String mContent;
    private String mTable;
    private PayType mPayType;
    private String mPayContent;

    public SetOrderAsyncTask(OnSetOrderAsyncTaskListener mListener, String mContent, String mTable, PayType mPayType, String mPayContent) {
        this.mListener = mListener;
        this.mContent = mContent;
        this.mTable = mTable;
        this.mPayType = mPayType;
        this.mPayContent = mPayContent;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        HttpURLConnection httpURLConnection;
        URL url;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            url = new URL(URL_SET_ORDER);
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

            BraecoWaiterUtils.log("Order " + mContent);

            JSONObject order = new JSONObject();
            order.put("contents", new JSONArray(mContent));
            order.put("table", mTable);
            switch (mPayType) {
                case CASH:
                    break;
                case ALIPAY:
                    break;
                case WECHAT:
                    order.put("use_wx_qr", 1);
                    break;
                case BALANCE:
                    order.put("use_membership_card", mPayContent);
                    break;
            }


            DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());
            BraecoWaiterUtils.log("Order " + order.toString());
            out.write(order.toString().getBytes("UTF-8"));
            out.flush();
            out.close();

            BraecoWaiterUtils.log(httpURLConnection.getResponseCode() + "");

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
            if (byteArrayOutputStream != null) BraecoWaiterUtils.log("SetOrderAsyncTask: " + byteArrayOutputStream.toString());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        BraecoWaiterUtils.log("SetOrderAsyncTask: " + (result == null ? "Null" : result.toString()));
        if (result == null) {
            if (mListener != null) mListener.fail("网络异常");
        } else {
            String message = null;
            try {
                message = result.getString("message");
                switch (message) {
                    case BraecoWaiterUtils.STRING_401:
                        mListener.signOut();
                        break;
                    case "success":
                        mListener.success((result.has("qr_code") ? result.getString("qr_code") : null), mPayType);
                        break;
                    case "Invalid combo":
                        mListener.fail("套餐格式不合法");
                        break;
                    case "The money given does not match database":
                        mListener.fail("价格与服务器不符合，请重新下单");
                        break;
                    case "Table not found":
                        if ("tkot".equals(mTable)) mListener.fail("外带桌位不存在，请重新下单");
                        else mListener.fail("桌位不存在，请重新下单");
                        break;
                    case "Dinner not online":
                        mListener.fail("餐厅端未开启，请开启后重新下单");
                        break;
                    case "Dish not found":
                        mListener.fail("其中某些餐品不存在，请重新下单");
                        break;
                    case "Dish disabled":
                        mListener.fail("其中某些餐品暂时无法提供，请重新下单");
                        break;
                    case "Version too old":
                        mListener.fail("版本过旧，请更新后下单");
                        break;
                    case "Some dish is beyond limit":
                        mListener.fail("其中某些限量供应的餐品售罄，正在刷新餐品");
                        break;
                    case "Membership card not exists":
                        mListener.fail("会员不存在，请确认会员信息");
                        break;
                    case "Not enough money":
                        mListener.fail("会员余额不足");
                        break;
                    default:
                        mListener.fail("网络异常，请重新下单");
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (mListener != null) mListener.fail("网络异常");
            }
        }
    }
}
