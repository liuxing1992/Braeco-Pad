package com.braeco.braecowaiter.UIs;

import android.view.View;
import android.view.ViewGroup;

import com.daimajia.androidanimations.library.BaseViewAnimator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by Weiping on 2015/12/19.
 */

public class MySlideOutDownAnimator extends BaseViewAnimator {
    @Override
    protected void prepare(View target) {
        ViewGroup parent = (ViewGroup)target.getParent();
        int distance = parent.getHeight() - target.getTop();
        getAnimatorAgent().playTogether(
                ObjectAnimator.ofFloat(target,"translationY",0,distance)
        );
    }
}
