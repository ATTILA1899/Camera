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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import com.android.camera.ListPreference;
import com.tct.camera.R;
import com.android.camera.SettingUtils;
import com.android.camera.CameraSettings;
import com.android.camera.GC;

public class InLineSettingIso extends InLineSettingItem {
    private static final String TAG = "InLineSettingIso";
    private static final boolean LOG = true;

    private SeekBar mSeekBar;

    private Context mContext;

    public InLineSettingIso(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
    }

    @Override
    protected void onFinishInflate() {
        Log.d(TAG,"onFinishInflate");
        super.onFinishInflate();
        mSeekBar =(SeekBar)findViewById(R.id.zoom_control);
        mSeekBar.setOnSeekBarChangeListener(mskinToneSeekListener);
   }

    @Override
    public void initialize(ListPreference preference) {
        Log.d(TAG,"initialize");
        super.initialize(preference);
    }

    @Override
    protected void updateView() {
        int length = mPreference.getEntryValues().length;
        mSeekBar.setProgress((int)((float)mIndex/ (length-1) * mSeekBar.getMax()));
    }
  private OnSeekBarChangeListener mskinToneSeekListener = new OnSeekBarChangeListener() {
    public void onStartTrackingTouch(SeekBar bar) {
    // no support
    }

    public void onProgressChanged(SeekBar bar, int progress, boolean fromtouch) {
        Log.i(TAG,"LMCH onProgressChanged progress:"+progress);

    }

    public void onStopTrackingTouch(SeekBar bar) {
        int process = bar.getProgress();
        int max = bar.getMax();
        CharSequence[] values = mPreference.getEntryValues();
        int index = (int)((float)process/max * (values.length - 1) + 0.5);
        changeIndex(index);
        bar.setProgress((int)((float)index / (values.length-1) * max));
    }
};
}
