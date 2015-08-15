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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.widget.TextView;

import com.android.camera.GC;
import com.android.camera.ListPreference;
import com.tct.camera.R;

import android.util.Log;
import android.view.View;

import com.android.camera.CameraActivity;

/* Just used for resetToDefault */
public class InLineSettingRestore extends InLineSettingItem {
    private static final String TAG = "InLineSettingRestore";
    private TextView mRestoreDefaultTV;
    private onResetPreferenceListener mListener;
    private Context mContext;

    public InLineSettingRestore(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public interface onResetPreferenceListener {
        public void onResetPre();
    }

    public void setListener(onResetPreferenceListener listener) {
        mListener = listener;
    }

    @Override
    protected void setTitle(ListPreference preference) {
        mRestoreDefaultTV.setText(getContext().getString(
                R.string.pref_restore_detail));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRestoreDefaultTV = ((TextView) findViewById(R.id.title));
    }

    @Override
    public void initialize(ListPreference preference) {
        // Add content descriptions for the increment and decrement buttons.
        Log.d(TAG, "initialize:preference" + preference);
        setTitle(preference);
        this.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // PR874091-lan.shan-001 Modify begin
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        ((CameraActivity) mContext).resetToDefault();
                    }
                };

                ((CameraActivity) mContext).getCameraViewManager().getRotateSettingDialog().showAlertDialog(
                        mContext.getString(R.string.restore_settings_dialog_title),
                        mContext.getString(R.string.restore_settings_dialog_msg),
                        mContext.getString(R.string.dialog_ok), runnable,
                        mContext.getString(R.string.location_service_dialog_cancel), null,R.drawable.ic_dialog_alert_material);

//                new AlertDialog.Builder(mContext)
//                        .setIconAttribute(android.R.attr.alertDialogIcon)
//                        .setTitle(R.string.restore_settings_dialog_title)
//                        .setMessage(R.string.restore_settings_dialog_msg)
//                        .setPositiveButton(
//                                R.string.dialog_ok,
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog,
//                                            int arg1) {
//                                        ((CameraActivity) mContext).resetToDefault();
//                                    }
//                                })
//                        .setNegativeButton(
//                                R.string.location_service_dialog_cancel,
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog,
//                                            int arg1) {
//                                        dialog.cancel();
//                                    }
//                                }).show();
                // PR874091-lan.shan-001 Modify end
            }
        });
    }

    @Override
    protected void updateView() {
    }
}
