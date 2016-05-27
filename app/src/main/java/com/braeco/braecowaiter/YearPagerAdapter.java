package com.braeco.braecowaiter;

import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by Weiping on 2015/12/14.
 */
public class YearPagerAdapter extends PagerAdapter {

    private OnMonthClickListener onMonthClickListener;

    YearPagerAdapter(OnMonthClickListener onMonthClickListener) {
        this.onMonthClickListener = onMonthClickListener;
    }

    private TextView tv0, tv1, tv2, tv3, tv4, tv5;

    @Override
    public int getCount() {
        return (MeFragmentData.MAX_YEAR - MeFragmentData.MIN_YEAR + 1) * 2;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return o == view;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {

        int nowMonth = 0;
        if (BraecoWaiterApplication.selectedMonth != -1) {
            nowMonth = BraecoWaiterApplication.selectedMonth;
        } else {
            Calendar calendar = Calendar.getInstance();
            nowMonth = calendar.get(Calendar.MONTH) % 6;
        }

        FrameLayout ly = (FrameLayout) LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_fragment_year, null);
        container.addView(ly);

        TextView year = (TextView) ly.findViewById(R.id.year);
        year.setText((MeFragmentData.MIN_YEAR + position / 2) + "");

        tv0 = (TextView) ly.findViewById(R.id.tv0);
        tv1 = (TextView) ly.findViewById(R.id.tv1);
        tv2 = (TextView) ly.findViewById(R.id.tv2);
        tv3 = (TextView) ly.findViewById(R.id.tv3);
        tv4 = (TextView) ly.findViewById(R.id.tv4);
        tv5 = (TextView) ly.findViewById(R.id.tv5);

        if (position % 2 == 1) {
            tv0.setText("七月");
            tv1.setText("八月");
            tv2.setText("九月");
            tv3.setText("十月");
            tv4.setText("十一月");
            tv5.setText("十二月");
        }

        tv0.setOnClickListener(new MonthOnClickListener(tv0, tv1, tv2, tv3, tv4, tv5, position, 0));
        tv1.setOnClickListener(new MonthOnClickListener(tv0, tv1, tv2, tv3, tv4, tv5, position, 1));
        tv2.setOnClickListener(new MonthOnClickListener(tv0, tv1, tv2, tv3, tv4, tv5, position, 2));
        tv3.setOnClickListener(new MonthOnClickListener(tv0, tv1, tv2, tv3, tv4, tv5, position, 3));
        tv4.setOnClickListener(new MonthOnClickListener(tv0, tv1, tv2, tv3, tv4, tv5, position, 4));
        tv5.setOnClickListener(new MonthOnClickListener(tv0, tv1, tv2, tv3, tv4, tv5, position, 5));

        switch (nowMonth) {
            case 0 : tv0.setBackgroundResource(R.drawable.shape_circle); tv0.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 1 : tv1.setBackgroundResource(R.drawable.shape_circle); tv1.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 2 : tv2.setBackgroundResource(R.drawable.shape_circle); tv2.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 3 : tv3.setBackgroundResource(R.drawable.shape_circle); tv3.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 4 : tv4.setBackgroundResource(R.drawable.shape_circle); tv4.setTextColor(Color.parseColor("#FFFFFF")); break;
            case 5 : tv5.setBackgroundResource(R.drawable.shape_circle); tv5.setTextColor(Color.parseColor("#FFFFFF")); break;
        }

        return ly;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //cast to LinearLayout
        container.removeView((FrameLayout)object);
    }

    public interface OnMonthClickListener {
        void OnMonthClick(long t);
    }

    class MonthOnClickListener implements View.OnClickListener {

        private int position = 0;
        private int p = 0;
        private TextView tv0, tv1, tv2, tv3, tv4, tv5;

        MonthOnClickListener(
                TextView tv0,
                TextView tv1,
                TextView tv2,
                TextView tv3,
                TextView tv4,
                TextView tv5,
                int position,
                int p) {
            this.tv0 = tv0;
            this.tv1 = tv1;
            this.tv2 = tv2;
            this.tv3 = tv3;
            this.tv4 = tv4;
            this.tv5 = tv5;
            this.position = position;
            this.p = p;
        }

        @Override
        public void onClick(View v) {
            int year = position / 2 + MeFragmentData.MIN_YEAR;
            int month = p + (position % 2) * 6 + 1;
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, 1, 0, 0, 0);
            calendar.add(Calendar.SECOND, 0);
            int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.add(Calendar.SECOND, 0);
            onMonthClickListener.OnMonthClick(calendar.getTimeInMillis() / 1000);
        }
    }

}
