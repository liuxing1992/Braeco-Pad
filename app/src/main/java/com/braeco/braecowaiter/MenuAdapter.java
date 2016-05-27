package com.braeco.braecowaiter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.Authority;
import com.braeco.braecowaiter.Model.AuthorityManager;
import com.braeco.braecowaiter.UIs.MyUpBounceAnimator;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.hb.views.PinnedSectionListView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.grantland.widget.AutofitTextView;

/**
 * Created by Weiping on 2015/12/4.
 */
public class MenuAdapter extends BaseAdapter
        implements
        PinnedSectionListView.PinnedSectionListAdapter,
        DialogAttributesAdapter.OnTagClickListener {

    OnOrderListener orderListener;

    public MenuAdapter(OnOrderListener orderListener) {
        this.orderListener = orderListener;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == 1;
    }

    @Override
    public int getCount() {
        return BraecoWaiterApplication.mButton.size() + BraecoWaiterApplication.mMenu.size();
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

        final MenuAdapterViewholder holder;

        if (convertView == null) {
            holder = new MenuAdapterViewholder();
            if (BraecoWaiterApplication.isPinned[position]) {
                // is a section
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_menu_section, null);
                holder.name = (TextView)convertView.findViewById(R.id.section);
            } else {
                // is a menu
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_menu, null);
                holder.base = (FrameLayout)convertView.findViewById(R.id.base_ly);
                holder.name = (TextView)convertView.findViewById(R.id.name);
                holder.price = (TextView)convertView.findViewById(R.id.price);
                holder.qi = (TextView)convertView.findViewById(R.id.qi);
                holder.num = (AutofitTextView)convertView.findViewById(R.id.num);
                holder.tags = (TextView)convertView.findViewById(R.id.tags);
                holder.tag = (AutofitTextView)convertView.findViewById(R.id.new_tags);
                holder.originalPrice = (TextView)convertView.findViewById(R.id.original_price);
                holder.original = (FrameLayout)convertView.findViewById(R.id.original);
                holder.minus = (MaterialIconView)convertView.findViewById(R.id.minus);
                holder.plus = (MaterialIconView)convertView.findViewById(R.id.plus);
            }

            convertView.setTag(holder);
        }
        else {
            holder = (MenuAdapterViewholder)convertView.getTag();
        }

        if (BraecoWaiterApplication.isPinned[position]) {
            // is a section
            holder.name.setText((String) BraecoWaiterApplication.mButton
                    .get(BraecoWaiterApplication.index[position]).get("button"));
        } else {
            // is a menu
            holder.name.setText((String) BraecoWaiterApplication.mMenu
                    .get(BraecoWaiterApplication.index[position]).get("name"));
            holder.price.setText(String.valueOf(BraecoWaiterApplication.mMenu
                    .get(BraecoWaiterApplication.index[position]).get("price")));

            holder.num.setText("" + BraecoWaiterApplication.orderedMeals
                    .get(BraecoWaiterApplication.index[position]).size());
            holder.original.setVisibility(View.INVISIBLE);
            List<String> tagStrings = new ArrayList<>();

            holder.qi.setVisibility(View.GONE);
            if (BraecoWaiterApplication.orderHasDiscount) {
                if ("sale".equals(BraecoWaiterApplication
                        .mMenu.get(BraecoWaiterApplication.index[position]).get("dc_type"))) {
                    tagStrings.add("减" + String.valueOf((int) BraecoWaiterApplication
                            .mMenu.get(BraecoWaiterApplication.index[position]).get("dc")) + "元");
                    holder.originalPrice.setText(String.valueOf(BraecoWaiterApplication.mMenu
                            .get(BraecoWaiterApplication.index[position]).get("price")));
                    holder.price.setText(String.format("%.2f",
                            ((double) BraecoWaiterApplication.mMenu
                                    .get(BraecoWaiterApplication.index[position]).get("price")
                                    - (int) BraecoWaiterApplication.mMenu.get(
                                    BraecoWaiterApplication.index[position]).get("dc"))));
                    holder.original.setVisibility(View.VISIBLE);
                } else if ("discount".equals(BraecoWaiterApplication
                        .mMenu.get(BraecoWaiterApplication.index[position]).get("dc_type"))) {
                    tagStrings.add(String.format("%1.1f",
                            (Float.valueOf((Integer) BraecoWaiterApplication
                                    .mMenu.get(BraecoWaiterApplication.index[position]).get("dc"))) / 10) + "折");
                    holder.originalPrice.setText(String.valueOf(BraecoWaiterApplication.mMenu
                            .get(BraecoWaiterApplication.index[position]).get("price")));
                    holder.price.setText(String.format("%.2f",
                            ((double) BraecoWaiterApplication.mMenu
                                    .get(BraecoWaiterApplication.index[position]).get("price")
                                    * (Float.valueOf((Integer) BraecoWaiterApplication
                                    .mMenu.get(BraecoWaiterApplication.index[position]).get("dc"))) / 100)));
                    holder.original.setVisibility(View.VISIBLE);
                } else if ("half".equals(BraecoWaiterApplication
                        .mMenu.get(BraecoWaiterApplication.index[position]).get("dc_type"))) {
                    tagStrings.add("第二杯半价");
                } else if ("limit".equals(BraecoWaiterApplication
                        .mMenu.get(BraecoWaiterApplication.index[position]).get("dc_type"))) {
                    tagStrings.add("剩" + BraecoWaiterApplication
                            .mMenu.get(BraecoWaiterApplication.index[position]).get("dc") + "件");
                } else if ("combo_static".equals(BraecoWaiterApplication.mMenu.get(BraecoWaiterApplication.index[position]).get("dc_type"))) {
                    // this is a set
                    holder.original.setVisibility(View.GONE);
                } else if ("combo_sum".equals(BraecoWaiterApplication.mMenu.get(BraecoWaiterApplication.index[position]).get("dc_type"))) {
                    // this is a set
                    holder.qi.setVisibility(View.VISIBLE);
                    holder.original.setVisibility(View.GONE);
                    holder.price.setText(String.format("%.2f", BraecoWaiterUtils.calculateComboSum(
                            (ArrayList<Map<String, Object>>) BraecoWaiterApplication.mMenu.get(BraecoWaiterApplication.index[position]).get("combo"),
                            true
                    )));
                } else {
                    holder.qi.setVisibility(View.GONE);
                    holder.original.setVisibility(View.GONE);
                }
            } else {
                holder.qi.setVisibility(View.GONE);
                holder.original.setVisibility(View.GONE);
                tagStrings.clear();
            }

            if (tagStrings.size() > 0) {
                holder.tags.setText(tagStrings.get(0));
                holder.tags.setVisibility(View.VISIBLE);
            } else {
                holder.tags.setVisibility(View.GONE);
            }

            String tagString = (String) BraecoWaiterApplication
                    .mMenu.get(BraecoWaiterApplication.index[position]).get("tag");
            if (tagString == null || "null".equals(tagString)) {
                holder.tag.setVisibility(View.INVISIBLE);
            } else {
                holder.tag.setVisibility(View.VISIBLE);
                holder.tag.setText(tagString);
            }

            if (BraecoWaiterApplication.orderedMeals
                    .get(BraecoWaiterApplication.index[position]).size() == 0) {
                holder.minus.setVisibility(View.INVISIBLE);
                holder.num.setVisibility(View.INVISIBLE);
            } else {
                holder.minus.setVisibility(View.VISIBLE);
                holder.num.setVisibility(View.VISIBLE);
            }

            if (!(Boolean) BraecoWaiterApplication.mMenu.get(BraecoWaiterApplication.index[position]).get("able")) {
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
                    for (int i = BraecoWaiterApplication.orderedMealsPair.size() - 1; i >= 0; i--) {
                        if (BraecoWaiterUtils.getInstance().isSameMeal(
                                BraecoWaiterApplication.orderedMealsPair.get(i).first,
                                BraecoWaiterApplication.orderedMeals
                                        .get(BraecoWaiterApplication.index[position]).peek())) {
                            Integer newNum
                                    = BraecoWaiterApplication.orderedMealsPair.get(i).second - 1;
                            if (newNum == 0) {
                                BraecoWaiterApplication.orderedMealsPair.remove(i);
                            } else {
                                BraecoWaiterApplication.orderedMealsPair.set(i,
                                        new Pair<>(BraecoWaiterApplication.orderedMealsPair.get(i).first, newNum));
                            }
                            break;
                        }
                    }
                    BraecoWaiterApplication.orderedMeals
                            .get(BraecoWaiterApplication.index[position]).pop();
                    holder.num.setText(BraecoWaiterApplication.orderedMeals
                            .get(BraecoWaiterApplication.index[position]).size() + "");
                    if (BraecoWaiterApplication.orderedMeals
                            .get(BraecoWaiterApplication.index[position]).size() == 0) {
                        holder.minus.setEnabled(false);
                        YoYo.with(Techniques.FadeOutRight)
                                .duration(700)
                                .playOn(holder.minus);
                        YoYo.with(Techniques.FadeOutRight)
                                .duration(700)
                                .playOn(holder.num);
//                        holder.minus.setVisibility(View.INVISIBLE);
//                        holder.num.setVisibility(View.INVISIBLE);
                    } else {
                        holder.minus.setVisibility(View.VISIBLE);
                        holder.num.setVisibility(View.VISIBLE);
                    }
                    orderListener.OnOrderListen(true);
                }
            });
            holder.plus.setOnClickListener(
                    new AddMeal(position, parent.getContext(), holder.num, holder.minus));
        }
        return convertView;
    }

    @Override public int getViewTypeCount() {
        return 2;
    }

    @Override public int getItemViewType(int position) {
        return BraecoWaiterApplication.isPinned[position] ? 1 : 0;
    }

    @Override
    public void OnTagClick(int tagGroup, int tagPosition) {

    }

    private class MenuAdapterViewholder {
        public FrameLayout base;
        public TextView name;
        public TextView price;
        public TextView qi;
        public AutofitTextView num;
        public TextView tags;
        public AutofitTextView tag;
        public FrameLayout original;
        public TextView originalPrice;
        public MaterialIconView minus;
        public MaterialIconView plus;
    }

    class AddMeal implements View.OnClickListener {

        private Context mContext;
        private Activity activity;
        private int position;

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

        private DialogAttributesAdapter.OnTagClickListener onTagClickListener;

        TextView num;
        MaterialIconView minus;

        public AddMeal(int position, Context mContext, TextView num, MaterialIconView minus) {
            this.position = position;
            if (mContext instanceof Activity) {
                activity = (Activity) mContext;
            }
            this.mContext = mContext;
            this.num = num;
            this.minus = minus;
        }

        @Override
        public void onClick(final View v) {
            if ("combo_static".equals(BraecoWaiterApplication.mMenu.get(BraecoWaiterApplication.index[position]).get("dc_type"))
                    || "combo_sum".equals(BraecoWaiterApplication.mMenu.get(BraecoWaiterApplication.index[position]).get("dc_type"))) {
                Intent intent = new Intent(mContext, ServiceMenuFragmentSet.class);
                intent.putExtra("id", (Integer) BraecoWaiterApplication.mMenu.get(BraecoWaiterApplication.index[position]).get("id"));
                ((Activity)mContext).startActivityForResult(intent, ServiceMenuFragment.START_SET);
                return;
            }

            if (BraecoWaiterApplication.mMenu.get(BraecoWaiterApplication.index[position]).get("dc_type").equals("limit")) {
                Integer limit = (Integer) BraecoWaiterApplication.mMenu.get(BraecoWaiterApplication.index[position]).get("dc");
                if (limit.equals(BraecoWaiterApplication.orderedMeals.get(BraecoWaiterApplication.index[position]).size())) {
                    new MaterialDialog.Builder(mContext)
                            .title("限量供应")
                            .content((String) BraecoWaiterApplication.mMenu.get(BraecoWaiterApplication.index[position]).get("name") +
                                    "菜品数量不足。")
                            .positiveText("确认")
                            .show();
                    return;
                }
            }

            menu = BraecoWaiterApplication.mMenu.get(BraecoWaiterApplication.index[position]);

            attributeGroupNum = (Integer) menu.get("num_shuxing");
            attributeGroupName = (String[])menu.get("shuxingName");
            attributePrice = (ArrayList<Double>[])menu.get("addshuxing");
            attributeItemName = (ArrayList<String>[])menu.get("shuxing");
            attributeGroupSize = (int[])menu.get("res");
            sum = (Double) BraecoWaiterApplication.mMenu
                    .get(BraecoWaiterApplication.index[position]).get("price");
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
                BraecoWaiterApplication.orderedMeals
                        .get(BraecoWaiterApplication.index[position])
                        .push(newMenu);

                BraecoWaiterUtils.getInstance().LogMap(newMenu);

                boolean isExist = false;
                for (int i = BraecoWaiterApplication.orderedMealsPair.size() - 1; i >= 0; i--) {
                    if (BraecoWaiterUtils.getInstance().isSameMeal(
                            BraecoWaiterApplication.orderedMealsPair.get(i).first,
                            newMenu)) {
                        Integer newNum = BraecoWaiterApplication.orderedMealsPair.get(i).second + 1;
                        BraecoWaiterApplication.orderedMealsPair.set(i,
                                new Pair<>(BraecoWaiterApplication.orderedMealsPair.get(i).first, newNum));
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    BraecoWaiterApplication.orderedMealsPair.add(new Pair<>(newMenu, 1));
                }

                minus.setVisibility(View.VISIBLE);
                num.setText(BraecoWaiterApplication.orderedMeals
                        .get(BraecoWaiterApplication.index[position]).size() + "");
                MenuAdapter.this.notifyDataSetChanged();
                if ("1".equals(num.getText().toString())) {
                    minus.setEnabled(true);
                    YoYo.with(Techniques.BounceInRight)
                            .duration(500)
                            .playOn(minus);
                    YoYo.with(Techniques.BounceInRight)
                            .duration(500)
                            .playOn(num);
                }

                orderListener.OnOrderListen(false);
                int[] startLocation = new int[2];
                v.getLocationInWindow(startLocation);  // get location
                // create a "ball"
                TextView ball = new TextView(mContext);
                ball.setBackgroundResource(R.drawable.tips_textview_bg);
                ball.setText("1");
                ball.setGravity(Gravity.CENTER);
                ball.setTextColor(Color.parseColor("#FFFFFF"));
                Resources r = mContext.getResources();
                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());
                ball.setHeight((int)px);
                ball.setWidth((int)px);
                // send the start location to activity
                orderListener.OnAnimationListen(ball, startLocation);
                // plus button animation
                YoYo.with(new MyUpBounceAnimator())
                        .duration(500)
                        .playOn(v);
            } else {

                final MaterialDialog dialog
                        = new MaterialDialog.Builder(mContext)
                        .title((String) BraecoWaiterApplication.mMenu.get(BraecoWaiterApplication.index[position]).get("name"))
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
                                    BraecoWaiterApplication.orderedMeals
                                            .get(BraecoWaiterApplication.index[position])
                                            .push(newMenu);

                                    BraecoWaiterUtils.getInstance().LogMap(newMenu);

                                    boolean isExist = false;
                                    for (int i = BraecoWaiterApplication.orderedMealsPair.size() - 1; i >= 0; i--) {
                                        if (BraecoWaiterUtils.getInstance().isSameMeal(
                                                BraecoWaiterApplication.orderedMealsPair.get(i).first,
                                                newMenu)) {
                                            Integer newNum = BraecoWaiterApplication.orderedMealsPair.get(i).second + 1;
                                            BraecoWaiterApplication.orderedMealsPair.set(i,
                                                    new Pair<>(BraecoWaiterApplication.orderedMealsPair.get(i).first, newNum));
                                            isExist = true;
                                            break;
                                        }
                                    }
                                    if (!isExist) {
                                        BraecoWaiterApplication.orderedMealsPair.add(new Pair<>(newMenu, 1));
                                    }

                                    minus.setVisibility(View.VISIBLE);
                                    num.setText(BraecoWaiterApplication.orderedMeals
                                            .get(BraecoWaiterApplication.index[position]).size() + "");
                                    MenuAdapter.this.notifyDataSetChanged();
                                    if ("1".equals(num.getText().toString())) {
                                        minus.setEnabled(true);
                                        YoYo.with(Techniques.BounceInRight)
                                                .duration(500)
                                                .playOn(minus);
                                        YoYo.with(Techniques.BounceInRight)
                                                .duration(500)
                                                .playOn(num);
                                    }
                                    orderListener.OnOrderListen(false);
                                    int[] startLocation = new int[2];
                                    v.getLocationInWindow(startLocation);
                                    TextView ball = new TextView(mContext);
                                    ball.setBackgroundResource(R.drawable.tips_textview_bg);
                                    ball.setText("1");
                                    ball.setGravity(Gravity.CENTER);
                                    ball.setTextColor(Color.parseColor("#FFFFFF"));
                                    Resources r = mContext.getResources();
                                    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());
                                    ball.setHeight((int)px);
                                    ball.setWidth((int)px);
                                    orderListener.OnAnimationListen(ball, startLocation);
                                    // plus button animation
                                    YoYo.with(new MyUpBounceAnimator())
                                            .duration(500)
                                            .playOn(v);
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
            price.setText(String.valueOf(fullPrice));
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

    public interface OnOrderListener {
        void OnOrderListen(boolean setNum);
        void OnAnimationListen(final View v, int[] startLocation);
    }

}
