/*
 * Copyright (C) 2012 The Android Open Source Project
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
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* ========================================================================== */

package com.android.camera;

import com.tct.camera.R;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.android.camera.qrcode.QRCodeManager;
import android.view.View;
import android.view.View.OnClickListener;

public class QRCodeSettingsActivity extends Activity {
    private CheckBox mCheckBox1;
    private CheckBox mCheckBox2;
    private CheckBox mCheckBox3;
    private View mQRCodeArrow;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.setContentView(R.layout.qrcode_settings);
        this.initializeUI();
    }

    private void initializeUI() {
        mCheckBox1 = (CheckBox) findViewById(R.id.QRCode_SettingsCheckBox1);
        mCheckBox2 = (CheckBox) findViewById(R.id.QRCode_SettingsCheckBox2);
        mCheckBox3 = (CheckBox) findViewById(R.id.QRCode_SettingsCheckBox3);
        this.mQRCodeArrow = findViewById(R.id.QRCode_Arrow);
        this.mQRCodeArrow.setOnClickListener(new OnClickListener(){
            public void onClick(View view) {
                finish();
            }
        });
        boolean crosshairs = QRCodeManager.showCrossHairs(this);
        boolean beep = QRCodeManager.isBeep(this);
        boolean vibrate = QRCodeManager.isVibrate(this);
        if(crosshairs)
            mCheckBox1.setChecked(true);
        else
            mCheckBox1.setChecked(false);
        if(beep)
            mCheckBox2.setChecked(true);
        else
            mCheckBox2.setChecked(false);
        if(vibrate)
            mCheckBox3.setChecked(true);
        else
            mCheckBox3.setChecked(false);

        OnCheckedChangeListener listener = new OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if(button == mCheckBox1) {
                    QRCodeManager.setCrossHairs(QRCodeSettingsActivity.this, isChecked);
                } else if(button == mCheckBox2) {
                    QRCodeManager.setBeep(QRCodeSettingsActivity.this, isChecked);
                } else if(button == mCheckBox3) {
                    QRCodeManager.setVibrate(QRCodeSettingsActivity.this, isChecked);
                }
            }
        };
        mCheckBox1.setOnCheckedChangeListener(listener);
        mCheckBox2.setOnCheckedChangeListener(listener);
        mCheckBox3.setOnCheckedChangeListener(listener);
    }
}
