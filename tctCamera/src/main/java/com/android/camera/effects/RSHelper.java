package com.android.camera.effects;

import com.android.camera.effects.RSFilter.ScriptC_convert;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;


public class RSHelper {

	private static Context mContext;
	private static ScriptC_convert mScript;
	private static RenderScript mRS;
	private static RSHelper mHelper;
	
	private RSHelper(Context context){
		mContext=context;
		mRS=RenderScript.create(mContext);
		mScript=new ScriptC_convert(mRS);
	}
	
	public static RSHelper getInstance(Context context){
		if(mHelper==null||mContext!=context||mScript==null||mRS==null){
			mHelper=new RSHelper(context);
		}
		
		return mHelper;
	}
	
	
	private Allocation mInAlloc;
	private Allocation mOutAlloc;
	private Allocation mIndexAlloc;
	private byte[] mOutput=null;
	private int mOutputLength=-1;
	public byte[] decodeNV21ToRGB888(byte[] nv21,int width ,int height,int degree){

		
		Type.Builder tbIn = new Type.Builder(mRS, Element.U8(mRS));
		tbIn.setX(nv21.length);
		Type.Builder tbOut= new Type.Builder(mRS, Element.U8(mRS));
		tbOut.setX(width*height*4);
		
		if(mInAlloc==null){
			mInAlloc=Allocation.createTyped(mRS, tbIn.create());
		}
		if(mOutAlloc==null){
			mOutAlloc=Allocation.createTyped(mRS, tbOut.create());
		}
		mInAlloc.copyFrom(nv21);
		
		mScript.bind_input(mInAlloc);
		mScript.bind_output(mOutAlloc);
		mScript.set_rotateDegree(degree);
		mScript.set_mImageWidth(width);
		mScript.set_mImageHeight(height);
		
		int[] indexBuffer=new int[height];
		for(int i=0;i<indexBuffer.length;i++){
			indexBuffer[i]=i;
		}
		
		Type.Builder tbIndex=new Type.Builder(mRS, Element.I32(mRS));
		tbIndex.setX(indexBuffer.length);
		if(mIndexAlloc==null){
			mIndexAlloc=Allocation.createTyped(mRS, tbIndex.create());
		}
		mIndexAlloc.copyFrom(indexBuffer);
		
		mScript.forEach_process(mIndexAlloc);
		
		if(mOutput==null||mOutputLength==-1){
			mOutputLength=width*height*4;
			mOutput=new byte[mOutputLength];
		}
		
		mOutAlloc.copyTo(mOutput);
		
		
		return mOutput;
	}
	
	public byte[] decodeNV21ToRGB888(byte[] nv21,int width ,int height){
		return decodeNV21ToRGB888(nv21,width,height,90);
	}
	
	
	private Allocation mInAlloc2;
	private Allocation mOutAlloc2;
	private Allocation mIndexAlloc2;
	private byte[] mOutput2=null;
	private int mOutputLength2=-1;
	private int mOriginWidth2=-1;
	private int mOriginHeight2=-1;
	public byte[] decodeBMPToYUV422(Bitmap bmp){
		int width=bmp.getWidth();
		int height=bmp.getHeight();
		
		boolean needUpdateAllocation=false;
		if(mOriginHeight2!=height||mOriginWidth2!=width){
			mOriginWidth2=width;
			mOriginHeight2=height;
			needUpdateAllocation=true;
		}
		Type.Builder tbIn = new Type.Builder(mRS, Element.RGBA_8888(mRS));
		tbIn.setX(width).setY(height);
//		tbIn.setX(nv21.length);
		Type.Builder tbOut= new Type.Builder(mRS, Element.U8(mRS));
		tbOut.setX(width*height+width*height/2);
		
//		if(mInAlloc2==null){
//			mInAlloc2=Allocation.createTyped(mRS, tbIn.create(),Allocation.USAGE_SCRIPT);
//		}
		if(mOutAlloc2==null||needUpdateAllocation){
			mOutAlloc2=Allocation.createTyped(mRS, tbOut.create(),Allocation.USAGE_SCRIPT);
		}
		mInAlloc2=Allocation.createFromBitmap(mRS, bmp);
		
		mScript.bind_bmpInput(mInAlloc2);
		mScript.bind_yuvOutput(mOutAlloc2);
//		
//		mScript.bind_input(mInAlloc2);
//		mScript.bind_output(mOutAlloc2);
		mScript.set_mImageWidth(width);
		mScript.set_mImageHeight(height);
		
		int[] indexBuffer=new int[height];
		for(int i=0;i<indexBuffer.length;i++){
			indexBuffer[i]=i;
		}
		
		Type.Builder tbIndex=new Type.Builder(mRS, Element.I32(mRS));
		tbIndex.setX(indexBuffer.length);
		if(mIndexAlloc2==null||needUpdateAllocation){
			mIndexAlloc2=Allocation.createTyped(mRS, tbIndex.create());
			mOriginHeight2=height;
		}
		mIndexAlloc2.copyFrom(indexBuffer);
		
		mScript.forEach_process_jpg2yuv(mIndexAlloc2);
		
		if(mOutput2==null||needUpdateAllocation){
			mOutputLength2=width*height+width*height/2;
			mOutput2=new byte[mOutputLength2];
		}
		
		mOutAlloc2.copyTo(mOutput2);
		
		
		return mOutput2;
	}
	
	
	public void destroy(){
		if(mIndexAlloc!=null){
			mIndexAlloc.destroy();
			mIndexAlloc=null;
		}
		if(mInAlloc!=null){
			mInAlloc.destroy();
			mInAlloc=null;
		}
		if(mOutAlloc!=null){
			mOutAlloc.destroy();
			mOutAlloc=null;
		}
		if(mIndexAlloc2!=null){
			mIndexAlloc2.destroy();
			mIndexAlloc2=null;
		}
		if(mInAlloc2!=null){
			mInAlloc2.destroy();
			mInAlloc2=null;
		}
		if(mOutAlloc2!=null){
			mOutAlloc2.destroy();
			mOutAlloc2=null;
		}
		if(mScript!=null){
			mScript.destroy();
			mScript=null;
		}
		if(mRS!=null){
			mRS.destroy();
			mRS=null;
		}
		mOutput=null;
		mOutputLength=-1;
		mOutput2=null;
		mOutputLength2=-1;
	}
	
	
}
