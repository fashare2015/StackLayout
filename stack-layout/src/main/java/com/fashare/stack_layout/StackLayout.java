package com.fashare.stack_layout;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by jinliangshan on 17/5/9.
 */
public class StackLayout extends LinearLayout {

    public StackLayout(Context context) {
        super(context);
        init();
    }

    public StackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    // ------ Adapter ------
    private Adapter mAdapter;
    ItemObserver mItemObserver = new ItemObserver();

    public Adapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
        onSetAdapter(adapter);
    }

    private void onSetAdapter(Adapter adapter) {
        adapter.registerDataSetObserver(mItemObserver);
        mItemObserver.dataChanged(adapter);
    }

    public static abstract class Adapter<VH extends ViewHolder>{
        // ------------ ViewHolder -------------
        public abstract VH onCreateViewHolder(ViewGroup parent, int position);

        public abstract void onBindViewHolder(VH holder, int position);

        public abstract int getItemCount();

        private VH getViewHolder(ViewGroup parent, int position){
            VH viewHolder = onCreateViewHolder(parent, position);
            if(viewHolder != null) {
                onBindViewHolder(viewHolder, position);
                return viewHolder;
            }else{
                throw new IllegalArgumentException("onCreateViewHolder() -> viewHolder is null");
            }
        }

        // --------- DataSetObservable ---------
        private final DataSetObservable mObservable = new DataSetObservable();

        public void notifyDataSetChanged() {
            mObservable.notifyChanged();
        }

        public void registerDataSetObserver(DataSetObserver observer) {
            mObservable.registerObserver(observer);
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            mObservable.unregisterObserver(observer);
        }
    }

    public static abstract class ViewHolder {
        public final View itemView;

        public ViewHolder(View itemView) {
            if (itemView == null) {
                throw new IllegalArgumentException("itemView may not be null");
            }
            this.itemView = itemView;
        }
    }

    private class ItemObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            dataChanged(mAdapter);
        }

        @Override
        public void onInvalidated() {
            dataChanged(mAdapter);
        }

        private void dataChanged(Adapter adapter) {
            StackLayout.this.removeAllViews();
            for(int i=0; i<adapter.getItemCount(); i++) {
                ViewHolder viewHolder = adapter.getViewHolder(StackLayout.this, i);
                StackLayout.this.addView(viewHolder.itemView);
            }
        }
    }

    // ------ 事件分发 ------
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mViewDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    private static final float TOUCH_SLOP_SENSITIVITY = 1.f;
    private DragCallback mDragCallback = new DragCallback();
    private ViewDragHelper mViewDragHelper = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, mDragCallback);

    private class DragCallback extends ViewDragHelper.Callback{
        // 仅捕获 mNestedScrollingChild
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            boolean captureView = true;


            return captureView;
        }

        // 控制边界, 防止 mNestedScrollingChild 的头部超出边界
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
//            if(child == mNestedScrollingChild){
//                int newTop = top;
//                newTop = Math.max(newTop, mOriginTop);
//                newTop = Math.min(newTop, mOriginTop + mDragToDismissRange);
//                return newTop;
//            }
            return top;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left;
        }

        //
//        // 需要指定 "滑动范围", 否则不会捕获 mNestedScrollingChild
//        @Override
//        public int getViewVerticalDragRange(View child) {
//            return mDragToDismissRange;
//        }
//
//        // 手指释放的时候回调
//        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            smoothScrollTo(releasedChild, 0);
        }
    }

    // ------ 滑动部分 begin ------
    private void smoothScrollTo(View child, int finalTop){
        mViewDragHelper.smoothSlideViewTo(child, 0, finalTop);
        ViewCompat.postOnAnimation(child, new SettleRunnable(child));
    }

    /**
     * smoothScrollTo() 中用到 mScroller,
     * 以此 SettleRunnable 代替 @Override computeScroll().
     *
     * copy from {@link BottomSheetBehavior}
     */
    private class SettleRunnable implements Runnable {

        private final View mView;

        SettleRunnable(View view) {
            mView = view;
        }

        @Override
        public void run() {
            if (mViewDragHelper != null && mViewDragHelper.continueSettling(true)) {
                ViewCompat.postOnAnimation(mView, this);    // 递归调用
            }
        }
    }

    private void scrollTo(View child, int finalTop){
        child.setTop(finalTop);
        ViewCompat.postInvalidateOnAnimation(child);
    }
}

