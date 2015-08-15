/******************************************************************************/
/*                                                                Date:05/2013*/
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/* Author :                                                                   */
/* Email  :                                                                   */
/* Role   :                                                                   */
/* Reference documents :                                                      */
/* -------------------------------------------------------------------------- */
/* Comments :                                                                 */
/* File     :                                                                 */
/* Labels   :                                                                 */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* ----------|----------------------|----------------------|----------------- */
/*    date   |        Author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.camera.ui;
//[BUGFIX]-Add by TCTNB.(zsy,wh),03/02/2014,608031
//import com.android.camera.TctMutex;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * A widget which switchs between the {@code Camera} and the {@code VideoCamera}
 * activities.
 */
public class Switcher extends ImageView implements View.OnTouchListener, OnGestureListener {

    private static final String TAG = "Switcher";

    /** A callback to be called when the user wants to switch activity. */
    public interface OnSwitchListener {
        // Returns true if the listener agrees that the switch can be changed.
        public boolean onSwitchChanged(Switcher source, boolean onOff);
    }

    private static final int ANIMATION_SPEED = 400;
    private static final long NO_ANIMATION = -1;

    private boolean mSwitch = false;
    private boolean mCurrentSwitherState = false;
    private int mPosition = 0;
    private long mAnimationStartTime = NO_ANIMATION;
    private int mAnimationStartPosition;
    private OnSwitchListener mListener;
    private GestureDetector mGestureDetector;
    private static final int FLING_MIN_DISTANCE = 10;
    private static final int FLING_MIN_VELOCITY = 0;
    private static final int UNKOWN = -1;
    private static final int TAP_EVENT = 0;
    private static final int FLING_RIGHT = 1;
    private static final int FLING_LEFT = 2;
    private static final int MOVE_EVENT = 3;
    private int mCurrentEventState = UNKOWN;
    private int mCurrentScrollStatus = UNKOWN;

    public Switcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnTouchListener(this);
        this.setLongClickable(true);
        mGestureDetector = new GestureDetector(this);
        mGestureDetector.setIsLongpressEnabled(true);

    }

    public void setSwitch(boolean onOff) {
        if (mSwitch == onOff)
            return;
        mSwitch = onOff;
        invalidate();
    }

    // Try to change the switch position. (The client can veto it.)
    private void tryToSetSwitch(boolean onOff) {
        if (mCurrentSwitherState == onOff) {
            Log.i(TAG, "zsy1225 onTouch mCurrentSwitherState == onOff");
            canSwitch = true;
            return;
        }

        if (mListener != null) {
            if (!mListener.onSwitchChanged(this, onOff)) {
                return;
            }
        }

        mCurrentSwitherState = onOff;
    }

    public void setOnSwitchListener(OnSwitchListener listener) {
        mListener = listener;
    }

    private void startParkingAnimation() {
        mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
        mAnimationStartPosition = mPosition;
    }

    private void trackTouchEvent(MotionEvent event) {
        Drawable drawable = getDrawable();
        int drawableWidth = drawable.getIntrinsicWidth();
        final int width = getWidth();
        final int available = width - drawableWidth;
        int x = (int) event.getX();
        mPosition = x;
        if (mPosition < 0)
            mPosition = 0;
        if (mPosition > available)
            mPosition = available;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        int drawableHeight = drawable.getIntrinsicHeight();
        int drawableWidth = drawable.getIntrinsicWidth();

        if (drawableWidth == 0 || drawableHeight == 0) {
            return; // nothing to draw (empty bounds)
        }

        final int available = getWidth() - drawableWidth;
        if (mAnimationStartTime != NO_ANIMATION) {
            long time = AnimationUtils.currentAnimationTimeMillis();
            int deltaTime = (int) (time - mAnimationStartTime);
            mPosition = mAnimationStartPosition +
                    ANIMATION_SPEED * (mSwitch ? deltaTime : -deltaTime) / 1000;
            if (mPosition < 0)
                mPosition = 0;
            if (mPosition > available)
                mPosition = available;
            boolean done = (mPosition == (mSwitch ? available : 0));
            if (!done) {
                invalidate();
            } else {
                mAnimationStartTime = NO_ANIMATION;
            }
        } else if (!isPressed()) {
            mPosition = mSwitch ? available : 0;
        }
        int offsetLeft = mPosition;
        int offsetTop = (getHeight()
                - drawableHeight) / 2;
        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.translate(offsetLeft, offsetTop);
        drawable.draw(canvas);
        canvas.restoreToCount(saveCount);
        if (mPosition == 0 || mPosition == available) {
            tryToSetSwitch(mSwitch);
        }
    }

    // Consume the touch events for the specified view.
    public void addTouchView(View v) {
        v.setOnTouchListener(this);
    }

    // TCTNB(Shangyong.Zhang), 2014/01/08, PR551034.
    private boolean canSwitch = true;
    public void setCanSwitch(boolean can) {
        canSwitch = can;
    }

    // This implements View.OnTouchListener so we intercept the touch events
    // and pass them to ourselves.
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG, "zsy1225 onTouch Action = " + event.getAction());
        if (!isEnabled()) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                startParkingAnimation(); // slide the view to the correct place.
            }
            return false;
        }

        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (canSwitch) {
                    Log.i(TAG, "zsy1225 onTouch ACTION_MOVE");
                    trackTouchEvent(event);
                    mCurrentEventState = MOVE_EVENT;
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "zsy1225 onTouch ACTION_UP");
                canSwitch = false;
                doActionUp();
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i(TAG, "zsy1225 onTouch ACTION_CANCEL");
                canSwitch = false;
                tryToSetSwitch(mSwitch);
                break;
        }
        return true;
    }

    private void doActionUp() {
        setPressed(false);
        if (mCurrentEventState == TAP_EVENT) {
            mSwitch = !mSwitch;
        } else if (mCurrentEventState == FLING_RIGHT) {
            mSwitch = true;
        } else if (mCurrentEventState == FLING_LEFT) {
            mSwitch = false;
        } else if (mCurrentEventState == MOVE_EVENT) {
            if (mCurrentScrollStatus == FLING_RIGHT) {
                mSwitch = true;
            } else if (mCurrentScrollStatus == FLING_LEFT) {
                mSwitch = false;
            } else if (mCurrentScrollStatus == TAP_EVENT) {
                mSwitch = !mSwitch;
            }
        }
        startParkingAnimation();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        setPressed(true);
        mAnimationStartTime = NO_ANIMATION;
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub
        mCurrentEventState = TAP_EVENT;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        mCurrentEventState = TAP_EVENT;
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // TODO Auto-generated method stub
        if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE) {
            // scroll left
            mCurrentScrollStatus = FLING_LEFT;
        } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE) {
            // scroll right
            mCurrentScrollStatus = FLING_RIGHT;
        } else {
            mCurrentScrollStatus = TAP_EVENT;
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub
        mCurrentEventState = TAP_EVENT;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // TODO Auto-generated method stub
        if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE
                                      && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
            // Fling left
            mCurrentEventState = FLING_LEFT;
        } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE
                                      && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
            // Fling right
            mCurrentEventState = FLING_RIGHT;
        } else {
            mCurrentEventState = TAP_EVENT;
        }
        return true;
    }
}
