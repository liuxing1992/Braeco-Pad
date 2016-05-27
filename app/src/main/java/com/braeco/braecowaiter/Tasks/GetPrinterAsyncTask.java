package com.braeco.braecowaiter.Tasks;

import android.os.AsyncTask;

import com.braeco.braecowaiter.BraecoWaiterApplication;
import com.braeco.braecowaiter.BraecoWaiterData;
import com.braeco.braecowaiter.BraecoWaiterUtils;
import com.braeco.braecowaiter.Interfaces.OnGetPrinterAsyncTaskListener;
import com.braeco.braecowaiter.Model.Printer;
import com.braeco.braecowaiter.Model.Waiter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Weiping on 2016/5/12.
 */
public class GetPrinterAsyncTask extends AsyncTask<String, Void, JSONObject> {

    public static final String URL_GET_PRINTER = BraecoWaiterData.BRAECO_PREFIX + "/Dinner/Printer/Get";

    private OnGetPrinterAsyncTaskListener mListener;

    public GetPrinterAsyncTask(OnGetPrinterAsyncTaskListener mListener) {
        this.mListener = mListener;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        HttpURLConnection httpURLConnection;
        URL url;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            url = new URL(URL_GET_PRINTER);
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
                return new JSONObject(byteArrayOutputStream.toString());
            } else if (httpURLConnection.getResponseCode() == 401) {
                return BraecoWaiterUtils.get401Json();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            if (byteArrayOutputStream != null) BraecoWaiterUtils.log("GetPrinterAsyncTask: " + byteArrayOutputStream.toString());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        BraecoWaiterUtils.log("GetPrinterAsyncTask: " + (result == null ? "Null" : result.toString()));
        if (result == null) {
            if (mListener != null) mListener.fail();
        } else {
            String message = null;
            try {
                message = result.getString("message");
                if (BraecoWaiterUtils.STRING_401.equals(message)) {
                    if (mListener != null) mListener.signOut();
                } else if (!"success".equals(message)) {
                    if (mListener != null) mListener.fail();
                } else {
                    // successful
                    JSONArray printersJSON = result.getJSONArray("printer");
                    BraecoWaiterApplication.printers = new ArrayList<>();
                    for (int i = 0; i < printersJSON.length(); i++) {
                        JSONObject printerJSON = printersJSON.getJSONObject(i);
                        if (printerJSON.getInt("width") == 0) {
                            // fly goose
                            BraecoWaiterApplication.printers.add(new Printer(
                                    printerJSON.getInt("id"),
                                    printerJSON.getInt("width"),
                                    printerJSON.getInt("page"),
                                    printerJSON.getString("name"),
                                    printerJSON.isNull("remark") ? null : printerJSON.getString("remark"),
                                    getSetForInteger(printerJSON.getJSONArray("ban")),
                                    getSetForInteger(printerJSON.getJSONArray("ban_cat")),
                                    getSetForString(printerJSON.getJSONArray("ban_table")),
                                    printerJSON.getBoolean("separate")
                            ));
                        } else {
                            BraecoWaiterApplication.printers.add(new Printer(
                                    printerJSON.getInt("id"),
                                    printerJSON.getInt("width"),
                                    printerJSON.getInt("size"),
                                    printerJSON.getInt("page"),
                                    printerJSON.getString("name"),
                                    printerJSON.isNull("remark") ? null : printerJSON.getString("remark"),
                                    printerJSON.getInt("offset"),
                                    getSetForInteger(printerJSON.getJSONArray("ban")),
                                    getSetForInteger(printerJSON.getJSONArray("ban_cat")),
                                    getSetForString(printerJSON.getJSONArray("ban_table")),
                                    printerJSON.getBoolean("separate")
                            ));
                        }
                    }
                    mListener.success();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (mListener != null) mListener.fail();
            }
        }
    }

    private HashSet<Integer> getSetForInteger(JSONArray jsonArray) throws JSONException {
        HashSet<Integer> integers = new HashSet<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            integers.add(jsonArray.getInt(i));
        }
        return integers;
    }

    private HashSet<String> getSetForString(JSONArray jsonArray) throws JSONException {
        HashSet<String> strings = new HashSet<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            strings.add(jsonArray.getString(i));
        }
        return strings;
    }
}
