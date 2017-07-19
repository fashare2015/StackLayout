package com.fashare.stacklayout;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fashare.stack_layout.StackLayout;
import com.fashare.stack_layout.transformer.AngleTransformer;
import com.fashare.stack_layout.transformer.StackPageTransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    StackLayout mStackLayout;
    Adapter mAdapter;

    List<String> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        loadData(0);
    }

    private void initView() {
        mStackLayout = (StackLayout) findViewById(R.id.stack_layout);
        mStackLayout.setAdapter(mAdapter = new Adapter(mData = new ArrayList<>()));
        mStackLayout.addPageTransformer(
                new StackPageTransformer(),
                new MyAlphaTransformer(),
                new AngleTransformer()
        );

        mStackLayout.setOnSwipeListener(new StackLayout.OnSwipeListener() {
            @Override
            public void onSwiped(View swipedView, int swipedItemPos, boolean isSwipeLeft, int itemLeft) {
                toast((isSwipeLeft? "往左": "往右") + "移除" + mData.get(swipedItemPos) + "." + "剩余" + itemLeft + "项");

                if(itemLeft < 5){
                    loadData(++ curPage);
                }
            }
        });
    }

    int curPage = 0;

    private void loadData(final int page) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.getData().addAll(Arrays.asList(String.valueOf(page*3), String.valueOf(page*3+1), String.valueOf(page*3+2)));
                mAdapter.notifyDataSetChanged();
            }
        }, 1000);
    }

    class Adapter extends StackLayout.Adapter<Adapter.ViewHolder>{
        List<String> mData;

        public void setData(List<String> data) {
            mData = data;
        }

        public List<String> getData() {
            return mData;
        }

        public Adapter(List<String> data) {
            mData = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.mTextView.setText(mData.get(position));
            ((CardView)holder.itemView).setCardBackgroundColor(new Random().nextInt() | 0xff000000);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ViewHolder extends StackLayout.ViewHolder{
            TextView mTextView;
            public ViewHolder(View itemView) {
                super(itemView);
                mTextView = (TextView) itemView.findViewById(R.id.tv);
            }
        }

    }

    void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
