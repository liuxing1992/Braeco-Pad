package com.braeco.braecowaiter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.Map;

/**
 * Created by Weiping on 2015/12/21.
 */

public class MeFragmentMenuMenuEditAttributeAdapter extends BaseAdapter {

    private OnClickViewListener onClickViewListener;

    public MeFragmentMenuMenuEditAttributeAdapter(OnClickViewListener onClickViewListener) {
        this.onClickViewListener = onClickViewListener;
    }

    @Override
    public int getCount() {
        return BraecoWaiterData.attributes.size();
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

        Map<String, Object> attribute = BraecoWaiterData.attributes.get(position);

        if (BraecoWaiterData.ATTRIBUTE_GROUP.equals(attribute.get("type"))) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_attribute_group, null);
            LinearLayout ly = (LinearLayout)convertView.findViewById(R.id.ly);
            MaterialIconView icon = (MaterialIconView) convertView.findViewById(R.id.icon);
            TextView data = (TextView)convertView.findViewById(R.id.data);

            data.setText("属性：" + (String)attribute.get("data"));

            setOnClickViewListener(ly, position, BraecoWaiterData.CHANGE_NAME);
            setOnClickViewListener(icon, position, BraecoWaiterData.DELETE);
        }

        else if (BraecoWaiterData.ATTRIBUTE.equals(attribute.get("type"))) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_attribute, null);
            MaterialIconView icon = (MaterialIconView) convertView.findViewById(R.id.icon);
            LinearLayout ly1 = (LinearLayout)convertView.findViewById(R.id.ly1);
            LinearLayout ly2 = (LinearLayout)convertView.findViewById(R.id.ly2);
            TextView data1 = (TextView)convertView.findViewById(R.id.data1);
            TextView data2 = (TextView)convertView.findViewById(R.id.data2);

            data1.setText("选项：" + (String)attribute.get("data1"));
            data2.setText("价差：¥ " + (String)attribute.get("data2"));

            setOnClickViewListener(ly1, position, BraecoWaiterData.CHANGE_NAME);
            setOnClickViewListener(ly2, position, BraecoWaiterData.CHANGE_PRICE);
            setOnClickViewListener(icon, position, BraecoWaiterData.DELETE);
        }

        else if (BraecoWaiterData.ATTRIBUTE_ADD.equals(attribute.get("type"))) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_attribute_add, null);
            LinearLayout ly = (LinearLayout)convertView.findViewById(R.id.ly);

            setOnClickViewListener(ly, position, BraecoWaiterData.ADD);
        }

        else if (BraecoWaiterData.ATTRIBUTE_GROUP_ADD.equals(attribute.get("type"))) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_attribute_group_add, null);
            LinearLayout ly = (LinearLayout)convertView.findViewById(R.id.ly);

            setOnClickViewListener(ly, position, BraecoWaiterData.ADD);
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
