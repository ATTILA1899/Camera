package com.tct.jni;


import android.graphics.Bitmap;
import android.util.Log;

public class BeautyFaceLib{
		
	
	public static int init(int nWidth, int nHeight)
	{
		 return native_init(nWidth,nHeight);
	}
	
	public synchronized static int process(int nWidth, int nHeight, int skinSoftLevel, int skinBrightLevel, byte[] data)
	{
		 return native_process(nWidth,nHeight,skinSoftLevel, skinBrightLevel, data);
	}
	
	public static void uninit()
	{
		native_uninit();
	}
	private static native int  native_init(int nWidth,int nHeight);
	private static native int  native_process( int nWidth, int nHeight,  int skinSoftLevel, int skinBrightLevel,byte[] data);
	private static native void native_uninit();
	 
	  
	static {	
		Log.e("BeautyFaceLib", " BeautyFaceLib loadLibrary()");
		System.loadLibrary("arcsoft_beauty_shot");
		System.loadLibrary("beautyface");	
	}
	
}