/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* ----------|----------------------|----------------------|----------------- */
/*    date   |        Author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 08/14/2014|      jian.zhang1     |      PR764272        |Show toast when B-*/
/*           |                      |                      |urstShot isn't so-*/
/*           |                      |                      |ppurted           */
/* ******************************************************************************/
package com.android.camera;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.android.camera.ui.CameraControls;
import com.android.camera.ui.CameraRootView;
import com.android.camera.ui.ModuleSwitcher;
import com.android.camera.ui.TctViewFinderView;
import com.android.camera.util.CameraUtil;
import com.tct.camera.R;

import com.android.camera.ui.ShutterButton;
import com.android.camera.GC;
import com.android.camera.manager.BackButtonManager;
import com.android.camera.manager.TctPanoramaProcessManager;
import com.android.camera.manager.TctParameter;
/**
 * The UI of {@link TctWideAnglePanoramaUI}.
 */
public class TctWideAnglePanoramaUI implements
        TextureView.SurfaceTextureListener,
        ShutterButton.OnShutterButtonListener,
        CameraRootView.MyDisplayListener,
        View.OnLayoutChangeListener {

    @SuppressWarnings("unused")
    private static final String TAG = "YUHUAN_TctWideAnglePanoramaUI";

    private CameraActivity mActivity;
    private WideAnglePanoramaModule mController;

    private ViewGroup mRootView;
    //private ModuleSwitcher mSwitcher;
    private FrameLayout mCaptureLayout;
    private View mReviewLayout;
    private ImageView mReview;
    private View mPreviewBorder;
    //private View mLeftIndicator;
    //private View mRightIndicator;
    //private View mCaptureIndicator;
    //private PanoProgressBar mCaptureProgressBar;
    private PanoProgressBar mSavingProgressBar;
    //private TextView mTooFastPrompt;
    private View mPreviewLayout;
    private ViewGroup mReviewControl;
    private TextureView mTextureView;
    private ShutterButton mShutterButton;
    private BackButtonManager  mBackButtonManager;
    //private CameraControls mCameraControls;

    private Matrix mProgressDirectionMatrix = new Matrix();
    private float[] mProgressAngle = new float[2];

    private DialogHelper mDialogHelper;

    // Color definitions.
    //private int mIndicatorColor;
    //private int mIndicatorColorFast;
    private int mReviewBackground;
    private SurfaceTexture mSurfaceTexture;
    private View mPreviewCover;

    /** Constructor. */
    public TctWideAnglePanoramaUI(
            CameraActivity activity,
            WideAnglePanoramaModule controller,
            ViewGroup root) {
        mActivity = activity;
        mController = controller;
        mRootView = root;
        mMosaicFrameProcessor=mController.getMosaicFrameProcessor();
        createContentView();
        //mSwitcher = (ModuleSwitcher) mRootView.findViewById(R.id.camera_switcher);
        //mSwitcher.setCurrentIndex(ModuleSwitcher.WIDE_ANGLE_PANO_MODULE_INDEX);
        //mSwitcher.setSwitchListener(mActivity);
    }

    public void onStartCapture() {
        hideSwitcher();
        //mShutterButton.setImageResource(R.drawable.btn_shutter_recording);
        //mCaptureIndicator.setVisibility(View.VISIBLE);
        showDirectionIndicators(PanoProgressBar.DIRECTION_NONE);
        tctInit();
    }

    public void showPreviewUI() {
        mCaptureLayout.setVisibility(View.VISIBLE);
        showUI();
    }

    public void onStopCapture() {

        //mCaptureIndicator.setVisibility(View.INVISIBLE);
//[BUGFIX]-MOD by TCTNB,hongyuan-wang, 2014-08-19,PR758742 Begin
        hideTooFastIndication();
	isPanoramaSuccess = false;
//[BUGFIX]-MOD by TCTNB, hongyuan-wang , 2014-08-19,PR758742 End
        //hideDirectionIndicators();
        tctReset();
        mActivity.getCameraViewManager().setViewState(GC.VIEW_STATE_SHUTTER_PRESS);
        mShutterButton = mActivity.getShutterManager().getShutterButton();
        mShutterButton.setVisibility(View.GONE);
    }

    public void hideSwitcher() {
        //mSwitcher.closePopup();
        //mSwitcher.setVisibility(View.INVISIBLE);
    }

    public void hideUI() {
        //hideSwitcher();
        //mCameraControls.setVisibility(View.INVISIBLE);
    }

    public void showUI() {
        //showSwitcher();
        //mCameraControls.setVisibility(View.VISIBLE);
    }

    public void onPreviewFocusChanged(boolean previewFocused) {
        if (previewFocused) {
            showUI();
        } else {
            hideUI();
        }
    }

    public boolean arePreviewControlsVisible() {
        return false;//(mCameraControls.getVisibility() == View.VISIBLE);
    }

    public void showSwitcher() {
        //mSwitcher.setVisibility(View.VISIBLE);
    }

    public void setCaptureProgressOnDirectionChangeListener(
            PanoProgressBar.OnDirectionChangeListener listener) {
        //mCaptureProgressBar.setOnDirectionChangeListener(listener);
    }

    public void resetCaptureProgress() {
      // mCaptureProgressBar.reset();
    }

    public void setMaxCaptureProgress(int max) {
        //mCaptureProgressBar.setMaxProgress(max);
    }

    public void showCaptureProgress() {
        //mCaptureProgressBar.setVisibility(View.VISIBLE);
    }

    public void updateCaptureProgress(
            float panningRateXInDegree, float panningRateYInDegree,
            float progressHorizontalAngle, float progressVerticalAngle,
            float maxPanningSpeed) {

        if ((Math.abs(panningRateXInDegree) > maxPanningSpeed)
                || (Math.abs(panningRateYInDegree) > maxPanningSpeed)) {
            showTooFastIndication();
        } else {
            hideTooFastIndication();
        }

        // progressHorizontalAngle and progressVerticalAngle are relative to the
        // camera. Convert them to UI direction.
        mProgressAngle[0] = progressHorizontalAngle;
        mProgressAngle[1] = progressVerticalAngle;
        mProgressDirectionMatrix.mapPoints(mProgressAngle);

        int angleInMajorDirection =
                (Math.abs(mProgressAngle[0]) > Math.abs(mProgressAngle[1]))
                        ? (int) mProgressAngle[0]
                        : (int) mProgressAngle[1];
        //mCaptureProgressBar.setProgress((angleInMajorDirection));
        updateTctProcess(angleInMajorDirection);
    }

    public void setProgressOrientation(int orientation) {
        mProgressDirectionMatrix.reset();
        mProgressDirectionMatrix.postRotate(orientation);
    }

    public void showDirectionIndicators(int direction) {
        switch (direction) {
            case PanoProgressBar.DIRECTION_NONE:
                //mLeftIndicator.setVisibility(View.VISIBLE);
                //mRightIndicator.setVisibility(View.VISIBLE);
                break;
            case PanoProgressBar.DIRECTION_LEFT:
                //mLeftIndicator.setVisibility(View.VISIBLE);
                //mRightIndicator.setVisibility(View.INVISIBLE);
                break;
            case PanoProgressBar.DIRECTION_RIGHT:
                //mLeftIndicator.setVisibility(View.INVISIBLE);
                //mRightIndicator.setVisibility(View.VISIBLE);
                break;
        }
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        mSurfaceTexture = surfaceTexture;
        mController.onPreviewUIReady();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        mController.onPreviewUIDestroyed();
        mSurfaceTexture = null;
        Log.d(TAG, "surfaceTexture is destroyed");
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        // Make sure preview cover is hidden if preview data is available.
        if (mPreviewCover.getVisibility() != View.GONE) {
            mPreviewCover.setVisibility(View.GONE);
        }
    }

    private void hideDirectionIndicators() {
        //mLeftIndicator.setVisibility(View.INVISIBLE);
        //mRightIndicator.setVisibility(View.INVISIBLE);
    }

    public Point getPreviewAreaSize() {
        return new Point(
                mTextureView.getWidth(), mTextureView.getHeight());
    }

    public void reset() {
        //mShutterButton.setImageResource(R.drawable.btn_new_shutter);
        mReviewLayout.setVisibility(View.GONE);
        mBackButtonManager.show();
        //mCaptureProgressBar.setVisibility(View.INVISIBLE);
        tctReset();
        //[BUGFIX]-ADD by TCTNB,hongyuan-wang, 2014-08-19,PR758742 Begin
	if (!isPanoramaSuccess){
           showGuideString(GUIDE_ERROR);
	}
	//[BUGFIX]-ADD by TCTNB,hongyuan-wang, 2014-08-19,PR758742 End
    }

    public void showFinalMosaic(Bitmap bitmap, int orientation) {
        if (bitmap != null && orientation != 0) {
            Matrix rotateMatrix = new Matrix();
            rotateMatrix.setRotate(orientation);
            Bitmap b = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                    rotateMatrix, false);
            if(bitmap!=b){
            	bitmap.recycle();
            }
            bitmap=b;
            
        }

        mReview.setImageBitmap(bitmap);
        mCaptureLayout.setVisibility(View.GONE);
        mReviewLayout.setVisibility(View.VISIBLE);
        mBackButtonManager.hide();
    }

    public void onConfigurationChanged(
            Configuration newConfig, boolean threadRunning) {
        Drawable lowResReview = null;
        if (threadRunning) lowResReview = mReview.getDrawable();

        // Change layout in response to configuration change
        LayoutInflater inflater = (LayoutInflater)
                mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mReviewControl.removeAllViews();
        inflater.inflate(R.layout.pano_review_control, mReviewControl, true);

        //mRootView.bringChildToFront(mCameraControls);
        setViews(mActivity.getResources());
        if (threadRunning) {
            mReview.setImageDrawable(lowResReview);
            mCaptureLayout.setVisibility(View.GONE);
            mReviewLayout.setVisibility(View.VISIBLE);
            mBackButtonManager.hide();
        }
    }

    public void resetSavingProgress() {
	//[BUGFIX]-ADD by TCTNB,hongyuan-wang, 2014-08-19,PR758742 Begin
	isPanoramaSuccess = true;
	//[BUGFIX]-ADD by TCTNB,hongyuan-wang, 2014-08-19,PR758742 End
        mSavingProgressBar.reset();
        mSavingProgressBar.setRightIncreasing(true);
    }

    public void updateSavingProgress(int progress) {
        mSavingProgressBar.setProgress(progress);
    }

    // PR879002-lan.shan-001 Add begin
    public boolean isSavingProgressRunning() {
        if (mSavingProgressBar == null) {
            return false;
        }

        return (mSavingProgressBar.getVisibility() == View.VISIBLE);
    }
    // PR879002-lan.shan-001 Add end

    @Override
    public void onShutterButtonFocus(boolean pressed) {
        // Do nothing.
    }

    @Override
    public void onShutterButtonClick() {
        mController.onShutterButtonClick();
    }

    @Override
    public void onShutterButtonLongClick() {
        //[BUGFIX]-MOD by jian.zhang1 for PR764272 2014.08.14 Begin
        String showing = mActivity
                .getString(R.string.normal_camera_continuous_not_supported);
        mActivity.showInfo(showing,false,false);
        //[BUGFIX]-MOD by jian.zhang1 for PR764272 2014.08.14 Begin
    }

    @Override
    public void onLayoutChange(
            View v, int l, int t, int r, int b,
            int oldl, int oldt, int oldr, int oldb) {
        mController.onPreviewUILayoutChange(l, t, r, b);
    }

    public void showAlertDialog(
            String title, String failedString,
            String OKString, Runnable runnable) {
        mDialogHelper.showAlertDialog(title, failedString, OKString, runnable);
    }

    public void showWaitingDialog(String title) {
        mDialogHelper.showWaitingDialog(title);
    }

    public void dismissAllDialogs() {
        mDialogHelper.dismissAll();
    }

    private void createContentView() {
    	Log.d(TAG,"###YUHUAN###createContentView ");
        LayoutInflater inflator = (LayoutInflater) mActivity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflator.inflate(R.layout.panorama_module, mRootView, true);

        Resources appRes = mActivity.getResources();
        //mIndicatorColor = appRes.getColor(R.color.pano_progress_indication);
        mReviewBackground = appRes.getColor(R.color.review_background);
       // mIndicatorColorFast = appRes.getColor(R.color.pano_progress_indication_fast);

        mPreviewCover = mRootView.findViewById(R.id.preview_cover);
        mPreviewLayout = mRootView.findViewById(R.id.pano_preview_layout);
        mReviewControl = (ViewGroup) mRootView.findViewById(R.id.pano_review_control);
        mReviewLayout = mRootView.findViewById(R.id.pano_review_layout);
        mReview = (ImageView) mRootView.findViewById(R.id.pano_reviewarea);
        mCaptureLayout = (FrameLayout) mRootView.findViewById(R.id.panorama_capture_layout);
        //mCaptureProgressBar = (PanoProgressBar) mRootView.findViewById(R.id.pano_pan_progress_bar);
        //mCaptureProgressBar.setBackgroundColor(appRes.getColor(R.color.pano_progress_empty));
        //mCaptureProgressBar.setDoneColor(appRes.getColor(R.color.pano_progress_done));
        //mCaptureProgressBar.setIndicatorColor(mIndicatorColor);
        //mCaptureProgressBar.setIndicatorWidth(20);

        mPreviewBorder = mCaptureLayout.findViewById(R.id.pano_preview_area_border);

        //mLeftIndicator = mRootView.findViewById(R.id.pano_pan_left_indicator);
        //mRightIndicator = mRootView.findViewById(R.id.pano_pan_right_indicator);
        //mLeftIndicator.setEnabled(false);
        //mRightIndicator.setEnabled(false);
        //mTooFastPrompt = (TextView) mRootView.findViewById(R.id.pano_capture_too_fast_textview);
        //mCaptureIndicator = mRootView.findViewById(R.id.pano_capture_indicator);

        mShutterButton = mActivity.getShutterManager().getShutterButton();
        mBackButtonManager = mActivity.getBackButtonManager();
        //mShutterButton = (ShutterButton) mRootView.findViewById(R.id.shutter_button);
        //mShutterButton.setImageResource(R.drawable.btn_new_shutter);
        mShutterButton.setOnShutterButtonListener(this);
        // Hide menu and indicators.
        //mRootView.findViewById(R.id.menu).setVisibility(View.GONE);
        //mRootView.findViewById(R.id.on_screen_indicators).setVisibility(View.GONE);
        mReview.setBackgroundColor(mReviewBackground);

        // TODO: set display change listener properly.
        //((CameraRootView) mRootView).setDisplayChangeListener(null);
        mTextureView = (TextureView) mRootView.findViewById(R.id.pano_preview_textureview);
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.addOnLayoutChangeListener(this);
        //mCameraControls = (CameraControls) mRootView.findViewById(R.id.camera_controls);

        mDialogHelper = new DialogHelper();
        setViews(appRes);
        showGuideString(GUIDE_SHUTTER);
    }

    private void setViews(Resources appRes) {
       // int weight = appRes.getInteger(R.integer.SRI_pano_layout_weight);

//        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mPreviewLayout.getLayoutParams();
//        lp.weight = weight;
//        mPreviewLayout.setLayoutParams(lp);
//
//        lp = (LinearLayout.LayoutParams) mReview.getLayoutParams();
//        lp.weight = weight;
//        mPreviewLayout.setLayoutParams(lp);

        mSavingProgressBar = (PanoProgressBar) mRootView.findViewById(R.id.pano_saving_progress_bar);
        mSavingProgressBar.setIndicatorWidth(0);
        mSavingProgressBar.setMaxProgress(100);
        mSavingProgressBar.setBackgroundColor(appRes.getColor(R.color.pano_progress_empty));
        mSavingProgressBar.setDoneColor(appRes.getColor(R.color.pano_progress_indication));

        View cancelButton = mRootView.findViewById(R.id.pano_review_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mController.cancelHighResStitching();
            }
        });

        tct_applyLayout();
    }

    private void showTooFastIndication() {
        //mTooFastPrompt.setVisibility(View.VISIBLE);
        // The PreviewArea also contains the border for "too fast" indication.
        mTctPanoramaProcessManager.showTipsIndication(TctPanoramaProcessManager.MSG_TOO_FAST);
        mPreviewBorder.setVisibility(View.VISIBLE);
        ///mCaptureProgressBar.setIndicatorColor(mIndicatorColorFast);
        //mLeftIndicator.setEnabled(true);
        ////mRightIndicator.setEnabled(true);
    }

    private void hideTooFastIndication() {
        //mTooFastPrompt.setVisibility(View.GONE);
        if(mTctPanoramaProcessManager != null){
           mTctPanoramaProcessManager.setIsFast(false);
           mTctPanoramaProcessManager.hideTipsIndication();
        }
        mPreviewBorder.setVisibility(View.INVISIBLE);
        //mCaptureProgressBar.setIndicatorColor(mIndicatorColor);
        //mLeftIndicator.setEnabled(false);
        ////mRightIndicator.setEnabled(false);
    }

    public void flipPreviewIfNeeded() {
        // Rotation needed to display image correctly clockwise
        int cameraOrientation = mController.getCameraOrientation();
        // Display rotated counter-clockwise
        int displayRotation = CameraUtil.getDisplayRotation(mActivity);
        // Rotation needed to display image correctly on current display
        int rotation = (cameraOrientation - displayRotation + 360) % 360;
        if (rotation >= 180) {
            mTextureView.setRotation(180);
        } else {
            mTextureView.setRotation(0);
        }
    }

    @Override
    public void onDisplayChanged() {
        //mCameraControls.checkLayoutFlip();
        flipPreviewIfNeeded();
    }

    public void initDisplayChangeListener() {
         //((CameraRootView) mRootView).setDisplayChangeListener(this);
    }

    public void removeDisplayChangeListener() {
        //((CameraRootView) mRootView).removeDisplayChangeListener();
    }

    public void showPreviewCover() {
        mPreviewCover.setVisibility(View.VISIBLE);
        tctReset();
    }

    private class DialogHelper {
        private ProgressDialog mProgressDialog;
        private AlertDialog mAlertDialog;

        DialogHelper() {
            mProgressDialog = null;
            mAlertDialog = null;
        }

        public void dismissAll() {
            if (mAlertDialog != null) {
                mAlertDialog.dismiss();
                mAlertDialog = null;
            }
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            mActivity.disableNavigationBar();
        }

        public void showAlertDialog(
                CharSequence title, CharSequence message,
                CharSequence buttonMessage, final Runnable buttonRunnable) {
            dismissAll();
            mAlertDialog = (new AlertDialog.Builder(mActivity))
                    .setTitle(title)
                    .setMessage(message)
                    .setNeutralButton(buttonMessage, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            buttonRunnable.run();
                        }
                    })
                    .show();
        }

        public void showWaitingDialog(CharSequence message) {
            dismissAll();
            mProgressDialog = ProgressDialog.show(mActivity, null, message, true, false);
        }
    }

    private static class FlipBitmapDrawable extends BitmapDrawable {

        public FlipBitmapDrawable(Resources res, Bitmap bitmap) {
            super(res, bitmap);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            int cx = bounds.centerX();
            int cy = bounds.centerY();
            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            canvas.rotate(180, cx, cy);
            super.draw(canvas);
            canvas.restore();
        }
    }

    /************************* Panorama UI **********************************************/
    private TctViewFinderView mViewFinderView;
    private int mDirection = TctPanoramaProcessManager.DIRECTION_UNKNOWN;
    private TctPanoramaProcessManager mTctPanoramaProcessManager;
    private boolean mNeedUpdateViewFinder;
    private float mMainAngle = 0.0f;
    private float mSecondAngle = 0.0f;
    private MosaicFrameProcessor mMosaicFrameProcessor;
    private static final int GENERATE_FINAL_MOSAIC_ERROR = 20;
    private static final int GENERATE_TEMP_SUCCESS = 21;
    private static final int GENERATE_TEMP_ERROR = 22;

    public static final int GUIDE_SHUTTER = 0;
    public static final int GUIDE_MOVE = 1;
    public static final int GUIDE_ERROR = 2;

    private MergeBitmapThread mMergeThread;
    private ArrayList<Bitmap> mMergedBitmaps;
    private boolean isFinish;
    private boolean mNeedWait;

 //[BUGFIX]-ADD by TCTNB, hongyuan-wang , 2014-08-19,PR758742 begin
    private boolean isPanoramaSuccess = true;
  //[BUGFIX]-ADD by TCTNB, hongyuan-wang , 2014-08-19,PR758742 End
    Handler mTctHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
               case GENERATE_TEMP_SUCCESS:
                    updateBitmap2();
                    break;
               case GENERATE_TEMP_ERROR:
                    /*TODO*/
                    //stopCapture(false);
                    //onBackgroundThreadFinished();
                    break;
            }
        }

    };

    public void showGuideString(int step) {
        int guideId = 0;
        switch (step) {
        case GUIDE_SHUTTER:
	//[BUGFIX]-MOD by TCTNB,hongyuan-wang, 2014-08-19,PR758742 Begin
          //  guideId = R.string.panorama_guide_shutter;
       //[BUGFIX]-MOD by TCTNB, hongyuan-wang , 2014-08-19,PR758742 End
            break;
        case GUIDE_MOVE:
            //[BUGFIX]-Mod By TCTNB(Hui.Shen), 12/30/2013, PR580217,Panorama mode pop up info is not right.
            //guideId = R.string.panorama_guide_choose_direction;
            //[BUGFIX]-MOD by TCTNB,hongyuan-wang, 2014-08-19,PR758742 Begin
         // guideId = R.string.panorama_guide_toward_direction;
	  //[BUGFIX]-MOD by TCTNB, hongyuan-wang , 2014-08-19,PR758742 End
            break;
        case GUIDE_ERROR:
            guideId = R.string.pano_dialog_panorama_generate_failed;
        default:
            break;
        }

        // show current guide
        if (guideId != 0) {
            showInfo(guideId);
        }
    }

    private void showInfo(int id){
	 //[BUGFIX]-MOD by TCTNB, hongyuan-wang , 2014-08-19,PR758742 Begin
        mActivity.showInfo(mActivity.getString(id),false,false);
         //[BUGFIX]-MOD by TCTNB, hongyuan-wang , 2014-08-19,PR758742 End
    }

    private void tctInit() {
        mActivity.getCameraViewManager().setViewState(GC.VIEW_STATE_SHUTTER_PRESS);
        mNeedUpdateViewFinder = true;
        mMergedBitmaps = new ArrayList<Bitmap>();
        mDirection = TctPanoramaProcessManager.DIRECTION_UNKNOWN;
        mTctPanoramaProcessManager = new TctPanoramaProcessManager(mController,
                mViewFinderView, mRootView);
        mTctPanoramaProcessManager.init(mController.getDeviceOrientation());

        isFinish = false;
        mNeedWait = true;
        mMergeThread = new MergeBitmapThread();
        mMergeThread.start();
        showGuideString(GUIDE_MOVE);
    }

    private void updateTctProcess(int angleInMajorDirection) {
        if (mDirection == TctPanoramaProcessManager.DIRECTION_UNKNOWN) {
            mDirection = mTctPanoramaProcessManager.setDirection(angleInMajorDirection, mProgressAngle[0],
                    mProgressAngle[1]);
            if (mDirection == TctPanoramaProcessManager.DIRECTION_UNKNOWN) {
                return;
            }
        }

        if (mDirection == TctPanoramaProcessManager.DIRECTION_LEFT
                || mDirection == TctPanoramaProcessManager.DIRECTION_RIGHT) {
            mMainAngle = mProgressAngle[0];
            mSecondAngle = mProgressAngle[1];
        } else {
            mMainAngle = mProgressAngle[1];
            mSecondAngle = mProgressAngle[0];
        }

        mNeedUpdateViewFinder = mTctPanoramaProcessManager.setProcess(mMainAngle,
                mSecondAngle);
        if (mNeedUpdateViewFinder) {
            mViewFinderView.calculateProcess(
            mDirection,
            Math.abs(mTctPanoramaProcessManager.getCurrentMaxProcess()));
        }
    }

    public void tct_applyLayout(){
        mViewFinderView = (TctViewFinderView) mRootView
                .findViewById(R.id.tct_pano_progress_view);


        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mPreviewLayout.getLayoutParams();
        TctParameter.init(mActivity, mDirection);
        int w = mActivity.getResources().getDisplayMetrics().heightPixels;
        int h = mActivity.getResources().getDisplayMetrics().widthPixels;

        float lcdLongside = Math.max(TctParameter.getDisplayWidth(),TctParameter.getDisplayHeight());
        float lcdShortside = Math.min(TctParameter.getDisplayWidth(),TctParameter.getDisplayHeight());

        double rate = 1 - 0.538;
        double ratio = (double) lcdLongside/lcdShortside;

        
        //layoutParams.topMargin= (int)((TctParameter.getDisplayHeight()- TctParameter.getDisplayWidth() * 4/3) * 3/8);;
        int topMargin=(int)((lcdLongside - lcdShortside * 4/3) * rate);
        int bottomMargin=(int)((lcdLongside - lcdShortside * 4/3) * (1-rate));
        if(mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
        	layoutParams.leftMargin =(int)((lcdLongside - lcdShortside * 4/3) * rate);
        	layoutParams.rightMargin = (int)((lcdLongside - lcdShortside * 4/3) * (1-rate));
        	layoutParams.height = TctParameter.getDisplayHeight();
            layoutParams.width = (int)layoutParams.height * 4 / 3;
            
        }else{
        	layoutParams.topMargin =(int)((lcdLongside - lcdShortside * 4/3) * rate);
        	layoutParams.bottomMargin = (int)((lcdLongside - lcdShortside * 4/3) * (1-rate));
        	layoutParams.width = TctParameter.getDisplayWidth();
            layoutParams.height = (int)layoutParams.width * 4 / 3;
        }
        mPreviewLayout.setLayoutParams(layoutParams);
    }

    private void tctReset() {
        mActivity.getCameraViewManager().setViewState(GC.VIEW_STATE_NORMAL);
        mShutterButton.setVisibility(View.VISIBLE);
        isFinish = true;
        mNeedWait = false;
        if (mMergeThread != null && mMergeThread.isAlive()) {
            try {
                mMergeThread.join();
            } catch (Exception e) {
            }
        }
        if (mTctPanoramaProcessManager != null) {
            mTctPanoramaProcessManager.reset();
            mTctPanoramaProcessManager=null;
        }
        if (mMergedBitmaps != null) {
            for (Bitmap b : mMergedBitmaps) {
                if (b != null) {
                    b.recycle();
                    b = null;
                }
            }
            mMergedBitmaps.clear();
        }
    }

    class MergeBitmapThread extends Thread {
        @Override
        public void run() {
            while (true) {
                int i = 0;
                if (isFinish) {
                    break;
                }
                try {
                    while (mNeedWait && i++ < 15) {
                        sleep(50);
                    }
                    mNeedWait = true;
                } catch (Exception e) {
                }

                if (createMergeBitmap()) {
                    //mTctHandler.removeMessages(GENERATE_TEMP_ERROR);
                    mTctHandler.removeMessages(GENERATE_TEMP_SUCCESS);
                    mTctHandler.sendEmptyMessage(GENERATE_TEMP_SUCCESS);
                }/*else{
                    if(!isFinish && mMainAngle > TctPanoramaProcessManager.MAIN_IGNORE_ERROR_ANGLE){
                       mTctHandler.sendEmptyMessage(GENERATE_TEMP_ERROR);
                    }
                }*/
            }
        }

        private synchronized boolean createMergeBitmap() {
        	Log.d(TAG,"###YUHUAN###createMergeBitmap#isFinish=" + isFinish);
            if (isFinish || mController.getCaptureState() == WideAnglePanoramaModule.CAPTURE_STATE_VIEWFINDER) {
                return false;
            }
            try {
                Bitmap temp = mController.getGenerateMosaicTempBitmap();
                temp = TctPanoramaProcessManager.rotate(temp, 90, true);
                if (mMergedBitmaps.size() > 10) {
                    mMergedBitmaps.get(0).recycle();
                    mMergedBitmaps.remove(0);
                }
                if (temp != null && !temp.isRecycled()) {
                    mMergedBitmaps.add(temp);
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG,"createMergeBitmap Error");
            }
            return false;
        }
    }

    private void updateBitmap2() {
        if (mMergedBitmaps.size() <= 0) {
            return;
        }
        Bitmap newBitmap = mMergedBitmaps.get(mMergedBitmaps.size() - 1);
        if (newBitmap != null && !newBitmap.isRecycled()) {
            mViewFinderView.setImageBitmap(newBitmap);
        }
    }

    public void onFrameAvailable() {
        mNeedWait = false;
    }
  //Add by fei.gao for PR953178 --begin
    //set shutter button
    public void setStatues(boolean value){
    	mShutterButton.setSelected(value);
    }
  //Add by fei.gao for PR953178 --end
}
