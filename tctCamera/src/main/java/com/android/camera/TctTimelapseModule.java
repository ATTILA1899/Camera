package com.android.camera;

import java.io.File;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.android.camera.CameraManager.CameraAFCallback;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.app.OrientationManager;
import com.android.camera.custom.CustomFields;
import com.android.camera.custom.CustomUtil;
import com.android.camera.manager.ShutterManager;
import com.android.camera.manager.SwitcherManager;
import com.android.camera.manager.TopViewManager;
import com.android.camera.qrcode.QRCodeManager;
import com.android.camera.ui.ShutterButton.OnShutterButtonListener;
import com.android.camera.util.CameraUtil;
import com.muvee.timelapsesdk.TimeLapseEngine;
import com.muvee.timelapsesdk.TimeLapseEngine.TimeLapseInfoListener;
import com.tct.camera.R;

@SuppressWarnings("deprecation")
public class TctTimelapseModule implements CameraModule, TctTimelapeController, OnShutterButtonListener, TimeLapseInfoListener,TopViewManager.SettingListener {
	
	private static final String TAG = "TctTimelapseModule";
	
	private final int MIN_BATTERY_PCT = 5; //5%
	private final long SYSTEM_CHECK_INTERVAL = 15*1000; //Check for battery and storage every 15 seconds
	private static final int MSG_PREVIEW_STARTED = 1;
	
	private CameraActivity mActivity;
	private TctTimelapseUI mUI;  
	private CameraProxy mCameraDevice;
	private Parameters mParameters;
	private int mCameraId;
	boolean mPreviewing = false;
	boolean mRecording = false;
	private long lastSystemCheckTime = SYSTEM_CHECK_INTERVAL;
	private final CameraErrorCallback mErrorCallback = new CameraErrorCallback();
	private final Handler mHandler = new MainHandler();
	private int mDisplayRotation;
    private int mCameraDisplayOrientation;
    private TopViewManager mTopViewManager;
    private ShutterManager mShutterManager;
    private SwitcherManager mSwitcherManager;
    //private Intent batteryStatus;
    private TimeLapseEngine mEngine = new TimeLapseEngine();
    private ContentValues mCurrentVideoValues;
	private LocationManager mLocationManager;
	private ContentResolver mContentResolver;
	private ComboPreferences mPreferences;
	private String mStorageLocation;
	private int batteryLevel;

	@Override
    public void onSharedPreferenceChanged() {
	    updateGridManager();
	    /*[BUGFIX]added by lijuan.zhang ,2015-05-28,PR992513/991647 begin*/
	    setTimelapseParams();
        mActivity.updateStorageSpaceAndHint();
        /*[BUGFIX]added by lijuan.zhang ,2015-05-28,PR992513/991647 end*/
    }

	private OrientationManager mOrientationManager;

	@Override
	public void init(CameraActivity activity, View root) {
		mActivity = activity;
		mPreferences = new ComboPreferences(mActivity);
		CameraSettings.upgradeGlobalPreferences(mPreferences.getGlobal());
		mCameraId = getPreferredCameraId(mPreferences);
		mActivity.setCameraId(mCameraId);
        //modify by minghui.hua for PR910859 begin
        mPreferences.setLocalId(mActivity, mCameraId);
        CameraSettings.upgradeLocalPreferences(mPreferences.getLocal());
        //modify by minghui.hua for PR910859 end

		mUI = new TctTimelapseUI(activity, this, root);
        
        mTopViewManager = mActivity.getCameraViewManager().getTopViewManager();
        mShutterManager = mActivity.getCameraViewManager().getShutterManager();
        mSwitcherManager = mActivity.getCameraViewManager().getSwitcherManager();        
        mShutterManager.setOnShutterButtonListener(this);
        mTopViewManager.setListener(this);
//        mUI.setShutterButton(mShutterManager.getShutterButton());
        mLocationManager = new LocationManager(mActivity, null);
        
        mContentResolver = mActivity.getContentResolver();
        
        mOrientationManager=OrientationManager.getInstance();
        
        mEngine.setTimeLapseInfoListener(this);
        mEngine.setCameraID(mCameraId);
        setTimelapseParams();

        // modify by minghui.hua for PR970566
        updateGridManager();
	}
	
	private void setTimelapseParams(){
	    /*[BUGFIX]added by lijuan.zhang ,2015-05-28,PR992513/991647 begin*/
	    String savePathValue = mPreferences.getString(CameraSettings.KEY_CAMERA_SAVEPATH, "-1");
        Storage.setSaveSDCard(savePathValue.equals("1"), savePathValue.equals("-1"));
        /*[BUGFIX]added by lijuan.zhang ,2015-05-28,PR992513/991647 end*/
		if (Storage.isSaveSDCard() && SDCard.instance().isWriteable()) {
			mStorageLocation = SDCard.instance().getDirectory();
        } else {
        	mStorageLocation = Storage.DIRECTORY;
        }
		mEngine.setOutputLocation(mStorageLocation);
				
		/*mEngine.setOutputFileName("timelapse.mp4");
		mEngine.setOutputFilePath("/storage/sdcard0/DCIM/Camera/video.mp4");
		mEngine.setTimelapseInterval(0.5f);			
		mEngine.setVideoSize(TimeLapseEngine.RESOLUTION_FHD);
		mEngine.setVideoQuality(TimeLapseEngine.QUALITY_HIGH);
		mEngine.setMaxFileSize(400);
		mEngine.setMaxFileDuration(30);*/
	}

    private void updateGridManager() {
        // PR918800-sichao.hu add begin
        boolean isGrid = mActivity.getString(R.string.setting_on_value).equals(
                mPreferences.getString(CameraSettings.KEY_CAMERA_GRID,
                        mActivity.getString(R.string.setting_off_value)));

        if (isGrid) {
            mActivity.getCameraViewManager().getGridManager().show();
        } else {
            mActivity.getCameraViewManager().getGridManager().hide();
        }// PR918800-sichao.hu add end
    }

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);		
			float batteryPct = level / (float)scale;			
			batteryLevel = (int) (batteryPct*100);			
			Log.i(TAG, "::getBatteryPct: batteryLevel = " + batteryLevel);		
		}
		
	};
	
	private int getPreferredCameraId(ComboPreferences preferences) {
		int intentCameraId = CameraUtil.getCameraFacingIntentExtras(mActivity);
		if (intentCameraId != -1) {
			// Testing purpose. Launch a specific camera through the intent
			// extras.
			return intentCameraId;
		} else {
			return CameraSettings.readPreferredCameraId(preferences);
		}
	}

	@Override
	public void onPreviewFocusChanged(boolean previewFocused) {
		
	}

	@Override
	public void onPauseBeforeSuper() {
		Log.i(TAG, "onPauseBeforeSuper");
		if(mRecording){
			endRecording();
		}
		
//		mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		closeCamera();
		mActivity.unregisterReceiver(broadcastReceiver);
		isMediaRecorderCreated=false;
	}

	@Override
	public void onPauseAfterSuper() {
		
	}

    //modify by feiqiang.cheng for PR918972 start
	private CameraOpenThread mCameraOpenThread = null;
	@Override
	public void onResumeBeforeSuper() {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		mActivity.registerReceiver(broadcastReceiver, ifilter);
		mCameraOpenThread = new CameraOpenThread();
		mCameraOpenThread.start();
	}
    //modify by feiqiang.cheng for PR918972 end
	
	@Override
	public void onResumeAfterSuper() {
//		mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

    @Override
    public void onConfigurationChanged(Configuration config) {
        // modify by minghui.hua for PR992500
        setDisplayOrientation();
    }

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void installIntentFilter() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onBackPressed() {
		Log.i(TAG, "onBackPressed");
		if(mRecording){
			endRecording();
			return true;
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onSingleTapUp(View view, int x, int y) {	
		if(!mRecording && mCameraId==0){
			if(mParameters.getSupportedFocusModes().contains(Parameters.FOCUS_MODE_AUTO)&&
					(!mParameters.getFocusMode().equals(Parameters.FOCUS_MODE_AUTO))){
				mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
				mCameraDevice.setParameters(mParameters);
			}
			mUI.onSingleTapUp(x, y);			
		}
	}

	@Override
	public void onPreviewTextureCopied() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onCaptureTextureCopied() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUserInteraction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean updateStorageHintOnResume() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private int mOrientation;
	

	@Override
	public void onOrientationChanged(int orientation) {
        // Log.i(TAG, "onOrientationChanged = "+orientation);
		if(mEngine==null){
			return;
		}
		int rotation = 0;
		switch(orientation){
			case 0: rotation = Surface.ROTATION_0; break;
			case 90: rotation = Surface.ROTATION_90; break;
			case 180: rotation = Surface.ROTATION_180; break;
			case 270: rotation = Surface.ROTATION_270; break;
		}
		mEngine.setRotation(rotation);
		
		if(mRecording){
        	return;
		}
		
		if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return;
        int oldOrientation = mOrientation;
        mOrientation = CameraUtil.roundOrientation(orientation, mOrientation);
        
        if(mActivity.isReversibleEnabled()){
	        if((mOrientation==180||mOrientation==0)
	        		&&mOrientation!=oldOrientation){
	        	
	        	startPreview();
	        }
        }
	}

	@Override
	public void onShowSwitcherPopup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMediaSaveServiceConnected(MediaSaveService s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean arePreviewControlsVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resizeForPreviewAspectRatio() {
		// TODO Auto-generated method stub
		
	}

	//pr1000636,1003054	yuguan.chen added begin
	@Override
	public void resetToDefault() {
		Log.i(TAG, "resetToDefault");
		if (mActivity.getGC() == null) return;
		QRCodeManager.resetPrefsToDefault(mActivity);
        mActivity.getGC().resetPrefsToDefault();
        //Add by zhimin.yu for PR970374 begin
        //resetToDefaultFaceBeautySetting();
        //Add by zhimin.yu for PR970374 end
        /// add by yaping.liu for pr966435 {
        if (mPreferences != null) {
            mPreferences.edit().remove(CameraSettings.KEY_CAMERA_SAVEPATH).commit();
        }
        onSharedPreferenceChanged();
	}
	
    //Add by zhimin.yu for PR970374 begin
	/*
    private void resetToDefaultFaceBeautySetting(){
    	PhotoModule.faceBeauty_soften = PhotoModule.FACEBEAUTY_SOFTEN;
    	PhotoModule.faceBeauty_bright = PhotoModule.FACEBEAUTY_BRIGHT;
        if (null != mParameters) {
            checkThread();
            mParameters.set("arcsoft-mode", "off");
            mParameters.set("arcsoft-skin-soften", PhotoModule.FACEBEAUTY_SOFTEN);
            mParameters.set("arcsoft-skin-bright", PhotoModule.FACEBEAUTY_BRIGHT);
        }
        applyParametersToServer();
        saveFaceBeautySBValue();
    }*/
    //Add by zhimin.yu for PR970374 end
    
    /*
	private void saveFaceBeautySBValue() {
		mPreferences
				.edit()
				.putInt(CameraSettings.KEY_ARCSOFT_SOFTEN,
						PhotoModule.faceBeauty_soften).apply();
		mPreferences
				.edit()
				.putInt(CameraSettings.KEY_ARCSOFT_BRIGHT,
						PhotoModule.faceBeauty_bright).apply();
	}*/
    
    //Apply parameters to server, so native camera can apply current settings.
    //Note: mCameraDevice.setParameters() consumes long time, please don't use it unnecessary.
    public void applyParametersToServer() {
        if (mCameraDevice != null && mParameters != null) {
            checkThread();
            mCameraDevice.setParameters(mParameters);
        }
        Log.i(TAG, "applyParameterToServer() mParameters=" + mParameters + ", mCameraDevice=" + mCameraDevice);
    }
    
    // modify by minghui.hua for PR992130
    private void checkThread(){
        if (Thread.currentThread().getId() != 1) {
            CameraActivity.TraceQCTLog("set parameters not in main thread", new Throwable());
        }
    }
    //1000636,1003054	yuguan.chen added end

	@Override
	public ComboPreferences getPreferences() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doWhenCloseActivity() {
		// TODO Auto-generated method stub
		Log.i(TAG, "doWhenCloseActivity");
	}

	@Override
	public void onPreviewUIReady() {
		Log.i(TAG, "onPreviewUIReady++");
		 startPreview();
		 mTopViewManager.hideviews(true, false, true);
	}

	@Override
	public void onPreviewUIDestroyed() {
		Log.i(TAG, "onPreviewUIDestroyed++");
		stopPreview();
	}
	
	protected class CameraOpenThread extends Thread {
        @Override
        public void run() {
            openCamera();
        }
    }

	
	private Parameters mInitialParams;
    private void openCamera() {
        if (mCameraDevice == null) {
            mCameraId = mActivity.getCameraId();
            mCameraDevice = CameraUtil.openCamera(
                    mActivity, mCameraId, mHandler,
                    mActivity.getCameraOpenErrorCallback());
        }
        if (mCameraDevice == null) {
            // Error.
            return;
        }
        mActivity.setCameraDevice(mCameraDevice);
        mInitialParams=mCameraDevice.getParameters();
        mParameters = mCameraDevice.getParameters();
        
        
        mHandler.post(new Runnable(){
        	public void run(){
        		String defAntibanding=CustomUtil.getInstance().getString(CustomFields.DEF_ANTIBAND_DEFAULT, "auto");
                
                // Set anti banding parameter.
                String antiBanding = mPreferences.getString(
                         CameraSettings.KEY_ANTIBANDING,defAntibanding);//FR 720721 , sichao.hu modified
//                         mActivity.getString(R.string.pref_camera_antibanding_default));
                Log.v(TAG, "antiBanding value =" + antiBanding);
                if (CameraUtil.isSupported(antiBanding, mParameters.getSupportedAntibanding())) {
            		mParameters.setAntibanding(antiBanding);
                }
                if(mParameters.getSupportedFocusModes().contains(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)){
                	mParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }
                if(mCameraDevice!=null){
                    initialPreviewSize();
                    mCameraDevice.getCamera().setParameters(mParameters);
                }
        	}
        });
        
        startPreview();
    }
    
    private static boolean toleranceRatio(double target, double candidate) {
        final double ASPECT_TOLERANCE = 0.01;
        boolean tolerance = false;
        if (candidate > 0) {
            tolerance = Math.abs(target - candidate) <= ASPECT_TOLERANCE;
        }
        return tolerance;
    }
	
	private void initialPreviewSize() {
		if(mParameters==null||mCameraDevice==null){
			return;
		}
		List<Size> sizes = mParameters.getSupportedPreviewSizes();
		// optimalSize = mCameraDevice.getCamera().new Size(0,0);
		Rect rect = new Rect();
		mActivity.getWindowManager().getDefaultDisplay().getRectSize(rect);
		int w = rect.width();
		int h = rect.height();
		double ratio = 16d / 9;
		if (w > h) {
			if (h != 0) {
				ratio = (double)w / h;
			}
		} else {
			if (w != 0) {
				ratio = (double)h / w;
			}
		}
		String previewRatio = GC.PICTURE_RATIO_16_9;
		if (toleranceRatio(16.0 / 9.0, ratio)) {
			previewRatio = GC.PICTURE_RATIO_16_9;
		}
		if (toleranceRatio(5.0 / 3.0, ratio)) {
			previewRatio = GC.PICTURE_RATIO_5_3;
		}
		if (toleranceRatio(4.0 / 3.0, ratio)) {
			previewRatio = GC.PICTURE_RATIO_4_3;
		}
		Log.i(TAG, "previewRatio= " + previewRatio);
		Size optimalSize = CameraUtil.getOptimalPreviewSize(mActivity, sizes,
				Double.parseDouble(previewRatio));

		if (optimalSize == null) {
			return;
		}
		Size original = mParameters.getPreviewSize();
		CameraActivity.TraceLog(TAG, "origin preview size is "+original.width+"x"+original.height);
		CameraActivity.TraceLog(TAG, "optimalSize preview size is "+optimalSize.width+"x"+optimalSize.height);
		if (!original.equals(optimalSize)||optimalSize==null) {
			mParameters.setPreviewSize(optimalSize.width, optimalSize.height);
			// Zoom related settings will be changed for different preview
			// sizes, so set and read the parameters to get latest values
			mCameraDevice.setParameters(mParameters);
			startPreview();
		}
	}
    
	private void startPreview() {
        Log.i(TAG, "startPreview");
        
        SurfaceTexture surfaceTexture = mUI.getSurfaceTexture();
        if (surfaceTexture == null || mCameraDevice == null) {
            return;
        }
        
        
        mCameraDevice.setErrorCallback(mErrorCallback);
        if (mPreviewing == true) {
            stopPreview();
        }

        setDisplayOrientation();
//        mCameraDevice.setDisplayOrientation(mCameraDisplayOrientation);
        

        try {
            mCameraDevice.setPreviewTexture(surfaceTexture);
            mCameraDevice.startPreview();
            mHandler.sendEmptyMessage(MSG_PREVIEW_STARTED);
            mEngine.setCamera(mCameraDevice.getCamera());
            mPreviewing = true;
            //onPreviewStarted();
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("startPreview failed", ex);
        } 
    }
	
	private void setCameraParameters() {
		//Lock auto exposure 
		mParameters=mCameraDevice.getParameters();
//		mCameraDevice.cancelAutoFocus();//pr1015398-yuguan.chen removed
		mCameraDevice.cancelAutoFocusSync();//pr1015398-yuguan.chen added
		mUI.onAutoFocusCompleted(true);
		if(mParameters!=null ){
			if( mParameters.isAutoExposureLockSupported()&&!mParameters.getAutoExposureLock()){
				mParameters.setAutoExposureLock(true);
			}
			if(mParameters.getSupportedFocusModes().contains(Parameters.FOCUS_MODE_AUTO)&&
					(!mParameters.getFocusMode().equals(Parameters.FOCUS_MODE_AUTO))){
				mParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
			}
			mCameraDevice.setParameters(mParameters);
			
		}
	}
	
	private void resetCameraParameters(){
		mParameters=mCameraDevice.getParameters();
		if(mParameters!=null){
			if(mParameters.isAutoExposureLockSupported()){
				if(mParameters.getAutoExposureLock()){
					mParameters.setAutoExposureLock(false);
					mCameraDevice.getCamera().setParameters(mParameters);//PR929199-sichao.hu modified
				}
				if(mParameters.getSupportedFocusModes().contains(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)){
                	mParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }
			}
			
		}
	}
	
	private void closeCamera() {
        Log.v(TAG, "closeCamera",new Throwable());
        //add by feiqiang.cheng for PR918972 start
        if(mCameraOpenThread!=null && mCameraOpenThread.isAlive()){
        	try {
				mCameraOpenThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	mCameraOpenThread = null;
        }
        //add by feiqiang.cheng for PR918972 end
        if (mCameraDevice == null) {
            Log.d(TAG, "already stopped.");
            return;
        }
        mCameraDevice.setParameters(mInitialParams);
        mCameraDevice.setZoomChangeListener(null);
        mCameraDevice.setErrorCallback(null);
        //pr1003610-yuguan.chen modified begin
//        CameraHolder.instance().strongRelease();
        if (mActivity.isSecureCamera() && !CameraActivity.isFirstStartAfterScreenOn()) {
            // Blocks until camera is actually released.
            CameraHolder.instance().strongRelease();
        } else {
            CameraHolder.instance().release();
        }
        //pr1003610-yuguan.chen modified end
        if(isMediaRecorderCreated){
        	mEngine.resetMediaRecorder();
        }
//        mEngine=null;//This will cause otherwhere null-pointer ,remove it
        mCameraDevice = null;
        mPreviewing = false;
    }
	
    public void stopPreview() {
    	if (!mPreviewing) {
    		return;
    	}
        mCameraDevice.stopPreview();
        mPreviewing = false;
    }
    
    private void setDisplayOrientation() {
        mDisplayRotation = CameraUtil.getDisplayRotation(mActivity);
        mCameraDisplayOrientation = CameraUtil.getDisplayOrientation(mDisplayRotation, mCameraId);
        // Change the camera display orientation
        if (mCameraDevice != null) {
        	int orientation=mCameraDisplayOrientation;
        	if(mActivity.isReversibleEnabled()){
        		if(mOrientation==180){
        			orientation=270;
        		}else if(mOrientation==0){
        			orientation=90;
        		}
        	}
        	CameraActivity.TraceLog(TAG,"setOrientation for "+orientation);
            mCameraDevice.setDisplayOrientation(orientation);
//            mCameraDevice.setDisplayOrientation(mCameraDisplayOrientation);            
            Log.i(TAG, "setDisplayOrientation " + mCameraDisplayOrientation);
        }
    }
	
	private class MainHandler extends Handler {
		@Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_PREVIEW_STARTED: {
                // modify by minghui.hua for PR946277
                if (GC.START_QRSCAN_INTENT.equals(mActivity.getCurIntent().getAction())) {
                    mActivity.goToScanMode();
                } else if(GC.START_FRONT_CAMERA_INTENT.equals(mActivity.getCurIntent().getAction())) {
                    mActivity.backToPhotoMode();
                }
                break;
            }
            }
        }
	}

	@Override
	public void onShutterButtonFocus(boolean pressed) {
		Log.v(TAG, "onShutterButtonFocus " + pressed);			
	}

	private boolean isEnable=true;
	@Override
	public void onShutterButtonClick() {
		//PR929199-sichao.hu add begin , to avoid to frequent recording of timelapse
		if(!isEnable){
			return;
		}
		Log.v(TAG, "onShutterButtonClick" + mRecording);
		isEnable=false;
		mHandler.postDelayed(new Runnable(){
			public void run(){
				isEnable=true;
			}
		},500);
		//PR929199-sichao.hu add end
		if(mRecording){
			endRecording();
		}else{
			startRecording();
		}
	}
	
	private void updateUI(){
		mUI.updateUI(mRecording);
		if(mRecording){
			mTopViewManager.hideViewsForTimeLapse();
			mSwitcherManager.hideVideo();
			mShutterManager.mRecordTimeText.setText(timeStringMMSS(0));
			mShutterManager.mOutputTimeText.setText(timeStringMMSS(0));
			mShutterManager.showTimeStamps();
		}else{
			mTopViewManager.showViewsForTimeLapse();
			mSwitcherManager.showVideo();
			mShutterManager.hideTimeStamps();
		}
	}
	
	//PR897911-sichao.hu-modified
	/*public String timeStringMMSS(int time) {
		int totoalTime=time;
		time/=1000;
		int sec = time%60;
		time = time/60;
		int min = time%60;
		if(time>=60){
			time=time/60;
			int hour=time;
			String timeOutput=String.format("%d:%02d:%02d",hour,min,sec);
			return timeOutput;
		}
		
		return String.format("%02d:%02d", min,sec);
	}//PR897911-sichao.hu modified end*/
	//Add by zhiming.yu for PR936932 begin
    public String timeStringMMSS(long time) {
        long seconds = time / 1000; // round down to compute seconds
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long remainderMinutes = minutes - (hours * 60);
        long remainderSeconds = seconds - (minutes * 60);
        StringBuilder timeStringBuilder = new StringBuilder();
        // Hours
        if (hours > 0) {
            if (hours < 10) {
                timeStringBuilder.append('0');
            }
            timeStringBuilder.append(hours);
            timeStringBuilder.append(':');
        }
        // Minutes
        if (remainderMinutes < 10) {
            timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderMinutes);
        timeStringBuilder.append(':');
        // Seconds
        if (remainderSeconds < 10) {
            timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderSeconds);
        return timeStringBuilder.toString();
	}//Add by zhiming.yu for PR936932 end
	
	boolean isMediaRecorderCreated=false;
	private void startRecording(){
		
		
		boolean battery = checkBatteryStatus();
		boolean storage = checkStorageStatus();
		
		Log.i(TAG, "::startRecording::battery = "+battery +" storage = "+ storage);	
						
		if(battery && storage){
			setCameraParameters();
			mOrientationManager.lockOrientation();
			mEngine.startRecording();
			mRecording = true;
			isMediaRecorderCreated=true;
			mUI.setSwipingEnabled(false);
			mActivity.disableDrawer();
			updateUI();			
        } else if (!battery) {
            Toast.makeText(mActivity, mActivity.getString(R.string.battery_is_low),
                    Toast.LENGTH_SHORT).show();
        } else if (!storage) {
            Toast.makeText(mActivity, mActivity.getString(R.string.spaceIsLow_content_storage),
                    Toast.LENGTH_SHORT).show();
        }
	}
	
	private void endRecording(){
		Log.i(TAG, "::endRecording::");
		mEngine.endRecording();
		mOrientationManager.unlockOrientation();
		resetCameraParameters();
		mRecording = false;
		lastSystemCheckTime = SYSTEM_CHECK_INTERVAL;
		mUI.setSwipingEnabled(true);
		mActivity.enableDrawer();
		updateUI();
		addVideoToGallery();	
	}
	
	private void addVideoToGallery(){
		String path = mEngine.getOutputFilePath();		
		Log.i(TAG, "addVideoToGallery::path = " +path);
		if(path!=null){
			File file = new File(path);
			String name = file.getName();
			int duration = getDuration(path);
			mCurrentVideoValues = new ContentValues(9);
			mCurrentVideoValues.put(Video.Media.TITLE, name);
			mCurrentVideoValues.put(Video.Media.DISPLAY_NAME, name);
			mCurrentVideoValues.put(Video.Media.DATE_TAKEN, System.currentTimeMillis());
			mCurrentVideoValues.put(MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000);
			mCurrentVideoValues.put(Video.Media.MIME_TYPE, "video/mp4");
			mCurrentVideoValues.put(Video.Media.DATA, path);
			mCurrentVideoValues.put(Video.Media.RESOLUTION,
					Integer.toString(mEngine.getVideoWidth()) + "x" +
	                Integer.toString(mEngine.getVideoHeight()));
			Location loc = mLocationManager.getCurrentLocation();
			if (loc != null) {
				mCurrentVideoValues.put(Video.Media.LATITUDE, loc.getLatitude());
				mCurrentVideoValues.put(Video.Media.LONGITUDE, loc.getLongitude());
			}

			mActivity.getMediaSaveService().addVideo(path, duration, 
					mCurrentVideoValues, mOnVideoSavedListener, mContentResolver);
		}		
	}
	
	private int getDuration(String path){
		int duration = 0;
		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
			duration = mediaPlayer.getDuration();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mediaPlayer.release();
			mediaPlayer = null;
		}

		return duration;
	}
	
	private final MediaSaveService.OnMediaSavedListener mOnVideoSavedListener =
			new MediaSaveService.OnMediaSavedListener() {
		@Override
		public void onMediaSaved(Uri uri) {
			if (uri != null) {
				mActivity.notifyNewMedia(uri);
			}
		}
	};

	@Override
    public void onShutterButtonLongClick() {
	    CameraActivity.TraceLog(TAG, "onShutterButtonLongClick");
        String showing = mActivity.getString(R.string.normal_camera_continuous_not_supported);
        mActivity.showInfo(showing, false, false);
    }

	@Override
	public void onError(String error) {
		Log.i(TAG, "onError "+error);		
	}

    @Override
    public void onProgressUpdate(long recordTime, long outputTime) {
        // TODO Auto-generated method stub
        if(lastSystemCheckTime<recordTime){
            lastSystemCheckTime += SYSTEM_CHECK_INTERVAL;
            Log.i(TAG, "::onProgressUpdate: " + "++System Check++");
            checkDeviceState();
        }
        //mShutterManager.mRecordTimeText.setText(timeStringMMSS((int) recordTime));
        //mShutterManager.mOutputTimeText.setText(timeStringMMSS((int) outputTime));
        mShutterManager.mRecordTimeText.setText(timeStringMMSS(recordTime));
        mShutterManager.mOutputTimeText.setText(timeStringMMSS(outputTime));
    }

	@Override
	public void onRecordingStopped(int reason) {
		Log.i(TAG, "onRecordingStopped::reason = "+reason);
		if(reason==TimeLapseEngine.MAX_DURATION_REACHED){
			endRecording();
			Toast.makeText(mActivity, "MAX_DURATION_REACHED", Toast.LENGTH_SHORT).show();
		}else if(reason==TimeLapseEngine.MAX_FILESIZE_REACHED){
			endRecording();
			Toast.makeText(mActivity, "MAX_FILESIZE_REACHED", Toast.LENGTH_SHORT).show();
		}		
	}

	@Override
	public void onFocusChanged(float xCord, float yCord) {
		Log.v(TAG, "onFocusChanged "+xCord +","+ yCord);	
		if(!mPreviewing){//PR998561-sichao.hu added. need to make sure cancelAutoFocus is called before unlock camera
		    return;
		}
//		mEngine.focusTo(xCord, yCord);
		mCameraDevice.cancelAutoFocus();
		mCameraDevice.autoFocus(mHandler, mAutoFocusCallback);
	}
	
	
	private final AutoFocusCallback mAutoFocusCallback =
            new AutoFocusCallback();
	
	
	private final class AutoFocusCallback implements CameraAFCallback {
        @Override
        public void onAutoFocus(boolean focused, CameraProxy camera) {
            mUI.onAutoFocusCompleted(focused);
            // new requirement,touch focus at first time,then change to continous focus
//            if (!Parameters.FOCUS_MODE_CONTINUOUS_VIDEO.equals(mParameters.getFocusMode())) {
//                mHandler.sendEmptyMessageDelayed(MSG_CHANGE_FOCUS, CHANGE_FOCUS_DELAY);
//            }
        }
    }
	
	private void checkDeviceState(){		
		boolean battery = checkBatteryStatus();
		boolean storage = checkStorageStatus();
		
		Log.i(TAG, "::checkDeviceState battery = "+battery +" storage = "+ storage);	
		if(mRecording && (!battery || !storage)){
			endRecording();			
		}
	}
	
	private boolean checkBatteryStatus(){
		return batteryLevel>=MIN_BATTERY_PCT;
	}
	
	private boolean checkStorageStatus(){
		Log.i(TAG, "::checkStorageStatus: location = " + mStorageLocation);
		StatFs statFs = new StatFs(mStorageLocation);
	    long bytesAvailable = (long)statFs.getBlockSize() * (long)statFs.getAvailableBlocks();
	    Log.i(TAG, "::checkStorageStatus: free space = " + bytesAvailable);
	    return bytesAvailable>=Storage.LOW_STORAGE_THRESHOLD_BYTES;	 
	}

	@Override
	public void onAutoFocusCompleted(boolean success) {
		mUI.onAutoFocusCompleted(success);		
	}

	@Override
	public void setStartPreviewCallback(IStartPreviewCallback cb) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onCameraSwitched() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFlashPicked() {
		// TODO Auto-generated method stub
		return false;
	}

}
