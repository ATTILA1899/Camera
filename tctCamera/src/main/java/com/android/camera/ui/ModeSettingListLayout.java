/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.android.camera.CameraActivity;
import com.android.camera.CameraSettings;
import com.android.camera.ListPreference;
import android.util.Log;

import com.android.camera.ui.InLineSettingItem;
import com.tct.camera.R;
import com.android.camera.GC;
import com.android.camera.SettingUtils;

import com.android.camera.PreferenceInflater;
import com.android.camera.PreferenceGroup;
import com.android.camera.ui.ListViewForScrollView;

import java.util.ArrayList;
//import com.mediatek.common.featureoption.FeatureOption;

/* A popup window that contains several camera settings. */
public class ModeSettingListLayout extends LinearLayout implements InLineSettingItem.Listener,
        AdapterView.OnItemClickListener, OnScrollListener {
    @SuppressWarnings("unused")
    private static final String TAG = "ModeSettingListLayout";
    private static final boolean LOG = true;

    private Listener mListener;
    private ArrayList<ListPreference> mListItem = new ArrayList<ListPreference>();
    private ArrayAdapter<ListPreference> mListItemAdapter;
    private InLineSettingItem mLastItem;
    private ListViewForScrollView mSettingList;
    //ming.zhang modify begin
    private String[] mKeys;
    //ming.zhang modify end

    public interface Listener {
        void onModeSettingChanged(ModeSettingListLayout settingList);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSettingList = (ListViewForScrollView) findViewById(R.id.modeList);
    }

    private class SettingsListAdapter extends ArrayAdapter<ListPreference> {
        LayoutInflater mInflater;

        public SettingsListAdapter() {
            super(ModeSettingListLayout.this.getContext(), 0, mListItem);
            mInflater = LayoutInflater.from(getContext());
        }

        private int getSettingLayoutId(ListPreference pref) {
            // If the preference is null, it will be the only item , i.e.
            // 'Restore setting' in the popup window.
            if (pref == null) {
                return R.layout.in_line_setting_restore;
            }

            // Currently, the RecordLocationPreference is the only setting
            // which applies the on/off switch.
            if (isSwitchSettingItem(pref)) {
                return R.layout.in_line_setting_switch;
            } else if (isIsoSettingItem(pref)) {
                return R.layout.in_line_setting_iso;
            } /*else if(isImageListwhiteBlanceItem(pref)){
                return R.layout.in_line_setting_whiteblance;
            } */else if(isExposureSettingItem(pref)){
                return R.layout.in_line_setting_exposure;
            } else if(isSingleChoiceSettingItem(pref)){
                return R.layout.in_line_setting_single_choice_three;
            } else if(isSingleChoiceSettingItemFour(pref)){
                return R.layout.in_line_setting_single_choice_four;
            } else if(isAdvancedSettingItem(pref)){
                return R.layout.in_line_setting_advance;
            } else if(isSingleChoiceSettingItemTwo(pref)){
                return R.layout.in_line_setting_single_choice_two;
            } else if (isBackSettingItem(pref)) {
                return R.layout.in_line_setting_back;
            }else if (isRestoreSettingItem(pref)) {
                return R.layout.in_line_setting_restore;
            }else if (isCheckBoxSettingItem(pref)) {
                return R.layout.in_line_setting_check_box;
            }

            return R.layout.in_line_setting_options;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.i(TAG,"LLLL getView       position:"+position);
            ListPreference pref = mListItem.get(position);
            if (convertView != null) {
                if (pref == null) {
                    if (!(convertView instanceof InLineSettingRestore)) {
                        convertView = null;
                    }
                }else if (isAdvancedSettingItem(pref)) {
                    if (!(convertView instanceof InLineSettingAdvanced)) {
                        convertView = null;
                    }
                }else if (isSwitchSettingItem(pref)) {
                    if (!(convertView instanceof InLineSettingSwitch)) {
                        convertView = null;
                    }
                }else if (isExposureSettingItem(pref)) {
                    if (!(convertView instanceof InLineSettingExposure)) {
                        convertView = null;
                    }
                }
                else if (isIsoSettingItem(pref)) {
                    if (!(convertView instanceof InLineSettingIso)) {
                        convertView = null;
                    }
                }
                else if (isBackSettingItem(pref)) {
                    if (!(convertView instanceof InLineSettingBack)) {
                        convertView = null;
                    }
                }
                else if(isSingleChoiceSettingItemTwo(pref)) {
                    if (!(convertView instanceof InLineSettingSingleChoiceTwo)) {
                        convertView = null;
                    }
                }
                else if (isSingleChoiceSettingItem(pref)) {
                    if (!(convertView instanceof InLineSettingSingleChoiceThree)) {
                        convertView = null;
                    }
                }
                else if (isSingleChoiceSettingItemFour(pref)) {
                    if (!(convertView instanceof InLineSettingSingleChoiceFour)) {
                        convertView = null;
                    }
                }else if (isRestoreSettingItem(pref)) {
                    if (!(convertView instanceof InLineSettingRestore)) {
                        convertView = null;
                    }
                }else if (isCheckBoxSettingItem(pref)) {
                    if (!(convertView instanceof InLineSettingCheckBox)) {
                        convertView = null;
                    }
                }
                if (convertView != null && pref != null) {
                    ((InLineSettingItem)convertView).initialize(pref);
                    SettingUtils.setEnabledState(convertView, (pref == null ? true : pref.isEnabled()));
                    return convertView;
                }
            }
            Log.i(TAG,"LLLL convertView == null");
            int viewLayoutId = getSettingLayoutId(pref);
            InLineSettingItem view = (InLineSettingItem)
                    mInflater.inflate(viewLayoutId, parent, false);
            if (viewLayoutId == R.layout.in_line_setting_restore) {
                view.setId(R.id.restore_default);
            }
            if (view != null) {
                view.initialize(pref); // no init for restore one
                view.setSettingChangedListener(ModeSettingListLayout.this);
            }
            SettingUtils.setEnabledState(convertView, (pref == null ? true : pref.isEnabled()));

            return view;
        }
    }

    //[BUGFIX]-Add by TCTNB,bin.zhang2, 2014-06-17,PR700969 Begin
    public void updateView() {
        if (mListItemAdapter != null) {
            mListItemAdapter.notifyDataSetChanged();
        }
    }
    //[BUGFIX]-Add by TCTNB,bin.zhang2, 2014-06-17,PR700969 End

    private boolean isSingleChoiceSettingItemFour(ListPreference pref){
        return CameraSettings.KEY_SELF_TIMER.equals(pref.getKey());
    }

    private boolean isSingleChoiceSettingItemTwo(ListPreference pref){
        return CameraSettings.KEY_VIDEO_FRONT_QUALITY.equals(pref.getKey())
                || CameraSettings.KEY_PICTURE_RATIO.equals(pref.getKey());
    }

    private boolean isSingleChoiceSettingItemFive(ListPreference pref){
        return CameraSettings.KEY_FOCUS_MODE.equals(pref.getKey());
    }
    private boolean isAdvancedSettingItem(ListPreference pref){
        return CameraSettings.KEY_ADVANCED_SETTING.equals(pref.getKey());
    }
    private boolean isBackSettingItem(ListPreference pref){
            return CameraSettings.KEY_BACK.equals(pref.getKey());
    }

    private boolean isRestoreSettingItem(ListPreference pref){
        return CameraSettings.KEY_RESTORE_TO_DEFAULT.equals(pref.getKey());
    }

    private boolean isSingleChoiceSettingItem(ListPreference pref){
        return  CameraSettings.KEY_PICTURE_SIZE_FLAG.equals(pref.getKey())
               ||CameraSettings.KEY_VIDEO_QUALITY.equals(pref.getKey())
               || CameraSettings.KEY_AUTOEXPOSURE.equals(pref.getKey());
    }

    private boolean isSwitchSettingItem(ListPreference pref) {
        return false;/*CameraSettings.KEY_RECORD_LOCATION.equals(pref.getKey())
                || CameraSettings.KEY_SOUND.equals(pref.getKey())
                || CameraSettings.KEY_GPS_TAG.equals(pref.getKey())
                || CameraSettings.KEY_PICTURE_RATIO.equals(pref.getKey())
                || CameraSettings.KEY_SOUND_RECORDING.equals(pref.getKey())
                || CameraSettings.KEY_VIDEO_EIS.equals(pref.getKey())
                || CameraSettings.KEY_TOUCH_SHUTTER.equals(pref.getKey())
                || CameraSettings.KEY_AUTOEXPOSURE_LOCK.equals(pref.getKey())
                || CameraSettings.KEY_CAMERA_WAV_DENOISE.equals(pref.getKey())
                || CameraSettings.KEY_VOICE_SHUTTER.equals(pref.getKey())
                || CameraSettings.KEY_VIDEO_LIMIT_FOR_MMS_KEY.equals(pref.getKey())
                || CameraSettings.KEY_ZSL.equals(pref.getKey())
                || CameraSettings.KEY_FACE_DETECTION.equals(pref.getKey())
                || CameraSettings.KEY_REFOCUS_ON_SHOOT.equals(pref.getKey());*/
    }

    private boolean isCheckBoxSettingItem(ListPreference pref){
        return CameraSettings.KEY_RECORD_LOCATION.equals(pref.getKey())
                || CameraSettings.KEY_SOUND.equals(pref.getKey())
                || CameraSettings.KEY_GPS_TAG.equals(pref.getKey())
                || CameraSettings.KEY_SOUND_RECORDING.equals(pref.getKey())
                || CameraSettings.KEY_VIDEO_EIS.equals(pref.getKey())
                || CameraSettings.KEY_TOUCH_SHUTTER.equals(pref.getKey())
                || CameraSettings.KEY_AUTOEXPOSURE_LOCK.equals(pref.getKey())
                || CameraSettings.KEY_CAMERA_WAV_DENOISE.equals(pref.getKey())
                || CameraSettings.KEY_VOICE_SHUTTER.equals(pref.getKey())
                || CameraSettings.KEY_VIDEO_LIMIT_FOR_MMS_KEY.equals(pref.getKey())
                || CameraSettings.KEY_ZSL.equals(pref.getKey())
                || CameraSettings.KEY_FACE_DETECTION.equals(pref.getKey())
                || CameraSettings.KEY_REFOCUS_ON_SHOOT.equals(pref.getKey());
    }

    private boolean isVirtualSettingItem(ListPreference pref) {
        return false;/*CameraSettings.KEY_IMAGE_PROPERTIES.equals(pref.getKey())
                || CameraSettings.KEY_FACE_BEAUTY_PROPERTIES.equals(pref.getKey());*/
    }
    // add by hui.xu@2013/3/4 begin
    private boolean isImageListSettingItem(ListPreference pref) {
        return CameraSettings.KEY_FLASH_MODE.equals(pref.getKey());
//              || CameraSettings.KEY_WHITE_BALANCE.equals(pref.getKey());
    }

    private boolean isImageListwhiteBlanceItem(ListPreference pref) {
        return  CameraSettings.KEY_WHITE_BALANCE.equals(pref.getKey());
    }

    private boolean isIsoSettingItem(ListPreference pref) {
        return CameraSettings.KEY_ISO.equals(pref.getKey());
    }

    private boolean isExposureSettingItem(ListPreference pref) {
         return CameraSettings.KEY_EXPOSURE.equals(pref.getKey());
    }


    public void setSettingChangedListener(Listener listener) {
        mListener = listener;
    }

    public ModeSettingListLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initialize(String[] keys, boolean addrestore) {
        //PR1007965- sichao .hu remove begin, remove ModeSettingList initialization , this block of codes are removed from current Camera
//        mKeys = keys;
//        CameraActivity context = (CameraActivity)getContext();
//        mListItem.clear();
//        // Prepare the setting items.
//        for (int i = 0; i < keys.length; ++i) {
//            ListPreference pref = context.getGC().getListPreference(keys[i]);
//            if (pref != null) {
//                Log.i(TAG,"LLLL pref != null pref:"+pref);
//                if (isListItemEnable(context,keys[i]))
//                    mListItem.add(pref);
//            } else {
//              Log.i(TAG,"LLLL pref == null");
//            }
//        }
//        // Prepare the restore setting line.
////        if (addrestore) {
////            mListItem.add(null);
////        }
//
//        mListItemAdapter = new SettingsListAdapter();
//        mSettingList.setAdapter(mListItemAdapter);
//        mSettingList.setOnItemClickListener(this);
//        mSettingList.setSelector(android.R.color.transparent);
//        mSettingList.setOnScrollListener(this);
        //PR1007965-sichao.hu remove end
    }
    private boolean isListItemEnable(CameraActivity context, String key) {
        if (context.getGC().getCurrentMode() == GC.MODE_NIGHT
            && key.equals(CameraSettings.KEY_EXPOSURE))
            return false;
        if (GC.isSettingEnable(GC.getSettingIndex(key)))
            return true;
        return false;
    }
    @Override
    public void onSettingChanged(InLineSettingItem item) {
        if (mLastItem != null && mLastItem != item) {
            //mLastItem.collapseChild();
        }
        if (mListener != null) {
            mListener.onModeSettingChanged(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG,"LMCH0914 position:"+position);
    }

    public void reloadPreference() {
        int count = mSettingList.getChildCount();
        for (int i = 0; i < count; i++) {
            ListPreference pref = mListItem.get(i);
            if (pref != null) {
                InLineSettingItem settingItem =
                        (InLineSettingItem) mSettingList.getChildAt(i);
                settingItem.reloadPreference();
            }
        }
    }

    @Override
    public void onDismiss(InLineSettingItem item) {
        Log.i(TAG, "onDismiss(" + item + ") mLastItem=" + mLastItem);
        mLastItem = null;
    }

    @Override
    public void onShow(InLineSettingItem item) {
        if (LOG) {
            Log.i(TAG, "onShow(" + item + ") mLastItem=" + mLastItem);
        }
        if (mLastItem != null && mLastItem != item) {
            //zsy//mLastItem.collapseChild();
        }
        mLastItem = item;
    }

    public boolean collapseChild() {
        boolean collapse = false;
        if (mLastItem != null) {
            //zsy//collapse = mLastItem.collapseChild();
        }
        if (LOG) {
            Log.i(TAG, "collapseChild() return " + collapse);
        }
        return collapse;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        if (LOG) {
            Log.i(TAG, "onScroll(" + firstVisibleItem + ", " + visibleItemCount
                    + ", " + totalItemCount + ")");
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (LOG) {
            Log.i(TAG, "onScrollStateChanged(" + scrollState + ")");
        }
        if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            collapseChild();
        }
    }
}
