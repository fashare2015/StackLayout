# 效果图
![StackLayout](screen-record/stack_layout.gif)

# 功能
- 自定义卡片的堆叠效果
- 自定义卡片移除动画
- 支持加载更多

# 使用方式
## gradle dependency
```gradle
// 1. Add it in your root build.gradle at the end of repositories:
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

// 2. Add the dependency in your app/build.gradle
dependencies {
    compile 'com.github.fashare2015:StackLayout:1.0.0'
}
```

## xml布局
- 父布局使用`clipChildren="false"`, 使之能全屏拖动
```xml
<RelativeLayout
    ...
    android:clipChildren="false">

    <com.fashare.stack_layout.StackLayout
        android:id="@+id/stack_layout"
        android:layout_width="match_parent"
        android:layout_height="500dp"/>

</RelativeLayout>
```

## Adapter
用法基本同RecyclerView，不赘述。
```java
mStackLayout = (StackLayout) findViewById(R.id.stack_layout);
mStackLayout.setAdapter(mAdapter = new MyAdapter(mData = new ArrayList<>()));

// 刷新数据
mData.addAll(...);
mAdapter.notifyDataSetChanged();
```

## PageTransformer：堆叠效果、滑动动画
内置了三个效果，即gif效果图上的效果。
```java
mStackLayout.addPageTransformer(
        new StackPageTransformer(),     // 堆叠
        new AlphaTransformer(),         // 渐变
        new AngleTransformer()          // 角度
);
```

自定义：根据position区分状态，做相应的动画。详见demo和接口注释。

| position                  | 状态                      | 
| --------------------------|:--------------------------|
| [-1, -1]                  | 完全移出屏幕, 待remove状态| 
| (-1, 0)                   | 手指拖动状态              | 
| [0, 栈内页面数)           | 栈中状态                  |
| [栈内页面数, 总页面数)    | 显示不下, 待显示状态      |

## OnSwipeListener: 滑动结果回调
接口作用：（各参数定义见接口注释）
- 区分向左移除、还是向右移除
- 移除后，可做`loadmore`动作
```java
mStackLayout.setOnSwipeListener(new StackLayout.OnSwipeListener() {
    @Override
    public void onSwiped(View swipedView, int swipedItemPos, boolean isSwipeLeft, int itemLeft) {
        toast((isSwipeLeft? "往左": "往右") + "移除" + mData.get(swipedItemPos) + "." + "剩余" + itemLeft + "项");

        // 少于5条, 加载更多
        if(itemLeft < 5){
            // TODO: loadmore
        }
    }
});
```
# 实现细节
[控件实现细节介绍](控件实现细节介绍.md)

# 参考
https://github.com/flschweiger/SwipeStack

https://github.com/xiepeijie/SwipeCardView

https://github.com/mcxtzhang/ZLayoutManager

https://github.com/yuqirong/CardSwipeLayout

https://github.com/xmuSistone/android-card-slide-panel
