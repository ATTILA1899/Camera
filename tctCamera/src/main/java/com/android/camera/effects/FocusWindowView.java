package com.android.camera.effects;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;

import com.tct.camera.R;

public final class FocusWindowView extends View implements AnimationListener {
    /** Retention time of focus window after focus done.*/
    public static final int FOCUS_TIMEOUT = 2000;

    /** Animation time of focus window scaling up when start focus.*/
    private static final int SCALING_UP_TIME = 1000;

    /** Animation time of focus window scaling done when focus done.*/
    private static final int SCALING_DOWN_TIME = 300;

    // Focus window state definition
    /** Focus window state of disappear state.*/
    private static final int ST_DISAPPEAR = 0;

    /** Focus window state of focusing state.*/
    private static final int ST_FOCUSING = 1;

    /** Focus window state of time outting.*/
    private static final int ST_TIMEOUTTING = 2;

    /** Focus window state of window locked.*/
    private static final int ST_LOCKED = 3;

    // Focus window event message.
    /** Message of disappear focus window.*/
    private static final int MSG_DISAPPEAR = 0;

    /** Message of reset focus window.*/
    private static final int MSG_RESETWND = 1;

    private boolean mTimeout = false;

    private int mState = ST_DISAPPEAR;

    private ScaleAnimation mStartAnimation, mEndAnimation;

    private final Handler mEventHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DISAPPEAR:
                    if (mState == ST_LOCKED) return;
                    mState = ST_DISAPPEAR;
                    setBackground(null);
                    setVisibility(View.INVISIBLE);
                    break;
                case MSG_RESETWND:
                    Animation ani;
                    mState = ST_DISAPPEAR;
                    setBackground(null);
                    setVisibility(View.INVISIBLE);
                    removeMessages(MSG_DISAPPEAR);
                    if ((ani = getAnimation()) != null) ani.cancel();
                    // Reset scale
                    setScaleX(1f);
                    setScaleY(1f);
                    break;
            }
        }
    };

    public FocusWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int pivotX = getWidth() / 2;
        int pivotY = getHeight() / 2;
        if (mStartAnimation == null) {
            mStartAnimation = new ScaleAnimation(1, 1.15f, 1, 1.15f, pivotX, pivotY);
            mStartAnimation.setDuration(SCALING_UP_TIME);
            mStartAnimation.setFillAfter(true);
            mEndAnimation = new ScaleAnimation(1.15f, 1, 1.15f, 1, pivotX, pivotY);
            mEndAnimation.setDuration(SCALING_DOWN_TIME);
            mEndAnimation.setFillAfter(true);
            mEndAnimation.setAnimationListener(this);
        }
    }

    /** Show start focus window at the specific coordinate.
     * @param x : X coordinate of the focus window.
     * @param y : Y coordinate of the focus window.*/
    public void showStart(int x, int y) {
        Animation ani;
        setBackground(null);
        setVisibility(View.INVISIBLE);
        mEventHandler.removeMessages(MSG_DISAPPEAR);

        if ((ani = getAnimation()) != null) {
            ani.cancel();
        }

        // Reset scale
        setScaleX(1f);
        setScaleY(1f);
        mState = ST_FOCUSING;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)getLayoutParams();
        params.setMargins(x - getWidth() / 2, y - getHeight() / 2, 0, 0);
        setVisibility(View.VISIBLE);
        setBackgroundResource(R.drawable.ic_focus_focusing);
        requestLayout();
        setAnimation(mStartAnimation);
        mStartAnimation.start();
    }

    /** Show focus done window.
     * @param success : Whether it is successful.
     * @param timeout : true as delayed disappear.*/
    public void showEnd(boolean success, boolean timeout) {
        if (mState != ST_LOCKED) mState = ST_TIMEOUTTING;
        mTimeout = timeout;
        int resid = success ? R.drawable.ic_focus_focused : R.drawable.ic_focus_failed;
        setBackgroundResource(resid);
        setAnimation(mEndAnimation);
        mEndAnimation.start();
    }

    public void setWindowLocked(boolean locked) {
        if (locked) {
            if (mState == ST_DISAPPEAR) {
                setBackgroundResource(R.drawable.ic_focus_focused);
            }
            mState = ST_LOCKED;
        } else {
            mState = ST_TIMEOUTTING;
        }
    }

    /** Force stop the animation and clear the focus window*/
    public void reset() {
        mEventHandler.sendEmptyMessage(MSG_RESETWND);
    }

    @Override
    public void onAnimationEnd(Animation useless) {
        if (mState == ST_LOCKED) return;
        if (mTimeout) {
            mEventHandler.sendEmptyMessageDelayed(MSG_DISAPPEAR, FOCUS_TIMEOUT);
        } else {
            mState = ST_DISAPPEAR;
            setBackground(null);
            setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onAnimationRepeat(Animation arg0) {}

    @Override
    public void onAnimationStart(Animation arg0) {}
}
