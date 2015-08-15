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
package com.android.camera.qrcode;

import com.android.camera.util.IntentHelper;
import com.tct.camera.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.content.Intent;
import java.util.ArrayList;
import java.util.Collections;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

public class QRCodeManager {
    private final static String TAG = "QRCodeManager";
    private final static String KEY_QRCODE_SETTINGS = "qrcode_settings";
    private final static String KEY_QRCODE_CROSSHAIRS = "qrcode_cross_hairs";
    private final static String KEY_QRCODE_BEEP = "qrcode_beep";
    private final static String KEY_QRCODE_VIBRATE = "qrcode_vibrate";
    private final static String KEY_QRCODE_HISTORY = "qrcode_history";
    private static ArrayList<QRCode> qrcodeList = null;

    private final static boolean DEFAULT_CROSSHAIRS = true;
    private final static boolean DEFAULT_BEEP = true;
    private final static boolean DEFAULT_VIBRATE = false;
    //Load the qrcode to qrcodeList ArrayList
    private static void loadHistory(Context context) {
        SharedPreferences sps = context.getSharedPreferences(KEY_QRCODE_SETTINGS, 0);
        String contents = sps.getString(KEY_QRCODE_HISTORY, "");
        qrcodeList = new ArrayList<QRCode>();
        try {
            String[] ss = contents.split("~~");
            for(String s : ss) {
                QRCode qrcode = QRCode.parseFromString(s);
                if(qrcode != null)
                    qrcodeList.add(qrcode);
            }
            QRCodeComparator comparator = new QRCodeComparator();
            Collections.sort(qrcodeList, comparator);
        } catch (Exception e) {
        }
    }

    public static ArrayList<QRCode> getQRCodeList(Context context) {
        if(qrcodeList == null)
            loadHistory(context);
        return qrcodeList;
    }

    private static void saveHistory(Context context) {
        SharedPreferences sps = context.getSharedPreferences(KEY_QRCODE_SETTINGS, 0);
        StringBuilder sb = new StringBuilder();
        if(qrcodeList != null) {
            for(int i=0; i<qrcodeList.size(); i++) {
                QRCode qrcode = qrcodeList.get(i);
                sb.append(qrcode.toString());
                if(i!=qrcodeList.size()-1)
                    sb.append("~~");
            }
        }
        sps.edit().putString(KEY_QRCODE_HISTORY, sb.toString()).commit();
    }

    public static void addQRCode(Context context, QRCode qrcode) {
        if(qrcodeList == null)
            loadHistory(context);
        if(!contains(qrcode)) {
            qrcodeList.add(qrcode);
            saveHistory(context);
        }
    }

    //TODO TBD check whether already contains the QRcode with same text<br>
    //TBD currently it will remove duplicate QRCode
    private static boolean contains(QRCode qrcode) {
        for(QRCode code : qrcodeList) {
            if(qrcode.rawText.equals(code.rawText))
                return true;
        }
        return false;
    }

    public static void deleteQRCode(Context context, QRCode qrcode) {
        if(qrcodeList == null)
            loadHistory(context);
        qrcodeList.remove(qrcode);
        saveHistory(context);
    }

    public static void clearAll(Context context) {
        if(qrcodeList == null)
            loadHistory(context);
        qrcodeList.clear();
        saveHistory(context);
    }

    //Activity call this method to share qrcode
    public static void shareQRCode(Context context, QRCode code) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.qrcode_share));
        intent.putExtra(Intent.EXTRA_TEXT, code.rawText);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.qrcode_share)));
    }

    //Check whether it should goto detail page
    public static boolean shouldGotoDetailPage(QRCode code) {
        int type = code.type;
        if(type == QRCode.TYPE_URL || type == QRCode.TYPE_PLAINTEXT
            || type == QRCode.TYPE_LOCATION || type == QRCode.TYPE_WIFI)
            return true;
        return false;
    }

    //Activity call this method to open qrcode<br>
    //It will do corresponding operation according to QRCode type
    public static void openQRCode(Context context, QRCode code) {
        Log.i(TAG, "OpenQRCode "+code.rawText);
        int type = code.type;
        if(type == QRCode.TYPE_URL) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(code.url));
            context.startActivity(intent);
        } else if(type == QRCode.TYPE_PLAINTEXT) {
            Intent intent = new Intent(IntentHelper.NOTE_ACTION);
            intent.addCategory("android.intent.category.DEFAULT");
            intent.putExtra("text", code.rawText);
            try{
              context.startActivity(intent);
            }catch(Exception e){
                intent = new Intent(IntentHelper.NOTE_ACTION_OLD);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.putExtra("text", code.rawText);
                try{
                    context.startActivity(intent);
                }catch(Exception ex){
                    Toast.makeText(context, R.string.missing_app,Toast.LENGTH_LONG).show();
                }
            }
        }else if(type == QRCode.TYPE_LOCATION) {
            Uri uri = Uri.parse(code.geo);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } else if(type == QRCode.TYPE_WIFI) {
            String wifiType = code.wifiType;
            String wifiSSID = code.wifiSSID;
            String wifiPassword = code.wifiPassword;
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = "\""+wifiSSID+"\"";
            if(wifiType.equals(QRCode.WIFI_TYPE_NOPASSWORD)) {
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfig.allowedAuthAlgorithms.clear();
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            } else if(wifiType.equals(QRCode.WIFI_TYPE_WEP)) {
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                wifiConfig.wepKeys[0] = "\""+wifiPassword+"\"";
            } else if(wifiType.equals(QRCode.WIFI_TYPE_WPA)) {
                wifiConfig.preSharedKey = "\""+wifiPassword+"\"";
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                //wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                //wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            }
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiManager.addNetwork(wifiConfig);
            Toast.makeText(context, R.string.qrcode_toast_wifi, Toast.LENGTH_SHORT).show();
        } else if(type == QRCode.TYPE_CONTACT) {
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            intent.putExtra(ContactsContract.Intents.Insert.NAME, code.contactName);
            intent.putExtra(ContactsContract.Intents.Insert.POSTAL, code.contactAddress);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, code.contactPhoneNumber);
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, code.contactEmail);
            context.startActivity(intent);
        } else if(type == QRCode.TYPE_PHONENUMBER) {
            Uri uri = Uri.parse("tel:"+code.phoneNumber);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } else if(type == QRCode.TYPE_EMAIL) {
            Uri uri = Uri.parse("mailto:"+code.email);
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            context.startActivity(intent);
        } else if(type == QRCode.TYPE_CALENDAR) {
        } else if(type == QRCode.TYPE_SMS) {
            Uri uri = Uri.parse("smsto:"+code.sms);
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            intent.putExtra("sms_body", code.smsText);
            context.startActivity(intent);
        }
    }

    public static boolean showCrossHairs(Context context) {
        SharedPreferences sps = context.getSharedPreferences(KEY_QRCODE_SETTINGS, 0);
        return sps.getBoolean(KEY_QRCODE_CROSSHAIRS, DEFAULT_CROSSHAIRS);
    }

    public static boolean isBeep(Context context) {
        SharedPreferences sps = context.getSharedPreferences(KEY_QRCODE_SETTINGS, 0);
        return sps.getBoolean(KEY_QRCODE_BEEP, DEFAULT_BEEP);
    }

    public static boolean isVibrate(Context context) {
        SharedPreferences sps = context.getSharedPreferences(KEY_QRCODE_SETTINGS, 0);
        return sps.getBoolean(KEY_QRCODE_VIBRATE, DEFAULT_VIBRATE);
    }

    public static void setCrossHairs(Context context, boolean b) {
        SharedPreferences sps = context.getSharedPreferences(KEY_QRCODE_SETTINGS, 0);
        sps.edit().putBoolean(KEY_QRCODE_CROSSHAIRS, b).commit();
    }

    public static void setBeep(Context context, boolean b) {
        SharedPreferences sps = context.getSharedPreferences(KEY_QRCODE_SETTINGS, 0);
        sps.edit().putBoolean(KEY_QRCODE_BEEP, b).commit();
    }

    public static void setVibrate(Context context, boolean b) {
        SharedPreferences sps = context.getSharedPreferences(KEY_QRCODE_SETTINGS, 0);
        sps.edit().putBoolean(KEY_QRCODE_VIBRATE, b).commit();
    }
    public static void resetPrefsToDefault(Context context) {
        SharedPreferences sps = context.getSharedPreferences(KEY_QRCODE_SETTINGS, 0);
        sps.edit().putBoolean(KEY_QRCODE_CROSSHAIRS, DEFAULT_CROSSHAIRS).commit();
        sps.edit().putBoolean(KEY_QRCODE_BEEP, DEFAULT_BEEP).commit();
        sps.edit().putBoolean(KEY_QRCODE_VIBRATE, DEFAULT_VIBRATE).commit();
    }
}
