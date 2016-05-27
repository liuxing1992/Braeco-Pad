package com.braeco.braecowaiter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.braeco.braecowaiter.UIs.ExpandableLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2016/2/19.
 */
public class ServiceMenuFragmentSetAdapter extends BaseAdapter {

    private ServiceMenuFragmentSetSubAdapter.OnPlusClickListener onPlusClickListener;
    private ServiceMenuFragmentSetSubAdapter.OnMinusClickListener onMinusClickListener;
    private Map<String, Object> set;
    private ArrayList<HashMap<String, Object>> combos;

    private HashMap<Integer, Boolean> hide;

    public ServiceMenuFragmentSetAdapter(Map<String, Object> set, ServiceMenuFragmentSetSubAdapter.OnPlusClickListener onPlusClickListener, ServiceMenuFragmentSetSubAdapter.OnMinusClickListener onMinusClickListener) {
        this.set = set;
        this.onPlusClickListener = onPlusClickListener;
        this.onMinusClickListener = onMinusClickListener;

        combos = (ArrayList<HashMap<String, Object>>)set.get("combo");

        hide = new HashMap<>();
    }

    public void setExpandState(int position, boolean state) {
        hide.put(position, !state);
    }

    @Override
    public int getCount() {
        return combos.size();
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

        convertView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_set_combo_select, null);

        Map<String, Object> combo = combos.get(position);

        LinearLayout title = (LinearLayout)convertView.findViewById(R.id.combo);
        AutofitTextView name = (AutofitTextView)convertView.findViewById(R.id.name);
        name.setText((String)combo.get("name"));
        AutofitTextView num = (AutofitTextView)convertView.findViewById(R.id.number);
        int maxNumber = (Integer)combo.get("require");
        num.setText(maxNumber == 0 ? "可任意选择" : "N选" + maxNumber);
        AutofitTextView selected = (AutofitTextView)convertView.findViewById(R.id.selected);
        int selectedNumber = 0;
        for (Stack<Map<String, Object>> menu : ServiceMenuFragmentSet.orderedCombos.get(position).values()) {
            selectedNumber += menu.size();
        }
        selected.setText(selectedNumber == 0 ? "尚未选择" : "已选" + selectedNumber + "款");
        final RelativeLayout button = (RelativeLayout) convertView.findViewById(R.id.button);
        final ExpandableLayout expandableLayout = (ExpandableLayout)convertView.findViewById(R.id.expandable_layout);
        ExpandedListView listView = (ExpandedListView)convertView.findViewById(R.id.list_view);
        ServiceMenuFragmentSetSubAdapter adapter = new ServiceMenuFragmentSetSubAdapter((HashSet<Integer>)combo.get("content"), (Integer)combo.get("discount"), ((HashSet<Integer>)combo.get("content")).size(), position, onMinusClickListener, onPlusClickListener);
        listView.setAdapter(adapter);

        if (hided(position)) expandableLayout.hideImmediately();
        else expandableLayout.showImmediately();

        button.setRotation(hided(position) ? 180f : 0f);
        View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (hided(position)) expandableLayout.show();
                else expandableLayout.hide();
                BraecoWaiterUtils.createRotateAnimator(button, hided(position) ? 180f : 0f, hided(position) ? 0f : 180f).start();
                hide.put(position, !hided(position));
            }
        };
        button.setOnClickListener(mOnClickListener);
        title.setOnClickListener(mOnClickListener);

        return convertView;
    }

    private boolean hided(int position) {
        return hide.containsKey(position) && hide.get(position);
    }

}
