package com.android.camera.effects;

import com.android.camera.CameraActivity;
import com.android.camera.effects.EffectsUtils.ActivityStateObserver;
import com.android.camera.effects.EffectsUtils.OrientationObserver;
import com.android.camera.effects.EffectsUtils.TaskQueueThread;
import com.android.camera.effects.EffectsUtils.TaskQueueThread.ITaskHandler;
import com.android.camera.effects.camera.LiveFilterCameraConfigurator;
import com.android.camera.effects.camera.LiveFilterImageSaver;
import com.android.camera.effects.camera.LiveFilterStillCaptureController;
import com.android.camera.effects.camera.LiveFilterThumbnailContorller;
import com.android.camera.effects.filter.FilterSource;
import com.android.camera.effects.gl.GLEffectsRender;

import android.content.Context;
import com.android.camera.effects.LiveFilterCamera;

public final class LiveFilterInstanceHolder {
    private Context mContext;
    private TaskQueueThread<ILiveFilterTask> mTaskThread
            = new TaskQueueThread<ILiveFilterTask>();
    private GLEffectsRender mEffectsRender;
    private OrientationObserver mOrientationObserver;
    private LiveFilterImageSaver mImageSaver;
    private ActivityStateObserver mActivityStateObserver;
    private LiveFilterCameraConfigurator mCameraConf;
    private LiveFilterThumbnailContorller mThumbnailContorller;
    private LiveFilterStillCaptureController mCaptureController;

    private final ITaskHandler<ILiveFilterTask> mTaskHandler =
            new ITaskHandler<ILiveFilterTask>() {
        @Override
        public void handleTask(ILiveFilterTask task) {
            task.run();
        }
    };

    /** Interface definition for runnable task.*/
    public interface ILiveFilterTask {
        /** To run this task.*/
        public void run();
    }

    // Singleton mode
    private static LiveFilterInstanceHolder mInstance;

    private LiveFilterInstanceHolder(Context context) {
        mContext = context;
        mTaskThread.setTaskHandler(mTaskHandler);
        mTaskThread.start();
    }

    public static LiveFilterInstanceHolder createInstance(Context context) {
        if (mInstance != null)
            return mInstance;
        mInstance = new LiveFilterInstanceHolder(context);
        return mInstance;
    }

    public static LiveFilterInstanceHolder getIntance() {return mInstance;}

    public Context getContext() {return mContext;}

    public GLEffectsRender getEffectsRender() {return mEffectsRender;}

    public OrientationObserver getOrientationObserver() {return mOrientationObserver;}

    public LiveFilterImageSaver getImageSaver() {return mImageSaver;}

    public ActivityStateObserver getActivityStateObserver() {return mActivityStateObserver;}

    public LiveFilterCameraConfigurator getCameraConfigurator() {return mCameraConf;}

    public LiveFilterThumbnailContorller getThumbnailContorller(LiveFilterCamera filterContext) {mThumbnailContorller.setFilterContext(filterContext);  return mThumbnailContorller;}

    public LiveFilterStillCaptureController getStillCaptureController() {return mCaptureController;}

    public TaskQueueThread<ILiveFilterTask> getSharedTaskThread() {return mTaskThread;}

    public void prepare() {
        if (mCameraConf != null) return;
        //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-11,PR759341 Begin
        //mCameraConf = new LiveFilterCameraConfigurator();
        mCameraConf = new LiveFilterCameraConfigurator(mContext);
        //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-11,PR759341 End
        mImageSaver = new LiveFilterImageSaver(mContext);
        mEffectsRender = new GLEffectsRender();
        mCaptureController = new LiveFilterStillCaptureController();
        mOrientationObserver = new OrientationObserver(mContext);
        mThumbnailContorller = new LiveFilterThumbnailContorller(mContext);
        mActivityStateObserver = new ActivityStateObserver();
        mEffectsRender.prepare();
        FilterSource.createInstance(mContext).prepare();
    }
}
