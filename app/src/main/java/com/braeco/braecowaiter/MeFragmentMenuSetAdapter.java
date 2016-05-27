package com.braeco.braecowaiter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.lguipeng.library.animcheckbox.AnimCheckBox;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
import java.util.Map;

import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2015/12/18.
 */
public class MeFragmentMenuSetAdapter extends BaseAdapter {

    private OnCheckListener mOnCheckListener;
    private static String size = null;
    private ArrayList<Boolean> isCheck;
    private boolean selectable = false;

    public MeFragmentMenuSetAdapter(Context context, OnCheckListener onCheckListener, ArrayList<Boolean> isCheck) {
        this.mOnCheckListener = onCheckListener;
        this.isCheck = isCheck;
        if (size == null) {
            size = "?imageView2/1/w/" + BraecoWaiterUtils.dp2px(80, context)
                    + "/h/" + BraecoWaiterUtils.dp2px(80, context);
        }
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    @Override
    public int getCount() {
        return BraecoWaiterApplication.mSet.size() + 1 + 1;
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (position == getCount() - 1) {
            // all selected
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu_set_edit_all, null);
            TextView allText = (TextView) convertView.findViewById(R.id.all_text);
            final AnimCheckBox check = (AnimCheckBox) convertView.findViewById(R.id.check);
            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    check.setChecked(!check.isChecked());
                    if (mOnCheckListener != null) mOnCheckListener.selectAll(check.isChecked());
                }
            });
            if (selectable) {
                allText.setVisibility(View.VISIBLE);
                check.setVisibility(View.VISIBLE);
                check.setChecked(true);
                for (int i = 0; i < isCheck.size(); i++) {
                    if (!isCheck.get(i)) {
                        check.setChecked(false);
                        break;
                    }
                }
            } else {
                allText.setVisibility(View.INVISIBLE);
                check.setVisibility(View.INVISIBLE);
            }
        } else if (position == getCount() - 2) {
            // new
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu_set_edit_add, null);
        } else {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu_set_edit, null);

            LinearLayout base = (LinearLayout)convertView.findViewById(R.id.base_ly);
            ImageView image = (ImageView)convertView.findViewById(R.id.image);
            TextView name = (TextView)convertView.findViewById(R.id.name);
            MaterialIconView eyeOff = (MaterialIconView)convertView.findViewById(R.id.eye_off);
            AutofitTextView tag = (AutofitTextView)convertView.findViewById(R.id.new_tags);
            TextView price = (TextView)convertView.findViewById(R.id.price);
            TextView qi = (TextView)convertView.findViewById(R.id.qi);
            MaterialIconView icon = (MaterialIconView)convertView.findViewById(R.id.icon);
            final AnimCheckBox check = (AnimCheckBox)convertView.findViewById(R.id.check);

            Picasso.with(parent.getContext())
                    .load(BraecoWaiterApplication.mSet.get(position).get("img") + size)
                    .placeholder(R.drawable.default_200_200)
                    .error(R.drawable.default_200_200)
                    .into(image);

            name.setText((String)BraecoWaiterApplication.mSet.get(position).get("name"));

            if ("combo_sum".equals(BraecoWaiterApplication.mSet.get(position).get("dc_type"))) {
                qi.setVisibility(View.VISIBLE);
                ArrayList<Map<String, Object>> combos
                        = (ArrayList<Map<String, Object>>)BraecoWaiterApplication.mSet.get(position).get("combo");
                price.setText(String.format("%.2f", BraecoWaiterUtils.calculateComboSum(combos, true)));
            } else {
                qi.setVisibility(View.GONE);
                price.setText(String.valueOf(BraecoWaiterApplication.mSet.get(position).get("price")));
            }

            String tagString = (String)BraecoWaiterApplication.mSet.get(position).get("tag");
            if (tagString == null || "null".equals(tagString)) {
                tag.setVisibility(View.INVISIBLE);
            } else {
                tag.setVisibility(View.VISIBLE);
                tag.setText(tagString);
            }

            if ((Boolean)BraecoWaiterApplication.mSet.get(position).get("able")) {
                eyeOff.setVisibility(View.GONE);
            } else {
                eyeOff.setVisibility(View.VISIBLE);
                base.setBackgroundResource(R.color.disable_gray);
            }

            check.setOnCheckedChangeListener(new AnimCheckBox.OnCheckedChangeListener() {
                @Override
                public void onChange(boolean checked) {
                    mOnCheckListener.onCheck(position, checked);
                }
            });

            if (selectable) {
                check.setVisibility(View.VISIBLE);
                check.setChecked(isCheck.get(position));
                icon.setVisibility(View.INVISIBLE);
            } else {
                check.setVisibility(View.GONE);
                icon.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    interface OnCheckListener {
        void onCheck(int position, boolean check);
        void selectAll(boolean selected);
    }

}
