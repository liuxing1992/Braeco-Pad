package com.braeco.braecowaiter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Interfaces.OnPostSmallTicketAsyncTaskListener;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.Tasks.PostSmallTicketAsyncTask;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

//

public class MeFragmentData extends BraecoAppCompatActivity
        implements
        WeekPagerAdapter.OnDayClickListener,
        MonthPagerAdapter.OnWeekClickListener,
        YearPagerAdapter.OnMonthClickListener,
        View.OnClickListener,
        DatePickerDialog.OnDateSetListener {

    private final static String[] WEEK_NAME = new String[]{"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
    private final static String[] TIME_UNIT = new String[]{"day", "month", "year"};

    public final static int MIN_YEAR = 2015;
    public final static int MAX_YEAR = 2020;

    private Context mContext;

    private TextView title;

    private ViewPager viewpager;
    private WeekPagerAdapter weekPagerAdapter;
    private MonthPagerAdapter monthPagerAdapter;
    private YearPagerAdapter yearPagerAdapter;
    private TextView date;

    private FrameLayout frameLayout;

    private SmartTabLayout tab;
    private ViewPager pagers;

    // day, week and month
    private int timeType = 0;
    // profit, goods and vip
    private int viewType = 1;
    private int mYear, mMonth, mDay;
    private int nYear, nMonth, nDay;
    private int oYear, oMonth;

    private LinearLayout back;

    private TextView print;
    private boolean printing = false;

    // only set to not -1 value once finish get data
    private boolean firstTime = true;  // first time to load data
    private int lastYear = -1;
    private int lastMonth = -1;   // 1 2 3 ...
    private int lastDay = -1;     // 1 2 3 ...
    private int last4Week = -1;   // 0 1 2 ...
    private int lastWeek = -1;    // 0 1 2 3
    private int last6Month = -1;  // 0 1 2 ...
    private int lastSMonth = -1;  // 0 1 2 3 4 5

    private FrameLayout day_fy;
    private FrameLayout week_fy;
    private FrameLayout month_fy;

    private long ST;
    private long EN;
    private int RANK_END;
    private long MEMBERSHIP_TIME;
    private String MEMBERSHIP_UNIT;

    private boolean loadSuccessfully = false;

    // the data got
    // profit page
    private int ORDERS_NUM = 0;
    private double PROFIT = 0;
    private ArrayList<Map<String, Object>> TYPE_PROFITS = new ArrayList<>();
    private ArrayList<Double> PROFITS = new ArrayList<>();
    private ArrayList<Double> FEES = new ArrayList<>();
    // goods page
    private ArrayList<Map<String, Object>> MEALS = new ArrayList<>();
    // vip page
    private ArrayList<Map<String, Object>> VIPS = new ArrayList<>();

    // terminal
    private boolean[] TASKS = new boolean[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_data);

        BraecoWaiterApplication.DATA_STATICS_FINISH = false;

        BraecoWaiterApplication.selectedDate[0] = -1;
        BraecoWaiterApplication.selectedDate[1] = -1;
        BraecoWaiterApplication.selectedDate[2] = -1;

        mContext = this;

        title = (TextView)findViewById(R.id.title);

        back = (LinearLayout)findViewById(R.id.back);
        back.setOnClickListener(this);

        tab = (SmartTabLayout)findViewById(R.id.viewpagertab);
        pagers = (ViewPager)findViewById(R.id.pager);
        FragmentPagerItems pages = new FragmentPagerItems(mContext);
        pages.add(FragmentPagerItem.of("营业总额", DayProfitFragment.class));
        pages.add(FragmentPagerItem.of("单品销量", DayGoodsFragment.class));
        pages.add(FragmentPagerItem.of("会员发展", DayVipFragment.class));
        FragmentPagerItemAdapter adapter
                = new FragmentPagerItemAdapter(getSupportFragmentManager(), pages);
        pagers.setOffscreenPageLimit(3);
        pagers.setAdapter(adapter);
        tab.setViewPager(pagers);
        ((TextView) tab.getTabAt(0)).setTextColor(BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.colorPrimary));
        ((TextView) tab.getTabAt(1)).setTextColor(BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.colorPrimary));
        ((TextView) tab.getTabAt(2)).setTextColor(BraecoWaiterUtils.getInstance().getColorFromResource(mContext, R.color.colorPrimary));

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

        print = (TextView)findViewById(R.id.print);
        print.setOnClickListener(this);

        day_fy = (FrameLayout)findViewById(R.id.day);
        day_fy.setOnClickListener(this);
        week_fy = (FrameLayout)findViewById(R.id.week);
        week_fy.setOnClickListener(this);
        month_fy = (FrameLayout)findViewById(R.id.month);
        month_fy.setOnClickListener(this);

        onDateSet(null, mYear, mMonth - 1, mDay);
    }

    @Override
    public void OnDayClick(int p) {

        int days = viewpager.getCurrentItem() * 7;

        Calendar fromDate = Calendar.getInstance();
        fromDate.set(1900, 1, 5, 0, 0, 0);
        fromDate.add(Calendar.SECOND, 0);

        fromDate.add(Calendar.DATE, days + p);

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");

        mYear = fromDate.get(Calendar.YEAR);
        mMonth = fromDate.get(Calendar.MONTH) + 1;
        mDay = fromDate.get(Calendar.DAY_OF_MONTH);

        onDateSet(null, mYear, mMonth - 1, mDay);
    }

    @Override
    public void OnWeekClick(long t) {

        BraecoWaiterUtils.createMondays();
        for (int i = 0; i < BraecoWaiterUtils.MONDAYS.size(); i++) {
            if (t <= BraecoWaiterUtils.MONDAYS.get(i)) {
                for (int j = 0; j < 4; j++) {
                    if (t <= BraecoWaiterUtils.MONDAYS.get(i) - (3 - j) * BraecoWaiterUtils.ONE_WEEK_LONG) {
                        if (i == last4Week && j == lastWeek) {
                            return;
                        }
                        break;
                    }
                }
                break;
            }
        }

        EN = t;
        ST = EN - BraecoWaiterUtils.ONE_WEEK_LONG + 1;
        MEMBERSHIP_TIME = EN;
        MEMBERSHIP_UNIT = "WEEK";
        getData(false);
    }

    @Override
    public void OnMonthClick(long t) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(t * 1000));
        if ((calendar.get(Calendar.YEAR) - MIN_YEAR)
                + (calendar.get(Calendar.MONTH)) / 6 == last6Month
                && (calendar.get(Calendar.MONTH)) % 6 == lastSMonth
                && loadSuccessfully) return;

        EN = t;
        calendar = Calendar.getInstance();
        calendar.setTime(new Date(EN * 1000));
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.SECOND, 0);
        ST = calendar.getTimeInMillis() / 1000;
        MEMBERSHIP_TIME = EN;
        MEMBERSHIP_UNIT = "MONTH";
        getData(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.date:
                selectDate();
                break;
            case R.id.back:
                finish();
                break;
            case R.id.print:
                print();
                break;
            case R.id.day:
                if (timeType == 0) return;
                timeType = 0;
                // to judge whether first select
                BraecoWaiterApplication.selectedDate[0] = lastYear;
                BraecoWaiterApplication.selectedDate[1] = lastMonth;
                BraecoWaiterApplication.selectedDate[2] = lastDay;
                getData(true);
                break;
            case R.id.week:
                if (timeType == 1) return;
                timeType = 1;
                // to judge whether first select
                BraecoWaiterApplication.selectedWeek = lastWeek;
                getData(true);
                break;
            case R.id.month:
                if (timeType == 2) return;
                timeType = 2;
                BraecoWaiterApplication.selectedMonth = lastMonth;
                getData(true);
                break;
        }
    }

    private void selectDate() {
        switch (timeType) {
            case 0:
                int year = Integer.valueOf(date.getText().toString().substring(0, 4));
                int month = Integer.valueOf(date.getText().toString().substring(5, 7));
                int day = Integer.valueOf(date.getText().toString().substring(8, 10));
                DatePickerDialog tpd = DatePickerDialog.newInstance(
                        this, year, month - 1, day);
                tpd.setAccentColor(BraecoWaiterUtils.getInstance()
                        .getColorFromResource(this, R.color.colorPrimary));
                tpd.setYearRange(MIN_YEAR, MAX_YEAR);
                tpd.setTitle("单日选择");
                tpd.show(getFragmentManager(), "Timepickerdialog3");
                break;
            case 1:
                BraecoWaiterUtils.createMondays();
                Object[] os = BraecoWaiterUtils.MONDAYS_STRING.toArray();
                String[] weeks = new String[os.length];
                for (int i = 0; i < os.length; i++) weeks[i] = (String)os[i];
                new MaterialDialog.Builder(this)
                        .title("周选择")
                        .items(weeks)
                        .alwaysCallSingleChoiceCallback()
                        .itemsCallbackSingleChoice(last4Week, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(final MaterialDialog dialog, View view, final int which, CharSequence text) {
                                dialog.dismiss();
                                String[] weeks = new String[4];
                                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                                for (int i = 0; i < 4; i++) {
                                    weeks[i] = "从" + format1.format((BraecoWaiterUtils.MONDAYS.get(which) - (4 - i) * BraecoWaiterUtils.ONE_WEEK_LONG  + 1) * 1000)
                                            + "到" + format1.format((BraecoWaiterUtils.MONDAYS.get(which) - (3 - i) * BraecoWaiterUtils.ONE_WEEK_LONG) * 1000);
                                }
                                new MaterialDialog.Builder(mContext)
                                        .title("周选择")
                                        .items(weeks)
                                        .itemsCallbackSingleChoice(lastWeek, new MaterialDialog.ListCallbackSingleChoice() {
                                            @Override
                                            public boolean onSelection(MaterialDialog dialog, View view, int which2, CharSequence text) {
                                                if (which == last4Week && which2 == lastWeek) return true;
                                                EN = BraecoWaiterUtils.MONDAYS.get(which) - (3 - which2) * BraecoWaiterUtils.ONE_WEEK_LONG;
                                                ST = EN - BraecoWaiterUtils.ONE_WEEK_LONG + 1;
                                                MEMBERSHIP_TIME = EN;
                                                MEMBERSHIP_UNIT = "WEEK";
                                                getData(false);
                                                return true;
                                            }
                                        })
                                        .negativeText("取消")
                                        .onAny(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                                if (dialogAction == DialogAction.NEGATIVE) {
                                                    materialDialog.dismiss();
                                                }
                                            }
                                        })
                                        .show();
                                return true;
                            }
                        })
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                if (dialogAction == DialogAction.NEGATIVE) {
                                    materialDialog.dismiss();
                                }
                            }
                        })
                        .show();
                break;
            case 2:
                String[] months = new String[(MAX_YEAR - MIN_YEAR + 1) * 12];
                for (int i = 0; i < months.length; i++) {
                    months[i] = (MIN_YEAR + i / 12) + "年" + ((i % 12 + 1) < 10 ? "0" + (i % 12 + 1) : (i % 12 + 1)) + "月";
                }
                new MaterialDialog.Builder(this)
                        .title("月选择")
                        .items(months)
                        .alwaysCallSingleChoiceCallback()
                        .itemsCallbackSingleChoice(last6Month * 6 + lastSMonth, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(final MaterialDialog dialog, View view, final int which, CharSequence text) {
                                if (last6Month == which / 6 && lastSMonth == which % 6) return true;
                                dialog.dismiss();
                                int year = which / 12 + MeFragmentData.MIN_YEAR;
                                int month = which % 12 + 1;
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, month - 1, 1, 0, 0, 0);
                                calendar.add(Calendar.SECOND, 0);
                                ST = calendar.getTimeInMillis() / 1000;
                                int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                                calendar.set(Calendar.DAY_OF_MONTH, day);
                                calendar.add(Calendar.SECOND, 0);
                                EN = calendar.getTimeInMillis() / 1000;
                                MEMBERSHIP_TIME = EN;
                                MEMBERSHIP_UNIT = "MONTH";
                                getData(false);
                                return true;
                            }
                        })
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                if (dialogAction == DialogAction.NEGATIVE) {
                                    materialDialog.dismiss();
                                }
                            }
                        })
                        .show();
                break;
        }
    }

    private void getData(boolean changeType) {
        // set the 5 params and get data
        if (changeType) {
            switch (timeType) {
                case 0:
                    if (lastYear == -1 || lastMonth == -1 || lastDay == -1) {
                        // first time to day view
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);
                        calendar.add(Calendar.SECOND, 0);
                        EN = calendar.getTimeInMillis() / 1000;
                    } else {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(lastYear, lastMonth - 1, lastDay, 23, 59, 59);
                        EN = calendar.getTimeInMillis() / 1000;
                    }
                    ST = EN - 24 * 60 * 60 + 1;
                    MEMBERSHIP_TIME = EN;
                    MEMBERSHIP_UNIT = "DAY";
                    break;
                case 1:
                    if (last4Week == -1 || lastWeek == -1) {
                        // first time to week view
                        Calendar calendar = Calendar.getInstance();
                        long t = calendar.getTimeInMillis() / 1000;
                        BraecoWaiterUtils.createMondays();
                        for (int i = 0; i < BraecoWaiterUtils.MONDAYS.size(); i++) {
                            if (t <= BraecoWaiterUtils.MONDAYS.get(i)) {
                                for (int j = 0; j < 4; j++) {
                                    if (t <= BraecoWaiterUtils.MONDAYS.get(i) - (3 - j) * BraecoWaiterUtils.ONE_WEEK_LONG) {
                                        EN = BraecoWaiterUtils.MONDAYS.get(i) - (3 - j) * BraecoWaiterUtils.ONE_WEEK_LONG;
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    } else {
                        EN = BraecoWaiterUtils.MONDAYS.get(last4Week) - (3 - lastWeek) * BraecoWaiterUtils.ONE_WEEK_LONG;
                    }
                    ST = EN - BraecoWaiterUtils.ONE_WEEK_LONG + 1;
                    MEMBERSHIP_TIME = EN;
                    MEMBERSHIP_UNIT = "WEEK";
                    break;
                case 2:
                    if (last6Month == -1 || lastSMonth == -1) {
                        // first time to month view
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0, 0);
                        calendar.add(Calendar.SECOND, 0);
                        ST = calendar.getTimeInMillis() / 1000;
                        int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                        calendar.set(Calendar.DAY_OF_MONTH, day);
                        calendar.add(Calendar.SECOND, 0);
                        EN = calendar.getTimeInMillis() / 1000;
                    } else {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(last6Month / 2 + MIN_YEAR, lastSMonth + (last6Month % 2) * 6, 1, 0, 0, 0);
                        calendar.add(Calendar.SECOND, 0);
                        ST = calendar.getTimeInMillis() / 1000;
                        int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                        calendar.set(Calendar.DAY_OF_MONTH, day);
                        calendar.add(Calendar.SECOND, 0);
                        EN = calendar.getTimeInMillis() / 1000;
                    }
                    MEMBERSHIP_TIME = EN;
                    MEMBERSHIP_UNIT = "MONTH";
                    break;
            }
        }

        // init
        ORDERS_NUM = 0;
        PROFIT = 0;
        TYPE_PROFITS = new ArrayList<>();
        MEALS.clear();
        VIPS = new ArrayList<>();

        // judge whether the time is valid
        Calendar calendar = Calendar.getInstance();
        if (ST > calendar.getTimeInMillis() / 1000 + 30 * 60) {
            // if the time if behind half an hour future
            ORDERS_NUM = 0;
            PROFIT = 0;
            MEALS.clear();
        }


        // set the 3 fragments data to a progress
        if (BraecoWaiterApplication.dayProfitFragment != null)
            BraecoWaiterApplication.dayProfitFragment.ready();
        if (BraecoWaiterApplication.dayGoodsFragment != null)
            BraecoWaiterApplication.dayGoodsFragment.ready();
        if (BraecoWaiterApplication.dayVipFragment != null)
            BraecoWaiterApplication.dayVipFragment.ready();

        // unit
        // year
        // month
        // day
        // length
        // dir
        BraecoWaiterApplication.ME_FRAGMENT_DATA_TASK_NUM++;
        if (timeType == 0) {
            // day
            Calendar dayCalendar = Calendar.getInstance();
            dayCalendar.setTime(new Date(EN * 1000));
            new GetAllData(BraecoWaiterApplication.ME_FRAGMENT_DATA_TASK_NUM)
                    .execute("http://brae.co/Dinner/Manage/Statistic/All",
                            "day",
                            dayCalendar.get(Calendar.YEAR) + "",
                            (dayCalendar.get(Calendar.MONTH) + 1) + "",
                            dayCalendar.get(Calendar.DAY_OF_MONTH) + "",
                            7 + "",
                            0 + "");
        } else if (timeType == 1) {
            // week
            Calendar weekCalendar = Calendar.getInstance();
            weekCalendar.setTime(new Date(EN * 1000));
            nYear = weekCalendar.get(Calendar.YEAR);
            nMonth = weekCalendar.get(Calendar.MONTH) + 1;
            nDay = weekCalendar.get(Calendar.DAY_OF_MONTH);
            Log.d("BraecoWaiter", "POST: " + nYear + " " + nMonth + " " + nDay);
            new GetAllData(BraecoWaiterApplication.ME_FRAGMENT_DATA_TASK_NUM)
                    .execute("http://brae.co/Dinner/Manage/Statistic/All",
                            "week",
                            nYear + "",
                            nMonth + "",
                            nDay + "",
                            4 + "",
                            0 + "");
        } else if (timeType == 2) {
            // month
            Calendar monthCalendar = Calendar.getInstance();
            monthCalendar.setTime(new Date(EN * 1000));
            oYear = monthCalendar.get(Calendar.YEAR);
            oMonth = monthCalendar.get(Calendar.MONTH) + 1;
            new GetAllData(BraecoWaiterApplication.ME_FRAGMENT_DATA_TASK_NUM)
                    .execute("http://brae.co/Dinner/Manage/Statistic/All",
                            "month",
                            oYear + "",
                            oMonth + "",
                            monthCalendar.get(Calendar.DAY_OF_MONTH) + "",
                            6 + "",
                            0 + "");
        }

        // set the last params
        switch (timeType) {
            case 0:
                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(EN * 1000);
                calendar.add(Calendar.SECOND, 0);
                lastYear = calendar.get(Calendar.YEAR);
                lastMonth = calendar.get(Calendar.MONTH) + 1;
                lastDay = calendar.get(Calendar.DAY_OF_MONTH);
                break;
            case 1:
                BraecoWaiterUtils.createMondays();
                for (int i = 0; i < BraecoWaiterUtils.MONDAYS.size(); i++) {
                    if (EN <= BraecoWaiterUtils.MONDAYS.get(i)) {
                        for (int j = 0; j < 4; j++) {
                            if (EN <= BraecoWaiterUtils.MONDAYS.get(i) - (3 - j) * BraecoWaiterUtils.ONE_WEEK_LONG) {
                                last4Week = i;
                                lastWeek = j;
                                break;
                            }
                        }
                        break;
                    }
                }
                break;
            case 2:
                int maxMonths = (MAX_YEAR - MIN_YEAR + 1) * 12;
                for (int i = 0; i < maxMonths; i++) {
                    Calendar monthCalendar = Calendar.getInstance();
                    monthCalendar.set(MIN_YEAR + i / 12, i % 12, 1, 0, 0, 0);
                    monthCalendar.add(Calendar.SECOND, 0);
                    int days = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    monthCalendar.set(Calendar.DAY_OF_MONTH, days);
                    monthCalendar.add(Calendar.SECOND, 0);
                    if (EN <= monthCalendar.getTimeInMillis() / 1000) {
                        last6Month = i / 6;
                        lastSMonth = i % 6;
                        break;
                    }
                }
                break;
        }

        updateViewPager();
    }

    private class GetAllData extends AsyncTask<String, Void, String> {

        private int task;

        public GetAllData(int task) {
            loadSuccessfully = false;
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
                if (BuildConfig.DEBUG) {
                    Log.d("BraecoWaiter", "get all data: ");
                    BraecoWaiterUtils.longInfo("BraecoWaiter", result);
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(new BasicNameValuePair("unit", params[1]));
            pairList.add(new BasicNameValuePair("year", params[2]));
            pairList.add(new BasicNameValuePair("month", params[3]));
            pairList.add(new BasicNameValuePair("day", params[4]));
            pairList.add(new BasicNameValuePair("length", params[5]));
            pairList.add(new BasicNameValuePair("dir", params[6]));
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

            if (task != BraecoWaiterApplication.ME_FRAGMENT_DATA_TASK_NUM) return;

            if (BuildConfig.DEBUG) {
                Log.d("BraecoWaiter", "get all data: ");
                BraecoWaiterUtils.longInfo("BraecoWaiter", result);
            }

            if (result != null && BraecoWaiterApplication.dayProfitFragment != null) {
                JSONObject array;
                try {
                    array = new JSONObject(result);
                    if ("success".equals(array.getString("message"))) {

                        loadSuccessfully = true;

                        if (timeType == 0) {
                            dealDayData(array);
                        } else if (timeType == 1) {
                            dealWeekData(array);
                        } else if (timeType == 2) {
                            dealMonthData(array);
                        }

                    } else {
                        BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                    }

                } catch (JSONException e) {
                    BraecoWaiterUtils.showToast(mContext, "网络连接失败");
                    if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentData json error");
                    e.printStackTrace();
                }
            } else {
                BraecoWaiterUtils.showToast(mContext, "网络连接失败");
            }
        }
    }

    private void dealDayData(JSONObject data) {
        try {
            String currentDayString = "";
            currentDayString += mYear + "-";
            currentDayString += mMonth + "-";
            currentDayString += mDay;

            Log.d("BraecoWaiter", currentDayString);

            JSONArray jsonSeparated = data.getJSONArray("separated");
            JSONObject jsonCurrentDay = null;
            if (jsonSeparated.length() > 0)
                jsonCurrentDay = jsonSeparated.getJSONObject(jsonSeparated.length() - 1);
            int mealSum = 0;
            String mealBest = "";
            if (jsonCurrentDay == null
                || !currentDayString.equals(jsonCurrentDay.getString("date"))) {
                // the selected day is after now
                ORDERS_NUM = 0;
                PROFIT = 0;
                TYPE_PROFITS = new ArrayList<>();
                mealSum = 0;
                mealBest = "暂无数据";
                MEALS = new ArrayList<>();
            } else {
                PROFIT = jsonCurrentDay.getDouble("price");
                ORDERS_NUM = jsonCurrentDay.getInt("sum_order");
                MEALS = new ArrayList<>();
                JSONArray jsonProfits = jsonCurrentDay.getJSONArray("channel_detail");
                for (int i = 0; i < jsonProfits.length(); i++) {
                    JSONObject jsonProfit = jsonProfits.getJSONObject(i);
                    Map<String, Object> profit = new HashMap<>();
                    profit.put("rate", jsonProfit.getDouble("rate"));
                    profit.put("amount", jsonProfit.getDouble("amount"));
                    profit.put("channel", jsonProfit.getString("channel"));
                    TYPE_PROFITS.add(profit);
                }
                JSONArray jsonRanks = jsonCurrentDay.getJSONArray("rank");
                for (int i = 0; i < jsonRanks.length(); i++) {
                    JSONObject jsonRank = jsonRanks.getJSONObject(i);
                    Map<String, Object> meal = new HashMap<>();
                    meal.put("sum", jsonRank.getInt("sum"));
                    meal.put("price", jsonRank.getDouble("price"));
                    meal.put("name", jsonRank.getString("name"));
                    MEALS.add(meal);
                    mealSum += (Integer)meal.get("sum");
                }
                if (MEALS.size() > 0) mealBest = MEALS.get(0).get("name") + "";
            }

            PROFITS = new ArrayList<>();
            FEES = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            calendar.set(mYear, mMonth - 1, mDay, 0, 0, 0);
            calendar.add(Calendar.SECOND, 0);
            int j = jsonSeparated.length() - 1;
            for (int i = 6; i >= 0; i--) {
                currentDayString = String.format("%4d", calendar.get(Calendar.YEAR)) + "-" +
                        (calendar.get(Calendar.MONTH) + 1) + "-" +
                        (calendar.get(Calendar.DAY_OF_MONTH));
                Map<String, Object> vip = new HashMap<>();
                JSONObject jsonVip = null;
                if (j >= 0) jsonVip = jsonSeparated.getJSONObject(j);
                if (jsonVip == null || !currentDayString.equals(jsonVip.getString("date"))) {
                    vip.put("toast", (calendar.get(Calendar.MONTH) + 1) + "月"
                            + calendar.get(Calendar.DAY_OF_MONTH) + "日");
                    vip.put("axis", calendar.get(Calendar.DAY_OF_MONTH) + "日");
                    vip.put("new_member", 0);
                    vip.put("membership_charge", 0.0);
                    VIPS.add(vip);
                    PROFITS.add(0.0);
                    FEES.add(0.0);
                } else {
                    vip.put("toast", (calendar.get(Calendar.MONTH) + 1) + "月"
                            + calendar.get(Calendar.DAY_OF_MONTH) + "日");
                    vip.put("axis", calendar.get(Calendar.DAY_OF_MONTH) + "日");
                    vip.put("new_member", jsonVip.getInt("new_member"));
                    vip.put("membership_charge", jsonVip.getDouble("membership_charge"));
                    VIPS.add(vip);
                    PROFITS.add(jsonVip.getDouble("price"));
                    FEES.add(jsonVip.getDouble("fee"));
                    j--;
                }
                calendar.add(Calendar.DATE, -1);
            }
            Collections.reverse(VIPS);
            Collections.reverse(PROFITS);
            Collections.reverse(FEES);

            if (BraecoWaiterApplication.dayProfitFragment != null)
                BraecoWaiterApplication.dayProfitFragment.drawChart(ORDERS_NUM, PROFIT, TYPE_PROFITS, PROFITS, FEES, VIPS);
            if (BraecoWaiterApplication.dayGoodsFragment != null)
                BraecoWaiterApplication.dayGoodsFragment.drawChart(mealSum, mealBest, MEALS);
            if (BraecoWaiterApplication.dayVipFragment != null)
                BraecoWaiterApplication.dayVipFragment.drawChart(VIPS);

        } catch (JSONException j) {
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentData json error");
            j.printStackTrace();
        }
    }

    private void dealWeekData(JSONObject data) {
        try {
            String currentWeekString = "";
            currentWeekString = String.format("%4d", nYear) + "-" + nMonth + "-" + nDay;

            Log.d("BraecoWaiter", currentWeekString);

            JSONArray jsonSeparated = data.getJSONArray("separated");
            JSONObject jsonCurrentWeek = null;
            if (jsonSeparated.length() > 0)
                jsonCurrentWeek = jsonSeparated.getJSONObject(jsonSeparated.length() - 1);
            int mealSum = 0;
            String mealBest = "";
            if (jsonCurrentWeek == null
                    || !inRange(currentWeekString, jsonCurrentWeek.getString("date"))) {
                // the selected day is after now
                ORDERS_NUM = 0;
                PROFIT = 0;
                TYPE_PROFITS = new ArrayList<>();
                mealSum = 0;
                mealBest = "暂无数据";
                MEALS = new ArrayList<>();
            } else {
                PROFIT = jsonCurrentWeek.getDouble("price");
                ORDERS_NUM = jsonCurrentWeek.getInt("sum_order");
                MEALS = new ArrayList<>();
                JSONArray jsonProfits = jsonCurrentWeek.getJSONArray("channel_detail");
                for (int i = 0; i < jsonProfits.length(); i++) {
                    JSONObject jsonProfit = jsonProfits.getJSONObject(i);
                    Map<String, Object> profit = new HashMap<>();
                    profit.put("rate", jsonProfit.getDouble("rate"));
                    profit.put("amount", jsonProfit.getDouble("amount"));
                    profit.put("channel", jsonProfit.getString("channel"));
                    TYPE_PROFITS.add(profit);
                }
                JSONArray jsonRanks = jsonCurrentWeek.getJSONArray("rank");
                for (int i = 0; i < jsonRanks.length(); i++) {
                    JSONObject jsonRank = jsonRanks.getJSONObject(i);
                    Map<String, Object> meal = new HashMap<>();
                    meal.put("sum", jsonRank.getInt("sum"));
                    meal.put("price", jsonRank.getDouble("price"));
                    meal.put("name", jsonRank.getString("name"));
                    MEALS.add(meal);
                    mealSum += (Integer)meal.get("sum");
                }
                if (MEALS.size() > 0) mealBest = MEALS.get(0).get("name") + "";
            }

            PROFITS = new ArrayList<>();
            FEES = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            calendar.set(nYear, nMonth - 1, nDay, 0, 0, 0);
            calendar.add(Calendar.SECOND, 0);
            int j = jsonSeparated.length() - 1;
            for (int i = 3; i >= 0; i--) {
                currentWeekString = String.format("%4d", calendar.get(Calendar.YEAR)) + "-"
                        + ((calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
                Map<String, Object> vip = new HashMap<>();
                JSONObject jsonVip = null;
                if (j >= 0) jsonVip = jsonSeparated.getJSONObject(j);
                if (jsonVip == null
                        || !inRange(currentWeekString, jsonVip.getString("date"))) {
//                    Log.d("BraecoWaiter", "Not in range");
                    vip.put("toast", getToastFromPoint(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
                    vip.put("axis", getAxisFromPoint(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
                    vip.put("new_member", 0);
                    vip.put("membership_charge", 0.0);
                    VIPS.add(vip);
                    PROFITS.add(0.0);
                    FEES.add(0.0);
                } else {
//                    Log.d("BraecoWaiter", "In range");
                    vip.put("toast", getToastFromRange(jsonVip.getString("date")));
                    vip.put("axis", getAxisFromRange(jsonVip.getString("date")));
                    vip.put("new_member", jsonVip.getInt("new_member"));
                    vip.put("membership_charge", jsonVip.getDouble("membership_charge"));
                    VIPS.add(vip);
                    PROFITS.add(jsonVip.getDouble("price"));
                    FEES.add(jsonVip.getDouble("fee"));
                    j--;
                }
                calendar.add(Calendar.DATE, -7);
            }
            Collections.reverse(VIPS);
            Collections.reverse(PROFITS);
            Collections.reverse(FEES);

            if (BraecoWaiterApplication.dayProfitFragment != null)
                BraecoWaiterApplication.dayProfitFragment.drawChart(ORDERS_NUM, PROFIT, TYPE_PROFITS, PROFITS, FEES, VIPS);
            if (BraecoWaiterApplication.dayGoodsFragment != null)
                BraecoWaiterApplication.dayGoodsFragment.drawChart(mealSum, mealBest, MEALS);
            if (BraecoWaiterApplication.dayVipFragment != null)
                BraecoWaiterApplication.dayVipFragment.drawChart(VIPS);

        } catch (JSONException j) {
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentData json error");
            j.printStackTrace();
        }
    }

    private void dealMonthData(JSONObject data) {
        try {
            String currentMonthString = "";
            currentMonthString = String.format("%4d", oYear) + "-" + oMonth;

            Log.d("BraecoWaiter", currentMonthString);

            JSONArray jsonSeparated = data.getJSONArray("separated");
            JSONObject jsonCurrentMonth = null;
            if (jsonSeparated.length() > 0)
                jsonCurrentMonth = jsonSeparated.getJSONObject(jsonSeparated.length() - 1);
            int mealSum = 0;
            String mealBest = "";
            if (jsonCurrentMonth == null
                    || !currentMonthString.equals(jsonCurrentMonth.getString("date"))) {
                // the selected day is after now
                ORDERS_NUM = 0;
                PROFIT = 0;
                TYPE_PROFITS = new ArrayList<>();
                mealSum = 0;
                mealBest = "暂无数据";
                MEALS = new ArrayList<>();
            } else {
                PROFIT = jsonCurrentMonth.getDouble("price");
                ORDERS_NUM = jsonCurrentMonth.getInt("sum_order");
                MEALS = new ArrayList<>();
                JSONArray jsonProfits = jsonCurrentMonth.getJSONArray("channel_detail");
                for (int i = 0; i < jsonProfits.length(); i++) {
                    JSONObject jsonProfit = jsonProfits.getJSONObject(i);
                    Map<String, Object> profit = new HashMap<>();
                    profit.put("rate", jsonProfit.getDouble("rate"));
                    profit.put("amount", jsonProfit.getDouble("amount"));
                    profit.put("channel", jsonProfit.getString("channel"));
                    TYPE_PROFITS.add(profit);
                }
                JSONArray jsonRanks = jsonCurrentMonth.getJSONArray("rank");
                for (int i = 0; i < jsonRanks.length(); i++) {
                    JSONObject jsonRank = jsonRanks.getJSONObject(i);
                    Map<String, Object> meal = new HashMap<>();
                    meal.put("sum", jsonRank.getInt("sum"));
                    meal.put("price", jsonRank.getDouble("price"));
                    meal.put("name", jsonRank.getString("name"));
                    MEALS.add(meal);
                    mealSum += (Integer)meal.get("sum");
                }
                if (MEALS.size() > 0) mealBest = MEALS.get(0).get("name") + "";
            }

            PROFITS = new ArrayList<>();
            FEES = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            calendar.set(oYear, oMonth - 1, 1, 0, 0, 0);
            calendar.add(Calendar.SECOND, 0);
            int j = jsonSeparated.length() - 1;
            for (int i = 5; i >= 0; i--) {
                currentMonthString = String.format("%4d", calendar.get(Calendar.YEAR)) + "-"
                        + ((calendar.get(Calendar.MONTH) + 1));
                Map<String, Object> vip = new HashMap<>();
                JSONObject jsonVip = null;
                if (j >= 0) jsonVip = jsonSeparated.getJSONObject(j);
                if (jsonVip == null || !currentMonthString.equals(jsonVip.getString("date"))) {
                    vip.put("toast", calendar.get(Calendar.YEAR) + "年"
                            + (calendar.get(Calendar.MONTH) + 1) + "月");
                    vip.put("axis", (calendar.get(Calendar.MONTH) + 1) + "月");
                    vip.put("new_member", 0);
                    vip.put("membership_charge", 0.0);
                    VIPS.add(vip);
                    PROFITS.add(0.0);
                    FEES.add(0.0);
                } else {
                    vip.put("toast", calendar.get(Calendar.YEAR) + "年"
                            + (calendar.get(Calendar.MONTH) + 1) + "月");
                    vip.put("axis", (calendar.get(Calendar.MONTH) + 1) + "月");
                    vip.put("new_member", jsonVip.getInt("new_member"));
                    vip.put("membership_charge", jsonVip.getDouble("membership_charge"));
                    VIPS.add(vip);
                    PROFITS.add(jsonVip.getDouble("price"));
                    FEES.add(jsonVip.getDouble("fee"));
                    j--;
                }
                calendar.add(Calendar.MONTH, -1);
            }
            Collections.reverse(VIPS);
            Collections.reverse(PROFITS);
            Collections.reverse(FEES);

            if (BraecoWaiterApplication.dayProfitFragment != null)
                BraecoWaiterApplication.dayProfitFragment.drawChart(ORDERS_NUM, PROFIT, TYPE_PROFITS, PROFITS, FEES, VIPS);
            if (BraecoWaiterApplication.dayGoodsFragment != null)
                BraecoWaiterApplication.dayGoodsFragment.drawChart(mealSum, mealBest, MEALS);
            if (BraecoWaiterApplication.dayVipFragment != null)
                BraecoWaiterApplication.dayVipFragment.drawChart(VIPS);

        } catch (JSONException j) {
            if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "MeFragmentData json error");
            j.printStackTrace();
        }
    }

    private void updateViewPager() {
        // once the data is received, update the time viewpager
        // including change pagers and set the selected and the time text
        switch (timeType) {
            case 0:
                Calendar nowDate = Calendar.getInstance();
                Calendar fromDate = Calendar.getInstance();
                nowDate.set(mYear, mMonth - 1, mDay);
                nowDate.add(Calendar.SECOND, 0);
                fromDate.set(1900, 1, 5, 0, 0, 0);
                fromDate.add(Calendar.SECOND, 0);

                int days = (int)((nowDate.getTimeInMillis() - fromDate.getTimeInMillis()) / (24 * 60 * 60 * 1000));

                BraecoWaiterApplication.selectedDate[0] = mYear;
                BraecoWaiterApplication.selectedDate[1] = mMonth;
                BraecoWaiterApplication.selectedDate[2] = mDay;

                weekPagerAdapter = new WeekPagerAdapter(this);
                viewpager.setAdapter(weekPagerAdapter);
                viewpager.setOffscreenPageLimit(1);
                viewpager.setCurrentItem(days / 7, false);

                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = format1.format(nowDate.getTime()) + "，"
                        + WEEK_NAME[nowDate.get(Calendar.DAY_OF_WEEK) - 1];

                date.setText(dateString);
                title.setText("日结");
                print.setVisibility(View.VISIBLE);
                break;
            case 1:
                BraecoWaiterApplication.selectedWeek = lastWeek;
                monthPagerAdapter = new MonthPagerAdapter(this);
                viewpager.setAdapter(monthPagerAdapter);
                viewpager.setCurrentItem(last4Week);

                Calendar st = Calendar.getInstance();
                Calendar en = Calendar.getInstance();
                st.setTime(new Date(ST * 1000));
                en.setTime(new Date(EN * 1000));
                format1 = new SimpleDateFormat("yyyy-MM-dd");
                dateString = "从" + format1.format(st.getTime())
                        + "到" + format1.format(en.getTime());
                date.setText(dateString);
                title.setText("周结");
                print.setVisibility(View.INVISIBLE);
                break;
            case 2:
                BraecoWaiterApplication.selectedMonth = (oMonth - 1) % 6;
                Calendar now = Calendar.getInstance();
                yearPagerAdapter = new YearPagerAdapter(this);
                viewpager.setAdapter(yearPagerAdapter);
                viewpager.setCurrentItem((oYear - MIN_YEAR) * 2 + (oMonth - 1) / 6);

                date.setText(oYear + "年" + oMonth + "月");

                title.setText("月结");
                print.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void print() {
        Timer tExit = null;
        if (printing == false) {
            printing = true;
            new PostSmallTicketAsyncTask(mOnPostSmallTicketAsyncTaskListener, mYear, mMonth, mDay).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            BraecoWaiterUtils.showToast(mContext, "打印" + mMonth + "月" + mDay + "日小票中……");
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    printing = false;
                }
            }, 10000);
        } else {
            BraecoWaiterUtils.showToast(mContext, "已在打印，请稍候再试");
        }
    }

    private OnPostSmallTicketAsyncTaskListener mOnPostSmallTicketAsyncTaskListener = new OnPostSmallTicketAsyncTaskListener() {
        @Override
        public void success() {

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
    public void onDateSet(@Nullable DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        // if the date is the same as the last date, and the last data is loaded successfully, just return
        if (year == lastYear
                && monthOfYear == lastMonth - 1
                && lastDay == dayOfMonth
                && loadSuccessfully) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth, 23, 59, 59);
        calendar.add(Calendar.SECOND, 0);
        EN = calendar.getTimeInMillis() / 1000;
        ST = EN - 24 * 60 * 60 + 1;
        MEMBERSHIP_TIME = EN;
        MEMBERSHIP_UNIT = "DAY";
        getData(false);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BraecoWaiterApplication.DATA_STATICS_FINISH = true;
    }

    private String getToastFromPoint(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, 0, 0, 0);
        calendar.add(Calendar.SECOND, 0);

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2014, 11, 30, 0, 0, 0);
        startCalendar.add(Calendar.SECOND, 0);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(2020, 0, 1, 0, 0, 0);
        endCalendar.add(Calendar.SECOND, 0);

        while (endCalendar.after(startCalendar)) {
            if (calendar.before(startCalendar)) {
                Calendar lowCalendar = (Calendar) startCalendar.clone();
                lowCalendar.add(Calendar.DATE, -7);
                lowCalendar.add(Calendar.SECOND, 1);
                startCalendar.add(Calendar.DATE, -1);
                return (lowCalendar.get(Calendar.MONTH) + 1) + "月" + lowCalendar.get(Calendar.DAY_OF_MONTH) + "日~"
                        + (startCalendar.get(Calendar.MONTH) + 1) + "月" + startCalendar.get(Calendar.DAY_OF_MONTH) + "日";
            }
            startCalendar.add(Calendar.DATE, 7);
        }
        return "";
    }

    private String getAxisFromPoint(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, 0, 0, 0);
        calendar.add(Calendar.SECOND, 0);

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2014, 11, 30, 0, 0, 0);
        startCalendar.add(Calendar.SECOND, 0);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(2020, 0, 1, 0, 0, 0);
        endCalendar.add(Calendar.SECOND, 0);

        while (endCalendar.after(startCalendar)) {
            if (calendar.before(startCalendar)) {
                Calendar lowCalendar = (Calendar) startCalendar.clone();
                lowCalendar.add(Calendar.DATE, -7);
                lowCalendar.add(Calendar.SECOND, 1);
                startCalendar.add(Calendar.DATE, -1);
                return (lowCalendar.get(Calendar.MONTH) + 1) + "." + lowCalendar.get(Calendar.DAY_OF_MONTH) + "~"
                        + (startCalendar.get(Calendar.MONTH) + 1) + "." + startCalendar.get(Calendar.DAY_OF_MONTH);
            }
            startCalendar.add(Calendar.DATE, 7);
        }
        return "";
    }

    private String getToastFromRange(String range) {
        Calendar lowCalendar = stringToCalendar(range.substring(0, range.lastIndexOf("~") - 1));
        Calendar highCalendar = stringToCalendar(range.substring(range.lastIndexOf("~") + 2, range.length()));
        highCalendar.add(Calendar.DATE, -1);
        return (lowCalendar.get(Calendar.MONTH) + 1) + "月" + lowCalendar.get(Calendar.DAY_OF_MONTH) + "日~"
                + (highCalendar.get(Calendar.MONTH) + 1) + "月" + highCalendar.get(Calendar.DAY_OF_MONTH) + "日";
    }

    private String getAxisFromRange(String range) {
        Calendar lowCalendar = stringToCalendar(range.substring(0, range.lastIndexOf("~") - 1));
        Calendar highCalendar = stringToCalendar(range.substring(range.lastIndexOf("~") + 2, range.length()));
        highCalendar.add(Calendar.DATE, -1);
        return (lowCalendar.get(Calendar.MONTH) + 1) + "." + lowCalendar.get(Calendar.DAY_OF_MONTH) + "~"
                + (highCalendar.get(Calendar.MONTH) + 1) + "." + highCalendar.get(Calendar.DAY_OF_MONTH);
    }

    private boolean inRange(String now, String range) {
        Calendar nowCalendar = stringToCalendar(now);
        Calendar lowCalendar = stringToCalendar(range.substring(0, range.lastIndexOf("~") - 1));
        Calendar highCalendar = stringToCalendar(range.substring(range.lastIndexOf("~") + 2, range.length()));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        Log.d("BraecoWaiter", format.format(nowCalendar.getTime()) + " "
//                + format.format(lowCalendar.getTime()) + " "
//                + format.format(highCalendar.getTime()));
        return (!nowCalendar.before(lowCalendar) && nowCalendar.before(highCalendar));
    }

    private Calendar stringToCalendar(String string) {
        string = "-" + string;
        int st = 0;
        int ed = 0;
        int[] nums = new int[3];
        int k = 0;
        for (int i = 0; i < string.length(); ) {
            if (string.charAt(i) == '-') {
                st = i + 1;
                for (ed = st + 1; ed < string.length(); ed++) {
                    if (string.charAt(ed) == '-') {
                        break;
                    }
                }
                nums[k++] = Integer.parseInt(string.substring(st, ed));
                i = ed;
            } else {
                i++;
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(nums[0], nums[1] - 1, nums[2], 0, 0, 0);
        calendar.add(Calendar.SECOND, 0);
        return calendar;
    }
}
