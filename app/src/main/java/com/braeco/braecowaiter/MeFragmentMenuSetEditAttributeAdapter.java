package com.braeco.braecowaiter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.HashSet;
import java.util.Map;

/**
 * Created by Weiping on 2015/12/21.
 */

public class MeFragmentMenuSetEditAttributeAdapter extends BaseAdapter {

    private OnClickViewListener onClickViewListener;

    public MeFragmentMenuSetEditAttributeAdapter(OnClickViewListener onClickViewListener) {
        this.onClickViewListener = onClickViewListener;
    }

    @Override
    public int getCount() {
        return BraecoWaiterData.setAttributes.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {

        Map<String, Object> attribute = BraecoWaiterData.setAttributes.get(position);

        if (BraecoWaiterData.SET_ATTRIBUTE_NAME.equals(attribute.get("type"))) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_set_attribute_name, null);
            LinearLayout ly = (LinearLayout)convertView.findViewById(R.id.ly);
            MaterialIconView icon = (MaterialIconView) convertView.findViewById(R.id.icon);
            TextView data = (TextView)convertView.findViewById(R.id.data);

            data.setText((String)attribute.get("data"));

            setOnClickViewListener(ly, position, BraecoWaiterData.CHANGE_SET_NAME);
            setOnClickViewListener(icon, position, BraecoWaiterData.DELETE_SET);
        }

        else if (BraecoWaiterData.SET_ATTRIBUTE_BODY.equals(attribute.get("type"))) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_set_attribute_body, null);
            MaterialIconView icon = (MaterialIconView) convertView.findViewById(R.id.icon);
            LinearLayout ly1 = (LinearLayout)convertView.findViewById(R.id.ly1);
            LinearLayout ly2 = (LinearLayout)convertView.findViewById(R.id.ly2);
            LinearLayout ly3 = (LinearLayout)convertView.findViewById(R.id.ly3);
            TextView data1 = (TextView)convertView.findViewById(R.id.data1);
            TextView data2 = (TextView)convertView.findViewById(R.id.data2);
            TextView data3 = (TextView)convertView.findViewById(R.id.data3);

            if (attribute.get("data1") == null) {
                data1.setText("");
            } else {
                data1.setText(((HashSet<Integer>)attribute.get("data1")).size() + "款");
            }
            int size = (Integer)attribute.get("data2");
            if (size == -2) {
                data2.setText("");
            } else {
                data2.setText(size == -1 ? "任选或不选" : "N选" + size);
            }
            int discount;
            if (attribute.containsKey("data3")) {
                if (attribute.get("data3") == null) {
                    discount = -2;
                } else {
                    discount = (Integer)attribute.get("data3");
                }
            } else {
                discount = -2;
            }
            if (discount == -2) {
                data3.setText("固定总价，折扣无效");
            } else if (discount == -1) {
                data3.setText("");
            } else {
                data3.setText(discount == 100 ? "原价" : String.format("%.1f", discount * 1.0 / 10) + "折");
            }

            setOnClickViewListener(ly1, position, BraecoWaiterData.CHANGE_SET);
            setOnClickViewListener(ly2, position, BraecoWaiterData.CHANGE_SET_SIZE);
            setOnClickViewListener(ly3, position, BraecoWaiterData.CHANGE_SET_DISCOUNT);
        }

        else if (BraecoWaiterData.SET_ATTRIBUTE_ADD.equals(attribute.get("type"))) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_set_attribute_group_add, null);
            LinearLayout ly = (LinearLayout)convertView.findViewById(R.id.ly);

            setOnClickViewListener(ly, position, BraecoWaiterData.ADD_SET);
        }

        return convertView;
    }

    private void setOnClickViewListener(View view, final int position, final Integer type) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickViewListener.OnClickView(position, type);
            }
        });
    }

    public interface OnClickViewListener {
        void OnClickView(int position, Integer action);
    }
}
