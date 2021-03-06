package com.braeco.braecowaiter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by Weiping on 2015/12/11.
 */

public class ExpandedGridView extends GridView {

    private android.view.ViewGroup.LayoutParams params;
    private int old_count = 0;

    public ExpandedGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
