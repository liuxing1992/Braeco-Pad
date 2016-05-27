package com.braeco.braecowaiter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;

/**
 * Created by Weiping on 2015/12/16.
 */
public class CustomTextSliderView extends BaseSliderView {
    private static Typeface font = null;
    private Context context ;

    protected CustomTextSliderView(Context context) {
        super(context);
    }

    @Override
    public View getView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.render_type_text,null);
        ImageView target = (ImageView)v.findViewById(R.id.daimajia_slider_image);
        TextView description = (TextView)v.findViewById(R.id.description);
        description.setText(getDescription());
        description.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        description.setPadding(0, 0, 20, 0);
        bindEventAndShow(v, target);
        return v;
    }
}
