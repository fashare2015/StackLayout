package com.fashare.stack_layout;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.fashare.stack_layout.widget.ScrollManager;
import com.fashare.stack_layout.widget.StackPageTransformer;

/**
 * Created by jinliangshan on 17/5/9.
 */
public class StackLayout extends FrameLayout {
    public static final String TAG = "StackLayout";

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
                viewHolder.itemView.setTag(R.id.sl_item_pos, position);
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
            // remove 前, 取消所有动画
            mViewDragHelper.abort();
            StackLayout.this.removeAllViews();
            for(int i=0; i<adapter.getItemCount(); i++) {
                ViewHolder viewHolder = adapter.getViewHolder(StackLayout.this, i);
                StackLayout.this.addView(viewHolder.itemView, 0);
            }
        }
    }

    // ------ Current Item ------
    private int mCurrentItem;

    public void setCurrentItem(int item){
        mCurrentItem = item;
    }

    public int getCurrentItem(){
        return mCurrentItem;
    }

    // ------ 事件分发 ------
    private ViewDragHelper mViewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback(){
        private ScrollManager mScrollManager;
        private View mParent = StackLayout.this;

        public ScrollManager getScrollManager() {
            if(mScrollManager == null)
                mScrollManager = new ScrollManager(getViewDragHelper());
            return mScrollManager;
        }

        // 仅捕获 topChild, 即 最顶上的卡片 可拖动
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return (int)child.getTag(R.id.sl_item_pos) == mCurrentItem;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mParent.getHeight();
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mParent.getWidth();
        }

        // 控制边界, 防止 child 超出上下边界
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            int newTop = top;
//            newTop = Math.max(newTop, 0);
//            newTop = Math.min(newTop, ((ViewGroup)child.getParent()).getHeight() - child.getHeight());
            return 0;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            int totalRange = mParent.getWidth();
            float position = (1.0f * (changedView.getLeft() - 0))/totalRange;
            transformPage(position);
        }

        // 手指释放的时候回调
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            final int totalRange = mParent.getWidth();
            int left = releasedChild.getLeft();
            if(Math.abs(left - 0) < totalRange/2) {
                getScrollManager().smoothScrollTo(releasedChild, 0, releasedChild.getTop(), new ScrollManager.Callback() {
                    @Override
                    public void onProgress(View view, float scale) {
                        Log.d(TAG, scale + "");
                    }

                    @Override
                    public void onComplete(View view) {}
                });

            }else {
                getScrollManager().smoothScrollTo(releasedChild, totalRange * (left < 0 ? -1 : 1), releasedChild.getTop(), new ScrollManager.Callback() {
                    @Override
                    public void onProgress(View view, float scale) {
                        Log.d(TAG, scale + "");
                    }

                    @Override
                    public void onComplete(View view) {
                        int curPos = getCurrentItem();
                        setCurrentItem((curPos + 1) % mAdapter.getItemCount());
                        removeView(view);
                        addView(mAdapter.getViewHolder(StackLayout.this, curPos).itemView, 0);
                    }
                });
            }
        }
    });

    private ViewDragHelper getViewDragHelper(){
        return mViewDragHelper;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return getViewDragHelper().shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getViewDragHelper().processTouchEvent(event);
        return true;
    }

    // ------ PageTransformer ------
    private PageTransformer mPageTransformer = new StackPageTransformer();

    public void setPageTransformer(PageTransformer pageTransformer) {
        mPageTransformer = pageTransformer;
    }

    public static abstract class PageTransformer {
        public abstract void transformPage(View otherPage, float position);
    }

    private void transformPage(float position) {
        if(mAdapter == null)
            return ;
        int itemCount = mAdapter.getItemCount();
        for(int i=0; i<itemCount; i++) {
            View page = getChildAt(i);
            mPageTransformer.transformPage(page, -Math.abs(position) + ((int)page.getTag(R.id.sl_item_pos) - mCurrentItem + itemCount) % itemCount);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        transformPage(0);
    }
}

