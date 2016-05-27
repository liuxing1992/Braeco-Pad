package com.braeco.braecowaiter.Tasks;

import android.os.AsyncTask;

import com.braeco.braecowaiter.BraecoWaiterApplication;
import com.braeco.braecowaiter.BraecoWaiterData;
import com.braeco.braecowaiter.BraecoWaiterUtils;
import com.braeco.braecowaiter.Enums.ActivityType;
import com.braeco.braecowaiter.Interfaces.OnGetActivityAsyncTaskListener;
import com.braeco.braecowaiter.Model.Activity;
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

public class GetActivityAsyncTask extends AsyncTask<String, Void, JSONObject> {

    public static final String URL_GET_ACTIVITY = BraecoWaiterData.BRAECO_PREFIX + "/Activity/Get";

    public static int TASK_ID = 0;

    private OnGetActivityAsyncTaskListener mListener;

    private int task;

    public GetActivityAsyncTask(OnGetActivityAsyncTaskListener mListener, int task) {
        this.mListener = mListener;
        this.task = task;
        TASK_ID = task;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        HttpURLConnection httpURLConnection;
        URL url;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            url = new URL(URL_GET_ACTIVITY);
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
            if (byteArrayOutputStream != null) BraecoWaiterUtils.log("GetActivityAsyncTask: " + byteArrayOutputStream.toString());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        if (task != TASK_ID) return;

        BraecoWaiterUtils.log("GetActivityAsyncTask: " + (result == null ? "Null" : result.toString()));
        if (result == null) {
            if (mListener != null) mListener.fail("获取活动信息失败（网络异常）");
        } else {
            String message;
            try {
                message = result.getString("message");
                if (BraecoWaiterUtils.STRING_401.equals(message)) {
                    if (mListener != null) mListener.signOut();
                } else if (!"success".equals(message)) {
                    if (mListener != null) mListener.fail("获取活动信息失败（网络异常）");
                } else {
                    // successful
                    JSONArray jsonActivities = result.getJSONArray("activities");
                    ArrayList<Activity> activities = new ArrayList<>();

                    for (int i = 0; i < jsonActivities.length(); i++) {
                        JSONObject jsonActivity = jsonActivities.getJSONObject(i);
                        Activity newActivity = new Activity(
                                jsonActivity.getInt("id"),
                                jsonActivity.getString("title"),
                                jsonActivity.getString("intro"),
                                jsonActivity.getString("content"),
                                jsonActivity.getString("pic"),
                                jsonActivity.getString("date_begin"),
                                jsonActivity.getString("date_end"),
                                jsonActivity.getBoolean("is_valid"),
                                getType(jsonActivity.getString("type"))
                        );
                        newActivity.setDetails(new ArrayList<>());

                        switch (newActivity.getType()) {
                            case REDUCE:
                                JSONArray detailsJson = jsonActivity.getJSONArray("detail");
                                for (int j = 0; j < detailsJson.length(); j++) {
                                    newActivity.getDetails().add(detailsJson.getJSONObject(j).getInt("least"));
                                    newActivity.getDetails().add(detailsJson.getJSONObject(j).getInt("reduce"));
                                }
                                break;
                            case GIVE:
                                JSONArray givesJson = jsonActivity.getJSONArray("detail");
                                for (int j = 0; j < givesJson.length(); j++) {
                                    newActivity.getDetails().add(givesJson.getJSONObject(j).getInt("least"));
                                    newActivity.getDetails().add(givesJson.getJSONObject(j).getString("dish"));
                                }
                                break;
                            case OTHER:
                                newActivity.getDetails().add(jsonActivity.getString("detail"));
                                break;
                        }

                        activities.add(newActivity);
                    }

                    BraecoWaiterData.activities = activities;
                    BraecoWaiterUtils.sortActivities();
                    if (mListener != null) mListener.success();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (mListener != null) mListener.fail("获取活动信息失败（网络异常）");
            }
        }
    }

    private ActivityType getType(String string) {
        switch (string) {
            case "theme": return ActivityType.THEME;
            case "reduce": return ActivityType.REDUCE;
            case "give": return ActivityType.GIVE;
            case "other": return ActivityType.OTHER;
        }
        return ActivityType.NONE;
    }
}
