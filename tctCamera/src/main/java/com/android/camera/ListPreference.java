/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.camera;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;

import com.android.camera.config.PersoConfig;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.UsageStatistics;
import com.tct.camera.R;
import com.android.camera.custom.CustomFields;
import com.android.camera.custom.CustomUtil;

/**
 * A type of <code>CameraPreference</code> whose number of possible values
 * is limited.
 */
public class ListPreference extends CameraPreference {
    private static final String TAG = "YUHUAN_ListPreference";
    public static final int UNKNOWN = -1;
    public static final boolean LOG = true;
    private final String mKey;
    private String mValue;
    private final CharSequence[] mDefaultValues;

    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private CharSequence[] mLabels;
    private boolean mLoaded = false;
    //[FEATURE]-ADD-BEGIN by TCTNB(Mingchun.Li), 2013/10/11, FR-473251,Camera ergo
    private String mOverrideValue;
    private boolean mEnabled = true;
    private CharSequence[] mOriginalSupportedEntries;
    private CharSequence[] mOriginalSupportedEntryValues;
    private CharSequence[] mOriginalEntries;
    private CharSequence[] mOriginalEntryValues;
    private boolean mClickable = true;
    //[FEATURE]-ADD-END by TCTNB(Mingchun.Li)
    private final static String PROPERTY_VIDEO_EIS_ON = "def.tct.video.eis.on";

    public ListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.ListPreference, 0, 0);

        mKey = CameraUtil.checkNotNull(
                a.getString(R.styleable.ListPreference_key));

        // We allow the defaultValue attribute to be a string or an array of
        // strings. The reason we need multiple default values is that some
        // of them may be unsupported on a specific platform (for example,
        // continuous auto-focus). In that case the first supported value
        // in the array will be used.
        int attrDefaultValue = R.styleable.ListPreference_defaultValue;
        TypedValue tv = a.peekValue(attrDefaultValue);
        if (tv != null && tv.type == TypedValue.TYPE_REFERENCE) {
            mDefaultValues = a.getTextArray(attrDefaultValue);
        } else {
            mDefaultValues = new CharSequence[1];
            /*[FEATURE]-Set the perso of shutter sound added by lijuan.zhang ,20141103 begin*/
            if(mKey.equals(CameraSettings.KEY_SOUND)) {
                mDefaultValues[0] = CustomUtil.getInstance().getBoolean(CustomFields.DEF_TCTCAMERA_SHUTTER_SOUND_ON, true) ? "on" : "off";
            }else if (mKey.equals(CameraSettings.KEY_ANTIBANDING)) {
                mDefaultValues[0] = CustomUtil.getInstance().getString(CustomFields.DEF_ANTIBAND_DEFAULT, "auto");
            }else {
            	mDefaultValues[0] = a.getString(attrDefaultValue);
            }
            /*[FEATURE]-Set the perso of shutter sound added by lijuan.zhang ,20141103 begin*/
            if (mKey.equals(CameraSettings.KEY_ZSL)){
                mDefaultValues[0] = "off";
            }
            if(mKey.equals(CameraSettings.KEY_PICTURE_SIZE_FLAG)){
                if(context instanceof CameraActivity){
                    if(((CameraActivity) context).getCameraId() == GC.CAMERAID_FRONT){
                        mDefaultValues[0] = "" + context.getString(R.string.pref_camera_picturesize_flag_default_front);
                    }
                }
                Log.i(TAG, "mumo defaultvalues= " + mDefaultValues[0]);
            }
            // modify by minghui.hua for PR951998
            if (mKey.equals(CameraSettings.KEY_CAMERA_SAVEPATH)){
                if(SDCard.instance().isDefaultStorage()){
                    mDefaultValues[0] = "1";//SD Card
                }else{
                    mDefaultValues[0] = "0";//Phone
                }
            }
        }

        setEntries(a.getTextArray(R.styleable.ListPreference_entries));
        setEntryValues(a.getTextArray(
                R.styleable.ListPreference_entryValues));
        setLabels(a.getTextArray(
                R.styleable.ListPreference_labelList));
        a.recycle();
        //[FEATURE]-ADD-BEGIN by TCTNB(Mingchun.Li), 2013/10/11, FR-473251,Camera ergo
        mOriginalEntryValues = mEntryValues;
        mOriginalEntries = mEntries;
        //[FEATURE]-ADD-END by TCTNB(Mingchun.Li)
    }

    public String getKey() {
        return mKey;
    }

    public CharSequence[] getEntries() {
        return mEntries;
    }

    public CharSequence[] getEntryValues() {
        return mEntryValues;
    }

    public synchronized String findStringOfValue(String value) {
        for (int i = 0, n = mEntryValues.length; i < n; ++i) {
            if (CameraUtil.equals(mEntryValues[i], value)) {
                return mEntries[i].toString();
            }
    }

        Log.w(TAG, "findStringOfValue(" + value + ") not find!!");
        return null;
    }

    public CharSequence[] getLabels() {
        return mLabels;
    }

    public void setEntries(CharSequence entries[]) {
    	Log.d(TAG,"###YUHUAN###setEntries into ");
        mEntries = entries == null ? new CharSequence[0] : entries;
    }

    public void setEntryValues(CharSequence values[]) {
    	Log.d(TAG,"###YUHUAN###setEntryValues into ");
        mEntryValues = values == null ? new CharSequence[0] : values;
    }

    public void setLabels(CharSequence labels[]) {
        mLabels = labels == null ? new CharSequence[0] : labels;
    }

    public String getValue() {
        if (!mLoaded) {
            mValue = getSharedPreferences().getString(mKey,
                    findSupportedDefaultValue());
            mLoaded = true;
        }
        return mValue;
    }

    //feiqiang.cheng add
    public void setDefaultValue(String def){
    	Log.d(TAG,"###YUHUAN###setDefaultValue#def=" + def);
        if (mDefaultValues != null && mDefaultValues.length > 0 ) {
            mDefaultValues[0] = def;
        }
    }

    //[FEATURE]-ADD-BEGIN by TCTNB(Mingchun.Li), 2013/10/11, FR-473251,Camera ergo
    public String getDefaultValue() {
        if (mDefaultValues != null && mDefaultValues.length > 0 && mDefaultValues[0] != null) {
            return String.valueOf(mDefaultValues[0]);
        }
        return null;
    }
    //[FEATURE]-ADD-END by TCTNB(Mingchun.Li)
    // Find the first value in mDefaultValues which is supported.
    private String findSupportedDefaultValue() {
        for (int i = 0; i < mDefaultValues.length; i++) {
            for (int j = 0; j < mEntryValues.length; j++) {
                // Note that mDefaultValues[i] may be null (if unspecified
                // in the xml file).
                if (mEntryValues[j].equals(mDefaultValues[i])) {
                    return mDefaultValues[i].toString();
                }
            }
        }
        return null;
    }

    public void setValue(String value) {
    	Log.d(TAG,"###YUHUAN###setValue#value=" + value);
        if (findIndexOfValue(value) < 0) {
            value = findSupportedDefaultValue();
        }
        mValue = value;
        Log.d(TAG,"###YUHUAN###setValue#mValue=" + mValue);
        persistStringValue(value);
    }

    public void setValueIndex(int index) {
        setValue(mEntryValues[index].toString());
    }

    public int findIndexOfValue(String value) {
    	Log.d(TAG,"###YUHUAN###findIndexOfValue#mEntryValues.length=" + mEntryValues.length);
        for (int i = 0, n = mEntryValues.length; i < n; ++i) {
        	Log.d(TAG,"###YUHUAN###findIndexOfValue#mEntryValues[" + i + "]="+ mEntryValues[i]);
            if (CameraUtil.equals(mEntryValues[i], value)) return i;
        }
        return -1;
    }

    public int getCurrentIndex() {
        return findIndexOfValue(getValue());
    }

    public String getEntry() {
        int index = findIndexOfValue(getValue());
        // Avoid the crash if fail to find value.
        if (index == -1) {
            Log.e(TAG, "Fail to find value = " + getValue());
            index = 0;
        }
        return mEntries[index].toString();
    }

    public String getLabel() {
        return mLabels[findIndexOfValue(getValue())].toString();
    }

    protected void persistStringValue(String value) {
    	Log.d(TAG,"###YUHUAN###persistStringValue#value=" + value);
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(mKey, value);
        editor.apply();
        UsageStatistics.onEvent("CameraSettingsChange", value, mKey);
    }

    @Override
    public void reloadValue() {
        this.mLoaded = false;
    }

    public void filterUnsupported(List<String> supported) {
        ArrayList<CharSequence> entries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> entryValues = new ArrayList<CharSequence>();
        for (int i = 0, len = mOriginalEntryValues.length; i < len; i++) {
            if (supported.indexOf(mOriginalEntryValues[i].toString()) >= 0) {
                entries.add(mOriginalEntries[i]);
                entryValues.add(mOriginalEntryValues[i]);
            }
        }
        int size = entries.size();
        mEntries = entries.toArray(new CharSequence[size]);
        mEntryValues = entryValues.toArray(new CharSequence[size]);
        /// M: here remember all supported values
        mOriginalSupportedEntries = mEntries;
        mOriginalSupportedEntryValues = mEntryValues;
    }

    public synchronized void filterDisabled(List<String> supported) {
        if (LOG) {
            Log.v(TAG, "filterDisabled(" + supported + ")");
        }
        ArrayList<CharSequence> entries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> entryValues = new ArrayList<CharSequence>();
        for (int i = 0, len = mOriginalSupportedEntryValues.length; i < len; i++) {
            if (supported.indexOf(mOriginalSupportedEntryValues[i].toString()) >= 0) {
                entries.add(mOriginalSupportedEntries[i]);
                entryValues.add(mOriginalSupportedEntryValues[i]);
            }
        }
        int size = entries.size();
        mEntries = entries.toArray(new CharSequence[size]);
        mEntryValues = entryValues.toArray(new CharSequence[size]);
    }
    //[FEATURE]-ADD-END by TCTNB(Mingchun.Li)
    public void filterDuplicated() {
        ArrayList<CharSequence> entries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> entryValues = new ArrayList<CharSequence>();
        for (int i = 0, len = mEntryValues.length; i < len; i++) {
            if (!entries.contains(mEntries[i])) {
                entries.add(mEntries[i]);
                entryValues.add(mEntryValues[i]);
            }
        }
        int size = entries.size();
        mEntries = entries.toArray(new CharSequence[size]);
        mEntryValues = entryValues.toArray(new CharSequence[size]);
    }
    //[FEATURE]-ADD-BEGIN by TCTNB(Mingchun.Li), 2013/10/11, FR-473251,Camera ergo
    public synchronized void restoreSupported() {
        if (LOG) {
            Log.v(TAG, "restoreSupported() mOriginalSupportedEntries=" + mOriginalSupportedEntries);
        }
        if (mOriginalSupportedEntries != null) {
            mEntries = mOriginalSupportedEntries;
        }
        if (mOriginalSupportedEntryValues != null) {
            mEntryValues = mOriginalSupportedEntryValues;
        }
    }
    //[FEATURE]-ADD-END by TCTNB(Mingchun.Li)

    public void print() {
        Log.v(TAG, "Preference key=" + getKey() + ". value=" + getValue());
        for (int i = 0; i < mEntryValues.length; i++) {
            Log.v(TAG, "entryValues[" + i + "]=" + mEntryValues[i]);
        }
    }

    //[FEATURE]-ADD-BEGIN by TCTNB(Mingchun.Li), 2013/10/11, FR-473251,Camera ergo
    public synchronized void setOverrideValue(String override, boolean restoreSupported) {
        if (LOG) {
            Log.v(TAG, "setOverrideValue(" + override + ", " + restoreSupported + ") " + this);
        }
        mOverrideValue = override;
        if (override == null) { //clear
            mEnabled = true;
            if (restoreSupported) {
                restoreSupported();
            }
        } else if (SettingUtils.isBuiltList(override)) {
            //mEnabled = true; //Do not change enable state.
            mOverrideValue = SettingUtils.getDefaultValue(override);
            filterDisabled(SettingUtils.getEnabledList(override));
        } else if (SettingUtils.isDisableValue(override)) { //disable
            mEnabled = false;
            mOverrideValue = null;
        } else { //reset
            mEnabled = false;
            //for special case, override value may be not in list.
            //for example: HDR not in user list, but can be set by user.
            if (mOverrideValue != null && findIndexOfValue(mOverrideValue) == -1) {
                mOverrideValue = findSupportedDefaultValue();
                Log.w(TAG, "setOverrideValue(" + override + ") not in list! mOverrideValue=" + mOverrideValue);
            }
        }
        mLoaded = false;
    }

    public void setOverrideValue(String override) {
        setOverrideValue(override, true);
    }
    public String getOverrideValue() {
        return mOverrideValue;
    }

    public int getIconId(int index) {
        return UNKNOWN;
    }
    public boolean isEnabled() {
        return mEnabled;
    }
    public void setEnabled(boolean enabled) {
        if (LOG) {
            Log.v(TAG, "setEnabled(" + enabled + ")");
        }
        mEnabled = enabled;
    }

    public CharSequence[] getOriginalEntryValues() {
        return mOriginalEntryValues;
    }

    public CharSequence[] getOriginalEntries() {
        return mOriginalEntries;
    }

    public CharSequence[] getOriginalSupportedEntryValues() {
        return mOriginalSupportedEntryValues;
    }

    public CharSequence[] getOriginalSupportedEntries() {
        return mOriginalSupportedEntries;
    }

    public void setClickable(boolean clickable) {
        mClickable = clickable;
    }

    public boolean isClickable() {
        return mClickable;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ListPreference(mKey=")
        .append(mKey)
        .append(", mTitle=")
        .append(getTitle())
        .append(", mOverride=")
        .append(mOverrideValue)
        .append(", mEnable=")
        .append(mEnabled)
        .append(", mValue=")
        .append(mValue)
        .append(", mClickable=")
        .append(mClickable)
        .append(")")
        .toString();
    }
    //[FEATURE]-ADD-END by TCTNB(Mingchun.Li)
}
