package com.android.jrd.externel;

import java.lang.reflect.Method;
import java.util.HashMap;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;

public class ExtendCamera {
    private static Camera mCamera;

    private static ExtendCamera sInstance;

    public synchronized static ExtendCamera getInstance(Camera camera) {
        if (sInstance == null || camera != mCamera) {
            sInstance = new ExtendCamera(camera);
        }
        return sInstance;
    }

    private HashMap<String, Method> mCameraMethodMap = new HashMap<String, Method>();

    private static final String TAG = "YUHUAN_ExtendCamera";

    private ExtendCamera(Camera camera) {
        mCamera = camera;
        Class<Camera> cameraClz = Camera.class;
        Method[] methods = cameraClz.getMethods();
        for (Method method : methods) {
        	Log.d(TAG,"###YUHUAN###ExtendCamera#method.getName()=" + method.getName());
            mCameraMethodMap.put(method.getName(), method);
        }
    }
    
    private boolean isSupport(String method){
    	return mCameraMethodMap.containsKey(method);
    }

    private void set(String method, Object value) {
        if (value == null) {
            return;
        }
        Method setMethod = mCameraMethodMap.get(method);

        if (setMethod != null) {
            setMethod.setAccessible(true);
            try {
                setMethod.invoke(mCamera, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * The return value stands for whether the burst shot needs to 
     * request taking picture for every shot
     */
    public boolean needTakePicturePerShotDuringBurst(){
    	String setQualcommBurstshot="setLongshot";
    	return isSupport(setQualcommBurstshot);
    }
   
    public void setLongshot(boolean enable,Parameters param) {
    	Log.d(TAG,"###YUHUAN###setLongshot#enable=" + enable);
    	String setQualcommBurstshot="setLongshot";
    	if(isSupport(setQualcommBurstshot)){
    		Log.d(TAG,"###YUHUAN###set Longshot");
    		set("setLongshot",enable);
    	}else{
    		ExtendParameters extCamera=ExtendParameters.getInstance(param);
    		extCamera.setBurstShot(enable);
    	}
        
    }

}
