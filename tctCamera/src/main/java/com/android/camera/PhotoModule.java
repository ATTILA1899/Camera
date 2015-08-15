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
/*    date   |        Author        |         Key          |     comment      */
/* 2014-08-07|    chao.luo          |     PR744625         |Burst shooting repeatly,Camera have been in saving pictures screen. */
/* ----------|----------------------|----------------------|----------------- */
/* 08/07/2014|      jian.zhang1     |  PR752979,PR750874   |Only show one dialog*/
/*           |                      |                      |to remember location*/
/*    date   |        Author        |         Key          |     comment      */
/* 2014-08-08|    chao.luo          |     PR752855         |Can't burst shot use volume key or first long press shutter.. */
/* ----------|----------------------|----------------------|----------------- */
/* 08/14/2014|      jian.zhang1     |      PR764272        |Show toast when B-*/
/*           |                      |                      |urstShot isn't so-*/
/*           |                      |                      |ppurted           */
/* ----------|----------------------|----------------------|----------------- */
/* 08/19/2014|qiannan.wang          |738934                |[Gallery]Photo de */
/*           |                      |                      |tails are incorre */
/*           |                      |                      |ct.               */
/* ----------|----------------------|----------------------|----------------- */
/* 08/21/2014|jian.zhang1           |770765                |Set rotation for  */
/*           |                      |                      |timer count view  */
/* ----------|----------------------|----------------------|----------------- */
/* ******************************************************************************/
package com.android.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.camera.CameraManager.CameraAFCallback;
import com.android.camera.CameraManager.CameraAFMoveCallback;
import com.android.camera.CameraManager.CameraPictureCallback;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.CameraManager.CameraShutterCallback;
import com.android.camera.PhotoModule.NamedImages.NamedEntity;
import com.android.camera.custom.CustomFields;
import com.android.camera.custom.CustomUtil;
import com.android.camera.custom.picturesize.PictureSizePerso;
import com.android.camera.custom.picturesize.PictureSizePerso.PictureSize;
import com.android.camera.exif.ExifInterface;
import com.android.camera.exif.ExifTag;
import com.android.camera.exif.Rational;
import com.android.camera.manager.CameraManualModeManager;
import com.android.camera.manager.HelpGuideManager;
import com.android.camera.manager.InfoManager;
import com.android.camera.manager.QrcodeViewManager;
import com.android.camera.manager.ShutterManager;
import com.android.camera.manager.SwitcherManager;
import com.android.camera.manager.TopViewManager;
import com.android.camera.manager.ZoomManager;
import com.android.camera.qrcode.BitmapLuminanceSource;
import com.android.camera.qrcode.QRCode;
import com.android.camera.qrcode.QRCodeManager;
import com.android.camera.qrcode.QRCodeParser;
import com.android.camera.ui.BeautyFaceSeekBar;
import com.android.camera.ui.CountDownView.OnCountDownFinishedListener;
import com.android.camera.ui.FaceView;
import com.android.camera.ui.ManualModeSeekBar;
import com.android.camera.ui.ManualShutterSpeedAdapter;
import com.android.camera.ui.ModuleSwitcher;
import com.android.camera.ui.RotateTextToast;
import com.android.camera.ui.ShutterButton;
import com.android.camera.ui.ZoomControl;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.GcamHelper;
import com.android.camera.util.UsageStatistics;
import com.android.jrd.externel.CamcorderProfileEx;
import com.android.jrd.externel.ExtendCamera;
import com.android.jrd.externel.ExtendParameters;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.jrdcamera.modeview.PolaroidView;
import com.tct.camera.R;
import com.tct.jni.SceneDetectorLib;
//[FEATURE]-Add BEGIN by TCTNB,xutao.wu, 2014-07-24, PR635962
//[FEATURE]-Add END by TCTNB,xutao.wu, 2014-07-24, PR635962

public class PhotoModule implements CameraModule, PhotoController,
		ZoomControl.OnZoomIndexChangedListener, FocusOverlayManager.Listener,
		CameraPreference.OnPreferenceChangedListener,
		ShutterButton.OnShutterButtonListener, MediaSaveService.Listener,
		OnCountDownFinishedListener, SensorEventListener,
		TopViewManager.SettingListener, FaceBeautySettingCallBack {

	private static final String TAG = "YUHUAN_PhotoModule";

	public interface CaptureListener {
		public void onCaptureStart();

		public void onCaptureDone();
	}

	private CaptureListener mCaptureListener;

	public void setCaptureListener(CaptureListener l) {
		mCaptureListener = l;
	}

	// QCom data members
	public static boolean mBrightnessVisible = true;
	private static final int MAX_SHARPNESS_LEVEL = 6;
	private boolean mRestartPreview = false;
	private int mSnapshotMode;
	private int doChangeZSL;
	private int mBurstSnapNum = 1;
	private int mReceivedSnapNum = 0;
	public boolean mFaceDetectionEnabled = false;
	private static int burstshot_guidetip_display = 1;
	// modify by minghui.hua for PR1003310
	public static int BURST_NUM_MAX = 10;
	private long mCapturedTotalSize = 0;
	private boolean mLongpressFlag = false;
	private BurstshotSoundplayThread mBurstshotSoundPlayThread;

	/* Histogram variables */
	private GraphView mGraphView;
	private static final int STATS_DATA = 257;
	public static int statsdata[] = new int[STATS_DATA];
	public boolean mHiston = false;
	// We number the request code from 1000 to avoid collision with Gallery.
	private static final int REQUEST_CROP = 1000;

	private static final int SETUP_PREVIEW = 1;
	private static final int FIRST_TIME_INIT = 2;
	private static final int CLEAR_SCREEN_DELAY = 3;
	private static final int SET_CAMERA_PARAMETERS_WHEN_IDLE = 4;
	private static final int SHOW_TAP_TO_FOCUS_TOAST = 5;
	private static final int SWITCH_CAMERA = 6;
	private static final int SWITCH_CAMERA_START_ANIMATION = 7;
	private static final int CAMERA_OPEN_DONE = 8;
	private static final int OPEN_CAMERA_FAIL = 9;
	private static final int CAMERA_DISABLED = 10;
	private static final int SET_SKIN_TONE_FACTOR = 11;
	private static final int SET_PHOTO_UI_PARAMS = 12;
	private static final int SWITCH_TO_GCAM_MODULE = 13;
	private static final int CONFIGURE_SKIN_TONE_FACTOR = 14;
	private static final int START_SMILE_DETECT = 17;
	private static final int CANCEL_SMILE_FOCUS = 20;
	private static final int ON_PREVIEW_STARTED = 21;
	private static final int RESET_SHUTTER_MSG = 22;
	private static final int INIT_PREVIEW_PARAMETERS = 23;

	private static final int REQUEST_QRCODE_PICK_IMAGE = 1001;

	private static final int offset = 100;
	private static final int SHOW_BURST_NUM = 1 + offset;
	private static final int DISMISS_BURST_NUM = 2 + offset;

	// The subset of parameters we need to update in setCameraParameters().
	private static final int UPDATE_PARAM_INITIALIZE = 1;
	private static final int UPDATE_PARAM_ZOOM = 2;
	private static final int UPDATE_PARAM_PREFERENCE = 4;
	private static final int UPDATE_PARAM_ALL = -1;

	// This is the delay before we execute onResume tasks when coming
	// from the lock screen, to allow time for onPause to execute.
	private static final int ON_RESUME_TASKS_DELAY_MSEC = 20;

	private static final String DEBUG_IMAGE_PREFIX = "DEBUG_";

	public static final int FACEBEAUTY_SOFTEN = 50;
	public static final int FACEBEAUTY_BRIGHT = 50;
	public static int faceBeauty_soften = FACEBEAUTY_SOFTEN;
	public static int faceBeauty_bright = FACEBEAUTY_BRIGHT;

	// copied from Camera hierarchy
	private CameraActivity mActivity;
	private CameraProxy mCameraDevice;
	private int mCameraId;
	private Parameters mParameters;
	private boolean mPaused;
	private View mRootView;

	private PhotoUI mUI;
	private CustomUtil mCustomUtil;

	public static Size optimalSize;

	// The activity is going to switch to the specified camera id. This is
	// needed because texture copy is done in GL thread. -1 means camera is not
	// switching.
	protected int mPendingSwitchCameraId = -1;
	private boolean mOpenCameraFail;
	private boolean mCameraDisabled;

	// When setCameraParametersWhenIdle() is called, we accumulate the subsets
	// needed to be updated in mUpdateSet.
	private int mUpdateSet;

	public static final int SCREEN_DELAY = 2 * 60 * 1000;

	private int mZoomValue; // The current zoom value.

	private Parameters mInitialParams;
	private boolean mFocusAreaSupported;
	private boolean mMeteringAreaSupported;
	private boolean mAeLockSupported;
	private boolean mAwbLockSupported;
	private boolean mContinuousFocusSupported;
	private boolean mTouchAfAecFlag;
	private boolean mLongshotSave = true;
	// add for burstshot start
	private boolean isBurstshotBreaked = false;
	// private String LargestPicturesize;
	private String[] pictureSizeNonfullscreen;
	private String[] pictureSizeFullscreen;
	private String oldTargetRatio;
	private String oldPictureSizeFlag;
	private String oldPicturesize;
	private String targetRatio;

	// The degrees of the device rotated clockwise from its natural orientation.
	// Add by tiexing.xie Begin PR845568 2014.11.20
	// private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
	// Add by tiexing.xie Begin PR845568 2014.11.20
	private int mOrientation = 0;
	private ComboPreferences mPreferences;

	private static final String sTempCropFilename = "crop-temp";

	private ContentProviderClient mMediaProviderClient;
	private boolean mFaceDetectionStarted = false;

	// private static final String PERSIST_LONG_ENABLE =
	// "persist.camera.longshot.enable";
	// private static final String PERSIST_LONG_SAVE =
	// "persist.camera.longshot.save";
	// private static final String PERSIST_PREVIEW_RESTART =
	// "persist.camera.feature.restart";

	// Add by Begin tiexing.xie PR856951 20141201
	// private static final String START_QRSCAN_INTENT
	// ="com.tct.camera.STARTQRSCAN";
	// private static final String START_FRONT_CAMERA_INTENT
	// ="com.tct.camera.STARTFRONTCAMERA";
	// Add by End tiexing.xie PR856951 20141201
	// private static final int MINIMUM_BRIGHTNESS = 0;
	// private static final int MAXIMUM_BRIGHTNESS = 6;

	private static final int CAMERA_SUPPORT_MODE_ZSL = 2;
	private static final int CAMERA_SUPPORT_MODE_NONZSL = 3;

	private int mbrightness = 3;
	private int mbrightness_step = 1;
	private ProgressBar brightnessProgressBar;
	// Constant from android.hardware.Camera.Parameters
	private static final String KEY_PICTURE_FORMAT = "picture-format";
	private static final String KEY_QC_RAW_PICUTRE_SIZE = "raw-size";
	public static final String PIXEL_FORMAT_JPEG = "jpeg";
	private int mStreamID;
	private SoundPool mBurstSound;
	private int mSoundID;

	private static final int MIN_SCE_FACTOR = -10;
	private static final int MAX_SCE_FACTOR = +10;
	private int SCE_FACTOR_STEP = 10;
	private int mskinToneValue = 0;
	private boolean mSkinToneSeekBar = false;
	private boolean mSeekBarInitialized = false;
	private SeekBar skinToneSeekBar;
	private TextView LeftValue;
	private TextView RightValue;
	private TextView Title;

	private boolean mPreviewRestartSupport = false;

	private SoundPool mQRCodeSound;
	private int mQRCodeSoundID;

	// mCropValue and mSaveUri are used only if isImageCaptureIntent() is true.
	private String mCropValue;
	private Uri mSaveUri;

	private Uri mDebugUri;
	// //[BUGFIX]-ADD-BEGIN by tiexing.xie 2014.11.19
	// public List<Size> supportPictureSize = new ArrayList<Size>();
	// public List<Size> pictureSize_16_9 = new ArrayList<Size>();
	// public List<Size> pictureSize_4_3 = new ArrayList<Size>();
	// //[BUGFIX]-ADD-END by tiexing.xie 2014.11.19

	public static boolean NEW_FLAG = true;
	private boolean locationFirstRunFlag = true;

	// We use a queue to generated names of the images to be used later
	// when the image is ready to be saved.
	private NamedImages mNamedImages;

	private boolean isCaptureSoundEnable;
	private boolean isInSmileSnap = false;
	// [BUGFIX]-ADD-begin by chao.luo, PR-752855, 2014-8-8
	private boolean mVolumeKeyDownAtEve = false;
	private boolean mCameraKeyLongPressed = false;
	// [BUGFIX]-ADD-end by chao.luo, PR-752855, 2014-8-8
	private boolean mCameraViewRefresh = false; // [BUG-899418]lijuan.zhang
												// add,20151115

	public static int ISO_MINVALUE = 0;
	public static int ISO_MAXVALUE = 0;
	public static int ISO_EXPOSURETIME_MINVALUE = 0;
	// public static int ISO_EXPOSURETIME_MAXVALUE=0;
	public static int FOUCS_POS_RATIO_MINVALUE = 0;
	public static int FOUCS_POS_RATIO_MAXVALUE = 0;

	public static int WB_DEFAULT_AUTO = 2;

	public final static int NOT_SHOW_AUTO_MODE = 1;
	public final static int SHOW_AUTO_MODE = 0;

	private boolean isFirstDisplay_night = false;
	private boolean isFirstDisplay_backlight = false;

	private Runnable mDoSnapRunnable = new Runnable() {
		@Override
		public void run() {
			// Don't run this on expression4, smile, zsl mode, the action may be
			// changed.
			int mode = mGC.getCurrentMode();
			if (GC.MODE_EXPRESSION4 == mode || GC.MODE_SMILE == mode
					|| GC.MODE_ZSL == mode) {
				return;
			}
			onShutterButtonClick();
		}
	};

	// This runnable is used to take picture for expression4 mode.
	private Runnable mExpressionDoSnapRunnable = new Runnable() {
		@Override
		public void run() {
			if (mCameraState == SWITCHING_CAMERA || !mActivity.isInCamera()) {
				return;
			}
			if (ExpressionThread.canTakePictureAgain()) {
				Log.d(TAG, "zsy1031 canTakePictureAgain");
				ExpressionThread.onShutterButtonClick();
				// Here just take picture simply, if needed, extend it.
				mSnapshotOnIdle = false;
				mFocusManager.doSnap();
			}
		}
	};

	private Runnable mSmileShotDoSnapRunnable = new Runnable() {
		public void run() {

			if (mCameraState == SNAPSHOT_IN_PROGRESS) {
				return;
			}

			if (!mActivity.mFilmStripView.inCameraFullscreen()) {
				mHandler.sendEmptyMessageDelayed(START_SMILE_DETECT, 1000);
				return;
			}

			if (!mIsImageCaptureIntent && mCameraId == GC.CAMERAID_BACK) {
				boolean startFocus = doSmileFocus();
				if (startFocus) {
					if (mHandler.hasMessages(CANCEL_SMILE_FOCUS)) {
						mHandler.removeMessages(CANCEL_SMILE_FOCUS);
					}
					// if everything goes well, 2.5s is enough for focus
					mHandler.sendEmptyMessageDelayed(CANCEL_SMILE_FOCUS, 2500);
				}
			}
			onShutterButtonClick();
		}
	};

	private boolean doSmileFocus() {
		if (null == mFocusManager)
			return false;
		if (null == mUI)
			return false;
		FaceView mFaceView = mUI.mFaceView;
		if (null == mFaceView)
			return false;
		RectF mRect = mFaceView.getRect();
		if (null == mRect)
			return false;
		int left = Math.round(mRect.left);
		int top = Math.round(mRect.top);
		int right = Math.round(mRect.right);
		int bottom = Math.round(mRect.bottom);
		int pointX;
		int pointY;
		int orientation = mFaceView.getFaceOrientation();
		if (orientation % 180 == 90) {
			pointX = Math.round((top + bottom) / 2);
			pointY = Math.round((left + right) / 2);
		} else {
			pointX = Math.round((left + right) / 2);
			pointY = Math.round((top + bottom) / 2);
		}
		isInSmileSnap = true;

		boolean fullScreen = false;
		if (null != mPreferences) {
			fullScreen = mPreferences
					.getString(
							CameraSettings.KEY_PICTURE_RATIO,
							mActivity
									.getString(R.string.pref_camera_picture_ratio_default))
					.equals(mActivity.getString(R.string.setting_on_value));
		}
		fullScreen = mUI.isPreviewFullScreen();
		// 81 is a magic number here, add it to fit the face position
		mFocusManager.onSingleTapUp(Math.abs(pointX),
				fullScreen ? Math.abs(pointY) : Math.abs(pointY) + 81);
		return true;
	}

	/**
	 * An unpublished intent flag requesting to return as soon as capturing is
	 * completed.
	 * 
	 * TODO: consider publishing by moving into MediaStore.
	 */
	private static final String EXTRA_QUICK_CAPTURE = "android.intent.extra.quickCapture";

	// The display rotation in degrees. This is only valid when mCameraState is
	// not PREVIEW_STOPPED.
	private int mDisplayRotation;
	// The value for android.hardware.Camera.setDisplayOrientation.
	private int mCameraDisplayOrientation;
	// The value for UI components like indicators.
	private int mDisplayOrientation;
	// The value for android.hardware.Camera.Parameters.setRotation.
	private int mJpegRotation;
	// Indicates whether we are using front camera
	private boolean mMirror;
	private boolean mFirstTimeInitialized;
	private boolean mIsImageCaptureIntent;

	private int mCameraState = PREVIEW_STOPPED;
	private boolean mSnapshotOnIdle = false;

	private boolean mShowProgress = false;

	private ContentResolver mContentResolver;
	private ShutterManager mShutterManager;
	private TopViewManager mTopViewManager;
	private ZoomManager mZoomManager;
	private SwitcherManager mSwitcherManager;
	private QrcodeViewManager mQrcodeViewManager;
	private CameraManualModeManager mCameraManualModeManager;
	private HelpGuideManager mHelpGuideManager;
	private CameraManualModeCallBackListener mCameraManualModeCallBackListener;
	private LocationManager mLocationManager;
	private static boolean isLocationOpened = false;
	// / add by yaping.liu for pr963472 {
	private static final int SETGPS = 1002;
	// / }

	private final PostViewPictureCallback mPostViewPictureCallback = new PostViewPictureCallback();
	private final RawPictureCallback mRawPictureCallback = new RawPictureCallback();
	private final AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
	private final Object mAutoFocusMoveCallback = ApiHelper.HAS_AUTO_FOCUS_MOVE_CALLBACK ? new AutoFocusMoveCallback()
			: null;

	private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();
	// private final StatsCallback mStatsCallback = new StatsCallback();

	private long mFocusStartTime;
	private long mShutterCallbackTime;
	private long mPostViewPictureCallbackTime;
	private long mRawPictureCallbackTime;
	private long mJpegPictureCallbackTime;
	private long mOnResumeTime;
	private byte[] mJpegImageData;

	// These latency time are for the CameraLatency test.
	public long mAutoFocusTime;
	public long mShutterLag;
	public long mShutterToPictureDisplayedTime;
	public long mPictureDisplayedToJpegCallbackTime;
	public long mJpegCallbackFinishTime;
	public long mCaptureStartTime;

	// This handles everything about focus.
	private FocusOverlayManager mFocusManager;

	private String mSceneMode;
	private String mCurrTouchAfAec = "touch-on";

	private MessageQueue.IdleHandler mIdleHandler = null;

	private PreferenceGroup mPreferenceGroup;

	private boolean mQuickCapture;
	private SensorManager mSensorManager;
	private float[] mGData = new float[3];
	private float[] mMData = new float[3];
	private float[] mR = new float[16];
	private int mHeading = -1;
	private GC mGC;
	// True if all the parameters needed to start preview is ready.
	private boolean mCameraPreviewParamsReady = false;

	private final static long QRCODE_CHECK_DELAY = 700;
	// [BUGFIX]-ADD-END by xuan.zhou, PR-639439, 2014-4-9
	private QRCodeCheckThread checkThread = null;
	private long lastCheckTime = -1;

	private static final int ASD_FREQUENCY = 10;
	private int mAsdDetectCounter = 0;

	private static final int MSG_ASD = 0x10111111;
	private static final int MSG_PREVIEW_DATA_READY = MSG_ASD + 1;
	private static final int MSG_BEAUTY_FACE_YUV_DATA_READY = MSG_PREVIEW_DATA_READY + 1;
	private static final int MSG_QUIT = MSG_BEAUTY_FACE_YUV_DATA_READY + 1;

	private static final int ASD_AUTO = 0;
	private static final int ASD_LANDSCAPE = 1;
	private static final int ASD_PORTRAIT = 2;
	private static final int ASD_NIGHT = 4;
	private static final int ASD_BACKLIT = 6;
	private static final int ASD_GOURMET = 19;

	private int mAsdWidth = 0;
	private int mAsdHeight = 0;

	private final Handler mHandler = new MainHandler();

	private int mAsdState = ASD_AUTO;
	private boolean needStartAsdLooper = false;

	private class ASDBuffer {
		boolean isUsing = false;
		byte[] callbackData = null;
		int width;
		int height;
	}

	private int mASDShowingState = ASD_AUTO;
	private ASDBuffer mASDCallbackData = new ASDBuffer();
	private ASDThread mASDProcessThread = null;
	boolean mIsUnderASD = false;
	private byte[] mPreviewBuffer = null;
	
	/*
	private CameraManager.CameraPreviewDataCallback arcsoftCB = new CameraManager.CameraPreviewDataCallback() {

		@Override
		public void onPreviewFrame(byte[] data, CameraProxy camera) {
			if (data == null) {
				return;
			}
			if (mParameters != null) {
				int previewBufferSize = mParameters.getPreviewSize().width
						* mParameters.getPreviewSize().height * 3 / 2;

				if (mPreviewBuffer == null) {
					mPreviewBuffer = new byte[previewBufferSize];
				}

				if (data.length == previewBufferSize) {
					mPreviewBuffer = null;
					mPreviewBuffer = data;
				} else {
					mPreviewBuffer = null;
					mPreviewBuffer = new byte[previewBufferSize];
				}

			}
			if (mCameraDevice != null)
				mCameraDevice.addCallbackBuffer(mPreviewBuffer);
			if (isASDFrameCallbackClosed || isInCapture) {
				// modify by minghui.hua for PR947464
				return;
			}

			if (mGC.getCurrentMode() == GC.MODE_ARCSOFT_FACEBEAUTY
					|| mGC.getCurrentMode() == GC.MODE_HDR
					|| mGC.getCurrentMode() == GC.MODE_MANUAL || mCameraId == 1) {
				//mIsEnableNight = false;
				mAsdState = ASD_AUTO;
				mASDShowingState = ASD_AUTO;
				if (mGC.getCurrentMode() == GC.MODE_HDR) {
					mSceneMode = Parameters.SCENE_MODE_HDR;
					if (!mParameters.getSceneMode().equals(
							Parameters.SCENE_MODE_HDR)) {
						checkThread();
						mParameters.setSceneMode(Parameters.SCENE_MODE_HDR);
						applyParametersToServer();
					}
				} else if (!mParameters.getSceneMode().equals(
						Parameters.SCENE_MODE_AUTO)) {
					mSceneMode = Parameters.SCENE_MODE_AUTO;
					checkThread();
					mParameters.setSceneMode(Parameters.SCENE_MODE_AUTO);
					applyParametersToServer();
				}

				return;
			}
			if (mASDProcessThread == null
					&& "on".equals(mActivity.getResources().getString(
							R.string.def_arcsoft_feature_onoff))) {// v-nj-feiqiang.cheng
																	// for
																	// PR941559,941555
				needToTurnOffAsd = false;
				mASDProcessThread = new ASDThread();
				initFirstDisplay();
				mASDProcessThread.start();
			}

			// if(mActivity.isInAsd()){
			mAsdDetectCounter++;
			if (mAsdDetectCounter >= ASD_FREQUENCY) {
				mAsdDetectCounter = 0;
				synchronized (mASDCheckLock) {
					if (!mASDCallbackData.isUsing) {
						mASDCallbackData.callbackData = data;
						int datalength = data.length;
						if (mASDCallbackData.width * mASDCallbackData.height
								* 3 / 2 != datalength) {// NV21 data length , 6
														// bytes for 4 pixels
							mASDCallbackData.width = camera.getParameters()
									.getPreviewSize().width;
							mASDCallbackData.height = camera.getParameters()
									.getPreviewSize().height;

							if (mASDCallbackData.width
									* mASDCallbackData.height * 3 / 2 != datalength) {
								return;
							}
						}

					}

					mASDCheckLock.notify();
				}
			}
			// }
		}

	};
	*/

	List<String> fileNames = new ArrayList<String>();

	public void saveFile(byte[] yuv420sp, String fileName, boolean append) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(fileName, append);
			fos.write(yuv420sp);
			fos.close();
			if (fileNames.size() < 100) {
				fileNames.add(fileName);
			} else {
				String delfileName = fileNames.get(0);
				File delFile = new File(delfileName);
				if (delFile.exists()) {
					delFile.delete();
					fileNames.remove(0);
					fileNames.add(fileName);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initFirstDisplay() {
		isFirstDisplay_backlight = getValue(CameraSettings.BACK_LIGHt_TOAST_ISFIRST_DISPLAY);
		isFirstDisplay_night = getValue(CameraSettings.NIGHT_MODE_TOAST_ISFIRST_DISPLAY);
	}

	private Object mASDCheckLock = new Object();
	private int showToastStatus = -1;
	private boolean needToTurnOffAsd = false;

	private class ASDThread extends Thread {
		int showAutoToast = SHOW_AUTO_MODE;

		public void run() {
			while (mIsUnderASD) {
				synchronized (mASDCheckLock) {
					try {
						if (mPaused) {
							return;
						}
						mASDCheckLock.wait();
					} catch (InterruptedException e) {
					}

					if (needToTurnOffAsd) {
						return;
					}
					if (!mActivity.isInCamera()) {
						continue;
					}
					mASDCallbackData.isUsing = true;
					int width = mASDCallbackData.width;
					int height = mASDCallbackData.height;

					byte[] data = mASDCallbackData.callbackData.clone();

					// SimpleDateFormat formatter=new
					// SimpleDateFormat("HH:mm:ss:SSS");
					// Date curDate =new
					// Date(System.currentTimeMillis());//获取当前时间
					// String str = formatter.format(curDate);
					//
					// saveFile(data, String.format("/sdcard/arcsoft/ns_%s.jpg",
					// str),
					// false);
					// CameraActivity.TraceQCTLog("data length is "+data.length+" width is "+width+
					// " height is "+height +" and should take "+
					// width*height*3/2);
					if (data.length != width * height * 3 / 2) {// Width and
																// Height is
																// wrong ,
																// ignore this
																// case
						continue;
					}

					if (SceneDetectorLib.instancePool() == 0) {
						SceneDetectorLib.init(width, height);
						mAsdWidth = width;
						mAsdHeight = height;
					} else if (mAsdWidth != width || mAsdHeight != height) {
						SceneDetectorLib.uninit();
						SceneDetectorLib.init(width, height);
						mAsdWidth = width;
						mAsdHeight = height;
					}
					mAsdState = SceneDetectorLib.process(width, height, data);
					data = null;
					// CameraActivity.TraceQCTLog("ASD Callback "+" state ="+mAsdState);
					mASDCallbackData.isUsing = false;

					/*
					if ((mAsdState == ASD_BACKLIT && mGC
							.getListPreference(
									GC.ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE)
							.findIndexOfValue(
									mGC.getListPreference(
											GC.ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE)
											.getValue()) != 1)
							|| (mAsdState == ASD_NIGHT && mGC
									.getListPreference(
											GC.ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE)
									.findIndexOfValue(
											mGC.getListPreference(
													GC.ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE)
													.getValue()) != 1)) {
						mAsdState = ASD_AUTO;
					}*/

					if (mAsdState == ASD_LANDSCAPE || mAsdState == ASD_PORTRAIT
							|| mAsdState == ASD_GOURMET) {// Don't do any
															// process in these
															// three scenes
						mAsdState = ASD_AUTO;
					}
					if (isInCapture) {
						continue;
					}
					if (mASDShowingState != mAsdState) {
						mASDShowingState = mAsdState;
						//mIsEnableNight = (mAsdState == ASD_NIGHT);
						mHandler.post(new Runnable() {
							public void run() {
								String flashMode = mPreferences
										.getString(
												CameraSettings.KEY_FLASH_MODE,
												mActivity
														.getString(R.string.pref_camera_flashmode_default));

								/*
								if (mIsEnableNight) {
									flashMode = Parameters.FLASH_MODE_OFF;
								}*/
								
								if (mParameters != null
										&& mParameters.getFlashMode() != null
										&& !mParameters.getFlashMode().equals(
												flashMode)) {
									checkThread();
									mParameters.setFlashMode(flashMode);
								}
								// updateZsl();
							}
						});
						if (mActivity.getCameraViewManager().getInfoManager()
								.getUodoStatus() == InfoManager.NIGHT_MODE_DISABLE
								|| mActivity.getCameraViewManager()
										.getInfoManager().getUodoStatus() == InfoManager.BACK_NIGHT_DISABLE) {
							showAutoToast = NOT_SHOW_AUTO_MODE;
						} else {
							showAutoToast = SHOW_AUTO_MODE;
						}
						mActivity.getCameraViewManager().getInfoManager()
								.setUndoStatus();
						mHandler.sendMessage(Message.obtain(mHandler, MSG_ASD,
								mAsdState, showAutoToast));
					}
				}
			}
		}
	}

	//private boolean mIsEnableNight = false;

	private CameraManager.CameraPreviewDataCallback qrcodecb = new CameraManager.CameraPreviewDataCallback() {
		public void onPreviewFrame(final byte[] data,
				CameraManager.CameraProxy camera) {
			long now = System.currentTimeMillis();
			long diff = now - lastCheckTime;
			if (diff < QRCODE_CHECK_DELAY) {
				return;
			} else {
				Log.i(TAG, "LMCH04183 callback )( diff:" + diff
						+ " checkThread:" + checkThread);
				if (checkThread != null)
					Log.i(TAG, "LMCH04183 checkThread.isFinished():"
							+ checkThread.isFinished());
				if (checkThread != null && !checkThread.isFinished())
					return;
				Log.i(TAG, "LMCH04183 callback <|||||||||>");
				lastCheckTime = now;
				checkThread = new QRCodeCheckThread(data);
				checkThread.start();
			}
		}
	};

	private void handleQRCodeResult(Result result) {
		final String text = result.getText();
		final QRCode qrcode = QRCodeParser.parse(text);
		Log.i(TAG, "LMCH04183 result 222 qrcode:" + qrcode);
		if (text != null && text.length() > 0) {
			QRCodeManager.addQRCode(mActivity, qrcode);
			if (QRCodeManager.shouldGotoDetailPage(qrcode)) {
				Intent intent = new Intent(mActivity,
						com.android.camera.QRCodeResultActivity.class);
				intent.putExtra("qrcode", qrcode);
				mActivity.startActivity(intent);
			} else {
				QRCodeManager.openQRCode(mActivity, qrcode);
			}
			if (QRCodeManager.isBeep(mActivity)) {
				mQRCodeSound.play(mQRCodeSoundID, 1.0f, 1.0f, 1, 0, 1.0f);
			}
			if (QRCodeManager.isVibrate(mActivity)) {
				Vibrator vibrator = (Vibrator) mActivity
						.getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(300);
			}
		}
	}

	private class BurstshotSoundplayThread extends Thread {

		@Override
		public void run() {
			Log.d(TAG,"###YUHUAN###BurstshotSoundplayThread#mPaused=" + mPaused);
			Log.d(TAG,"###YUHUAN###BurstshotSoundplayThread#isBurstshotBreaked=" + isBurstshotBreaked);
			Log.d(TAG,"###YUHUAN###BurstshotSoundplayThread#mLongpressFlag=" + mLongpressFlag);
			Log.d(TAG,"###YUHUAN###BurstshotSoundplayThread#mReceivedSnapNum=" + mReceivedSnapNum);
			Log.d(TAG,"###YUHUAN###BurstshotSoundplayThread#BURST_NUM_MAX=" + BURST_NUM_MAX);
			
			while ((!mPaused) && (!isBurstshotBreaked) && mLongpressFlag
					&& mReceivedSnapNum <= BURST_NUM_MAX) {
				mActivity.getMediaSaveService().updateShowNumber4Bustshot(
						mReceivedSnapNum);
				mHandler.sendEmptyMessage(SHOW_BURST_NUM);
				if (isCaptureSoundEnable) {
					if (mStreamID != 0)
						mBurstSound.stop(mStreamID);
					mStreamID = mBurstSound.play(mSoundID, 1.0f, 1.0f, 1, 0,
							1.5f);
				}
				try {
					Thread.sleep(100);
					if (mReceivedSnapNum == BURST_NUM_MAX) {
						Log.i(TAG,
								"The burstshot GOto MAX then shutterButton up");
						mHandler.sendEmptyMessage(RESET_SHUTTER_MSG);
						break;
					}
					// Do not take the picture if there is not enough storage.
					if ((mActivity.getStorageSpaceBytes() - mCapturedTotalSize) <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
						Log.i(TAG,
								"there is not enough storage then shutterButton up");
						mHandler.sendEmptyMessage(RESET_SHUTTER_MSG);
						break;
					}
					if (isMemoryLow()) {
						mActivity
								.getCameraViewManager()
								.getInfoManager()
								.showText(
										mActivity
												.getString(R.string.burstshot_low_memory_auto_stop),
										2000, false);
						Log.i(TAG, "isMemoryLow then shutterButton up");
						mHandler.sendEmptyMessage(RESET_SHUTTER_MSG);
						break;
					}
				} catch (Exception e) {
					Log.e(TAG,
							"LMCH0424a occurred error when playing sound and showing burstshot number.");
				}
			}

			// modify by minghui.hua for PR919150
			mActivity.sendShowProgressMessage(String.format(Locale.ENGLISH,
					mActivity.getString(R.string.continuous_saving_pictures),
					mReceivedSnapNum));
			mActivity.getMediaSaveService().capturedDone4Burstshot(
					mReceivedSnapNum);
			// [BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-26,PR770858 End
			mHandler.sendEmptyMessageDelayed(DISMISS_BURST_NUM, 500);
			// [BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-15,PR752872 End
		}

	}

	// The Thread to check QRCode <br>
	// It will avoid blocking main Thread
	private class QRCodeCheckThread extends Thread {
		private byte[] data;
		private boolean finished = false;

		public QRCodeCheckThread(byte[] data) {
			this.data = data;
		}

		public void run() {
			if (mCameraDevice == null) {
				mCameraDevice = mActivity.getCameraDevice();
			}
			Camera.Parameters params = mCameraDevice.getParameters();
			int width = params.getPreviewSize().width;
			int height = params.getPreviewSize().height;
			byte[] rotatedData = new byte[data.length];
			if (width * height * 3 / 2 != data.length) {// PR973746, Add
														// boundary check for
														// nv21 data ,this data
														// may not synchronized
														// with parameter
				return;
			}
			CameraUtil.rotateYUV240SP(data, rotatedData, width, height);
			// for (int y = 0; y < height; y++) {
			// for (int x = 0; x < width; x++)
			// rotatedData[x * height + height - y - 1] = data[x + y * width];
			// }
			int tmp = width;
			width = height;
			height = tmp;
			HashMap<DecodeHintType, String> hashmap = new HashMap<DecodeHintType, String>();
			hashmap.put(DecodeHintType.TRY_HARDER, "tryHarder");
			PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
					rotatedData, width, height, width / 5, height / 4,
					width * 3 / 5, height / 2, false);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			MultiFormatReader reader = new MultiFormatReader();
			try {
				Result result = reader.decode(bitmap, hashmap);
				Log.i(TAG, "LMCH04183 result +++++");
				handleQRCodeResult(result);
			} catch (Exception e) {
				tmp = width;
				width = height;
				height = tmp;
				source = new PlanarYUVLuminanceSource(data, width, height,
						width / 5, height / 4, width * 3 / 5, height / 2, false);
				bitmap = new BinaryBitmap(new HybridBinarizer(source));
				try {
					Result originalResult = reader.decode(bitmap, hashmap);
					handleQRCodeResult(originalResult);
				} catch (Exception originalE) {
					originalE.printStackTrace();
				}
				e.printStackTrace();
			}
			finished = true;
		}

		public boolean isFinished() {
			return finished;
		}
	}

	private class QRCodeScanTask extends AsyncTask<Void, Void, Result> {
		private String imagePath;
		private ProgressDialog mProgressDialog;

		public QRCodeScanTask(String imagePath) {
			this.imagePath = imagePath;
		}

		@Override
		public void onPreExecute() {
			mProgressDialog = new ProgressDialog(mActivity);
			mProgressDialog.setMessage(mActivity
					.getString(R.string.qrcode_scanning));
			mProgressDialog.show();
		}

		public Result doInBackground(Void... params) {
			try {
				MultiFormatReader multiFormatReader = new MultiFormatReader();
				Bitmap bm = BitmapFactory.decodeFile(imagePath);
				int width = bm.getWidth();
				int height = bm.getHeight();
				Log.i(TAG, "ori width= " + width + " height= " + height);
				final int targetWidth = 720, targetHeight = 720;
				if (width > targetWidth || height > targetHeight) {
					float scale = 0.0f;
					if (width > height) {
						scale = (float) targetWidth / width;
					} else {
						scale = (float) targetHeight / height;
					}
					int w = Math.round(scale * width);
					int h = Math.round(scale * height);
					Log.i(TAG, "scal width= " + w + " height= " + h);
					bm = Bitmap.createScaledBitmap(bm, w, h, true);
				}
				Result result = multiFormatReader
						.decodeWithState(new BinaryBitmap(new HybridBinarizer(
								new BitmapLuminanceSource(bm))));
				return result;
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public void onPostExecute(Result result) {
			mProgressDialog.dismiss();
			if (result == null)
				Toast.makeText(mActivity, R.string.qrcode_notfound,
						Toast.LENGTH_SHORT).show();
			else
				handleQRCodeResult(result);
		}
	}

	private MediaSaveService.OnMediaSavedListener mOnMediaSavedListener = new MediaSaveService.OnMediaSavedListener() {
		@Override
		public void onMediaSaved(Uri uri) {
			if (uri != null) {
				mActivity.notifyNewMedia(uri);
			}
		}
	};

	private void checkDisplayRotation() {
		// Set the display orientation if display rotation has changed.
		// Sometimes this happens when the device is held upside
		// down and camera app is opened. Rotation animation will
		// take some time and the rotation value we have got may be
		// wrong. Framework does not have a callback for this now.
		if (CameraUtil.getDisplayRotation(mActivity) != mDisplayRotation) {
			setDisplayOrientation();
		}
		if (SystemClock.uptimeMillis() - mOnResumeTime < 5000) {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					checkDisplayRotation();
				}
			}, 100);
		}
	}

	final int ZSL_OFF = 0;
	final int ZSL_ON = 1;
	int ZSL_STATE = -1;

	/**
	 * This Handler is used to post message back onto the main thread of the
	 * application
	 */
	private class MainHandler extends Handler {
		public MainHandler() {
			super(Looper.getMainLooper());
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RESET_SHUTTER_MSG: {
				// modify by minghui.hua for PR1003310
				onShutterButtonFocus(false);
				break;
			}
			case SETUP_PREVIEW: {
				setupPreview();
				break;
			}

			case CLEAR_SCREEN_DELAY: {
				mActivity.getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				break;
			}

			case FIRST_TIME_INIT: {
				initializeFirstTime();
				break;
			}

			case SET_CAMERA_PARAMETERS_WHEN_IDLE: {
				setCameraParametersWhenIdle(0);
				break;
			}

			case SHOW_TAP_TO_FOCUS_TOAST: {
				showTapToFocusToast();
				break;
			}

			case SWITCH_CAMERA: {
				switchCamera();
				break;
			}

			case SWITCH_CAMERA_START_ANIMATION: {
				// TODO: Need to revisit
				// ((CameraScreenNail)
				// mActivity.mCameraScreenNail).animateSwitchCamera();
				break;
			}

			case CAMERA_OPEN_DONE: {
				onCameraOpened();
				break;
			}

			case OPEN_CAMERA_FAIL: {
				mOpenCameraFail = true;
				CameraUtil.showErrorAndFinish(mActivity,
						R.string.cannot_connect_camera);
				break;
			}

			case CAMERA_DISABLED: {
				mCameraDisabled = true;
				CameraUtil.showErrorAndFinish(mActivity,
						R.string.camera_disabled);
				break;
			}
			case SET_SKIN_TONE_FACTOR: {
				Log.v(TAG, "set tone bar: mSceneMode = " + mSceneMode);
				// setSkinToneFactor();
				mSeekBarInitialized = true;
				// skin tone ie enabled only for party and portrait BSM
				// when color effects are not enabled
				String colorEffect = mPreferences
						.getString(
								CameraSettings.KEY_COLOR_EFFECT,
								mActivity
										.getString(R.string.pref_camera_coloreffect_default));
				if ((Parameters.SCENE_MODE_PARTY.equals(mSceneMode) || Parameters.SCENE_MODE_PORTRAIT
						.equals(mSceneMode))
						&& (Parameters.EFFECT_NONE.equals(colorEffect))) {
					;
				} else {
					Log.v(TAG, "Skin tone bar: disable");
					// disableSkinToneSeekBar();
				}
				break;
			}
			case SET_PHOTO_UI_PARAMS: {
				CameraActivity.TraceLog(TAG, "update UI Params");
				setCameraParametersWhenIdle(UPDATE_PARAM_PREFERENCE);
				mUI.updateOnScreenIndicators(mParameters, mPreferenceGroup,
						mPreferences);
				resizeForPreviewAspectRatio();
				break;
			}

			case SWITCH_TO_GCAM_MODULE: {
				mActivity.onModuleSelected(ModuleSwitcher.GCAM_MODULE_INDEX);
			}

			case CONFIGURE_SKIN_TONE_FACTOR: {
				if (isCameraIdle()) {
					mParameters = mCameraDevice.getParameters();
					checkThread();
					mParameters.set("skinToneEnhancement",
							String.valueOf(msg.arg1));
					mCameraDevice.setParameters(mParameters);
				}
				break;
			}
			case START_SMILE_DETECT: {
				SmileShotThread.detect(mActivity, mSmileShotDoSnapRunnable);
				break;
			}

			case CANCEL_SMILE_FOCUS: {
				mFocusManager.onShutterUp();
				setupPreview();
				SmileShotThread.detect(mActivity, mSmileShotDoSnapRunnable);
				break;
			}

			case SHOW_BURST_NUM: {
				mUI.setBurstShootingVisibility(mReceivedSnapNum);
				break;
			}

			case DISMISS_BURST_NUM: {
				mUI.setBurstShootingVisibility(0);
				break;
			}

			case INIT_PREVIEW_PARAMETERS: {
				setCameraParameters(UPDATE_PARAM_ALL);
				resizeForPreviewAspectRatio();
				if (mParameterStateListener != null) {
					mParameterStateListener.onParameterUpdated();
					mParameterStateListener = null;
				}
				break;
			}
			case ON_PREVIEW_STARTED: {
				onPreviewStarted();
				checkDisplayRotation();
				/**
				 * PR932985-sichao.hu add begin , to avoid do UI update in
				 * thread other than main thread Add by Begin tiexing.xie
				 * PR856951 20141201, PR946277
				 */
				if (GC.START_QRSCAN_INTENT.equals(mActivity.getCurIntent()
						.getAction())) {
					mActivity.goToScanMode();
				} else if (GC.START_FRONT_CAMERA_INTENT.equals(mActivity
						.getCurIntent().getAction())) {
					mActivity.backToPhotoMode();
				}
				// Add by End tiexing.xie PR856951 20141201
				// PR932985-sichao.hu add end

				if (mStartPreviewCallback != null)
					mStartPreviewCallback.onPreviewStarted();
				break;
			}

			case MSG_ASD:
				if (isInCapture) {
					break;
				}
				List<String> sceneModes = mParameters.getSupportedSceneModes();
				mSceneMode = Parameters.SCENE_MODE_AUTO;
				switch (msg.arg1) {
				case ASD_AUTO:
					if (sceneModes.contains(Parameters.SCENE_MODE_AUTO)) {
						checkThread();
						mParameters.setSceneMode(Parameters.SCENE_MODE_AUTO);
					}
					if (msg.arg2 == NOT_SHOW_AUTO_MODE) {
						// do nothing
					} else if (msg.arg2 == SHOW_AUTO_MODE) {
						mUI.setCurrentModeVisible(mAsdAutoTitle, true, false,
								false);
					}
					CameraActivity.TraceLog(TAG,
							"***************auto mode**************");
					break;
				case ASD_LANDSCAPE:
					if (sceneModes.contains(Parameters.SCENE_MODE_LANDSCAPE)) {
						checkThread();
						mParameters
								.setSceneMode(Parameters.SCENE_MODE_LANDSCAPE);
					}
					mUI.setCurrentModeVisible("Landscape", true, false, false);
					CameraActivity.TraceLog(TAG,
							"***************landscape mode**************");
					break;
				case ASD_PORTRAIT:
					if (sceneModes.contains(Parameters.SCENE_MODE_PORTRAIT)) {
						checkThread();
						mParameters
								.setSceneMode(Parameters.SCENE_MODE_PORTRAIT);
					}
					mUI.setCurrentModeVisible("Portrait", true, false, false);
					CameraActivity.TraceLog(TAG,
							"***************portrait mode**************");
					break;
				case ASD_NIGHT:
					// if(sceneModes.contains(Parameters.SCENE_MODE_NIGHT)){
					// mParameters.setSceneMode(Parameters.SCENE_MODE_NIGHT);
					// }
					// if(isFirstDisplay_night){
					// mUI.setCurrentModeVisible(mAsdNightTitle,
					// true,true,true);
					// isFirstDisplay_night=false;
					// saveValue(CameraSettings.NIGHT_MODE_TOAST_ISFIRST_DISPLAY,
					// isFirstDisplay_night);
					// }else{
					mUI.setCurrentModeVisible(mAsdNightTitle, true, false,
							false);
					// }
					CameraActivity.TraceLog(TAG,
							"***************night mode**************");

					break;
				case ASD_BACKLIT:
					if (sceneModes.contains(Parameters.SCENE_MODE_HDR)) {
						mSceneMode = CameraUtil.SCENE_MODE_HDR;
						checkThread();
						mParameters.setSceneMode(Parameters.SCENE_MODE_HDR);
					}
					// if(isFirstDisplay_backlight){
					// mUI.setCurrentModeVisible(mAsdBacklightTitle,
					// true,true,true);
					// isFirstDisplay_backlight=false;
					// saveValue(CameraSettings.BACK_LIGHt_TOAST_ISFIRST_DISPLAY,
					// isFirstDisplay_backlight);
					// }else{
					mUI.setCurrentModeVisible(mAsdBacklightTitle, true, false,
							false);
					// }
					CameraActivity.TraceLog(TAG,
							"***************backlight mode**************");
					break;
				case ASD_GOURMET:
					mUI.setCurrentModeVisible("Gourmet", true, false, false);
					CameraActivity.TraceLog(TAG,
							"***************gourmet mode**************");
					break;
				}
				// PR 940085 - Soar Gao modify begin
				// Add for focus in moving
				if (mContinuousFocusSupported
						&& ApiHelper.HAS_AUTO_FOCUS_MOVE_CALLBACK) {
					updateAutoFocusMoveCallback();
				}
				// PR 940085 - Neo Skunkworks - Soar Gao - 001 end
				applyParametersToServer();
			}
		}
	}

	private class CameraOpenThread extends Thread {
		public void run() {
			long openCameraTimeBegin = System.currentTimeMillis();
			openCamera();
			long openCameraTimeEnd = System.currentTimeMillis();
			
			Log.d(TAG,"###YUHUAN###CameraOpenThread#open camera cost time=" + (openCameraTimeEnd - openCameraTimeBegin));
			
			long startPreviewTimeBegin = System.currentTimeMillis();
			startPreview();
			long startPreviewTimeEnd = System.currentTimeMillis();
			Log.d(TAG,"###YUHUAN###CameraOpenThread#start preview cost time=" + (startPreviewTimeEnd - startPreviewTimeBegin));
		}
	}

	private String mAsdAutoTitle;
	private String mAsdNightTitle;
	private String mAsdBacklightTitle;
	private Thread mCameraOpenThread;

	private ActivityManager mActivityManager;
	private ActivityManager.MemoryInfo mMemoryInfo;
	private Runtime mRuntime;

	@Override
	public void init(CameraActivity activity, View parent) {
		Log.d(TAG,"###YUHUAN###int PhotoModule ");
		mActivity = activity;
		mAsdAutoTitle = activity.getString(R.string.asd_auto);
		mAsdNightTitle = activity.getString(R.string.asd_night);
		mAsdBacklightTitle = activity.getString(R.string.asd_backlight);
		mGC = activity.getGC();

		mActivityManager = (ActivityManager) mActivity
				.getSystemService(Activity.ACTIVITY_SERVICE);
		mMemoryInfo = new ActivityManager.MemoryInfo();
		mRuntime = Runtime.getRuntime();

		mRootView = parent;
		mPreferences = new ComboPreferences(mActivity);
		CameraSettings.upgradeGlobalPreferences(mPreferences.getGlobal());
		mCameraId = getPreferredCameraId(mPreferences);
		// Add by Begin tiexing.xie PR856951 20141201,946277
		// if(!mActivity.gIsIntentParsed){
		if (GC.START_FRONT_CAMERA_INTENT.equals(mActivity.getCurIntent()
				.getAction())) {
			mCameraId = 1;
		} else if (GC.START_QRSCAN_INTENT.equals(mActivity.getCurIntent()
				.getAction())) {
			mCameraId = 0;
		}
		// }
		// mActivity.gIsIntentParsed=true;
		// Add by End tiexing.xie PR856951 20141201
		mActivity.setCameraId(mCameraId);
		/* [BUG-899418] lijuan.zhang add,20150115 begin */
		if (!mCameraViewRefresh)
			mActivity.getCameraViewManager().refresh();
		/* [BUG-899418] lijuan.zhang add,20150115 end */
		Log.d(TAG,"###YUHUAN###int#mCameraOpenThread= " + mCameraOpenThread);
		Log.d(TAG,"###YUHUAN###int#mActivity.mIsModuleSwitching= " + mActivity.mIsModuleSwitching);
		if (mCameraOpenThread == null && !mActivity.mIsModuleSwitching) {
			mCameraOpenThread = new CameraOpenThread();
			mCameraOpenThread.start();
		}

		mUI = new PhotoUI(activity, this, parent);
		mContentResolver = mActivity.getContentResolver();
		mShutterManager = mActivity.getShutterManager();
		mShutterManager.getShutterButton().setClickable(false);
		mTopViewManager = mActivity.getCameraViewManager().getTopViewManager();
		mZoomManager = mActivity.getCameraViewManager().getZoomManager();
		mSwitcherManager = mActivity.getCameraViewManager()
				.getSwitcherManager();
		if (mIsImageCaptureIntent) {
			mSwitcherManager.showSwitcher();
			mSwitcherManager.hideVideo();
		}
		mQrcodeViewManager = mActivity.getCameraViewManager()
				.getQrcodeViewManager();
		mCameraManualModeManager = mActivity.getCameraViewManager()
				.getCameraManualModeManager();
		mCameraManualModeManager
				.setManualModeCallBackListener(new CameraManualModeCallBackListener());
		mShutterManager.setOnShutterButtonListener(this);
		mTopViewManager.setListener(this);
		setCaptureListener(mCameraManualModeManager);

		// Surface texture is from camera screen nail and startPreview needs it.
		// This must be done before startPreview.
		mIsImageCaptureIntent = isImageCaptureIntent();
		mHelpGuideManager = mActivity.getCameraViewManager()
				.getHelpGuideManager();

		mPreferences.setLocalId(mActivity, mCameraId);
		CameraSettings.upgradeLocalPreferences(mPreferences.getLocal());
		// we need to reset exposure for the preview
		// modify by minghui.hua for PR918007
		// resetExposureCompensation();

		initializeControlByIntent();
		mQuickCapture = mActivity.getCurIntent().getBooleanExtra(
				EXTRA_QUICK_CAPTURE, false);
		mLocationManager = new LocationManager(mActivity, mUI);
		// mSensorManager =
		// (SensorManager)(mActivity.getSystemService(Context.SENSOR_SERVICE));
		// brightnessProgressBar =
		// (ProgressBar)mRootView.findViewById(R.id.progress);
		// if (brightnessProgressBar instanceof SeekBar) {
		// SeekBar seeker = (SeekBar) brightnessProgressBar;
		// seeker.setOnSeekBarChangeListener(mSeekListener);
		// }
		// brightnessProgressBar.setMax(MAXIMUM_BRIGHTNESS);
		// brightnessProgressBar.setProgress(mbrightness);
		// skinToneSeekBar = (SeekBar)
		// mRootView.findViewById(R.id.skintoneseek);
		// skinToneSeekBar.setOnSeekBarChangeListener(mskinToneSeekListener);
		// skinToneSeekBar.setVisibility(View.INVISIBLE);
		// Title = (TextView)mRootView.findViewById(R.id.skintonetitle);
		// RightValue = (TextView)mRootView.findViewById(R.id.skintoneright);
		// LeftValue = (TextView)mRootView.findViewById(R.id.skintoneleft);
		// [FEATURE]-Add BEGIN by TCTNB,xutao.wu, 2014-07-24, PR635962
		// LiveFilterInstanceHolder instanceHolder =
		// LiveFilterInstanceHolder.createInstance(activity);
		// instanceHolder.prepare();
		// [FEATURE]-Add END by TCTNB,xutao.wu, 2014-07-24, PR635962
		// / modify by yaping.liu for pr966435 {
		String savePathValue = mPreferences.getString(
				CameraSettings.KEY_CAMERA_SAVEPATH, "-1");
		Storage.setSaveSDCard(savePathValue.equals("1"),
				savePathValue.equals("-1"));
		// / }
		//initFacebeautySBValue();
	}

	private void loadBurstSound() {
		mBurstSound = new SoundPool(10, SoundClips.getAudioTypeForSoundPool(),
				0);
		// mSoundID = mBurstSound.load(
		// "/system/media/audio/ui/"
		// + SystemProperties.get(
		// "config.camera_burst_shot_sound",
		// "continuous_shot.ogg"), 1);
		/*
		 * load the continuous shot sound from the res/raw when new
		 * SoundPool,you must load the sound res again modify by minghui.hua for
		 * PR919156
		 */
		mSoundID = mBurstSound.load(mActivity, R.raw.continuous_shot, 1);
	}

	private void unloadBurstSound() {
		if (mBurstSound != null) {
			mBurstSound.unload(mSoundID);
			mBurstSound.release();
			mBurstSound = null;
			mSoundID = 0;
		}
	}

	private void initializeControlByIntent() {
		mUI.initializeControlByIntent();
		if (mIsImageCaptureIntent) {
			setupCaptureParams();
		}
	}

	private void onPreviewStarted() {
		setCameraState(IDLE);
		startFaceDetection();
		// if
		// (mActivity.getResources().getBoolean(R.bool.feature_gallery2_camera_setGeoTaggingOffByDefault_on))
		// {
		// offLocationFirstRun();
		// } else {
		// locationFirstRun();
		// }
		// [BUGFIX] Modify by jian.zhang1 for PR752979,PR750874 2014.08.07 Begin
		// locationFirstRun();
		// [BUGFIX] Modify by jian.zhang1 for PR752979,PR750874 2014.08.07 End
		// mHelpGuideManager.showManualModeHelpViewByCurrentMode(GC.MODE_PHOTO);
		if (locationFirstRunFlag) {
			locationFirstRun();
		}
	}

	// Prompt the user to pick to record location for the very first run of
	// camera only
	private void locationFirstRun() {
		if (RecordLocationPreference.isSet(mPreferences)) {
			return;
		}
		if (mActivity.isSecureCamera())
			return;
		// Check if the back camera exists
		int backCameraId = CameraHolder.instance().getBackCameraId();
		if (backCameraId == -1) {
			// If there is no back camera, do not show the prompt.
			return;
		}
		mUI.showLocationDialog();
		locationFirstRunFlag = false;
	}

	private void offLocationFirstRun() {
		setLocationPreference(RecordLocationPreference.VALUE_OFF);
	}

	@Override
	public void enableRecordingLocation(boolean enable) {
		CameraActivity.TraceLog(TAG, "enableRecordingLocation : enable = "
				+ enable);
		setGpsTagPreference(enable ? RecordLocationPreference.VALUE_ON
				: RecordLocationPreference.VALUE_OFF);
		setLocationPreference(enable ? RecordLocationPreference.VALUE_ON
				: RecordLocationPreference.VALUE_OFF);
	}

	@Override
	public void onPreviewUIReady() {
		if (!mIsPreviewStarted) {
			startPreview();
		}
	}

	@Override
	public void onPreviewUIDestroyed() {
		if (mCameraDevice == null) {
			return;
		}
		mCameraDevice.setPreviewTexture(null);
		stopPreview();
	}

	private void setLocationPreference(String value) {
		mPreferences.edit()
				.putString(CameraSettings.KEY_RECORD_LOCATION, value).apply();
		CameraActivity.TraceLog(TAG, "setLocationPreference : value = " + value
				+ "; isLocationOpened" + isLocationOpened);
		// TODO: Fix this to use the actual onSharedPreferencesChanged listener
		// instead of invoking manually
		if (value.equals(RecordLocationPreference.VALUE_ON) != isLocationOpened) {
			onSharedPreferenceChanged();
		}
	}

	/**
	 * init polaroid
	 */

	public void initPolaroid(boolean on) {
		if (on) {
			mUI.updatePreviewLayout(optimalSize.width, optimalSize.width, true);
			mActivity.resetDisplayForPolaroid();
		}
		mUI.initPolaroidView(on);
	}

	private void onCameraOpened() {
		Log.d(TAG,"###YUHUAN###onCameraOpened into ");
		View root = mUI.getRootView();
		// These depend on camera parameters.

		if (!mFirstTimeInitialized) {
			mHandler.sendEmptyMessage(FIRST_TIME_INIT);
		} else {
			initializeSecondTime();
		}

		int width = root.getWidth();
		int height = root.getHeight();
		mFocusManager.setPreviewSize(width, height);
		openCameraCommon();
		resizeForPreviewAspectRatio();
		if (mActivity.isNonePickIntent()
				&& mActivity.isInCamera()
				&& !mActivity.getCameraViewManager().getTopViewManager()
						.getOverallMenuManager().isShowing()) {
			mActivity.enableDrawer();
		}
		if (mShutterManager != null) {
			mShutterManager.enableShutter(true);
		}
		// [BUGFIX]-ADD-BEGIN by yongsheng.shan, PR-907303, 2015-01-26
		// if(mActivity != null){
		// mActivity.onCameraOpened();
		// }
		// [BUGFIX]-ADD-END by yongsheng.shan, PR-907303, 2015-01-26
	}

	//boolean needRecoverToFB = false;

	private void switchCamera() {
		/*
		if (mIsFaceBeauty) {
			needRecoverToFB = true;
		}*/
		if (mPaused)
			return;
		closeAllMode();
		Log.v(TAG, "Start to switch camera. id=" + mPendingSwitchCameraId);
		mCameraId = mPendingSwitchCameraId;
		mPendingSwitchCameraId = -1;
		setCameraId(mCameraId);

		mActivity.setViewState(GC.VIEW_STATE_CAMERA_SWITCH);
		// from onPause
		closeCamera();
		mUI.collapseCameraControls();
		mUI.clearFaces();
		// disableSkinToneSeekBar();
		if (mFocusManager != null)
			mFocusManager.removeMessages();

		// Restart the camera and initialize the UI. From onCreate.
		mPreferences.setLocalId(mActivity, mCameraId);
		CameraSettings.upgradeLocalPreferences(mPreferences.getLocal());
		mCameraDevice = CameraUtil.openCamera(mActivity, mCameraId, mHandler,
				mActivity.getCameraOpenErrorCallback());

		if (mCameraDevice == null) {
			Log.e(TAG, "Failed to open camera:" + mCameraId + ", aborting.");
			return;
		}
		mActivity.setCameraDevice(mCameraDevice);
		mParameters = mCameraDevice.getParameters();

		initializeCapabilities();
		CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];
		mMirror = (info.facing == CameraInfo.CAMERA_FACING_FRONT);
		mFocusManager.setMirror(mMirror);
		mFocusManager.setParameters(mInitialParams);
		setCameraParameters(UPDATE_PARAM_ALL);
		resizeForPreviewAspectRatio();
		setupPreview();
		// reset zoom value index
		mZoomValue = 0;

		openCameraCommon();

		// Start switch camera animation. Post a message because
		// onFrameAvailable from the old camera may already exist.
		mHandler.sendEmptyMessage(SWITCH_CAMERA_START_ANIMATION);
		mTopViewManager.setCurrentMode(mGC.getCurrentMode());
		mZoomManager.resetZoom();
		mGC.persoPictureSizeTitle();
		
		/*
		if (needRecoverToFB) {
			startFB();
			needRecoverToFB = false;
		}*/

	}

	protected void setCameraId(int cameraId) {
		mActivity.setCameraId(mCameraId);
		ListPreference pref = mPreferenceGroup
				.findPreference(CameraSettings.KEY_CAMERA_ID);
		pref.setValue("" + cameraId);
	}

	// either open a new camera or switch cameras
	private void openCameraCommon() {
		loadCameraPreferences();
		setCameraId(mCameraId);// PR942353-sichao.hu added
		mActivity.getCameraViewManager().refresh();
		mUI.onCameraOpened(mPreferenceGroup, mPreferences, mParameters, this);
		if (mIsImageCaptureIntent) {
			mUI.overrideSettings(CameraSettings.KEY_CAMERA_HDR_PLUS,
					mActivity.getString(R.string.setting_off_value));
		}
		updateCameraSettings();
		showTapToFocusToastIfNeeded();
		mCameraViewRefresh = true;// [BUG-899418]lijuan.zhang add,20151115
		// checkVideoQuality();//pr1005366-yuguan.chen removed for video quality
		// perso
	}

	// private final Object PERSO_LOCK=new Object();
	private void openCamera() {
		// We need to check whether the activity is paused before long
		// operations to ensure that onPause() can be done ASAP.
		if (mPaused) {
			return;
		}
		Log.v(TAG, "Open camera device.");
		CameraActivity.TraceQCTLog("OpenCameraDevice");
		mCameraDevice = CameraUtil.openCamera(mActivity, mCameraId, mHandler,
				mActivity.getCameraOpenErrorCallback());
		if (mCameraDevice == null) {
			Log.e(TAG, "Failed to open camera:" + mCameraId);
			mHandler.sendEmptyMessage(OPEN_CAMERA_FAIL);
			return;
		}
		mActivity.setCameraDevice(mCameraDevice);
		mParameters = mCameraDevice.getParameters();
		mCameraPreviewParamsReady = true;
		mInitialParams = mParameters;
		if (mFocusManager == null)
			initializeFocusManager();
		initializeCapabilities();
		mHandler.sendEmptyMessageDelayed(CAMERA_OPEN_DONE, 100);
		//initManualParameter();
		return;
	}

	// private boolean prepareCamera() {
	// // We need to check whether the activity is paused before long
	// // operations to ensure that onPause() can be done ASAP.
	// Log.v(TAG, "Open camera device.");
	// mCameraDevice = CameraUtil.openCamera(
	// mActivity, mCameraId, mHandler,
	// mActivity.getCameraOpenErrorCallback());
	// if (mCameraDevice == null) {
	// Log.e(TAG, "Failed to open camera:" + mCameraId);
	// return false;
	// }
	// mActivity.setCameraDevice(mCameraDevice);
	// mParameters = mCameraDevice.getParameters();
	// mPersoPictureSize = new PersoPictureSize(mGC, mCameraDevice, mActivity);
	// initializeCapabilities();
	// if (mFocusManager == null) initializeFocusManager();
	// setCameraParameters(UPDATE_PARAM_ALL);
	// mHandler.sendEmptyMessage(CAMERA_OPEN_DONE);
	// mCameraPreviewParamsReady = true;
	// startPreview();
	// mOnResumeTime = SystemClock.uptimeMillis();
	// checkDisplayRotation();
	// startPerso();
	// initManualParameter();
	// return true;
	// }

	private void checkVideoQuality() {
		// feiqiang.cheng add begin
		if (mCameraDevice.getParameters().getSupportedVideoSizes() != null) {
			GC mGC = mActivity.getGC();
			CharSequence[] entries = mGC.getListPreference(
					CameraSettings.KEY_VIDEO_QUALITY).getEntries();
			List<CharSequence> listEntries = new ArrayList<CharSequence>();
			CharSequence[] values = mGC.getListPreference(
					CameraSettings.KEY_VIDEO_QUALITY).getEntryValues();
			List<CharSequence> listValues = new ArrayList<CharSequence>();
			for (int i = 0; i < values.length; i++) {
				Log.i(TAG, "openCameraCommon values[" + i + "]=" + values[i]
						+ ",entries[" + i + "]=" + entries[i]);
				CamcorderProfile profile = CamcorderProfileEx.getProfile(
						mCameraId, Integer.valueOf(values[i].toString()));
				if (profile == null)
					continue;
				boolean bFound = false;
				for (Size s : mCameraDevice.getParameters()
						.getSupportedVideoSizes()) {
					Log.i(TAG, "openCameraCommon s.videoFrameWidth=" + s.width
							+ ",s.videoFrameHeight=" + s.height);
					if (s.width == profile.videoFrameWidth
							&& s.height == profile.videoFrameHeight) {
						bFound = true;
						break;
					}
				}
				if (!bFound && values[i].toString().equals("4")) {
					// profile = CamcorderProfileEx.getProfile(mCameraId, 10);
					bFound = true;
					values[i] = String.valueOf(CamcorderProfileEx
							.getQualityNum("QUALITY_VGA"));// pr1000285,1000388
															// yuguan.chen
															// modified.
					entries[i] = "VGA";
				}
				if (bFound) {
					listEntries.add(entries[i]);
					listValues.add(values[i]);
				}
			}
			// yuguan.chen added begin to guarantee list has data
			if (listValues.size() == 0) {
				listEntries.add("VGA");
				listValues.add(String.valueOf(CamcorderProfileEx
						.getQualityNum("QUALITY_VGA")));
			}
			// yuguan.chen added end to guarantee list has data
			// update list preference
			CharSequence[] entriesNew = new CharSequence[listEntries.size()];
			for (int i = 0; i < listEntries.size(); i++) {
				entriesNew[i] = listEntries.get(i);
				Log.i("cfq", "after entriesNew " + entriesNew[i]);
			}
			CharSequence[] valuesNew = new CharSequence[listValues.size()];
			for (int i = 0; i < listValues.size(); i++) {
				valuesNew[i] = listValues.get(i);
				Log.i("cfq", "after valuesNew " + valuesNew[i]);
			}
			mGC.getListPreference(CameraSettings.KEY_VIDEO_QUALITY).setEntries(
					entriesNew);
			mGC.getListPreference(CameraSettings.KEY_VIDEO_QUALITY)
					.setEntryValues(valuesNew);
			mGC.getListPreference(CameraSettings.KEY_VIDEO_QUALITY)
					.setDefaultValue(valuesNew[0].toString());
		}
	}

	@Override
	public void onScreenSizeChanged(int width, int height) {
		if (mFocusManager != null)
			mFocusManager.setPreviewSize(width, height);
	}

	@Override
	public void onPreviewRectChanged(Rect previewRect) {
		if (mFocusManager != null)
			mFocusManager.setPreviewRect(previewRect);
	}

	private void resetExposureCompensation() {
		String value = mPreferences.getString(CameraSettings.KEY_EXPOSURE,
				CameraSettings.EXPOSURE_DEFAULT_VALUE);
		if (!CameraSettings.EXPOSURE_DEFAULT_VALUE.equals(value)) {
			Editor editor = mPreferences.edit();
			editor.putString(CameraSettings.KEY_EXPOSURE, "0");
			editor.apply();
		}
	}

	void setPreviewFrameLayoutCameraOrientation() {
		CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];
		// if camera mount angle is 0 or 180, we want to resize preview
		if (info.orientation % 180 == 0) {
			mUI.cameraOrientationPreviewResize(true);
		} else {
			mUI.cameraOrientationPreviewResize(false);
		}
	}

	@Override
	public void resizeForPreviewAspectRatio() {
		CameraActivity.TraceLog(TAG, "resizeForPreview", new Throwable());
		setPreviewFrameLayoutCameraOrientation();
		Size size = mParameters.getPreviewSize();
		Log.e(TAG, "Width = " + size.width + "Height = " + size.height);
		mUI.setAspectRatio((float) size.width / size.height);
		// modify by minghui.hua for PR1004153
		if (mActivity.getGC().getCurrentMode() == GC.MODE_POLAROID) {
			mUI.updatePreviewLayout(size.width, size.width, true);
		} else {
			mUI.updatePreviewLayout(size.width, size.height, false);
		}

	}

	private void keepMediaProviderInstance() {
		// We want to keep a reference to MediaProvider in camera's lifecycle.
		// TODO: Utilize mMediaProviderClient instance to replace
		// ContentResolver calls.
		Log.d(TAG,"###YUHUAN###keepMediaProviderInstance#mMediaProviderClient=" + mMediaProviderClient);
		if (mMediaProviderClient == null) {
			mMediaProviderClient = mContentResolver
					.acquireContentProviderClient(MediaStore.AUTHORITY);
		}
	}

	// Snapshots can only be taken after this is called. It should be called
	// once only. We could have done these things in onCreate() but we want to
	// make preview screen appear as soon as possible.
	private void initializeFirstTime() {
		Log.d(TAG,"###YUHUAN###initializeFirstTime ");
		if (mFirstTimeInitialized || mPaused) {
			return;
		}

		// Initialize location service.
		boolean recordLocation = RecordLocationPreference.get(mPreferences,
				mContentResolver);
		CameraActivity.TraceLog(TAG,
				"initializeFirstTime() : recordLocation = " + recordLocation);
		recordLocation = checkLocationService(recordLocation);
		mLocationManager.recordLocation(recordLocation);

		keepMediaProviderInstance();

		mUI.initializeFirstTime();
		MediaSaveService s = mActivity.getMediaSaveService();
		// We set the listener only when both service and shutterbutton
		// are initialized.
		if (s != null) {
			s.setListener(this);
		}

		if (mNamedImages == null)
			mNamedImages = new NamedImages();
		// mGraphView = (GraphView)mRootView.findViewById(R.id.graph_view);
		if (mGraphView == null) {
			Log.e(TAG, "mGraphView is null");
		} else {
			mGraphView.setPhotoModuleObject(this);
		}

		mFirstTimeInitialized = true;
		Log.d(TAG, "addIdleHandler in first time initialization");
		addIdleHandler();
		loadQRCodeSound();
		loadBurstSound();
		mActivity.updateStorageSpaceAndHint();
	}

	// If the activity is paused and resumed, this method will be called in
	// onResume.
	private void initializeSecondTime() {
		Log.d(TAG,"###YUHUAN###initializeSecondTime ");
		// Start location update if needed.
		boolean recordLocation = RecordLocationPreference.get(mPreferences,
				mContentResolver);
		CameraActivity.TraceLog(TAG,
				"initializeSecondTime() : recordLocation = " + recordLocation);
		mLocationManager.recordLocation(recordLocation);
		recordLocation = checkLocationService(recordLocation);
		MediaSaveService s = mActivity.getMediaSaveService();
		if (s != null) {
			s.setListener(this);
		}
		if (mNamedImages == null)
			mNamedImages = new NamedImages();
		// Add by zhimin.yu for PR931912,PR942025, begin
		if (false == mGC.getCameraPreview() && isImageCaptureIntent()) {
			return;
		}
		// Add by zhimin.yu for PR931912,PR942025, end
		if (!mIsImageCaptureIntent
				&& !(GC.VIEW_STATE_SHUTTER_PRESS == mActivity.getViewState())) { // PR851347-lan.shan-001
																					// Add
			mSwitcherManager.showSwitcher();
		}
		if (mIsImageCaptureIntent) {
			mSwitcherManager.showSwitcher();
			mSwitcherManager.hideVideo();
		}
		mUI.initializeSecondTime(mParameters);
		keepMediaProviderInstance();
		loadQRCodeSound();
		loadBurstSound();
	}

	private void showTapToFocusToastIfNeeded() {
		// Show the tap to focus toast if this is the first start.
		if (mFocusAreaSupported
				&& mPreferences.getBoolean(
						CameraSettings.KEY_CAMERA_FIRST_USE_HINT_SHOWN, true)) {
			// Delay the toast for one second to wait for orientation.
			mHandler.sendEmptyMessageDelayed(SHOW_TAP_TO_FOCUS_TOAST, 1000);
		}
	}

	private void addIdleHandler() {
		if (mIdleHandler == null) {
			mIdleHandler = new MessageQueue.IdleHandler() {
				@Override
				public boolean queueIdle() {
					Storage.ensureOSXCompatible();
					return false;
				}
			};

			MessageQueue queue = Looper.myQueue();
			queue.addIdleHandler(mIdleHandler);
		}
	}

	private void removeIdleHandler() {
		if (mIdleHandler != null) {
			MessageQueue queue = Looper.myQueue();
			queue.removeIdleHandler(mIdleHandler);
			mIdleHandler = null;
		}
	}

	@Override
	public void startFaceDetection() {
		if (mFaceDetectionEnabled == false || mFaceDetectionStarted
				|| mCameraState != IDLE)
			return;
		if (mParameters.getMaxNumDetectedFaces() > 0) {
			mFaceDetectionStarted = true;
			CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];
			mUI.onStartFaceDetection(mDisplayOrientation,
					(info.facing == CameraInfo.CAMERA_FACING_FRONT));
			mCameraDevice.setFaceDetectionCallback(mHandler, mUI);
			mCameraDevice.startFaceDetection();
		}
	}

	@Override
	public void stopFaceDetection() {
		if (mFaceDetectionEnabled == false || !mFaceDetectionStarted)
			return;
		if (mParameters.getMaxNumDetectedFaces() > 0) {
			mFaceDetectionStarted = false;
			mCameraDevice.setFaceDetectionCallback(null, null);
			mCameraDevice.stopFaceDetection();
			mUI.clearFaces();
		}
	}

	private boolean mNeedResnapDuringLongshot = true;

	private final class LongshotShutterCallback implements
			CameraShutterCallback {

		@Override
		public void onShutter(CameraProxy camera) {
			mShutterCallbackTime = System.currentTimeMillis();
			mShutterLag = mShutterCallbackTime - mCaptureStartTime;
			if (!mNeedResnapDuringLongshot) {
				return;
			}
			Log.e(TAG, "[KPI Perf] PROFILE_SHUTTER_LAG mShutterLag = "
					+ mShutterLag + "ms");
			synchronized (mCameraDevice) {

				// [BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-26,PR770858 Begin
				// if (mCameraState != LONGSHOT) {
				if ((mReceivedSnapNum >= BURST_NUM_MAX)
						|| (mCameraState != LONGSHOT)) {
					// [BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-26,PR770858
					// End
					isBurstshotBreaked = true;
					return;
				}

				String defaultValue = CustomUtil.getInstance().getBoolean(
						CustomFields.DEF_TCTCAMERA_SHUTTER_SOUND_ON, true) ? "on"
						: "off";
				boolean isCaptureSoundEnable = mPreferences.getString(
						CameraSettings.KEY_SOUND, defaultValue).equals("on");

				mCameraDevice.getCamera().enableShutterSound(
						mLongpressFlag ? false : isCaptureSoundEnable);
				if (mLongshotSave) {
					mCameraDevice.takePicture(mHandler,
							new LongshotShutterCallback(), mRawPictureCallback,
							mPostViewPictureCallback,
							new LongshotPictureCallback(null));
				} else {
					mCameraDevice.takePicture(mHandler,
							new LongshotShutterCallback(), mRawPictureCallback,
							mPostViewPictureCallback, new JpegPictureCallback(
									null));
				}
			}
		}
	}

	private final class ShutterCallback implements CameraShutterCallback {

		private boolean mNeedsAnimation;

		public ShutterCallback(boolean needsAnimation) {
			mNeedsAnimation = needsAnimation;
		}

		@Override
		public void onShutter(CameraProxy camera) {
			mShutterCallbackTime = System.currentTimeMillis();
			mShutterLag = mShutterCallbackTime - mCaptureStartTime;
			Log.e(TAG, "[KPI Perf] PROFILE_SHUTTER_LAG mShutterLag = "
					+ mShutterLag + "ms");
			if (!mIsImageCaptureIntent
					&& ((mSceneMode == CameraUtil.SCENE_MODE_HDR)
							|| mASDShowingState == ASD_BACKLIT
							|| mASDShowingState == ASD_NIGHT)) {
				showProgress(mActivity
						.getString(R.string.pano_review_saving_indication_str));
				mShowProgress = true;
			}
			if (mNeedsAnimation) {
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						animateAfterShutter();
					}
				});
			}
		}
	}

	/*
	 * private final class StatsCallback implements
	 * android.hardware.Camera.CameraDataCallback {
	 * 
	 * @Override public void onCameraData(int [] data, android.hardware.Camera
	 * camera) { //if(!mPreviewing || !mHiston || !mFirstTimeInitialized){
	 * if(!mHiston || !mFirstTimeInitialized){ return; } /*The first element in
	 * the array stores max hist value . Stats data begin from second value
	 */
	/*
	 * synchronized(statsdata) {
	 * System.arraycopy(data,0,statsdata,0,STATS_DATA); }
	 * mActivity.runOnUiThread(new Runnable() { public void run() {
	 * if(mGraphView != null) mGraphView.PreviewChanged(); } }); } }
	 */
	private final class PostViewPictureCallback implements
			CameraPictureCallback {
		@Override
		public void onPictureTaken(byte[] data, CameraProxy camera) {
			mPostViewPictureCallbackTime = System.currentTimeMillis();
			Log.v(TAG, "mShutterToPostViewCallbackTime = "
					+ (mPostViewPictureCallbackTime - mShutterCallbackTime)
					+ "ms");
		}
	}

	private final class RawPictureCallback implements CameraPictureCallback {
		@Override
		public void onPictureTaken(byte[] rawData, CameraProxy camera) {
			mRawPictureCallbackTime = System.currentTimeMillis();
			Log.v(TAG, "mShutterToRawCallbackTime = "
					+ (mRawPictureCallbackTime - mShutterCallbackTime) + "ms");
		}
	}

	byte[][] mNightShotCache = new byte[CameraActivity.MAX_NIGHT_SHOT][];

	private final class LongshotPictureCallback implements
			CameraPictureCallback {
		Location mLocation;

		public LongshotPictureCallback(Location loc) {
			mLocation = loc;
		}

		@Override
		public void onPictureTaken(final byte[] jpegData, CameraProxy camera) {
			Log.i(TAG, "LMCH04232 play burst onPictureTaken");
			// [BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-15,PR752872 Begin
			/*
			 * if (mPaused || !mLongpressFlag || (jpegData == null) ||
			 * (mReceivedSnapNum > mCurrentShowShotsNum)) { // real > show
			 * number, enough.
			 */

			Log.d(TAG,"###YUHUAN###onPictureTaken#mIsFromBurstShot=" + mIsFromBurstShot);
			if (mIsFromBurstShot) {
				Log.d(TAG,"###YUHUAN###onPictureTaken#mPaused=" + mPaused);
				Log.d(TAG,"###YUHUAN###onPictureTaken#mLongpressFlag=" + mLongpressFlag);
				Log.d(TAG,"###YUHUAN###onPictureTaken#jpegData=" + jpegData);
				if (mPaused || !mLongpressFlag || (jpegData == null)
						|| mReceivedSnapNum >= BURST_NUM_MAX) { // real > show
																// number,
																// enough.
					// [BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-15,PR752872
					// End
					return;
				}
			}
			mReceivedSnapNum++;
			Log.d(TAG,"###YUHUAN###onPictureTaken#mReceivedSnapNum=" + mReceivedSnapNum);
			mCapturedTotalSize += jpegData.length;
			Log.i(TAG, "LMCH04232 play burst mReceivedSnapNum:"
					+ mReceivedSnapNum);

			if (mReceivedSnapNum == 1) {
				if (mIsFromBurstShot) {
					mBurstshotSoundPlayThread = new BurstshotSoundplayThread();
					mBurstshotSoundPlayThread.start();
				}
				
				Log.d(TAG,"###YUHUAN###onPictureTaken#mNeedResnapDuringLongshot=" + mNeedResnapDuringLongshot);
				if (!mNeedResnapDuringLongshot) {
					setupPreview();
					try {
						Thread.sleep(600);
					} catch (Exception e) {
						Log.e(TAG,
								"shutterbuttonlongpressed, thread sleep error "
										+ e);
					}
				}
			}
			if (mReceivedSnapNum == BURST_NUM_MAX) {
				mHandler.sendEmptyMessage(SHOW_BURST_NUM);
			}
			mFocusManager.updateFocusUI(); // Ensure focus indicator is hidden.

			ExifInterface exif = Exif.getExif(jpegData);
			int orientation = Exif.getOrientation(exif);

			// Burst snapshot. Generate new image name.
			if (mReceivedSnapNum > 1)
				mNamedImages.nameNewImage(mCaptureStartTime);

			// Calculate the width and the height of the jpeg.
			Size s = mParameters.getPictureSize();
			int width, height;
			if ((mJpegRotation + orientation) % 180 == 0) {
				width = s.width;
				height = s.height;
			} else {
				width = s.height;
				height = s.width;
			}

			String pictureFormat = mParameters.get(KEY_PICTURE_FORMAT);
			Log.d(TAG,"###YUHUAN###onPictureTaken#pictureFormat=" + pictureFormat);
			
			if (pictureFormat != null
					&& !pictureFormat.equalsIgnoreCase(PIXEL_FORMAT_JPEG)) {
				// overwrite width and height if raw picture
				String pair = mParameters.get(KEY_QC_RAW_PICUTRE_SIZE);
				if (pair != null) {
					int pos = pair.indexOf('x');
					if (pos != -1) {
						width = Integer.parseInt(pair.substring(0, pos));
						height = Integer.parseInt(pair.substring(pos + 1));
					}
				}
			}
			// [BUGFIX]-Add-BEGIN by TCTNJ.qiannan.wang,08/19/2014,738934,
			// [Gallery]Photo details are incorrect
			setTctExifInfo(exif);
			// [BUGFIX]-Add-END by TCTNJ.qiannan.wang
			NamedEntity name = mNamedImages.getNextNameEntity();
			String title = (name == null) ? null : name.title;
			long date = (name == null) ? -1 : name.date;

			if (title == null) {
				Log.e(TAG, "Unbalanced name/data pair");

			} else {
				if (date == -1)
					date = mCaptureStartTime;
				if (mHeading >= 0) {
					// heading direction has been updated by the sensor.
					ExifTag directionRefTag = exif.buildTag(
							ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
							ExifInterface.GpsTrackRef.MAGNETIC_DIRECTION);
					ExifTag directionTag = exif.buildTag(
							ExifInterface.TAG_GPS_IMG_DIRECTION, new Rational(
									mHeading, 1));
					exif.setTag(directionRefTag);
					exif.setTag(directionTag);
				}

				// [BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-15,PR752872 Begin
				if (isBurstshotBreaked) {
					exif = null;
					return;
				}
				// [BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-15,PR752872 End

				String mPictureFormat = mParameters.get(KEY_PICTURE_FORMAT);
				mActivity.getMediaSaveService().addImage4BurstShot(jpegData,
						title, date, mLocation, width, height, orientation,
						exif, mOnMediaSavedListener, mContentResolver,
						mPictureFormat);
			}
		}
	}

	private final class JpegPictureCallback implements CameraPictureCallback {
		Location mLocation;

		public JpegPictureCallback(Location loc) {
			mLocation = loc;
		}

		@Override
		public void onPictureTaken(final byte[] jpegData, CameraProxy camera) {
			Log.d(TAG,"###YUHUAN###onPictureTaken#mPaused=" + mPaused);
			if (mPaused) {
				return;
			}

			Log.d(TAG,"###YUHUAN###onPictureTaken#mJPEGCallbackCount=" + mJPEGCallbackCount);
			Log.d(TAG,"###YUHUAN###onPictureTaken#mNightShotCurrentMax=" + mNightShotCurrentMax);
			if (++mJPEGCallbackCount < mNightShotCurrentMax) {
				return;
			}

			mJPEGCallbackCount = 0;
			isInCapture = false;
			
			Log.d(TAG,"###YUHUAN###onPictureTaken#mIsImageCaptureIntent=" + mIsImageCaptureIntent);
			if (mIsImageCaptureIntent) {
				stopPreview();
			}
			// if (mSceneMode ==
			// CameraUtil.SCENE_MODE_HDR||mASDShowingState==backlit||mASDShowingState==night)
			// {
			if (mSceneMode == CameraUtil.SCENE_MODE_HDR
					|| mASDShowingState == ASD_BACKLIT
					|| mASDShowingState == ASD_NIGHT) {
				mActivity.enableDrawer();
			}
			mUI.showSwitcher();
			mUI.setSwipingEnabled(true);
			mActivity.getCameraViewManager().getRotateProgress().hide();
			mShowProgress = false;
			// }

			mReceivedSnapNum = mReceivedSnapNum + 1;
			mJpegPictureCallbackTime = System.currentTimeMillis();
			/*
			 * if(mSnapshotMode == CameraInfo.CAMERA_SUPPORT_MODE_ZSL) {
			 * Log.v(TAG, "JpegPictureCallback : in zslmode"); mParameters =
			 * mCameraDevice.getParameters(); mBurstSnapNum =
			 * mParameters.getInt("num-snaps-per-shutter"); }
			 */
			Log.v(TAG, "JpegPictureCallback: Received = " + mReceivedSnapNum
					+ "Burst count = " + mBurstSnapNum);
			// If postview callback has arrived, the captured image is displayed
			// in postview callback. If not, the captured image is displayed in
			// raw picture callback.
			if (mPostViewPictureCallbackTime != 0) {
				mShutterToPictureDisplayedTime = mPostViewPictureCallbackTime
						- mShutterCallbackTime;
				mPictureDisplayedToJpegCallbackTime = mJpegPictureCallbackTime
						- mPostViewPictureCallbackTime;
			} else {
				mShutterToPictureDisplayedTime = mRawPictureCallbackTime
						- mShutterCallbackTime;
				mPictureDisplayedToJpegCallbackTime = mJpegPictureCallbackTime
						- mRawPictureCallbackTime;
			}
			Log.v(TAG, "mPictureDisplayedToJpegCallbackTime = "
					+ mPictureDisplayedToJpegCallbackTime + "ms");

			boolean needRestartPreview = !mIsImageCaptureIntent
					&& !mPreviewRestartSupport && (mCameraState != LONGSHOT)
					&& (mSnapshotMode != CAMERA_SUPPORT_MODE_ZSL) // ZSL
																	// improvement
																	// by
																	// jpxiong
					&& (mReceivedSnapNum == mBurstSnapNum);
			if (needRestartPreview) {
				setupPreview();
			} else if ((mReceivedSnapNum == mBurstSnapNum)
					&& (mCameraState != LONGSHOT)) {
				mFocusManager.resetTouchFocus();
				if (CameraUtil.FOCUS_MODE_CONTINUOUS_PICTURE
						.equals(mFocusManager.getFocusMode())) {
					mCameraDevice.cancelAutoFocus();
					startFaceDetection();
				}
				mUI.resumeFaceDetection();
				setCameraState(IDLE);
				isInSmileSnap = false;
			}

			mFocusManager.updateFocusUI(); // Ensure focus indicator is hidden.
			ExifInterface exif = Exif.getExif(jpegData);
			int orientation = Exif.getOrientation(exif);

			if (mActivity.getGC().getCurrentMode() == GC.MODE_POLAROID) {
				mUI.countForPolaroid();
				mUI.showCapturedImageForPolaroid(jpegData,
						mActivity.getOrientation(), mMirror);
				mActivity.setViewState(GC.VIEW_STATE_NORMAL);
				return;
			}

			if (!mIsImageCaptureIntent) {
				// Burst snapshot. Generate new image name.
				if (mReceivedSnapNum > 1)
					mNamedImages.nameNewImage(mCaptureStartTime);

				// Calculate the width and the height of the jpeg.
				Size s = mParameters.getPictureSize();
				int width, height;
				if ((mJpegRotation + orientation) % 180 == 0) {
					width = s.width;
					height = s.height;
				} else {
					width = s.height;
					height = s.width;
				}

				String pictureFormat = mParameters.get(KEY_PICTURE_FORMAT);
				if (pictureFormat != null
						&& !pictureFormat.equalsIgnoreCase(PIXEL_FORMAT_JPEG)) {
					// overwrite width and height if raw picture
					String pair = mParameters.get(KEY_QC_RAW_PICUTRE_SIZE);
					if (pair != null) {
						int pos = pair.indexOf('x');
						if (pos != -1) {
							width = Integer.parseInt(pair.substring(0, pos));
							height = Integer.parseInt(pair.substring(pos + 1));
						}
					}
				}
				// [BUGFIX]-Add-BEGIN by TCTNJ.qiannan.wang,08/19/2014,738934,
				// [Gallery]Photo details are incorrect
				setTctExifInfo(exif);
				// [BUGFIX]-Add-END by TCTNJ.qiannan.wang
				NamedEntity name = mNamedImages.getNextNameEntity();
				String title = (name == null) ? null : name.title;
				long date = (name == null) ? -1 : name.date;

				// Handle debug mode outputs
				if (mDebugUri != null) {
					// If using a debug uri, save jpeg there.
					saveToDebugUri(jpegData);

					// Adjust the title of the debug image shown in mediastore.
					if (title != null) {
						title = DEBUG_IMAGE_PREFIX + title;
					}
				}

				if (title == null) {
					Log.e(TAG, "Unbalanced name/data pair");
				} else {
					if (date == -1)
						date = mCaptureStartTime;
					if (mHeading >= 0) {
						// heading direction has been updated by the sensor.
						ExifTag directionRefTag = exif.buildTag(
								ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
								ExifInterface.GpsTrackRef.MAGNETIC_DIRECTION);
						ExifTag directionTag = exif.buildTag(
								ExifInterface.TAG_GPS_IMG_DIRECTION,
								new Rational(mHeading, 1));
						exif.setTag(directionRefTag);
						exif.setTag(directionTag);
					}

					String mPictureFormat = mParameters.get(KEY_PICTURE_FORMAT);
					if (mCaptureListener != null)
						mCaptureListener.onCaptureDone();
					if (GC.MODE_EXPRESSION4 != mGC.getCurrentMode()) {
						mActivity.getMediaSaveService().addImage(jpegData,
								title, date, mLocation, width, height,
								orientation, exif, mOnMediaSavedListener,
								mContentResolver, mPictureFormat);
						// [BUGFIX]-Add by TCTNB,bin.zhang2, 2014-06-18,PR706182
						// Begin
						// mActivity.setViewState(GC.VIEW_STATE_NORMAL); // ZSL
						// improvement by jpxiong
						// [BUGFIX]-Add by TCTNB,bin.zhang2, 2014-06-18,PR706182
						// End
					} else {
						mActivity.getCameraViewManager()
								.showExpressionThumbnail();
						// Bitmap bitmap = CameraUtil.makeBitmap(jpegData, width
						// * height / 4);
						// // Calculate the orientation to show thumbnail image
						// with portrait.
						// // NOTE:The image with this orientation is different
						// to the real captured image.
						// int orien = (mJpegRotation + 90) % 360;

						// bitmap = CameraUtil.rotate(bitmap, orien);
						// // Here use the portrait image to generate the final
						// image first,
						// // and rotate the final image to landscape/portrait.
						// ExpressionThread.onPictureTaken(bitmap, width,
						// height);
						// mActivity.getCameraViewManager().refreshExpressionThumbnail(bitmap);
						if (ExpressionThread.isTakePictureJobDone()
								&& ExpressionThread.canGenerateFinalPicture()) {
							Log.d(TAG,
									"zsy1031 ExpressionThread.isExpressionJobDone()");
							int[] order = mActivity.getCameraViewManager()
									.getExpressionThumbnailManager()
									.getImageOrder();
							byte[] jpegDataExp = ExpressionThread
									.getFinalImage(order);
							if (jpegDataExp == null)
								Log.e(TAG,
										"zsy1031 onPictureTaken:Generate Expression4 final image Error: null pointer");
							ExifInterface exifi = Exif.getExif(jpegDataExp);
							int ori = Exif.getOrientation(exifi);
							if (mHeading >= 0) {
								// heading direction has been updated by the
								// sensor.
								ExifTag directionRefTag = exif
										.buildTag(
												ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
												ExifInterface.GpsTrackRef.MAGNETIC_DIRECTION);
								ExifTag directionTag = exifi.buildTag(
										ExifInterface.TAG_GPS_IMG_DIRECTION,
										new Rational(mHeading, 1));
								exifi.setTag(directionRefTag);
								exifi.setTag(directionTag);
							}
							mNamedImages.nameNewImage(mCaptureStartTime);
							mActivity.getMediaSaveService().addImage(
									jpegDataExp, title, date, mLocation, width,
									height, orientation, exifi,
									mOnMediaSavedListener, mContentResolver,
									mPictureFormat);
							Log.e("746780", "mActivity.gotoGallery");
							mActivity.gotoGallery();
							mActivity.setViewState(GC.VIEW_STATE_NORMAL);
							mGC.setExpressionState(false);
							mActivity.getCameraViewManager()
									.hideExpressionThumbnail();
							mShutterManager.getShutterButton()
									.setImageResource(
											R.drawable.btn_shutter_expression4);
						}
					}

					// smile mode detect & capture loop.
					if (GC.MODE_SMILE == mGC.getCurrentMode()) {
						mHandler.sendEmptyMessageDelayed(START_SMILE_DETECT,
								1000);
					}

				}
				// Animate capture with real jpeg data instead of a preview
				// frame.
				if (mCameraState != LONGSHOT) {
					// int cap_anim_num=mPreferences.getInt("cap_animation", 0);
					// mPreferences.edit().putInt("cap_animation",
					// cap_anim_num+1).apply();
					// if(cap_anim_num<10){
					// mUI.animateCapture(jpegData, mOrientation, mMirror); //
					// ZSL improvement by jpxiong
					// }
				}
				if (mGC.getCurrentMode() != GC.MODE_EXPRESSION4) {
					mShutterManager.enableShutter(true);
					mActivity.setViewState(GC.VIEW_STATE_NORMAL);
				}
			} else {
				// [BUGFIX]-Del by TCTNB,shidong.hou, 2014-08-18,PR753472 Begin
				// mShutterManager.enableShutter(true);
				// [BUGFIX]-Del by TCTNB,shidong.hou, 2014-08-18,PR753472 End
				mJpegImageData = jpegData;
				if (!mQuickCapture) {
					mShutterManager.hide();
					// Add by zhimin.yu for PR931912,PR942025
					mGC.setCameraPreview(false);
					mUI.showCapturedImageForReview(jpegData, orientation,
							mMirror);
				} else {
					onCaptureDone();
				}
			}

			// Check this in advance of each shot so we don't add to shutter
			// latency. It's true that someone else could write to the SD card
			// in
			// the mean time and fill it, but that could have happened between
			// the
			// shutter press and saving the JPEG too.
			mActivity.updateStorageSpaceAndHint();

			long now = System.currentTimeMillis();
			mJpegCallbackFinishTime = now - mJpegPictureCallbackTime;
			Log.v(TAG, "mJpegCallbackFinishTime = " + mJpegCallbackFinishTime
					+ "ms");

			if (mReceivedSnapNum == mBurstSnapNum)
				mJpegPictureCallbackTime = 0;

			/*
			 * if (mHiston && (mSnapshotMode
			 * ==CameraInfo.CAMERA_SUPPORT_MODE_ZSL)) {
			 * mActivity.runOnUiThread(new Runnable() { public void run() { if
			 * (mGraphView != null) { mGraphView.setVisibility(View.VISIBLE);
			 * mGraphView.PreviewChanged(); } } }); } if (mSnapshotMode ==
			 * CameraInfo.CAMERA_SUPPORT_MODE_ZSL && mCameraState != LONGSHOT &&
			 * mReceivedSnapNum == mBurstSnapNum) { cancelAutoFocus(); }
			 */
			// mUI.updatePreviewLayout(1920, 1080);
		}
	}

	private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
		public void onStartTrackingTouch(SeekBar bar) {
			// no support
		}

		public void onProgressChanged(SeekBar bar, int progress,
				boolean fromtouch) {
		}

		public void onStopTrackingTouch(SeekBar bar) {
		}
	};

	private OnSeekBarChangeListener mskinToneSeekListener = new OnSeekBarChangeListener() {
		public void onStartTrackingTouch(SeekBar bar) {
			// no support
		}

		public void onProgressChanged(SeekBar bar, int progress,
				boolean fromtouch) {
			int value = (progress + MIN_SCE_FACTOR) * SCE_FACTOR_STEP;
			if (progress > (MAX_SCE_FACTOR - MIN_SCE_FACTOR) / 2) {
				RightValue.setText(String.valueOf(value));
				LeftValue.setText("");
			} else if (progress < (MAX_SCE_FACTOR - MIN_SCE_FACTOR) / 2) {
				LeftValue.setText(String.valueOf(value));
				RightValue.setText("");
			} else {
				LeftValue.setText("");
				RightValue.setText("");
			}
			if (value != mskinToneValue && mCameraDevice != null) {
				mskinToneValue = value;
				Message msg = mHandler.obtainMessage(
						CONFIGURE_SKIN_TONE_FACTOR, mskinToneValue, 0);
				mHandler.sendMessage(msg);
			}
		}

		
		public void onStopTrackingTouch(SeekBar bar) {
			/*
			Log.v(TAG, "Set onStopTrackingTouch mskinToneValue = "
					+ mskinToneValue);
			Editor editor = mPreferences.edit();
			editor.putString(CameraSettings.KEY_SKIN_TONE_ENHANCEMENT_FACTOR,
					Integer.toString(mskinToneValue));
			editor.apply();
			*/
		}
	};

	private final class AutoFocusCallback implements CameraAFCallback {
		@Override
		public void onAutoFocus(boolean focused, CameraProxy camera) {
			if (mPaused)
				return;

			CameraActivity.TraceLog("QCT", "TAF State is " + focused);
			mAutoFocusTime = System.currentTimeMillis() - mFocusStartTime;
			Log.v(TAG, "mAutoFocusTime = " + mAutoFocusTime + "ms");
			if (mCameraState != PhotoController.LONGSHOT)
				setCameraState(IDLE);
			mFocusManager.onAutoFocus(focused, mUI.isShutterPressed());
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private final class AutoFocusMoveCallback implements CameraAFMoveCallback {
		@Override
		public void onAutoFocusMoving(boolean moving, CameraProxy camera) {
			CameraActivity.TraceLog("QCT", "CAF State is " + moving);
			if (mParameters != null) {
				if (!mParameters.getFocusMode().equals(
						Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
					mFocusManager.onAutoFocusMoving(false);
				}
			}
			mFocusManager.onAutoFocusMoving(moving);
		}
	}

	/**
	 * This class is just a thread-safe queue for name,date holder objects.
	 */
	public static class NamedImages {
		private Vector<NamedEntity> mQueue;

		public NamedImages() {
			mQueue = new Vector<NamedEntity>();
		}

		public void nameNewImage(long date) {
			NamedEntity r = new NamedEntity();
			r.title = CameraUtil.createJpegName(date);
			r.date = date;
			mQueue.add(r);
		}

		public NamedEntity getNextNameEntity() {
			synchronized (mQueue) {
				if (!mQueue.isEmpty()) {
					return mQueue.remove(0);
				}
			}
			return null;
		}

		public static class NamedEntity {
			public String title;
			public long date;
		}
	}

	// [BUGFIX]-Add-BEGIN by TCTNJ.qiannan.wang,08/19/2014,738934,
	// [Gallery]Photo details are incorrect
	private void setTctExifInfo(ExifInterface exif) {
		boolean captureModeEnable = CustomUtil.getInstance().getBoolean(
				CustomFields.DEF_CAPTURE_MODE_EXIF_ENABLE, false);
		if (captureModeEnable && exif != null && mGC != null) {
			CameraUtil.setCaptureModeExifInfo(exif, mGC.getCurrentMode());
		}
		// String maker = SystemProperties.get("ro.product.maker", "");
		// if(TextUtils.isEmpty(maker)){
		// maker = SystemProperties.get("ro.product.brand", "");
		// }
		// String model = SystemProperties.get("ro.product.model", "");
		// ExifTag tctmake = exif.buildTag(ExifInterface.TAG_MAKE,maker);
		// ExifTag tctmodel = exif.buildTag(ExifInterface.TAG_MODEL,model);
		// exif.setTag(tctmake);
		// exif.setTag(tctmodel);
	}

	// [BUGFIX]-Add-END by TCTNJ.qiannan.wang
	private void setCameraState(int state) {
		mCameraState = state;
		switch (state) {
		case PhotoController.PREVIEW_STOPPED:
		case PhotoController.SNAPSHOT_IN_PROGRESS:
		case PhotoController.LONGSHOT:
		case PhotoController.SWITCHING_CAMERA:
			mUI.enableGestures(false);
			break;
		case PhotoController.IDLE:
			mUI.enableGestures(true);
			break;
		}
	}

	/*
	 * [BUG-794522] When rendering HDR image show an activity
	 * circle,lijuan.zhang added 20141114 begin
	 */
	private void showProgress(String msg) {
		mActivity.getCameraViewManager().getRotateProgress()
				.showProgress(msg, mOrientation);
	}

	/*
	 * [BUG-794522] When rendering HDR image show an activity
	 * circle,lijuan.zhang added 20141114 end
	 */

	private void animateAfterShutter() {
		// Only animate when in full screen capture mode
		// i.e. If monkey/a user swipes to the gallery during picture taking,
		// don't show animation
		Log.d(TAG,"###YUHUAN###animateAfterShutter#mIsImageCaptureIntent=" + mIsImageCaptureIntent);
		if (!mIsImageCaptureIntent) {
			mUI.animateFlash();
		}
	}

	private boolean isInCapture = false;

	public boolean getIsInCapture() {
		return isInCapture;
	}

	public boolean getIsInCountDown() {
		return mUI.isCountingDown();
	}

	@Override
	public boolean capture() {
		// If we are already in the middle of taking a snapshot or the image
		// save request
		// is full then ignore.
		Log.d(TAG, "capture start");
		if (mCameraDevice == null || mCameraState == SNAPSHOT_IN_PROGRESS
				|| mCameraState == SWITCHING_CAMERA
				|| mActivity.getMediaSaveService() == null
				|| mActivity.getMediaSaveService().isQueueFull()) {
			return false;
		}
		mCaptureStartTime = System.currentTimeMillis();
		mPostViewPictureCallbackTime = 0;
		mJpegImageData = null;

		// mShutterManager.enableShutter(true);
		final boolean animateBefore = (mSceneMode == CameraUtil.SCENE_MODE_HDR
				|| mASDShowingState == ASD_BACKLIT || mASDShowingState == ASD_NIGHT);
		if (mHiston) {
			/*
			 * if (mSnapshotMode != CameraInfo.CAMERA_SUPPORT_MODE_ZSL) {
			 * mHiston = false; mCameraDevice.setHistogramMode(null); }
			 */
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					if (mGraphView != null)
						mGraphView.setVisibility(View.INVISIBLE);
				}
			});
		}

		// huanglin 20150212 for PR928833
		// if (animateBefore) {
		// animateAfterShutter();
		// }
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				if (animateBefore) {
					animateAfterShutter();
				}
			}
		});

		// [BUGFIX]-MOD-BEGIN by yongsheng.shan, PR-888347, 2015-01-19

		checkThread();
		
		mParameters.set("burst-num", BURST_NUM_MAX);
		mNightShotCurrentMax = 1;
		
		// yuhuan 20150710 set hdr parameter ===begin
		/*if (mGC.getCurrentMode() == GC.MODE_HDR) {
			mParameters.set("cap-mode", "hdr");
		}*/
		// yuhuan 20150710 set hdr parameter ===end
		
		applyParametersToServer();
		// [BUGFIX]-MOD-END by yongsheng.shan, PR-888347, 2015-01-19
		// Set rotation and gps data.
		int orientation;
		// We need to be consistent with the framework orientation (i.e. the
		// orientation of the UI.) when the auto-rotate screen setting is on.
		if (mActivity.isAutoRotateScreen()) {
			orientation = (360 - mDisplayRotation) % 360;
		} else {
			orientation = mOrientation;
		}
		mJpegRotation = CameraUtil.getJpegRotation(mCameraId, orientation);
		mParameters.setRotation(mJpegRotation);
		String pictureFormat = mParameters.get(KEY_PICTURE_FORMAT);
		Location loc = null;
		if (pictureFormat != null
				&& PIXEL_FORMAT_JPEG.equalsIgnoreCase(pictureFormat)) {
			loc = mLocationManager.getCurrentLocation();
			mLocation = loc;// PR900096,v-nj-feiqiang.cheng
		}
		CameraUtil.setGpsParameters(mParameters, loc);
		mCameraDevice.setParameters(mParameters);
		mParameters = mCameraDevice.getParameters();

		// mBurstSnapNum = mParameters.getInt("num-snaps-per-shutter");
		mReceivedSnapNum = 0;
		mPreviewRestartSupport = false;
		// mPreviewRestartSupport = SystemProperties.getBoolean(
		// PERSIST_PREVIEW_RESTART, false);
		mPreviewRestartSupport &= CameraSettings
				.isInternalPreviewSupported(mParameters);
		mPreviewRestartSupport &= (mBurstSnapNum == 1);
		mPreviewRestartSupport &= PIXEL_FORMAT_JPEG
				.equalsIgnoreCase(pictureFormat);

		// We don't want user to press the button again while taking a
		// multi-second HDR photo.
		// mShutterManager.enableShutter(false);
		mActivity.setViewState(GC.VIEW_STATE_SHUTTER_PRESS);

		if (mCaptureListener != null)
			mCaptureListener.onCaptureStart();

		isInCapture = true;
		if (mCameraState == LONGSHOT) {
			if (mLongshotSave) {
				mCameraDevice.takePicture(mHandler,
						new LongshotShutterCallback(), mRawPictureCallback,
						mPostViewPictureCallback, new LongshotPictureCallback(
								loc));
			} else {
				mCameraDevice.takePicture(mHandler,
						new LongshotShutterCallback(), mRawPictureCallback,
						mPostViewPictureCallback, new JpegPictureCallback(loc));
			}
		} else {
			mCameraDevice.takePicture(mHandler, new ShutterCallback(
					!animateBefore), mRawPictureCallback,
					mPostViewPictureCallback, new JpegPictureCallback(loc));
			setCameraState(SNAPSHOT_IN_PROGRESS);
		}

		if (mNamedImages == null) {
			mNamedImages = new NamedImages();
		}
		mNamedImages.nameNewImage(mCaptureStartTime);
		if (mSnapshotMode != CAMERA_SUPPORT_MODE_ZSL) {
			mFaceDetectionStarted = false;
		}
		UsageStatistics
				.onEvent(
						UsageStatistics.COMPONENT_CAMERA,
						UsageStatistics.ACTION_CAPTURE_DONE,
						"Photo",
						0,
						UsageStatistics.hashFileName(mNamedImages.mQueue
								.lastElement().title + ".jpg"));
		return true;
	}

	@Override
	public void setFocusParameters() {
		setCameraParameters(UPDATE_PARAM_PREFERENCE);
	}

	private int getPreferredCameraId(ComboPreferences preferences) {
		int intentCameraId = CameraUtil.getCameraFacingIntentExtras(mActivity);
		if (intentCameraId != -1) {
			// Testing purpose. Launch a specific camera through the intent
			// extras.
			return intentCameraId;
		} else {
			return CameraSettings.readPreferredCameraId(preferences);
		}
	}

	private void updateCameraSettings() {
		String sceneMode = null;
		String flashMode = null;
		String redeyeReduction = null;
		String aeBracketing = null;
		String focusMode = null;
		String colorEfect = null;
		String exposureCompensation = null;
		String touchAfAec = null;

		String ubiFocusOn = mActivity
				.getString(R.string.pref_camera_advanced_feature_value_ubifocus_on);
		String chromaFlashOn = mActivity
				.getString(R.string.pref_camera_advanced_feature_value_chromaflash_on);
		String optiZoomOn = mActivity
				.getString(R.string.pref_camera_advanced_feature_value_optizoom_on);
		/*String optiZoom = mParameters.get(CameraSettings.KEY_QC_OPTI_ZOOM);
		String chromaFlash = mParameters
				.get(CameraSettings.KEY_QC_CHROMA_FLASH);
		String ubiFocus = mParameters.get(CameraSettings.KEY_QC_AF_BRACKETING);

		if ((ubiFocus != null && ubiFocus.equals(ubiFocusOn))
				|| (chromaFlash != null && chromaFlash.equals(chromaFlashOn))
				|| (optiZoom != null && optiZoom.equals(optiZoomOn))) {
			mSceneMode = sceneMode = Parameters.SCENE_MODE_AUTO;
			flashMode = Parameters.FLASH_MODE_OFF;
			focusMode = Parameters.FOCUS_MODE_INFINITY;
			redeyeReduction = mActivity
					.getString(R.string.pref_camera_redeyereduction_entry_disable);
			aeBracketing = mActivity
					.getString(R.string.pref_camera_ae_bracket_hdr_entry_off);
			colorEfect = mActivity
					.getString(R.string.pref_camera_coloreffect_default);
			exposureCompensation = CameraSettings.EXPOSURE_DEFAULT_VALUE;

			overrideCameraSettings(flashMode, null, focusMode,
					exposureCompensation, touchAfAec, null, null, null, null,
					colorEfect, sceneMode, redeyeReduction, aeBracketing);
		}
		*/

		// If scene mode is set, for flash mode, white balance and focus mode
		// read settings from preferences so we retain user preferences.
		if (!Parameters.SCENE_MODE_AUTO.equals(mSceneMode)) {
			flashMode = mPreferences
					.getString(CameraSettings.KEY_FLASH_MODE, mActivity
							.getString(R.string.pref_camera_flashmode_default));
			String validVideoFlashMode = GC
					.getValidFlashModeForVideo(flashMode);
			mGC.overrideSettingsValue(
					CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE,
					validVideoFlashMode);
			String whiteBalance = mPreferences
					.getString(
							CameraSettings.KEY_WHITE_BALANCE,
							mActivity
									.getString(R.string.pref_camera_whitebalance_default));
			focusMode = mFocusManager.getFocusMode();
			colorEfect = mParameters.getColorEffect();
			exposureCompensation = Integer.toString(mParameters
					.getExposureCompensation());
			touchAfAec = mCurrTouchAfAec;

			/*
			 * overrideCameraSettings(flashMode, whiteBalance, focusMode,
			 * exposureCompensation, touchAfAec, mParameters.getAutoExposure(),
			 * Integer.toString(mParameters.getSaturation()),
			 * Integer.toString(mParameters.getContrast()),
			 * Integer.toString(mParameters.getSharpness()), colorEfect,
			 * sceneMode, redeyeReduction, aeBracketing);
			 */
		} else if (mFocusManager.isZslEnabled()) {
			overrideCameraSettings(flashMode, null, focusMode,
					exposureCompensation, touchAfAec, null, null, null, null,
					colorEfect, sceneMode, redeyeReduction, aeBracketing);
		} else {
			overrideCameraSettings(flashMode, null, focusMode,
					exposureCompensation, touchAfAec, null, null, null, null,
					colorEfect, sceneMode, redeyeReduction, aeBracketing);
		}
	}

	private void overrideCameraSettings(final String flashMode,
			final String whiteBalance, final String focusMode,
			final String exposureMode, final String touchMode,
			final String autoExposure, final String saturation,
			final String contrast, final String sharpness,
			final String coloreffect, final String sceneMode,
			final String redeyeReduction, final String aeBracketing) {
		mUI.overrideSettings(CameraSettings.KEY_FLASH_MODE, flashMode,
				CameraSettings.KEY_WHITE_BALANCE, whiteBalance,
				CameraSettings.KEY_FOCUS_MODE, focusMode,
				CameraSettings.KEY_EXPOSURE, exposureMode,
				CameraSettings.KEY_TOUCH_AF_AEC, touchMode,
				CameraSettings.KEY_AUTOEXPOSURE, autoExposure,
				CameraSettings.KEY_SATURATION, saturation,
				CameraSettings.KEY_CONTRAST, contrast,
				CameraSettings.KEY_SHARPNESS, sharpness,
				CameraSettings.KEY_COLOR_EFFECT, coloreffect,
				CameraSettings.KEY_SCENE_MODE, sceneMode,
				CameraSettings.KEY_REDEYE_REDUCTION, redeyeReduction,
				CameraSettings.KEY_AE_BRACKET_HDR, aeBracketing);
	}

	private void loadQRCodeSound() {
		mQRCodeSound = new SoundPool(10, SoundClips.getAudioTypeForSoundPool(),
				0);
		mQRCodeSoundID = mQRCodeSound.load(mActivity, R.raw.qrcode_beep, 1);
	}

	private void unloadQRCodeSound() {
		if (mQRCodeSound != null) {
			mQRCodeSound.unload(mQRCodeSoundID);
			mQRCodeSound.release();
			mQRCodeSound = null;
		}
	}

	class SizeComparator implements Comparator<Size> {
		@Override
		public int compare(Size arg0, Size arg1) {
			Size size0 = arg0;
			Size size1 = arg1;
			if (size0.width < size1.width) {
				return 1;
			} else if (size0.width == size1.width) {
				return 0;
			} else {
				return -1;
			}
		}

	}

	public static String getRatioString(int width, int height) {
		double ratio = (double) height / width;
		if (ratio < 1.0)
			ratio = 1 / ratio;
		if (toleranceRatio(16.0 / 9.0, ratio)) {
			return GC.PICTURE_RATIO_16_9;
		}
		if (toleranceRatio(5.0 / 3.0, ratio)) {
			return GC.PICTURE_RATIO_5_3;
		}
		if (toleranceRatio(4.0 / 3.0, ratio)) {
			return GC.PICTURE_RATIO_4_3;
		}
		return SettingUtils.getRatioString(ratio);
	}

	private static boolean toleranceRatio(double target, double candidate) {
		final double ASPECT_TOLERANCE = 0.01;
		boolean tolerance = false;
		if (candidate > 0) {
			tolerance = Math.abs(target - candidate) <= ASPECT_TOLERANCE;
		}
		return tolerance;
	}

	// [BUGFIX]-ADD-END by tiexing.xie 2014.11.19

	private void loadCameraPreferences() {
		CameraSettings settings = new CameraSettings(mActivity, mInitialParams,
				mCameraId, CameraHolder.instance().getCameraInfo());
		mPreferenceGroup = settings
				.getPreferenceGroup(R.xml.camera_preferences);
	}

	private int UI_STATE_PORTRAIT = 0;
	private int UI_STATE_LANDSCAPE = 1;

	private int mUIState = UI_STATE_PORTRAIT;

	@Override
	public void onOrientationChanged(int orientation) {
		// We keep the last known orientation. So if the user first orient
		// the camera then point the camera to floor or sky, we still have
		// the correct orientation.

		// if(mFocusManager!=null&&mFocusManager.isFocusIdle()&&mCameraState==IDLE){
		// if(mShutterManager!=null){
		// mShutterManager.enableShutter(true);
		// }
		// }
		if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN)
			return;

		int curMode = mGC.getCurrentMode();
		if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
				&& mUIState == UI_STATE_PORTRAIT) {

			if (mUI != null && optimalSize != null) {
				mUIState = UI_STATE_LANDSCAPE;
				if (curMode == GC.MODE_POLAROID) {
					mUI.updatePreviewLayout(optimalSize.width,
							optimalSize.width, true);
					resizeForPreviewAspectRatio();// landscape
				} else {
					mUI.updatePreviewLayout(optimalSize.width,
							optimalSize.height, false);
				}
			}
		} else if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
				&& mUIState == UI_STATE_LANDSCAPE) {
			if (mUI != null && optimalSize != null) {
				mUIState = UI_STATE_PORTRAIT;
				if (curMode == GC.MODE_POLAROID) {
					mUI.updatePreviewLayout(optimalSize.width,
							optimalSize.width, true);
					resizeForPreviewAspectRatio();
				} else {
					mUI.updatePreviewLayout(optimalSize.width,
							optimalSize.height, false);
				}
			}
		}

		int oldOrientation = mOrientation;
		mOrientation = CameraUtil.roundOrientation(orientation, mOrientation);

		if (mActivity.isReversibleEnabled()) {
			if ((mOrientation == 180 || mOrientation == 0)
					&& mOrientation != oldOrientation) {
				// startPreview();
				setDisplayOrientation();

			}
		}

		if (oldOrientation != mOrientation) {
			Log.v(TAG, "onOrientationChanged, update parameters");
			if (mParameters != null && mCameraDevice != null) {
				onSharedPreferenceChanged();
			}
			mUI.setRotatePolaroid(orientation);
		}

		// Show the toast after getting the first orientation changed.
		if (mHandler.hasMessages(SHOW_TAP_TO_FOCUS_TOAST)) {
			mHandler.removeMessages(SHOW_TAP_TO_FOCUS_TOAST);
			showTapToFocusToast();
		}

		// [BUGFIX]-Added by jian.zhang1 for PR770765 2014.08.21 Begin
		mUI.rotateCountDown(mOrientation);
		// [BUGFIX]-Added by jian.zhang1 for PR770765 2014.08.21 End
		// need to re-initialize mGraphView to show histogram on rotate
		/*
		 * mGraphView = (GraphView)mRootView.findViewById(R.id.graph_view);
		 * if(mGraphView != null){ mGraphView.setPhotoModuleObject(this);
		 * mGraphView.PreviewChanged(); }
		 */
	}

	@Override
	public void onStop() {
		Log.d(TAG,"###YUHUAN###onStop#mMediaProviderClient=" + mMediaProviderClient);
		if (mMediaProviderClient != null) {
			mMediaProviderClient.release();
			mMediaProviderClient = null;
		}
	}

	@Override
	public void onCaptureCancelled() {
		mActivity.setResultEx(Activity.RESULT_CANCELED, new Intent());
		// Add by zhimin.yu for PR931912,PR942025, begin
		if (isImageCaptureIntent()) {
			mGC.setCameraPreview(true);
		}
		// Add by zhimin.yu for PR931912,PR942025, end
		mActivity.finish();
	}

	@Override
	public void onCaptureRetake() {
		if (mPaused)
			return;
		// Add by zhimin.yu for PR931912,PR942025, begin
		if (isImageCaptureIntent()) {
			mGC.setCameraPreview(true);
		}
		// Add by zhimin.yu for PR931912,PR942025, end
		mUI.hidePostCaptureAlert();
		mActivity.setViewState(GC.VIEW_STATE_NORMAL);
		setupPreview();
	}

	/**
	 * save pic for polaroid
	 * 
	 * @param data
	 */
	public Uri savePicForPolaroid(Bitmap bitmap) {
		if (null == mNamedImages) {
			mNamedImages = new NamedImages();
		}
		mNamedImages.nameNewImage(System.currentTimeMillis());
		String pictureFormat = mParameters.get(KEY_PICTURE_FORMAT);
		NamedEntity name = mNamedImages.getNextNameEntity();
		String title = (name == null) ? null : name.title;
		long date = (name == null) ? -1 : name.date;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		Uri uri = Storage.addImage(mActivity.getContentResolver(), title, date,
				mLocation, mOrientation, null, baos.toByteArray(),
				bitmap.getWidth(), bitmap.getHeight(), pictureFormat);
		return uri;
	}

	// PR900096,v-nj-feiqiang.cheng add begin
	private Location mLocation;

	public void savePicWhenNotNormal(byte[] data) {

		ExifInterface exif = Exif.getExif(data);
		int orientation = Exif.getOrientation(exif);

		// Burst snapshot. Generate new image name.
		if (mReceivedSnapNum > 1)
			mNamedImages.nameNewImage(mCaptureStartTime);

		// Calculate the width and the height of the jpeg.
		Size s = mParameters.getPictureSize();
		int width, height;
		if ((mJpegRotation + orientation) % 180 == 0) {
			width = s.width;
			height = s.height;
		} else {
			width = s.height;
			height = s.width;
		}

		String pictureFormat = mParameters.get(KEY_PICTURE_FORMAT);
		if (pictureFormat != null
				&& !pictureFormat.equalsIgnoreCase(PIXEL_FORMAT_JPEG)) {
			// overwrite width and height if raw picture
			String pair = mParameters.get(KEY_QC_RAW_PICUTRE_SIZE);
			if (pair != null) {
				int pos = pair.indexOf('x');
				if (pos != -1) {
					width = Integer.parseInt(pair.substring(0, pos));
					height = Integer.parseInt(pair.substring(pos + 1));
				}
			}
		}
		// [BUGFIX]-Add-BEGIN by TCTNJ.qiannan.wang,08/19/2014,738934,
		// [Gallery]Photo details are incorrect
		setTctExifInfo(exif);
		// [BUGFIX]-Add-END by TCTNJ.qiannan.wang
		NamedEntity name = mNamedImages.getNextNameEntity();
		String title = (name == null) ? null : name.title;
		long date = (name == null) ? -1 : name.date;

		// Handle debug mode outputs
		if (mDebugUri != null) {
			// If using a debug uri, save jpeg there.
			saveToDebugUri(data);

			// Adjust the title of the debug image shown in mediastore.
			if (title != null) {
				title = DEBUG_IMAGE_PREFIX + title;
			}
		}

		if (title == null) {
			Log.e(TAG, "Unbalanced name/data pair");
		} else {
			if (date == -1)
				date = mCaptureStartTime;
			if (mHeading >= 0) {
				// heading direction has been updated by the sensor.
				ExifTag directionRefTag = exif.buildTag(
						ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
						ExifInterface.GpsTrackRef.MAGNETIC_DIRECTION);
				ExifTag directionTag = exif.buildTag(
						ExifInterface.TAG_GPS_IMG_DIRECTION, new Rational(
								mHeading, 1));
				exif.setTag(directionRefTag);
				exif.setTag(directionTag);
			}

			String mPictureFormat = mParameters.get(KEY_PICTURE_FORMAT);
			// if (mCaptureListener != null) mCaptureListener.onCaptureDone();
			if (GC.MODE_EXPRESSION4 != mGC.getCurrentMode()) {
				mActivity.getMediaSaveService()
						.addImage(data, title, date, mLocation, width, height,
								orientation, exif, mOnMediaSavedListener,
								mContentResolver, mPictureFormat);
				// [BUGFIX]-Add by TCTNB,bin.zhang2, 2014-06-18,PR706182 Begin
				// mActivity.setViewState(GC.VIEW_STATE_NORMAL); // ZSL
				// improvement by jpxiong
				// [BUGFIX]-Add by TCTNB,bin.zhang2, 2014-06-18,PR706182 End
			}
		}
	}

	// PR900096,v-nj-feiqiang.cheng add end

	/**
	 * get location
	 */
	public Location getLocation() {
		return mLocation;
	}

	public String getPicFormat() {
		return mParameters.get(KEY_PICTURE_FORMAT);
	}

	@Override
	public void onCaptureDone() {
		if (mPaused) {
			return;
		}
		// Add by zhimin.yu for PR931912,PR942025, begin
		if (isImageCaptureIntent()) {
			mGC.setCameraPreview(true);
		}
		// Add by zhimin.yu for PR931912,PR942025, end
		byte[] data = mJpegImageData;

		if (mCropValue == null) {
			// First handle the no crop case -- just return the value. If the
			// caller specifies a "save uri" then write the data to its
			// stream. Otherwise, pass back a scaled down version of the bitmap
			// directly in the extras.
			if (mSaveUri != null) {
				// PR900096,v-nj-feiqiang.cheng add begin
				if ("true".equals(mActivity.getResources().getString(
						R.string.def_camera_whether_savepic_whennotnormal))) {
					savePicWhenNotNormal(data);
				}
				// PR900096,v-nj-feiqiang.cheng add end
				OutputStream outputStream = null;
				try {
					outputStream = mContentResolver.openOutputStream(mSaveUri);
					outputStream.write(data);
					outputStream.close();

					mActivity.setResultEx(Activity.RESULT_OK);
					mActivity.finish();
				} catch (IOException ex) {
					// ignore exception
				} finally {
					CameraUtil.closeSilently(outputStream);
				}
			} else {
				ExifInterface exif = Exif.getExif(data);
				int orientation = Exif.getOrientation(exif);
				Bitmap bitmap = CameraUtil.makeBitmap(data, 50 * 1024);
				bitmap = CameraUtil.rotate(bitmap, orientation);
				mActivity.setResultEx(Activity.RESULT_OK, new Intent(
						"inline-data").putExtra("data", bitmap));
				mActivity.finish();
			}
		} else {
			// Save the image to a temp file and invoke the cropper
			Uri tempUri = null;
			FileOutputStream tempStream = null;
			try {
				File path = mActivity.getFileStreamPath(sTempCropFilename);
				path.delete();
				tempStream = mActivity.openFileOutput(sTempCropFilename, 0);
				tempStream.write(data);
				tempStream.close();
				tempUri = Uri.fromFile(path);
			} catch (FileNotFoundException ex) {
				mActivity.setResultEx(Activity.RESULT_CANCELED);
				mActivity.finish();
				return;
			} catch (IOException ex) {
				mActivity.setResultEx(Activity.RESULT_CANCELED);
				mActivity.finish();
				return;
			} finally {
				CameraUtil.closeSilently(tempStream);
			}

			Bundle newExtras = new Bundle();
			if (mCropValue.equals("circle")) {
				newExtras.putString("circleCrop", "true");
			}
			if (mSaveUri != null) {
				newExtras.putParcelable(MediaStore.EXTRA_OUTPUT, mSaveUri);
			} else {
				newExtras.putBoolean(CameraUtil.KEY_RETURN_DATA, true);
			}
			if (mActivity.isSecureCamera()) {
				newExtras.putBoolean(CameraUtil.KEY_SHOW_WHEN_LOCKED, true);
			}

			// TODO: Share this constant.
			final String CROP_ACTION = "com.android.camera.action.CROP";
			Intent cropIntent = new Intent(CROP_ACTION);

			cropIntent.setData(tempUri);
			cropIntent.putExtras(newExtras);

			mActivity.startActivityForResult(cropIntent, REQUEST_CROP);
		}
	}

	@Override
	public void onShutterButtonFocus(boolean pressed) {
		if (mPaused || mUI.collapseCameraControls()
				|| (mCameraState == SNAPSHOT_IN_PROGRESS)
				|| (mCameraState == PREVIEW_STOPPED))
			return;
		// Do not do focus if there is not enough storage.
		if (pressed && !canTakePicture())
			return;
		// [BUGFIX]-Del by TCTNB,shidong.hou, 2014-08-26,PR770858 Begin
		/*
		 * if((!pressed)&&(mCurrentShowShotsNum < BURST_NUM_MAX )) {
		 * isBurstshotBreaked = true; }
		 */
		// [BUGFIX]-Del by TCTNB,shidong.hou, 2014-08-26,PR770858 End

		CameraActivity.TraceLog(TAG, "onShutterButtonFocus pressed=" + pressed
				+ " mIsFromBurstShot=" + mIsFromBurstShot);
		if (mIsFromBurstShot) {
			doOnShutterButtonFocus(pressed);
		}

		String refocusOnShoot = mPreferences
				.getString(CameraSettings.KEY_REFOCUS_ON_SHOOT, mActivity
						.getString(R.string.pref_camera_refocusonshoot_default));
		if (!refocusOnShoot.equals(mActivity
				.getString(R.string.pref_camera_refocusonshoot_default))) {
			if (pressed) {
				mFocusManager.onShutterDown();
			} else {
				mFocusManager.onShutterUp();
			}
		}
	}

	private void doOnShutterButtonFocus(boolean pressed) {
		synchronized (mCameraDevice) {
			if (mCameraState == LONGSHOT) {
				isInCapture = false;
				mCameraDevice.setLongshot(false);
				if (!mFocusManager.isZslEnabled()) {
					setupPreview();
				} else {
					setCameraState(IDLE);
					mFocusManager.resetTouchFocus();
					if (CameraUtil.FOCUS_MODE_CONTINUOUS_PICTURE
							.equals(mFocusManager.getFocusMode())) {
						mCameraDevice.cancelAutoFocus();
					}
					mUI.resumeFaceDetection();
				}
			}
		}

		// for countdown mode, we need to postpone the shutter release
		// i.e. lock the focus during countdown.
		if (!pressed && mLongpressFlag && !mUI.isCountingDown()) {
			mLongpressFlag = false;
			// mIgnoreClick = true;
			setBurstShotMode(false);

			if (!needRestart()) {
				setCameraParameters(UPDATE_PARAM_PREFERENCE);
			} else {
				try {
					setupPreview();
				} catch (Exception e) {
					Log.e(TAG, "setupPreview Exception");
				}
			}
			// the burstshot is breaked, if showing > real, just discard extra.
			// [BUGFIX]-Del by TCTNB,shidong.hou, 2014-08-15,PR752872 Begin
			// if (isBurstshotBreaked && mCurrentShowShotsNum >
			// mReceivedSnapNum)
			// mCurrentShowShotsNum = mReceivedSnapNum;
			// [BUGFIX]-Del by TCTNB,shidong.hou, 2014-08-15,PR752872 End
			// [BUGFIX]-Modify by TCTNB,chao.luo, 2014-08-07,PR744625 Begin
			// mActivity.getMediaSaveService().capturedDone4Burstshot(mCurrentShowShotsNum);
			// [BUGFIX]-Modify by TCTNB,chao.luo, 2014-08-07,PR744625 End
			// [BUGFIX]-Del by TCTNB,shidong.hou, 2014-08-26,PR770858 Begin
			/*
			 * //[BUGFIX]-Modify by TCTNB,chao.luo, 2014-08-07,PR744625 Begin
			 * mActivity
			 * .getMediaSaveService().capturedDone4Burstshot(mCurrentShowShotsNum
			 * ); //[BUGFIX]-Modify by TCTNB,chao.luo, 2014-08-07,PR744625 End
			 * mHandler.sendEmptyMessageDelayed(DISMISS_BURST_NUM, 500);
			 */
			// [BUGFIX]-Del by TCTNB,shidong.hou, 2014-08-26,PR770858 End
		}
	}

	public void burstshotSavedDone() {
		mIsFromBurstShot = false;
		Log.i(TAG, "burstshotSavedDone mReceivedSnapNum:" + mReceivedSnapNum);
		// [BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-26,PR770858 Begin
		if ((mCameraState == LONGSHOT)) {
			doOnShutterButtonFocus(false);
		}
		// [BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-26,PR770858 End
		mReceivedSnapNum = 0;
		mCapturedTotalSize = 0;
	}

	private boolean shouldIgnoreClick() {
		boolean result = false;
		if (mGC.getCurrentMode() == GC.MODE_QRCODE || isSettingZSL)
			result = true;
		return result;
	}

	@Override
	public void onShutterButtonClick() {
		if (mActivity == null || !mIsPreviewStarted) {
			return;
		}
		if (mLongpressFlag || mPaused || mUI.collapseCameraControls()
				|| (mCameraState == SWITCHING_CAMERA)
				|| (mCameraState == PREVIEW_STOPPED))
			return;
		if (shouldIgnoreClick())
			return;
		// Do not take the picture if there is not enough storage.
		if (mActivity.getStorageSpaceBytes() <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
			Log.i(TAG, "Not enough space or storage not ready. remaining="
					+ mActivity.getStorageSpaceBytes());
			return;
		}
		Log.v(TAG, "onShutterButtonClick: mCameraState=" + mCameraState);

		if (mSceneMode == CameraUtil.SCENE_MODE_HDR
				|| mASDShowingState == ASD_BACKLIT
				|| mASDShowingState == ASD_NIGHT) {
			mUI.hideSwitcher();
			mUI.setSwipingEnabled(false);
			mActivity.disableDrawer();
		}

		// Need to disable focus for ZSL mode
		Log.d(TAG, "###YUHUAN###onShutterButtonClick#mSnapshotMode="
				+ mSnapshotMode);
		if (mSnapshotMode == CAMERA_SUPPORT_MODE_ZSL) {
			mFocusManager.setZslEnable(true);
		} else {
			mFocusManager.setZslEnable(false);
		}

		// If the user wants to do a snapshot while the previous one is still
		// in progress, remember the fact and do it after we finish the previous
		// one and re-start the preview. Snapshot in progress also includes the
		// state that autofocus is focusing and a picture will be taken when
		// focus callback arrives.
		if ((mFocusManager.isFocusingSnapOnFinish() || mCameraState == SNAPSHOT_IN_PROGRESS)
				&& !mIsImageCaptureIntent) {
			mSnapshotOnIdle = true;
			return;
		}

		// Add for smile shot.
		int curMode = mGC.getCurrentMode();
		if (GC.MODE_SMILE == curMode) {
			if (mCameraState == SNAPSHOT_IN_PROGRESS) {
				Log.i(TAG, "onShutter ButtonClick  inprogress  return 3");
				return;
			}
			Log.i(TAG, "onShutter ButtonClick  inprogress  return 4");
			mFocusManager.doSnap();
			return;
		}
		// Add for Manual mode
		if (GC.MODE_MANUAL == curMode) {
			checkThread();
			mParameters.setAntibanding("off");
		}
		// Add for expression4.
		if (GC.MODE_EXPRESSION4 == curMode) {
			if (ExpressionThread.isExpressionThreadActive()) {
				Log.i(TAG, "onShutter ButtonClick  isExpressionThreadActive");
				// User action: user click the button to take picture.
				// Take care that onShutterButtonClick() maybe called not by
				// user.
				mActivity.runOnUiThread(mExpressionDoSnapRunnable);
			} else {
				Log.d(TAG, "zsy1031 ExpressionThread.detect");
				mActivity.setViewState(GC.VIEW_STATE_SHUTTER_PRESS);
				mGC.setExpressionState(true);
				mShutterManager.getShutterButton().setImageResource(
						R.drawable.btn_shutter_expression4_ing);
				// Run Expression detect thread.
				ExpressionThread.detect(mActivity, mHandler, mOrientation,
						mCameraDevice, mExpressionDoSnapRunnable);
			}
			Log.i(TAG, "onShutter ButtonClick    return 5");
			return;
		}

		mShutterManager.enableShutter(false);
		// reset the flag for next burstsho capture
		mActivity.getMediaSaveService().init4BurstshotStart(false);

		String timer = mPreferences.getString(CameraSettings.KEY_SELF_TIMER,
				mActivity.getString(R.string.pref_camera_timer_default));
		/* [BUG-890467]modified by lijuan.zhang,2015/01/04, begin */
		// boolean playSound =
		// mPreferences.getString(CameraSettings.KEY_TIMER_SOUND_EFFECTS,
		// mActivity.getString(R.string.pref_camera_timer_sound_default))
		// .equals(mActivity.getString(R.string.setting_on_value));
		String defaultValue = CustomUtil.getInstance().getBoolean(
				CustomFields.DEF_TCTCAMERA_SHUTTER_SOUND_ON, true) ? "on"
				: "off";
		boolean playSound = mPreferences.getString(CameraSettings.KEY_SOUND,
				defaultValue).equals(
				mActivity.getString(R.string.setting_on_value));
		/* [BUG-890467]modified by lijuan.zhang,2015/01/04, end */

		int seconds = Integer.parseInt(timer);
		// When shutter button is pressed, check whether the previous countdown
		// is
		// finished. If not, cancel the previous countdown and start a new one.
		if (mUI.isCountingDown()) {
			mUI.cancelCountDown();
		}
		if (seconds > 0) {
			mActivity.disableDrawer();
			mActivity.setSwipingEnabled(false);
			mUI.startCountDown(seconds, playSound);
		} else {
			mSnapshotOnIdle = false;
			mFocusManager.doSnap();
		}
	}

	private void doNormalLongClick() {
		Log.i(TAG, "LMCH0424 doNormalLongClick E");
		/*
		 * if ((null != mCameraDevice) && ((mCameraState == IDLE) ||
		 * (mCameraState == FOCUSING))) { boolean enable = false; enable =
		 * SystemProperties.getBoolean(PERSIST_LONG_ENABLE, false); if ( enable
		 * ) { enable = SystemProperties.getBoolean(PERSIST_LONG_SAVE, false);
		 * mLongshotSave = enable; mCameraDevice.setLongshot(true);
		 * setCameraState(PhotoController.LONGSHOT); mFocusManager.doSnap(); } }
		 */
	}

	private boolean isBurstShotSupported() {
		return (mActivity.getCameraId() != GC.CAMERAID_FRONT)
				&& !isImageCaptureIntent()
				&& (mGC.getCurrentMode() == GC.MODE_PHOTO
						&& mASDShowingState != ASD_BACKLIT && mASDShowingState != ASD_NIGHT);
	}

	private boolean isMemoryLow() {
		mActivityManager.getMemoryInfo(mMemoryInfo);
		return mMemoryInfo.lowMemory
				|| (mRuntime.maxMemory() - mRuntime.totalMemory()) < Storage.LOW_STORAGE_THRESHOLD_BYTES;
	}

	private void longPress4Burstshot() {
		Log.d(TAG,"###YUHUAN###longPress4Burstshot into ");
		// [BUGFIX]-MOD by jian.zhang1 for PR764272 2014.08.14 Begin
		Log.d(TAG,"###YUHUAN###longPress4Burstshot#isBurstShotSupported=" + isBurstShotSupported());
		if (!isBurstShotSupported()) {
			String showing = mActivity
					.getString(R.string.normal_camera_continuous_not_supported);
			Log.d(TAG,"###YUHUAN###longPress4Burstshot#showing=" + showing);
			mActivity.showInfo(showing, false, false);
			return;
		}
		// [BUGFIX]-MOD by jian.zhang1 for PR764272 2014.08.14 End

		mActivity.updateStorageSpaceAndHint();
		if (mUI.isCountingDown() || (null == mCameraDevice)) {
			Log.w(TAG, "LMCH0422 the is countdown OR  device is null");
			return;
		}
		if (isMemoryLow()) {
			// mActivity.getCameraViewManager().getInfoManager().showText(
			// mActivity.getString(R.string.burstshot_low_memory_auto_stop),
			// 2000);
			return;
		}
		if (!canTakePicture()) {
			Log.w(TAG, "LMCH0422 there are not enough space, sd card removed");
			return;
		}

		// /////////Preference update in normal burst shot///////////////////
		Log.d(TAG,"###YUHUAN###longPress4Burstshot#mIsFromBurstShot=" + mIsFromBurstShot);
		if (mIsFromBurstShot) {
			SharedPreferences sharedata = mActivity.getSharedPreferences(
					"burstshot_guidetip", mActivity.MODE_PRIVATE);
			if (sharedata != null) {
				burstshot_guidetip_display = sharedata.getInt(
						"burstshot_guidetip", burstshot_guidetip_display);
				Log.d(TAG,"###YUHUAN###longPress4Burstshot#burstshot_guidetip_display=" + burstshot_guidetip_display);
			}
			if (burstshot_guidetip_display == 1) {
				/*
				 * Dlg_tips_Continuous dlgTips = new Dlg_tips_Continuous(
				 * mActivity, R.style.launchFirstDialog); dlgTips.show();
				 */
				burstshot_guidetip_display = 0;
				SharedPreferences.Editor editor = mActivity
						.getSharedPreferences("burstshot_guidetip",
								mActivity.MODE_PRIVATE).edit();
				editor.putInt("burstshot_guidetip", burstshot_guidetip_display);
				editor.commit();
				Log.w(TAG, "LMCH0422 show burstshot dialog");
				// [BUGFIX]-ADD-begin by chao.luo, PR-752855, 2014-8-8
				// return;
				// [BUGFIX]-ADD-End by chao.luo, PR-752855, 2014-8-8
			}

			mActivity.getMediaSaveService().init4BurstshotStart(true);
		}
		// ////////////////////////////////////////////////////////////
		setBurstShotMode(true);
		mLongpressFlag = true;
		Log.d(TAG,"###YUHUAN###longPress4Burstshot#needRestart()=" + needRestart());
		if (!needRestart()) {
			setCameraParameters(UPDATE_PARAM_PREFERENCE);
		} else {
			setupPreview();
			// if not 8M camera, ripple may appear at the beginning of longshot,
			// sleep for while to avoid this.
			try {
				Thread.sleep(600);
			} catch (Exception e) {
				Log.e(TAG, "shutterbuttonlongpressed, thread sleep error " + e);
			}
		}

		mLongshotSave = true;
		isBurstshotBreaked = false;
		mCapturedTotalSize = 0;
		setCameraState(PhotoController.LONGSHOT);
		ExtendCamera extCamera = ExtendCamera.getInstance(mCameraDevice
				.getCamera());
		mNeedResnapDuringLongshot = extCamera
				.needTakePicturePerShotDuringBurst();
		Log.d(TAG,"###YUHUAN###longPress4Burstshot#mNeedResnapDuringLongshot=" + mNeedResnapDuringLongshot);
		// [BUGFIX]-MOD-BEGIN by yongsheng.shan, PR-888667, 2015-01-12
		checkThread();
		mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
		mCameraDevice.setParameters(mParameters);
		// [BUGFIX]-MOD-END by yongsheng.shan, PR-888667, 2015-01-12

		// PR 901325 by jpxiong begin
		isInCapture = true;
		try {
			mCameraDevice.setLongshot(true);
		} catch (java.lang.RuntimeException e) {
			Log.e(TAG,
					"ERROR!!! setLongShot exception:"
							+ e
							+ "! Try to disable the longshot first and then enable longshot again!!!");
			mCameraDevice.setLongshot(false);
			mCameraDevice.setLongshot(true);
		}
		// PR 901325 by jpxiong end
		mFocusManager.doSnapInLongshot();
	}

	class MyByteBufferQueue {
		private int queueLength = 1;
		private int bufferSize = 1;
		List<MyByteBuffer> bufferList = null;
		List<MyByteBuffer> freeBuffer, lockedBuffer, processedBuffer;

		MyByteBufferQueue(int queueLength, int bufferSize) {
			this.queueLength = queueLength;
			this.bufferSize = bufferSize;
			freeBuffer = new ArrayList<MyByteBuffer>();

			for (int i = 0; i < this.queueLength; i++) {
				freeBuffer.add(i, new MyByteBuffer(this.bufferSize));
			}
		}

		MyByteBuffer getFreeBuffer() {
			if (freeBuffer.size() > 0) {
				MyByteBuffer bb = freeBuffer.remove(0);
				return bb;
			}
			return null;
		}

		void unLockBuffer(MyByteBuffer buffer) {
			freeBuffer.add(buffer);
		}

	}

	class MyByteBuffer {
		byte[] bufferData = null;

		MyByteBuffer(int bufferLength) {
			this.bufferData = new byte[bufferLength];
		}

		public byte[] getByteBuffer() {
			return bufferData;
		}

	}

	private int mSkinSoftLevel = 100;
	private int mSkinBrightLevel = 100;

	private int mOriginPreviewWidth = -1;
	private int mOriginPreviewHeight = -1;
	private Bitmap mBitmap;
	private Rect mRect = new Rect();

	boolean mIsFBSurfaceAvailable = false;
	Matrix mFBSurfaceMatrix = null;
	private RectF mSrcRect = null;
	private RectF mDstRect = null;
	SurfaceHolder.Callback mFBSurfaceCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			mIsFBSurfaceAvailable = false;
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			mIsFBSurfaceAvailable = true;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {

		}
	};

	//public final static int ARCSOFTFACEBEAUTY_ON = 0;
	//public final static int ARCSOFTFACEBEAUTY_OFF = 1;
	//private final static int ARCSOFTFACEBEAUTY_NONEED = -1;
	//int mIsFaceBeautyModeOn = ARCSOFTFACEBEAUTY_NONEED;

	/*
	public void setArcsoftFaceBeauty(int on) {
		// if(on){
		// startFB();
		// }else{
		// stopFB();
		// }
		mIsFaceBeautyModeOn = on;

	}*/

	/*
	public void startFB() {
		CameraActivity.TraceLog(TAG, "Start FB");
		CameraActivity.TraceLog(TAG, "Turn on facebeauty");
		checkThread();
		mParameters.set("arcsoft-mode", "abs");
		mParameters.set("arcsoft-skin-soften", faceBeauty_soften);
		mParameters.set("arcsoft-skin-bright", faceBeauty_bright);
		GC.setSettingEnable(GC.ROW_SETTING_ARCSOFT_NIGHT, false);
		mTopViewManager.setPhotoMode(this);
		applyParametersToServer();
		mIsFaceBeautyModeOn = ARCSOFTFACEBEAUTY_ON;
		// initFBParams();
		// mShutterManager.refresh();
		// mSwitcherManager.hide();
	}

	public void stopFB() {
		// recoverFromFB();
		CameraActivity.TraceLog(TAG, "Close FB");
		GC.setSettingEnable(GC.ROW_SETTING_ARCSOFT_NIGHT, true);
		CameraActivity.TraceLog(TAG, "Turn off acsoftmode");
		checkThread();
		if (mParameters == null || mCameraDevice == null) {// Camera not started
															// yet?
			return;
		}
		mParameters.set("arcsoft-mode", "off");
		// mParameters.set("arcsoft-skin-soften", faceBeauty_soften);
		// mParameters.set("arcsoft-skin-bright", faceBeauty_bright);
		// mParameters.set("tct-fff-enable", "off");
		applyParametersToServer();
		mIsFaceBeautyModeOn = ARCSOFTFACEBEAUTY_NONEED;
		saveFaceBeautySBValue();
	}
	*/

	/*
	private void saveFaceBeautySBValue() {
		mPreferences.edit()
				.putInt(CameraSettings.KEY_ARCSOFT_SOFTEN, faceBeauty_soften)
				.apply();
		mPreferences.edit()
				.putInt(CameraSettings.KEY_ARCSOFT_BRIGHT, faceBeauty_bright)
				.apply();
	}

	private void initFacebeautySBValue() {

		faceBeauty_soften = mPreferences.getInt(
				CameraSettings.KEY_ARCSOFT_SOFTEN, faceBeauty_soften);
		faceBeauty_bright = mPreferences.getInt(
				CameraSettings.KEY_ARCSOFT_BRIGHT, faceBeauty_bright);
	}*/

	private void saveValue(String key, boolean value) {
		mPreferences.edit().putBoolean(key, value).apply();
	}

	private boolean getValue(String key) {
		return mPreferences.getBoolean(key, true);
	}

	private void initManualParameter() {
		// ISO_MINVALUE=mParameters.getInt("min-iso");
		// ISO_MAXVALUE=mParameters.getInt("max-iso");
		// ISO_EXPOSURETIME_MINVALUE=(int)(Double.parseDouble(mParameters.get("min-exposure-time")));
		// ISO_EXPOSURETIME_MAXVALUE=(int)(Double.parseDouble(mParameters.get("max-exposure-time")));
		// v-nj-feiqiang.cheng begin for PR941559,941555
		/*if ("off".equals(mActivity.getResources().getString(
				R.string.def_manualmode_feature_onoff)))
			return;
		*/
		// v-nj-feiqiang.cheng end for PR941559,941555
		// null==mParameters.get("min-focus-pos-ratio");
		// FOUCS_POS_RATIO_MINVALUE=mParameters.getInt("min-focus-pos-ratio");
		// FOUCS_POS_RATIO_MAXVALUE=mParameters.getInt("max-focus-pos-ratio");

	}

	// public void saveFile(byte[] yuv420sp, String fileName, boolean append){
	// FileOutputStream fos;
	// try {
	// fos = new FileOutputStream(fileName, append);
	// fos.write(yuv420sp);
	// fos.close();
	// } catch ( IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	public static final String DCIM = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
			.toString();
	public static final String DIRECTORY = DCIM + "/Camera";

	private void saveNightShotImg(byte[] jpegData, int imgWidth, int imgHeight,
			Location loc) {
		String fileName = String.format("%d.jpg", System.currentTimeMillis());
		String filePath = String.format("%s/%s", DIRECTORY, fileName);
		try {
			FileOutputStream fos = new FileOutputStream(new File(filePath));

			fos.write(jpegData);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ContentValues v = new ContentValues();
		v.put(MediaColumns.TITLE, fileName);
		v.put(MediaColumns.DISPLAY_NAME, fileName);
		v.put(ImageColumns.DATE_TAKEN, System.currentTimeMillis());
		v.put(MediaColumns.MIME_TYPE, "image/jpeg");
		v.put(MediaColumns.DATA, filePath);
		v.put(MediaColumns.SIZE, filePath.length());
		v.put(ImageColumns.WIDTH, imgWidth);
		v.put(ImageColumns.HEIGHT, imgHeight);
		mActivity.getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
		// uri =
		// mActivity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		// v);
	}

	// private void saveNightShotImg(byte[] jpegData, int imgWidth, int
	// imgHeight,
	// Location loc) {
	//
	// ExifInterface exif = Exif.getExif(jpegData);
	// int orientation = Exif.getOrientation(exif);
	//
	// // Burst snapshot. Generate new image name.
	// if (mReceivedSnapNum > 1)
	// mNamedImages.nameNewImage(mCaptureStartTime);
	//
	// // Calculate the width and the height of the jpeg.
	// int width, height;
	// if ((mJpegRotation + orientation) % 180 == 0) {
	// width = imgWidth;
	// height = imgHeight;
	// } else {
	// width = imgHeight;
	// height = imgWidth;
	// }
	//
	// String pictureFormat = mParameters.get(KEY_PICTURE_FORMAT);
	// if (pictureFormat != null
	// && !pictureFormat.equalsIgnoreCase(PIXEL_FORMAT_JPEG)) {
	// // overwrite width and height if raw picture
	// String pair = mParameters.get(KEY_QC_RAW_PICUTRE_SIZE);
	// if (pair != null) {
	// int pos = pair.indexOf('x');
	// if (pos != -1) {
	// width = Integer.parseInt(pair.substring(0, pos));
	// height = Integer.parseInt(pair.substring(pos + 1));
	// }
	// }
	// }
	// // [BUGFIX]-Add-BEGIN by TCTNJ.qiannan.wang,08/19/2014,738934,
	// // [Gallery]Photo details are incorrect
	// setTctExifInfo(exif);
	// // [BUGFIX]-Add-END by TCTNJ.qiannan.wang
	// NamedEntity name = mNamedImages.getNextNameEntity();
	// String title = (name == null) ? null : name.title;
	// long date = (name == null) ? -1 : name.date;
	//
	// // Handle debug mode outputs
	// if (mDebugUri != null) {
	// // If using a debug uri, save jpeg there.
	// saveToDebugUri(jpegData);
	//
	// // Adjust the title of the debug image shown in mediastore.
	// if (title != null) {
	// title = DEBUG_IMAGE_PREFIX + title;
	// }
	// }
	//
	// if (title == null) {
	// Log.e(TAG, "Unbalanced name/data pair");
	// } else {
	// if (date == -1)
	// date = mCaptureStartTime;
	// if (mHeading >= 0) {
	// // heading direction has been updated by the sensor.
	// ExifTag directionRefTag = exif.buildTag(
	// ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
	// ExifInterface.GpsTrackRef.MAGNETIC_DIRECTION);
	// ExifTag directionTag = exif.buildTag(
	// ExifInterface.TAG_GPS_IMG_DIRECTION, new Rational(
	// mHeading, 1));
	// exif.setTag(directionRefTag);
	// exif.setTag(directionTag);
	// }
	//
	// String mPictureFormat = mParameters.get(KEY_PICTURE_FORMAT);
	// if (mCaptureListener != null)
	// mCaptureListener.onCaptureDone();
	// mActivity.getMediaSaveService().addImage(jpegData, title, date,
	// loc, width, height, orientation, exif,
	// mOnMediaSavedListener, mContentResolver, mPictureFormat);
	// // [BUGFIX]-Add by TCTNB,bin.zhang2, 2014-06-18,PR706182 Begin
	// mActivity.setViewState(GC.VIEW_STATE_NORMAL);
	// // [BUGFIX]-Add by TCTNB,bin.zhang2, 2014-06-18,PR706182 End
	// }
	//
	// // Animate capture with real jpeg data instead of a preview frame.
	// // if (mCameraState != LONGSHOT) {
	// // mUI.animateCapture(jpegData, mOrientation, mMirror);
	// // }
	// mShutterManager.enableShutter(true);
	// mActivity.setViewState(GC.VIEW_STATE_NORMAL);
	//
	// // Check this in advance of each shot so we don't add to shutter
	// // latency. It's true that someone else could write to the SD card in
	// // the mean time and fill it, but that could have happened between the
	// // shutter press and saving the JPEG too.
	// mActivity.updateStorageSpaceAndHint();
	// }

	boolean mIsFromBurstShot = false;

	@Override
	public void onShutterButtonLongClick() {
		Log.d(TAG,"###YUHUAN###onShutterButtonLongClick into ");
		if (!CustomUtil.getInstance().getBoolean(
				CustomFields.DEF_BURST_SHOT_ENABLE, true)) {
			// modify by minghui.hua for CR992573
			CameraActivity.TraceLog(TAG,
					"onShutterButtonLongClick SDMID not support burst shot");
			return;
		}

		/*
		boolean isNightMode = mActivity.getString(R.string.setting_on_value)
				.equals(mPreferences.getString(
						CameraSettings.KEY_CAMERA_ARCSOFT_NIGHT,
						mActivity.getString(R.string.setting_off_value)));
		Log.d(TAG,"###YUHUAN###onShutterButtonLongClick#isNightMode=" + isNightMode);
		*/
		
		/*
		if (mIsEnableNight) {
			return;
		}*/
		
		mIsFromBurstShot = true;
		try {
			String value = CustomUtil.getInstance().getString(
					CustomFields.DEF_BURST_NUM_MAX, "20");
			Log.d(TAG,"###YUHUAN###onShutterButtonLongClick#value=" + value);
			BURST_NUM_MAX = Integer.parseInt(value);
		} catch (Exception e) {
			BURST_NUM_MAX = 10;
		}
		if (mActivity.getResources().getBoolean(R.bool.longPress4Burstshot)) {
			longPress4Burstshot();
		} else {
			doNormalLongClick();
		}
	}

	@Override
	public void installIntentFilter() {
		// Do nothing.
	}

	@Override
	public boolean updateStorageHintOnResume() {
		return mFirstTimeInitialized;
	}

	@Override
	public void onResumeBeforeSuper() {
		Log.d(TAG,"###onResumeAfterSuper#time at=" + System.currentTimeMillis());
		mPaused = false;
	}

	private boolean prepareCamera() {
		// We need to check whether the activity is paused before long
		// operations to ensure that onPause() can be done ASAP.
		Log.v(TAG, "Open camera device.");
		mCameraDevice = CameraUtil.openCamera(mActivity, mCameraId, mHandler,
				mActivity.getCameraOpenErrorCallback());
		if (mCameraDevice == null) {
			Log.e(TAG, "Failed to open camera:" + mCameraId);
			return false;
		}
		mActivity.setCameraDevice(mCameraDevice);
		mParameters = mCameraDevice.getParameters();
		initializeCapabilities();
		if (mFocusManager == null)
			initializeFocusManager();
		setCameraParameters(UPDATE_PARAM_ALL);
		mHandler.sendEmptyMessage(CAMERA_OPEN_DONE);
		mCameraPreviewParamsReady = true;
		startPreview();
		mOnResumeTime = SystemClock.uptimeMillis();
		checkDisplayRotation();
		//initManualParameter();
		return true;
	}

	@Override
	public void onResumeAfterSuper() {
		// Add delay on resume from lock screen only, in order to to speed up
		// the onResume --> onPause --> onResume cycle from lock screen.
		// Don't do always because letting go of thread can cause delay.
		Log.d(TAG,"###YUHUAN###onResumeAfterSuper#time at=" + System.currentTimeMillis());
		String action = mActivity.getCurIntent().getAction();
		mCameraId = mActivity.getCameraId();
		if (MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA.equals(action)
				|| MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE
						.equals(action)) {
			Log.v(TAG, "On resume, from lock screen.");
			// Note: onPauseAfterSuper() will delete this runnable, so we will
			// at most have 1 copy queued up.
			mHandler.postDelayed(new Runnable() {
				public void run() {
					onResumeTasks();
				}
			}, ON_RESUME_TASKS_DELAY_MSEC);
		} else {
			Log.v(TAG, "On resume.");
			onResumeTasks();
		}
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mActivity.updateStorageSpaceAndHint();
			}

		});
		if (mActivity.getCameraId() != mCameraId) {
			mActivity.setCameraId(mCameraId);
		}

	}

	private void onResumeTasks() {
		Log.v(TAG, "###YUHUAN###onResumeTasks");
		if (mOpenCameraFail || mCameraDisabled)
			return;

		/* [BUG-899418] lijuan.zhang add,20150115 begin */
		if (!mCameraViewRefresh)
			mActivity.getCameraViewManager().refresh();
		/* [BUG-899418] lijuan.zhang add,20150115 end */
		Log.v(TAG, "###YUHUAN###onResumeTasks#mCameraOpenThread=" + mCameraOpenThread);
		if (mCameraOpenThread == null) {
			mCameraOpenThread = new CameraOpenThread();
			mCameraOpenThread.start();
		}

		if (mNamedImages == null)
			mNamedImages = new NamedImages();

		if (mShowProgress) {
			mActivity.getCameraViewManager().getRotateProgress().hide();
			mShowProgress = false;
		}

		mJpegPictureCallbackTime = 0;
		mZoomValue = 0;
		// modify by minghui.hua for PR918007
		// resetExposureCompensation();
		// if (!prepareCamera()) {
		// // Camera failure.
		// return;
		// }

		if (mSkinToneSeekBar != true) {
			Log.v(TAG, "Send tone bar: mSkinToneSeekBar = " + mSkinToneSeekBar);
			mHandler.sendEmptyMessage(SET_SKIN_TONE_FACTOR);
		}
		// If first time initialization is not finished, put it in the
		// message queue.
		// if (!mFirstTimeInitialized) {
		// mHandler.sendEmptyMessage(FIRST_TIME_INIT);
		// } else {
		// initializeSecondTime();
		// }
		mUI.initDisplayChangeListener();
		mZoomManager.setOnZoomIndexChangedListener(this);
		keepScreenOnAwhile();

		UsageStatistics.onContentViewChanged(UsageStatistics.COMPONENT_CAMERA,
				"PhotoModule");

		// Sensor gsensor =
		// mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// if (gsensor != null) {
		// mSensorManager.registerListener(this, gsensor,
		// SensorManager.SENSOR_DELAY_NORMAL);
		// }
		//
		// Sensor msensor =
		// mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		// if (msensor != null) {
		// mSensorManager.registerListener(this, msensor,
		// SensorManager.SENSOR_DELAY_NORMAL);
		// }

		if (GC.MODE_EXPRESSION4 == mGC.getCurrentMode()) {
			if (ExpressionThread.getExpressionIndex() != -1
					&& mActivity.getCameraViewManager() != null
					&& mActivity.getCameraViewManager()
							.checkExpressionThumbnail(
									ExpressionThread.getExpressionIndex())) {
				ExpressionThread.backForCheck();
			}
		}

	}

	@Override
	public void onPauseBeforeSuper() {
		mPaused = true;

		// Sensor gsensor =
		// mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// if (gsensor != null) {
		// mSensorManager.unregisterListener(this, gsensor);
		// }
		// // stop burstshot thread first.
		if (mLongpressFlag) {
			mActivity.getMediaSaveService().capturedDone4Burstshot(
					mReceivedSnapNum);
		}
		//
		// Sensor msensor =
		// mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		// if (msensor != null) {
		// mSensorManager.unregisterListener(this, msensor);
		// }

		// When user pressed home-key and re-entered the camera, should check
		// MODE behavior.
		
		int curMode = mGC.getCurrentMode();
		if (curMode == GC.MODE_EXPRESSION4
				&& ExpressionThread.isExpressionThreadActive()) {
			// If current mode is expression4 and not stop, don't set viewstate
			// to normal.
		} else {
			if (!mGC.getSwitchPhotoVideoState()
					&& !mActivity.getCameraViewManager()
							.getCameraManualModeManager().isShowing()) {
				Log.i(TAG, "LMCH0106 setviewstate normal");
				// Add by zhimin.yu for PR931912,PR942025, begin
				if (!(false == mGC.getCameraPreview())
						|| !isImageCaptureIntent()) {
					mActivity.setViewState(GC.VIEW_STATE_NORMAL);
				}
				// Add by zhimin.yu for PR931912,PR942025, end
			}
		}
		if (curMode == GC.MODE_MANUAL
				&& mActivity.getCameraViewManager().getTopViewManager().mShowingManualModel) {
			mActivity
					.getCameraViewManager()
					.getCameraManualModeManager()
					.hideManualMenu(
							mActivity.getCameraViewManager()
									.getTopViewManager()
									.getViewForHideManualModeMenu());
		}
		Log.d(TAG, "remove idle handleer in onPause");
		removeIdleHandler();
	}

	@Override
	public void onPauseAfterSuper() {
		Log.v(TAG, "On pause.");
		// mUI.showPreviewCover();//PR893130-sichao.hu removed

		try {
			if (mCameraOpenThread != null) {
				mCameraOpenThread.join();
			}
		} catch (InterruptedException e) {

		}

		if (mASDProcessThread != null) {
			synchronized (mASDCheckLock) {
				try {
					needToTurnOffAsd = true;
					mIsUnderASD = false;
					mASDCallbackData.notify();
				} catch (Exception e) {
				}
			}
			mASDProcessThread = null;
			mASDShowingState = ASD_AUTO;
		}
		mCameraOpenThread = null;
		// [BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-26,PR770858 Begin
		mUI.setBurstShootingVisibility(0);
		// [BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-26,PR770858 End

		// Reset the focus first. Camera CTS does not guarantee that
		// cancelAutoFocus is allowed after preview stops.
		if (mCameraDevice != null && mCameraState != PREVIEW_STOPPED) {
			mCameraDevice.cancelAutoFocus();
		}
		// If the camera has not been opened asynchronously yet,
		// and startPreview hasn't been called, then this is a no-op.
		// (e.g. onResume -> onPause -> onResume).
		stopPreview();

		mNamedImages = null;

		if (mLocationManager != null)
			mLocationManager.recordLocation(false);

		// If we are in an image capture intent and has taken
		// a picture, we just clear it in onPause.
		// Add by zhimin.yu for PR931912,PR942025, begin
		if (!(false == mGC.getCameraPreview()) || !isImageCaptureIntent()) {
			mJpegImageData = null;
		}
		// Add by zhimin.yu for PR931912,PR942025, end
		// Remove the messages and runnables in the queue.
		mHandler.removeCallbacksAndMessages(null);

		closeCamera();

		resetScreenOn();
		mUI.onPause();

		mPendingSwitchCameraId = -1;
		if (mFocusManager != null)
			mFocusManager.removeMessages();
		MediaSaveService s = mActivity.getMediaSaveService();
		if (s != null) {
			s.setListener(null);
		}
		mUI.removeDisplayChangeListener();
		mZoomManager.setOnZoomIndexChangedListener(null);
		unloadQRCodeSound();
		unloadBurstSound();
		mCameraViewRefresh = false;// [BUG-899418]lijuan.zhang add,20151115
	}

	/**
	 * The focus manager is the first UI related element to get initialized, and
	 * it requires the RenderOverlay, so initialize it here
	 */
	private void initializeFocusManager() {
		// Create FocusManager object. startPreview needs it.
		// if mFocusManager not null, reuse it
		// otherwise create a new instance
		if (mFocusManager != null) {
			mFocusManager.removeMessages();
		} else {
			CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];
			mMirror = (info.facing == CameraInfo.CAMERA_FACING_FRONT);
			String[] defaultFocusModes = mActivity
					.getResources()
					.getStringArray(R.array.pref_camera_focusmode_default_array);
			mFocusManager = new FocusOverlayManager(mPreferences,
					defaultFocusModes, mInitialParams, this, mMirror,
					mActivity.getMainLooper(), mUI);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.v(TAG, "onConfigurationChanged");
		setDisplayOrientation();
		resizeForPreviewAspectRatio();
		mUI.initPolaroidView(mActivity.getGC().getCurrentMode() == GC.MODE_POLAROID);
	}

	@Override
	public void updateCameraOrientation() {
		if (mDisplayRotation != CameraUtil.getDisplayRotation(mActivity)) {
			setDisplayOrientation();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CROP: {
			Intent intent = new Intent();
			if (data != null) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					intent.putExtras(extras);
				}
			}
			mActivity.setResultEx(resultCode, intent);
			mActivity.finish();

			File path = mActivity.getFileStreamPath(sTempCropFilename);
			path.delete();

			break;
		}
		case REQUEST_QRCODE_PICK_IMAGE: {
			Log.i(TAG, "LMCH04184 REQUEST_QRCODE_PICK_IMAGE");
			if (data == null)
				return;
			Uri selectedImage = data.getData();
			if (selectedImage == null)
				return;
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = mActivity.getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			new QRCodeScanTask(picturePath).execute();
			break;
		}
		// / add by yaping.liu for pr963472 {
		case SETGPS: {
			boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(
					mContentResolver,
					android.location.LocationManager.GPS_PROVIDER);
			boolean networkEnabled = Settings.Secure.isLocationProviderEnabled(
					mContentResolver,
					android.location.LocationManager.NETWORK_PROVIDER);
			CameraActivity.TraceLog(TAG, "onActivityResult : gpsEnabled = "
					+ gpsEnabled + " ; networkEnabled = " + networkEnabled);
			if (!(gpsEnabled || networkEnabled)) {
				if (isLocationOpened) {// Add by zhimin.yu for PR967300 begin
					isLocationOpened = false;
					setLocationPreference(RecordLocationPreference.VALUE_OFF);
				}
				return;
			}
			setGpsTagPreference(RecordLocationPreference.VALUE_ON);
			if (!isLocationOpened) {
				isLocationOpened = true;
				setLocationPreference(RecordLocationPreference.VALUE_ON);
			}// Add by zhimin.yu for PR967300 end
			boolean recordLocation = RecordLocationPreference.get(mPreferences,
					mContentResolver);
			CameraActivity.TraceLog(TAG,
					"onActivityResult->setLocationPreference(VALUE_ON) "
							+ "; recordLocation" + recordLocation);
			mLocationManager.recordLocation(recordLocation);
			mTopViewManager.refreshMenu();
			break;
		}
		// / }
		}
	}

	protected CameraManager.CameraProxy getCamera() {
		return mCameraDevice;
	}

	public ComboPreferences getPreferences() {
		return mPreferences;
	}

	private boolean canTakePicture() {
		return isCameraIdle()
				&& (mActivity.getStorageSpaceBytes() > Storage.LOW_STORAGE_THRESHOLD_BYTES);
	}

	@Override
	public void autoFocus() {
		if (null == mCameraDevice)
			return;
		mFocusStartTime = System.currentTimeMillis();
		mCameraDevice.autoFocus(mHandler, mAutoFocusCallback);
		setCameraState(FOCUSING);
	}

	@Override
	public void cancelAutoFocus() {
		if (null != mCameraDevice) {
			mCameraDevice.cancelAutoFocus();
			setCameraState(IDLE);
			setCameraParameters(UPDATE_PARAM_PREFERENCE);
		}
	}

	// Preview area is touched. Handle touch focus.
	@Override
	public void onSingleTapUp(View view, int x, int y) {
		if (mPaused || mCameraDevice == null || !mFirstTimeInitialized
				|| mCameraState == SNAPSHOT_IN_PROGRESS
				|| mCameraState == SWITCHING_CAMERA
				|| mCameraState == PREVIEW_STOPPED) {
			return;
		}
		if (mActivity.getGC().getCurrentMode() == GC.MODE_QRCODE) {
			if (mActivity.getCameraViewManager().getQrcodeViewManager().scanItem
					.getVisibility() == View.VISIBLE)
				mActivity.getCameraViewManager().getQrcodeViewManager().scanItem
						.setVisibility(View.GONE);
		}
		if (mActivity.getCameraViewManager().getCameraManualModeManager()
				.isShowing())
			return;
		if (!mUI.canSingleTapUp(view, x, y))
			return;
		// If Touch AF/AEC is disabled in UI, return
		if (this.mTouchAfAecFlag == false) {
			return;
		}
		// Check if metering area or focus area is supported.
		if (!mFocusAreaSupported && !mMeteringAreaSupported)
			return;

		if (mActivity.getCameraId() == GC.CAMERAID_BACK
				&& mActivity.getCameraViewManager()
						.getCameraManualModeManager().isAutoFocus())
			mFocusManager.onSingleTapUp(x, y);
		else if (mActivity.getCameraId() == GC.CAMERAID_FRONT
				&& mTouchAfAecFlag) {// PR930301-sichao.hu add begin
			mFocusManager.onSingleTapForTouchAE(x, y);
		}// PR930301-sichao.hu add end
	}

	@Override
	public boolean onBackPressed() {
		// if(mActivity.getGC().getCurrentMode() == GC.MODE_QRCODE){
		// if(mActivity.getCameraViewManager().getQrcodeViewManager().scanItem.getVisibility()
		// == View.VISIBLE){
		// mActivity.getCameraViewManager().getQrcodeViewManager().scanItem.setVisibility(View.GONE);
		// // return true;//PR931355-sichao.hu removed
		// return false;//PR931355-sichao.hu added
		// }
		// }
		if (mActivity.getCameraViewManager().dialogInShowing()) {
			mActivity.getCameraViewManager().hideDialog();
			mActivity.disableNavigationBar();
			return true;
		}
		// PR932963-sichao.hu add begin
		if (mTopViewManager.getShowMode()) {
			mTopViewManager.hideCaptureMode();
			mActivity.disableNavigationBar();
			return true;
		}// PR932963-sichao.hu add end

		int curMode = mGC.getCurrentMode();
		if (curMode == GC.MODE_EXPRESSION4
				&& ExpressionThread.isExpressionThreadActive()) {
			ExpressionThread.stopDetect();
			mActivity.getCameraViewManager().hideExpressionThumbnail();
			mActivity.setViewState(GC.VIEW_STATE_NORMAL);
			mGC.setExpressionState(false);
			mShutterManager.getShutterButton().setImageResource(
					R.drawable.btn_shutter_expression4);
			return true;
		}
		//
		// if(mIsFaceBeauty){
		// stopFB();
		// mActivity.notifyModeChanged(GC.MODE_PHOTO);
		// mSwitcherManager.showSwitcher();
		// return true;
		// if(mGC.getCurrentMode() == GC.MODE_MANUAL){
		// mCameraManualModeManager.hide();
		// mActivity.setViewState(GC.VIEW_STATE_HIDE_SET);
		// //reset settings
		// mActivity.getCameraViewManager().getCameraManualModeManager().resetISOETimeFocus();
		// }

		// huannglin 20150210 for PR927635
		/*
		if (mUI.isCountingDown()
				&& (mGC.getCurrentMode() == GC.MODE_ARCSOFT_FACEBEAUTY)) {
			mUI.cancelCountDownwithinFaceBeauty();
			return true;
		}*/

		// if(mGC.getCurrentMode()!=GC.MODE_PHOTO){
		// mActivity.setViewState(GC.VIEW_STATE_NORMAL);
		// mActivity.backToPhotoMode();
		// return true;
		// }

		// modify by minghui.hua for PR941666
		if (mFocusManager != null && !mFocusManager.isFocusCompleted()
				&& curMode == GC.MODE_PHOTO) {
			cancelAutoFocus();
		}
		// Add by fei.gao for (back in facebeauty ,but the menu's value not
		// save) -begin
		/*
		if (mGC.getCurrentMode() != GC.MODE_ARCSOFT_FACEBEAUTY) {
			stopFB();
		}*/
		// Add by fei.gao for (back in facebeauty ,but the menu's value not
		// save) -end
		return mUI.onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		// [BUGFIX]-MOD-begin chao.luo, PR-752855, 2014-8-8
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_HEADSETHOOK:
			// case KeyEvent.KEYCODE_FOCUS:
			// if (/*TODO: mActivity.isInCameraApp() &&*/ mFirstTimeInitialized)
			// {
			// if (event.getRepeatCount() == 0) {
			// onShutterButtonFocus(true);
			// }
			// return true;
			// }
			Log.d(TAG, "onKeyDown");
			if (mCameraState == PREVIEW_STOPPED) {
				mVolumeKeyDownAtEve = true;
			}
			if (event.isLongPress()) {
				mCameraKeyLongPressed = true;
				onShutterButtonLongClick();
			}
			return true;
		case KeyEvent.KEYCODE_FOCUS:
			// [BUGFIX]-MOD-end chao.luo, PR-752855, 2014-8-8
			return false;
			// case KeyEvent.KEYCODE_CAMERA:
			// if (mFirstTimeInitialized && event.getRepeatCount() == 0) {
			// onShutterButtonClick();
			// }
			// return true;
			// case KeyEvent.KEYCODE_DPAD_LEFT:
			// if ( (mCameraState != PREVIEW_STOPPED) &&
			// (mFocusManager.getCurrentFocusState() !=
			// mFocusManager.STATE_FOCUSING) &&
			// (mFocusManager.getCurrentFocusState() !=
			// mFocusManager.STATE_FOCUSING_SNAP_ON_FINISH) ) {
			// if (mbrightness > MINIMUM_BRIGHTNESS) {
			// mbrightness-=mbrightness_step;
			// /* Set the "luma-adaptation" parameter */
			// mParameters = mCameraDevice.getParameters();
			// mParameters.set("luma-adaptation", String.valueOf(mbrightness));
			// mCameraDevice.setParameters(mParameters);
			// }
			// //brightnessProgressBar.setProgress(mbrightness);
			// //brightnessProgressBar.setVisibility(View.VISIBLE);
			// }
			// break;
			// case KeyEvent.KEYCODE_DPAD_RIGHT:
			// if ( (mCameraState != PREVIEW_STOPPED) &&
			// (mFocusManager.getCurrentFocusState() !=
			// mFocusManager.STATE_FOCUSING) &&
			// (mFocusManager.getCurrentFocusState() !=
			// mFocusManager.STATE_FOCUSING_SNAP_ON_FINISH) ) {
			// if (mbrightness < MAXIMUM_BRIGHTNESS) {
			// mbrightness+=mbrightness_step;
			// /* Set the "luma-adaptation" parameter */
			// mParameters = mCameraDevice.getParameters();
			// mParameters.set("luma-adaptation", String.valueOf(mbrightness));
			// mCameraDevice.setParameters(mParameters);
			// }
			// //brightnessProgressBar.setProgress(mbrightness);
			// //brightnessProgressBar.setVisibility(View.VISIBLE);
			// }
			// break;
			// case KeyEvent.KEYCODE_DPAD_CENTER:
			// // If we get a dpad center event without any focused view, move
			// // the focus to the shutter button and press it.
			// if (mFirstTimeInitialized && event.getRepeatCount() == 0) {
			// // Start auto-focus immediately to reduce shutter lag. After
			// // the shutter button gets the focus, onShutterButtonFocus()
			// // will be called again but it is fine.
			// onShutterButtonFocus(true);
			// mUI.pressShutterButton();
			// }
			// return true;
		}
		if (mCameraState == IDLE) {
			if (mActivity.getCameraViewManager().onKeyDown(keyCode, event))
				return true;
		}
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_HEADSETHOOK:
			// [BUGFIX]-MOD-begin chao.luo, PR-752855, 2014-8-8
			if (/* mActivity.isInCameraApp() && */mFirstTimeInitialized
					&& !mCameraKeyLongPressed
					&& mShutterManager.getShutterType() != ShutterManager.SHUTTER_TYPE_OK_CANCEL) {
				if (mShutterManager.isShutterButtonEnabled()
						&& !mVolumeKeyDownAtEve
						&& mCameraState != PREVIEW_STOPPED) {
					onShutterButtonClick();
				} else if (mVolumeKeyDownAtEve) {
					mVolumeKeyDownAtEve = false;
				}
			}
			if (mCameraKeyLongPressed) {
				onShutterButtonFocus(false);
				mCameraKeyLongPressed = false;
				return true;
			}
			return true;
			// [BUGFIX]-MOD-end chao.luo, PR-752855, 2014-8-8
		case KeyEvent.KEYCODE_FOCUS:
			if (mFirstTimeInitialized) {
				onShutterButtonFocus(false);
			}
			return true;
		}
		// [BUGFIX]-ADD-BEGIN by kechao.chen, PR-765640, 2014-8-27
		if (mCameraState == IDLE) {
			if (mActivity.getCameraViewManager().onKeyUp(keyCode, event))
				return true;
		}
		// [BUGFIX]-ADD-BEGIN by kechao.chen, PR-765640, 2014-8-27
		return false;
	}

	@Override
	public void doWhenCloseActivity() {
		closeAllMode();
	}

	private void closeCamera() {
		Log.v(TAG, "Close camera device.");
		if (mCameraDevice != null) {
			mCameraDevice.setZoomChangeListener(null);
			mCameraDevice.setFaceDetectionCallback(null, null);
			mCameraDevice.setErrorCallback(null);
			//checkPreviewCallback(true);

			if (mActivity.isSecureCamera()
					&& !CameraActivity.isFirstStartAfterScreenOn()) {
				// Blocks until camera is actually released.
				CameraHolder.instance().strongRelease();
			} else {
				CameraHolder.instance().release();
			}
			CameraActivity.TraceQCTLog("Camera Closed");

			mFaceDetectionStarted = false;
			mCameraDevice = null;
			setCameraState(PREVIEW_STOPPED);
			mFocusManager.onCameraReleased();
		}
	}

	private void setDisplayOrientation() {
		mDisplayRotation = CameraUtil.getDisplayRotation(mActivity);
		mDisplayOrientation = CameraUtil.getDisplayOrientation(
				mDisplayRotation, mCameraId);
		mCameraDisplayOrientation = mDisplayOrientation;
		mUI.setDisplayOrientation(mDisplayOrientation);
		if (mFocusManager != null) {
			mFocusManager.setDisplayOrientation(mDisplayOrientation);
		}
		// Change the camera display orientation
		if (mCameraDevice != null) {
			int orientation = mCameraDisplayOrientation;
			if (mActivity.isReversibleEnabled()) {
				if (mOrientation == 180) {
					orientation = 270;
				} else if (mOrientation == 0) {
					orientation = 90;
				}
			}
			CameraActivity.TraceLog(TAG, "setOrientation for " + orientation);
			mCameraDevice.setDisplayOrientation(orientation);
		}
	}

	/** Only called by UI thread. */
	private void setupPreview() {
		mFocusManager.resetTouchFocus();
		startPreview();
	}

	boolean mIgnoreFaceDetection = false;// PR940002-sichao.hu added

	private boolean mIsASDCallbacking = false;

	/*
	void checkPreviewCallback(boolean needRemoveCallback) {
		if (mCameraDevice == null) {
			return;
		}
		mIgnoreFaceDetection = false;// PR940002-sichao.hu added

		mASDProcessThread = null;
		if (needRemoveCallback) {
			mPreviewBuffer = null;
			mIsASDCallbacking = false;
			mCameraDevice.setPreviewDataCallback(mHandler, null);
			isASDFrameCallbackClosed = true;
			if (mASDProcessThread != null) {
				synchronized (mASDCheckLock) {
					try {
						mIsUnderASD = false;
						mASDCheckLock.notify();
					} catch (Exception e) {
					}
				}
			}
			return;
		}
		if (mGC.getCurrentMode() == GC.MODE_QRCODE) {
			mPreviewBuffer = null;
			mIgnoreFaceDetection = true;// PR940002-sichao.hu added
			mCameraDevice.setPreviewDataCallback(mHandler, qrcodecb);
			mIsASDCallbacking = false;
			return;
		}
		
		boolean isASD = mActivity.getString(R.string.setting_on_value).equals(
				mPreferences.getString(CameraSettings.KEY_CAMERA_ARCSOFT_ASD,
						mActivity.getString(R.string.setting_off_value)));
		isASD = isASD & (mCameraId == 0)
				& (mGC.getCurrentMode() != GC.MODE_HDR)
				& (mGC.getCurrentMode() != GC.MODE_MANUAL);
		Log.i(TAG, "asdlog isASD= " + isASD);
		if (!isASD) {
			mAsdState = ASD_AUTO;
			mASDShowingState = ASD_AUTO;
			if (mGC.getCurrentMode() == GC.MODE_HDR)
				mSceneMode = Parameters.SCENE_MODE_HDR;
			else
				mSceneMode = Parameters.SCENE_MODE_AUTO;
			// mParameters.setSceneMode(mSceneMode);
		}

		mIsEnableNight = mActivity.getString(R.string.setting_on_value).equals(
				mPreferences.getString(
						CameraSettings.KEY_CAMERA_ARCSOFT_NIGHT,
						CustomUtil.getInstance().getString(
								CustomFields.DEF_PHOTO_ARCSOFT_NIGHT_DEFAULT,
								"off")));
		mGC.flagArcsoftNight(mIsEnableNight);

		if (isASD && mActivity.isNonePickIntent()) {
			isASDFrameCallbackClosed = false;
			if (mIsASDCallbacking) {
				return;
			}
			CameraActivity.TraceQCTLog("Start ASD");
			int bufferSize = mParameters.getPreviewSize().width
					* mParameters.getPreviewSize().height * 3 / 2;
			if (mPreviewBuffer == null || mPreviewBuffer.length != bufferSize) {
				mPreviewBuffer = null;
				mPreviewBuffer = new byte[bufferSize];
			}
			mCameraDevice.addCallbackBuffer(mPreviewBuffer);
			mCameraDevice.setPreviewDataCallbackWithBuffer(mHandler, arcsoftCB);
			mIsASDCallbacking = true;
			mIsUnderASD = true;
		} else {
			mPreviewBuffer = null;
			mCameraDevice.setPreviewDataCallback(mHandler, null);
			mIsASDCallbacking = false;
			isASDFrameCallbackClosed = true;
			if (mASDProcessThread != null) {
				synchronized (mASDCallbackData) {
					try {
						mIsUnderASD = false;
						mASDCallbackData.notify();
					} catch (Exception e) {
					}
				}
				mASDProcessThread = null;
			}
		}
	}*/

	private boolean isASDFrameCallbackClosed = false;

	private void restartPreview() {
		if (mPaused || mCameraDevice == null) {
			return;
		}
		Log.i(TAG, "startPreview");
		// Any decisions we make based on the surface texture state
		// need to be protected.
		SurfaceTexture st = mUI.getSurfaceTexture();
		if (st == null) {
			CameraActivity.TraceLog(TAG, "surfaceTexture is not ready");
			Log.w(TAG, "startPreview: surfaceTexture is not ready.");
			return;
		}
		// Let UI set its expected aspect ratio
		mCameraDevice.setPreviewTexture(st);
		if (mCameraState != PREVIEW_STOPPED) {
			stopPreview();
		}

		//checkPreviewCallback(false);
		setCameraState(IDLE);
		mCameraDevice.startPreview();
		CameraActivity.TraceQCTLog("StartPreview", new Throwable());
		mIsPreviewStarted = true;
	}

	private interface IParameterStateListener {
		public void onParameterUpdated();
	}

	private IParameterStateListener mParameterStateListener = null;

	boolean mIsPreviewStarted = false;

	/**
	 * This can run on a background thread, post any view updates to
	 * MainHandler.
	 */
	/*
	 * Add synchronized to avoid double-start-preview when there's a race
	 * between open camera thread and UI thread(in onPreviewUIReady method)
	 */
	private synchronized void startPreview() {
		CameraActivity.TraceLog(TAG, "Call start preview");
		if (mPaused || mCameraDevice == null) {
			return;
		}
		Log.i(TAG, "startPreview");
		// Any decisions we make based on the surface texture state
		// need to be protected.
		// Add by zhimin.yu for PR931912,PR942025, begin
		if ((false == mGC.getCameraPreview()) && isImageCaptureIntent()) {
			return;
		}
		// Add by zhimin.yu for PR931912,PR942025, end
		SurfaceTexture st = mUI.getSurfaceTexture();
		if (st == null) {
			CameraActivity.TraceLog(TAG, "surfaceTexture is not ready");
			Log.w(TAG, "startPreview: surfaceTexture is not ready.");
			return;
		}
		// Let UI set its expected aspect ratio
		mCameraDevice.setPreviewTexture(st);

		if (!mCameraPreviewParamsReady) {
			CameraActivity.TraceLog(TAG, "parameters for preview is not ready");
			Log.w(TAG, "startPreview: parameters for preview is not ready.");
			return;
		}
		mCameraDevice.setErrorCallback(mErrorCallback);
		// ICS camera frameworks has a bug. Face detection state is not cleared
		// 1589
		// after taking a picture. Stop the preview to work around it. The bug
		// was fixed in JB.
		Log.d(TAG,"###YUHUAN###startPreview#mCameraState=" + mCameraState);
		if (mCameraState != PREVIEW_STOPPED) {
			stopPreview();
		}

		mHandler.sendEmptyMessage(INIT_PREVIEW_PARAMETERS);
		mParameterStateListener = new IParameterStateListener() {
			@Override
			public void onParameterUpdated() {
				//checkPreviewCallback(false);

				mCameraDevice.startPreview();
				mIsPreviewStarted = true;

				setDisplayOrientation();
				Log.v(TAG, "startPreview");

				if (!mSnapshotOnIdle) {
					// If the focus mode is continuous autofocus, call
					// cancelAutoFocus to
					// resume it because it may have been paused by autoFocus
					// call.
					if (mFocusManager == null) {
						initializeFocusManager();
					}
					if (CameraUtil.FOCUS_MODE_CONTINUOUS_PICTURE
							.equals(mFocusManager.getFocusMode())) {
						mCameraDevice.cancelAutoFocus();
					}
					mFocusManager.setAeAwbLock(false); // Unlock AE and AWB.
				}

				mFocusManager.onPreviewStarted();
				mHandler.sendEmptyMessage(ON_PREVIEW_STARTED);

				isInSmileSnap = false;
				if (mSnapshotOnIdle) {
					mHandler.post(mDoSnapRunnable);
				}
			}

		};

	}

	@Override
	public void stopPreview() {
		if (mCameraDevice != null && mCameraState != PREVIEW_STOPPED) {
			Log.v(TAG, "stopPreview");
			CameraActivity.TraceQCTLog("StopPreview");
			mCameraDevice.stopPreview();
			mIsPreviewStarted = false;
			//checkPreviewCallback(true);
			// if(mGC.getCurrentMode() == GC.MODE_MANUAL &&
			// mActivity.getCameraViewManager().getTopViewManager().mShowingManualModel
			// &&
			// !mActivity.getCameraViewManager().getCameraManualModeManager().isDoingManualMode()){
			// mActivity.getCameraViewManager().getCameraManualModeManager().hideManualMenu(mActivity.getCameraViewManager().getTopViewManager().getViewForHideManualModeMenu());
			// }
		}
		setCameraState(PREVIEW_STOPPED);
		// modify for PR966464
		long pid = Thread.currentThread().getId();
		CameraActivity.TraceLog(TAG, "stopPreview pid=" + pid);
		if (pid != 1) {
			mHandler.post(new Runnable() {
				public void run() {
					if (mFocusManager != null)
						mFocusManager.onPreviewStopped();
				}
			});
		} else {
			if (mFocusManager != null)
				mFocusManager.onPreviewStopped();
		}
	}

	@SuppressWarnings("deprecation")
	private void updateCameraParametersInitialize() {
		// Reset preview frame rate to the maximum because it may be lowered by
		// video camera application.
		int[] fpsRange = CameraUtil.getPhotoPreviewFpsRange(mParameters);
        Log.d(TAG,"###YUHUAN###updateCameraParametersInitialize#fpsRange[0]=" + fpsRange[0]);
        Log.d(TAG,"###YUHUAN###updateCameraParametersInitialize#fpsRange[1]=" + fpsRange[1]);
		checkThread();
		if (fpsRange != null && fpsRange.length > 0) {
			mParameters.setPreviewFpsRange(
					fpsRange[Parameters.PREVIEW_FPS_MIN_INDEX],
					fpsRange[Parameters.PREVIEW_FPS_MAX_INDEX]);
		}

		mParameters.set(CameraUtil.RECORDING_HINT, CameraUtil.FALSE);

		// Disable video stabilization. Convenience methods not available in API
		// level <= 14
		String vstabSupported = mParameters
				.get("video-stabilization-supported");
		Log.d(TAG,"###YUHUAN###updateCameraParametersInitialize#vstabSupported=" + vstabSupported);
		if ("true".equals(vstabSupported)) {
			mParameters.set("video-stabilization", "false");
		}
	}

	private void updateCameraParametersZoom() {
		// Set zoom.
		if (mParameters.isZoomSupported()) {
			Parameters p = mCameraDevice.getParameters();
			mZoomValue = p.getZoom();
			Log.d(TAG,"###YUHUAN###updateCameraParametersZoom#mZoomValue=" + mZoomValue);
			checkThread();
			mParameters.setZoom(mZoomValue);
		}
	}

	private boolean needRestart() {
		mRestartPreview = false;
		String zsl = mPreferences.getString(
				CameraSettings.KEY_ZSL,
				mActivity.getString(R.string.setting_off_value));
		Log.d(TAG,"###YUHUAN###needRestart#zsl=" + zsl);
		if (zsl.equals("on")
				&& mSnapshotMode != CAMERA_SUPPORT_MODE_ZSL
				&& (/*
					 * mASDShowingState != ASD_NIGHT && mASDShowingState !=
					 * ASD_BACKLIT &&
					 */!mParameters.getSceneMode().equals(
						Parameters.SCENE_MODE_HDR) && needZSLOn)// PR925998,v-nj-feiqiang.cheng
				&& mCameraState != PREVIEW_STOPPED) {
			// Switch on ZSL Camera mode
			Log.v(TAG, "luna Switching to ZSL Camera Mode. Restart Preview");
			mRestartPreview = true;
			return mRestartPreview;
		}
		if (zsl.equals("off") && mSnapshotMode != CAMERA_SUPPORT_MODE_NONZSL
				&& mCameraState != PREVIEW_STOPPED) {
			// Switch on Normal Camera mode
			mRestartPreview = true;
			return mRestartPreview;
		}
		return mRestartPreview;
	}

	private void qcomUpdateAdvancedFeatures(String ubiFocus,
			String chromaFlash, String optiZoom) {
		// TODO
		checkThread();
		
		/*
		if (CameraUtil.isSupported(ubiFocus,
				CameraSettings.getSupportedAFBracketingModes(mParameters))) {
			mParameters.set(CameraSettings.KEY_QC_AF_BRACKETING, ubiFocus);
		}
		if (CameraUtil.isSupported(chromaFlash,
				CameraSettings.getSupportedChromaFlashModes(mParameters))) {
			mParameters.set(CameraSettings.KEY_QC_CHROMA_FLASH, chromaFlash);
		}
		if (CameraUtil.isSupported(optiZoom,
				CameraSettings.getSupportedOptiZoomModes(mParameters))) {
			mParameters.set(CameraSettings.KEY_QC_OPTI_ZOOM, optiZoom);
		}
		*/
	}

	private void qcomUpdateCameraParametersPreference() {
		ExtendParameters extParams = ExtendParameters.getInstance(mParameters);
		checkThread();
		// Set Brightness.
		//mParameters.set("luma-adaptation", String.valueOf(mbrightness));
		extParams.SetBrightness(String.valueOf(mbrightness));

		if (Parameters.SCENE_MODE_AUTO.equals(mSceneMode)
				|| Parameters.SCENE_MODE_HDR.equals(mSceneMode)) {
			// Set Touch AF/AEC parameter.
			String touchAfAec = mPreferences
					.getString(CameraSettings.KEY_TOUCH_AF_AEC, mActivity
							.getString(R.string.pref_camera_touchafaec_default));
			if (CameraUtil.isSupported(touchAfAec,
					extParams.getSupportedTouchAfAec())) {
				mCurrTouchAfAec = touchAfAec;
				extParams.setTouchAfAec(touchAfAec);
				this.mTouchAfAecFlag = "touch-on".equals(touchAfAec) ? true
						: false;
			}
		} else {
			extParams.setTouchAfAec("touch-off");
			mFocusManager.resetTouchFocus();
		}

		// qcom Related Parameter update
		// Set Brightness.
		/*
		 * mParameters.set("luma-adaptation", String.valueOf(mbrightness));
		 * 
		 * if (Parameters.SCENE_MODE_AUTO.equals(mSceneMode)) { // Set Touch
		 * AF/AEC parameter. String touchAfAec = mPreferences.getString(
		 * CameraSettings.KEY_TOUCH_AF_AEC,
		 * mActivity.getString(R.string.pref_camera_touchafaec_default)); if
		 * (CameraUtil.isSupported(touchAfAec,
		 * mParameters.getSupportedTouchAfAec())) { mCurrTouchAfAec =
		 * touchAfAec; mParameters.setTouchAfAec(touchAfAec); } } else {
		 * mParameters.setTouchAfAec(mParameters.TOUCH_AF_AEC_OFF);
		 * mFocusManager.resetTouchFocus(); } try {
		 * if(mParameters.getTouchAfAec().equals(mParameters.TOUCH_AF_AEC_ON))
		 * this.mTouchAfAecFlag = true; else this.mTouchAfAecFlag = false; }
		 * catch(Exception e){ Log.e(TAG, "Handled NULL pointer Exception"); }
		 */

		// Set Picture Format
		// Picture Formats specified in UI should be consistent with
		// PIXEL_FORMAT_JPEG and PIXEL_FORMAT_RAW constants
		String pictureFormat = mPreferences
				.getString(CameraSettings.KEY_PICTURE_FORMAT, mActivity
						.getString(R.string.pref_camera_picture_format_default));
		mParameters.set(KEY_PICTURE_FORMAT, pictureFormat);

		// Set JPEG quality.
		String jpegQuality = mPreferences.getString(
				CameraSettings.KEY_JPEG_QUALITY,
				mActivity.getString(R.string.pref_camera_jpegquality_default));
		// mUnsupportedJpegQuality = false;
		Size pic_size = mParameters.getPictureSize();
		if (pic_size == null) {
			Log.e(TAG, "error getPictureSize: size is null");
		} else {
			if ("100".equals(jpegQuality) && (pic_size.width >= 3200)) {
				// mUnsupportedJpegQuality = true;
			} else {
				// mParameters.setJpegQuality(JpegEncodingQualityMappings.getQualityNumber(jpegQuality));
				mParameters.setJpegQuality(100);
			}
		}

		// Set Selectable Zone Af parameter.
		String selectableZoneAf = mPreferences
				.getString(
						CameraSettings.KEY_SELECTABLE_ZONE_AF,
						mActivity
								.getString(R.string.pref_camera_selectablezoneaf_default));
		if (CameraUtil.isSupported(selectableZoneAf,
				extParams.getSupportedSelectableZoneAf())) {
			extParams.setSelectableZoneAf(selectableZoneAf);
		}

		// Set wavelet denoise mode
		String Denoise = mPreferences.getString(CameraSettings.KEY_DENOISE,
				mActivity.getString(R.string.pref_camera_denoise_default));
		if (CameraUtil.isSupported(Denoise,
				extParams.getSupportedDenoiseModes())) {
			extParams.setDenoise(Denoise);
		}
		// Set Redeye Reduction
		String redeyeReduction = mPreferences
				.getString(
						CameraSettings.KEY_REDEYE_REDUCTION,
						mActivity
								.getString(R.string.pref_camera_redeyereduction_default));
		if (CameraUtil.isSupported(redeyeReduction,
				extParams.getSupportedRedeyeReductionModes())) {
			extParams.setRedeyeReductionMode(redeyeReduction);
		}
		/*
		 * String selectableZoneAf = mPreferences.getString(
		 * CameraSettings.KEY_SELECTABLE_ZONE_AF,
		 * mActivity.getString(R.string.pref_camera_selectablezoneaf_default));
		 * List<String> str = mParameters.getSupportedSelectableZoneAf(); if
		 * (CameraUtil.isSupported(selectableZoneAf,
		 * mParameters.getSupportedSelectableZoneAf())) {
		 * mParameters.setSelectableZoneAf(selectableZoneAf); }
		 * 
		 * // Set wavelet denoise mode if
		 * (mParameters.getSupportedDenoiseModes() != null) { String Denoise =
		 * mPreferences.getString( CameraSettings.KEY_DENOISE,
		 * mActivity.getString(R.string.pref_camera_denoise_default));
		 * mParameters.setDenoise(Denoise); } // Set Redeye Reduction String
		 * redeyeReduction = mPreferences.getString(
		 * CameraSettings.KEY_REDEYE_REDUCTION,
		 * mActivity.getString(R.string.pref_camera_redeyereduction_default));
		 * if (CameraUtil.isSupported(redeyeReduction,
		 * mParameters.getSupportedRedeyeReductionModes())) {
		 * mParameters.setRedeyeReductionMode(redeyeReduction); } // Set ISO
		 * parameter
		 */
		// use resume parameters ,so no need to work in manualmode
		if (mGC.getCurrentMode() != GC.MODE_MANUAL) {
			String iso = mPreferences.getString(CameraSettings.KEY_ISO,
					mActivity.getString(R.string.pref_camera_iso_default));
			// if (CameraUtil.isSupported(iso,
			// mParameters.getSupportedIsoValues())) {
			// mParameters.setISOValue(iso);
			// }

			String isoValue = extParams.parseISOValueFormat(iso);

			if (CameraUtil.isSupported(isoValue,
					extParams.getSupportedISOValues())) {
				extParams.setISOValue(isoValue);
			}
		}

		// Set color effect parameter.
		String colorEffect = mPreferences.getString(
				CameraSettings.KEY_COLOR_EFFECT,
				mActivity.getString(R.string.pref_camera_coloreffect_default));
		Log.v(TAG, "Color effect value =" + colorEffect);
		if (CameraUtil.isSupported(colorEffect,
				mParameters.getSupportedColorEffects())) {
			mParameters.setColorEffect(colorEffect);
		}
		// Set Saturation
		String saturationStr = mPreferences.getString(
				CameraSettings.KEY_SATURATION,
				mActivity.getString(R.string.pref_camera_saturation_default));
		int saturation = Integer.parseInt(saturationStr);
		Log.v(TAG, "Saturation value =" + saturation);
		if (0 <= saturation) {
			extParams.setSaturation(String.valueOf(saturation));
		}
		// Set contrast parameter.
		String contrastStr = mPreferences.getString(
				CameraSettings.KEY_CONTRAST,
				mActivity.getString(R.string.pref_camera_contrast_default));
		int contrast = Integer.parseInt(contrastStr);
		Log.v(TAG, "Contrast value =" + contrast);
		if (0 <= contrast) {
			extParams.setContrast(String.valueOf(contrast));
		}
		// Set sharpness parameter TODO
		String sharpnessStr = mPreferences.getString(
				CameraSettings.KEY_SHARPNESS,
				mActivity.getString(R.string.pref_camera_sharpness_default));
		int maxSharpness = mParameters.get("max-sharpness") == null ? 0
				: Integer.valueOf(mParameters.get("max-sharpness"));
		int sharpness = Integer.parseInt(sharpnessStr)
				* (maxSharpness / MAX_SHARPNESS_LEVEL);
		Log.v(TAG, "Sharpness value =" + sharpness);
		if (0 <= sharpness) {
			extParams.setSharpness(String.valueOf(sharpness));
		}

		// Set Face Recognition TODO
		/*
		String faceRC = mPreferences.getString(
				CameraSettings.KEY_FACE_RECOGNITION,
				mActivity.getString(R.string.pref_camera_facerc_default));
		Log.v(TAG, "Face Recognition value = " + faceRC);
		if (CameraUtil.isSupported(faceRC,
				CameraSettings.getSupportedFaceRecognitionModes(mParameters))) {
			mParameters.set(CameraSettings.KEY_QC_FACE_RECOGNITION, faceRC);
		}*/
		
		// Set AE Bracketing TODO
		String aeBracketing = mPreferences
				.getString(CameraSettings.KEY_AE_BRACKET_HDR, mActivity
						.getString(R.string.pref_camera_ae_bracket_hdr_default));
		Log.v(TAG, "AE Bracketing value =" + aeBracketing);
		
		/*
		if (CameraUtil.isSupported(aeBracketing,
				CameraSettings.getSupportedAEBracketingModes(mParameters))) {
			mParameters.set(CameraSettings.KEY_QC_AE_BRACKETING, aeBracketing);
		}*/
		
		// Set Advanced features. TODO
		String advancedFeature = mPreferences
				.getString(
						CameraSettings.KEY_ADVANCED_FEATURES,
						mActivity
								.getString(R.string.pref_camera_advanced_feature_default));
		Log.v(TAG, " advancedFeature value =" + advancedFeature);

		if (advancedFeature != null) {
			String ubiFocusOff = mActivity
					.getString(R.string.pref_camera_advanced_feature_value_ubifocus_off);
			String chromaFlashOff = mActivity
					.getString(R.string.pref_camera_advanced_feature_value_chromaflash_off);
			String optiZoomOff = mActivity
					.getString(R.string.pref_camera_advanced_feature_value_optizoom_off);

			if (advancedFeature
					.equals(mActivity
							.getString(R.string.pref_camera_advanced_feature_value_ubifocus_on))) {
				qcomUpdateAdvancedFeatures(advancedFeature, chromaFlashOff,
						optiZoomOff);
			} else if (advancedFeature
					.equals(mActivity
							.getString(R.string.pref_camera_advanced_feature_value_chromaflash_on))) {
				qcomUpdateAdvancedFeatures(ubiFocusOff, advancedFeature,
						optiZoomOff);
			} else if (advancedFeature
					.equals(mActivity
							.getString(R.string.pref_camera_advanced_feature_value_optizoom_on))) {
				qcomUpdateAdvancedFeatures(ubiFocusOff, chromaFlashOff,
						advancedFeature);
			} else {
				qcomUpdateAdvancedFeatures(ubiFocusOff, chromaFlashOff,
						optiZoomOff);
			}
		}
		// Set auto exposure parameter.
		String autoExposure = mPreferences.getString(
				CameraSettings.KEY_AUTOEXPOSURE,
				mActivity.getString(R.string.pref_camera_autoexposure_default));
		Log.v(TAG, "autoExposure value =" + autoExposure);
		if (CameraUtil.isSupported(autoExposure,
				extParams.getSupportedAutoexposure())) {
			extParams.setAutoExposure(autoExposure);
		}
		String defAntibanding = CustomUtil.getInstance().getString(
				CustomFields.DEF_ANTIBAND_DEFAULT, "auto");
		String antiBanding = defAntibanding;
		if (CustomUtil.getInstance().needResetAnti()) {
			antiBanding = CustomUtil.getInstance().getMccMncFrecuency();
			CameraActivity.TraceLog(TAG, "reset antiBanding=" + antiBanding);
			if (TextUtils.isEmpty(antiBanding)) {
				antiBanding = mPreferences.getString(
						CameraSettings.KEY_ANTIBANDING, defAntibanding);
			} else {
				mPreferences.edit()
						.putString(CameraSettings.KEY_ANTIBANDING, antiBanding)
						.commit();
			}
		} else {
			// FR 720721 , sichao.hu modified
			antiBanding = mPreferences.getString(
					CameraSettings.KEY_ANTIBANDING, defAntibanding);
		}
		CameraActivity.TraceLog(TAG, "antiBanding value =" + antiBanding);
		// Set anti banding parameter.
		if (CameraUtil.isSupported(antiBanding,
				mParameters.getSupportedAntibanding())) {
			mParameters.setAntibanding(antiBanding);
		}

		updateZsl();

		// Set face detetction parameter.
		String faceDetection = mPreferences
				.getString(CameraSettings.KEY_FACE_DETECTION, mActivity
						.getString(R.string.pref_camera_facedetection_default));

		// only for qcom
		if (CameraUtil.isSupported(faceDetection,
				extParams.getSupportedFDModeValues())) {
			extParams.setFaceDetectionMode(faceDetection);
		}

		if (mIgnoreFaceDetection) {// PR940002-sichao.hu add begin
			mUI.clearFaces();
			stopFaceDetection();
			mFaceDetectionEnabled = false;
		} else {// PR940002-sichao.hu add end

			if ("on".equals(faceDetection) && mFaceDetectionEnabled == false) {
				mFaceDetectionEnabled = true;
				startFaceDetection();
			} else if ("off".equals(faceDetection)
					&& mFaceDetectionEnabled == true) {
				stopFaceDetection();
				mFaceDetectionEnabled = false;
			}
		}

		// skin tone ie enabled only for auto,party and portrait BSM
		// when color effects are not enabled
		if ((Parameters.SCENE_MODE_PARTY.equals(mSceneMode) || Parameters.SCENE_MODE_PORTRAIT
				.equals(mSceneMode))
				&& (Parameters.EFFECT_NONE.equals(colorEffect))) {
			// Set Skin Tone Correction factor
			Log.v(TAG, "set tone bar: mSceneMode = " + mSceneMode);
			if (mSeekBarInitialized == true)
				mHandler.sendEmptyMessage(SET_SKIN_TONE_FACTOR);
		}

		// TODO
		// if(!Parameters.FOCUS_MODE_FIXED.equals(mParameters.getFocusMode())){
		// this.mTouchAfAecFlag = true;
		// }else{
		// this.mTouchAfAecFlag = false;
		// }
		/*
		 * // Set color effect parameter. String colorEffect =
		 * mPreferences.getString( CameraSettings.KEY_COLOR_EFFECT,
		 * mActivity.getString(R.string.pref_camera_coloreffect_default));
		 * Log.v(TAG, "Color effect value =" + colorEffect); if
		 * (CameraUtil.isSupported(colorEffect,
		 * mParameters.getSupportedColorEffects())) {
		 * mParameters.setColorEffect(colorEffect); } //Set Saturation String
		 * saturationStr = mPreferences.getString(
		 * CameraSettings.KEY_SATURATION,
		 * mActivity.getString(R.string.pref_camera_saturation_default)); int
		 * saturation = Integer.parseInt(saturationStr); Log.v(TAG,
		 * "Saturation value =" + saturation); if((0 <= saturation) &&
		 * (saturation <= mParameters.getMaxSaturation())){
		 * mParameters.setSaturation(saturation); } // Set contrast parameter.
		 * String contrastStr = mPreferences.getString(
		 * CameraSettings.KEY_CONTRAST,
		 * mActivity.getString(R.string.pref_camera_contrast_default)); int
		 * contrast = Integer.parseInt(contrastStr); Log.v(TAG,
		 * "Contrast value =" +contrast); if((0 <= contrast) && (contrast <=
		 * mParameters.getMaxContrast())){ mParameters.setContrast(contrast); }
		 * // Set sharpness parameter String sharpnessStr =
		 * mPreferences.getString( CameraSettings.KEY_SHARPNESS,
		 * mActivity.getString(R.string.pref_camera_sharpness_default)); int
		 * sharpness = Integer.parseInt(sharpnessStr) *
		 * (mParameters.getMaxSharpness()/MAX_SHARPNESS_LEVEL); Log.v(TAG,
		 * "Sharpness value =" + sharpness); if((0 <= sharpness) && (sharpness
		 * <= mParameters.getMaxSharpness())){
		 * mParameters.setSharpness(sharpness); } // Set Face Recognition String
		 * faceRC = mPreferences.getString( CameraSettings.KEY_FACE_RECOGNITION,
		 * mActivity.getString(R.string.pref_camera_facerc_default)); Log.v(TAG,
		 * "Face Recognition value = " + faceRC); if
		 * (CameraUtil.isSupported(faceRC,
		 * CameraSettings.getSupportedFaceRecognitionModes(mParameters))) {
		 * mParameters.set(CameraSettings.KEY_QC_FACE_RECOGNITION, faceRC); } //
		 * Set AE Bracketing String aeBracketing = mPreferences.getString(
		 * CameraSettings.KEY_AE_BRACKET_HDR,
		 * mActivity.getString(R.string.pref_camera_ae_bracket_hdr_default));
		 * Log.v(TAG, "AE Bracketing value =" + aeBracketing); if
		 * (CameraUtil.isSupported(aeBracketing,
		 * CameraSettings.getSupportedAEBracketingModes(mParameters))) {
		 * mParameters.set(CameraSettings.KEY_QC_AE_BRACKETING, aeBracketing); }
		 * // Set Advanced features. String advancedFeature =
		 * mPreferences.getString( CameraSettings.KEY_ADVANCED_FEATURES,
		 * mActivity.getString(R.string.pref_camera_advanced_feature_default));
		 * Log.v(TAG, " advancedFeature value =" + advancedFeature);
		 * 
		 * if(advancedFeature != null) { String ubiFocusOff =
		 * mActivity.getString(R.string.
		 * pref_camera_advanced_feature_value_ubifocus_off); String
		 * chromaFlashOff = mActivity.getString(R.string.
		 * pref_camera_advanced_feature_value_chromaflash_off); String
		 * optiZoomOff = mActivity.getString(R.string.
		 * pref_camera_advanced_feature_value_optizoom_off);
		 * 
		 * if (advancedFeature.equals(mActivity.getString(R.string.
		 * pref_camera_advanced_feature_value_ubifocus_on))) {
		 * qcomUpdateAdvancedFeatures(advancedFeature, chromaFlashOff,
		 * optiZoomOff); } else if
		 * (advancedFeature.equals(mActivity.getString(R.string.
		 * pref_camera_advanced_feature_value_chromaflash_on))) {
		 * qcomUpdateAdvancedFeatures(ubiFocusOff, advancedFeature,
		 * optiZoomOff); } else if
		 * (advancedFeature.equals(mActivity.getString(R.string.
		 * pref_camera_advanced_feature_value_optizoom_on))) {
		 * qcomUpdateAdvancedFeatures(ubiFocusOff, chromaFlashOff,
		 * advancedFeature); } else { qcomUpdateAdvancedFeatures(ubiFocusOff,
		 * chromaFlashOff, optiZoomOff); } } // Set auto exposure parameter.
		 * String autoExposure = mPreferences.getString(
		 * CameraSettings.KEY_AUTOEXPOSURE,
		 * mActivity.getString(R.string.pref_camera_autoexposure_default));
		 * Log.v(TAG, "autoExposure value =" + autoExposure); if
		 * (CameraUtil.isSupported(autoExposure,
		 * mParameters.getSupportedAutoexposure())) {
		 * mParameters.setAutoExposure(autoExposure); }
		 * 
		 * // Set anti banding parameter. String antiBanding =
		 * mPreferences.getString( CameraSettings.KEY_ANTIBANDING,
		 * mActivity.getString(R.string.pref_camera_antibanding_default));
		 * Log.v(TAG, "antiBanding value =" + antiBanding); if
		 * (CameraUtil.isSupported(antiBanding,
		 * mParameters.getSupportedAntibanding())) {
		 * mParameters.setAntibanding(antiBanding); }
		 * 
		 * String zsl = mPreferences.getString(CameraSettings.KEY_ZSL,
		 * mActivity.getString(R.string.pref_camera_zsl_default)); String
		 * capMode = mPreferences.getString(CameraSettings.KEY_CAPTURE_MODE,
		 * GC.CAPTURE_MODE_NORMAL); mParameters.set(GC.KEY_CAPTURE_MODE,
		 * capMode); mParameters.setZSLMode(zsl); if(zsl.equals("on")) {
		 * //Switch on ZSL Camera mode mSnapshotMode =
		 * CameraInfo.CAMERA_SUPPORT_MODE_ZSL; mParameters.setCameraMode(1);
		 * mFocusManager.setZslEnable(true);
		 * 
		 * //Raw picture format is not supported under ZSL mode Editor editor =
		 * mPreferences.edit();
		 * editor.putString(CameraSettings.KEY_PICTURE_FORMAT,
		 * mActivity.getString(R.string.pref_camera_picture_format_value_jpeg));
		 * editor.apply();
		 * mUI.overrideSettings(CameraSettings.KEY_PICTURE_FORMAT,
		 * mActivity.getString(R.string.pref_camera_picture_format_entry_jpeg));
		 * 
		 * //Try to set CAF for ZSL
		 * if(CameraUtil.isSupported(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
		 * mParameters.getSupportedFocusModes()) && !mFocusManager.isTouch()) {
		 * mFocusManager
		 * .overrideFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		 * mParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); }
		 * else if (mFocusManager.isTouch()) {
		 * mFocusManager.overrideFocusMode(null);
		 * mParameters.setFocusMode(mFocusManager.getFocusMode()); } else { //
		 * If not supported use the current mode
		 * mFocusManager.overrideFocusMode(mFocusManager.getFocusMode()); }
		 * 
		 * if(!pictureFormat.equals(PIXEL_FORMAT_JPEG)) {
		 * mActivity.runOnUiThread(new Runnable() { public void run() {
		 * Toast.makeText(mActivity, R.string.error_app_unsupported_raw,
		 * Toast.LENGTH_SHORT).show(); } }); } } else if(zsl.equals("off")) {
		 * mSnapshotMode = CameraInfo.CAMERA_SUPPORT_MODE_NONZSL;
		 * mParameters.setCameraMode(0); mFocusManager.setZslEnable(false);
		 * mFocusManager.overrideFocusMode(null);
		 * mParameters.setFocusMode(mFocusManager.getFocusMode()); } // Set face
		 * detetction parameter. String faceDetection = mPreferences.getString(
		 * CameraSettings.KEY_FACE_DETECTION,
		 * mActivity.getString(R.string.pref_camera_facedetection_default));
		 * 
		 * if (CameraUtil.isSupported(faceDetection,
		 * mParameters.getSupportedFaceDetectionModes())) {
		 * mParameters.setFaceDetectionMode(faceDetection);
		 * if(faceDetection.equals("on") && mFaceDetectionEnabled == false) {
		 * mFaceDetectionEnabled = true; startFaceDetection(); }
		 * if(faceDetection.equals("off") && mFaceDetectionEnabled == true) {
		 * stopFaceDetection(); mFaceDetectionEnabled = false; } } // skin tone
		 * ie enabled only for auto,party and portrait BSM // when color effects
		 * are not enabled if((Parameters.SCENE_MODE_PARTY.equals(mSceneMode) ||
		 * Parameters.SCENE_MODE_PORTRAIT.equals(mSceneMode)) &&
		 * (Parameters.EFFECT_NONE.equals(colorEffect))) { //Set Skin Tone
		 * Correction factor Log.v(TAG, "set tone bar: mSceneMode = " +
		 * mSceneMode); if(mSeekBarInitialized == true)
		 * mHandler.sendEmptyMessage(SET_SKIN_TONE_FACTOR); }
		 * 
		 * //Set Histogram String histogram = mPreferences.getString(
		 * CameraSettings.KEY_HISTOGRAM,
		 * mActivity.getString(R.string.pref_camera_histogram_default)); if
		 * (CameraUtil.isSupported(histogram,
		 * mParameters.getSupportedHistogramModes()) && mCameraDevice != null) {
		 * // Call for histogram if(histogram.equals("enable")) {
		 * mActivity.runOnUiThread(new Runnable() { public void run() {
		 * if(mGraphView != null) { mGraphView.setVisibility(View.VISIBLE);
		 * mGraphView.PreviewChanged(); } } });
		 * mCameraDevice.setHistogramMode(mStatsCallback); mHiston = true; }
		 * else { mHiston = false; mActivity.runOnUiThread(new Runnable() {
		 * public void run() { if (mGraphView != null)
		 * mGraphView.setVisibility(View.INVISIBLE); } });
		 * mCameraDevice.setHistogramMode(null); } } // Read Flip mode from adb
		 * command //value: 0(default) - FLIP_MODE_OFF //value: 1 - FLIP_MODE_H
		 * //value: 2 - FLIP_MODE_V //value: 3 - FLIP_MODE_VH int
		 * preview_flip_value =
		 * SystemProperties.getInt("debug.camera.preview.flip", 0); int
		 * video_flip_value = SystemProperties.getInt("debug.camera.video.flip",
		 * 0); int picture_flip_value =
		 * SystemProperties.getInt("debug.camera.picture.flip", 0); int rotation
		 * = CameraUtil.getJpegRotation(mCameraId, mOrientation);
		 * mParameters.setRotation(rotation); if (rotation == 90 || rotation ==
		 * 270) { // in case of 90 or 270 degree, V/H flip should reverse if
		 * (preview_flip_value == 1) { preview_flip_value = 2; } else if
		 * (preview_flip_value == 2) { preview_flip_value = 1; } if
		 * (video_flip_value == 1) { video_flip_value = 2; } else if
		 * (video_flip_value == 2) { video_flip_value = 1; } if
		 * (picture_flip_value == 1) { picture_flip_value = 2; } else if
		 * (picture_flip_value == 2) { picture_flip_value = 1; } } String
		 * preview_flip = CameraUtil.getFilpModeString(preview_flip_value);
		 * String video_flip = CameraUtil.getFilpModeString(video_flip_value);
		 * String picture_flip =
		 * CameraUtil.getFilpModeString(picture_flip_value);
		 * if(CameraUtil.isSupported(preview_flip,
		 * CameraSettings.getSupportedFlipMode(mParameters))){
		 * mParameters.set(CameraSettings.KEY_QC_PREVIEW_FLIP, preview_flip); }
		 * if(CameraUtil.isSupported(video_flip,
		 * CameraSettings.getSupportedFlipMode(mParameters))){
		 * mParameters.set(CameraSettings.KEY_QC_VIDEO_FLIP, video_flip); }
		 * if(CameraUtil.isSupported(picture_flip,
		 * CameraSettings.getSupportedFlipMode(mParameters))){
		 * mParameters.set(CameraSettings.KEY_QC_SNAPSHOT_PICTURE_FLIP,
		 * picture_flip); }
		 */
	}

	private void updateZsl() {
        Log.d(TAG,"###YUHUAN###updateZsl into ");
		// PR1010695-sichao.hu add begin , to avoid double-start-preview in case
		// of non-zsl
		if ("off".equals(mActivity.getString(R.string.setting_off_value))) {
			if (ZSL_STATE == -1) {
				ZSL_STATE = ZSL_OFF;
			}
		} else {
			if (ZSL_STATE == -1) {
				ZSL_STATE = ZSL_ON;
			}
		}// PR1010695-sichao.hu add end

		int originZSLState = ZSL_STATE;

		ExtendParameters extParams = ExtendParameters.getInstance(mParameters);
		// /set zsl parameter
		String pictureFormat = mPreferences
				.getString(CameraSettings.KEY_PICTURE_FORMAT, mActivity
						.getString(R.string.pref_camera_picture_format_default));
		String zsl = mPreferences.getString(
				CameraSettings.KEY_ZSL,
				mActivity.getString(R.string.setting_off_value));
		Log.d(TAG,"###YUHUAN###updateZsl#zsl=" + zsl);

		/*
		boolean isNightMode = mActivity.getString(R.string.setting_on_value)
				.equals(mPreferences.getString(
						CameraSettings.KEY_CAMERA_ARCSOFT_NIGHT,
						mActivity.getString(R.string.setting_off_value)));
	    */

		//
		// if(zsl.equals(mActivity.getString(R.string.setting_on_value))){
		// GC.setSettingEnable(GC.ROW_SETTING_ARCSOFT_NIGHT, false);
		// }else{
		// GC.setSettingEnable(GC.ROW_SETTING_ARCSOFT_NIGHT, true);
		// }
		checkThread();
		
		/*
		Log.d(TAG,"###YUHUAN###updateZsl#mIsEnableNight=" + mIsEnableNight);
		if (mIsEnableNight) {
			// zsl=mActivity.getString(R.string.setting_off_value);
			if (mActivity.gNeedLowerNight) {
				Size size = mParameters.getPictureSize();
				int width = size.width;
				int height = size.height;
				if (size.width * size.height > 8000000) {
					List<Size> sizes = mParameters.getSupportedPictureSizes();
					for (int i = 0; i < sizes.size(); i++) {
						if (sizes.get(i).width * sizes.get(i).height > 8000000) {
							continue;
						} else {
							width = sizes.get(i).width;
							height = sizes.get(i).height;
							break;
						}
					}
				}
				mParameters.setPictureSize(width, height);
			}
		}*/

		// PR913488-sichao.hu added,PR957536
		Log.d(TAG,"###YUHUAN###updateZsl#needZSLOn=" + needZSLOn);
		int curMode = mGC.getCurrentMode();
		if (!needZSLOn || curMode == GC.MODE_HDR || curMode == GC.MODE_MANUAL) {
			zsl = mActivity.getString(R.string.setting_off_value);
		}// PR913488-sichao.hu add end
		
		Log.d(TAG,"###YUHUAN###updateZsl#before zsl=" + zsl);
		zsl = extParams.parseZSLValueFormat(zsl);
		Log.d(TAG,"###YUHUAN###updateZsl#after zsl=" + zsl);
		CameraActivity.TraceLog(TAG, "ZSL ON ?" + zsl);

		if (CameraUtil.isSupported(zsl, extParams.getSupportedZSLValues())) {
			extParams.setZSLMode(zsl);
		}
		// TODO
		String capMode = mPreferences.getString(
				CameraSettings.KEY_CAPTURE_MODE, GC.CAPTURE_MODE_NORMAL);
		mParameters.set(GC.KEY_CAPTURE_MODE, capMode);
		if (zsl.equals("on")) {
			ZSL_STATE = ZSL_ON;
			// Switch on ZSL Camera mode
			mSnapshotMode = CAMERA_SUPPORT_MODE_ZSL;
			mFocusManager.setZslEnable(true);

			// Raw picture format is not supported under ZSL mode
			Editor editor = mPreferences.edit();
			editor.putString(CameraSettings.KEY_PICTURE_FORMAT, mActivity
					.getString(R.string.pref_camera_picture_format_value_jpeg));
			editor.apply();
			mUI.overrideSettings(CameraSettings.KEY_PICTURE_FORMAT, mActivity
					.getString(R.string.pref_camera_picture_format_entry_jpeg));

			// Try to set CAF for ZSL
			if (CameraUtil.isSupported(
					Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
					mParameters.getSupportedFocusModes())
					&& !mFocusManager.isTouch()) {
				mFocusManager
						.overrideFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				mParameters
						.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			} else if (mFocusManager.isTouch()) {
				mFocusManager.overrideFocusMode(null);
				mParameters.setFocusMode(mFocusManager.getFocusMode());
			} else {
				// If not supported use the current mode
				mFocusManager.overrideFocusMode(mFocusManager.getFocusMode());
			}

			if (!pictureFormat.equals(PIXEL_FORMAT_JPEG)) {
				mActivity.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(mActivity,
								R.string.error_app_unsupported_raw,
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		} else if (zsl.equals("off")) {
			ZSL_STATE = ZSL_OFF;
			mSnapshotMode = CAMERA_SUPPORT_MODE_NONZSL;
			mFocusManager.setZslEnable(false);
			mFocusManager.overrideFocusMode(null);
			mParameters.setFocusMode(mFocusManager.getFocusMode());
		}

		filterFocus();

		applyParametersToServer();
		if (originZSLState != ZSL_STATE) {
			restartPreview();
			isSettingZSL = true;
			mHandler.postDelayed(new Runnable() {
				public void run() {
					isSettingZSL = false;
				}
			}, 500);
		}
		updateAutoFocusMoveCallback();
	}

	private void filterFocus() {
		if (mCameraId == GC.CAMERAID_FRONT) {
			mFocusManager.resetTouchFocus();
		}
		if (GC.CAPTURE_MODE_BURST_SHOT.equals(mPreferences.getString(
				CameraSettings.KEY_CAPTURE_MODE, GC.CAPTURE_MODE_NORMAL))) {
			// In burst shot state , we need to lock Focus
			mFocusManager.resetTouchFocus();
			if (mParameters.getSupportedFocusModes().contains(
					Parameters.FOCUS_MODE_AUTO)
					&& !mParameters.getFocusMode().equals(
							Parameters.FOCUS_MODE_AUTO)) {
				checkThread();
				mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
			}
		}
	}

	private boolean isSettingZSL = false;

	public void applyManualPreferenceToParameters() {
		if (mParameters == null || mCameraManualModeManager == null)
			return;

		int mode = GC.MODE_MANUAL_BACK_CAMERA_ORDER[0];
		int row = GC.MODE_MANUAL_CAMERA_SETTING_ROW[0];
		String value = "";
		checkThread();
		for (int i = 0; i < GC.MODE_MANUAL_BACK_CAMERA_ORDER.length; i++) {
			mode = GC.MODE_MANUAL_BACK_CAMERA_ORDER[i];
			row = GC.MODE_MANUAL_CAMERA_SETTING_ROW[i];
			value = mGC.getListPreference(row).getDefaultValue();
			Log.i(TAG, "LMCH0919 row:" + row + " value:" + value);
			switch (mode) {
			case GC.MODE_MANUAL_ISO:
				// mParameters.setISOValue(value);
				ExtendParameters extParams = ExtendParameters
						.getInstance(mParameters);

				String isoValue = extParams.parseISOValueFormat(value);

				if (CameraUtil.isSupported(isoValue,
						extParams.getSupportedISOValues())) {

					extParams.setISOValue(isoValue);
				}
				break;
			case GC.MODE_MANUAL_EXPOSURE:
				// mParameters.setExposureCompensation(mCameraManualModeManager.getExposureValue(value));
				break;
			// case GC.MODE_MANUAL_FLASH_DUTY:
			// if(CameraUtil.isSupported(value,
			// mParameters.getSupportedFlashModes())){
			// mParameters.setFlashMode(value);
			// }
			// break;
			case GC.MODE_MANUAL_FOCUS_VALUE:
				mParameters.setFocusMode(value);
				break;
			case GC.MODE_MANUAL_WHITE_BALANCE:
				mParameters.setWhiteBalance(value);
				break;
			}
		}
		applyParametersToServer();
	}

	private class CameraManualModeCallBackListener implements
			CameraManualModeManager.ManualModeCallBackListener {

		@Override
		public void updateISOValue(int isoValue) {
			// mParameters.setISOValue(isoValue);
			// applyParametersToServer();
			// ExtendParameters
			// extParams=ExtendParameters.getInstance(mParameters);
			// isoValue=extParams.parseISOValueFormat(isoValue);
			// if
			// (CameraUtil.isSupported(isoValue,extParams.getSupportedISOValues()))
			// {
			// extParams.setISOValue(isoValue);
			// }
			checkThread();
			// Modify by fei.gao according the iso document
			int minISO = mParameters.getInt("min-iso");
			int maxISO = mParameters.getInt("max-iso");
			if (Integer.parseInt(ManualModeSeekBar.isoString[isoValue]) >= minISO
					&& Integer.parseInt(ManualModeSeekBar.isoString[isoValue]) <= maxISO) {
				if (isEnableZsl()) {
					enableZSL(false);
				}
				mParameters.set("iso", "manual");
				mParameters
						.set("continuous-iso",
								Integer.parseInt(ManualModeSeekBar.isoString[isoValue]));
			}
			if (Integer.parseInt(ManualModeSeekBar.isoString[isoValue]) == Integer
					.parseInt(ManualModeSeekBar.isoString[0])) {
				mParameters.set("iso", "auto");
				if (!isEnableZsl()
						&& "0".equals(mParameters.get("exposure-time"))) {
					enableZSL(true);
				}
			}
			applyParametersToServer();
		}

		@Override
		public void updateManualFocusValue(int focusMode) {
			// Log.i(TAG,"LMCH0915 updateManualFocusValue focusValue:"+focusMode+" pos:"+mParameters.getCurrentFocusPosition());
			// mParameters.setFocusMode(focusMode);
			// int minFocus = mParameters.getInt("min-focus-pos-ratio");
			// int maxFocus = mParameters.getInt("max-focus-pos-ratio");
			checkThread();
			if (focusMode > FOUCS_POS_RATIO_MINVALUE
					&& focusMode <= FOUCS_POS_RATIO_MAXVALUE) {
				mParameters.setFocusMode("manual");
				mParameters.set("manual-focus-pos-type", 2);// scale mode is 2,
															// diopter mode is 3
				mParameters.set("manual-focus-position", focusMode);
				// To make sure the clear focus is done without any CAF callback
				// dirty write
				mCameraDevice.getCamera().setAutoFocusMoveCallback(null);
				clearFocus();
			} else if (focusMode == FOUCS_POS_RATIO_MINVALUE) {
				// mParameters.setFocusMode("auto");
				mParameters
						.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				updateAutoFocusMoveCallback();
			}

			applyParametersToServer();
		}

		@Override
		public void updateFlashDutyValue(String flashmode) {
			Log.i(TAG, "LMCH0915 updateFlashDutyValue fdValue:" + flashmode);
			// applyFlashDutyToParameter(fdValue);
			if (CameraUtil.isSupported(flashmode,
					mParameters.getSupportedFlashModes())) {
				checkThread();
				mParameters.setFlashMode(flashmode);
			}
			applyParametersToServer();
		}

		@Override
		public void updateWBValue(String wbValue) {
			Log.i(TAG, "LMCH0915 updateWBValue wbValue:" + wbValue);
			checkThread();
			mParameters.setWhiteBalance(wbValue);
			applyParametersToServer();
		}

		public void updateExposureTime(int ecValue) {
			// mParameters.setExposureCompensation(ecValue);

			final String minExpTime = mParameters.get("min-exposure-time");
			final String maxExpTime = mParameters.get("max-exposure-time");
			checkThread();
			if (Double
					.parseDouble(ManualShutterSpeedAdapter.exposureTimeDouble[ecValue]) <= Double
					.parseDouble(maxExpTime)
					&& Double
							.parseDouble(ManualShutterSpeedAdapter.exposureTimeDouble[ecValue]) > Double
							.parseDouble(minExpTime)) {
				// if(isEnableZsl()){
				// enableZSL(false);
				// }
				mParameters.set("exposure-time",
						ManualShutterSpeedAdapter.exposureTimeDouble[ecValue]);
			}
			if (Double
					.parseDouble(ManualShutterSpeedAdapter.exposureTimeDouble[ecValue]) == Double
					.parseDouble(ManualShutterSpeedAdapter.exposureTimeDouble[0])) {
				mParameters.set("exposure-time", "0");
				// if(!isEnableZsl() && "auto".equals(mParameters.get("iso"))){
				// enableZSL(true);
				// }
			}

			applyParametersToServer();
		}

		@Override
		public void resetManualModeParamters() {
			// TODO Auto-generated method stub
			// resetZSL();

			// PR935716-sichao.hu add begin
			// If mParameters ==null , then it's currently not in photoModule ,
			// And if it's not in photoModule , all the Manual mode setting is
			// not taking effect,
			// So under this case , we can ignore the reset here
			if (mParameters == null) {
				return;
			}// PR935716-sichao.hu add end
			checkThread();
			mParameters.set("exposure-time", "0");
			mParameters.set("iso", "auto");
			mParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			updateAutoFocusMoveCallback();
			mParameters.setWhiteBalance("auto");// set default value
			applyParametersToServer();

		}
	}

	// Apply parameters to server, so native camera can apply current settings.
	// Note: mCameraDevice.setParameters() consumes long time, please don't use
	// it unnecessary.
	public void applyParametersToServer() {
		if (mCameraDevice != null && mParameters != null) {
			checkThread();
			mCameraDevice.setParameters(mParameters);
		}
		Log.i(TAG, "applyParameterToServer() mParameters=" + mParameters
				+ ", mCameraDevice=" + mCameraDevice);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setAutoExposureLockIfSupported() {
		Log.d(TAG,"###YUHUAN###setAutoExposureLockIfSupported#mAeLockSupported=" + mAeLockSupported);
		if (mAeLockSupported) {
			checkThread();
			mParameters.setAutoExposureLock(mFocusManager.getAeAwbLock());
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setAutoWhiteBalanceLockIfSupported() {
		Log.d(TAG,"###YUHUAN###setAutoExposureLockIfSupported#mAwbLockSupported=" + mAwbLockSupported);
		if (mAwbLockSupported) {
			checkThread();
			mParameters.setAutoWhiteBalanceLock(mFocusManager.getAeAwbLock());
		}
	}

	private void setFocusAreasIfSupported() {
		Log.d(TAG,"###YUHUAN###setAutoExposureLockIfSupported#mFocusAreaSupported=" + mFocusAreaSupported);
		if (mFocusAreaSupported) {
			checkThread();
			mParameters.setFocusAreas(mFocusManager.getFocusAreas());
		}
	}

	private void setMeteringAreasIfSupported() {
		Log.d(TAG,"###YUHUAN###setAutoExposureLockIfSupported#mMeteringAreaSupported=" + mMeteringAreaSupported);
		if (mMeteringAreaSupported) {
			checkThread();
			mParameters.setMeteringAreas(mFocusManager.getMeteringAreas());
		}
	}

	private static final int NIGHT_SHOT_MAX_EFFECT_NUM = 6;
	private int mNightShotCurrentMax = NIGHT_SHOT_MAX_EFFECT_NUM;
	private int mJPEGCallbackCount = 0;

	private boolean updateCameraParametersPreference() {
		Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#mIsFaceBeauty=" + mIsFaceBeauty);
		if (mIsFaceBeauty) {
			return false;
		}
		setAutoExposureLockIfSupported();
		setAutoWhiteBalanceLockIfSupported();
		setFocusAreasIfSupported();
		setMeteringAreasIfSupported();

		/*
		Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#mIsFaceBeautyModeOn=" + mIsFaceBeautyModeOn);
		if (mIsFaceBeautyModeOn == ARCSOFTFACEBEAUTY_ON) {
			startFB();
		} else if (mIsFaceBeautyModeOn == ARCSOFTFACEBEAUTY_OFF) {
			stopFB();
		}
		*/

		boolean isFFFOn = mActivity.getString(R.string.setting_on_value)
				.equals(mPreferences.getString(CameraSettings.KEY_CAMERA_FFF,
						mActivity.getString(R.string.setting_on_value)));
		Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#isFFFOn=" + isFFFOn);

		int curMode = mGC.getCurrentMode();
		Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#curMode=" + curMode);
		if (mCameraId == GC.CAMERAID_FRONT || curMode == GC.MODE_MANUAL) {
			isFFFOn = false;
		}
		checkThread();
		mParameters.set("tct-fff-enable", isFFFOn ? "on" : "off");
		// set camera capture sound
		/*
		 * [FEATURE]-Set the perso of shutter sound added by lijuan.zhang
		 * ,20141103 begin
		 */
		// isCaptureSoundEnable =
		// mPreferences.getString(CameraSettings.KEY_SOUND,
		// mActivity.getString(R.string.pref_camera_capturesound_default))
		// .equals(mActivity.getString(R.string.setting_on_value));
		String defaultValue = CustomUtil.getInstance().getBoolean(
				CustomFields.DEF_TCTCAMERA_SHUTTER_SOUND_ON, true) ? "on"
				: "off";
		Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#defaultValue=" + defaultValue);
		isCaptureSoundEnable = mPreferences.getString(CameraSettings.KEY_SOUND,
				defaultValue).equals(
				mActivity.getString(R.string.setting_on_value));
		/*
		 * [FEATURE]-Set the perso of shutter sound added by lijuan.zhang
		 * ,20141103 end
		 */
		Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#isCaptureSoundEnable=" + isCaptureSoundEnable);
		Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#mLongpressFlag=" + mLongpressFlag);
		
		mCameraDevice.enableShutterSound(mLongpressFlag ? false
				: isCaptureSoundEnable);
		// Set picture size.
		PictureSize pictureSize = getPictureSize();
		Log.e(TAG, "getPictureSize= " + pictureSize.toString());
		Size old_size = mParameters.getPictureSize();
		Log.e(TAG, "old picture_size = " + old_size.width + " x "
				+ old_size.height);
		// List<Size> supported = mParameters.getSupportedPictureSizes();
		// CameraSettings.setCameraPictureSize(
		// pictureSize, supported, mParameters);
		mParameters.setPictureSize(pictureSize.width, pictureSize.height);
		Size size = mParameters.getPictureSize();
		Log.v(TAG, "new picture_size = " + size.width + " x " + size.height);
		if (old_size != null && size != null) {
			if (!size.equals(old_size) && mCameraState != PREVIEW_STOPPED) {
				Log.v(TAG, "Picture Size changed. Restart Preview");
				mRestartPreview = true;
			}
		}
		String sizeRatio = CameraSettings.getRatioString(size.width,
				size.height);

		// Set a preview size that is closest to the viewfinder height and has
		// the right aspect ratio.
		List<Size> sizes = mParameters.getSupportedPreviewSizes();
		String previewRatio = null;
		if (pictureSize == null)
			pictureSize = new PictureSize(size.width, size.height);
		int swidth = pictureSize.width;
		int sheight = pictureSize.height;
		previewRatio = getRatioString(swidth, sheight);
		Log.i(TAG, "previewRatio= " + previewRatio);
		optimalSize = CameraUtil.getOptimalPreviewSize(mActivity, sizes,
				Double.parseDouble(previewRatio));

		if (optimalSize == null) {
			CameraUtil.getOptimalPreviewSize(mActivity, sizes,
					(double) size.width / size.height);
		}
		Size original = mParameters.getPreviewSize();
		Log.i(TAG, "optimal preview size= " + optimalSize.width + "x"
				+ optimalSize.height);
		Log.i(TAG, "original preview size= " + original.width + "x"
				+ original.height);
		if (!original.equals(optimalSize)) {
			mParameters.setPreviewSize(optimalSize.width, optimalSize.height);
			// Zoom related settings will be changed for different preview
			// sizes, so set and read the parameters to get latest values
			if (mHandler.getLooper() == Looper.myLooper()) {
				// On UI thread only, not when camera starts up
				mFocusManager.resetTouchFocus();
				mRestartPreview = true;
			}
			mCameraDevice.setParameters(mParameters);
			mParameters = mCameraDevice.getParameters();
			Log.v(TAG, "Preview Size changed. Restart Preview");
			mRestartPreview = true;
		}
		// modify by minghui.hua for PR986981
		if (curMode == GC.MODE_POLAROID) {
			mUI.updatePreviewLayout(optimalSize.width, optimalSize.width, true);
		} else {
			mUI.updatePreviewLayout(optimalSize.width, optimalSize.height,
					false);
		}

		Log.v(TAG, "Preview size is " + optimalSize.width + "x"
				+ optimalSize.height);

		// Since changing scene mode may change supported values, set scene mode
		// first. HDR is a scene mode. To promote it in UI, it is stored in a
		// separate preference.
		String onValue = mActivity.getString(R.string.setting_on_value);
		String hdr = mPreferences.getString(CameraSettings.KEY_CAMERA_HDR,
				mActivity.getString(R.string.pref_camera_hdr_default));
		String hdrPlus = mPreferences.getString(
				CameraSettings.KEY_CAMERA_HDR_PLUS,
				mActivity.getString(R.string.pref_camera_hdr_plus_default));
		boolean hdrOn = onValue.equals(hdr);
		Log.v(TAG, "HDR is On ?" + hdrOn);
		boolean hdrPlusOn = onValue.equals(hdrPlus);
		Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#hdrPlusOn=" + hdrPlusOn);

		ExtendParameters extparam = ExtendParameters.getInstance(mParameters);
		if (GC.isTintlessNeed()) {
			extparam.setTintless("enable");
		} else {
			extparam.setTintless("disable");
		}

		boolean isGrid = mActivity.getString(R.string.setting_on_value).equals(
				mPreferences.getString(CameraSettings.KEY_CAMERA_GRID,
						mActivity.getString(R.string.setting_off_value)));

		if (isGrid && curMode != GC.MODE_QRCODE
				&& mActivity.isNonePickIntent()) {
			mActivity.getCameraViewManager().getGridManager().show();
		} else {
			mActivity.getCameraViewManager().getGridManager().hide();
		}

		// [BUGFIX]-ADD-BEGIN by xuan.zhou, PR-691991, 2014-6-9
		// Night and Sports should be supported here.
		String night = mPreferences.getString(CameraSettings.KEY_CAMERA_NIGHT,
				mActivity.getString(R.string.pref_camera_night_default));
		String sports = mPreferences.getString(
				CameraSettings.KEY_CAMERA_SPORTS,
				mActivity.getString(R.string.pref_camera_sports_default));
		boolean nightOn = onValue.equals(night);
		Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#nightOn=" + nightOn);
		boolean sportsOn = onValue.equals(sports);
		Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#sportsOn=" + sportsOn);
		// [BUGFIX]-ADD-END by xuan.zhou, PR-691991, 2014-6-9

		boolean doGcamModeSwitch = false;
		if (hdrPlusOn && GcamHelper.hasGcamCapture()) {
			// Kick off mode switch to gcam.
			doGcamModeSwitch = true;
		} else {
			// [BUGFIX]-MOD-BEGIN by xuan.zhou, PR-691991, 2014-6-9
			Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#mASDShowingState=" + mASDShowingState);
			if (hdrOn || mASDShowingState == ASD_BACKLIT/*
														 * PR938201-sichao.hu
														 * added
														 */) {
				mSceneMode = CameraUtil.SCENE_MODE_HDR;
				// [BUGFIX]-DEL-BEGIN by xuan.zhou, PR-691991, 2014-6-9
				// Why auto is set when mode is hdr? Ingore it.
				// if
				// (!(Parameters.SCENE_MODE_AUTO).equals(mParameters.getSceneMode()))
				// {
				// mParameters.setSceneMode(Parameters.SCENE_MODE_AUTO);
				// mCameraDevice.setParameters(mParameters);
				// mParameters = mCameraDevice.getParameters();
				// }
				// [BUGFIX]-DEL-END by xuan.zhou, PR-691991, 2014-6-9
			} else if (nightOn) {
				mSceneMode = CameraUtil.SCENE_MODE_NIGHT;
			} else if (sportsOn) {
				mSceneMode = CameraUtil.SCENE_MODE_SPORTS;
			} else {
				mSceneMode = mPreferences
						.getString(
								CameraSettings.KEY_SCENE_MODE,
								mActivity
										.getString(R.string.pref_camera_scenemode_default));
			}
			// [BUGFIX]-MOD-END by xuan.zhou, PR-691991, 2014-6-9
		}
		
		Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#mSceneMode=" + mSceneMode);
		if (CameraUtil.isSupported(mSceneMode,
				mParameters.getSupportedSceneModes())) {
			if (!mParameters.getSceneMode().equals(mSceneMode)) {
				mParameters.setSceneMode(mSceneMode);

				// Setting scene mode will change the settings of flash mode,
				// white balance, and focus mode. Here we read back the
				// parameters, so we can know those settings.
				mCameraDevice.setParameters(mParameters);
				mParameters = mCameraDevice.getParameters();
			}
		} else {
			mSceneMode = mParameters.getSceneMode();
			if (mSceneMode == null) {
				mSceneMode = Parameters.SCENE_MODE_AUTO;
			}
		}

		// Set JPEG quality.
		int jpegQuality = CameraProfile.getJpegEncodingQualityParameter(
				mCameraId, CameraProfile.QUALITY_HIGH);
		// mParameters.setJpegQuality(jpegQuality);
		mParameters.setJpegQuality(100);

		// For the following settings, we need to check if the settings are
		// still supported by latest driver, if not, ignore the settings.

		// Set exposure compensation
		int value = CameraSettings.readExposure(mPreferences);
		Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#value=" + value);
		int max = mParameters.getMaxExposureCompensation();
		Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#max=" + max);
		int min = mParameters.getMinExposureCompensation();
		Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#min=" + min);
		if (value >= min && value <= max) {
			mParameters.setExposureCompensation(value);
		} else {
			Log.w(TAG, "invalid exposure range: " + value);
		}

		if (Parameters.SCENE_MODE_AUTO.equals(mSceneMode)) {
			// Set flash mode.
			String flashMode = mPreferences
					.getString(CameraSettings.KEY_FLASH_MODE, mActivity
							.getString(R.string.pref_camera_flashmode_default));
			Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#flashMode=" + flashMode);
			String validVideoFlashMode = GC
					.getValidFlashModeForVideo(flashMode);
			Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#validVideoFlashMode=" + validVideoFlashMode);
			mGC.overrideSettingsValue(
					CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE,
					validVideoFlashMode);
			if (mLongpressFlag)
				flashMode = "off";
			List<String> supportedFlash = mParameters.getSupportedFlashModes();
			if (CameraUtil.isSupported(flashMode, supportedFlash)) {
				// modify by minghui.hua for PR992130
				mParameters.setFlashMode(flashMode);
			} else {
				flashMode = mParameters.getFlashMode();
				if (flashMode == null) {
					flashMode = mActivity
							.getString(R.string.pref_camera_flashmode_no_flash);
				}
				// setFlashMode2Preference(flashMode);
			}

			// Set white balance parameter.
			if (curMode != GC.MODE_MANUAL) {
				String whiteBalance = mPreferences
						.getString(
								CameraSettings.KEY_WHITE_BALANCE,
								mActivity
										.getString(R.string.pref_camera_whitebalance_default));
				Log.d(TAG,"###YUHUAN###updateCameraParametersPreference#whiteBalance=" + whiteBalance);
				if (CameraUtil.isSupported(whiteBalance,
						mParameters.getSupportedWhiteBalance())) {
					mParameters.setWhiteBalance(whiteBalance);
				} else {
					whiteBalance = mParameters.getWhiteBalance();
					if (whiteBalance == null) {
						whiteBalance = Parameters.WHITE_BALANCE_AUTO;
					}
				}

				// Set focus mode.
				mFocusManager.overrideFocusMode(null);
				mParameters.setFocusMode(mFocusManager.getFocusMode());

			}
		} else {
			mFocusManager.overrideFocusMode(mParameters.getFocusMode());
			// [BUGFIX]-ADD-BEGIN by xuan.zhou, PR-691991, 2014-6-9
			mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			// [BUGFIX]-ADD-END by xuan.zhou, PR-691991, 2014-6-9
		}
		if (mContinuousFocusSupported && ApiHelper.HAS_AUTO_FOCUS_MOVE_CALLBACK) {
			updateAutoFocusMoveCallback();
		}
		// QCom related parameters updated here.
		qcomUpdateCameraParametersPreference();
		if (curMode == GC.MODE_MANUAL) {
			mActivity.getCameraViewManager().getCameraManualModeManager()
					.resumeSettingsToParamers();
		}
		doChangeZSL = mSnapshotMode;
		return doGcamModeSwitch;
	}

	private void enableZSL(boolean isEnable) {
		ExtendParameters extParams = ExtendParameters.getInstance(mParameters);
		String zsl = "";
		if (isEnable) {
			zsl = mActivity.getString(R.string.setting_on_value);
		} else {
			zsl = mActivity.getString(R.string.setting_off_value);
		}
		zsl = extParams.parseZSLValueFormat(zsl);
		if (CameraUtil.isSupported(zsl, extParams.getSupportedZSLValues())) {
			extParams.setZSLMode(zsl);
		}
		checkThread();
		if (zsl.equals("on")) {
			// Switch on ZSL Camera mode
			mSnapshotMode = CAMERA_SUPPORT_MODE_ZSL;
			mFocusManager.setZslEnable(true);

			// Raw picture format is not supported under ZSL mode
			Editor editor = mPreferences.edit();
			editor.putString(CameraSettings.KEY_PICTURE_FORMAT, mActivity
					.getString(R.string.pref_camera_picture_format_value_jpeg));
			editor.apply();
			mUI.overrideSettings(CameraSettings.KEY_PICTURE_FORMAT, mActivity
					.getString(R.string.pref_camera_picture_format_entry_jpeg));

			// Try to set CAF for ZSL
			if (CameraUtil.isSupported(
					Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
					mParameters.getSupportedFocusModes())
					&& !mFocusManager.isTouch()) {
				mFocusManager
						.overrideFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				mParameters
						.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			} else if (mFocusManager.isTouch()) {
				mFocusManager.overrideFocusMode(null);
				mParameters.setFocusMode(mFocusManager.getFocusMode());
			} else {
				// If not supported use the current mode
				mFocusManager.overrideFocusMode(mFocusManager.getFocusMode());
			}

			// if(!pictureFormat.equals(PIXEL_FORMAT_JPEG)) {
			// mActivity.runOnUiThread(new Runnable() {
			// public void run() {
			// Toast.makeText(mActivity, R.string.error_app_unsupported_raw,
			// Toast.LENGTH_SHORT).show();
			// }
			// });
			// }
		} else if (zsl.equals("off")) {
			mSnapshotMode = CAMERA_SUPPORT_MODE_NONZSL;
			mFocusManager.setZslEnable(false);
			mFocusManager.overrideFocusMode(null);
			mParameters.setFocusMode(mFocusManager.getFocusMode());
		}
		updateAutoFocusMoveCallback();
	}

	private boolean isEnableZsl() {
		if (mSnapshotMode == CAMERA_SUPPORT_MODE_NONZSL) {
			return false;
		} else if (mSnapshotMode == CAMERA_SUPPORT_MODE_ZSL) {
			return true;
		} else {
			return false;
		}
	}

	private void resetZSL() {
		if (doChangeZSL == CAMERA_SUPPORT_MODE_ZSL && !isEnableZsl()) {
			enableZSL(true);
		} else {
			enableZSL(false);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void updateAutoFocusMoveCallback() {
		if (mCameraDevice == null)
			return;
		if (mParameters.getFocusMode().equals(
				CameraUtil.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			mCameraDevice.setAutoFocusMoveCallback(mHandler,
					(CameraAFMoveCallback) mAutoFocusMoveCallback);
		} else {
			mCameraDevice.setAutoFocusMoveCallback(null, null);
		}
	}

	// We separate the parameters into several subsets, so we can update only
	// the subsets actually need updating. The PREFERENCE set needs extra
	// locking because the preference can be changed from GLThread as well.
	private void setCameraParameters(int updateSet) {
		Log.d(TAG,"###YUHUAN###setCameraParameters#updateSet=" + updateSet);
		if (mCameraDevice == null)
			return;
		boolean doModeSwitch = false;
		if ((updateSet & UPDATE_PARAM_INITIALIZE) != 0) {
			updateCameraParametersInitialize();
		}

		if ((updateSet & UPDATE_PARAM_ZOOM) != 0) {
			updateCameraParametersZoom();
		}

		if ((updateSet & UPDATE_PARAM_PREFERENCE) != 0) {
			doModeSwitch = updateCameraParametersPreference();
		}

		checkThread();
		mCameraDevice.setParameters(mParameters);

		// mHandler.post(new Runnable(){
		//
		// @Override
		// public void run() {
		// mActivity.getCameraViewManager().setViewState(GC.VIEW_STATE_REFRESH_TOP_VIEW);
		// }
		//
		// });

		// Switch to gcam module if HDR+ was selected
		if (doModeSwitch && !mIsImageCaptureIntent) {
			mHandler.sendEmptyMessage(SWITCH_TO_GCAM_MODULE);
		}
	}

	// If the Camera is idle, update the parameters immediately, otherwise
	// accumulate them in mUpdateSet and update later.
	private void setCameraParametersWhenIdle(int additionalUpdateSet) {
		mUpdateSet |= additionalUpdateSet;
		if (mCameraDevice == null) {
			// We will update all the parameters when we open the device, so
			// we don't need to do anything now.
			mUpdateSet = 0;
			return;
		} else if (isCameraIdle()) {
			setCameraParameters(mUpdateSet);
			if (mRestartPreview && mCameraState != PREVIEW_STOPPED) {
				Log.v(TAG, "Restarting Preview...");
				stopPreview();
				resizeForPreviewAspectRatio();
				startPreview();
				setCameraState(IDLE);
			}
			mRestartPreview = false;
			updateCameraSettings();
			mUpdateSet = 0;
		} else {
			if (!mHandler.hasMessages(SET_CAMERA_PARAMETERS_WHEN_IDLE)) {
				mHandler.sendEmptyMessageDelayed(
						SET_CAMERA_PARAMETERS_WHEN_IDLE, 1000);
			}
		}
	}

	@Override
	public boolean isCameraIdle() {
		return (mCameraState == IDLE)
				|| (mCameraState == PREVIEW_STOPPED)
				|| ((mFocusManager != null) && mFocusManager.isFocusCompleted() && (mCameraState != SWITCHING_CAMERA));
	}

	@Override
	public boolean isImageCaptureIntent() {
		String action = mActivity.getCurIntent().getAction();
		return (MediaStore.ACTION_IMAGE_CAPTURE.equals(action) || CameraActivity.ACTION_IMAGE_CAPTURE_SECURE
				.equals(action));
	}

	private void setupCaptureParams() {
		Bundle myExtras = mActivity.getCurIntent().getExtras();
		if (myExtras != null) {
			mSaveUri = (Uri) myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
			mCropValue = myExtras.getString("crop");
		}
	}

	public void doModeChange(int newMode) {
		closeAllMode();
		Log.i(TAG, "LMCH0414 doModeChange mode:" + newMode);
		switch (newMode) {
		case GC.MODE_HDR:
			setHDRMode(true);
			mHelpGuideManager.showManualModeHelpViewByCurrentMode(GC.MODE_HDR);
			// if(!mHelpGuideManager.isHdrHelpGuideEnable()){//PR919415,v-nj-feiqiang.cheng
			mActivity.showInfo(mActivity.getString(R.string.photo_hdr_toast),
					false, false);
			// }
			break;
		case GC.MODE_RAW:
			setRawMode(true);
			break;
		case GC.MODE_SMILE:
			SmileShotThread.detect(mActivity, mSmileShotDoSnapRunnable);
			break;
		case GC.MODE_SPORTS:
			setSportsMode(true);
			break;
		case GC.MODE_NIGHT:
			setNightMode(true); // [BUGFIX]-ADD by xuan.zhou, PR-691991,
								// 2014-6-9
			break;
		case GC.MODE_MANUAL:
			setManualMode(true);
			mHelpGuideManager
					.showManualModeHelpViewByCurrentMode(GC.MODE_MANUAL);
			// TODO:to do some init when change to this mode
			break;
		case GC.MODE_EXPRESSION4:
			// PR873260-lan.shan-001 Modify begin
			// Add guide tips for expression4
			mHelpGuideManager
					.showManualModeHelpViewByCurrentMode(GC.MODE_EXPRESSION4);
			if (!mHelpGuideManager.isCollageHelpGuideEnable()) {
				mActivity.showInfo(
						mActivity.getString(R.string.expression_guide_capture),
						false, false);
			}
			// PR873260-lan.shan-001 Modify end
			// Just do this work on onShutterButtonClick, remember to stop it.
			break;
		// [FEATURE]-Add BEGIN by TCTNB,xutao.wu, 2014-07-24, PR635962
		case GC.MODE_FILTER:
			// Intent intent = new Intent("android.media.action.camerafilter");
			// Intent intent=new
			// Intent(mActivity,LiveFilterCamera.class);//PR834296-sichao.hu
			// modified, this intent is more reliable
			// // Set parameters to LiveFilterCamera
			//
			// /*[FEATURE]-Set the perso of shutter sound added by lijuan.zhang
			// ,20141103 begin*/
			// // boolean isCaptureSoundEnable =
			// mPreferences.getString(CameraSettings.KEY_SOUND,
			// //
			// mActivity.getString(R.string.pref_camera_capturesound_default))
			// // .equals("on");
			// String defaultValue =
			// CustomUtil.getInstance().getBoolean(CustomFields.DEF_TCTCAMERA_SHUTTER_SOUND_ON,
			// true) ? "on" : "off";
			// boolean isCaptureSoundEnable =
			// mPreferences.getString(CameraSettings.KEY_SOUND,
			// defaultValue)
			// .equals("on");
			// /*[FEATURE]-Set the perso of shutter sound added by lijuan.zhang
			// ,20141103 end*/
			// LiveFilterCameraConfigurator cfg =
			// LiveFilterInstanceHolder.getIntance().getCameraConfigurator();
			// cfg.setCameraId(CameraSettings.readPreferredCameraId(mPreferences));
			// cfg.setShutterSound(isCaptureSoundEnable);
			// mActivity.startActivityForResult(intent,
			// mActivity.REQ_CODE_GO_TO_FILTER);
			// mActivity.overridePendingTransition(R.anim.livecamera_in,
			// R.anim.livecamera_main_out);
			break;
		case GC.MODE_POLAROID:
			initPolaroid(true);
			break;
		// [FEATURE]-Add END by TCTNB,xutao.wu, 2014-07-24, PR635962
		default:
			Log.i(TAG, "LMCH0414 we has not this mode:" + newMode);
			break;
		}
		onSharedPreferenceChanged();
		// if(newMode==GC.MODE_HDR){
		// mTopViewManager.hideviews(true,false,false);
		// }
	}

	public void clearFocus() {
		if (mFocusManager != null)
			mFocusManager.resetTouchFocus();
		if (mUI != null)
			mUI.clearFaces();

	}

	private void closeAllMode() {
		CameraActivity.TraceLog(TAG, "Close all modes");
		setRawMode(false);
		SmileShotThread.stopDetect();
		setHDRMode(false);
		setManualMode(false);
		setSportsMode(false);
		//setQRcodeMode(false);
		setNightMode(false);
		//setArcsoftNight(false);
		if (mActivity.getGC().getCurrentMode() != GC.MODE_POLAROID) {
			initPolaroid(false);
		}

		// Add by fei.gao begin for (change to photo mode from facebeauty ,the
		// effect is working)
		/*
		if (mGC.getCurrentMode() != GC.MODE_ARCSOFT_FACEBEAUTY) {
			stopFB();
		}*/
		// Add by fei.gao end for (change to photo mode from facebeauty ,the
		// effect is working)
		/*
		if (mIsFaceBeautyModeOn != ARCSOFTFACEBEAUTY_NONEED) {
			setArcsoftFaceBeauty(ARCSOFTFACEBEAUTY_OFF);
		}*/
		mTopViewManager.hideviews(false, false, false);
		clearFocus();
	}

	private void setRawMode(boolean on) {
		Editor editor = mPreferences.edit();
		if (on) {
			// editor.putString(CameraSettings.KEY_ZSL, "off");
			if (mParameters == null) {
				if (mCameraDevice != null)
					mParameters = mCameraDevice.getParameters();
			}
			String raw = CameraSettings.getSupportedRawFormat(mParameters);
			Log.i(TAG, "LMCH0414 setRawMode raw:" + raw);
			editor.putString(CameraSettings.KEY_PICTURE_FORMAT, raw);
		} else {
			// editor.putString(CameraSettings.KEY_ZSL,
			// "on");
			editor.putString(CameraSettings.KEY_PICTURE_FORMAT, "jpeg");
		}
		editor.apply();
	}

	// Macro mode is not a really scene mode, but just a focus mode.
	// To use this focus mode, scene mode should be auto.
	private void setMacroMode(boolean on) {
		Editor editor = mPreferences.edit();
		editor.putString(CameraSettings.KEY_SCENE_MODE, "auto");
		if (on)
			editor.putString(CameraSettings.KEY_FOCUS_MODE, "macro");
		else
			editor.putString(CameraSettings.KEY_FOCUS_MODE,
					"continuous-picture");
		editor.apply();
	}

	private void setNightMode(boolean on) {
		Editor editor = mPreferences.edit();
		if (on)
			editor.putString(CameraSettings.KEY_CAMERA_NIGHT, "on");
		else
			editor.putString(CameraSettings.KEY_CAMERA_NIGHT, "off");
		editor.apply();
	}

	/*
	private void setArcsoftNight(boolean on) {
		Editor editor = mPreferences.edit();
		if (on)
			editor.putString(CameraSettings.KEY_CAMERA_ARCSOFT_NIGHT, "on");
		else
			editor.putString(CameraSettings.KEY_CAMERA_ARCSOFT_NIGHT, "off");
		editor.apply();
	}*/

	private void setFlashMode2Preference(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(CameraSettings.KEY_FLASH_MODE, value);
		// editor.putString(CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE, value);
		editor.apply();
	}

	private void setBurstShotMode(boolean on) {
		Log.d(TAG,"###YUHUAN###setBurstShotMode#on=" + on);
		mActivity.setSwipingEnabled(!on);// PR898421-sichao.hu added
		if (on) {// PR910549-sichao.hu added
			mActivity.disableDrawer();
			// }else{
			// mActivity.enableDrawer();
		}
		Editor editor = mPreferences.edit();
		if (on) {
			// editor.putString(CameraSettings.KEY_ZSL, "on");
			editor.putString(CameraSettings.KEY_JPEG_QUALITY, "90");
			editor.putString(CameraSettings.KEY_CAPTURE_MODE,
					GC.CAPTURE_MODE_BURST_SHOT);
		} else {
			// editor.putString(CameraSettings.KEY_ZSL,
			// "on");
			editor.putString(CameraSettings.KEY_JPEG_QUALITY, "superfine");
			editor.putString(CameraSettings.KEY_CAPTURE_MODE,
					GC.CAPTURE_MODE_NORMAL);
		}
		editor.apply();
	}

	private boolean needZSLOn = true;// PR913488-sichao.hu added

	private void setHDRMode(boolean on) {
		Editor editor = mPreferences.edit();
		if (on) {
			// editor.putString(CameraSettings.KEY_ZSL, "off");
			needZSLOn = true;// PR913488-sichao.hu added
			editor.putString(CameraSettings.KEY_CAMERA_HDR, "on");
			// mActivity.showInfo(mActivity.getString(R.string.hdr_guide_capture));
		} else {
			// editor.putString(CameraSettings.KEY_ZSL,
			// "on");
			needZSLOn = true;// PR913488-sichao.hu added
			editor.putString(CameraSettings.KEY_CAMERA_HDR, "off");
		}
		editor.apply();
	}

	private void setManualMode(boolean on) {
		if (on) {
			needZSLOn = false;
		} else {
			needZSLOn = true;
		}
		// if (mContinuousFocusSupported &&
		// ApiHelper.HAS_AUTO_FOCUS_MOVE_CALLBACK&&mCameraDevice!=null) {
		// mCameraDevice.setAutoFocusMoveCallback(mHandler,
		// (CameraAFMoveCallback) mAutoFocusMoveCallback);
		// }

	}

	private void setSportsMode(boolean on) {
		Editor editor = mPreferences.edit();
		if (on)
			editor.putString(CameraSettings.KEY_CAMERA_SPORTS, "on");
		else
			editor.putString(CameraSettings.KEY_CAMERA_SPORTS, "off");
		editor.apply();
	}

	/*
	private void setQRcodeMode(boolean on) {
		mTopViewManager.setMenuEnabled(!on);
		if (on) {
			mUI.hideFocus();
			mQrcodeViewManager.show();
			mQrcodeViewManager.reInflate();
			checkPreviewCallback(false);
		} else {
			if (mCameraDevice != null) {
				mQrcodeViewManager.hide();
				mUI.showFocus();
				// boolean
				// isASD=mActivity.getString(R.string.setting_on_value).equals(
				// mPreferences.getString(
				// CameraSettings.KEY_CAMERA_ARCSOFT_ASD,
				// mActivity.getString(R.string.setting_on_value)));
				// //PR931355-sichao.hu add begin
				// if(!isASD){
				// mIsEnableNight=false;
				// mAsdState=auto;
				// }//PR931355-sichao.hu add end
				//
				checkPreviewCallback(false);
				// if(!(isASD&&mActivity.isNonePickIntent())){
				// mCameraDevice.setPreviewDataCallback(mHandler,null);
				// }else{//PR931355-sichao.hu add begin
				// mCameraDevice.setPreviewDataCallback(mHandler, arcsoftCB);
				// }//PR931355-sichao.hu add end
			}
		}

	}*/

	// wxt @Override
	public void resetToDefault() {
		if (mGC == null)
			return;
		// super.resetToDefault();
		QRCodeManager.resetPrefsToDefault(mActivity);
		mGC.resetPrefsToDefault();
		// Add by zhimin.yu for PR970374 begin
		//resetToDefaultFaceBeautySetting();
		// Add by zhimin.yu for PR970374 end
		// close current mode
		closeAllMode();
		// / add by yaping.liu for pr966435 {
		if (mPreferences != null) {
			mPreferences.edit().remove(CameraSettings.KEY_CAMERA_SAVEPATH)
					.commit();
		}
		// / }
		onSharedPreferenceChanged();
	}

	@Override
	public void onSharedPreferenceChanged() {
		Log.d(TAG,"###YUHUAN###onSharedPreferenceChanged ");
		// ignore the events after "onPause()"
		if (mPaused)
			return;
		// [BUGFIX]-ADD-BEGIN by xuan.zhou, PR-621401, 2014-4-8
		// It's better to do this after KEY_GPS_TAG set.
		// boolean recordLocation = RecordLocationPreference.get(
		// mPreferences, mContentResolver);
		// mLocationManager.recordLocation(recordLocation);
		// [BUGFIX]-ADD-END by xuan.zhou, PR-621401, 2014-4-8

		/*
		boolean isASD = mActivity.getString(R.string.setting_on_value).equals(
				mPreferences.getString(CameraSettings.KEY_CAMERA_ARCSOFT_ASD,
						mActivity.getString(R.string.setting_on_value)));
		Log.d(TAG,"###YUHUAN###onSharedPreferenceChanged#isASD " + isASD);

		boolean needRestartPreviewInCaseOfASD = false;
		if (!isASD) {
			//mIsEnableNight = false;
			mAsdState = ASD_AUTO;
		}*/

		// if(isASD){
		// mActivity.getDrawLayout().disableMenu(ModeListView.MODE_ARCSOFT_FACEBEAUTY_ID);
		// }else{
		// mActivity.getDrawLayout().enableMenu(ModeListView.MODE_ARCSOFT_FACEBEAUTY_ID);
		// }
		if (needRestart()) {
			Log.v(TAG, "luna Restarting Preview... Camera Mode Changhed");
			stopPreview();
			startPreview();
			setCameraState(IDLE);
			mRestartPreview = false;
		}

		//checkPreviewCallback(false);
		/*
		 * Check if the PhotoUI Menu is initialized or not. This should be
		 * initialized during onCameraOpen() which should have been called by
		 * now. But for some reason that is not executed till now, then schedule
		 * these functionality for later by posting a message to the handler
		 */
		if (mUI.mMenuInitialized) {
			CameraActivity.TraceLog(TAG, "onSharePreferenceChanged in idle");
			setCameraParametersWhenIdle(UPDATE_PARAM_PREFERENCE);
			mUI.updateOnScreenIndicators(mParameters, mPreferenceGroup,
					mPreferences);
			resizeForPreviewAspectRatio();
		} else {
			mHandler.sendEmptyMessage(SET_PHOTO_UI_PARAMS);
		}
		/*
		 * if (mSeekBarInitialized == true){ Log.v(TAG,
		 * "onSharedPreferenceChanged Skin tone bar: change"); // skin tone is
		 * enabled only for party and portrait BSM // when color effects are not
		 * enabled String colorEffect = mPreferences.getString(
		 * CameraSettings.KEY_COLOR_EFFECT,
		 * mActivity.getString(R.string.pref_camera_coloreffect_default));
		 * if((Parameters.SCENE_MODE_PARTY.equals(mSceneMode) ||
		 * Parameters.SCENE_MODE_PORTRAIT.equals(mSceneMode)) &&
		 * (Parameters.EFFECT_NONE.equals(colorEffect))) { Log.v(TAG,
		 * "Party/Portrait + No effect, SkinToneBar enabled"); } else {
		 * disableSkinToneSeekBar(); } }
		 */
		// mActivity.gIsNightMode=mActivity.getString(R.string.setting_on_value).equals(
		// mPreferences.getString(
		// CameraSettings.KEY_CAMERA_ARCSOFT_NIGHT,
		// mActivity.getString(R.string.setting_off_value)));
		// / modify by yaping.liu for pr966435 {
		String savePathValue = mPreferences.getString(
				CameraSettings.KEY_CAMERA_SAVEPATH, "-1");
		Storage.setSaveSDCard(savePathValue.equals("1"),
				savePathValue.equals("-1"));
		// / }
		mActivity.updateStorageSpaceAndHint();
		CameraActivity.MAX_SCALE = Integer.parseInt(CustomUtil.getInstance()
				.getString(CustomFields.DEF_SCALE_MAX, "4"));

		boolean isGpsEnable = mPreferences.getString(
				CameraSettings.KEY_GPS_TAG,
				mActivity.getString(R.string.pref_gps_tag_default)).equals(
				mActivity.getString(R.string.setting_on_value));
		Log.d(TAG,"###YUHUAN###onSharedPreferenceChanged#isGpsEnable=" + isGpsEnable);
		
		// [BUGFIX]-ADD-BEGIN by xuan.zhou, PR-621401, 2014-4-8
		isGpsEnable = checkLocationService(isGpsEnable);
		CameraActivity.TraceLog(TAG,
				"onSharedPreferenceChanged : isGpsEnable = " + isGpsEnable
						+ "; isLocationOpened = " + isLocationOpened);
		if (isGpsEnable) {
			if (!isLocationOpened) {
				isLocationOpened = true;
				setLocationPreference(RecordLocationPreference.VALUE_ON);
			}
		} else {
			if (isLocationOpened) {
				isLocationOpened = false;
				setLocationPreference(RecordLocationPreference.VALUE_OFF);
			}
		}
		boolean recordLocation = RecordLocationPreference.get(mPreferences,
				mContentResolver);
		CameraActivity.TraceLog(TAG,
				"onSharedPreferenceChanged : mPreferences = " + mPreferences
						+ "; mContentResolver = " + mContentResolver
						+ "; recordLocation = " + recordLocation);
		mLocationManager.recordLocation(recordLocation);
		// [BUGFIX]-ADD-END by xuan.zhou, PR-621401, 2014-4-8
	}

	// Add by zhimin.yu for PR967300 begin
	public void updateisLocationOpend(boolean b) {
		isLocationOpened = b;
	}

	// Add by zhimin.yu for PR967300 end
	private boolean checkLocationService(boolean enable) {
		boolean recordLocation = enable;
		// If GPS tag is set off, not check needed.
		Log.d(TAG,"###YUHUAN###checkLocationService#recordLocation=" + recordLocation);
		if (!recordLocation)
			return recordLocation;

		boolean gpsEnabled = Settings.Secure
				.isLocationProviderEnabled(mContentResolver,
						android.location.LocationManager.GPS_PROVIDER);
		Log.d(TAG,"###YUHUAN###checkLocationService#gpsEnabled=" + gpsEnabled);
		boolean networkEnabled = Settings.Secure.isLocationProviderEnabled(
				mContentResolver,
				android.location.LocationManager.NETWORK_PROVIDER);
		Log.d(TAG,"###YUHUAN###checkLocationService#networkEnabled=" + networkEnabled);

		if (!(gpsEnabled || networkEnabled)) {
			recordLocation = false;

			// GPS is not available, set the tag off
			setGpsTagPreference(mActivity.getString(R.string.setting_off_value));
			setLocationPreference(RecordLocationPreference.VALUE_OFF);
			mActivity.getCameraViewManager().reloadAdvancedPreference();

			// Show an alertDialog to prompt user to enable location services.
			// [BUGFIX]-ADD-BEGIN by xuan.zhou, PR-655302, 2014-4-23
			new AlertDialog.Builder(mActivity)
					.setIconAttribute(android.R.attr.alertDialogIcon)
					.setTitle(R.string.location_service_dialog_title)
					.setMessage(R.string.location_service_dialog_msg)
					.setPositiveButton(
							R.string.location_service__dialog_setting,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int arg1) {
									Intent intent = new Intent(
											"android.settings.LOCATION_SOURCE_SETTINGS");
									// / modify by yaping.liu for pr963472 {
									mActivity.startActivityForResult(intent,
											SETGPS);
									// / }
								}
							})
					.setNegativeButton(R.string.location_service_dialog_cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int arg1) {
									// / add by yaping.liu for pr963472 {
									// mTopViewManager.refreshMenu();
									// / }
									dialog.cancel();
								}
							}).show();
			mTopViewManager.refreshMenu();// Add by zhimin.yu for PR968957
		}

		return recordLocation;
	}

	// [BUGFIX]-ADD-END by xuan.zhou, PR-621401, 2014-4-8

	private void setGpsTagPreference(String value) {
		Log.d(TAG,"###YUHUAN###setGpsTagPreference#value=" + value);
		Editor editor = mPreferences.edit();
		editor.putString(CameraSettings.KEY_GPS_TAG, value);
		editor.apply();
	}

	@Override
	public void onCameraPickerClicked(int cameraId) {
		// modify by minghui.hua for PR942353,968128
		CameraActivity.TraceLog(TAG,
				"onCameraPickerClicked switch camera. cameraId=" + cameraId);
		String action = mActivity.getIntent().getAction();
		if (GC.START_FRONT_CAMERA_INTENT.equals(action)
				|| GC.START_QRSCAN_INTENT.equals(action)) {
			mActivity.setCurIntent(Intent.ACTION_MAIN);
		}
		mPendingSwitchCameraId = cameraId;

		// [BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-22,PR764214 Begin
		mTopViewManager.setCameraPickerEnabled(false);
		// [BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-22,PR764214 End
		// We need to keep a preview frame for the animation before
		// releasing the camera. This will trigger onPreviewTextureCopied.
		// TODO: Need to animate the camera switch
		switchCamera();
		// [BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-22,PR764214 Begin
		mTopViewManager.setCameraPickerEnabled(true);
		// [BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-22,PR764214 End
	}

	@Override
	public boolean onCameraSwitched() {
		Log.v(TAG, "WXT onCameraSwitched ");
		if (mPaused || mPendingSwitchCameraId != -1)
			return false;

		if (mPreferences == null || mPreferenceGroup == null) {
			// modify by minghui.hua for PR1010758,1022137
			CameraActivity.TraceLog(TAG, "onCameraSwitched return false");
			return false;
		}
		ListPreference pref = mPreferenceGroup
				.findPreference(CameraSettings.KEY_CAMERA_ID);
		if (pref != null) {
			int index = pref.findIndexOfValue(pref.getValue());
			CharSequence[] values = pref.getEntryValues();
			index = (index + 1) % values.length;
			int newCameraId = Integer.parseInt((String) values[index]);
			if (mCameraId == newCameraId) {
				index = (index + 1) % values.length;
				newCameraId = Integer.parseInt((String) values[index]);
			}
			mPendingSwitchCameraId = newCameraId;
			onCameraPickerClicked(mPendingSwitchCameraId);
			Log.v(TAG,
					"WXT onCameraSwitched return ture  mPendingSwitchCameraId:"
							+ mPendingSwitchCameraId);
			return true;
		}
		Log.v(TAG, "WXT onCameraSwitched return false ");
		return false;
	}

	@Override
	public boolean onFlashPicked() {
		if (mPaused)
			return false;
		setCameraParameters(UPDATE_PARAM_PREFERENCE);
		return true;
	}

	// Preview texture has been copied. Now camera can be released and the
	// animation can be started.
	@Override
	public void onPreviewTextureCopied() {
		mHandler.sendEmptyMessage(SWITCH_CAMERA);
	}

	@Override
	public void onCaptureTextureCopied() {
	}

	@Override
	public void onUserInteraction() {
		if (!mActivity.isFinishing())
			keepScreenOnAwhile();
	}

	private void resetScreenOn() {
		// mHandler.removeMessages(CLEAR_SCREEN_DELAY);
		// mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private void keepScreenOnAwhile() {
		// mHandler.removeMessages(CLEAR_SCREEN_DELAY);
		// mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// mHandler.sendEmptyMessageDelayed(CLEAR_SCREEN_DELAY, SCREEN_DELAY);
	}

	@Override
	public void onOverriddenPreferencesClicked() {
		if (mPaused)
			return;
		mUI.showPreferencesToast();
	}

	private void initTargetRatio() {
		targetRatio = mGC.getPreviewRatio();
	}

	private PictureSize getPictureSize() {
		String pictureSizeFlag = null;
		String defaultFlag = null;
		if (mCameraId == 0) {
			defaultFlag = mActivity
					.getString(R.string.pref_camera_picturesize_flag_default);

		} else if (mCameraId == 1) {
			defaultFlag = mActivity
					.getString(R.string.pref_camera_picturesize_flag_default_front);
		}
		pictureSizeFlag = mPreferences.getString(
				CameraSettings.KEY_PICTURE_SIZE_FLAG, defaultFlag);
		Log.d(TAG,"###YUHUAN###getPictureSize#pictureSizeFlag=" + pictureSizeFlag);
		initTargetRatio();

		PictureSizePerso picPerso = PictureSizePerso.getInstance();
		if (!picPerso.isInitialized(mCameraId)) {
			picPerso.init(mCameraDevice, mCameraId);
		}

		int pictureSizeFlagNum = Integer.valueOf(pictureSizeFlag);
		Log.d(TAG,"###YUHUAN###getPictureSize#pictureSizeFlagNum=" + pictureSizeFlagNum);
		
		PictureSize picSize = null;
		if (mActivity.getCameraId() == GC.CAMERAID_FRONT) {
			picSize = picPerso.getPersoSupportedSizes(mActivity.getCameraId())
					.get(pictureSizeFlagNum);
		} else if (mActivity.getCameraId() == GC.CAMERAID_BACK) {
			picSize = picPerso.getPersoSupportedSizes(mActivity.getCameraId())
					.get(pictureSizeFlagNum);
		}
		
		Log.d(TAG,"###YUHUAN###getPictureSize#picSize=" + picSize.width + "x" + picSize.height);
		if (picSize != null) {
			int w = picSize.width;
			int h = picSize.height;
			// modify by yaping.liu begin
			// Set a jpegthumbnail size that is closest to the Picture height
			// and has the right aspect ratio.
			List<Size> sizes = mParameters.getSupportedJpegThumbnailSizes();
			Size optimalJpegThumbnailSize = CameraUtil
					.getOptimalJpegThumbnailSize(sizes, (double) w / h);
			Size originalThumbnailSize = mParameters.getJpegThumbnailSize();
			checkThread();
			if (!originalThumbnailSize.equals(optimalJpegThumbnailSize)) {
				mParameters.setJpegThumbnailSize(
						optimalJpegThumbnailSize.width,
						optimalJpegThumbnailSize.height);
			}
			// modify by yaping.liu end

			mParameters.setJpegThumbnailQuality(85);
		}
		return picSize;
	}

	private void showTapToFocusToast() {
		// TODO: Use a toast?
		new RotateTextToast(mActivity, R.string.tap_to_focus, 0).show();
		// Clear the preference.
		Editor editor = mPreferences.edit();
		editor.putBoolean(CameraSettings.KEY_CAMERA_FIRST_USE_HINT_SHOWN, false);
		editor.apply();
	}

	private void initializeCapabilities() {
		mInitialParams = mCameraDevice.getParameters();
		mFocusAreaSupported = CameraUtil.isFocusAreaSupported(mInitialParams);
		mMeteringAreaSupported = CameraUtil
				.isMeteringAreaSupported(mInitialParams);
		mAeLockSupported = CameraUtil
				.isAutoExposureLockSupported(mInitialParams);
		mAwbLockSupported = CameraUtil
				.isAutoWhiteBalanceLockSupported(mInitialParams);
		mContinuousFocusSupported = mInitialParams.getSupportedFocusModes()
				.contains(CameraUtil.FOCUS_MODE_CONTINUOUS_PICTURE);
	}

	@Override
	public void onCountDownFinished() {
		mSnapshotOnIdle = false;
		mActivity.setSwipingEnabled(true);
		mActivity.enableDrawer();
		mFocusManager.doSnap();
		mFocusManager.onShutterUp();
	}

	@Override
	public void onCountDownCanceled() {
		mSnapshotOnIdle = false;
		mActivity.setSwipingEnabled(true);
		mActivity.enableDrawer();
	}

	@Override
	public void onShowSwitcherPopup() {
		mUI.onShowSwitcherPopup();
	}

	@Override
	public int onZoomChanged(int index) {
		// Not useful to change zoom value when the activity is paused.
		if (mPaused)
			return index;
		mZoomValue = index;
		if (mParameters == null || mCameraDevice == null)
			return index;
		// Set zoom parameters asynchronously
		checkThread();
		mParameters.setZoom(mZoomValue);
		mCameraDevice.setParameters(mParameters);
		Parameters p = mCameraDevice.getParameters();
		if (p != null)
			return p.getZoom();
		return index;
	}

	@Override
	public void onZoomIndexChanged(int index) {
		int currentIndex = onZoomChanged(index);
		Log.i(TAG, "LMCH0416 onZoomIndexChanged index:" + currentIndex);
	}

	@Override
	public int getCameraState() {
		return mCameraState;
	}

	@Override
	public void onQueueStatus(boolean full) {
		mShutterManager.enableShutter(!full);
	}

	@Override
	public void onMediaSaveServiceConnected(MediaSaveService s) {
		// We set the listener only when both service and shutterbutton
		// are initialized.
		if (mFirstTimeInitialized) {
			s.setListener(this);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int type = event.sensor.getType();
		float[] data;
		if (type == Sensor.TYPE_ACCELEROMETER) {
			data = mGData;
		} else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
			data = mMData;
		} else {
			// we should not be here.
			return;
		}
		for (int i = 0; i < 3; i++) {
			data[i] = event.values[i];
		}
		float[] orientation = new float[3];
		SensorManager.getRotationMatrix(mR, null, mGData, mMData);
		SensorManager.getOrientation(mR, orientation);
		mHeading = (int) (orientation[0] * 180f / Math.PI) % 360;
		if (mHeading < 0) {
			mHeading += 360;
		}
	}

	private boolean mIsFaceBeauty = false;

	public void setFaceBeauty(boolean isFaceBeauty) {
		mIsFaceBeauty = isFaceBeauty;
		mActivity.gIsFaceBeauty = isFaceBeauty;
	}

	@Override
	public void onPreviewFocusChanged(boolean previewFocused) {
		mUI.onPreviewFocusChanged(previewFocused);
	}

	// TODO: Delete this function after old camera code is removed
	@Override
	public void onRestorePreferencesClicked() {
	}

	/*
	 * private void setSkinToneFactor() { if(mCameraDevice == null ||
	 * mParameters == null || skinToneSeekBar == null) return;
	 * 
	 * String skinToneEnhancementPref = "enable";
	 * if(CameraUtil.isSupported(skinToneEnhancementPref,
	 * mParameters.getSupportedSkinToneEnhancementModes())) {
	 * if(skinToneEnhancementPref.equals("enable")) { int skinToneValue =0; int
	 * progress; //get the value for the first time! if (mskinToneValue ==0) {
	 * String factor = mPreferences.getString(
	 * CameraSettings.KEY_SKIN_TONE_ENHANCEMENT_FACTOR, "0"); skinToneValue =
	 * Integer.parseInt(factor); }
	 * 
	 * Log.v(TAG, "Skin tone bar: enable = " + mskinToneValue);
	 * enableSkinToneSeekBar(); //As a wrokaround set progress again to show the
	 * actually progress on screen. if (skinToneValue != 0) { progress =
	 * (skinToneValue/SCE_FACTOR_STEP)-MIN_SCE_FACTOR;
	 * skinToneSeekBar.setProgress(progress); } } else { Log.v(TAG,
	 * "Skin tone bar: disable"); disableSkinToneSeekBar(); } } else {
	 * Log.v(TAG, "Skin tone bar: Not supported");
	 * skinToneSeekBar.setVisibility(View.INVISIBLE); } }
	 * 
	 * private void enableSkinToneSeekBar() { int progress;
	 * if(brightnessProgressBar != null)
	 * brightnessProgressBar.setVisibility(View.INVISIBLE);
	 * skinToneSeekBar.setMax(MAX_SCE_FACTOR-MIN_SCE_FACTOR);
	 * skinToneSeekBar.setVisibility(View.VISIBLE);
	 * skinToneSeekBar.requestFocus(); if (mskinToneValue != 0) { progress =
	 * (mskinToneValue/SCE_FACTOR_STEP)-MIN_SCE_FACTOR;
	 * mskinToneSeekListener.onProgressChanged(skinToneSeekBar, progress,
	 * false); } else { progress = (MAX_SCE_FACTOR-MIN_SCE_FACTOR)/2;
	 * RightValue.setText(""); LeftValue.setText(""); }
	 * skinToneSeekBar.setProgress(progress);
	 * mActivity.findViewById(R.id.linear).bringToFront();
	 * skinToneSeekBar.bringToFront(); Title.setText("Skin Tone Enhancement");
	 * Title.setVisibility(View.VISIBLE);
	 * RightValue.setVisibility(View.VISIBLE);
	 * LeftValue.setVisibility(View.VISIBLE); mSkinToneSeekBar = true; }
	 * 
	 * private void disableSkinToneSeekBar() {
	 * skinToneSeekBar.setVisibility(View.INVISIBLE);
	 * Title.setVisibility(View.INVISIBLE);
	 * RightValue.setVisibility(View.INVISIBLE);
	 * LeftValue.setVisibility(View.INVISIBLE); mskinToneValue = 0;
	 * mSkinToneSeekBar = false; Editor editor = mPreferences.edit();
	 * editor.putString(CameraSettings.KEY_SKIN_TONE_ENHANCEMENT_FACTOR,
	 * Integer.toString(mskinToneValue - MIN_SCE_FACTOR)); editor.apply();
	 * if(brightnessProgressBar != null)
	 * brightnessProgressBar.setVisibility(View.VISIBLE); }
	 */
	/*
	 * Provide a mapping for Jpeg encoding quality levels from String
	 * representation to numeric representation.
	 */
	@Override
	public boolean arePreviewControlsVisible() {
		return mUI.arePreviewControlsVisible();
	}

	// For debugging only.
	public void setDebugUri(Uri uri) {
		mDebugUri = uri;
	}

	// For debugging only.
	private void saveToDebugUri(byte[] data) {
		if (mDebugUri != null) {
			OutputStream outputStream = null;
			try {
				outputStream = mContentResolver.openOutputStream(mDebugUri);
				outputStream.write(data);
				outputStream.close();
			} catch (IOException e) {
				Log.e(TAG, "Exception while writing debug jpeg file", e);
			} finally {
				CameraUtil.closeSilently(outputStream);
			}
		}
	}

	@Override
	public void updateFaceBeautySetting(int value, int type) {
		// TODO Auto-generated method stub
		/*
		if (type == BeautyFaceSeekBar.SOFTEN_TYPE) {
			faceBeauty_soften = value;
		} else if (type == BeautyFaceSeekBar.BRIGHT_TYPE) {
			faceBeauty_bright = value;
		}
		if (null != mParameters) {
			checkThread();
			mParameters.set("arcsoft-mode", "abs");
			mParameters.set("arcsoft-skin-soften", faceBeauty_soften);
			mParameters.set("arcsoft-skin-bright", faceBeauty_bright);
		}
		applyParametersToServer();
		saveFaceBeautySBValue();// pr1004544-yuguan.chen added
		*/
	}

	// Add by zhimin.yu for PR970374 begin
	/*
	private void resetToDefaultFaceBeautySetting() {
		faceBeauty_soften = FACEBEAUTY_SOFTEN;
		faceBeauty_bright = FACEBEAUTY_BRIGHT;
		if (null != mParameters) {
			checkThread();
			mParameters.set("arcsoft-mode", "off");
			mParameters.set("arcsoft-skin-soften", faceBeauty_soften);
			mParameters.set("arcsoft-skin-bright", faceBeauty_soften);
		}
		applyParametersToServer();
	}*/

	// Add by zhimin.yu for PR970374 end

	private IStartPreviewCallback mStartPreviewCallback;

	@Override
	public void setStartPreviewCallback(IStartPreviewCallback cb) {
		mStartPreviewCallback = cb;
	}

	@Override
	public void checkShutter() {
		if (mFocusManager != null && mFocusManager.isFocusIdle()
				&& mCameraState == IDLE) {
			if (mShutterManager != null) {
				mShutterManager.enableShutter(true);
			}
		}
	}

	// modify by minghui.hua for PR992130
	private void checkThread() {
		if (Thread.currentThread().getId() != 1) {
			CameraActivity.TraceQCTLog("set parameters not in main thread",
					new Throwable());
		}
	}
}

/*
 * Below is no longer needed, except to get rid of compile error TODO: Remove
 * these
 */
class JpegEncodingQualityMappings {
	private static final String TAG = "JpegEncodingQualityMappings";
	private static final int DEFAULT_QUALITY = 85;
	private static HashMap<String, Integer> mHashMap = new HashMap<String, Integer>();

	static {
		mHashMap.put("normal", CameraProfile.QUALITY_LOW);
		mHashMap.put("fine", CameraProfile.QUALITY_MEDIUM);
		mHashMap.put("superfine", CameraProfile.QUALITY_HIGH);
	}

	// Retrieve and return the Jpeg encoding quality number
	// for the given quality level.
	public static int getQualityNumber(String jpegQuality) {
		try {
			int qualityPercentile = Integer.parseInt(jpegQuality);
			if (qualityPercentile >= 0 && qualityPercentile <= 100)
				return qualityPercentile;
			else
				return DEFAULT_QUALITY;
		} catch (NumberFormatException nfe) {
			// chosen quality is not a number, continue
		}
		Integer quality = mHashMap.get(jpegQuality);
		if (quality == null) {
			Log.w(TAG, "Unknown Jpeg quality: " + jpegQuality);
			return DEFAULT_QUALITY;
		}
		return CameraProfile
				.getJpegEncodingQualityParameter(quality.intValue());
	}
}

class GraphView extends View {
	private Bitmap mBitmap;
	private Paint mPaint = new Paint();
	private Paint mPaintRect = new Paint();
	private Canvas mCanvas = new Canvas();
	private float mScale = (float) 3;
	private float mWidth;
	private float mHeight;
	private PhotoModule mPhotoModule;
	private CameraManager.CameraProxy mGraphCameraDevice;
	private float scaled;
	private static final int STATS_SIZE = 256;
	private static final String TAG = "GraphView";

	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mPaintRect.setColor(0xFFFFFFFF);
		mPaintRect.setStyle(Paint.Style.FILL);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		mCanvas.setBitmap(mBitmap);
		mWidth = w;
		mHeight = h;
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.v(TAG, "in Camera.java ondraw");
		if (mPhotoModule == null || !mPhotoModule.mHiston) {
			Log.e(TAG, "returning as histogram is off ");
			return;
		}

		if (mBitmap != null) {
			final Paint paint = mPaint;
			final Canvas cavas = mCanvas;
			final float border = 5;
			float graphheight = mHeight - (2 * border);
			float graphwidth = mWidth - (2 * border);
			float left, top, right, bottom;
			float bargap = 0.0f;
			float barwidth = graphwidth / STATS_SIZE;

			cavas.drawColor(0xFFAAAAAA);
			paint.setColor(Color.BLACK);

			for (int k = 0; k <= (graphheight / 32); k++) {
				float y = (float) (32 * k) + border;
				cavas.drawLine(border, y, graphwidth + border, y, paint);
			}
			for (int j = 0; j <= (graphwidth / 32); j++) {
				float x = (float) (32 * j) + border;
				cavas.drawLine(x, border, x, graphheight + border, paint);
			}
			synchronized (PhotoModule.statsdata) {
				// Assumption: The first element contains
				// the maximum value.
				int maxValue = Integer.MIN_VALUE;
				if (0 == PhotoModule.statsdata[0]) {
					for (int i = 1; i <= STATS_SIZE; i++) {
						if (maxValue < PhotoModule.statsdata[i]) {
							maxValue = PhotoModule.statsdata[i];
						}
					}
				} else {
					maxValue = PhotoModule.statsdata[0];
				}
				mScale = (float) maxValue;
				for (int i = 1; i <= STATS_SIZE; i++) {
					scaled = (PhotoModule.statsdata[i] / mScale) * STATS_SIZE;
					if (scaled >= (float) STATS_SIZE)
						scaled = (float) STATS_SIZE;
					left = (bargap * (i + 1)) + (barwidth * i) + border;
					top = graphheight + border;
					right = left + barwidth;
					bottom = top - scaled;
					cavas.drawRect(left, top, right, bottom, mPaintRect);
				}
			}
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}
		if (mPhotoModule.mHiston && mPhotoModule != null) {
			mGraphCameraDevice = mPhotoModule.getCamera();
			if (mGraphCameraDevice != null) {
				mGraphCameraDevice.sendHistogramData();
			}
		}
	}

	public void PreviewChanged() {
		invalidate();
	}

	public void setPhotoModuleObject(PhotoModule photoModule) {
		mPhotoModule = photoModule;
	}

}

interface FaceBeautySettingCallBack {
	void updateFaceBeautySetting(int value, int type);
}
