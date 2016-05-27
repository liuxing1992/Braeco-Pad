package com.braeco.braecowaiter;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Weiping on 2016/2/29.
 */
public class ServiceMenuFragmentCarSetItemAdapter extends BaseAdapter {

    private ArrayList<ArrayList<Map<String, Object>>> combos;
    private ArrayList<Pair<Map<String, Object>, Integer>> combosInPair;
    private int id;

    public ServiceMenuFragmentCarSetItemAdapter(ArrayList<ArrayList<Map<String, Object>>> combos, int id) {
        this.combos = combos;
        this.id = id;

        // calculate the same meal
        combosInPair = new ArrayList<>();
        for (ArrayList<Map<String, Object>> meals : combos) {
            for (Map<String, Object> meal : meals) {
                // for every meal(id, properties only)
                boolean exist = false;
                int index = 0;
                for (Pair<Map<String, Object>, Integer> pair : combosInPair) {
                    if (BraecoWaiterUtils.isSameSubMeal(pair.first, meal)) {
                        // this meal is put already
                        combosInPair.set(index, new Pair<Map<String, Object>, Integer>(meal, pair.second + 1));
                        exist = true;
                        break;
                    }
                    index++;
                }
                if (!exist) {
                    // this meal is not put
                    combosInPair.add(new Pair<Map<String, Object>, Integer>(meal, 1));
                }
            }
        }
    }

    @Override
    public int getCount() {
        return combosInPair.size();
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
                .inflate(R.layout.item_set_detail, null);

        String name = "";
        String propertiesString = "（";

        int id = (Integer)combosInPair.get(position).first.get("id");
        for (Map<String, Object> menu : BraecoWaiterApplication.mSettingMenu) {
            if ((Integer)menu.get("id") == id) {
                name = (String)menu.get("name");
                break;
            }
        }
        boolean isFirstProperty = true;
        for (String property : (ArrayList<String>)combosInPair.get(position).first.get("properties")) {
            if (!isFirstProperty) propertiesString += "、";
            isFirstProperty = false;
            propertiesString += property;
        }
        propertiesString += "）";

        ((TextView)convertView.findViewById(R.id.name)).setText(name + ("（）".equals(propertiesString) ? "" : propertiesString));
        ((TextView)convertView.findViewById(R.id.num)).setText("*" + combosInPair.get(position).second);

        return convertView;
    }
}
