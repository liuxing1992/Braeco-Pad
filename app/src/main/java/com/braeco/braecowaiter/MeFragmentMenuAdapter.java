package com.braeco.braecowaiter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.lguipeng.library.animcheckbox.AnimCheckBox;

import net.steamcrafted.materialiconlib.MaterialIconView;

/**
 * Created by Weiping on 2015/12/18.
 */

public class MeFragmentMenuAdapter extends BaseAdapter {

    private OnCheckListener onCheckListener;

    public MeFragmentMenuAdapter(OnCheckListener onCheckListener) {
        this.onCheckListener = onCheckListener;
    }

    @Override
    public int getCount() {
        return BraecoWaiterApplication.mButton.size() + 1;
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

        if (position == BraecoWaiterApplication.mButton.size()) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu_edit_add, null);
        } else {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu_edit, null);

            TextView name = (TextView)convertView.findViewById(R.id.name);
            TextView number = (TextView)convertView.findViewById(R.id.number);
            MaterialIconView icon = (MaterialIconView)convertView.findViewById(R.id.icon);
            final AnimCheckBox check = (AnimCheckBox)convertView.findViewById(R.id.check);
            check.setVisibility(View.INVISIBLE);
            check.setOnCheckedChangeListener(new AnimCheckBox.OnCheckedChangeListener() {
                @Override
                public void onChange(boolean checked) {
                    onCheckListener.onCheck(position, checked);
                }
            });

            if (position == 0) {
                name.setText("*套餐设置");
                number.setText("套餐数：" + (BraecoWaiterApplication.b[position + 1] - BraecoWaiterApplication.b[position]));
            } else {
                name.setText((String) BraecoWaiterApplication.mButton.get(position).get("button"));
                number.setText("餐品数：" + (BraecoWaiterApplication.b[position + 1] - BraecoWaiterApplication.b[position]));
            }
        }

        return convertView;
    }

    interface OnCheckListener {
        void onCheck(int position, boolean check);
    }

}
