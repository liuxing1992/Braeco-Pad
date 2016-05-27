package com.braeco.braecowaiter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.braeco.braecowaiter.Model.Activity;
import com.github.siyamed.shapeimageview.mask.PorterShapeImageView;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialIconView;

/**
 * Created by Weiping on 2015/12/24.
 */
public class MeFragmentActivityAdapter extends BaseAdapter {

    private String size = null;

    public MeFragmentActivityAdapter(Context context) {
        if (size == null) {
            size = "?imageView2/1/w/" + BraecoWaiterUtils.dp2px(80, context)
                    + "/h/" + BraecoWaiterUtils.dp2px(80, context);
        }
    }

    @Override
    public int getCount() {
        return BraecoWaiterData.activities.size();
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

        Activity activity = BraecoWaiterData.activities.get(position);

        switch (activity.getType()) {
            case SECTION_REDUCE:
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_activity_section, null);
                TextView sectionReduce = (TextView) convertView.findViewById(R.id.section);
                sectionReduce.setText("促销活动");
                break;
            case SECTION_THEME:
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_activity_section, null);
                TextView sectionTheme = (TextView) convertView.findViewById(R.id.section);
                sectionTheme.setText("主题活动");
                break;
            case ADD:
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_activity_new, null);
                TextView newTV = (TextView) convertView.findViewById(R.id.new_text);
                MaterialIconView icon = (MaterialIconView) convertView.findViewById(R.id.icon);
                newTV.setVisibility(View.VISIBLE);
                icon.setVisibility(View.VISIBLE);

                if (position == getCount() - 1) convertView.findViewById(R.id.divider).setVisibility(View.VISIBLE);
                break;
            default:
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_activity, null);
                PorterShapeImageView image = (PorterShapeImageView) convertView.findViewById(R.id.image);
                TextView name = (TextView) convertView.findViewById(R.id.name);
                TextView intro = (TextView) convertView.findViewById(R.id.intro);
                icon = (MaterialIconView) convertView.findViewById(R.id.icon);

                Picasso.with(parent.getContext())
                        .load(BraecoWaiterData.activities.get(position).getPicture() + size)
                        .placeholder(R.drawable.default_200_200)
                        .error(R.drawable.default_200_200)
                        .into(image);

                name.setText(BraecoWaiterData.activities.get(position).getTitle());
                intro.setText(Html.fromHtml(BraecoWaiterData.activities.get(position).getIntroduction()));
                icon.setVisibility(View.VISIBLE);
                break;
        }

        return convertView;
    }

}
