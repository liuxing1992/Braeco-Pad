package com.braeco.braecowaiter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.braeco.braecowaiter.Enums.ActivityType;

import java.util.ArrayList;

/**
 * Created by Weiping on 2015/12/24.
 */
public class MeFragmentActivityDetailAdapter extends BaseAdapter {

    private ActivityType type;
    private ArrayList<Object> list;

    public MeFragmentActivityDetailAdapter(ActivityType type, ArrayList<Object> list) {
        this.type = type;
        this.list = list;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    @Override
    public int getCount() {
        if (type.equals(BraecoWaiterData.ACTIVITY_OTHER)) return list.size();
        else return list.size() / 2;
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
                .inflate(R.layout.item_activity_detail, null);

        TextView price = (TextView)convertView.findViewById(R.id.price_text);
        TextView typeTV = (TextView)convertView.findViewById(R.id.type_text);
        TextView sale = (TextView)convertView.findViewById(R.id.sale_text);

        switch (type) {
            case GIVE:
                price.setText("¥" + String.format("%.2f", (Integer)list.get(position * 2) * 1.0));
                typeTV.setText("送");
                sale.setText((String)list.get(position * 2 + 1));
                break;
            case REDUCE:
                price.setText("¥" + String.format("%.2f", (Integer)list.get(position * 2) * 1.0));
                typeTV.setText("减");
                sale.setText("¥" + String.format("%.2f", (Integer)list.get(position * 2 + 1) * 1.0));
                break;
            case OTHER:
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_activity_detail_other, null);

                TextView tv = (TextView)convertView.findViewById(R.id.text);
                tv.setText(list.get(0) + "");
                break;
        }

        return convertView;
    }
}
