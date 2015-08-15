package com.android.camera.effects.camera;

import java.util.List;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Handler;
import android.os.Message;

import com.android.camera.effects.EffectsUtils;
import com.android.camera.effects.EffectsUtils.CameraDevMessage;
import com.android.camera.effects.LiveFilterInstanceHolder.ILiveFilterTask;
import com.android.camera.effects.LiveFilterInstanceHolder;
import com.android.camera.effects.camera.Components.I3ACallback;
import com.android.camera.effects.camera.Components.I3AControl;
import com.android.camera.effects.camera.Components.IPlatformCameraHolder;
import com.android.camera.effects.camera.Components.IPreviewDataProducer;
import com.android.camera.effects.camera.Components.IStillImageDataHandler;
import com.android.camera.effects.camera.Components.IStillImageDataProducer;

public final class LiveFilterCameraDevice
        implements IPreviewDataProducer, IStillImageDataProducer, I3AControl,
        FaceDetectionListener, AutoFocusCallback {
    /** Start continuous auto focus.*/
    public static final int CAF_START = 0;

    /** Continuous auto focus success.*/
    public static final int CAF_SUCCESS = 1;

    /** Continuous auto focus failed.*/
    public static final int CAF_FAIL = 2;

    private static final String TAG = "LiveFilterCameraDevice";

    /** Mark whether the camera is waiting for SurfaceTexture.*/
    private boolean mWaitForPreviewTexture = false;

    /** The 3A result handler.*/
    private I3ACallback m3ACallback;

    /** The SurfaceTexture of preview frame.*/
    private SurfaceTexture mPreviewTexture;

    private CameraEventHandler mEventHandler;

    /** The platform camera device holder.*/
    private IPlatformCameraHolder mCameraHolder = null;

    /** Handler to handle still image data.*/
    private IStillImageDataHandler mStillImageDataHandler = null;

    private final ILiveFilterTask mStartPreviewTask = new ILiveFilterTask() {
        @Override
        public void run() {mCameraHolder.startPreview(mPreviewTexture);}
    };

    private final static class CameraEventHandler extends Handler {
        private IPlatformCameraHolder mCameraHolder;
        private boolean mIsDiscarded = false;
        public boolean m3ALocked = false;

        public void discard() {
            mCameraHolder = null;
            mIsDiscarded = true;
        }

        public CameraEventHandler(IPlatformCameraHolder holder) {mCameraHolder = holder;}

        @Override
        public void handleMessage(Message msg) {
            if (mIsDiscarded) return;
            switch (msg.what) {
            case CameraDevMessage.MSG_START_CAF:
                if (mCameraHolder.isCAFMode()) return;
                mCameraHolder.startCAF();
                break;
            case CameraDevMessage.MSG_UNLOCK_3A_START_CAF:
                if (m3ALocked) {
                    mCameraHolder.unlock3AAndStartCAF();
                    m3ALocked = false;
                } else {
                    sendEmptyMessageDelayed(CameraDevMessage.MSG_START_CAF, 1000);
                }
                break;
            }
        }
    }

    private final PictureCallback mPictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mEventHandler.sendEmptyMessage(CameraDevMessage.MSG_UNLOCK_3A_START_CAF);
            mCameraHolder.startPreview(mPreviewTexture);
            m3ACallback.onSnapDone();
            int effect = LiveFilterInstanceHolder.getIntance()
                    .getEffectsRender().getCurrentEffect();
            mStillImageDataHandler.onJpegData(data, effect);
        }
    };

    private final ShutterCallback mShutterCallback = new ShutterCallback() {
        @Override
        public void onShutter() {}
    };

    public LiveFilterCameraDevice(IPlatformCameraHolder holder) {
        mCameraHolder = holder;
    }

    public void waitForPreviewTextureAvailable() {
        EffectsUtils.LogV(TAG, "waitForPreviewTexture");
        if (mPreviewTexture != null) {
            mCameraHolder.startPreview(mPreviewTexture);
        } else {
            mWaitForPreviewTexture = true;
        }
    }

    public void prepareLooper() {
        if (mEventHandler != null) mEventHandler.discard();
        mEventHandler = new CameraEventHandler(mCameraHolder);
    }

    public void onDeviceChanged() {
        m3ACallback.onDeviceChanged(mCameraHolder);
    }

    public void onCAFStateChanged(int state) {
        m3ACallback.onCAFState(state);
    }

    @Override
    public void setStillImageDataHander(IStillImageDataHandler handler) {
        mStillImageDataHandler = handler;
    }

    // IPreviewDataProducer functions
    @Override
    public void onPreviewTextureAvalible(SurfaceTexture texture) {
        mPreviewTexture = texture;
        if (mWaitForPreviewTexture) {
            LiveFilterInstanceHolder.getIntance()
                    .getSharedTaskThread().addTask(mStartPreviewTask);
        }
    }

    @Override
    public void onLosePreviewTexture() {
        mPreviewTexture = null;
        mCameraHolder.stopPreview();
    }

    // I3AControl functions
    @Override
    public void set3ACallback(I3ACallback callback) {m3ACallback = callback;}

    @Override
    public void recalculate3A(List<Area> aeawb, List<Area> af) {
        mEventHandler.removeMessages(CameraDevMessage.MSG_START_CAF);
        mCameraHolder.recalculate3A(aeawb, af);
    }

    @Override
    public void cancelAF() {
        mEventHandler.removeMessages(CameraDevMessage.MSG_START_CAF);
        mCameraHolder.cancelAF();
    }

    @Override
    public void snap() {
        mEventHandler.removeMessages(CameraDevMessage.MSG_START_CAF);
        mCameraHolder.snap(LiveFilterInstanceHolder.getIntance().getOrientationObserver()
                .getCameraOrientation(mCameraHolder.getCameraInfo()),
                mPictureCallback, mShutterCallback);
    }

    @Override
    public void lock3A() {
        mEventHandler.m3ALocked = true;
        mEventHandler.removeMessages(CameraDevMessage.MSG_START_CAF);
        mCameraHolder.lock3A();
    }

    // AutoFocusCallback
    @Override
    public void onAutoFocus(boolean success, Camera useless) {
        if (mCameraHolder.isCAFMode()) return;
        mEventHandler.sendEmptyMessageDelayed(CameraDevMessage.MSG_START_CAF, 1000);
        m3ACallback.on3ADone(success);
    }

    // FaceDetectionListener
    @Override
    public void onFaceDetection(Face[] faces, Camera useless) {}

    @Override
    public void unlock3A() {
        mEventHandler.sendEmptyMessage(CameraDevMessage.MSG_UNLOCK_3A_START_CAF);
    }
}
