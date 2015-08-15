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
/* 11/27/2013|       Hui.Shen       |       PR-575596      |modify some visual*/
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
import com.android.camera.ListPreference;
import com.tct.camera.R;
import com.android.camera.CameraActivity;
import com.android.camera.CameraSettings;
import com.android.camera.GC;
import com.android.camera.SettingUtils;

/* A switch setting control which turns on/off the setting. */
public class InLineSettingSingleChoiceFour extends InLineSettingItem {
    private static final String TAG = "InLineSettingSingleChoiceFour";
    private static final boolean LOG = true;
    private Button mBtnChoice1;
    private Button mBtnChoice2;
    private Button mBtnChoice3;
    private Button mBtnChoice4;
    private Context mContext;
    private CameraActivity mCameraContext; //[BUGFIX]-ADD by xuan.zhou, PR-618743, 2014-3-14
    private GestureDetector mGestureDetector = new GestureDetector(new myOnGestureListener());

    OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean desiredState) {
            changeIndex(desiredState ? 1 : 0);
        }
    };

    public InLineSettingSingleChoiceFour(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        //[BUGFIX]-ADD-BEGIN by xuan.zhou, PR-618743, 2014-3-14
        mCameraContext = (CameraActivity) context;
        //[BUGFIX]-ADD-END by xuan.zhou, PR-618743, 2014-3-14
    }

    @Override
    protected void onFinishInflate() {
        Log.d(TAG,"onFinishInflate");
        super.onFinishInflate();
        mBtnChoice1=(Button)findViewById(R.id.btn_choice1);
        mBtnChoice2=(Button)findViewById(R.id.btn_choice2);
        mBtnChoice3=(Button)findViewById(R.id.btn_choice3);
        mBtnChoice4=(Button)findViewById(R.id.btn_choice4);
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
        mBtnChoice4.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(mIndex!=3){
                    mIndex=3;
                    changeIndex(mIndex);
                }
            }});
    }

    @Override
    public void initialize(ListPreference preference) {
        Log.d(TAG,"initialize");
        super.initialize(preference);
        if(preference.getEntries().length>=4){
            Log.d(TAG,
                    "initialize:" + preference.getEntries()[0] + ","
                            + preference.getEntries()[1] + ","
                            + preference.getEntries()[2] + ","
                            + preference.getEntries()[3]);
            mBtnChoice1.setText(preference.getEntries()[0]);
            mBtnChoice2.setText(preference.getEntries()[1]);
            mBtnChoice3.setText(preference.getEntries()[2]);
            mBtnChoice4.setText(preference.getEntries()[3]);
        }
    }

    @Override
    protected void updateView() {
        //[BUGFIX]-ADD-BEGIN by xuan.zhou, PR-618743, 2014-3-14
        //If it's in smile mode, timer doesn't work, lowlight it.
        if (isTimerInSmileMode()) {
            disableTimer();
            return;
        } else {
            enableTimer();
        }
        //[BUGFIX]-ADD-END by xuan.zhou, PR-618743, 2014-3-14
        if (mIndex >= mPreference.getEntryValues().length || mIndex < 0) { return ; }
        switch(mIndex){
        case 0:
            highLight(mBtnChoice1,0);
            normalLight(mBtnChoice2,1);
            normalLight(mBtnChoice3,2);
            normalLight(mBtnChoice4,3);
           break;
        case 1:
            highLight(mBtnChoice2,1);
            normalLight(mBtnChoice1,0);
            normalLight(mBtnChoice3,2);
            normalLight(mBtnChoice4,3);
           break;
        case 2:
            highLight(mBtnChoice3,2);
            normalLight(mBtnChoice2,1);
            normalLight(mBtnChoice1,0);
            normalLight(mBtnChoice4,3);
            break;
        case 3:
            highLight(mBtnChoice4,3);
            normalLight(mBtnChoice3,2);
            normalLight(mBtnChoice2,1);
            normalLight(mBtnChoice1,0);
            break;
        }
    }

    //[BUGFIX]-ADD-BEGIN by xuan.zhou, PR-618743, 2014-3-14
    private boolean isTimerInSmileMode() {
        if (!CameraSettings.KEY_SELF_TIMER.equals(mPreference.getKey())) {
            return false;
        }
        return mCameraContext.getGC().getCurrentMode() == GC.MODE_SMILE;
    }

    private void disableTimer() {
        mBtnChoice1.setEnabled(false);
        mBtnChoice2.setEnabled(false);
        mBtnChoice3.setEnabled(false);
        mBtnChoice4.setEnabled(false);

        this.setAlpha(0.6f);
    }

    private void enableTimer() {
        mBtnChoice1.setEnabled(true);
        mBtnChoice2.setEnabled(true);
        mBtnChoice3.setEnabled(true);
        mBtnChoice4.setEnabled(true);

        this.setAlpha(1.0f);
    }
    //[BUGFIX]-ADD-END by xuan.zhou, PR-618743, 2014-3-14

    private void highLight(Button btn,int location){
        //btn.setTextColor(SettingUtils.getMainColor(getContext()));
        switch(location){
        case 0:
            //btn.setBackgroundResource(R.drawable.press_leftt);
            btn.setSelected(true);
            break;
        case 1:
        case 2:
            //btn.setBackgroundResource(R.drawable.press_middle);
            btn.setSelected(true);
            break;
        case 3:
            //btn.setBackgroundResource(R.drawable.press_right);
            btn.setSelected(true);
            break;
        }
    }

    //[BUGFIX]-Mod-BEGIN by TCTNB(Hui.Shen), PR-575596, modify some camera visual issues.
    private void normalLight(Button btn, int location){
        //btn.setTextColor(mContext.getResources().getColor(R.color.setting_item_text_color_normal));
        //btn.setBackgroundResource(R.drawable.normal);
        switch(location){
        case 0:
            //btn.setBackgroundResource(R.drawable.normal_left);
            btn.setSelected(false);
            break;
        case 1:
            //btn.setBackgroundResource(R.drawable.normal_middle);
            btn.setSelected(false);
            break;
        case 2:
            //btn.setBackgroundResource(R.drawable.normal_middle);
            btn.setSelected(false);
            break;
        case 3:
            //btn.setBackgroundResource(R.drawable.normal_right);
            btn.setSelected(false);
            break;
        }

    }
    //[BUGFIX]-Mod-END by TCTNB(Hui.Shen), PR-575596

    /*@Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mSwitch != null) {
            mSwitch.setEnabled(enabled);
        }
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
        event.getText().add(mPreference.getTitle());
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (LOG) {
                Log.v(TAG, "onClick() mPreference=" + mPreference);
            }
            if (mPreference != null && mPreference.isClickable() && mPreference.isEnabled()) {
                if (mListener != null) {
                    mListener.onShow(InLineSettingSwitch.this);
                }
                if (mSwitch != null) {
                    mSwitch.performClick();
                }
            }
        }
    };*/

    // add by yashuang.mu@tcl.com for Switch button state change area begin.
    private class myOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // TODO Auto-generated method stub
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // TODO Auto-generated method stub
            //[BUGFIX]-Del-BEGIN by TCTNJ.(wei.wang3),07/02/2013, PR-453392,
            /*float x = e2.getX() - e1.getX();
            float y = e2.getY() - e1.getY();
            float x_abs = Math.abs(x);
            float y_abs = Math.abs(y);
            if (x_abs >= y_abs) {
                // gesture left or right
                if (x > 0) {
                    // right
                    mSwitch.setChecked(true);
                    return true;
                } else if (x < 0) {
                    // left
                    mSwitch.setChecked(false);
                    return true;
                }

            }*/
            //[BUGFIX]-Del-END  by TCTNJ.(wei.wang3)
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // TODO Auto-generated method stub
            return false;
        }
    }
    // add by yashuang.mu@tcl.com for Switch button state change area end.

}
