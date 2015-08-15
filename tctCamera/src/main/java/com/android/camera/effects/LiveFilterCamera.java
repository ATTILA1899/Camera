/**********************************************************************************************************/
/*     Modifications on Features list / Changes Request / Problems Report                                 */
/**********************************************************************************************************/
/*    date   |        author        |         Key          |     comment                                  */
/************|**********************|**********************|***********************************************/
/* 08/15/2014|       jian.zhang1    |   PR759360,759369    |Add OrientationEventListener for filter labels*/
/* 09/02/2014|    zhijie.gui        |   PR779646           |Modify the pos of label                       */
/************|**********************|**********************|***********************************************/
package com.android.camera.effects;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.TextView;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout.LayoutParams;

import android.content.Intent;

import com.android.camera.CameraActivity;
import com.tct.camera.R;
import com.android.camera.util.CameraUtil;
import com.android.camera.effects.EffectsUtils.ActivityStateObserver;
import com.android.camera.effects.EffectsUtils.MainMessage;
import com.android.camera.effects.EffectsUtils.OrientationObserver;
import com.android.camera.effects.EffectsUtils.TaskQueueThread.TaskObserver;
import com.android.camera.effects.camera.LiveFilterCameraConfigurator;
import com.android.camera.effects.camera.LiveFilterImageSaver;
import com.android.camera.effects.camera.LiveFilterSmartShutterButton;
import com.android.camera.effects.camera.LiveFilterStillCaptureController;
import com.android.camera.effects.camera.LiveFilterThumbnailContorller;
import com.android.camera.effects.gl.GLEffectsRender;
import com.android.camera.effects.gl.GLEffectsRender.EffectsRenderObserver;
import com.android.camera.effects.gl.GLPreviewRootSurfaceView;
import com.android.camera.ui.FocusIndicatorRotateLayout;
import com.android.camera.ui.RotateImageView;

public final class LiveFilterCamera extends Activity implements EffectsRenderObserver {
    private static final String TAG = "LiveFilterCamera";
    private static final int REQ_CODE_BACK_TO_NORMAL_CAMERA = 1453;

    private View mControlLayout, mThumbLayout, mLabelLayout, mThumbnailLoadingView;

    private GLEffectsRender mRender;

    private RotateImageView mCameraSwitchBtn, mOverviewBtn, mThumbnail;

    private OrientationObserver mOrientationObserver;

    private LiveFilterImageSaver mImageSaver;

    private ActivityStateObserver mActivityStateObserver;

    private LiveFilterSmartShutterButton mShutterBtn;

    private LiveFilterThumbnailContorller mThumbnailContorller;

    /**A camera configure to keep a configured camera.*/
    private LiveFilterCameraConfigurator mCameraCfg;

    //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 Begin
    private int lastOrientation = 0;

    private int mWidth;

    private MyOrientationEventListener mOrientationEventListener;

    private TextView liveeffect_sepia;
    private TextView liveeffect_ansel;
    private TextView liveeffect_retro;
    private TextView liveeffect_fisheye;
    private TextView liveeffect_none;
    private TextView liveeffect_negative;
    private TextView liveeffect_relief;
    private TextView liveeffect_charcoal;
    private TextView liveeffect_edge;
    private ArrayList<TextView> mLabels = new ArrayList<TextView>();
    //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 End

    /** A still capture controller to manager the picture taken process.*/
    private LiveFilterStillCaptureController mCaptureController;
    private CameraActivity mCameraContext;
    private final Handler mEventHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MainMessage.MSG_RENDER_VIEWMODE_CHANGED:
                    doOnViewModeChanged(msg.arg1);
                    break;
                case MainMessage.MSG_REFRESH_THUMBNAIL:
                    //Log.e("Liu","WXT MSG_REFRESH_THUMBNAIL");
                    mThumbnailContorller.refreshThumbnail();
                    break;
                case MainMessage.MSG_THUMBNAIL_LOADING:
                    //Log.e("Liu","WXT MSG_THUMBNAIL_LOADING mThumbnailLoadingView = "+mThumbnailLoadingView);
                    if (mThumbnailLoadingView != null) {
                        mThumbnailLoadingView.setVisibility(View.VISIBLE);
                    }
                    break;
                case MainMessage.MSG_THUMBNAIL_DONE:
                    //Log.e("Liu","WXT MSG_THUMBNAIL_DONE");
                    if (mThumbnailLoadingView != null) {
                        mThumbnailLoadingView.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
            case R.id.rimv_switch_camera:
                mCameraCfg.switchCamera();
                break;
            case R.id.rimgv_preivew_effects:
                if (!mCaptureController.onOverViewButtonClicked()) break;
                mRender.switchToOverview();
                break;
            case R.id.rimgv_thumb:
                mThumbnailContorller.onThumbnailClicked(LiveFilterCamera.this);
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_filter_camera);

        // Get back instances form instance holder
        LiveFilterInstanceHolder instanceHolder = LiveFilterInstanceHolder.getIntance();
        mCameraContext = (CameraActivity)instanceHolder.getContext();
        mRender = instanceHolder.getEffectsRender();
        mCameraCfg = instanceHolder.getCameraConfigurator();
        mImageSaver = instanceHolder.getImageSaver();
        mCaptureController = instanceHolder.getStillCaptureController();
        mOrientationObserver = instanceHolder.getOrientationObserver();
        mThumbnailContorller = instanceHolder.getThumbnailContorller(this);
        mImageSaver.setThumbnailHandler(mThumbnailContorller);
        mImageSaver.setSaveTaskObserver(new TaskObserver() {
            @Override
            public void onTask(int n) {
                mEventHandler.sendEmptyMessage(MainMessage.MSG_THUMBNAIL_LOADING);
            }

            @Override
            public void onIdle() {
                mEventHandler.sendEmptyMessage(MainMessage.MSG_THUMBNAIL_DONE);
            }
        });

        mActivityStateObserver = instanceHolder.getActivityStateObserver();

        // Setup Observer
        mOrientationObserver.removeAll();
        mActivityStateObserver.removeAll();
        mActivityStateObserver.addFollowers(mRender, mOrientationObserver, mCaptureController);
        mActivityStateObserver.addFollowers(mThumbnailContorller);

        // Setup component instances
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);

        mCameraCfg.prepareLooper();
        mCameraCfg.setScreenSize(screenSize);
        mCameraCfg.setCameraSwitchObserver(mCaptureController);
        mCameraCfg.getPhotoDataProducer().setStillImageDataHander(mImageSaver);

        mCaptureController.setScreenSize(screenSize);
        mCaptureController.set3AControl(mCameraCfg.get3AControl());

        mRender.setObserver(this);
        mRender.setTapListener(mCaptureController.getRenderViewTapListener());
        mRender.setPreviewDataProducer(mCameraCfg.getPreviewDataProducer());

        // Open camera session
        mCameraCfg.openCameraSession();

        // Find and setup views.
        setupViews();
    }

    @SuppressLint("NewApi") private void setupViews() {
        mCaptureController.setSnapshotBorder(findViewById(R.id.v_snapshot_border));
        mShutterBtn = (LiveFilterSmartShutterButton)findViewById(R.id.btn_shutter);
        mShutterBtn.setShutterButtonListener(mCaptureController.getShutterButtonListener());
        mCaptureController.setFocusWindow((FocusWindowView)findViewById(R.id.focus_window));
        mCameraSwitchBtn = (RotateImageView)findViewById(R.id.rimv_switch_camera);
        mCameraSwitchBtn.setOnClickListener(mOnClickListener);
        mControlLayout = findViewById(R.id.ctl_preview_effects);
        mOverviewBtn = (RotateImageView)findViewById(R.id.rimgv_preivew_effects);
        mThumbLayout = findViewById(R.id.lay_thumb);
        mThumbnailLoadingView = findViewById(R.id.probar_processing);
        Log.i("Liu","mThumbnailLoadingView = "+mThumbnailLoadingView);
        mThumbnail = (RotateImageView)findViewById(R.id.rimgv_thumb);
        Log.i("Liu","mThumbnail = "+mThumbnail);
        mThumbnail.setOnClickListener(mOnClickListener);
        mOrientationObserver.addFollowers(mThumbnail);
        mThumbnailContorller.setThumbnailImage(mThumbnail, mEventHandler);
        mLabelLayout = findViewById(R.id.ly_labels);
        mOverviewBtn.setOnClickListener(mOnClickListener);
        mOrientationObserver.addFollowers(mShutterBtn, mOverviewBtn, mCameraSwitchBtn);

        //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 Begin
        WindowManager wm = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(outMetrics);
        mWidth = outMetrics.widthPixels;
        liveeffect_sepia = (TextView) findViewById(R.id.liveeffect_sepia);
        liveeffect_ansel = (TextView) findViewById(R.id.liveeffect_ansel);
        liveeffect_retro = (TextView) findViewById(R.id.liveeffect_retro);
        liveeffect_fisheye = (TextView) findViewById(R.id.liveeffect_fisheye);
        liveeffect_none = (TextView) findViewById(R.id.liveeffect_none);
        liveeffect_negative = (TextView) findViewById(R.id.liveeffect_negative);
        liveeffect_relief = (TextView) findViewById(R.id.liveeffect_relief);
        liveeffect_charcoal = (TextView) findViewById(R.id.liveeffect_charcoal);
        liveeffect_edge = (TextView) findViewById(R.id.liveeffect_edge);
        addLabels(liveeffect_sepia,liveeffect_ansel,liveeffect_retro,
                liveeffect_fisheye,liveeffect_none,liveeffect_negative,
                liveeffect_relief,liveeffect_charcoal,liveeffect_edge);
        mOrientationEventListener = new MyOrientationEventListener(this);
        mCaptureController.setOrientationEventListener(mOrientationEventListener);
        //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 End
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("liveFilter WXT1","onResume");		
        mCameraCfg.checkCameraSession();
        mActivityStateObserver.onResume();
    }

    @Override
    protected void onPause() {
        mActivityStateObserver.onPause();
        mCameraCfg.closeCameraSession();
        Log.i("liveFilter WXT1","onPause");
        //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 Begin
        mOrientationEventListener.disable();
        //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 End
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        onBackToNormalCamera();
    }

    @Override
    public void onViewModeChanged(int mode) {
        Message msg = new Message();
        msg.what = MainMessage.MSG_RENDER_VIEWMODE_CHANGED;
        msg.arg1 = mode;
        mEventHandler.sendMessage(msg);
    }

    private void doOnViewModeChanged(int mode) {
        mCaptureController.onViewModeChanged(mode);
        if (mode == GLEffectsRender.MODE_SINGVIEW) {
            mShutterBtn.setVisibility(View.VISIBLE);
            mThumbLayout.setVisibility(View.VISIBLE);
            mControlLayout.setVisibility(View.VISIBLE);
        } else {
            mLabelLayout.setVisibility(View.VISIBLE);
            //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 Begin
            mOrientationEventListener.enable();
            //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 End
        }
    }

    @Override
    public void onBackToNormalCamera() {
        Intent intent = new Intent();
        LiveFilterCamera.this.setResult(144, intent);    
        mLabelLayout.setVisibility(View.INVISIBLE);
        finish();
        overridePendingTransition(0, R.anim.livecamera_out);
    }

    public void gotoGallery() {
	if (mCameraContext.getLastBitmap() != null) {
          Log.i("liveFilter ","goto Gallery   thumnail bitmap:"+mCameraContext.getLastBitmap());		
	    Intent intent = new Intent(Intent.ACTION_VIEW).setClassName(
                        "com.android.gallery3d", "com.android.gallery3d.app.GalleryActivity");
	    intent.setType("*/*");
	    intent.setData(mCameraContext.getLastBitmapUri());
	    mCameraContext.startActivity(intent);
	}
	else
	{
          Log.i("liveFilter WXT1","goto Gallery  not thumnail bitmap");
	}

        onBackToNormalCamera();
    }

    @Override
    public void onStartChangeViewMode(int toMode) {
        if (toMode == GLEffectsRender.MODE_SINGVIEW) {
            //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 Begin
            mOrientationEventListener.disable();
            //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 End
            mLabelLayout.setVisibility(View.GONE);
        } else {
            mShutterBtn.setVisibility(View.GONE);
            mThumbLayout.setVisibility(View.GONE);
            mControlLayout.setVisibility(View.GONE);
        }
    }

    //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 Begin
    private class MyOrientationEventListener extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == ORIENTATION_UNKNOWN) {
                        return;
            }
            orientation = CameraUtil.roundOrientation(orientation, 0);
            if(lastOrientation != orientation){
                        rotateTextView(orientation);
                        lastOrientation = orientation;
            }
        }
    }

    private void rotateTextView(int orientation) {
        for (TextView label : mLabels) {
             rotateTextView(orientation,label);
        }
    }

    public void rotateTextView(int orientation, TextView textView) {
        switch (orientation) {
        case 0:
        case 180:
            textView.setRotation(-orientation);
            textView.setGravity(Gravity.CENTER);
            if (lastOrientation == 90 || lastOrientation == 270) {
                        textView.setTranslationX(0);
                        textView.setTranslationY(0);
            }
            break;
        case 90:
        case 270:
            textView.setRotation(-orientation);
            textView.setGravity(Gravity.CENTER);
            //[BUGFIX]-Modify by TCTNB,zhijie.gui, 2014-09-02,PR779646 Begin
            textView.setTranslationX(0);//mWidth / 6 - mWidth / 24);
            //[BUGFIX]-Modify by TCTNB,zhijie.gui, 2014-09-02,PR779646 End
            textView.setTranslationY(-mWidth / 12);
            break;
        }
    }

    private void addLabels(TextView... labels) {
        for (TextView label : labels) {
            mLabels.add(label);
        }
    }
    //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 End
}
