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
public class MeFragmentMenuSetEditAttributeComboSubAdapter extends BaseAdapter {

    private ArrayList<Map<String, Object>> subMenu;
    private OnMenuClickListener onMenuClickListener;
    private ArrayList<AnimCheckBox> menuChecks;

    public MeFragmentMenuSetEditAttributeComboSubAdapter(OnMenuClickListener onMenuClickListener, ArrayList<Map<String, Object>> subMenu) {
        this.onMenuClickListener = onMenuClickListener;
        this.subMenu = subMenu;

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
                .inflate(R.layout.item_combo_sub, null);

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
        subMenu.get(position).put("check", menuCheck.isChecked());
        if (!menuCheck.isChecked()) onMenuClickListener.onClick((Integer)subMenu.get(position).get("id"), false, false);
        else {
            boolean allCheck = true;
            for (Map<String, Object> meal : subMenu) {
                if (!(Boolean)meal.get("check")) {
                    allCheck = false;
                    break;
                }
            }
            onMenuClickListener.onClick((Integer)subMenu.get(position).get("id"), true, allCheck);
        }
    }

    public interface OnMenuClickListener {
        /**
         *
         * @param id The id of the meal.
         * @param check Whether the meal is being checked now.
         * @param allCheck Whether the group of this meal is all being checked now.
         */
        void onClick(int id, boolean check, boolean allCheck);
    }
}
