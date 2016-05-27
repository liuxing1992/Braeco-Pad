package com.braeco.braecowaiter.UIs;

/**
 * Created by Weiping on 2016/5/18.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.braeco.braecowaiter.R;

public class ExpandableLayout extends RelativeLayout
{
    private Boolean isAnimationRunning = false;
    private Boolean isOpened = false;
    private Integer duration;
    private FrameLayout contentLayout;
    private Animation animation;

    public ExpandableLayout(Context context)
    {
        super(context);
    }

    public ExpandableLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    public ExpandableLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attrs) {
        final View rootView = View.inflate(context, R.layout.ui_expandable_layout, this);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableLayout);
        final int contentID = typedArray.getResourceId(R.styleable.ExpandableLayout_nbContentLayout, -1);
        contentLayout = (FrameLayout) rootView.findViewById(R.id.view_expandable_contentLayout);

        if (contentID == -1)
            throw new IllegalArgumentException("ContentLayout cannot be null!");

        if (isInEditMode())
            return;

        duration = typedArray.getInt(R.styleable.ExpandableLayout_nbDuration, 500);
        final View contentView = View.inflate(context, contentID, null);
        contentView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        contentLayout.addView(contentView);
        contentLayout.setVisibility(GONE);
        typedArray.recycle();
    }

    public void showImmediately() {
        contentLayout.setVisibility(VISIBLE);
        contentLayout.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
        contentLayout.requestLayout();
    }

    public void hideImmediately() {
        contentLayout.setVisibility(GONE);
    }

    public void hide() {
        if (isAnimationRunning) return;
        isAnimationRunning = true;
        contentLayout.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final int targetHeight = contentLayout.getMeasuredHeight();
        contentLayout.setVisibility(VISIBLE);

        ValueAnimator valueAnimator = ValueAnimator.ofInt(targetHeight, 0).setDuration(500);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animator) {
                getLayoutParams().height = (int) animator.getAnimatedValue();
                requestLayout();
            }
        });
        valueAnimator.addListener(mAnimatorListenerAdapter);
        valueAnimator.start();
    }

    public void show() {
        if (isAnimationRunning) return;
        isAnimationRunning = true;
        contentLayout.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final int targetHeight = contentLayout.getMeasuredHeight();
        contentLayout.setVisibility(VISIBLE);

        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, targetHeight).setDuration(500);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animator) {
                getLayoutParams().height = (int) animator.getAnimatedValue();
                requestLayout();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAnimationRunning = false;
                getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                requestLayout();
            }
        });
        valueAnimator.start();
    }

    private AnimatorListenerAdapter mAnimatorListenerAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            isAnimationRunning = false;
        }
    };
}