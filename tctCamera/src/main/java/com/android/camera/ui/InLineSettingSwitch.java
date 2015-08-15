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

package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.android.camera.ListPreference;
import com.android.camera.CameraSettings;
import com.android.camera.CameraActivity;
import android.util.Log;
import com.tct.camera.R;
//[BUGFIX]-Add by TCTNB.(zsy,wh),03/02/2014,608031
//wxt import com.android.camera.TctMutex;

/* A switch setting control which turns on/off the setting. */
public class InLineSettingSwitch extends InLineSettingItem {
    private static final String TAG = "InLineSettingSwitch";
    private static final boolean LOG = true;
    private Switch mSwitch;
    //[BUGFIX]-Add by TCTNJ.(wei.wang3),07/02/2013,453392,
    public static boolean settingswitch = false;
    // add by yashuang.mu@tcl.com for Switch button state change area begin.
    InLineSettingSwitch mLayoutSwitch;
    CameraActivity mContext;
    private GestureDetector mGestureDetector = new GestureDetector(new myOnGestureListener());
    // add by yashuang.mu@tcl.com for Switch button state change area end.
    OnTouchListener mOnTouchListener= new OnTouchListener(){

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //[BUGFIX]-Add by TCTNB.(zsy,wh),03/02/2014,608031
            //wxt if(TctMutex.isTakingPicture()) return true;
            // TODO Auto-generated method stub
            //[BUGFIX]-Add by TCTNJ.(wei.wang3),07/02/2013,453392,
            settingswitch = true;
            return mGestureDetector.onTouchEvent(event);
        }

    };

    OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean desiredState) {
            if (mPreference.getKey().equals(CameraSettings.KEY_VIDEO_LIMIT_FOR_MMS_KEY)) {
                //wxt mContext.getCameraViewManager().getTopViewManager().getVideoMenuManager().showVideoSetWhenChanged(desiredState);
            }
            changeIndex(desiredState ? 1 : 0);
        }
    };

    public InLineSettingSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = (CameraActivity) context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSwitch = (Switch) findViewById(R.id.setting_switch);
        mSwitch.setOnCheckedChangeListener(mCheckedChangeListener);
        //set the Track and Thumb Drawable.
        mSwitch.setTrackDrawable((getContext().getResources().getDrawable(R.drawable.btn_mode_switch_on)));
        mSwitch.setThumbDrawable((getContext().getResources().getDrawable(R.drawable.btn_mode_switch)));
        mSwitch.setTextOn("   ");
        mSwitch.setTextOff("   ");
        mSwitch.setSwitchMinWidth(mSwitch.getTrackDrawable().getMinimumWidth());
        setOnClickListener(mOnClickListener);
        // add for Switch button state change area begin.
        mLayoutSwitch = (InLineSettingSwitch) findViewById(R.id.layout_setting_switch);
        mLayoutSwitch.setOnTouchListener(mOnTouchListener);
        mSwitch.setOnTouchListener(mOnTouchListener);
    }

    @Override
    public void initialize(ListPreference preference) {
        super.initialize(preference);
        // Add content descriptions for the increment and decrement buttons.
        mSwitch.setContentDescription(getContext().getResources().getString(
                R.string.accessibility_switch, mPreference.getTitle()));
        //int index = mPreference.findIndexOfValue(mPreference.getValue());
        //TctLog.d(TAG,"zsy924 initialize::mPreference.getDefaultValue():"+mPreference.getValue()+"  index:"+index);
        //changeIndex(index);
        updateView();
    }

    @Override
    protected void updateView() {
        mSwitch.setOnCheckedChangeListener(null);
        mOverrideValue = mPreference.getOverrideValue();
        if (mOverrideValue == null) {
            mSwitch.setChecked(mIndex == 1);
        } else {
            int index = mPreference.findIndexOfValue(mOverrideValue);
            mSwitch.setChecked(index == 1);
        }
        //update the background Drawable, for different view.
        if (mSwitch.isChecked()) {
            mSwitch.setTrackDrawable((getContext().getResources().getDrawable(R.drawable.btn_mode_switch_on)));
            mSwitch.setThumbDrawable((getContext().getResources().getDrawable(R.drawable.btn_mode_switch)));
        } else {
            mSwitch.setTrackDrawable((getContext().getResources().getDrawable(R.drawable.btn_mode_switch_off)));
            mSwitch.setThumbDrawable((getContext().getResources().getDrawable(R.drawable.btn_mode_switchoff)));
        }

        setEnabled(mPreference.isEnabled());
        mSwitch.setOnCheckedChangeListener(mCheckedChangeListener);
    }

    @Override
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
            //[BUGFIX]-Add by TCTNB.(zsy,wh),03/02/2014,608031
            //wxt if(TctMutex.isTakingPicture()) return;
            if (LOG) {
                Log.i(TAG, "WXT onClick() mPreference=" + mPreference);
            }
            Log.i(TAG,"LMCH0914 WXT switch onClick mPreference:"+mPreference);
            if (mPreference != null && mPreference.isClickable() && mPreference.isEnabled()) {
                if (mListener != null) {
                    mListener.onShow(InLineSettingSwitch.this);
                }
                if (mSwitch != null) {
                    mSwitch.performClick();
                }
            }
        }
    };

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
