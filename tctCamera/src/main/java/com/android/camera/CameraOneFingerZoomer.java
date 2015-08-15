package com.android.camera;

import android.content.Context;
import android.view.MotionEvent;
import android.util.Log;
import com.android.camera.GC;

public class CameraOneFingerZoomer {
    private static final String TAG = "CameraOneFingerZoomer";
    private static final boolean LOG = GC.LOG;
    private CameraActivity mContext = null;
    private float mOneTimeDistance = 400;
    private OnZoomerListener mListener = null;
    private float mLastY = 0;

    public interface OnZoomerListener {
        /**
         * Begin zooming
         * @param e touch event when zooming is started
         */
        void onZoomBegin(float x, float y);

        /**
         * End zooming
         */
        void onZoomEnd(float x, float y);

        /**
         * Zooming value changed. Based on last zoom
         * @param zoom zoom rate
         */
        void onZoom(float zoom);
    }

    /**
     * Constructor Method
     * @param context context
     * @param listener {@link OnZoomerListener}
     */
    public CameraOneFingerZoomer(CameraActivity context, OnZoomerListener listener) {
        mContext = context;
        mListener = listener;
        mOneTimeDistance = (float) (context.getResources().getDisplayMetrics().heightPixels) / 2.0f;
    }

    /**
     * Check whether zooming is in progress
     * @return true: zooming. false: otherwise
     */
    public boolean isZooming() {
        return mContext.mIsZooming;
    }

    public void onDoubleTap(float x, float y) {
        if (LOG) {Log.i(TAG,"onDoubleTap ");}	
        mContext.setZoomingStatus(true);
        mLastY = y;
        if (mListener != null) {
            mListener.onZoomBegin(x, y);
        }

    }

    /**
     * Process the touch event
     * @param ev touch event
     * @return true: event processed. false: otherwise
     */
    public boolean onTouchEvent(MotionEvent ev) {
        if (mContext.mIsZooming) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (LOG) {Log.i(TAG,"ZoomManager::CameraOneFingerZoomer.ACTION_DOWN");}
                    // Stop Zooming when get another ACTION_DOWN, in case
                    // ACTION_UP is not received by some reason
                case MotionEvent.ACTION_UP:
                    if (LOG) {Log.i(TAG,"ZoomManager::CameraOneFingerZoomer.ACTION_UP ");}
                    if (mListener != null) {
                        mListener.onZoomEnd(ev.getX(), ev.getY());
                    }
                    mContext.setZoomingStatus(false);
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (LOG) {Log.i(TAG,"ZoomManager::CameraOneFingerZoomer.ACTION_MOVE");}
                    float deltaY = ev.getRawY() - mLastY;
                    float zoomRate = (float)(deltaY / mOneTimeDistance);
                    if (zoomRate > 1) {zoomRate = 1.0f;}
                    else if (zoomRate < -1) {zoomRate = -1.0f;}
                    if (mListener != null) {
                        mListener.onZoom(zoomRate);
                    }
                    mLastY = ev.getY();
                    break;
            }
        }
        return mContext.mIsZooming;
    }
}

