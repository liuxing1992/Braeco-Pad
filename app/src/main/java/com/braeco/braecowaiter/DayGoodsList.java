package com.braeco.braecowaiter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.braeco.braecowaiter.Interfaces.OnGetGoodsAsyncTaskListener;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.Tasks.GetGoodsAsyncTask;
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
import java.util.Comparator;
import java.util.List;

import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class DayGoodsList extends BraecoAppCompatActivity
        implements
        WeekPagerAdapter.OnDayClickListener,
        View.OnClickListener,
        DatePickerDialog.OnDateSetListener{

    private ArrayList<Double> goodsNum = new ArrayList<>();
    private ArrayList<Double> goodsProfit = new ArrayList<>();
    private ArrayList<String> goodsName = new ArrayList<>();

    private SortableTableView<String[]> table;

    private final static String[] WEEK_NAME = new String[]{"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

    private Context mContext;

    private ViewPager viewpager;
    private WeekPagerAdapter weekPagerAdapter;
    private TextView date;

    private FrameLayout frameLayout;

    // day, week and month
    private int timeType = 0;
    // profit, goods and vip
    private int viewType = 1;
    private int mYear, mMonth, mDay;

    private TextView emptyTip;

    private LinearLayout back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_goods_list);

        BraecoWaiterApplication.DATA_STATICS_FINISH = false;

        mContext = this;

        emptyTip = (TextView)findViewById(R.id.empty_tip);
        emptyTip.setVisibility(View.INVISIBLE);

        back = (LinearLayout)findViewById(R.id.back);
        back.setOnClickListener(this);

        date = (TextView)findViewById(R.id.date_tv);
        frameLayout = (FrameLayout)findViewById(R.id.date);

        frameLayout.setOnClickListener(this);

        viewpager = (ViewPager)findViewById(R.id.viewpager);
        weekPagerAdapter = new WeekPagerAdapter(this);
        viewpager.setAdapter(weekPagerAdapter);
        viewpager.setOffscreenPageLimit(1);

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

        table = (SortableTableView<String[]>)findViewById(R.id.table);
        table.setColumnCount(3);
        table.setColumnWeight(0, 5);
        table.setColumnWeight(1, 3);
        table.setColumnWeight(2, 5);
        table.setHeaderBackgroundColor(
                BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.white));
        SimpleTableHeaderAdapter simpleTableHeaderAdapter
                = new SimpleTableHeaderAdapter(mContext, "餐品名", "销量", "销售总额");
        ((TextView)simpleTableHeaderAdapter.getHeaderView(1, null)).setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        ((TextView)simpleTableHeaderAdapter.getHeaderView(2, null)).setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        simpleTableHeaderAdapter.setTextColor(
                BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.colorPrimary));
        simpleTableHeaderAdapter.setTextSize(14);
        table.setHeaderAdapter(simpleTableHeaderAdapter);

        BraecoWaiterApplication.GOODS_LIST_TASK_NUM++;
        updateData();
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
                tpd.setYearRange(2000, 2020);
                tpd.show(getFragmentManager(), "Timepickerdialog3");
                updateData();
                break;
            case R.id.back:
                finish();
                break;
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar nowDate = Calendar.getInstance();
        Calendar fromDate = Calendar.getInstance();
        nowDate.set(year, monthOfYear, dayOfMonth);
        nowDate.add(Calendar.SECOND, 0);
        fromDate.set(1900, 1, 5, 0, 0, 0);
        fromDate.add(Calendar.SECOND, 0);

        int days = (int)((nowDate.getTimeInMillis() - fromDate.getTimeInMillis()) / (24 * 60 * 60 * 1000));

        int weekDay;
        if (nowDate.get(Calendar.DAY_OF_WEEK) == 1) weekDay = 6;
        else weekDay = nowDate.get(Calendar.DAY_OF_WEEK) - 2;

        BraecoWaiterApplication.selectedDate[0] = year;
        BraecoWaiterApplication.selectedDate[1] = monthOfYear + 1;
        BraecoWaiterApplication.selectedDate[2] = dayOfMonth;

        weekPagerAdapter = new WeekPagerAdapter(this);
        viewpager.setAdapter(weekPagerAdapter);
        viewpager.setOffscreenPageLimit(1);
        viewpager.setCurrentItem(days / 7, false);

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = format1.format(nowDate.getTime()) + "，"
                + WEEK_NAME[nowDate.get(Calendar.DAY_OF_WEEK) - 1];

        mYear = nowDate.get(Calendar.YEAR);
        mMonth = nowDate.get(Calendar.MONTH) + 1;
        mDay = nowDate.get(Calendar.DAY_OF_MONTH);

        BraecoWaiterApplication.GOODS_LIST_TASK_NUM++;
        updateData();

        date.setText(dateString);
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

        date.setText(dateString);

        BraecoWaiterApplication.GOODS_LIST_TASK_NUM++;
        updateData();
    }

    private void updateData() {
        new GetGoodsAsyncTask(mOnGetGoodsAsyncTaskListener, GetGoodsAsyncTask.TASK_ID, mYear,mMonth, mDay, -1, 1000).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private OnGetGoodsAsyncTaskListener mOnGetGoodsAsyncTaskListener = new OnGetGoodsAsyncTaskListener() {
        @Override
        public void success(JSONObject result) {
            try {
                if ("success".equals(result.getString("message"))) {
                    JSONArray jsonGoods = result.getJSONArray("rank");
                    int length = jsonGoods.length();
                    goodsNum.clear();
                    goodsProfit.clear();
                    goodsName.clear();
                    int sumInt = 0;
                    for (int i = 0; i < length; i++) {
                        JSONObject good = jsonGoods.getJSONObject(i);
                        goodsNum.add(Double.parseDouble(good.get("sum").toString()));
                        sumInt += goodsNum.get(i);
                        goodsProfit.add(Double.parseDouble(good.get("price").toString()));
                        goodsName.add(good.getString("name"));
                    }

                    if (length > 0) {
                        emptyTip.setVisibility(View.INVISIBLE);
                        table.setVisibility(View.VISIBLE);

                        String[][] strings = new String[length][];
                        for (int i = 0; i < length; i++) {
                            strings[i] = new String[3];
                            strings[i][0] = goodsName.get(i);
                            strings[i][1] = String.format("%.0f", goodsNum.get(i));
                            strings[i][2] = "¥ " + String.format("%.2f", goodsProfit.get(i));
                        }

                        table.setDataAdapter(new GoodsTableAdapter(mContext, strings));
                        table.setColumnComparator(0, new StringComparator());
                        table.setColumnComparator(1, new Double1Comparator());
                        table.setColumnComparator(2, new Double2Comparator());

                    } else {
                        emptyTip.setVisibility(View.VISIBLE);
                        table.setVisibility(View.INVISIBLE);
                    }

                } else {
                    BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                }
            } catch (JSONException e) {
                if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentData json error");
                e.printStackTrace();
            }
        }

        @Override
        public void fail(String message) {
            BraecoWaiterUtils.showToast(message);
        }

        @Override
        public void signOut() {
            BraecoWaiterUtils.forceToLoginFor401(mContext);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BraecoWaiterApplication.DATA_STATICS_FINISH = true;
    }

    private static class StringComparator implements Comparator<String[]> {
        @Override
        public int compare(String[] s1, String[] s2) {
            return s1[0].compareTo(s2[0]);
        }
    }

    private static class Double1Comparator implements Comparator<String[]> {
        @Override
        public int compare(String[] s1, String[] s2) {
            Double d1 = Double.parseDouble(s1[1]);
            Double d2 = Double.parseDouble(s2[1]);
            return d1.compareTo(d2);
        }
    }

    private static class Double2Comparator implements Comparator<String[]> {
        @Override
        public int compare(String[] s1, String[] s2) {
            String str1 = "0";
            String str2 = "0";
            for (int i = 0; i < s1[2].length(); i++) {
                if ('0' <= s1[2].charAt(i) && s1[2].charAt(i) <= '9') {
                    str1 = s1[2].substring(i, s1[2].length());
                    break;
                }
            }
            for (int i = 0; i < s2[2].length(); i++) {
                if ('0' <= s2[2].charAt(i) && s2[2].charAt(i) <= '9') {
                    str2 = s2[2].substring(i, s2[2].length());
                    break;
                }
            }
            Double d1 = Double.parseDouble(str1);
            Double d2 = Double.parseDouble(str2);
            return d1.compareTo(d2);
        }
    }
}
