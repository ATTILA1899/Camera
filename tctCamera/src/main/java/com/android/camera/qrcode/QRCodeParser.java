package com.android.camera.qrcode;

public class QRCodeParser{

    private final static String URL_PREFIX = "URLTO:";
    private final static String URL_PREFIX_2 = "urlto:";
    private final static String HTTP_PREFIX = "http://";
    private final static String HTTPS_PREFIX = "https://";
    private final static String GEO_PREFIX = "GEO:";
    private final static String GEO_PREFIX_2 = "geo:";
    private final static String WIFI_PREFIX = "WIFI:";
    private final static String WIFI_PREFIX_2 = "wifi:";
    private final static String CONTACT_PREFIX = "MECARD:";
    private final static String PHONE_PREFIX = "TEL:";
    private final static String PHONE_PREFIX_2 = "tel:";
    private final static String EMAIL_PREFIX = "MAILTO:";
    private final static String EMAIL_PREFIX_2 = "mailto:";
    private final static String SMS_PREFIX = "SMSTO:";
    private final static String SMS_PREFIX_2 = "smsto:";

    // Parse the QRCode and get QRCode object
    public static QRCode parse(String input)
    {
        QRCode code = new QRCode();
        code.type = QRCode.TYPE_PLAINTEXT;
        code.time = System.currentTimeMillis();
        code.rawText = input;
        code.text = input;
        if (input.startsWith(URL_PREFIX) || input.startsWith(URL_PREFIX_2)){
            code.type = QRCode.TYPE_URL;
            code.url = input.substring(URL_PREFIX.length());
            return code;
        }
        else if (input.startsWith(HTTP_PREFIX)){
            code.type = QRCode.TYPE_URL;
            code.url = input;
            return code;
        }
        else if (input.startsWith(HTTPS_PREFIX)){
            code.type = QRCode.TYPE_URL;
            code.url = input;
            return code;
        }
        else if (input.startsWith(GEO_PREFIX) || input.startsWith(GEO_PREFIX_2)){
            code.type = QRCode.TYPE_LOCATION;
            code.geo = input;
            return code;
        }
        else if (input.startsWith(WIFI_PREFIX) || input.startsWith(WIFI_PREFIX_2)){
            code.type = QRCode.TYPE_WIFI;
            try {
                String left = input.substring(WIFI_PREFIX.length());
                int index1 = left.indexOf("T:");
                int index2 = left.indexOf(";", index1);
                String type = left.substring(index1+2, index2);
                if(type.equals("WPA")) {
                    code.wifiType = QRCode.WIFI_TYPE_WPA;
                } else if(type.equals("WEP")) {
                    code.wifiType = QRCode.WIFI_TYPE_WEP;
                } else {
                    code.wifiType = QRCode.WIFI_TYPE_NOPASSWORD;
                }
                int index3 = left.indexOf("S:");
                int index4 = left.indexOf(";", index3);
                String ssid = left.substring(index3+2, index4);
                code.wifiSSID = ssid;
                int index5 = left.indexOf("P:");
                int index6 = left.indexOf(";", index5);
                String password = left.substring(index5+2, index6);
                code.wifiPassword = password;            
                return code;
            } catch (Exception e) {
            }            
        }
        else if (input.startsWith(CONTACT_PREFIX)){
            code.type = QRCode.TYPE_CONTACT;
            String left = input.substring(CONTACT_PREFIX.length());
            try {
                int index1 = left.indexOf("N:");
                int index2 = left.indexOf(";", index1);
                String name = left.substring(index1+2, index2);
                int index3 = left.indexOf("ADR:");
                int index4 = left.indexOf(";", index3);
                String address = left.substring(index3+4, index4);
                int index5 = left.indexOf("TEL:");
                int index6 = left.indexOf(";", index5);
                String tel = left.substring(index5+4, index6);
                int index7 = left.indexOf("EMAIL:");
                int index8 = left.indexOf(";", index7);
                String email = left.substring(index7+6, index8);
                code.contactName = name;
                code.contactAddress = address;
                code.contactPhoneNumber = tel;
                code.contactEmail = email;
                return code;
            } catch(Exception e) {
            }            
        }
        else if (input.startsWith(PHONE_PREFIX)
                || input.startsWith(PHONE_PREFIX_2)){
            code.type = QRCode.TYPE_PHONENUMBER;
            code.phoneNumber = input.substring(PHONE_PREFIX.length());
            return code;
        }
        else if (input.startsWith(EMAIL_PREFIX)
                || input.startsWith(EMAIL_PREFIX_2)){
            code.type = QRCode.TYPE_EMAIL;
            code.email = input.substring(EMAIL_PREFIX.length());
            return code;
        }
        else if (input.startsWith(SMS_PREFIX) || input.startsWith(SMS_PREFIX_2)){
            code.type = QRCode.TYPE_SMS;
            String left = input.substring(SMS_PREFIX.length());
            int index = left.indexOf(":");
            if(index >= 0) {
                code.sms = left.substring(0, index);
                code.smsText = left.substring(index+1);
            } else {
                code.sms = left;
                code.smsText = "";
            }
            return code;
        }
        return code;
    }
}
