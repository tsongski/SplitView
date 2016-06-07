package com.tsongski.splitview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

/**
 * Created by yaocong on 2015/7/30.
 */
public class SplitView extends ViewGroup implements View.OnTouchListener {

    private float mSplitRatio;
    private int mMinSplitTop;
    private int mMinSplitBottom;

    private View mTop, mMid, mBottom;
    private int mSplitHeight;
    private int mTouchSlop;
    private int mLastMotionY = 0;

    public SplitView(Context context) {
        super(context);
        init(context, null);
    }

    public SplitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        l = t = 0;
        mTop.layout(l, t, l + mTop.getMeasuredWidth(), t + mTop.getMeasuredHeight());
        mMid.layout(l, t + mSplitHeight - mMid.getMeasuredHeight(), l + mMid.getMeasuredWidth(), t + mSplitHeight);
        mBottom.layout(l, t + mSplitHeight, l + mBottom.getMeasuredWidth(), t + mSplitHeight + mBottom.getMeasuredHeight());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setupViews();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = (int) event.getY();
                return true;
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
                    requestLayout();
                }
                return true;
        }
        return false;
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SplitView);
        mSplitRatio = a.getFloat(R.styleable.SplitView_splitRatio, 0.382f);
        mMinSplitTop = a.getDimensionPixelSize(R.styleable.SplitView_minSplitTop, 0);
        mMinSplitBottom = a.getDimensionPixelSize(R.styleable.SplitView_minSplitBottom, 0);
        a.recycle();

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

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
}

