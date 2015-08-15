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
/* Author :  feiqiang.cheng                                                   */
/* Email  :  feiqiang.chen@tct-nj.com                                         */
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
/* 08/15/2013|feiqiang.cheng        |500352                |About the camera  */
/*           |                      |                      |advanced settings */
/*           |                      |                      |                  */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.camera.ListPreference;
import com.tct.camera.R;
import android.util.Log;
import android.view.View;
import com.android.camera.CameraActivity;
import com.android.camera.GC;
/* A restore setting is simply showing the restore title. */
public class InLineSettingAdvanced extends InLineSettingItem {
    private static final String TAG="InLineSettingAdvanced";
    private TextView mAdvancedSettingTV;
    private Context mContext;

    public InLineSettingAdvanced(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
    }


    @Override
    protected void setTitle(ListPreference preference) {
        mAdvancedSettingTV.setText(
                getContext().getString(R.string.pref_advanced_detail));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAdvancedSettingTV=((TextView) findViewById(R.id.title));
    }

    @Override
    public void initialize(ListPreference preference) {
        // Add content descriptions for the increment and decrement buttons.
        Log.d(TAG,"initialize:preference"+preference);
        setTitle(preference);
        mAdvancedSettingTV.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
               ((CameraActivity)mContext).setViewState(GC.VIEW_STATE_ADVANCED);
            }
        });
    }

    @Override
    protected void updateView() { }
}
