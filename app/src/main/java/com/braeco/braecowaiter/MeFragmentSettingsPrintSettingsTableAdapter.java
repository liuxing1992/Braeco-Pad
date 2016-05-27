package com.braeco.braecowaiter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.lguipeng.library.animcheckbox.AnimCheckBox;

/**
 * Created by Weiping on 2016/5/14.
 */
public class MeFragmentSettingsPrintSettingsTableAdapter extends BaseAdapter {
    @Override
    public int getCount() {
        if (BraecoWaiterApplication.tables != null) return BraecoWaiterApplication.tables.size();
        else return 0;
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
                    .inflate(R.layout.item_printer_table, null);
            viewHolder.table = (TextView) convertView.findViewById(R.id.text_view);
            viewHolder.table.setHeight(BraecoWaiterUtils.getScreenWidth(parent.getContext()) / 3);
            viewHolder.check = (AnimCheckBox) convertView.findViewById(R.id.check);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ViewHolder finalViewHolder = viewHolder;
        viewHolder.table.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalViewHolder.check.setChecked(!finalViewHolder.check.isChecked());
            }
        });
        viewHolder.table.setText(BraecoWaiterApplication.tables.get(position).getId());

        return convertView;
    }

    class ViewHolder {
        public TextView table;
        public AnimCheckBox check;
    }
}
