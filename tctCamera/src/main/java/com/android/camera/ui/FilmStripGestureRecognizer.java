/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.camera.ui;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.util.Log;

import com.android.camera.CameraActivity;

// This class aggregates two gesture detectors: GestureDetector,
// ScaleGestureDetector.
public class FilmStripGestureRecognizer {
    @SuppressWarnings("unused")
    private static final String TAG = "FilmStripGestureRecognizer";

    public interface Listener {
        boolean onSingleTapUp(float x, float y);
        boolean onDoubleTap(float x, float y);
        boolean onScroll(float x, float y, float dx, float dy);
        boolean onFling(float velocityX, float velocityY);
        boolean onScaleBegin(float focusX, float focusY);
        boolean onScale(float focusX, float focusY, float scale);
        boolean onDown(float x, float y);
        boolean onUp(float x, float y);
        void onScaleEnd();
        void onLongPress(float x, float y);
    }

    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    private final Listener mListener;
    //[BUGFIX]-Modify by TCTNB,bin.zhang2, 2014-06-27,PR702954 Begin
    private Listener mCameraListener;
    //[BUGFIX]-Modify by TCTNB,bin.zhang2, 2014-06-27,PR702954 End
    private CameraActivity mContext;
    private boolean mIgnorGestureForZooming;
    private boolean firstDown=false;  //Add by fei.gao for PR964866


    public FilmStripGestureRecognizer(Context context, Listener listener, Listener cameraListener) {
        mContext = (CameraActivity) context;
        mListener = listener;
        mCameraListener = cameraListener;
        mGestureDetector = new GestureDetector(context, new MyGestureListener(),
                null, true /* ignoreMultitouch */);
        mGestureDetector.setOnDoubleTapListener(new MyDoubleTapListener());
        mScaleDetector = new ScaleGestureDetector(
                context, new MyScaleListener());
    }

    //[BUGFIX]-Add by TCTNB,bin.zhang2, 2014-06-27,PR702954 Begin
    public void setCameraListener(Listener cameraListener) {
        mCameraListener = cameraListener;
    }
    //[BUGFIX]-Add by TCTNB,bin.zhang2, 2014-06-27,PR702954 End

    public void onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
        	if(mContext.judgeIsOnDownInCamera()){
        		firstDown = true;
        	}else{
        		firstDown =false;
        	}
            if (mContext.isInCamera()) {
                mCameraListener.onUp(event.getX(), event.getY());
            } else {
                mListener.onUp(event.getX(), event.getY());
            }
        }
    }
    public Listener getGestureListener() {
        return mListener;
    }
    private boolean shouldIgnoreCurrentGesture() {
        return mIgnorGestureForZooming;
    }
    private class MyGestureListener
                extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(
                MotionEvent e1, MotionEvent e2, float dx, float dy) {
        	if (mContext.isInCamera()) {
        		if(!mContext.getCameraViewManager().getTopViewManager().getOverallMenuManager().isShowing())
        		mContext.enableDrawer();
                if (shouldIgnoreCurrentGesture()) {
                    return false;
                }
                Log.i(TAG,"LMCH0416 dx:"+dx);
                if (dx < 20 && dx > -20) {
                    return false;
                }
                
                return mListener.onScroll(e2.getX(), e2.getY(), dx, dy);
            } else {
            	mContext.disableDrawer();
                return mListener.onScroll(e2.getX(), e2.getY(), dx, dy);
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            if (mContext.isInCamera()) {
                if (shouldIgnoreCurrentGesture()) {
                    return false;
                }
                return mListener.onFling(velocityX, velocityY);
            } else {
                return mListener.onFling(velocityX, velocityY);
            }
        }

        @Override
        public boolean onDown(MotionEvent e) {
        	 //Add by fei.gao for PR964866 -begin
        	if(mContext.judgeIsOnDownInCamera()){
        		firstDown = true;
        	}else{
        		firstDown=false;
        	}
        	 //Add by fei.gao for PR964866 -end
            if(mContext.isInCamera()) {
                mIgnorGestureForZooming = false;
                mCameraListener.onDown(e.getX(), e.getY());
            } else {
                mListener.onDown(e.getX(), e.getY());
            }
            return super.onDown(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mContext.isInCamera()) {
                mCameraListener.onLongPress(e.getX(), e.getY());
            }
        }
    }

    /**
     * The listener that is used to notify when a double-tap or a confirmed
     * single-tap occur.
     */
    private class MyDoubleTapListener implements GestureDetector.OnDoubleTapListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mContext.isInCamera()) {
                return mCameraListener.onSingleTapUp(e.getX(), e.getY());
            } else {
                return mListener.onSingleTapUp(e.getX(), e.getY());
            }
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mContext.isInCamera()) {
                return mCameraListener.onDoubleTap(e.getX(), e.getY());
            } else {
                return mListener.onDoubleTap(e.getX(), e.getY());
            }
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }
    }

    private class MyScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if (mContext.isInCamera()) {
                mIgnorGestureForZooming = true;
                return mCameraListener.onScaleBegin(
                        detector.getFocusX(), detector.getFocusY());
            } else {
                return mListener.onScaleBegin(
                        detector.getFocusX(), detector.getFocusY());
            }
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (mContext.isInCamera()) {
                return mCameraListener.onScale(detector.getFocusX(),
                        detector.getFocusY(), detector.getScaleFactor());
            } else {
                return mListener.onScale(detector.getFocusX(),
                        detector.getFocusY(), detector.getScaleFactor());
            }
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (mContext.isInCamera()) {
                mCameraListener.onScaleEnd();
            } else {
                mListener.onScaleEnd();
            }
        }
    }
    public boolean isDownInCamera(){
    	return firstDown;
    }
}
