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
import java.util.HashSet;
import java.util.Map;

import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2016/2/17.
 */
public class MeFragmentMenuSetEditAttributeComboAdapter extends BaseAdapter {

    private OnCategoryClickListener mListener;
    private HashSet<Integer> combos;
    private ArrayList<ArrayList<Map<String, Object>>> allSubMenus;
    private ArrayList<AnimCheckBox> categoryChecks;

    private AnimCheckBox theAllCheck;
    private int selectableMenuSize = -1;

    private boolean[] expanded;

    private SparseBooleanArray expandState = new SparseBooleanArray();

    private ArrayList<String> categoryNames;

    public MeFragmentMenuSetEditAttributeComboAdapter(
            HashSet<Integer> combos,
            OnCategoryClickListener mListener) {
        this.combos = combos;
        this.mListener = mListener;

        for (int i = getCount() - 1; i >= 0; i--) {
            expandState.append(i, true);
        }

        categoryNames = new ArrayList<>();

        // Todo to optimize
        allSubMenus = new ArrayList<>();
        for (int position = 0; position < BraecoWaiterApplication.mButton.size(); position++) {
            ArrayList<Map<String, Object>> subMenus = new ArrayList<>();
            int categoryId = (Integer) BraecoWaiterApplication.mButton.get(position).get("id");
            if (categoryId == -1) continue;
            categoryNames.add((String) BraecoWaiterApplication.mButton.get(position).get("button"));
            int size = BraecoWaiterApplication.mSelectedMenu.size();
            for (int i = 0; i < size; i++) {
                if (BraecoWaiterApplication.mSelectedMenu.get(i).get("categoryid").equals(categoryId)) {
                    Map<String, Object> subMenu = new HashMap<>();
                    subMenu.put("name", BraecoWaiterApplication.mSelectedMenu.get(i).get("name"));
                    subMenu.put("id", BraecoWaiterApplication.mSelectedMenu.get(i).get("id"));
                    int id = (Integer) BraecoWaiterApplication.mSelectedMenu.get(i).get("id");
                    subMenu.put("check", combos.contains(id));
                    subMenus.add(subMenu);
                }
            }
            allSubMenus.add(subMenus);
        }

        categoryChecks = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) categoryChecks.add(null);

        expanded = new boolean[getCount()];
        for (int i = 1; i < getCount(); i++) expanded[i] = true;
    }

    @Override
    public int getCount() {
        // Don't calculate the sets.
        if (BraecoWaiterApplication.mSet != null && BraecoWaiterApplication.mSet.size() != 0)
            return BraecoWaiterApplication.mButton.size();
        else return BraecoWaiterApplication.mButton.size() + 1;
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
            if (getSelectableMenuSize() == combos.size()) check.setChecked(true);
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

            MeFragmentMenuSetEditAttributeComboSubAdapter adapter
                    = new MeFragmentMenuSetEditAttributeComboSubAdapter(
                    new MeFragmentMenuSetEditAttributeComboSubAdapter.OnMenuClickListener() {
                        @Override
                        public void onClick(int id, boolean check, boolean allCheck) {
                            if (check) combos.add(id);
                            else combos.remove(id);
                            categoryCheck.setChecked(allCheck);
                            mListener.selectMeal();
                        }
                    }, allSubMenus.get(position - 1));

            categoryCheck.setChecked(isAllCheck(allSubMenus.get(position - 1)));
            categoryName.setText(categoryNames.get(position - 1) + "（" + allSubMenus.get(position - 1).size() + "）");

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
                    for (int i = 0; i < listView.getCount(); i++) {
                        View view = BraecoWaiterUtils.getViewByPosition(i, listView);
                        AnimCheckBox subCheck = (AnimCheckBox) view.findViewById(R.id.check);
                        if (subCheck == null) continue;
                        subCheck.setChecked(categoryCheck.isChecked());
                    }
                    if (categoryCheck.isChecked()) {
                        for (int i = 0; i < allSubMenus.get(position - 1).size(); i++) {
                            combos.add((Integer) allSubMenus.get(position - 1).get(i).get("id"));
                        }
                    } else {
                        for (int i = 0; i < allSubMenus.get(position - 1).size(); i++) {
                            combos.remove(allSubMenus.get(position - 1).get(i).get("id"));
                        }
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

    public boolean isAllCheck(ArrayList<Map<String, Object>> subMenu) {
        boolean allCheck = true;
        for (int i = 0; i < subMenu.size(); i++) {
            if (!(Boolean)subMenu.get(i).get("check")) {
                allCheck = false;
                break;
            }
        }
        return allCheck;
    }

    public void selectAll(boolean check) {
        for (int i = allSubMenus.size() - 1; i >= 0; i--) {
            int size = allSubMenus.get(i).size();
            for (int j = 0; j < size; j++) {
                allSubMenus.get(i).get(j).put("check", check);
                if (check) combos.add((Integer)allSubMenus.get(i).get(j).get("id"));
                else combos.remove(allSubMenus.get(i).get(j).get("id"));
            }
        }
    }

    public int getSelectableMenuSize() {
        if (selectableMenuSize == -1) {
            selectableMenuSize = 0;
            for (int i = allSubMenus.size() - 1; i >= 0; i--) {
                selectableMenuSize += allSubMenus.get(i).size();
            }
        }
        return selectableMenuSize;
    }

    public void setAllCheck() {
        if (theAllCheck != null) {
            if (getSelectableMenuSize() == combos.size()) theAllCheck.setChecked(true);
            else theAllCheck.setChecked(false);
        }
    }

    public interface OnCategoryClickListener {
        void selectMeal();
        void selectCategory();
        void selectAll(boolean selected);
    }

    public ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "rotation", from, to);
        animator.setDuration(300);
        animator.setInterpolator(Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR));
        return animator;
    }

}
