package com.fashare.stack_layout.transformer;

import android.view.View;

import com.fashare.stack_layout.StackLayout;

/**
 * User: fashare(153614131@qq.com)
 * Date: 2017-07-19
 * Time: 00:33
 * <br/>
 *
 * 左右滑动时的角度效果
 */
public final class AngleTransformer extends StackLayout.PageTransformer {
    private float mMinAngle = -30f;
    private float mMaxAngle = 0f;

    public AngleTransformer(float minAngle, float maxAngle) {
        mMinAngle = minAngle;
        mMaxAngle = maxAngle;
    }

    public AngleTransformer() {
        this(-30f, 0f);
    }

    @Override
    public void transformPage(View view, float position, boolean isSwipeLeft) {
        View parent = (View) view.getParent();

        int pageWidth = parent.getMeasuredWidth();
        int pageHeight = parent.getMeasuredHeight();

        view.setPivotX(pageWidth/2);
        view.setPivotY(pageHeight);

        if (position > -1 && position <= 0) { // (-1,0]
            view.setVisibility(View.VISIBLE);

            view.setRotation((mMaxAngle - (mMaxAngle-mMinAngle) * Math.abs(position)) * (isSwipeLeft? 1: -1));
        } else{
            view.setRotation(mMaxAngle);
        }
    }
}