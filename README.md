# SplitView

## 概述
>仿猿题库练题里面效果，两个 View 分层，滑动中间的 View 可调节上下 View 的高度

### 目标效果

![alt tag](https://github.com/tsongski/SplitView/blob/master/ytk.gif)

### 实现效果

![alt tag](https://github.com/tsongski/SplitView/blob/master/splitView.gif)


## 实现原理
> 继承 ViewGroup ，内部分为三个子 View ,分别是 top, mid (用来拖动的 View ), bottom


    protected void setupViews() {
      mTop = getChildAt(0);
      mMid = getChildAt(1);
      mBottom = getChildAt(2);
      View handlerView = mMid.findViewById(R.id.handler);
      if (handlerView == null) {
          handlerView = mMid;
      }
      handlerView.setOnTouchListener(this);
    }

> 维护一个 `mSplitHeight` ，用来设置分割线(即上下 View 的交界处)的位置
>在 `onMeasure(int widthMeasureSpec, int heightMeasureSpec)` 中初始化 mSplitHeight 并测量子view的大小

    {

      if (getChildCount() != 3) {
            throw new RuntimeException("give 3 views");
        }

        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (mSplitHeight == 0) {
            mSplitHeight = (int)(height * mSplitRatio);
        }

      //measure child
      measureChild(mTop, widthMeasureSpec, MeasureSpec.makeMeasureSpec(mSplitHeight, MeasureSpec.EXACTLY));
      measureChild(mMid, widthMeasureSpec, heightMeasureSpec);
      measureChild(mBottom, widthMeasureSpec, MeasureSpec.makeMeasureSpec(getMeasuredHeight() - mSplitHeight, MeasureSpec.EXACTLY));
    }

> 在onLayout方法中指定各个子 View 的位置

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
            l = t = 0;
            mTop.layout(l, t, l + mTop.getMeasuredWidth(), t + mTop.getMeasuredHeight());
            mMid.layout(l, t + mSplitHeight - mMid.getMeasuredHeight(), l + mMid.getMeasuredWidth(), t + mSplitHeight);
            mBottom.layout(l, t + mSplitHeight, l + mBottom.getMeasuredWidth(), t + mSplitHeight + mBottom.getMeasuredHeight());
        }

> 最后要做的就是监听 mid View 滑动时改变 mSplitHeight 的值了。
>在 mid 的 `onTouch(View v, MotionEvent event)` 中判断，当 MotionEvent.ACTION_MOVE 时，
改变分割线的高度，然后再 requestLayout() 重新绘制即可。

    {
      case MotionEvent.ACTION_MOVE:
          int delta = (int) (event.getY() - mLastMotionY);
          if (Math.abs(delta) > mTouchSlop) {
              if (delta > 0) {
                  mSplitHeight += delta - mTouchSlop;
              } else {
                  mSplitHeight += delta + mTouchSlop;
              }

              if (mSplitHeight < mMinSplitTop || mSplitHeight < mMid.getHeight()) {
                  mSplitHeight = Math.max(mMinSplitTop, mMid.getHeight());
              } else if (mSplitHeight > getHeight() || mSplitHeight > getHeight() - mMinSplitBottom) {
                  mSplitHeight = Math.min(getHeight(), getHeight() - mMinSplitBottom);
              }
              // mSplitHeight 改变之后重绘
              requestLayout();
          }
          return true;
    }
