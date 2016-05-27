package com.braeco.braecowaiter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.PieChartView;
import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2015/12/1.
 */

public class DayGoodsFragment extends Fragment {

    private static final int SHOW_NAME_NUM = 3;
    private static final int SHOW_MEAL_NUM = 10;
    private static final long ARROW_ANIMATION_DURATION = 300;

    private PieChartView chart;
    private PieChartData data;

    private TextView sum;
    private AutofitTextView best;

    private ScrollView scrollview;

    private FloatingActionButton list;

    private ProgressBar progressBar;
    private TextView empty;

    private ArrayList<Map<String, Object>> meals = new ArrayList<>();

    private FrameLayout toPie;
    private FrameLayout toTable;

    private View selector1;
    private View selector2;

    private LinearLayout tableLy;
    private ExpandedListView table;
    private RelativeLayout arrow0;
    private RelativeLayout arrow1;
    private RelativeLayout arrow2;
    private DayGoodsAdapter adapter;
    private LinearLayout name;
    private LinearLayout number;
    private LinearLayout money;

    private ArrayList<Object[]> goods = new ArrayList<>();

    private boolean showPie = true;
    private boolean sortByName = true;
    private boolean sortByNameReverse = false;
    private boolean sortByNumber = false;
    private boolean sortByNumberReverse = false;
    private boolean sortByMoney = false;
    private boolean sortByMoneyReverse = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "DayGoodsFragment onCreateView");
        if (BraecoWaiterApplication.dayGoodsFragment != null) {
            BraecoWaiterApplication.dayGoodsFragment.onDestroy();
            BraecoWaiterApplication.dayGoodsFragment = this;
        } else {
            BraecoWaiterApplication.dayGoodsFragment = this;
        }
        View messageLayout = inflater.inflate(R.layout.fragment_day_goods, container, false);

        toPie = (FrameLayout)messageLayout.findViewById(R.id.to_pie);
        toPie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selector1.setVisibility(View.VISIBLE);
                selector2.setVisibility(View.INVISIBLE);
                if (goods.size() == 0) {
                    return;
                }
                showPie = true;
                tableLy.setVisibility(View.GONE);
                chart.setVisibility(View.VISIBLE);
                scrollview.fullScroll(ScrollView.FOCUS_UP);
            }
        });
        toTable = (FrameLayout)messageLayout.findViewById(R.id.to_table);
        toTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selector1.setVisibility(View.INVISIBLE);
                selector2.setVisibility(View.VISIBLE);
                if (goods.size() == 0) {
                    return;
                }
                showPie = false;
                tableLy.setVisibility(View.VISIBLE);
                chart.setVisibility(View.INVISIBLE);
                scrollview.fullScroll(ScrollView.FOCUS_UP);
            }
        });

        selector1 = messageLayout.findViewById(R.id.selector1);
        selector2 = messageLayout.findViewById(R.id.selector2);
        selector2.setVisibility(View.INVISIBLE);

        tableLy = (LinearLayout)messageLayout.findViewById(R.id.table_ly);
        tableLy.setVisibility(View.GONE);

        table = (ExpandedListView)messageLayout.findViewById(R.id.table);
        adapter = new DayGoodsAdapter(goods);
        table.setAdapter(adapter);

        name = (LinearLayout)messageLayout.findViewById(R.id.name);
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByName();
            }
        });
        number = (LinearLayout)messageLayout.findViewById(R.id.number);
        number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByNumber();
            }
        });
        money = (LinearLayout)messageLayout.findViewById(R.id.money);
        money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByMoney();
            }
        });

        arrow0 = (RelativeLayout)messageLayout.findViewById(R.id.arrow0);
        arrow1 = (RelativeLayout)messageLayout.findViewById(R.id.arrow1);
        arrow2 = (RelativeLayout)messageLayout.findViewById(R.id.arrow2);

        progressBar = (ProgressBar)messageLayout.findViewById(R.id.progressbar);
        empty = (TextView)messageLayout.findViewById(R.id.empty_tip);

        scrollview = (ScrollView)messageLayout.findViewById(R.id.scrollView);

        chart = (PieChartView)messageLayout.findViewById(R.id.chart);
        chart.setChartRotationEnabled(false);

        chart.setOnValueTouchListener(new PieChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int arcIndex, SliceValue value) {
                if (meals.size() <= arcIndex) return;
                BraecoWaiterUtils.showToast(getActivity(),
                        String.valueOf("卖出" + meals.get(arcIndex).get("name")) + " : "
                        + String.format("%.0f", value.getValue()) + "件，共"
                        + String.format("%.2f", meals.get(arcIndex).get("price")) + "元",
                        true);
            }
            @Override
            public void onValueDeselected() {

            }
        });

        sum = (TextView) messageLayout.findViewById(R.id.sum);
        best = (AutofitTextView)messageLayout.findViewById(R.id.best);

        list = (FloatingActionButton)messageLayout.findViewById(R.id.list_view);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), DayGoodsList.class));
            }
        });
        list.setVisibility(View.GONE);

        return messageLayout;
    }

    private void sortByName() {
        sortByNumber = false;
        sortByMoney = false;
        if (sortByName) {
            if (sortByNameReverse) {
                Collections.sort(goods, new Comparator<Object[]>() {
                    @Override
                    public int compare(Object[] lhs, Object[] rhs) {
                        String l = lhs[0] + "";
                        String r = rhs[0] + "";
                        return r.compareTo(l);
                    }
                });
            } else {
                Collections.sort(goods, new Comparator<Object[]>() {
                    @Override
                    public int compare(Object[] lhs, Object[] rhs) {
                        String l = lhs[0] + "";
                        String r = rhs[0] + "";
                        return l.compareTo(r);
                    }
                });
            }
            createRotateAnimator(
                    arrow0,
                    (sortByNameReverse ? 180f : 0f),
                    (sortByNameReverse ? 0f : 180f),
                    ARROW_ANIMATION_DURATION).start();
            sortByNameReverse = !sortByNameReverse;
        } else {
            sortByName = true;
            sortByNameReverse = false;
            Collections.sort(goods, new Comparator<Object[]>() {
                @Override
                public int compare(Object[] lhs, Object[] rhs) {
                    String l = lhs[0] + "";
                    String r = rhs[0] + "";
                    return r.compareTo(l);
                }
            });
            createRotateAnimator(
                    arrow0,
                    180f,
                    0f,
                    0).start();
        }
        arrow0.setVisibility(View.VISIBLE);
        arrow1.setVisibility(View.INVISIBLE);
        arrow2.setVisibility(View.INVISIBLE);
        adapter.notifyDataSetChanged();
    }

    private void sortByNumber() {
        sortByName = false;
        sortByMoney = false;
        if (sortByNumber) {
            if (sortByNumberReverse) {
                Collections.sort(goods, new Comparator<Object[]>() {
                    @Override
                    public int compare(Object[] lhs, Object[] rhs) {
                        Integer l = (Integer)lhs[1];
                        Integer r = (Integer)rhs[1];
                        return r.compareTo(l);
                    }
                });
            } else {
                Collections.sort(goods, new Comparator<Object[]>() {
                    @Override
                    public int compare(Object[] lhs, Object[] rhs) {
                        Integer l = (Integer)lhs[1];
                        Integer r = (Integer)rhs[1];
                        return l.compareTo(r);
                    }
                });
            }
            createRotateAnimator(
                    arrow1,
                    (sortByNumberReverse ? 180f : 0f),
                    (sortByNumberReverse ? 0f : 180f),
                    ARROW_ANIMATION_DURATION).start();
            sortByNumberReverse = !sortByNumberReverse;
        } else {
            sortByNumber = true;
            sortByNumberReverse = false;
            Collections.sort(goods, new Comparator<Object[]>() {
                @Override
                public int compare(Object[] lhs, Object[] rhs) {
                    Integer l = (Integer)lhs[1];
                    Integer r = (Integer)rhs[1];
                    return r.compareTo(l);
                }
            });
            createRotateAnimator(
                    arrow1,
                    180f,
                    0f,
                    0).start();
        }
        arrow0.setVisibility(View.INVISIBLE);
        arrow1.setVisibility(View.VISIBLE);
        arrow2.setVisibility(View.INVISIBLE);
        adapter.notifyDataSetChanged();
    }

    private void sortByMoney() {
        sortByName = false;
        sortByNumber = false;
        if (sortByMoney) {
            if (sortByMoneyReverse) {
                Collections.sort(goods, new Comparator<Object[]>() {
                    @Override
                    public int compare(Object[] lhs, Object[] rhs) {
                        Double l = (Double)lhs[2];
                        Double r = (Double)rhs[2];
                        return r.compareTo(l);
                    }
                });
            } else {
                Collections.sort(goods, new Comparator<Object[]>() {
                    @Override
                    public int compare(Object[] lhs, Object[] rhs) {
                        Double l = (Double)lhs[2];
                        Double r = (Double)rhs[2];
                        return l.compareTo(r);
                    }
                });
            }
            createRotateAnimator(
                    arrow2,
                    (sortByMoneyReverse ? 180f : 0f),
                    (sortByMoneyReverse ? 0f : 180f),
                    ARROW_ANIMATION_DURATION).start();
            sortByMoneyReverse = !sortByMoneyReverse;
        } else {
            sortByMoney = true;
            sortByMoneyReverse = false;
            Collections.sort(goods, new Comparator<Object[]>() {
                @Override
                public int compare(Object[] lhs, Object[] rhs) {
                    Double l = (Double)lhs[2];
                    Double r = (Double)rhs[2];
                    return r.compareTo(l);
                }
            });
            createRotateAnimator(
                    arrow2,
                    180f,
                    0f,
                    0).start();
        }
        arrow0.setVisibility(View.INVISIBLE);
        arrow1.setVisibility(View.INVISIBLE);
        arrow2.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    public void ready() {
        chart.setVisibility(View.INVISIBLE);
        tableLy.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        empty.setVisibility(View.INVISIBLE);
        sum.setText("请稍候");
        best.setText("请稍候");

        toPie.setEnabled(false);
        toTable.setEnabled(false);
    }

    public void drawChart(int MEAL_NUM, String MEAL_BEST, ArrayList<Map<String, Object>> MEALS) {

        toPie.setEnabled(true);
        toTable.setEnabled(true);

        sum.setText(MEAL_NUM + "");
        best.setText(MEAL_BEST);

        meals = (ArrayList<Map<String, Object>>) MEALS.clone();
        goods = new ArrayList<>();

        if (MEALS.size() > 0) {
            List<SliceValue> values = new ArrayList<SliceValue>();
            for (int i = 0; i < MEALS.size(); ++i) {
                if (i < SHOW_MEAL_NUM) {
                    SliceValue sliceValue
                            = new SliceValue((Integer)MEALS.get(i).get("sum"), ChartUtils.pickColor());
                    if (i < SHOW_NAME_NUM) sliceValue.setLabel(MEALS.get(i).get("name")
                            + " - " + String.format("%.0f", (Integer)MEALS.get(i).get("sum") * 1.0));
                    else sliceValue.setLabel("");
                    values.add(sliceValue);
                }
                Object[] objects = new Object[3];
                objects[0] = MEALS.get(i).get("name");
                objects[1] = MEALS.get(i).get("sum");
                objects[2] = MEALS.get(i).get("price");
                goods.add(objects);
            }

            data = new PieChartData(values);
            data.setHasLabels(true);
            data.setHasLabelsOnlyForSelected(false);
            data.setHasLabelsOutside(false);
            data.setHasCenterCircle(true);

            chart.setPieChartData(data);
            if (showPie) {
                chart.setVisibility(View.VISIBLE);
                tableLy.setVisibility(View.GONE);
            } else {
                chart.setVisibility(View.INVISIBLE);
                tableLy.setVisibility(View.VISIBLE);
            }

            progressBar.setVisibility(View.INVISIBLE);
            empty.setVisibility(View.INVISIBLE);
        } else {
            sum.setText(MEAL_NUM + "");
            best.setText("暂无数据");
            chart.setVisibility(View.INVISIBLE);
            tableLy.setVisibility(View.GONE);
            progressBar.setVisibility(View.INVISIBLE);
            empty.setVisibility(View.VISIBLE);
        }

        Log.d("BraecoWaiter", goods.size() + "");
        adapter = new DayGoodsAdapter(goods);
        table.setAdapter(adapter);
        sortByName = false;
        sortByName();

        scrollview.fullScroll(ScrollView.FOCUS_UP);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BraecoWaiterApplication.dayGoodsFragment = null;
    }

    public ObjectAnimator createRotateAnimator(final View target, final float from, final float to, final long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "rotation", from, to);
        animator.setDuration(duration);
        return animator;
    }

}
