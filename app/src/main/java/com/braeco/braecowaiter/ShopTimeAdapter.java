package com.braeco.braecowaiter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by Weiping on 2015/12/6.
 */

public class ShopTimeAdapter extends BaseAdapter {

    @Override
    public int getCount() {
        if (BraecoWaiterApplication.shopTimes != null)
            return BraecoWaiterApplication.shopTimes.size();
        return 0;
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
        convertView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop_time, null);
        TextView tv = (TextView) convertView.findViewById(R.id.textview);
        tv.setText(BraecoWaiterApplication.shopTimes.get(position));
        return convertView;
    }
}
