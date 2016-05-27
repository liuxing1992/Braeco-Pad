package com.braeco.braecowaiter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;

/**
 * Created by Weiping on 2015/12/16.
 */
public class UpdatePictureAdapter extends BaseAdapter {

    @Override
    public int getCount() {
        for (int i = 0; i < BraecoWaiterApplication.pictureAddress.length; i++) {
            if ("".equals(BraecoWaiterApplication.pictureAddress[i])) {
                return i + 1;
            }
        }
        return BraecoWaiterApplication.pictureAddress.length + 1;
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

        if (position == getCount() - 1) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_update_picture_last, null);
            return convertView;
        }

        convertView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_update_picture, null);

        SquareImageView image = (SquareImageView)convertView.findViewById(R.id.image);

        Picasso.with(parent.getContext())
                .load(BraecoWaiterApplication.pictureAddress[position])
                .placeholder(R.drawable.empty_logo)
                .error(R.drawable.empty_logo)
                .into(image);

        return convertView;
    }

}
