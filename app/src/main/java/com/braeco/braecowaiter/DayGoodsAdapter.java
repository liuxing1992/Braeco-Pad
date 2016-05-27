package com.braeco.braecowaiter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Weiping on 2015/12/29.
 */
public class DayGoodsAdapter extends BaseAdapter {

    private ArrayList<Object[]> data = new ArrayList<>();

    public DayGoodsAdapter(ArrayList<Object[]> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
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
                .inflate(R.layout.item_good_detail, null);

        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView number = (TextView) convertView.findViewById(R.id.num);
        TextView money = (TextView) convertView.findViewById(R.id.price);

        name.setText(data.get(position)[0] + "");
        number.setText(data.get(position)[1] + "");
        money.setText("¥" + String.format("%.2f", (float)(double)data.get(position)[2]));

        return convertView;
    }
}
