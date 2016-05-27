package com.braeco.braecowaiter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by Weiping on 2016/5/12.
 */
public class MeFragmentSettingsPrintAdapter extends BaseAdapter {
    @Override
    public int getCount() {
        return BraecoWaiterApplication.printers == null ? 0 : BraecoWaiterApplication.printers.size();
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
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_printer, null);
            viewHolder.name = (TextView)convertView.findViewById(R.id.name);
            viewHolder.remark = (TextView)convertView.findViewById(R.id.remark);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.name.setText(BraecoWaiterApplication.printers.get(position).getName());
        if ("".equals(BraecoWaiterApplication.printers.get(position).getRemark())
                || BraecoWaiterApplication.printers.get(position).getRemark() == null
                || "null".equals(BraecoWaiterApplication.printers.get(position).getRemark())) {
            viewHolder.name.setGravity(Gravity.CENTER_VERTICAL);
            viewHolder.remark.setVisibility(View.GONE);
        } else {
            viewHolder.name.setGravity(Gravity.BOTTOM);
            viewHolder.remark.setVisibility(View.VISIBLE);
            viewHolder.remark.setText(BraecoWaiterApplication.printers.get(position).getRemark());
        }

        return convertView;
    }

    class ViewHolder {
        TextView name;
        TextView remark;
    }
}
