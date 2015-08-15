/******************************************************************************/
/*                                                                Date:09/2013*/
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/* Author :                                                                   */
/* Email  :                                                                   */
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
/* 10/15/2013|     wangxiaofei      |        515667        |[Ergo][DEV]Camera */
/* ----------|----------------------|----------------------|----------------- */
/* 11/29/2013|       Hui.Shen       |       PR-552287      |[Camcorder]Short  */
/*           |                      |                      |press menu key in */
/*           |                      |                      |camcorder is unuse*/
/******************************************************************************/

package com.android.camera.manager;

import java.util.HashMap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TabHost;

import com.android.camera.CameraActivity;
import com.android.camera.CameraSettings;
import com.android.camera.ComboPreferences;
import com.android.camera.GC;
import com.android.camera.ListPreference;
import com.android.camera.PhotoModule;
import com.android.camera.ui.BeautyFaceSeekBar;
import com.android.camera.ui.RotateImageView;
import com.jrdcamera.modeview.DrawLayout;
import com.jrdcamera.modeview.MenuDrawable;
import com.tct.camera.R;
//import com.android.camera.TctMutex;
//import com.android.camera.Util;
//import com.android.camera.manager.PhotoMenuManager;
//import com.android.camera.ui.PickerButton;
//import com.android.camera.ui.ModeSettingListLayout;
//import com.android.camera.ui.PickerButton;
//import com.android.camera.ui.PickerButton.Listener;
//import com.android.camera.ui.MaxLinearLayout;

public class TopViewManager extends ViewManager implements View.OnClickListener,
    View.OnTouchListener/*,
        ModeSettingListLayout.Listener, CameraActivity.OnPreferenceReadyListener, OnTabChangeListener,
        CameraActivity.OnParametersReadyListener */{
    private static final String TAG = "YUHUAN_TopViewManager";
    private static final boolean LOG = true;

    public interface SettingListener {
        boolean onCameraSwitched();
        boolean onFlashPicked();
    }

    private static final int SETTING_PAGE_LAYER = VIEW_LAYER_SETTING;
    private static final int MSG_REMOVE_SETTING = 0;
    private static final int FACE_MENU_GONE = 1001;
    private static final int FACE_GONE_DELAY=500;
    private static final int DELAY_MSG_REMOVE_SETTING_MS = 3000; //delay remove setting
    private static final String TAB_INDICATOR_KEY_PREVIEW = "preview";
    private static final String TAB_INDICATOR_KEY_COMMON = "common";
    private static final String TAB_INDICATOR_KEY_CAMERA = "camera";
    private static final String TAB_INDICATOR_KEY_VIDEO = "video";

    private boolean mNeedUpdate;
    private boolean mPreferenceReady;
    private boolean mShowingContainer;
    private boolean mShowingMode;
    private boolean mShowingVideoSet;
    private boolean mShowingAdvancedSet;
    public boolean mShowingManualModel;
    public boolean mhasShowManualMode=false;
    public  boolean mShowingFaceBeautyMenu;
    private boolean mMenuEnabled = true;
    private static boolean mIsHDVideoQuality;
    private boolean mRecordingStarted = false;
    //Add by zhiming.yu for PR913890 begin
    private boolean mSetVedioCameraPickerShowView = false;
    //Add by zhiming.yu for PR913890 end
    private Animation mFadeIn;
    private Animation mFadeOut;
    private SettingListener mListener;
    //private ModeSettingListLayout mSettingListLayout;
    private TabHost mTabHost;
    private View mSettingIndicator;
    private ViewGroup mModeLayout;
    private ViewGroup mSettingLayout;
    private ViewPager mPager;
    private RotateImageView mIndicator;
    private RotateImageView mModeIndicator;
    private RotateImageView mFlashPicker;
    private RotateImageView mCameraPicker;
    private RotateImageView mManualPicker;
    private RotateImageView mFaceBeautyPicker;
    private LinearLayout faceBeautyMenu;
    private BeautyFaceSeekBar beauty_bright;
    private BeautyFaceSeekBar beauty_soften;
//    private RotateImageView mBackPicker;
    private AdvancedMenuManager mAdvancedMenuManager;
    private PhotoMenuManager mPhotoMenuManager;
    private OverallMenuManager mOverAllMenuManager;
    private VideoMenuManager mVideoMenuManager;
    private ComboPreferences mPreferences;
    private int mShowTimes = 0;


    private Handler mMainHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (LOG) {
                Log.i(TAG, "handleMessage(" + msg + ")");
            }
            switch (msg.what) {
            case MSG_REMOVE_SETTING:
                //If we removeView and addView frequently, drawing cache may be wrong.
                //Here avoid do this action frequently to workaround that issue.
                if (mSettingLayout != null && mSettingLayout.getParent() != null) {
                    //getContext().removeView(mSettingLayout, SETTING_PAGE_LAYER);
                }
                break;
                //Add by fei.gao for (can focus in some modes )  -begin
            case FACE_MENU_GONE:
            	if(faceBeautyMenu !=null){
            		faceBeautyMenu.setVisibility(View.GONE);
            	}
            	break;
            	//Add by fei.gao for (can focus in some modes )  -end
            default:
                break;
            }
        };
    };

    public TopViewManager(CameraActivity context) {
        super(context);
        mContext = context;
        mAdvancedMenuManager = new AdvancedMenuManager(mContext);
        mVideoMenuManager = new VideoMenuManager(mContext);
    }
    public TopViewManager(CameraActivity context, int layer) {
        super(context, layer);
        mContext = context;
        mPhotoMenuManager = new PhotoMenuManager(mContext);
        mOverAllMenuManager=new OverallMenuManager(mContext);
        mAdvancedMenuManager = new AdvancedMenuManager(mContext);
        mVideoMenuManager = new VideoMenuManager(mContext);
    }
    
    public boolean isOverallMenuShowing(){
    	if(mOverAllMenuManager==null){
    		return false;
    	}
    	
    	return mOverAllMenuManager.isShowing();
    }
    
    @Override
    public void initialize() {
    	Log.d(TAG,"###YUHUAN###initialize ");
        applyListeners();
        if (mCameraPicker != null) {
            if(mGC.isVideoModule()) {
                mCameraPicker.setImageResource(R.drawable.ic_switch_video_facing_holo);
            } else {
                mCameraPicker.setImageResource(R.drawable.ic_switch_photo_facing_holo);
            }
        }
        if (mModeIndicator != null)
            mModeIndicator.setImageResource(R.drawable.btn_settings);
        if (mFlashPicker != null) {
            reloadPreference();
        }
        if(mManualPicker != null){
            mManualPicker.setImageResource(R.drawable.btn_manual_menu);
        }

//        if(mBackPicker != null){
//            mBackPicker.setImageResource(R.drawable.btn_back);
//        }
    }

    public void reloadPreference() {
        if(mPreferences == null) {
            mPreferences = new ComboPreferences(mContext);
            CameraSettings.upgradeGlobalPreferences(mPreferences.getGlobal());
            mPreferences.setLocalId(mContext, GC.CAMERAID_BACK);
            CameraSettings.upgradeLocalPreferences(mPreferences.getLocal());
        }
        
        /*
        String flashMode;

        if (mGC.getCurrentMode() != GC.MODE_VIDEO) {
            flashMode = mPreferences.getString(CameraSettings.KEY_FLASH_MODE,
                            mContext.getString(R.string.pref_camera_flashmode_default));
        } else {
            flashMode = mPreferences.getString(CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE,
                            mContext.getString(R.string.pref_camera_video_flashmode_default));
        }
        if (flashMode.equals("auto")) {
            mFlashPicker.setImageResource(R.drawable.bg_onscreen_flash_auto_picker);
        } else if (flashMode.equals("off")) {
            mFlashPicker.setImageResource(R.drawable.bg_onscreen_flash_off_picker);
        } else if (flashMode.equals("on")) {
            mFlashPicker.setImageResource(R.drawable.bg_onscreen_flash_on_picker);
        } else if (flashMode.equals("torch")) {
            mFlashPicker.setImageResource(R.drawable.bg_onscreen_flash_on_picker);
//            mFlashPicker.setImageResource(R.drawable.bg_onscreen_flash_torch_picker);
        } else if (flashMode.equals(mContext.getString(R.string.pref_camera_flashmode_no_flash))){
        	mFlashPicker.setVisibility(View.GONE);
        }
        */
    }
    public void refreshModeIndicator() {
        if (mModeIndicator == null) {
            View view = inflate(R.layout.onscreen_pickers);
            mModeIndicator = (RotateImageView) view.findViewById(R.id.setting_mode);
        }
        if (mCameraPicker == null) {
            View view = inflate(R.layout.onscreen_pickers);
            mCameraPicker = (RotateImageView)view.findViewById(R.id.onscreen_camera_picker);
        }
    }


    View view;

    @Override
    protected View getView() {
        view = inflate(R.layout.onscreen_pickers);
        mSettingIndicator = view;
        faceBeautyMenu=(LinearLayout)view.findViewById(R.id.facebeautymenu);
//        faceBeautyMenu.setTranslationX(mContext.getResources().getDisplayMetrics().heightPixels+mContext.getResources().getDimension(R.dimen.navigationbar_height));
//      faceBeautyMenu.setVisibility(View.GONE);
        beauty_bright=(BeautyFaceSeekBar)view.findViewById(R.id.bright);
        beauty_soften=(BeautyFaceSeekBar)view.findViewById(R.id.soften);
        //mFlashPicker = (RotateImageView)view.findViewById(R.id.onscreen_flash_picker);
        mCameraPicker = (RotateImageView)view.findViewById(R.id.onscreen_camera_picker);
        mModeIndicator = (RotateImageView)view.findViewById(R.id.setting_mode);
        mManualPicker = (RotateImageView)view.findViewById(R.id.onscreen_manual_picker);
        mFaceBeautyPicker=(RotateImageView)view.findViewById(R.id.onscreen_facebeauty_picker);
//        mBackPicker = (RotateImageView)view.findViewById(R.id.onscreen_back_picker);
        if(mFlashPicker != null){
           mFlashPicker.setOnTouchListener(this);
        }
        if(mCameraPicker != null){
           mCameraPicker.setOnTouchListener(this);
        }
        if(mModeIndicator != null){
           mModeIndicator.setOnTouchListener(this);
        }
        if(mManualPicker != null){
            mManualPicker.setOnTouchListener(this);
        }
        if(mFaceBeautyPicker != null){
        	mFaceBeautyPicker.setOnTouchListener(this);
        }

//        if(mBackPicker != null){
//            mBackPicker.setOnTouchListener(this);
//        }

        initialize();
        return view;
    }

    public void bringToFront() {
        if (view == null) {
            view.bringToFront();
        }
    }
    private void applyListeners() {
        if (mFlashPicker != null) {
            mFlashPicker.setOnClickListener(this);
        }
        if (mCameraPicker != null) {
            mCameraPicker.setOnClickListener(this);
        }

        if (mModeIndicator != null) {
            mModeIndicator.setOnClickListener(this);
        }

        if (mManualPicker != null) {
            mManualPicker.setOnClickListener(this);
        }
        
        if (mFaceBeautyPicker != null) {
        	mFaceBeautyPicker.setOnClickListener(this);
        }
//        if (mBackPicker != null) {
//            mBackPicker.setOnClickListener(this);
//        }

        if (mPhotoMenuManager != null) {
            mPhotoMenuManager.setSharedPreferenceChangedListener(mContext.getCurrentModule());
        }
        
        if(mOverAllMenuManager!=null){
        	mOverAllMenuManager.setSharedPreferenceChangedListener(mContext.getCurrentModule());
        }

        if (mAdvancedMenuManager != null) {
            mAdvancedMenuManager.setSharedPreferenceChangedListener(mContext.getCurrentModule());
        }
        if (mVideoMenuManager != null) {
            mVideoMenuManager.setSharedPreferenceChangedListener(mContext.getCurrentModule());
        }
        
    }

    private void releaseListeners() {
        if (mFlashPicker != null) {
            mFlashPicker.setOnClickListener(null);
        }
        if (mCameraPicker != null) {
            mCameraPicker.setOnClickListener(null);
        }

        if (mModeIndicator != null) {
            mModeIndicator.setOnClickListener(null);
        }

        if(mManualPicker != null){
            mManualPicker.setOnClickListener(null);
        }
        if(mFaceBeautyPicker != null){
        	mFaceBeautyPicker.setOnClickListener(null);
        }

//        if(mBackPicker != null){
//            mBackPicker.setOnClickListener(null);
//        }

        if (mPhotoMenuManager != null) {
            mPhotoMenuManager.setSharedPreferenceChangedListener(null);
        }

        if (mAdvancedMenuManager != null) {
            mAdvancedMenuManager.setSharedPreferenceChangedListener(null);
        }
        
        if (mOverAllMenuManager != null) {
        	mOverAllMenuManager.setSharedPreferenceChangedListener(null);
        }

        if (mVideoMenuManager != null) {
            mVideoMenuManager.setSharedPreferenceChangedListener(null);
        }
    }

    public void setListener(SettingListener listener) {
        mListener = listener;
    }
    public boolean onBackPressed() {
        boolean mBackconsumed = false;
        if(mShowingMode || mShowingAdvancedSet || mShowingVideoSet || mShowingManualModel || mShowingFaceBeautyMenu) {
            Log.i(TAG,"###YUHUAN###onBackPressed");
            hideAdvancedSet();
            hideVideoSet();
            hideCaptureMode();
            hideManualModel();
            hideFaceBeautyMenu();
            mBackconsumed = true;
        }
        
        if(isOverallMenuShowing()){
        	hideAdvanceSetting();
        	mBackconsumed=true;
        }
        return mBackconsumed;
    }
    @Override
    public void onInflate() {
        super.onInflate();
        
        Log.d(TAG,"###YUHUAN###onInflate into ");
        mPhotoMenuManager.reInflate();
        mOverAllMenuManager.reInflate();
        mVideoMenuManager.reInflate();
        mAdvancedMenuManager.reInflate();
    }

    @Override
    public void onRelease() {
        super.onRelease();
        releaseListeners();
        mPhotoMenuManager.release();
        mOverAllMenuManager.release();
        mVideoMenuManager.release();
        mAdvancedMenuManager.release();
    }

    @Override
    public void onRefresh() {
    	Log.d(TAG,"###YUHUAN###onRefresh#isModeIndicatorSupported=" + isModeIndicatorSupported());
        if (!isModeIndicatorSupported()) {
            hideView(mModeIndicator);
        } else {
            showView(mModeIndicator);
        }
        
        /*
        Log.d(TAG,"###YUHUAN###onRefresh#isFlashSupported=" + isFlashSupported());
        if (isFlashSupported()){
            showView(mFlashPicker);
        }else{
            hideView(mFlashPicker);
        }
        */
        
        Log.d(TAG,"###YUHUAN###onRefresh#isCameraPickedSupported=" + isCameraPickedSupported());
        if (isCameraPickedSupported()) {
            showView(mCameraPicker);
        }else{
            hideView(mCameraPicker);
        }
        
        Log.d(TAG,"###YUHUAN###onRefresh#isManualPickedSupportd=" + isManualPickedSupportd());
        if(isManualPickedSupportd()){
            showView(mManualPicker);
        }else {
            hideView(mManualPicker);
        }
        
        Log.d(TAG,"###YUHUAN###onRefresh#isFacebeautyPickedSupportd=" + isFacebeautyPickedSupportd());
        if(isFacebeautyPickedSupportd() ){
            showView(mFaceBeautyPicker);
        }else {
            hideView(mFaceBeautyPicker);
        }

//        if (isBackPickedSupported()){
//            showView(mBackPicker);
//        }else {
//            hideView(mBackPicker);
//        }

        if (mCameraPicker != null) {
            if(mGC.getCurrentMode() == GC.MODE_VIDEO) {
                mCameraPicker.setImageResource(R.drawable.ic_switch_video_facing_holo);
            } else {
                mCameraPicker.setImageResource(R.drawable.ic_switch_photo_facing_holo);
            }
            boolean enabled = mCameraPicker.isEnabled() && isEnabled();
            mCameraPicker.setEnabled(enabled);
        }
        if (mFlashPicker != null) {
            reloadPreference();
            boolean enabled = mFlashPicker.isEnabled() && isEnabled();
            mFlashPicker.setEnabled(enabled);
        }
        if (mModeIndicator != null) {
            mModeIndicator.setImageResource(R.drawable.btn_settings);
            boolean enabled = mModeIndicator.isEnabled() && isEnabled();
            mModeIndicator.setEnabled(enabled);
        }
        if(mManualPicker != null) {
            mManualPicker.setImageResource(R.drawable.btn_manual_menu);
            boolean enabled = mManualPicker.isEnabled() && isEnabled();
            mManualPicker.setEnabled(enabled);
        }
        
        if(mFaceBeautyPicker != null) {
        	mFaceBeautyPicker.setImageResource(R.drawable.btn_facebeauty_menu);
            boolean enabled = mFaceBeautyPicker.isEnabled() && isEnabled();
            mFaceBeautyPicker.setEnabled(enabled);
        }
        //Modify by fei.gao for (back to facebeauty from time lapse need init seekbar's value)  -begin
        // modify by minghui.hua for PR955936
//        if(mGC.getCurrentMode() == GC.MODE_ARCSOFT_FACEBEAUTY && faceBeautyMenu!=null){
//        	faceBeautyMenu.setVisibility(View.INVISIBLE);
//        }else {
//            initMenuforFirstanimation();
//        }
        //Add by fei.gao --begin for (facebeauty menu's value can refresh)
        if(mGC.getCurrentMode() == GC.MODE_ARCSOFT_FACEBEAUTY){
        	initSeekBar();
        }
        	faceMenuGone();
      //Add by fei.gao --end for (facebeauty menu's value can refresh)
        	//Modify by fei.gao for (back to facebeauty from time lapse need init seekbar's value)  -end
//        if(mBackPicker != null) {
//            mBackPicker.setImageResource(R.drawable.btn_back);
//            boolean enabled = mBackPicker.isEnabled() && isEnabled();
//            mBackPicker.setEnabled(enabled);
//        }
        if (mPhotoMenuManager != null)
            mPhotoMenuManager.refresh();
        
        if(mOverAllMenuManager!=null){
        	mOverAllMenuManager.refresh();
        }
    }

    private boolean isModeIndicatorSupported() {
        if(mContext.isNonePickIntent()){
            return true;
//        return mGC.getCurrentMode() != GC.MODE_QRCODE;
//                && mGC.getCurrentMode() != GC.MODE_MANUAL
//                && mGC.getCurrentMode() != GC.MODE_PANORAMA;
//                && mGC.getCurrentMode() != GC.MODE_HDR;
		} else {
			int curMode = mGC.getCurrentMode();
			return curMode != GC.MODE_VIDEO && curMode != GC.MODE_PHOTO;
		}
    }


	private boolean isFlashSupported() {
		if (mGC.isInArcsoftNight()) {
			return false;
		}

		int curMode = mGC.getCurrentMode();
		if (mContext.isNonePickIntent()) {
			return mContext.getCameraId() != GC.CAMERAID_FRONT
					&& curMode != GC.MODE_HDR
					&& curMode != GC.MODE_PANORAMA
					&& curMode != GC.MODE_NIGHT
					&& curMode != GC.MODE_SPORTS
					&& curMode != GC.MODE_QRCODE
					&& curMode != GC.MODE_TIMELAPSE
					&& !mContext.getCameraViewManager()
							.getCameraManualModeManager().isShowing();
			// [BUGFIX]-MOD-BEGIN by yongsheng.shan, PR-913860, 2015-01-30
		} else if (mContext.isImageCaptureIntent()) {
			return mContext.getCameraId() != GC.CAMERAID_FRONT;
			// [BUGFIX]-MOD-END by yongsheng.shan, PR-913860, 2015-01-30
		} else {
			if (mContext.isVideoCaptureIntent()) {
				return !mGC.isVideoRecording();
			} else if (curMode == GC.MODE_VIDEO) {
				return false;
			}
			return true;
		}
	}

	private boolean isCameraPickedSupported() {
		int curMode = mGC.getCurrentMode();
		return curMode != GC.MODE_PANORAMA && curMode != GC.MODE_QRCODE
				&& curMode != GC.MODE_HDR && curMode != GC.MODE_MANUAL
				&& curMode != GC.MODE_TIMELAPSE;
	}
	
    private boolean isManualPickedSupportd() {
        return mGC.getCurrentMode() == GC.MODE_MANUAL
                && !mContext.getCameraViewManager().getCameraManualModeManager().isShowing();
    }
    
    private boolean isFacebeautyPickedSupportd() {
        return mGC.getCurrentMode() == GC.MODE_ARCSOFT_FACEBEAUTY && !mShowingFaceBeautyMenu ;
//                && !mContext.getCameraViewManager().getCameraManualModeManager().isShowing();
    }

	private boolean isBackPickedSupported() {
		int curMode = mGC.getCurrentMode();
		return curMode != GC.MODE_PHOTO
				&& curMode != GC.MODE_VIDEO
				&& !mContext.getCameraViewManager()
						.getCameraManualModeManager().isShowing();
	}

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    public void resetToDefault() {
        if (mFlashPicker != null) {
            mGC.overrideSettingsValue(CameraSettings.KEY_FLASH_MODE, "default");
            mGC.overrideSettingsValue(CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE,"off");
            reloadPreference();
        }

        if (mAdvancedMenuManager != null) {
            mAdvancedMenuManager.resetToDefault();
        }

        if (mPhotoMenuManager != null) {
            mPhotoMenuManager.resetToDefault();
        }
        
        if (mOverAllMenuManager != null) {
        	mOverAllMenuManager.resetToDefault();
        }

        if (mVideoMenuManager != null) {
            mVideoMenuManager.resetToDefault();
        }
        hideAdvancedSet();
        hideVideoSet();
        hideCaptureMode();
        hideManualModel();
        hideFaceBeautyMenu();
    }
    

    
    @Override
    public void onClick(View view) {
        Log.i(TAG,"###YUHUAN###onClick enter");
        //modify by minghui.hua for PR909592 begin
        if(!isEnabled()){
            Log.w(TAG,"onClick,ignore this click");
            return;
        }
        //modify by minghui.hua for PR909592 end

        if(view == mFlashPicker) {
        	Log.d(TAG,"###YUHUAN###onClick#view=mFlashPicker");
            // Click flash button to change flash mode.
            if(mGC.getCurrentMode() != GC.MODE_VIDEO) {
                mGC.roundNextSettingsValue(CameraSettings.KEY_FLASH_MODE, mFlashPicker);
                String glbFlashMode=mGC.getPrefValue(CameraSettings.KEY_FLASH_MODE);
                String validVideoFlashMode=GC.getValidFlashModeForVideo(glbFlashMode);
                mGC.overrideSettingsValue(CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE,validVideoFlashMode);
            }else{
                mGC.roundNextSettingsValue(CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE, mFlashPicker);
            }
            reloadPreference();
            if (mListener != null) mListener.onFlashPicked();
        } else if(view == mCameraPicker) {
        	Log.d(TAG,"###YUHUAN###onClick#view=mCameraPicker");
            if (mListener != null){
                mListener.onCameraSwitched();
                if(!mContext.isNonePickIntent()){
                	mContext.disableDrawer();
                }
            }
        } else if (view == mModeIndicator) {
        	Log.d(TAG,"###YUHUAN###onClick#view=mModeIndicator");
               mContext.getMenuDrawer().openDrawer(Gravity.LEFT);
        } else if (view == mManualPicker) {
        	Log.d(TAG,"###YUHUAN###onClick#view=mManualPicker");
            showManualMode();
        }else if (view == mFaceBeautyPicker) {
        	Log.d(TAG,"###YUHUAN###onClick#view=mFaceBeautyPicker");
        	if(null != faceBeautyMenu && faceBeautyMenu.getVisibility() == View.GONE){
        		showFacebeautyMenuUp();
        	}
        }
//        else if (view == mBackPicker) {
//            mContext.setViewState(GC.VIEW_STATE_NORMAL);
//            setCurrentMode(GC.MODE_PHOTO);
//        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        //[BUGFIX]-Add by TCTNB.wangxiaofei,10/15/2013,515667,[Ergo][DEV]Camera
        case KeyEvent.KEYCODE_MENU:
            if (!mMenuEnabled) return true;
            
            Log.d(TAG,"###YUHUAN###onKeyDown#mGC.isVideoModule()=" + mGC.isVideoModule());
            if(!mGC.isVideoModule()){
            	Log.d(TAG,"###YUHUAN###onKeyDown#mShowingAdvancedSet=" + mShowingAdvancedSet);
                if(!mShowingAdvancedSet){
                    //if (mContext.getCameraViewManager().getCameraManualModeManager().isShowing()) {
                	Log.d(TAG,"###YUHUAN###onKeyDown#mShowingManualModel=" + mShowingManualModel);
                    if (mShowingManualModel) {
//                        mContext.setViewState(GC.VIEW_STATE_NORMAL);
//                        mContext.getCameraViewManager().getCameraManualModeManager().hide();
                        hideManualModel();
                    } else if (!mShowingMode) {
                        showMode();
                    } else {
                        hideCaptureMode();
                    }
                } else {
                    hideAdvancedSet();
                }
            } else {
                if(!mShowingVideoSet) {
                    if (!mContext.isRecording()) {
                        showVideoSet();
                    }
                } else {
                    hideVideoSet();
                }
            }
            return true;
        }
        return false;
    }

    public void setSharedPreferenceChangedListener(
            PhotoMenuManager.onSharedPreferenceChangedListener listener) {
        mPhotoMenuManager.setSharedPreferenceChangedListener(listener);
        mOverAllMenuManager.setSharedPreferenceChangedListener(listener);
        mVideoMenuManager.setSharedPreferenceChangedListener(listener);
        mAdvancedMenuManager.setSharedPreferenceChangedListener(listener);
    }

    public boolean handleMenuEvent() {
        boolean handle = false;
        if (isEnabled() && isShowing() && mModeIndicator != null) {
            mModeIndicator.performClick();
            handle = true;
        }
        if (LOG) {
            Log.i(TAG, "handleMenuEvent() isEnabled()=" + isEnabled() + ", isShowing()=" + isShowing()
                    + ", mModeIndicator=" + mModeIndicator + ", return " + handle);
        }
        return handle;
    }

    private void releaseSettingResource() {
        if (LOG) {
            Log.i(TAG, "releaseSettingResource()");
        }
        collapse(true);
        if (mSettingLayout != null) {
            mPager = null;
            mSettingLayout = null;
        }
    }

    public void showSetting() {
        if (mShowingMode) {
            hideCaptureMode();
        }
    }
    public void showVideoSet() {

            if (!mShowingVideoSet) {
                setShowVideoSet(true);
                mVideoMenuManager.showVideoSet();
            }
    }
    public void showAdvancedSet() {
            if (!mShowingAdvancedSet) {
                setShowAdvanedSet(true);
                mAdvancedMenuManager.showAdvanced();
            }
    }

    public void showManualMode() {
        if (!mShowingManualModel) {
            setShowingManualModel(true);
            mContext.setViewState(GC.VIEW_STATE_MANUAL_MANU);
            mContext.getCameraViewManager().getCameraManualModeManager().show();
        }
    }
    
    public void showFacebeautyMenuUp(){
    	
    	if(!mShowingFaceBeautyMenu){
    		setShowingFaceBeautyMenu(true);
    		mContext.setViewState(GC.VIEW_STATE_FACE_BEAUTY_MENU);
    		showFaceBeautyMenu();
    	}
    	
    }

	public void showMode() {
        Log.d(TAG,"###YUHUAN###showMode#mShowingMode=" + mShowingMode);
        Log.d(TAG,"###YUHUAN###showMode#isOverallMenuShowing=" + isOverallMenuShowing());
		if (!mShowingMode || !isOverallMenuShowing()) {// pr1008577-yuguan.chen
														// modified
			setShowMode(true);
			Log.d(TAG,"###YUHUAN###showMode#mShowTimes=" + mShowTimes);
			if (mShowTimes > 0) {// TODO temp add
				mShowTimes = -1;
				// mPhotoMenuManager.reInflate();
				Log.d(TAG,"###YUHUAN###showMode#before mOverAllMenuManager.reInflate===");
				mOverAllMenuManager.reInflate();
				Log.d(TAG,"###YUHUAN###showMode#after mOverAllMenuManager.reInflate===");
			}
			// mPhotoMenuManager.show();
			Log.d(TAG,"###YUHUAN###showMode#before mOverAllMenuManager.show===");
			mOverAllMenuManager.show();
			Log.d(TAG,"###YUHUAN###showMode#after mOverAllMenuManager.show===");
		}
	}

    @Override
    public void show() {//TODO temp add
        super.show();
        if(mPhotoMenuManager != null && mShowTimes == 0){
           mShowTimes++;
           mPhotoMenuManager.show();
           mPhotoMenuManager.hideswift();
        }
        
//        if(mOverAllMenuManager!=null&&mShowTimes==0){
//        	mShowTimes++;
//        	mOverAllMenuManager.show();
//        	mOverAllMenuManager.hideswift();
//        }
    }

    public void setMenuEnabled(boolean enabled) {
        mMenuEnabled = enabled;
    }
    public void setShowAdvanedSet(boolean mode) {
        mShowingAdvancedSet = mode;
    }

    public void setShowMode(boolean mode) {
        mShowingMode = mode;
    }
    public void setShowVideoSet(boolean set) {
        mShowingVideoSet = set;
    }
    public void setShowingManualModel(boolean mode) {
        mShowingManualModel = mode;
    }
    public void setShowingFaceBeautyMenu(boolean mode) {
        mShowingFaceBeautyMenu = mode;
    }

    public boolean getShowMode() {
        return mShowingMode;
    }
    public AdvancedMenuManager getAdvancedMenuManager(){
          return mAdvancedMenuManager;
    }
    public void removeSet() {
        mPhotoMenuManager.reInflate();
        mOverAllMenuManager.reInflate();
        mAdvancedMenuManager.reInflate();
        mVideoMenuManager.reInflate();
    }
    public void hideCaptureMode() {
    	Log.d(TAG,"###YUHUAN###hideCaptureMode#mShowingMode=" + mShowingMode);
        if (mShowingMode) {
            setShowMode(false);
            mModeIndicator.setImageResource(R.drawable.btn_settings);
            mPhotoMenuManager.hide();
            mOverAllMenuManager.hide();
        }
    }
    
    public void hideAdvanceSetting(){
    	if(mOverAllMenuManager!=null){
    		mOverAllMenuManager.hide();
    	}
    }
    
    public void hideviews(boolean flashpicker,boolean modepicker,boolean camerapicker) {
        if (flashpicker) {
            hideView(mFlashPicker);
        }
        if (modepicker) {
            hideView(mSettingIndicator);
        }
        if (camerapicker) {
            hideView(mCameraPicker);
        }

    }
    //Add by zhimin.yu for PR945430 begin
    public void hideSettingIndicator(){
    	Log.d(TAG,"###YUHUAN###hideSettingIndicator");
        hideView(mSettingIndicator);
    }

    public void showSettingIndicator(){
    	Log.d(TAG,"###YUHUAN###showSettingIndicator");
        showView(mSettingIndicator);
    }
    //Add by zhimin.yu for PR945430 end
    public void hideVideoSet() {
        if(mShowingVideoSet) {
            setShowVideoSet(false);
            mModeIndicator.setImageResource(R.drawable.btn_settings);
            mVideoMenuManager.hide();
        }
    }

    public void hideAdvancedSet() {
    	Log.d(TAG,"###YUHUAN###hideAdvancedSet#mShowingAdvancedSet=" + mShowingAdvancedSet);
        if(mShowingAdvancedSet) {
            mAdvancedMenuManager.hide();
            setShowAdvanedSet(false);
            mModeIndicator.setImageResource(R.drawable.btn_settings);
        }
    }

    public void hideManualModel() {
        if(mShowingManualModel) {
        	setShowingManualModel(false);
            mContext.getCameraViewManager().getCameraManualModeManager().hideManualMenu(mSettingIndicator);
        }
    }
    
    public void hideFaceBeautyMenu() {
        if(mShowingFaceBeautyMenu) {
        	setShowingFaceBeautyMenu(false);
        	hideFaceBeautyMenuUp();
        }
    }

    public void setCurrentMode(int mode) {
        if( mPhotoMenuManager != null)
            mPhotoMenuManager.setCurrentMode(mode);
    }
    public void showRecordingUI(boolean recording) {
        mRecordingStarted = recording;
        if (recording) {
            hideVideoSet();
            hideswift();
        } else {
            show();
        }
    }
    //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-22,PR764214 Begin
    public void setCameraPickerEnabled(boolean enabled){
        if(mCameraPicker != null) {
            mCameraPicker.setEnabled(enabled);
        }
    }
    //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-22,PR764214 End

    @Override
    public void onOrientationChanged(int orientation) {
        if (mPhotoMenuManager != null) {
            mPhotoMenuManager.onOrientationChanged(orientation);
        }
        if(mOverAllMenuManager!=null){
        	mOverAllMenuManager.onOrientationChanged(orientation);
        }
        if (mAdvancedMenuManager != null) {
            mAdvancedMenuManager.onOrientationChanged(orientation);
        }
        if (mVideoMenuManager != null) {
            mVideoMenuManager.onOrientationChanged(orientation);
        }
    }
    
//Add by fei.gao for Camera_animation -begin
    public void setModeListView(DrawLayout view,MenuDrawable menu){
    	view.setDrawLayoutCall(mPhotoMenuManager);
    	mOverAllMenuManager.setDrawLayout(menu);
    }

    // modify by minghui.hua for PR949603
    public void refreshMenu(){
        if (mOverAllMenuManager!=null) {
            mOverAllMenuManager.refreshValueAndView();
        }
    }
	
	public void showMenu() {
		// TODO Auto-generated method stub
		Log.d(TAG,"###YUHUAN###showMenu#mGC.isVideoModule()=" + mGC.isVideoModule());
		Log.d(TAG,"###YUHUAN###showMenu#mShowingMode=" + mShowingMode);
		
		if(!mGC.isVideoModule()){
          if(!mShowingAdvancedSet){
              //if (mContext.getCameraViewManager().getCameraManualModeManager().isShowing()) {
              if (mShowingManualModel) {
//                  mContext.setViewState(GC.VIEW_STATE_NORMAL);
//                  mContext.getCameraViewManager().getCameraManualModeManager().hide();
                  hideManualModel();
              } else if (!mShowingMode) {
                  showMode();
              } else {
                  hideCaptureMode();
              }
          } else {
              hideAdvancedSet();
          }
      } else {
          if(!mShowingVideoSet) {
              if (!mContext.isRecording()) {
                  showVideoSet();
              }
          } else {
              hideVideoSet();
          }
      }
	}
//Add by fei.gao for Camera_animation -end
	
	  @SuppressLint("NewApi")
	public void showFaceBeautyMenu() {
		  hideViewForFaceBeautyMenu();
//		  faceBeautyMenu.setTranslationX(0.0f);
		  faceBeautyMenu.setVisibility(View.VISIBLE);
		   int cx = (mFaceBeautyPicker.getLeft() + mFaceBeautyPicker.getRight()) / 2;
	        int cy = (mFaceBeautyPicker.getTop() + mFaceBeautyPicker.getBottom()) / 2;
	        float radius = ((faceBeautyMenu.getWidth()+faceBeautyMenu.getHeight())/4) * 2.0f;
	        Animator reveal=ViewAnimationUtils.createCircularReveal(faceBeautyMenu, cx, cy, 0, radius);
	        reveal.setDuration(500);
	        reveal.setInterpolator(new AccelerateDecelerateInterpolator());
	        reveal.start();
	    }
	    
	    @SuppressLint("NewApi")
		public void hideFaceBeautyMenuUp() {
	    	if(faceBeautyMenu.getTranslationX()==0){
	        int cx = (mFaceBeautyPicker.getLeft() + mFaceBeautyPicker.getRight()) / 2;
	        int cy = (mFaceBeautyPicker.getTop() + mFaceBeautyPicker.getBottom()) / 2;
	        float radius =((faceBeautyMenu.getWidth()+faceBeautyMenu.getHeight())/4) * 2.0f;
	            Animator reveal = ViewAnimationUtils.createCircularReveal(
	            		faceBeautyMenu, cx, cy, radius, 0);
	            reveal.addListener(new AnimatorListenerAdapter() {
	                @Override
	                public void onAnimationEnd(Animator animation) {
	                	if(mContext.getGC().getCurrentMode()==GC.MODE_ARCSOFT_FACEBEAUTY){
	                		showViewForFaceBeautyMenu();
	                	}
//	                	faceBeautyMenu.setTranslationX(mContext.getResources().getDisplayMetrics().heightPixels+mContext.getResources().getDimension(R.dimen.navigationbar_height));
	                	faceBeautyMenu.setVisibility(View.GONE);
	                	
	                }
	            });
	            reveal.setDuration(500);
	            reveal.setInterpolator(new AccelerateDecelerateInterpolator());
	            reveal.start();
	            
	    	}
	             
	    }
	    
	    public void  showViewForFaceBeautyMenu(){
	              showView(mFaceBeautyPicker);
	              showView(mModeIndicator);
	              showView(mCameraPicker);

	    }
	    public void  hideViewForFaceBeautyMenu(){
            hideView(mFaceBeautyPicker);
            hideView(mModeIndicator);
            hideView(mCameraPicker);

  }
	    public View getViewForHideManualModeMenu(){
	    	return mSettingIndicator;
	    }
	    
	    private void initSeekBar(){
	    	beauty_bright.initData(true,PhotoModule.faceBeauty_bright);
	    	beauty_soften.initData(false,PhotoModule.faceBeauty_soften);
	    }
	    
	    public void showViewsForTimeLapse(){
	    	showView(mModeIndicator);
	    }
	    
	    public void hideViewsForTimeLapse(){
	    	hideView(mModeIndicator);
	    }
	    
	    public void showViewsForManualMode(){
	    	showView(mModeIndicator);
	    	showView(mFlashPicker);
	    	showView(mManualPicker);
	    }
	    
	    public void hideViewsForManualMode(){
	    	hideView(mModeIndicator);
	    	hideView(mFlashPicker);
	    	hideView(mManualPicker);
	    }
	    public void setPhotoMode(PhotoModule mPhotoModule){
	    	beauty_bright.setCallBack(mPhotoModule);
	    	beauty_soften.setCallBack(mPhotoModule);
	    	initSeekBar();
	    }
       //Add by zhiming.yu for PR913890 begin
       public void setVedioCameraPickershowView(boolean enabled){
            mSetVedioCameraPickerShowView = enabled;
            if(mSetVedioCameraPickerShowView){
                showView(mCameraPicker);
            }else{
                hideView(mCameraPicker);
            }
       }
       //Add by zhiming.yu for PR913890 end
       
       public void setInitAnimationForMenu(boolean setInitAnimation){
    	   mhasShowManualMode =setInitAnimation;
       }
       
       public OverallMenuManager getOverallMenuManager(){
       	if(mOverAllMenuManager!=null){
       		return mOverAllMenuManager;
       	}else{
       		return null;
       	}
       }
       
       public boolean isInCapture(){
    	   if(mListener instanceof PhotoModule){
    		   return ((PhotoModule)mListener).getIsInCapture();
    	   }else{
    		   return false;
    	   }
//    	   return ((PhotoModule)mListener).getIsInCapture();
       }
       
       public boolean isInCountDown(){
    	   if(mListener instanceof PhotoModule){
    		   
    		   return ((PhotoModule)mListener).getIsInCountDown();
    	   }else{
    		   return false;
    	   }
       }
 	public SettingListener getListener(){
    	   return mListener;
       }
 	
 	private void faceMenuGone(){
 		if(faceBeautyMenu.getVisibility() == View.INVISIBLE){
        	mMainHandler.sendEmptyMessageDelayed(FACE_MENU_GONE,FACE_GONE_DELAY);	
        }
 	}
 	public void disableMenu(){
 		Log.d(TAG,"###YUHUAN###disableMenu#mModeIndicator= " + mModeIndicator);
 		if(mModeIndicator !=null){
 			mModeIndicator.enableFilter(false);
 			mModeIndicator.setEnabled(false);
 			
 		}
 	}
 	public void enableMenu(){
 		Log.d(TAG,"###YUHUAN###enableMenu#mModeIndicator=" + mModeIndicator);
 		if(mModeIndicator !=null){
 			mModeIndicator.enableFilter(true);
 			mModeIndicator.setEnabled(true);
 		}
 	}
}
