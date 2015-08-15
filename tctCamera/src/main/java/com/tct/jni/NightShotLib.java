package com.tct.jni;


import android.graphics.Bitmap;
import android.util.Log;

public class NightShotLib{
		
	
	public static int init(int nWidth, int nHeight)
	{
		 return native_init(nWidth,nHeight);
	}
	public static int process(int nWidth, int nHeight,byte[] src, byte[] dst, int index)
	{
		 return native_process(nWidth, nHeight,src, dst, index);
	}	
	public static int process_argb(int nWidth, int nHeight,int[] src, byte[] dst, int index)
	{
		 return native_process_argb(nWidth, nHeight,src, dst, index);
	}	
	public static int process_bitmap( int nWidth, int nHeight, Bitmap bmp, byte[] dst, int index)
	{
		 return native_process_bitmap(nWidth, nHeight, bmp, dst, index);
	}	
	
	public static byte[] process_bitmap_new( int nWidth, int nHeight, Bitmap bmp, int index)
	{
		 return native_process_bitmap_new(nWidth, nHeight, bmp, index);
	}
	
	public static int process_jpeg( int nWidth, int nHeight, byte[] jpeg, int dataLength, byte[] dst, int index)
	{
		 return native_process_jpeg(nWidth, nHeight, jpeg, dataLength, dst, index);
	}	
	public static int uninit()
	{
		return native_uninit();
	}
	
	private static native int  native_init(int nWidth,int nHeight);
	private static native int  native_process( int nWidth, int nHeight, byte[] src, byte[] dst, int index);
	private static native int  native_process_argb( int nWidth, int nHeight, int[] src, byte[] dst, int index);
	private static native int  native_process_bitmap( int nWidth, int nHeight, Bitmap bmp, byte[] dst, int index);
	private static native byte[]  native_process_bitmap_new( int nWidth, int nHeight, Bitmap bmp, int index);
	private static native int  native_process_jpeg(int nWidth, int nHeight, byte[] jpeg, int dataLength, byte[] dst,int index);
	private static native int  native_uninit();
	 
	  
	static {	
		Log.d("NightShotLib", " NightShotLib loadLibrary()");
		System.loadLibrary("arcsoft_night_shot");
		System.loadLibrary("tctjpeg");
		System.loadLibrary("nightshot");	
	}
	
}