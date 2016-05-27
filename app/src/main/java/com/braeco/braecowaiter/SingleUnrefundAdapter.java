package com.braeco.braecowaiter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by Weiping on 2015/12/27.
 */
public class SingleUnrefundAdapter extends BaseAdapter {
    @Override
    public int getCount() {
        return BraecoWaiterData.unRefundMeals.size();
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
                .inflate(R.layout.item_unrefund, null);

        TextView name = (TextView)convertView.findViewById(R.id.name);
        TextView num = (TextView)convertView.findViewById(R.id.num);
        TextView price = (TextView)convertView.findViewById(R.id.price);

        name.setText(BraecoWaiterData.unRefundMeals.get(position).get("properties") + "");
        num.setText("*" + BraecoWaiterData.unRefundMeals.get(position).get("sum"));
        price.setText(getNegativePrice("" + BraecoWaiterData.unRefundMeals.get(position).get("price")));

        return convertView;
    }

    private String getNegativePrice(String price) {
        Double d = Double.parseDouble(price);
        if (d < 0) return "-¥ " + String.format("%.2f", -d);
        else return "¥ " + String.format("%.2f", d);
    }
}
