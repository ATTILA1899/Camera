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
/* 2014.08.06|      jian.zhang1     |         PR754174     |Load default value*/
/*           |                      |                      |for front camera  */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.camera.manager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.android.camera.CameraActivity;

import android.util.Log;
import com.tct.camera.R;
import com.android.camera.CameraSettings;
import com.android.camera.GC;
import com.android.camera.ComboPreferences;
import com.android.camera.ui.ModeSettingListLayout;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ui.RotateLayout;
import com.android.camera.manager.PhotoMenuManager.onSharedPreferenceChangedListener;

public class VideoMenuManager extends ViewManager implements
    ModeSettingListLayout.Listener{
    private static final String TAG = "VideoMenuManager";
    private static final boolean LOG = true;


    private int mCameraIdStored = 0;
    private View view;
    private CameraActivity mContext;
    private View viewMode;
    private onSharedPreferenceChangedListener mSharedPreferenceChangedListener;
    private ModeSettingListLayout mModeSettingListLayout;
    private LinearLayout mPhotoMenuManagerVisible;
    private RotateLayout mPhotoMenuManagerRota;
    private ImageView mTitleArrowImg;
    private TextView mOkBtn;
    private String isLimit;
    private int mOrientation = -1;

    public VideoMenuManager(CameraActivity context) {
        super(context, VIEW_LAYER_SETTING);
        mContext = context;
    }


    @Override
    protected View getView() {
        /*wxt if (mGC.getDisPlayRatio() == GC.PICTURE_RATIO_5_3) {
            viewMode = inflate(R.layout.video_setting_list_5_3, VIEW_LAYER_SETTING);
        } else */{
            viewMode = inflate(R.layout.video_setting_list, VIEW_LAYER_SETTING);
        }
        mPhotoMenuManagerVisible = (LinearLayout) viewMode.findViewById(R.id.mode_picker_visible);
        mPhotoMenuManagerRota = (RotateLayout) viewMode.findViewById(R.id.mode_picker_rota);
        mModeSettingListLayout = (ModeSettingListLayout) viewMode.findViewById(R.id.modeSettingLayout);
        mModeSettingListLayout.setBackgroundResource(R.drawable.setting_bg);
        mTitleArrowImg=(ImageView)viewMode.findViewById(R.id.video_setting_title_arrow);
        viewMode.setVisibility(View.VISIBLE);
        mPhotoMenuManagerVisible.setVisibility(View.VISIBLE);
        mModeSettingListLayout.setVisibility(View.VISIBLE);
        mOkBtn = (TextView) viewMode.findViewById(R.id.video_setting_ok_btn);
        mOkBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                ((CameraActivity)mContext).setViewState(GC.VIEW_STATE_HIDE_SET);
            }
        });
        ListHolder holder;
        Log.i(TAG,"LMCH1011 getView cameraid:"+mContext.getCameraId());
        if(mContext.isVideoCaptureIntent()) {
            if(mContext.getCameraId() == GC.CAMERAID_FRONT)
            {
                holder = new ListHolder(GC.SETTING_GROUP_COMMON_FOR_MMSVIDEO_SETTING_FRONT);
            } else {
                holder = new ListHolder(GC.SETTING_GROUP_COMMON_FOR_MMSVIDEO_SETTING);
            }
        } else {
            if(mContext.getCameraId() == GC.CAMERAID_FRONT)
            {
                holder = new ListHolder(GC.SETTING_GROUP_COMMON_FOR_VIDEO_SETTING_FRONT);
            } else {
                holder = new ListHolder(GC.SETTING_GROUP_COMMON_FOR_VIDEO_SETTING);
            }
        }
        if (isLimit == null) {
            restoreLimitForMmsSet();
        }
        Log.i(TAG,"LLLL before initalize holder.mSettingKeys:"+holder.mSettingKeys);
        mModeSettingListLayout.initialize(GC.getSettingKeys(holder.mSettingKeys), false);

        return viewMode;
    }

    //[BUGFIX] Added by jian.zhang1 for PR754174 2014.08.06 Begin
    public void reloadFrontCameraPreference(){
        String[] spNames = ComboPreferences.getSharedPreferencesNames(mContext);
        int tmpLen = spNames.length;
        SharedPreferences frontCameraPreference = mContext.getSharedPreferences(spNames[tmpLen-1], Context.MODE_PRIVATE);
        Editor editor = frontCameraPreference.edit();
        editor.putString(CameraSettings.KEY_VIDEO_EIS, mContext.getResources().getString(R.string.pref_video_eis_default));
        //pr1013470-yuguan.chen removed
//        editor.putString(CameraSettings.KEY_VIDEO_FRONT_QUALITY, mContext.getResources().getString(R.string.pref_video_front_quality_default));
        editor.putString(CameraSettings.KEY_SOUND_RECORDING, mContext.getResources().getString(R.string.pref_sound_recording_def_value));
        editor.apply();
        editor.commit();
    }
    //[BUGFIX] Added by jian.zhang1 for PR754174 2014.08.06 End

    @Override
    public void resetToDefault() {
        // Reload preference to update UI.
        if (mModeSettingListLayout != null)
            mModeSettingListLayout.reloadPreference();
        //[BUGFIX] Added by jian.zhang1 for PR754174 2014.08.06 Begin
        reloadFrontCameraPreference();
        //[BUGFIX] Added by jian.zhang1 for PR754174 2014.08.06 End
        restoreLimitForMmsSet();
    }
    private void restoreLimitForMmsSet() {
//        if (!mContext.getResources().getBoolean(R.bool.def_camera_tf_enable)) return;
        isLimit = mContext.getPreferences().getString(CameraSettings.KEY_VIDEO_LIMIT_FOR_MMS_KEY,
            mContext.getString(R.string.pref_video_limit_for_mms_default));
        boolean isLimitedSwitchOn;
        isLimitedSwitchOn = isLimit.equals(mContext.getString(R.string.setting_on_value));
        if (isLimitedSwitchOn) {
            GC.setSettingEnable(GC.ROW_SETTING_VIDEO_QUALITY,false);
            GC.setSettingEnable(GC.ROW_SETTING_VIDEO_FRONT_QUALITY,false);
        } else {
            GC.setSettingEnable(GC.ROW_SETTING_VIDEO_QUALITY,true);
//            if (mContext.getResources().getBoolean(R.bool.feature_camera_front_video_quality_set_enable)) {
                GC.setSettingEnable(GC.ROW_SETTING_VIDEO_FRONT_QUALITY,true);
//            }
        }
    }
    public void show() {
        super.show();
        if(mOrientation==-1){
            updateLayout(0);
        }else{
            updateLayout(mOrientation);
        }
        mModeSettingListLayout.setSettingChangedListener(VideoMenuManager.this);
    }
    public void setSharedPreferenceChangedListener(onSharedPreferenceChangedListener l) {
        mSharedPreferenceChangedListener = l;
    }

    public void showVideoSet(){
            restoreLimitForMmsSet();
            this.show();
    }
    public void showVideoSetWhenChanged(boolean on){
            if (on) {
                GC.setSettingEnable(GC.ROW_SETTING_VIDEO_QUALITY,false);
                GC.setSettingEnable(GC.ROW_SETTING_VIDEO_FRONT_QUALITY,false);
            } else {
                GC.setSettingEnable(GC.ROW_SETTING_VIDEO_QUALITY,true);
//                if (mContext.getResources().getBoolean(R.bool.feature_camera_front_video_quality_set_enable)) {
                    GC.setSettingEnable(GC.ROW_SETTING_VIDEO_FRONT_QUALITY,true);
//                }
            }
            reInflate();
            this.show();
    }
    @Override
    public void onModeSettingChanged(ModeSettingListLayout settingList) {
        // TODO Auto-generated method stub
        Log.i(TAG,"LMCH0914 onModeSettingChanged enter");
        if (mSharedPreferenceChangedListener != null) {
            mSharedPreferenceChangedListener.onSharedPreferenceChanged();
        }
        refresh();
    }
    private class ListHolder {
        int[] mSettingKeys;
        public ListHolder(int[] keys) {
            mSettingKeys = keys;
        }
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (mOrientation != orientation) {
            mOrientation = orientation;
            updateLayout(mOrientation);
        }
    }

    private void updateLayout(int orientation) {
        if(mTitleArrowImg == null){
            return;
        }
        if (orientation == 0 ) {
            mTitleArrowImg.setBackgroundResource(R.drawable.setting_title_arrow_port);
        }else if(orientation == 180){
            mTitleArrowImg.setBackgroundResource(R.drawable.setting_title_arrow_land_green);
        }else {
            mTitleArrowImg.setBackgroundResource(R.drawable.setting_title_arrow_land);
        }
    }

}
