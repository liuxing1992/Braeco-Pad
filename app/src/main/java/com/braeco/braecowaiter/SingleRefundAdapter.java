package com.braeco.braecowaiter;

import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
import java.util.Map;

import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2015/12/4.
 */

public class SingleRefundAdapter extends BaseAdapter {

    private OnOrderListener orderListener;
    public ArrayList<Integer> number = new ArrayList<>();

    public SingleRefundAdapter(OnOrderListener orderListener) {
        calculatePrices();
        this.orderListener = orderListener;
        number = new ArrayList<>();
        for (int i = BraecoWaiterData.refundMeals.size() - 1; i >= 0; i--) number.add(0);
    }

    @Override
    public int getCount() {
        return BraecoWaiterData.refundMeals.size();
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

        final RefundAdapterViewholder holder;

        if (convertView == null) {
            holder = new RefundAdapterViewholder();

            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu_refund, null);

            holder.name = (TextView)convertView.findViewById(R.id.name);
            holder.price = (TextView)convertView.findViewById(R.id.price);
            holder.num = (TextView)convertView.findViewById(R.id.num);
            holder.attributes = (TextView)convertView.findViewById(R.id.attributes);
            holder.tags = (AutofitTextView) convertView.findViewById(R.id.tags);
            holder.minus = (MaterialIconView)convertView.findViewById(R.id.minus);
            holder.plus = (ImageView)convertView.findViewById(R.id.plus);

            convertView.setTag(holder);
        }
        else {
            holder = (RefundAdapterViewholder)convertView.getTag();
        }

        holder.name.setText((String)BraecoWaiterData.refundMeals.get(position).get("name"));
        holder.price.setText("¥ " + String.format("%.2f", Double.parseDouble("" + BraecoWaiterData.refundMeals.get(position).get("price"))));
        holder.num.setText("" + number.get(position));

        String tagsString = "";
        String attributesString = "";
        boolean firstTag = true;
        boolean firstAttribute = true;
        final boolean isSet = (Boolean)BraecoWaiterData.refundMeals.get(position).get("isSet");
        if (isSet) {
            // is set
            holder.tags.setVisibility(View.GONE);
            holder.attributes.setText((String)BraecoWaiterData.refundMeals.get(position).get("properties"));
        } else {
            // is meal
            String[] properties = (String[]) BraecoWaiterData.refundMeals.get(position).get("property");
            for (int i = 0; i < properties.length; i++) {
                String tag = properties[i];
                if (tag.charAt(0) == '*') {
                    if (!firstTag) tagsString += " ";
                    firstTag = false;
                    tagsString += tag.substring(1);
                } else {
                    if (!firstAttribute) attributesString += "、";
                    firstAttribute = false;
                    attributesString += tag;
                }
            }

            holder.tags.setText(tagsString);
            if ("".equals(tagsString)) holder.tags.setVisibility(View.GONE);
            holder.attributes.setText(attributesString);
        }

        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // decrease a meal
                // delete the meal in orderedMeals according to orderedMealsPair
                if (number.get(position) == 0) return;
                number.set(position, number.get(position) - 1);
                holder.num.setText("" + number.get(position));
                orderListener.OnOrderListen();
                SingleRefundAdapter.this.notifyDataSetChanged();
            }
        });
        holder.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // increase a meal
                // add a meal in orderedMeals according to orderedMealsPair
                int sum = (Integer) BraecoWaiterData.refundMeals.get(position).get("sum");
                if (number.get(position) == sum) return;
                number.set(position, number.get(position) + 1);
                holder.num.setText("" + number.get(position));
                orderListener.OnOrderListen();
                SingleRefundAdapter.this.notifyDataSetChanged();
            }
        });

        return convertView;
    }

    private class RefundAdapterViewholder {
        public TextView name;
        public TextView price;
        public TextView num;
        public TextView attributes;
        public AutofitTextView tags;
        public MaterialIconView minus;
        public ImageView plus;
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
                            if (halfNum > 0) {
                                price += (Double) BraecoWaiterApplication.orderedMealsPair.get(k).first.get("fullPrice") / 2;
                                BraecoWaiterApplication.discounts[0] += (Double) BraecoWaiterApplication.orderedMealsPair.get(k).first.get("fullPrice") / 2;
                                halfNum--;
                            } else {
                                price += (Double) BraecoWaiterApplication.orderedMealsPair.get(k).first.get("fullPrice");
                            }
                        }
                        BraecoWaiterApplication.prices[position2] = price;
                    }
                    position2--;
                }
            } else {
                if ("sale".equals(BraecoWaiterApplication.orderedMealsPair.get(i).first.get("dc_type"))) {
                    Double sale = Double.valueOf(
                            (Integer) BraecoWaiterApplication.orderedMealsPair.get(i).first.get("dc"));
                    BraecoWaiterApplication.prices[position] =
                            ((Double) BraecoWaiterApplication.orderedMealsPair.get(i).first.get("fullPrice") - sale)
                            * BraecoWaiterApplication.orderedMealsPair.get(i).second;
                    BraecoWaiterApplication.discounts[1] += sale * BraecoWaiterApplication.orderedMealsPair.get(i).second;
                } else if ("discount".equals(BraecoWaiterApplication.orderedMealsPair.get(i).first.get("dc_type"))) {
                    Double discount = (Double.valueOf((Integer) BraecoWaiterApplication.orderedMealsPair
                            .get(position).first.get("dc"))) / 100;
                    BraecoWaiterApplication.prices[position] = (Double) BraecoWaiterApplication.orderedMealsPair.get(i).first.get("fullPrice")
                            * BraecoWaiterApplication.orderedMealsPair.get(i).second * discount;
                    BraecoWaiterApplication.discounts[2] += (Double) BraecoWaiterApplication.orderedMealsPair.get(i).first.get("fullPrice")
                            * BraecoWaiterApplication.orderedMealsPair.get(i).second * (1 - discount);
                } else {
                    BraecoWaiterApplication.prices[position] = (Double) BraecoWaiterApplication.orderedMealsPair.get(i).first.get("fullPrice")
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
