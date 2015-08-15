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
public class InLineSettingSingleChoiceTwo extends InLineSettingItem {
    private static final String TAG = "InLineSettingSingleChoiceTwo";
    private static final boolean LOG = true;
    private Button mBtnChoice1;
    private Button mBtnChoice2;
    private Context mContext;
    private CameraActivity mCameraContext;


    OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean desiredState) {
            changeIndex(desiredState ? 1 : 0);
        }
    };

    public InLineSettingSingleChoiceTwo(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        mCameraContext = (CameraActivity) context;

    }

    @Override
    protected void onFinishInflate() {
        Log.d(TAG,"onFinishInflate");
        super.onFinishInflate();
        mBtnChoice1=(Button)findViewById(R.id.btn_choice1);
        mBtnChoice2=(Button)findViewById(R.id.btn_choice2);
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
    }

    @Override
    public void initialize(ListPreference preference) {
//        if(mContext.getResources().getBoolean(R.bool.def_camera_for_picturesizename_enable)
//            && preference.getKey().equals(CameraSettings.KEY_VIDEO_FRONT_QUALITY)){
//            if (  mCameraContext.getGC().isVideoFront2M()) {
//                mBtnChoice1.setText(GC.VIDEO_SIZE_FRONTCAMERA_2M[0]);
//                mBtnChoice2.setText(GC.VIDEO_SIZE_FRONTCAMERA_2M[1]);
//            } else {
//                mBtnChoice1.setText(GC.VIDEO_SIZE_FRONTCAMERA_0_3M[0]);
//                mBtnChoice2.setText(GC.VIDEO_SIZE_FRONTCAMERA_0_3M[1]);
//            }
//        } else{
            if(preference.getKey().equals(CameraSettings.KEY_PICTURE_RATIO)){
                mBtnChoice1.setText(mContext.getString(R.string.pref_camera_picturesize_ratio_entry_16_9));
                mBtnChoice2.setText(mContext.getString(R.string.pref_camera_picturesize_ratio_entry_4_3));
            }else{
                mBtnChoice1.setText(mContext.getString(R.string.pref_video_quality_entry_fine));
                mBtnChoice2.setText(mContext.getString(R.string.pref_video_quality_entry_normal));
            }
//        }
        super.initialize(preference);
    }

    @Override
    protected void updateView() {
        if (mIndex >= mPreference.getEntryValues().length || mIndex < 0) { return ; }
        //[BUGFIX]-Mod-BEGIN by TCTNB(Hui.Shen), 12/27/2013, PR575596, modify some camera visual issues.
        switch(mIndex){
        case 0:
            highLight(mBtnChoice1,0);
            normalLight(mBtnChoice2,1);
            break;
        case 1:
            highLight(mBtnChoice2,1);
            normalLight(mBtnChoice1,0);
            break;
        }
        //[BUGFIX]-Mod-END by TCTNB(Hui.Shen), PR575596
    }

    private void highLight(Button btn,int location){
        switch(location){
        case 0:
            //btn.setBackgroundResource(R.drawable.press_leftt);
            btn.setSelected(true);
            break;
        case 1:
            //btn.setBackgroundResource(R.drawable.press_right);
            btn.setSelected(true);
            break;
        }
    }

    //[BUGFIX]-Mod-BEGIN by TCTNB(Hui.Shen) 12/27/2013, PR575596,Modify some camera visual issues.
    private void normalLight(Button btn, int location){
        switch(location){
        case 0:
            //btn.setBackgroundResource(R.drawable.normal_left);
            btn.setSelected(false);
            break;
        case 1:
            //btn.setBackgroundResource(R.drawable.normal_right);
            btn.setSelected(false);
            break;
        }
    }
    //[BUGFIX]-Mod-END by TCTNB(Hui.Shen) 12/27/2013, PR575596
}
