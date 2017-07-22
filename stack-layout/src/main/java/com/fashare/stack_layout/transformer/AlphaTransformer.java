package com.fashare.stack_layout.transformer;

import android.view.View;

import com.fashare.stack_layout.StackLayout;

/**
 * User: fashare(153614131@qq.com)
 * Date: 2017-07-19
 * Time: 00:33
 * <br/>
 *
 * 左右滑动时的透明度渐变
 */
public final class AlphaTransformer extends StackLayout.PageTransformer {
    private float mMinAlpha = 0f;
    private float mMaxAlpha = 1f;

    public AlphaTransformer(float minAlpha, float maxAlpha) {
        mMinAlpha = minAlpha;
        mMaxAlpha = maxAlpha;
    }

    public AlphaTransformer() {
        this(0f, 1f);
    }

    @Override
    public void transformPage(View view, float position, boolean isSwipeLeft) {
        if (position > -1 && position <= 0) { // (-1,0]
            view.setVisibility(View.VISIBLE);

            view.setAlpha(mMaxAlpha - (mMaxAlpha-mMinAlpha) * Math.abs(position));
        } else{
            view.setAlpha(mMaxAlpha);
        }
    }
}