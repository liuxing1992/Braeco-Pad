package com.braeco.braecowaiter;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import me.gujun.android.taggroup.TagGroup;

/**
 * Created by Weiping on 2015/12/9.
 */
public class DialogAttributesAdapter extends BaseAdapter {

    private OnTagClickListener onTagClickListener;

    private Map<String, Object> meal;
    private String[] attributeGroupName;
    private ArrayList<Double>[] attributePrice;
    private ArrayList<String>[] attributeItemName;
    private int[] attributeGroupSize;
    private int attributeGroupNum;
    private int[] choices;

    DialogAttributesAdapter(OnTagClickListener onTagClickListener, Map<String, Object> meal) {
        this.onTagClickListener = onTagClickListener;
        this.meal = meal;
        attributeGroupName = (String[])meal.get("shuxingName");
        attributePrice = (ArrayList<Double>[])meal.get("addshuxing");
        attributeItemName = (ArrayList<String>[])meal.get("shuxing");
        attributeGroupSize = (int[])meal.get("res");
        attributeGroupNum = (Integer) meal.get("num_shuxing");
        choices = new int[attributeGroupNum];
        for (int i = 0; i < attributeGroupNum; i++) choices[i] = 0;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public int getCount() {
        return attributeGroupNum;
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

        convertView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dialog_attributes, null);

        TextView groupsName = (TextView)convertView.findViewById(R.id.group_name);
        final TagGroup tags = (TagGroup)convertView.findViewById(R.id.tags);

        groupsName.setText(attributeGroupName[position]);
        tags.setTags(attributeItemName[position]);

        for (int i = 0; i < tags.getTags().length; i++) {
            if (i == choices[position]) {
                tags.getChildAt(i).setBackgroundResource(R.drawable.shape_rounded_tag);
                Resources resource = parent.getContext().getResources();
                ColorStateList csl = resource.getColorStateList(R.color.attribute_select_text_color);
                if (csl != null) {
                    ((TextView)tags.getChildAt(i)).setTextColor(csl);
                }
            } else {
                tags.getChildAt(i).setBackgroundResource(0);
                ((TextView)tags.getChildAt(i)).setTextColor(Color.parseColor("#000000"));
            }
        }

        tags.setOnTagClickListener(new TagGroup.OnTagClickListener() {
            @Override
            public void onTagClick(String tag) {
                for (int i = 0; i < tags.getTags().length; i++) {
                    if (tag.equals(tags.getTags()[i])) {
                        choices[position] = i;
                        onTagClickListener.OnTagClick(position, i);
                        tags.getChildAt(i).setBackgroundResource(R.drawable.shape_rounded_tag);
                        Resources resource = parent.getContext().getResources();
                        ColorStateList csl = resource.getColorStateList(R.color.attribute_select_text_color);
                        if (csl != null) {
                            ((TextView)tags.getChildAt(i)).setTextColor(csl);
                        }
                    } else {
                        tags.getChildAt(i).setBackgroundResource(0);
                        ((TextView)tags.getChildAt(i)).setTextColor(Color.parseColor("#000000"));
                    }
                }
            }
        });

        return convertView;
    }

    public interface OnTagClickListener {
        void OnTagClick(int tagGroup, int tagPosition);
    }

}
