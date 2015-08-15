package com.tct.jni;


import android.util.Log;

public class SceneDetectorLib{
		
	private static int mCount=0;
	
	public static int instancePool(){
		return mCount;
	}
	public static int init(int nWidth, int nHeight)
	{
		mCount++;
		 return native_init(nWidth,nHeight);
	}
	public static int process(int nWidth, int nHeight,byte[] data)
	{
		 return native_process(nWidth,nHeight,data);
	}	
	public static int uninit()
	{
		mCount--;
		if(mCount<0){
			mCount=0;
			return -1;
		}
		return native_uninit();
	}
	
	private static native int  native_init(int nWidth,int nHeight);
	private static native int  native_process( int nWidth, int nHeight, byte[] data);
	private static native int  native_uninit();
	 
	  
	static {	
		Log.d("SceneDetectorLib", " SceneDetectorLib loadLibrary()");
		System.loadLibrary("mpbase");	
		System.loadLibrary("arcsoft_picauto");
		System.loadLibrary("scenedetector");
	}
	
}