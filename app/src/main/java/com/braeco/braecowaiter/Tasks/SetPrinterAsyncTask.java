package com.braeco.braecowaiter.Tasks;

import android.os.AsyncTask;

import com.braeco.braecowaiter.BraecoWaiterApplication;
import com.braeco.braecowaiter.BraecoWaiterData;
import com.braeco.braecowaiter.BraecoWaiterUtils;
import com.braeco.braecowaiter.Interfaces.OnSetPrinterAsyncTaskListener;
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

public class SetPrinterAsyncTask extends AsyncTask<String, Void, JSONObject> {

    public static final String URL_SET_PRINTER = BraecoWaiterData.BRAECO_PREFIX + "/Dinner/Printer/Update/";

    private OnSetPrinterAsyncTaskListener mListener;

    public SetPrinterAsyncTask(OnSetPrinterAsyncTaskListener mListener) {
        this.mListener = mListener;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        HttpURLConnection httpURLConnection;
        URL url;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            url = new URL(URL_SET_PRINTER + BraecoWaiterApplication.modifyingPrinter.getId());
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

            JSONObject printer = new JSONObject();
            printer.put("page", BraecoWaiterApplication.modifyingPrinter.getPage());
            printer.put("ban", new JSONArray(BraecoWaiterApplication.modifyingPrinter.getBan()));
            printer.put("ban_cat", new JSONArray(BraecoWaiterApplication.modifyingPrinter.getBanCategory()));
            printer.put("ban_table", new JSONArray(BraecoWaiterApplication.modifyingPrinter.getBanTable()));
            printer.put("separate", BraecoWaiterApplication.modifyingPrinter.isSeparate());
            printer.put("remark", BraecoWaiterApplication.modifyingPrinter.getRemark());
            printer.put("name", BraecoWaiterApplication.modifyingPrinter.getName());
            if (BraecoWaiterApplication.modifyingPrinter.getWidth() != 0) {
                printer.put("width", BraecoWaiterApplication.modifyingPrinter.getWidth());
                printer.put("offset", BraecoWaiterApplication.modifyingPrinter.getOffset());
                printer.put("size", BraecoWaiterApplication.modifyingPrinter.getSize());
            }
            DataOutputStream out = new DataOutputStream(httpURLConnection.getOutputStream());
            BraecoWaiterUtils.log("Printer " + printer.toString());
            out.write(printer.toString().getBytes("UTF-8"));
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
            if (byteArrayOutputStream != null) BraecoWaiterUtils.log("SetPrinterAsyncTask: " + byteArrayOutputStream.toString());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        BraecoWaiterUtils.log("SetPrinterAsyncTask: " + (result == null ? "Null" : result.toString()));
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
                        mListener.success();
                        break;
                    case "Dish not found":
                        mListener.fail("餐品不存在");
                        break;
                    case "Table not found":
                        mListener.fail("桌位不存在");
                        break;
                    case "Invalid page":
                        mListener.fail("打印联数非法");
                        break;
                    case "Invalid separate":
                        mListener.fail("打印方式非法");
                        break;
                    case "Invalid remark":
                        mListener.fail("备注非法");
                        break;
                    case "Invalid name":
                        mListener.fail("名字非法");
                        break;
                    case "Invalid width":
                        mListener.fail("纸张宽度非法");
                        break;
                    case "Invalid offset":
                        mListener.fail("偏移量非法");
                        break;
                    case "Invalid size":
                        mListener.fail("字号大小非法");
                        break;
                    default:
                        mListener.fail("网络异常");
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (mListener != null) mListener.fail("网络异常");
            }
        }
    }
}
