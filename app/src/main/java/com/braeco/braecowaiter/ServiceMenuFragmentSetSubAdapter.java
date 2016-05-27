package com.braeco.braecowaiter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2016/2/19.
 */
public class ServiceMenuFragmentSetSubAdapter extends BaseAdapter {

    private OnPlusClickListener onPlusClickListener;
    private OnMinusClickListener onMinusClickListener;

    private HashSet<Integer> combos;
    private ArrayList<Map<String, Object>> menus;
    private Integer discount;
    private Integer n;  // the combos number in this set
    private Integer p;  // the position of this combo set

    private static String size = null;

    public ServiceMenuFragmentSetSubAdapter(HashSet<Integer> combos, Integer discount, Integer n, Integer p, OnMinusClickListener onMinusClickListener, OnPlusClickListener onPlusClickListener) {
        this.combos = combos;
        this.discount = discount;
        this.n = n;
        this.p = p;
        this.onMinusClickListener = onMinusClickListener;
        this.onPlusClickListener = onPlusClickListener;

        menus = new ArrayList<>();
        for (Integer id : combos) {
            for (int i = BraecoWaiterApplication.mSettingMenu.size() - 1; i >= 0; i--) {
                if (id.equals(BraecoWaiterApplication.mSettingMenu.get(i).get("id"))) {
                    menus.add(BraecoWaiterApplication.mSettingMenu.get(i));
                    break;
                }
            }
        }

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

        final SetSubAdapterViewholder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_set_combo_select_sub, null);
            holder = new SetSubAdapterViewholder();
            holder.firstDivider = convertView.findViewById(R.id.first_divider);
            holder.base = (LinearLayout)convertView.findViewById(R.id.base_ly);
            holder.image = (PorterShapeImageView)convertView.findViewById(R.id.image);
            holder.name = (TextView)convertView.findViewById(R.id.name);
            holder.price = (TextView)convertView.findViewById(R.id.price);
            holder.num = (AutofitTextView)convertView.findViewById(R.id.num);
            holder.tags = (TextView)convertView.findViewById(R.id.tags);
            holder.tag = (AutofitTextView)convertView.findViewById(R.id.new_tags);
            holder.originalPrice = (TextView)convertView.findViewById(R.id.original_price);
            holder.original = (FrameLayout)convertView.findViewById(R.id.original);
            holder.minus = (MaterialIconView)convertView.findViewById(R.id.minus);
            holder.plus = (MaterialIconView)convertView.findViewById(R.id.plus);

            convertView.setTag(holder);
        }
        else {
            holder = (SetSubAdapterViewholder)convertView.getTag();
        }

        if (position == 0) holder.firstDivider.setVisibility(View.GONE);
        else holder.firstDivider.setVisibility(View.VISIBLE);

        final Integer id = (Integer)menus.get(position).get("id");

        if (size == null) {
            size = "?imageView2/1/w/" + BraecoWaiterUtils.dp2px(80, parent.getContext())
                    + "/h/" + BraecoWaiterUtils.dp2px(80, parent.getContext());
        }

        Picasso.with(parent.getContext())
                .load(menus.get(position).get("img") + size)
                .placeholder(R.drawable.default_200_200)
                .error(R.drawable.default_200_200)
                .into(holder.image);

        holder.name.setText((String)menus.get(position).get("name"));

        if (discount != -2) {
            // is sum type set
            double price = (Double)menus.get(position).get("price");
            double priceAfterDiscount = Double.parseDouble(new DecimalFormat("#.##").format(price * discount / 100));
            holder.price.setText(String.valueOf(priceAfterDiscount));
            boolean isSamePrices = ((int)(price * 100)) == ((int)(priceAfterDiscount * 100));
            if (isSamePrices) {
                holder.original.setVisibility(View.INVISIBLE);
            } else {
                holder.original.setVisibility(View.VISIBLE);
                holder.originalPrice.setText(String.valueOf(price));
            }
        } else {
            holder.price.setText(String.valueOf(menus.get(position).get("price")));
            holder.original.setVisibility(View.INVISIBLE);
        }

        holder.num.setText(ServiceMenuFragmentSet.orderedCombos.get(p).get(id).size() + "");

        holder.tags.setVisibility(View.GONE);

        String tagString = (String)menus.get(position).get("tag");
        if (tagString == null || "null".equals(tagString)) {
            holder.tag.setVisibility(View.INVISIBLE);
        } else {
            holder.tag.setVisibility(View.VISIBLE);
            holder.tag.setText(tagString);
        }

        if (ServiceMenuFragmentSet.orderedCombos.get(p).get(id).size() == 0) {
            holder.minus.setVisibility(View.INVISIBLE);
            holder.num.setVisibility(View.INVISIBLE);
        } else {
            holder.minus.setVisibility(View.VISIBLE);
            holder.num.setVisibility(View.VISIBLE);
        }

        if (!(Boolean)menus.get(position).get("able")) {
            holder.base.setBackgroundResource(R.color.disable_gray);
            holder.plus.setIcon(MaterialDrawableBuilder.IconValue.EYE_OFF);
            holder.plus.setEnabled(false);
            return convertView;
        } else {
            holder.base.setBackgroundResource(R.color.white);
            holder.plus.setIcon(MaterialDrawableBuilder.IconValue.PLUS_CIRCLE);
            holder.plus.setEnabled(true);
        }

        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // decrease a meal
                ServiceMenuFragmentSet.orderedCombos.get(p).get(id).pop();
                holder.num.setText(ServiceMenuFragmentSet.orderedCombos.get(p).get(id).size() + "");
                if (ServiceMenuFragmentSet.orderedCombos.get(p).get(id).size() == 0) {
                    holder.minus.setEnabled(false);
                    YoYo.with(Techniques.FadeOutRight)
                            .duration(700)
                            .playOn(holder.minus);
                    YoYo.with(Techniques.FadeOutRight)
                            .duration(700)
                            .playOn(holder.num);
                } else {
                    holder.minus.setVisibility(View.VISIBLE);
                    holder.num.setVisibility(View.VISIBLE);
                }
                onMinusClickListener.onMinus(id, p);
            }
        });
        holder.plus.setOnClickListener(
                new AddMeal(menus.get(position), parent.getContext(), holder.num, holder.minus, discount));


        return convertView;
    }

    class AddMeal implements View.OnClickListener {

        private Context mContext;

        private TextView price;
        private TextView choice;

        private Map<String, Object> menu;

        private int attributeGroupNum;
        private String[] attributeGroupName;
        private ArrayList<Double>[] attributePrice;
        private ArrayList<String>[] attributeItemName;
        private int[] attributeGroupSize;
        private double sum;
        private int[] choices;
        private double fullPrice;
        private int discount = -2;

        private DialogAttributesAdapter.OnTagClickListener onTagClickListener;

        TextView num;
        MaterialIconView minus;

        public AddMeal(Map<String, Object> menu, Context mContext, TextView num, MaterialIconView minus, int discount) {
            this.menu = menu;
            this.mContext = mContext;
            this.num = num;
            this.minus = minus;
            this.discount = discount;
        }

        @Override
        public void onClick(final View v) {

            final Integer id = (Integer)menu.get("id");

            if (menu.get("dc_type").equals("limit")) {
                Integer limit = (Integer) menu.get("dc");
                if (limit.equals(ServiceMenuFragmentSet.orderedCombos.get(p).get(id).size())) {
                    new MaterialDialog.Builder(mContext)
                            .title("限量供应")
                            .content(menu.get("name") +
                                    "菜品数量不足。")
                            .positiveText("确认")
                            .show();
                    return;
                }
            }

            attributeGroupNum = (Integer) menu.get("num_shuxing");
            attributeGroupName = (String[])menu.get("shuxingName");
            attributePrice = (ArrayList<Double>[])menu.get("addshuxing");
            attributeItemName = (ArrayList<String>[])menu.get("shuxing");
            attributeGroupSize = (int[])menu.get("res");
            sum = (Double)menu.get("price");
            choices = new int[attributeGroupNum];
            for (int i = 0; i < attributeGroupNum; i++) choices[i] = 0;

            boolean noChoices = true;  // whether the user cannot choose
            for (int i = 0; i < attributeGroupNum; i++) {
                if (attributeItemName[i].size() > 1) {
                    noChoices = false;
                    break;
                }
            }

            if (noChoices) {
                // new order

                // calculate price
                double attributePrices = 0;
                for (int i = 0; i < attributeGroupNum; i++) {
                    attributePrices += attributePrice[i].get(choices[i]);
                }
                fullPrice = sum + attributePrices;

                Map<String, Object> newMenu = new HashMap<>(menu);
                newMenu.put("choices", choices);
                newMenu.put("fullPrice", fullPrice);
                // judge limit
                int maxNumber = (Integer)((ArrayList<Map<String, Object>>)ServiceMenuFragmentSet.set.get("combo")).get(p).get("require");
                int selectedNumber = 0;
                for (HashMap.Entry<Integer, Stack<Map<String, Object>>> mealStack : ServiceMenuFragmentSet.orderedCombos.get(p).entrySet()) {
                    selectedNumber += mealStack.getValue().size();
                }
                if (selectedNumber >= maxNumber && maxNumber != 0) {
                    // limit this operation
                    new MaterialDialog.Builder(mContext)
                            .title("已达上限")
                            .content(((ArrayList<Map<String, Object>>)ServiceMenuFragmentSet.set.get("combo")).get(p).get("name") + "的选择已完成，请先删除其他单品。")
                            .positiveText("确认")
                            .show();
                } else {
                    // add the sub meal
                    ServiceMenuFragmentSet.orderedCombos.get(p).get(id).push(newMenu);

                    BraecoWaiterUtils.getInstance().LogMap(newMenu);

                    minus.setVisibility(View.VISIBLE);
                    num.setText(ServiceMenuFragmentSet.orderedCombos.get(p).get(id).size() + "");
                    ServiceMenuFragmentSetSubAdapter.this.notifyDataSetChanged();
                    if ("1".equals(num.getText().toString())) {
                        minus.setEnabled(true);
                        YoYo.with(Techniques.BounceInRight)
                                .duration(500)
                                .playOn(minus);
                        YoYo.with(Techniques.BounceInRight)
                                .duration(500)
                                .playOn(num);
                    }

                    onPlusClickListener.onPlus(id, p);
                }

            } else {

                final MaterialDialog dialog
                        = new MaterialDialog.Builder(mContext)
                        .title((String)menu.get("name"))
                        .customView(R.layout.dialog_service_menu, true)
                        .positiveText("加入购物车")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                if (dialogAction == DialogAction.POSITIVE) {
                                    // new order
                                    Map<String, Object> newMenu = new HashMap<>(menu);
                                    newMenu.put("choices", choices);
                                    newMenu.put("fullPrice", fullPrice);

                                    // judge limit
                                    int maxNumber = (Integer)((ArrayList<Map<String, Object>>)ServiceMenuFragmentSet.set.get("combo")).get(p).get("require");
                                    int selectedNumber = 0;
                                    for (HashMap.Entry<Integer, Stack<Map<String, Object>>> mealStack : ServiceMenuFragmentSet.orderedCombos.get(p).entrySet()) {
                                        selectedNumber += mealStack.getValue().size();
                                    }
                                    if (selectedNumber >= maxNumber && maxNumber != 0) {
                                        // limit this operation
                                        new MaterialDialog.Builder(mContext)
                                                .title("已达上限")
                                                .content(((ArrayList<Map<String, Object>>)ServiceMenuFragmentSet.set.get("combo")).get(p).get("name") + "的选择已完成，请先删除其他单品。")
                                                .positiveText("确认")
                                                .show();
                                    } else {
                                        // add the sub meal
                                        ServiceMenuFragmentSet.orderedCombos.get(p).get(id).push(newMenu);

                                        BraecoWaiterUtils.getInstance().LogMap(newMenu);

                                        minus.setVisibility(View.VISIBLE);
                                        num.setText(ServiceMenuFragmentSet.orderedCombos.get(p).get(id).size() + "");
                                        ServiceMenuFragmentSetSubAdapter.this.notifyDataSetChanged();
                                        if ("1".equals(num.getText().toString())) {
                                            minus.setEnabled(true);
                                            YoYo.with(Techniques.BounceInRight)
                                                    .duration(500)
                                                    .playOn(minus);
                                            YoYo.with(Techniques.BounceInRight)
                                                    .duration(500)
                                                    .playOn(num);
                                        }
                                        onPlusClickListener.onPlus(id, p);
                                    }
                                }
                            }
                        })
                        .show();
                View view = dialog.getCustomView();

                price = (TextView)view.findViewById(R.id.price);
                choice = (TextView)view.findViewById(R.id.choice);

                setPrice();
                setChoices();

                ListView listView = (ListView) view.findViewById(R.id.list_view);
                DialogAttributesAdapter adapter = new DialogAttributesAdapter(new DialogAttributesAdapter.OnTagClickListener() {
                    @Override
                    public void OnTagClick(int tagGroup, int tagPosition) {
                        choices[tagGroup] = tagPosition;
                        setPrice();
                        setChoices();
                    }
                }, menu);
                listView.setAdapter(adapter);

            }
        }

        private void setPrice() {
            // calculate price
            double attributePrices = 0;
            for (int i = 0; i < attributeGroupNum; i++) {
                attributePrices += attributePrice[i].get(choices[i]);
            }
            fullPrice = sum + attributePrices;
            DecimalFormat df = new DecimalFormat("#.##");
            if (discount != -2) price.setText(
                    String.format(
                            "%.2f",
                            Double.parseDouble(df.format(fullPrice * discount / 100))));
            else price.setText(
                    String.format(
                            "%.2f",
                            Double.parseDouble(df.format(fullPrice))));
        }

        private void setChoices() {
            // write choices
            String choicesString = "已选：";
            for (int i = 0; i < attributeGroupNum; i++) {
                if (i != 0) choicesString += "、";
                choicesString += attributeItemName[i].get(choices[i]);
            }
            choice.setText(choicesString);
        }
    }

    private class SetSubAdapterViewholder {
        public View firstDivider;
        public LinearLayout base;
        public PorterShapeImageView image;
        public TextView name;
        public TextView price;
        public AutofitTextView num;
        public TextView tags;
        public AutofitTextView tag;
        public FrameLayout original;
        public TextView originalPrice;
        public MaterialIconView minus;
        public MaterialIconView plus;
    }

    public interface OnPlusClickListener {
        void onPlus(int id, int p);
    }

    public interface OnMinusClickListener {
        void onMinus(int id, int p);
    }
}
