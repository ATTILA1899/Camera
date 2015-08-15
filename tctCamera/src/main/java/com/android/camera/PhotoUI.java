/*
 * Copyright (C) 2012 The Android Open Source Project
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
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* ----------|----------------------|----------------------|----------------- */
/* 08/21/2014|jian.zhang1           |770765                |Set rotation for  */
/*           |                      |                      |timer count view  */
/* ----------|----------------------|----------------------|----------------- */
package com.android.camera;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewStub;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.camera.CameraPreference.OnPreferenceChangedListener;
import com.android.camera.FocusOverlayManager.FocusUI;
import com.android.camera.custom.CustomFields;
import com.android.camera.custom.CustomUtil;
import com.android.camera.manager.BackButtonManager;
import com.android.camera.ui.AbstractSettingPopup;
import com.android.camera.ui.CameraControls;
import com.android.camera.ui.CameraRootView;
import com.android.camera.ui.CountDownView;
import com.android.camera.ui.CountDownView.OnCountDownFinishedListener;
import com.android.camera.ui.FaceView;
import com.android.camera.ui.FocusIndicator;
import com.android.camera.ui.FocusIndicatorRotateLayout;
import com.android.camera.ui.ModuleSwitcher;
import com.android.camera.ui.PieRenderer;
import com.android.camera.ui.PieRenderer.PieListener;
import com.android.camera.ui.RenderOverlay;
import com.android.camera.ui.RotatePhoto;
import com.android.camera.ui.ShutterButton;
import com.android.camera.ui.ZoomRenderer;
import com.android.camera.util.CameraUtil;
import com.jrdcamera.modeview.PolaroidView;
import com.tct.camera.R;
//import android.widget.RelativeLayout.LayoutParams;

public class PhotoUI implements PieListener,
    PreviewGestures.SingleTapListener,
    FocusUI, TextureView.SurfaceTextureListener,
    LocationManager.Listener, CameraRootView.MyDisplayListener,
    CameraManager.CameraFaceDetectionCallback {

    private static final String TAG = "CAM_UI";
    private static final int DOWN_SAMPLE_FACTOR = 4;
    private final AnimationManager mAnimationManager;
    private CameraActivity mActivity;
    private PhotoController mController;
    private PreviewGestures mGestures;

    private View mRootView;
    private SurfaceTexture mSurfaceTexture;

    private PopupWindow mPopup;
    private ShutterButton mShutterButton;
    private CountDownView mCountDownView;

    protected FaceView mFaceView;
    private FocusIndicatorRotateLayout mFocusAreaIndicator;
    private RenderOverlay mRenderOverlay;
    private View mReviewCancelButton;
    private View mReviewDoneButton;
    private View mReviewRetakeButton;
    private RotatePhoto mReviewImage;
    private DecodeImageForReview mDecodeTaskForReview = null;
    private DecodeImageForPolaroid mDecodeTaskForPolaroid = null;

    private View mMenuButton;
    private PhotoMenu mMenu;
    private ModuleSwitcher mSwitcher;
    private CameraControls mCameraControls;
    private AlertDialog mLocationDialog;

    // Small indicators which show the camera settings in the viewfinder.
    private OnScreenIndicators mOnScreenIndicators;

    private PieRenderer mPieRenderer;
    private ZoomRenderer mZoomRenderer;
    private Toast mNotSelectableToast;

    private int mZoomMax;
    private List<Integer> mZoomRatios;

    private int mPreviewWidth = 0;
    private int mPreviewHeight = 0;
    public boolean mMenuInitialized = false;
    private float mSurfaceTextureUncroppedWidth;
    private float mSurfaceTextureUncroppedHeight;
    private TextView burstShootingNum = null;
    private TextView burstShootingNumRight = null;
    private TextView asdCurrentScene=null;

    private ImageView mPreviewThumb;
    private View mFlashOverlay;
    private PolaroidView mPolaroidView;
    private BackButtonManager mBackButtonManager;

    private SurfaceTextureSizeChangedListener mSurfaceTextureSizeListener;
    private TextureView mTextureView;
    private Matrix mMatrix = null;
    private float mAspectRatio = 4f / 3f;
    private boolean mAspectRatioResize;
    private RelativeLayout mPreviewFrameLayout;

    private boolean mOrientationResize;
    private boolean mPrevOrientationResize;
    private View mPreviewCover;
    private final Object mSurfaceTextureLock = new Object();
    
    public interface SurfaceTextureSizeChangedListener {
        public void onSurfaceTextureSizeChanged(int uncroppedWidth, int uncroppedHeight);
    }

    private OnLayoutChangeListener mLayoutListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right,
                int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            int width = right - left;
            int height = bottom - top;
            if (mPreviewWidth != width || mPreviewHeight != height
                    || (mOrientationResize != mPrevOrientationResize)
                    || mAspectRatioResize) {
                mPreviewWidth = width;
                mPreviewHeight = height;
                	setTransformMatrix(width, height);	
                	mController.onScreenSizeChanged((int) mSurfaceTextureUncroppedWidth,
                        (int) mSurfaceTextureUncroppedHeight);
                mAspectRatioResize = false;
            }
        }
    };

    //for full screen and non-full screen
    RelativeLayout.LayoutParams lp;
    int mOnDip;
    int mOffDip;
    int mTopDip;
    int mBottomDip;

    public void updatePreviewLayout(int pw,int ph,boolean isPolaroid) {
        //if (!mActivity.isInCameraApp()) return;
        RelativeLayout.LayoutParams oldLP =
                (android.widget.RelativeLayout.LayoutParams) mPreviewFrameLayout.getLayoutParams();
        lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        int w = mActivity.getResources().getDisplayMetrics().heightPixels;
        int h = mActivity.getResources().getDisplayMetrics().widthPixels;
        try {
            android.graphics.Point realSize = new android.graphics.Point();
            Display.class.getMethod("getRealSize",
                    android.graphics.Point.class).invoke(mActivity.getWindowManager().getDefaultDisplay(), realSize);
            w = realSize.x;
            h = realSize.y;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "tiny w= " + w + "  h= " + h);
        float lcdLongside = Math.max(w,h);
        float lcdShortside = Math.min(w,h);
        float picsizeLongside = Math.max(pw,ph);
        float picsizeShortside = Math.min(pw,ph);

        double rate = 1 - 0.538;//0.538 is the rate for the bottom_gap/bottom_gap+top_gap 
        double ratio = (double) picsizeLongside/picsizeShortside;
        /*lcdLongside - lcdShortside * picsizeLongside/picsizeShortside    this part means the black gaps on the screen , both the top one and the bottom one are included*/
        if(isPolaroid){
        	mTopDip = Math.abs((h-w)/2);
        	mBottomDip = Math.abs((h-w)/2);
        }else{
        	mTopDip = (int)((lcdLongside - lcdShortside * picsizeLongside/picsizeShortside) * rate);
        	mBottomDip = (int)((lcdLongside - lcdShortside * picsizeLongside/picsizeShortside) * (1-rate));
        }
        if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            lp.leftMargin=mTopDip;
            lp.rightMargin=mBottomDip;
        } else {
            lp.topMargin=mTopDip;
            lp.bottomMargin=mBottomDip;
        }
        if((oldLP.topMargin == lp.topMargin && oldLP.topMargin!=0)
                ||(oldLP.leftMargin == lp.leftMargin && oldLP.leftMargin!=0)){
            return;
        }

        Log.i(TAG,"LMCH0418 mTopDip:"+mTopDip+" mBottomDip:"+mBottomDip);
        mPreviewFrameLayout.setLayoutParams(lp);
    }

    public boolean isPreviewFullScreen() {
        RelativeLayout.LayoutParams mLp =
            (android.widget.RelativeLayout.LayoutParams) mPreviewFrameLayout.getLayoutParams();
        int margin = 0;
        if (mActivity.getResources().getConfiguration().orientation ==
                                        Configuration.ORIENTATION_LANDSCAPE) {
            margin = (int)Math.max(mLp.leftMargin, mLp.rightMargin);
        } else {
            margin = (int)Math.max(mLp.topMargin, mLp.bottomMargin);
        }
        return (0 == margin);
    }

    @Override
    public void resetFocusAreaLayout() {
        // Put focus indicator to the center.
        if(mFocusAreaIndicator != null){
            RelativeLayout.LayoutParams p =
                    (RelativeLayout.LayoutParams) mFocusAreaIndicator.getLayoutParams();
            int[] rules = p.getRules();
            rules[RelativeLayout.CENTER_IN_PARENT] = RelativeLayout.TRUE;
            p.setMargins(0, 0, 0, 0);
        }
    }

    @Override
    public void updateFocusAreaLayout(int previewWidth, int previewHeight, int x,int y) {
        int focusWidth = 0;
        int focusHeight = 0;
        if(mFocusAreaIndicator != null){
            focusWidth = mFocusAreaIndicator.getWidth();
            focusHeight = mFocusAreaIndicator.getHeight();
            if (focusWidth == 0 || focusHeight == 0) {
                Log.i(TAG, "UI Component not initialized, cancel this touch");
                return;
            }
            RelativeLayout.LayoutParams p =
                    (RelativeLayout.LayoutParams) mFocusAreaIndicator.getLayoutParams();
            int left = CameraUtil.clamp(x - focusWidth / 2, 0, previewWidth - focusWidth);
            int top = CameraUtil.clamp(y - focusHeight / 2 - mTopDip, 0, previewHeight - focusHeight);
            p.setMargins(left, top, 0, 0);
            // Disable "center" rule because we no longer want to put it in the center.
            int[] rules = p.getRules();
            rules[RelativeLayout.CENTER_IN_PARENT] = 0;
            mFocusAreaIndicator.requestLayout();
        }
    }

    private class DecodeTask extends AsyncTask<Void, Void, Bitmap> {
        private final byte [] mData;
        private int mOrientation;
        private boolean mMirror;

        public DecodeTask(byte[] data, int orientation, boolean mirror) {
            mData = data;
            mOrientation = orientation;
            mMirror = mirror;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            // Decode image in background.
            Bitmap bitmap = CameraUtil.downSample(mData, DOWN_SAMPLE_FACTOR);
            if ((mOrientation != 0 || mMirror) && (bitmap != null)) {
                Matrix m = new Matrix();
                if (mMirror) {
                    // Flip horizontally
                    m.setScale(-1f, 1f);
                }
                m.preRotate(mOrientation);
                Bitmap bmp=Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m,
                        false);
                if(bmp!=bitmap){
                	bitmap.recycle();
                	bitmap=bmp;
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
        	if(bitmap!=null&&!bitmap.isRecycled())
        		mPreviewThumb.setImageBitmap(bitmap);
        	
        	
            mAnimationManager.startCaptureAnimation(mPreviewThumb);
        }
    }

    private class DecodeImageForReview extends DecodeTask {
        public DecodeImageForReview(byte[] data, int orientation, boolean mirror) {
            super(data, orientation, mirror);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                return;
            }
            mReviewImage.setImageBitmap(bitmap);
            mReviewImage.setVisibility(View.VISIBLE);
          //[BUGFIX]-Add by yongsheng.shan, 2015-01-20,PR900632 Begin
            mBackButtonManager.hide();
          //[BUGFIX]-Add by yongsheng.shan, 2015-01-20,PR900632 End
            mDecodeTaskForReview = null;
        }
    }

    public PhotoUI(CameraActivity activity, PhotoController controller, View parent) {
        mActivity = activity;
        mController = controller;
        mRootView = parent;

        mActivity.getLayoutInflater().inflate(R.layout.photo_module,
                (ViewGroup) mRootView, true);
        mRenderOverlay = (RenderOverlay) mRootView.findViewById(R.id.render_overlay);
        mFlashOverlay = mRootView.findViewById(R.id.flash_overlay);
        mPolaroidView = (PolaroidView) mRootView.findViewById(R.id.polaroid_view);
        mPolaroidView.setPhotoUI(this);
        mPreviewCover = mRootView.findViewById(R.id.preview_cover);
        // display the view
        mTextureView = (TextureView) mRootView.findViewById(R.id.preview_content);
        CameraActivity.TraceQCTLog("GetTextureView");
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.addOnLayoutChangeListener(mLayoutListener);
        asdCurrentScene=(TextView)mRootView.findViewById(R.id.asd_title);
        burstShootingNum = (TextView)mRootView.findViewById(R.id.brust_shooting);
        burstShootingNumRight = (TextView)mRootView.findViewById(R.id.brust_shooting_right);
        mPreviewFrameLayout =(RelativeLayout) mRootView.findViewById(R.id.preview_frame_layout);
        initIndicators();
       //[BUGFIX]-Add by yongsheng.shan, 2015-01-20,PR900632 Begin
        mBackButtonManager = mActivity.getBackButtonManager();
      //[BUGFIX]-Add by yongsheng.shan, 2015-01-20,PR900632 End        

        //mShutterButton = (ShutterButton) mRootView.findViewById(R.id.shutter_button);
        //mSwitcher = (ModuleSwitcher) mRootView.findViewById(R.id.camera_switcher);
        //mSwitcher.setCurrentIndex(ModuleSwitcher.PHOTO_MODULE_INDEX);
        //mSwitcher.setSwitchListener(mActivity);
        //mMenuButton = mRootView.findViewById(R.id.menu);
        ViewStub faceViewStub = (ViewStub) mRootView
                .findViewById(R.id.face_view_stub);
        if (faceViewStub != null) {
            faceViewStub.inflate();
            mFaceView = (FaceView) mRootView.findViewById(R.id.face_view);
            setSurfaceTextureSizeChangedListener(mFaceView);
        }
        mFocusAreaIndicator = (FocusIndicatorRotateLayout) mRootView.findViewById(R.id.focus_indicator_rotate_layout);
        //mCameraControls = (CameraControls) mRootView.findViewById(R.id.camera_controls);
        mAnimationManager = new AnimationManager();

        mOrientationResize = false;
        mPrevOrientationResize = false;
        
    }

        
    public void setCurrentModeVisible(String mode,boolean visible,boolean needUndo,boolean longDis){
//    	asdCurrentScene.setText(mode);
//    	if(visible){
//    		asdCurrentScene.setVisibility(View.VISIBLE);
//    	}else{
//    		asdCurrentScene.setVisibility(View.GONE);
//    	}
//    	asdCurrentScene.invalidate();
    	mActivity.showInfo(mode,needUndo,longDis);
    	
//    	mAsdToast.setText(mode);
//    	if(visible){
//    		if(mActivity.isInCamera()&&!mActivity.getCameraViewManager().
//    				getTopViewManager().isOverallMenuShowing())
//    			mAsdToast.show();
//    	}else{
//    		mAsdToast.cancel();
//    	}
    }
    
    public void setBurstShootingVisibility(int number) {
        Log.i(TAG, "LMCH04233 number:" + number);
        if (CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_TCTCAMERA_BURST_SHOOTING_NUM_TOPRIGHT, false)) {
            if (number > 0) {
                burstShootingNumRight.setVisibility(View.VISIBLE);
                burstShootingNumRight.setText("" + number);
                burstShootingNumRight.invalidate();
            } else {
                burstShootingNumRight.setVisibility(View.GONE);
            }
        } else {
            if (number > 0) {
                burstShootingNum.setVisibility(View.VISIBLE);
                burstShootingNum.setText("" + number);
                burstShootingNum.invalidate();
            } else {
                burstShootingNum.setVisibility(View.GONE);
            }
        }
    }

     public void cameraOrientationPreviewResize(boolean orientation){
        mPrevOrientationResize = mOrientationResize;
        mOrientationResize = orientation;
     }

    public void setAspectRatio(float ratio) {
        if (ratio <= 0.0) throw new IllegalArgumentException();

        if (mOrientationResize &&
                mActivity.getResources().getConfiguration().orientation
                != Configuration.ORIENTATION_PORTRAIT) {
            ratio = 1 / ratio;
        }

        Log.d(TAG,"setAspectRatio() ratio["+ratio+"] mAspectRatio["+mAspectRatio+"]");
        mAspectRatio = ratio;
        mAspectRatioResize = true;
        mTextureView.requestLayout();
        if(null != mTextureView && mActivity.getGC().getCurrentMode() == GC.MODE_POLAROID && null !=  mPolaroidView && mPolaroidView.getPolarCount() == PolaroidView.POLAR_FOUR && mTextureView.getScaleX() != 0.5f){
        	mTextureView.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					mTextureView.setScaleX(0.5f);
		        	mTextureView.setScaleY(0.5f);
				}
			});  // hai you oritation
        	
        }
    }

    public void setSurfaceTextureSizeChangedListener(SurfaceTextureSizeChangedListener listener) {
        mSurfaceTextureSizeListener = listener;
    }

    private void setTransformMatrix(int width, int height) {
        mMatrix = mTextureView.getTransform(mMatrix);
        float scaleX = 1f, scaleY = 1f;
        float scaledTextureWidth, scaledTextureHeight;
        if (mOrientationResize){
            scaledTextureWidth = height * mAspectRatio;
            if(scaledTextureWidth > width){
                scaledTextureWidth = width;
                scaledTextureHeight = scaledTextureWidth / mAspectRatio;
            } else {
                scaledTextureHeight = height;
            }
        } else {
            if (width > height) {
                scaledTextureWidth = Math.max(width,
                        (int) (height * mAspectRatio));
                scaledTextureHeight = Math.max(height,
                        (int)(width / mAspectRatio));
            } else {
                scaledTextureWidth = Math.max(width,
                        (int) (height / mAspectRatio));
                scaledTextureHeight = Math.max(height,
                        (int) (width * mAspectRatio));
            }
        }

        if (mSurfaceTextureUncroppedWidth != scaledTextureWidth ||
                mSurfaceTextureUncroppedHeight != scaledTextureHeight) {
            mSurfaceTextureUncroppedWidth = scaledTextureWidth;
            mSurfaceTextureUncroppedHeight = scaledTextureHeight;
            if (mSurfaceTextureSizeListener != null) {
                mSurfaceTextureSizeListener.onSurfaceTextureSizeChanged(
                        (int) mSurfaceTextureUncroppedWidth, (int) mSurfaceTextureUncroppedHeight);
            }
        }
        scaleX = scaledTextureWidth / width;
        scaleY = scaledTextureHeight / height;
        if(mActivity.getGC().getCurrentMode()  == GC.MODE_POLAROID && mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
        	mMatrix.setScale(scaleY,scaleX , (float) width / 2, (float) height / 2);
        }else{
        	mMatrix.setScale(scaleX, scaleY, (float) width / 2, (float) height / 2);
        }
        mTextureView.setTransform(mMatrix);
        RectF previewRect = null;
        // Calculate the new preview rectangle.
    	previewRect=new RectF(0, mTopDip, width, height+mTopDip);//Update taf area
        mMatrix.mapRect(previewRect);
        mController.onPreviewRectChanged(CameraUtil.rectFToRect(previewRect));
    }

    protected Object getSurfaceTextureLock() {
        return mSurfaceTextureLock;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        synchronized (mSurfaceTextureLock) {
        	CameraActivity.TraceQCTLog("SurfaceTextureCallback");
            Log.v(TAG, "SurfaceTexture ready.");
            mSurfaceTexture = surface;
            mController.onPreviewUIReady();
            // Workaround for b/11168275, see b/10981460 for more details
            if (mPreviewWidth != 0 && mPreviewHeight != 0) {
                // Re-apply transform matrix for new surface texture
                setTransformMatrix(mPreviewWidth, mPreviewHeight);
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        synchronized (mSurfaceTextureLock) {
            mSurfaceTexture = null;
            mController.onPreviewUIDestroyed();
            Log.w(TAG, "SurfaceTexture destroyed");
            return true;
        }
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Make sure preview cover is hidden if preview data is available.
        if (mPreviewCover.getVisibility() != View.GONE) {
            mPreviewCover.setVisibility(View.GONE);
        }
    }

    public View getRootView() {
        return mRootView;
    }

    private void initIndicators() {
        //mOnScreenIndicators = new OnScreenIndicators(mActivity,
        //        mRootView.findViewById(R.id.on_screen_indicators));
    }

    public void onCameraOpened(PreferenceGroup prefGroup, ComboPreferences prefs,
            Camera.Parameters params, OnPreferenceChangedListener listener) {
        if (mPieRenderer == null) {
            mPieRenderer = new PieRenderer(mActivity);
            mPieRenderer.setPieListener(this);
            mRenderOverlay.addRenderer(mPieRenderer);
        }
        /*

        if (mMenu == null) {
            mMenu = new PhotoMenu(mActivity, this, mPieRenderer);
            mMenu.setListener(listener);
        }
        mMenu.initialize(prefGroup);
        mMenuInitialized = true;
        */

        if (mZoomRenderer == null) {
            mZoomRenderer = new ZoomRenderer(mActivity);
            mRenderOverlay.addRenderer(mZoomRenderer);
        }

        if (mGestures == null) {
            // this will handle gesture disambiguation and dispatching
            mGestures = new PreviewGestures(mActivity, this, mZoomRenderer, mPieRenderer);
            mRenderOverlay.setGestures(mGestures);
        }
        mGestures.setZoomEnabled(params.isZoomSupported());
        mGestures.setRenderOverlay(mRenderOverlay);
        mRenderOverlay.requestLayout();

        initializeZoom(params);
        updateOnScreenIndicators(params, prefGroup, prefs);
    }

    public void animateCapture(final byte[] jpegData, int orientation, boolean mirror) {
        // Decode jpeg byte array and then animate the jpeg
        DecodeTask task = new DecodeTask(jpegData, orientation, mirror);
        task.execute();
    }

    private void openMenu() {
        if (mPieRenderer != null) {
            // If autofocus is not finished, cancel autofocus so that the
            // subsequent touch can be handled by PreviewGestures
            if (mController.getCameraState() == PhotoController.FOCUSING) {
                    mController.cancelAutoFocus();
            }
            mPieRenderer.showInCenter();
        }
    }

    public void initializeControlByIntent() {
        mPreviewThumb = (ImageView) mRootView.findViewById(R.id.preview_thumb);
        /*mPreviewThumb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.gotoGallery();
            }
        });
        mMenuButton = mRootView.findViewById(R.id.menu);
        mMenuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenu();
            }
        });*/
        if (mController.isImageCaptureIntent()) {
            hideSwitcher();
            //ViewGroup cameraControls = (ViewGroup) mRootView.findViewById(R.id.camera_controls);
            //mActivity.getLayoutInflater().inflate(R.layout.review_module_control, cameraControls);

            mReviewDoneButton = mRootView.findViewById(R.id.btn_done);
            mReviewCancelButton = mRootView.findViewById(R.id.btn_cancel);
            mReviewRetakeButton = mRootView.findViewById(R.id.btn_retake);
            mReviewImage = (RotatePhoto) mRootView.findViewById(R.id.review_image);
            //[BUGFIX]-Add by TCTNB,zhijie.gui, 2014-08-20,PR753458 Begin
            //mReviewCancelButton.setVisibility(View.VISIBLE);
            //[BUGFIX]-Add by TCTNB,zhijie.gui, 2014-08-20,PR753458 End

            mReviewDoneButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mController.onCaptureDone();
                  //[BUGFIX]-Add by yongsheng.shan, 2015-01-20,PR900632 Begin
                    mBackButtonManager.show();
                  //[BUGFIX]-Add by yongsheng.shan, 2015-01-20,PR900632 End
                }
            });
            mReviewCancelButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mController.onCaptureCancelled();
                  //[BUGFIX]-Add by yongsheng.shan, 2015-01-20,PR900632 Begin
                    mBackButtonManager.show();
                  //[BUGFIX]-Add by yongsheng.shan, 2015-01-20,PR900632 End
                }
            });

            mReviewRetakeButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mController.onCaptureRetake();
                  //[BUGFIX]-Add by yongsheng.shan, 2015-01-20,PR900632 Begin
                    mBackButtonManager.show();
                  //[BUGFIX]-Add by yongsheng.shan, 2015-01-20,PR900632 End
                }
            });
        }
    }

    public void hideUI() {
        //mCameraControls.setVisibility(View.INVISIBLE);
        //mSwitcher.closePopup();
    }

    public void showUI() {
        //mCameraControls.setVisibility(View.VISIBLE);
    }

    public boolean arePreviewControlsVisible() {
        return false;//(mCameraControls.getVisibility() == View.VISIBLE);
    }

    public void hideSwitcher() {
        //mSwitcher.closePopup();
        //mSwitcher.setVisibility(View.INVISIBLE);
    }
    public void hideFocus() {
        mFocusAreaIndicator.setVisibility(View.GONE);
        mFaceView.setVisibility(View.GONE);
    }
    public void showFocus() {
        mFocusAreaIndicator.setVisibility(View.VISIBLE);
    }
    public void showSwitcher() {
        //mSwitcher.setVisibility(View.VISIBLE);
    }
    // called from onResume but only the first time
    public  void initializeFirstTime() {
        // Initialize shutter button.
        //mShutterButton.setImageResource(R.drawable.btn_new_shutter);
        //mShutterButton.setOnShutterButtonListener(mController);
        //mShutterButton.setVisibility(View.VISIBLE);
    }

    // called from onResume every other time
    public void initializeSecondTime(Camera.Parameters params) {
      //[BUGFIX]-Add by yongsheng.shan, 2015-01-26,PR900632 Begin
    	//If user use power-key change state from OnPause() to OnResume().
    	//Backbutton will be disappear.
        mBackButtonManager.show();
      //[BUGFIX]-Add by yongsheng.shan, 2015-01-26,PR900632 End
        initializeZoom(params);
        if (mController.isImageCaptureIntent()) {
            hidePostCaptureAlert();
        }
        if (!mActivity.getCameraViewManager().getCameraManualModeManager().isShowing() && !(GC.VIEW_STATE_SHUTTER_PRESS == mActivity.getViewState())) { // PR851347-lan.shan-001 Add
            mActivity.setViewState(GC.VIEW_STATE_NORMAL);
        }
        if (mMenu != null) {
            mMenu.reloadPreferences();
        }
    }

    public void showLocationDialog() {
        mLocationDialog = new AlertDialog.Builder(mActivity)
                .setTitle(R.string.remember_location_title)
                .setMessage(R.string.remember_location_prompt)
                .setPositiveButton(R.string.remember_location_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                mController.enableRecordingLocation(true);
                                mLocationDialog = null;
                            }
                        })
                .setNegativeButton(R.string.remember_location_no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.cancel();
                            }
                        })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mController.enableRecordingLocation(false);
                        mLocationDialog = null;
                    }
                })
                .show();
    }

    public void initializeZoom(Camera.Parameters params) {
        if ((params == null) || !params.isZoomSupported()
                || (mZoomRenderer == null)) return;
        mZoomMax = params.getMaxZoom();
        mZoomRatios = params.getZoomRatios();
        // Currently we use immediate zoom for fast zooming to get better UX and
        // there is no plan to take advantage of the smooth zoom.
        if (mZoomRenderer != null) {
            mZoomRenderer.setZoomMax(mZoomMax);
            mZoomRenderer.setZoom(params.getZoom());
            mZoomRenderer.setZoomValue(mZoomRatios.get(params.getZoom()));
            mZoomRenderer.setOnZoomChangeListener(new ZoomChangeListener());
        }
    }

    @Override
    public void showGpsOnScreenIndicator(boolean hasSignal) { }

    @Override
    public void hideGpsOnScreenIndicator() { }

    public void overrideSettings(final String ... keyvalues) {
        if (mMenu == null) return;
        mMenu.overrideSettings(keyvalues);
    }

    public void updateOnScreenIndicators(Camera.Parameters params,
            PreferenceGroup group, ComboPreferences prefs) {
        if (params == null || group == null) return;
        /*mOnScreenIndicators.updateSceneOnScreenIndicator(params.getSceneMode());
        mOnScreenIndicators.updateExposureOnScreenIndicator(params,
                CameraSettings.readExposure(prefs));
        mOnScreenIndicators.updateFlashOnScreenIndicator(params.getFlashMode());
        int wbIndex = 2;
        ListPreference pref = group.findPreference(CameraSettings.KEY_WHITE_BALANCE);
        if (pref != null) {
            wbIndex = pref.getCurrentIndex();
        }
        mOnScreenIndicators.updateWBIndicator(wbIndex);
        boolean location = RecordLocationPreference.get(
                prefs, mActivity.getContentResolver());
        mOnScreenIndicators.updateLocationIndicator(location);
        */
    }

    public void setCameraState(int state) {
    }

    public void animateFlash() {
        mAnimationManager.startFlashAnimation(mFlashOverlay);
    }

    public void enableGestures(boolean enable) {
        if (mGestures != null) {
            mGestures.setEnabled(enable);
        }
    }

    // forward from preview gestures to controller
    @Override
    public void onSingleTapUp(View view, int x, int y) {
        if ( y > mPreviewHeight + mTopDip) return;
        mController.onSingleTapUp(view, x, y);
    }

    public boolean canSingleTapUp(View view,int x,int y) {
        if ( y > mPreviewHeight + mTopDip) return false;
        return true;
    }

	// huannglin 20150210 for PR927635
	public boolean cancelCountDownwithinFaceBeauty() {
		if (mCountDownView != null && isCountingDown()) {
			cancelCountDown();
			return true;
		}
		return false;
	}

    public boolean onBackPressed() {
        if (mPieRenderer != null && mPieRenderer.showsItems()) {
            mPieRenderer.hide();
            return true;
        }
        if (mCountDownView != null && isCountingDown()) {
            cancelCountDown();
            return true;
        }
        // In image capture mode, back button should:
        // 1) if there is any popup, dismiss them, 2) otherwise, get out of
        // image capture
        if (mController.isImageCaptureIntent()) {
            mController.onCaptureCancelled();
            return true;
        } else if (!mController.isCameraIdle()) {
            // ignore backs while we're taking a picture
            return true;
        } else {
            return false;
        }
    }

    public void onPreviewFocusChanged(boolean previewFocused) {
        if (previewFocused) {
            showUI();
        } else {
            hideUI();
        }
        if (mFaceView != null) {
            mFaceView.setBlockDraw(!previewFocused);
        }
        if (mGestures != null) {
            mGestures.setEnabled(previewFocused);
        }
        if (mRenderOverlay != null) {
            // this can not happen in capture mode
            mRenderOverlay.setVisibility(previewFocused ? View.VISIBLE : View.GONE);
        }
        if (mPieRenderer != null) {
            mPieRenderer.setBlockFocus(!previewFocused);
        }
        setShowMenu(previewFocused);
        if (!previewFocused && mCountDownView != null) mCountDownView.cancelCountDown();
    }

    public void showPopup(AbstractSettingPopup popup) {
        /*hideUI();

        if (mPopup == null) {
            mPopup = new PopupWindow(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            mPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mPopup.setOutsideTouchable(true);
            mPopup.setFocusable(true);
            mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    mPopup = null;
                    mMenu.popupDismissed();
                    showUI();

                    // Switch back into fullscreen/lights-out mode after popup
                    // is dimissed.
                    mActivity.setSystemBarsVisibility(false);
                }
            });
        }
        popup.setVisibility(View.VISIBLE);
        mPopup.setContentView(popup);
        mPopup.showAtLocation(mRootView, Gravity.CENTER, 0, 0);
        */
    }

    public void dismissPopup() {
        if (mPopup != null && mPopup.isShowing()) {
            mPopup.dismiss();
        }
    }

    public void onShowSwitcherPopup() {
        if (mPieRenderer != null && mPieRenderer.showsItems()) {
            mPieRenderer.hide();
        }
    }

    private void setShowMenu(boolean show) {
        if (mOnScreenIndicators != null) {
            mOnScreenIndicators.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (mMenuButton != null) {
            mMenuButton.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public boolean collapseCameraControls() {
        // TODO: Mode switcher should behave like a popup and should hide itself when there
        // is a touch outside of it.
        //mSwitcher.closePopup();
        // Remove all the popups/dialog boxes
        boolean ret = false;
        if (mPopup != null) {
            dismissPopup();
            ret = true;
        }
        onShowSwitcherPopup();
        return ret;
    }

    protected void showCapturedImageForReview(byte[] jpegData, int orientation, boolean mirror) {
        mDecodeTaskForReview = new DecodeImageForReview(jpegData, orientation, mirror);
        mDecodeTaskForReview.execute();
        //mOnScreenIndicators.setVisibility(View.GONE);
        //mMenuButton.setVisibility(View.GONE);
        mFocusAreaIndicator.setVisibility(View.GONE);
        CameraUtil.fadeIn(mReviewDoneButton);
        //mShutterButton.setVisibility(View.INVISIBLE);
        CameraUtil.fadeIn(mReviewCancelButton);
        CameraUtil.fadeIn(mReviewRetakeButton);
        pauseFaceDetection();
    }

    protected void hidePostCaptureAlert() {
        if (mDecodeTaskForReview != null) {
            mDecodeTaskForReview.cancel(true);
        }
        mReviewImage.setVisibility(View.GONE);
        mFocusAreaIndicator.setVisibility(View.VISIBLE);
        //mOnScreenIndicators.setVisibility(View.VISIBLE);
        //mMenuButton.setVisibility(View.VISIBLE);
        CameraUtil.fadeOut(mReviewDoneButton);
        CameraUtil.fadeOut(mReviewCancelButton);
        //mShutterButton.setVisibility(View.VISIBLE);
        CameraUtil.fadeOut(mReviewRetakeButton);
        resumeFaceDetection();
    }

    public void setDisplayOrientation(int orientation) {
        if (mFaceView != null) {
            mFaceView.setDisplayOrientation(orientation);
        }
    }

    // shutter button handling

    public boolean isShutterPressed() {
        return false;//mShutterButton.isPressed();
    }

    /**
     * Enables or disables the shutter button.
     */
    public void enableShutter(boolean enabled) {
        /*if (mShutterButton != null) {
            mShutterButton.setEnabled(enabled);
        }*/
    }

    public void pressShutterButton() {
        /*if (mShutterButton.isInTouchMode()) {
            mShutterButton.requestFocusFromTouch();
        } else {
            mShutterButton.requestFocus();
        }
        mShutterButton.setPressed(true);
        */
    }

    private class ZoomChangeListener implements ZoomRenderer.OnZoomChangedListener {
        @Override
        public void onZoomValueChanged(int index) {
            int newZoom = mController.onZoomChanged(index);
            if (mZoomRenderer != null) {
                mZoomRenderer.setZoomValue(mZoomRatios.get(newZoom));
            }
        }

        @Override
        public void onZoomStart() {
            if (mPieRenderer != null) {
                mPieRenderer.hide();
                mPieRenderer.setBlockFocus(true);
            }
        }

        @Override
        public void onZoomEnd() {
            if (mPieRenderer != null) {
                mPieRenderer.setBlockFocus(false);
            }
        }
    }

    @Override
    public void onPieOpened(int centerX, int centerY) {
        setSwipingEnabled(false);
        if (mFaceView != null) {
            mFaceView.setBlockDraw(true);
        }
        // Close module selection menu when pie menu is opened.
        //mSwitcher.closePopup();
    }

    @Override
    public void onPieClosed() {
        setSwipingEnabled(true);
        if (mFaceView != null) {
            mFaceView.setBlockDraw(false);
        }
    }

    public void setSwipingEnabled(boolean enable) {
        mActivity.setSwipingEnabled(enable);
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public TextureView getTextureView(){
    	return mTextureView;
    }
    // Countdown timer

    private void initializeCountDown() {
        // modify by minghui.hua for PR939691
//        mActivity.getLayoutInflater().inflate(R.layout.count_down_to_capture,
//                (ViewGroup) mRootView, true);
        mCountDownView = mActivity.getCameraViewManager().getCountDownView();
        mCountDownView.setCountDownFinishedListener((OnCountDownFinishedListener) mController);
    }

    public boolean isCountingDown() {
        return mCountDownView != null && mCountDownView.isCountingDown();
    }

    public void cancelCountDown() {
        if (mCountDownView == null) return;
        mCountDownView.cancelCountDown();
    }

    public void startCountDown(int sec, boolean playSound) {
        if (mCountDownView == null) initializeCountDown();
        mCountDownView.startCountDown(sec, playSound);
    }

    //[BUGFIX]-Added by jian.zhang1 for PR770765 2014.08.21 Begin
    public void rotateCountDown(int orientation){
        if(isCountingDown()){
            mCountDownView.setRotation(-orientation);
        }
    }
    //[BUGFIX]-Added by jian.zhang1 for PR770765 2014.08.21 Begin

    public void showPreferencesToast() {
        if (mNotSelectableToast == null) {
            String str = mActivity.getResources().getString(R.string.not_selectable_in_scene_mode);
            mNotSelectableToast = Toast.makeText(mActivity, str, Toast.LENGTH_SHORT);
        }
        mNotSelectableToast.show();
    }

    public void showPreviewCover() {
        mPreviewCover.setVisibility(View.VISIBLE);
    }
    

    public void onPause() {
        cancelCountDown();

        // Clear UI.
        collapseCameraControls();
        if (mFaceView != null) mFaceView.clear();

        if (mLocationDialog != null && mLocationDialog.isShowing()) {
            mLocationDialog.dismiss();
        }
        mLocationDialog = null;
    }

    public void initDisplayChangeListener() {
        //((CameraRootView) mRootView).setDisplayChangeListener(this);
    }

    public void removeDisplayChangeListener() {
        //((CameraRootView) mRootView).removeDisplayChangeListener();
    }

    // focus UI implementation

    private FocusIndicator getFocusIndicator() {
        return (mFaceView != null && mFaceView.faceExists()) ? mFaceView : mFocusAreaIndicator;
    }

    @Override
    public boolean hasFaces() {
        return (mFaceView != null && mFaceView.faceExists());
    }

    public void clearFaces() {
        if (mFaceView != null) mFaceView.clear();
    }

    @Override
    public void clearFocus() {
        FocusIndicator indicator = getFocusIndicator();
        if (indicator != null) indicator.clear();
    }

    @Override
    public void setFocusPosition(int x, int y) {
    	if(mPieRenderer==null){
    		return;
    	}
        mPieRenderer.setFocus(x, y);
    }

    @Override
    public void onFocusStarted() {
        if(mActivity.getCameraViewManager().getCameraManualModeManager().isAutoFocus()){
            //Add by zhimin.yu for PR945824 begin
			int curMode = mActivity.getGC().getCurrentMode();
			if (curMode != GC.MODE_QRCODE && curMode != GC.MODE_POLAROID) {
				getFocusIndicator().showStart();
			}
            //Add by zhimin.yu for PR945824 end
        }
    }

    @Override
    public void onFocusSucceeded(boolean timeout) {
        getFocusIndicator().showSuccess(timeout);
    }

    @Override
    public void onFocusFailed(boolean timeout) {
        getFocusIndicator().showFail(timeout);
    }

    @Override
    public void pauseFaceDetection() {
        if (mFaceView != null) mFaceView.pause();
    }

    @Override
    public void resumeFaceDetection() {
        if (mFaceView != null) mFaceView.resume();
    }

    public void onStartFaceDetection(int orientation, boolean mirror) {
    	if(mActivity.getGC().getCurrentMode() == GC.MODE_POLAROID){
    		return ;
    	}
        mFaceView.clear();
        mFaceView.setVisibility(View.VISIBLE);
        mFaceView.setDisplayOrientation(orientation);
        mFaceView.setMirror(mirror);
        mFaceView.resume();
    }

    @Override
    public void onFaceDetection(Face[] faces, CameraManager.CameraProxy camera) {
    	if(mActivity.getGC().getCurrentMode() == GC.MODE_POLAROID){
    		return;
    	}
        mFaceView.setFaces(faces);
        SmileShotThread.updateFace(faces);
        ExpressionThread.updateFace(faces);
        ExpressionThread.updateRectF(mFaceView.getFaceRectF(faces));
    }

    @Override
    public void onDisplayChanged() {
        Log.d(TAG, "Device flip detected.");
        mCameraControls.checkLayoutFlip();
        mController.updateCameraOrientation();
    }

    public PolaroidView getPolaroidView(){
    	return mPolaroidView;
    }
    
    protected void showCapturedImageForPolaroid(byte[] jpegData, int orientation, boolean mirror) {
    	mDecodeTaskForPolaroid = new DecodeImageForPolaroid(jpegData, orientation, mirror);
    	mDecodeTaskForPolaroid.execute();
        //mOnScreenIndicators.setVisibility(View.GONE);
        //mMenuButton.setVisibility(View.GONE);
        mFocusAreaIndicator.setVisibility(View.GONE);
//        CameraUtil.fadeIn(mReviewDoneButton);
        //mShutterButton.setVisibility(View.INVISIBLE);
//        CameraUtil.fadeIn(mReviewCancelButton);
//        CameraUtil.fadeIn(mReviewRetakeButton);
//        pauseFaceDetection();
    }
    
    private class DecodeImageForPolaroid extends DecodeTask {
    	private int mDataOritation =0 ; 
    	private byte[] mData ; 
    	private boolean mirror ; 
        public DecodeImageForPolaroid(byte[] data, int orientation, boolean mirror) {
            super(data, orientation, mirror);
            this.mDataOritation = orientation;
            this.mData = data;
            this.mirror = mirror;
            
        }
        @Override
        protected Bitmap doInBackground(Void... params) {
        	// TODO Auto-generated method stub
        	Bitmap bit =BitmapFactory.decodeByteArray(mData, 0, mData.length);
        	Bitmap bmp= null;
        	int minLen = Math.min(bit.getHeight(),bit.getWidth());
        	Matrix m = new Matrix();
                if (mirror) {
                    // Flip horizontally
                    m.setScale(-1f, 1f);
                }
                if(mDataOritation != 0 && mDataOritation != 180){
                	m.postRotate(mDataOritation+180);
                }else{
                	m.postRotate(mDataOritation);
                }
    		if(bit.getWidth() <= bit.getHeight()){
    			bmp = Bitmap.createBitmap(bit , 0, Math.abs(bit.getHeight()-bit.getWidth())/2, minLen, minLen, m , false);
    		}else{
    			bmp = Bitmap.createBitmap(bit , Math.abs(bit.getHeight()-bit.getWidth())/2 , 0, minLen, minLen, m , false);
    		}
    		if(bit != null && !bit.isRecycled()){
    			bit.recycle();
    		}
        	return bmp;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            	 mPolaroidView.setImageBitmap(bitmap);

//				PolaroidView.transBitmap = bitmap;
//				Intent intent = new Intent(mActivity,PolarActivity.class);
//				mActivity.startActivity(intent);
				
				
                 mPolaroidView.setVisibility(View.VISIBLE);
                 mDecodeTaskForPolaroid = null;
           
        }
    }
    public static final int MARKID = 1000;
    public void initPolaroidView(boolean on ){
    	mBackButtonManager.refresh();
    	mPolaroidView.setVisibility(View.GONE);
    	if(on){
    		//disabel swipe
    		setSwipingEnabled(false);
    		mActivity.setIsLockDrawer(false);
    	//disable focus
    		hideFocus();
    	if(mPolaroidView.getPolarCount() == PolaroidView.POLAR_ONE){
    		if(mTextureView.getScaleX() == 0.5f){
    			resetTextureView();
    		}
    		mPolaroidView.setVisibility(View.VISIBLE);
    	}else if(mPolaroidView.getPolarCount() == PolaroidView.POLAR_TWO){
    		if(mTextureView.getScaleX() == 0.5f){
    			resetTextureView();
    		}
    		mPolaroidView.setVisibility(View.VISIBLE);
    		mPolaroidView.setPicCount(-2);
    		mPolaroidView.setImageBitmap(getInitBitmap());
    	}else if(mPolaroidView.getPolarCount() == PolaroidView.POLAR_FOUR){
    		if(mTextureView.getScaleX() == 1.0f || mTextureView.getTranslationX() != 0 || mTextureView.getTranslationY() != 0){
    			mPolaroidView.changeTextureViewSize(-1);	
    		}
    		mPolaroidView.setVisibility(View.VISIBLE);
    		mPolaroidView.setPicCount(-2);
			mPolaroidView.setImageBitmap(getInitBitmap());
    	}
    	}else{
    		if(mTextureView.getScaleX() == 0.5f){
    			resetTextureView();
    		}
    		showFocus();
    		setSwipingEnabled(true);
    		if(null != mFaceView){
    			mFaceView.setVisibility(View.VISIBLE);
    		}
    		mPolaroidView.setVisibility(MARKID);
    		mPolaroidView.setImageBitmap(null);
    	}
    	
    }
    private void resetTextureView(){
    	mTextureView.animate().scaleX(1.0f).setDuration(100).start();
		mTextureView.animate().scaleY(1.0f).setDuration(100).start();
		mTextureView.animate().translationX(0).setDuration(100).start();
		mTextureView.animate().translationY(0).setDuration(100).start();
    }
    private  Bitmap getInitBitmap(){
    	Bitmap output=Bitmap.createBitmap(mActivity.getResources().getDisplayMetrics().widthPixels, mActivity.getResources().getDisplayMetrics().widthPixels, Config.ARGB_8888);
    	Canvas canvas = new Canvas(output);
    	
    	if(mPolaroidView.getPolarCount() == 4){
    	 Path path = new Path();
    	 float mScreenWid = mActivity.getResources().getDisplayMetrics().widthPixels;
    	 path.moveTo(0, 0);
    	 path.lineTo(0, mScreenWid/2);
    	 path.lineTo(mScreenWid/2, mScreenWid/2);
    	 path.lineTo(mScreenWid/2, 0);
    	 canvas.clipPath(path,Op.DIFFERENCE);
    	 canvas.drawColor(Color.parseColor("#1A1A1A"));
    	 float[][] temPoint = new float[3][2];
    	 temPoint[0][0] = mScreenWid/2 ; temPoint[0][1] = 0;
    	 temPoint[1][0] = 0 ; temPoint[1][1] = mScreenWid/2;
    	 temPoint[2][0] = mScreenWid/2 ; temPoint[2][1] = mScreenWid/2;
    	 mPolaroidView.drawRectCircleText(canvas, temPoint[0], "2" , false);
    	 mPolaroidView.drawRectCircleText(canvas, temPoint[1], "3" , false);
    	 mPolaroidView.drawRectCircleText(canvas, temPoint[2], "4" , false);
    	 Paint paint = new Paint();
    	 paint.setAntiAlias(true);
    	 paint.setColor(Color.BLACK);
    	 paint.setStrokeWidth(3);
    	 canvas.drawLine(0, mActivity.getResources().getDisplayMetrics().widthPixels/2, mActivity.getResources().getDisplayMetrics().widthPixels, mActivity.getResources().getDisplayMetrics().widthPixels/2, paint);
    	 canvas.drawLine(mActivity.getResources().getDisplayMetrics().widthPixels/2, 0, mActivity.getResources().getDisplayMetrics().widthPixels/2, mActivity.getResources().getDisplayMetrics().widthPixels, paint);
    	}else if(mPolaroidView.getPolarCount() == 2){
    		canvas.drawColor(Color.parseColor("#1A1A1A"));
    	}
    	return output;
    	}
	
	public boolean countForPolaroid(){
		return (mPolaroidView != null && mPolaroidView.counterConout());
	}
	
	public void setRotatePolaroid(int rotation){
		if(mActivity.getGC().getCurrentMode() == GC.MODE_POLAROID && null != mPolaroidView && mPolaroidView.getPolarCount() == 4){
			mPolaroidView.rotateCircleFour();
		}
		
	}
}

