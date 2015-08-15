/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |       Key        |       comment        */
/* ----------|----------------------|------------------|--------------------- */
/* 2013/12/17|   shangyong.zhang    |    CR572129      | one finger zoom dev  */
/* ----------|----------------------|------------------|--------------------- */
/* 2013/12/19|   Mingchun.Li        |    PR574769      | longpress snapshot   */
/* ----------|----------------------|------------------|--------------------- */
/* 2013/12/19|   shangyong.zhang    |    PR573322      | double click capture */
/* ----------|----------------------|------------------|--------------------- */
/******************************************************************************/

package com.android.camera.manager;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.os.Handler;
import android.os.Message;

import com.android.camera.CameraActivity;
import com.android.camera.GC;
import com.android.camera.CameraOneFingerZoomer;
import com.android.camera.TctTimelapseModule;
import com.android.camera.VideoModule;
import com.android.camera.WideAnglePanoramaModule;
import com.android.camera.ui.ZoomControlBar;
import com.android.camera.ui.FilmStripGestureRecognizer;
import com.android.camera.ui.FilmStripGestureRecognizer.Listener;
import com.android.camera.ui.ZoomControl.OnZoomIndexChangedListener;
import com.tct.camera.R;

public class ZoomManager extends ViewManager /*implements ScaleGestureDetector.OnScaleGestureListener implements CameraActivity.OnParametersReadyListener,
CameraActivity.OnFullScreenChangedListener,
CameraActivity.Resumable*/ {
    private static final String TAG = "ZoomManager";
    private static final boolean LOG = GC.LOG;

    private static final int UNKNOWN = -1;
    private static final int RATIO_FACTOR_RATE = 100;
    private final Handler mHandler = new MainHandler();
    private static final int SHOW_ZOOMBAR = 1;
    private static final int HIDE_ZOOMBAR = 2;

    //private Listener mGalleryGestureListener;
    //private MyListener mGestureListener = new MyListener();
    private ZoomControlBar mZoomControlBar;
    private boolean mResumed;
    private boolean mDeviceSupport;
    private int mLastZoomRatio = UNKNOWN;
    private int mLastZoomIndex = 0;
    //for scale gesture
    private static final float ZERO = 1;
    private float mZoomIndexFactor = ZERO;
    //for zooming behavior
    private boolean mIgnorGestureForZooming;
    private CameraActivity mCamera;
    private OnZoomIndexChangedListener mlistener;
    private MyListener mGestureListener;
    private CameraOneFingerZoomer mZoomer;
    private float mZoomStartFactor;
    private float mZoomTotalDeltaRatio;
    public static int minimunZoomIndex;
    public static int mediumZoomIndex;
    public static int maxmumZoomIndex;

    public ZoomManager(CameraActivity context) {
        super(context);
        mCamera = context;
    }

    public ZoomManager(CameraActivity context,int viewlayer) {
        super(context,viewlayer);
        mCamera = context;
    }

    public void init() {
        if (LOG) {
            Log.i(TAG,"init");
        }
        if(mGestureListener == null) mGestureListener = new MyListener();

        if(mZoomer == null)
        mZoomer = new CameraOneFingerZoomer(mContext, new CameraOneFingerZoomer.OnZoomerListener() {
            float xBegin, yBegin, xEnd, yEnd;
            int count = 0;
            public void onZoomEnd(float x, float y) {
                if (LOG) {
                    Log.i(TAG,"onZoomEnd:: x= " + x + " y= " + y);
                }
                xEnd = x;
                yEnd = y;
                //if (Math.abs(yEnd - yBegin) < 15.0f) {
                    //doZoomInZoomOut();//for the ergo have not this feature, just for standby
                //}
            }

            public void onZoomBegin(float x, float y) {
                if (LOG) {
                    Log.i(TAG,"onZoomBegin:: x= " + x + " y= " + y);
                }
                xBegin = x;
                yBegin = y;
                count = 0;

                mZoomStartFactor = mContext.mZoomRatios.get(mLastZoomIndex)/100;
                mZoomTotalDeltaRatio = 0.0f;
            }

            public void onZoom(float zoom) {
                if (LOG) {Log.i(TAG,"onZoom:: zoom = " + zoom);}
                // modify by minghui.hua for PR935718
                if (count++ < 5) {
                    // Don't zoom!!
                } else {
                    doZoomInZoomOut(zoom);
                }
            }
        });
    }

    public Listener getCameraGestureListener() {
		return mGestureListener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return (mContext.isInCamera() && mZoomer != null && mZoomer.onTouchEvent(event));
    }

    public void setOnZoomIndexChangedListener(OnZoomIndexChangedListener listener) {
        mlistener = listener;
    }

    @Override
    protected View getView() {
        View view = inflate(R.layout.indicator_bar);
        mZoomControlBar = (ZoomControlBar)view.findViewById(R.id.zoom_control);
        initialize();

        return view;
    }


    public void resetZoom() {
        mZoomIndexFactor = ZERO;
        if (isValidZoomIndex(0)) {
            mLastZoomRatio = mContext.mZoomRatios.get(0);
        }
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_ZOOMBAR: {
                    show();
                    break;
                }

                case HIDE_ZOOMBAR: {
                     hide();
                    break;
                }

            }
       }
   }

    public boolean onScale(float scale) {
        if (mlistener == null) return false;
        mZoomIndexFactor *= scale;
        if (mZoomIndexFactor <= ZERO) {
            mZoomIndexFactor = ZERO;
        } else if (mZoomIndexFactor >= getMaxZoomIndexFactor()) {
            mZoomIndexFactor = getMaxZoomIndexFactor();
        }
        int zoomIndex = findZoomIndex(Math.round(mZoomIndexFactor * RATIO_FACTOR_RATE));
        float zoomvalue = (float)mContext.mZoomRatios.get(zoomIndex)/100;
        if(mZoomControlBar == null || mShowing == false) {
            show();
        }
        mZoomControlBar.setZoomIndex(zoomvalue);
        performZoom(zoomIndex, true);
        return true;
    }
    public void performZoom(int zoomIndex, boolean userAction) {
        if (LOG) {
            Log.i(TAG,"performZoom(zoomIndex:"+zoomIndex+",userAction:"+userAction+")");
        }
        mLastZoomIndex = zoomIndex;
        mHandler.removeMessages(HIDE_ZOOMBAR);
        mHandler.sendEmptyMessageDelayed(HIDE_ZOOMBAR, 2000);
        if (mlistener != null)
            mlistener.onZoomIndexChanged(zoomIndex);
    }

    // Zoom to max or keep original.
    private void doZoomInZoomOut() {
        if (LOG) {Log.i(TAG,"doZoomInZoomOut");}
        int oldIndex = mLastZoomIndex;
        int zoomIndex = 0;
        if (oldIndex == 0) {
            mZoomIndexFactor = getMaxZoomIndexFactor();
        } else {
            mZoomIndexFactor = ZERO;
        }
        if(mZoomControlBar == null || mShowing == false) {
            show();
        }
        mZoomControlBar.setZoomIndex(mZoomIndexFactor);
        zoomIndex = findZoomIndex(Math.round(mZoomIndexFactor * RATIO_FACTOR_RATE));
        performZoom(zoomIndex, true);
    }

    // Zoom with the appointed delta ratio.
    private void doZoomInZoomOut(float ratio) {
        if (ratio > 1.0f || ratio < -1.0f) {
            if (LOG) {Log.w(TAG,"Bad Ratio::doZoomInZoomOut:ratio = " + ratio);}
            return;
        }
        if (LOG) {Log.i(TAG,"doZoomInZoomOut::ratio = " + ratio);}
        float maxFactor = getMaxZoomIndexFactor();
        if (LOG) {Log.i(TAG,"doZoomInZoomOut::maxFactor = " + maxFactor);}
        mZoomTotalDeltaRatio += ratio;
        mZoomIndexFactor = (float)((maxFactor-1.0) * mZoomTotalDeltaRatio + mZoomStartFactor);
        if (mZoomIndexFactor > maxFactor) {
            mZoomIndexFactor = maxFactor;
            mZoomTotalDeltaRatio -= ratio;
        } else if (mZoomIndexFactor < 1) {
            mZoomIndexFactor = 1;
            mZoomTotalDeltaRatio -= ratio;
        }
        if (LOG) {Log.i(TAG,"doZoomInZoomOut::mZoomTotalDeltaRatio = " + mZoomTotalDeltaRatio);}
        if (LOG) {Log.i(TAG,"doZoomInZoomOut::mZoomIndexFactor = " + mZoomIndexFactor);}
        if(mZoomControlBar == null || mShowing == false) {show();}
        mZoomControlBar.setZoomIndex(mZoomIndexFactor);
        int zoomIndex = findZoomIndex(Math.round(mZoomIndexFactor * RATIO_FACTOR_RATE));
        performZoom(zoomIndex, true);
    }

    private class MyListener implements FilmStripGestureRecognizer.Listener {
        @Override
        public boolean onDown(float x, float y) {
            if (LOG) {
                Log.i(TAG,"onDown("+x+","+y+")");
            }

            mIgnorGestureForZooming = false;
            return false;
        }

        @Override
        public boolean onFling(float velocityX, float velocityY) {
            if (LOG) {
                Log.i(TAG,"onFling velocityx:"+velocityX+" velocityY:"+velocityY);
            }
//            if (velocityX > 1000 && (Math.abs(velocityX) > Math.abs(velocityY)) && mCamera.isNonePickIntent()) {// left
//                mContext.goToScanMode();
//            }else{//right
//                mContext.backToPhotoMode();
//            }
            if (velocityX < (-1000) && (Math.abs(velocityX) > Math.abs(velocityY))){
                if(mContext.getCurrentModule() instanceof VideoModule){
                    if(((VideoModule) mContext.getCurrentModule()).isRecording())
                        return false;
                }
                if(mContext.getCurrentModule() instanceof WideAnglePanoramaModule){
                    return false;
                }
                if(mContext.getCurrentModule() instanceof TctTimelapseModule){
                    return false;
                }
                if(mContext.getTotalNumber() == 1){
                    // have no picture
                    return false;
                }
                if(mGC.getCurrentMode() != GC.MODE_POLAROID){
                	mContext.backToPhotoMode();
                }
            }
            return false;
        }

        @Override
        public boolean onScroll(float dx, float dy, float totalX, float totalY) {
            if (LOG) {
                Log.i(TAG,"onScroll isInCameraApp:"+getContext().isInCamera()
                    +"dx:"+dx+" dy:"+dy+" totalX:"+totalX+" totalY:"+totalY);
            }

            if (shouldIgnoreCurrentGesture()) {
                return false;
            }

            if (mZoomer.isZooming()) {return true;}

            mContext.setViewState(GC.VIEW_STATE_FLING);

            return false;
        }

        @Override
        public boolean onSingleTapUp(float x, float y) {
            if (LOG) {
                Log.i(TAG,"MyListener.onSingleTapUp x:"+x+" y:"+y);
            }
            Log.i(TAG, "doom onsingletapup");
            getContext().onSingleTapUp(null, (int)x,(int)y);
            return false;
        }

        @Override
        public boolean onUp(float x, float y) {
            if (LOG) {
                Log.i(TAG,"onUp");
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(float x, float y) {
            Log.i(TAG, "onDoubleTap x="+x+" y="+y+" mLastZoomIndex="+mLastZoomIndex);
            if (!isAppSupported() || !isEnabled() || mContext.mZoomRatios == null) {
                Log.w(TAG, "onDoubleTap return");
                return false;
            }
            minimunZoomIndex = findZoomIndex(mContext.mZoomRatios.get(0));
            maxmumZoomIndex = mContext.mZoomRatios.size()-1;
//        	mediumZoomIndex = (minimunZoomIndex + maxmumZoomIndex)/2;
            mediumZoomIndex = 18; //change 55 to 18 when change zoom ratio <200

            if(mLastZoomIndex == minimunZoomIndex){
                float doubleZoomvalue = (float)mContext.mZoomRatios.get(mediumZoomIndex)/100;
                if(mZoomControlBar == null || mShowing == false) {
                    show();
                }
                Log.i(TAG, "doom first to medium = " + doubleZoomvalue);
                mZoomControlBar.setZoomIndex(doubleZoomvalue);
                performZoom(mediumZoomIndex, true);
            }else if (mLastZoomIndex == mediumZoomIndex){
                float doubleZoomvalue = (float)mContext.mZoomRatios.get(maxmumZoomIndex)/100;
                if(mZoomControlBar == null || mShowing == false) {
                    show();
                }
                mZoomControlBar.setZoomIndex(doubleZoomvalue);
                performZoom(maxmumZoomIndex, true);
            }else if(mLastZoomIndex == maxmumZoomIndex){
                float doubleZoomvalue = (float)mContext.mZoomRatios.get(minimunZoomIndex)/100;
                if(mZoomControlBar == null || mShowing == false) {
                    show();
                }
                mZoomControlBar.setZoomIndex(doubleZoomvalue);
                performZoom(minimunZoomIndex, true);
            }else{
                float doubleZoomvalue = (float)mContext.mZoomRatios.get(0)/100;
                if(mZoomControlBar == null || mShowing == false) {
                    show();
                }
                mZoomControlBar.setZoomIndex(doubleZoomvalue);
                performZoom(0, true);
            }
            getContext().setViewState(GC.VIEW_STATE_SWITCH_PHOTO_VIDEO);

            if (mZoomer != null) {
                mZoomer.onDoubleTap(x, y);
            }

            return true;
        }

        @Override
        public boolean onScale(float focusX, float focusY, float scale) {
            if (LOG) {
                Log.i(TAG, "onScale isInCameraApp:"+getContext().isInCamera()+"(" + focusX + ", " + focusY + ", " + scale + ")");
            }

            if (!isAppSupported() || !isEnabled()) {
                return false;
            }

            if (Float.isNaN(scale) || Float.isInfinite(scale)) {
                return false;
            }

            getContext().setViewState(GC.VIEW_STATE_SWITCH_PHOTO_VIDEO);

            mZoomIndexFactor *= scale;
            if (mZoomIndexFactor <= ZERO) {
                mZoomIndexFactor = ZERO;
            } else if (mZoomIndexFactor >= getMaxZoomIndexFactor()) {
                mZoomIndexFactor = getMaxZoomIndexFactor();
            }
            int zoomIndex = findZoomIndex(Math.round(mZoomIndexFactor * RATIO_FACTOR_RATE));
            float zoomvalue = (float)mContext.mZoomRatios.get(zoomIndex)/100;
            if(mZoomControlBar == null || mShowing == false) {
                show();
            }
            mZoomControlBar.setZoomIndex(zoomvalue);
            performZoom(zoomIndex, true);
            Log.i(TAG, "doom zoomvalue= " + zoomvalue + " zoomIndex= " + zoomIndex);
            return true;

        }

        @Override
        public boolean onScaleBegin(float focusX, float focusY) {
            if (LOG) {
                Log.i(TAG, "onScaleBegin(" + focusX + ", " + focusY + ")");
            }

            mIgnorGestureForZooming = true;
            return true;
        }

        @Override
        public void onScaleEnd() {
            if (LOG) {
                Log.i(TAG, "onScaleEnd");
            }
        }
        @Override
        public void onLongPress(float x, float y) {
            if (LOG) {
                Log.i(TAG, "onLongPress");
            }

            if (mZoomer.isZooming()) {return;}

            // modify by minghui.hua for PR952806 new requirement
//            if (getContext().getCurrentModule() instanceof PhotoModule)
//                getContext().getShutterManager().performShutter();
        }
    }

    private boolean isAppSupported() {
        boolean enable = GC.isZoomEnable(mGC.getCurrentMode());
        return enable;
    }

    private int findZoomIndex(int zoomRatio) {
        int find = 0; //if not find, return 0
        if (mContext.mZoomRatios != null) {
            int len = mContext.mZoomRatios.size();
            if (len == 1) {
                find = 0;
            } else {
                int max = mContext.mZoomRatios.get(len - 1);
                int min = mContext.mZoomRatios.get(0);
                if (zoomRatio <= min) {
                    find = 0;
                } else if (zoomRatio >= max) {
                    find = len - 1;
                } else {
                    for (int i = 0; i < len - 1; i++) {
                        int cur = mContext.mZoomRatios.get(i);
                        int next = mContext.mZoomRatios.get(i + 1);
                        if (zoomRatio >= cur && zoomRatio < next) {
                            find = i;
                            break;
                        }
                    }
                }
            }
        }
        return find;
    }

    private boolean isValidZoomIndex(int zoomIndex) {
        boolean valid = false;
        if (mContext.mZoomRatios != null && zoomIndex >= 0 && zoomIndex < mContext.mZoomRatios.size()) {
            valid = true;
        }
        if (LOG) {
            Log.i(TAG, "isValidZoomIndex(" + zoomIndex + ") return " + valid);
        }
        return valid;
    }

    public int getMaxZoomIndexEx() {
        return getMaxZoomIndex();
    }

    private int getMaxZoomIndex() {
        int index = UNKNOWN;
        if (mContext.mZoomRatios != null) {
            index = mContext.mZoomRatios.size() - 1;
        }
        return index;
    }

    public float getMaxZoomIndexFactorEx(){
        return getMaxZoomIndexFactor();
    }

    private float getMaxZoomIndexFactor() {
        return (float)getMaxZoomRatio() / RATIO_FACTOR_RATE;
    }

    private int getMaxZoomRatio() {
        int ratio = UNKNOWN;
        if (mContext.mZoomRatios != null) {
            ratio = mContext.mZoomRatios.get(mContext.mZoomRatios.size() - 1);
        }
        return ratio;
    }

    private boolean shouldIgnoreCurrentGesture() {
        return (isAppSupported() && isEnabled() && mIgnorGestureForZooming )
                  || (mGC.getCurrentMode() == GC.MODE_EXPRESSION4
                  && mGC.getExpressionState());
    }
}
