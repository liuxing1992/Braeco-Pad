package com.braeco.braecowaiter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;

public class MeFragmentShopTime extends BraecoAppCompatActivity
        implements
        View.OnClickListener,
        TimePickerDialog.OnTimeSetListener {

    private LinearLayout back;
    private LinearLayout day;
    private ListView times;
    private LinearLayout add;
    private TextView select;

    private ShopTimeAdapter shopTimeAdapter;

    private String[] days = new String[]{"每周一", "每周二", "每周三", "每周四", "每周五", "每周六", "每周日"};
    private String[] daysShort = new String[]{"一", "二", "三", "四", "五", "六", "日"};
    private ArrayList<Integer> selected = new ArrayList<>();

    private int setPosition = -1;
    private boolean isFirst = true;

    private String hourString;
    private String hourStringEnd;
    private String minuteString;
    private String minuteStringEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_fragment_shop_time);

        day = (LinearLayout)findViewById(R.id.day);
        add = (LinearLayout)findViewById(R.id.add);
        back = (LinearLayout)findViewById(R.id.back);

        select = (TextView)findViewById(R.id.selected);

        times = (ListView)findViewById(R.id.list_view);
        shopTimeAdapter = new ShopTimeAdapter();
        times.setAdapter(shopTimeAdapter);

        day.setOnClickListener(this);
        add.setOnClickListener(this);
        back.setOnClickListener(this);

        times.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setPosition = position;
                isFirst = true;
                Calendar now = Calendar.getInstance();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        MeFragmentShopTime.this,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                );
                tpd.setAccentColor(BraecoWaiterUtils.getInstance()
                        .getColorFromResource(MeFragmentShopTime.this, R.color.colorPrimary));
                tpd.setTitle("从");
                tpd.show(getFragmentManager(), "Timepickerdialog");
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.day:
                Integer[] ints = new Integer[selected.size()];
                for (int i = 0; i < selected.size(); i++) {
                    ints[i] = selected.get(i);
                }
                new MaterialDialog.Builder(this)
                        .title("营业日选择")
                        .items(days)
                        .itemsCallbackMultiChoice(ints,
                                new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                selected.clear();
                                for (int i = 0; i < which.length; i++) selected.add(which[i]);
                                return true;
                            }
                        })
                        .positiveText("确认")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                if (dialogAction == DialogAction.POSITIVE) {
                                    String selectedString = "";
                                    if (selected.size() != 0) {
                                        selectedString = "每周";
                                    }
                                    for (int i = 0; i < selected.size(); i++) {
                                        if (i != 0) selectedString += "、";
                                        selectedString += daysShort[selected.get(i)];
                                    }
                                    select.setText(selectedString);
                                }
                            }
                        })
                        .show();
                break;
            case R.id.add:
                setPosition = -1;
                isFirst = true;
                Calendar now = Calendar.getInstance();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        MeFragmentShopTime.this,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                );
                tpd.setAccentColor(BraecoWaiterUtils.getInstance()
                        .getColorFromResource(MeFragmentShopTime.this, R.color.colorPrimary));
                tpd.setTitle("从");
                tpd.show(getFragmentManager(), "Timepickerdialog");
                break;
            case R.id.back:
                finish();
                break;
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        if (isFirst) {
            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = TimePickerDialog.newInstance(
                    MeFragmentShopTime.this,
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );
            tpd.setAccentColor(BraecoWaiterUtils.getInstance()
                    .getColorFromResource(MeFragmentShopTime.this, R.color.colorPrimary));
            tpd.setTitle("到");
            tpd.show(getFragmentManager(), "Timepickerdialog2");
            isFirst = false;
            hourString = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
            minuteString = minute < 10 ? "0"+minute : ""+minute;
        } else {
            isFirst = true;
            hourStringEnd = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
            minuteStringEnd = minute < 10 ? "0"+minute : ""+minute;
            String timeRange = hourString + ":" + minuteString + "~" + hourStringEnd + ":" + minuteStringEnd;
            if (setPosition == -1) {
                if (BraecoWaiterApplication.shopTimes == null) {
                    BraecoWaiterApplication.shopTimes = new ArrayList<>();
                }
                BraecoWaiterApplication.shopTimes.add(timeRange);
            } else {
                BraecoWaiterApplication.shopTimes.set(setPosition, timeRange);
            }
            shopTimeAdapter.notifyDataSetChanged();
        }
    }
}
