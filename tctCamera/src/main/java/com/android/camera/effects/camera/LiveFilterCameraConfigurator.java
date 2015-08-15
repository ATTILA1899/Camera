package com.android.camera.effects.camera;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;

import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
//import android.hardware.Camera.AFDataCallback;
import android.hardware.Camera.Area;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;

import com.android.camera.effects.EffectsUtils;
import com.android.camera.effects.LiveFilterInstanceHolder;
import com.android.camera.effects.LiveFilterInstanceHolder.ILiveFilterTask;
import com.android.camera.effects.camera.Components.*;
import com.android.camera.effects.gl.GLEffectsRender;
//[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-11,PR759341 Begin
import com.android.camera.util.CameraUtil;
import android.content.Context;
import com.tct.camera.R;
//[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-11,PR759341 End

/** Class of store and set parameters to platform camera hardware.
 * It is also an {@link IPlatformCameraHolder}.*/
public final class LiveFilterCameraConfigurator implements IPlatformCameraHolder {
    private static final String TAG = "LiveFilterCameraConfigurator";
    private int mCameraId = 0;
    private Point mScreenSize;
    private Camera mPlatformCamera;
    private boolean mCameraOpening = false;
    private boolean mCameraIdChanged = false;
    private boolean mShutterSoundEnabled = false;
    private final CameraInfo mCameraInfo = new CameraInfo();
    private Parameters mParameters;
    private ICameraSwitchObserver mCameraSwitchObserver;
    private LiveFilterCameraDevice mCameraDevice;
    //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-11,PR759341 Begin
    private Context mContext;
    //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-11,PR759341 End

    /** Interface definition for observing camera switching.*/
    public interface ICameraSwitchObserver {
        /** Called when try to start switch camera.
         * @return Return true if this operation is accepted, otherwise false.*/
        public boolean onStartSwitch();

        /** Called when camera switching is done.*/
        public void onSwitchDone();
    }

    /*private final AFDataCallback mAFDataCallback = new AFDataCallback() {
        @Override
        public void onAFData(byte[] data, Camera useless) {
            IntBuffer buff = ByteBuffer.wrap(data)
                    .order(ByteOrder.nativeOrder()).asIntBuffer();
            mCameraDevice.onCAFStateChanged(buff.get(5));
        }
    };*/

    private ILiveFilterTask mOpenCameraSessionTask = new ILiveFilterTask() {
        @Override
        public void run() {
            Camera.getCameraInfo(mCameraId, mCameraInfo);
            mPlatformCamera = Camera.open(mCameraId);
            mParameters = mPlatformCamera.getParameters();
            setupCamera();
            mCameraDevice.onDeviceChanged();
            mCameraDevice.waitForPreviewTextureAvailable();
            mCameraOpening = false;
            mCameraSwitchObserver.onSwitchDone();
            //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-19,PR753337 Begin
            LiveFilterInstanceHolder.getIntance().getStillCaptureController().setAfSupport(CameraUtil.isSupported(Parameters.FOCUS_MODE_AUTO,
                mParameters.getSupportedFocusModes()));
            //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-19,PR753337 End
        }
    };

    //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-11,PR759341 Begin
    /*
    public LiveFilterCameraConfigurator() {
        mCameraDevice = new LiveFilterCameraDevice(this);
    }
    */
    public LiveFilterCameraConfigurator(Context context) {
        mCameraDevice = new LiveFilterCameraDevice(this);
        mContext = context;
    }
    //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-11,PR759341 End

    public void switchCamera() {
        if (mCameraOpening) return;
        if (!mCameraSwitchObserver.onStartSwitch()) return;
        mCameraIdChanged = !mCameraIdChanged;
        mCameraId = (mCameraId + 1) % 2;
        closeCameraSession();
        openCameraSession();
    }

    public void prepareLooper() {
        mCameraDevice.prepareLooper();
    }

    public void setCameraSwitchObserver(ICameraSwitchObserver observer) {
        mCameraSwitchObserver = observer;
    }

    public void setScreenSize(Point screenSize) {
        mScreenSize = screenSize;
    }

    @Override
    public void closeCameraSession() {
        LiveFilterInstanceHolder.getIntance().getSharedTaskThread()
                .removeTask(mOpenCameraSessionTask);
        while (mCameraOpening) {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                EffectsUtils.LogE(TAG, e.toString());
            }
        }
        mPlatformCamera.setAutoFocusMoveCallback(null);
        mPlatformCamera.release();
        mPlatformCamera = null;
    }

    @Override
    public void openCameraSession() {
        if (mCameraOpening) return;
        mCameraOpening = true;
        LiveFilterInstanceHolder.getIntance()
                .getSharedTaskThread().addTask(mOpenCameraSessionTask);
    }

    public void checkCameraSession() {
        if (mPlatformCamera == null) {
            openCameraSession();
        }
    }

    public IPreviewDataProducer getPreviewDataProducer() {
        return mCameraDevice;
    }

    public IStillImageDataProducer getPhotoDataProducer() {
        return mCameraDevice;
    }

    /** Get the {@link I3AControl} for platform camera device.
     * @return the {@link I3AControl} to control the platform camera device.*/
    public I3AControl get3AControl() {
        return mCameraDevice;
    }

    private void setupCamera() {
        float ratio = mScreenSize.y / (float)mScreenSize.x;
//[BUGFIX]-Mod-BEGIN by hongyuan-wang,08/06/2014,PR-750322
        Size preSize = EffectsUtils
                .getOptimalPreviewSize(mParameters.getSupportedPreviewSizes(), mScreenSize);
//[BUGFIX]-Mod-END by hongyuan-wang,08/06/2014,PR-750322
        mParameters.setPreviewSize(preSize.width, preSize.height);
        Size picSize = EffectsUtils
                .getOptimalSize(mParameters.getSupportedPictureSizes(), ratio);
        mParameters.setPictureSize(picSize.width, picSize.height);
        //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-19,PR753337 Begin
        /*
        mParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        */
        if(CameraUtil.isSupported(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, mParameters.getSupportedFocusModes())){
            mParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else {
            mParameters.setFocusMode(Parameters.FOCUS_MODE_INFINITY);
        }
        //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-19,PR753337 End
        //mPlatformCamera.setAFDataCallback(mAFDataCallback);
        mPlatformCamera.setFaceDetectionListener(mCameraDevice);

        //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-07-04,PR759341 Begin
        String antiBanding = mContext.getResources().getString(R.string.pref_camera_antibanding_default);
        if (CameraUtil.isSupported(antiBanding, mParameters.getSupportedAntibanding())) {
        	//[BUGFIX]-ADD-BEGIN by kechao.chen, PR-770369 	, 2014-8-21
        	if(CameraUtil.isSpecialCountryforAntiBanding(mContext)){
        		mParameters.setAntibanding(Parameters.ANTIBANDING_60HZ);
        	}else{
        		mParameters.setAntibanding(antiBanding);
        	}
        	//[BUGFIX]-ADD-BEGIN by kechao.chen, PR-770369 	, 2014-8-21
        }
        //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-07-04,PR759341 End

        mPlatformCamera.setParameters(mParameters);
    }

    /** Turn on/off the shutter sound.
     * @param enabled : true to turn on the shutter sound, otherwise false.*/
    public void setShutterSound(boolean enabled) {
        mShutterSoundEnabled = enabled;
    }

    /** The camera id to livefilter camera.
     * @param id : The specific camera id.*/
    public void setCameraId(int id) {
        mCameraId = id;
        mCameraIdChanged = false;
    }

    @Override
    public int getCameraId() {
        return mCameraId;
    }

    /** Check if camera id has been changed by livefilter camera.
     * @return True if camera id has been changed, Otherwise false.*/
    public boolean isCameraIdChanged() {
        return mCameraIdChanged;
    }

    @Override
    public Camera getPlatformCameraHW() {
        return mPlatformCamera;
    }

    @Override
    public void startPreview(SurfaceTexture previewTexture) {
        if (mPlatformCamera == null) return;
        try {
            mPlatformCamera.setPreviewTexture(previewTexture);
        } catch (IOException e) {
            EffectsUtils.LogE(TAG, e.toString());
        }
        mPlatformCamera.startPreview();
    }

    @Override
    public void stopPreview() {
        if (mPlatformCamera == null) return;
        mPlatformCamera.stopPreview();
    }

    @Override
    public void recalculate3A(List<Area> aeawb, List<Area> af) {
        if (mPlatformCamera == null) return;
        //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-19,PR753337 Begin
        /*
        mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
        mParameters.setMeteringAreas(aeawb);
        mParameters.setFocusAreas(af);
        mPlatformCamera.setParameters(mParameters);
        mPlatformCamera.autoFocus(mCameraDevice);
        */
        if(CameraUtil.isSupported(Parameters.FOCUS_MODE_AUTO, mParameters.getSupportedFocusModes())){
            mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
        } else {
            mParameters.setFocusMode(Parameters.FOCUS_MODE_INFINITY);
        }
        mParameters.setMeteringAreas(aeawb);
        if(CameraUtil.isFocusAreaSupported(mParameters)) {
            mParameters.setFocusAreas(af);
        }
        mPlatformCamera.setParameters(mParameters);
        if(CameraUtil.isSupported(Parameters.FOCUS_MODE_AUTO,
                mParameters.getSupportedFocusModes())) {
            mPlatformCamera.autoFocus(mCameraDevice);
        }
        //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-19,PR753337 End
    }

    @Override
    public void cancelAF() {
        if (mPlatformCamera == null) return;
        //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-19,PR753337 Begin
        /*
        mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
        mPlatformCamera.cancelAutoFocus();
        */
        if(CameraUtil.isSupported(Parameters.FOCUS_MODE_AUTO, mParameters.getSupportedFocusModes())){
            mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
            mPlatformCamera.cancelAutoFocus();
        } else {
            mParameters.setFocusMode(Parameters.FOCUS_MODE_INFINITY);
        }
        //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-19,PR753337 End
        //mPlatformCamera.setAFDataCallback(null);
        mPlatformCamera.setParameters(mParameters);
    }

    @Override
    public void startCAF() {
        if (mPlatformCamera == null) return;
        //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-19,PR753337 Begin
        /*
        mParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        */
        if(CameraUtil.isSupported(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, mParameters.getSupportedFocusModes())){
            mParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else {
            mParameters.setFocusMode(Parameters.FOCUS_MODE_INFINITY);
        }
        //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-19,PR753337 End
        //mPlatformCamera.setAFDataCallback(mAFDataCallback);
        mPlatformCamera.setParameters(mParameters);
    }

    @Override
    public boolean isCAFMode() {
        return mParameters.getFocusMode()
                .equals(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    @Override
    public void lock3A() {
        if (mPlatformCamera == null) return;
        mParameters.setAutoExposureLock(true);
        mParameters.setAutoWhiteBalanceLock(true);
        //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-19,PR753337 Begin
        /*
        mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
        */
        if(CameraUtil.isSupported(Parameters.FOCUS_MODE_AUTO, mParameters.getSupportedFocusModes())){
            mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
        } else {
            mParameters.setFocusMode(Parameters.FOCUS_MODE_INFINITY);
        }
        //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-19,PR753337 End
        //mPlatformCamera.setAFDataCallback(null);
        mPlatformCamera.setParameters(mParameters);
    }

    @Override
    public void unlock3AAndStartCAF() {
        if (mPlatformCamera == null) return;
        mParameters.setAutoExposureLock(false);
        mParameters.setAutoWhiteBalanceLock(false);
        startCAF();
    }

    @Override
    public void snap(int orientation, PictureCallback picture, ShutterCallback shutter) {
        mParameters.setRotation(orientation);
        mPlatformCamera.setParameters(mParameters);
        mPlatformCamera.takePicture(mShutterSoundEnabled ? shutter : null, null, picture);
    }

    @Override
    public CameraInfo getCameraInfo() {
        return mCameraInfo;
    }
}
