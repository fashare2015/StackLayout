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

import com.fashare.stack_layout.transformer.StackPageTransformer;
import com.fashare.stack_layout.widget.ScrollManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                ViewHolder viewHolder = adapter.getViewHolder(StackLayout.this, (i + getCurrentItem()) % adapter.getItemCount());
                StackLayout.this.addView(viewHolder.itemView, 0);
            }

            transformPage(0, true);
        }
    }

    // ------ Current Item ------
    private int mCurrentItem;

    private void setCurrentItem(int item){
        mCurrentItem = item;
    }

    private int getCurrentItem(){
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
//            Log.d(TAG, "tryCaptureView: " + child.getTag(R.id.sl_item_pos));
            return (int)child.getTag(R.id.sl_item_pos) == getCurrentItem();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mParent.getHeight();
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mParent.getWidth();
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return top;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
//            Log.d(TAG, "onViewPositionChanged: " + changedView.getTag(R.id.sl_item_pos));
            if(!tryCaptureView(changedView, 0)) // 有时候 changedView 和 getCurrentItem 会不一致, 还得再判断一下
                return ;
            int totalRange = mParent.getWidth();
            float position = (1.0f * (changedView.getLeft() - 0))/totalRange;
            transformPage(position, changedView.getLeft() < 0);
        }

        // 手指释放的时候回调
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
//            Log.d(TAG, "onViewReleased: " + releasedChild.getTag(R.id.sl_item_pos));
            if(!tryCaptureView(releasedChild, 0))
                return ;
            final int totalRange = mParent.getWidth();
            int left = releasedChild.getLeft();
            if(Math.abs(left - 0) < totalRange/2) {
                getScrollManager().smoothScrollTo(releasedChild, 0, 0, new ScrollManager.Callback() {
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
                        removeView(view);
                        addView(view, 0);
                        setCurrentItem((getCurrentItem() + 1) % mAdapter.getItemCount());
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
    private List<PageTransformer> mPageTransformerList = new ArrayList<>();

    public void addPageTransformer(PageTransformer... pageTransformerList) {
        mPageTransformerList.addAll(Arrays.asList(pageTransformerList));
    }

    public static abstract class PageTransformer {
        public abstract void transformPage(View otherPage, float position, boolean isSwipeLeft);
    }

    private void transformPage(float topPagePos, boolean isSwipeLeft) {
        if(mAdapter == null)
            return ;
        List<PageTransformer> list = new ArrayList<>(mPageTransformerList); // 保护性复制, 防止污染原来的list
        if(list.isEmpty())
            list.add(new StackPageTransformer());   // default PageTransformer

        int itemCount = mAdapter.getItemCount();
        for(int i=0; i<itemCount; i++) {
            View page = getChildAt(i);
            if(page == null)
                return ;
            for (PageTransformer pageTransformer : list) {
                pageTransformer.transformPage(page, -Math.abs(topPagePos) + ((int) page.getTag(R.id.sl_item_pos) - getCurrentItem() + itemCount) % itemCount, isSwipeLeft);
            }
        }
    }

    private boolean isFirstLayout = true;
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(isFirstLayout) {
            transformPage(0, true);
            isFirstLayout = false;
        }
    }
}

