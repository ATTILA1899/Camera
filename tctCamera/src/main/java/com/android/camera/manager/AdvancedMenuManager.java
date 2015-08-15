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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.android.camera.CameraActivity;

import android.util.DisplayMetrics;
import android.util.Log;
//import com.android.camera.FeatureSwitcher;
//import com.android.camera.ModeChecker;
import com.tct.camera.R;
//import com.android.camera.Util;
import com.android.camera.GC;
import com.android.camera.ui.ModeSettingListLayout;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ui.RotateLayout;
import com.android.camera.manager.PhotoMenuManager.onSharedPreferenceChangedListener;
//import com.android.camera.ui.SettingListLayout;

public class AdvancedMenuManager extends ViewManager implements
    ModeSettingListLayout.Listener{
    private static final String TAG = "YUHUAN_AdvancedMenuManager";

    private int mCameraIdStored = 0;
    private View view;
    private CameraActivity mContext;
    private View viewMode;
    private onSharedPreferenceChangedListener mSharedPreferenceChangedListener;
    private ModeSettingListLayout mModeSettingListLayout;
    private LinearLayout mPhotoMenuManagerVisible;
    private RotateLayout mPhotoMenuManagerRota;
    private String oldTargetRatio;
    private TextView mBackBtn;
    private TextView mOKBtn;
    private LinearLayout mPhotoMenuTitle;
    private ImageView mTitleArrowImg;

    private int mOrientation = -1;
    private boolean needUpdateLayout = false;
    private ScrollView mSettingScrollView;

    public AdvancedMenuManager(CameraActivity context) {
        super(context, VIEW_LAYER_SETTING);
        mContext = context;
    }


    @Override
    protected View getView() {

        /*wxt if (mGC.getDisPlayRatio() == GC.PICTURE_RATIO_5_3) {
            viewMode = inflate(R.layout.advanced_setting_list_5_3, VIEW_LAYER_SETTING);
        } else */{
            viewMode = inflate(R.layout.advanced_setting_list, VIEW_LAYER_SETTING);
        }
        mPhotoMenuTitle=(LinearLayout)viewMode.findViewById(R.id.setting_menu_title);
        mTitleArrowImg=(ImageView)viewMode.findViewById(R.id.setting_title_arrow);
        mPhotoMenuManagerVisible = (LinearLayout) viewMode.findViewById(R.id.mode_picker_visible);
        mPhotoMenuManagerRota = (RotateLayout) viewMode.findViewById(R.id.mode_picker_rota);
        mModeSettingListLayout = (ModeSettingListLayout) viewMode.findViewById(R.id.modeSettingLayout);

        mSettingScrollView=(ScrollView)viewMode.findViewById(R.id.photo_basicsetting_scrollview);
        mSettingScrollView.setBackgroundResource(R.drawable.setting_bg);
        mBackBtn = (TextView)viewMode.findViewById(R.id.setting_back_btn);
        mOKBtn = (TextView)viewMode.findViewById(R.id.setting_ok_btn);

        mBackBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                ((CameraActivity)mContext).setViewState(GC.VIEW_STATE_BACK);
            }
        });

        mOKBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                ((CameraActivity)mContext).setViewState(GC.VIEW_STATE_HIDE_SET);
            }
        });

        viewMode.setVisibility(View.VISIBLE);

        mPhotoMenuManagerVisible.setVisibility(View.VISIBLE);

        mModeSettingListLayout.setVisibility(View.VISIBLE);
        ListHolder holder;
        if(mContext.getCameraId() == 1)
        {
            holder = new ListHolder(GC.SETTING_GROUP_COMMON_SETTING_FRONT);
        } else {
            holder = new ListHolder(GC.getCommonAdvanceSetting());
        }
        Log.e(TAG,"LLLL before initalize holder.mSettingKeys:"+holder.mSettingKeys);
        mModeSettingListLayout.initialize(GC.getSettingKeys(holder.mSettingKeys), false);

        return viewMode;
    }

    @Override
    public void resetToDefault() {
        // Reload preference to update UI.
        if (mModeSettingListLayout != null)
            mModeSettingListLayout.reloadPreference();
    }

    public void show() {
        super.show();
        if(mOrientation==-1){
            updateLayout(0);
        }else{
            updateLayout(mOrientation);
        }
        mModeSettingListLayout.setSettingChangedListener(AdvancedMenuManager.this);
        //[BUGFIX]-Add by TCTNB,bin.zhang2, 2014-06-17,PR700969 Begin
        mModeSettingListLayout.updateView();
        //[BUGFIX]-Add by TCTNB,bin.zhang2, 2014-06-17,PR700969 End
    }

    @Override
    public void onRefresh() {
        needUpdateLayout=true;
    }

    public void setSharedPreferenceChangedListener(onSharedPreferenceChangedListener l) {
        mSharedPreferenceChangedListener = l;
    }

    public void showAdvanced(){
            String targetRatio = mContext.getGC().getPreviewRatio();
            if (!targetRatio.equals(oldTargetRatio))
                reInflate();
            oldTargetRatio = targetRatio;
            this.show();
    }

    @Override
    public void onModeSettingChanged(ModeSettingListLayout settingList) {
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
        if (mOrientation != orientation || needUpdateLayout) {
            needUpdateLayout = false;
            mOrientation = orientation;
            updateLayout(mOrientation);
        }
    }

    private void updateLayout(int orientation) {
    	DisplayMetrics dm = new DisplayMetrics();
    	mContext.getWindowManager().getDefaultDisplay()
        .getMetrics(dm);
    	float scaledDensity = dm.scaledDensity;
        if(mSettingScrollView==null || mPhotoMenuTitle == null){
            return;
        }
        if (orientation == 0 ) {
            mTitleArrowImg.setBackgroundResource(R.drawable.setting_title_arrow_port);
        }else if(orientation == 180){
            mTitleArrowImg.setBackgroundResource(R.drawable.setting_title_arrow_land_green);
        }else {
            mTitleArrowImg.setBackgroundResource(R.drawable.setting_title_arrow_land);
        }
        //LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams lp= (LayoutParams) mSettingScrollView.getLayoutParams();
            if (orientation == 0 || orientation == 180) {
                if(GC.isSettingEnable(GC.ROW_SETTING_SHUTTER_SOUND)){
                    lp.height = (int) (280*scaledDensity);
                }else{
                    lp.height= (int) (245*scaledDensity);
                }
                mSettingScrollView.setLayoutParams(lp);
            }else{
                lp.height = (int) (245*scaledDensity);
                mSettingScrollView.setLayoutParams(lp);
                mSettingScrollView.smoothScrollTo(0, 0);
            }
    }
}
