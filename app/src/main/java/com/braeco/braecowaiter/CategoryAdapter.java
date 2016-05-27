package com.braeco.braecowaiter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2015/12/4.
 */

public class CategoryAdapter extends BaseAdapter {

    private int selectedPosition = 0;

    @Override
    public int getCount() {
        return BraecoWaiterApplication.mButton.size();
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

        CategoryViewholder holder;

        if (convertView == null) {
            holder = new CategoryViewholder();
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category, null);
            holder.base = (FrameLayout)convertView.findViewById(R.id.base);
            holder.color = (TextView)convertView.findViewById(R.id.color);
            holder.name = (TextView)convertView.findViewById(R.id.name);
            holder.num = (AutofitTextView)convertView.findViewById(R.id.num);

            convertView.setTag(holder);
        }
        else {
            holder = (CategoryViewholder)convertView.getTag();
        }

        if (position == BraecoWaiterApplication.mButton.size() - 1) {
            holder.base.setPadding(0, 0, 0, BraecoWaiterUtils.dp2px(100, parent.getContext()));
        } else {
            holder.base.setPadding(0, 0, 0, 0);
        }

        holder.name.setText((String) BraecoWaiterApplication.mButton
                .get(position).get("button") +
                "("
                + (BraecoWaiterApplication.a[position + 1] - BraecoWaiterApplication.a[position])
                + ")"
        );
        if (position != selectedPosition) {
            holder.color.setVisibility(View.INVISIBLE);
            holder.name.setBackgroundResource(R.color.background_gray);
        } else {
            holder.color.setVisibility(View.VISIBLE);
            holder.name.setBackgroundResource(R.color.white);
        }

        int orderNum = 0;
        for (int i = BraecoWaiterApplication.a[position];
             i < BraecoWaiterApplication.a[position + 1]; i++) {
            orderNum += BraecoWaiterApplication.orderedMeals.get(i).size();
        }
        if (orderNum == 0) {
            holder.num.setVisibility(View.INVISIBLE);
        } else {
            holder.num.setVisibility(View.VISIBLE);
            holder.num.setText(orderNum + "");
        }

        return convertView;
    }

    public void select(int p) {
        selectedPosition = p;
        this.notifyDataSetChanged();
    }

    private class CategoryViewholder {
        public FrameLayout base;
        public TextView color;
        public TextView name;
        public AutofitTextView num;
    }
}
