package com.braeco.braecowaiter.UIs;

import android.view.View;

import com.daimajia.androidanimations.library.BaseViewAnimator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by Weiping on 2015/12/30.
 */
public class MyDownBounceAnimator extends BaseViewAnimator {
    @Override
    public void prepare(View target) {
        getAnimatorAgent().playTogether(
                ObjectAnimator.ofFloat(target,"translationY",30,0,15,0,5,0)
        );
    }
}
