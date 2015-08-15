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
/* ============================================================================ */
/*     Modifications on Features list / Changes Request / Problems Report       */
/* ----------|----------------------|----------------------|------------------- */
/*    date   |        Author        |         Key          |     comment        */
/* ----------|----------------------|----------------------|------------------- */
/* 2014-08-05|    jian.zhang1       |     PR754915         |-Distinguish the    */
/*           |                      |                      |-default storage    */
/* ----------|----------------------|----------------------|------------------- */
/* 2014-08-07|    chao.luo          |     PR744625         |Burst shooting      */
/*           |                      |                      |-repeatly,Camera    */
/*           |                      |                      |-have been in saving*/
/*           |                      |                      |-pictures screen.   */
/* ----------|----------------------|----------------------|------------------- */
/* 2014-09-02|    xianzhong.zhang   |     PR773694         |[Gallery]Undo delete*/
/*           |                      |                      |-photos dialog box  */
/*           |                      |                      |-isn't pops up--    */
/* ----------|----------------------|----------------------|------------------- */
package com.android.camera;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;

import com.android.camera.CameraManager.CameraOpenErrorCallback;
import com.android.camera.CameraModule.IStartPreviewCallback;
import com.android.camera.app.AppManagerFactory;
import com.android.camera.app.OrientationManager;
import com.android.camera.app.PanoramaStitchingManager;
import com.android.camera.app.PlaceholderManager;
import com.android.camera.crop.CropActivity;
import com.android.camera.custom.CustomUtil;
import com.android.camera.data.CameraDataAdapter;
import com.android.camera.data.CameraPreviewData;
import com.android.camera.data.FixedFirstDataAdapter;
import com.android.camera.data.FixedLastDataAdapter;
import com.android.camera.data.InProgressDataWrapper;
import com.android.camera.data.LocalData;
import com.android.camera.data.LocalDataAdapter;
import com.android.camera.data.LocalMediaObserver;
import com.android.camera.data.MediaDetails;
import com.android.camera.data.SimpleViewData;
import com.android.camera.effects.RSHelper;
import com.android.camera.manager.BackButtonManager;
import com.android.camera.manager.CameraViewManager;
import com.android.camera.manager.DialogRunnable;
import com.android.camera.manager.InfoManager;
import com.android.camera.manager.ShutterManager;
import com.android.camera.tinyplanet.TinyPlanetFragment;
import com.android.camera.ui.CameraControls;
import com.android.camera.ui.DetailsDialog;
import com.android.camera.ui.FilmStripGestureRecognizer;
import com.android.camera.ui.FilmStripView;
import com.android.camera.ui.ModuleSwitcher;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ui.RotateLayout;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.GcamHelper;
import com.android.camera.util.IntentHelper;
import com.android.camera.util.PhotoSphereHelper;
import com.android.camera.util.PhotoSphereHelper.PanoramaViewHelper;
import com.android.camera.util.UsageStatistics;
import com.jrdcamera.modeview.DrawLayout;
import com.jrdcamera.modeview.MenuDrawable;
import com.jrdcamera.modeview.ModeListView;
import com.tct.camera.R;
import com.tct.jni.SceneDetectorLib;

public class CameraActivity extends Activity
        implements ModuleSwitcher.ModuleSwitchListener,
        ActionBar.OnMenuVisibilityListener,
        ShareActionProvider.OnShareTargetSelectedListener {

    private static final String TAG = "YUHUAN_CameraActivity";

    private static final String INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE =
            "android.media.action.STILL_IMAGE_CAMERA_SECURE";
    public static final String ACTION_IMAGE_CAPTURE_SECURE =
            "android.media.action.IMAGE_CAPTURE_SECURE";
    public static final String ACTION_TRIM_VIDEO =
            "com.android.camera.action.TRIM";
    public static final String MEDIA_ITEM_PATH = "media-item-path";
    
    public static final boolean gNeedLowerNight=false;
    
    public static int MAX_SCALE=4;
    
    // The intent extra for camera from secure lock screen. True if the gallery
    // should only show newly captured pictures. sSecureAlbumId does not
    // increment. This is used when switching between camera, camcorder, and
    // panorama. If the extra is not set, it is in the normal camera mode.
    public static final String SECURE_CAMERA_EXTRA = "secure_camera";

    private static final String EXTRA_QUICK_CAPTURE = "android.intent.extra.quickCapture";
    private static final String EXTRA_VIDEO_WALLPAPER_IDENTIFY = "identity";//String
    private static final String EXTRA_VIDEO_WALLPAPER_RATION = "ratio";//float
    private static final String EXTRA_VIDEO_WALLPAPER_IDENTIFY_VALUE = "com.mediatek.vlw";

    private static final float WALLPAPER_DEFAULT_ASPECTIO = 1.2f;
    private float mWallpaperAspectio;

    private static final int PICK_TYPE_NORMAL = 0;
    private static final int PICK_TYPE_PHOTO = 1;
    private static final int PICK_TYPE_VIDEO = 2;
    private static final int PICK_TYPE_WALLPAPER = 3;
    private int mPickType;

    /**
     * Request code from an activity we started that indicated that we do not
     * want to reset the view to the preview in onResume.
     */
    public static final int REQ_CODE_DONT_SWITCH_TO_PREVIEW = 142;
    //[FEATURE]-Add BEGIN by TCTNB,xutao.wu, 2014-07-24, PR635962
    public static final int REQ_CODE_GO_TO_FILTER = 143;
    public static final int REQ_CODE_BACK_FROM_FILTER_BACK_KEY = 144;
    public static final int REQ_CODE_BACK_TO_VIEWFINDER=145;//PR932774-sichao.hu added
    private Bitmap mLastThumbnailBitmap = null;
    //[FEATURE]-Add END by TCTNB,xutao.wu, 2014-07-24, PR635962
    public static final String TCT_SAVE_DONE_ACTION = "com.tct.camera.SAVE_DONE";
    public static final int REQ_CODE_GCAM_DEBUG_POSTCAPTURE = 999;

    private static final int HIDE_ACTION_BAR = 1;
    private static final long SHOW_ACTION_BAR_TIMEOUT_MS = 3000;

    /** Whether onResume should reset the view to the preview. */
    private boolean mResetToPreviewOnResume = true;

    // Supported operations at FilmStripView. Different data has different
    // set of supported operations.
    private static final int SUPPORT_DELETE = 1 << 0;
    private static final int SUPPORT_ROTATE = 1 << 1;
    private static final int SUPPORT_INFO = 1 << 2;
    private static final int SUPPORT_CROP = 1 << 3;
    private static final int SUPPORT_SETAS = 1 << 4;
    private static final int SUPPORT_EDIT = 1 << 5;
    private static final int SUPPORT_TRIM = 1 << 6;
    private static final int SUPPORT_SHARE = 1 << 7;
    private static final int SUPPORT_SHARE_PANORAMA360 = 1 << 8;
    private static final int SUPPORT_SHOW_ON_MAP = 1 << 9;
    private static final int SUPPORT_ALL = 0xffffffff;

    private static final int REQUEST_QRCODE_PICK_IMAGE = 1001;

    /** This data adapter is used by FilmStripView. */
    private LocalDataAdapter mDataAdapter;
    /** This data adapter represents the real local camera data. */
    private LocalDataAdapter mWrappedDataAdapter;

    private PanoramaStitchingManager mPanoramaManager;
    private PlaceholderManager mPlaceholderManager;
    private int mCurrentModuleIndex;
    private CameraModule mCurrentModule;
    private FrameLayout mAboveFilmstripControlLayout;
    private View mCameraModuleRootView;
    protected FilmStripView mFilmStripView;
    private ProgressBar mBottomProgress;
    private View mPanoStitchingPanel;
    private int mResultCodeForTesting;
    private Intent mResultDataForTesting;
    private Intent mCurIntent;
    private OnScreenHint mStorageHint;
    private Dialog mDetailsDialog;
    private long mStorageSpaceBytes = Storage.LOW_STORAGE_THRESHOLD_BYTES;
    private boolean mAutoRotateScreen;
    private boolean mSecureCamera;
    // This is a hack to speed up the start of SecureCamera.
    private static boolean sFirstStartAfterScreenOn = true;
    private int mLastRawOrientation=0;
    private MyOrientationEventListener mOrientationListener;
    private OrientationManager mOrientationManager;
    private Handler mMainHandler;
    private PanoramaViewHelper mPanoramaViewHelper;
    private CameraPreviewData mCameraPreviewData;
    private ActionBar mActionBar;
    private OnActionBarVisibilityListener mOnActionBarVisibilityListener = null;
    private Menu mActionBarMenu;
    private ViewGroup mUndoDeletionBar;
    private boolean mIsUndoingDeletion = false;
    private boolean mIsEditActivityInProgress = false;

    private Uri[] mNfcPushUris = new Uri[1];

    private ShareActionProvider mStandardShareActionProvider;
    private Intent mStandardShareIntent;
    private ShareActionProvider mPanoramaShareActionProvider;
    private Intent mPanoramaShareIntent;
    private LocalMediaObserver mLocalImagesObserver;
    private LocalMediaObserver mLocalVideosObserver;
    protected CameraManager.CameraProxy mCameraDevice;

    private final int DEFAULT_SYSTEM_UI_VISIBILITY = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
    private boolean mPendingDeletion = false;

    private Intent mVideoShareIntent;
    private Intent mImageShareIntent;

    public static final int VIEW_LAYER_ROOT = 0;
    public static final int VIEW_LAYER_BOTTOM = 1;
    public static final int VIEW_LAYER_NORMAL = 2;
    public static final int VIEW_LAYER_TOP = 3;
    public static final int VIEW_LAYER_SETTING = 4;
    public static final int VIEW_LAYER_PREVIEW = 5;
    public static final int VIEW_LAYER_DIALOG = 6;

    private static final int DISMIS_PROGRESS                = 3;
    //[BUGFIX]-Add by TCTNB,chao.luo, 2014-08-07,PR744625 Begin
    private static final int SHOW_PROGRESS = 4;
    //[BUGFIX]-Add by TCTNB,chao.luo, 2014-08-07,PR744625 End
    //modify by minghui.hua for PR920518,922873
    private static final int HIDE_NEVIGATION = 5;
    private static final long HIDE_NEVIGATION_TIMEOUT_MS = 2000;
    private static final int CLEAR_SCREEN_DELAY = 6;

    public List<Integer> mZoomRatios;
    public boolean mIsZooming = false;
    
    public final boolean gIsNightMode=false;
    
    public boolean gIsASD=false;
    
    public boolean gIsFaceBeauty=false;
    
    public static final int MAX_NIGHT_SHOT=6;

    public long pubStorageSpaceBytes;

    private ViewGroup mViewLayerRoot;
    private ViewGroup mViewLayerBottom;
    private ViewGroup mViewLayerNormal;
    private ViewGroup mViewLayerTop;
    private ViewGroup mViewLayerSetting;
    private ViewGroup mViewLayerExpression;
    private ViewGroup mViewLayerPreview;
    private ViewGroup mViewLayerDialog;


    private CameraViewManager mCameraViewManager;
    private GC mGC;
    private int mCameraId;
 // [BUGFIX]-ADD-BEGIN by yongsheng.shan, PR-907303, 2015-01-26 
    private int NOTIFY_MODE_CHANGED_NEW_MODE ;
 // [BUGFIX]-ADD-END by yongsheng.shan, PR-907303, 2015-01-26 
    private CameraReceiver mCameraReceiver;
    private IntentFilter myfilter;
    private SDCardReceiver mSDCardReceiver;
    private IntentFilter mSDfilter;
    private int mViewState;
    
//Add by fei.gao for Camera_animation dev -begin
    private ModeListView modeList;
    private LinearLayout modelistparent;
    private MenuDrawable mMenuDrawable;
    private DrawLayout mDrawLayout;
    private RotateImageView mBack;
    private LinearLayout mModeSettings;
	 private RotateImageView mModeSettings_image;
    private RotateLayout mModeSettings_text;
    private RotateLayout mAsdTitleBar;
    private boolean isShowNavigationBar=false;
    public boolean mIsModuleSwitching=false;
    public RSHelper gRSHelper;
//Add by fei.gao for Camera_animation dev -end

    // [BUGFIX]-ADD-BEGIN by xuan.zhou, PR-695166, 2014-6-23
    private static final String TCT_FACE_UNLOCK = "com.tct.camera.face_unlock_stop";
    boolean isFaceLockBlocked = false;
    boolean isResumed = false;
    // [BUGFIX]-ADD-END by xuan.zhou, PR-695166, 2014-6-23
    
    private boolean isLockDrawer =false;
    
    private static boolean SYSTEM_ROTATE=false;
//    private boolean hadRotate=false;
    private boolean HasChangeReversible=false;
    
    
    private boolean mDisableOrientation=false;
 
    public boolean isInAsd(){
    	return gIsASD;
    }
    
    public boolean isInFacebeauty(){
    	return gIsFaceBeauty;
    }
    
    private double mCurrentTime=0;
    static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    public static void TraceLog(String tag,String msg){
    	TraceLog(tag,msg,null);
    }
    
    public static void TraceLog(String tag,String msg,Throwable th){
//    	StringBuilder sb=new StringBuilder("At "+df.format(new Date()));
//    	sb.append(" ");
//    	sb.append(msg);
    	if(th==null)
    		Log.w(tag, msg);
    	else
    		Log.w(tag, msg,th);
    }
    
    public static void TraceQCTLog(String msg){
    	long pid=Thread.currentThread().getId();
    	Log.w("QCT", msg+" at thread "+pid); 
    }
    
    public static void TraceQCTLog(String msg,Throwable th){
    	long pid=Thread.currentThread().getId();
    	Log.w("QCT", msg+" at thread "+pid,th); 
    }
    
    private boolean isProgressShow;
    private boolean isResetToDefault = false ;
    
    public void resetToDefault() {
    	isResetToDefault = true ;
        // Reset to default first for camera functions.
        mCurrentModule.resetToDefault();
        // Reset to default finally for camera UI.
        mCameraViewManager.resetToDefault();
        disableNavigationBar();
        enableDrawer();
        isResetToDefault = false ;
    }

    public boolean isResetToDefault(){
    	return isResetToDefault;
    }
    
    public int getOrientation(){
    	return mOrientation;
    }
    
    public int getLastOrientation(){
    	return mLastRawOrientation;
    }
    
    private int mOrientation=0;
    
    public boolean isReversibleEnabled(){
    	return false;
    }
    

    private boolean mIsBatterySaverOn=false;
    public boolean isBatterySaverEnabled(){
        
        return mIsBatterySaverOn;
    }
    
    private void updateBatterySaverState(){
        int isBatterySaverOn=Settings.Global.getInt(getContentResolver(), "low_power",0);//Read Battery Saver mode state
        mIsBatterySaverOn=isBatterySaverOn==1;
    }
    
    private class MyOrientationEventListener
            extends OrientationEventListener {
        public MyOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // We keep the last known orientation. So if the user first orient
            // the camera then point the camera to floor or sky, we still have
            // the correct orientation.
        	
        	
            if (orientation == ORIENTATION_UNKNOWN) {
                return;
            }
            
            if(mOrientationManager!=null){
            	if(mOrientationManager.isOrientationLocked()){
            		return;
            	}
            }
            
            orientation = CameraUtil.roundOrientation(orientation, mLastRawOrientation);
            mOrientation=orientation;
            
            mCurrentModule.onOrientationChanged(orientation);
            
//            if(Settings.Global.getInt(getContentResolver(), "degree_rotation" ,0) == 1){
//					if(orientation == 0){
//						hadRotate=false;
//					}else if(orientation == 180){
//						hadRotate =true;
//					}
//					
//            if(hadRotate){
//					switch (orientation){
//					case 0:
//						orientation=180;
//						break;
//					case 90:
//							orientation=270;
//						break;
//					case 180:
//							orientation =0;
//						break;
//					case 270:
//							orientation=90;
//						break;
//					}
//            }
					
//				}
//            if(orientation != mLastRawOrientation){
//                Log.d(TAG, "onOrientation changed, orientation="+orientation);
//            }
            mLastRawOrientation = orientation;
            mFilmStripView.onOrientationChanged(orientation,false);
            if(mDisableOrientation){
            	return;
            }
        	
            
            mGC.setOrientation(mCameraModuleRootView,orientation,true);
            
            mBack.setOrientation(orientation, true);
            mModeSettings_image.setOrientation(orientation, true);
            mModeSettings_text.setOrientation(orientation, true);
            mMenuDrawable.setOrientation(orientation,true);
//            mAsdTitleBar.setOrientation(orientation, true);
            if (mCameraViewManager != null){
                mCameraViewManager.onOrientationChanged(orientation);
            }
            
            
            
        }
    }

    private MediaSaveService mMediaSaveService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder b) {
            mMediaSaveService = ((MediaSaveService.LocalBinder) b).getService();
            mCurrentModule.onMediaSaveServiceConnected(mMediaSaveService);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            if (mMediaSaveService != null) {
                mMediaSaveService.setListener(null);
                mMediaSaveService = null;
            }
        }
    };

    private CameraOpenErrorCallback mCameraOpenErrorCallback =
            new CameraOpenErrorCallback() {
                @Override
                public void onCameraDisabled(int cameraId) {
                    UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
                            UsageStatistics.ACTION_OPEN_FAIL, "security");

                    CameraUtil.showErrorAndFinish(CameraActivity.this,
                            R.string.camera_disabled);
                }

                @Override
                public void onDeviceOpenFailure(int cameraId) {
                    UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
                            UsageStatistics.ACTION_OPEN_FAIL, "open");

                    CameraUtil.showErrorAndFinish(CameraActivity.this,
                            R.string.cannot_connect_camera);
                }

                @Override
                public void onReconnectionFailure(CameraManager mgr) {
                    UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
                            UsageStatistics.ACTION_OPEN_FAIL, "reconnect");

                    CameraUtil.showErrorAndFinish(CameraActivity.this,
                            R.string.cannot_connect_camera);
                }
            };

    // close activity when screen turns off
    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    private static BroadcastReceiver sScreenOffReceiver;

    private static class ScreenOffReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            sFirstStartAfterScreenOn = true;
        }
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case HIDE_ACTION_BAR:
                    removeMessages(HIDE_ACTION_BAR);
                    CameraActivity.this.setSystemBarsVisibility(false);
                    break;
                case DISMIS_PROGRESS:
                    dismissProgress();
                    if(isInCamera()){
                        enableDrawer();
                    }
                    break;
                //[BUGFIX]-Add by TCTNB,chao.luo, 2014-08-07,PR744625 Begin
                case SHOW_PROGRESS:
                    String desStr = (String)msg.obj;
                    showProgress(desStr);
                    break;
                //[BUGFIX]-Add by TCTNB,chao.luo, 2014-08-07,PR744625 End
                case HIDE_NEVIGATION:
                    forceDisableNavigationBar();
                    break;
                case CLEAR_SCREEN_DELAY:
                    getWindow().clearFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
            }
        }
    }

    public interface OnActionBarVisibilityListener {
        public void onActionBarVisibilityChanged(boolean isVisible);
    }

    public void setOnActionBarVisibilityListener(OnActionBarVisibilityListener listener) {
        mOnActionBarVisibilityListener = listener;
    }

    public static boolean isFirstStartAfterScreenOn() {
        return sFirstStartAfterScreenOn;
    }
    public boolean isInCamera() {
//        Exception e = new Exception();
//        e.printStackTrace();
        return mFilmStripView.inCameraFullscreen();
    }
    
    //Add by fei.gao for PR964866 -begin
    public boolean judgeIsOnDownInCamera(){
    	return  mFilmStripView.isCameraPreview();
    }
    
    public boolean isDownInCameraPreView(){
    	return mFilmStripView.isDownInCameraPreview();
    }
    //Add by fei.gao for PR964866 -end
    public void onSingleTapUp(View view, int x, int y) {
        setViewState(GC.VIEW_STATE_BLANK);
        mCurrentModule.onSingleTapUp(view, x, y);
    }

    public static void resetFirstStartAfterScreenOn() {
        sFirstStartAfterScreenOn = false;
    }

    private String fileNameFromDataID(int dataID) {
        final LocalData localData = mDataAdapter.getLocalData(dataID);
        
        File localFile = new File(localData.getPath());
        return localFile.getName();
    }
    
    private FilmStripView.Listener mFilmStripListener =
            new FilmStripView.Listener() {
                @Override
                public void onDataPromoted(int dataID) {
                    UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
                            UsageStatistics.ACTION_DELETE, "promoted", 0,
                            UsageStatistics.hashFileName(fileNameFromDataID(dataID)));

                    removeData(dataID);
                }

                @Override
                public void onDataDemoted(int dataID) {
                    UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
                            UsageStatistics.ACTION_DELETE, "demoted", 0,
                            UsageStatistics.hashFileName(fileNameFromDataID(dataID)));

                    removeData(dataID);
                }

                
                private boolean needBackToFB=false;
                @Override
                public void onDataFullScreenChange(int dataID, boolean full) {
                    boolean isCameraID = isCameraPreview(dataID);
                    
                    
                    if (full && isCameraID && CameraActivity.this.hasWindowFocus()){
                        updateStorageSpaceAndHint();
                    }
                    if (!isCameraID) {
                        if (!full) {
                            // Always show action bar in filmstrip mode
                            CameraActivity.this.setSystemBarsVisibility(true, false);
                        } else if (mActionBar.isShowing()) {
                            // Hide action bar after time out in full screen mode
                            mMainHandler.sendEmptyMessageDelayed(HIDE_ACTION_BAR,
                                    SHOW_ACTION_BAR_TIMEOUT_MS);
                        }
                    }
                }

                /**
                 * Check if the local data corresponding to dataID is the camera
                 * preview.
                 *
                 * @param dataID the ID of the local data
                 * @return true if the local data is not null and it is the
                 *         camera preview.
                 */
                private boolean isCameraPreview(int dataID) {
                    LocalData localData = mDataAdapter.getLocalData(dataID);
                    if (localData == null) {
                        Log.w(TAG, "Current data ID not found.");
                        return false;
                    }
                    return localData.getLocalDataType() == LocalData.LOCAL_CAMERA_PREVIEW;
                }

                @Override
                public void onReload() {
                    setPreviewControlsVisibility(true);
                    CameraActivity.this.setSystemBarsVisibility(false);
                }

                @Override
                public void onCurrentDataCentered(int dataID) {
                	if (dataID != 0 && !mFilmStripView.isCameraPreview() && !mFilmStripView.inFullScreen()) {
                        // For now, We ignore all items that are not the camera preview.
                		return;
                    }
                    if(!arePreviewControlsVisible()) {
                        setPreviewControlsVisibility(true);
                        CameraActivity.this.setSystemBarsVisibility(false);
                    }
                }

                @Override
                public void onCurrentDataOffCentered(int dataID) {
                    if (dataID != 0 && !mFilmStripView.isCameraPreview()) {
                        // For now, We ignore all items that are not the camera preview.
                        return;
                    }

                    if (arePreviewControlsVisible()) {
                        setPreviewControlsVisibility(false);
                    }
                }

                @Override
                public void onDataFocusChanged(final int dataID, final boolean focused) {
                    boolean isPreview = isCameraPreview(dataID);
                    boolean isFullScreen = mFilmStripView.inFullScreen();
                    if (isPreview) {
                        mMainHandler.removeMessages(CLEAR_SCREEN_DELAY);
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }else {
                        mMainHandler.removeMessages(CLEAR_SCREEN_DELAY);
                        mMainHandler.sendEmptyMessageDelayed(CLEAR_SCREEN_DELAY,
                                PhotoModule.SCREEN_DELAY);
                    }
                    if (isFullScreen && isPreview && CameraActivity.this.hasWindowFocus()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateStorageSpaceAndHint();
                            }
                        });
                    }
                    // Delay hiding action bar if there is any user interaction
                    if (mMainHandler.hasMessages(HIDE_ACTION_BAR)) {
                        mMainHandler.removeMessages(HIDE_ACTION_BAR);
                        mMainHandler.sendEmptyMessageDelayed(HIDE_ACTION_BAR,
                                SHOW_ACTION_BAR_TIMEOUT_MS);
                    }
                    // TODO: This callback is UI event callback, should always
                    // happen on UI thread. Find the reason for this
                    // runOnUiThread() and fix it.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LocalData currentData = mDataAdapter.getLocalData(dataID);
                            if (currentData == null) {
                                Log.w(TAG, "Current data ID not found.");
                                hidePanoStitchingProgress();
                                return;
                            }
                            boolean isCameraID = currentData.getLocalDataType() ==
                                    LocalData.LOCAL_CAMERA_PREVIEW;
                            if (!focused) {
                                if (isCameraID) {
                                    mCurrentModule.onPreviewFocusChanged(false);
                                    CameraActivity.this.setSystemBarsVisibility(true);
                                }
                                hidePanoStitchingProgress();
                            } else {
                                if (isCameraID) {
                                    // Don't show the action bar in Camera
                                    // preview.
                                    CameraActivity.this.setSystemBarsVisibility(false);

                                    if (mPendingDeletion) {
                                        performDeletion();
                                    }
                                } else {
                                	//pr948256-yuguan.chen modified begin
                                	mMainHandler.post(new Runnable(){
                                		@Override
                                		public void run(){
                                			updateActionBarMenu(dataID);
                                		}
                                	});
                                	//pr948256-yuguan.chen modified end
                                }

                                Uri contentUri = currentData.getContentUri();
                                if (contentUri == null) {
                                    hidePanoStitchingProgress();
                                    return;
                                }
                                int panoStitchingProgress = mPanoramaManager.getTaskProgress(
                                        contentUri);
                                if (panoStitchingProgress < 0) {
                                    hidePanoStitchingProgress();
                                    return;
                                }
                                showPanoStitchingProgress();
                                updateStitchingProgress(panoStitchingProgress);
                            }
                        }
                    });
                }

                @Override
                public void onToggleSystemDecorsVisibility(int dataID) {
                    // If action bar is showing, hide it immediately, otherwise
                    // show action bar and hide it later
                    if (mActionBar.isShowing()) {
                        CameraActivity.this.setSystemBarsVisibility(false);
                    } else {
                        // Don't show the action bar if that is the camera preview.
                        boolean isCameraID = isCameraPreview(dataID);
                        if (!isCameraID) {
                            CameraActivity.this.setSystemBarsVisibility(true, true);
                        }
                    }
                }

                @Override
                public void setSystemDecorsVisibility(boolean visible) {
                    CameraActivity.this.setSystemBarsVisibility(visible);
                }
            };

    public void gotoGallery() {
    	Log.e("846780", "gotoGallery");
        UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA, UsageStatistics.ACTION_FILMSTRIP,
                "thumbnailTap");

        mFilmStripView.getController().goToNextItem();
    }

    public void showInfo(final String text,boolean needUndo,boolean longDis) {
    	if(longDis){
    		showInfo(text, InfoManager.DELAY_MSG_SHOW_ONSCREEN_VIEW_LONG,needUndo);
    	}else{
    		showInfo(text, InfoManager.DELAY_MSG_SHOW_ONSCREEN_VIEW,needUndo);	
    	}
        
    }

    public void showInfo(final String text, int showMs,boolean needUndo) {
        mCameraViewManager.getInfoManager().showText(text,showMs,needUndo);
    }

    //[BUGFIX]-Add by TCTNB,chao.luo, 2014-08-07,PR744625 Begin
    public void sendShowProgressMessage(String msgStr) {
        TraceLog(TAG, "sendShowProgressMessage msgStr="+msgStr);
        Message msg = mMainHandler.obtainMessage();
        msg.what = SHOW_PROGRESS;
        msg.obj = msgStr;
        mMainHandler.sendMessage(msg);
        //mMainHandler.sendEmptyMessageDelayed(DISMIS_PROGRESS, 60*1000); // delayed 60 seconds.
    }
    //[BUGFIX]-Add by TCTNB,chao.luo, 2014-08-07,PR744625 End

    public void showProgress(String msg){
        isProgressShow = true;
        setViewState(GC.VIEW_STATE_SHUTTER_PRESS);
        mCameraViewManager.getRotateProgress().showProgress(msg, mOrientation);
    }

    //[BUGFIX]-Add by TCTNB,chao.luo, 2014-08-07,PR744625 Begin
    public void sendDismissProgressMessage() {
        //mMainHandler.removeMessages(DISMIS_PROGRESS);
        mMainHandler.sendEmptyMessageDelayed(DISMIS_PROGRESS, 3*1000);
    }
    //[BUGFIX]-Add by TCTNB,chao.luo, 2014-08-07,PR744625 End

    public void sendDismissProgressImediate(){
        mMainHandler.removeMessages(DISMIS_PROGRESS);
    	mMainHandler.sendEmptyMessage(DISMIS_PROGRESS);
    }

    public void removeHideMsg(){
        mMainHandler.removeMessages(HIDE_NEVIGATION);
    }

    /*when the dismiss message is coming but the progress is not show,
      we will wait for the progress show unless @removeDismissMessage
      is called by the code.
    */
    private void dismissProgress() {
        if(!isProgressShow) {
            //[BUGFIX]-Modify by TCTNB,chao.luo, 2014-08-07,PR744625 Begin
            //mMainHandler.removeMessages(DISMIS_PROGRESS);
            //mMainHandler.sendEmptyMessageDelayed(DISMIS_PROGRESS, 100);
            //[BUGFIX]-Modify by TCTNB,chao.luo, 2014-08-07,PR744625 End
            return;
        }
        isProgressShow = false;
        mCameraViewManager.getRotateProgress().hide();
        mCameraViewManager.setViewState(GC.VIEW_STATE_NORMAL);
        if (mCurrentModule instanceof PhotoModule)
            ((PhotoModule)mCurrentModule).burstshotSavedDone();
    }

    public Bitmap getLastBitmap() {
       try{
           mLastThumbnailBitmap.recycle();
           mLastThumbnailBitmap = null;
        }catch(Exception e){}
        Thumbnail result[] = new Thumbnail[1];
        ContentResolver resolver = getContentResolver();
        // Load the thumbnail from the media provider.
        mLastThumbnailBitmap = Thumbnail.getLastThumbnailFromContentResolver(
                resolver, result);
        return mLastThumbnailBitmap;
    }

    public Uri getLastBitmapUri() {
        Thumbnail result[] = new Thumbnail[1];
        ContentResolver resolver = getContentResolver();
        // Load the thumbnail from the media provider.
        return Thumbnail.getLastMediaUriFromContentResolver(
                resolver, result);
    }

    public ComboPreferences getPreferences() {
        return mCurrentModule.getPreferences();
    }
    
    // public boolean gIsIntentParsed=false;

    /**
     * If {@param visible} is false, this hides the action bar and switches the system UI
     * to lights-out mode.
     */
    // TODO: This should not be called outside of the activity.
    public void setSystemBarsVisibility(boolean visible) {
        setSystemBarsVisibility(visible, false);
    }

    /**
     * If {@param visible} is false, this hides the action bar and switches the
     * system UI to lights-out mode. If {@param hideLater} is true, a delayed message
     * will be sent after a timeout to hide the action bar.
     */
    private void setSystemBarsVisibility(boolean visible, boolean hideLater) {
        mMainHandler.removeMessages(HIDE_ACTION_BAR);

//        int currentSystemUIVisibility = mAboveFilmstripControlLayout.getSystemUiVisibility();
//        int newSystemUIVisibility = DEFAULT_SYSTEM_UI_VISIBILITY |
//                (visible ? View.SYSTEM_UI_FLAG_VISIBLE :
//                        View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN);
//        if (newSystemUIVisibility != currentSystemUIVisibility) {
//            mAboveFilmstripControlLayout.setSystemUiVisibility(newSystemUIVisibility);
//        }

        boolean currentActionBarVisibility = mActionBar.isShowing();
        if (visible != currentActionBarVisibility) {
            if (visible) {
                disableDrawer();
                mActionBar.show();
            } else {
                if (null != mFilmStripView && mFilmStripView.isCameraPreview() && mFilmStripView.inFullScreen() && !mFilmStripView.isOnDown()) {//// PR 938999 - Soar Gao modify
                	enableDrawer();
                }
                mActionBar.hide();
            }
        }
        //modify by minghui.hua for PR941360
        if (mOnActionBarVisibilityListener != null) {
            mOnActionBarVisibilityListener.onActionBarVisibilityChanged(visible);
            if (visible) {
                showNavigationBar();
            }else {
                forceDisableNavigationBar();
            }
        }

        // Now delay hiding the bars
        if (visible && hideLater) {
            mMainHandler.sendEmptyMessageDelayed(HIDE_ACTION_BAR, SHOW_ACTION_BAR_TIMEOUT_MS);
        }
    }

    private void hidePanoStitchingProgress() {
        mPanoStitchingPanel.setVisibility(View.GONE);
    }

    private void showPanoStitchingProgress() {
        mPanoStitchingPanel.setVisibility(View.VISIBLE);
    }

    private void updateStitchingProgress(int progress) {
        mBottomProgress.setProgress(progress);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setupNfcBeamPush() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(CameraActivity.this);
        if (adapter == null) {
            return;
        }

        if (!ApiHelper.HAS_SET_BEAM_PUSH_URIS) {
            // Disable beaming
            adapter.setNdefPushMessage(null, CameraActivity.this);
            return;
        }

        adapter.setBeamPushUris(null, CameraActivity.this);
        adapter.setBeamPushUrisCallback(new CreateBeamUrisCallback() {
            @Override
            public Uri[] createBeamUris(NfcEvent event) {
                return mNfcPushUris;
            }
        }, CameraActivity.this);
    }

    private void setNfcBeamPushUri(Uri uri) {
        mNfcPushUris[0] = uri;
    }

    private void setStandardShareIntent(Uri contentUri, String mimeType) {
        mStandardShareIntent = getShareIntentFromType(mimeType);
        if (mStandardShareIntent != null) {
            mStandardShareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            mStandardShareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (mStandardShareActionProvider != null) {
                mStandardShareActionProvider.setShareIntent(mStandardShareIntent);
            }
        }
    }

    /**
     * Get the share intent according to the mimeType
     *
     * @param mimeType The mimeType of current data.
     * @return the video/image's ShareIntent or null if mimeType is invalid.
     */
    private Intent getShareIntentFromType(String mimeType) {
        // Lazily create the intent object.
        if (mimeType.startsWith("video/")) {
            if (mVideoShareIntent == null) {
                mVideoShareIntent = new Intent(Intent.ACTION_SEND);
                mVideoShareIntent.setType("video/*");
            }
            return mVideoShareIntent;
        } else if (mimeType.startsWith("image/")) {
            if (mImageShareIntent == null) {
                mImageShareIntent = new Intent(Intent.ACTION_SEND);
                mImageShareIntent.setType("image/*");
            }
            return mImageShareIntent;
        }
        Log.w(TAG, "unsupported mimeType " + mimeType);
        return null;
    }

    private void setPanoramaShareIntent(Uri contentUri) {
        if (mPanoramaShareIntent == null) {
            mPanoramaShareIntent = new Intent(Intent.ACTION_SEND);
        }
        mPanoramaShareIntent.setType("application/vnd.google.panorama360+jpg");
        mPanoramaShareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        if (mPanoramaShareActionProvider != null) {
            mPanoramaShareActionProvider.setShareIntent(mPanoramaShareIntent);
        }
    }

    @Override
    public void onMenuVisibilityChanged(boolean isVisible) {
        // If menu is showing, we need to make sure action bar does not go away.
        mMainHandler.removeMessages(HIDE_ACTION_BAR);
        if (!isVisible) {
            mMainHandler.sendEmptyMessageDelayed(HIDE_ACTION_BAR, SHOW_ACTION_BAR_TIMEOUT_MS);
        }
    }

    @Override
    public boolean onShareTargetSelected(ShareActionProvider shareActionProvider, Intent intent) {
        int currentDataId = mFilmStripView.getCurrentId();
        if (currentDataId < 0) {
            return false;
        }
        UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA, UsageStatistics.ACTION_SHARE,
                intent.getComponent().getPackageName(), 0,
                UsageStatistics.hashFileName(fileNameFromDataID(currentDataId)));
    	// [BUGFIX]-ADD-BEGIN by yongsheng.shan, PR-908369, 2015-01-22
		mResetToPreviewOnResume = false;
		// [BUGFIX]-ADD-END by yongsheng.shan, PR-908369, 2015-01-22 
        return true;
    }

    /**
     * According to the data type, make the menu items for supported operations
     * visible.
     *
     * @param dataID the data ID of the current item.
     */
    private void updateActionBarMenu(int dataID) {
        LocalData currentData = mDataAdapter.getLocalData(dataID);
        if (currentData == null) {
            return;
        }
        int type = currentData.getLocalDataType();
        TraceLog(TAG, "updateActionBarMenu: dataID = " + dataID + " type = " + type);

        if (mActionBarMenu == null) {
            return;
        }

        int supported = 0;

        switch (type) {
            case LocalData.LOCAL_IMAGE:
                supported |= SUPPORT_DELETE | SUPPORT_ROTATE | SUPPORT_INFO
                        | SUPPORT_CROP | SUPPORT_SETAS | SUPPORT_EDIT
                        | SUPPORT_SHOW_ON_MAP;
                break;
            case LocalData.LOCAL_VIDEO:
                supported |= SUPPORT_DELETE | SUPPORT_INFO | SUPPORT_TRIM;
                break;
            case LocalData.LOCAL_PHOTO_SPHERE:
                supported |= SUPPORT_DELETE | SUPPORT_ROTATE | SUPPORT_INFO
                        | SUPPORT_CROP | SUPPORT_SETAS | SUPPORT_EDIT
                        | SUPPORT_SHARE | SUPPORT_SHOW_ON_MAP;
                break;
            case LocalData.LOCAL_360_PHOTO_SPHERE:
                supported |= SUPPORT_DELETE | SUPPORT_ROTATE | SUPPORT_INFO
                        | SUPPORT_CROP | SUPPORT_SETAS | SUPPORT_EDIT
                        | SUPPORT_SHARE | SUPPORT_SHARE_PANORAMA360
                        | SUPPORT_SHOW_ON_MAP;
                break;
            default:
                break;
        }

        // In secure camera mode, we only support delete operation.
        if (isSecureCamera()) {
            supported &= SUPPORT_DELETE;
        }

        setMenuItemVisible(mActionBarMenu, R.id.action_delete,
                (supported & SUPPORT_DELETE) != 0);
        setMenuItemVisible(mActionBarMenu, R.id.action_rotate_ccw,
                (supported & SUPPORT_ROTATE) != 0);
        setMenuItemVisible(mActionBarMenu, R.id.action_rotate_cw,
                (supported & SUPPORT_ROTATE) != 0);
        setMenuItemVisible(mActionBarMenu, R.id.action_details,
                (supported & SUPPORT_INFO) != 0);
        setMenuItemVisible(mActionBarMenu, R.id.action_crop,
                (supported & SUPPORT_CROP) != 0);
        setMenuItemVisible(mActionBarMenu, R.id.action_setas,
                (supported & SUPPORT_SETAS) != 0);
        setMenuItemVisible(mActionBarMenu, R.id.action_edit,
                (supported & SUPPORT_EDIT) != 0);
        setMenuItemVisible(mActionBarMenu, R.id.action_trim,
                (supported & SUPPORT_TRIM) != 0);

        boolean standardShare = (supported & SUPPORT_SHARE) != 0;
        boolean panoramaShare = (supported & SUPPORT_SHARE_PANORAMA360) != 0;
        setMenuItemVisible(mActionBarMenu, R.id.action_share, standardShare);
        setMenuItemVisible(mActionBarMenu, R.id.action_share_panorama, panoramaShare);

        if (panoramaShare) {
            // For 360 PhotoSphere, relegate standard share to the overflow menu
            MenuItem item = mActionBarMenu.findItem(R.id.action_share);
            if (item != null) {
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                item.setTitle(getResources().getString(R.string.share_as_photo));
            }
            // And, promote "share as panorama" to action bar
            item = mActionBarMenu.findItem(R.id.action_share_panorama);
            if (item != null) {
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
            setPanoramaShareIntent(currentData.getContentUri());
        }
        if (standardShare) {
            if (!panoramaShare) {
                MenuItem item = mActionBarMenu.findItem(R.id.action_share);
                if (item != null) {
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                    item.setTitle(getResources().getString(R.string.share));
                }
            }
            setStandardShareIntent(currentData.getContentUri(), currentData.getMimeType());
            setNfcBeamPushUri(currentData.getContentUri());
        }

        boolean itemHasLocation = currentData.getLatLong() != null;
        setMenuItemVisible(mActionBarMenu, R.id.action_show_on_map,
                itemHasLocation && (supported & SUPPORT_SHOW_ON_MAP) != 0);
    }

    private void setMenuItemVisible(Menu menu, int itemId, boolean visible) {
        MenuItem item = menu.findItem(itemId);
        if (item != null)
            item.setVisible(visible);
    }

    private ImageTaskManager.TaskListener mPlaceholderListener =
            new ImageTaskManager.TaskListener() {

                @Override
                public void onTaskQueued(String filePath, final Uri imageUri) {
                	Log.d(TAG,"###YUHUAN###mPlaceholderListener.onTaskQueued#filePath=" + filePath);
                	Log.d(TAG,"###YUHUAN###mPlaceholderListener.onTaskQueued#imageUri=" + imageUri.toString());
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyNewMedia(imageUri);
                            int dataID = mDataAdapter.findDataByContentUri(imageUri);
                            if (dataID != -1) {
                                LocalData d = mDataAdapter.getLocalData(dataID);
                                InProgressDataWrapper newData = new InProgressDataWrapper(d, true);
                                mDataAdapter.updateData(dataID, newData);
                            }
                        }
                    });
                }

                @Override
                public void onTaskDone(String filePath, final Uri imageUri) {
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mDataAdapter.refresh(getContentResolver(), imageUri);
                        }
                    });
                }

                @Override
                public void onTaskProgress(String filePath, Uri imageUri, int progress) {
                    // Do nothing
                }
    };

    private ImageTaskManager.TaskListener mStitchingListener =
            new ImageTaskManager.TaskListener() {
                @Override
                public void onTaskQueued(String filePath, final Uri imageUri) {
                	Log.d(TAG,"###YUHUAN###mStitchingListener.onTaskQueued#filePath=" + filePath);
                	Log.d(TAG,"###YUHUAN###mStitchingListener.onTaskQueued#imageUri=" + imageUri.toString());
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyNewMedia(imageUri);
                            int dataID = mDataAdapter.findDataByContentUri(imageUri);
                            if (dataID != -1) {
                                // Don't allow special UI actions (swipe to
                                // delete, for example) on in-progress data.
                                LocalData d = mDataAdapter.getLocalData(dataID);
                                InProgressDataWrapper newData = new InProgressDataWrapper(d);
                                mDataAdapter.updateData(dataID, newData);
                            }
                        }
                    });
                }

                @Override
                public void onTaskDone(String filePath, final Uri imageUri) {
                    Log.v(TAG, "onTaskDone:" + filePath);
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            int doneID = mDataAdapter.findDataByContentUri(imageUri);
                            int currentDataId = mFilmStripView.getCurrentId();

                            if (currentDataId == doneID) {
                                hidePanoStitchingProgress();
                                updateStitchingProgress(0);
                            }

                            mDataAdapter.refresh(getContentResolver(), imageUri);
                        }
                    });
                }

                @Override
                public void onTaskProgress(
                        String filePath, final Uri imageUri, final int progress) {
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            int currentDataId = mFilmStripView.getCurrentId();
                            if (currentDataId == -1) {
                                return;
                            }
                            if (imageUri.equals(
                                    mDataAdapter.getLocalData(currentDataId).getContentUri())) {
                                updateStitchingProgress(progress);
                            }
                        }
                    });
                }
            };

    public MediaSaveService getMediaSaveService() {
        return mMediaSaveService;
    }

    public void notifyNewMedia(Uri uri) {
        ContentResolver cr = getContentResolver();
        String mimeType = cr.getType(uri);
        
     // [BUGFIX]-MOD-BEGIN by yongsheng.shan, PR-960456, 2015-03-27 
        if(null == mimeType || mimeType.length() == 0)
        {
            android.util.Log.w(TAG, "The new media with mimeType == null " + ", uri:" + uri);
            return ;
        }
     // [BUGFIX]-MOD-END by yongsheng.shan, PR-960456, 2015-03-27 
        if (mimeType.startsWith("video/")) {
            sendBroadcast(new Intent(CameraUtil.ACTION_NEW_VIDEO, uri));
            mDataAdapter.addNewVideo(cr, uri);
        } else if (mimeType.startsWith("image/")) {
            CameraUtil.broadcastNewPicture(this, uri);
            mDataAdapter.addNewPhoto(cr, uri);
        } else if (mimeType.startsWith("application/stitching-preview")) {
            mDataAdapter.addNewPhoto(cr, uri);
        } else if (mimeType.startsWith(PlaceholderManager.PLACEHOLDER_MIME_TYPE)) {
            mDataAdapter.addNewPhoto(cr, uri);
        } else {
            android.util.Log.w(TAG, "Unknown new media with MIME type:"
                    + mimeType + ", uri:" + uri);
        }
    }

    private void removeData(int dataID) {
    	mDataAdapter.removeData(CameraActivity.this, dataID);
        //[BUGFIX] Modify by Xianzhong.Zhang for PR773694 begin 09/02/2014
        if (mDataAdapter.getTotalNumber() >= 2) {
        //[BUGFIX] Modify by Xianzhong.Zhang for PR773694 end 09/02/2014
            showUndoDeletionBar();
        } else {
            // If camera preview is the only view left in filmstrip,
            // no need to show undo bar.
            mPendingDeletion = true;
            performDeletion();
        	mFilmStripView.getController().goToFullScreen();
        }
    }

    /**
     * delete the photo immediately
     * @param dataID
     */
    public void removePhoto(int dataID) {
        mDataAdapter.removeData(CameraActivity.this, dataID);
        mPendingDeletion = true;
        performDeletion();
    }

    private void bindMediaSaveService() {
        Intent intent = new Intent(this, MediaSaveService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindMediaSaveService() {
        if (mConnection != null) {
            unbindService(mConnection);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.operations, menu);
        mActionBarMenu = menu;
        
        // Configure the standard share action provider
        MenuItem item = menu.findItem(R.id.action_share);
        mStandardShareActionProvider = (ShareActionProvider) item.getActionProvider();
        mStandardShareActionProvider.setShareHistoryFileName("standard_share_history.xml");
        if (mStandardShareIntent != null) {
            mStandardShareActionProvider.setShareIntent(mStandardShareIntent);
        }

        // Configure the panorama share action provider
        item = menu.findItem(R.id.action_share_panorama);
        mPanoramaShareActionProvider = (ShareActionProvider) item.getActionProvider();
        mPanoramaShareActionProvider.setShareHistoryFileName("panorama_share_history.xml");
        if (mPanoramaShareIntent != null) {
            mPanoramaShareActionProvider.setShareIntent(mPanoramaShareIntent);
        }

        mStandardShareActionProvider.setOnShareTargetSelectedListener(this);
        mPanoramaShareActionProvider.setOnShareTargetSelectedListener(this);

        return super.onCreateOptionsMenu(menu);
    }
    
    public void reloadZoom(){
    	mFilmStripView.getController().loadHyperImage();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(isInCamera()){
    		return true;
    	}
        int currentDataId = mFilmStripView.getCurrentId();
        if (currentDataId < 0) {
            return false;
        }
        final LocalData localData = mDataAdapter.getLocalData(currentDataId);
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_delete:
                /**
                 * for new design,the icon funtion change to launch Gllery,
                 * modify by minghui.hua for PR919347
                UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
                        UsageStatistics.ACTION_DELETE, null, 0,
                        UsageStatistics.hashFileName(fileNameFromDataID(currentDataId)));
                removeData(currentDataId);*/
                try {
                    //Add by zhimin.yu for PR945430
                    if(!(false == mGC.getCameraPreview() && isImageCaptureIntent())){
                         mCameraViewManager.getTopViewManager().hideSettingIndicator();
                    }
                    startActivityForResult(IntentHelper.getGalleryIntent(this),
                            REQ_CODE_BACK_TO_VIEWFINDER);
                } catch (ActivityNotFoundException e) {
                    Log.w(TAG, "Failed to launch gallery activity, closing");
                    finish();
                }
                return true;
            case R.id.action_edit:
                UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
                        UsageStatistics.ACTION_EDIT, null, 0,
                        UsageStatistics.hashFileName(fileNameFromDataID(currentDataId)));
                launchEditor(localData);
                return true;
            case R.id.action_trim: {
                // This is going to be handled by the Gallery app.
	                Intent intent = new Intent(ACTION_TRIM_VIDEO);
	                LocalData currentData = mDataAdapter.getLocalData(
	                        mFilmStripView.getCurrentId());
	                intent.setData(currentData.getContentUri());
	                // We need the file path to wrap this into a RandomAccessFile.
	                intent.putExtra(MEDIA_ITEM_PATH, currentData.getPath());
	                startActivityForResult(intent, REQ_CODE_DONT_SWITCH_TO_PREVIEW);
                return true;
            }
            case R.id.action_rotate_ccw:
            	mFilmStripView.clearZoom();
                localData.rotate90Degrees(this, mDataAdapter, currentDataId, false);
			if (mFilmStripView.isZoomOut()) {
				mFilmStripView.resetZoomView();
			}
                return true;
            case R.id.action_rotate_cw:
            	mFilmStripView.clearZoom();
                localData.rotate90Degrees(this, mDataAdapter, currentDataId, true);
			if (mFilmStripView.isZoomOut()) {
				mFilmStripView.resetZoomView();
			}
                return true;
            case R.id.action_crop: {
                UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
                        UsageStatistics.ACTION_CROP, null, 0,
                        UsageStatistics.hashFileName(fileNameFromDataID(currentDataId)));
                Intent intent = new Intent(CropActivity.CROP_ACTION);
                intent.setClass(this, CropActivity.class);
                intent.setDataAndType(localData.getContentUri(), localData.getMimeType())
                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(intent, REQ_CODE_DONT_SWITCH_TO_PREVIEW);
                return true;
            }
            case R.id.action_setas: {
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA)
                        .setDataAndType(localData.getContentUri(),
                                localData.getMimeType())
                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra("mimeType", intent.getType());
                startActivityForResult(Intent.createChooser(
                        intent, getString(R.string.set_as)), REQ_CODE_DONT_SWITCH_TO_PREVIEW);
                return true;
            }
            case R.id.action_details:
                (new AsyncTask<Void, Void, MediaDetails>() {
                    @Override
                    protected MediaDetails doInBackground(Void... params) {
                        return localData.getMediaDetails(CameraActivity.this);
                    }

                    @Override
                    protected void onPostExecute(MediaDetails mediaDetails) {
                        if (mediaDetails != null) {
                            // modify by minghui.hua for PR930597
                            DialogRunnable runnable = new DialogRunnable(){
                                @Override
                                public void run() {
                                    super.run();
                                    Log.d(TAG, "onPostExecute run");
                                    if(mFilmStripView.inFullScreen()){
                                        mMainHandler.sendEmptyMessageDelayed(HIDE_ACTION_BAR,
                                                SHOW_ACTION_BAR_TIMEOUT_MS);
                                    }
                                }
                            };
                            mDetailsDialog = DetailsDialog.create(CameraActivity.this, mediaDetails,runnable);
                            mDetailsDialog.show();
                            mMainHandler.removeMessages(HIDE_ACTION_BAR);
                        }
                    }
                }).execute();
                return true;
            case R.id.action_show_on_map:
                double[] latLong = localData.getLatLong();
                if (latLong != null) {
                    CameraUtil.showOnMap(this, latLong);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isCaptureIntent() {
        if (MediaStore.ACTION_VIDEO_CAPTURE.equals(getIntent().getAction())
                || MediaStore.ACTION_IMAGE_CAPTURE.equals(getIntent().getAction())
                || MediaStore.ACTION_IMAGE_CAPTURE_SECURE.equals(getIntent().getAction())) {
            return true;
        } else {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        TraceQCTLog("StartCamera");
        TraceLog(TAG,"StartCamera");
        // modify by minghui.hua for PR943681
        SDCard.instance().updateSDCardMountPath();
        /// modify by yaping.liu for pr966435 {
        Storage.setSaveSDCard(Storage.isSaveSDCard(), false);
        /// }

        GcamHelper.init(getContentResolver());
        // Intent intent = getIntent();
        mCurIntent = getIntent();
        parseIntent();
        CustomUtil cu = CustomUtil.getInstance(getApplicationContext());
        cu.setCustomFromSystem();
        mGC = new GC(this);
        mGC.setCameraPreview(true);
        init();
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_ACTION_BAR);
        WindowManager.LayoutParams params = window.getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        window.setAttributes(params);

        setContentView(R.layout.camera_filmstrip);
        mActionBar = getActionBar();
        mActionBar.addOnMenuVisibilityListener(this);
        // hiden the actionbar icon
        mActionBar.setDisplayShowHomeEnabled(false);
        
        /* [Custom] init the CustomUtil to customization : lijuan.zhang add 20141107 begin*/
        
        
        /* [Custom] init the CustomUtil to customization : lijuan.zhang add 20141107 end*/
        
        Log.d(TAG,"###YUHUAN###ApiHelper.HAS_ROTATION_ANIMATION=" + ApiHelper.HAS_ROTATION_ANIMATION);
        if (ApiHelper.HAS_ROTATION_ANIMATION) {
            setRotationAnimation();
        }

        mMainHandler = new MainHandler(getMainLooper());
        // Check if this is in the secure camera mode.
        String action = mCurIntent.getAction();
        if (INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(action)
                || ACTION_IMAGE_CAPTURE_SECURE.equals(action)) {
            mSecureCamera = true;
        } else {
            mSecureCamera = mCurIntent.getBooleanExtra(SECURE_CAMERA_EXTRA, false);
        }

        if (mSecureCamera) {
            // Change the window flags so that secure camera can show when locked
            params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
            window.setAttributes(params);

            // Filter for screen off so that we can finish secure camera activity
            // when screen is off.
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            registerReceiver(mScreenOffReceiver, filter);
            // TODO: This static screen off event receiver is a workaround to the
            // double onResume() invocation (onResume->onPause->onResume). We should
            // find a better solution to this.
            if (sScreenOffReceiver == null) {
                sScreenOffReceiver = new ScreenOffReceiver();
                registerReceiver(sScreenOffReceiver, filter);
            }
        }
        mAboveFilmstripControlLayout =
                (FrameLayout) findViewById(R.id.camera_above_filmstrip_layout);
        mAboveFilmstripControlLayout.setFitsSystemWindows(true);
        // Hide action bar first since we are in full screen mode first, and
        // switch the system UI to lights-out mode.
        this.setSystemBarsVisibility(false);
        mPanoramaManager = AppManagerFactory.getInstance(this)
                .getPanoramaStitchingManager();
        mPlaceholderManager = AppManagerFactory.getInstance(this)
                .getGcamProcessingManager();
        mPanoramaManager.addTaskListener(mStitchingListener);
        mPlaceholderManager.addTaskListener(mPlaceholderListener);
        LayoutInflater inflater = getLayoutInflater();
        View rootLayout = inflater.inflate(R.layout.camera, null, false);
        mCameraModuleRootView = rootLayout.findViewById(R.id.camera_app_root);
        mPanoStitchingPanel = findViewById(R.id.pano_stitching_progress_panel);
        mBottomProgress = (ProgressBar) findViewById(R.id.pano_stitching_progress_bar);
        mCameraPreviewData = new CameraPreviewData(rootLayout,
                FilmStripView.ImageData.SIZE_FULL,
                FilmStripView.ImageData.SIZE_FULL);
        // Put a CameraPreviewData at the first position.
        mWrappedDataAdapter = new FixedFirstDataAdapter(
                new CameraDataAdapter(new ColorDrawable(
                        getResources().getColor(R.color.photo_placeholder))),
                mCameraPreviewData);
        mFilmStripView = (FilmStripView) findViewById(R.id.filmstrip_view);
        mFilmStripView.setViewGap(
                getResources().getDimensionPixelSize(R.dimen.camera_film_strip_gap));
        mPanoramaViewHelper = new PanoramaViewHelper(this);
        mPanoramaViewHelper.onCreate();
        mFilmStripView.setPanoramaViewHelper(mPanoramaViewHelper);
        // Set up the camera preview first so the preview shows up ASAP.
        mFilmStripView.setListener(mFilmStripListener);

        int moduleIndex = -1;
        if (MediaStore.INTENT_ACTION_VIDEO_CAMERA.equals(action)
                || MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
            moduleIndex = ModuleSwitcher.VIDEO_MODULE_INDEX;
        } else if (MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA.equals(action)
                || MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(action)) {
            moduleIndex = ModuleSwitcher.PHOTO_MODULE_INDEX;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (prefs.getInt(CameraSettings.KEY_STARTUP_MODULE_INDEX, -1)
                        == ModuleSwitcher.GCAM_MODULE_INDEX && GcamHelper.hasGcamCapture()) {
                moduleIndex = ModuleSwitcher.GCAM_MODULE_INDEX;
            }
        } else if (MediaStore.ACTION_IMAGE_CAPTURE.equals(action)
                || MediaStore.ACTION_IMAGE_CAPTURE_SECURE.equals(action)) {
            moduleIndex = ModuleSwitcher.PHOTO_MODULE_INDEX;
        } else {
            // If the activity has not been started using an explicit intent,
            // read the module index from the last time the user changed modes
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//            moduleIndex = prefs.getInt(CameraSettings.KEY_STARTUP_MODULE_INDEX, -1);
//            if ((moduleIndex == ModuleSwitcher.GCAM_MODULE_INDEX &&
//                    !GcamHelper.hasGcamCapture()) || moduleIndex < 0) {
//                moduleIndex = ModuleSwitcher.PHOTO_MODULE_INDEX;
//            }
            moduleIndex = ModuleSwitcher.PHOTO_MODULE_INDEX;
        }
//Add by fei.gao for Camera_animation dev -begin
        mMenuDrawable=(MenuDrawable)findViewById(R.id.drawer_camera);
        modeList=(ModeListView)findViewById(R.id.mode_listview);
        modelistparent=(LinearLayout)findViewById(R.id.modelistparent);
        
        mDrawLayout =(DrawLayout)findViewById(R.id.drawer_linear);
        mDrawLayout.setModeListView(this,modeList,modelistparent);
        mDrawLayout.setMenuDrawable(mMenuDrawable);
        getCameraViewManager().getTopViewManager().setModeListView(mDrawLayout,mMenuDrawable);
        mMenuDrawable.setDrawerLayout(this,mDrawLayout,modeList);
        
        mBack=(RotateImageView)findViewById(R.id.mode_back);
        mModeSettings=(LinearLayout)findViewById(R.id.mode_settings);
        
        mModeSettings_image=(RotateImageView)findViewById(R.id.mode_settings_image);
        mModeSettings_text=(RotateLayout)findViewById(R.id.mode_settings_text);
//        mAsdTitleBar=(RotateLayout)mCameraModuleRootView.findViewById(R.id.asd_wrapper);
        
        initOnTouch();
        
        //Add by fei.gao for Camera_animation dev -end

        
        
        modeList.setGC(mGC);
        
        FilmStripGestureRecognizer.Listener ll = mCameraViewManager.getZoomManager().getCameraGestureListener();
        mFilmStripView.setCameraGestureListener(ll);
        mOrientationListener = new MyOrientationEventListener(this);
        mOrientationManager =OrientationManager.getInstance(this);
        mGC.setCurrentMode(moduleIndex);
        setModuleFromIndex(moduleIndex);
        mCameraViewManager.show();
        mCurrentModule.setStartPreviewCallback(new IStartPreviewCallback() {
            @Override
            public void onPreviewStarted() {
                if (mCameraReceiver == null) {
                    mCameraReceiver = new CameraReceiver();
                    myfilter = new IntentFilter();
                    // [BUGFIX]-Add by xuan.zhou,PR-695166, 2014-6-23
                    myfilter.addAction(TCT_SAVE_DONE_ACTION);
                    myfilter.addAction(TCT_FACE_UNLOCK);
                    CameraActivity.this.registerReceiver(mCameraReceiver, myfilter);
                }

                if (mSDCardReceiver == null) {
                    mSDCardReceiver = new SDCardReceiver();
                    mSDfilter = new IntentFilter();
                    mSDfilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
                    mSDfilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
                    // sdcard broadcast intent's uri=file:///mnt/sdcard,
                    // so must add the data scheme,to receive the broadcast
                    mSDfilter.addDataScheme("file");
                    CameraActivity.this.registerReceiver(mSDCardReceiver, mSDfilter);
                }
            }
        });
        mCurrentModule.init(this, getViewLayer(VIEW_LAYER_PREVIEW));

        if (!mSecureCamera) {
            mDataAdapter = mWrappedDataAdapter;
            mFilmStripView.setDataAdapter(mDataAdapter);
            if (!isCaptureIntent()) {
                mDataAdapter.requestLoad(getContentResolver());
            }
        } else {
            // Put a lock placeholder as the last image by setting its date to
            // 0.
            ImageView v = (ImageView) getLayoutInflater().inflate(
                    R.layout.secure_album_placeholder, null);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
                                UsageStatistics.ACTION_GALLERY, null);
                        startActivity(IntentHelper.getGalleryIntent(CameraActivity.this));
                    } catch (ActivityNotFoundException e) {
                        Log.w(TAG, "Failed to launch gallery activity, closing");
                    }
                    finish();
                }
            });
            mDataAdapter = new FixedLastDataAdapter(
                    mWrappedDataAdapter,
                    new SimpleViewData(
                            v,
                            v.getDrawable().getIntrinsicWidth(),
                            v.getDrawable().getIntrinsicHeight(),
                            0, 0));
            // Flush out all the original data.
            mDataAdapter.flush();
            mFilmStripView.setDataAdapter(mDataAdapter);
        }

        setupNfcBeamPush();

        mLocalImagesObserver = new LocalMediaObserver();
        mLocalVideosObserver = new LocalMediaObserver();

        getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
                mLocalImagesObserver);
        getContentResolver().registerContentObserver(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true,
                mLocalVideosObserver);
//        if(!isNonePickIntent()){
       	 disableDrawer();
//        }
        
//        if(this.getWindowManager().getDefaultDisplay().getRotation()==2){
//        	hadRotate=true;
//        }else{
//        	hadRotate=false;
//        }
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        closeReversible();
    }

    public boolean isImageCaptureIntent() {
        return PICK_TYPE_PHOTO == mPickType;
    }

    public boolean isVideoCaptureIntent() {
        return PICK_TYPE_VIDEO == mPickType;
    }

    public boolean isVideoWallPaperIntent() {
        return PICK_TYPE_WALLPAPER == mPickType;
    }

    private void parseIntent() {
        String action = mCurIntent.getAction();
        Log.d(TAG,"###YUHUAN###parseIntent#action=" + action);
        if (MediaStore.ACTION_IMAGE_CAPTURE.equals(action)) {
            mPickType = PICK_TYPE_PHOTO;
        } else if (EXTRA_VIDEO_WALLPAPER_IDENTIFY_VALUE.equals(
                mCurIntent.getStringExtra(EXTRA_VIDEO_WALLPAPER_IDENTIFY))) {
            mWallpaperAspectio = mCurIntent.getFloatExtra(EXTRA_VIDEO_WALLPAPER_RATION,
                    WALLPAPER_DEFAULT_ASPECTIO);
            mCurIntent.putExtra(EXTRA_QUICK_CAPTURE, true);
            mPickType = PICK_TYPE_WALLPAPER;
        } else if (MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
            mPickType = PICK_TYPE_VIDEO;
        } else {
            mPickType = PICK_TYPE_NORMAL;
        }
    }

    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        this.startActivityForResult(intent, REQUEST_QRCODE_PICK_IMAGE);
    }

    public void setViewState(int state) {
        mCameraViewManager.setViewState(state);
        mViewState = state;
    }

    public int getViewState(){
        return mViewState;
    }

    // goto scan mode as swipe to left in photo mode and back camera
    // modify by minghui.hua for PR946277
    public void goToScanMode(){
        // if(getCameraId() == GC.CAMERAID_BACK && mGC.getCurrentMode() == GC.MODE_PHOTO){
            //hide the setting set
            setViewState(GC.VIEW_STATE_HIDE_SET);
            doModeChanged(GC.MODE_QRCODE);
        // }
    }

    //back to photomode
    public void backToPhotoMode(){
    	Log.d(TAG,"###YUHUAN###backToPhotoMode ");
//        if(mGC.getCurrentMode() == GC.MODE_QRCODE){
    		disableNavigationBar();
            doModeChanged(GC.MODE_PHOTO);
//        }
    }

    private void doModeChanged(int mode){
        // if(mCurrentModule instanceof PhotoModule){
            int realmode = GC.getModeIndex(mode);
            Log.d(TAG,"###YUHUAN###doModeChanged#realmode=" + realmode);
            if (mGC.getCurrentMode() != realmode) {
                Log.i(TAG,"doModeChanged realmode:"+realmode);
                mGC.setCurrentMode(realmode);
                notifyModeChanged(mGC.getCurrentMode());
            }
        // }
    }

    public boolean isNonePickIntent() {
        return PICK_TYPE_NORMAL == mPickType;
    }

    private void init() {
        mCameraViewManager = new CameraViewManager(this);
    }
    public GC getGC() {
//        if (mGC == null) {
//            Log.i(TAG,"LMCH0410 mGC should not be null");
//            mGC = new GC(this);
//        }
        return mGC;
    }
    public CameraViewManager getCameraViewManager() {
//        if (mCameraViewManager == null) {
//            Log.i(TAG,"LMCH0410 mCameraViewManager should not be null");
//            if (mGC == null) {
//                mGC = new GC(this);
//            }
//            mCameraViewManager = new CameraViewManager(this);
//        }
        return mCameraViewManager;
    }
    public View getRootLayout() {
        return mCameraModuleRootView;
    }

    public ShutterManager getShutterManager() {
        return mCameraViewManager.getShutterManager();
    }

    public BackButtonManager getBackButtonManager() {
        return mCameraViewManager.getBackButtonManager();
    }
    public ViewGroup getViewLayer(int layer) {
        ViewGroup viewLayer = null;
        switch (layer) {
        case VIEW_LAYER_ROOT:
            if (mViewLayerRoot == null)
                mViewLayerRoot = (ViewGroup) mCameraModuleRootView.findViewById(R.id.camera_app_root);
            viewLayer = mViewLayerRoot;
            break;
        case VIEW_LAYER_BOTTOM:
            if (mViewLayerBottom == null)
                mViewLayerBottom = (ViewGroup) mCameraModuleRootView.findViewById(R.id.view_layer_bottom);
            viewLayer = mViewLayerBottom;
            break;
        case VIEW_LAYER_NORMAL:
            if (mViewLayerNormal == null)
                mViewLayerNormal = (ViewGroup) mCameraModuleRootView.findViewById(R.id.view_layer_normal);
            viewLayer = mViewLayerNormal;
            break;
        case VIEW_LAYER_TOP:
            if (mViewLayerTop == null)
                mViewLayerTop = (ViewGroup) mCameraModuleRootView.findViewById(R.id.view_layer_top);
            viewLayer = mViewLayerTop;
            break;
        case VIEW_LAYER_SETTING:
            if (mViewLayerSetting == null)
                mViewLayerSetting = (ViewGroup) mCameraModuleRootView.findViewById(R.id.view_layer_setting);
            viewLayer = mViewLayerSetting;
            break;
        case VIEW_LAYER_PREVIEW:
            if (mViewLayerPreview== null)
                mViewLayerPreview = (ViewGroup) mCameraModuleRootView.findViewById(R.id.view_layer_preview);
            viewLayer = mViewLayerPreview;
            break;
        case VIEW_LAYER_DIALOG:
        	if (mViewLayerDialog== null)
        		mViewLayerDialog = (ViewGroup) mCameraModuleRootView.findViewById(R.id.view_layer_dialog);
            viewLayer = mViewLayerDialog;
            break;
        default:
            throw new RuntimeException("Wrong layer:" + layer);
        }
        if (viewLayer == null)
            Log.d(TAG, "zsye95 getViewLayer("+layer+") return null");
        return viewLayer;
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) 
    private void setRotationAnimation() {
        int rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_ROTATE;
        rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE;
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.rotationAnimation = rotationAnimation;
        win.setAttributes(winParams);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        mCurrentModule.onUserInteraction();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean result = false;
        if(ev.getActionMasked() == MotionEvent.ACTION_UP){
	        if((null != mMenuDrawable) && (null != modeList) && mMenuDrawable.isDrawerOpened()){
	        		modeList.updateModeMenu();
	        }
	        if((null != mMenuDrawable) && (null !=mModeSettings_image) && mMenuDrawable.isDrawerOpened() ){
	        	mModeSettings_image.setImageResource(R.drawable.mode_settings_normal);
        }
        }
        
        if(isInCamera()){
            if (mCameraViewManager != null){
                result |= mCameraViewManager.dispatchTouchEvent(ev);
            }
            return result || super.dispatchTouchEvent(ev);
        }

        result = super.dispatchTouchEvent(ev);
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            // Real deletion is postponed until the next user interaction after
            // the gesture that triggers deletion. Until real deletion is performed,
            // users can click the undo button to bring back the image that they
            // chose to delete.
            if (mPendingDeletion && !mIsUndoingDeletion) {
                 performDeletion();
            }
        }
        
        
        return result;
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // [BUGFIX]-ADD by xuan.zhou, PR-695166, 2014-6-23
        isResumed = false;
        // Delete photos that are pending deletion
        performDeletion();
        showNavigationBar();
        mMainHandler.removeMessages(HIDE_ACTION_BAR);
        resetReversible();
        // modify by minghui.hua for PR930733
        // mFilmStripView.resetOritation();
        mOrientationListener.disable();
        mCurrentModule.onPauseBeforeSuper();
        super.onPause();
        mCurrentModule.onPauseAfterSuper();
        mOrientationManager.pause();

        // modify by minghui.hua for PR971338
        if (mMenuDrawable!=null && mMenuDrawable.isDrawerOpened()) {
            mMenuDrawable.closeDrawers();
        }

        mLocalImagesObserver.setActivityPaused(true);
        mLocalVideosObserver.setActivityPaused(true);

        /**
         * return to camera after exit by home key
         * the delete dialog and detail dialog will not dismiss
         */
        if (mFilmStripView.getDeleteDialog()!=null) {
            mFilmStripView.getDeleteDialog().dismiss();
        }
        if (mDetailsDialog!=null) {
            mDetailsDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // modify by minghui.hua for PR973462
        if (mFilmStripView.isZoomOut()) {
            mFilmStripView.resetZoomView();
        }
        if (requestCode == REQ_CODE_DONT_SWITCH_TO_PREVIEW) {
            // modify by minghui.hua for PR930404
            mMainHandler.sendEmptyMessageDelayed(HIDE_ACTION_BAR,
                    SHOW_ACTION_BAR_TIMEOUT_MS);
            mResetToPreviewOnResume = false;
            mIsEditActivityInProgress = false;
        }
        //[FEATURE]-Add BEGIN by TCTNB,xutao.wu, 2014-07-24, PR635962
        else if(resultCode == REQ_CODE_BACK_FROM_FILTER_BACK_KEY) {
            getCameraViewManager().getTopViewManager().setCurrentMode(GC.MODE_PHOTO);
        }
        //[FEATURE]-Add END by TCTNB,xutao.wu, 2014-07-24, PR635962
        else{
            super.onActivityResult(requestCode, resultCode, data);
            mCurrentModule.onActivityResult(requestCode, resultCode, data);
        }
    }

	public void notifyModeChanged(int newMode) {
		Log.d(TAG, "###YUHUAN###notifyModeChanged#newMode=" + newMode);
		// [BUGFIX]-MOD-BEGIN by yongsheng.shan, PR-907303, 2015-01-26
		NOTIFY_MODE_CHANGED_NEW_MODE = newMode;
		if (mGC.getOldCameraId() == GC.MODE_PANORAMA
				|| mGC.getCurrentMode() == GC.MODE_PANORAMA) {
			int PendingModeId = newMode;
			// delete by fei.gao -begin for the Process of changing mode ,now
			// press back can exit app,not back to photo mode
			// if (mGC.getOldCameraId() == GC.MODE_PANORAMA)
			// PendingModeId = GC.MODE_PHOTO;
			// delete by fei.gao --end for the Process of changing mode ,now
			// press back can exit app,not back to photo mode
			doModeChange(PendingModeId);
		} else if (mGC.getOldCameraId() == GC.MODE_TIMELAPSE
				|| mGC.getCurrentMode() == GC.MODE_TIMELAPSE) {
			int PendingModeId = newMode;
			// delete by fei.gao -begin for the Process of changing mode ,now
			// press back can exit app,not back to photo mode
			// if (mGC.getOldCameraId() == GC.MODE_TIMELAPSE)
			// PendingModeId = GC.MODE_PHOTO;
			// delete by fei.gao -end for the Process of changing mode ,now
			// press back can exit app,not back to photo mode
			doModeChange(PendingModeId);
		} else {
			onCameraOpened();
		}
		// [BUGFIX]-MOD-END by yongsheng.shan, PR-907303, 2015-01-26
		if (getCameraViewManager().getQrcodeViewManager().scanItem != null) {
			if (getCameraViewManager().getQrcodeViewManager().scanItem
					.getVisibility() == View.VISIBLE)
				getCameraViewManager().getQrcodeViewManager().scanItem
						.setVisibility(View.GONE);
		}
		if (mCameraViewManager != null) {
			mCameraViewManager.refresh();
		}
	}
 // [BUGFIX]-ADD-BEGIN by yongsheng.shan, PR-907303, 2015-01-26   
    public void onCameraOpened(){ 
        if (mCurrentModule instanceof PhotoModule) {
        	int realmode = GC.getModeIndex(NOTIFY_MODE_CHANGED_NEW_MODE);
        	if (mGC.getCurrentMode() != realmode) {
        		Log.i(TAG,"doModeChanged realmode:"+realmode);
        		mGC.setCurrentMode(realmode);
        	}
            ((PhotoModule) mCurrentModule).doModeChange(NOTIFY_MODE_CHANGED_NEW_MODE);
        }
        if (mCameraViewManager != null) {
            mCameraViewManager.refresh();
        }
    }
 // [BUGFIX]-ADD-END by yongsheng.shan, PR-907303, 2015-01-26 

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mCurIntent = intent;
        String action = mCurIntent.getAction();
        Log.d(TAG, "onNewIntent action="+mCurIntent.getAction());
        if (GC.START_QRSCAN_INTENT.equals(action)) {
            mCameraId = 0;
        }else if (GC.START_FRONT_CAMERA_INTENT.equals(action)) {
            mCameraId = 1;
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        
        // TODO: Handle this in OrientationManager.
        // Auto-rotate off
    	//[BUGFIX]-Add by TCTNB,kechao.chen, 2014-08-02,PR742932 Begin
//        if (Settings.System.getInt(getContentResolver(),
//                Settings.System.ACCELEROMETER_ROTATION, 0) == 0) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
//            mAutoRotateScreen = false;
//        } else {
//            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
//            mAutoRotateScreen = false;//true;
//        }
    	//[BUGFIX]-Add by TCTNB,kechao.chen, 2014-08-02,PR742932 End
        //mCameraViewManager.refresh();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //pr1000712 yuguan.chen added begin
        if(mGC != null) {
        	mGC.setContext(this);
        }
        //pr1000712 yuguan.chen added end
        
//    	if(this.getWindowManager().getDefaultDisplay().getRotation()==2){
//        	hadRotate=true;
//        }else{
//        	hadRotate=false;
//        }
    	closeReversible();
    	updateBatterySaverState();
    	if(mFilmStripView!=null){
    		mFilmStripView.clearZoom();
    	}
        // PR861176-lan.shan-001 Add begin
    	if(isInCamera()){
	        if (Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) {
	            setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
	        }
    	}
        // PR861176-lan.shan-001 Add end

        UsageStatistics.onEvent(UsageStatistics.COMPONENT_CAMERA,
                UsageStatistics.ACTION_FOREGROUNDED, this.getClass().getSimpleName());

        mOrientationListener.enable();

        // [BUGFIX]-MOD-BEGIN by xuan.zhou, PR-695166, 2014-6-23
        // If screen is face locked, block it and don't resume and open camera now,
        // or FaceLockService can not open front camera later.
        if (!CameraUtil.isFaceLocked(this) && !isResumed) {
            isFaceLockBlocked = false;
            
            mCurrentModule.onResumeBeforeSuper();
            super.onResume();
            mCurrentModule.onResumeAfterSuper();
        } else if (!isResumed){
            isFaceLockBlocked = true;
            super.onResume();
        } else {
            super.onResume();
        }
        
        mOrientationManager.resume();
        //[BUGFIX]-MOD-BEGIN by xuan.zhou, PR-695166, 2014-6-23
        if(mGC.getCurrentMode() != GC.MODE_POLAROID){
        	setSwipingEnabled(true);
        }else{
        	setSwipingEnabled(false);
        	setIsLockDrawer(false);
        }
        
        if (mResetToPreviewOnResume) {
            // Go to the preview on resume.
            //Add by zhimin.yu for PR931912 begin
			if(!(false == mGC.getCameraPreview()) || !isImageCaptureIntent()){
                mFilmStripView.getController().goToFirstItem();
                disableNavigationBar();
            }
            //Add by zhimin.yu for PR931912 end
        }
        // Default is showing the preview, unless disabled by explicitly
        // starting an activity we want to return from to the filmstrip rather
        // than the preview.
        mResetToPreviewOnResume = true;

        if (mLocalVideosObserver.isMediaDataChangedDuringPause()
                || mLocalImagesObserver.isMediaDataChangedDuringPause()) {
            if (!mSecureCamera) {
                // If it's secure camera, requestLoad() should not be called
                // as it will load all the data.
                mDataAdapter.requestLoad(getContentResolver());
            }
        }
        mLocalImagesObserver.setActivityPaused(false);
        mLocalVideosObserver.setActivityPaused(false);
        if(mPickType == PICK_TYPE_NORMAL && mGC.getCurrentMode() == GC.MODE_VIDEO){
            switchPhotoVideo(GC.MODE_PHOTO);
        }
        Log.d(TAG, "start to reset oritation");
      //modify by fei.gao for PR986459 --begin
        if(mFilmStripView !=null && mFilmStripView.inFullScreen()){
        	if(!mFilmStripView.isCameraPreview()&& mCameraViewManager.getTopViewManager().getOverallMenuManager().isShowing()){
        		enableDrawer();
        	}
        	if(mFilmStripView.isCameraPreview()){
        		mFilmStripView.resetOritation();
        	}
        }
      //modify by fei.gao for PR986459 --end

        //modify by minghui.hua for PR900938,930404
        if (!mMainHandler.hasMessages(HIDE_ACTION_BAR)) {
            mMainHandler.sendEmptyMessageDelayed(HIDE_NEVIGATION,
                    HIDE_NEVIGATION_TIMEOUT_MS);
        }

        SDCard.instance().updateSDCardMountPath();
        /// modify by yaping.liu for pr966435 {
        Storage.setSaveSDCard(Storage.isSaveSDCard(), false);
        /// }
    }

    // modify by minghui.hua for PR946277
    public Intent getCurIntent(){
        return mCurIntent;
    }
    public void setCurIntent(String action){
        mCurIntent.setAction(action);
    }

    @Override
    public void onStart() {
        super.onStart();
        bindMediaSaveService();
        mPanoramaViewHelper.onStart();
        //Add by zhimin.yu for PR945430 begin
        if(!(false == mGC.getCameraPreview() && isImageCaptureIntent())){
             mCameraViewManager.getTopViewManager().showSettingIndicator();
        }
        //Add by zhimin.yu for PR945430 end
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        mPanoramaViewHelper.onStop();
        unbindMediaSaveService();
    }
    
    
    private AudioManager mAudioManager;
    
    private boolean mIsAudioFocused=false;
    
    public void gainAudioFocus(){
    	if(mAudioManager==null){
    		mAudioManager=(AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
    	}
    	
    	int requestResult=mAudioManager.requestAudioFocus(null, AudioManager.STREAM_SYSTEM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    	if(requestResult==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
    		mIsAudioFocused=true;
    	}
    }
    
    public void abandonAudioFocus(){
    	if(mAudioManager==null){
    		return;
    	}
    	if(mIsAudioFocused){
    		int requestResult=mAudioManager.abandonAudioFocus(null);
    		if(requestResult==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
    			mIsAudioFocused=false;
    		}
    	}
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
    	if(gRSHelper!=null){
    		gRSHelper.destroy();
    		gRSHelper=null;
    	}

        /**
         * It's not need to release camera again, as the camera already released
         * in "onPause". In PR973794's condition, there are "CameraLauncher" and
         * "VideoCamera" in the heap. When we exit camera, then back to video
         * preview, camera will be released again in CameraLauncher's onDestroy
         * after it was opened in VideoCamera's onResume, so Camera will FC
         * because the camera is null
         **/
        // PR973794, PR940131-sichao.hu add begin
        // CameraHolder.instance().strongRelease();
        // PR940131-sichao.hu add end

    	if(SceneDetectorLib.instancePool()>0){
    		SceneDetectorLib.uninit();
    	}

        abandonAudioFocus();
        if (mSecureCamera) {
            unregisterReceiver(mScreenOffReceiver);
        }
        getContentResolver().unregisterContentObserver(mLocalImagesObserver);
        getContentResolver().unregisterContentObserver(mLocalVideosObserver);
        if(mCameraReceiver != null){
            this.unregisterReceiver(mCameraReceiver);
        }
        if (mSDCardReceiver != null) {
            this.unregisterReceiver(mSDCardReceiver);
        }

        doWhenCloseActivity();
        //[FEATURE]-Add BEGIN by TCTNB,xutao.wu, 2014-07-24, PR635962
        if(mLastThumbnailBitmap != null){
           mLastThumbnailBitmap.recycle();
           mLastThumbnailBitmap = null;
        }
        //[FEATURE]-Add END by TCTNB,xutao.wu, 2014-07-24, PR635962
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        mCurrentModule.onConfigurationChanged(config);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if( isProgressShow)return true;
        //PR931242-sichao.hu add begin
        if(mCameraViewManager!=null&&mCameraViewManager.getTopViewManager()!=null&&keyCode!=KeyEvent.KEYCODE_BACK/*PR932963-sichao.hu added*/){
        	if(mCameraViewManager.getTopViewManager().isOverallMenuShowing()){
        		// yuhuan 20150702
        		return super.onKeyDown(keyCode, event);
        	}
        }//PR931242-sichao.hu add end
        
        if(mGC.getCurrentMode() == GC.MODE_QRCODE && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) ){
            return super.onKeyDown(keyCode, event);
        }
        boolean result = false;
        if (mFilmStripView.inCameraFullscreen()) {
            if (mCurrentModule.onKeyDown(keyCode, event)) {
                return true;
            }
            // Prevent software keyboard or voice search from showing up.
            if (keyCode == KeyEvent.KEYCODE_SEARCH
                    || keyCode == KeyEvent.KEYCODE_MENU) {
                if (event.isLongPress()) {
                    return true;
                }
            }
            //result = mCurrentModule.onKeyDown(keyCode,  event);
        }
        return result || super.onKeyDown(keyCode, event);

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if( isProgressShow)return true;
        //PR931242-sichao.hu add begin
        if(mCameraViewManager!=null&&mCameraViewManager.getTopViewManager()!=null){
        	if(mCameraViewManager.getTopViewManager().isOverallMenuShowing()&&keyCode!=KeyEvent.KEYCODE_BACK/*PR932963-sichao.hu add*/){
        		return super.onKeyUp(keyCode, event);
        	}
        }//PR931242-sichao.hu add end
        if(mGC.getCurrentMode() == GC.MODE_QRCODE && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) ){
            return super.onKeyUp(keyCode, event);
        }
        if (mFilmStripView.inCameraFullscreen() && mCurrentModule.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        //Add by zhimin.yu for PR931912,PR942025, begin
        if(isImageCaptureIntent()){
            mGC.setCameraPreview(true);
        }
        //Add by zhimin.yu for PR931912,PR942025, end
    	if(mFilmStripView!=null){
    		mFilmStripView.clearZoom();
    	}
    	if(!isNonePickIntent()){
    		super.onBackPressed();
    		return;
    	}
    	boolean backResult;
        if (!mFilmStripView.inCameraFullscreen()) {
            mFilmStripView.getController().goToFirstItem();
            mCurrentModule.resizeForPreviewAspectRatio();
            Log.d(TAG, "onbackpressed reset oritation");
            mFilmStripView.resetOritation();
            //modify by minghui.hua for PR1012793
            enableDrawer();
            disableNavigationBar();
        } else if ((backResult=mCurrentModule.onBackPressed())==false & !mCameraViewManager.onBackPressed()) {
//        	if((mGC.getCurrentMode()==GC.MODE_PANORAMA||mGC.getCurrentMode()==GC.MODE_TIMELAPSE)
//        			&&!backResult){
//    			setViewState(GC.VIEW_STATE_NORMAL);
//    			mCameraViewManager.getTopViewManager().setCurrentMode(GC.MODE_PHOTO);
//    			return;
//    		}
//        	if(!(mCurrentModule instanceof PhotoModule)){
//        		notifyModeChanged(GC.MODE_PHOTO);
//        		return ;
//        	}
            super.onBackPressed();
        }
    }

    public void doWhenCloseActivity() {
        mCurrentModule.doWhenCloseActivity();
    }

    public boolean isAutoRotateScreen() {
        return mAutoRotateScreen;
    }

    public long pubGetStorageSpaceBytes(){
        updateStorageSpaceAndHint();
        pubStorageSpaceBytes = mStorageSpaceBytes;
        return pubStorageSpaceBytes;
    }

    protected void updateStorageSpace() {
        mStorageSpaceBytes = Storage.getAvailableSpace();
    }

    protected long getStorageSpaceBytes() {
        return mStorageSpaceBytes;
    }

    protected void updateStorageSpaceAndHint() {
        updateStorageSpace();
        updateStorageHint(mStorageSpaceBytes);
    }

    protected void updateStorageHint(long storageSpace) {
        String message = null;
        if (storageSpace == Storage.UNAVAILABLE) {
            message = getString(R.string.no_storage);
        } else if (storageSpace == Storage.PREPARING) {
            message = getString(R.string.preparing_sd);
        } else if (storageSpace == Storage.UNKNOWN_SIZE) {
            message = getString(R.string.access_sd_fail);
        } else if (storageSpace <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
            //[BUGFIX]-MOD by jian.zhang1, 2014-08-05,PR754915 Begin
            //message = getString(R.string.spaceIsLow_content);
//            if(Environment.tct_isEmmcPrimary()){
//                message = getString(R.string.spaceIsLow_content_phone);
//            }else{
//                message = getString(R.string.spaceIsLow_content);
                message = getString(R.string.spaceIsLow_content_storage);
//            }
            //[BUGFIX]-MOD by jian.zhang1, 2014-08-05,PR754915 End
        }

        if (message != null) {
            if (mStorageHint == null) {
                mStorageHint = OnScreenHint.makeText(this, message);
            } else {
                mStorageHint.setText(message);
            }
            mStorageHint.show();
        } else if (mStorageHint != null) {
            mStorageHint.cancel();
            mStorageHint = null;
        }
    }

    public int getTotalNumber(){
        return mDataAdapter.getTotalNumber();
    }

    protected void setResultEx(int resultCode) {
        mResultCodeForTesting = resultCode;
        setResult(resultCode);
    }

    protected void setResultEx(int resultCode, Intent data) {
        mResultCodeForTesting = resultCode;
        mResultDataForTesting = data;
        setResult(resultCode, data);
    }

    public int getResultCode() {
        return mResultCodeForTesting;
    }

    public Intent getResultData() {
        return mResultDataForTesting;
    }

    public boolean isSecureCamera() {
        return mSecureCamera;
    }

    @Override
    public void onModuleSelected(int moduleIndex) {
        if (mCurrentModuleIndex == moduleIndex) {
            return;
        }
      //Add by fei.gao --begin for (change mode from facebeauty to timelapse ,the facebeauty menu's values can't save ,so do stopFB to save the values)
		/*
        if (mGC.getOldCameraId() == GC.MODE_ARCSOFT_FACEBEAUTY
				&& mGC.getCurrentMode() != GC.MODE_ARCSOFT_FACEBEAUTY
				&& mCurrentModule instanceof PhotoModule) {
			((PhotoModule) mCurrentModule).stopFB();
		}*/
    	//Add by fei.gao --end for (change mode from facebeauty to timelapse ,the facebeauty menu's values can't save ,so do stopFB to save the values)
    	mIsModuleSwitching = true;
        CameraHolder.instance().keep();
        closeModule(mCurrentModule);
        setModuleFromIndex(moduleIndex);

        if(mCameraViewManager.getCameraManualModeManager().isShowing()){
            mCameraViewManager.getCameraManualModeManager().hide();
            mCameraViewManager.getTopViewManager().setShowingManualModel(false);
        }

        openModule(mCurrentModule);
        mCurrentModule.onOrientationChanged(mLastRawOrientation);
        if (mMediaSaveService != null) {
            mCurrentModule.onMediaSaveServiceConnected(mMediaSaveService);
        }
        if (mCameraViewManager != null) {
            mCameraViewManager.reInflate();
            mCameraViewManager.getZoomManager().resetZoom();
        }
        
        //Add by fei.gao --begin for (change mode from time lapse to facebeauty ,the facebeauty effect can't work)
        //set facebeauty
        /*
        if(moduleIndex == GC.MODE_ARCSOFT_FACEBEAUTY && mCurrentModule instanceof PhotoModule){
        	((PhotoModule)mCurrentModule).setArcsoftFaceBeauty(PhotoModule.ARCSOFTFACEBEAUTY_ON);
        }*/
      //Add by fei.gao --end for (change mode from time lapse to facebeauty ,the facebeauty effect can't work)
       if(moduleIndex == GC.MODE_POLAROID && mCurrentModule instanceof PhotoModule){
    	   ((PhotoModule)mCurrentModule).initPolaroid(true);
       }
        // Store the module index so we can use it the next time the Camera
        // starts up.
      //[Bug-fix] ADD-begin kechao.chen, 2014-08-23, PR770698
        if(moduleIndex != GC.MODE_PANORAMA) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putInt(CameraSettings.KEY_STARTUP_MODULE_INDEX, moduleIndex).apply();
        }
        SharedPreferences pref = ComboPreferences.get(this);
        if(moduleIndex != GC.MODE_HDR) {
            // reset HDR to off when the current module is not HdrModule
            // modify by minghui.hua for PR965397
            pref.edit().putString(CameraSettings.KEY_CAMERA_HDR, "off").apply();
        }else {
            showInfo(getString(R.string.photo_hdr_toast),false,false);
            pref.edit().putString(CameraSettings.KEY_CAMERA_HDR, "on").apply();
        }
      //[Bug-fix] ADD-begin kechao.chen, 2014-08-23, PR770698
        mIsModuleSwitching = false;
    }

    public void switchPhotoVideo(int index) {
        TraceLog(TAG, "switchPhotoVideo index="+index);
        // modify by minghui.hua for PR1021042
        if (index == mGC.getCurrentMode()) {
            TraceLog(TAG, "switchPhotoVideo already changed ,return !!");
            return;
        }
        mGC.setCurrentMode(index);
        onModuleSelected(index);
    }
    
    public void switchTimelapse(){
        if(getCameraViewManager().getQrcodeViewManager().scanItem != null){
            if(getCameraViewManager().getQrcodeViewManager().scanItem.getVisibility() == View.VISIBLE)
                getCameraViewManager().getQrcodeViewManager().scanItem.setVisibility(View.GONE);
        }
        //modify by minghui.hua for PR970718
        if(GC.START_FRONT_CAMERA_INTENT.equals(mCurIntent.getAction())
                ||GC.START_QRSCAN_INTENT.equals(mCurIntent.getAction())){
            setCurIntent(Intent.ACTION_MAIN);
        }
        mGC.setCurrentMode(GC.MODE_TIMELAPSE);
        onModuleSelected(GC.MODE_TIMELAPSE);
    }

    public void doModeChange(int index) {
        onModuleSelected(index);
    }
    /**
     * Sets the mCurrentModuleIndex, creates a new module instance for the given
     * index an sets it as mCurrentModule.
     */
    private void setModuleFromIndex(int moduleIndex) {
    	Log.d(TAG,"###YUHUAN###setModuleFromIndex#moduleIndex=" + moduleIndex);
        mCurrentModuleIndex = moduleIndex;
        switch (moduleIndex) {
            case ModuleSwitcher.VIDEO_MODULE_INDEX:
                mCurrentModule = new VideoModule();
                break;

            case ModuleSwitcher.PHOTO_MODULE_INDEX:
                mCurrentModule = new PhotoModule();
                break;
                
                
            case GC.MODE_TIMELAPSE:
            	mCurrentModule=new TctTimelapseModule();
            	break;
            	
            	
            case ModuleSwitcher.WIDE_ANGLE_PANO_MODULE_INDEX:
                mCurrentModule = new WideAnglePanoramaModule();
                break;

            case ModuleSwitcher.LIGHTCYCLE_MODULE_INDEX:
                mCurrentModule = PhotoSphereHelper.createPanoramaModule();
                break;
            case ModuleSwitcher.GCAM_MODULE_INDEX:
                // Force immediate release of Camera instance
                CameraHolder.instance().strongRelease();
                mCurrentModule = GcamHelper.createGcamModule();
                break;
            default:
                // Fall back to photo mode.
                mCurrentModule = new PhotoModule();
                mCurrentModuleIndex = ModuleSwitcher.PHOTO_MODULE_INDEX;
                break;
        }
    }

    /**
     * Launches an ACTION_EDIT intent for the given local data item.
     */
    public void launchEditor(LocalData data) {
        if (!mIsEditActivityInProgress) {
        	mFilmStripView.clearZoom();
            Intent intent = new Intent(Intent.ACTION_EDIT)
                    .setDataAndType(data.getContentUri(), data.getMimeType())
                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivityForResult(intent, REQ_CODE_DONT_SWITCH_TO_PREVIEW);
            } catch (ActivityNotFoundException e) {
                startActivityForResult(Intent.createChooser(intent, null),
                        REQ_CODE_DONT_SWITCH_TO_PREVIEW);
            }
            mIsEditActivityInProgress = true;
        }
    }

    /**
     * Launch the tiny planet editor.
     *
     * @param data the data must be a 360 degree stereographically mapped
     *            panoramic image. It will not be modified, instead a new item
     *            with the result will be added to the filmstrip.
     */
    public void launchTinyPlanetEditor(LocalData data) {
        TinyPlanetFragment fragment = new TinyPlanetFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TinyPlanetFragment.ARGUMENT_URI, data.getContentUri().toString());
        bundle.putString(TinyPlanetFragment.ARGUMENT_TITLE, data.getTitle());
        fragment.setArguments(bundle);
        fragment.show(getFragmentManager(), "tiny_planet");
    }

    /**
     * Launches an share intent for the given local data item.
     */
    public void launchShare(LocalData data) {
        if (!mIsEditActivityInProgress) {
            mFilmStripView.clearZoom();
            String typeStr = "image/*";
            if (!data.isPhoto()) {
                typeStr = "video/*";
            }
            Intent intent = new Intent(Intent.ACTION_SEND)
                    // modify by minghui.hua for PR928778
                    //.setDataAndType(data.getContentUri(),"image/jpeg")
                    .setType(typeStr)
                    .putExtra(Intent.EXTRA_STREAM, data.getContentUri())
                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivityForResult(
                        Intent.createChooser(intent, getResources().getText(R.string.share)),
                        REQ_CODE_DONT_SWITCH_TO_PREVIEW);
            } catch (ActivityNotFoundException e) {
                startActivityForResult(Intent.createChooser(intent, null),
                        REQ_CODE_DONT_SWITCH_TO_PREVIEW);
            }
            mIsEditActivityInProgress = true;
        }
    }

    private void openModule(CameraModule module) {
        Log.i(TAG,"###YUHUAN###openModule#viewLayer:"+getViewLayer(VIEW_LAYER_PREVIEW));
        module.init(this, getViewLayer(VIEW_LAYER_PREVIEW));
        module.onResumeBeforeSuper();
        module.onResumeAfterSuper();
    }

    private void closeModule(CameraModule module) {
        module.onPauseBeforeSuper();
        module.onPauseAfterSuper();
        ((ViewGroup) getViewLayer(VIEW_LAYER_PREVIEW)).removeAllViews();
    }

    private void performDeletion() {
        if (!mPendingDeletion) {
            return;
        }
        hideUndoDeletionBar(false);
        mDataAdapter.executeDeletion(CameraActivity.this);

        int currentId = mFilmStripView.getCurrentId();
        updateActionBarMenu(currentId);
        mFilmStripListener.onCurrentDataCentered(currentId);
    }

    public void showUndoDeletionBar() {
        if (mPendingDeletion) {
            performDeletion();
        }

        //in full screen, not show the UndoDeletionBar
        if (mFilmStripView.inFullScreen()) {
            return;
        }
        Log.v(TAG, "showing undo bar");
        mPendingDeletion = true;
        if (mUndoDeletionBar == null) {
            ViewGroup v = (ViewGroup) getLayoutInflater().inflate(
                    R.layout.undo_bar, mAboveFilmstripControlLayout, true);
            mUndoDeletionBar = (ViewGroup) v.findViewById(R.id.camera_undo_deletion_bar);
            View button = mUndoDeletionBar.findViewById(R.id.camera_undo_deletion_button);
            //[BUGFIX] Modify by Xianzhong.Zhang for PR773694 begin 09/02/2014
            View deleBtn = mUndoDeletionBar.findViewById(R.id.camera_deletion_button);
            deleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ((mDataAdapter.getTotalNumber() == 1) &&
                            CameraActivity.this.hasWindowFocus()) {
                        // Special case for the deleting the last picture FullScreen
                        // the camera preview .
                        mFilmStripView.getController().goToFullScreen();
                    }
                    hideUndoDeletionBar(true);
                }
            });
            //[BUGFIX] Modify by Xianzhong.Zhang for PR773694 end 09/02/2014
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDataAdapter.undoDataRemoval();
                    hideUndoDeletionBar(true);
                }
            });
            // Setting undo bar clickable to avoid touch events going through
            // the bar to the buttons (eg. edit button, etc) underneath the bar.
            mUndoDeletionBar.setClickable(true);
            // When there is user interaction going on with the undo button, we
            // do not want to hide the undo bar.
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        mIsUndoingDeletion = true;
                    } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                        mIsUndoingDeletion =false;
                    }
                    return false;
                }
            });
        }
        mUndoDeletionBar.setAlpha(0f);
        mUndoDeletionBar.setVisibility(View.VISIBLE);
        mUndoDeletionBar.animate().setDuration(200).alpha(1f).setListener(null).start();
    }

    private void hideUndoDeletionBar(boolean withAnimation) {
        Log.v(TAG, "Hiding undo deletion bar");
        mPendingDeletion = false;
        if (mUndoDeletionBar != null) {
            if (withAnimation) {
                mUndoDeletionBar.animate()
                        .setDuration(200)
                        .alpha(0f)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                // Do nothing.
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mUndoDeletionBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                // Do nothing.
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                                // Do nothing.
                            }
                        })
                        .start();
            } else {
                mUndoDeletionBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onShowSwitcherPopup() {
        mCurrentModule.onShowSwitcherPopup();
    }

    /**
     * Enable/disable swipe-to-filmstrip. Will always disable swipe if in
     * capture intent.
     * modify by minghui.hua for PR930242
     * @param enable {@code true} to enable swipe.
     */
    public void setSwipingEnabled(boolean enable) {
        if (isCaptureIntent()||isSecureCamera()) {
            mCameraPreviewData.lockPreview(true);
            //Modify by fei.gao	for PR 987461 -begin
            if(isSecureCamera()){
            	setIsLockDrawer(false);
            }else{
            	setIsLockDrawer(true);
            }
          //Modify by fei.gao	for PR 987461 -end
        } else {
            mCameraPreviewData.lockPreview(!enable);
            setIsLockDrawer(!enable);
        }
        
        
    }


    /**
     * Check whether camera controls are visible.
     *
     * @return whether controls are visible.
     */
    private boolean arePreviewControlsVisible() {
        return mCurrentModule.arePreviewControlsVisible();
    }

    /**
     * Show or hide the {@link CameraControls} using the current module's
     * implementation of {@link #onPreviewFocusChanged}.
     *
     * @param showControls whether to show camera controls.
     */
    private void setPreviewControlsVisibility(boolean showControls) {
        mCurrentModule.onPreviewFocusChanged(showControls);
    }

    // Accessor methods for getting latency times used in performance testing
    public long getAutoFocusTime() {
        return (mCurrentModule instanceof PhotoModule) ?
                ((PhotoModule) mCurrentModule).mAutoFocusTime : -1;
    }

    public long getShutterLag() {
        return (mCurrentModule instanceof PhotoModule) ?
                ((PhotoModule) mCurrentModule).mShutterLag : -1;
    }

    public long getShutterToPictureDisplayedTime() {
        return (mCurrentModule instanceof PhotoModule) ?
                ((PhotoModule) mCurrentModule).mShutterToPictureDisplayedTime : -1;
    }

    public long getPictureDisplayedToJpegCallbackTime() {
        return (mCurrentModule instanceof PhotoModule) ?
                ((PhotoModule) mCurrentModule).mPictureDisplayedToJpegCallbackTime : -1;
    }

    public long getJpegCallbackFinishTime() {
        return (mCurrentModule instanceof PhotoModule) ?
                ((PhotoModule) mCurrentModule).mJpegCallbackFinishTime : -1;
    }

    public long getCaptureStartTime() {
        return (mCurrentModule instanceof PhotoModule) ?
                ((PhotoModule) mCurrentModule).mCaptureStartTime : -1;
    }

    public boolean isRecording() {
        return (mCurrentModule instanceof VideoModule) ?
                ((VideoModule) mCurrentModule).isRecording() : false;
    }

    public CameraOpenErrorCallback getCameraOpenErrorCallback() {
        return mCameraOpenErrorCallback;
    }

    // For debugging purposes only.
    public CameraModule getCurrentModule() {
        return mCurrentModule;
    }
    
    public VideoModule getVideoModule(){
    	if(mCurrentModule!=null&&mCurrentModule instanceof VideoModule){
    		return (VideoModule)mCurrentModule;
    	}
    	return null;
    }
    
    public int getCameraId() {
        return mCameraId;
    }
    public void setCameraId(int id) {
        mCameraId = id;
    }

    public void setCameraDevice(CameraManager.CameraProxy mdevice) {
        mCameraDevice = mdevice;
        mZoomRatios = mdevice.getParameters().getZoomRatios();

        // delete >200 ratio for new requirement
        Iterator<Integer> iterator = mZoomRatios.iterator();
        while (iterator.hasNext()) {
            Integer item = iterator.next();
            if (item.intValue() > 200) {
                iterator.remove();
            }
        }
    }

    public void setZoomingStatus(boolean isZooming) {
        mIsZooming = isZooming;
    }

    public CameraManager.CameraProxy getCameraDevice() {
        return mCameraDevice;
    }
    public class CameraReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context,Intent intent){
            if(TCT_SAVE_DONE_ACTION.equals(intent.getAction())){
            //we wait the broadcast send by mediasaveservice
                if (mCurrentModule instanceof PhotoModule) {
                    ((PhotoModule)mCurrentModule).burstshotSavedDone();
                    setViewState(GC.VIEW_STATE_NORMAL);
                    sendDismissProgressImediate();
                }
            }
            // [BUGFIX]-ADD-BEGIN by xuan.zhou, PR-695166, 2014-6-23
            else if(TCT_FACE_UNLOCK.equals(intent.getAction())) {
                if (isFaceLockBlocked) {
                    isFaceLockBlocked = false;
                    isResumed = true;
                    mCurrentModule.onResumeBeforeSuper();
                    mCurrentModule.onResumeAfterSuper();
                }
            }
        }
    }

    public class SDCardReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context,Intent intent){
            if(intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)
                    || intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)){
                // modify by minghui.hua for PR949603,953996
                updateStorageSpaceAndHint();
                // hide the select positon dialog
                getCameraViewManager().hideDialog();
                getCameraViewManager().getTopViewManager().refreshMenu();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
	private void initOnTouch(){
    	mBack.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					mBack.setImageResource(R.drawable.mode_back_pressed);
					break;
				case MotionEvent.ACTION_UP:
					mBack.setImageResource(R.drawable.mode_back_normal);
					mMenuDrawable.closeDrawers();
					break;
				case MotionEvent.ACTION_CANCEL:
					mBack.setImageResource(R.drawable.mode_back_normal);
					break;
					default:
						break;
				}
				return true;
			}
		});
    	
    	mModeSettings.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				Log.d(TAG,"###YUHUAN#mModeSettings#onTouch#event.getAction()= " + event.getAction());
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					mModeSettings_image.setImageResource(R.drawable.mode_settings_pressed);
					break;
				case MotionEvent.ACTION_UP:
					mModeSettings_image.setImageResource(R.drawable.mode_settings_normal);
					Log.d(TAG,"###YUHUAN###show settings dialog begin");
					getCameraViewManager().getTopViewManager().showMode();
	 				mMenuDrawable.closeDrawers();
	 				disableDrawer();
					break;
//				case MotionEvent.ACTION_CANCEL:
//					mModeSettings_image.setImageResource(R.drawable.mode_settings_normal);
//					break;
					default:
						break;
				}
				return true;
			}
		});
    	
    }
    
    public void disableDrawer(){
    	Log.d(TAG,"###YUHUAN###disableDrawer into ");
    	if(mMenuDrawable!=null && mMenuDrawable.getDrawerLockMode(Gravity.LEFT) != DrawerLayout.LOCK_MODE_LOCKED_CLOSED){
    		mMenuDrawable.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    		getCameraViewManager().getTopViewManager().disableMenu();
    	}
    }
    
    public  void enableDrawer(){
    	Log.d(TAG,"###YUHUAN###enableDrawer into ");
    	if(mMenuDrawable!=null&&isNonePickIntent() && mMenuDrawable.getDrawerLockMode(Gravity.LEFT) != DrawerLayout.LOCK_MODE_UNLOCKED && !isLockDrawer){//PR916486-sichao.hu added
    		mMenuDrawable.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            // getCameraViewManager().getTopViewManager().enableMenu();
        }

        // modify by minghui.hua for PR1012794
        if (mMenuDrawable != null
                && mMenuDrawable.getDrawerLockMode(Gravity.LEFT) == DrawerLayout.LOCK_MODE_UNLOCKED
                && !isLockDrawer) {
            getCameraViewManager().getTopViewManager().enableMenu();
        }
    }

    @SuppressLint("InlinedApi")
    public void disableNavigationBar() {
        int oldUiOptions = mFilmStripView.getSystemUiVisibility();
        final int newUiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                ^ View.SYSTEM_UI_FLAG_HIDE_NAVIGATION ^ View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if (newUiOptions != oldUiOptions) {
            mMainHandler.post(new Runnable() {
                public void run() {
                    mFilmStripView.setSystemUiVisibility(newUiOptions);
                    isShowNavigationBar = false;
                }
            });
        }
    }

    /**
     * Enter camera from lock screen or search page with keypad,the navigation bar will
     * be shown, but the navigation bar's status is hidden by "getSystemUiVisibility"
     * so we delay 1s (not 500ms), force to disable the naviagtion bar again
     * modify by minghui.hua for PR920518
     */
    @SuppressLint("InlinedApi")
    public void forceDisableNavigationBar() {
        final int newUiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                ^ View.SYSTEM_UI_FLAG_HIDE_NAVIGATION ^ View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        mMainHandler.post(new Runnable() {
            public void run() {
                mFilmStripView.setSystemUiVisibility(newUiOptions);
                isShowNavigationBar = false;
            }
        });
    }

    @SuppressLint("InlinedApi")
    public void initNavigationBar(){
      	
    	    final int newUiOptions =View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN ^ View.SYSTEM_UI_FLAG_HIDE_NAVIGATION  ^ View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY ;
    	    mMainHandler.post(new Runnable(){
  	    		public void run(){
  	    			mFilmStripView.setSystemUiVisibility(newUiOptions);
  	  				isShowNavigationBar=false;
  	    		}
  	    	});
  				
    		
    }
    //SYSTEM_UI_FLAG_LAYOUT_STABLE
    @SuppressLint("InlinedApi")
	public void showNavigationBar(){
    	
    	int oldUiOptions=mFilmStripView.getSystemUiVisibility();
    	final int newUiOptions= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION ^ View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
    	if(newUiOptions!=oldUiOptions){
    		mMainHandler.post(new Runnable(){
  	    		public void run(){
					mFilmStripView.setSystemUiVisibility(newUiOptions);
					isShowNavigationBar=true;
  	    		}
    		});
    	}
    }
    
    @SuppressLint("InlinedApi")
   	private void resetNavigationBar(){
       	
       	if(isShowNavigationBar){
       		showNavigationBar();
       	}else{
       		disableNavigationBar();
       	}
       }
    public DrawLayout getDrawLayout(){
    	return mDrawLayout;
    }
    public MenuDrawable getMenuDrawer(){
    	return mMenuDrawable;
    }
    //Settings.Global.getInt(getContentResolver(), "degree_rotation" ,0) == 1
    private void closeReversible(){
    	if(Settings.Global.getInt(getContentResolver(), "degree_rotation" ,0) == 1){
    		HasChangeReversible=true;
    		Settings.Global.putInt(getContentResolver(), "degree_rotation" ,0);
    	}
    }
    public void resetReversible(){
    	if(HasChangeReversible){
    		HasChangeReversible=false;
    		Settings.Global.putInt(getContentResolver(), "degree_rotation" ,1);
    	}
    }
    public void setDisableOrientation(boolean mDis){
    	mDisableOrientation =mDis;
    	if(mDisableOrientation){
            GC.setOrientation(mCameraViewManager.getViewLayer(), 0, true);
    	}
    }
    public void setIsLockDrawer(boolean value){
    	isLockDrawer = value;
    }
    //Add by zhimin.yu for PR967300 begin
    public void updateisLocationOpend(boolean brecordloc){
        if (mCurrentModule instanceof PhotoModule) {
            ((PhotoModule)mCurrentModule).updateisLocationOpend(brecordloc);
        } else if (mCurrentModule instanceof WideAnglePanoramaModule) {
            ((WideAnglePanoramaModule) mCurrentModule).updateisLocationOpend(brecordloc);
        }
    }
    //Add by zhimin.yu for PR967300 end
    //Modify by fei.gao for PR988884 --begin
    public boolean checkDeviceHasNavigationBar() {

		boolean hasMenuKey = ViewConfiguration.get(this)
				.hasPermanentMenuKey();
		if (!hasMenuKey) {
			return true;//has navgationbar
		}else{
			return false;
		}
	}
  //Modify by fei.gao for PR988884 --end
    
    public void resetDisplayForPolaroid(){
    	if(isDestroyed()){
    		return;
    	}
    	if(mFilmStripView!=null && mFilmStripView.inCameraFullscreen()){
    		mDataAdapter.requestLoad(getContentResolver());
    		mFilmStripView.getController().goToFirstItem();
        	disableNavigationBar();
    	}
    }
    public void resetPolaroid(boolean isEnable){
    	if(mCurrentModule instanceof PhotoModule){
    		((PhotoModule)mCurrentModule).initPolaroid(isEnable);
    	}
    }
}
