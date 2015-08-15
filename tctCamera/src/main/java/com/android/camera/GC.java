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
/* 11/13/2013|     wangxiaofei      |        550266        |Setting function  */
/*           |                      |                      |still can be used */
/*           |                      |                      | during Expressi- */
/*           |                      |                      |on mode           */
/* ----------|----------------------|----------------------|----------------- */
/* 11/14/2013|       hui.shen       |       CR-553221      |Add smile mode    */
/* ----------|----------------------|----------------------|----------------- */
/* 08/05/2014|       jian.zhang1    |       PR748288       |Hide Micro,display QR code*/
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/
package com.android.camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Bitmap;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.camera.custom.CustomFields;
import com.android.camera.custom.CustomUtil;
import com.android.camera.custom.picturesize.PictureSizePerso;
import com.android.camera.custom.videosize.VideoSizePerso;
import com.android.camera.manager.SwitcherManager;
import com.android.camera.ui.Rotatable;
import com.android.jrd.externel.CamcorderProfileEx;
import com.tct.camera.R;
//import com.android.camera.manager.PhotoMenuManager;
/**
 * GC is means global config, that used to store and manager global variables.
 * This class is very important and will be used frequently, so named GC just for convenience.
 */
public class GC {
    private static final String TAG = "YUHUAN_GC";
    private static final int NOT_FOUND = -1;
    public static final boolean LOG = true;

    public static final String CAMERA_SAVEDONE_SERVICE_RESULT_ACTION = "com.android.camera.savedoneservices";
    public static final String KEY_CAPTURE_MODE = "cap-mode";
    public static final String KEY_EIS_ENABLE = "video-stabilization";
    public static final String CAPTURE_MODE_BURST_SHOT = "burstshot";
    public static final String CAPTURE_MODE_NORMAL = "normal";

    // Basic Processing State Value
    public static final String UNDO = "undo";
    public static final String PRE_DO = "pre-do"; //previous do
    public static final String DOING = "doing";
    public static final String STOP = "stop";
    public static final String POST_DO = "post-do"; //posticus do
    public static final String DONE = "done";

    // Camera Launcher State Value
    // This value means who launched the camera, by intent(such as mms) or by app itself.
    public static final String LAUNCHER_ITSELF = "launchbyitself";
    public static final String LAUNCHER_INTENT = "launchbyintent";

    public static final String START_QRSCAN_INTENT ="com.tct.camera.STARTQRSCAN";
    public static final String START_FRONT_CAMERA_INTENT ="com.tct.camera.STARTFRONTCAMERA";

    // CameraID State Value
    public static final int CAMERAID_BACK = 0;
    public static final int CAMERAID_FRONT = 1;

    // Camera Mode State Value, also used for mode index.
    public static final int UNKNOWN            = -1;
    public static final int MODE_PHOTO         =  0;
    public static final int MODE_VIDEO         =  1;
    public static final int MODE_PANORAMA      =  2;
    public static final int MODE_PANOR360      =  3;
    public static final int MODE_HDR           =  4;
    public static final int MODE_SMILE         =  5;
    public static final int MODE_NIGHT         =  6;
    public static final int MODE_SPORTS        =  7;
    public static final int MODE_MACRO         =  8;
    public static final int MODE_EXPRESSION4   =  9;
    public static final int MODE_FACE_BEAUTY   = 10;
    public static final int MODE_ZSL           = 11; // Maybe useless
    public static final int MODE_RAW           = 12;
    public static final int MODE_QRCODE        = 13;
    public static final int MODE_WIDEANGLE     = 14;
    //public static final int MODE_MANUAL        = 15;
    public static final int MODE_FILTER        = 16;
    //public static final int MODE_MAKEUP        = 17;
    public static final int MODE_SLOWMOTION    = 18;
    //public static final int MODE_COLLAGE       = 19;

    public static final int MODE_MANUAL = 20;
    public static final int MODE_MANUAL_ISO = 21;
    public static final int MODE_MANUAL_EXPOSURE = 22;
    public static final int MODE_MANUAL_SHUTTER_SPEED = 23;
    public static final int MODE_MANUAL_FLASH_DUTY = 24;
    public static final int MODE_MANUAL_FOCUS_VALUE = 25;
    public static final int MODE_MANUAL_WHITE_BALANCE = 26;
    public static final int MODE_ARCSOFT_FACEBEAUTY=27;
    public static final int MODE_TIMELAPSE=28;
    public static final int MODE_POLAROID=29;
    public static final int MODE_ARCSOFT_NIGHT=30;

    public static final int MODE_NUM_ALL       = 31;
    

    // Launch Camera Process State Key
    public static final String STATE_LAUNCH_CAMERA = "state-launchcamera";
    // Switch Camera between Front and Back Process State Key
    public static final String STATE_SWITCH_CAMERA = "state-switchcamera";
    // Switch Camera Mode Process State Key
    public static final String STATE_SWITCH_MODE = "state-switchmode";
    // Take Picture Process State Key
    public static final String STATE_TAKE_PICTURE = "state-takepicture";
    // Video Recording Process State Key
    public static final String STATE_VIDEO_RECORD = "state-videorecord";
    // Exit Camera State Key
    public static final String STATE_EXIT_CAMERA = "state-exitcamera";

    // Camera Launcher State Key
    public static final String STATE_CAMERA_LAUNCHER = "state-cameralauncher";

    // CameraID State Key
    public static final String STATE_CAMERAID = "state-cameraid";

    // Camera Mode State Key
    public static final String STATE_MODE = "state-mode";

    // This map is used to store camera state.
    private HashMap<String, String> mStateMap = new HashMap<String, String>() {
        {
            put(STATE_LAUNCH_CAMERA, UNDO);
            put(STATE_SWITCH_CAMERA, UNDO);
            put(STATE_SWITCH_MODE, UNDO);
            put(STATE_TAKE_PICTURE, UNDO);
            put(STATE_VIDEO_RECORD, UNDO);
            put(STATE_EXIT_CAMERA, UNDO);

            put(STATE_CAMERA_LAUNCHER, LAUNCHER_ITSELF);
            put(STATE_CAMERAID, Integer.toString(CAMERAID_BACK));
            put(STATE_MODE, Integer.toString(MODE_PHOTO));
        }
    };
    //description in settings
    private HashMap<Integer, Integer> mSettingDescription = new HashMap<Integer, Integer>() {
        {
        		put(ROW_SETTING_ARCSOFT_ASD,R.string.pref_camera_arcsoft_asd_description);
        }
    };

    
    public  int getDescription(int row_setting){
    	return mSettingDescription.get(row_setting);
    }
    
    public final boolean isStateBusy(final String ... stateKeys) {
        boolean busy = false;
        for (int i = 0; i < stateKeys.length; i++) {
            String key = stateKeys[i];
            busy = busy || mStateMap.get(key) == PRE_DO
                || mStateMap.get(key) == DOING
                || mStateMap.get(key) == POST_DO;
        }
        return busy;
    }

    // Get State
    public final String getState(String stateKey) {
        return mStateMap.get(stateKey);
    }

    // Set State
    public final void setState(String stateKey, String stateValue) {
        mStateMap.put(stateKey, stateValue);
    }

    // Get Camera ID
    public final int getCameraID() {
        return Integer.parseInt(getState(STATE_CAMERAID));
    }

    // Set Camera ID
    public final void setCameraID(int cameraID) {
        setState(STATE_CAMERAID, Integer.toString(cameraID));
    }

    private int oldModeId = UNKNOWN;

    // Get Current Mode
    public final int getCurrentMode() {
    	Log.d(TAG,"###YUHUAN###getCurrentMode");
        return Integer.parseInt(getState(STATE_MODE));
    }
    
    private boolean mIsVideoRecording=false;
    public boolean isVideoRecording(){
        return mIsVideoRecording;
    }
    
    
    public void flagVideoRecording(boolean isRecording){
        mIsVideoRecording=isRecording;
    }

    private boolean mIsInArcsoftNight=false;
    public boolean isInArcsoftNight(){
        return mIsInArcsoftNight;
    }
    
    public void flagArcsoftNight(boolean isNight){
        mIsInArcsoftNight=isNight;
    }
    
    // Set Current Mode
    public final void setCurrentMode(int mode) {
    	Log.d(TAG,"###YUHUAN###setCurrentMode#mode=" + mode);
        oldModeId = getCurrentMode();
        setState(STATE_MODE, Integer.toString(mode));
    }
    public int getOldCameraId() {
        return oldModeId;
    }
    //set the bursshot save state
    private static boolean isBurstshotSave = false;

    public static void setBurstshotSaveState(boolean mBurstshotSave) {
        isBurstshotSave = mBurstshotSave;
    }

    public static boolean getBurstshotSaveState() {
        return isBurstshotSave;
    }

    private static boolean isExpressionIgore = false;

    public static void setExpressionState(boolean mIsExpressionIgore) {
        isExpressionIgore = mIsExpressionIgore;
    }

    public static boolean getExpressionState() {
        return isExpressionIgore;
    }

    private boolean isSwitchState = false;
    public void setSwitchPhotoVideoState(boolean mSwitchState) {
        isSwitchState = mSwitchState;
    }

    public boolean getSwitchPhotoVideoState() {
        return isSwitchState;
    }

    // ModePicker
    public static int GRIDVIEW_COLUMN_FRONT = 3;
    public static final int GRIDVIEW_COLUMN_BACK  = 4;
    public static final int[] MODE_SET_BACK =  new int[]
        { MODE_PANORAMA, MODE_MANUAL,MODE_QRCODE,MODE_HDR, MODE_SPORTS, MODE_FILTER,MODE_NIGHT,MODE_FACE_BEAUTY,MODE_MACRO};
    public static final int[] MODE_SET_FRONT = new int[]
        { MODE_FACE_BEAUTY, MODE_EXPRESSION4, MODE_FILTER,MODE_SMILE };
    public static final int[] MODE_SET_BACK_MMS = new int[] { };
    public static final int[] MODE_SET_FRONT_MMS = new int[] { };

    // MODE_ENABLE[i] = true, means mode i enable, otherwise disable.
    public static final boolean[] MODE_ENABLE = new boolean[MODE_NUM_ALL];
    public static final int[] MODE_ICONS_HIGHTLIGHT = new int[MODE_NUM_ALL];
    public static final int[] MODE_ICONS_NORMAL = new int[MODE_NUM_ALL];
    public static final int[] SHUTTER_ICONS = new int[MODE_NUM_ALL];
    public static final int[] MODE_NAMES = new int[MODE_NUM_ALL];
    public static final boolean[] MATRIX_ZOOM_ENABLE = new boolean[MODE_NUM_ALL];

    static {
        // Enable all mode by default.
        for(int i=0; i<MODE_NUM_ALL; i++) {
            MODE_ENABLE[i] = true;
        }
        MODE_ICONS_HIGHTLIGHT[MODE_PHOTO]       = R.drawable.ic_mode_photo_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_VIDEO]       = R.drawable.ic_mode_photo_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_PANORAMA]    = R.drawable.ic_mode_panorama_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_PANOR360]    = R.drawable.ic_mode_panorama360_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_HDR]         = R.drawable.ic_mode_hdr_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_SMILE]       = R.drawable.ic_mode_smile_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_NIGHT]       = R.drawable.ic_mode_night_focus;
        //[BUGFIX]-Mod-BEGIN by TCTNB.hui.shen,11/05/2013, PR-548457, sports mode
        //MODE_ICONS_HIGHTLIGHT[MODE_SPORTS]      = R.drawable.ic_mode_photo_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_SPORTS]      = R.drawable.ic_mode_sports_focus;
        //[BUGFIX]-Mod-END by TCTNB.hui.shen,11/05/2013, PR-548457, sports mode
        MODE_ICONS_HIGHTLIGHT[MODE_MACRO]       = R.drawable.ic_mode_photo_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_EXPRESSION4] = R.drawable.ic_mode_expression4_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_FACE_BEAUTY] = R.drawable.ic_mode_facebeauty_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_ZSL]         = R.drawable.ic_mode_burstshot_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_RAW]         = R.drawable.ic_mode_raw_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_QRCODE]      = R.drawable.ic_mode_qrcode_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_WIDEANGLE]   = R.drawable.ic_mode_qrcode_focus;
        // yuhuan 20150701
        //MODE_ICONS_HIGHTLIGHT[MODE_MANUAL]      = R.drawable.ic_mode_qrcode_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_FILTER]      = R.drawable.ic_mode_qrcode_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_MANUAL]               = R.drawable.ic_mode_manual_focus;
        MODE_ICONS_HIGHTLIGHT[MODE_MANUAL_ISO]           = R.drawable.manual_mode_iso_item_pressed;
        MODE_ICONS_HIGHTLIGHT[MODE_MANUAL_EXPOSURE]      = R.drawable.manual_mode_exposure_item_pressed;
        MODE_ICONS_HIGHTLIGHT[MODE_MANUAL_SHUTTER_SPEED] = R.drawable.manual_mode_shutter_speed_item_pressed;
        MODE_ICONS_HIGHTLIGHT[MODE_MANUAL_FLASH_DUTY]    = R.drawable.manual_mode_flash_item_pressed;
        MODE_ICONS_HIGHTLIGHT[MODE_MANUAL_FOCUS_VALUE]   = R.drawable.manual_mode_focal_length_item_pressed;
        MODE_ICONS_HIGHTLIGHT[MODE_MANUAL_WHITE_BALANCE] = R.drawable.manual_mode_wb_item_pressed;

        MODE_ICONS_NORMAL[MODE_PHOTO]       = R.drawable.ic_mode_photo_normal;
        MODE_ICONS_NORMAL[MODE_VIDEO]       = R.drawable.ic_mode_photo_normal;
        MODE_ICONS_NORMAL[MODE_PANOR360]    = R.drawable.ic_mode_panorama360_normal;
        MODE_ICONS_NORMAL[MODE_SMILE]       = R.drawable.ic_mode_smile_normal;
        //[BUGFIX]-Mod-BEGIN by TCTNB.hui.shen,11/05/2013, PR-548457, sports mode
        //MODE_ICONS_NORMAL[MODE_SPORTS]      = R.drawable.ic_mode_photo_normal;
        //[BUGFIX]-Mod-END by TCTNB.hui.shen,11/05/2013, PR-548457, sports mode
        MODE_ICONS_NORMAL[MODE_MACRO]       = R.drawable.ic_mode_photo_normal;
        MODE_ICONS_NORMAL[MODE_EXPRESSION4] = R.drawable.mode_collage_img;
        MODE_ICONS_NORMAL[MODE_FACE_BEAUTY] = R.drawable.mode_makeup_img;
        MODE_ICONS_NORMAL[MODE_ZSL]         = R.drawable.ic_mode_burstshot_normal;
        MODE_ICONS_NORMAL[MODE_RAW]         = R.drawable.ic_mode_raw_normal;

        MODE_ICONS_NORMAL[MODE_PANORAMA]    = R.drawable.mode_panorama_img;
        MODE_ICONS_NORMAL[MODE_HDR]         = R.drawable.mode_hdr_img;
        MODE_ICONS_NORMAL[MODE_NIGHT]       = R.drawable.mode_night_img;
        MODE_ICONS_NORMAL[MODE_SPORTS]      = R.drawable.mode_sport_img;
        MODE_ICONS_NORMAL[MODE_QRCODE]      = R.drawable.mode_qrcode_img;
        MODE_ICONS_NORMAL[MODE_WIDEANGLE]   = R.drawable.mode_wideangle_img;
        MODE_ICONS_NORMAL[MODE_MANUAL]      = R.drawable.mode_manual_img;
        MODE_ICONS_NORMAL[MODE_FILTER]      = R.drawable.mode_filter_img;
        MODE_ICONS_NORMAL[MODE_SLOWMOTION]  = R.drawable.mode_slowmotion_img;
        //MODE_ICONS_NORMAL[MODE_MANUAL]               = R.drawable.ic_mode_manual_normal;
        MODE_ICONS_NORMAL[MODE_MANUAL_ISO]           = R.drawable.manual_mode_iso_item_normal;
        MODE_ICONS_NORMAL[MODE_MANUAL_EXPOSURE]      = R.drawable.manual_mode_exposure_item_normal;
        MODE_ICONS_NORMAL[MODE_MANUAL_SHUTTER_SPEED] = R.drawable.manual_mode_shutter_speed_item_normal;
        MODE_ICONS_NORMAL[MODE_MANUAL_FLASH_DUTY]    = R.drawable.manual_mode_flash_item_normal;
        MODE_ICONS_NORMAL[MODE_MANUAL_FOCUS_VALUE]   = R.drawable.manual_mode_focal_length_item_normal;
        MODE_ICONS_NORMAL[MODE_MANUAL_WHITE_BALANCE] = R.drawable.manual_mode_wb_item_normal;


        //SHUTTER_ICONS[MODE_PHOTO]       = R.drawable.btn_shutter_photo;
        //SHUTTER_ICONS[MODE_VIDEO]       = R.drawable.btn_shutter_video;
        SHUTTER_ICONS[MODE_PHOTO]       = R.drawable.btn_photo;
        SHUTTER_ICONS[MODE_PANORAMA]    = R.drawable.btn_shutter_panorama;
        SHUTTER_ICONS[MODE_PANOR360]    = R.drawable.btn_shutter_panorama360;
        SHUTTER_ICONS[MODE_HDR]         = R.drawable.btn_shutter_hdr;
        //[FEATURE]-Mod-BEGIN by TCTNB(hui.shen) 11/14/2013, CR553221, add smile mode
        //SHUTTER_ICONS[MODE_SMILE]       = R.drawable.btn_shutter_smile;
        SHUTTER_ICONS[MODE_SMILE]       = R.drawable.btn_smilecapture;
        //[FEATURE]-Mod-END by TCTNB(hui.shen) 11/14/2013, CR553221, add smile mode
        SHUTTER_ICONS[MODE_NIGHT]       = R.drawable.btn_shutter_night;
        SHUTTER_ICONS[MODE_SPORTS]      = R.drawable.btn_shutter_sports;
        SHUTTER_ICONS[MODE_MACRO]       = R.drawable.btn_shutter_photo;
        SHUTTER_ICONS[MODE_EXPRESSION4] = R.drawable.btn_shutter_expression4;
        SHUTTER_ICONS[MODE_FACE_BEAUTY] = R.drawable.btn_shutter_facebeauty;
        SHUTTER_ICONS[MODE_ZSL]         = R.drawable.btn_shutter_photo;//setting_burstshot;
        SHUTTER_ICONS[MODE_RAW]         = R.drawable.btn_shutter_photo;
        SHUTTER_ICONS[MODE_WIDEANGLE]   = R.drawable.btn_shutter_wideangle;
        // yuhuan 20150701
        //SHUTTER_ICONS[MODE_MANUAL]      = R.drawable.btn_shutter_manual;
        SHUTTER_ICONS[MODE_FILTER]      = R.drawable.btn_shutter_photo;
        SHUTTER_ICONS[MODE_SLOWMOTION]  = R.drawable.btn_shutter_photo;
        SHUTTER_ICONS[MODE_ARCSOFT_FACEBEAUTY]=R.drawable.btn_facebeauty;
        SHUTTER_ICONS[MODE_TIMELAPSE] 	= R.drawable.btn_shutter_timelapse;
        SHUTTER_ICONS[MODE_POLAROID] 	= R.drawable.btn_shutter_polaroid;
        SHUTTER_ICONS[MODE_ARCSOFT_NIGHT] 	= R.drawable.btn_shutter_polaroid;

        // Initialization of Mode Name
        MODE_NAMES[MODE_PHOTO] = R.string.pref_camera_capturemode_entry_normal;
        MODE_NAMES[MODE_HDR] = R.string.pref_camera_capturemode_enrty_hdr;
        MODE_NAMES[MODE_PANORAMA] = R.string.pref_camera_capturemode_enrty_panorama;
        MODE_NAMES[MODE_FACE_BEAUTY] = R.string.pref_camera_capturemode_enrty_makeup;
        MODE_NAMES[MODE_SMILE] = R.string.pref_camera_capturemode_enrty_smile;
        MODE_NAMES[MODE_ZSL] = R.string.pref_camera_burstshot_title;
        MODE_NAMES[MODE_NIGHT] = R.string.pref_camera_capturemode_entry_nightmode;
        MODE_NAMES[MODE_PANOR360] = R.string.pref_camera_capturemode_enrty_panor360;
        MODE_NAMES[MODE_SPORTS] = R.string.pref_camera_capturemode_entry_sports;
        MODE_NAMES[MODE_MACRO] = R.string.pref_camera_capturemode_entry_micro;
        MODE_NAMES[MODE_EXPRESSION4] = R.string.pref_camera_capturemode_enrty_collage;
        MODE_NAMES[MODE_RAW] = R.string.pref_camera_capturemode_enrty_raw;
        MODE_NAMES[MODE_QRCODE] = R.string.pref_camera_capturemode_entry_qrcode;
        MODE_NAMES[MODE_WIDEANGLE]   = R.string.pref_camera_capturemode_enrty_wideangle;
        // yuhuan 20150701
        //MODE_NAMES[MODE_MANUAL]      = R.string.pref_camera_capturemode_enrty_manual;
        MODE_NAMES[MODE_FILTER]      = R.string.pref_camera_capturemode_enrty_filter;
        MODE_NAMES[MODE_SLOWMOTION]  = R.string.pref_camera_capturemode_enrty_slowmotion;
        MODE_NAMES[MODE_MANUAL]               = R.string.pref_camera_capturemode_enrty_manual;
        MODE_NAMES[MODE_MANUAL_ISO]           = R.string.pref_camera_iso_title;
        MODE_NAMES[MODE_MANUAL_EXPOSURE]      = R.string.pref_exposure_title;
        MODE_NAMES[MODE_MANUAL_SHUTTER_SPEED] = R.string.pref_camera_capturemode_enrty_manual_shutterspeed;
        MODE_NAMES[MODE_MANUAL_FLASH_DUTY]    = R.string.pref_camera_flash_duty_title;
        MODE_NAMES[MODE_MANUAL_FOCUS_VALUE]   = R.string.pref_camera_capturemode_enrty_manual_focusvalue;
        MODE_NAMES[MODE_MANUAL_WHITE_BALANCE] = R.string.pref_camera_whitebalance_title;


        MATRIX_ZOOM_ENABLE[MODE_PHOTO]       = true;
        MATRIX_ZOOM_ENABLE[MODE_VIDEO]       = true;
        MATRIX_ZOOM_ENABLE[MODE_PANORAMA]    = false;
        MATRIX_ZOOM_ENABLE[MODE_PANOR360]    = false;
        MATRIX_ZOOM_ENABLE[MODE_HDR]         = true;
        MATRIX_ZOOM_ENABLE[MODE_SMILE]       = true;
        MATRIX_ZOOM_ENABLE[MODE_NIGHT]       = true;
        MATRIX_ZOOM_ENABLE[MODE_SPORTS]      = true;
        MATRIX_ZOOM_ENABLE[MODE_MACRO]       = true;
        MATRIX_ZOOM_ENABLE[MODE_EXPRESSION4] = true;
        MATRIX_ZOOM_ENABLE[MODE_FACE_BEAUTY] = true;
        MATRIX_ZOOM_ENABLE[MODE_ZSL]         = true;
        MATRIX_ZOOM_ENABLE[MODE_RAW]         = true;
        //modify by minghui.hua for PR927419
        // yuhuan 20150701 
        MATRIX_ZOOM_ENABLE[MODE_MANUAL]    = false;
        MATRIX_ZOOM_ENABLE[MODE_ARCSOFT_FACEBEAUTY]= true;
        // MATRIX_ZOOM_ENABLE[MODE_TIMELAPSE]   = true;
    }

    public static int MANU_NUM = 5;

    public static final int MANU_ISO          =  1;
    public static final int MANU_EXPOSURE     =  2;
    public static final int MANU_FOCUS        =  3;
    public static final int MANU_WHITEBLANCE  =  4;
    
    //public static final int MANU_FLASH        =  3;

    public static final int[] MANU_NAMES = new int[MANU_NUM];
    public static final int[] MANU_PICS = new int[MANU_NUM];
    public static final int[] MANU_HIGHLIGHT_PICS = new int[MANU_NUM];
    public static final int[] MANU_TITLE_NAMES = new int [MANU_NUM];
    static {
        MANU_NAMES[MANU_ISO] = R.string.manual_manu_iso;
        MANU_NAMES[MANU_EXPOSURE] = R.string.manual_manu_exposure;
        //MANU_NAMES[MANU_FLASH] = R.string.manual_manu_flash;
        MANU_NAMES[MANU_WHITEBLANCE] = R.string.manual_manu_wb;
        MANU_NAMES[MANU_FOCUS] = R.string.manual_manu_focus;

        MANU_PICS[MANU_ISO] = R.drawable.manu_item_iso;
        MANU_PICS[MANU_EXPOSURE] = R.drawable.manu_item_exposure;
        //MANU_PICS[MANU_FLASH] = R.drawable.manu_item_flash;
        MANU_PICS[MANU_WHITEBLANCE] = R.drawable.manu_item_wb;
        MANU_PICS[MANU_FOCUS] = R.drawable.manu_item_focus;

        MANU_HIGHLIGHT_PICS[MANU_ISO] = R.drawable.manu_item_iso_normal;
        MANU_HIGHLIGHT_PICS[MANU_EXPOSURE] = R.drawable.manu_item_exposure_normal;
        //MANU_HIGHLIGHT_PICS[MANU_FLASH] = R.drawable.manu_item_flash_normal;
        MANU_HIGHLIGHT_PICS[MANU_WHITEBLANCE] = R.drawable.manu_item_wb_normal;
        MANU_HIGHLIGHT_PICS[MANU_FOCUS] = R.drawable.manu_item_focus_normal;

        MANU_TITLE_NAMES[MANU_ISO] = R.string.manual_manu_title_iso;
        MANU_TITLE_NAMES[MANU_EXPOSURE] = R.string.manual_manu_title_exposure;
        MANU_TITLE_NAMES[MANU_WHITEBLANCE] = R.string.manual_manu_title_wb;
        MANU_TITLE_NAMES[MANU_FOCUS] = R.string.manual_manu_title_focus;

    }

    private static final int OFFSET = 100;

    public static int getModeIndex(int mode) {
        int index = mode % OFFSET;
        return index;
    }

    public int getCurrentShutterIcon() {
    	Log.d(TAG,"###YUHUAN###getCurrentShutterIcon#getCurrentMode()=" + getCurrentMode());
        if (getCurrentMode() == UNKNOWN)
            return SHUTTER_ICONS[MODE_PHOTO];
        if(mContext.isNonePickIntent()){
        	SHUTTER_ICONS[MODE_VIDEO]       = R.drawable.btn_video;
        }else{
        	SHUTTER_ICONS[MODE_VIDEO]       = R.drawable.btn_video_none_intent;
        }
        return SHUTTER_ICONS[getCurrentMode()];
    }

    public boolean isVideoModule() {
        return (mContext.isNonePickIntent()
            && (SwitcherManager.getCameraModule() == SwitcherManager.STATE_OF_VIDEO))
            || getCurrentMode()== GC.MODE_VIDEO;
    }

    /* View state Id */
    public static final int VIEW_STATE_NORMAL				=  0;
    public static final int VIEW_STATE_BACK				=  1;
    public static final int VIEW_STATE_ADVANCED			=  2;
    public static final int VIEW_STATE_BLANK				=  3;
    public static final int VIEW_STATE_SWITCH_PHOTO_VIDEO =  4;
    public static final int VIEW_STATE_SET				=  5;
    public static final int VIEW_STATE_RESTORE			=  6;
    public static final int VIEW_STATE_HIDE_SET			=  7;
    public static final int VIEW_STATE_SHOW_SET			=  8;
    public static final int VIEW_STATE_SHUTTER_PRESS		=  9;
    public static final int VIEW_STATE_CAMERA_SWITCH		=  10;
    public static final int VIEW_STATE_RECORDING			=  11;
    public static final int VIEW_STATE_FLING          =  12;
    public static final int VIEW_STATE_COUNTDOWN_START	=  13; //Shangyong,PR551894
    public static final int VIEW_STATE_COUNTDOWN_CANCEL	=  14; //Shangyong,PR551894
    public static final int VIEW_STATE_MANUAL_MANU		=  15;
    public static final int VIEW_STATE_HDR				=  16;
    public static final int VIEW_STATE_FACE_BEAUTY_MENU   =17;
    public static final int VIEW_STATE_MANUAL_MANU_HIDE  =18;
    public static final int VIEW_STATE_UI_ORIENTATION  =19;
    public static final int VIEW_STATE_REFRESH_TOP_VIEW=20;
    
    

    /* Action Definition */
    public static final int Action_Reset2Default    =  0;
    public static final int Action_Switch2Back      =  1;
    public static final int Action_Switch2Front     =  2;
    public static final int Action_Switch2Photo     =  3;
    public static final int Action_Switch2Video     =  4;
    public static final int Action_Switch2HDR       =  5;
    public static final int Action_Switch2Panorama  =  6;
    public static final int Action_Switch2Panor360  =  7;
    public static final int Action_Switch2Smile     =  8;
    public static final int Action_Switch2Night     =  9;
    public static final int Action_Switch2Sports    = 10;
    public static final int Action_Switch2Micro     = 11;
    public static final int Action_Expression4      = 12;
    public static final int Action_FlashSupport     = 13;
    public static final int Action_FullScreenSpt    = 14; //Spt = Support, Same below
    public static final int Action_ShutterSoundSpt  = 15;
    public static final int Action_GPSTagSupport    = 16;
    public static final int Action_PictureQuality   = 17;
    public static final int Action_TimerSupport     = 18;
    public static final int Action_ISOSupport       = 19;
    public static final int Action_ExposureSpt      = 20;
    public static final int Action_ShowAdvanced     = 21; //Advanced Settings
    public static final int Action_COUNT            = 22;

    /* Scene Definition */
    public static final int Scene_Back          =  0;
    public static final int Scene_Front         =  1;
    public static final int Scene_Photo         =  2;
    public static final int Scene_Video         =  3;
    public static final int Scene_Hdr           =  4;
    public static final int Scene_Panorama      =  5;
    public static final int Scene_Panor360      =  6;
    public static final int Scene_Smile         =  7;
    public static final int Scene_Night         =  8;
    public static final int Scene_Sports        =  9;
    public static final int Scene_Micro         = 10;
    public static final int Scene_Expression4   = 11;

    /* To define the scene matrix of actions witch is supported or not */
    private static final boolean[][] Matrix_ActionSupport = new boolean[Action_COUNT][];
    private static final boolean T = true;
    private static final boolean F = false;
    static {
        // scene column : back front photo video hdr panorama panor360 smile night sports micro expression4
        // column title:                                             b  f  p  v  h  p  3  s  n  s  m  e
        Matrix_ActionSupport[Action_Reset2Default]   = new boolean[]{ T, T, T, T, T, T, T, T, T ,T, T, T, }; // 0:
        Matrix_ActionSupport[Action_Switch2Back]     = new boolean[]{ F, T, T, T, T, T, T, T, T ,T, T, T, }; // 1:
        Matrix_ActionSupport[Action_Switch2Front]    = new boolean[]{ T, F, T, T, T, T, T, T, T ,T, T, T, }; // 2:
        Matrix_ActionSupport[Action_Switch2Photo]    = new boolean[]{ T, T, F, T, T, T, T, T, T ,T, T, T, }; // 3:
        Matrix_ActionSupport[Action_Switch2Video]    = new boolean[]{ T, T, T, F, T, T, T, T, T ,T, T, T, }; // 4:
        Matrix_ActionSupport[Action_Switch2HDR]      = new boolean[]{ T, F, T, T, F, F, F, T, T ,T, T, T, }; // 5:
        Matrix_ActionSupport[Action_Switch2Panorama] = new boolean[]{ T, F, T, T, T, F, T, T, T ,T, T, T, }; // 6:
        Matrix_ActionSupport[Action_Switch2Panor360] = new boolean[]{ T, F, T, T, T, T, F, T, T ,T, T, T, }; // 7:
        Matrix_ActionSupport[Action_Switch2Smile]    = new boolean[]{ T, T, T, T, T, F, F, F, T ,T, T, T, }; // 8:
        Matrix_ActionSupport[Action_Switch2Night]    = new boolean[]{ T, T, T, T, T, F, F, T, F ,T, T, T, }; // 9:
        Matrix_ActionSupport[Action_Switch2Sports]   = new boolean[]{ T, F, T, T, T, F, F, T, T ,F, T, T, }; // 10:
        Matrix_ActionSupport[Action_Switch2Micro]    = new boolean[]{ T, F, T, T, T, T, T, T, T ,T, F, T, }; // 11:
        Matrix_ActionSupport[Action_Expression4]     = new boolean[]{ T, T, T, T, T, T, T, T, T ,T, T, F, }; // 12:
        Matrix_ActionSupport[Action_FlashSupport]    = new boolean[]{ T, T, T, F, T, T, T, T, T ,T, T, T, }; // 13:
        Matrix_ActionSupport[Action_FullScreenSpt]   = new boolean[]{ T, T, T, T, T, T, T, T, T ,T, T, T, }; // 14:
        Matrix_ActionSupport[Action_ShutterSoundSpt] = new boolean[]{ T, T, T, T, T, T, T, T, T ,T, T, T, }; // 15:
        Matrix_ActionSupport[Action_GPSTagSupport]   = new boolean[]{ T, T, T, T, T, T, T, T, T ,T, T, T, }; // 16:
        Matrix_ActionSupport[Action_PictureQuality]  = new boolean[]{ T, T, T, T, T, T, T, T, T ,T, T, T, }; // 17:
        Matrix_ActionSupport[Action_TimerSupport]    = new boolean[]{ T, T, T, T, T, T, T, T, T ,T, T, T, }; // 18:
        Matrix_ActionSupport[Action_ISOSupport]      = new boolean[]{ T, T, T, T, T, T, T, T, T ,T, T, T, }; // 19:
        Matrix_ActionSupport[Action_ExposureSpt]     = new boolean[]{ T, T, T, T, T, T, T, T, T ,T, T, T, }; // 20:
        Matrix_ActionSupport[Action_ShowAdvanced]    = new boolean[]{ T, T, T, T, T, T, T, T, T ,T, T, T, }; // 21:
    }


    public boolean isCameraErgoForChinaEnable() {
        return isCameraErgoForChinaEnable;
    }

    private static int currentAction = NOT_FOUND;

    public static void setCurrentAction(int action) {
        currentAction = action;
    }

    public static int getCurrentAction() {
        return currentAction;
    }

    private static int currentScene = NOT_FOUND;

    public static void setCurrentScene(int scene) {
        currentScene = scene;
    }

    public static int getCurrentScene() {
        return currentScene;
    }

    public static boolean isSupportedAction(int scene, int action, int cameraId) {
        // cameraId = 0 means scene = Scene_Back
        // cameraId = 1 means scene = Scene_Front
        if (scene == NOT_FOUND || action == NOT_FOUND) return false;
        if (Matrix_ActionSupport[action][cameraId]) {
            return Matrix_ActionSupport[action][scene];
        }
        return false;
    }

    public static void setOrientation(View view, int orientation, boolean animation) {
        if (view == null) { return; }
        if (view instanceof Rotatable) {
            ((Rotatable) view).setOrientation(orientation, animation);
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            for (int i = 0, count = group.getChildCount(); i < count; i++) {
                setOrientation(group.getChildAt(i), orientation, animation);
            }
        }
    }

    private static CameraActivity mContext;
    private boolean isCameraErgoForChinaEnable;
    private String defaultDisplayRatio;
    private String LargestPicturesize;
    private int[] pictureSizeNonfullscreen;
    private String[] videoSize;

    public GC(CameraActivity context) {
        mContext = context;
        initPerso();
        defaultDisplayRatio = CameraSettings.getRatioString(
            mContext.getResources().getDisplayMetrics().widthPixels,
            mContext.getResources().getDisplayMetrics().heightPixels);
    }
    public CameraActivity getContext(){
        return mContext;
    }
    public void setContext(CameraActivity context){
        mContext = context;
    }

    // modify by minghui.hua for PR978950 begin
    private boolean mCapturedImagePreviewing = true;

    public void setCameraPreview(boolean flag) {
        mCapturedImagePreviewing = flag;
    }

    public boolean getCameraPreview() {
        return mCapturedImagePreviewing;
    }
    // modify by minghui.hua for PR978950 end

    private void initPerso() {
    	Log.d(TAG,"###YUHUAN###initPerso into ");
//        isCameraErgoForChinaEnable = mContext.getResources().getBoolean(R.bool.def_camera_for_china_enable);
        //[BUGFIX] Added by jian.zhang1 for PR748288 2014.08.05 Begin
//        MODE_ENABLE[MODE_MACRO] = mContext.getResources().getBoolean(R.bool.feature_camera_micro_enable);

        MODE_ENABLE[MODE_SMILE] = CustomUtil.getInstance().getBoolean(CustomFields.DEF_SMILE_MODE_ENABLE, false);
        MODE_ENABLE[MODE_FACE_BEAUTY] = CustomUtil.getInstance().getBoolean(CustomFields.DEF_FACE_BEAUTY_MODE_ENABLE, false);
        MODE_ENABLE[MODE_MACRO] = true;
        MODE_ENABLE[MODE_SMILE] = CustomUtil.getInstance().getBoolean(CustomFields.DEF_SMILE_MODE_ENABLE, false);
        MODE_ENABLE[MODE_FACE_BEAUTY] = CustomUtil.getInstance().getBoolean(CustomFields.DEF_FACE_BEAUTY_MODE_ENABLE, false);
        // yuhuan 20150701 disable face beauty
        MODE_ENABLE[MODE_FACE_BEAUTY] = false;
        GRIDVIEW_COLUMN_FRONT = MODE_ENABLE[MODE_FACE_BEAUTY] ? 3 : 2;
        //[BUGFIX] Added by jian.zhang1 for PR748288 2014.08.05 End
        //MODE_ENABLE[MODE_FACE_BEAUTY] = mContext.getResources().getBoolean(R.bool.feature_camera_facebeauty_enable);
        //MODE_ENABLE[MODE_QRCODE] = mContext.getResources().getBoolean(R.bool.feature_camera_qrcode_enable);
//        SETTING_ENABLE[ROW_SETTING_VIDEO_FRONT_QUALITY] =
//            mContext.getResources().getBoolean(R.bool.feature_camera_front_video_quality_set_enable);
        SETTING_ENABLE[ROW_SETTING_VIDEO_FRONT_QUALITY] = true;
//        SETTING_ENABLE[ROW_SETTING_LIMIT_FOR_MMS] = mContext.getResources().getBoolean(R.bool.def_camera_tf_enable);
        SETTING_ENABLE[ROW_SETTING_LIMIT_FOR_MMS] = false;
        //[BUGFIX]-Add by TCTNB.yufeng.lan,04/22/2014,RR-658514,To add anti-banding setting ( 50Hz/60Hz) in Camera.
//        SETTING_ENABLE[ROW_SETTING_ANTIBANDING] = mContext.getResources().getBoolean(R.bool.def_camera_antibanding_show);
        SETTING_ENABLE[ROW_SETTING_ANTIBANDING] = true;
        //TODO the shutter sound option will not show in china version
        //SETTING_ENABLE[ROW_SETTING_SHUTTER_SOUND] =mContext.getResources().getBoolean(R.bool.def_camera_shootsound_show);
    }

    public static void setSettingEnable(int key,boolean enable) {
        SETTING_ENABLE[key] = enable;
    }
    public static boolean isSettingEnable(int key) {
        if(key >= 0 && SETTING_ENABLE[key]) {
            return true;
        }
        return false;
    }

    /*
      this here just for future use
    */

    public static boolean isTintlessNeed() {
    	return true;
//        return mContext.getCameraId() == GC.CAMERAID_BACK;
    }

    public String getDisPlayRatio() {
        return defaultDisplayRatio;
    }

    public int[] getPictureSizenames() {
        String targetRatio = getPreviewRatio();
        Log.d(TAG,"###YUHUAN###getPictureSizenames#targetRatio=" + targetRatio);

        List<Size> sizes2 = mContext.getCameraDevice().getParameters().getSupportedPictureSizes();
        
        // yuhuan 20150703 for test   ===begin
        for(int i=0;i<sizes2.size();i++) {
        	Size testSize = sizes2.get(i);
        	Log.d(TAG,"###YUHUAN###getPictureSizenames#testSize[" + i + "]=" + testSize.width + "x" + testSize.height);
        }
        // yuhuan 20150703 for test   ===end
        Size size ;
        size = sizes2.get(0);
        LargestPicturesize = SettingUtils.buildSize(size.width, size.height);
        if (LOG) {
            android.util.Log.i(TAG,"###YUHUAN###getPictureSizenames#largestPictureSize=" + LargestPicturesize);
        }
        //TODO this should be changed
        //if (LargestPicturesize.equals(PICTURE_SIZE_NONEFULLSCREEN_13M[3])) {
            if (targetRatio.equals(PICTURE_RATIO_16_9)){
                pictureSizeNonfullscreen = PICTURE_SIZE_FULLSCREEN_13M_16_9_NAMES;
            } else if (targetRatio.equals(PICTURE_RATIO_4_3)) {
                pictureSizeNonfullscreen = PICTURE_SIZE_NONEFULLSCREEN_NAMES_13M;
       //     }
        }
        return pictureSizeNonfullscreen;
    }

    public String[] getVideoSizenames() {
        if (mContext.getCameraDevice() == null) return null;
        String targetRatio = getDisPlayRatio();

        List<Size> sizes2 = mContext.getCameraDevice().getParameters().getSupportedPictureSizes();
        Size size ;
        size = sizes2.get(0);
        LargestPicturesize = SettingUtils.buildSize(size.width, size.height);
        if (LOG) {
            android.util.Log.i(TAG,"LMCH1228 largestPictureSize:"+LargestPicturesize);
            android.util.Log.i(TAG,"LMCH1228 targetRatio:"+targetRatio);
        }
        if (targetRatio.equals(PICTURE_RATIO_16_9)){
            videoSize = VIDEO_SIZE_BACKCAMERA_16_9;
        } else if (targetRatio.equals(PICTURE_RATIO_5_3)) {
            videoSize = VIDEO_SIZE_BACKCAMERA_5_3;
        } else {
            videoSize = VIDEO_SIZE_BACKCAMERA_16_9;
        }
        return videoSize;
    }
    public boolean isVideoFront2M() {
        if (mContext.getCameraDevice() == null) return false;
        List<Size> sizes2 = mContext.getCameraDevice().getParameters().getSupportedPictureSizes();
        Size size ;
        size = sizes2.get(0);
        LargestPicturesize = SettingUtils.buildSize(size.width, size.height);
        if (LOG) {
            android.util.Log.i(TAG,"LMCH1228 largestPictureSize:"+LargestPicturesize);
        }
        //here the 1280x960 is 1.2M, bigger than 720x480,so use the same picsize[] as 2M
        if (LargestPicturesize.equals("1600x1200")
            || LargestPicturesize.equals("1280x960")) {
            return true;
        } else {
            return false;
        }

    }
    public String getPreviewRatio() {
//        boolean fullScreen = (mContext.getPreferences().getString(
//                               CameraSettings.KEY_PICTURE_RATIO, mContext.getString(R.string.pref_camera_picture_ratio_default)))
//                                .equals(mContext.getString(R.string.setting_off_value));
        if(mContext.getPreferences()==null){
            return mContext.getString(R.string.pref_camera_picture_ratio_default);
        }
    	String targetRatio=mContext.getPreferences().getString(
                CameraSettings.KEY_PICTURE_RATIO, mContext.getString(R.string.pref_camera_picture_ratio_default));
    	Log.d(TAG,"###YUHUAN###getPreviewRatio#targetRatio=" + targetRatio);
//        String targetRatio;
//        if (fullScreen) {
//            targetRatio = getDisPlayRatio();
//        } else {
//            targetRatio = GC.PICTURE_RATIO_4_3;
//        }
//        if(mContext.isImageCaptureIntent()) {
//            targetRatio = GC.PICTURE_RATIO_4_3;
//        }

        return targetRatio;
    }

/************************************************************************************/
/*                                 SettingChecker-Start                             */
/************************************************************************************/
    public static final int ROW_SETTING_ADVANCED            = 0;
    public static final int ROW_SETTING_BACK                = 1;
    public static final int ROW_SETTING_VIDEO_QUALITY       = 2;
    public static final int ROW_SETTING_JPEG_QUALITY        = 3;
    public static final int ROW_SETTING_PICTURE_SIZE        = 4;
    public static final int ROW_SETTING_PICTURE_RATIO       = 5;
    public static final int ROW_SETTING_ISO                 = 6;
    public static final int ROW_SETTING_EXPOSURE            = 7;
    public static final int ROW_SETTING_GPS_TAG             = 8;
    public static final int ROW_SETTING_FLASH               = 9;
    public static final int ROW_SETTING_SELF_TIMER          = 10;
    public static final int ROW_SETTING_SHUTTER_SOUND       = 11;
    public static final int ROW_SETTING_SOUND_RECORDING     = 12;
    public static final int ROW_SETTING_WHITE_BALANCE       = 13;
    public static final int ROW_TOUCH_SHUTTER               = 14;
    public static final int ROW_SETTING_SCENCE_MODE         = 15;
    public static final int ROW_SETTING_IMAGE_PROPERTIES    = 16;
    public static final int ROW_SETTING_COLOR_EFFECT        = 17;
    public static final int ROW_SETTING_CONTINUOUS          = 18;
    public static final int ROW_SETTING_CONTINUOUS_NUM      = 19;
    public static final int ROW_SETTING_TIME_LAPSE          = 20;
    public static final int ROW_SETTING_LIVE_EFFECT         = 21;
    public static final int ROW_SETTING_FACEBEAUTY_PROPERTIES   = 22;
    public static final int ROW_SETTING_FACEBEAUTY_SMOOTH       = 23;
    public static final int ROW_SETTING_FACEBEAUTY_SKINCOLOR    = 24;
    public static final int ROW_SETTING_FACEBEAUTY_SHARP        = 25;
    public static final int ROW_POWER_MODE      = 26;
    public static final int ROW_SETTING_VIDEO_FRONT_QUALITY     = 27;
    public static final int ROW_SETTING_FOCUS_MODE          = 28;
    public static final int ROW_SETTING_AUTOEXPOSURE        = 29;
    public static final int ROW_SETTING_AUTOEXPOSURE_LOCK   = 30;
    public static final int ROW_SETTING_WAV_DENOISE         = 31;
    public static final int ROW_SETTING_VIDEO_FLASH_MODE    = 32;
    public static final int ROW_SETTING_PICTURE_SIZE_FLAG   = 33;
    public static final int ROW_VOICE_SHUTTER   = 34;//[FEATURE]-Add by TCTNB.(Liu Jie),12/18/2013,550519,VoiceShooting
    public static final int ROW_SETTING_LIMIT_FOR_MMS       = 35;
    public static final int ROW_SETTING_EIS     = 36;
    public static final int ROW_SETTING_ZSL   = 37;
    public static final int ROW_SETTING_DENOISE     = 38;
    public static final int ROW_SETTING_ANTIBANDING = 39;//[BUGFIX]-Add by TCTNB.yufeng.lan,04/22/2014,RR-658514,Antibanding
    public static final int ROW_SETTING_RESTORE_TO_DEFAULT   = 40;
    public static final int ROW_SETTING_FACEDETECTION   = 41;
    public static final int ROW_SETTING_REFOCUS_ON_SHOOT   = 42;
    public static final int ROW_SETTING_ARCSOFT_NIGHT=43;
    public static final int ROW_SETTING_ARCSOFT_ASD=44;
    
    public static final int ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE =45;
    public static final int ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE =46;
    
    public static final int ROW_SETTING_FFF=47;
    public static final int ROW_SETTING_GRID=48;
    public static final int ROW_SETTING_SAVEPATH=49;
    public static final int SETTING_ROW_COUNT               = 50;

    public ListPreference[] mListPrefs = new ListPreference[SETTING_ROW_COUNT];
    private static final boolean[] SETTING_ENABLE = new boolean[SETTING_ROW_COUNT];
    static {
        for(int i=0;i<SETTING_ROW_COUNT;i++) {
            SETTING_ENABLE[i] = true;
        }
    }
    public static final int PREVIEW_LAYOUT_16_9 = 1;
    public static final int PREVIEW_LAYOUT_4_3 = 2;
    public static final String PICTURE_RATIO_16_9 = "1.7778";
    public static final String PICTURE_RATIO_5_3 = "1.6667";
    public static final String PICTURE_RATIO_4_3 = "1.3333";
    public static final String CAMERA_FACING_FRONT_STRING = "camera_facing_front";
    public static final String CAMERA_FACING_BACK_STRING = "camera_facing_back";
    public static ListPreference PictureSizeFlagPre;
    public static ListPreference videoSizesFront;
    public static ListPreference videoSizesBack;
    public static String[] entries;
    public static ListPreference SavePathPre;
    public static String[] saveEntries;

    //TODO the photo size and the num of picture size should be check with UE designer
    // picture size for back fullscreen
    public static final String[] PICTURESIZE_BACK_FULLSCREEN_16_9 = new String[] {
		"2560x1440",
		//"2588x1456",//3.8M
        "3264x1836",//6M
        //"4128x2322"//9M
        "4096x2304"
    };

    // picture size for back none fullscreen
    public static final String[] PICTURESIZE_BACK_NONEFULLSCREEN = new String[] {
        "2592x1944",//5M
        "3264x2448",//8M
        //"4000x3000",//12M
        "4160x3120"
    };

    //front picture size
    public static  final String[] PICTURESIZE_FRONT_NONEFULLSCREEN = new String[] {"1600x1200"};
    public static  final String[] PICTURESIZE_FRONT_FULLSCREEN = new String[] {"1920x1080"};

    public static final String[] VIDEO_SIZE_BACKCAMERA_16_9 = new String[] {
        "1920x1080",
        "1280x720",
        "856x480",
    };
    public static final String[] VIDEO_SIZE_BACKCAMERA_5_3 = new String[] {
        "1800x1080",
        "1200x720",
        "800x480",
    };

    public static final String[] VIDEO_SIZE_FRONTCAMERA_2M = new String[] {
        "720x480",
        "640x480",
    };

    public static final String[] VIDEO_SIZE_FRONTCAMERA_0_3M = new String[] {
        "640x480",
        "320x240",
    };
    public static final int PICTURE_SIZE_SMALL          =  0;
    public static final int PICTURE_SIZE_MIDIUM         =  1;
    public static final int PICTURE_SIZE_LARGE          =  2;
    public static final int PICTURE_SIZE_COUNT          =  3;

    public static final int[] PICTURE_SIZE_FULLSCREEN_13M_16_9_NAMES     = new int[PICTURE_SIZE_COUNT];
    public static final int[] PICTURE_SIZE_NONEFULLSCREEN_NAMES_13M      = new int[PICTURE_SIZE_COUNT];
    static {
        PICTURE_SIZE_FULLSCREEN_13M_16_9_NAMES[PICTURE_SIZE_LARGE]     = R.string.pref_camera_picturesize_6mp;
        PICTURE_SIZE_FULLSCREEN_13M_16_9_NAMES[PICTURE_SIZE_MIDIUM]    = R.string.pref_camera_picturesize_3_8mp;
        PICTURE_SIZE_FULLSCREEN_13M_16_9_NAMES[PICTURE_SIZE_SMALL]     = R.string.pref_camera_picturesize_1mp;

        PICTURE_SIZE_NONEFULLSCREEN_NAMES_13M[PICTURE_SIZE_LARGE]      = R.string.pref_camera_picturesize_13mp;
        PICTURE_SIZE_NONEFULLSCREEN_NAMES_13M[PICTURE_SIZE_MIDIUM]     = R.string.pref_camera_picturesize_8mp;
        PICTURE_SIZE_NONEFULLSCREEN_NAMES_13M[PICTURE_SIZE_SMALL]      = R.string.pref_camera_picturesize_5mp;
    }

    private static final int[] SETTING_GROUP_COMMON_FOR_ADVANCED_SETTING = new int[] {
        ROW_SETTING_PICTURE_RATIO,
        ROW_SETTING_GPS_TAG,
        ROW_SETTING_SHUTTER_SOUND,
        ROW_SETTING_FACEDETECTION,
        ROW_SETTING_REFOCUS_ON_SHOOT,
        ROW_SETTING_ZSL,
        ROW_SETTING_RESTORE_TO_DEFAULT
    };
    
    public static int[] getCommonAdvanceSetting(){
    	Log.d(TAG,"###YUHUAN###getCommonAdvanceSetting");
    	int[] advanceSetting=SETTING_GROUP_COMMON_FOR_ADVANCED_SETTING;
    	 if(!CustomUtil.getInstance().getBoolean("def_TctCamera_shutter_sound_visible", true)){
    		 advanceSetting=new int[]{
    				 ROW_SETTING_PICTURE_RATIO,
    			        ROW_SETTING_GPS_TAG,
    			        ROW_SETTING_FACEDETECTION,
    			        ROW_SETTING_REFOCUS_ON_SHOOT,
    			        ROW_SETTING_ZSL,
    			        ROW_SETTING_RESTORE_TO_DEFAULT 
    		 };
         }
    	 
    	 return advanceSetting;
    }
    

    public static final int[] SETTING_GROUP_COMMON_FOR_VIDEO_SETTING = new int[] {
        ROW_SETTING_VIDEO_QUALITY,
        ROW_SETTING_EIS,
        ROW_SETTING_SOUND_RECORDING,
        ROW_SETTING_LIMIT_FOR_MMS
    };

    public static final int[] SETTING_GROUP_COMMON_FOR_MMSVIDEO_SETTING = new int[] {
        ROW_SETTING_SOUND_RECORDING,
        ROW_SETTING_EIS,
    };

    public static final int[] SETTING_GROUP_COMMON_FOR_VIDEO_SETTING_FRONT = new int[] {
        ROW_SETTING_VIDEO_FRONT_QUALITY,
        ROW_SETTING_SOUND_RECORDING,
        ROW_SETTING_EIS,
        ROW_SETTING_LIMIT_FOR_MMS
    };

    public static final int[] SETTING_GROUP_COMMON_FOR_MMSVIDEO_SETTING_FRONT = new int[] {
        ROW_SETTING_SOUND_RECORDING,
        ROW_SETTING_EIS,
    };

    public static final int[] SETTING_GROUP_COMMON_FOR_MODE_SETTING = new int[] {
        ROW_SETTING_PICTURE_SIZE_FLAG,
        ROW_SETTING_SELF_TIMER,
        ROW_SETTING_ADVANCED,
    };

    //[FEATURE]-Add-BEGIN by TCTNB.(Liu Jie),12/18/2013,550519,VoiceShooting
    public static final int[]  SETTING_GROUP_COMMON_SETTING_WITH_VOICE = new int[] {
        ROW_SETTING_PICTURE_RATIO,
        ROW_SETTING_SHUTTER_SOUND,
        ROW_VOICE_SHUTTER,
        ROW_SETTING_ADVANCED,
    };
    public static final int[] SETTING_GROUP_COMMON_SETTING_FRONT_WITH_VOICE = new int[] {
        ROW_VOICE_SHUTTER,
    };
    //[FEATURE]-Add-END by TCTNB.(Liu Jie)

    public static final int[] SETTING_GROUP_COMMON_FOR_MMS_SETTING = new int[] {
        ROW_SETTING_SHUTTER_SOUND,
    };

    public static final int[] SETTING_GROUP_COMMON_FOR_PANORAMA_MODE_SETTING = new int[] {
        //ROW_SETTING_SHUTTER_SOUND,
    };

    public static final int[] SETTING_GROUP_COMMON_SETTING_FRONT = new int[] {
        //[BUGFIX]-Deleted by TCTNB.hui.shen,11/07/2013, PR-550183,
        //there should be no shutter_sound setting on front_camera.
        ROW_SETTING_PICTURE_RATIO,
    };
    public static final int[] SETTING_GROUP_COMMON_MMS_FRONT = new int[] {
        ROW_SETTING_SHUTTER_SOUND,
    };

    public static final String[] KEYS_FOR_SETTING = new String[SETTING_ROW_COUNT];


    static {
        KEYS_FOR_SETTING[ROW_SETTING_FLASH]             = CameraSettings.KEY_FLASH_MODE;
        KEYS_FOR_SETTING[ROW_SETTING_EXPOSURE]          = CameraSettings.KEY_EXPOSURE;
        KEYS_FOR_SETTING[ROW_SETTING_SCENCE_MODE]       = CameraSettings.KEY_SCENE_MODE;
        KEYS_FOR_SETTING[ROW_SETTING_WHITE_BALANCE]     = CameraSettings.KEY_WHITE_BALANCE;
        KEYS_FOR_SETTING[ROW_SETTING_COLOR_EFFECT]      = CameraSettings.KEY_COLOR_EFFECT;
        KEYS_FOR_SETTING[ROW_SETTING_SHUTTER_SOUND]     = CameraSettings.KEY_SOUND;
        KEYS_FOR_SETTING[ROW_SETTING_PICTURE_SIZE]      = CameraSettings.KEY_PICTURE_SIZE;
        KEYS_FOR_SETTING[ROW_SETTING_ISO]               = CameraSettings.KEY_ISO;
        KEYS_FOR_SETTING[ROW_SETTING_SOUND_RECORDING]   = CameraSettings.KEY_SOUND_RECORDING;
        KEYS_FOR_SETTING[ROW_SETTING_EIS]               = CameraSettings.KEY_VIDEO_EIS;
        KEYS_FOR_SETTING[ROW_SETTING_SHUTTER_SOUND]     = CameraSettings.KEY_SOUND;
        KEYS_FOR_SETTING[ROW_SETTING_JPEG_QUALITY]      = CameraSettings.KEY_JPEG_QUALITY;
        KEYS_FOR_SETTING[ROW_SETTING_SELF_TIMER]        = CameraSettings.KEY_SELF_TIMER;
        KEYS_FOR_SETTING[ROW_SETTING_TIME_LAPSE]        = CameraSettings.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL;
        KEYS_FOR_SETTING[ROW_SETTING_LIVE_EFFECT]       = CameraSettings.KEY_VIDEO_EFFECT;
        KEYS_FOR_SETTING[ROW_SETTING_VIDEO_QUALITY]     = CameraSettings.KEY_VIDEO_QUALITY;
        KEYS_FOR_SETTING[ROW_SETTING_VIDEO_FRONT_QUALITY]   = CameraSettings.KEY_VIDEO_FRONT_QUALITY;
        KEYS_FOR_SETTING[ROW_SETTING_CONTINUOUS_NUM]    = CameraSettings.KEY_CONTINUOUS_NUM;
        KEYS_FOR_SETTING[ROW_SETTING_PICTURE_RATIO]     = CameraSettings.KEY_PICTURE_RATIO;
        KEYS_FOR_SETTING[ROW_SETTING_ADVANCED]          = CameraSettings.KEY_ADVANCED_SETTING;
        KEYS_FOR_SETTING[ROW_SETTING_GPS_TAG]           = CameraSettings.KEY_GPS_TAG;
        //KEYS_FOR_SETTING[ROW_SETTING_ARCSOFT_NIGHT]		= CameraSettings.KEY_CAMERA_ARCSOFT_NIGHT;
        //KEYS_FOR_SETTING[ROW_SETTING_ARCSOFT_ASD]		= CameraSettings.KEY_CAMERA_ARCSOFT_ASD;
        
        //KEYS_FOR_SETTING[ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE]		= CameraSettings.KEY_CAMERA_ARCSOFT_ASD_NIGHT_MODE;
        //KEYS_FOR_SETTING[ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE]		= CameraSettings.KEY_CAMERA_ARCSOFT_ASD_BACK_NIGHT_MODE;
        
        KEYS_FOR_SETTING[ROW_SETTING_FFF]				= CameraSettings.KEY_CAMERA_FFF;
        KEYS_FOR_SETTING[ROW_SETTING_GRID]				= CameraSettings.KEY_CAMERA_GRID;
        KEYS_FOR_SETTING[ROW_SETTING_BACK]              = CameraSettings.KEY_BACK;
        KEYS_FOR_SETTING[ROW_SETTING_RESTORE_TO_DEFAULT] = CameraSettings.KEY_RESTORE_TO_DEFAULT;
        KEYS_FOR_SETTING[ROW_TOUCH_SHUTTER]             = CameraSettings.KEY_TOUCH_SHUTTER;
        KEYS_FOR_SETTING[ROW_SETTING_FOCUS_MODE]        = CameraSettings.KEY_FOCUS_MODE;
        KEYS_FOR_SETTING[ROW_SETTING_AUTOEXPOSURE]      = CameraSettings.KEY_AUTOEXPOSURE;
        KEYS_FOR_SETTING[ROW_SETTING_WAV_DENOISE]       = CameraSettings.KEY_CAMERA_WAV_DENOISE;
        KEYS_FOR_SETTING[ROW_SETTING_AUTOEXPOSURE_LOCK] = CameraSettings.KEY_AUTOEXPOSURE_LOCK;
        KEYS_FOR_SETTING[ROW_SETTING_VIDEO_FLASH_MODE]  = CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE;
        KEYS_FOR_SETTING[ROW_SETTING_PICTURE_SIZE_FLAG] = CameraSettings.KEY_PICTURE_SIZE_FLAG;
        KEYS_FOR_SETTING[ROW_VOICE_SHUTTER] = CameraSettings.KEY_VOICE_SHUTTER;
        KEYS_FOR_SETTING[ROW_SETTING_LIMIT_FOR_MMS]     = CameraSettings.KEY_VIDEO_LIMIT_FOR_MMS_KEY;
        KEYS_FOR_SETTING[ROW_SETTING_ANTIBANDING]       = CameraSettings.KEY_ANTIBANDING;//[BUGFIX]-Add by TCTNB.yufeng.lan,04/22/2014,RR-658514
        KEYS_FOR_SETTING[ROW_SETTING_FACEDETECTION]     = CameraSettings.KEY_FACE_DETECTION;
        KEYS_FOR_SETTING[ROW_SETTING_ZSL]               = CameraSettings.KEY_ZSL;
        KEYS_FOR_SETTING[ROW_SETTING_REFOCUS_ON_SHOOT]  = CameraSettings.KEY_REFOCUS_ON_SHOOT;
        KEYS_FOR_SETTING[ROW_SETTING_SAVEPATH]			= CameraSettings.KEY_CAMERA_SAVEPATH;
    }

    public static String[] getSettingKeys(int[] indexes) {
        if (indexes != null) {
            int len = indexes.length;
            String[] keys = new String[len];
            for (int i = 0; i < len; i++) {
                keys[i] = KEYS_FOR_SETTING[indexes[i]];
            }
            return keys;
        }
        return null;
    }
    
    public static String getSettingKey(int index){
    	if(index<KEYS_FOR_SETTING.length){
    		return KEYS_FOR_SETTING[index];
    	}
    	return null;
    }

    public static int getSettingIndex(String str) {
        if (str != null) {
            for (int i = 0; i < SETTING_ROW_COUNT; i++) {
                if (str == KEYS_FOR_SETTING[i]) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static final int[] MODE_MANUAL_CAMERA_SETTING_ROW = new int[] {
        ROW_SETTING_ISO,
        ROW_SETTING_EXPOSURE,
        //ROW_SETTING_FLASH,
        ROW_SETTING_FOCUS_MODE,
        ROW_SETTING_WHITE_BALANCE,
    };

    public static final int[] MODE_MANUAL_CAMERA_IMAGEVIEW_TYPE = new int[] {
        MODE_MANUAL_WHITE_BALANCE,MODE_MANUAL_EXPOSURE,
    };

    public boolean isManualModeImageViewType(int manualMode) {
        for (int i = 0; i < MODE_MANUAL_CAMERA_IMAGEVIEW_TYPE.length; i++) {
            if (manualMode == MODE_MANUAL_CAMERA_IMAGEVIEW_TYPE[i])
                return true;
        }
        return false;
    }

    public static final int[] MODE_MANUAL_BACK_CAMERA_ORDER = new int[]{
        MODE_MANUAL_ISO,
        MODE_MANUAL_EXPOSURE,
        //MODE_MANUAL_FLASH_DUTY,
        MODE_MANUAL_FOCUS_VALUE,
        MODE_MANUAL_WHITE_BALANCE
    };

    public boolean isManualModeByGivenMode(int mode) {
        for (int i = 0; i < MODE_MANUAL_BACK_CAMERA_ORDER.length; i++) {
            if (mode == MODE_MANUAL_BACK_CAMERA_ORDER[i])
                return true;
        }
        return false;
    }

    public static boolean isZoomEnable(int mode) {
        return MATRIX_ZOOM_ENABLE[mode];
    }

    public synchronized void setListPreference(int row, ListPreference pref) {
        if (LOG) {
            Log.i(TAG, " set mListPrefs[" +row+ " ] = " + pref);
        }
        mListPrefs[row] = pref;
    }


    public synchronized ListPreference getListPreference(int row) {
        return mListPrefs[row];
    }

    public synchronized ListPreference getListPreference(String key) {
        int row = SettingUtils.index(GC.KEYS_FOR_SETTING, key);
        return getListPreference(row);
    }

    public synchronized void clearListPreference() {
        if (LOG) {
            Log.i(TAG, "clearListPreference()");
        }
        for (int i = 0, len = mListPrefs.length; i < len; i++) {
            mListPrefs[i] = null;
        }
    }

    public static PreferenceGroup getPreferenceGroup(int preferenceRes) {
        PreferenceInflater inflater = new PreferenceInflater(mContext);
        PreferenceGroup group =
                (PreferenceGroup) inflater.inflate(preferenceRes);
        return group;
    }
    
    
    static final String[][] VIDEO_FLASH_MAP={
        {"on","torch"},
        {"torch","torch"},
        {"auto","off"},
        {"off","off"},
    };
    
    
    
    public static String getValidFlashModeForVideo(String glbFlashMode){
        for(String[] map : VIDEO_FLASH_MAP){
            if(map[0].equals(glbFlashMode)){
                return map[1];
            }
        }
        return "off";
    }

    // Override setting's values, but not include modes, such as hdr, smile, sports etc.
    public void overrideSettingsValue(final String ... keyvalues) {
        for (int i = 0; i < keyvalues.length; i += 2) {
            String key = keyvalues[i];
            String value = keyvalues[i + 1];
            ListPreference pref = getListPreference(key);
            if (pref != null && key.equals(pref.getKey())) {
                if (value != null && value.equals("default"))
                    pref.setValue(pref.getDefaultValue());
                else if (value != null) pref.setValue(value);
            }
        }
    }

    public void roundNextSettingsValue(final String prefKey, ImageView view) {
        if (prefKey == null) return;
        ListPreference pref = getListPreference(prefKey);
        Log.i(TAG,"roundNextSettingsValue:: pref = "+pref);
        if (pref != null) {
            int index = pref.findIndexOfValue(pref.getValue());
            CharSequence[] values = pref.getEntryValues();
            index = (index + 1) % values.length;
            pref.setValue((String) values[index]);
        }
    }
    
    public String getPrefValue(String prefKey){
        if(prefKey==null){
            return null;
        }
        ListPreference pref = getListPreference(prefKey);
        if(pref!=null){
            String value=pref.getValue();
            if(value==null){
                value=pref.getDefaultValue();
            }
            return value;
        }else{
            return null;
        }
    }
    
    private void roundNextValidSettingsValue(final String prefKey, String ... validValues ){
        if (prefKey == null) return;
        ListPreference pref = getListPreference(prefKey);
        Log.i(TAG,"roundNextSettingsValue:: pref = "+pref);
        if (pref != null) {
            int index = pref.findIndexOfValue(pref.getValue());
            CharSequence[] values = pref.getEntryValues();
            index = (index + 1) % values.length;
            if(validValues==null){
                return;
            }
            for(String validValue : validValues){
                if(validValue.equals((String) values[index])){
                    pref.setValue((String) values[index]);
                    break;
                }
            }
        }
    }

    public void resetPrefsToDefault() {
        for (int i = 0; i < SETTING_ROW_COUNT; i++) {
            if (mListPrefs[i] != null && mListPrefs[i].getDefaultValue() != null) {
                mListPrefs[i].setValue(mListPrefs[i].getDefaultValue());
                if (LOG) {
                    Log.i(TAG, " resetPrefsToDefault mListPrefs[" + i + " ] = " + mListPrefs[i].getDefaultValue());
                }
            }
        }
    }

    public String getPrefDefaultValue(final String prefKey) {
        ListPreference pref = getListPreference(prefKey);
        return pref.getDefaultValue();
    }

    public CharSequence[] getPrefEntries(final String prefKey) {
        ListPreference pref = getListPreference(prefKey);
        return pref.getEntries();
    }

    public CharSequence[] getPrefEntryValues(final String prefKey) {
        ListPreference pref = getListPreference(prefKey);
        return pref.getEntryValues();
    }

    public CharSequence[] getPrefEntryValues(final int prefRow) {
        ListPreference pref = getListPreference(prefRow);
        return pref.getEntryValues();
    }
    
    public void persoPictureSizeTitle(){
    	Log.d(TAG,"###YUHUAN###persoPictureSizeTitle#PictureSizeFlagPre=" + PictureSizeFlagPre);
		if(null == PictureSizeFlagPre) return;
		if(mContext.getCameraId() == CAMERAID_BACK){
			Log.i(TAG, "osca Gc.persoBack");
//			entries = PersoPictureSize.sizeTitleBack;
			
			entries=PictureSizePerso.getInstance().getPersoSupporteTitle(CAMERAID_BACK);
		}else{
			Log.i(TAG, "osca Gc.persoFront");
//			entries = PersoPictureSize.sizeTitleFront;
			entries=PictureSizePerso.getInstance().getPersoSupporteTitle(CAMERAID_FRONT);
		}
		PictureSizeFlagPre.setEntries(entries);
	}
    
    public void persoVideoQuality(){
    	int cameraId = mContext.getCameraId();
    	String[] qualities = VideoSizePerso.getInstance().getPersoSupportedQualities(cameraId);
    	int defualt = VideoSizePerso.getInstance().getDefaultValue(cameraId);
    	ListPreference videoSizes = null;
		if(mContext.getCameraId() == CAMERAID_BACK){
			Log.i(TAG, "osca Gc.persoBack");
			videoSizes = videoSizesBack;
		}else{
			Log.i(TAG, "osca Gc.persoFront");
			videoSizes = videoSizesFront;
		}
		videoSizes.setEntries(VideoSizePerso.getInstance().getPersoSupporteTitle(cameraId));
		videoSizes.setEntryValues(qualities);
		videoSizes.setDefaultValue(qualities[defualt]);
		if(!VideoSizePerso.getInstance().getPersoSupportedQualityList(cameraId).contains(videoSizes.getValue())) {
			boolean changed = false;
			for(String value : qualities) {
				if(String.valueOf(CamcorderProfileEx.getQualityNum(value)).equals(videoSizes.getValue())) {
					videoSizes.setValue(value);
					changed = true;
					break;
				}
			}
			if(!changed) {
				videoSizes.setValue(qualities[defualt]);
			}
		}
	}
    
//    public void persoSavePath(){
//    	Boolean writeable = SDCard.instance().isWriteable();
//    	Log.i(TAG, "nice gc.df= " + writeable);
//    	if(writeable){
//    		String[] entr = new String[]{mContext.getResources().getString(R.string.pref_camera_savepath_entry_0),
//    				mContext.getResources().getString(R.string.pref_camera_savepath_entry_1)};
//    		SavePathPre.setEntries(entr);
//    	}else{
//    		String[] entr = new String[]{mContext.getResources().getString(R.string.pref_camera_savepath_entry_0)};
//    		SavePathPre.setEntries(entr);
//    	}
//    }

    public void initPreference(PreferenceGroup group) {
    	Log.d(TAG,"###YUHUAN###initPreference ");
        ListPreference videoQuality = group.findPreference(CameraSettings.KEY_VIDEO_QUALITY);
        ListPreference videoFrontQuality = group.findPreference(CameraSettings.KEY_VIDEO_FRONT_QUALITY);
        ListPreference timeLapseInterval = group.findPreference(CameraSettings.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL);
        ListPreference pictureSize = group.findPreference(CameraSettings.KEY_PICTURE_SIZE);
        ListPreference pictureSizeFlag = group.findPreference(CameraSettings.KEY_PICTURE_SIZE_FLAG);
        IconListPreference whiteBalance = (IconListPreference) group.findPreference(CameraSettings.KEY_WHITE_BALANCE);
        ListPreference sceneMode = group.findPreference(CameraSettings.KEY_SCENE_MODE);
        ListPreference flashMode = group.findPreference(CameraSettings.KEY_FLASH_MODE);
        ListPreference focusMode = group.findPreference(CameraSettings.KEY_FOCUS_MODE);
        IconListPreference exposure = (IconListPreference) group.findPreference(CameraSettings.KEY_EXPOSURE);
        IconListPreference cameraIdPref = (IconListPreference) group.findPreference(CameraSettings.KEY_CAMERA_ID);
        ListPreference videoFlashMode = group.findPreference(CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE);
        ListPreference videoEffect = group.findPreference(CameraSettings.KEY_VIDEO_EFFECT);
        ListPreference cameraHdr = group.findPreference(CameraSettings.KEY_CAMERA_HDR);
        //ListPreference jpegQuality = group.findPreference(CameraSettings.KEY_JPEG_QUALITY);
        ListPreference selftimer = group.findPreference(CameraSettings.KEY_SELF_TIMER);
        ListPreference sound = group.findPreference(CameraSettings.KEY_SOUND);
        ListPreference touchshutter = group.findPreference(CameraSettings.KEY_TOUCH_SHUTTER);
        ListPreference pictureRatio = group.findPreference(CameraSettings.KEY_PICTURE_RATIO);
        ListPreference advancedSetting = group.findPreference(CameraSettings.KEY_ADVANCED_SETTING);
        ListPreference gpsTag = group.findPreference(CameraSettings.KEY_GPS_TAG);
        //ListPreference arcsoftNight=group.findPreference(CameraSettings.KEY_CAMERA_ARCSOFT_NIGHT);
        //ListPreference arcsoftAsd=group.findPreference(CameraSettings.KEY_CAMERA_ARCSOFT_ASD);
        
        
        //ListPreference arcsoftAsd_night=group.findPreference(CameraSettings.KEY_CAMERA_ARCSOFT_ASD_NIGHT_MODE);
        //ListPreference arcsoftAsd_back_night=group.findPreference(CameraSettings.KEY_CAMERA_ARCSOFT_ASD_BACK_NIGHT_MODE);
        
        ListPreference fastFaceFocus=group.findPreference(CameraSettings.KEY_CAMERA_FFF);
        ListPreference grid=group.findPreference(CameraSettings.KEY_CAMERA_GRID);
        ListPreference back = group.findPreference(CameraSettings.KEY_BACK);
        ListPreference restoreToDefault = group.findPreference(CameraSettings.KEY_RESTORE_TO_DEFAULT);
        ListPreference iso = group.findPreference(CameraSettings.KEY_ISO);
        ListPreference soundRecording = group.findPreference(CameraSettings.KEY_SOUND_RECORDING);
        ListPreference videoEIS = group.findPreference(CameraSettings.KEY_VIDEO_EIS);
        ListPreference autoExposure = group.findPreference(CameraSettings.KEY_AUTOEXPOSURE);
        ListPreference autoExposureLock = group.findPreference(CameraSettings.KEY_AUTOEXPOSURE_LOCK);
        ListPreference wavDenoise = group.findPreference(CameraSettings.KEY_CAMERA_WAV_DENOISE);
        ListPreference voiceshutter = group.findPreference(CameraSettings.KEY_VOICE_SHUTTER);//[FEATURE]-Add by TCTNB.(Liu Jie),12/18/2013,550519,VoiceShooting
        ListPreference limitForMms  = group.findPreference(CameraSettings.KEY_VIDEO_LIMIT_FOR_MMS_KEY);
        ListPreference antibanding = group.findPreference(CameraSettings.KEY_ANTIBANDING);//[BUGFIX]-Add by TCTNB.yufeng.lan,04/22/2014,RR-658514
        ListPreference facedetection = group.findPreference(CameraSettings.KEY_FACE_DETECTION);
        ListPreference zsl = group.findPreference(CameraSettings.KEY_ZSL);
        ListPreference refocusOnShoot = group.findPreference(CameraSettings.KEY_REFOCUS_ON_SHOOT);
        ListPreference savePath = group.findPreference(CameraSettings.KEY_CAMERA_SAVEPATH);
        clearListPreference();
        setListPreference(ROW_SETTING_VIDEO_FRONT_QUALITY, videoFrontQuality);
        setListPreference(ROW_SETTING_WHITE_BALANCE, whiteBalance);

        setListPreference(ROW_SETTING_FLASH, flashMode);
        setListPreference(ROW_SETTING_SELF_TIMER, selftimer);
        setListPreference(ROW_SETTING_SHUTTER_SOUND, sound);
        //setListPreference(ROW_SETTING_JPEG_QUALITY, jpegQuality);
        setListPreference(ROW_TOUCH_SHUTTER, touchshutter);
        setListPreference(ROW_SETTING_PICTURE_RATIO, pictureRatio);
        setListPreference(ROW_SETTING_ADVANCED, advancedSetting);
        setListPreference(ROW_SETTING_BACK, back);
        setListPreference(ROW_SETTING_RESTORE_TO_DEFAULT, restoreToDefault);
        setListPreference(ROW_SETTING_GPS_TAG, gpsTag);
        setListPreference(ROW_SETTING_ISO, iso);
        setListPreference(ROW_SETTING_EXPOSURE, exposure);
        setListPreference(ROW_SETTING_VIDEO_QUALITY, videoQuality);
        setListPreference(ROW_SETTING_SOUND_RECORDING, soundRecording);
        setListPreference(ROW_SETTING_EIS, videoEIS);
        setListPreference(ROW_SETTING_FOCUS_MODE, focusMode);
        setListPreference(ROW_SETTING_AUTOEXPOSURE, autoExposure);
        setListPreference(ROW_SETTING_AUTOEXPOSURE_LOCK, autoExposureLock);
        setListPreference(ROW_SETTING_WAV_DENOISE, wavDenoise);
        setListPreference(ROW_SETTING_VIDEO_FLASH_MODE, videoFlashMode);
        setListPreference(ROW_SETTING_PICTURE_SIZE_FLAG, pictureSizeFlag);
        setListPreference(ROW_VOICE_SHUTTER, voiceshutter);//[FEATURE]-Add by TCTNB.(Liu Jie),12/18/2013,550519,VoiceShooting
        setListPreference(ROW_SETTING_LIMIT_FOR_MMS, limitForMms);
        setListPreference(ROW_SETTING_ANTIBANDING, antibanding);//[BUGFIX]-Add by TCTNB.yufeng.lan,04/22/2014,RR-658514
        setListPreference(ROW_SETTING_FACEDETECTION, facedetection);
        setListPreference(ROW_SETTING_ZSL, zsl);
        //setListPreference(ROW_SETTING_ARCSOFT_NIGHT,arcsoftNight);
        //setListPreference(ROW_SETTING_ARCSOFT_ASD,arcsoftAsd);
        
        //setListPreference(ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE,arcsoftAsd_night);
        //setListPreference(ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE,arcsoftAsd_back_night);
        
        setListPreference(ROW_SETTING_FFF,fastFaceFocus);
        setListPreference(ROW_SETTING_GRID,grid);
        setListPreference(ROW_SETTING_REFOCUS_ON_SHOOT, refocusOnShoot);
        setListPreference(ROW_SETTING_SAVEPATH, savePath);
        PictureSizeFlagPre = group.findPreference(CameraSettings.KEY_PICTURE_SIZE_FLAG);
//        SavePathPre = group.findPreference(CameraSettings.KEY_CAMERA_SAVEPATH);
        persoPictureSizeTitle();
//        persoSavePath();
        //pr1005366-yuguan.chen added begin for video quality perso
        videoSizesFront = group.findPreference(CameraSettings.KEY_VIDEO_FRONT_QUALITY);
        videoSizesBack = group.findPreference(CameraSettings.KEY_VIDEO_QUALITY);
        persoVideoQuality();
        //pr1005366-yuguan.chen added end for video quality perso
    }
/************************************************************************************/
/*                                   SettingChecker-End                             */
/************************************************************************************/

}
