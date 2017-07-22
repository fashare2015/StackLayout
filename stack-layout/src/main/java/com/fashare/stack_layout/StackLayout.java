package com.fashare.stack_layout;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
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
 * Created by fashare on 17/5/9.
 *
 * 层叠卡片控件
 */
public class StackLayout extends FrameLayout {
    public static final String TAG = "StackLayout";

    public StackLayout(Context context) {
        super(context);
    }

    public StackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // ------ Adapter ------
    @NonNull private Adapter mAdapter = Adapter.EMPTY;
    ItemObserver mItemObserver = new ItemObserver();

    public Adapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(@NonNull Adapter adapter) {
        mAdapter = adapter;
        onSetAdapter(adapter);
    }

    private void onSetAdapter(Adapter adapter) {
        adapter.registerDataSetObserver(mItemObserver);
        setCurrentItem(0);
        mItemObserver.dataChanged(adapter);
    }

    /**
     * 类似 RecyclerView.Adapter
     * @param <VH> {@link ViewHolder}
     */
    public static abstract class Adapter<VH extends ViewHolder>{
        public static Adapter<?> EMPTY = new Adapter<ViewHolder>() {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int position) { return null; }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {}

            @Override
            public int getItemCount() { return 0; }
        };

        // ------------ ViewHolder -------------
        public abstract VH onCreateViewHolder(ViewGroup parent, int position);

        public abstract void onBindViewHolder(VH holder, int position);

        public abstract int getItemCount();

        private VH getViewHolder(ViewGroup parent, int position){
            VH viewHolder = onCreateViewHolder(parent, position);
            if(viewHolder != null) {
                onBindViewHolder(viewHolder, position);
                ViewHolder.setPosition(viewHolder.itemView, position);
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

        private static void setPosition(View view, int pos){
            view.setTag(R.id.sl_item_pos, pos);
        }

        public static int getPosition(View view){
            return (int)view.getTag(R.id.sl_item_pos);
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

        private boolean isDataChangedWhileScrolling = false;

        private void dataChanged(Adapter adapter) {
            // 滑动过程中记录数据数据已脏, 待滑动结束刷新数据
            if(mViewDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE){
                isDataChangedWhileScrolling = true;
                return ;
            }
            isDataChangedWhileScrolling = false;

            StackLayout.this.removeAllViews();
            for(int i=getCurrentItem(); i<adapter.getItemCount(); i ++) {
                ViewHolder viewHolder = adapter.getViewHolder(StackLayout.this, i);
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
//            Log.d(TAG, "tryCaptureView: " + ViewHolder.getPosition(child) + ", cur: " + getCurrentItem());
            return mViewDragHelper.getViewDragState() == ViewDragHelper.STATE_IDLE  // 空闲状态
                    && ViewHolder.getPosition(child) == getCurrentItem();           // 且是最上面的卡片, 才可捕获
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
//            Log.d(TAG, "onViewPositionChanged: " + ViewHolder.getPosition(changedView));
            int totalRange = mParent.getWidth();
            float position = (1.0f * (left - 0))/totalRange;
            transformPage(position, left < 0);
        }

        // 手指释放的时候回调
        @Override
        public void onViewReleased(final View releasedChild, float xvel, float yvel) {
//            Log.d(TAG, "onViewReleased: " + ViewHolder.getPosition(releasedChild));
            final int totalRange = mParent.getWidth();
            final int left = releasedChild.getLeft();
            if(Math.abs(left - 0) < totalRange/2) {
                getScrollManager().smoothScrollTo(releasedChild, 0, 0, new ScrollManager.Callback() {
                    @Override
                    public void onComplete(View view) {
//                        Log.d(TAG, "onViewReleased: cancel" + ViewHolder.getPosition(releasedChild));
                        if(mItemObserver.isDataChangedWhileScrolling)
                            mItemObserver.dataChanged(mAdapter);
                    }
                });

            }else {
                getScrollManager().smoothScrollTo(releasedChild, totalRange * (left < 0 ? -1 : 1), releasedChild.getTop(), new ScrollManager.Callback() {
                    @Override
                    public void onComplete(View view) {
//                        Log.d(TAG, "onViewReleased: remove" + ViewHolder.getPosition(releasedChild));
                        removeView(view);
                        setCurrentItem(getCurrentItem() + 1);
                        mOnSwipeListener.onSwiped(view, ViewHolder.getPosition(view), left < 0, mAdapter.getItemCount() - getCurrentItem());

                        if(mItemObserver.isDataChangedWhileScrolling)
                            mItemObserver.dataChanged(mAdapter);
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

    /**
     * 卡片滑动动画接口, 类似 {@link ViewPager.PageTransformer}
     */
    public static abstract class PageTransformer {
        /**
         * 根据 position 做相应的动画, 其中 position:
         *  [-1, -1]                -> 完全移出屏幕, 待remove状态
         *  (-1, 0)                 -> 手指拖动状态
         *  [0, 栈内页面数)           -> 栈中状态
         *  [栈内页面数, 总页面数)     -> 显示不下, 待显示状态
         *
         * @param page          各卡片的根布局, 即 {@link ViewHolder#itemView }
         * @param position      各卡片的位置
         * @param isSwipeLeft   向左滑动
         */
        public abstract void transformPage(View page, float position, boolean isSwipeLeft);
    }

    private void transformPage(float topPagePos, boolean isSwipeLeft) {
        List<PageTransformer> list = new ArrayList<>(mPageTransformerList); // 保护性复制, 防止污染原来的list
        if(list.isEmpty())
            list.add(new StackPageTransformer());   // default PageTransformer

        int itemCount = mAdapter.getItemCount();
        for(int i=0; i<itemCount; i++) {
            View page = getChildAt(i);
            if(page == null)
                return ;
            for (PageTransformer pageTransformer : list) {
                pageTransformer.transformPage(page, -Math.abs(topPagePos) + ViewHolder.getPosition(page) - getCurrentItem(), isSwipeLeft);
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

    // ------ OnSwipeListener ------
    @NonNull private OnSwipeListener mOnSwipeListener = OnSwipeListener.EMPTY;

    public void setOnSwipeListener(@NonNull OnSwipeListener onSwipeListener) {
        mOnSwipeListener = onSwipeListener;
    }

    /**
     * 滑动事件监听
     */
    public static abstract class OnSwipeListener{
        public static final OnSwipeListener EMPTY = new OnSwipeListener() {
            @Override
            public void onSwiped(View swipedView, int swipedItemPos, boolean isSwipeLeft, int itemLeft) {}
        };

        /**
         * 已被移除屏幕时回调. 另外, 可以根据 itemLeft, 决定何时加载更多.
         *
         * @param swipedView        被移除屏幕的view
         * @param swipedItemPos     swipedView 对应的 AdapterPos
         * @param isSwipeLeft       往左滑动
         * @param itemLeft          当前剩余的item个数 (栈中的 + 待显示的)
         */
        public abstract void onSwiped(View swipedView, int swipedItemPos, boolean isSwipeLeft, int itemLeft);
    }
}

