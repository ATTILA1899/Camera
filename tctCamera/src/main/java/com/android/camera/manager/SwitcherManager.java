/******************************************************************************/
/*                                                                Date:06/2013*/
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
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/


package com.android.camera.manager;

import android.content.Intent;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

import com.android.camera.CameraActivity;

import com.android.camera.GC;
import com.android.camera.Storage;
import com.android.camera.VideoModule;
import com.android.camera.ui.RotateImageView;
import com.android.camera.util.IntentHelper;
import com.tct.camera.R;

public class SwitcherManager extends ViewManager implements
        View.OnClickListener {
    private static final String TAG = "SwitcherManager";
    private static final boolean LOG = true;

    private LinearLayout mSwitcher;
    private RotateImageView mSwitcherPhotoVideo;
    private RotateImageView mSwitcherBack;
    private CameraActivity mContext;
    public static final int STATE_OF_VIDEO = 1;
    public static final int STATE_OF_PHOTO = 0;
    private static final int SWITCHER_CHANGED = 0;
    private static int mCurrentState = STATE_OF_PHOTO;
    private static final String PIC_URI = "content://media/external/";
    private static final String PIC_TYPE = "image/*";
    public View view = null;
    
    public static boolean flag = false;


    public SwitcherManager(CameraActivity context) {
        super(context);
        mContext = context;
        setFileter(false);
    }

    public SwitcherManager(CameraActivity context, int layer) {
        super(context, layer);
        mContext = context;
        setFileter(false);
    }

    @Override
    public void initialize() {
        if (mSwitcher != null) {
            mSwitcher.setOnClickListener(this);
        }
    }

    public static int getCameraModule() {
        return mCurrentState;
    }

    @Override
    protected View getView() {
        view = inflate(R.layout.photo_video_switcher);
        mSwitcher = (LinearLayout) view.findViewById(R.id.camera_switcher);
        mSwitcherPhotoVideo = (RotateImageView) view.findViewById(R.id.camera_video_switch_icon);
        mSwitcherBack=(RotateImageView)view.findViewById(R.id.camera_back_switch_icon);
        mSwitcherBack.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					mSwitcherBack.setImageResource(R.drawable.btn_shutter_back_pressed);
					break;
				case MotionEvent.ACTION_UP:
					mSwitcherBack.setImageResource(R.drawable.btn_shutter_back_normal);
					mContext.onBackPressed();
		            
					break;
					default:
						break;
				}
				return true;
			}
		});
        
        // flag the loading event
        initialize();
        return view;
    }

    public void hideVideo(){
    	if(mSwitcher!=null){
    		mSwitcher.setVisibility(View.GONE);
    	}
    }
    
    public void showVideo(){
    	if(mSwitcher!=null){
    		mSwitcher.setVisibility(View.VISIBLE);
    	}
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mSwitcher != null) {
            mSwitcher.setEnabled(enabled);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mSwitcher) {
            if (mGC.getCurrentMode() != GC.MODE_VIDEO && mGC.getCurrentMode() != GC.MODE_POLAROID) {
                if (mContext.pubGetStorageSpaceBytes() <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
                    return;
                }
                mContext.switchPhotoVideo(GC.MODE_VIDEO);
            }else if(mGC.getCurrentMode() == GC.MODE_POLAROID){
            	//start to gallery
            	try{
	            	mContext.startActivityForResult(IntentHelper.getGalleryIntent(mContext),
	                        CameraActivity.REQ_CODE_BACK_TO_VIEWFINDER);
            	}catch(Exception e){
            		
            	}
            } else {
            	VideoModule mVideoModule = mContext.getVideoModule();
            	if(mVideoModule != null){
                	mVideoModule.onStopVideoRecording();
            	}
            	mContext.switchPhotoVideo(GC.MODE_PHOTO);
            }
            
            mContext.setViewState(GC.VIEW_STATE_SWITCH_PHOTO_VIDEO);
        }
    }

    public void setVisibility(boolean visibility) {
        /**
         * if (mSwitchControlPanelLayout != null) { if(visibility)
         * mSwitchControlPanelLayout.setVisibility(View.VISIBLE); else
         * mSwitchControlPanelLayout.setVisibility(View.GONE); }
         **/
    }

    @Override
    protected void onRefresh() {
        if (isCameraSwitchedSupported()) {
            showView(view);
        } else {
            hideView(view);
            return;
        }
        if (mSwitcher != null) {
            if (mGC.getCurrentMode() != GC.MODE_VIDEO && mGC.getCurrentMode() != GC.MODE_POLAROID) {
                mSwitcherPhotoVideo.setImageResource(R.drawable.btn_shutter_normal_video);
            }else if(mGC.getCurrentMode() == GC.MODE_POLAROID){
            	mSwitcherPhotoVideo.setImageResource(R.drawable.btn_shutter_polaroid_gallery);
            } else {
                mSwitcherPhotoVideo
                        .setImageResource(R.drawable.btn_shutter_video_record);
            }
        }
        // recover the init value
    }
    @Override
    public void show() {
        if (isCameraSwitchedSupported()) super.show();
    }

    private boolean isCameraSwitchedSupported() {
        boolean result = mGC.getCurrentMode() != GC.MODE_QRCODE
            && !mContext.getCameraViewManager().getCameraManualModeManager().isShowing();
        return result;
    }



    public void showSwitcher() {
        show();
    }

    public boolean isPaused() {
        if (mContext != null /* && mContext.isPaused() */) {
            return true;
        }
        return false;
    }

}