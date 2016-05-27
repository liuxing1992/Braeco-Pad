package com.braeco.braecowaiter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.lguipeng.library.animcheckbox.AnimCheckBox;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Weiping on 2016/2/17.
 */
public class MeFragmentSettingsPrintSettingsMealSubAdapter extends BaseAdapter {

    private ArrayList<Map<String, Object>> subMenu;
    private ArrayList<AnimCheckBox> menuChecks;

    private OnMenuClickListener mListener;

    public MeFragmentSettingsPrintSettingsMealSubAdapter(ArrayList<Map<String, Object>> subMenu, OnMenuClickListener mListener) {
        this.subMenu = subMenu;
        this.mListener = mListener;

        menuChecks = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) menuChecks.add(null);
    }

    @Override
    public int getCount() {
        return subMenu.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_printer_meal_sub, null);

        LinearLayout menu = (LinearLayout)convertView.findViewById(R.id.menu);
        TextView menuName = (TextView)convertView.findViewById(R.id.menu_name);
        final AnimCheckBox menuCheck = (AnimCheckBox)convertView.findViewById(R.id.check);
        menuChecks.set(position, menuCheck);
        menuCheck.setChecked((Boolean)subMenu.get(position).get("check"));

        menuName.setText((String)subMenu.get(position).get("name"));

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelection(menuCheck, position);

            }
        });

        menuCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelection(menuCheck, position);
            }
        });

        return convertView;
    }

    private void toggleSelection(AnimCheckBox menuCheck, int position) {
        menuCheck.setChecked(!menuCheck.isChecked());
        if (!menuCheck.isChecked()) {
            BraecoWaiterApplication.modifyingPrinter.getBan().add((Integer)subMenu.get(position).get("id"));
        } else {
            BraecoWaiterApplication.modifyingPrinter.getBan().remove(subMenu.get(position).get("id"));
        }
        subMenu.get(position).put("check", menuCheck.isChecked());
        mListener.onClick((Integer)subMenu.get(position).get("id"), menuCheck.isChecked());
    }

    public interface OnMenuClickListener {
        void onClick(int id, boolean check);
    }
}
