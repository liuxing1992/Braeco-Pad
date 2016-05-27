package com.braeco.braecowaiter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.Authority;
import com.braeco.braecowaiter.Model.AuthorityManager;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class ServiceMenuFragmentCar extends BraecoAppCompatActivity
        implements MenuOrderAdapter.OnOrderListener {

    private ListView meals;
    private MenuOrderAdapter menuOrderAdapter;

    private TextView sum;
    private LinearLayout makeSure;

    private Context mContext;

    private LinearLayout back;

    private TextView clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_menu_fragment_car);

        sortMealPair();

        mContext = this;

        back = (LinearLayout)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        meals = (ListView)findViewById(R.id.list_view);
        menuOrderAdapter = new MenuOrderAdapter(this);
        meals.setAdapter(menuOrderAdapter);

        sum = (TextView)findViewById(R.id.sum);
        makeSure = (LinearLayout)findViewById(R.id.make_sure);
        makeSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AuthorityManager.ableTo(Authority.GIVE_ORDER)) {
                    Intent intent = new Intent(mContext, ServiceMenuFragmentCarMakeSure.class);
                    startActivityForResult(intent, 1);
                } else {
                    AuthorityManager.showDialog(mContext, "辅助点餐");
                }
            }
        });

        clear = (TextView)findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(mContext)
                        .title("清空")
                        .content("确认要清空购物车吗？您将返回到辅助点餐界面")
                        .positiveText("确认")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                if (dialogAction == DialogAction.POSITIVE) {
                                    if (!BraecoWaiterApplication.LOADING_MENU) {
                                        for (int i = BraecoWaiterApplication.orderedMeals.size() - 1; i >= 0; i--)
                                            BraecoWaiterApplication.orderedMeals.get(i).clear();
                                        BraecoWaiterApplication.orderedMealsPair.clear();
                                        finish();
                                    }
                                } else if (dialogAction == DialogAction.NEGATIVE) {

                                }
                            }
                        })
                        .show();
            }
        });

        calculateSum();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (BraecoWaiterApplication.FINISH_ORDER) {
            BraecoWaiterApplication.FINISH_ORDER = false;
            finish();
        }
        switch(requestCode) {
            case (1) : {
                if (resultCode == RESULT_OK) {
                    if ("NOT_MATCH".equals(data.getStringExtra("ERROR"))) {
                        finish();
                    }
                    if ("NO_ERROR".equals(data.getStringExtra("ERROR"))) {
                        finish();
                    }
                }
                break;
            }
        }
    }

    @Override
    public void OnOrderListen() {
        if (BraecoWaiterApplication.orderedMealsPair.size() == 0) {
            finish();
        }
        calculateSum();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (menuOrderAdapter != null) menuOrderAdapter.notifyDataSetChanged();
        OnOrderListen();
    }

    private void calculateSum() {
        double priceSum = 0;
        for (int i = 0; i < BraecoWaiterApplication.prices.length; i++) {
            priceSum += BraecoWaiterApplication.prices[i];
        }
        sum.setText("合计：¥ " + String.format("%.2f", priceSum));
    }

    private void sortMealPair() {
        Collections.sort(BraecoWaiterApplication.orderedMealsPair, new Comparator<Pair<Map<String, Object>, Integer>>() {
            @Override
            public int compare(Pair<Map<String, Object>, Integer> lhs, Pair<Map<String, Object>, Integer> rhs) {
                Double d1 = (Double)lhs.first.get("fullPrice");
                Double d2 = (Double)rhs.first.get("fullPrice");
                return d2.compareTo(d1);
            }
        });
    }
}
