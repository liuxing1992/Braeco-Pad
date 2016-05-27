package com.braeco.braecowaiter.Tasks;

import android.os.AsyncTask;

import com.braeco.braecowaiter.BraecoWaiterApplication;
import com.braeco.braecowaiter.BraecoWaiterData;
import com.braeco.braecowaiter.BraecoWaiterUtils;
import com.braeco.braecowaiter.Interfaces.OnGetTableAsyncTaskListener;
import com.braeco.braecowaiter.Model.Table;
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

/**
 * Created by Weiping on 2016/5/12.
 */
public class GetTableAsyncTask extends AsyncTask<String, Void, JSONObject> {

    public static final String URL_GET_TABLE =  BraecoWaiterData.BRAECO_PREFIX + "/Table/Get";

    private OnGetTableAsyncTaskListener mListener;

    public GetTableAsyncTask(OnGetTableAsyncTaskListener mListener) {
        this.mListener = mListener;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        HttpURLConnection httpURLConnection;
        URL url;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            url = new URL(URL_GET_TABLE);
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
            if (byteArrayOutputStream != null) BraecoWaiterUtils.log("GetTableAsyncTask: " + byteArrayOutputStream.toString());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        BraecoWaiterUtils.log("GetTableAsyncTask: " + (result == null ? "Null" : result.toString()));
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
                    ArrayList<Table> newTables = new ArrayList<>();
                    JSONArray tables = result.getJSONArray("table");
                    for (int i = 0; i < tables.length(); i++) {
                        // Notice that if the table is existed,
                        // we should not change the status of the table.
                        if ("外带".equals(tables.getString(i))) continue;
                        String id = tables.getString(i);
                        boolean existed = false;
                        int position = -1;
                        for (int j = 0; BraecoWaiterApplication.tables != null && j < BraecoWaiterApplication.tables.size(); j++) {
                            Table table = BraecoWaiterApplication.tables.get(j);
                            if (table.getId() != null && table.getId().equals(id)) {
                                existed = true;
                                position = j;
                                break;
                            }
                        }
                        if (existed) {
                            newTables.add(new Table(id, BraecoWaiterApplication.tables.get(position).isUsed()));
                        } else {
                            newTables.add(new Table(id, false));
                        }
                    }
                    BraecoWaiterApplication.tables = newTables;
                    BraecoWaiterUtils.sortTables();
                    mListener.success();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (mListener != null) mListener.fail("网络连接失败");
            }
        }
    }
}
