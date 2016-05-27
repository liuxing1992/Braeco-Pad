package com.braeco.braecowaiter;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;
import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2015/12/1.
 */

public class DayProfitFragment extends Fragment {

    private static final String[] PROFIT_TYPE_NAME = new String[]{"现金", "微信", "微信P2P", "支付宝", "支付宝F2F", "百度钱包"};
    private static final String[] PROFIT_TYPE_AXIS = new String[]{"现金", "微信", "微信P2P", "支付宝", "支付宝F2F", "百度钱包"};

    private ScrollView scrollView;

    private ColumnChartView chart;
    private ColumnChartData data;

    private LineChartView lineChart;
    private LineChartData lineData;

    private ArrayList<Line> lines;

    private View selector1;
    private View selector2;

    private FrameLayout sumFy;
    private FrameLayout bestFy;

    private TextView sum;
    private TextView best;

    private ProgressBar progressBar;
    private TextView empty;

    private LinearLayout color;

    private int profitLine = 1;
    private ArrayList<Map<String, Object>> vips = new ArrayList<>();
    private int lastSize = 0;
    private boolean showType = true;
    private boolean linesAreZero = false;
    private boolean columnsAreZero = false;
    private ArrayList<String> toasts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "DayProfitFragment onCreateView");
        View messageLayout = inflater.inflate(R.layout.fragment_day_profit, container, false);

        if (BraecoWaiterApplication.dayProfitFragment != null) {
            BraecoWaiterApplication.dayProfitFragment.onDestroy();
            BraecoWaiterApplication.dayProfitFragment = this;
        } else {
            BraecoWaiterApplication.dayProfitFragment = this;
        }

        sumFy = (FrameLayout)messageLayout.findViewById(R.id.sum_fy);
        sumFy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showType = true;
                chart.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.INVISIBLE);
                color.setVisibility(View.GONE);
                progressBar.setVisibility(View.INVISIBLE);
                empty.setVisibility(View.INVISIBLE);
                selector1.setVisibility(View.VISIBLE);
                selector2.setVisibility(View.INVISIBLE);
                if (columnsAreZero) {
                    chart.setVisibility(View.INVISIBLE);
                    empty.setVisibility(View.VISIBLE);
                }
            }
        });

        bestFy = (FrameLayout)messageLayout.findViewById(R.id.best_fy);
        bestFy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showType = false;
                chart.setVisibility(View.INVISIBLE);
                lineChart.setVisibility(View.VISIBLE);
                color.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                empty.setVisibility(View.INVISIBLE);
                selector1.setVisibility(View.INVISIBLE);
                selector2.setVisibility(View.VISIBLE);
            }
        });

        color = (LinearLayout)messageLayout.findViewById(R.id.color);
        color.setVisibility(View.INVISIBLE);

        selector1 = (View)messageLayout.findViewById(R.id.selector1);
        selector2 = (View)messageLayout.findViewById(R.id.selector2);
        selector2.setVisibility(View.INVISIBLE);

        progressBar = (ProgressBar)messageLayout.findViewById(R.id.progressbar);
        empty = (TextView)messageLayout.findViewById(R.id.empty_tip);

        scrollView = (ScrollView)messageLayout.findViewById(R.id.scrollView);

        chart = (ColumnChartView) messageLayout.findViewById(R.id.chart);
        chart.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
                BraecoWaiterUtils.showToast(getActivity(),
                        String.format("%.2f", value.getValue())
                        + "元通过"
                        + PROFIT_TYPE_NAME[columnIndex]
                        + "支付",
                        true);
            }
            @Override
            public void onValueDeselected() {

            }
        });

        lineChart = (LineChartView)messageLayout.findViewById(R.id.line);
        lineChart.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
                if (lineIndex == 2) return;
                if (lineIndex == profitLine) {
                    BraecoWaiterUtils.showToast(getActivity(),
                            "在"
                                    + vips.get(pointIndex).get("toast")
                                    + "营业额为" + String.format("%.2f", value.getY()) + "元", true);
                    if (profitLine != 1) {
                        Collections.swap(lineChart.getLineChartData().getLines(), 0, 1);
                        profitLine = 1 - profitLine;
                    }
                } else {
                    BraecoWaiterUtils.showToast(getActivity(),
                            "在"
                                    + vips.get(pointIndex).get("toast")
                                    + "手续费为" + String.format("%.2f", value.getY()) + "元", true);
                    if (profitLine != 0) {
                        Collections.swap(lineChart.getLineChartData().getLines(), 0, 1);
                        profitLine = 1 - profitLine;
                    }
                }
            }

            @Override
            public void onValueDeselected() {

            }
        });

        sum = (TextView) messageLayout.findViewById(R.id.sum);
        best = (AutofitTextView)messageLayout.findViewById(R.id.best);

        List<Column> columns = new ArrayList<>();
        List<SubcolumnValue> values;
        for (int i = 0; i < 6; ++i) {
            values = new ArrayList<>();
            values.add(new SubcolumnValue(0f, ChartUtils.pickColor()));
            Column column = new Column(values);
            column.setHasLabels(false);
            columns.add(column);
        }

        data = new ColumnChartData(columns);

        Axis axisX = new Axis();

        List<AxisValue> axisLabels = new ArrayList<>();
        for (int i = 0; i < 6; i++) axisLabels.add(new AxisValue(i).setLabel(PROFIT_TYPE_AXIS[i]));
        axisX.setValues(axisLabels);
        axisX.setMaxLabelChars(3);
        axisX.setTextColor(Color.parseColor("#000000"));

        Axis axisY = new Axis().setHasLines(true);
        axisX.setName("支付方式");
        axisY.setName("");
        axisY.setTextColor(Color.parseColor("#000000"));
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        chart.setColumnChartData(data);
        chart.setZoomEnabled(false);

        ready();

        return messageLayout;
    }

    public void drawBasicChart(int size) {
        // 0 is vip number
        // 1 is vip money
        lines = new ArrayList<>();
        for (int i = 0; i < 2; ++i) {

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

        lineData = new LineChartData(lines);

        Axis axisX = new Axis();
        Axis axisY = new Axis(new ArrayList<AxisValue>());

        lineData.setAxisXBottom(axisX);
        lineData.setAxisYLeft(axisY);

        lineData.setBaseValue(Float.NEGATIVE_INFINITY);
        lineChart.setLineChartData(lineData);

        Calendar now = Calendar.getInstance();

        List<AxisValue> axisLabels = new ArrayList<>();
        for (int i = size - 1; i >= 0; i--) {
            axisLabels.add(new AxisValue(i).setLabel(now.get(Calendar.DAY_OF_MONTH) + "日"));
            now.add(Calendar.DATE, -1);
        }
        axisX.setName("营业总额和手续费趋势");
        axisX.setValues(axisLabels);
        axisX.setMaxLabelChars(3);
        axisX.setTextColor(Color.parseColor("#000000"));

        axisY.setTextColor(Color.parseColor("#000000"));
        lineData.setAxisXBottom(axisX);
        lineData.setAxisYLeft(axisY);

        lineChart.setLineChartData(lineData);
        lineChart.setZoomEnabled(false);
    }

    public void drawChart(
            int ORDER_NUM,
            double PROFIT,
            ArrayList<Map<String, Object>> TYPE_PROFITS,
            ArrayList<Double> PROFITS,
            ArrayList<Double> FEES,
            ArrayList<Map<String, Object>> VIPS) {

        if (lastSize != VIPS.size()) {
            drawBasicChart(VIPS.size());
            lastSize = VIPS.size();
        }

        vips = (ArrayList<Map<String, Object>>) VIPS.clone();
        sumFy.setEnabled(true);
        bestFy.setEnabled(true);

        sum.setText(ORDER_NUM + "");
        best.setText("¥" + BraecoWaiterUtils.round02(PROFIT, 2));

        // draw lines
        List<AxisValue> axisLabels = new ArrayList<>();
        for (int i = VIPS.size() - 1; i >= 0; i--) {
            if (i == 0 && VIPS.size() == 4) axisLabels.add(new AxisValue(i).setLabel("　　　" + VIPS.get(i).get("axis") + ""));
            else if (i == 3 && VIPS.size() == 4) axisLabels.add(new AxisValue(i).setLabel("" + VIPS.get(i).get("axis") + "　　　"));
            else axisLabels.add(new AxisValue(i).setLabel(VIPS.get(i).get("axis") + ""));
        }
        lineChart.getChartData().getAxisXBottom().setValues(axisLabels);

        for (int i = 0; i < VIPS.size(); i++) {
            lineData.getLines().get(profitLine).getValues().get(i).setLabel(getMoney((float)(double)PROFITS.get(i)));
            lineData.getLines().get(profitLine).getValues().get(i).setTarget(i, (float)(double)PROFITS.get(i));
            lineData.getLines().get(1 - profitLine).getValues().get(i).setLabel(getMoney((float)(double)FEES.get(i)));
            lineData.getLines().get(1 - profitLine).getValues().get(i).setTarget(i, (float)(double)FEES.get(i));
        }
        lineData.getLines().get(profitLine).setPointColor(ChartUtils.COLORS[0]);
        lineData.getLines().get(profitLine).setColor(ChartUtils.COLORS[0]);
        lineData.getLines().get(1 - profitLine).setPointColor(ChartUtils.COLORS[2]);
        lineData.getLines().get(1 - profitLine).setColor(ChartUtils.COLORS[2]);

        // draw columns
        if (PROFIT == 0) {
            columnsAreZero = true;
        } else {
            columnsAreZero = false;
            Double[] typeProfit = new Double[]{0d, 0d, 0d, 0d, 0d, 0d};

            for (int i = 0; i < TYPE_PROFITS.size(); i++) {
                if ("cash".equals(TYPE_PROFITS.get(i).get("channel"))) {
                    typeProfit[0] += (Double) TYPE_PROFITS.get(i).get("amount");
                } else if ("wx_pub".equals(TYPE_PROFITS.get(i).get("channel"))) {
                    typeProfit[1] += (Double) TYPE_PROFITS.get(i).get("amount");
                } else if ("p2p_wx_pub".equals(TYPE_PROFITS.get(i).get("channel"))) {
                    typeProfit[2] += (Double) TYPE_PROFITS.get(i).get("amount");
                } else if ("alipay_wap".equals(TYPE_PROFITS.get(i).get("channel"))) {
                    typeProfit[3] += (Double) TYPE_PROFITS.get(i).get("amount");
                } else if ("alipay_qr_f2f".equals(TYPE_PROFITS.get(i).get("channel"))) {
                    typeProfit[4] += (Double) TYPE_PROFITS.get(i).get("amount");
                } else if ("bfb_wap".equals(TYPE_PROFITS.get(i).get("channel"))) {
                    typeProfit[5] += (Double) TYPE_PROFITS.get(i).get("amount");
                }
            }

            List<Column> columns = new ArrayList<>();
            List<SubcolumnValue> values;
            for (int i = 0; i < 6; ++i) {
                values = new ArrayList<>();
                values.add(new SubcolumnValue((float)(double)typeProfit[i],
                        ChartUtils.pickColor()));

                Column column = new Column(values);
                column.setHasLabels(false);
                columns.add(column);
            }

            for (int i = 0; i < 6; i++) {
                data.getColumns().get(i).getValues().get(0).setTarget((float)(double)typeProfit[i]);
            }

        }

        linesAreZero = true;
        for (int i = 0; i < PROFITS.size(); i++) {
            if (PROFITS.get(i) != 0f || FEES.get(i) != 0f) {
                linesAreZero = false;
                break;
            }
        }

        deleteThirdLine();

        if (showType) {
            if (columnsAreZero) {
                chart.setVisibility(View.INVISIBLE);
                lineChart.setVisibility(View.INVISIBLE);
                color.setVisibility(View.GONE);
                progressBar.setVisibility(View.INVISIBLE);
                empty.setVisibility(View.VISIBLE);
            } else {
                chart.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.INVISIBLE);
                color.setVisibility(View.GONE);
                progressBar.setVisibility(View.INVISIBLE);
                empty.setVisibility(View.INVISIBLE);
            }
        } else {
            if (linesAreZero) {
                addThirdLine();
            }
            chart.setVisibility(View.INVISIBLE);
            lineChart.setVisibility(View.VISIBLE);
            color.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            empty.setVisibility(View.INVISIBLE);
        }

        chart.startDataAnimation();
        lineChart.startDataAnimation();
    }

    public void ready() {
        chart.setVisibility(View.INVISIBLE);
        lineChart.setVisibility(View.INVISIBLE);
        color.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        empty.setVisibility(View.INVISIBLE);
        sum.setText("请稍候");
        best.setText("请稍候");

        sumFy.setEnabled(false);
        bestFy.setEnabled(false);
    }

    private void deleteThirdLine() {
        if (lineData.getLines().size() == 3) {
            lineData.getLines().remove(2);
        }
    }

    private void addThirdLine() {
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
        lineData.getLines().add(line);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BraecoWaiterApplication.dayProfitFragment = null;
    }

    private String getMoney(float number) {
        if (number == 0) return "¥0";
        if (number > 1000) return "¥" + String.format("%.2f", number / 1000) + "K";
        else return "¥" + String.format("%.2f", number);
    }

}
