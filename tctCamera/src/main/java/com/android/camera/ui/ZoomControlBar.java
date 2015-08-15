/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import com.android.camera.CameraActivity;
import com.tct.camera.R;
import com.android.camera.util.CameraUtil;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.android.camera.manager.ZoomManager;

import android.view.View;

/**
 * A view that contains camera zoom control and its layout.
 */
public class ZoomControlBar extends ZoomControl {
    private static final String TAG = "ZoomControlBar";
    private static final int THRESHOLD_FIRST_MOVE = CameraUtil.dpToPixel(10); // pixels
    // Space between indicator icon and the zoom-in/out icon.
    private static final int ICON_SPACING = CameraUtil.dpToPixel(12);
    private static final int THRESHOLD_MOVE = CameraUtil.dpToPixel(6); // pixels
    private ZoomSlider mSliderBar;
    private boolean mStartChanging;
    private int mSliderPosition = 0;
    private int mSliderLength;
    private int mWidth;
    private int mIconWidth;
    private int mTotalIconWidth;
    private int mLastSetSliderPosition = 0;
    private CameraActivity mCamera;
    private ZoomManager mZoomManager;

    public ZoomControlBar(Context context, AttributeSet attrs) {
        super(context, attrs, true);
        mSliderBar = new ZoomSlider(context, attrs);
        addView(mSliderBar);
        mSliderBar.setVisibility(View.VISIBLE);
        mCamera = (CameraActivity) context;
        mZoomManager = mCamera.getCameraViewManager().getZoomManager();
    }

    public void setActivated(boolean activated) {
        super.setActivated(activated);
        mSliderBar.setActivated(activated);
    }

    private int getSliderPosition(int x) {
        // Calculate the absolute offset of the slider in the zoom control bar.
        // For left-hand users, as the device is rotated for 180 degree for
        // landscape mode, the zoom-in bottom should be on the top, so the
        // position should be reversed.
        int pos; // the relative position in the zoom slider bar
        if (mOrientation == 90) {
            pos = mWidth - mTotalIconWidth - x;
        } else {
            pos = x - mTotalIconWidth;
        }
        if (pos < 0)
            pos = 0;
        if (pos > mSliderLength)
            pos = mSliderLength;
        return pos;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mIconWidth = mZoomIn.getMeasuredWidth();
        mTotalIconWidth = mIconWidth + ICON_SPACING;
        mSliderLength = mWidth - (2 * mTotalIconWidth);
    }

    public boolean dispatchTouchEvent(MotionEvent event) {

        if (!isEnabled() || (mWidth == 0))
            return false;
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setActivated(false);
                closeZoomControl();
                break;

            case MotionEvent.ACTION_DOWN:
                setActivated(true);
                mStartChanging = false;
            case MotionEvent.ACTION_MOVE:
                int pos = getSliderPosition((int) event.getX());
                if (!mStartChanging) {
                    // Make sure the movement is large enough before we start
                    // changing the zoom.
                    int delta = mSliderPosition - pos;
                    if ((delta > THRESHOLD_FIRST_MOVE) || (delta < -THRESHOLD_FIRST_MOVE)) {
                        mStartChanging = true;
                    }
                }
                if (mStartChanging) {
                    mSliderPosition = pos;
                    /** modify by minghui.hua for PR938037
                    if (mLastSetSliderPosition - mSliderPosition > THRESHOLD_MOVE
                            || mLastSetSliderPosition - mSliderPosition < -THRESHOLD_MOVE) {
//                        performZoom(1.0d * mSliderPosition / mSliderLength);
                        mLastSetSliderPosition = mSliderPosition;
                        Log.i(TAG,"LMCH923 mSliderLength :"+mSliderLength);
                        mSliderBar.setSliderPosition(mSliderPosition);
                    }*/
                }
                int maxIndex = mZoomManager.getMaxZoomIndexEx();
                Log.i(TAG,"LMCH1011 maxIndex:"+maxIndex);
                mLastSetSliderPosition = mSliderPosition;
                mSliderBar.setSliderPosition(mSliderPosition);
                //[BUGFIX]-Mod for 6x zoom, pengdong.su, 461408.10.0=====>59.0
                mZoomManager.performZoom((int) maxIndex * mSliderPosition / mSliderLength, true);
        }
        return true;
    }


    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // if (mZoomMax == 0) return;
        int height = bottom - top;
        // For left-hand users, as the device is rotated for 180 degree,
        // the zoom-in button should be on the top.
        if (mOrientation == 90) {
            mZoomIn.layout(0, 0, mIconWidth, height);
            mZoomOut.layout(mWidth - mIconWidth, 0, mWidth, height);
        } else {
            mZoomOut.layout(0, 0, mIconWidth, height);
            mZoomIn.layout(mWidth - mIconWidth, 0, mWidth, height);
        }
        int sliderWidth = mSliderBar.getSliderWidth();
        mSliderBar.layout(mTotalIconWidth - sliderWidth / 2, 0, mWidth - mTotalIconWidth
                + sliderWidth / 2, height);
    }
    public void setZoomIndex(float index) {
        //super.setZoomIndex(index);
        // mZoomIndex=index;
        float mMaxIndex = 6.0f;
        if(mZoomManager != null) {
            mMaxIndex = mZoomManager.getMaxZoomIndexFactorEx();
        }
        if(mSliderLength == 0) mSliderLength = 348;
        mSliderBar.setSliderPosition((int)((index-1)*mSliderLength/(mMaxIndex-1)));
        mStartChanging=false;
        requestLayout();
    }

    @Override
    public void onClick(View view) {
        Log.i(TAG,"LMCH923 onClick view:"+view);
        if (mZoomIn == view) {
            mSliderBar.setSliderPosition(0);
        } else if (mZoomOut == view) {
            mSliderBar.setSliderPosition(mSliderLength);
        }
    }
}
