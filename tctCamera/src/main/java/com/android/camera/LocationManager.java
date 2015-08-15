/*
 * Copyright (C) 2011 The Android Open Source Project
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

import com.android.camera.manager.TopViewManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

/**
 * A class that handles everything about location.
 */
public class LocationManager {
    private static final String TAG = "LocationManager";

    private CameraActivity mContext;
    private Listener mListener;
    private android.location.LocationManager mLocationManager;
    private boolean mRecordLocation;

    private ComboPreferences mPreferences;
    private int mCameraId;
    private TopViewManager mTopViewManager;
    LocationListener [] mLocationListeners = new LocationListener[] {
            new LocationListener(android.location.LocationManager.NETWORK_PROVIDER),
            new LocationListener(android.location.LocationManager.GPS_PROVIDER)
    };

    public interface Listener {
        public void showGpsOnScreenIndicator(boolean hasSignal);
        public void hideGpsOnScreenIndicator();
   }
    //Add by zhimin.yu for PR952573 begin
    public LocationManager(CameraActivity context, Listener listener) {
        mContext = context;
        mListener = listener;
        ini();
    }
    private void ini(){
        mPreferences = new ComboPreferences(mContext);
        mCameraId = mContext.getCameraId();
        mPreferences.setLocalId(mContext, mCameraId);
        mTopViewManager = mContext.getCameraViewManager().getTopViewManager();
    }
    //Add by zhimin.yu for PR952573 end
    public Location getCurrentLocation() {
        if (!mRecordLocation) return null;

        // go in best to worst order
        for (int i = 0; i < mLocationListeners.length; i++) {
            Location l = mLocationListeners[i].current();
            if (l != null) return l;
        }
        CameraActivity.TraceLog(TAG, "No location received yet.");
        return null;
    }

    public void recordLocation(boolean recordLocation) {
        CameraActivity.TraceLog(TAG, "recordLocation : recordLocation = " + recordLocation);
        if (mRecordLocation != recordLocation) {
            mRecordLocation = recordLocation;
            if (recordLocation) {
                startReceivingLocationUpdates();
            } else {
                stopReceivingLocationUpdates();
            }
        }
    }

    @SuppressLint("InlinedApi")
    private void startReceivingLocationUpdates() {
        CameraActivity.TraceLog(TAG,"startReceivingLocationUpdates");
        if (mLocationManager == null) {
            mLocationManager = (android.location.LocationManager)
                    mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        if (mLocationManager != null) {
            //Add by zhimin.yu for PR967300 begin
            int mode = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            CameraActivity.TraceLog(TAG,"startReceivingLocationUpdates : mode = " + mode);
            switch (mode) {
            case android.provider.Settings.Secure.LOCATION_MODE_OFF:
                break;
            case android.provider.Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                requestLocationUpdatesGps();
                break;
            case android.provider.Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                requestLocationUpdatesNetWork();
                break;
            case android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                requestLocationUpdatesNetWork();
                requestLocationUpdatesGps();
                break;
            default:
                break;
        }
        }
    }

    private void requestLocationUpdatesNetWork(){
        try {
            mLocationManager.requestLocationUpdates(
                    android.location.LocationManager.NETWORK_PROVIDER,
                    1000,
                    0F,
                    mLocationListeners[0]);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "provider does not exist " + ex.getMessage());
        }
    }

    private void requestLocationUpdatesGps(){
        try {
            mLocationManager.requestLocationUpdates(
                    android.location.LocationManager.GPS_PROVIDER,
                    1000,
                    0F,
                    mLocationListeners[1]);
            if (mListener != null) mListener.showGpsOnScreenIndicator(false);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "provider does not exist " + ex.getMessage());
        }
    }

    private void stopReceivingLocationUpdates() {
        CameraActivity.TraceLog(TAG,"stopReceivingLocationUpdates");
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
        if (mListener != null) mListener.hideGpsOnScreenIndicator();
    }
    //Add by zhimin.yu for PR967300 end
    private class LocationListener
            implements android.location.LocationListener {
        Location mLastLocation;
        boolean mValid = false;
        String mProvider;

        public LocationListener(String provider) {
            mProvider = provider;
            mLastLocation = new Location(mProvider);
        }

        @Override
        public void onLocationChanged(Location newLocation) {
            CameraActivity.TraceLog(TAG, "LocationListener");
            if (newLocation.getLatitude() == 0.0
                    && newLocation.getLongitude() == 0.0) {
                // Hack to filter out 0.0,0.0 locations
                return;
            }
            // If GPS is available before start camera, we won't get status
            // update so update GPS indicator when we receive data.
            if (mListener != null && mRecordLocation &&
                    android.location.LocationManager.GPS_PROVIDER.equals(mProvider)) {
                mListener.showGpsOnScreenIndicator(true);
            }
            if (!mValid) {
                CameraActivity.TraceLog(TAG, "Got first location.");
            }
            mLastLocation.set(newLocation);
            mValid = true;
        }

        @Override
        public void onProviderEnabled(String provider) {
            CameraActivity.TraceLog(TAG, "onProviderEnabled : provider = " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            CameraActivity.TraceLog(TAG, "onProviderDisabled : provider = " + provider);
            //Add by zhimin.yu for PR952573
            setRecordLocationPreference(RecordLocationPreference.VALUE_OFF);
            setGpsTagPreference(RecordLocationPreference.VALUE_OFF);
            mContext.updateisLocationOpend(false);
            mTopViewManager.refreshMenu();
            mValid = false;
        }
        //Add by zhimin.yu for PR952573 begin
        private void setRecordLocationPreference(String value){
            Editor editor = mPreferences.edit();
            editor.putString(CameraSettings.KEY_RECORD_LOCATION, value);
            editor.apply();
        }
        private void setGpsTagPreference(String value){
            Editor editor = mPreferences.edit();
            editor.putString(CameraSettings.KEY_GPS_TAG, value);
            editor.apply();
        }
        //Add by zhimin.yu for PR952573 end
        @Override
        public void onStatusChanged(
                String provider, int status, Bundle extras) {
            CameraActivity.TraceLog(TAG, "onStatusChanged : provider = " + provider);
            switch(status) {
                case LocationProvider.OUT_OF_SERVICE:
                case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                    mValid = false;
                    if (mListener != null && mRecordLocation &&
                            android.location.LocationManager.GPS_PROVIDER.equals(provider)) {
                        mListener.showGpsOnScreenIndicator(false);
                    }
                    break;
                }
            }
        }

        public Location current() {
            return mValid ? mLastLocation : null;
        }
    }
}
