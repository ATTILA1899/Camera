/* Copyright (c) 2014, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.android.camera;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.StatFs;
import android.os.Environment;
//import android.os.storage.StorageVolume;
//import android.os.storage.IMountService;
//import android.os.ServiceManager;
import android.util.Log;

public class SDCard {
    private static final String TAG = "YUHUAN_SDCard";

    private static final int VOLUME_SDCARD_INDEX = 1;
//    private IMountService mMountService = null;
//    private StorageVolume mVolume = null;
    private String path = null;
    private String rawpath = null;
    private static SDCard sSDCard;
    private String sdcardRoot = null;
    private boolean isDefaultStorage = false;

    public boolean isWriteable() {
//        if (mVolume == null) return false;
        final String state = getSDCardStorageState();
        Log.i(TAG, "nice SDcardState= " + state);
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public String getDirectory() {
//        if (mVolume == null) {
//            return null;
//        }
//        if (path == null) {
//            path = mVolume.getPath() + "/DCIM/Camera";
//        }
//        path = "/storage/sdcard1" + "/DCIM/Camera";
        path = sdcardRoot + "/DCIM/Camera";
        return path;
    }

    public String getRawDirectory() {
//        if (mVolume == null) {
//            return null;
//        }
//        if (rawpath == null) {
//            rawpath = mVolume.getPath() + "/DCIM/Camera/raw";
//        }
//        path = "/storage/sdcard1" + "/DCIM/Camera/raw";
        path = sdcardRoot + "/DCIM/Camera/raw";
        return rawpath;
    }

    public static synchronized SDCard instance() {
        if (sSDCard == null) {
            sSDCard = new SDCard();
        }
        return sSDCard;
    }

    @SuppressLint("NewApi")
    private String getSDCardStorageState() {
        try {
            // return Environment.getExternalStorageState(new File("/storage/sdcard1"));
            return Environment.getExternalStorageState(new File(sdcardRoot));
        } catch (Exception e) {
            Log.w(TAG, "Failed to read SDCard storage state; assuming REMOVED: " + e);
            return Environment.MEDIA_REMOVED;
        }
    }

    private SDCard() {
//        try {
//            mMountService = IMountService.Stub.asInterface(ServiceManager
//                                                           .getService("mount"));
//            final StorageVolume[] volumes = mMountService.getVolumeList();
//            if (volumes.length > VOLUME_SDCARD_INDEX) {
//                mVolume = volumes[VOLUME_SDCARD_INDEX];
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "couldn't talk to MountService", e);
//        }
        updateSDCardMountPath();
    }

    // modify by minghui.hua for PR943681
    public void updateSDCardMountPath(){
        sdcardRoot = "/storage/sdcard1";
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime()
                    .exec("getprop persist.sys.sdcard.swap");
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                if(0 == line.trim().compareTo("0")){
                    // default storage is SDCard
                    sdcardRoot = "/storage/sdcard0";
                    isDefaultStorage = true;
                }else if( 0 == line.trim().compareTo("1")){
                    // default storage is Phone
                    sdcardRoot = "/storage/sdcard1";
                    isDefaultStorage = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(TAG, "###YUHUAN###sdcardRoot=" + sdcardRoot);
    }

    public boolean isDefaultStorage(){
        return this.isDefaultStorage;
    }
}
