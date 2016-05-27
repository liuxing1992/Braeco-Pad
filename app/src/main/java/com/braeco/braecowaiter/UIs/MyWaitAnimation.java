package com.braeco.braecowaiter.UIs;

import android.view.View;

import com.daimajia.androidanimations.library.BaseViewAnimator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by Weiping on 2015/12/19.
 */

public class MyWaitAnimation extends BaseViewAnimator {

    @Override
    public void prepare(View target) {
        target.setVisibility(View.VISIBLE);
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(target,"translationY",0, 0);
        getAnimatorAgent().playTogether(
                objectAnimator1
        );
    }

}
