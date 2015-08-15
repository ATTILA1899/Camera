package com.android.camera.qrcode;

import java.io.Serializable;

public class QRCode implements Serializable{
    public final static int TYPE_URL = 0;
    public final static int TYPE_LOCATION = 1;
    public final static int TYPE_WIFI = 2;
    public final static int TYPE_CONTACT = 3;
    public final static int TYPE_PHONENUMBER = 4;
    public final static int TYPE_EMAIL = 5;
    public final static int TYPE_CALENDAR = 6;
    public final static int TYPE_SMS = 7;
    public final static int TYPE_PLAINTEXT = 8;
    public final static int TYPE_UPC = 9;

    public final static String WIFI_TYPE_WEP = "WEP";
    public final static String WIFI_TYPE_WPA = "WPA";
    public final static String WIFI_TYPE_NOPASSWORD = "nopass";

    public int type = TYPE_PLAINTEXT;
    public long time;
    public String rawText;
    // For URL Type
    public String url;
    // For Location Type
    public String geo;
    // For wifi configuration
    public String wifiType;
    public String wifiSSID;
    public String wifiPassword;
    public boolean wifiHide;
    // For contact information
    public String contactName = "";
    public String contactAddress = "";
    public String contactPhoneNumber = "";
    public String contactEmail = "";
    // For phone number
    public String phoneNumber;
    // For email
    public String email;
    // For SMS
    public String sms;
    public String smsText;
    // For plaintext
    public String text;

    public String toString() {
        return time + "\n" + rawText;
    }

    public static QRCode parseFromString(String input) {
        try{
            int index = input.indexOf("\n");
            long time = Long.parseLong(input.substring(0,index).trim());
            String rawText = input.substring(index+1).trim();
            QRCode qrcode = QRCodeParser.parse(rawText);
            qrcode.time = time;
            return qrcode;
        } catch(Exception e) {
            return null;
        }        
    }
}
