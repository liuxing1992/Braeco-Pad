package com.braeco.braecowaiter;

import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by Weiping on 2015/12/7.
 */

public class WeekPagerAdapter extends PagerAdapter {

    private OnDayClickListener onDayClickListener;

    WeekPagerAdapter(OnDayClickListener onDayClickListener) {
        this.onDayClickListener = onDayClickListener;
    }

    private TextView tv0, tv1, tv2, tv3, tv4, tv5, tv6;

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return o == view;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {

        LinearLayout ly = (LinearLayout) LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_fragment_week, null);
        container.addView(ly);

        int days = position * 7;
        int nowWeekDay;

        Calendar fromDate = Calendar.getInstance();
        nowWeekDay = fromDate.get(Calendar.DAY_OF_WEEK);
        if (nowWeekDay == 1) nowWeekDay = 6;
        else nowWeekDay -= 2;
        fromDate.set(1900, 1, 5, 0, 0, 0);
        fromDate.add(Calendar.SECOND, 0);

        fromDate.add(Calendar.DATE, days);
        int weekDay;
        if (fromDate.get(Calendar.DAY_OF_WEEK) == 1) {
            weekDay = 7;
        } else {
            weekDay = fromDate.get(Calendar.DAY_OF_WEEK) - 2;
        }
        fromDate.add(Calendar.DATE, weekDay);

        tv0 = (TextView) ly.findViewById(R.id.tv0);
        tv1 = (TextView) ly.findViewById(R.id.tv1);
        tv2 = (TextView) ly.findViewById(R.id.tv2);
        tv3 = (TextView) ly.findViewById(R.id.tv3);
        tv4 = (TextView) ly.findViewById(R.id.tv4);
        tv5 = (TextView) ly.findViewById(R.id.tv5);
        tv6 = (TextView) ly.findViewById(R.id.tv6);

        if (fromDate.get(Calendar.YEAR) == BraecoWaiterApplication.selectedDate[0]
                && fromDate.get(Calendar.MONTH) + 1 == BraecoWaiterApplication.selectedDate[1]
                && fromDate.get(Calendar.DAY_OF_MONTH) == BraecoWaiterApplication.selectedDate[2]) {
            nowWeekDay = 0;
        }
        tv0.setText(fromDate.get(Calendar.DAY_OF_MONTH) + "");
        fromDate.add(Calendar.DATE, 1);
        if (fromDate.get(Calendar.YEAR) == BraecoWaiterApplication.selectedDate[0]
                && fromDate.get(Calendar.MONTH) + 1 == BraecoWaiterApplication.selectedDate[1]
                && fromDate.get(Calendar.DAY_OF_MONTH) == BraecoWaiterApplication.selectedDate[2]) {
            nowWeekDay = 1;
        }
        tv1.setText(fromDate.get(Calendar.DAY_OF_MONTH) + "");
        fromDate.add(Calendar.DATE, 1);
        if (fromDate.get(Calendar.YEAR) == BraecoWaiterApplication.selectedDate[0]
                && fromDate.get(Calendar.MONTH) + 1 == BraecoWaiterApplication.selectedDate[1]
                && fromDate.get(Calendar.DAY_OF_MONTH) == BraecoWaiterApplication.selectedDate[2]) {
            nowWeekDay = 2;
        }
        tv2.setText(fromDate.get(Calendar.DAY_OF_MONTH) + "");
        fromDate.add(Calendar.DATE, 1);
        if (fromDate.get(Calendar.YEAR) == BraecoWaiterApplication.selectedDate[0]
                && fromDate.get(Calendar.MONTH) + 1 == BraecoWaiterApplication.selectedDate[1]
                && fromDate.get(Calendar.DAY_OF_MONTH) == BraecoWaiterApplication.selectedDate[2]) {
            nowWeekDay = 3;
        }
        tv3.setText(fromDate.get(Calendar.DAY_OF_MONTH) + "");
        fromDate.add(Calendar.DATE, 1);
        if (fromDate.get(Calendar.YEAR) == BraecoWaiterApplication.selectedDate[0]
                && fromDate.get(Calendar.MONTH) + 1 == BraecoWaiterApplication.selectedDate[1]
                && fromDate.get(Calendar.DAY_OF_MONTH) == BraecoWaiterApplication.selectedDate[2]) {
            nowWeekDay = 4;
        }
        tv4.setText(fromDate.get(Calendar.DAY_OF_MONTH) + "");
        fromDate.add(Calendar.DATE, 1);
        if (fromDate.get(Calendar.YEAR) == BraecoWaiterApplication.selectedDate[0]
                && fromDate.get(Calendar.MONTH) + 1 == BraecoWaiterApplication.selectedDate[1]
                && fromDate.get(Calendar.DAY_OF_MONTH) == BraecoWaiterApplication.selectedDate[2]) {
            nowWeekDay = 5;
        }
        tv5.setText(fromDate.get(Calendar.DAY_OF_MONTH) + "");
        fromDate.add(Calendar.DATE, 1);
        if (fromDate.get(Calendar.YEAR) == BraecoWaiterApplication.selectedDate[0]
                && fromDate.get(Calendar.MONTH) + 1 == BraecoWaiterApplication.selectedDate[1]
                && fromDate.get(Calendar.DAY_OF_MONTH) == BraecoWaiterApplication.selectedDate[2]) {
            nowWeekDay = 6;
        }
        tv6.setText(fromDate.get(Calendar.DAY_OF_MONTH) + "");

        tv0.setOnClickListener(new DayOnClickListener(tv0, tv1, tv2, tv3, tv4, tv5, tv6, 0));
        tv1.setOnClickListener(new DayOnClickListener(tv0, tv1, tv2, tv3, tv4, tv5, tv6, 1));
        tv2.setOnClickListener(new DayOnClickListener(tv0, tv1, tv2, tv3, tv4, tv5, tv6, 2));
        tv3.setOnClickListener(new DayOnClickListener(tv0, tv1, tv2, tv3, tv4, tv5, tv6, 3));
        tv4.setOnClickListener(new DayOnClickListener(tv0, tv1, tv2, tv3, tv4, tv5, tv6, 4));
        tv5.setOnClickListener(new DayOnClickListener(tv0, tv1, tv2, tv3, tv4, tv5, tv6, 5));
        tv6.setOnClickListener(new DayOnClickListener(tv0, tv1, tv2, tv3, tv4, tv5, tv6, 6));

        switch (nowWeekDay) {
            case 0 : tv0.setBackgroundResource(R.drawable.shape_circle); tv0.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 1 : tv1.setBackgroundResource(R.drawable.shape_circle); tv1.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 2 : tv2.setBackgroundResource(R.drawable.shape_circle); tv2.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 3 : tv3.setBackgroundResource(R.drawable.shape_circle); tv3.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 4 : tv4.setBackgroundResource(R.drawable.shape_circle); tv4.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 5 : tv5.setBackgroundResource(R.drawable.shape_circle); tv5.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 6 : tv6.setBackgroundResource(R.drawable.shape_circle); tv6.setTextColor(Color.parseColor("#FFFFFF")); break;
        }

        return ly;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //cast to LinearLayout
        container.removeView((LinearLayout)object);
    }

    public interface OnDayClickListener {
        void OnDayClick(int p);
    }

    class DayOnClickListener implements View.OnClickListener {

        private int p = 0;
        private TextView tv0, tv1, tv2, tv3, tv4, tv5, tv6;

        DayOnClickListener(
                TextView tv0,
                TextView tv1,
                TextView tv2,
                TextView tv3,
                TextView tv4,
                TextView tv5,
                TextView tv6, int p) {
            this.tv0 = tv0;
            this.tv1 = tv1;
            this.tv2 = tv2;
            this.tv3 = tv3;
            this.tv4 = tv4;
            this.tv5 = tv5;
            this.tv6 = tv6;
            this.p = p;
        }

        @Override
        public void onClick(View v) {
            onDayClickListener.OnDayClick(p);
        }
    }

    public void select(int p) {
        Log.d("BraecoWaiter", p + "");
        tv0.setBackgroundResource(0);
        tv1.setBackgroundResource(0);
        tv2.setBackgroundResource(0);
        tv3.setBackgroundResource(0);
        tv4.setBackgroundResource(0);
        tv5.setBackgroundResource(0);
        tv6.setBackgroundResource(0);
        tv0.setTextColor(Color.parseColor("#000000"));
        tv1.setTextColor(Color.parseColor("#000000"));
        tv2.setTextColor(Color.parseColor("#000000"));
        tv3.setTextColor(Color.parseColor("#000000"));
        tv4.setTextColor(Color.parseColor("#000000"));
        tv5.setTextColor(Color.parseColor("#000000"));
        tv6.setTextColor(Color.parseColor("#000000"));
        switch (p) {
            case 0 : tv0.setBackgroundResource(R.drawable.shape_circle); tv0.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 1 : tv1.setBackgroundResource(R.drawable.shape_circle); tv1.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 2 : tv2.setBackgroundResource(R.drawable.shape_circle); tv2.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 3 : tv3.setBackgroundResource(R.drawable.shape_circle); tv3.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 4 : tv4.setBackgroundResource(R.drawable.shape_circle); tv4.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 5 : tv5.setBackgroundResource(R.drawable.shape_circle); tv5.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 6 : tv6.setBackgroundResource(R.drawable.shape_circle); tv6.setTextColor(Color.parseColor("#FFFFFF")); break;
        }
    }
}
