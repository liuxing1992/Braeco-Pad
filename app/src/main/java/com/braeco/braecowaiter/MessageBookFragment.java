package com.braeco.braecowaiter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Weiping on 2015/12/1.
 */
public class MessageBookFragment extends Fragment
        implements
        SwipeRefreshLayout.OnRefreshListener,
        OnMoreListener,
        MessageBookFragmentRecyclerViewAdapter.OnRefundListener {

    private Activity activity;

    private SuperRecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

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
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MessageBookFragment onCreateView");
        View messageLayout = inflater.inflate(R.layout.fragment_message_book, container, false);

        recyclerView = (SuperRecyclerView) messageLayout.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setRefreshListener(this);
        recyclerView.setRefreshingColorResources(R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary);

        BraecoWaiterApplication.messageBookFragmentRecyclerViewAdapter
                = new MessageBookFragmentRecyclerViewAdapter(
                (MessageBookFragmentRecyclerViewAdapter.OnDealListener) activity, this);

        recyclerView.setAdapter(BraecoWaiterApplication.messageBookFragmentRecyclerViewAdapter);

        return messageLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BraecoWaiterData.JUST_REFRESH_RECORDS) {
            BraecoWaiterData.JUST_REFRESH_RECORDS = false;
            BraecoWaiterUtils.showToast(getActivity(), "您刚刚进行了退款操作，正在刷新订单，请稍候");
            refreshRefundedOrder();
        }
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Todo
                if (recyclerView != null)
                    recyclerView.getSwipeToRefresh().setRefreshing(false);
            }
        }, 3000);
    }

    @Override
    public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        if (BraecoWaiterApplication.messageBookFragmentRecyclerViewAdapter != null)
            BraecoWaiterApplication.messageBookFragmentRecyclerViewAdapter.notifyDataSetChanged();
    }

    public void scrollToBottom() {
        if (recyclerView != null) {
            // Todo
        }
    }

    @Override
    public void onRefund(int p) {
        if (!AuthorityManager.ableTo(Authority.REFUND)) {
            AuthorityManager.showDialog(getContext(), "为客人退款");
            return;
        }
        BraecoWaiterData.refundMeals = new ArrayList<>();
        BraecoWaiterData.unRefundMeals = new ArrayList<>();
        Map<String, Object> record = BraecoWaiterApplication.allOrder.get(p);
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

    private void refreshRefundedOrder() {
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Refresh refund: " + BraecoWaiterData.refundId);
        new GetRefundedOrder().execute(
                "http://brae.co/Dinner/Manage/Orders/" + BraecoWaiterData.refundId);
    }

    private class GetRefundedOrder extends AsyncTask<String, Void, String> {

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
                Log.d("BraecoWaiter", "Refresh refund: " + result);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> pairList = new ArrayList<>();
            try {
                UrlEncodedFormEntity e = new UrlEncodedFormEntity(
                        pairList, "UTF-8");
                e.setContentEncoding(HTTP.UTF_8);
                HttpEntity requestHttpEntity = e;

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
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Refresh refund: " + result);

            if (result != null) {
                JSONObject jsonResult;
                try {
                    jsonResult = new JSONObject(result);
                    if ("success".equals(jsonResult.getString("message"))) {
                        BraecoWaiterUtils.showToast(getActivity(), "刷新订单成功");
                        JSONObject jsonOrder = jsonResult.getJSONObject("order");
                        JSONArray array = jsonOrder.getJSONArray("content");

                        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

                        String refund = jsonOrder.getString("refund");

                        double prices = 0;
                        for (int i = 0 ; i < array.length() ; i++) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            JSONObject obj2 = array.getJSONObject(i);
                            if (obj2.getInt("type") == 0) {
                                // is meal
                                map.put("id", obj2.getInt("id"));
                                map.put("name", obj2.getString("name"));
                                if ("会员充值".equals(obj2.getString("name"))) {
                                    refund = "不可退款";
                                }
                                map.put("price" , obj2.getDouble("price"));
                                map.put("sum", obj2.getInt("sum"));
                                prices += (Double)map.get("price") * (Integer)map.get("sum");
                                JSONArray arr = obj2.getJSONArray("property");
                                String[] str1 = new String[arr.length()];
                                for (int j = 0 ; j < arr.length() ; j++) {
                                    str1[j] = arr.getString(j);
                                }
                                if (arr.length() > 0) {
                                    String properties = (String)map.get("name") + "（";
                                    for (int k = 0; k < arr.length(); k++) {
                                        if (k > 0) properties += " ";
                                        properties += str1[k];
                                    }
                                    properties += "）";
                                    if (i == array.length() - 1) {
                                        map.put("properties", properties);
                                    } else {
                                        map.put("properties", properties);
                                    }
                                } else {
                                    if (i == array.length() - 1) {
                                        map.put("properties", map.get("name"));
                                    } else {
                                        map.put("properties", map.get("name"));
                                    }
                                }
                                map.put("isSet", false);
                                map.put("property", str1);
                                map.put("ableRefund", BraecoWaiterUtils.ableRefund(map));
                                map.put("refundingOrEd", BraecoWaiterUtils.refundingOrEd(map));
                            } else if (obj2.getInt("type") == 1) {
                                // is set
                                map.put("id", obj2.getInt("id"));
                                map.put("name", obj2.getString("name"));
                                if ("会员充值".equals(obj2.getString("name"))) {
                                    refund = "不可退款";
                                }
                                map.put("price" , obj2.getDouble("price"));
                                map.put("sum", obj2.getInt("sum"));
                                prices += (Double)map.get("price") * (Integer)map.get("sum");

                                // for combos
                                ArrayList<ArrayList<Map<String, Object>>> combos = new ArrayList<>();
                                ArrayList<Pair<Map<String, Object>, Integer>> combosInPair = new ArrayList<>();

                                JSONArray combosJSON = obj2.getJSONArray("property");

                                map.put("refund_property", combosJSON);

                                int combosSize = combosJSON.length();
                                for (int j = 0; j < combosSize; j++) {
                                    JSONArray meals = combosJSON.getJSONArray(j);
                                    ArrayList<Map<String, Object>> combo = new ArrayList<>();
                                    int mealsSize = meals.length();
                                    for (int k = 0; k < mealsSize; k++) {
                                        Map<String, Object> meal = new HashMap<>();
                                        JSONArray propertiesJSON = meals.getJSONObject(k).getJSONArray("p");
                                        ArrayList<String> properties = new ArrayList<>();
                                        int propertiesSize = propertiesJSON.length();
                                        for (int u = 0; u < propertiesSize; u++) {
                                            properties.add(propertiesJSON.getString(u));
                                        }
                                        meal.put("id", meals.getJSONObject(k).getInt("id"));
                                        meal.put("name", meals.getJSONObject(k).getString("name"));
                                        meal.put("properties", properties);
                                        combo.add(meal);
                                    }
                                    combos.add(combo);
                                }
                                // calculate the same meal
                                for (ArrayList<Map<String, Object>> meals : combos) {
                                    for (Map<String, Object> meal : meals) {
                                        // for every meal(id, properties only)
                                        boolean exist = false;
                                        int index = 0;
                                        for (Pair<Map<String, Object>, Integer> pair : combosInPair) {
                                            if (BraecoWaiterUtils.isSameSubMeal(pair.first, meal)) {
                                                // this meal is put already
                                                combosInPair.set(index, new Pair<>(meal, pair.second + 1));
                                                exist = true;
                                                break;
                                            }
                                            index++;
                                        }
                                        if (!exist) {
                                            // this meal is not put
                                            combosInPair.add(new Pair<>(meal, 1));
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
                                map.put("isSet", true);
                                map.put("properties", map.get("name") + setPropertiesString);
                                map.put("ableRefund", BraecoWaiterUtils.ableRefund(map));
                                map.put("refundingOrEd", BraecoWaiterUtils.refundingOrEdForSet(map));
                            }
                            list.add(map);
                        }

                        // change the meals in the original data
                        int size = BraecoWaiterApplication.allOrder.size();
                        for (int i = 0; i < size; i++) {
                            if (BraecoWaiterApplication.allOrder.get(i).get("id").equals(BraecoWaiterData.refundId)) {
                                BraecoWaiterApplication.allOrder.get(i).remove("content");
                                BraecoWaiterApplication.allOrder.get(i).put("content", list);
                                BraecoWaiterApplication.allOrder.get(i).put("prices", prices);
                                BraecoWaiterApplication.allOrder.get(i).put("refund", refund);
                                break;
                            }
                        }
                        if (BraecoWaiterApplication.messageBookFragmentRecyclerViewAdapter != null)
                            BraecoWaiterApplication.messageBookFragmentRecyclerViewAdapter.notifyDataSetChanged();

                    }
                } catch (JSONException e) {
                    BraecoWaiterUtils.showToast(getActivity(), "刷新订单失败, 网络连接失败");
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "Refresh refund activity json error");
                    e.printStackTrace();
                }
            } else {
                BraecoWaiterUtils.showToast(getActivity(), "刷新订单失败, 网络连接失败");
            }
        }
    }
}
