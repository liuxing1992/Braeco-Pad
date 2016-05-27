package com.braeco.braecowaiter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.lguipeng.library.animcheckbox.AnimCheckBox;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2015/12/18.
 */
public class MeFragmentMenuMenuAdapter extends BaseAdapter {

    private OnCheckListener mOnCheckListener;
    private int buttonPosition = -1;
    private ArrayList<Boolean> isCheck;

    private boolean selectable = false;
    private static String size = null;

    public MeFragmentMenuMenuAdapter(int buttonPosition, Context context, OnCheckListener onCheckListener, ArrayList<Boolean> isCheck) {
        this.mOnCheckListener = onCheckListener;
        this.buttonPosition = buttonPosition;
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
        return BraecoWaiterApplication.b[buttonPosition + 1] - BraecoWaiterApplication.b[buttonPosition] + 1 + 1;
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
                    .inflate(R.layout.item_menu_menu_edit_all, null);
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
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu_menu_edit_add, null);
        } else {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu_menu_edit, null);

            LinearLayout base = (LinearLayout)convertView.findViewById(R.id.base_ly);
            ImageView image = (ImageView)convertView.findViewById(R.id.image);
            TextView name = (TextView)convertView.findViewById(R.id.name);
            MaterialIconView eyeOff = (MaterialIconView)convertView.findViewById(R.id.eye_off);
            AutofitTextView redTag = (AutofitTextView)convertView.findViewById(R.id.tags);
            AutofitTextView orangeTag = (AutofitTextView)convertView.findViewById(R.id.new_tags);
            TextView price = (TextView)convertView.findViewById(R.id.price);
            TextView qi = (TextView)convertView.findViewById(R.id.qi);
            qi.setVisibility(View.GONE);
            FrameLayout original = (FrameLayout)convertView.findViewById(R.id.original);
            TextView originalPrice = (TextView)convertView.findViewById(R.id.original_price);
            MaterialIconView icon = (MaterialIconView)convertView.findViewById(R.id.icon);
            final AnimCheckBox check = (AnimCheckBox)convertView.findViewById(R.id.check);

            int p = BraecoWaiterApplication.b[buttonPosition] + position;

            Picasso.with(parent.getContext())
                    .load(BraecoWaiterApplication.mSettingMenu.get(p).get("img") + size)
                    .placeholder(R.drawable.default_200_200)
                    .error(R.drawable.default_200_200)
                    .into(image);

            name.setText((String) BraecoWaiterApplication.mSettingMenu.get(p).get("name"));

            price.setText(String.valueOf(BraecoWaiterApplication.mSettingMenu
                    .get(p).get("price")));

            original.setVisibility(View.INVISIBLE);
            String redTagString = null;
            if ("sale".equals(BraecoWaiterApplication.mSettingMenu.get(p).get("dc_type"))) {
                redTagString = "减" + String.valueOf((int) BraecoWaiterApplication
                        .mSettingMenu.get(p).get("dc")) + "元";
                originalPrice.setText(String.valueOf(BraecoWaiterApplication.mSettingMenu
                        .get(p).get("price")));
                price.setText(String.format("%.2f",
                        ((double) BraecoWaiterApplication.mSettingMenu
                                .get(p).get("price")
                                - (int) BraecoWaiterApplication.mSettingMenu.get(p).get("dc"))));
                original.setVisibility(View.VISIBLE);
            } else if ("discount".equals(BraecoWaiterApplication.mSettingMenu.get(p).get("dc_type"))) {
                redTagString = String.format("%1.1f",
                        (Float.valueOf((Integer) BraecoWaiterApplication
                                .mSettingMenu.get(p).get("dc"))) / 10) + "折";
                originalPrice.setText(String.valueOf(BraecoWaiterApplication.mSettingMenu.get(p).get("price")));
                price.setText(String.format("%.2f",
                        ((double) BraecoWaiterApplication.mSettingMenu
                                .get(p).get("price")
                                * (Float.valueOf((Integer) BraecoWaiterApplication
                                .mSettingMenu.get(p).get("dc"))) / 100)));
                original.setVisibility(View.VISIBLE);
            } else if ("half".equals(BraecoWaiterApplication.mSettingMenu.get(p).get("dc_type"))) {
                redTagString = "第二杯半价";
            } else if ("limit".equals(BraecoWaiterApplication.mSettingMenu.get(p).get("dc_type"))) {
                redTagString = "剩" + BraecoWaiterApplication.mSettingMenu.get(p).get("dc") + "件";
            } else if ("combo_only".equals(BraecoWaiterApplication
                    .mSettingMenu.get(p).get("dc_type"))) {
                redTagString = "仅在套餐中出现";
            } else if ("combo_sum".equals(BraecoWaiterApplication
                    .mSettingMenu.get(p).get("dc_type"))) {
                qi.setVisibility(View.VISIBLE);
                ArrayList<Map<String, Object>> combos = (ArrayList<Map<String, Object>>) BraecoWaiterApplication.mSettingMenu.get(p).get("combo");
                price.setText(String.format("%.2f", BraecoWaiterUtils.calculateComboSum(combos, true)));
            }

            if (BraecoWaiterUtils.notNull(redTagString)) {
                redTag.setText(redTagString);
                redTag.setVisibility(View.VISIBLE);
            } else redTag.setVisibility(View.GONE);

            String orangeTagString = (String) BraecoWaiterApplication.mSettingMenu.get(p).get("tag");
            if (BraecoWaiterUtils.notNull(orangeTagString)) {
                orangeTag.setVisibility(View.VISIBLE);
                orangeTag.setText(orangeTagString);
            } else orangeTag.setVisibility(View.INVISIBLE);

            if ((Boolean) BraecoWaiterApplication.mSettingMenu.get(p).get("able")) {
                eyeOff.setVisibility(View.GONE);
            } else {
                eyeOff.setVisibility(View.VISIBLE);
                base.setBackgroundResource(R.color.disable_gray);
            }

            check.setVisibility(View.INVISIBLE);
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
