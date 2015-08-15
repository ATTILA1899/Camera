/******************************************************************************/
/*                                                                Date:05/2013*/
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
/* 11/13/2013|     wangxiaofei      |        550266        |Setting function  */
/*           |                      |                      |still can be used */
/*           |                      |                      | during Expressi- */
/*           |                      |                      |on mode           */
/* ----------|----------------------|----------------------|----------------- */
/* 11/15/2013|   shangyong.zhang    |        551894        |Capture button sti*/
/*           |                      |                      |-ll can be used du*/
/*           |                      |                      |-ring count down. */
/* ========================================================================== */
/******************************************************************************/

package com.android.camera.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.graphics.Bitmap;


import com.android.camera.CameraActivity;
import com.tct.camera.R;
import com.android.camera.ui.CountDownView;
import com.android.camera.util.CameraUtil;
//import com.android.camera.ui.ShutterButton;
import com.android.camera.CameraModule;
import com.android.camera.GC;
import com.android.camera.PhotoModule;
import com.android.camera.TctTimelapseModule;
import com.android.camera.VideoModule;
import com.android.camera.WideAnglePanoramaModule;
import com.android.camera.manager.ExpressionThumbnailManager;
import com.android.camera.manager.InfoManager;

public class CameraViewManager extends ViewManager {
    private static final String TAG = "YUHUAN_CameraViewManager";

    private ShutterManager mShutterManager;
    private TopViewManager mTopViewManager;
    private SwitcherManager mSwitcherManager;
    private BackButtonManager mBackButtonManager;
    private PreviewGridManager mGridManager;
    private ZoomManager mZoomManager;
    private QrcodeViewManager mQrcodeViewManager;
    private InfoManager  mInfoManager;
    private ExpressionThumbnailManager mExpressionThumbnailManager;
    private RotateProgress mRotateProgress;
    private CountDownView mCountDownView;
    private CameraManualModeManager mCameraManualModeManager;
    private HelpGuideManager mHelpGuideManager;
    private RotateDialog mRotatedialog;
    private RotateSettingDialog mRotateSettingDialog;
    private RotateListDialog mRotateListDialog;
    public CameraViewManager(CameraActivity context) {
        super(context);
        initializeCommonManagers();
    }

    public void initializeCommonManagers() {
        Log.i(TAG,"###YUHUAN###initializeCommonManagers enter");
        mShutterManager = new ShutterManager(mContext,VIEW_LAYER_NORMAL);
        mCameraManualModeManager = new CameraManualModeManager(mContext,VIEW_LAYER_PREVIEW);
        mHelpGuideManager = new HelpGuideManager(mContext);
        mTopViewManager = new TopViewManager(mContext, VIEW_LAYER_NORMAL);
        mSwitcherManager = new SwitcherManager(mContext, VIEW_LAYER_NORMAL);
        mBackButtonManager=new BackButtonManager(mContext,VIEW_LAYER_NORMAL);
        mGridManager=new PreviewGridManager(mContext,VIEW_LAYER_NORMAL);
        mZoomManager = new ZoomManager(mContext,VIEW_LAYER_NORMAL);
        mQrcodeViewManager = new QrcodeViewManager(mContext,VIEW_LAYER_NORMAL);
        mInfoManager = new InfoManager(mContext,VIEW_LAYER_NORMAL);
        mRotateProgress = new RotateProgress(mContext,VIEW_LAYER_NORMAL);
        mCountDownView = new CountDownView(mContext,VIEW_LAYER_NORMAL);
        mExpressionThumbnailManager = new ExpressionThumbnailManager(mContext,VIEW_LAYER_PREVIEW);
        mRotatedialog = new RotateDialog(mContext);
        mRotateSettingDialog=new RotateSettingDialog(mContext);
        mRotateListDialog =new RotateListDialog(mContext);
        mZoomManager.init();
    }

    @Override
    public void initialize() {
        if (mShutterManager != null)
            mShutterManager.initialize();
        if (mTopViewManager != null)
            mTopViewManager.initialize();
        if (mSwitcherManager != null)
            mSwitcherManager.initialize();
        if (mZoomManager != null)
            mZoomManager.initialize();
        if(mRotateSettingDialog !=null){
        	mRotateSettingDialog.initialize();
        }
        if(mRotateListDialog !=null){
        	mRotateListDialog.initialize();
        }
    }

    @Override
    protected void onRefresh() {
        if (mShutterManager != null)
            mShutterManager.refresh();
        if (mTopViewManager != null)
            mTopViewManager.refresh();
        if (mSwitcherManager != null)
            mSwitcherManager.refresh();
        if (mQrcodeViewManager != null) {
            mQrcodeViewManager.refresh();
        }
        if (mZoomManager != null)
            mZoomManager.initialize();
        if (mExpressionThumbnailManager != null)
            mExpressionThumbnailManager.refresh();
        if(mRotateSettingDialog !=null){
        	mRotateSettingDialog.refresh();
        }
        if(mRotateListDialog !=null){
        	mRotateListDialog.refresh();
        }
    }

    @Override
    protected void onRelease() {
        if (mShutterManager != null)
            mShutterManager.release();
        if (mTopViewManager != null)
            mTopViewManager.release();
        if (mSwitcherManager != null)
            mSwitcherManager.release();
        if (mZoomManager != null)
            mZoomManager.release();
        if (mQrcodeViewManager != null)
            mQrcodeViewManager.release();
        if (mExpressionThumbnailManager != null)
            mExpressionThumbnailManager.release();
        if (mRotatedialog != null)
            mRotatedialog.release();
        if (mRotateSettingDialog != null)
        	mRotateSettingDialog.release();
        if (mRotateListDialog != null)
        	mRotateListDialog.release();
        if(mRotateSettingDialog !=null){
        	mRotateSettingDialog.release();
        }
        if(mRotateListDialog !=null){
        	mRotateListDialog.release();
        }
        mExpressionThumbnailManager = null;
        super.onRelease();
    }

    @Override
    public void resetToDefault() {
        if (mSwitcherManager != null)
            mSwitcherManager.resetToDefault();
        if (mShutterManager != null)
            mShutterManager.resetToDefault();
        if (mTopViewManager != null)
            mTopViewManager.resetToDefault();
        if (mZoomManager != null)
            mZoomManager.resetToDefault();
        if (mQrcodeViewManager != null)
            mQrcodeViewManager.resetToDefault();
        if(mRotateSettingDialog !=null){
        	mRotateSettingDialog.resetToDefault();
        }
        if(mRotateListDialog!=null){
        	mRotateListDialog.resetToDefault();
        }
        if(mCameraManualModeManager !=null){
        	  mCameraManualModeManager.resetToDefault();
         }

    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean result = false;
        if (mZoomManager != null) {
            result |= mZoomManager.dispatchTouchEvent(event);
        }
        if(mBackButtonManager != null && mGC.getCurrentMode() == GC.MODE_POLAROID && !mBackButtonManager.isInMenuArea(event)){
        	mBackButtonManager.collepsePolaroidMenu();
        }
        return result;
    }

    protected View getView() {
        View view = null;
        return view;
    }

    public void showGrid(){
        if(mGridManager!=null){
        	mGridManager.show();
        }

    }
    
    @Override
    public void show() {
        Log.i(TAG,"LMCH0410 show help me");
        super.show();
        if (mShutterManager != null)
            mShutterManager.show();
        if (mTopViewManager != null)
            mTopViewManager.show();
        if (mSwitcherManager != null) {
            if (mContext.isNonePickIntent())
                mSwitcherManager.show();
        }
        if(mBackButtonManager!=null){
        	mBackButtonManager.show();
        }
        
        if (mQrcodeViewManager != null) {
            if(mGC.getCurrentMode() == GC.MODE_QRCODE) {
                mQrcodeViewManager.show();
            } else {
                mQrcodeViewManager.hide();
            }
        }
        if (!mShowing) mShowing = true;
    }

    public void setViewState(int state) {
        Log.i(TAG,"###YUHUAN###setViewState#state=" + state);
        switch (state) {
        case GC.VIEW_STATE_COUNTDOWN_START:
            setMenuEnabled(false);
            setViewState(GC.VIEW_STATE_SHUTTER_PRESS);
            mSwitcherManager.setVisibility(false);
            break;
        case GC.VIEW_STATE_COUNTDOWN_CANCEL:
            setViewState(GC.VIEW_STATE_NORMAL);
            break;
        case GC.VIEW_STATE_NORMAL:
            setMenuEnabled(true);
            mShutterManager.setEnabled(true);
            mTopViewManager.setEnabled(true);
            mSwitcherManager.setEnabled(true);
            mSwitcherManager.setVisibility(true);
            mTopViewManager.hideFaceBeautyMenu();
            mTopViewManager.hideManualModel();
            //wxt mVoiceShootingManager.setEnabled(true);//[FEATURE]-Add-BEGIN by TCTNB.(Liu Jie),12/18/2013,550519,VoiceShooting
            if(getContext().isNonePickIntent()) {
                mSwitcherManager.show();
                //wxt mReviewThumbnailManager.show();
                //wxt mVoiceShootingManager.show();
            }
            mShutterManager.show();
            mTopViewManager.show();
            break;
        case GC.VIEW_STATE_BACK:
             mTopViewManager.hideAdvancedSet();
             mTopViewManager.showMode();
             mTopViewManager.hideFaceBeautyMenu();
            break;
        case GC.VIEW_STATE_ADVANCED:
             mTopViewManager.hideCaptureMode();
             mTopViewManager.showAdvancedSet();
             mTopViewManager.hideFaceBeautyMenu();
            break;
        case GC.VIEW_STATE_RESTORE:
            break;
        case GC.VIEW_STATE_BLANK:
            mTopViewManager.hideManualModel();
            mTopViewManager.hideCaptureMode();
            mTopViewManager.hideAdvancedSet();
            mTopViewManager.hideVideoSet();
            mTopViewManager.hideFaceBeautyMenu();
            break;
        case GC.VIEW_STATE_SET:
            break;
        case GC.VIEW_STATE_SWITCH_PHOTO_VIDEO:
            mTopViewManager.hideCaptureMode();
            mTopViewManager.hideAdvancedSet();
            mTopViewManager.hideVideoSet();
            mTopViewManager.hideFaceBeautyMenu();
          //[BUGFIX]-Add-END by TCTNB.kechao.chen,2014/08/11, PR-743380
          //[BUGFIX]-Add-END by TCTNB.kechao.chen,2014/08/22, PR-771949
            //setViewState(GC.VIEW_STATE_NORMAL);
          //[BUGFIX]-Add-END by TCTNB.kechao.chen,2014/08/22, PR-771949
          //[BUGFIX]-Add-END by TCTNB.kechao.chen,2014/08/11, PR-743380
            break;
        case GC.VIEW_STATE_HIDE_SET:
            mTopViewManager.hideCaptureMode();
            mTopViewManager.hideAdvancedSet();
            mTopViewManager.hideVideoSet();
            mTopViewManager.hideFaceBeautyMenu();
            break;
        case GC.VIEW_STATE_SHOW_SET:
            break;
        case GC.VIEW_STATE_SHUTTER_PRESS:
            // [BUGFIX]-ADD-BEGIN by TCTNB(Shangyong.Zhang), 2013/11/14, PR551894.
            // Camera button still can be used during count down.
        	int curMode = mGC.getCurrentMode();
			if (curMode != GC.MODE_PANORAMA && curMode != GC.MODE_EXPRESSION4
					&& curMode != GC.MODE_SMILE && curMode != GC.MODE_VIDEO) {
				mShutterManager.setEnabled(false);
				setMenuEnabled(false);
			} else if (mGC.getCurrentMode() == GC.MODE_EXPRESSION4) {
				setMenuEnabled(false);
			}
            mTopViewManager.setEnabled(false);
            mSwitcherManager.setEnabled(false);
            // [BUGFIX]-ADD-END by TCTNB(Shangyong.Zhang), 2013/11/14.
            mTopViewManager.hideCaptureMode();
            mTopViewManager.hideAdvancedSet();
            mTopViewManager.hideVideoSet();
//            mTopViewManager.hideFaceBeautyMenu();
            //wxt mVoiceShootingManager.hide();//[FEATURE]-Add-BEGIN by TCTNB.(Liu Jie),12/18/2013,550519,VoiceShooting
            mSwitcherManager.hide();
            mTopViewManager.hide();
            //wxt mReviewThumbnailManager.hide();
            break;
        case GC.VIEW_STATE_CAMERA_SWITCH:
            mTopViewManager.hideCaptureMode();
            mTopViewManager.hideAdvancedSet();
            mTopViewManager.hideVideoSet();
            mTopViewManager.hideFaceBeautyMenu();
            mTopViewManager.removeSet();
            break;
        case GC.VIEW_STATE_FLING:
            mTopViewManager.hideCaptureMode();
            mTopViewManager.hideAdvancedSet();
            mTopViewManager.hideVideoSet();
            mTopViewManager.hideFaceBeautyMenu();
            //wxt mZoomManager.hide();
            break;
        case GC.VIEW_STATE_MANUAL_MANU:
            //mShutterManager.hide();
            //mSwitcherManager.hide();
            mTopViewManager.hideViewsForManualMode();
            mTopViewManager.hide();
            mTopViewManager.setShowMode(false);
            break;
        case GC.VIEW_STATE_HDR:
        	mTopViewManager.hideviews(true, false, false);
        	break;
        case GC.VIEW_STATE_FACE_BEAUTY_MENU:
            //mShutterManager.hide();
            //mSwitcherManager.hide();
//            mTopViewManager.hideviews(true,true,true);
//            mTopViewManager.setShowMode(false);
            break;
        case GC.VIEW_STATE_MANUAL_MANU_HIDE:
        	mCameraManualModeManager.hide();
        	mTopViewManager.show();
        	mTopViewManager.showViewsForManualMode();
        	mSwitcherManager.show();
        	mSwitcherManager.showVideo();
        	setViewState(GC.VIEW_STATE_BLANK);
        	break;
        case GC.VIEW_STATE_UI_ORIENTATION:
        	mSwitcherManager.reInflateWithoutHide();
        	mTopViewManager.reInflateWithoutHide();
        	mBackButtonManager.reInflateWithoutHide();
        	mShutterManager.reInflateWithoutHide();
        	switch (mContext.getGC().getCurrentMode() ){
        	case GC.MODE_PHOTO:
        		mShutterManager.setOnShutterButtonListener((PhotoModule)mTopViewManager.getListener());//Add shutter's listener
        		break;
        	case GC.MODE_HDR:
        		mShutterManager.setOnShutterButtonListener((PhotoModule)mTopViewManager.getListener());//Add shutter's listener
        		break;
        	case GC.MODE_TIMELAPSE:
        		mShutterManager.setOnShutterButtonListener((TctTimelapseModule)mTopViewManager.getListener());//Add shutter's listener
        		break;
        	case GC.MODE_MANUAL:
        		mShutterManager.setOnShutterButtonListener((PhotoModule)mTopViewManager.getListener());//Add shutter's listener
        		break;
        	case GC.MODE_POLAROID:
        		mShutterManager.setOnShutterButtonListener((PhotoModule)mTopViewManager.getListener());//Add shutter's listener
        		break;
        	case GC.MODE_ARCSOFT_FACEBEAUTY:
        		mShutterManager.setOnShutterButtonListener((PhotoModule)mTopViewManager.getListener());//Add shutter's listener
        		break;
        	case GC.MODE_PANORAMA:
        		mShutterManager.setOnShutterButtonListener((WideAnglePanoramaModule)mTopViewManager.getListener());//Add shutter's listener
        		break;
        	case GC.MODE_VIDEO:
        		mShutterManager.setOnShutterButtonListener((VideoModule)mTopViewManager.getListener());//Add shutter's listener
        		break;
        		default :
        			break;
        	}
        	mTopViewManager.onRefresh();
        	break;
        case GC.VIEW_STATE_REFRESH_TOP_VIEW:
            mTopViewManager.onRefresh();
            break;
        default:
            break;
        }
    }

    @Override
    public void onInflate() {
        super.onInflate();
        //wxt mPreviewManager.reInflate();
        Log.i(TAG, "CameraViewManager onInflate()");
        mTopViewManager.reInflate();
        mCameraManualModeManager.reInflate();
        if(mGC.getCurrentMode() == GC.MODE_QRCODE) {
            Log.i(TAG, "CameraViewManager onInflate(): QRCode Mode");
            //wxt this.getViewLayer(VIEW_LAYER_NORMAL).setVisibility(View.GONE);
        } else {
            mTopViewManager.show();
            mContext.getViewLayer(VIEW_LAYER_NORMAL).setVisibility(View.VISIBLE);
        }
    }

    public boolean onBackPressed() {
//        if(mGC.getCurrentMode() == GC.MODE_QRCODE) {
//            mTopViewManager.setCurrentMode(GC.MODE_PHOTO);
//            return true;
//        }
        return mTopViewManager.onBackPressed() || mCameraManualModeManager.onBackPressed();
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mTopViewManager.onKeyDown(keyCode,event);
    }

  //[BUGFIX]-ADD-BEGIN by kechao.chen, PR-765640, 2014-8-27
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(mGC.getCurrentMode() == GC.MODE_QRCODE) {
            return mQrcodeViewManager.onKeyUp(keyCode,event);
        }else{
            return false;
        }
    }
  //[BUGFIX]-ADD-BEGIN by kechao.chen, PR-765640, 2014-8-27

    public void setMenuEnabled(boolean enabled) {
        if (mTopViewManager != null) {
            mTopViewManager.setMenuEnabled(enabled);
        }
    }
    public void showRecordingUI(boolean recording) {
        if (mTopViewManager != null)
            mTopViewManager.showRecordingUI(recording);
    }
    public ShutterManager getShutterManager() {
        return mShutterManager;
    }
    
    public BackButtonManager getBackButtonManager() {
        return mBackButtonManager;
    }
    
    public PreviewGridManager getGridManager(){
    	return mGridManager;
    }
    
    public SwitcherManager getSwitcherManager() {
        return mSwitcherManager;
    }
    public TopViewManager getTopViewManager() {
        return mTopViewManager;
    }
    public ZoomManager getZoomManager() {
        return mZoomManager;
    }
    public QrcodeViewManager getQrcodeViewManager() {
        return mQrcodeViewManager;
    }
    public InfoManager getInfoManager(){
       return mInfoManager;
    }
    public RotateProgress getRotateProgress(){
       return mRotateProgress;
    }
    public CountDownView getCountDownView(){
        return mCountDownView;
     }
    public RotateDialog getRotateDialog(){
       return mRotatedialog;
    }
    public RotateSettingDialog getRotateSettingDialog(){
        return mRotateSettingDialog;
     }
    public RotateListDialog getRotateListDialog(){
        return mRotateListDialog;
     }
    public CameraManualModeManager getCameraManualModeManager() {
        return mCameraManualModeManager;
    }

    public HelpGuideManager getHelpGuideManager() {
        return mHelpGuideManager;
    }

    public ExpressionThumbnailManager getExpressionThumbnailManager(){
        return mExpressionThumbnailManager;
    }

    public void refreshExpressionThumbnail(Bitmap bitmap) {
        if (mExpressionThumbnailManager == null) return;
        mExpressionThumbnailManager.refreshThumbnail(bitmap);
    }

    public boolean checkExpressionThumbnail(int index) {
        if (mExpressionThumbnailManager == null) return false;
        return mExpressionThumbnailManager.checkExpressionThumbnail(index);
    }

    public void showExpressionThumbnail(){
        if (mExpressionThumbnailManager == null) return;
        if (!mExpressionThumbnailManager.isShowing()) {
            mExpressionThumbnailManager.resetThumbnail2Null();
            mExpressionThumbnailManager.show();
        }
    }
    public void hideExpressionThumbnail(){
        if (mExpressionThumbnailManager == null) return;
            mExpressionThumbnailManager.hide();
    }

    public void reloadAdvancedPreference() {
        if (mTopViewManager != null &&
            mTopViewManager.getAdvancedMenuManager() != null) {
            mTopViewManager.getAdvancedMenuManager().resetToDefault();
        }
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (mTopViewManager != null) {
                mTopViewManager.onOrientationChanged(orientation);
            }
        if (mExpressionThumbnailManager != null)
           {
                mExpressionThumbnailManager.onOrientationChanged(orientation);
           }
        if(mRotateListDialog !=null){
        	mRotateListDialog.onOrientationChanged(orientation);
        }
        if (mCameraManualModeManager != null) {
        	mCameraManualModeManager.onOrientationChanged(orientation);
        }
    }
    
    //dialog is showing
    public boolean dialogInShowing(){
    	if((mRotatedialog !=null &&  mRotatedialog.isShowing()) || (mRotateSettingDialog !=null &&  mRotateSettingDialog.isShowing()) || (mRotateListDialog !=null &&  mRotateListDialog.isShowing())){
    		return true;
    	}else{
    		return false;
    	}
    	
    }
    
    public void hideDialog(){
    	if(mRotatedialog !=null &&  mRotatedialog.isShowing()){
    		mRotatedialog.hide();
    	}
    			
    	if (mRotateSettingDialog !=null &&  mRotateSettingDialog.isShowing()){
    		mRotateSettingDialog.hide();		
    	} 

    	if(mRotateListDialog !=null &&  mRotateListDialog.isShowing()){
    		mRotateListDialog.hide();		
    	}
    }
}
