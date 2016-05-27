package com.braeco.braecowaiter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by Weiping on 2015/12/6.
 */
public class ChooseTableAdapter extends BaseAdapter {

    @Override
    public int getCount() {
        if (BraecoWaiterApplication.tables == null) return 0;
        else return BraecoWaiterApplication.tables.size();
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
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_choose_table, null);

            viewHolder.table = (TextView) convertView.findViewById(R.id.text_view);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.table.setText(BraecoWaiterApplication.tables.get(position).getId());

        return convertView;
    }

    class ViewHolder {
        public TextView table;
    }
}
