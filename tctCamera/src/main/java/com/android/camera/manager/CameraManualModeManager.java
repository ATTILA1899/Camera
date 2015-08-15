package com.android.camera.manager;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.GC;
import com.android.camera.IconListPreference;
import com.android.camera.PhotoModule;
import com.android.camera.ui.ManualImageView;
import com.android.camera.ui.ManualModeLayout;
import com.android.camera.ui.ManualModeSeekBar;
import com.android.camera.ui.ManualModeWhiteblanceAdapter;
import com.android.camera.ui.ManualShutterSpeedAdapter;
import com.android.camera.ui.ManualTextView;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ui.RotateLayout;
import com.jrdcamera.modeview.HorizontalListView;
import com.tct.camera.R;
//import android.util.SparseArray;
//
//import android.content.ComponentName;
//import android.content.Intent;
//import android.content.IntentFilter;
//import java.util.Collections;
//import com.android.camera.util.CameraUtil;
//import com.android.camera.ui.ManualModeCircleCtlLayout;
//import com.android.camera.ui.ManualModeCircleWheel;
//import com.android.camera.ui.CircleImageView;
//import com.android.camera.ui.CircleTextView;
//import com.android.camera.ui.ShutterButton;

public class CameraManualModeManager extends ViewManager
    implements GridView.OnItemClickListener, View.OnClickListener, ManualModeLayout.OnItemSelectedListener,
    /*ManualModeCircleWheel.WheelChangeListener,*/PhotoModule.CaptureListener/*, Camera.OnParametersReadyListener*/ {

    private static final String TAG = "CameraManualModeManager";

    private GridView mManuGrid;
    private ViewHold viewhold;
    private static final int GRID_COLUMNS = 4;
    public List<Picture> mPictures;
    
    private PictureAdapter adapter;

    public interface ManualModeCallBackListener {
        void updateISOValue(int isoValue);
        void updateManualFocusValue(int focusMode);
        void updateFlashDutyValue(String flashMode);
        void updateWBValue(String wbValue);
        void updateExposureTime(int ecValue);
        void resetManualModeParamters();
    }

    private ManualModeCallBackListener mManualModeCallBackListener;

    private View mManualModeCtlLayout;
    //private Camera mContext;

    private TextView mSelectedTitleTextView;
    private TextView mSelectedTitleTextViewValue;
    private TextView mSelectedValueTextView;
//    private TextView mManualModeBackItem;
//    private ManualModeCircleWheel mManualModeCircleWheel;

//    private TextView backkeyTextview;
//    private TextView resetkeyTextview;
    //private LinearLayout mShutterLayout;
//    private ShutterButton mShutter;

//    private ManualModeCircleCtlLayout mManualModeCircleCtlLayout;
//    private ManualModeLayout mManualModeLayout;
    private HorizontalListView mHorizontalListView;
    private ManualModeWhiteblanceAdapter mWhiteBlanceAdapter;
    private ManualShutterSpeedAdapter mManualShutterSpeedAdapter;
    private LinearLayout mRecyclerParent;
    private boolean mResumed;
    private boolean mInitialized = false;
    private int mCurrentManualMode = GC.MODE_MANUAL_ISO;
    private int mOldManualMode = -1;
    private int mCurrentFocusValue = 0;
//    private String mCurrentTextHint;
    private int mCurrentSettingRow = GC.ROW_SETTING_ISO;
    private String mCurrentSelectedValue;
    private String mCurrentSelectedEntry;
    private ManualTextView mOldText = null;//just for restore the color
    private ManualTextView mCurrentText = null;//just for restore the color
    private ManualImageView mOldManaulImageView = null;
    private ManualImageView mCurrentManualImageView = null;
    private ManualModeSeekBar mManualBar;
    
    private int setISOValue=PhotoModule.ISO_MINVALUE;
    private int setExposureTime=PhotoModule.ISO_EXPOSURETIME_MINVALUE;
    private int setFoucsValue=PhotoModule.FOUCS_POS_RATIO_MINVALUE;
    private int setWBValue=PhotoModule.WB_DEFAULT_AUTO;//WB default value
    private int mSetExposureTimePosition = 0;
    private TypedArray mWBLabel=null;
    private TypedArray mWBEntris= null;
    private TypedArray mWBImage=null;
    
    private static final int MANUAL_MODE_FOCUS_VALUE_10CM = 500;
    private static final int MANUAL_MODE_FOCUS_VALUE_INFINITY = 170;
//    private static final int MANUAL_MODE_FOCUS_VALUE_TOTAL_STEP = MANUAL_MODE_FOCUS_VALUE_10CM - MANUAL_MODE_FOCUS_VALUE_INFINITY;
//    private static final int MANUAL_MODE_FOCUS_VALUE_TOTAL_ANGLE = 360 - 3 * 360 / 12;
//    private static final float MANUAL_MODE_FOCUS_VALUE_STEP_PER_ANGLE = 
//        1.0f * MANUAL_MODE_FOCUS_VALUE_TOTAL_STEP / MANUAL_MODE_FOCUS_VALUE_TOTAL_ANGLE;

//    private static final int[] MANUAL_MODE_WHEEL_SUPPORT_LIST = new int[] {
//        GC.MODE_MANUAL_FLASH_DUTY,
//    };

//    private static final int[] MANUAL_MODE_WHEEL_DRAWABLES = new int[] {
//        R.drawable.manual_mode_circle_wheel_flash_bg_src,
//    };
    
    private boolean isShowingWhenCapture = false;
    private class CircleImageViewHolder {
        String value;
        int icon;
        int highlighticon;
    };
    private HelpGuideManager mHelpGuideManager;
    public CameraManualModeManager(CameraActivity context) {
        super(context);
        mContext = context;
        //mContext.addResumable(this);
        //mContext.addOnParametersReadyListener(this);
        mHelpGuideManager = new HelpGuideManager(mContext);
        setAnimationEnabled(false, false);  // disable the hide/show animation
        initWBTypedArray();
    }

    public CameraManualModeManager(CameraActivity context, int layer) {
        super(context,layer);
        mContext = context;
        //mContext.addResumable(this);
        //mContext.addOnParametersReadyListener(this);
        mHelpGuideManager = new HelpGuideManager(mContext);
        setAnimationEnabled(false, false);  // disable the hide/show animation
        initWBTypedArray();
    }

    @Override
    protected void onRelease() {
        super.onRelease();
        mManualModeCtlLayout = null;
        mInitialized = false;
    }


    @Override
    protected View getView() {
        initialize();
        return mManualModeCtlLayout;
    }
    
    @Override
    public void show() {
        /*if (!isSupportManualMode()) {
            return;
        }*/
        CameraActivity.TraceLog(TAG,"show mCurrentManualMode="+mCurrentManualMode);
        mHelpGuideManager.showManualModeHelpViewByCurrentMode(mCurrentManualMode);
        //mContext.getGC().updateShowSettingContainer(true);
        if(mManualModeCtlLayout == null){
            getViewWithoutShow();
        }else {
            // modify by minghui.hua for PR955004
            if (mContext.getGC().isManualModeImageViewType(mCurrentManualMode)) {
                // S,WB
                changeUI(false);
            } else {
                //ISO, F
                 changeUI(true);
            }
        }
        if(mManualModeCtlLayout != null && !mShowing) {
    
        adapter.notifyDataSetChanged();
//            mManualModeCtlLayout.setTranslationX(mContext.getResources().getDisplayMetrics().widthPixels);
//            ViewPropertyAnimator viewAnimation = mManualModeCtlLayout.animate().translationX(0);
//            viewAnimation.setInterpolator(new ManualModeInterpolator());
//            viewAnimation.setDuration(400).start(); 
        }
        super.show();
        //getView().setAnimation(getFadeInAnimation());
        //mContext.setViewState(GC.VIEW_STATE_MANUAL_MANU);
        if(!mContext.getCameraViewManager().getTopViewManager().mhasShowManualMode && mManualModeCtlLayout != null){
        	mContext.getCameraViewManager().getCameraManualModeManager().hideManualMenu(mContext.getCameraViewManager().getTopViewManager().getViewForHideManualModeMenu());
        	mContext.getCameraViewManager().getTopViewManager().mhasShowManualMode=true;
        }
        
    }
    
    @Override
    protected void fadeIn() {
    	// TODO Auto-generated method stub
    	showManualMenu();
    	super.fadeIn();
    }

    @Override
    public void hide() {
        CameraActivity.TraceLog(TAG, "hide isShowing() = " + isShowing());
        super.hide();
    }

    @Override
    public void onClick(View v) {
//        if (v == backkeyTextview) {
//            Log.i(TAG,"LMCH0915 backkeyTextview click");
//            onBackPressed();
//            mContext.getCameraViewManager().getTopViewManager().showMode();
//        } else if (v == resetkeyTextview) {
//            //TODO:the respond of the reset key
//            Runnable runnable = new Runnable() {
//                @Override
//                public void run() {
//                    //resetManualModePreferences();
//                    //mCameraManualModeManager.onRestorePreferenceClicked();
//                    //mSettingManager.resetManualModeSetting();
//                    ((PhotoModule)(mContext.getCurrentModule())).applyManualPreferenceToParameters();
//                    //applyParameterForFocus(false);
//                    resetManualModePreferences();
//                    adapter.notifyDataSetChanged();
//                    onBackPressed();
//                    mGC.setCurrentMode(GC.MODE_PHOTO);
//                    mContext.notifyModeChanged(GC.MODE_PHOTO);
//                    mContext.getCameraViewManager().getTopViewManager().showMode();
//                }
//            };
//
//            mContext.getCameraViewManager().getRotateDialog().showAlertDialog(null,
//                    mContext.getString(R.string.confirm_restore_toauto_message),
//                    mContext.getString(android.R.string.ok), runnable,
//                    mContext.getString(android.R.string.cancel), null);
//        }
    }

    public void resetManualModePreferences() {
        if (mGC == null)
            return;
        int row = 0;
        for (int i = 0; i < GC.MODE_MANUAL_CAMERA_SETTING_ROW.length ; i++) {
            row = GC.MODE_MANUAL_CAMERA_SETTING_ROW[i];
            mGC.getListPreference(row).setValue(mGC.getListPreference(row).getDefaultValue());
        }
    }

    public boolean onBackPressed() {
        if (mHelpGuideManager.isShowing())return true;
        if (mShowing) {
            hide();
            mContext.setViewState(GC.VIEW_STATE_NORMAL);
            return true;
        } else if(mGC.getCurrentMode() == GC.MODE_MANUAL){
//            mContext.getCameraViewManager().getTopViewManager().setCurrentMode(GC.MODE_PHOTO);
//            mContext.setViewState(GC.VIEW_STATE_NORMAL);
//            return true;
            return false;
        }
        return false;
    }

    private void initializeLayout() {
        mManualModeCtlLayout = inflate(R.layout.manual_mode_ctl_layout);
//        mManualModeLayout = (ManualModeLayout) mManualModeCtlLayout.findViewById(R.id.manual_mode_layout);
        mHorizontalListView=(HorizontalListView)mManualModeCtlLayout.findViewById(R.id.manual_mode_whiteblance);
        mRecyclerParent=(LinearLayout)mManualModeCtlLayout.findViewById(R.id.recycler_parent);
//        mManualModeLayout.setOnItemSelectedListener(this);
        mManuGrid = (GridView) mManualModeCtlLayout.findViewById(R.id.manual_manu_gallery);
        mManuGrid.setSelector(new ColorDrawable(Color.TRANSPARENT));
        adapter = new PictureAdapter(GC.MANU_NAMES, GC.MANU_PICS, GC.MANU_HIGHLIGHT_PICS,mContext);
        mManuGrid.setAdapter(adapter);
        mManuGrid.setNumColumns(GRID_COLUMNS);
        mManuGrid.setOnItemClickListener(this);

        mManualBar = (ManualModeSeekBar)mManualModeCtlLayout.findViewById(R.id.manual_progress_view);
        mManualBar.setCameraManualModeManager(this);
//        backkeyTextview = (TextView) mManualModeCtlLayout.findViewById(R.id.back_key);
//        resetkeyTextview = (TextView) mManualModeCtlLayout.findViewById(R.id.reset_key);
        //mShutterLayout = (LinearLayout) mManualModeCtlLayout.findViewById(R.id.shutter_layout);

//        mManualModeCircleCtlLayout = (ManualModeCircleCtlLayout) mManualModeCtlLayout.findViewById(R.id.manual_mode_circle_layout);
//        mManualModeCircleCtlLayout.setOnItemSelectedListener(this);
        
//        mSelectedTitleTextView = (TextView) mManualModeCtlLayout.findViewById(R.id.top_manual_mode_title);
//        mSelectedTitleTextViewValue = (TextView) mManualModeCtlLayout.findViewById(R.id.top_manual_mode_value);
//        mSelectedValueTextView = (TextView)  mManualModeCtlLayout.findViewById(R.id.top_manual_mode_value);
//
//        mManualModeCircleWheel = (ManualModeCircleWheel) mManualModeCtlLayout.findViewById(R.id.manual_mode_circle_wheel);
//        mManualModeCircleWheel.setWheelChangeListener(this);
//
//        mManualModeBackItem = (TextView) mManualModeCtlLayout.findViewById(R.id.manual_mode_back);
//        mManualModeBackItem.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                mManualModeBackItem.setPressed(true);
//                hide();
//                //mContext.getGC().showManualModeSetting();
//            }
//        });
//        showView(backkeyTextview);
//        showView(resetkeyTextview);
        //mShutterLayout.setBackgroundResource(R.drawable.back_reset);
//        backkeyTextview.setOnClickListener(this);
//        resetkeyTextview.setOnClickListener(this);
//        mShutter = (ShutterButton) mManualModeCtlLayout.findViewById(R.id.shutter_button_photo);
//        mShutter.setOnShutterButtonListener((PhotoModule)mContext.getCurrentModule());
    }

    @Override
    public void onCaptureDone() {
        if (isShowingWhenCapture) {
            isShowingWhenCapture = false;
            show();
        }
    }

    @Override
    public void onCaptureStart() {
        CameraActivity.TraceLog(TAG,"onCaptureStart isShowing:"+isShowing());
        if (isShowing()) {
            isShowingWhenCapture = true;
            hide();
        }
    }
    private void reInflateManualModeCtlLayout() {
        mInitialized = false;
        try{
            if (mResumed) {
                initialize();
            }
        } catch(Exception e) {
            CameraActivity.TraceLog(TAG, "reInflateManualModeCtlLayout Exception:" + e);
        }
    }

    private static String[] convertToStringArray(CharSequence[] charSequences) {
        if (charSequences instanceof String[]) {
            return (String[]) charSequences;
        }
        String[] strings = new String[charSequences.length];
        for (int index = 0; index < charSequences.length; index++) {
            strings[index] = charSequences[index].toString();
        }
        return strings;
    }

//    private int getRowByCurrentManualMode() {
//        //int modeIndex = mCurrentManualMode - GC.MODE_MANUAL_BACK_CAMERA_ORDER[0];
//        //modeIndex = (modeIndex >= 0 && modeIndex < GC.MODE_MANUAL_CAMERA_SETTING_ROW.length) ? modeIndex : 0;
//
//        return GC.ROW_SETTING_WHITE_BALANCE;//GC.MODE_MANUAL_CAMERA_SETTING_ROW[modeIndex];
//    }

    private String[] getCurrentManualModeSupportValues() {
        String[] supportList = convertToStringArray(mContext.getGC().getListPreference(mCurrentSettingRow).getEntryValues());
        /*if (mCurrentManualMode == GC.MODE_MANUAL_FOCUS_VALUE) {
            return updateFocusValueModeSupportValues(supportList);
        } else */
        if (mCurrentManualMode == GC.MODE_MANUAL_EXPOSURE) {
            return updateExposureCompensationSupportValues(supportList);
        } else {
            return supportList;
        }
    }
    private String[] getCurrentManualModeSupportEntries() {
        String[] supportList = convertToStringArray(mContext.getGC().getListPreference(mCurrentSettingRow).getEntries());
        /*if (mCurrentManualMode == GC.MODE_MANUAL_FOCUS_VALUE) {
            return updateFocusValueModeSupportValues(supportList);
        } else */
        if (mCurrentManualMode == GC.MODE_MANUAL_EXPOSURE) {
            return updateExposureCompensationSupportValues(supportList);
        } else {
            return supportList;
        }
    }

    private String[] updateExposureCompensationSupportValues(String[] originalValues) {
        for (int i = 0; i < originalValues.length; i++) {
            if (originalValues[i].startsWith("-") || "0".equals(originalValues[i]))
                continue;
            else
                originalValues[i] = "+" + originalValues[i];
        }

        return originalValues;
    }

    private String[] updateFocusValueModeSupportValues(String[] originalValues) {
        String [] focusWithPlaceHolders = new String[originalValues.length + 10];

        for (int i = 0; i < focusWithPlaceHolders.length; i++) {
            focusWithPlaceHolders[i] = (i < originalValues.length) ? 
                                    originalValues[i] : 
                                    " ";   // use the " " char to occupy the space
        }

        return focusWithPlaceHolders;
    }

    private int[] getCurrentManualModeSupportIcons() {
        return ((IconListPreference)mContext.getGC().getListPreference(mCurrentSettingRow)).getIconIds();
    }

    private int[] getCurrentManualModeSupportHighLightIcons() {
        return ((IconListPreference)mContext.getGC().getListPreference(mCurrentSettingRow)).getHighlightIconIds();
    }

    private void updatePreferenceValue(String value) {
        if (mCurrentManualMode == GC.MODE_MANUAL_EXPOSURE && value.startsWith("+"))
            value = value.substring(1);
        else if (mCurrentManualMode == GC.MODE_MANUAL_SHUTTER_SPEED && !"0".equals(value))
//            mContext.getGC().getListPreference(GC.ROW_SETTING_EXPOSURE).setValue("0"); // reset it, as the shutter speed is not auto
        mContext.getGC().getListPreference(mCurrentSettingRow).setValue(value);
    }
    
    private void addCircleTextView(String selectedValue) {
        String[] supportList = getCurrentManualModeSupportValues();
        String[] supportEntryList = getCurrentManualModeSupportEntries();
//        String[] supportEntryValueList;
//        String[] supportEntryList4Use;
//        supportEntryList4Use = ajustEntryListOrder(supportList, supportEntryList, selectedValue);
//        supportEntryValueList = ajustEntryValueListOrder(supportList, supportEntryList, selectedValue);
//        for (int i = 0; i < supportEntryList4Use.length; i++) {
////            CircleTextView ctv = new CircleTextView(mContext);
//            ManualTextView ctv = new ManualTextView(mContext);
//            if ("auto".equals(supportEntryList4Use[i])) {
//                ctv.setText("Auto");
//            } else {
//                if (supportEntryList4Use[i].length() > 2) {
//                    ctv.setText(supportEntryList4Use[i]);
//                } else {
//                    ctv.setText(" " + supportEntryList4Use[i]);
//                }
//            }
//            ctv.setName(supportEntryList4Use[i]);
//            ctv.setEntryValue(supportEntryValueList[i]);
//            ctv.setGravity(Gravity.CENTER);
////            mManualModeCircleCtlLayout.addView(ctv);
////            mManualModeLayout.addView(ctv);
//        }
        initManualSeekbar();
//        mManualBar.initManualValue(supportList, supportEntryList);
//        mManualBar.setManualDefaultValue(mCurrentSelectedEntry);
//        mManualBar.setManualTitle(mContext.getGC().getListPreference(mCurrentSettingRow).getTitle());
    }

    private String[] ajustEntryListOrder(String[] orginalValues,String[] orginalEntries, String selectedValue) {
        String[] ajustedValues = new String[orginalEntries.length];
        for (int i = 0; i < orginalEntries.length; i++) {
            ajustedValues[i] = orginalEntries[i];
        }
//        int pos = 0;
//        for (int i = 0; i < orginalEntries.length; i++) {
//            Log.i(TAG,"LMCH09142 orginalValues[i]:"+orginalValues[i]+" selectedValue:"+selectedValue);
//            if (orginalEntries[i].equals(selectedValue)) {
//                pos = i;
//                break;
//            }
//        }
//
//        int moveLength = orginalEntries.length - pos;
//
//        for (int i = 0, j = moveLength; i < orginalEntries.length; i++, j++) {
//            if (i < pos) {
//                ajustedValues[j] = orginalEntries[i];
//            } else {
//                ajustedValues[i - pos] = orginalEntries[i];
//            }
//        }
        return ajustedValues;
    }

    private String[] ajustEntryValueListOrder(String[] orginalValues,String[] orginalEntries, String selectedValue) {
        String[] ajustedValues = new String[orginalValues.length];
        for (int i = 0; i < orginalValues.length; i++) {
            ajustedValues[i] = orginalValues[i];
        }
//        int pos = 0;
        
//        for (int i = 0; i < orginalEntries.length; i++) {
//            if (orginalEntries[i].equals(selectedValue)) {
//                pos = i;
//                break;
//            }
//        }
//        
//
//        int moveLength = orginalEntries.length - pos;
//
//        for (int i = 0, j = moveLength; i < orginalValues.length; i++, j++) {
//            if (i < pos) {
//                ajustedValues[j] = orginalValues[i];
//            } else {
//                ajustedValues[i - pos] = orginalValues[i];
//            }
//        }
        return ajustedValues;
    }

    private CircleImageViewHolder[] ajustCircleImageViewValuesOrder(CircleImageViewHolder[] civhs, String selectedValue) {
        CircleImageViewHolder[] ajustedCivhs = new CircleImageViewHolder[civhs.length];

        System.arraycopy(civhs, 0, ajustedCivhs, 0, civhs.length);
        for (int i = 0; i < civhs.length; i++){
            ajustedCivhs[i] = civhs[i];
        }
//        int pos = 0;
//        for (int i = 0; i < civhs.length; i++) {
//            if (civhs[i].value.equals(selectedValue)) {
//                pos = i;
//                break;
//            }
//        }
//        int moveLength = civhs.length - pos;
//        for (int i = 0, j = moveLength; i < civhs.length; i++, j++) {
//            if (i < pos) {
//                ajustedCivhs[j] = civhs[i];
//            } else {
//                ajustedCivhs[i - pos] = civhs[i];
//            }
//        }

        return ajustedCivhs;
    }

    private CircleImageViewHolder[] getCircleImageViewHolders() {
        String[] supportValues = getCurrentManualModeSupportValues();
        int[] supportIcons = getCurrentManualModeSupportIcons();
        int[] supportHighLightIcons = getCurrentManualModeSupportHighLightIcons();

        CircleImageViewHolder[] civhs = new CircleImageViewHolder[supportValues.length];

        for (int i = 0; i < supportValues.length; i++) {
            civhs[i] = new CircleImageViewHolder();
            civhs[i].value = supportValues[i];
            civhs[i].icon = supportIcons[i];
            civhs[i].highlighticon = supportHighLightIcons[i];
        }

        return civhs;
    }

    private void addCircleImageView(String selectedValue) {
        CircleImageViewHolder[] civhs = ajustCircleImageViewValuesOrder(getCircleImageViewHolders(), selectedValue);

        for (int i = 0; i < civhs.length; i++) {
//            CircleImageView civ = new CircleImageView(mContext);
            ManualImageView civ = new ManualImageView(mContext);
//           if(i == civhs.length-1){
//                civ.setBackgroundResource(R.drawable.circle_image_diver_right);
//            }else if(i == 0){
//                civ.setBackgroundResource(R.drawable.circle_image_diver_left);
//            }
            if((i+1)%2 == 0){
                civ.setBackgroundResource(R.drawable.circle_image_diver);
            } else{
                civ.setBackgroundResource(R.drawable.circle_image_diver_right);
            }
            civ.setImageResource(civhs[i].icon);
            civ.setName(civhs[i].value);
            civ.setHighlightId(civhs[i].highlighticon);
            civ.setNormalId(civhs[i].icon);
//            mManualModeCircleCtlLayout.addView(civ);
    //        mManualModeLayout.setVisibility(View.VISIBLE);
     //       mManualModeLayout.addView(civ);
        }
    }

    public void initialize() {
        CameraActivity.TraceLog(TAG, "initialize");
        //mCurrentSettingRow = getRowByCurrentManualMode();
        if(mContext.getGC().getListPreference(mCurrentSettingRow)==null){
        	return;
        }
        mCurrentSelectedEntry = mContext.getGC().getListPreference(mCurrentSettingRow).getEntry();
        mCurrentSelectedValue = mContext.getGC().getListPreference(mCurrentSettingRow).getValue();
        CameraActivity.TraceLog(TAG,"initialize mCurrentSelectedValue:"+mCurrentSelectedValue);
        /*if (isWheelSupport(mCurrentManualMode)) {
            initializeWheel();
        } else {*/
        initializeCircleCtlLayout();
        //}
//        updateTitleTextView();
        // ensure the mode values are same as the selection on the slide
        updateModeSelection();
        
        mInitialized = true;
    }

    private void initializeCircleCtlLayout() {
        initializeLayout();
 //       mManualModeLayout.removeAllViews();

        if (mContext.getGC().isManualModeImageViewType(mCurrentManualMode)) {
            CameraActivity.TraceLog(TAG, "initializeCircleCtlLayout mCurrentSelectedEntry:" + mCurrentSelectedEntry);
 //           addCircleImageView(mCurrentSelectedValue);
 //          mManualModeLayout.setManualModeCircleViewType(
//                    ManualModeLayout.MANUAL_MODE_CIRCLE_IMAGEVIEW_TYPE);
            initManualRecyclerView();
            changeUI(false);
        } else {
        	 changeUI(true);
        	 initManualSeekbar();
//            mManualModeLayout.setManualModeCircleViewType(
//                    ManualModeLayout.MANUAL_MODE_CIRCLE_TEXTVIEW_TYPE);
        }
//        
//        if (mManualModeCircleCtlLayout == null)
//            initializeLayout();
//        mManualModeCircleCtlLayout.removeAllViews();
//
//        if (mContext.getGC().isManualModeImageViewType(mCurrentManualMode)) {
//            Log.i(TAG,"LMCH0917 mCurrentSelectedEntry:"+mCurrentSelectedEntry);
//            addCircleImageView(mCurrentSelectedValue); // add the circle image views to the layout
//            mManualModeCircleCtlLayout.setManualModeCircleViewType(
//                ManualModeCircleCtlLayout.MANUAL_MODE_CIRCLE_IMAGEVIEW_TYPE);
//        } else {
//            addCircleTextView(mCurrentSelectedEntry); // add the circle text views to the layout
//            mManualModeCircleCtlLayout.setManualModeCircleViewType(
//            ManualModeCircleCtlLayout.MANUAL_MODE_CIRCLE_TEXTVIEW_TYPE);
//        }
//        mManualModeCircleCtlLayout.setVisibility(View.VISIBLE);
//        mManualModeCircleWheel.setVisibility(View.GONE);

    }

    private void updateModeSelection(){
        /*if (isWheelSupport(mCurrentManualMode)) {
             onSelectionChange(mCurrentSelectedEntry);
        } else {*/
//             onSelectionChange(mManualModeCircleCtlLayout.getSelectedItem(),
//                mCurrentSelectedEntry, mCurrentSelectedValue);
        //}
//        mManualModeLayout.initSelectedItem(mCurrentSelectedValue, mCurrentSelectedEntry);
//        onSelectionChange(mManualModeLayout.getSelectedItem(),
//                mCurrentSelectedEntry, mCurrentSelectedValue);
    	if(mWhiteBlanceAdapter != null){
    		mWhiteBlanceAdapter.notifyDataSetChanged();
    	}
    	if(mManualShutterSpeedAdapter != null){
    		mManualShutterSpeedAdapter.notifyDataSetChanged();
    	}
    	
    }

//    private int getIndexByGivenValueInValueList(String selectedValue, String[] supportList) {
//        for (int i = 0; i < supportList.length; i++)
//            if (supportList[i].equals(selectedValue))
//                return i;
//
//        return 0;
//    }
//
//    private int getWheelManualModeIndex(int mode) {
//        for (int i = 0; i < MANUAL_MODE_WHEEL_SUPPORT_LIST.length; i++)
//            if (MANUAL_MODE_WHEEL_SUPPORT_LIST[i] == mode)
//                return i;
//
//        return -1;
//    }

//    private int getWheelDrawablesByMode(int mode) {
//        int index = getWheelManualModeIndex(mode);
//        return (index < 0 || index > MANUAL_MODE_WHEEL_DRAWABLES.length) ?
//            R.drawable.manual_mode_circle_bg_src :
//            MANUAL_MODE_WHEEL_DRAWABLES[index];
//    }

//    private boolean isWheelSupport(int mode) {
//        if (getWheelManualModeIndex(mode) < 0)
//            return false;
//
//        return true;
//    }

    private  int mCurrentOrientation = 0;
    private int mLastOrientation = 0;
    @Override
    public void onOrientationChanged(int orientation) {
    	mCurrentOrientation = orientation;
    	if(mLastOrientation != mCurrentOrientation){
    		if(mManualShutterSpeedAdapter!=null){
        		mManualShutterSpeedAdapter.notifyDataSetChanged();
        	}
    		mLastOrientation = mCurrentOrientation;
    	}
    	
        super.onOrientationChanged(orientation);
    }

    @Override
    public boolean collapse(boolean force) {
        if (isShowing()) {
            hide();
            return true;
        }
        return false;
    }

    public void onModeChanged(int mode) {
        mCurrentManualMode = mode;
        reInflateManualModeCtlLayout();
        updateTitleTextView();
        show();
    }

    public void onRestorePreferenceClicked() {
        updateTitleTextView();
        if (mSelectedValueTextView != null)
            mSelectedValueTextView.setText(mCurrentSelectedEntry);
        reInflateManualModeCtlLayout();
    }

    /*public boolean isSupportManualMode() {
        return CameraUtil.isManualModeSupport() && 
            (mContext.getGC().isManualMode() || mContext.getGC().isManualModeMain()) &&
            (mContext.getCameraId() == CameraInfo.CAMERA_FACING_BACK);
    }

    public boolean isNeedChangeToManualFocus(){
        return isSupportManualMode() && !"auto".equalsIgnoreCase(mContext.getListPreference(GC.ROW_SETTING_FOCUS_VALUE).getValue());
    }*/

    public void setManualModeCallBackListener(ManualModeCallBackListener listener) {
        mManualModeCallBackListener = listener;
    }

    /*@Override
    public void onCameraParameterReady() {
        // manualMain will not invoke the onCameraParameterReady, so ignore it.
        if (!isSupportManualMode()) {
            Log.w(TAG, "WARNING! the current scenario not support manual mode. Do nothing !");
            return;
        }
        // onCameraParameterReady always later than initialize, so ensure the right parameter will set to bottom layer
//        updateModeSelection();
        if (mContext.getGC().getCurrentDirtyManualModes().size() != 0) {
            mContext.applyManualPreferenceToParameters();
        }
    }*/

    @Override
    public void onSpecialRegionAngleUpdate(float angle, boolean clockwise) {
        if (mManualModeCallBackListener == null) {
            CameraActivity.TraceLog(TAG, "onSpecialRegionAngleUpdate mManualModeCallBackListener is NULL !");
            return;
        }

        switch(mCurrentManualMode) {
            case GC.MODE_MANUAL_FOCUS_VALUE:
                /*if (mCurrentFocusValue == 0) {
                    mCurrentFocusValue =  clockwise ? MANUAL_MODE_FOCUS_VALUE_10CM - (int)(MANUAL_MODE_FOCUS_VALUE_STEP_PER_ANGLE * angle)
                           : MANUAL_MODE_FOCUS_VALUE_INFINITY - (int)(MANUAL_MODE_FOCUS_VALUE_STEP_PER_ANGLE * angle);
                } else {
                    mCurrentFocusValue = mCurrentFocusValue - (int)(MANUAL_MODE_FOCUS_VALUE_STEP_PER_ANGLE * angle);
                }*/
                //mManualModeCallBackListener.updateManualFocusValue(mCurrentFocusValue);
                break;
        }

        if (mSelectedValueTextView != null)
            mSelectedValueTextView.setText(String.valueOf(mCurrentFocusValue));
    }
    


    private void updateTitleTextView() {
        if (mGC.isManualModeByGivenMode(mCurrentManualMode) && mSelectedTitleTextView != null) {
            mSelectedTitleTextViewValue.setText(mCurrentSelectedEntry);
            mSelectedTitleTextView.setText(mContext.getString(GC.MODE_NAMES[mCurrentManualMode])+": ");
        }
    }

    private String getStringByValue(String value) {
        if (mCurrentManualMode == GC.MODE_MANUAL_EXPOSURE && value.startsWith("+"))
            value = value.substring(1);
        return mContext.getGC().getListPreference(mCurrentSettingRow).findStringOfValue(value);
    }

    public int getExposureValue(String newValue) {
        int ecValue = 0;
        try{
            if (newValue.startsWith("-")) {
                // trim the negative flag
                ecValue = -1 * Integer.parseInt(newValue.substring(1));
            } else if (newValue.startsWith("+")){
                ecValue = Integer.parseInt(newValue.substring(1));
            } else {
                // 0 is the default value, so do nothing
            }
        } catch(java.lang.NumberFormatException e) {
            // use the defalut value : 0
        }

        return ecValue;
    }

    public int getShutterSpeedValue(String newValue) {
        int shutterSpeed = 0;
        try{
            if (newValue.endsWith("\"")) {
                // string end with the "\0", so minus the "s" char
                shutterSpeed = 1000 * 1000 * Integer.parseInt(newValue.substring(0, newValue.length() - 1)); // unit:s -> us
            } else if ("bulb".equalsIgnoreCase(newValue)) {
                shutterSpeed = -1;
            } else if ("auto".equalsIgnoreCase(newValue)) {

            } else {
                shutterSpeed = 1000 * 1000 / Integer.parseInt(newValue);
            }
        } catch(java.lang.NumberFormatException e) {
            // use the defalut value : 0
        } catch(java.lang.ArithmeticException ae) {
            CameraActivity.TraceLog(TAG, "ArithmeticException" + ae);
        }
        return shutterSpeed;
    }

    public int getFocusValue(String newValue) {
        int focusValue = 0;
        if ("auto".equalsIgnoreCase(newValue)) {
            // TO-DO-LIST
            mCurrentFocusValue = 0;
        } else if ("Macro".equalsIgnoreCase(newValue)) {
            focusValue = MANUAL_MODE_FOCUS_VALUE_10CM;
            mCurrentFocusValue = 0;
        } else if ("Infinity".equalsIgnoreCase(newValue)){
            focusValue = MANUAL_MODE_FOCUS_VALUE_INFINITY;
            mCurrentFocusValue = 0;
        } else {
        }

        return focusValue;
    }

    public int getFlashDutyValue(String newValue) {
        int flashDuty = 0; //auto

        // bad design, should be re-implement future
        // -1 is always the default value in the bottom layer, so start from 2/-2
        if ("off".equalsIgnoreCase(newValue) || "auto".equalsIgnoreCase(newValue)) {
            flashDuty = -1;
        } else if ("flash-low".equalsIgnoreCase(newValue)) {
            flashDuty = 2;
        } else if ("flash-medium".equalsIgnoreCase(newValue)) {
            flashDuty = 3;
        } else if ("flash-high".equalsIgnoreCase(newValue)) {
            flashDuty = 4;
        } else if ("flash-full".equalsIgnoreCase(newValue)){
            flashDuty = 5;
        } else if ("torch-low".equalsIgnoreCase(newValue)){
            flashDuty = -2;
        } else if ("torch-medium".equalsIgnoreCase(newValue)){
            flashDuty = -3;
        } else if ("torch-high".equalsIgnoreCase(newValue)){
            flashDuty = -4;
        } else if ("torch-full".equalsIgnoreCase(newValue)){
            flashDuty = -5;
        }

        return flashDuty;
    }

    @Override
    public void onSelectionChange(View view, String name, String entryValue) {
        CameraActivity.TraceLog(TAG,"onSelectionChange view:"+view+" name:"+name+" entryValue:"+entryValue);
        mCurrentSelectedValue = entryValue;
        mCurrentSelectedEntry = name;
//        updateTitleTextView();

        if (view instanceof ManualTextView) {
            mCurrentText = (ManualTextView)view;
            mCurrentText.setTextColor(mContext.getResources().getColor(R.color.circle_item_highlight_color));
            CameraActivity.TraceLog(TAG,"onSelectionChange mCurrentText:"+mCurrentText);

            if (mOldText == null) {
                mOldText = (ManualTextView)view;
            } else if (mCurrentText != mOldText){
                CameraActivity.TraceLog(TAG,"onSelectionChange old text restore color oldText:"+mOldText);
                mOldText.setTextColor(mContext.getResources().getColor(R.color.circle_item_normal_color));
                mOldText = mCurrentText;
            }
        }

        if (view instanceof ManualImageView) {
            mCurrentManualImageView = (ManualImageView) view;
            mCurrentManualImageView.setImageResource(mCurrentManualImageView.getHighLightId());
            if (mOldManaulImageView == null) {
                mOldManaulImageView = mCurrentManualImageView;
            } else if (mCurrentManualImageView != mOldManaulImageView){
                mOldManaulImageView.setImageResource(mOldManaulImageView.getNormalId());
                mOldManaulImageView = mCurrentManualImageView;
            }
        }
        if (mManualModeCallBackListener == null) {
            CameraActivity.TraceLog(TAG, "onSelectionChange Error ! mManualModeCallBackListener is NULL");
            return;
        }
        if (adapter != null) adapter.notifyDataSetChanged();
        String newValue = entryValue;
        try {
            updatePreferenceValue(newValue);
        } catch(java.lang.IllegalArgumentException e) {
            CameraActivity.TraceLog(TAG, "onSelectionChange IllegalArgumentException:" + e);
        }
        switch(mCurrentManualMode) {
            case GC.MODE_MANUAL_ISO:
                mManualModeCallBackListener.updateISOValue(setISOValue);
                break;

            case GC.MODE_MANUAL_EXPOSURE:
                mManualModeCallBackListener.updateExposureTime(setExposureTime);
                break;

            case GC.MODE_MANUAL_SHUTTER_SPEED:
//                mManualModeCallBackListener.updateShutterSpeedValue(getShutterSpeedValue(newValue));
                break;

//            case GC.MODE_MANUAL_FLASH_DUTY:
//                mManualModeCallBackListener.updateFlashDutyValue(mCurrentSelectedValue);
//                break;

            case GC.MODE_MANUAL_FOCUS_VALUE:
                 mManualModeCallBackListener.updateManualFocusValue(setFoucsValue);
                break;

            case GC.MODE_MANUAL_WHITE_BALANCE:
                mManualModeCallBackListener.updateWBValue(mWBEntris.getString(setWBValue));
                break;

            default:
                break;
        }

        if (mSelectedValueTextView != null)
            mSelectedValueTextView.setText(getStringByValue(newValue));
        
         mContext.getShutterManager().refresh();

    }

	public void saveManualProgressBarValues(String value) {
		if (adapter != null) adapter.notifyDataSetChanged();
		try {
			updatePreferenceValue(value);
		} catch (java.lang.IllegalArgumentException e) {
		    CameraActivity.TraceLog(TAG, "saveManualProgressBarValues IllegalArgumentException:" + e);
		}
		switch (mCurrentManualMode) {
		case GC.MODE_MANUAL_ISO:
			setISOValue=Integer.parseInt(value);
			mManualModeCallBackListener.updateISOValue(setISOValue);
			break;

		case GC.MODE_MANUAL_EXPOSURE:
			setExposureTime=Integer.parseInt(value);
			mManualModeCallBackListener
					.updateExposureTime(setExposureTime);
			break;

		case GC.MODE_MANUAL_FOCUS_VALUE:
			setFoucsValue=Integer.parseInt(value);
			mManualModeCallBackListener
					.updateManualFocusValue(setFoucsValue);
			break;

		case GC.MODE_MANUAL_WHITE_BALANCE:
			setWBValue=Integer.parseInt(value);
			mManualModeCallBackListener.updateWBValue(mWBEntris.getString(setWBValue));
			break;

		default:
			break;
		}
		if (mSelectedValueTextView != null)
			mSelectedValueTextView.setText(getStringByValue(value));
	}

//    @Override
//    public void onSelectionChange(String newValue) {
//        try {
//            updatePreferenceValue(newValue);
//        } catch(java.lang.IllegalArgumentException e) {
//            Log.e(TAG, "IllegalArgumentException:" + e);
//        }
//        switch(mCurrentManualMode) {
//            case GC.MODE_MANUAL_FLASH_DUTY:
////                mManualModeCallBackListener.updateFlashDutyValue(newValue);
//                break;
//        }
//
//        if (mSelectedValueTextView != null)
//            mSelectedValueTextView.setText(getStringByValue(newValue));
//        Log.i(TAG,"LMCH09142 value:"+newValue+" mCurrentSelectedValue:"+mCurrentSelectedValue);
//        mCurrentSelectedEntry = newValue;
//        updateTitleTextView();
//
//        mContext.getShutterManager().refresh();
//    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        switch(position+1) {
            case GC.MANU_ISO:
                mCurrentManualMode = GC.MODE_MANUAL_ISO;
                mCurrentSettingRow = GC.ROW_SETTING_ISO; 
                break;
            case GC.MANU_EXPOSURE:
                mCurrentManualMode = GC.MODE_MANUAL_EXPOSURE;
                mCurrentSettingRow = GC.ROW_SETTING_EXPOSURE; 
                break;
//            case GC.MANU_FLASH:
//                mCurrentManualMode = GC.MODE_MANUAL_FLASH_DUTY;
//                mCurrentSettingRow = GC.ROW_SETTING_FLASH; 
//                break;
            case GC.MANU_WHITEBLANCE:
                mCurrentManualMode = GC.MODE_MANUAL_WHITE_BALANCE;
                mCurrentSettingRow = GC.ROW_SETTING_WHITE_BALANCE;
                break;
            case GC.MANU_FOCUS:
                mCurrentManualMode = GC.MODE_MANUAL_FOCUS_VALUE;
                mCurrentSettingRow = GC.ROW_SETTING_FOCUS_MODE; 
                break;
            default:break;
        }
        
        if (mOldManualMode == mCurrentManualMode) {
            return;
        }
        mOldManualMode = mCurrentManualMode;
        reInflateWithoutHide();
    }

    class PictureAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
		private RotateLayout mRotateLayout;


        public PictureAdapter(int[] titles, int[] images,int[] hightlightimages, Context context) {
            super();
            mPictures = new ArrayList<Picture>();
            mInflater = LayoutInflater.from(context);
            for (int j = 0; j < titles.length; j++) {
                if (titles[j] != 0 && images[j] != 0) {
                    android.util.Log.i(TAG,"LMCH0911 titles[j]:"+titles[j]+" images[j]:"+images[j]);
                    Picture pic = new Picture(mContext.getString(titles[j]), images[j], hightlightimages[j]);
                    mPictures.add(pic);
                }
            }
        }
        @Override
        public int getCount() {
            if (mPictures != null) {
                return mPictures.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return mPictures.get(position);
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
//                convertView = mInflater.inflate(R.layout.manual_manu_item, null);
                convertView = mInflater.inflate(R.layout.manual_manu_gridview_item, null);
                viewhold = new ViewHold();
                viewhold.mTitleTextView = (TextView) convertView.findViewById(R.id.item_title);
                viewhold.mTextView = (TextView) convertView.findViewById(R.id.description_item);
                viewhold.mRoot = (LinearLayout)convertView.findViewById(R.id.item_root);
			mRotateLayout =(RotateLayout) convertView.findViewById(R.id.manul_mode_rotatelayout);
                convertView.setTag(viewhold);
//                viewhold.mImageView = (ImageView) convertView.findViewById(R.id.photo_icon);
//                viewhold.mTextView = (TextView) convertView.findViewById(R.id.description_view);
//                viewhold.mItemRightline=(View)convertView.findViewById(R.id.item_rightline);
            } else {
                viewhold = (ViewHold) convertView.getTag();
            }
//            if((position+1)%GRID_COLUMNS==0){
//                viewhold.mItemRightline.setVisibility(View.GONE);
//            }
            CameraActivity.TraceLog(TAG,"getView mPictures size:"+mPictures.size()+" position:"+position);
            Picture picture = mPictures.get(position);
            mRotateLayout.setOrientation(mContext.getLastOrientation(), false);

            int row;
            switch(position+1) {
                case GC.MANU_ISO:
                    row = GC.ROW_SETTING_ISO; break;
                case GC.MANU_EXPOSURE:
                    row = GC.ROW_SETTING_EXPOSURE; break;
//                case GC.MANU_FLASH:
//                    row = GC.ROW_SETTING_FLASH; 
//                    break;
                case GC.MANU_WHITEBLANCE:
                    row = GC.ROW_SETTING_WHITE_BALANCE; break;
                case GC.MANU_FOCUS:
                    row = GC.ROW_SETTING_FOCUS_MODE; break;
                default:
                    row = GC.ROW_SETTING_ISO;
                    break;
            }
            if (mCurrentSettingRow == row) {
                viewhold.mRoot.setBackgroundResource(R.drawable.manual_gridview_circle_bg_pressed);
                viewhold.mTitleTextView.setText(GC.MANU_TITLE_NAMES[position+1]);
//                viewhold.mTitleTextView.setTextColor(Color.WHITE);
                viewhold.mTitleTextView.setTypeface(Typeface.DEFAULT_BOLD);
                viewhold.mTitleTextView.setTypeface(Typeface.DEFAULT_BOLD);
                //Modify by zhiming.yu for PR927414
                switch (mCurrentSettingRow) {
                case GC.ROW_SETTING_ISO:
                   if(setISOValue==PhotoModule.ISO_MINVALUE || setISOValue==0){
                        //viewhold.mTextView.setText("auto");
                        viewhold.mTextView.setText(mContext.getString(R.string.pref_camera_iso_s_f_entry_auto));
                    }else{
                        viewhold.mTextView.setText(ManualModeSeekBar.isoString[setISOValue]);
                    }
                    break;
                case GC.ROW_SETTING_EXPOSURE:
                   if(setExposureTime==PhotoModule.ISO_EXPOSURETIME_MINVALUE){
                        //viewhold.mTextView.setText("auto");
                        viewhold.mTextView.setText(mContext.getString(R.string.pref_camera_iso_s_f_entry_auto));
                    }else{
                        viewhold.mTextView.setText(ManualShutterSpeedAdapter.exposureTimeString[setExposureTime]);
                    }
                    break;
                case GC.ROW_SETTING_FOCUS_MODE:
                    if(setFoucsValue==PhotoModule.FOUCS_POS_RATIO_MINVALUE){
                        //viewhold.mTextView.setText("auto");
                        viewhold.mTextView.setText(mContext.getString(R.string.pref_camera_iso_s_f_entry_auto));
                    }else if(setFoucsValue==PhotoModule.FOUCS_POS_RATIO_MAXVALUE){
                        viewhold.mTextView.setText("∞");
                    }else{
                        viewhold.mTextView.setText(setFoucsValue+"");
                    }
                    break;
                case GC.ROW_SETTING_WHITE_BALANCE:
                    viewhold.mTextView.setText(setImageIcon(setWBValue));
                    break;
                default:
                    viewhold.mTextView.setText(mGC.getListPreference(row).getEntry());
                    break;
                }
//                viewhold.mTextView.setTextColor(Color.WHITE);
                viewhold.mTextView.setTypeface(Typeface.DEFAULT_BOLD);
//                viewhold.mImageView.setImageResource(picture.getHightLightImageId());
            } else {
                viewhold.mRoot.setBackgroundResource(R.drawable.manual_gridview_circle_bg);
                viewhold.mTitleTextView.setText(GC.MANU_TITLE_NAMES[position+1]);
                if ((position+1) == GC.MANU_ISO){
                    if(setISOValue==PhotoModule.ISO_MINVALUE || setISOValue == 0){
                        //viewhold.mTextView.setText("auto");
                        viewhold.mTextView.setText(mContext.getString(R.string.pref_camera_iso_s_f_entry_auto));
                    }else{
                        viewhold.mTextView.setText(ManualModeSeekBar.isoString[setISOValue]);
                    }
                }
                
                if ((position+1) == GC.MANU_EXPOSURE) {
                    if(setExposureTime==0){
                        //viewhold.mTextView.setText("auto");
                        viewhold.mTextView.setText(mContext.getString(R.string.pref_camera_iso_s_f_entry_auto));
                    }else{
                        viewhold.mTextView.setText(ManualShutterSpeedAdapter.exposureTimeString[setExposureTime]);
                    }
                }
                
                if ((position+1) == GC.MANU_WHITEBLANCE){
                    viewhold.mTextView.setText(setImageIcon(setWBValue));
                }
                
                if ((position+1) == GC.MANU_FOCUS) {
                    if(setFoucsValue==PhotoModule.FOUCS_POS_RATIO_MINVALUE){
                        //viewhold.mTextView.setText("auto");
                        viewhold.mTextView.setText(mContext.getString(R.string.pref_camera_iso_s_f_entry_auto));
                    }else if(setFoucsValue==PhotoModule.FOUCS_POS_RATIO_MAXVALUE){
                        viewhold.mTextView.setText("∞");
                    }else{
                        viewhold.mTextView.setText(setFoucsValue+"");
                    }
                }
            }
            //Modify by zhiming.yu for PR927414

            convertView.setEnabled(true);
            convertView.setClickable(false);
            convertView.setFocusable(false);
            convertView.setAlpha(1f);
            return convertView;
        }

        public ViewHold getHold() {
            return viewhold;
        }
        
        private CharSequence setImageIcon(int id){
        	
        	String html = "<img src='" + mWBImage.getResourceId(setWBValue, 0) + "'/>";
    		ImageGetter imgGetter = new ImageGetter() {
    			@Override
    			public Drawable getDrawable(String source) {
    				// TODO Auto-generated method stub
    				int id = Integer.parseInt(source);
    				Drawable d = mContext.getResources().getDrawable(id);
    				d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
    				return d;
    			}
    		};
    		CharSequence charSequence = Html.fromHtml(html, imgGetter, null);
        	return charSequence;
        }
    }



    class Picture {
        private String mTitle;
        private int mImageId;
        private int mImageHighLightId;
        private boolean selected = false;

        public Picture(String title, int imageid,int highlightimageid) {
            super();
            this.mTitle = title;
            this.mImageId = imageid;
            this.mImageHighLightId = highlightimageid;
        }

        public String getTitle(){
            return mTitle;
        }

        public void setTitle(String title) {
            this.mTitle = title;
        }

        public int getImageId() {
            return mImageId;
        }

        public void setImageId(int imageid) {
            this.mImageId = imageid;
        }

        public int getHightLightImageId() {
            return mImageHighLightId;
        }

        public void setHightLightImageId(int imageid) {
            this.mImageHighLightId = imageid;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }


    }

    class ViewHold {
        public ImageView mImageView;
        public TextView mTextView;
        public View mItemRightline;
        public LinearLayout mRoot;
        public TextView mTitleTextView;
    }

    @SuppressLint("NewApi")
	public void showManualMenu() {
    	initManualSeekbar();
    	initManualRecyclerView();
    	mManualModeCtlLayout.setTranslationX(0.0f);
    	RotateImageView view=(RotateImageView)(mContext.getCameraViewManager().getTopViewManager().view).findViewById(R.id.onscreen_manual_picker);
        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;
      float radius = Math.max(mManualModeCtlLayout.getWidth(), mManualModeCtlLayout.getHeight()) * 1.0f;
        Animator reveal=ViewAnimationUtils.createCircularReveal(mManualModeCtlLayout, cx, cy, 0, radius);
        reveal.setDuration(500);
        reveal.setInterpolator(new AccelerateDecelerateInterpolator());
        reveal.start();
    }
    
    
    @SuppressLint("NewApi")
	public void hideManualMenu(final View onscreenView) {
    	RotateImageView view=(RotateImageView)(mContext.getCameraViewManager().getTopViewManager().view).findViewById(R.id.onscreen_manual_picker);
        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;
             float radius = Math.max(mManualModeCtlLayout.getWidth(), mManualModeCtlLayout.getHeight()) * 1.0f;
             Animator reveal = ViewAnimationUtils.createCircularReveal(
            		mManualModeCtlLayout, cx, cy, radius, 0);
            reveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                	if(mGC.getCurrentMode() == GC.MODE_MANUAL){
                		mManualModeCtlLayout.setTranslationX(mContext.getResources().getDisplayMetrics().heightPixels+mContext.getResources().getDimension(R.dimen.navigationbar_height));
                		if(!mContext.getCameraViewManager().getTopViewManager().isInCountDown() && !mContext.getCameraViewManager().getTopViewManager().isInCapture()){
                			mContext.setViewState(GC.VIEW_STATE_MANUAL_MANU_HIDE);
                		}else{
                			mContext.getCameraViewManager().getTopViewManager().mShowingManualModel=true;
                		}
                		
                	}
                }
            });
            reveal.setDuration(500);
            reveal.setInterpolator(new AccelerateDecelerateInterpolator());
            if(!reveal.isRunning()){
            	reveal.start();
            }
            
             
    }

    public void resetISOETimeFocus(){
    	//update data
    	setISOValue=PhotoModule.ISO_MINVALUE;
    	setExposureTime=PhotoModule.ISO_EXPOSURETIME_MINVALUE;
    	setFoucsValue=PhotoModule.FOUCS_POS_RATIO_MINVALUE;
    	setWBValue = PhotoModule.WB_DEFAULT_AUTO;
    	mCurrentManualMode = GC.MODE_MANUAL_ISO;
    	mCurrentSettingRow = GC.ROW_SETTING_ISO;
    	
//    	mContext.getGC().getListPreference(GC.ROW_SETTING_WHITE_BALANCE).setValue("auto");
    	
//    	mContext.getGC().getListPreference(mCurrentSettingRow).setValueIndex(mContext.getGC().getListPreference(mCurrentSettingRow).findIndexOfValue("auto"));
    	//update UI
    	if(mWhiteBlanceAdapter!=null){
    		mWhiteBlanceAdapter.resetUI();
    	}
    	
    	if(mManualBar != null){
    		mManualBar.resetUI();
    	}
    	
    	mManualModeCallBackListener.resetManualModeParamters();
    }
  private void initWBTypedArray(){
    	mWBLabel =mContext.getResources().obtainTypedArray(R.array.pref_camera_whitebalance_labels);
    	mWBEntris = mContext.getResources().obtainTypedArray(R.array.pref_camera_whitebalance_entryvalues);
    	mWBImage =mContext.getResources().obtainTypedArray(R.array.whitebalance_icons_des);
    	//set default iso
    }
  private void  initManualSeekbar(){
      switch(mCurrentManualMode){
      case GC.MODE_MANUAL_ISO:
      	mManualBar.initData(setISOValue,0, 0,GC.MODE_MANUAL_ISO);
			break;
//      case GC.MODE_MANUAL_EXPOSURE:
//      	mManualBar.initData(setExposureTime,0,0,GC.MODE_MANUAL_EXPOSURE);
//      	break;
      case GC.MODE_MANUAL_FOCUS_VALUE:
      	mManualBar.initData(setFoucsValue,PhotoModule.FOUCS_POS_RATIO_MINVALUE, PhotoModule.FOUCS_POS_RATIO_MAXVALUE,GC.MODE_MANUAL_FOCUS_VALUE);
		default:
			break;
      }
  }
  
  private void initManualRecyclerView(){
	  switch(mCurrentManualMode){
      case GC.MODE_MANUAL_EXPOSURE:
    	  mManualShutterSpeedAdapter = new ManualShutterSpeedAdapter(mContext);
          mManualShutterSpeedAdapter.setCameraManualModeManager(this,setExposureTime);
          mHorizontalListView.setAdapter(mManualShutterSpeedAdapter);
          mHorizontalListView.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mHorizontalListView.scrollTo(setExposureTime);
			}
		});
          mHorizontalListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				mManualShutterSpeedAdapter.setSelectionId(position);
				mManualShutterSpeedAdapter.notifyDataSetChanged();
				saveManualProgressBarValues(position+"");
			}
          
          });
      	break;
      case GC.MODE_MANUAL_WHITE_BALANCE:
    	  mWhiteBlanceAdapter=new ManualModeWhiteblanceAdapter(mContext);
          mWhiteBlanceAdapter.setCameraManualModeManager(this,setWBValue);
          mHorizontalListView.setAdapter(mWhiteBlanceAdapter);
          mHorizontalListView.setOnItemClickListener(new OnItemClickListener() {

  			@Override
  			public void onItemClick(AdapterView<?> parent, View view,
  					int position, long id) {
  				// TODO Auto-generated method stub
  				mWhiteBlanceAdapter.setSelectionId(position);
              mWhiteBlanceAdapter.notifyDataSetChanged();
              saveManualProgressBarValues(position+"");
  			}
            
            });
          break;
	default:
	break;
      } 
  }
  
  public void resumeSettingsToParamers(){
  		if(!ManualModeSeekBar.isoString[setISOValue].equals(ManualModeSeekBar.isoString[0])){
	  		mCurrentManualMode = GC.MODE_MANUAL_ISO;
	  		saveManualProgressBarValues(setISOValue+"");
		}
		if(!ManualShutterSpeedAdapter.exposureTimeString[setExposureTime].equals(ManualShutterSpeedAdapter.exposureTimeString[0])){
			mCurrentManualMode = GC.MODE_MANUAL_EXPOSURE;
			saveManualProgressBarValues(setExposureTime+"");
		}
		if(setFoucsValue !=PhotoModule.FOUCS_POS_RATIO_MINVALUE){
			mCurrentManualMode = GC.MODE_MANUAL_FOCUS_VALUE;
			saveManualProgressBarValues(setFoucsValue+"");
		}
		if(setWBValue != PhotoModule.WB_DEFAULT_AUTO){
			mCurrentManualMode = GC.MODE_MANUAL_WHITE_BALANCE;
			saveManualProgressBarValues(setWBValue+"");
		}
		switch (mOldManualMode) {
		case GC.MODE_MANUAL_ISO:
			mCurrentManualMode = GC.MODE_MANUAL_ISO;
			mCurrentSettingRow = GC.ROW_SETTING_ISO;
			break;
		case GC.MODE_MANUAL_EXPOSURE:
			mCurrentManualMode = GC.MODE_MANUAL_EXPOSURE;
			mCurrentSettingRow = GC.ROW_SETTING_EXPOSURE;
			break;
		case GC.MODE_MANUAL_FOCUS_VALUE:
			mCurrentManualMode = GC.MODE_MANUAL_FOCUS_VALUE;
			mCurrentSettingRow = GC.ROW_SETTING_FOCUS_MODE;
			break;
		case GC.MODE_MANUAL_WHITE_BALANCE:
			mCurrentManualMode = GC.MODE_MANUAL_WHITE_BALANCE;
			mCurrentSettingRow = GC.ROW_SETTING_WHITE_BALANCE;
			break;

		default:
			mCurrentManualMode = GC.MODE_MANUAL_ISO;
			mCurrentSettingRow = GC.ROW_SETTING_ISO;
			break;
		}
		
  }
  private void changeUI(boolean isShowSeekbar){
	  if(isShowSeekbar){
		  mManualBar.setVisibility(View.VISIBLE);
		  mRecyclerParent.setVisibility(View.GONE);
	  }else{
		  mManualBar.setVisibility(View.GONE);
		  mRecyclerParent.setVisibility(View.VISIBLE);
	  }
  }
  
  public boolean isAutoFocus(){
	  if(setFoucsValue == PhotoModule.FOUCS_POS_RATIO_MINVALUE){
		  return true;
	  }else{
		  return false;
	  }
  }
  
  @Override
public void resetToDefault() {
	// TODO Auto-generated method stub
	  resetISOETimeFocus();
	super.resetToDefault();
}
  
  public boolean isDoingManualMode(){
		if(!ManualModeSeekBar.isoString[setISOValue].equals(ManualModeSeekBar.isoString[0]) || !ManualShutterSpeedAdapter.exposureTimeString[setExposureTime].equals(ManualShutterSpeedAdapter.exposureTimeString[0]) || setFoucsValue !=PhotoModule.FOUCS_POS_RATIO_MINVALUE || setWBValue != PhotoModule.WB_DEFAULT_AUTO){
			return true;
		}else{
			return false;
		}
		
	}
}

