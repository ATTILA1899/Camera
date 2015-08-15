package com.android.camera.manager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera.CameraInfo;
import android.provider.Settings;
import android.view.View;
import android.view.ViewStub;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.GC;
import com.tct.camera.R;
import android.util.Log;
//import com.android.camera.util.HelpUtils;
import android.widget.Toast;
import android.view.Gravity;
/**
 * add by minghui.hua for PR714261,713440
 */
public class HelpGuideManager extends ViewManager implements OnClickListener {
    private static final String TAG = "HelpGuideManager";

    private RelativeLayout mTotalLayout;
    private RelativeLayout mContinousShotLayout;
    private RelativeLayout mQrLayout;

    // [manual mode] +
    private RelativeLayout mManualModeLayout;
    private RelativeLayout mMModeISOLayout;
    private RelativeLayout mMModeExposureLayout;
    private RelativeLayout mMModeWBLayout;
    private RelativeLayout mMModeFocusDistanceLayout;
    private RelativeLayout mMModeFlashDutyLayout;
    private RelativeLayout mMModeShutterSpeedLayout;

    private RelativeLayout mModeHDRLayout;
    private RelativeLayout mModeCollageLayout; // PR873260-lan.shan-001 Add
    
    private RelativeLayout mModeBurstLayout;
    private RelativeLayout mModeScannerLayout;

    private Button mManualModeBtn;
    private Button mMModeISOBtn;
    private Button mMModeExposureBtn;
    private Button mMModeWBBtn;
    private Button mMModeFocusDistanceBtn;
    private Button mMModeShutterSpeedBtn;
    private Button mMModeFlashDutyBtn;

    private Button mModeHDRBtn;
    private Button mModeCollageBtn; // PR873260-lan.shan-001 Add
    
    private Button mModeBurstBtn;
    private Button mModeScannerBtn;

    private HelpTipNode mHelpTipValues[];
    // [manual mode] -

    private Button mContinousShotBtn;
    private Button mHelpDoneButton;

    private boolean mContinusTag = false;
    private boolean mQrTag = false;
    private boolean mHdrTag = false;
    private boolean mCollageTag = false; // PR873260-lan.shan-001 Add
    private String SharedPreferencesName = "tags";

    public HelpGuideManager(CameraActivity context) {
        super(context, ViewManager.VIEW_LAYER_SETTING);
    }

    @Override
    protected View getView() {
        mTotalLayout = (RelativeLayout) inflate(R.layout.continuous_shot_hint);
        return mTotalLayout;
    }

    @Override
    public void show() {
        super.show();
    }
    @Override
    public void hide(){
        super.hide();
    }

    @Override
    public boolean collapse(boolean force) {
        super.hide();
        return false;
    }

    @Override
    protected void onRefresh() {
        //refreshHelpView();
    }

    public boolean isHelpGuideEnable() {
        SharedPreferences sp = getContext().getSharedPreferences(SharedPreferencesName, Context.MODE_PRIVATE);
        mContinusTag = sp.getBoolean("firstboot_camera_tip_dialog", true) ||
                (Settings.System.getInt(getContext().getContentResolver(), "firstboot_camera_tip_dialog",0) == 0);
        mQrTag = sp.getBoolean("firstboot_camera_qr_dialog", true) ||
                (Settings.System.getInt(getContext().getContentResolver(), "firstboot_camera_qr_dialog",0) == 0);
        return mContinusTag|mQrTag;
    }

    public boolean isHdrHelpGuideEnable() {
        SharedPreferences sp = getContext().getSharedPreferences(SharedPreferencesName, Context.MODE_PRIVATE);
        mHdrTag = sp.getBoolean("firstboot_mode_hdr_dialog", true) ||
                (Settings.System.getInt(getContext().getContentResolver(), "firstboot_mode_hdr_dialog",0) == 0);
        return mHdrTag;
    }

    // PR873260-lan.shan-001 Add begin
    public boolean isCollageHelpGuideEnable() {
        SharedPreferences sp = getContext().getSharedPreferences(SharedPreferencesName, Context.MODE_PRIVATE);
        mCollageTag = sp.getBoolean("firstboot_mode_collage_dialog", true) ||
                (Settings.System.getInt(getContext().getContentResolver(), "firstboot_mode_collage_dialog",0) == 0);
        return mCollageTag;
    }
    // PR873260-lan.shan-001 Add end

    /* [FR-842619]Add the tips when first launching Camera,Lijuan Zhang added,20141117 Begin*/
    public void showScannerGuide(int mode){
    	if(mode == GC.MODE_PHOTO && mModeScannerLayout == null) {
        	ViewStub stub = (ViewStub) mTotalLayout.findViewById(R.id.viewstub_scanner);
        	mModeScannerLayout = (RelativeLayout)stub.inflate();
        }
    	mModeBurstLayout.setVisibility(View.GONE);
    	mModeScannerLayout.setVisibility(View.VISIBLE);
    }
    /* [FR-842619]Add the tips when first launching Camera,Lijuan Zhang added,20141117 end*/
    
    private void setTagSP(String tag) {
        SharedPreferences sp = getContext().getSharedPreferences(SharedPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(tag, false);
        editor.commit();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            // [manual mode] +
            case R.id.manual_mode_hint_btn:
                doOnClick(getTagKey(GC.MODE_MANUAL));
                break;
            case R.id.mmode_iso_hint_btn:
                doOnClick(getTagKey(GC.MODE_MANUAL_ISO));
                break;
            case R.id.mmode_exposure_hint_btn:
                doOnClick(getTagKey(GC.MODE_MANUAL_EXPOSURE));
                break;
            case R.id.mmode_whitebalance_hint_btn:
                doOnClick(getTagKey(GC.MODE_MANUAL_WHITE_BALANCE));
                break;
            case R.id.mmode_focus_distance_hint_btn:
                doOnClick(getTagKey(GC.MODE_MANUAL_FOCUS_VALUE));
                break;
            case R.id.mmode_flashduty_hint_btn:
                doOnClick(getTagKey(GC.MODE_MANUAL_FLASH_DUTY));
                break;
            // [manual mode] -

            case R.id.mode_hdr_hint_btn:
                doOnClick(getTagKey(GC.MODE_HDR));
                break;
            // PR873260-lan.shan-001 Add begin
            case R.id.mode_collage_hint_btn:
                doOnClick(getTagKey(GC.MODE_EXPRESSION4));
                break;
            // PR873260-lan.shan-001 Add end
            case R.id.mode_burst_hint_btn:
            	showScannerGuide(GC.MODE_PHOTO);
                break;
            case R.id.mode_scanner_hint_btn:
            	doOnClick(getTagKey(GC.MODE_PHOTO));
                break;
            default:
                break;
        }
    }

    @Override
    public void onOrientationChanged(int orientation) {
        super.onOrientationChanged(orientation);
    }

    // [manual mode] +

    @Override
    public void onRelease() {
        mManualModeLayout = null;
        mMModeISOLayout = null;
        mMModeExposureLayout = null;
        mMModeWBLayout = null;
        mMModeFocusDistanceLayout = null;
        mMModeFlashDutyLayout = null;
        mMModeShutterSpeedLayout = null;

        mModeHDRLayout = null;
        mModeCollageLayout = null; // PR873260-lan.shan-001 Add
        
        mModeBurstLayout = null;
        mModeScannerLayout = null;

        mManualModeBtn = null;
        mMModeISOBtn = null;
        mMModeExposureBtn = null;
        mMModeWBBtn = null;
        mMModeFocusDistanceBtn = null;
        mMModeShutterSpeedBtn = null;
        mMModeFlashDutyBtn = null;

        mModeHDRBtn = null;
        mModeCollageBtn = null; // PR873260-lan.shan-001 Add
        mModeBurstBtn = null;
        mModeScannerBtn = null;
    }

    public boolean onBackPressed() {
        if (mShowing) return true;
        return false;
    }
    public boolean isManualModeHelpTipsShowing() {
        return getHelpTipTag(GC.MODE_MANUAL);
    }

    private boolean getTagValue(SharedPreferences sp, String key) {
        return sp.getBoolean(key, true) || (Settings.System.getInt(getContext().getContentResolver(), key ,0) == 0);
    }

    private String getTagKey(int mode) {
        if (mHelpTipValues == null)
            initHelpTipValues();

        for (HelpTipNode node : mHelpTipValues) {
            android.util.Log.i(TAG,"LMCH09181 mHelpTipValues:"+mHelpTipValues+" node:"+node);
            if (node.getMode() == mode)
                return node.getModeKey();
        }

        return null;
    }

    private void initHelpTipValues() {
        SharedPreferences sp = getContext().getSharedPreferences(SharedPreferencesName, Context.MODE_PRIVATE);
        String keys[] = {
            "firstboot_manual_mode_dialog",
            "firstboot_mmode_exposure_dialog",
            "firstboot_mmode_flashduty_dialog",
            "firstboot_mmode_focus_distance_dialog",
            "firstboot_mmode_whitebalance_dialog",
            "firstboot_mmode_iso_dialog",
            "firstboot_mode_hdr_dialog",
            "firstboot_mode_burstshooting_dialog",
            "firstboot_mode_scanner_dialog",
            "firstboot_mode_collage_dialog", // PR873260-lan.shan-001 Add
        };
        mHelpTipValues = new HelpTipNode[10]; // PR873260-lan.shan-001 Modify
        mHelpTipValues[0] = new HelpTipNode(GC.MODE_MANUAL, keys[0], getTagValue(sp, keys[0]));
        mHelpTipValues[1] = new HelpTipNode(GC.MODE_MANUAL_EXPOSURE, keys[1], getTagValue(sp, keys[1]));
        mHelpTipValues[2] = new HelpTipNode(GC.MODE_MANUAL_FLASH_DUTY, keys[2], getTagValue(sp, keys[2]));
        mHelpTipValues[3] = new HelpTipNode(GC.MODE_MANUAL_FOCUS_VALUE, keys[3], getTagValue(sp, keys[3]));
        mHelpTipValues[4] = new HelpTipNode(GC.MODE_MANUAL_WHITE_BALANCE, keys[4], getTagValue(sp, keys[4]));
        mHelpTipValues[5] = new HelpTipNode(GC.MODE_MANUAL_ISO, keys[5], getTagValue(sp, keys[5]));
        mHelpTipValues[6] = new HelpTipNode(GC.MODE_HDR, keys[6], getTagValue(sp, keys[6]));
        mHelpTipValues[7] = new HelpTipNode(GC.MODE_PHOTO, keys[7], getTagValue(sp, keys[7]));
        mHelpTipValues[8] = new HelpTipNode(GC.MODE_PHOTO, keys[8], getTagValue(sp, keys[8]));
        mHelpTipValues[9] = new HelpTipNode(GC.MODE_EXPRESSION4, keys[9], getTagValue(sp, keys[9])); // PR873260-lan.shan-001 Add
    }

    private boolean getHelpTipTag(int mode) {
        if (mHelpTipValues == null) {
            initHelpTipValues();
        }

        for (HelpTipNode node : mHelpTipValues) {
            if (node.getMode() == mode)
                return node.getModeTag();
        }

        return false;
    }

    private void updateHelpTipTag(String key, boolean value) {
        if (mHelpTipValues == null) {
            initHelpTipValues();
        }

        for (HelpTipNode node : mHelpTipValues) {
            if (key.equalsIgnoreCase(node.getModeKey())) {
                node.setModeTag(value);
                return;
            }
        }
    }

    private void hideAllChild() {
        if (mTotalLayout == null)
            return;
        int count = mTotalLayout.getChildCount();
        View v = null;
        for (int i = 0; i < count; i++) {
            v = mTotalLayout.getChildAt(i);
            v.setVisibility(View.GONE);
        }
        v = null;
    }

    private void doShowManualModeLayout(RelativeLayout layout, Button btn, int viewStubId, int btnId) {
        if (layout == null) {
            ViewStub stub = (ViewStub) mTotalLayout.findViewById(viewStubId);
            layout = (RelativeLayout)stub.inflate();
            btn = (Button) mManualModeLayout.findViewById(btnId);
            btn.setOnClickListener(this);
        }
        layout.setVisibility(View.VISIBLE);
    }

    public void showManualModeHelpViewByCurrentMode(int mode) {
//        if (!getContext().isNonePickIntent()) {
//           return;
//        }
//
//        if (getHelpTipTag(mode)) {
//            super.show();
//        } else {
//            return;
//        }
//        mTotalLayout.bringToFront();
//        hideAllChild();
//
//        switch(mode) {
//            case GC.MODE_MANUAL:
//                if (mManualModeLayout == null) {
//                    ViewStub stub = (ViewStub) mTotalLayout.findViewById(R.id.viewstub_manual_mode_hint);
//                    mManualModeLayout = (RelativeLayout)stub.inflate();
//                    mManualModeBtn = (Button) mManualModeLayout.findViewById(R.id.manual_mode_hint_btn);
//                    mManualModeBtn.setOnClickListener(this);
//                }
//                mManualModeLayout.setVisibility(View.VISIBLE);
//                break;
//
//            case GC.MODE_MANUAL_ISO:
//                if (mMModeISOLayout == null) {
//                    ViewStub stub = (ViewStub) mTotalLayout.findViewById(R.id.viewstub_iso);
//                    mMModeISOLayout = (RelativeLayout)stub.inflate();
//                    mMModeISOBtn = (Button) mMModeISOLayout.findViewById(R.id.mmode_iso_hint_btn);
//                    mMModeISOBtn.setOnClickListener(this);
//                }
//                mMModeISOLayout.setVisibility(View.VISIBLE);
//                break;
//
//            case GC.MODE_MANUAL_EXPOSURE:
//                if (mMModeExposureLayout == null) {
//                    ViewStub stub = (ViewStub) mTotalLayout.findViewById(R.id.viewstub_exposure);
//                    mMModeExposureLayout = (RelativeLayout)stub.inflate();
//                    mMModeExposureBtn = (Button) mMModeExposureLayout.findViewById(R.id.mmode_exposure_hint_btn);
//                    mMModeExposureBtn.setOnClickListener(this);
//                }
//                mMModeExposureLayout.setVisibility(View.VISIBLE);
//                break;
//
//            case GC.MODE_MANUAL_FLASH_DUTY:
//                if (mMModeFlashDutyLayout == null) {
//                    ViewStub stub = (ViewStub) mTotalLayout.findViewById(R.id.viewstub_flash);
//                    mMModeFlashDutyLayout = (RelativeLayout)stub.inflate();
//                    mMModeFlashDutyBtn = (Button) mMModeFlashDutyLayout.findViewById(R.id.mmode_flashduty_hint_btn);
//                    mMModeFlashDutyBtn.setOnClickListener(this);
//                }
//                mMModeFlashDutyLayout.setVisibility(View.VISIBLE);
//                break;
//
//            case GC.MODE_MANUAL_FOCUS_VALUE:
//                if (mMModeFocusDistanceLayout == null) {
//                    ViewStub stub = (ViewStub) mTotalLayout.findViewById(R.id.viewstub_focusdistance);
//                    mMModeFocusDistanceLayout = (RelativeLayout)stub.inflate();
//                    mMModeFocusDistanceBtn = (Button) mMModeFocusDistanceLayout.findViewById(R.id.mmode_focus_distance_hint_btn);
//                    mMModeFocusDistanceBtn.setOnClickListener(this);
//                }
//                mMModeFocusDistanceLayout.setVisibility(View.VISIBLE);
//                break;
//
//            case GC.MODE_MANUAL_WHITE_BALANCE:
//                if (mMModeWBLayout == null) {
//                    ViewStub stub = (ViewStub) mTotalLayout.findViewById(R.id.viewstub_whitebalance);
//                    mMModeWBLayout = (RelativeLayout)stub.inflate();
//                    mMModeWBBtn = (Button) mMModeWBLayout.findViewById(R.id.mmode_whitebalance_hint_btn);
//                    mMModeWBBtn.setOnClickListener(this);
//                }
//                mMModeWBLayout.setVisibility(View.VISIBLE);
//                break;
//            case GC.MODE_HDR:
//                if (mModeHDRLayout == null) {
//                    ViewStub stub = (ViewStub) mTotalLayout.findViewById(R.id.viewstub_hdr);
//                    mModeHDRLayout = (RelativeLayout)stub.inflate();
//                    mModeHDRBtn = (Button) mModeHDRLayout.findViewById(R.id.mode_hdr_hint_btn);
//                    mModeHDRBtn.setOnClickListener(this);
//                }
//                mModeHDRLayout.setVisibility(View.VISIBLE);
//                break;
//            case GC.MODE_PHOTO:
//                if (mModeBurstLayout == null) {
//                    ViewStub stub = (ViewStub) mTotalLayout.findViewById(R.id.viewstub_burstshooting);
//                    mModeBurstLayout = (RelativeLayout)stub.inflate();
//                    mModeBurstBtn = (Button) mModeBurstLayout.findViewById(R.id.mode_burst_hint_btn);
//                    mModeBurstBtn.setOnClickListener(this);
//                }
//                mModeBurstLayout.setVisibility(View.VISIBLE);
//                if(mModeScannerLayout == null) {
//                	ViewStub stub = (ViewStub) mTotalLayout.findViewById(R.id.viewstub_scanner);
//                	mModeScannerLayout = (RelativeLayout)stub.inflate();
//                	mModeScannerBtn = (Button) mModeScannerLayout.findViewById(R.id.mode_scanner_hint_btn);
//                	mModeScannerBtn.setOnClickListener(this);
//                }
//                mModeScannerLayout.setVisibility(View.GONE);
//                break;
//            // PR873260-lan.shan-001 Add begin
//            case GC.MODE_EXPRESSION4:
//                if (mModeCollageLayout == null) {
//                    ViewStub stub = (ViewStub) mTotalLayout.findViewById(R.id.viewstub_collage);
//                    mModeCollageLayout = (RelativeLayout)stub.inflate();
//                    mModeCollageBtn = (Button) mModeCollageLayout.findViewById(R.id.mode_collage_hint_btn);
//                    mModeCollageBtn.setOnClickListener(this);
//                }
//                mModeCollageLayout.setVisibility(View.VISIBLE);
//                break;
//            // PR873260-lan.shan-001 Add end
//            default:
//                break;
//        }
//        mTotalLayout.bringToFront();
    }
    
    

    private void doOnClick(String tag) {
        Settings.System.putInt(mContext.getContentResolver(), tag, 1);
        setTagSP(tag);
        hide();
        updateHelpTipTag(tag, false);
        mContext.initNavigationBar();
    }

    private class HelpTipNode {
        private int mode;
        private String modeKeyString;
        private boolean modeTag;

        public HelpTipNode(int mode, String key, boolean value) {
            this.mode = mode;
            this.modeKeyString = key;
            this.modeTag = value;
        }

        public int getMode() {
            return mode;
        }

        public String getModeKey() {
            return modeKeyString;
        }

        public boolean getModeTag() {
            return modeTag;
        }

        public void setModeTag(boolean modeTag) {
            this.modeTag = modeTag;
        }
    }
    // [manual mode] -

}
