package com.braeco.braecowaiter;

import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Weiping on 2015/12/14.
 */
public class MonthPagerAdapter extends PagerAdapter {

    private OnWeekClickListener onWeekClickListener;
    public static ArrayList<Long> MONDAYS = null;
    public static long FOUR_WEEK_LONG = 4 * 7 * 24 * 60 * 60;
    public static long ONE_WEEK_LONG = 7 * 24 * 60 * 60;

    MonthPagerAdapter(OnWeekClickListener onWeekClickListener) {
        this.onWeekClickListener = onWeekClickListener;
        createSundays();
    }

    public void createSundays() {
        if (MONDAYS != null) return;
        MONDAYS = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(2015, 0, 26, 23, 59, 59);
        calendar.add(Calendar.SECOND, 0);
        long startSunday = calendar.getTimeInMillis() / 1000;
        long fourWeeksLong = 4 * 7 * 24 * 60 * 60;
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(2050, 0, 1, 0, 0, 0);
        endCalendar.add(Calendar.SECOND, 0);
        long endSunday = endCalendar.getTimeInMillis() / 1000;
        while (startSunday < endSunday) {
            MONDAYS.add(startSunday);
            startSunday += fourWeeksLong;
        }
    }

    private TextView tv0, tv1, tv2, tv3;

    @Override
    public int getCount() {
        return MONDAYS.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return o == view;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {

        int nowWeek = 0;
        if (BraecoWaiterApplication.selectedWeek != -1) {
            nowWeek = BraecoWaiterApplication.selectedWeek;
        } else {
            Calendar calendar = Calendar.getInstance();
            long t = calendar.getTimeInMillis() / 1000;
            for (int i = 0; i < MONDAYS.size(); i++) {
                if (t <= MONDAYS.get(i)) {
                    for (int j = 0; j < 4; j++) {
                        if (t <= MONDAYS.get(i) - (3 - j) * ONE_WEEK_LONG) {
                            nowWeek = j;
                            break;
                        }
                    }
                    break;
                }
            }
        }

        LinearLayout ly = (LinearLayout) LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_fragment_month, null);
        container.addView(ly);

        tv0 = (TextView) ly.findViewById(R.id.tv0);
        tv1 = (TextView) ly.findViewById(R.id.tv1);
        tv2 = (TextView) ly.findViewById(R.id.tv2);
        tv3 = (TextView) ly.findViewById(R.id.tv3);

        tv0.setOnClickListener(new WeekOnClickListener(tv0, tv1, tv2, tv3, position, 0));
        tv1.setOnClickListener(new WeekOnClickListener(tv0, tv1, tv2, tv3, position, 1));
        tv2.setOnClickListener(new WeekOnClickListener(tv0, tv1, tv2, tv3, position, 2));
        tv3.setOnClickListener(new WeekOnClickListener(tv0, tv1, tv2, tv3, position, 3));

        switch (nowWeek) {
            case 0 : tv0.setBackgroundResource(R.drawable.shape_circle); tv0.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 1 : tv1.setBackgroundResource(R.drawable.shape_circle); tv1.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 2 : tv2.setBackgroundResource(R.drawable.shape_circle); tv2.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 3 : tv3.setBackgroundResource(R.drawable.shape_circle); tv3.setTextColor(Color.parseColor("#FFFFFF")); break;
        }

        return ly;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //cast to LinearLayout
        container.removeView((LinearLayout)object);
    }

    public interface OnWeekClickListener {
        void OnWeekClick(long t);
    }

    class WeekOnClickListener implements View.OnClickListener {

        private int position = 0;
        private int p = 0;
        private TextView tv0, tv1, tv2, tv3;

        WeekOnClickListener(
                TextView tv0,
                TextView tv1,
                TextView tv2,
                TextView tv3,
                int position,
                int p) {
            this.tv0 = tv0;
            this.tv1 = tv1;
            this.tv2 = tv2;
            this.tv3 = tv3;
            this.position = position;
            this.p = p;
        }

        @Override
        public void onClick(View v) {
            onWeekClickListener.OnWeekClick(MONDAYS.get(position) - (3 - p) * ONE_WEEK_LONG);
        }
    }

}
