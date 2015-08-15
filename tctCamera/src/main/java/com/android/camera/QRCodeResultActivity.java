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
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.camera.qrcode.QRCode;
import com.android.camera.qrcode.QRCodeParser;
import com.android.camera.qrcode.QRCodeManager;
import android.widget.Toast;

public class QRCodeResultActivity extends Activity {
    private final static String TAG = "QRCodeResultActivity";
    private ImageView mQRCodeResultImage;
    private TextView mQRCodeTypeText;
    private TextView mQRCodeContentText;
    private TextView mQRCodeHintText;
    private View mQRCodeShareButton;
    private View mQRCodeOpenButton;
    private View mQRCodeArrow;

    private QRCode qrcode;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.setContentView(R.layout.qrcode_result);
        Intent intent = getIntent();
        qrcode = (QRCode) intent.getSerializableExtra("qrcode");
        this.initializeUI();
        showQRCode();
    }

    private void initializeUI() {
        this.mQRCodeResultImage = (ImageView) findViewById(R.id.QRCode_ResultImage);
        this.mQRCodeTypeText = (TextView) findViewById(R.id.QRCode_TypeText);
        this.mQRCodeContentText = (TextView) findViewById(R.id.QRCode_ContentText);
        this.mQRCodeHintText = (TextView) findViewById(R.id.QRCode_HintText);
        this.mQRCodeShareButton = findViewById(R.id.QRCode_ShareButton);
        this.mQRCodeOpenButton = findViewById(R.id.QRCode_OpenButton);
        this.mQRCodeArrow = findViewById(R.id.QRCode_Arrow);
        this.mQRCodeArrow.setOnClickListener(new OnClickListener(){
            public void onClick(View view) {
                finish();
            }
        });

        OnClickListener onClickListener = new OnClickListener(){
            public void onClick(View view) {
                if(view == mQRCodeShareButton) {
                    QRCodeManager.shareQRCode(QRCodeResultActivity.this, qrcode);
                } else if(view == mQRCodeOpenButton) {
                    QRCodeManager.openQRCode(QRCodeResultActivity.this, qrcode);
                }
            }
        };
        mQRCodeShareButton.setOnClickListener(onClickListener);
        mQRCodeOpenButton.setOnClickListener(onClickListener);
    }

    private void showQRCode() {
        int type = qrcode.type;
        if(type == QRCode.TYPE_URL) {
            mQRCodeTypeText.setText(R.string.qrcode_type_url);
            mQRCodeHintText.setText(R.string.qrcode_hint_url);
        } else if(type == QRCode.TYPE_LOCATION) {
            mQRCodeTypeText.setText(R.string.qrcode_type_geo);
            mQRCodeHintText.setText(R.string.qrcode_hint_geo);
        } else if(type == QRCode.TYPE_WIFI) {
            mQRCodeTypeText.setText(R.string.qrcode_type_wifi);
            mQRCodeHintText.setText(R.string.qrcode_hint_wifi);
        } else if(type == QRCode.TYPE_CONTACT) {
            mQRCodeTypeText.setText(R.string.qrcode_type_contact);
            mQRCodeHintText.setText(R.string.qrcode_hint_contact);
        } else if(type == QRCode.TYPE_PHONENUMBER) {
            mQRCodeTypeText.setText(R.string.qrcode_type_phonenumber);
            mQRCodeHintText.setText(R.string.qrcode_hint_phonenumber);
        } else if(type == QRCode.TYPE_EMAIL) {
            mQRCodeTypeText.setText(R.string.qrcode_type_email);
            mQRCodeHintText.setText(R.string.qrcode_hint_email);
        } else if(type == QRCode.TYPE_CALENDAR) {
            mQRCodeTypeText.setText(R.string.qrcode_type_calendar);
            mQRCodeHintText.setText(R.string.qrcode_hint_calendar);
        } else if(type == QRCode.TYPE_SMS) {
            mQRCodeTypeText.setText(R.string.qrcode_type_sms);
            mQRCodeHintText.setText(R.string.qrcode_hint_sms);
        } else if(type == QRCode.TYPE_PLAINTEXT) {
            mQRCodeTypeText.setText(R.string.qrcode_type_plaintext);
            mQRCodeHintText.setText(R.string.qrcode_hint_plaintext);
        }
        mQRCodeContentText.setText(qrcode.rawText);
    }
}
