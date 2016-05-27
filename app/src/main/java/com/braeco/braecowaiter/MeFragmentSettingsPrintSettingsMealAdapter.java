package com.braeco.braecowaiter;

import android.animation.ObjectAnimator;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.github.aakira.expandablelayout.Utils;
import com.github.lguipeng.library.animcheckbox.AnimCheckBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2016/2/17.
 */

public class MeFragmentSettingsPrintSettingsMealAdapter extends BaseAdapter {

    public static final String[] EXTRACT_NAME = new String[]{"退款单", "呼叫服务", "日结", "支付宝二维码", "满减", "会员卡充值", "会员卡优惠", "满送"};
    public static final int[] EXTRACT_ID = new int[]{-4, -3, -2, -1, 0, 1, 2, 3};

    private ArrayList<ArrayList<Map<String, Object>>> allSubMenus;
    private ArrayList<Map<String, Object>> categories;

    private SparseBooleanArray expandState = new SparseBooleanArray();

    private boolean[] expanded;
    private OnCategoryClickListener mListener;
    private AnimCheckBox theAllCheck;
    private boolean existSet = false;

    private ArrayList<MeFragmentSettingsPrintSettingsMealSubAdapter> adapters;

    public MeFragmentSettingsPrintSettingsMealAdapter(OnCategoryClickListener mListener) {

        this.mListener = mListener;

        for (int i = getCount() - 1; i >= 0; i--) {
            expandState.append(i, true);
        }

        // Notice in printer settings we should get meals from all meals.
        categories = new ArrayList<>();
        allSubMenus = new ArrayList<>();

        for (int position = 0; position < BraecoWaiterApplication.mButton.size(); position++) {
            Map<String, Object> category = new HashMap<>();
            int categoryId = (Integer) BraecoWaiterApplication.mButton.get(position).get("id");
            if (categoryId == -1) continue;  // this category is for set
            category.put("name", BraecoWaiterApplication.mButton.get(position).get("button") + "");
            category.put("id", categoryId);
            categories.add(category);

            ArrayList<Map<String, Object>> subMenus = new ArrayList<>();
            for (int i = 0; i < BraecoWaiterApplication.mSettingMenu.size(); i++) {
                if (BraecoWaiterApplication.mSettingMenu.get(i).get("categoryid").equals(categoryId)) {
                    Map<String, Object> subMenu = new HashMap<>();
                    subMenu.put("name", BraecoWaiterApplication.mSettingMenu.get(i).get("name"));
                    int id = (Integer) BraecoWaiterApplication.mSettingMenu.get(i).get("id");
                    subMenu.put("id", id);
                    subMenu.put("check", !BraecoWaiterApplication.modifyingPrinter.getBan().contains(id));
                    subMenus.add(subMenu);
                }
            }
            allSubMenus.add(subMenus);
        }

        // The other "meals".
        Map<String, Object> category = new HashMap<>();
        category.put("name", "其他选项");
        int categoryId = -1;
        category.put("id", categoryId);
        categories.add(category);

        ArrayList<Map<String, Object>> subMenus = new ArrayList<>();
        for (int i = 0; i < EXTRACT_NAME.length; i++) {
            Map<String, Object> subMenu = new HashMap<>();
            subMenu.put("name", EXTRACT_NAME[i]);
            subMenu.put("id", EXTRACT_ID[i]);
            subMenu.put("check", !BraecoWaiterApplication.modifyingPrinter.getBan().contains(EXTRACT_ID[i]));
            subMenus.add(subMenu);
        }
        allSubMenus.add(subMenus);

        for (int i = 0; i < allSubMenus.size(); i++) {
            categories.get(i).put("name", categories.get(i).get("name") + "（" + allSubMenus.get(i).size() + "）");
        }

        expanded = new boolean[getCount()];
        for (int i = 1; i < getCount(); i++) expanded[i] = true;
    }

    public void setAllCheck() {
        if (theAllCheck != null) {
            if (BraecoWaiterApplication.modifyingPrinter.getBan().size() == 0) theAllCheck.setChecked(true);
            else theAllCheck.setChecked(false);
        }
    }

    public void selectAll(boolean selected) {
        for (int i = 0; i < allSubMenus.size(); i++) {
            for (int j = 0; j < allSubMenus.get(i).size(); j++) {
                allSubMenus.get(i).get(j).put("check", selected);
            }
        }
    }

    @Override
    public int getCount() {
        // Don't calculate the sets.
        if (BraecoWaiterApplication.mSet != null && BraecoWaiterApplication.mSet.size() != 0)
            return BraecoWaiterApplication.mButton.size() + 1;
        else return BraecoWaiterApplication.mButton.size() + 1 + 1;
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
        if (position == 0) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_printer_meal_select_all, null);
            RelativeLayout all = (RelativeLayout)convertView.findViewById(R.id.all);
            final AnimCheckBox check = (AnimCheckBox)convertView.findViewById(R.id.check);
            all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    check.setChecked(!check.isChecked());
                    mListener.selectAll(check.isChecked());
                }
            });
            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    check.setChecked(!check.isChecked());
                    mListener.selectAll(check.isChecked());
                }
            });
            if (BraecoWaiterApplication.modifyingPrinter.getBan().size() == 0) check.setChecked(true);
            else check.setChecked(false);
            theAllCheck = check;
            return convertView;
        } else {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_printer_meal_select, null);
            final LinearLayout category = (LinearLayout)convertView.findViewById(R.id.category);
            final RelativeLayout triangle = (RelativeLayout)convertView.findViewById(R.id.button);
            AutofitTextView categoryName = (AutofitTextView)convertView.findViewById(R.id.category_name);
            final AnimCheckBox categoryCheck = (AnimCheckBox)convertView.findViewById(R.id.check);
            final ExpandedListView listView = (ExpandedListView)convertView.findViewById(R.id.list_view);

            MeFragmentSettingsPrintSettingsMealSubAdapter adapter
                    = new MeFragmentSettingsPrintSettingsMealSubAdapter(allSubMenus.get(position - 1),
                    new MeFragmentSettingsPrintSettingsMealSubAdapter.OnMenuClickListener() {
                @Override
                public void onClick(int id, boolean check) {
                    if (check) {
                        if (!categoryCheck.isChecked()) categoryCheck.setChecked(true);
                        BraecoWaiterApplication.modifyingPrinter.getBanCategory().remove(categories.get(position - 1).get("id"));
                    }
                    if (mListener != null) mListener.selectMeal();
                }
            });
            categoryCheck.setChecked(!BraecoWaiterApplication.modifyingPrinter.getBanCategory().contains(categories.get(position - 1).get("id")));
            categoryName.setText((String)categories.get(position - 1).get("name"));

            listView.setAdapter(adapter);
            listView.setFocusable(false);

            triangle.setRotation(expandState.get(position) ? 0f : -90f);
            category.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listView.getVisibility() == View.GONE) {
                        createRotateAnimator(triangle, -90f, 0f).start();
                        listView.setVisibility(View.VISIBLE);
                        expanded[position] = true;
                    } else {
                        createRotateAnimator(triangle, 0f, -90f).start();
                        listView.setVisibility(View.GONE);
                        expanded[position] = false;
                    }
                }
            });

            if (!expanded[position]) listView.setVisibility(View.GONE);
            else listView.setVisibility(View.VISIBLE);
            triangle.setRotation(expanded[position] ? 0f : -90f);

            categoryCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    categoryCheck.setChecked(!categoryCheck.isChecked());
                    if (categoryCheck.isChecked()) BraecoWaiterApplication.modifyingPrinter.getBanCategory().remove(categories.get(position - 1).get("id"));
                    else BraecoWaiterApplication.modifyingPrinter.getBanCategory().add((Integer) categories.get(position - 1).get("id"));
                    for (int i = 0; i < listView.getCount(); i++) {
                        View view = BraecoWaiterUtils.getViewByPosition(i, listView);
                        AnimCheckBox subCheck = (AnimCheckBox) view.findViewById(R.id.check);
                        if (subCheck == null) continue;
                        subCheck.setChecked(categoryCheck.isChecked());
                        if (categoryCheck.isChecked()) BraecoWaiterApplication.modifyingPrinter.getBan().remove(allSubMenus.get(position - 1).get(i).get("id"));
                        else BraecoWaiterApplication.modifyingPrinter.getBan().add((Integer)allSubMenus.get(position - 1).get(i).get("id"));
                    }
                    for (int i = 0; i < allSubMenus.get(position - 1).size(); i++) {
                        allSubMenus.get(position - 1).get(i).put("check", categoryCheck.isChecked());
                    }
                    mListener.selectCategory();
                }
            });

            return convertView;
        }
    }

    public ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "rotation", from, to);
        animator.setDuration(300);
        animator.setInterpolator(Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR));
        return animator;
    }

    public interface OnCategoryClickListener {
        void selectMeal();
        void selectCategory();
        void selectAll(boolean selected);
    }

}
