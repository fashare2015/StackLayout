package com.fashare.stack_layout.widget;

import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.view.View;

/**
 * User: fashare(153614131@qq.com)
 * Date: 2017-05-09
 * Time: 22:46
 * <br/><br/>
 *
 * 滑动到指定位置, 实现卡片回弹的效果
 */
public class ScrollManager {
    private ViewDragHelper mViewDragHelper;

    public ScrollManager(ViewDragHelper viewDragHelper) {
        mViewDragHelper = viewDragHelper;
    }

    public void scrollTo(View child, int finalLeft, int finalTop){
        child.setLeft(finalLeft);
        child.setTop(finalTop);
        ViewCompat.postInvalidateOnAnimation(child);
    }

    public void smoothScrollTo(View child, int finalLeft, int finalTop){
        smoothScrollTo(child, finalLeft, finalTop, null);
    }

    public void smoothScrollTo(View child, int finalLeft, int finalTop, Callback scrollCallback){
        mViewDragHelper.smoothSlideViewTo(child, finalLeft, finalTop);
        ViewCompat.postOnAnimation(child, new SettleRunnable(child, finalLeft, scrollCallback));
    }

    /**
     * smoothScrollTo() 中用到 mScroller,
     * 以此 SettleRunnable 代替 @Override computeScroll().
     *
     * copy from {@link BottomSheetBehavior}
     */
    private class SettleRunnable implements Runnable {
        private final View mView;
        private final Callback mScrollCallback;

        private final float mOriginLeft, mFinalLeft;

        public SettleRunnable(View view, int finalLeft, Callback scrollCallback) {
            mView = view;
            mScrollCallback = scrollCallback;
            mOriginLeft = mView.getLeft();
            mFinalLeft = finalLeft;
        }

        @Override
        public void run() {
            if (mViewDragHelper != null){
                if(mViewDragHelper.continueSettling(true)) {
                    ViewCompat.postOnAnimation(mView, this);    // 递归调用
                }else{
                    if(mScrollCallback != null)
                        mScrollCallback.onComplete(mView);
                }
            }
        }
    }

    public interface Callback{
        void onComplete(View view);
    }
}
