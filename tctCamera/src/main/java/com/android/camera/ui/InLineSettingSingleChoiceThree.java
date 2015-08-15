/*
 * Copyright (C) 2011 The Android Open Source Project
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
/* ----------|----------------------|----------------------|----------------- */
/* 12/27/2013|       Hui.Shen       |       PR-575596      |Modify some visual*/
/*           |                      |                      |issues.           */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/
package com.android.camera.ui;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

import com.android.camera.CameraActivity;
import com.android.camera.CameraSettings;
import com.android.camera.GC;
import com.android.camera.ListPreference;
import com.tct.camera.R;
import com.android.camera.SettingUtils;

/* A switch setting control which turns on/off the setting. */
public class InLineSettingSingleChoiceThree extends InLineSettingItem {
    private static final String TAG = "InLineSettingSingleChoiceThree";
    private static final boolean LOG = true;
    private Button mBtnChoice1;
    private Button mBtnChoice2;
    private Button mBtnChoice3;
    private Context mContext;
    private CameraActivity mCameraContext;

    OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean desiredState) {
            changeIndex(desiredState ? 1 : 0);
        }
    };

    public InLineSettingSingleChoiceThree(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        mCameraContext = (CameraActivity)context;
    }

    @Override
    protected void onFinishInflate() {
        Log.d(TAG,"onFinishInflate");
        super.onFinishInflate();
        mBtnChoice1=(Button)findViewById(R.id.btn_choice1);
        mBtnChoice2=(Button)findViewById(R.id.btn_choice2);
        mBtnChoice3=(Button)findViewById(R.id.btn_choice3);
        mBtnChoice1.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(mIndex!=0){
                    mIndex=0;
                    changeIndex(mIndex);
                }
            }});
        mBtnChoice2.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(mIndex!=1){
                    mIndex=1;
                    changeIndex(mIndex);
                }
             }});
        mBtnChoice3.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(mIndex!=2){
                    mIndex=2;
                    changeIndex(mIndex);
                }
             }});
    }

    @Override
    public void initialize(ListPreference preference) {
        initButtonText(preference);
        super.initialize(preference);
    }

    private void initButtonText(ListPreference preference) {
//       if ( mContext.getResources().getBoolean(R.bool.def_camera_for_picturesizename_enable)
//            && preference.getKey().equals(CameraSettings.KEY_PICTURE_SIZE_FLAG)) {
//            int[] picturename = mCameraContext.getGC().getPictureSizenames();
//            mBtnChoice1.setText(mContext.getString(picturename[0]));
//            mBtnChoice2.setText(mContext.getString(picturename[1]));
//            mBtnChoice3.setText(mContext.getString(picturename[2]));
//        } else 
//        	if ( mContext.getResources().getBoolean(R.bool.def_camera_for_picturesizename_enable)
//            && preference.getKey().equals(CameraSettings.KEY_VIDEO_QUALITY)) {
//            String[] videoname = mCameraContext.getGC().getVideoSizenames();
//            if (videoname == null) return;
//            mBtnChoice1.setText(videoname[0]);
//            mBtnChoice2.setText(videoname[1]);
//            mBtnChoice3.setText(videoname[2]);
//        } else {
            mBtnChoice1.setText(preference.getKey().equals(CameraSettings.KEY_VIDEO_QUALITY)?mContext.getString(R.string.pref_video_quality_entry_superfine):preference.getEntries()[0]);
            mBtnChoice2.setText(preference.getKey().equals(CameraSettings.KEY_VIDEO_QUALITY)?mContext.getString(R.string.pref_video_quality_entry_fine):preference.getEntries()[1]);
            mBtnChoice3.setText(preference.getKey().equals(CameraSettings.KEY_VIDEO_QUALITY)?mContext.getString(R.string.pref_video_quality_entry_normal):preference.getEntries()[2]);
//        }
    }

    @Override
    protected void updateView() {
        if (mIndex >= mPreference.getEntryValues().length || mIndex < 0) { return ; }
        Log.d(TAG,"LMCH1010 updateView:mIndex="+mIndex);
        //[BUGFIX]-Mod-BEGIN by TCTNB(Hui.Shen),12/27/2013, PR575596. Modify some visual issues.
        switch(mIndex){
        case 0:
            highLight(mBtnChoice1,0);
            normalLight(mBtnChoice2,1);
            normalLight(mBtnChoice3,2);
            break;
        case 1:
            highLight(mBtnChoice2,1);
            normalLight(mBtnChoice3,2);
            normalLight(mBtnChoice1,0);
            break;
        case 2:
            highLight(mBtnChoice3,2);
            normalLight(mBtnChoice2,1);
            normalLight(mBtnChoice1,0);
            break;
        }
        //[BUGFIX]-Mod-END by TCTNB(Hui.Shen),12/27/2013, PR575596.
    }

    private void highLight(Button btn,int location){
        //btn.setTextColor(SettingUtils.getMainColor(getContext()));
        switch(location){
        case 0:
            //btn.setBackgroundResource(R.drawable.press_leftt);
            btn.setSelected(true);
            break;
        case 1:
            //btn.setBackgroundResource(R.drawable.press_middle);
            btn.setSelected(true);
            break;
        case 2:
            //btn.setBackgroundResource(R.drawable.press_right);
            btn.setSelected(true);
            break;
        }
    }

    //[BUGFIX]-Mod-BEGIN by TCTNB(Hui.Shen), 12/27/2013, PR575596. Modify some camera visual issues.
    private void normalLight(Button btn, int location){
        //btn.setTextColor(mContext.getResources().getColor(R.color.setting_item_text_color_normal));
        //btn.setBackgroundResource(R.drawable.normal);
        switch(location){
        case 0:
            //btn.setBackgroundResource(R.drawable.normal_left);
            btn.setSelected(false);
            break;
        case 1:
           // btn.setBackgroundResource(R.drawable.normal_middle);
            btn.setSelected(false);
            break;
        case 2:
            //btn.setBackgroundResource(R.drawable.normal_right);
            btn.setSelected(false);
            break;
        }
    }
    //[BUGFIX]-Mod-END by TCTNB(Hui.Shen), 12/27/2013, PR575596.
}
