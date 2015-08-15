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
import android.widget.LinearLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import com.android.camera.qrcode.QRCodeManager;
import com.android.camera.qrcode.QRCode;
import java.util.ArrayList;
import android.view.LayoutInflater;
import java.util.Calendar;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.widget.ScrollView;

public class QRCodeHistoryActivity extends Activity {
    private LinearLayout mQRCodeContainer;
    private View mQRCodeScrollView;
    private View mQRCodeNoHistoryView;
    private View mQRCodeArrow;
    private LayoutInflater mLayoutInflater;
    private ArrayList<QRCode> mQRCodeList;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.setContentView(R.layout.qrcode_history);
        this.mLayoutInflater = LayoutInflater.from(this);
        this.initializeUI();
        this.showQRCodeList();
    }

    private void initializeUI() {
        this.mQRCodeContainer = (LinearLayout) findViewById(R.id.QRCodeContainer);
        this.mQRCodeScrollView = (ScrollView) findViewById(R.id.QRCodeScrollView);
        this.mQRCodeNoHistoryView = (TextView) findViewById(R.id.QRCodeNoHistoryText);
        this.mQRCodeArrow = findViewById(R.id.QRCode_Arrow);
        this.mQRCodeArrow.setOnClickListener(new OnClickListener(){
            public void onClick(View view) {
                finish();
            }
        });
    }

    //Display all QRCodes in LinearLayout
    private void showQRCodeList() {
        mQRCodeContainer.removeAllViews();
        mQRCodeList = QRCodeManager.getQRCodeList(this);
        if(mQRCodeList.size() == 0) {
            mQRCodeScrollView.setVisibility(View.GONE);
            mQRCodeNoHistoryView.setVisibility(View.VISIBLE);
        } else {
            mQRCodeScrollView.setVisibility(View.VISIBLE);
            mQRCodeNoHistoryView.setVisibility(View.GONE);
        }
        for(int i=mQRCodeList.size()-1; i>=0; i--) {
            final QRCode qrcode = mQRCodeList.get(i);
            final View view = mLayoutInflater.inflate(R.layout.qrcode_history_item, null);
            mQRCodeContainer.addView(view);
            ImageView iconImageView = (ImageView) view.findViewById(R.id.QRCodeHistory_Icon);
            ImageView deleteImageView = (ImageView) view.findViewById(R.id.QRCodeHistory_Delete);
            TextView contextTextView = (TextView) view.findViewById(R.id.QRCodeHistory_ContentText);
            TextView typeTextView = (TextView) view.findViewById(R.id.QRCodeHistory_TypeText);
            TextView dateTextView = (TextView) view.findViewById(R.id.QRCodeHistory_DateText);
            int type = qrcode.type;
            if(type != QRCode.TYPE_UPC)
                iconImageView.setImageResource(R.drawable.qrcode_title);
            else
                iconImageView.setImageResource(R.drawable.qrcode_icon_barcode);
            contextTextView.setText(qrcode.rawText);
            if(type == QRCode.TYPE_URL) {
                typeTextView.setText(R.string.qrcode_type_url);
            } else if(type == QRCode.TYPE_LOCATION) {
                typeTextView.setText(R.string.qrcode_type_geo);
            } else if(type == QRCode.TYPE_WIFI) {
                typeTextView.setText(R.string.qrcode_type_wifi);
            } else if(type == QRCode.TYPE_CONTACT) {
                typeTextView.setText(R.string.qrcode_type_contact);
            } else if(type == QRCode.TYPE_PHONENUMBER) {
                typeTextView.setText(R.string.qrcode_type_phonenumber);
            } else if(type == QRCode.TYPE_EMAIL) {
                typeTextView.setText(R.string.qrcode_type_email);
            } else if(type == QRCode.TYPE_CALENDAR) {
                typeTextView.setText(R.string.qrcode_type_calendar);
            } else if(type == QRCode.TYPE_SMS) {
                typeTextView.setText(R.string.qrcode_type_sms);
            } else if(type == QRCode.TYPE_PLAINTEXT) {
                typeTextView.setText(R.string.qrcode_type_plaintext);
            }
            dateTextView.setText(getDateDisplay(qrcode.time));
            deleteImageView.setOnClickListener(new OnClickListener(){
                public void onClick(View view) {
                    QRCodeManager.deleteQRCode(QRCodeHistoryActivity.this, qrcode);
                    showQRCodeList();
                }
            });
            view.setOnLongClickListener(new OnLongClickListener(){
                public boolean onLongClick(View view) {
                    showItemMenu(qrcode);
                    return true;
                }
            });
            view.setOnClickListener(new OnClickListener(){
                public void onClick(View view) {
                    if(QRCodeManager.shouldGotoDetailPage(qrcode)) {
                        Intent intent = new Intent(QRCodeHistoryActivity.this, com.android.camera.QRCodeResultActivity.class);
                        intent.putExtra("qrcode", qrcode);
                        startActivity(intent);
                    } else {
                        QRCodeManager.openQRCode(QRCodeHistoryActivity.this, qrcode);
                    }
                }
            });
        }
    }

    private void showItemMenu(final QRCode qrcode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        String[] items = new String[3];
        items[0] = getString(R.string.qrcode_history_open);
        items[1] = getString(R.string.qrcode_history_share);
        items[2] = getString(R.string.qrcode_history_clearall);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0) {
                    //Here the behaviour is same as after successful scan
                    if(QRCodeManager.shouldGotoDetailPage(qrcode)) {
                        Intent intent = new Intent(QRCodeHistoryActivity.this, com.android.camera.QRCodeResultActivity.class);
                        intent.putExtra("qrcode", qrcode);
                        startActivity(intent);
                    } else {
                        QRCodeManager.openQRCode(QRCodeHistoryActivity.this, qrcode);
                    }
                } else if(which == 1) {
                    QRCodeManager.shareQRCode(QRCodeHistoryActivity.this, qrcode);
                } else if(which == 2) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(QRCodeHistoryActivity.this);
                    builder.setTitle(R.string.qrcode_dialog_deletetitle);
                    builder.setMessage(R.string.qrcode_dialog_confirmdelete);
                    builder.setPositiveButton(R.string.qrcode_dialog_ok, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int id) {
                            QRCodeManager.clearAll(QRCodeHistoryActivity.this);
                            showQRCodeList();
                        }
                    });
                    builder.setNegativeButton(R.string.qrcode_dialog_cancel, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    builder.create().show();
                }
            }
        });
        builder.create().show();
    }

    //Get a string representing the date <br>
    //TODO use TextFormat for better performance
    private String getDateDisplay(long time) {
        Calendar now = Calendar.getInstance();
        int nowYear = now.get(Calendar.YEAR);
        int nowMonth = now.get(Calendar.MONTH)+1;
        int nowDay = now.get(Calendar.DAY_OF_MONTH);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) +1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        if(nowYear == year && nowMonth == month && nowDay == day) {
            StringBuilder sb = new StringBuilder();
            if(hour < 10)
                sb.append("0");
            sb.append(String.valueOf(hour));
            sb.append(":");
            if(minute < 10)
                sb.append("0");
            sb.append(String.valueOf(minute));
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(year);
            sb.append(".");
            if(month < 10)
                sb.append("0");
            sb.append(String.valueOf(month));
            sb.append(".");
            if(day < 10)
                sb.append("0");
            sb.append(String.valueOf(day));
            return sb.toString();
        }
    }
}
