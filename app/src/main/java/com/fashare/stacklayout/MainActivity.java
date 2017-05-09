package com.fashare.stacklayout;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fashare.stack_layout.StackLayout;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    StackLayout mStackLayout;
    Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mStackLayout = (StackLayout) findViewById(R.id.stack_layout);
        mStackLayout.setAdapter(mAdapter = new Adapter(Arrays.asList("1", "2", "3")));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.setData(Arrays.asList("5", "6", "7", "8"));
                mAdapter.notifyDataSetChanged();
            }
        }, 4000);
    }

    class Adapter extends StackLayout.Adapter<Adapter.ViewHolder>{
        List<String> mData;

        public void setData(List<String> data) {
            mData = data;
        }

        public Adapter(List<String> data) {
            mData = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mTextView.setText(mData.get(position));
            holder.itemView.setBackgroundColor(new Random().nextInt());
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ViewHolder extends StackLayout.ViewHolder{
            TextView mTextView;
            public ViewHolder(View itemView) {
                super(itemView);
                mTextView = (TextView) itemView.findViewById(android.R.id.text1);
            }
        }

    }
}
