package com.android.camera.qrcode;

import java.util.Comparator;

public class QRCodeComparator implements Comparator<QRCode> {
    
    public int compare(QRCode code1, QRCode code2) {
        long diff = code1.time - code2.time;
        if(diff < 0)
            return -1;
        else if(diff == 0)
            return 0;
        else
            return 1;
    }
   
}
