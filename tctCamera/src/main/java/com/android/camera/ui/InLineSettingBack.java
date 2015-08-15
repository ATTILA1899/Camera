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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import com.android.camera.ListPreference;
import com.tct.camera.R;
import android.view.View;
import com.android.camera.CameraActivity;
import com.android.camera.GC;

/* A restore setting is simply showing the restore title. */
public class InLineSettingBack extends InLineSettingItem {
	private static final String TAG="InLineSettingAdvanced";
	private TextView mInLineSettingBackTV;
	private Context mContext;

    public InLineSettingBack(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
    }


    @Override
    protected void setTitle(ListPreference preference) {
        mInLineSettingBackTV.setText(
                getContext().getString(R.string.pref_back_detail));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInLineSettingBackTV=((TextView) findViewById(R.id.title));
    }

    @Override
    public void initialize(ListPreference preference) {
        // Add content descriptions for the increment and decrement buttons.
        Log.d(TAG,"initialize:preference"+preference);
        setTitle(preference);
        mInLineSettingBackTV.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                ((CameraActivity)mContext).setViewState(GC.VIEW_STATE_BACK);
            }
        });
    }

    @Override
    protected void updateView() { }
}
