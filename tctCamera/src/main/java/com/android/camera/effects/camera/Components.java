package com.android.camera.effects.camera;

import java.util.List;

import com.android.camera.Thumbnail;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;

public final class Components {
    /** Interface definition for a component can be paused and resume.*/
    public static interface PauseResumeAble {
        /** Called when we want to pause this component.*/
        public void onPause();

        /** Called when we want to resume this component.*/
        public void onResume();
    }

    /** Interface definition for photo thumbnail handler.*/
    public static interface ThumbnailHandler {
        /** Get the expected thumbnail size from the ThumbnailHandler.*/
        public int getExpectedThumbnailSize();

        /** Called when a new thumbnail is generated.*/
        public void onNewThumbnailArrived(Thumbnail thumb);
    }

    /** Interface definition for preview data producer.*/
    public static interface IPreviewDataProducer {
        /** Called when the SurfaceTexture is available for preview.
         * @param surfaceTexture : The available SurfaceTexture*/
        public void onPreviewTextureAvalible(SurfaceTexture texture);

        /** Called when the SurfaceTexture is disabled.*/
        public void onLosePreviewTexture();
    }

    /** Interface definition for still image data producer.*/
    public static interface IStillImageDataProducer {
        /** Set IStillImageDataHandler for this producer.
         * @param handler : The still image data handler
         * @see IStillImageDataHandler*/
        public void setStillImageDataHander(IStillImageDataHandler handler);
    }

    /** Interface definition for still image data handler.*/
    public static interface IStillImageDataHandler {
        /**Called when new JPEG data is produced by producer.
         * @param data : The JPEG data byte array.
         * @param effect : The effect that will be implemented.*/
        public void onJpegData(byte[] data, int effect);
    }

    /** Interface definition for 3A result handler.*/
    public static interface I3ACallback {
        /** Called when snapping is done.*/
        public void onSnapDone();

        /** Called when 3A calculation is done.
         * @param success : Whether 3A calculate is succeed.*/
        public void on3ADone(boolean success);

        /** Called when CAF state is changed.
         * @param state : CAF state of {@link LiveFilterCameraDevice#CAF_START},
         * {@link LiveFilterCameraDevice#CAF_FAIL}, {@link LiveFilterCameraDevice#CAF_SUCCESS}.*/
        public void onCAFState(int state);

        /** Called when 3A device is changed.
         * @param holder : The holder of new camera 3A device.*/
        public void onDeviceChanged(IPlatformCameraHolder holder);
    }

    /** Interface definition for AF, AE and AWB control.*/
    public static interface I3AControl {
        /** Calculate 3A with given AE and AF windows.
         * @param aeawb : AE metering areas, set null as default AE/AWB Area.
         * @param af : AF metering areas, set null as default AF Area.*/
        public void recalculate3A(List<Area> aeawb, List<Area> af);

        /** Force cancel auto focus and continuous auto focus.*/
        public void cancelAF();

        /** Lock 3A parameters.*/
        public void lock3A();

        /** Unlock 3A parameters.*/
        public void unlock3A();

        /** Set 3A callback for the 3A device.
         * @param callback : The {@link I3ACallback} to handle 3A result.*/
        public void set3ACallback(I3ACallback callback);

        /** Do snap*/
        public void snap();
    }

    /**Interface definition for a platform camera hardware holder.*/
    public static interface IPlatformCameraHolder {
        /** To stop preview.*/
        public void stopPreview();

        /** Get camera id.
         * @return The current camera id.*/
        public int getCameraId();

        /** To start preview.
         * @param priviewTexture : The {@link SurfaceTexture} for preview display. */
        public void startPreview(SurfaceTexture priviewTexture);

        /** To get platform camera hardware.
         * @return The Android camera devices.*/
        public Camera getPlatformCameraHW();

        /** To close the camera session.*/
        public void closeCameraSession();

        /** To open the camera session.*/
        public void openCameraSession();

        /** To recalculate 3A with given AE, AWB, AF metering windows.
         * @param aeawb : The AE and AWB metering windows.
         * @param af : The AF metering windows. */
        public void recalculate3A(List<Area> aeawb, List<Area> af);

        /** To lock 3A parameters.*/
        public void lock3A();

        /** To unlock 3A parameters and start CAF.*/
        public void unlock3AAndStartCAF();

        /** To cancel CAF and AF.*/
        public void cancelAF();

        /** To start CAF*/
        public void startCAF();

        /** To start snap.
         * @param orientation : The picture orientation.
         * @param picture : The PictureCallback.
         * @param shutter : The ShutterCallback. */
        public void snap(int orientation, PictureCallback picture, ShutterCallback shutter);

        /** To check if camera is under CAF mode.
         * @return true is camera is under CAF mode, otherwise false*/
        public boolean isCAFMode();

        /** Get camera info of current camera.
         * @param The camera info of current camera.*/
        public CameraInfo getCameraInfo();
    }
}
