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
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 08/01/2014|       zhijie.gui     |      PR-734011       |turn on anti-band */
/* ----------|----------------------|----------------------|----------------- */
/* 08/12/2014|Fanghua.Gu            |738082                |[MMS]There have n */
/*           |                      |                      |o refresh option  */
/*           |                      |                      |when capture a vi */
/*           |                      |                      |deo in MMS        */
/* ----------|----------------------|----------------------|----------------- */
/* 08/20/2014|jian.zhang1           |753112                |Stop the MediaRec-*/
/*           |                      |                      |odrder in thread  */
/* ----------|----------------------|----------------------|----------------- */
/* ========================================================================== */

package com.android.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.camera.CameraManager.CameraAFCallback;
import com.android.camera.CameraManager.CameraPictureCallback;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.app.OrientationManager;
import com.android.camera.custom.CustomFields;
import com.android.camera.custom.CustomUtil;
import com.android.camera.exif.ExifInterface;
import com.android.camera.exif.ExifTag;
import com.android.camera.manager.ShutterManager;
import com.android.camera.manager.SwitcherManager;
import com.android.camera.manager.TopViewManager;
import com.android.camera.manager.ZoomManager;
import com.android.camera.ui.RotateTextToast;
import com.android.camera.ui.ShutterButton;
import com.android.camera.ui.ZoomControl;
import com.android.camera.util.AccessibilityUtils;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.UsageStatistics;
import com.android.jrd.externel.CamcorderProfileEx;
import com.android.jrd.externel.ExtendMediaRecorder;
import com.android.jrd.externel.ExtendParameters;
import com.tct.camera.R;
//import android.os.SystemProperties;
//import android.media.EncoderCapabilities;
//import android.media.EncoderCapabilities.VideoEncoderCap;

public class VideoModule implements CameraModule,
    VideoController,
    ZoomControl.OnZoomIndexChangedListener,
    CameraPreference.OnPreferenceChangedListener,
    ShutterButton.OnShutterButtonListener,
    MediaRecorder.OnErrorListener,
    MediaRecorder.OnInfoListener,
    TopViewManager.SettingListener{

    private static final String TAG = "YUHUAN_VideoModule";

    private static final int CHECK_DISPLAY_ROTATION = 3;
    private static final int CLEAR_SCREEN_DELAY = 4;
    private static final int UPDATE_RECORD_TIME = 5;
    private static final int ENABLE_SHUTTER_BUTTON = 6;
    private static final int SHOW_TAP_TO_SNAPSHOT_TOAST = 7;
    private static final int SWITCH_CAMERA = 8;
    private static final int SWITCH_CAMERA_START_ANIMATION = 9;
    private static final int MSG_CHANGE_FOCUS = 10;
    private static final int RECORD_END_MSG = 11;

    private static final int SCREEN_DELAY = 2 * 60 * 1000;
    private static final int CHANGE_FOCUS_DELAY = 3000;

    private static final long SHUTTER_BUTTON_TIMEOUT = 500L; // 500ms

    /**
     * An unpublished intent flag requesting to start recording straight away
     * and return as soon as recording is stopped.
     * TODO: consider publishing by moving into MediaStore.
     */
    private static final String EXTRA_QUICK_CAPTURE =
            "android.intent.extra.quickCapture";

    //[BUGFIX]Added by jian.zhang1 for PR753112 2014.08.20 Begin
    private boolean shouldAddToMediaStoreNow = false;
    private static boolean isSaving = false;
    private static final int TCT_SAVE_SUCCESS = 1;
    private static final int TCT_SAVE_FAIL = 0;
    public static SavingThread mSavingThread;
    private static final int VIDEO_SAVE_COMPLETE = 13;
    //[BUGFIX]Added by jian.zhang1 for PR753112 2014.08.20 End

    // module fields
    private CameraActivity mActivity;
    private boolean mPaused;
    private int mCameraId;
    private Parameters mParameters;

    private boolean mIsInReviewMode;
    private boolean mSnapshotInProgress = false;

    private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();

    private ComboPreferences mPreferences;
    private PreferenceGroup mPreferenceGroup;
    private boolean mSaveToSDCard = false;

    // Preference must be read before starting preview. We check this before starting
    // preview.
    private boolean mPreferenceRead;

    private boolean mIsVideoCaptureIntent;
    private boolean mQuickCapture;

    private MediaRecorder mMediaRecorder;

    private boolean mSwitchingCamera;
    private boolean mMediaRecorderRecording = false;
    private long mRecordingStartTime;
    private boolean mRecordingTimeCountsDown = false;
    private long mOnResumeTime;
    // The video file that the hardware camera is about to record into
    // (or is recording into.)
    private String mVideoFilename;
    private ParcelFileDescriptor mVideoFileDescriptor;

    // The video file that has already been recorded, and that is being
    // examined by the user.
    private String mCurrentVideoFilename;
    private Uri mCurrentVideoUri;
    private boolean mCurrentVideoUriFromMediaSaved;
    private ContentValues mCurrentVideoValues;

    private CamcorderProfile mProfile;

    // The video duration limit. 0 menas no limit.
    private int mMaxVideoDurationInMs;
    private long mMaxVideoSize;

    // Time Lapse parameters.
    private boolean mCaptureTimeLapse = false;
    // Default 0. If it is larger than 0, the camcorder is in time lapse mode.
    private int mTimeBetweenTimeLapseFrameCaptureMs = 0;

    boolean mPreviewing = false; // True if preview is started.
    // The display rotation in degrees. This is only valid when mPreviewing is
    // true.
    private int mDisplayRotation;
    private int mCameraDisplayOrientation;

    private int mDesiredPreviewWidth;
    private int mDesiredPreviewHeight;
    private ContentResolver mContentResolver;

    private TopViewManager mTopViewManager;
    private ShutterManager mShutterManager;
    private SwitcherManager mSwitcherManager;
    private ZoomManager mZoomManager;

    private LocationManager mLocationManager;
    private OrientationManager mOrientationManager;
    private static final String KEY_PREVIEW_FORMAT = "preview-format";
    private static final String QC_FORMAT_NV12_VENUS = "nv12-venus";
    private int mPendingSwitchCameraId;
    private final Handler mHandler = new MainHandler();
    private VideoUI mUI;
    private CameraProxy mCameraDevice;

    // The degrees of the device rotated clockwise from its natural orientation.
    private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;

    private int mZoomValue;  // The current zoom value.

    private boolean mStartRecPending = false;
    private boolean mStopRecPending = false;
    private boolean mStartPrevPending = false;
    private boolean mStopPrevPending = false;

    // The preview window is on focus
    private boolean mPreviewFocused = false;
    private boolean mRecordingSupported = true;

    private final AutoFocusCallback mAutoFocusCallback =
            new AutoFocusCallback();
    
    private final MediaSaveService.OnMediaSavedListener mOnVideoSavedListener =
            new MediaSaveService.OnMediaSavedListener() {
                @Override
                public void onMediaSaved(Uri uri) {
                    if (uri != null) {
                        mCurrentVideoUri = uri;
                        mCurrentVideoUriFromMediaSaved = true;
                        onVideoSaved();
                        mActivity.notifyNewMedia(uri);
                    }
                }
            };

    private final MediaSaveService.OnMediaSavedListener mOnPhotoSavedListener =
            new MediaSaveService.OnMediaSavedListener() {
                @Override
                public void onMediaSaved(Uri uri) {
                    if (uri != null) {
                        mActivity.notifyNewMedia(uri);
                    }
                }
            };


    private boolean mMediaRecoderRecordingPaused = false;
    private long mVideoRecordedDuration = 0;
    private boolean mRecordingTimeIndicator = true;

    private View.OnClickListener mPauseListener = new View.OnClickListener(){
        public void onClick(View v){
        	Log.i(TAG, "onClickListener clicked");
//            mShutterManager.setShutterPauseImage(mMediaRecoderRecordingPaused);
            mUI.setRecordingTimeImage(mMediaRecoderRecordingPaused);
            if (!mMediaRecorderRecording) {
                return;
            }

            if (mMediaRecoderRecordingPaused) {
                //[BUGFIX]-ADD-BEGIN by TCT.(Liu Jie),11/18/2013, PR-555942,The pause button press sound will be recorded in the new video.
                try {
                   Thread.sleep(400);
                } catch (Exception e) {}
                //[BUGFIX]-Add by TCT.(Liu Jie)
                try {
                    mMediaRecorder.start();
                    mRecordingStartTime = SystemClock.uptimeMillis() - mVideoRecordedDuration;
                    mVideoRecordedDuration = 0;
                    mMediaRecoderRecordingPaused = false;
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Could not start media recorder. ", e);
                    releaseMediaRecorder();
                }
            } else {
                pauseVideoRecording();
            }
        }
    };

    private View.OnClickListener mCaptureListener = new View.OnClickListener(){
        public void onClick(View v){
            takeASnapshot();
        }
    };

    private void pauseVideoRecording() {
        //[BUGFIX]-ADD-BEGIN by Xuan.Zhou, PR-581070, 2014-1-2, can't pause when SavingThread running
        if (mMediaRecorderRecording && !mMediaRecoderRecordingPaused) {
        //[BUGFIX]-ADD-END by Xuan.Zhou, PR-581070, 2014-1-2
        try {
                 ExtendMediaRecorder extMediaRecorder = ExtendMediaRecorder.getInstance(mMediaRecorder);
                 extMediaRecorder.pause();
                //mMediaRecorder.tct_pause();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Could not pause media recorder. ");
            }
            mVideoRecordedDuration = SystemClock.uptimeMillis() - mRecordingStartTime;
            mMediaRecoderRecordingPaused = true;
        }
    }

    protected class CameraOpenThread extends Thread {
        @Override
        public void run() {
            openCamera();
        }
    }

    private void openCamera() {
        if (mCameraDevice == null) {
            mCameraDevice = CameraUtil.openCamera(
                    mActivity, mCameraId, mHandler,
                    mActivity.getCameraOpenErrorCallback());
        }
        if (mCameraDevice == null) {
            // Error.
            return;
        }
        mActivity.setCameraDevice(mCameraDevice);
        mParameters = mCameraDevice.getParameters();
        mPreviewFocused = true;
    }

    //QCOM data Members Starts here
    static class DefaultHashMap<K, V> extends HashMap<K, V> {
        private V mDefaultValue;

        public void putDefault(V defaultValue) {
            mDefaultValue = defaultValue;
        }

        @Override
        public V get(Object key) {
            V value = super.get(key);
            return (value == null) ? mDefaultValue : value;
        }
        public K getKey(V toCheck) {
            Iterator<K> it = this.keySet().iterator();
            V val;
            K key;
            while(it.hasNext()) {
                key = it.next();
                val = this.get(key);
                if (val.equals(toCheck)) {
                    return key;
                }
            }
        return null;
        }
    }


    private static final DefaultHashMap<String, Integer>
            OUTPUT_FORMAT_TABLE = new DefaultHashMap<String, Integer>();
    private static final DefaultHashMap<String, Integer>
            VIDEO_ENCODER_TABLE = new DefaultHashMap<String, Integer>();
    private static final DefaultHashMap<String, Integer>
            AUDIO_ENCODER_TABLE = new DefaultHashMap<String, Integer>();
    private static final DefaultHashMap<String, Integer>
            VIDEOQUALITY_BITRATE_TABLE = new DefaultHashMap<String, Integer>();

    static {
        OUTPUT_FORMAT_TABLE.put("3gp", MediaRecorder.OutputFormat.THREE_GPP);
        OUTPUT_FORMAT_TABLE.put("mp4", MediaRecorder.OutputFormat.MPEG_4);
        OUTPUT_FORMAT_TABLE.putDefault(MediaRecorder.OutputFormat.DEFAULT);

        VIDEO_ENCODER_TABLE.put("h263", MediaRecorder.VideoEncoder.H263);
        VIDEO_ENCODER_TABLE.put("h264", MediaRecorder.VideoEncoder.H264);
        VIDEO_ENCODER_TABLE.put("m4v", MediaRecorder.VideoEncoder.MPEG_4_SP);
        VIDEO_ENCODER_TABLE.putDefault(MediaRecorder.VideoEncoder.DEFAULT);

        AUDIO_ENCODER_TABLE.put("amrnb", MediaRecorder.AudioEncoder.AMR_NB);
        // Enabled once support is added in MediaRecorder.
        // AUDIO_ENCODER_TABLE.put("qcelp", MediaRecorder.AudioEncoder.QCELP);
        // AUDIO_ENCODER_TABLE.put("evrc", MediaRecorder.AudioEncoder.EVRC);
        AUDIO_ENCODER_TABLE.put("amrwb", MediaRecorder.AudioEncoder.AMR_WB);
        AUDIO_ENCODER_TABLE.put("aac", MediaRecorder.AudioEncoder.AAC);
        AUDIO_ENCODER_TABLE.putDefault(MediaRecorder.AudioEncoder.DEFAULT);

    }

    private int mVideoEncoder;
    private int mAudioEncoder;
    private boolean mRestartPreview = false;
    private int videoWidth;
    private int videoHeight;
    boolean mUnsupportedResolution = false;
    private boolean mUnsupportedHFRVideoSize = false;
    private boolean mUnsupportedHSRVideoSize = false;
    private boolean mUnsupportedHFRVideoCodec = false;

    // This Handler is used to post message back onto the main thread of the
    // application
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            
                case ENABLE_SHUTTER_BUTTON:
                    mShutterManager.enableShutter(true);
                    break;

                case CLEAR_SCREEN_DELAY: {
                    mActivity.getWindow().clearFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
                }

                case UPDATE_RECORD_TIME: {
                    updateRecordingTime();
                    break;
                }
                case RECORD_END_MSG: {
                    if (mMediaRecorderRecording) onStopVideoRecording();

                    // Show the toast.
                    Toast.makeText(mActivity, R.string.video_reach_size_limit,
                            Toast.LENGTH_LONG).show();
                    if(!mIsVideoCaptureIntent){
                        mActivity.switchPhotoVideo(GC.MODE_PHOTO);
                    }
                }

                case CHECK_DISPLAY_ROTATION: {
                    // Restart the preview if display rotation has changed.
                    // Sometimes this happens when the device is held upside
                    // down and camera app is opened. Rotation animation will
                    // take some time and the rotation value we have got may be
                    // wrong. Framework does not have a callback for this now.
                    if ((CameraUtil.getDisplayRotation(mActivity) != mDisplayRotation)
                            && !mMediaRecorderRecording && !mSwitchingCamera) {
                        startPreview();
                    }
                    if (SystemClock.uptimeMillis() - mOnResumeTime < 5000) {
                        mHandler.sendEmptyMessageDelayed(CHECK_DISPLAY_ROTATION, 100);
                    }
                    break;
                }

                case SHOW_TAP_TO_SNAPSHOT_TOAST: {
                    //showTapToSnapshotToast();
                    break;
                }

                case SWITCH_CAMERA: {
                    switchCamera();
                    break;
                }

                //[BUGFIX]Added by jian.zhang1 for PR753112 2014.08.20 Begin
                case VIDEO_SAVE_COMPLETE: {
                    videoHasStoped(msg.arg1 == TCT_SAVE_FAIL);
                    break;
                }
                //[BUGFIX]Added by jian.zhang1 for PR753112 2014.08.20 Begin

                case SWITCH_CAMERA_START_ANIMATION: {
                    //TODO:
                    //((CameraScreenNail) mActivity.mCameraScreenNail).animateSwitchCamera();

                    // Enable all camera controls.
                    mSwitchingCamera = false;
                    break;
                }

                case MSG_CHANGE_FOCUS:{
                    mParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    mCameraDevice.setParameters(mParameters);
                    break;
                }

                default:
                    Log.v(TAG, "Unhandled message: " + msg.what);
                    break;
            }
        }
    }

    private BroadcastReceiver mReceiver = null;

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                stopVideoRecording();
            } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
                Toast.makeText(mActivity,
                        mActivity.getResources().getString(R.string.wait), Toast.LENGTH_LONG).show();
            }
        }
    }

    private String createName(long dateTaken) {
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                mActivity.getString(R.string.video_file_name_format));

        return dateFormat.format(date);
    }

    public ComboPreferences getPreferences() {
        return mPreferences;
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

    private void initializeSurfaceView() {
        if (!ApiHelper.HAS_SURFACE_TEXTURE_RECORDING) {  // API level < 16
            mUI.initializeSurfaceView();
        }
    }

    @Override
    public void init(CameraActivity activity, View root) {
        mActivity = activity;
        mUI = new VideoUI(activity, this, root);
        mPreferences = new ComboPreferences(mActivity);
        CameraSettings.upgradeGlobalPreferences(mPreferences.getGlobal());
        mCameraId = getPreferredCameraId(mPreferences);
        mActivity.setCameraId(mCameraId);

        mShutterManager = mActivity.getCameraViewManager().getShutterManager();
        mTopViewManager = mActivity.getCameraViewManager().getTopViewManager();
        mSwitcherManager = mActivity.getCameraViewManager().getSwitcherManager();
        mZoomManager = mActivity.getCameraViewManager().getZoomManager();
        mTopViewManager.setListener(this);
        mShutterManager.setOnShutterButtonListener(this);
        mShutterManager.setShutterPauseListener(mPauseListener);
        mShutterManager.setShutterCaptureListener(mCaptureListener);
        mPreferences.setLocalId(mActivity, mCameraId);
        CameraSettings.upgradeLocalPreferences(mPreferences.getLocal());

        mOrientationManager = OrientationManager.getInstance();

        /*
         * To reduce startup time, we start the preview in another thread.
         * We make sure the preview is started at the end of onCreate.
         */
        CameraOpenThread cameraOpenThread = new CameraOpenThread();
        cameraOpenThread.start();

        mContentResolver = mActivity.getContentResolver();

        /// modify by yaping.liu for pr966435 {
        String savePathValue = mPreferences.getString(CameraSettings.KEY_CAMERA_SAVEPATH, "-1");
        Storage.setSaveSDCard(savePathValue.equals("1"), savePathValue.equals("-1"));
        /// }
        mSaveToSDCard = Storage.isSaveSDCard();
        // Surface texture is from camera screen nail and startPreview needs it.
        // This must be done before startPreview.
        mIsVideoCaptureIntent = isVideoCaptureIntent();
        if(mIsVideoCaptureIntent){
           mShutterManager.mShutterButton.setImageResource(R.drawable.btn_video_capture);
        }
        initializeSurfaceView();

        // Make sure camera device is opened.
        try {
        	if(cameraOpenThread.isAlive()){//PR940446-sichao.hu added
        		cameraOpenThread.join();
        	}
            if (mCameraDevice == null) {
                return;
            }
        } catch (InterruptedException ex) {
            // ignore
        }
        readVideoPreferences();
        mUI.setPrefChangedListener(this);

        mQuickCapture = mActivity.getIntent().getBooleanExtra(EXTRA_QUICK_CAPTURE, false);
        mLocationManager = new LocationManager(mActivity, null);

        mUI.setOrientationIndicator(0, false);
        setDisplayOrientation();

        mUI.showTimeLapseUI(mCaptureTimeLapse);
        initializeVideoSnapshot();
        resizeForPreviewAspectRatio();

        initializeVideoControl();
        mPendingSwitchCameraId = -1;
    }

    // SingleTapListener
    // Preview area is touched. Take a picture.
    @Override
    public void onSingleTapUp(View view, int x, int y) {
//        takeASnapshot();
//        mShutterManager.mShutterPause.performClick();
    	if(mPaused){
    		return;
    	}
        if(mActivity.getCameraId() == GC.CAMERAID_BACK){
            if(!mParameters.getFocusMode().equals(Parameters.FOCUS_MODE_AUTO)){
                mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
                mCameraDevice.setParameters(mParameters);
            }
            mUI.onSingleTapUp(view,x, y);
        }
    }

    public void takeASnapshot() {
        // Only take snapshots if video snapshot is supported by device
        if (CameraUtil.isVideoSnapshotSupported(mParameters) && !mIsVideoCaptureIntent) {
            if (!mMediaRecorderRecording || mPaused || mSnapshotInProgress) {
                return;
            }
            MediaSaveService s = mActivity.getMediaSaveService();
            if (s == null || s.isQueueFull()) {
                return;
            }

            // Set rotation and gps data.
            int rotation = CameraUtil.getJpegRotation(mCameraId, mOrientation);
            mParameters.setRotation(rotation);
            Location loc = mLocationManager.getCurrentLocation();
            CameraUtil.setGpsParameters(mParameters, loc);
            // reset scene mode to "auto" for PR1023373
            mParameters.setSceneMode(Parameters.SCENE_MODE_AUTO);
            mCameraDevice.setParameters(mParameters);

            Log.v(TAG, "Video snapshot start");
            mCameraDevice.takePicture(mHandler,
                    null, null, null, new JpegPictureCallback(loc));
            showVideoSnapshotUI(true);
            mSnapshotInProgress = true;
            UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
                    UsageStatistics.ACTION_CAPTURE_DONE, "VideoSnapshot");
        }
    }

    @Override
    public void onStop() {}

    private void loadCameraPreferences() {
        CameraSettings settings = new CameraSettings(mActivity, mParameters,
                mCameraId, CameraHolder.instance().getCameraInfo());
        // Remove the video quality preference setting when the quality is given in the intent.
        mPreferenceGroup = filterPreferenceScreenByIntent(
                settings.getPreferenceGroup(R.xml.video_preferences));
    }

    private void initializeVideoControl() {
        loadCameraPreferences();
        mUI.initializePopup(mPreferenceGroup);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        // We keep the last known orientation. So if the user first orient
        // the camera then point the camera to floor or sky, we still have
        // the correct orientation.
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return;
        int newOrientation = CameraUtil.roundOrientation(orientation, mOrientation);

        if(mActivity.isReversibleEnabled()){
	        if((mOrientation==180||mOrientation==0)
	        		&&mOrientation!=newOrientation&&!isRecording()){
	        	startPreview();
	        }
        }
        
        
        if (mOrientation != newOrientation) {
            mOrientation = newOrientation;
            Log.v(TAG, "onOrientationChanged, update parameters");
            if ((mParameters != null) && (true == mPreviewing) && !mMediaRecorderRecording){
                setCameraParameters();
            }
        }

        // Show the toast after getting the first orientation changed.
        if (mHandler.hasMessages(SHOW_TAP_TO_SNAPSHOT_TOAST)) {
            mHandler.removeMessages(SHOW_TAP_TO_SNAPSHOT_TOAST);
            //showTapToSnapshotToast();
        }
    }

    private void startPlayVideoActivity() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(mCurrentVideoUri, convertOutputFormatToMimeType(mProfile.fileFormat));
        try {
            mActivity
                    .startActivityForResult(intent, CameraActivity.REQ_CODE_DONT_SWITCH_TO_PREVIEW);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Couldn't view video " + mCurrentVideoUri, ex);
        }
    }

    @Override
    @OnClickAttr
    public void onReviewPlayClicked(View v) {
        startPlayVideoActivity();
    }

    @Override
    @OnClickAttr
    public void onReviewDoneClicked(View v) {
        mIsInReviewMode = false;
        doReturnToCaller(true);
    }

    @Override
    @OnClickAttr
    public void onReviewCancelClicked(View v) {
        // TODO: It should be better to not even insert the URI at all before we
        // confirm done in review, which means we need to handle temporary video
        // files in a quite different way than we currently had.
        // Make sure we don't delete the Uri sent from the video capture intent.
        if (mCurrentVideoUriFromMediaSaved) {
            mContentResolver.delete(mCurrentVideoUri, null, null);
        }
        mIsInReviewMode = false;
        doReturnToCaller(false);
    }

    @Override
    @OnClickAttr
    public void onReviewRetakeClicked(View v) {
        if (mPaused)
            return;
        mUI.hideReviewUI();
        //mShutterManager.show();
        mActivity.setViewState(GC.VIEW_STATE_NORMAL);
        mShutterManager.setShutterPauseListener(mPauseListener);
        startPreview();
    }

    @Override
    public boolean isInReviewMode() {
        return mIsInReviewMode;
    }

    public void onStopVideoRecording() {
        //[BUGFIX]MOD by jian.zhang1 for PR753112 2014.08.20 Begin
        stopVideoRecording();
//        boolean recordFail = stopVideoRecording();
//        if (mIsVideoCaptureIntent) {
//            if (mQuickCapture) {
//                doReturnToCaller(!recordFail);
//            } else if (!recordFail) {
//                showCaptureResult();
//            }
//        } else if (!recordFail){
//            // Start capture animation.
//            if (!mPaused && ApiHelper.HAS_SURFACE_TEXTURE_RECORDING) {
//                // The capture animation is disabled on ICS because we use SurfaceView
//                // for preview during recording. When the recording is done, we switch
//                // back to use SurfaceTexture for preview and we need to stop then start
//                // the preview. This will cause the preview flicker since the preview
//                // will not be continuous for a short period of time.
//
//                //mUI.animateFlash();
//                //mUI.animateCapture();
//            }
//        }
        //[BUGFIX]MOD by jian.zhang1 for PR753112 2014.08.20 End
    }

    public void onVideoSaved() {
        if (mIsVideoCaptureIntent) {
            showCaptureResult();
        }
    }

    public void onProtectiveCurtainClick(View v) {
        // Consume clicks
    }

    public boolean isPreviewReady() {
        if ((mStartPrevPending == true || mStopPrevPending == true))
            return false;
        else
            return true;
    }

    public boolean isRecorderReady() {
        if ((mStartRecPending == true || mStopRecPending == true))
            return false;
        else
            return true;
    }

    @Override
    public void onShutterButtonClick() {
        Log.i(TAG, "###YUHUAN###onShutterButtonClick into ");
        if (mPaused || mUI.collapseCameraControls() ||
                mSwitchingCamera) return;
        boolean stop = mMediaRecorderRecording;

        if (isPreviewReady() == false)
            return;

        if (isRecorderReady() == false)
            return;

        if (stop) {
//            onStopVideoRecording();
            if(mIsVideoCaptureIntent){
                onStopVideoRecording();
            }else{
                mShutterManager.mShutterCapture.performClick();
            }
        } else {
            startVideoRecording();
        }
        mShutterManager.enableShutter(false);
        // Keep the shutter button disabled when in video capture intent
        // mode and recording is stopped. It'll be re-enabled when
        // re-take button is clicked.
        if (!(mIsVideoCaptureIntent && stop)) {
            mHandler.sendEmptyMessageDelayed(
                    ENABLE_SHUTTER_BUTTON, SHUTTER_BUTTON_TIMEOUT);
        }
    }

    @Override
    public void onShutterButtonFocus(boolean pressed) {
        mUI.setShutterPressed(pressed);
    }

    @Override
    public void onShutterButtonLongClick() {}

    private void qcomReadVideoPreferences() {
    	Log.d(TAG,"###YUHUAN###qcomReadVideoPreferences into ");
        String videoEncoder = mPreferences.getString(
               CameraSettings.KEY_VIDEO_ENCODER,
               mActivity.getString(R.string.pref_camera_videoencoder_default));
        mVideoEncoder = VIDEO_ENCODER_TABLE.get(videoEncoder);
        Log.d(TAG,"###YUHUAN###qcomReadVideoPreferences#mVideoEncoder=" + mVideoEncoder);

        String audioEncoder = mPreferences.getString(
               CameraSettings.KEY_AUDIO_ENCODER,
               mActivity.getString(R.string.pref_camera_audioencoder_default));
        mAudioEncoder = AUDIO_ENCODER_TABLE.get(audioEncoder);
        Log.d(TAG,"###YUHUAN###qcomReadVideoPreferences#mAudioEncoder=" + mAudioEncoder);

        String minutesStr = mPreferences.getString(
              CameraSettings.KEY_VIDEO_DURATION,
              mActivity.getString(R.string.pref_camera_video_duration_default));
        Log.d(TAG,"###YUHUAN###qcomReadVideoPreferences#minutesStr=" + minutesStr);
        int minutes = -1;
        try {
            minutes = Integer.parseInt(minutesStr);
        } catch(NumberFormatException npe) {
            // use default value continue
            minutes = Integer.parseInt(mActivity.getString(
                         R.string.pref_camera_video_duration_default));
        }
        
        Log.d(TAG,"###YUHUAN###qcomReadVideoPreferences#minutes=" + minutes);
        if (minutes == -1) {
            // User wants lowest, set 30s */
            mMaxVideoDurationInMs = 30000;
        } else {
            // 1 minute = 60000ms
            mMaxVideoDurationInMs = 60000 * minutes;
        }

        /*if(mParameters.isPowerModeSupported()) {
            String powermode = mPreferences.getString(
                    CameraSettings.KEY_POWER_MODE,
                    mActivity.getString(R.string.pref_camera_powermode_default));
            Log.v(TAG, "read videopreferences power mode =" +powermode);
            String old_mode = mParameters.getPowerMode();
            if(!old_mode.equals(powermode) && mPreviewing)
                mRestartPreview = true;

            mParameters.setPowerMode(powermode);
        }*/
   }

    //pr1005366-yuguan.chen modified for video quality perso
    private void readVideoPreferences() {
        // The preference stores values from ListPreference and is thus string type for all values.
        // We need to convert it to int manually.
//        String defaultQuality =  mCameraId == GC.CAMERAID_BACK ? mActivity.getResources().getString(R.string.pref_video_quality_default) :
//                mActivity.getResources().getString(R.string.pref_video_front_quality_default);
//        String videoQuality = null;
		// feiqiang.cheng validate logic
//		CamcorderProfile profile = CamcorderProfileEx.getProfile(mCameraId,
//				Integer.valueOf(defaultQuality));
//		boolean bFound = false;
//		if(profile!=null){
//			for (Size s : mCameraDevice.getParameters().getSupportedVideoSizes()) {
//				if (s.width == profile.videoFrameWidth
//						&& s.height == profile.videoFrameHeight) {
//					bFound = true;
//					break;
//				}
//			}
//		}
//		if (!bFound) {
//			if (mCameraId == GC.CAMERAID_FRONT)
//				defaultQuality = "QUALITY_VGA";//pr1000285,1000388 yuguan.chen modified.
//			else
//				defaultQuality = "QUALITY_720P";//pr1000285,1000388 yuguan.chen modified.
//		}
    	String defaultQuality = "";
    	String videoQuality = "";
		GC mGC = mActivity.getGC();
		if (mCameraId == GC.CAMERAID_FRONT) {
			ListPreference vFrontQuality = mGC.getListPreference(CameraSettings.KEY_VIDEO_FRONT_QUALITY);
			if (vFrontQuality != null) {
				defaultQuality = vFrontQuality.getDefaultValue();
				Log.d(TAG,"###YUHUAN###readVideoPreferences#front defaultQuality=" + defaultQuality);
			}
			videoQuality = mPreferences.getString(CameraSettings.KEY_VIDEO_FRONT_QUALITY, defaultQuality);
		} else {
			ListPreference vBackQuality = mGC.getListPreference(CameraSettings.KEY_VIDEO_QUALITY);
			if (vBackQuality != null) {
				defaultQuality = vBackQuality.getDefaultValue();
				Log.d(TAG,"###YUHUAN###readVideoPreferences#back defaultQuality=" + defaultQuality);
			}
			videoQuality = mPreferences.getString(CameraSettings.KEY_VIDEO_QUALITY, defaultQuality);
		}
		Log.d(TAG,"###YUHUAN###readVideoPreferences#videoQuality=" + videoQuality);
		// feiqiang.cheng validate logic
//        if (mCameraId == GC.CAMERAID_BACK){
//            videoQuality = mPreferences.getString(CameraSettings.KEY_VIDEO_QUALITY,
//                        defaultQuality);
//            Log.i(TAG, "doom videoQuality back = " + videoQuality);
//        }else{
//            videoQuality = mPreferences.getString(CameraSettings.KEY_VIDEO_FRONT_QUALITY,
//                        defaultQuality);
//            Log.i(TAG, "doom videoQuality front = " + videoQuality);
//        }
//        Log.i(TAG, "doom videoQuality  = " + videoQuality);
//        if (videoQuality == null) {
//             mParameters = mCameraDevice.getParameters();
//            boolean hasProfile = CamcorderProfile.hasProfile(
//                     Integer.parseInt(defaultQuality));
//            if (hasProfile == true){
//                videoQuality = defaultQuality;
//            } else {
//                // check for highest quality if default quality is not supported
//                videoQuality = CameraSettings.getSupportedHighestVideoQuality(mCameraId,
//                        defaultQuality, mParameters);
//            }
//            if (mCameraId == GC.CAMERAID_BACK){
//                mPreferences.edit().putString(CameraSettings.KEY_VIDEO_QUALITY, videoQuality);
//            }else{
//                 mPreferences.edit().putString(CameraSettings.KEY_VIDEO_FRONT_QUALITY, videoQuality);
//            }
//        }
//        int quality = Integer.valueOf(videoQuality);
        if (isLimitForMMSMode()) {
//            quality = CamcorderProfile.QUALITY_LOW;
        	videoQuality = "QUALITY_LOW";
        }
        // Set video quality.
        Intent intent = mActivity.getIntent();
        if (intent.hasExtra(MediaStore.EXTRA_VIDEO_QUALITY)) {
            int extraVideoQuality = intent.getIntExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            Log.d(TAG,"###YUHUAN###readVideoPreferences#extraVideoQuality=" + extraVideoQuality);
            if (extraVideoQuality > 0) {
//                quality = CamcorderProfile.QUALITY_HIGH;
            	videoQuality = "QUALITY_HIGH";
            } else {  // 0 is mms.
//                quality = CamcorderProfile.QUALITY_LOW;
            	videoQuality = "QUALITY_LOW";
            }
        }

        // Set video duration limit. The limit is read from the preference,
        // unless it is specified in the intent.
        if (intent.hasExtra(MediaStore.EXTRA_DURATION_LIMIT)) {
            int seconds =
                    intent.getIntExtra(MediaStore.EXTRA_DURATION_LIMIT, 0);
            mMaxVideoDurationInMs = 1000 * seconds;
        } else {
            mMaxVideoDurationInMs = CameraSettings.getMaxVideoDuration(mActivity);
        }

        // Read time lapse recording interval.
        String frameIntervalStr = mPreferences.getString(
                CameraSettings.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL,
                mActivity.getString(R.string.pref_video_time_lapse_frame_interval_default));
        mTimeBetweenTimeLapseFrameCaptureMs = Integer.parseInt(frameIntervalStr);
        Log.d(TAG,"###YUHUAN###readVideoPreferences#mTimeBetweenTimeLapseFrameCaptureMs=" + mTimeBetweenTimeLapseFrameCaptureMs);
        
        mCaptureTimeLapse = (mTimeBetweenTimeLapseFrameCaptureMs != 0);
        // TODO: This should be checked instead directly +1000.
//        if (mCaptureTimeLapse) quality += 1000;
        //quality = CamcorderProfile.QUALITY_1080P;
        Log.d(TAG,"###YUHUAN###readVideoPreferences#mCaptureTimeLapse=" + mCaptureTimeLapse);
        if (mCaptureTimeLapse) {
        	String[] compos = videoQuality.split("_");
        	videoQuality = compos[0] + "_TIME_LAPSE_" + compos[1];
        }
        mProfile = CamcorderProfileEx.getProfile(mCameraId, CamcorderProfileEx.getQualityNum(videoQuality));
		Log.d(TAG, "###readVideoPreferences#mProfile=" + mProfile + " videoQuality=" + videoQuality);
        getDesiredPreviewSize();
        qcomReadVideoPreferences();
        mPreferenceRead = true;
    }


    private int getMaxVideoSizeForMms() {
        Context context = mActivity.getWindow().getContext();
        int maxMessageSize;
//        if (mActivity.getResources().getBoolean(R.bool.def_camera_tf_enable)) {
//            maxMessageSize = Settings.System.getInt(context.getContentResolver(), "config_mms_max_size", 1024 * 1024);
//        } else {
            maxMessageSize = Settings.System.getInt(context.getContentResolver(), "config_mms_max_size", 300 * 1024);
//        }

        // Computer attachment size limit. Subtract 1K for some text.
        int sizeLimit = maxMessageSize - 1024;

        // The video recorder can sometimes return a file that's larger than the max we
        // say we can handle. Try to handle that overshoot by specifying an 85% limit.
        sizeLimit *= .85F;

        return sizeLimit;
    }

    private boolean isLimitForMMSMode() {
        boolean isLimit = mPreferences.getString(CameraSettings.KEY_VIDEO_LIMIT_FOR_MMS_KEY,
            mActivity.getString(R.string.pref_video_limit_for_mms_default))
            .equals(mActivity.getString(R.string.setting_on_value));
        Log.i(TAG,"###YUHUAN###isLimitForMMSMode#isLimit=" + isLimit);
        return isLimit;
    }

    private boolean is4KEnabled() {
//       if (mProfile.quality == CamcorderProfile.QUALITY_4kUHD ||
//           mProfile.quality == CamcorderProfile.QUALITY_4kDCI) {
//           return true;
//       } else {
           return false;
//       }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void getDesiredPreviewSize() {
        if (mCameraDevice == null) {
            return;
        }
        mParameters = mCameraDevice.getParameters();
        if (mParameters.getSupportedVideoSizes() == null || is4KEnabled()) {
            mDesiredPreviewWidth = mProfile.videoFrameWidth;
            Log.d(TAG,"###YUHUAN###getDesiredPreviewSize#mDesiredPreviewWidth1=" + mDesiredPreviewWidth);
            mDesiredPreviewHeight = mProfile.videoFrameHeight;
            Log.d(TAG,"###YUHUAN###getDesiredPreviewSize#mDesiredPreviewWidth1=" + mDesiredPreviewWidth);
        } else { // Driver supports separates outputs for preview and video.
            List<Size> sizes = mParameters.getSupportedPreviewSizes();
            Size preferred = mParameters.getPreferredPreviewSizeForVideo();
            int product = preferred.width * preferred.height;
            Iterator<Size> it = sizes.iterator();
            // Remove the preview sizes that are not preferred.
            while (it.hasNext()) {
                Size size = it.next();
                if (size.width * size.height > product) {
                    it.remove();
                }
            }
            Size optimalSize = CameraUtil.getOptimalPreviewSize(mActivity, sizes,
                    (double) mProfile.videoFrameWidth / mProfile.videoFrameHeight);
            mDesiredPreviewWidth = optimalSize.width;
            mDesiredPreviewHeight = optimalSize.height;
        }
        
        Log.d(TAG,"###YUHUAN###getDesiredPreviewSize#mDesiredPreviewWidth=" + mDesiredPreviewWidth);
        Log.d(TAG,"###YUHUAN###getDesiredPreviewSize#mDesiredPreviewHeight=" + mDesiredPreviewHeight);
        
        mUI.setPreviewSize(mDesiredPreviewWidth, mDesiredPreviewHeight);
    }

    void setPreviewFrameLayoutCameraOrientation(){
        CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];

        //if camera mount angle is 0 or 180, we want to resize preview
        if (info.orientation % 180 == 0)
            mUI.cameraOrientationPreviewResize(true);
        else
            mUI.cameraOrientationPreviewResize(false);
    }

    @Override
    public void resizeForPreviewAspectRatio() {
        setPreviewFrameLayoutCameraOrientation();
        mUI.setAspectRatio(
                (double) mProfile.videoFrameWidth / mProfile.videoFrameHeight);
    }

    @Override
    public void installIntentFilter() {
        // install an intent filter to receive SD card related events.
        IntentFilter intentFilter =
                new IntentFilter(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addDataScheme("file");
        mReceiver = new MyBroadcastReceiver();
        mActivity.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onResumeBeforeSuper() {
        mPaused = false;
        installIntentFilter();
    }

    @Override
    public void onResumeAfterSuper() {
        mShutterManager.enableShutter(false);
        mZoomValue = 0;
        //[BUGFIX]-Add by TCTNB,xianbin.tang, 2014-08-27,PR766200 Begin
        mUI.hidePreviewCover();
        //[BUGFIX]-Add by TCTNB,xianbin.tang, 2014-08-27,PR766200 End
        showVideoSnapshotUI(false);

        if (!mPreviewing) {
            openCamera();
            if (mCameraDevice == null) {
                return;
            }
            readVideoPreferences();
            resizeForPreviewAspectRatio();
            startPreview();
        } else {
            // preview already started
            mShutterManager.enableShutter(true);
        }

        mUI.initDisplayChangeListener();
        mZoomManager.setOnZoomIndexChangedListener(this);
        // Initializing it here after the preview is started.
        mUI.initializeZoom(mParameters);

        keepScreenOnAwhile();

//        mOrientationManager.resume();
        // Initialize location service.
        boolean recordLocation = RecordLocationPreference.get(mPreferences,
                mContentResolver);
        mLocationManager.recordLocation(recordLocation);

        if (mPreviewing) {
            mOnResumeTime = SystemClock.uptimeMillis();
            mHandler.sendEmptyMessageDelayed(CHECK_DISPLAY_ROTATION, 100);
        }

        UsageStatistics.onContentViewChanged(
                UsageStatistics.COMPONENT_CAMERA, "VideoModule");
        mHandler.post(new Runnable(){
            @Override
            public void run(){
                mActivity.updateStorageSpaceAndHint();
            }
        });
    }

    private void setDisplayOrientation() {
        mDisplayRotation = CameraUtil.getDisplayRotation(mActivity);
        mCameraDisplayOrientation = CameraUtil.getDisplayOrientation(mDisplayRotation, mCameraId);
        // Change the camera display orientation
        if (mCameraDevice != null) {
            int orientation=mCameraDisplayOrientation;
        	if(mActivity.isReversibleEnabled()){
        		if(mOrientation==180){
        			orientation=270;
        		}else if(mOrientation==0){
        			orientation=90;
        		}
        	}
            mCameraDevice.setDisplayOrientation(orientation);
        }
    }

    @Override
    public void updateCameraOrientation() {
        if (mMediaRecorderRecording) return;
        if (mDisplayRotation != CameraUtil.getDisplayRotation(mActivity)) {
            setDisplayOrientation();
        }
    }

    @Override
    public int onZoomChanged(int index) {
        // Not useful to change zoom value when the activity is paused.
        if (mPaused) return index;
        mZoomValue = index;
        if (mParameters == null || mCameraDevice == null) return index;
        // Set zoom parameters asynchronously
        mParameters.setZoom(mZoomValue);
        mCameraDevice.setParameters(mParameters);
        Parameters p = mCameraDevice.getParameters();
        if (p != null) return p.getZoom();
        return index;
    }
    @Override
    public void onZoomIndexChanged(int index) {
        int ret = onZoomChanged(index);
    }

    private void startPreview() {
        Log.v(TAG, "###YUHUAN###startPreview into ");
        mStartPrevPending = true;

        SurfaceTexture surfaceTexture = mUI.getSurfaceTexture();
        Log.d(TAG, "###YUHUAN###startPreview#mPreferenceRead=" + mPreferenceRead);
        Log.d(TAG, "###YUHUAN###startPreview#surfaceTexture=" + surfaceTexture);
        Log.d(TAG, "###YUHUAN###startPreview#mPaused=" + mPaused);
        Log.d(TAG, "###YUHUAN###startPreview#mCameraDevice=" + mCameraDevice);
        if (!mPreferenceRead || surfaceTexture == null || mPaused == true ||
                mCameraDevice == null) {
            mStartPrevPending = false;
            return;
        }

        mCameraDevice.setErrorCallback(mErrorCallback);
        
        Log.d(TAG, "###YUHUAN###startPreview#mPreviewing=" + mPreviewing);
        if (mPreviewing) {
            stopPreview();
        }

        setDisplayOrientation();
        mCameraDevice.setDisplayOrientation(mCameraDisplayOrientation);
        //Add by zhimin.yu for PR952662 begin
        // Set continuous autofocus.
        List<String> supportedFocus = mParameters.getSupportedFocusModes();
        if (isSupported(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO, supportedFocus)) {
             mParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
             mCameraDevice.getCamera().setParameters(mParameters);
        }
        //Add by zhimin.yu for PR952662 end
        setCameraParameters();

        try {
            mCameraDevice.setPreviewTexture(surfaceTexture);
            mCameraDevice.startPreview();
            mPreviewing = true;
            onPreviewStarted();
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("startPreview failed", ex);
        }
        mStartPrevPending = false;
    }

    private void onPreviewStarted() {
    	Log.d(TAG,"###YUHUAN###onPreviewStarted ");
        mShutterManager.enableShutter(true);
        if(mActivity.isNonePickIntent()){
	        mHandler.post(new Runnable(){
	
				@Override
				public void run() {
					// TODO Auto-generated method stub
					VideoModule.this.onShutterButtonClick();
				}
	
	        });
        }
    }

    @Override
    public void stopPreview() {
        mStopPrevPending = true;

        Log.d(TAG,"###YUHUAN###stopPreview#mPreviewing=" + mPreviewing);
        if (!mPreviewing) {
            mStopPrevPending = false;
            return;
        }
        mCameraDevice.stopPreview();
        mPreviewing = false;
        mStopPrevPending = false;
    }

    private void closeCamera() {
        CameraActivity.TraceLog(TAG, "closeCamera");
        if (mCameraDevice == null) {
            CameraActivity.TraceLog(TAG, "closeCamera() camera already stopped!");
            return;
        }
        mCameraDevice.setZoomChangeListener(null);
        mCameraDevice.setErrorCallback(null);
        CameraHolder.instance().release();
        mCameraDevice = null;
        mPreviewing = false;
        mSnapshotInProgress = false;
        mPreviewFocused = false;
        if (!mIsVideoCaptureIntent) {
            // modify by minghui.hua for PR992517
            CameraActivity.TraceLog(TAG, "closeCamera() normal video,should change to photo!");
            mActivity.switchPhotoVideo(GC.MODE_PHOTO);
        }
    }

    private void releasePreviewResources() {
        if (!ApiHelper.HAS_SURFACE_TEXTURE_RECORDING) {
            mUI.hideSurfaceView();
        }
    }

    @Override
    public void onPauseBeforeSuper() {
        CameraActivity.TraceLog(TAG, "onPauseBeforeSuper");
        mPaused = true;

        mUI.showPreviewCover();
        mActivity.abandonAudioFocus();
        if (mMediaRecorderRecording) {
            // Camera will be released in onStopVideoRecording.
            onStopVideoRecording();
        } else {
            closeCamera();
            releaseMediaRecorder();
        }

        //[BUGFIX]Added by jian.zhang1 for PR753112 2014.08.20 Begin
        if(!isSaving){
            closeVideoFileDescriptor();
         }
        //[BUGFIX]Added by jian.zhang1 for PR753112 2014.08.20 Begin
//        closeVideoFileDescriptor();
        releasePreviewResources();

        if (mReceiver != null) {
            mActivity.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        resetScreenOn();

        if (mLocationManager != null) mLocationManager.recordLocation(false);
//        mOrientationManager.pause();

        mHandler.removeMessages(CHECK_DISPLAY_ROTATION);
        mHandler.removeMessages(SWITCH_CAMERA);
        mHandler.removeMessages(SWITCH_CAMERA_START_ANIMATION);
        mPendingSwitchCameraId = -1;
        mSwitchingCamera = false;
        mPreferenceRead = false;

        mUI.collapseCameraControls();
        mUI.removeDisplayChangeListener();
        mZoomManager.setOnZoomIndexChangedListener(null);
    }

    @Override
    public void onPauseAfterSuper() {
    }

    @Override
    public void onUserInteraction() {
        if (!mMediaRecorderRecording && !mActivity.isFinishing()) {
            keepScreenOnAwhile();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mPaused) return true;
        if (mMediaRecorderRecording) {
            onStopVideoRecording();
            mActivity.switchPhotoVideo(GC.MODE_PHOTO);
            return true;
        } else if (mUI.hidePieRenderer()) {
            return true;
        } else {
            return mUI.removeTopLevelPopup();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Do not handle any key if the activity is paused.
        if (mPaused) {
            return true;
        }
        if( mActivity.getCameraViewManager().onKeyDown(keyCode,event)) {
            return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_CAMERA:
                if (event.getRepeatCount() == 0) {
                    mUI.clickShutter();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (event.getRepeatCount() == 0) {
                    mUI.clickShutter();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                if (mMediaRecorderRecording) return true;
                break;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_CAMERA:
                mUI.pressShutter(false);
                return true;
        }
        return false;
    }

    @Override
    public boolean isVideoCaptureIntent() {
        String action = mActivity.getIntent().getAction();
        return (MediaStore.ACTION_VIDEO_CAPTURE.equals(action));
    }

  //PR900096,v-nj-feiqiang.cheng add begin
    private void saveVideoWhenNotNormal(){
    	generateVideoFilename(mProfile.fileFormat);
    	InputStream inputStream=null;
		try {
			inputStream = mContentResolver.openInputStream(mCurrentVideoUri);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	//FileInputStream inputStream=new FileInputStream(mVideoFileDescriptor.getFileDescriptor());
		if (inputStream != null) {
			mCurrentVideoFilename = mVideoFilename;
			File dest = new File(mCurrentVideoFilename);
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(dest);
				byte[] buffer = new byte[2048];
				while (inputStream.read(buffer) != -1) {
					fos.write(buffer);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		saveVideoWithoutNotify();
    }
    
    private void saveVideoWithoutNotify(){
            long duration = SystemClock.uptimeMillis() - mRecordingStartTime;

            // [BUGFIX]-Add by TCTNB,haibo.chen, 2014-10-24,PR812550 Begin
            // Duration will be obtained first from decoder information to
            // avoid any errors about calculation.
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(mCurrentVideoFilename);
                mediaPlayer.prepare();
                duration = mediaPlayer.getDuration();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            Log.d(TAG, "saveVideo duration :" + duration);
            // [BUGFIX]-Add by TCTNB,haibo.chen, 2014-10-24,PR812550 End

            if (duration > 0) {
                if (mCaptureTimeLapse) {
                    duration = getTimeLapseVideoLength(duration);
                }
            } else {
                Log.w(TAG, "Video duration <= 0 : " + duration);
            }

            File origFile = new File(mCurrentVideoFilename);
            if (!origFile.exists() || origFile.length() <= 0) {
                Log.e(TAG, "Invalid file");
                mCurrentVideoValues = null;
                return;
            }

            /* Change the duration as per HFR selection */
            //String hfr = mParameters.getVideoHighFrameRate();
            String hfr = null;
            int defaultFps = 30;
            int hfrRatio = 1;
            if (!("off".equals(hfr))) {
                try
                {
                   int hfrFps = Integer.parseInt(hfr);
                   hfrRatio = hfrFps / defaultFps;
                }
                catch(Exception ex)
                {
                    //Do Nothing
                }
            }
            duration = duration * hfrRatio;

            mActivity.getMediaSaveService().addVideo(mCurrentVideoFilename,
                    duration, mCurrentVideoValues,
                    null, mContentResolver);
    }
    //PR900096,v-nj-feiqiang.cheng add end
    private void doReturnToCaller(boolean valid) {
        Intent resultIntent = new Intent();
        int resultCode;
        if (valid) {
        	//PR900096,v-nj-feiqiang.cheng add begin
        	if("true".equals(mActivity.getResources().getString(R.string.def_camera_whether_savepic_whennotnormal))){
        		saveVideoWhenNotNormal();
            }
        	//PR900096,v-nj-feiqiang.cheng add end
            resultCode = Activity.RESULT_OK;
            resultIntent.setData(mCurrentVideoUri);
        } else {
            resultCode = Activity.RESULT_CANCELED;
        }
        mActivity.setResultEx(resultCode, resultIntent);
        mActivity.finish();
    }

    private void cleanupEmptyFile() {
        if (mVideoFilename != null) {
            File f = new File(mVideoFilename);
            if (f.length() == 0 && f.delete()) {
                Log.v(TAG, "Empty video file deleted: " + mVideoFilename);
                mVideoFilename = null;
            }
        }
    }

    private void setupMediaRecorderPreviewDisplay() {
        // Nothing to do here if using SurfaceTexture.
        if (!ApiHelper.HAS_SURFACE_TEXTURE_RECORDING) {
            // We stop the preview here before unlocking the device because we
            // need to change the SurfaceTexture to SurfaceView for preview.
            stopPreview();
            mCameraDevice.setPreviewDisplay(mUI.getSurfaceHolder());
            // The orientation for SurfaceTexture is different from that for
            // SurfaceView. For SurfaceTexture we don't need to consider the
            // display rotation. Just consider the sensor's orientation and we
            // will set the orientation correctly when showing the texture.
            // Gallery will handle the orientation for the preview. For
            // SurfaceView we will have to take everything into account so the
            // display rotation is considered.
            mCameraDevice.setDisplayOrientation(
                    CameraUtil.getDisplayOrientation(mDisplayRotation, mCameraId));
            mCameraDevice.startPreview();
            mPreviewing = true;
            mMediaRecorder.setPreviewDisplay(mUI.getSurfaceHolder().getSurface());
        }
    }

    // Prepares media recorder.
    private void initializeRecorder() {
        Log.v(TAG, "###YUHUAN###initializeRecorder into ");
        // If the mCameraDevice is null, then this activity is going to finish
        if (mCameraDevice == null) return;

        if (!ApiHelper.HAS_SURFACE_TEXTURE_RECORDING) {
            // Set the SurfaceView to visible so the surface gets created.
            // surfaceCreated() is called immediately when the visibility is
            // changed to visible. Thus, mSurfaceViewReady should become true
            // right after calling setVisibility().
            mUI.showSurfaceView();
        }

        Intent intent = mActivity.getIntent();
        Bundle myExtras = intent.getExtras();

        videoWidth = mProfile.videoFrameWidth;
        Log.d(TAG, "###YUHUAN###initializeRecorder#videoWidth= " + videoWidth);
        videoHeight = mProfile.videoFrameHeight;
        Log.d(TAG, "###YUHUAN###initializeRecorder#videoHeight= " + videoHeight);
        mUnsupportedResolution = false;

        //check if codec supports the resolution, otherwise throw toast
//        List<VideoEncoderCap> videoEncoders = EncoderCapabilities.getVideoEncoders();
//        for (VideoEncoderCap videoEncoder: videoEncoders) {
//            if (videoEncoder.mCodec == mVideoEncoder){
//                if (videoWidth > videoEncoder.mMaxFrameWidth ||
//                    videoWidth < videoEncoder.mMinFrameWidth ||
//                    videoHeight > videoEncoder.mMaxFrameHeight ||
//                    videoHeight < videoEncoder.mMinFrameHeight){
//                        Log.e(TAG,"Selected codec "+mVideoEncoder+
//                          " does not support "+ videoWidth + "x" + videoHeight
//                          +" resolution");
//                        Log.e(TAG, "Codec capabilities: " +
//                          "mMinFrameWidth = " + videoEncoder.mMinFrameWidth + " , "+
//                          "mMinFrameHeight = " + videoEncoder.mMinFrameHeight + " , "+
//                          "mMaxFrameWidth = " + videoEncoder.mMaxFrameWidth + " , "+
//                          "mMaxFrameHeight = " + videoEncoder.mMaxFrameHeight);
//                        mUnsupportedResolution = true;
//                        Toast.makeText(mActivity, R.string.error_app_unsupported,
//                          Toast.LENGTH_LONG).show();
//                        return;
//                }
//                break;
//            }
//        }

        long requestedSizeLimit = 0;
        closeVideoFileDescriptor();
        mCurrentVideoUriFromMediaSaved = false;
        if (mIsVideoCaptureIntent && myExtras != null) {
            Uri saveUri = (Uri) myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
            if (saveUri != null) {
                try {
                    mVideoFileDescriptor =
                            mContentResolver.openFileDescriptor(saveUri, "rw");
                    mCurrentVideoUri = saveUri;
                } catch (java.io.FileNotFoundException ex) {
                    // invalid uri
                    Log.e(TAG, ex.toString());
                }
            }
            requestedSizeLimit = myExtras.getLong(MediaStore.EXTRA_SIZE_LIMIT);
        } else if(isLimitForMMSMode()) {
            Log.i(TAG,"LMCH03071 isLimitForMMSMode");
            requestedSizeLimit = getMaxVideoSizeForMms();
        }
        mMediaRecorder = new MediaRecorder();

        setupMediaRecorderPreviewDisplay();
        // Unlock the camera object before passing it to media recorder.
        mCameraDevice.unlock();
        mMediaRecorder.setCamera(mCameraDevice.getCamera());
        //String hfr = mParameters.getVideoHighFrameRate();
        //String hfr = null;
        
        Log.d(TAG, "###YUHUAN###initializeRecorder#mCaptureTimeLapse= " + mCaptureTimeLapse);
        Log.d(TAG, "###YUHUAN###initializeRecorder#mRecordingSupported= " + mRecordingSupported);
        if (!mCaptureTimeLapse && mRecordingSupported) {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            //mProfile.audioCodec = mAudioEncoder;
        } 
        
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        //mProfile.videoCodec = mVideoEncoder;
        //mProfile.duration = mMaxVideoDurationInMs;

        //mMediaRecorder.setProfile(mProfile);
        
        mMediaRecorder.setOutputFormat(mProfile.fileFormat);
        mMediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);
        mMediaRecorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
        mMediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
        //mMediaRecorder.setVideoEncoder(mProfile.videoCodec);
        mMediaRecorder.setVideoEncoder(mVideoEncoder);
        
        if (!mCaptureTimeLapse && mRecordingSupported) {
            mMediaRecorder.setAudioEncodingBitRate(mProfile.audioBitRate);
            mMediaRecorder.setAudioChannels(mProfile.audioChannels);
            mMediaRecorder.setAudioSamplingRate(mProfile.audioSampleRate);
            mMediaRecorder.setAudioEncoder(mAudioEncoder);
        } 
        
        mMediaRecorder.setMaxDuration(mMaxVideoDurationInMs);
        if (mCaptureTimeLapse) {
            double fps = 1000 / (double) mTimeBetweenTimeLapseFrameCaptureMs;
            setCaptureRate(mMediaRecorder, fps);
        }

        setRecordLocation();
        
        // Set maximum file size.
        mMaxVideoSize = mActivity.getStorageSpaceBytes() - Storage.LOW_STORAGE_THRESHOLD_BYTES;
        if (requestedSizeLimit > 0 && requestedSizeLimit < mMaxVideoSize) {
            mMaxVideoSize = requestedSizeLimit;
        }

        CameraActivity.TraceLog(TAG, "initializeRecorder mMaxVideoSize="+mMaxVideoSize);
        try {
            mMediaRecorder.setMaxFileSize(mMaxVideoSize);
        } catch (RuntimeException exception) {
            // We are going to ignore failure of setMaxFileSize here, as
            // a) The composer selected may simply not support it, or
            // b) The underlying media framework may not handle 64-bit range
            // on the size restriction.
        }

        // Set output file.
        // Try Uri in the intent first. If it doesn't exist, use our own
        // instead.
        if (mVideoFileDescriptor != null) {
            mMediaRecorder.setOutputFile(mVideoFileDescriptor.getFileDescriptor());
        } else {
            generateVideoFilename(mProfile.fileFormat);
            mMediaRecorder.setOutputFile(mVideoFilename);
        }



        // See android.hardware.Camera.Parameters.setRotation for
        // documentation.
        // Note that mOrientation here is the device orientation, which is the opposite of
        // what activity.getWindowManager().getDefaultDisplay().getRotation() would return,
        // which is the orientation the graphics need to rotate in order to render correctly.
        int rotation = 0;
        if (mOrientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            CameraInfo info = CameraHolder.instance().getCameraInfo()[mCameraId];
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - mOrientation + 360) % 360;
            } else {  // back-facing camera
                rotation = (info.orientation + mOrientation) % 360;
            }
        }
        mMediaRecorder.setOrientationHint(rotation);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare failed for " + mVideoFilename, e);
            releaseMediaRecorder();
            throw new RuntimeException(e);
        }

        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setOnInfoListener(this);
    }

    private static void setCaptureRate(MediaRecorder recorder, double fps) {
        recorder.setCaptureRate(fps);
    }

    private void setRecordLocation() {
        Location loc = mLocationManager.getCurrentLocation();
        if (loc != null) {
            mMediaRecorder.setLocation((float) loc.getLatitude(),
                    (float) loc.getLongitude());
        }
    }

    private void releaseMediaRecorder() {
        Log.v(TAG, "Releasing media recorder.");
        if (mMediaRecorder != null) {
            cleanupEmptyFile();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        mVideoFilename = null;
        mMediaRecoderRecordingPaused = false;
    }

    private void generateVideoFilename(int outputFileFormat) {
        long dateTaken = System.currentTimeMillis();
        String title = createName(dateTaken);
        // Used when emailing.
        String filename = title + convertOutputFormatToFileExt(outputFileFormat);
        String mime = convertOutputFormatToMimeType(outputFileFormat);
        String path = null;
        if (Storage.isSaveSDCard() && SDCard.instance().isWriteable()) {
            path = SDCard.instance().getDirectory() + '/' + filename;
        } else {
            path = Storage.DIRECTORY + '/' + filename;
        }
        String tmpPath = path + ".tmp";
        mCurrentVideoValues = new ContentValues(9);
        mCurrentVideoValues.put(Video.Media.TITLE, title);
        mCurrentVideoValues.put(Video.Media.DISPLAY_NAME, filename);
        mCurrentVideoValues.put(Video.Media.DATE_TAKEN, dateTaken);
        mCurrentVideoValues.put(MediaColumns.DATE_MODIFIED, dateTaken / 1000);
        mCurrentVideoValues.put(Video.Media.MIME_TYPE, mime);
        mCurrentVideoValues.put(Video.Media.DATA, path);
        mCurrentVideoValues.put(Video.Media.RESOLUTION,
                Integer.toString(mProfile.videoFrameWidth) + "x" +
                Integer.toString(mProfile.videoFrameHeight));
        Location loc = mLocationManager.getCurrentLocation();
        if (loc != null) {
            mCurrentVideoValues.put(Video.Media.LATITUDE, loc.getLatitude());
            mCurrentVideoValues.put(Video.Media.LONGITUDE, loc.getLongitude());
        }
        mVideoFilename = tmpPath;
        Log.v(TAG, "New video filename: " + mVideoFilename);
    }

    private void saveVideo() {
        if (mVideoFileDescriptor == null) {
            long duration = SystemClock.uptimeMillis() - mRecordingStartTime;

            // [BUGFIX]-Add by TCTNB,haibo.chen, 2014-10-24,PR812550 Begin
            // Duration will be obtained first from decoder information to
            // avoid any errors about calculation.
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(mCurrentVideoFilename);
                mediaPlayer.prepare();
                duration = mediaPlayer.getDuration();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            Log.d(TAG, "saveVideo duration :" + duration);
            // [BUGFIX]-Add by TCTNB,haibo.chen, 2014-10-24,PR812550 End

            if (duration > 0) {
                if (mCaptureTimeLapse) {
                    duration = getTimeLapseVideoLength(duration);
                }
            } else {
                Log.w(TAG, "Video duration <= 0 : " + duration);
            }

            File origFile = new File(mCurrentVideoFilename);
            if (!origFile.exists() || origFile.length() <= 0) {
                Log.e(TAG, "Invalid file");
                mCurrentVideoValues = null;
                return;
            }

            /* Change the duration as per HFR selection */
            //String hfr = mParameters.getVideoHighFrameRate();
            String hfr = null;
            int defaultFps = 30;
            int hfrRatio = 1;
            if (!("off".equals(hfr))) {
                try
                {
                   int hfrFps = Integer.parseInt(hfr);
                   hfrRatio = hfrFps / defaultFps;
                }
                catch(Exception ex)
                {
                    //Do Nothing
                }
            }
            duration = duration * hfrRatio;

            mActivity.getMediaSaveService().addVideo(mCurrentVideoFilename,
                    duration, mCurrentVideoValues,
                    mOnVideoSavedListener, mContentResolver);
        }
        mCurrentVideoValues = null;
    }

    private void deleteVideoFile(String fileName) {
        Log.v(TAG, "Deleting video " + fileName);
        File f = new File(fileName);
        if (!f.delete()) {
            Log.v(TAG, "Could not delete " + fileName);
        }
    }

    private PreferenceGroup filterPreferenceScreenByIntent(
            PreferenceGroup screen) {
        Intent intent = mActivity.getIntent();
        if (intent.hasExtra(MediaStore.EXTRA_VIDEO_QUALITY)) {
            CameraSettings.removePreferenceFromScreen(screen,
                    CameraSettings.KEY_VIDEO_QUALITY);
        }

        if (intent.hasExtra(MediaStore.EXTRA_DURATION_LIMIT)) {
            CameraSettings.removePreferenceFromScreen(screen,
                    CameraSettings.KEY_VIDEO_QUALITY);
        }
        return screen;
    }

    // from MediaRecorder.OnErrorListener
    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        Log.e(TAG, "MediaRecorder error. what=" + what + ". extra=" + extra);
        if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
            // We may have run out of space on the sdcard.
            stopVideoRecording();
            mActivity.updateStorageSpaceAndHint();
            mActivity.switchPhotoVideo(GC.MODE_PHOTO);
        }
    }

    // from MediaRecorder.OnInfoListener
    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            if (mMediaRecorderRecording){
               onStopVideoRecording();
               mActivity.switchPhotoVideo(GC.MODE_PHOTO);
            }
        } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
            if (mVideoFileDescriptor!=null) {
                mUI.setRecordingProgress(100);
            }
            mHandler.sendEmptyMessageDelayed(RECORD_END_MSG,100);

//            if (mMediaRecorderRecording) onStopVideoRecording();
//
//            // Show the toast.
//            Toast.makeText(mActivity, R.string.video_reach_size_limit,
//                    Toast.LENGTH_LONG).show();
//            if(!mIsVideoCaptureIntent){
//                mActivity.switchPhotoVideo(GC.MODE_PHOTO);
//            }
        }
    }

    /*
     * Make sure we're not recording music playing in the background, ask the
     * MediaPlaybackService to pause playback.
     */
    private void pauseAudioPlayback() {
        // Shamelessly copied from MediaPlaybackService.java, which
        // should be public, but isn't.
//        Intent i = new Intent("com.android.music.musicservicecommand");
//        i.putExtra("command", "pause");
//
//        mActivity.sendBroadcast(i);
        
        mActivity.gainAudioFocus();
    }
    
    

    // For testing.
    public boolean isRecording() {
        return mMediaRecorderRecording;
    }

    public void startVideoRecording() {
        Log.v(TAG, "###YUHUAN###startVideoRecording into ");
        if(mPaused){
            return;
        }
        mActivity.getGC().flagVideoRecording(true);
        forceFlashOffIfSupported(false);
        mCameraDevice.setParameters(mParameters);
        mActivity.getCameraViewManager().refresh();
        mStartRecPending = true;
        mUI.cancelAnimations();
        mUI.setSwipingEnabled(false);
        mActivity.disableDrawer();
        mActivity.updateStorageSpaceAndHint();
        if (mActivity.getStorageSpaceBytes() <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
            Log.v(TAG, "Storage issue, ignore the start request");
            mStartRecPending = false;
            return;
        }

        if( mUnsupportedHFRVideoSize == true) {
            Log.e(TAG, "Unsupported HFR and video size combinations");
            Toast.makeText(mActivity,R.string.error_app_unsupported_hfr, Toast.LENGTH_SHORT).show();
            mStartRecPending = false;
            return;
        }

        if (mUnsupportedHSRVideoSize == true) {
            Log.e(TAG, "Unsupported HSR and video size combinations");
            Toast.makeText(mActivity,R.string.error_app_unsupported_hsr, Toast.LENGTH_SHORT).show();
            mStartRecPending = false;
            return;
        }

        if( mUnsupportedHFRVideoCodec == true) {
            Log.e(TAG, "Unsupported HFR and video codec combinations");
            Toast.makeText(mActivity, R.string.error_app_unsupported_hfr_codec,
            Toast.LENGTH_SHORT).show();
            mStartRecPending = false;
            return;
        }
        //??
        //if (!mCameraDevice.waitDone()) return;
        mCurrentVideoUri = null;

        initializeRecorder();
        if (mUnsupportedResolution == true) {
              Log.v(TAG, "Unsupported Resolution according to target");
              mStartRecPending = false;
              return;
        }
        if (mMediaRecorder == null) {
            Log.e(TAG, "Fail to initialize media recorder");
            mStartRecPending = false;
            return;
        }

        pauseAudioPlayback();

        try {
        	Log.i(TAG, "mMediaRecorder.start()");
            mMediaRecorder.start(); // Recording is now started
        } catch (RuntimeException e) {
            Log.e(TAG, "Could not start media recorder. ", e);
            releaseMediaRecorder();
            // If start fails, frameworks will not lock the camera for us.
            mCameraDevice.lock();
            mStartRecPending = false;
            //modify by minghui.hua for PR979500
            if(!mIsVideoCaptureIntent){
                mActivity.switchPhotoVideo(GC.MODE_PHOTO);
                mUI.setSwipingEnabled(true);
                mActivity.enableDrawer();
            }
            return;
        }
        if(mIsVideoCaptureIntent){
            mShutterManager.setVideoCaptureIntentShutterButton();
        }else{
            mShutterManager.setShutterVideoStartUI(true);
        }
        // Make sure the video recording has started before announcing
        // this in accessibility.
        AccessibilityUtils.makeAnnouncement(mUI.getShutterButton(),
                mActivity.getString(R.string.video_recording_started));
//        mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
//		mCameraDevice.getCamera().setParameters(mParameters);
        // The parameters might have been altered by MediaRecorder already.
        // We need to force mCameraDevice to refresh before getting it.
        mCameraDevice.refreshParameters();
        // The parameters may have been changed by MediaRecorder upon starting
        // recording. We need to alter the parameters if we support camcorder
        // zoom. To reduce latency when setting the parameters during zoom, we
        // update mParameters here once.
        mParameters = mCameraDevice.getParameters();
        // mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
        //mParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		mCameraDevice.getCamera().setParameters(mParameters);
        //lmch mark zoommanager should do something here
        //mActivity.getCameraViewManager().enableCameraControls(false);
        mMediaRecorderRecording = true;
        mMediaRecoderRecordingPaused = false;
        // modify by minghui.hua for PR971964
        // mOrientationManager.lockOrientation();
        mRecordingStartTime = SystemClock.uptimeMillis();

        mActivity.getCameraViewManager().showRecordingUI(true);
//      mSwitcherManager.hide();
        mUI.showRecordingUI(true);

        updateRecordingTime();
        keepScreenOn();
        UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
                UsageStatistics.ACTION_CAPTURE_START, "Video");
        mStartRecPending = false;
    }

    private Bitmap getVideoThumbnail() {
        Bitmap bitmap = null;
        if (mVideoFileDescriptor != null) {
            bitmap = Thumbnail.createVideoThumbnailBitmap(mVideoFileDescriptor.getFileDescriptor(),
                    mDesiredPreviewWidth);
        } else if (mCurrentVideoUri != null) {
            try {
                mVideoFileDescriptor = mContentResolver.openFileDescriptor(mCurrentVideoUri, "r");
                bitmap = Thumbnail.createVideoThumbnailBitmap(
                        mVideoFileDescriptor.getFileDescriptor(), mDesiredPreviewWidth);
            } catch (java.io.FileNotFoundException ex) {
                // invalid uri
                Log.e(TAG, ex.toString());
            }
        }

        if (bitmap != null) {
            // MetadataRetriever already rotates the thumbnail. We should rotate
            // it to match the UI orientation (and mirror if it is front-facing camera).
            CameraInfo[] info = CameraHolder.instance().getCameraInfo();
            boolean mirror = (info[mCameraId].facing == CameraInfo.CAMERA_FACING_FRONT);
            bitmap = CameraUtil.rotateAndMirror(bitmap, 0, mirror);
        }
        return bitmap;
    }

    private void showCaptureResult() {
        mIsInReviewMode = true;
        Bitmap bitmap = getVideoThumbnail();
        if (bitmap != null) {
            mUI.showReviewImage(bitmap);
        }
        mShutterManager.hide();
        mUI.showReviewControls();
        mUI.enableCameraControls(false);
        mUI.showTimeLapseUI(false);
        //Add by zhiming.yu for PR913890 begin
        mTopViewManager.setVedioCameraPickershowView(false);
        //Add by zhiming.yu for PR913890 end
    }
    public void doWhenCloseActivity() {

    }

    //[BUGFIX]Added by jian.zhang1 for PR753112 2014.08.20 Begin
//    private boolean stopVideoRecording() {
//        Log.v(TAG, "stopVideoRecording");
//        mStopRecPending = true;
//        mUI.setSwipingEnabled(true);
//        if (!isVideoCaptureIntent()) {
//            mSwitcherManager.showSwitcher();
//        }
//
//        boolean fail = false;
//        if (mMediaRecorderRecording) {
//            boolean shouldAddToMediaStoreNow = false;
//
//            try {
//                mMediaRecorder.setOnErrorListener(null);
//                mMediaRecorder.setOnInfoListener(null);
//                mMediaRecorder.stop();
//                shouldAddToMediaStoreNow = true;
//                mCurrentVideoFilename = mVideoFilename;
//                Log.v(TAG, "stopVideoRecording: Setting current video filename: "
//                        + mCurrentVideoFilename);
//                AccessibilityUtils.makeAnnouncement(mUI.getShutterButton(),
//                        mActivity.getString(R.string.video_recording_stopped));
//            } catch (RuntimeException e) {
//                Log.e(TAG, "stop fail",  e);
//                if (mVideoFilename != null) deleteVideoFile(mVideoFilename);
//                fail = true;
//            }
//            mMediaRecorderRecording = false;
//
//            //If recording stops while snapshot is in progress, we might not get jpeg callback
//            //because cameraservice will disable picture related messages. Hence reset the
//            //flag here so that we can take liveshots in the next recording session.
//            mSnapshotInProgress = false;
//
//            //[BUGFIX]-Add by TCTNB,kechao.chen, 2014-08-02,PR742932 Begin
//            //mOrientationManager.unlockOrientation();
//            //[BUGFIX]-Add by TCTNB,kechao.chen, 2014-08-02,PR742932 Begin
//            // If the activity is paused, this means activity is interrupted
//            // during recording. Release the camera as soon as possible because
//            // face unlock or other applications may need to use the camera.
//            if (mPaused) {
//                closeCamera();
//            }
//
//            mUI.showRecordingUI(false);
//            mActivity.getCameraViewManager().showRecordingUI(false);
//            if (!mIsVideoCaptureIntent) {
//                mUI.enableCameraControls(true);
//                //lmch mark zoommanager should do something here;
//            }
//            // The orientation was fixed during video recording. Now make it
//            // reflect the device orientation as video recording is stopped.
//            mUI.setOrientationIndicator(0, true);
//            keepScreenOnAwhile();
//            if (shouldAddToMediaStoreNow && !fail) {
//                if (mVideoFileDescriptor == null) {
//                    saveVideo();
//                } else if (mIsVideoCaptureIntent) {
//                    // if no file save is needed, we can show the post capture UI now
//                    showCaptureResult();
//                }
//            }
//        }
//        // release media recorder
//        releaseMediaRecorder();
//        if (!mPaused) {
//            mCameraDevice.lock();
//            if (!ApiHelper.HAS_SURFACE_TEXTURE_RECORDING) {
//                stopPreview();
//                mUI.hideSurfaceView();
//                // Switch back to use SurfaceTexture for preview.
//                startPreview();
//            }
//        }
//        // Update the parameters here because the parameters might have been altered
//        // by MediaRecorder.
//        if (!mPaused) mParameters = mCameraDevice.getParameters();
//        UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
//                fail ? UsageStatistics.ACTION_CAPTURE_FAIL :
//                    UsageStatistics.ACTION_CAPTURE_DONE, "Video",
//                    SystemClock.uptimeMillis() - mRecordingStartTime);
//        mStopRecPending = false;
//      //[BUGFIX]-Add-BEGIN by TCTNB.Fanghua.Gu,08/12/2014,738082
////        if(mActivity.isNonePickIntent()){
//            mShutterManager.setShutterVideoStartUI(false);
////        }
//        //[BUGFIX]-Add-END by TCTNB.Fanghua.Gu
//
//        return fail;
//    }

    public void stopVideoRecording() {
        // [BUGFIX]-Add by TCTNB,haibo.chen, 2014-10-24,PR812550 Begin
        if (mMediaRecoderRecordingPaused) {
            mRecordingStartTime = SystemClock.uptimeMillis();
        }
        // [BUGFIX]-Add by TCTNB,haibo.chen, 2014-10-24,PR812550 End
        //CameraActivity.TraceQCTLog("Stop Recording", new Throwable());//PR934539, to track stop recording issue
        mHandler.removeMessages(MSG_CHANGE_FOCUS);
        mActivity.abandonAudioFocus();
        mStopRecPending = true;
        mUI.setSwipingEnabled(true);
        mUI.showRecordingProgress(false);
        mActivity.enableDrawer();
        if (!isVideoCaptureIntent()) {
            mSwitcherManager.showSwitcher();
        }
        mShutterManager.setShutterVideoStartUI(false);
        if (mMediaRecorderRecording) {
            if (!mIsVideoCaptureIntent) {
                showProgress(mActivity
                        .getString(R.string.pano_review_saving_indication_str));
            }
            if (mSavingThread == null || !mSavingThread.isAlive()) {
                isSaving = true;
                mSavingThread = new SavingThread();
                mSavingThread.start();
                try {
					mSavingThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }  
        mActivity.getGC().flagVideoRecording(false);
        if(mParameters!=null&&mCameraDevice!=null){
            forceFlashOffIfSupported(false);
            mCameraDevice.setParameters(mParameters);
        }
        mActivity.setViewState(GC.VIEW_STATE_NORMAL);
        if(mIsVideoCaptureIntent){
            mTopViewManager.hideviews(true, false, false);
        }
        // modify by minghui.hua for PR971964
        // mOrientationManager.unlockOrientation();
    }

    public class SavingThread extends Thread {
        @Override
        public void run() {
            shouldAddToMediaStoreNow = false;
            boolean fail = false;
            try {
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setOnInfoListener(null);
                Log.i(TAG, "MMMstopabove");
                mMediaRecorder.stop();
                shouldAddToMediaStoreNow = true;
                mCurrentVideoFilename = mVideoFilename;
                Log.v(TAG,
                        "stopVideoRecording: Setting current video filename: "
                                + mCurrentVideoFilename);
            } catch (RuntimeException e) {
                Log.e(TAG, "stop fail", e);
                if (mVideoFilename != null)
                    deleteVideoFile(mVideoFilename);
                fail = true;
            } finally {
            	Log.i(TAG, "SavingThread finally");
                mMediaRecorderRecording = false;
                mSnapshotInProgress = false;
                Message msg = mHandler.obtainMessage(VIDEO_SAVE_COMPLETE);
                msg.arg1 = fail ? TCT_SAVE_FAIL : TCT_SAVE_SUCCESS;
                mHandler.sendMessage(msg);
            }
            Log.d(TAG, "SavingThread end");
            super.run();
            
        }
    }

    private void videoHasStoped(boolean recordFail) {
        if (mPaused) {
            closeCamera();
        }
        mUI.showRecordingUI(false);
        if (!mIsVideoCaptureIntent) {
            mUI.enableCameraControls(true);
            // lmch mark zoommanager should do something here;
        }
        // The orientation was fixed during video recording. Now make it
        // reflect the device orientation as video recording is stopped.
        mUI.setOrientationIndicator(0, true);
        keepScreenOnAwhile();

//        if (shouldAddToMediaStoreNow) {
//            saveVideo();
//        }
        if (shouldAddToMediaStoreNow) {
            if (mVideoFileDescriptor == null) {
                Log.i(TAG, "lina mVideoFileDescriptor == null");
                saveVideo();
            } else if (mIsVideoCaptureIntent) {
            // if no file save is needed, we can show the post capture UI now
                Log.i(TAG, "lina show");
               showCaptureResult();
            }
       }
        // release media recorder
        releaseMediaRecorder();
        if (!mPaused) {
            mCameraDevice.lock();
            if (!ApiHelper.HAS_SURFACE_TEXTURE_RECORDING) {
                stopPreview();
                mUI.hideSurfaceView();
                // Switch back to use SurfaceTexture for preview.
                startPreview();
            }
        }
        // Update the parameters here because the parameters might have been
        // altered
        // by MediaRecorder.
        if (!mPaused) {
            mParameters = mCameraDevice.getParameters();
        }
        UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
                false ? UsageStatistics.ACTION_CAPTURE_FAIL
                        : UsageStatistics.ACTION_CAPTURE_DONE, "Video",
                SystemClock.uptimeMillis() - mRecordingStartTime);
        isSaving = false;
        AccessibilityUtils.makeAnnouncement(mUI.getShutterButton(),
                mActivity.getString(R.string.video_recording_stopped));

        mActivity.getCameraViewManager().getRotateProgress().hide();
        if(mActivity.isNonePickIntent()){
            mActivity.getCameraViewManager().setViewState(GC.VIEW_STATE_NORMAL);
        }

        mStopRecPending = false;

        if (mIsVideoCaptureIntent) {
            if (mQuickCapture) {
                doReturnToCaller(!recordFail);
            } else if (!recordFail) {
                showCaptureResult();
            }
        } else if (!recordFail){
            // Start capture animation.
            if (!mPaused && ApiHelper.HAS_SURFACE_TEXTURE_RECORDING) {
                // The capture animation is disabled on ICS because we use SurfaceView
                // for preview during recording. When the recording is done, we switch
                // back to use SurfaceTexture for preview and we need to stop then start
                // the preview. This will cause the preview flicker since the preview
                // will not be continuous for a short period of time.

                //mUI.animateFlash();
                //mUI.animateCapture();
            }
        }
    }

    private void showProgress(String msg){
        mUI.showRecordingUI(false);
        mActivity.getCameraViewManager().setViewState(GC.VIEW_STATE_SHUTTER_PRESS);
        mActivity.getCameraViewManager().getRotateProgress().showProgress(msg, mOrientation, false);
    }
    //[BUGFIX]Added by jian.zhang1 for PR753112 2014.08.20 End

    private void resetScreenOn() {
//        mHandler.removeMessages(CLEAR_SCREEN_DELAY);
//        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void keepScreenOnAwhile() {
        // modify by minghui.hua for PR956858, keep screen on after enter camera
//        mHandler.removeMessages(CLEAR_SCREEN_DELAY);
//        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//         mHandler.sendEmptyMessageDelayed(CLEAR_SCREEN_DELAY, SCREEN_DELAY);
    }

    private void keepScreenOn() {
//        mHandler.removeMessages(CLEAR_SCREEN_DELAY);
//        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private static String millisecondToTimeString(long milliSeconds, boolean displayCentiSeconds) {
        long seconds = milliSeconds / 1000; // round down to compute seconds
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long remainderMinutes = minutes - (hours * 60);
        long remainderSeconds = seconds - (minutes * 60);

        StringBuilder timeStringBuilder = new StringBuilder();

        // Hours
        if (hours > 0) {
            if (hours < 10) {
                timeStringBuilder.append('0');
            }
            timeStringBuilder.append(hours);

            timeStringBuilder.append(':');
        }

        // Minutes
        if (remainderMinutes < 10) {
            timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderMinutes);
        timeStringBuilder.append(':');

        // Seconds
        if (remainderSeconds < 10) {
            timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderSeconds);

        // Centi seconds
        if (displayCentiSeconds) {
            timeStringBuilder.append('.');
            long remainderCentiSeconds = (milliSeconds - seconds * 1000) / 10;
            if (remainderCentiSeconds < 10) {
                timeStringBuilder.append('0');
            }
            timeStringBuilder.append(remainderCentiSeconds);
        }

        return timeStringBuilder.toString();
    }

    private long getTimeLapseVideoLength(long deltaMs) {
        // For better approximation calculate fractional number of frames captured.
        // This will update the video time at a higher resolution.
        double numberOfFrames = (double) deltaMs / mTimeBetweenTimeLapseFrameCaptureMs;
        return (long) (numberOfFrames / mProfile.videoFrameRate * 1000);
    }

    private void updateRecordingTime() {
        //[BUGFIX]Added by jian.zhang1 for PR753112 2014.08.20 Begin
        if (!mMediaRecorderRecording || isSaving) {
        //if (!mMediaRecorderRecording) {
        //[BUGFIX]Added by jian.zhang1 for PR753112 2014.08.20 End
            return;
        }
        long now = SystemClock.uptimeMillis();
        long delta = now - mRecordingStartTime;
        if (mMediaRecoderRecordingPaused) {
            delta = mVideoRecordedDuration;
        }
        // Starting a minute before reaching the max duration
        // limit, we'll countdown the remaining time instead.
        boolean countdownRemainingTime = (mMaxVideoDurationInMs != 0
                && delta >= mMaxVideoDurationInMs - 60000);

        long deltaAdjusted = delta;
        if (countdownRemainingTime) {
            deltaAdjusted = Math.max(0, mMaxVideoDurationInMs - deltaAdjusted) + 999;
        }
        String text;

        long targetNextUpdateDelay;
        if (!mCaptureTimeLapse) {
            text = millisecondToTimeString(deltaAdjusted, false);
            targetNextUpdateDelay = 1000;
        } else {
            // The length of time lapse video is different from the length
            // of the actual wall clock time elapsed. Display the video length
            // only in format hh:mm:ss.dd, where dd are the centi seconds.
            text = millisecondToTimeString(getTimeLapseVideoLength(delta), true);
            targetNextUpdateDelay = mTimeBetweenTimeLapseFrameCaptureMs;
        }

        mUI.setRecordingTime(text);
        // modify by minghui.hua for PR942321
        if (mVideoFileDescriptor!=null) {
            mUI.showRecordingProgress(true);
            mUI.setRecordingProgress((int)(mVideoFileDescriptor.getStatSize()*100/mMaxVideoSize));
        }

        if (mRecordingTimeCountsDown != countdownRemainingTime) {
            // Avoid setting the color on every update, do it only
            // when it needs changing.
            mRecordingTimeCountsDown = countdownRemainingTime;

            int color = mActivity.getResources().getColor(countdownRemainingTime
                    ? R.color.recording_time_remaining_text
                    : R.color.recording_time_elapsed_text);

            mUI.setRecordingTimeTextColor(color);
        }

        mRecordingTimeIndicator = !mRecordingTimeIndicator;
        if (mMediaRecoderRecordingPaused && mRecordingTimeIndicator) {
            mUI.showRecordingTimeUI(false);
        } else {
            mUI.showRecordingTimeUI(true);
        }

        long actualNextUpdateDelay = 500;

        if (!mMediaRecoderRecordingPaused) {
            actualNextUpdateDelay = targetNextUpdateDelay - (delta % targetNextUpdateDelay);
        }
        mHandler.sendEmptyMessageDelayed(
                UPDATE_RECORD_TIME, actualNextUpdateDelay);
    }

    private static boolean isSupported(String value, List<String> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }

     private void qcomSetCameraParameters(){
        // add QCOM Parameters here
        // Set color effect parameter.
        String colorEffect = mPreferences.getString(
            CameraSettings.KEY_COLOR_EFFECT,
            mActivity.getString(R.string.pref_camera_coloreffect_default));
        Log.v(TAG, "Color effect value =" + colorEffect);
        if (isSupported(colorEffect, mParameters.getSupportedColorEffects())) {
            mParameters.setColorEffect(colorEffect);
        }

        /*String disMode = mPreferences.getString(
                CameraSettings.KEY_DIS,
                mActivity.getString(R.string.pref_camera_dis_default));
        Log.v(TAG, "DIS value =" + disMode);
        */
        /*if (isSupported(disMode,
                        CameraSettings.getSupportedDISModes(mParameters))) {
            mParameters.set(CameraSettings.KEY_QC_DIS_MODE, disMode);
        }*/

        mUnsupportedHFRVideoSize = false;
        mUnsupportedHFRVideoCodec = false;
        // To set preview format as YV12 , run command
        // "adb shell setprop "debug.camera.yv12" true"
//        String yv12formatset = SystemProperties.get("debug.camera.yv12");
//        if(yv12formatset.equals("true")) {
//            Log.v(TAG, "preview format set to YV12");
//            mParameters.setPreviewFormat (ImageFormat.YV12);
//        }
       // if 4K recoding is enabled, set preview format to NV12_VENUS
       if (is4KEnabled()) {
           Log.v(TAG, "4K enabled, preview format set to NV12_VENUS");
           mParameters.set(KEY_PREVIEW_FORMAT, QC_FORMAT_NV12_VENUS);
       }
        // Set High Frame Rate.
        String HighFrameRate = mPreferences.getString(
            CameraSettings.KEY_VIDEO_HIGH_FRAME_RATE,
            mActivity. getString(R.string.pref_camera_hfr_default));
        if(!("off".equals(HighFrameRate)) && !("hsr".equals(HighFrameRate))){
            mUnsupportedHFRVideoSize = true;
            String hfrsize = videoWidth+"x"+videoHeight;
            Log.v(TAG, "current set resolution is : "+hfrsize);
            try {
                /*for(Size size :  mParameters.getSupportedHfrSizes()){
                    if(size != null) {
                        Log.v(TAG, "supported hfr size : "+ size.width+ " "+size.height);
                        if(videoWidth <= size.width && videoHeight <= size.height) {
                            mUnsupportedHFRVideoSize = false;
                            Log.v(TAG,"Current hfr resolution is supported");
                            break;
                        }
                    }
                }*/
            } catch (NullPointerException e){
                Log.e(TAG, "supported hfr sizes is null");
            }

            int hfrFps = Integer.parseInt(HighFrameRate);
            int inputBitrate = videoWidth*videoHeight*hfrFps;

            //check if codec supports the resolution, otherwise throw toast
//            List<VideoEncoderCap> videoEncoders = EncoderCapabilities.getVideoEncoders();
//            for (VideoEncoderCap videoEncoder: videoEncoders) {
//                if (videoEncoder.mCodec == mVideoEncoder){
//                    int maxBitrate = (videoEncoder.mMaxHFRFrameWidth *
//                                     videoEncoder.mMaxHFRFrameHeight *
//                                     videoEncoder.mMaxHFRMode);
//                    if (inputBitrate > maxBitrate ){
//                            Log.e(TAG,"Selected codec "+mVideoEncoder+
//                                " does not support HFR " + HighFrameRate + " with "+ videoWidth
//                                + "x" + videoHeight +" resolution");
//                            Log.e(TAG, "Codec capabilities: " +
//                                "mMaxHFRFrameWidth = " + videoEncoder.mMaxHFRFrameWidth + " , "+
//                                "mMaxHFRFrameHeight = " + videoEncoder.mMaxHFRFrameHeight + " , "+
//                                "mMaxHFRMode = " + videoEncoder.mMaxHFRMode);
//                            mUnsupportedHFRVideoSize = true;
//                    }
//                    break;
//                }
//            }

            if(mUnsupportedHFRVideoSize)
                Log.v(TAG,"Unsupported hfr resolution");

            if(mVideoEncoder != MediaRecorder.VideoEncoder.H264)
                mUnsupportedHFRVideoCodec = true;
        }
        /*if (isSupported(HighFrameRate,
                mParameters.getSupportedVideoHighFrameRateModes()) &&
                !mUnsupportedHFRVideoSize &&
                !("hsr".equals(HighFrameRate))) {
            mParameters.setVideoHighFrameRate(HighFrameRate);
            mParameters.set("video-hsr", "off");
        }
        else {
            mParameters.setVideoHighFrameRate("off");
        }*/
        //[BUGFIX]-Add by TCTNB,zhijie.gui, 2014-08-01,PR734011 Begin
        // Set anti banding parameter.
        String defAntiband=CustomUtil.getInstance().getString(CustomFields.DEF_ANTIBAND_DEFAULT, "auto");
        String antiBanding = mPreferences.getString(
                 CameraSettings.KEY_ANTIBANDING,defAntiband);//FR 720721 ,sichao.hu modified
//                 mActivity.getString(R.string.pref_camera_antibanding_default));
        Log.v(TAG, "antiBanding value =" + antiBanding);
        if (CameraUtil.isSupported(antiBanding, mParameters.getSupportedAntibanding())) {
            mParameters.setAntibanding(antiBanding);
        }
        //[BUGFIX]-Add by TCTNB,zhijie.gui, 2014-08-01,PR734011 End

        mUnsupportedHSRVideoSize = false;

        if (("hsr".equals(HighFrameRate))) {
            mUnsupportedHSRVideoSize = true;
            String hsrsize = videoWidth+"x"+videoHeight;
            Log.v(TAG, "current set resolution is : "+hsrsize);
            try {
                Size size = null;
                /*if (isSupported("120",mParameters.getSupportedVideoHighFrameRateModes())) {
                    int index = mParameters.getSupportedVideoHighFrameRateModes().indexOf(
                        "120");
                    size = mParameters.getSupportedHfrSizes().get(index);
                }*/
                if (size != null) {
                    Log.v(TAG, "supported hsr size : "+ size.width+ " "+size.height);
                    if (videoWidth <= size.width && videoHeight <= size.height) {
                        mUnsupportedHSRVideoSize = false;
                        Log.v(TAG,"Current hsr resolution is supported");
                    }
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "supported hfr sizes is null");
            }

            if (mUnsupportedHSRVideoSize) Log.v(TAG,"Unsupported hsr resolution");
        }

        if (("hsr".equals(HighFrameRate)) && !mUnsupportedHSRVideoSize) {
            mParameters.set("video-hsr", "on");
        }
        else {
            mParameters.set("video-hsr", "off");
        }

        // Read Flip mode from adb command
        //value: 0(default) - FLIP_MODE_OFF
        //value: 1 - FLIP_MODE_H
        //value: 2 - FLIP_MODE_V
        //value: 3 - FLIP_MODE_VH
//        int preview_flip_value = SystemProperties.getInt("debug.camera.preview.flip", 0);
//        int video_flip_value = SystemProperties.getInt("debug.camera.video.flip", 0);
//        int picture_flip_value = SystemProperties.getInt("debug.camera.picture.flip", 0);
        int preview_flip_value = 0;
        int video_flip_value = 0;
        int picture_flip_value = 0;
        int rotation = CameraUtil.getJpegRotation(mCameraId, mOrientation);
        mParameters.setRotation(rotation);
        if (rotation == 90 || rotation == 270) {
            // in case of 90 or 270 degree, V/H flip should reverse
            if (preview_flip_value == 1) {
                preview_flip_value = 2;
            } else if (preview_flip_value == 2) {
                preview_flip_value = 1;
            }
            if (video_flip_value == 1) {
                video_flip_value = 2;
            } else if (video_flip_value == 2) {
                video_flip_value = 1;
            }
            if (picture_flip_value == 1) {
                picture_flip_value = 2;
            } else if (picture_flip_value == 2) {
                picture_flip_value = 1;
            }
        }
        String preview_flip = CameraUtil.getFilpModeString(preview_flip_value);
        String video_flip = CameraUtil.getFilpModeString(video_flip_value);
        String picture_flip = CameraUtil.getFilpModeString(picture_flip_value);

        if(CameraUtil.isSupported(preview_flip, CameraSettings.getSupportedFlipMode(mParameters))){
            mParameters.set(CameraSettings.KEY_QC_PREVIEW_FLIP, preview_flip);
        }
        if(CameraUtil.isSupported(video_flip, CameraSettings.getSupportedFlipMode(mParameters))){
            mParameters.set(CameraSettings.KEY_QC_VIDEO_FLIP, video_flip);
        }
        if(CameraUtil.isSupported(picture_flip, CameraSettings.getSupportedFlipMode(mParameters))){
            mParameters.set(CameraSettings.KEY_QC_SNAPSHOT_PICTURE_FLIP, picture_flip);
        }

        // Set Video HDR.
        String videoHDR = mPreferences.getString(
                CameraSettings.KEY_VIDEO_HDR,
                mActivity.getString(R.string.pref_camera_video_hdr_default));
        Log.v(TAG, "Video HDR Setting =" + videoHDR);
        ExtendParameters extParam = ExtendParameters.getInstance(mParameters);
        if (isSupported(videoHDR, extParam.getSupportedVideoHDRModes())) {
            extParam.setVideoHDRMode(videoHDR);
        } else
            extParam.setVideoHDRMode("off");

        // modify by minghui.hua for FR969967
        String eis_enable = mPreferences.getString(CameraSettings.KEY_VIDEO_EIS,
                mActivity.getString(R.string.pref_video_eis_default));
        Log.d(TAG,"###YUHUAN###qcomSetCameraParameters#eis_enable=" + eis_enable);
        if (CustomUtil.getInstance().getBoolean(CustomFields.DEF_VIDEO_EIS_ENABLE, true)) {
            mParameters.set(GC.KEY_EIS_ENABLE, eis_enable);
        } else {
            mParameters.set(GC.KEY_EIS_ENABLE, mActivity.getString(R.string.setting_off_value));
        }

        //HFR/HSR recording not supported for DIS and/ TimeLapse option
        //String hfr = mParameters.getVideoHighFrameRate();
        String hfr = null;
        String hsr = mParameters.get("video-hsr");
        if ( ((hfr != null) && (!hfr.equals("off"))) ||
             ((hsr != null) && (!hsr.equals("off"))) ) {
             // Read time lapse recording interval.
             String frameIntervalStr = mPreferences.getString(
                    CameraSettings.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL,
                    mActivity.getString(R.string.pref_video_time_lapse_frame_interval_default));
             int timeLapseInterval = Integer.parseInt(frameIntervalStr);
             if ((timeLapseInterval != 0)) {
                Log.v(TAG,"DIS/Time Lapse ON for HFR/HSR selection, turning HFR/HSR off");
                Toast.makeText(mActivity, R.string.error_app_unsupported_hfr_selection,
                          Toast.LENGTH_LONG).show();
                //mParameters.setVideoHighFrameRate("off");
                mParameters.set("video-hsr", "off");
                mUI.overrideSettings(CameraSettings.KEY_VIDEO_HIGH_FRAME_RATE,"off");
             }
        }

        //getSupportedPictureSizes will always send a sorted a list in descending order
        Size biggestSize = mParameters.getSupportedPictureSizes().get(0);

        /*if (biggestSize.width <= videoWidth || biggestSize.height <= videoHeight) {
            if (disMode.equals("enable")) {
                Log.v(TAG,"DIS is not supported for this video quality");
                Toast.makeText(mActivity, R.string.error_app_unsupported_dis,
                               Toast.LENGTH_LONG).show();
                mParameters.set(CameraSettings.KEY_QC_DIS_MODE, "disable");
                mUI.overrideSettings(CameraSettings.KEY_DIS,"disable");
            }
        }*/
    }
    @SuppressWarnings("deprecation")
    private void setCameraParameters() {
        Log.d(TAG,"###YUHUAN###setCameraParameters#mDesiredPreviewWidth=" + mDesiredPreviewWidth);
        Log.d(TAG,"###YUHUAN###setCameraParameters#mDesiredPreviewHeight=" + mDesiredPreviewHeight);
        mParameters.setPreviewSize(mDesiredPreviewWidth, mDesiredPreviewHeight);
        
        int[] fpsRange = CameraUtil.getMaxPreviewFpsRange(mParameters);
        if (fpsRange.length > 0) {
            mParameters.setPreviewFpsRange(
                    fpsRange[Parameters.PREVIEW_FPS_MIN_INDEX],
                    fpsRange[Parameters.PREVIEW_FPS_MAX_INDEX]);
        } else {
        	Log.d(TAG,"###YUHUAN###setCameraParameters#mProfile.videoFrameRate=" + mProfile.videoFrameRate);
            mParameters.setPreviewFrameRate(mProfile.videoFrameRate);
        }
        
        // yuhuan 20150703
        //mParameters.set("tct-fff-enable", "off");

        Log.d(TAG,"###YUHUAN###setCameraParameters#mPreviewFocused=" + mPreviewFocused);
        forceFlashOffIfSupported(!mPreviewFocused);
        /* [BUG]Reset the video size when the device doesn't support the frame size, lijuan.zhang added 20141105 begin*/
        List<Camera.Size> sizes = mParameters.getSupportedPreviewSizes(); 
        List<Camera.Size> supportVideoSizes = mParameters.getSupportedVideoSizes();
        Camera.Size cs = sizes.get(getNearestParamsToSet());
        Camera.Size videocs = supportVideoSizes.get(getNearestVideoSizeToSet());
        int newPreviewPx = mParameters.getPreviewSize().width * mParameters.getPreviewSize().height;
        int supportPreviewPx = cs.width * cs.height;
        if((newPreviewPx == supportPreviewPx) && (mParameters.getPreviewSize().width == cs.width)
        		&& (mProfile.videoFrameWidth * mProfile.videoFrameHeight <=  newPreviewPx) ) {
        	videoWidth = mProfile.videoFrameWidth;
        	videoHeight = mProfile.videoFrameHeight;
        } else {
	        videoWidth = videocs.width;
	        videoHeight = videocs.height;
        }
        Log.d(TAG,"###YUHUAN###setCameraParameters#videoWidth=" + videoWidth + ",videoHeight=" + videoHeight);
        /* [BUG]Reset the video size when the device doesn't support the frame size, lijuan.zhang added 20141105 end*/
        
        String recordSize = videoWidth + "x" + videoHeight;
        Log.d(TAG,"###YUHUAN###setCameraParameters#recordSize=" + recordSize);
        mParameters.set("video-size", recordSize);
        
        // yuhuan 20150703
        //mParameters.set("arcsoft-mode", "off");
        ExtendParameters extParams=ExtendParameters.getInstance(mParameters);
        String Denoise = mPreferences.getString( CameraSettings.KEY_DENOISE,
                mActivity.getString(R.string.pref_camera_denoise_default));
        Log.d(TAG,"###YUHUAN###setCameraParameters#Denoise=" + Denoise);
        if (CameraUtil.isSupported(Denoise, extParams.getSupportedDenoiseModes())) {
            extParams.setDenoise(Denoise);
        }
        mRecordingSupported = mPreferences.getString(CameraSettings.KEY_SOUND_RECORDING,
                                  mActivity.getString(R.string.pref_sound_recording_def_value))
                          .equals(mActivity.getString(R.string.setting_on_value));
        Log.d(TAG,"###YUHUAN###setCameraParameters#mRecordingSupported=" + mRecordingSupported);

        // Set white balance parameter.
        String whiteBalance = mPreferences.getString(
                CameraSettings.KEY_WHITE_BALANCE,
                mActivity.getString(R.string.pref_camera_whitebalance_default));
        Log.d(TAG,"###YUHUAN###setCameraParameters#whiteBalance=" + whiteBalance);
        if (isSupported(whiteBalance,
                mParameters.getSupportedWhiteBalance())) {
            mParameters.setWhiteBalance(whiteBalance);
        } else {
            whiteBalance = mParameters.getWhiteBalance();
            if (whiteBalance == null) {
                whiteBalance = Parameters.WHITE_BALANCE_AUTO;
            }
        }

        // Set zoom.
        if (mParameters.isZoomSupported()) {
            Parameters p = mCameraDevice.getParameters();
            mZoomValue = p.getZoom();
            Log.d(TAG,"###YUHUAN###setCameraParameters#mZoomValue=" + mZoomValue);
            mParameters.setZoom(mZoomValue);
        }

        // Set continuous autofocus.
        //List<String> supportedFocus = mParameters.getSupportedFocusModes();
        //if (isSupported(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO, supportedFocus)) {
            //mParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        //}

        mParameters.set(CameraUtil.RECORDING_HINT, CameraUtil.TRUE);

        // Enable video stabilization. Convenience methods not available in API
        // level <= 14
        String vstabSupported = mParameters.get("video-stabilization-supported");
        Log.d(TAG,"###YUHUAN###setCameraParameters#vstabSupported=" + vstabSupported);
        if ("true".equals(vstabSupported)) {
            mParameters.set("video-stabilization", "true");
        }

        // Set picture size.
        // The logic here is different from the logic in still-mode camera.
        // There we determine the preview size based on the picture size, but
        // here we determine the picture size based on the preview size.
        List<Size> supported = mParameters.getSupportedPictureSizes();
        Size optimalSize = CameraUtil.getOptimalVideoSnapshotPictureSize(supported,
                (double) mDesiredPreviewWidth / mDesiredPreviewHeight);
        Size original = mParameters.getPictureSize();
        if (!original.equals(optimalSize)) {
            mParameters.setPictureSize(optimalSize.width, optimalSize.height);
        }
        Log.v(TAG, "Video snapshot size is " + optimalSize.width + "x" +
                optimalSize.height);

        // Set JPEG quality.
        int jpegQuality = CameraProfile.getJpegEncodingQualityParameter(mCameraId,
                CameraProfile.QUALITY_HIGH);
        mParameters.setJpegQuality(jpegQuality);
        //Call Qcom related Camera Parameters
        qcomSetCameraParameters();
        mCameraDevice.setParameters(mParameters);
        // Keep preview size up to date.
        mParameters = mCameraDevice.getParameters();

        // Update UI based on the new parameters.
        mUI.updateOnScreenIndicators(mParameters, mPreferences);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Do nothing.
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.v(TAG, "onConfigurationChanged");
        setDisplayOrientation();
        resizeForPreviewAspectRatio();
    }

    @Override
    public void onOverriddenPreferencesClicked() {
    }

    @Override
    // TODO: Delete this after old camera code is removed
    public void onRestorePreferencesClicked() {
    }

    @Override
    public void onSharedPreferenceChanged() {
        // ignore the events after "onPause()" or preview has not started yet
    	Log.d(TAG,"###YUHUAN###onSharedPreferenceChanged#mPaused=" + mPaused);
        if (mPaused) {
            return;
        }
        synchronized (mPreferences) {
            // If mCameraDevice is not ready then we can set the parameter in
            // startPreview().
            if (mCameraDevice == null) return;

            boolean recordLocation = RecordLocationPreference.get(
                    mPreferences, mContentResolver);
            Log.d(TAG,"###YUHUAN###onSharedPreferenceChanged#recordLocation=" + recordLocation);
            mLocationManager.recordLocation(recordLocation);

            readVideoPreferences();
            mUI.showTimeLapseUI(mCaptureTimeLapse);
            // We need to restart the preview if preview size is changed.
            Size size = mParameters.getPreviewSize();
            Log.d(TAG,"###YUHUAN###onSharedPreferenceChanged#size=" + size.width + "x" + size.height);
            Log.d(TAG,"###YUHUAN###onSharedPreferenceChanged#mDesiredPreviewWidth=" + mDesiredPreviewWidth);
            Log.d(TAG,"###YUHUAN###onSharedPreferenceChanged#mDesiredPreviewHeight=" + mDesiredPreviewHeight);
            if (size.width != mDesiredPreviewWidth
                    || size.height != mDesiredPreviewHeight || mRestartPreview) {

                stopPreview();
                resizeForPreviewAspectRatio();
                startPreview(); // Parameters will be set in startPreview().
            } else {
                setCameraParameters();
            }
            mRestartPreview = false;
            mUI.updateOnScreenIndicators(mParameters, mPreferences);
            /// modify by yaping.liu for pr966435 {
            String savePathValue = mPreferences.getString(CameraSettings.KEY_CAMERA_SAVEPATH, "-1");
            Storage.setSaveSDCard(savePathValue.equals("1"), savePathValue.equals("-1"));
            /// }
            mActivity.updateStorageSpaceAndHint();
        }
    }
    
    /* [BUG]Reset the video size when the device doesn't support the frame size, lijuan.zhang added 20141112 begin*/
	public int getNearestParamsToSet() {
		mParameters = mCameraDevice.getParameters();
		int newPreviewWidth = mParameters.getPreviewSize().width;
		int newPreviewHeight = mParameters.getPreviewSize().height;
		List<Camera.Size> sizes = mParameters.getSupportedPreviewSizes();
		int newPreviewPx = newPreviewWidth * newPreviewHeight;
		int MinSize = -1;
		int index = 0;
		for (int i = 0; i < sizes.size(); i++) {
			Camera.Size cs = sizes.get(i);
			int supportPreviewPx = cs.width * cs.height;
			if (MinSize == -1) {
				MinSize = Math.abs(supportPreviewPx - newPreviewPx);
				index = i;
			} else if (Math.abs(supportPreviewPx - newPreviewPx) < MinSize) {
				MinSize = Math.abs(supportPreviewPx - newPreviewPx);
				index = i;
			}
		}
		return index;
	}
		
	public int getNearestVideoSizeToSet() {
		mParameters = mCameraDevice.getParameters();
		int newPreviewWidth = mParameters.getPreviewSize().width;
		int newPreviewHeight = mParameters.getPreviewSize().height;
		List<Camera.Size> supportVideoSizes = mParameters.getSupportedVideoSizes();
		int newPreviewPx = newPreviewWidth * newPreviewHeight;
		int MinSize = -1;
		int index = 0;
		for (int i = 0; i < supportVideoSizes.size(); i++) {
			Camera.Size videocs = supportVideoSizes.get(i);
			int supportVideoSizePx = videocs.width * videocs.height;
			if (MinSize == -1) {
				MinSize = Math.abs(supportVideoSizePx - newPreviewPx);
				index = i;
			} else if (Math.abs(supportVideoSizePx - newPreviewPx) < MinSize) {
				MinSize = Math.abs(supportVideoSizePx - newPreviewPx);
				index = i;
			}
		}
		return index;
	}
	/* [BUG]Reset the video size when the device doesn't support the frame size, lijuan.zhang added 20141112 end*/

    protected void setCameraId(int cameraId) {
        mActivity.setCameraId(mCameraId);
        ListPreference pref = mPreferenceGroup.findPreference(CameraSettings.KEY_CAMERA_ID);
        pref.setValue("" + cameraId);
    }

    private void switchCamera() {
        if (mPaused)  {
            return;
        }
        //[BUGFIX]-Add by TCTNB,xianbin.tang, 2014-08-27,PR766200 Begin
        mUI.showPreviewCover();
        //[BUGFIX]-Add by TCTNB,xianbin.tang, 2014-08-27,PR766200 End
        Log.d(TAG, "Start to switch camera.");
        mCameraId = mPendingSwitchCameraId;
        mPendingSwitchCameraId = -1;
        setCameraId(mCameraId);
        mActivity.setViewState(GC.VIEW_STATE_CAMERA_SWITCH);
        closeCamera();
        mUI.collapseCameraControls();
        // Restart the camera and initialize the UI. From onCreate.
        mPreferences.setLocalId(mActivity, mCameraId);
        CameraSettings.upgradeLocalPreferences(mPreferences.getLocal());
        openCamera();
        readVideoPreferences();
        startPreview();
        initializeVideoSnapshot();
        resizeForPreviewAspectRatio();
        initializeVideoControl();

        // From onResume
        mZoomValue = 0;
        mUI.initializeZoom(mParameters);
        mUI.setOrientationIndicator(0, false);
        mActivity.getCameraViewManager().refresh();

        // Start switch camera animation. Post a message because
        // onFrameAvailable from the old camera may already exist.
        mHandler.sendEmptyMessage(SWITCH_CAMERA_START_ANIMATION);
        mUI.updateOnScreenIndicators(mParameters, mPreferences);
        mZoomManager.resetZoom();
        //[BUGFIX]-Add by TCTNB,xianbin.tang, 2014-08-27,PR766200 Begin
        mHandler.postDelayed(new Runnable(){
            @Override
            public void run() {
                if(mUI != null){
                    mUI.hidePreviewCover();
                }
            }
        }, 500);
        //[BUGFIX]-Add by TCTNB,xianbin.tang, 2014-08-27,PR766200 End
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

    private void initializeVideoSnapshot() {
        if (mParameters == null) return;
        if (CameraUtil.isVideoSnapshotSupported(mParameters) && !mIsVideoCaptureIntent) {
            // Show the tap to focus toast if this is the first start.
            if (mPreferences.getBoolean(
                        CameraSettings.KEY_VIDEO_FIRST_USE_HINT_SHOWN, true)) {
                // Delay the toast for one second to wait for orientation.
//                mHandler.sendEmptyMessageDelayed(SHOW_TAP_TO_SNAPSHOT_TOAST, 1000);
            }
        }
    }

    void showVideoSnapshotUI(boolean enabled) {
        if (mParameters == null) return;
        if (CameraUtil.isVideoSnapshotSupported(mParameters) && !mIsVideoCaptureIntent) {
            if (enabled) {
                mUI.animateFlash();
                mUI.animateCapture();
            } else {
                mUI.showPreviewBorder(enabled);
            }
//            mShutterManager.enableShutter(!enabled);
        }
    }

    private void forceFlashOffIfSupported(boolean forceOff) {
        String flashMode;
        if (!forceOff) {
            //modify by minghui.hua for PR904085,PR920517 begin
//            flashMode = mPreferences.getString(
//                    CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE,
//                    mActivity.getString(R.string.pref_camera_video_flashmode_default));
            flashMode = mPreferences.getString(
                    CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE,
                    mActivity.getString(R.string.pref_camera_flashmode_default));
            if (Parameters.FLASH_MODE_ON.endsWith(flashMode)
                    ||Parameters.FLASH_MODE_TORCH.equals(flashMode)) {//double check for video flash mode setting
                flashMode = Parameters.FLASH_MODE_TORCH;
            }else {
                flashMode = Parameters.FLASH_MODE_OFF;
            }
            
            if(!mActivity.getGC().isVideoRecording()){
                flashMode=Parameters.FLASH_MODE_OFF;
            }
            //modify by minghui.hua for PR904085 end
        } else {
            flashMode = Parameters.FLASH_MODE_OFF;
        }
        Log.d(TAG,"###YUHUAN###forceFlashOffIfSupported#flashMode=" + flashMode);
        
        List<String> supportedFlash = mParameters.getSupportedFlashModes();
        if (isSupported(flashMode, supportedFlash)) {
        	Log.d(TAG,"###YUHUAN###forceFlashOffIfSupported#flashMode1=" + flashMode);
            mParameters.setFlashMode(flashMode);
        } else {
            flashMode = mParameters.getFlashMode();
            Log.d(TAG,"###YUHUAN###forceFlashOffIfSupported#flashMode2=" + flashMode);
            if (flashMode == null) {
                flashMode = mActivity.getString(
                        R.string.pref_camera_flashmode_no_flash);
                Log.d(TAG,"###YUHUAN###forceFlashOffIfSupported#flashMode3=" + flashMode);
            }
        }
    }

    /**
     * Used to update the flash mode. Video mode can turn on the flash as torch
     * mode, which we would like to turn on and off when we switching in and
     * out to the preview.
     *
     * @param forceOff whether we want to force the flash off.
     */
    private void forceFlashOff(boolean forceOff) {
        if (!mPreviewing || mParameters.getFlashMode() == null) {
            return;
        }
        forceFlashOffIfSupported(forceOff);
        mCameraDevice.setParameters(mParameters);
        mUI.updateOnScreenIndicators(mParameters, mPreferences);
    }

    @Override
    public void onPreviewFocusChanged(boolean previewFocused) {
        mUI.onPreviewFocusChanged(previewFocused);
        forceFlashOff(!previewFocused);
        mPreviewFocused = previewFocused;
    }

    @Override
    public boolean arePreviewControlsVisible() {
        return mUI.arePreviewControlsVisible();
    }

    private final class JpegPictureCallback implements CameraPictureCallback {
        Location mLocation;

        public JpegPictureCallback(Location loc) {
            mLocation = loc;
        }

        @Override
        public void onPictureTaken(byte [] jpegData, CameraProxy camera) {
            Log.v(TAG, "onPictureTaken");
            if(!mSnapshotInProgress || mPaused || mCameraDevice == null) return;
            mSnapshotInProgress = false;
            showVideoSnapshotUI(false);
            storeImage(jpegData, mLocation);
        }
    }

    private void storeImage(final byte[] data, Location loc) {
        long dateTaken = System.currentTimeMillis();
        String title = CameraUtil.createJpegName(dateTaken);
        ExifInterface exif = Exif.getExif(data);
        int orientation = Exif.getOrientation(exif);
        Size s = mParameters.getPictureSize();
        BitmapFactory.Options justBoundsOpts = new BitmapFactory.Options();
        justBoundsOpts.inJustDecodeBounds = true;
        Bitmap tmpBitmap = BitmapFactory.decodeByteArray(data,0,data.length, justBoundsOpts);
        int decodedWidth=s.width;
        int decodedHeight=s.height;
        if (justBoundsOpts.outWidth > 0 && justBoundsOpts.outHeight > 0) {
            decodedWidth = justBoundsOpts.outWidth;
            decodedHeight = justBoundsOpts.outHeight;
        }
        mActivity.getMediaSaveService().addImage(
                data, title, dateTaken, loc, decodedWidth, decodedHeight, orientation,
                exif, mOnPhotoSavedListener, mContentResolver,
                PhotoModule.PIXEL_FORMAT_JPEG);
    }

    private String convertOutputFormatToMimeType(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return "video/mp4";
        }
        return "video/3gpp";
    }

    private String convertOutputFormatToFileExt(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return ".mp4";
        }
        return ".3gp";
    }

    private void closeVideoFileDescriptor() {
        if (mVideoFileDescriptor != null) {
            try {
                mVideoFileDescriptor.close();
            } catch (IOException e) {
                Log.e(TAG, "Fail to close fd", e);
            }
            mVideoFileDescriptor = null;
        }
    }

    private void showTapToSnapshotToast() {
        new RotateTextToast(mActivity, R.string.video_snapshot_hint, 0)
                .show();
        // Clear the preference.
        Editor editor = mPreferences.edit();
        editor.putBoolean(CameraSettings.KEY_VIDEO_FIRST_USE_HINT_SHOWN, false);
        editor.apply();
    }

    @Override
    public boolean updateStorageHintOnResume() {
        return true;
    }

    // required by OnPreferenceChangedListener
    @Override
    public void onCameraPickerClicked(int cameraId) {
        mPendingSwitchCameraId = cameraId;
        Log.d(TAG, "Start to copy texture.");
        // We need to keep a preview frame for the animation before
        // releasing the camera. This will trigger onPreviewTextureCopied.
        // TODO: ((CameraScreenNail) mActivity.mCameraScreenNail).copyTexture();
        // Disable all camera controls.
        mSwitchingCamera = true;
        switchCamera();

    }

    @Override
    public boolean onCameraSwitched() {
        if (mPaused || mPendingSwitchCameraId != -1) return false;

        ListPreference pref = mPreferenceGroup.findPreference(CameraSettings.KEY_CAMERA_ID);
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
            return true;
        }
        return false;
    }
    @Override
    public boolean onFlashPicked() {
        if (mPaused) return false;
        setCameraParameters();
        return true;
    }
    @Override
    public void onShowSwitcherPopup() {
        mUI.onShowSwitcherPopup();
    }

    @Override
    public void onMediaSaveServiceConnected(MediaSaveService s) {
        // do nothing.
    }

    @Override
    public void onPreviewUIReady() {
        startPreview();
        if(!mActivity.isNonePickIntent()){
        	return;
        }
    }

    @Override
    public void onPreviewUIDestroyed() {
        stopPreview();
    }

    //wxt add
    public void resetToDefault() {
       ;
   }

    
	@Override
	public void onFocusChanged(float xCord, float yCord) {
		// TODO Auto-generated method stub
		mCameraDevice.cancelAutoFocus();
		mCameraDevice.autoFocus(mHandler, mAutoFocusCallback);
	}

    private final class AutoFocusCallback implements CameraAFCallback {
        @Override
        public void onAutoFocus(boolean focused, CameraProxy camera) {
            mUI.onAutoFocusCompleted(focused);
            // new requirement,touch focus at first time,then change to continous focus
//            if (!Parameters.FOCUS_MODE_CONTINUOUS_VIDEO.equals(mParameters.getFocusMode())) {
//                mHandler.sendEmptyMessageDelayed(MSG_CHANGE_FOCUS, CHANGE_FOCUS_DELAY);
//            }
        }
    }

	@Override
	public void setStartPreviewCallback(IStartPreviewCallback cb) {
		// TODO Auto-generated method stub
		
	}
}
