/******************************************************************************/
/*                                                                Date:06/2013*/
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/* Author :                                                                  */
/* Email  :                                                                                                       */
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

package com.android.camera.manager;

import android.view.View;
import android.util.Log;
import android.view.View.OnClickListener;
import android.graphics.Bitmap;

import com.android.camera.CameraActivity;
import com.android.camera.CameraModule;
import com.android.camera.util.CameraUtil;
import com.android.camera.ExpressionThread; // PR851347-lan.shan-001 Add
import com.android.camera.PhotoModule;
import com.android.camera.PhotoController;
import com.android.camera.VideoModule;
import com.android.camera.VideoController;
import com.android.camera.GC;
import com.tct.camera.R;
import com.android.camera.ui.ShutterButton;
import com.android.camera.ui.ShutterButton.OnShutterButtonListener;

import android.widget.ImageView;
import android.widget.TextView;

import com.android.camera.ui.RotateImageView;

import android.view.MotionEvent;

public class ShutterManager extends ViewManager {
    private static final String TAG = "ShutterManager";
    private static final boolean LOG = true;

    public static final int SHUTTER_TYPE_PHOTO_VIDEO = 0;
    public static final int SHUTTER_TYPE_PHOTO = 1;
    public static final int SHUTTER_TYPE_VIDEO = 2;
    public static final int SHUTTER_TYPE_OK_CANCEL = 3;
    public static final int SHUTTER_TYPE_CANCEL = 4;
    public static final int SHUTTER_TYPE_CANCEL_VIDEO = 5;

    private PhotoController mPhotoController;
    private VideoController mVideoController;
    private int mShutterType = SHUTTER_TYPE_PHOTO_VIDEO;
    public ShutterButton mShutterButton;
    public RotateImageView mShutterPause;
    public RotateImageView mShutterCapture;
    public TextView mRecordTimeText;
    public TextView mOutputTimeText;
    private boolean mVideoShutterMasked;
    private boolean mFullScreen = true;
    private boolean mChangeBackground = false;

    public void setShutterPauseListener(View.OnClickListener listener) {
        mShutterPause.setOnClickListener(listener);
    }

    public void setShutterPauseImage(boolean paused) {
        Log.i(TAG, "setShutterPauseImage paused:"+paused);
        mShutterPause.setImageResource(paused ? R.drawable.recording_pause : R.drawable.recording_record);
        mShutterButton.setImageResource(paused ? R.drawable.recording_pause : R.drawable.recording_stop);
    }

    public void setShutterVideoStartUI(boolean start){
        Log.i(TAG, "setShutterVideoStartUI start:"+start);
        mShutterPause.setImageResource(start ? R.drawable.recording_pause : R.drawable.recording_record);
//      mShutterPause.setVisibility(start ? View.VISIBLE : View.GONE);
//      mShutterCapture.setVisibility(start ? View.VISIBLE : View.GONE);
        mShutterPause.setVisibility(start ? View.GONE : View.GONE);
        mShutterCapture.setVisibility(start ? View.GONE : View.GONE);
        mShutterButton.setImageResource(R.drawable.btn_video);
//        mShutterButton.setImageResource(start ? R.drawable.btn_recording_stop : R.drawable.btn_video);
    }

    public void setVideoCaptureIntentShutterButton(){
        mShutterButton.setImageResource(R.drawable.btn_video_capture_active);
    }

    public ShutterManager(CameraActivity context) {
        super(context);
        setFileter(false);
    }

    public ShutterManager(CameraActivity context, int layer) {
        super(context, layer);
        setFileter(false);
    }

    @Override
    protected View getView() {
        View view = null;
        int layoutId = R.layout.camera_shutter_photo_video;
        switch(mShutterType) {
        case SHUTTER_TYPE_PHOTO_VIDEO:
        case SHUTTER_TYPE_PHOTO:
        case SHUTTER_TYPE_VIDEO:
            layoutId = R.layout.camera_shutter_photo_video;
            break;
        case SHUTTER_TYPE_OK_CANCEL:
            /*if (mGC.getDisPlayRatio() == GC.PICTURE_RATIO_5_3)
                layoutId = R.layout.review_module_control_5_3;
            else
                layoutId = R.layout.review_module_control;
            */
            break;
        case SHUTTER_TYPE_CANCEL_VIDEO:
            //layoutId = R.layout.review_module_video_control;
            break;
        default:
            break;
        }
        view = inflate(layoutId);
        mShutterButton = (ShutterButton) view.findViewById(R.id.shutter_button_photo);
        mShutterPause = (RotateImageView) view.findViewById(R.id.shutter_button_pause);
        mShutterCapture = (RotateImageView) view.findViewById(R.id.shutter_button_capture);
        
        mRecordTimeText = (TextView) view.findViewById(R.id.txt_record_time);
        mOutputTimeText = (TextView) view.findViewById(R.id.txt_out_time);
        
        if (mShutterButton != null)
            mShutterButton.setScaleType(ImageView.ScaleType.FIT_XY);
        return view;
    }

    public void setOnShutterButtonListener(OnShutterButtonListener listener) {
        mShutterButton.setOnShutterButtonListener(listener);
    }

    public void setShutterCaptureListener(View.OnClickListener listener) {

        mShutterCapture.setOnClickListener(listener);
    }

    @Override
    protected void onRelease() {
        if (mShutterButton != null) {
            mShutterButton.setOnShutterButtonListener(null);
        }
        mShutterButton = null;
    }



    public void switchShutter(int type) {
        if (LOG) {
            Log.i(TAG, "switchShutterType(" + type + ") mShutterType=" + mShutterType);
        }
        if (mShutterType != type) {
            mShutterType = type;
            reInflate();
        }
    }

    public int getShutterType() {
        return mShutterType;
    }

    public boolean setChangeBackgroundValue() {

        if (mChangeBackground) {
            mChangeBackground = false;
        } else {
            mChangeBackground = true;
        }
        return mChangeBackground;
    }
    @Override
    public void show() {
        if (isSupportedShuttermanager()) super.show();
    }

    private boolean isSupportedShuttermanager() {
        if (mContext.getCameraViewManager().getCameraManualModeManager().isShowing()) {
            return false;
        }
        return true;
    }
    public boolean setChangeBackgroundValueFalse() {
        mChangeBackground = false;
        return mChangeBackground;
    }

    @Override
    public void onRefresh() {
        Log.i(TAG,"LMCH0414 onRefresh mShutterButton:"+mShutterButton);
        if (mShutterButton!= null && !ExpressionThread.isExpressionThreadActive()) { // PR851347-lan.shan-001 Add
            mShutterButton.setImageResource(mGC.getCurrentShutterIcon());
        }
    }

    public ShutterButton getShutterButton() {
        return mShutterButton;
    }
    public void pressShutter(boolean pressed) {
        mShutterButton.setPressed(pressed);
    }

    public boolean performShutter() {
        boolean performed = false;
        if (mShutterButton != null && mShutterButton.isEnabled()) {
            mShutterButton.performClick();
            performed = true;
        }
        if (LOG) {
            Log.i(TAG, "performPhotoShutter() mShutterButton=" + mShutterButton + ", return " + mShutterButton);
        }
        return performed;
    }

    public void setShutterButtonEnabled(boolean enabled) {
        if (LOG) {
            Log.i(TAG, "setShutterButtonEnabled(" + enabled + ")");
        }
        mShutterButton.setEnabled(enabled);
        refresh();
    }

    public boolean isShutterButtonEnabled() {
        return mShutterButton.isEnabled();
    }

    public boolean isShutterPressed() {
        return mShutterButton.isPressed();
    }
    @Override
    public void setEnabled(boolean enabled) {
        if (mShutterButton != null) {
            mShutterButton.setEnabled(enabled);
            mShutterButton.setClickable(enabled);
        }
    }

    public void enableShutter(boolean enable) {
        if (mShutterButton != null) {
            mShutterButton.setEnabled(enable);
        }
    }
    
    public void showTimeStamps(){
    	mRecordTimeText.setVisibility(View.VISIBLE);
    	mOutputTimeText.setVisibility(View.VISIBLE);
    }
    
    public void hideTimeStamps(){
    	mRecordTimeText.setVisibility(View.GONE);
    	mOutputTimeText.setVisibility(View.GONE);
    }
}
