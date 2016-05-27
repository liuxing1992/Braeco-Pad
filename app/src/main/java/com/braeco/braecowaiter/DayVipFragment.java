package com.braeco.braecowaiter;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2015/12/1.
 */

public class DayVipFragment extends Fragment {

    private final static float ZERO = 0.01f;
    private final static int MAX_VIPS_NUM = 10000;

    private ScrollView scrollView;

    private LineChartView chart;
    private LineChartData data;

    private ArrayList<Line> lines;

    private View selector1;
    private View selector2;
    private TextView sum;
    private TextView best;

    private boolean showVipsNum = true;

    private FrameLayout vipNumber;
    private FrameLayout vipCharge;
    private ProgressBar progressBar;

    private ArrayList<Map<String, Object>> vips = new ArrayList<>();

    private int lastSize = 0;

    private TextView empty;

    private boolean vipsNumAreZeros = false;
    private boolean vipsChargeAreZeros = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "DayVipFragment onCreateView");
        View messageLayout = inflater.inflate(R.layout.fragment_day_vip, container, false);

        if (BraecoWaiterApplication.dayVipFragment != null) {
            BraecoWaiterApplication.dayVipFragment = this;
        } else {
            BraecoWaiterApplication.dayVipFragment = this;
        }

        scrollView = (ScrollView)messageLayout.findViewById(R.id.scrollView);
        progressBar = (ProgressBar)messageLayout.findViewById(R.id.progressbar);

        empty = (TextView)messageLayout.findViewById(R.id.empty_tip);

        selector1 = (View)messageLayout.findViewById(R.id.selector1);
        selector2 = (View)messageLayout.findViewById(R.id.selector2);
        selector2.setVisibility(View.INVISIBLE);
        vipNumber = (FrameLayout)messageLayout.findViewById(R.id.vipNumber);
        vipCharge = (FrameLayout)messageLayout.findViewById(R.id.vipCharge);

        vipNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVipsNum = true;
                empty.setVisibility(View.INVISIBLE);
                drawChart(vips);
//                chart.getChartData().getAxisYLeft().setName("新增会员数");
                selector1.setVisibility(View.VISIBLE);
                selector2.setVisibility(View.INVISIBLE);
                empty.setVisibility(View.INVISIBLE);
            }
        });

        vipCharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVipsNum = false;
                drawChart(vips);
//                chart.getChartData().getAxisYLeft().setName("会员充值金额");
                selector1.setVisibility(View.INVISIBLE);
                selector2.setVisibility(View.VISIBLE);
                empty.setVisibility(View.INVISIBLE);
            }
        });

        chart = (LineChartView) messageLayout.findViewById(R.id.chart);

        chart.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
                Log.d("BraecoWaiter", lineIndex + "");
                if (lineIndex == 1) return;
                if (showVipsNum) {
                    BraecoWaiterUtils.showToast(getActivity(),
                            "在"
                                    + vips.get(pointIndex).get("toast")
                                    + "新增会员" + String.format("%.0f", value.getY()) + "人", true);
                } else {
                    BraecoWaiterUtils.showToast(getActivity(),
                            "在"
                                    + vips.get(pointIndex).get("toast")
                                    + "会员充值" + String.format("%.2f", value.getY()) + "元", true);
                }
            }

            @Override
            public void onValueDeselected() {

            }
        });

        sum = (TextView) messageLayout.findViewById(R.id.sum);
        best = (AutofitTextView)messageLayout.findViewById(R.id.best);

        ready();

        return messageLayout;
    }

    public void drawBasicChart(int size) {
        // 0 is vip number
        // 1 is vip money
        lines = new ArrayList<>();
        for (int i = 0; i < 1; ++i) {

            List<PointValue> values = new ArrayList<PointValue>();
            for (int j = 0; j < size; ++j) {
                values.add(new PointValue(j, 0));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLORS[i]);
            line.setShape(ValueShape.CIRCLE);
            line.setCubic(false);
            line.setFilled(false);
            line.setHasLabels(true);
            line.setHasLabelsOnlyForSelected(false);
            line.setHasLines(true);
            line.setHasPoints(true);
            line.setPointColor(ChartUtils.COLORS[0]);
            lines.add(line);
        }

        data = new LineChartData(lines);

        Axis axisX = new Axis();
        Axis axisY = new Axis(new ArrayList<AxisValue>());

        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        chart.setLineChartData(data);

        Calendar now = Calendar.getInstance();

        List<AxisValue> axisLabels = new ArrayList<>();
        for (int i = size - 1; i >= 0; i--) {
            axisLabels.add(new AxisValue(i).setLabel(now.get(Calendar.DAY_OF_MONTH) + "日"));
            now.add(Calendar.DATE, -1);
        }
        axisX.setValues(axisLabels);
        axisX.setMaxLabelChars(3);
        axisX.setTextColor(Color.parseColor("#000000"));

//        if (showVipsNum) {
//            axisY.setName("新增会员数");
//        } else {
//            axisY.setName("会员充值金额");
//        }

        axisY.setTextColor(Color.parseColor("#000000"));
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        chart.setLineChartData(data);
        chart.setZoomEnabled(false);
    }

    public void ready() {
        chart.setVisibility(View.INVISIBLE);
        empty.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        sum.setText("请稍候");
        best.setText("请稍候");

        vipNumber.setEnabled(false);
        vipCharge.setEnabled(false);
    }

    public void drawChart(ArrayList<Map<String, Object>> VIPS) {

        Log.d("BraecoWaiter", "Drawing...");

        if (lastSize != VIPS.size()) {
            drawBasicChart(VIPS.size());
            lastSize = VIPS.size();
        }

        vips = (ArrayList<Map<String, Object>>) VIPS.clone();
        vipNumber.setEnabled(true);
        vipCharge.setEnabled(true);

        sum.setText(VIPS.get(VIPS.size() - 1).get("new_member") + "");
        best.setText("¥" + String.format("%.2f", (float)(double)VIPS.get(VIPS.size() - 1).get("membership_charge")));

        List<AxisValue> axisLabels = new ArrayList<>();
        for (int i = VIPS.size() - 1; i >= 0; i--) {
            if (i == 0 && VIPS.size() == 4) axisLabels.add(new AxisValue(i).setLabel("　　　" + VIPS.get(i).get("axis") + ""));
            else if (i == 3 && VIPS.size() == 4) axisLabels.add(new AxisValue(i).setLabel("" + VIPS.get(i).get("axis") + "　　　"));
            else axisLabels.add(new AxisValue(i).setLabel(VIPS.get(i).get("axis") + ""));
        }
        chart.getChartData().getAxisXBottom().setValues(axisLabels);

        vipsNumAreZeros = true;
        for (int i = 0; i < VIPS.size(); i++) {
            if ((Integer)VIPS.get(i).get("new_member") != 0) {
                vipsNumAreZeros = false;
                break;
            }
        }

        vipsChargeAreZeros = true;
        for (int i = 0; i < VIPS.size(); i++) {
            if ((float)(double)VIPS.get(i).get("membership_charge") != 0) {
                vipsChargeAreZeros = false;
                break;
            }
        }

        deleteSecondLine();

        if (showVipsNum) {
            if (vipsNumAreZeros) {
                for (int i = 0; i < VIPS.size(); i++) {
                    data.getLines().get(0).getValues().get(i).setLabel(getVipNumber((Integer)VIPS.get(i).get("new_member")));
                    if (i == 0) data.getLines().get(0).getValues().get(i).setTarget(i, ZERO);
                    else data.getLines().get(0).getValues().get(i).setTarget(i, 0);
                }
                addSecondLine();
            } else {
                for (int i = 0; i < VIPS.size(); i++) {
                    data.getLines().get(0).getValues().get(i).setLabel(getVipNumber((Integer)VIPS.get(i).get("new_member")));
                    data.getLines().get(0).getValues().get(i).setTarget(i, (Integer)VIPS.get(i).get("new_member"));
                }
            }
            chart.setVisibility(View.VISIBLE);
            data.getLines().get(0).setPointColor(ChartUtils.COLORS[0]);
            data.getLines().get(0).setColor(ChartUtils.COLORS[0]);
        } else {
            if (vipsChargeAreZeros) {
                for (int i = 0; i < VIPS.size(); i++) {
                    data.getLines().get(0).getValues().get(i).setLabel(getVipCharge((float)(double)VIPS.get(i).get("membership_charge")));
                    data.getLines().get(0).getValues().get(i).setTarget(i, ZERO);
                }
                addSecondLine();
            } else {
                for (int i = 0; i < VIPS.size(); i++) {
                    data.getLines().get(0).getValues().get(i).setLabel(getVipCharge((float)(double)VIPS.get(i).get("membership_charge")));
                    data.getLines().get(0).getValues().get(i).setTarget(i, (float)(double)VIPS.get(i).get("membership_charge"));
                }
            }
            chart.setVisibility(View.VISIBLE);
            data.getLines().get(0).setPointColor(ChartUtils.COLORS[2]);
            data.getLines().get(0).setColor(ChartUtils.COLORS[2]);
        }

        empty.setVisibility(View.INVISIBLE);
        chart.startDataAnimation();
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void deleteSecondLine() {
        if (data.getLines().size() == 2) {
            data.getLines().remove(1);
        }
    }

    private void addSecondLine() {
        List<PointValue> values = new ArrayList<PointValue>();
        for (int j = 0; j < 1; ++j) {
            values.add(new PointValue(j, 10));
        }
        Line line = new Line(values);
        line.setColor(Color.parseColor("#00000000"));
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(false);
        line.setFilled(false);
        line.setHasLabels(false);
        line.setHasLabelsOnlyForSelected(false);
        line.setHasLines(false);
        line.setHasPoints(false);
        line.setPointColor(Color.parseColor("#00000000"));
        data.getLines().add(line);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BraecoWaiterApplication.dayProfitFragment = null;
    }

    private String getVipNumber(int number) {
        return number + "人";
    }

    private String getVipCharge(float number) {
        if (number == 0) return "¥0";
        if (number > 1000) return "¥" + String.format("%.2f", number / 1000) + "K";
        else return "¥" + String.format("%.2f", number);
    }

}
