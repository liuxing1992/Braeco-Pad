package com.braeco.braecowaiter;

import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
import java.util.Map;

import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2015/12/4.
 */

public class MenuOrderAdapter extends BaseAdapter {

    OnOrderListener orderListener;

    public MenuOrderAdapter(OnOrderListener orderListener) {
        calculatePrices();
        this.orderListener = orderListener;
    }

    @Override
    public int getCount() {
        if (BraecoWaiterApplication.orderHasDiscount)
            return BraecoWaiterApplication.orderedMealsPair.size() + 1;
        else
            return BraecoWaiterApplication.orderedMealsPair.size();
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
    public View getView(final int position, View convertView, final ViewGroup parent) {

        MenuAdapterViewholder holder = null;
        MenuAdapterLastViewholder holder2 = null;

        if (position == BraecoWaiterApplication.orderedMealsPair.size()) {
            holder2 = new MenuAdapterLastViewholder();

            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu_car_last, null);

            holder2.halfNum = (TextView)convertView.findViewById(R.id.half_num);
            holder2.saleNum = (TextView)convertView.findViewById(R.id.sale_num);
            holder2.discountNum = (TextView)convertView.findViewById(R.id.discount_num);

            convertView.setTag(R.layout.item_menu_car_last, holder2);
        } else {
            holder = new MenuAdapterViewholder();

            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu_car, null);

            holder.name = (TextView)convertView.findViewById(R.id.name);
            holder.price = (TextView)convertView.findViewById(R.id.price);
            holder.num = (AutofitTextView) convertView.findViewById(R.id.num);
            holder.attributes = (TextView)convertView.findViewById(R.id.attributes);
            holder.listViewLayout = (LinearLayout)convertView.findViewById(R.id.listview_layout);
            holder.listView = (ExpandedListView)convertView.findViewById(R.id.list_view);
            holder.tags = (TextView) convertView.findViewById(R.id.tags);
            holder.minus = (MaterialIconView)convertView.findViewById(R.id.minus);
            holder.plus = (MaterialIconView)convertView.findViewById(R.id.plus);

            convertView.setTag(R.layout.item_menu_car, holder);
        }

        if (position == BraecoWaiterApplication.orderedMealsPair.size()) {
            holder2.halfNum.setText("- ¥ " + String.format("%.2f", BraecoWaiterApplication.discounts[0]));
            holder2.saleNum.setText("- ¥ " + String.format("%.2f", BraecoWaiterApplication.discounts[1]));
            holder2.discountNum.setText("- ¥ " + String.format("%.2f", BraecoWaiterApplication.discounts[2]));
        } else {
            holder.name.setText((String)BraecoWaiterApplication.orderedMealsPair.get(position).first.get("name"));
            holder.price.setText("¥ " + String.format("%.2f", BraecoWaiterApplication.orderedMealsPair.get(position).first.get("fullPrice")));
            holder.num.setText("" + BraecoWaiterApplication.orderedMealsPair.get(position).second);

            int[] choices = (int[])BraecoWaiterApplication.orderedMealsPair.get(position).first.get("choices");
            int attributeGroupNum = (Integer) BraecoWaiterApplication.orderedMealsPair.get(position).first.get("num_shuxing");
            ArrayList<String>[] attributeItemName = (ArrayList<String>[])BraecoWaiterApplication.orderedMealsPair.get(position).first.get("shuxing");

            if (BraecoWaiterApplication.orderedMealsPair.get(position).first.containsKey("isSet")
                    && (Boolean)BraecoWaiterApplication.orderedMealsPair.get(position).first.get("isSet")) {
                // is set
                ServiceMenuFragmentCarSetItemAdapter subAdapter = new ServiceMenuFragmentCarSetItemAdapter(
                        (ArrayList<ArrayList<Map<String, Object>>>)BraecoWaiterApplication.orderedMealsPair.get(position).first.get("properties"),
                        (Integer)BraecoWaiterApplication.orderedMealsPair.get(position).first.get("id"));
                holder.listView.setAdapter(subAdapter);
                holder.listViewLayout.setVisibility(View.VISIBLE);
                holder.attributes.setVisibility(View.GONE);
            } else {
                String attributesString = "";
                for (int i = 0; i < attributeGroupNum; i++) {
                    if (i != 0) attributesString += "、";
                    attributesString += attributeItemName[i].get(choices[i]);
                }
                holder.attributes.setText(attributesString);
                holder.attributes.setVisibility(View.VISIBLE);
                holder.listViewLayout.setVisibility(View.GONE);
            }

            String tagString = "";
            String tag = (String)BraecoWaiterApplication.orderedMealsPair.get(position).first.get("dc_type");
            if ("sale".equals(tag)) {
                tagString = "减" + String.valueOf(
                        BraecoWaiterApplication.orderedMealsPair.get(position).first.get("dc")) + "元";
            }
            if ("discount".equals(tag)) {
                tagString = String.format("%1.1f",
                        (Float.valueOf((Integer)BraecoWaiterApplication.orderedMealsPair
                                .get(position).first.get("dc"))) / 10) + "折";
            }
            if ("half".equals(tag)) {
                tagString = "第二杯半价";
            }
            if ("limit".equals(tag)) {
                tagString = "限量供应";
            }
            if ("".equals(tagString) || !BraecoWaiterApplication.orderHasDiscount) {
                holder.tags.setVisibility(View.INVISIBLE);
            } else {
                holder.tags.setVisibility(View.VISIBLE);
                holder.tags.setText(tagString);
            }
            if (BraecoWaiterApplication.orderedMealsPair.get(position).second == 0) {
                holder.minus.setVisibility(View.INVISIBLE);
                holder.num.setVisibility(View.INVISIBLE);
            } else {
                holder.minus.setVisibility(View.VISIBLE);
                holder.num.setVisibility(View.VISIBLE);
            }
            final MenuAdapterViewholder tempHolder = holder;
            holder.minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // decrease a meal
                    // delete the meal in orderedMeals according to orderedMealsPair
                    boolean quitLoop = false;
                    for (int i = 0; !quitLoop && i <  BraecoWaiterApplication.orderedMeals.size(); i++) {
                        for (int j = BraecoWaiterApplication.orderedMeals.get(i).size() - 1; j >= 0; j--) {
                            if (BraecoWaiterUtils.getInstance().isSameMeal(
                                    BraecoWaiterApplication.orderedMeals.get(i).get(j),
                                    BraecoWaiterApplication.orderedMealsPair.get(position).first)) {
                                BraecoWaiterApplication.orderedMeals.get(i).remove(j);
                                quitLoop = true;
                                break;
                            }
                        }
                    }
                    BraecoWaiterApplication.orderedMealsPair.set(position,
                            new Pair<>(BraecoWaiterApplication.orderedMealsPair.get(position).first,
                                    BraecoWaiterApplication.orderedMealsPair.get(position).second - 1));
                    tempHolder.num.setText((BraecoWaiterApplication.orderedMealsPair.get(position).second) + "");
                    if (BraecoWaiterApplication.orderedMealsPair.get(position).second.equals(0)) {
                        tempHolder.minus.setVisibility(View.INVISIBLE);
                        tempHolder.num.setVisibility(View.INVISIBLE);
                        BraecoWaiterApplication.orderedMealsPair.remove(position);
                    } else {
                        tempHolder.minus.setVisibility(View.VISIBLE);
                        tempHolder.num.setVisibility(View.VISIBLE);
                    }
                    calculatePrices();
                    orderListener.OnOrderListen();
                    MenuOrderAdapter.this.notifyDataSetChanged();
                }
            });
            holder.plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // increase a meal
                    // add a meal in orderedMeals accord  ing to orderedMealsPair

                    if (BraecoWaiterApplication.orderedMealsPair.get(position).first.get("dc_type").equals("limit")) {
                        Integer limit = (Integer) BraecoWaiterApplication.orderedMealsPair.get(position).first.get("dc");
                        for (int i = 0; i < BraecoWaiterApplication.orderedMeals.size(); i++) {
                            if (BraecoWaiterApplication.orderedMeals.get(i).size() != 0 &&
                                    BraecoWaiterApplication.orderedMeals.get(i).peek().get("name")
                                            .equals(BraecoWaiterApplication.orderedMealsPair.get(position).first.get("name"))) {
                                if (limit.equals(BraecoWaiterApplication.orderedMeals.get(i).size())) {
                                    new MaterialDialog.Builder(parent.getContext())
                                            .title("限量供应")
                                            .content("您已经选择到了此产品上限。")
                                            .positiveText("确认")
                                            .show();
                                    return;
                                }
                            }
                        }
                    }

                    for (int i = 0; i < BraecoWaiterApplication.orderedMeals.size(); i++) {
                        if (BraecoWaiterApplication.orderedMeals.get(i).size() != 0 &&
                                BraecoWaiterApplication.orderedMeals.get(i).peek().get("name")
                                        .equals(BraecoWaiterApplication.orderedMealsPair.get(position).first.get("name"))) {
                            BraecoWaiterApplication.orderedMeals.get(i).push(
                                    BraecoWaiterApplication.orderedMealsPair.get(position).first);
                            break;
                        }
                    }
                    BraecoWaiterApplication.orderedMealsPair.set(position,
                            new Pair<>(BraecoWaiterApplication.orderedMealsPair.get(position).first,
                                    BraecoWaiterApplication.orderedMealsPair.get(position).second + 1));
                    tempHolder.num.setText(BraecoWaiterApplication.orderedMealsPair.get(position).second + "");
                    calculatePrices();
                    orderListener.OnOrderListen();
                    MenuOrderAdapter.this.notifyDataSetChanged();
                }
            });
        }

        return convertView;
    }

    private class MenuAdapterViewholder {
        public TextView name;
        public TextView price;
        public AutofitTextView num;
        public TextView attributes;
        public LinearLayout listViewLayout;
        public ExpandedListView listView;
        public TextView tags;
        public MaterialIconView minus;
        public MaterialIconView plus;
    }

    private class MenuAdapterLastViewholder {
        TextView halfNum;
        TextView saleNum;
        TextView discountNum;
    }

    private void calculatePrices() {
        BraecoWaiterApplication.discounts = new double[]{0, 0, 0};
        BraecoWaiterApplication.prices = new double[BraecoWaiterApplication.orderedMealsPair.size()];
        int position = 0;
        for (int i = 0; i < BraecoWaiterApplication.prices.length; i++) BraecoWaiterApplication.prices[i] = -1;
        for (int i = 0; i < BraecoWaiterApplication.orderedMealsPair.size(); i++) {
            if (BraecoWaiterApplication.prices[position] != -1) {
                position++;
                continue;
            }
            if ("half".equals(BraecoWaiterApplication.orderedMealsPair.get(i).first.get("dc_type"))) {
                // if this is the fucking "second cup is half"
                int nums = 0;
                // if they have the same name, they are all fucking second cup is half
                for (Pair<Map<String, Object>, Integer> key : BraecoWaiterApplication.orderedMealsPair) {
                    if (key.first.get("name").equals(
                            BraecoWaiterApplication.orderedMealsPair.get(i).first.get("name"))) {
                        nums += key.second;
                    }
                }
                int halfNum = nums / 2;
                int position2 = BraecoWaiterApplication.orderedMealsPair.size() - 1;
                for (int k = BraecoWaiterApplication.orderedMealsPair.size() - 1; k >= 0; k--) {
                    if (BraecoWaiterApplication.orderedMealsPair.get(i).first.get("name")
                            .equals(BraecoWaiterApplication.orderedMealsPair.get(k).first.get("name"))) {
                        double price = 0;
                        for (int j = BraecoWaiterApplication.orderedMealsPair.get(k).second - 1; j >= 0; j--) {
                            if (halfNum > 0 && BraecoWaiterApplication.orderHasDiscount) {
                                price += (Double)BraecoWaiterApplication.orderedMealsPair.get(k).first.get("fullPrice") / 2;
                                BraecoWaiterApplication.discounts[0] += (Double)BraecoWaiterApplication.orderedMealsPair.get(k).first.get("fullPrice") / 2;
                                halfNum--;
                            } else {
                                price += (Double)BraecoWaiterApplication.orderedMealsPair.get(k).first.get("fullPrice");
                            }
                        }
                        BraecoWaiterApplication.prices[position2] = price;
                    }
                    position2--;
                }
            } else {
                if ("sale".equals(BraecoWaiterApplication.orderedMealsPair.get(i).first.get("dc_type"))
                        && BraecoWaiterApplication.orderHasDiscount) {
                    Double sale = Double.valueOf(
                            (Integer)BraecoWaiterApplication.orderedMealsPair.get(i).first.get("dc"));
                    BraecoWaiterApplication.prices[position] =
                            ((Double)BraecoWaiterApplication.orderedMealsPair.get(i).first.get("fullPrice") - sale)
                            * BraecoWaiterApplication.orderedMealsPair.get(i).second;
                    BraecoWaiterApplication.discounts[1] += sale * BraecoWaiterApplication.orderedMealsPair.get(i).second;
                } else if ("discount".equals(BraecoWaiterApplication.orderedMealsPair.get(i).first.get("dc_type"))
                        && BraecoWaiterApplication.orderHasDiscount) {
                    Double discount = (Double.valueOf((Integer)BraecoWaiterApplication.orderedMealsPair
                            .get(position).first.get("dc"))) / 100;
                    BraecoWaiterApplication.prices[position] = (Double)BraecoWaiterApplication.orderedMealsPair.get(i).first.get("fullPrice")
                            * BraecoWaiterApplication.orderedMealsPair.get(i).second * discount;
                    BraecoWaiterApplication.discounts[2] += (Double)BraecoWaiterApplication.orderedMealsPair.get(i).first.get("fullPrice")
                            * BraecoWaiterApplication.orderedMealsPair.get(i).second * (1 - discount);
                } else {
                    BraecoWaiterApplication.prices[position] = (Double)BraecoWaiterApplication.orderedMealsPair.get(i).first.get("fullPrice")
                            * BraecoWaiterApplication.orderedMealsPair.get(i).second;
                }
            }
            position++;
        }
    }

    public interface OnOrderListener {
        void OnOrderListen();
    }

}
