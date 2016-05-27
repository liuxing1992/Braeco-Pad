package com.braeco.braecowaiter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.Authority;
import com.braeco.braecowaiter.Model.AuthorityManager;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Weiping on 2015/12/1.
 */

public class ServiceRecordFragment extends Fragment
        implements
        SwipeRefreshLayout.OnRefreshListener,
        OnMoreListener,
        ServiceRecordFragmentRecyclerViewAdapter.OnRefundListener {

    private static final int REFUND = 0;

    private Activity activity;

    private SuperRecyclerView superRecyclerView;
    private ServiceRecordFragmentRecyclerViewAdapter adapter;

    private boolean isRefreshing = true;
    private boolean reset = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            activity = (Activity)context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "ServiceRecordFragment onCreateView");
        View messageLayout = inflater.inflate(R.layout.fragment_service_record, container, false);

        superRecyclerView = (SuperRecyclerView)messageLayout.findViewById(R.id.recyclerview);
        superRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        superRecyclerView.setRefreshListener(this);
        superRecyclerView.setRefreshingColorResources(R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary);
        superRecyclerView.setupMoreListener(this, Integer.MAX_VALUE);
        adapter = new ServiceRecordFragmentRecyclerViewAdapter(this);
        superRecyclerView.setAdapter(adapter);

        if (AuthorityManager.ableTo(Authority.VIEW_RECORD)) getRecords();
        else AuthorityManager.showDialog(getContext(), "查看流水订单");

        return messageLayout;
    }

    @Override
    public void onResume() {
        if (BraecoWaiterData.JUST_REFRESH_RECORDS) {
            BraecoWaiterData.JUST_REFRESH_RECORDS = false;
            BraecoWaiterUtils.showToast(getActivity(), "您刚刚进行了退款操作，正在刷新流水订单，请稍候");
            onRefresh();
        }
        super.onResume();
    }

    @Override
    public void onRefresh() {
        if (!AuthorityManager.ableTo(Authority.VIEW_RECORD)) {
            MaterialDialog dialog = AuthorityManager.showDialog(getContext(), "查看流水订单");
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    superRecyclerView.getSwipeToRefresh().setRefreshing(false);
                }
            });
            return;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Todo
                if (superRecyclerView != null)
                    superRecyclerView.getSwipeToRefresh().setRefreshing(false);
            }
        }, 3000);

        if (isRefreshing) {
            return;
        }
        isRefreshing = true;

        reset = true;
        BraecoWaiterApplication.currentTodayRecordsPage = 1;

        Calendar todaySt = Calendar.getInstance();
        todaySt.set(Calendar.HOUR_OF_DAY, 0);
        todaySt.set(Calendar.MINUTE, 0);
        todaySt.set(Calendar.SECOND, 0);
        todaySt.add(Calendar.SECOND, 0);
        Calendar todayEd = Calendar.getInstance();

        new GetRecords()
                .execute("http://brae.co/Dinner/Manage/Orders",
                        (todaySt.getTime().getTime() / 1000) + "",
                        (todayEd.getTime().getTime() / 1000) + "",
                        BraecoWaiterApplication.currentTodayRecordsPage + "",
                        10 + "");
    }

    @Override
    public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        if (!AuthorityManager.ableTo(Authority.VIEW_RECORD)) {
            superRecyclerView.hideMoreProgress();
            return;
        }
        if (BraecoWaiterApplication.todayRecords.size() == BraecoWaiterApplication.todayRecordsNum) {
            superRecyclerView.hideMoreProgress();
            return;
        }
        getRecords();
    }

    private void getRecords() {
        Calendar todaySt = Calendar.getInstance();
        todaySt.set(Calendar.HOUR_OF_DAY, 0);
        todaySt.set(Calendar.MINUTE, 0);
        todaySt.set(Calendar.SECOND, 0);
        todaySt.add(Calendar.SECOND, 0);
        Calendar todayEd = Calendar.getInstance();
        new GetRecords()
                .execute("http://brae.co/Dinner/Manage/Orders",
                        (todaySt.getTime().getTime() / 1000) + "",
                        (todayEd.getTime().getTime() / 1000) + "",
                        BraecoWaiterApplication.currentTodayRecordsPage + "",
                        10 + "");
    }

    @Override
    public void onRefund(int p) {
        if (!AuthorityManager.ableTo(Authority.REFUND)) {
            AuthorityManager.showDialog(getContext(), "为客人退款");
            return;
        }
        BraecoWaiterData.refundMeals = new ArrayList<>();
        BraecoWaiterData.unRefundMeals = new ArrayList<>();
        Map<String, Object> record = BraecoWaiterApplication.todayRecords.get(p);
        ArrayList<Map<String, Object>> meals = (ArrayList<Map<String,Object>>) record.get("content");
        BraecoWaiterData.refundId = (Integer)record.get("id");
        for (int i = 0; i < meals.size(); i++) {
            if ((Boolean)meals.get(i).get("ableRefund")) {
                BraecoWaiterData.refundMeals.add(meals.get(i));
            } else {
                BraecoWaiterData.unRefundMeals.add(meals.get(i));
            }
        }
        startActivity(new Intent(getActivity(), SingleRefundActivity.class));
    }

    private class GetRecords extends AsyncTask<String, Void, String> {
        protected String showResponseResult(HttpResponse response) {
            if (null == response) return null;

            HttpEntity httpEntity = response.getEntity();

            try {
                InputStream inputStream = httpEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String result = "";
                String line = "";
                while (null != (line = reader.readLine())) {
                    result += line;
                }
                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "getRecord result: " + result);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(new BasicNameValuePair("st", params[1]));
            pairList.add(new BasicNameValuePair("en", params[2]));
            pairList.add(new BasicNameValuePair("pn", params[3]));
            pairList.add(new BasicNameValuePair("pc", params[4]));
            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(
                        pairList);
                HttpPost httpPost = new HttpPost(params[0]);
                httpPost.addHeader("User-Agent", "BraecoWaiterAndroid/" + BraecoWaiterApplication.version);
                httpPost.addHeader("Cookie", "sid=" + BraecoWaiterApplication.sid);
                httpPost.setEntity(requestHttpEntity);
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 401) return BraecoWaiterUtils.STRING_401;
                return showResponseResult(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (BraecoWaiterUtils.STRING_401.equals(result)) {
                BraecoWaiterUtils.forceToLoginFor401(getContext());
                return;
            }
            Log.d("BraecoWaiter", "Records: " + result);
            if (result != null) {
                JSONObject array;
                try {
                    array = new JSONObject(result);
                    if ("success".equals(array.getString("message"))) {

                        if (reset) {
                            BraecoWaiterApplication.todayRecords.clear();
                            reset = false;
                        }

                        BraecoWaiterApplication.todayRecordsNum = array.getInt("sum");

                        JSONArray orders = array.getJSONArray("order");
                        int length = orders.length();

                        for (int i = 0; i < length; i++) {
                            JSONObject jsonOrder = orders.getJSONObject(i);
                            Map<String, Object> order = new HashMap<>();
                            order.put("id", jsonOrder.getInt("id"));
                            order.put("serial", jsonOrder.getString("serial"));
                            order.put("table", jsonOrder.getString("table"));
                            order.put("phone", jsonOrder.getString("phone"));
                            JSONArray jsonContent = jsonOrder.getJSONArray("content");
                            ArrayList<Map<String, Object>> meals = new ArrayList<>();
                            int contentLength = jsonContent.length();
                            for (int j = 0; j < contentLength; j++) {
                                JSONObject jsonMeal = jsonContent.getJSONObject(j);
                                Map<String, Object> meal = new HashMap<>();
                                meal.put("id", jsonMeal.getInt("id"));
                                meal.put("name", jsonMeal.getString("name"));
                                meal.put("price", jsonMeal.getString("price"));

                                if (!jsonMeal.has("type") || jsonMeal.getInt("type") == 0) {
                                    // is meal
                                    JSONArray jsonProperty = jsonMeal.getJSONArray("property");
                                    int propertyLength = jsonProperty.length();
                                    String[] property = new String[propertyLength];
                                    for (int k = 0; k < propertyLength; k++) {
                                        property[k] = jsonProperty.getString(k);
                                    }
                                    if (propertyLength > 0) {
                                        String properties = (String) meal.get("name") + "（";
                                        for (int k = 0; k < propertyLength; k++) {
                                            if (k > 0) properties += " ";
                                            properties += property[k];
                                        }
                                        properties += "）";
                                        if (j == contentLength - 1) {
                                            meal.put("properties", properties);
                                        } else {
                                            meal.put("properties", properties);
                                        }
                                    } else {
                                        if (j == contentLength - 1) {
                                            meal.put("properties", meal.get("name"));
                                        } else {
                                            meal.put("properties", meal.get("name"));
                                        }
                                    }
                                    meal.put("property", property);
                                    meal.put("isSet", false);
                                } else if (jsonMeal.getInt("type") == 1) {
                                    // is set

                                    // for combos
                                    ArrayList<ArrayList<Map<String, Object>>> combos = new ArrayList<>();
                                    ArrayList<Pair<Map<String, Object>, Integer>> combosInPair = new ArrayList<>();

                                    JSONArray combosJSON = jsonMeal.getJSONArray("property");

                                    meal.put("refund_property", combosJSON);

                                    int combosSize = combosJSON.length();
                                    for (int jj = 0; jj < combosSize; jj++) {
                                        JSONArray mealsJSON = combosJSON.getJSONArray(jj);
                                        ArrayList<Map<String, Object>> combo = new ArrayList<>();
                                        int mealsSize = mealsJSON.length();
                                        for (int k = 0; k < mealsSize; k++) {
                                            Map<String, Object> mealJSON = new HashMap<>();
                                            JSONArray propertiesJSON = mealsJSON.getJSONObject(k).getJSONArray("p");
                                            ArrayList<String> properties = new ArrayList<>();
                                            int propertiesSize = propertiesJSON.length();
                                            for (int u = 0; u < propertiesSize; u++) {
                                                properties.add(propertiesJSON.getString(u));
                                            }
                                            mealJSON.put("id", mealsJSON.getJSONObject(k).getInt("id"));
                                            mealJSON.put("name", mealsJSON.getJSONObject(k).getString("name"));
                                            mealJSON.put("properties", properties);
                                            combo.add(mealJSON);
                                        }
                                        combos.add(combo);
                                    }
                                    // calculate the same meal
                                    for (ArrayList<Map<String, Object>> combo : combos) {
                                        for (Map<String, Object> mealInCombo : combo) {
                                            // for every meal(id, properties only)
                                            boolean exist = false;
                                            int index = 0;
                                            for (Pair<Map<String, Object>, Integer> pair : combosInPair) {
                                                if (BraecoWaiterUtils.isSameSubMeal(pair.first, mealInCombo)) {
                                                    // this meal is put already
                                                    combosInPair.set(index, new Pair<Map<String, Object>, Integer>(mealInCombo, pair.second + 1));
                                                    exist = true;
                                                    break;
                                                }
                                                index++;
                                            }
                                            if (!exist) {
                                                // this meal is not put
                                                combosInPair.add(new Pair<Map<String, Object>, Integer>(mealInCombo, 1));
                                            }
                                        }
                                    }
                                    // write all the sub meals to property
                                    String setPropertiesString = "：";
                                    for (Pair<Map<String, Object>, Integer> p : combosInPair) {
                                        // get meal name
                                        setPropertiesString += "\n" + p.first.get("name");
                                        String propertiesString = "（";
                                        boolean isFirstProperty = true;
                                        for (String property : (ArrayList<String>)p.first.get("properties")) {
                                            if (!isFirstProperty) propertiesString += "、";
                                            isFirstProperty = false;
                                            propertiesString += property;
                                        }
                                        propertiesString += "）";
                                        if ("（）".equals(propertiesString)) {
                                            // this meal has no properties
                                            propertiesString = "";
                                        }
                                        setPropertiesString += propertiesString + " ×" + p.second;
                                    }
                                    if ("：".equals(setPropertiesString)) setPropertiesString = "";
                                    meal.put("properties", meal.get("name") + setPropertiesString);
                                    meal.put("isSet", true);
                                }

                                meal.put("sum", jsonMeal.getInt("sum"));
                                meal.put("ableRefund", BraecoWaiterUtils.ableRefund(meal));
                                meal.put("refundingOrEd", BraecoWaiterUtils.refundingOrEd(meal));
                                meals.add(meal);
                            }
                            order.put("content", meals);
                            order.put("price", jsonOrder.getDouble("price"));
                            order.put("channel", jsonOrder.getString("channel"));
                            order.put("create_date", jsonOrder.getInt("create_date"));
                            order.put("refund", jsonOrder.getString("refund"));
                            order.put("type", jsonOrder.getString("type"));
                            order.put("expand", false);
                            BraecoWaiterApplication.todayRecords.add(order);
                        }

                        adapter.notifyDataSetChanged();
                        BraecoWaiterApplication.currentTodayRecordsPage++;

                        if (superRecyclerView != null)
                            superRecyclerView.hideMoreProgress();

                        isRefreshing = false;

                    } else {
                        BraecoWaiterUtils.showToast(getActivity(), "网络连接失败");
                    }

                } catch (JSONException e) {
                    if (BuildConfig.DEBUG)
                        Log.d("BraecoWaiter", "ServiceRecordFragment json error");
                    e.printStackTrace();
                }
            } else {
                BraecoWaiterUtils.showToast(getActivity(), "网络连接失败");
            }
        }
    }

}
