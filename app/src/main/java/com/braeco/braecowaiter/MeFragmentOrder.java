package com.braeco.braecowaiter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.Authority;
import com.braeco.braecowaiter.Model.AuthorityManager;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.UIs.TitleLayout;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeFragmentOrder extends BraecoAppCompatActivity
        implements
        WeekPagerAdapter.OnDayClickListener,
        View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        OnMoreListener,
        DatePickerDialog.OnDateSetListener,
        MeFragmentOrderRecyclerViewAdapter.OnRefundListener,
        TitleLayout.OnTitleActionListener {

    private final static String[] WEEK_NAME
            = new String[]{"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

    private Context mContext;

    private TitleLayout title;

    private ViewPager viewpager;
    private WeekPagerAdapter weekPagerAdapter;
    private TextView date;

    private FrameLayout frameLayout;

    private int mYear, mMonth, mDay;

    private SuperRecyclerView superRecyclerView;
    private MeFragmentOrderRecyclerViewAdapter adapter;

    private boolean isRefreshing = true;
    private boolean reset = false;

    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_order);

        mContext = this;

        title = (TitleLayout)findViewById(R.id.title_layout);
        title.setOnTitleActionListener(this);

        date = (TextView)findViewById(R.id.date_tv);
        frameLayout = (FrameLayout)findViewById(R.id.date);

        frameLayout.setOnClickListener(this);

        viewpager = (ViewPager)findViewById(R.id.viewpager);
        weekPagerAdapter = new WeekPagerAdapter(this);
        viewpager.setAdapter(weekPagerAdapter);
        viewpager.setOffscreenPageLimit(1);

        superRecyclerView = (SuperRecyclerView)findViewById(R.id.recyclerview);
        superRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        superRecyclerView.setRefreshListener(this);
        superRecyclerView.setRefreshingColorResources(R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary, R.color.colorPrimary);
        superRecyclerView.setupMoreListener(this, Integer.MAX_VALUE);
        adapter = new MeFragmentOrderRecyclerViewAdapter(this);
        superRecyclerView.setAdapter(adapter);

        Calendar nowDate = Calendar.getInstance();
        Calendar fromDate = Calendar.getInstance();
        fromDate.set(1900, 1, 5, 0, 0, 0);
        fromDate.add(Calendar.SECOND, 0);

        int days = (int)((nowDate.getTimeInMillis() - fromDate.getTimeInMillis()) / (24 * 60 * 60 * 1000));
        viewpager.setCurrentItem(days / 7, false);

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = format1.format(nowDate.getTime()) + "，"
                + WEEK_NAME[nowDate.get(Calendar.DAY_OF_WEEK) - 1];

        date.setText(dateString);

        mYear = nowDate.get(Calendar.YEAR);
        mMonth = nowDate.get(Calendar.MONTH) + 1;
        mDay = nowDate.get(Calendar.DAY_OF_MONTH);

        reset = true;
        BraecoWaiterApplication.oneDayRecordsPage = 1;
        BraecoWaiterApplication.ME_FRAGMENT_ORDER_TASK_NUM++;
        ((TextView)superRecyclerView.getEmptyView().findViewById(R.id.empty_tip)).setText("加载中……");
        updateData();
    }

    @Override
    public void onResume() {
        if (BraecoWaiterData.JUST_REFRESH_RECORDS) {
            BraecoWaiterData.JUST_REFRESH_RECORDS = false;
            BraecoWaiterUtils.showToast(mContext, "您刚刚进行了退款操作，正在刷新流水订单，请稍候");
            onRefresh();
        }
        super.onResume();
    }

    @Override
    public void OnDayClick(int p) {

        int days = viewpager.getCurrentItem() * 7;

        Calendar fromDate = Calendar.getInstance();
        fromDate.set(1900, 1, 5, 0, 0, 0);
        fromDate.add(Calendar.SECOND, 0);

        fromDate.add(Calendar.DATE, days + p);

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");

        String dateString = format1.format(fromDate.getTime()) + "，"
                + WEEK_NAME[fromDate.get(Calendar.DAY_OF_WEEK) - 1];

        mYear = fromDate.get(Calendar.YEAR);
        mMonth = fromDate.get(Calendar.MONTH) + 1;
        mDay = fromDate.get(Calendar.DAY_OF_MONTH);

        onDateSet(null, mYear, mMonth - 1, mDay);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.date:
                int year = Integer.valueOf(date.getText().toString().substring(0, 4));
                int month = Integer.valueOf(date.getText().toString().substring(5, 7));
                int day = Integer.valueOf(date.getText().toString().substring(8, 10));
                DatePickerDialog tpd = DatePickerDialog.newInstance(
                        this, year, month - 1, day);
                tpd.setAccentColor(BraecoWaiterUtils.getInstance()
                        .getColorFromResource(this, R.color.colorPrimary));
                tpd.setTitle("单日选择");
                tpd.show(getFragmentManager(), "Timepickerdialog3");
//                updateData();
                break;
        }
    }

    @Override
    public void onDateSet(@Nullable DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar nowDate = Calendar.getInstance();
        Calendar fromDate = Calendar.getInstance();
        nowDate.set(year, monthOfYear, dayOfMonth);
        nowDate.add(Calendar.SECOND, 0);
        fromDate.set(1900, 1, 5, 0, 0, 0);
        fromDate.add(Calendar.SECOND, 0);

        int days = (int)((nowDate.getTimeInMillis() - fromDate.getTimeInMillis()) / (24 * 60 * 60 * 1000));

        BraecoWaiterApplication.selectedDate[0] = year;
        BraecoWaiterApplication.selectedDate[1] = monthOfYear + 1;
        BraecoWaiterApplication.selectedDate[2] = dayOfMonth;

        weekPagerAdapter = new WeekPagerAdapter(this);
        viewpager.setAdapter(weekPagerAdapter);
        viewpager.setOffscreenPageLimit(1);
        viewpager.setCurrentItem(days / 7, false);

        mYear = nowDate.get(Calendar.YEAR);
        mMonth = nowDate.get(Calendar.MONTH) + 1;
        mDay = nowDate.get(Calendar.DAY_OF_MONTH);

        reset = true;
        BraecoWaiterApplication.oneDayRecordsPage = 1;
        BraecoWaiterApplication.ME_FRAGMENT_ORDER_TASK_NUM++;
        ((TextView)superRecyclerView.getEmptyView().findViewById(R.id.empty_tip)).setText("加载中……");
        updateData();

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = format1.format(nowDate.getTime()) + "，"
                + WEEK_NAME[nowDate.get(Calendar.DAY_OF_WEEK) - 1];
        date.setText(dateString);
    }

    @Override
    public void onRefresh() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (superRecyclerView != null)
                    superRecyclerView.getSwipeToRefresh().setRefreshing(false);
            }
        }, 3000);

        if (isRefreshing) {
            return;
        }
        isRefreshing = true;

        reset = true;
        BraecoWaiterApplication.oneDayRecordsPage = 1;

        BraecoWaiterApplication.ME_FRAGMENT_ORDER_TASK_NUM++;
        ((TextView)superRecyclerView.getEmptyView().findViewById(R.id.empty_tip)).setText("加载中……");
        updateData();
    }

    @Override
    public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        if (BraecoWaiterApplication.oneDayRecords.size() == BraecoWaiterApplication.oneDayRecordsNum) {
            superRecyclerView.hideMoreProgress();
            return;
        }
        updateData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BraecoWaiterApplication.oneDayRecords.clear();
        BraecoWaiterApplication.oneDayRecordsPage = 1;
        BraecoWaiterApplication.oneDayRecordsNum = 0;
    }

    private void updateData() {

        Calendar todaySt = Calendar.getInstance();
        todaySt.set(mYear, mMonth - 1, mDay, 0, 0, 0);
        todaySt.add(Calendar.SECOND, 0);
        Calendar todayEd = Calendar.getInstance();
        todayEd.set(mYear, mMonth - 1, mDay, 23, 59, 59);
        todayEd.add(Calendar.SECOND, 0);

        Calendar now = Calendar.getInstance();
        if (todaySt.after(now)) {
            todaySt = (Calendar) now.clone();
            todaySt.set(Calendar.HOUR_OF_DAY, 0);
            todaySt.set(Calendar.MINUTE, 0);
            todaySt.set(Calendar.SECOND, 0);
            todaySt.add(Calendar.SECOND, 0);
            todayEd = (Calendar) now.clone();
            BraecoWaiterApplication.oneDayRecords.clear();
            BraecoWaiterApplication.oneDayRecordsNum = 0;
            BraecoWaiterApplication.oneDayRecordsPage = 1;
            adapter.notifyDataSetChanged();
            ((TextView)superRecyclerView.getEmptyView()
                    .findViewById(R.id.empty_tip)).setText("本日尚未有流水订单，下拉刷新试试吧？");
            return;
        } else if (todayEd.after(now)) {
            todayEd = (Calendar) now.clone();
        }

        new GetRecords(BraecoWaiterApplication.ME_FRAGMENT_ORDER_TASK_NUM)
                .execute("http://brae.co/Dinner/Manage/Orders",
                        (todaySt.getTime().getTime() / 1000) + "",
                        (todayEd.getTime().getTime() / 1000) + "",
                        BraecoWaiterApplication.oneDayRecordsPage + "",
                        5 + "");
    }

    @Override
    public void onRefund(int p) {
        if (!AuthorityManager.ableTo(Authority.REFUND)) {
            AuthorityManager.showDialog(mContext, "为客人退款");
            return;
        }
        BraecoWaiterData.refundMeals = new ArrayList<>();
        BraecoWaiterData.unRefundMeals = new ArrayList<>();
        Map<String, Object> record = BraecoWaiterApplication.oneDayRecords.get(p);
        ArrayList<Map<String, Object>> meals = (ArrayList<Map<String,Object>>) record.get("content");
        BraecoWaiterData.refundId = (Integer)record.get("id");
        for (int i = 0; i < meals.size(); i++) {
            if (!BraecoWaiterUtils.ableRefund(meals.get(i))) {
                BraecoWaiterData.unRefundMeals.add(meals.get(i));
            } else {
                BraecoWaiterData.refundMeals.add(meals.get(i));
            }
        }
        startActivity(new Intent(mContext, SingleRefundActivity.class));
    }

    @Override
    public void clickTitleBack() {
        finish();
    }

    @Override
    public void doubleClickTitle() {
//        superRecyclerView.getRecyclerView().smoothScrollBy(0, 0);
        superRecyclerView.getRecyclerView().scrollToPosition(0);
    }

    @Override
    public void clickTitleEdit() {

    }

    private class GetRecords extends AsyncTask<String, Void, String> {

        private int task;

        public GetRecords(int task) {
            this.task = task;
        }

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
                httpPost.addHeader("Cookie" , "sid=" + BraecoWaiterApplication.sid);
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
                BraecoWaiterUtils.forceToLoginFor401(mContext);
                return;
            }

            if (this.task != BraecoWaiterApplication.ME_FRAGMENT_ORDER_TASK_NUM) return;
            Log.d("BraecoWaiter", "MeFragment Records: " + result);
            if (result != null) {
                JSONObject array;
                try {
                    array = new JSONObject(result);
                    if ("success".equals(array.getString("message"))) {

                        if (reset) {
                            BraecoWaiterApplication.oneDayRecords.clear();
                            reset = false;
                        }

                        BraecoWaiterApplication.oneDayRecordsNum = array.getInt("sum");
                        if (BraecoWaiterApplication.oneDayRecordsNum == 0) {
                            ((TextView)superRecyclerView.getEmptyView()
                                    .findViewById(R.id.empty_tip)).setText("本日尚未有流水订单，下拉刷新试试吧？");
                        }

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
                            BraecoWaiterApplication.oneDayRecords.add(order);
                        }

                        adapter.notifyDataSetChanged();
                        BraecoWaiterApplication.oneDayRecordsPage++;

                        if (superRecyclerView != null)
                            superRecyclerView.hideMoreProgress();

                        isRefreshing = false;

                    } else {
                        BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                    }

                } catch (JSONException e) {
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentOrder json error");
                    e.printStackTrace();
                }
            } else {
                BraecoWaiterUtils.showToast(mContext, "网络连接失败");
            }
        }
    }
}
