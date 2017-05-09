package com.fashare.stack_layout;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by jinliangshan on 17/5/9.
 */
public class StackLayout extends LinearLayout {

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

    public StackLayout(Context context) {
        super(context);
    }

    public StackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
}

