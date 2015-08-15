package com.tct.beautyface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.android.camera.effects.RSHelper;
import com.android.camera.ui.RotateImageView;
import com.tct.camera.R;
import com.tct.jni.BeautyFaceLib;


public class BeautyFaceActivity extends Activity  implements SurfaceHolder.Callback, OnClickListener, OnSeekBarChangeListener{
	
	public static final String TAG       = "BeautyFaceActivity";
	public static final boolean isDebug = true;
	
	public Camera mCamera;
	public Parameters mParameters;
	public SurfaceHolder bf_previewHolder;
	public SurfaceView bf_preview;
	private ImageButton   mShutterBtn;
	private SeekBar       mSoftenSeekBar;
	private SeekBar       mBrightSeekBar;
	private RotateImageView mThumbnail;
	private View mThumbLayout, mThumbnailLoadingView;
	
	private int mPreviewWidth = 0;
	private int mPreviewHeight= 0;	
	private int mPictureWidth = 0;
	private int mPictureHeight= 0;
	private boolean isCapturing 		 = false;
	private boolean isThumbnailLoading = false;

	
	/**
	 * ArcSoft recommend value
	 */
	private int mSkinSoftenLevel = 30;
	private int mSkinBrightLevel = 30;
	static String fileName ;
	static String filePath ;
	
    public static final String DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
    public static final String DIRECTORY = DCIM + "/Camera";	

	int []argb ;
	Bitmap mBitmap = null;	
	RSHelper rsHelper = null;	
	MyByteBufferQueue bufferQueue = null;
	Uri uri = null;
	
	
	public final int MSG_PREVIEW_DATA_READY 	       = 0x0001;
	public final int MSG_BEAUTY_FACE_YUV_DATA_READY  = 0x0002;
	public final int MSG_BEAUTY_FACE_ARGB_DATA_READY = 0x0003;
	public final int MSG_DRAW_BEAUTY_FACE_DATA       = 0x0004;
	
    public static final int MSG_THUMBNAIL_LOADING 		   = 0x0010;
    public static final int MSG_THUMBNAIL_DONE	   		   = 0x0011;
    public static final int MSG_INTENT_IMAGE_VIEW       	   = 0x0012;	
	
	
	private Rect mRect=new Rect();
	
	
	private Handler mRecogHandler;
	private Looper mRecogLooper;
	private Thread mRecogThread=new Thread(){

		@Override
		public void run() {
			Looper.prepare();
			mRecogLooper=Looper.myLooper();
			mRecogHandler=new Handler(mRecogLooper){

			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
					case MSG_PREVIEW_DATA_READY: 
	            	log( "MSG_PREVIEW_DATA_READY "); 
	            	MyByteBuffer mbb = (MyByteBuffer)msg.obj;
	            	if(mbb != null)
	            	{
	    	        	BeautyFaceLib.process(mPreviewWidth, mPreviewHeight, mSkinSoftenLevel, mSkinBrightLevel, mbb.getByteBuffer());
	    	        	Message.obtain(mHandler, MSG_BEAUTY_FACE_YUV_DATA_READY,mbb).sendToTarget();
	            	}
	            	break;
				}
			}				
			};
			Looper.loop();				
		}		
	};
	
	private Handler mHandler = new Handler() {		 
        @Override 
        public void handleMessage(Message msg) {
                 switch (msg.what)
                 {
                 case MSG_BEAUTY_FACE_YUV_DATA_READY:

                		long start = 0;
                		start = System.currentTimeMillis();
           
                		MyByteBuffer mbb=(MyByteBuffer)msg.obj;
                     	rsHelper=RSHelper.getInstance(BeautyFaceActivity.this);
                     	byte[]  out=null;
                       	out=rsHelper.decodeNV21ToRGB888(mbb.getByteBuffer(), mPreviewWidth, mPreviewHeight);	        			
                     	log( "decodeNV21ToRGB888 ______________ time =  " + (System.currentTimeMillis() - start));             
                     	
                     	bufferQueue.unLockBuffer(mbb);
                     	if(mBitmap==null){	        			
                     		mBitmap=Bitmap.createBitmap(mPreviewHeight, mPreviewWidth, Config.ARGB_8888);
                     	}  
                     	
                     	start = System.currentTimeMillis();
	                    Buffer buff=ByteBuffer.wrap(out);	        			
	                    mBitmap.copyPixelsFromBuffer(buff);
	                    Canvas canvas=bf_previewHolder.lockCanvas(); 
	                    if(canvas != null)
	                    {
	                    	canvas.drawBitmap(mBitmap, null, mRect, null);
	                    	bf_previewHolder.unlockCanvasAndPost(canvas);
	                    	log(" drawBitmap  time = " + (System.currentTimeMillis() - start));
	                    }	       
                	 break;
                	 
                 case MSG_BEAUTY_FACE_ARGB_DATA_READY:
                	 break;
                	 
                 case MSG_DRAW_BEAUTY_FACE_DATA: 
                	 break;
                	 
                 case MSG_THUMBNAIL_DONE:
                     if (mThumbnailLoadingView != null) {
                         mThumbnailLoadingView.setVisibility(View.GONE);
                         mThumbnail.setImageURI(uri);
                         mShutterBtn.setVisibility(View.VISIBLE);
                         isCapturing = !isCapturing;
                         isThumbnailLoading = !isThumbnailLoading;
                     }              
                	 break;                	 
                 case MSG_THUMBNAIL_LOADING: 
                     if (mThumbnailLoadingView != null) {
                    	 isThumbnailLoading = !isThumbnailLoading;
                         mThumbnailLoadingView.setVisibility(View.VISIBLE);
                     }
                	 break;
                	 
                 case MSG_INTENT_IMAGE_VIEW:
         	    	log(String.format(" filePath = %s", filePath));
         			Intent intent = new Intent("android.intent.action.VIEW");
         			intent.addCategory("android.intent.category.DEFAULT");
         			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         			Uri uri1 = Uri.fromFile(new File(filePath));
         			intent.setDataAndType(uri1, "image/*");
         			startActivity(intent);   			
                	 break;
                	 default:
                		 break;
                 }                	 
            super.handleMessage(msg);
        }
    };   



    private SurfaceTexture mTexture;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRecogThread.start();
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.bf_preview);		
		
		mTexture=new SurfaceTexture(0);		
		
		bf_preview = (SurfaceView) findViewById(R.id.bf_preview);
		bf_previewHolder = bf_preview.getHolder(); 
		bf_previewHolder.addCallback(this);
		 
		mShutterBtn    = (ImageButton)findViewById(R.id.shutter);
		mShutterBtn.setOnClickListener(this);	
		
		mSoftenSeekBar     = (SeekBar) findViewById(R.id.soften);
		mBrightSeekBar     = (SeekBar) findViewById(R.id.bright);
		mSoftenSeekBar.setOnSeekBarChangeListener(this);
		mBrightSeekBar.setOnSeekBarChangeListener(this);
		mSoftenSeekBar.setProgress(mSkinSoftenLevel);
		mBrightSeekBar.setProgress(mSkinBrightLevel);
		
        mThumbLayout = findViewById(R.id.lay_thumb);
        mThumbnailLoadingView = findViewById(R.id.probar_processing);
        mThumbnail = (RotateImageView)findViewById(R.id.rimgv_thumb);
        mThumbnail.setOnClickListener(this);
       
		
		Button btn1080=(Button)findViewById(R.id.button_1080);
		btn1080.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				synchronized(this){
					mCamera.stopPreview();
					rsHelper.destroy();
					List<Size> sizes=mParameters.getSupportedPreviewSizes();
					mParameters = mCamera.getParameters();
					Size largestSize=sizes.get(0);
					mPreviewWidth = largestSize.width;
					mPreviewHeight= largestSize.height;
					mParameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
					mCamera.setParameters(mParameters);
				restartPreview();
				}
			}
			
		});
		Button btn720=(Button)findViewById(R.id.button_720);
		btn720.setOnClickListener(new OnClickListener(){

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				synchronized(this){
					mCamera.stopPreview();
				rsHelper.destroy();
				List<Size> sizes=mParameters.getSupportedPreviewSizes();
				mParameters = mCamera.getParameters();
				Size largestSize=sizes.get(2);
				mPreviewWidth = largestSize.width;
				mPreviewHeight= largestSize.height;
				mParameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
				mCamera.setParameters(mParameters);
				restartPreview();
				}
			}			
		});
		Button btn480=(Button)findViewById(R.id.button_480);
		btn480.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				synchronized(this){
					mCamera.stopPreview();
				rsHelper.destroy();
				List<Size> sizes=mParameters.getSupportedPreviewSizes();
				mParameters = mCamera.getParameters();
				Size largestSize=sizes.get(3);
				mPreviewWidth = largestSize.width;
				mPreviewHeight= largestSize.height;
				mParameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
				mCamera.setParameters(mParameters);
				restartPreview();
				}				
			}			
		});
	}	

	
	public void onResume() {
		super.onResume();		
		uri = getLastImageUriFromContentResolver(BeautyFaceActivity.this.getContentResolver());
		mThumbnail.setImageURI(uri);
	}
	
	public void onPause(){
		if(mCamera != null) { 
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		super.onPause();
	}
		
	
	@Override
	protected void onDestroy() {
		if(null != rsHelper)
		    rsHelper.destroy();
		mRecogLooper.quit();
		super.onDestroy();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.shutter:
			mHandler.sendEmptyMessage(MSG_THUMBNAIL_LOADING);
			mShutterBtn.setVisibility(View.INVISIBLE);
			if(!isCapturing){
				isCapturing = !isCapturing;
				mCamera.takePicture(null, null, mPictureCallback);
			}
			break;
		case R.id.rimgv_thumb:
			if(!isThumbnailLoading)
				mHandler.sendEmptyMessage(MSG_INTENT_IMAGE_VIEW);
			break;
			default:
				break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		if(mSoftenSeekBar == seekBar){
			mSkinSoftenLevel = progress;
		}
		else if (mBrightSeekBar == seekBar) {
			mSkinBrightLevel = progress;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}
	

	
	private void initCamera(){
		mCamera=Camera.open();
		mParameters = mCamera.getParameters();
		try {
			mCamera.setPreviewTexture(mTexture);
			mCamera.setPreviewCallback(mPreviewCallback);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Size> sizes=mParameters.getSupportedPreviewSizes();
		Size largestSize=sizes.get(2);
		
		Point out=new Point(0,0);
		this.getWindowManager().getDefaultDisplay().getSize(out);
		this.getWindowManager().getDefaultDisplay().getRectSize(mRect);
		mPreviewWidth=out.x;
		mPreviewHeight=out.y;
		boolean supported=false;
		for(Size s:sizes){
			if(s.height==mPreviewHeight&&s.width==mPreviewWidth){
				supported=true;
			}
		}		
		if(!supported){
			mParameters = mCamera.getParameters();
			mPreviewWidth = largestSize.width;
			mPreviewHeight= largestSize.height;
		}

		List<Size> pictureSizes = mParameters.getSupportedPictureSizes();
		int index=0;
		for(int i=0;i<pictureSizes.size();i++){
			if(pictureSizes.get(i).width>3000){
				continue;
			}
			index=i;
			break;
		}
		
		Size pictureSize = 	pictureSizes.get(index);	
		mPictureWidth = pictureSize.width;
		mPictureHeight= pictureSize.height;			
		mParameters.setPictureSize(mPictureWidth, mPictureHeight);	
		
		mParameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
//		mParameters.setZoom(1);
		mParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//		mParameters.setPreviewFpsRange(15, 30);
		mCamera.setParameters(mParameters);
		
		mParameters =  mCamera.getParameters();
		mPreviewWidth = mParameters.getPreviewSize().width;
		mPreviewHeight= mParameters.getPreviewSize().height;		
		mPictureWidth = mParameters.getPictureSize().width;
		mPictureHeight= mParameters.getPictureSize().height;	
		bufferQueue = new MyByteBufferQueue(3, 3*mPreviewWidth*mPreviewHeight/2);
		mCamera.startPreview();		
		
		log( String.format("mPreviewWidth = %d   mPreviewHeight = %d",  mPreviewWidth, mPreviewHeight));
		log( String.format("mPictureWidth = %d mPictureHeight = %d",  mPictureWidth, mPictureHeight));
		BeautyFaceLib.init(mPreviewWidth, mPreviewHeight);
	}
	
	public void restartPreview(){
		try {
			mCamera.setPreviewTexture(mTexture);
			mCamera.setPreviewCallback(mPreviewCallback);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mBitmap=Bitmap.createBitmap(mPreviewHeight, mPreviewWidth, Config.ARGB_8888);
		mCamera.startPreview();
		
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		new Thread(){
			@Override
			public void run(){
				initCamera();
			}
		}.start();
		/* buffer queue for preview callback frame data*/
	//	argb = new int[mPreviewWidth*mPreviewHeight];
     }  
            
    public void surfaceDestroyed(SurfaceHolder holder) {
     } 
        
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,int arg3) {			
	} 
	
	
	
	
	private PreviewCallback mPreviewCallback = new PreviewCallback() {

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub;				
			if(null != data )
			{
				log(String.format("mPreviewCallback data length = %d", data.length));
				MyByteBuffer mbb = bufferQueue.getFreeBuffer();
				if(mbb==null){
					return;
				}
				System.arraycopy(data, 0, mbb.getByteBuffer(), 0, data.length);	
				Message msg = Message.obtain(mRecogHandler, MSG_PREVIEW_DATA_READY,mbb);
				mRecogHandler.sendMessage(msg);				
			}
		}
	};
	
	long start = 0;
	byte [] tempData = null;
	
	private PictureCallback mPictureCallback = new PictureCallback() {	
	    public 	void onPictureTaken(byte[] data, Camera camera){
	    	log(String.format("mPictureCallback    data length = %d", data.length));
	    	restartPreview();
	    	tempData = data;	 
	    	fileName = String.format("%d.jpg", System.currentTimeMillis());
	    	filePath = String.format("%s/%s", DIRECTORY, fileName);
	    	
	    	new Thread(){
	    		public void run(){
	    			
	    			log(String.format("mPictureCallback    tempData.length = %d", tempData.length));
	    			start = System.currentTimeMillis();	
	    			Bitmap bmp = BitmapFactory.decodeByteArray(tempData, 0,	tempData.length);
	    			log(String.format("BitmapFactory.decodeByteArray   time = %dms ", System.currentTimeMillis() - start));
	    			
	    			start = System.currentTimeMillis();	
	    			if(null == rsHelper)
	    				rsHelper = RSHelper.getInstance(BeautyFaceActivity.this);
	    			byte[] temp = rsHelper.decodeBMPToYUV422(bmp);
	    			bmp.recycle();
	    			bmp=null;
	    			log(String.format(" rsHelper.decodeBMPToYUV422  time = %dms ", System.currentTimeMillis() - start));
	    			
	    			start = System.currentTimeMillis();
	    			BeautyFaceLib.process(mPictureWidth, mPictureHeight, mSkinSoftenLevel, mSkinBrightLevel, temp);
	    			log(String.format("BeautyFaceLib.process  time = %dms ", System.currentTimeMillis() - start));
	    			
	    			
	    			start = System.currentTimeMillis();
	    			YuvImage yuvimage = new YuvImage(temp, ImageFormat.NV21,mPictureWidth, mPictureHeight, null);
	    			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    			yuvimage.compressToJpeg(new Rect(0, 0, mPictureWidth, mPictureHeight), 100, baos);
	    			try {
	    				FileOutputStream fos = new FileOutputStream(new File(filePath));	    				
	    				fos.write(baos.toByteArray());
	    				fos.flush();
	    				fos.close();
	    				baos.close();
	    				yuvimage=null;
	    				temp=null;
	    			} catch (FileNotFoundException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	    			log(String.format("YuvImage  time = %dms ",	System.currentTimeMillis() - start));	    			

					ContentValues v = new ContentValues();
			        v.put(MediaColumns.TITLE, fileName);
			        v.put(MediaColumns.DISPLAY_NAME, fileName);
			        v.put(ImageColumns.DATE_TAKEN, System.currentTimeMillis());
			        v.put(MediaColumns.MIME_TYPE, "image/jpeg");
			        v.put(MediaColumns.DATA, filePath);
			        v.put(MediaColumns.SIZE, filePath.length());				
			        v.put(ImageColumns.WIDTH, mPictureWidth);
			        v.put(ImageColumns.HEIGHT,mPictureHeight);
					System.gc();
			        uri  = BeautyFaceActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
					log(String.format("Shan Uri = %s", uri.toString()));			
	    			mHandler.sendEmptyMessage(MSG_THUMBNAIL_DONE);//	    	mHandler.sendEmptyMessage(MSG_INTENT_IMAGE_VIEW);
	    		}	    		
	    	}.start();
	    }	    
	};
   

	public void log(String str){
		if(isDebug)
			Log.e(TAG, str);
	}
	
	public void saveFile(byte[] yuv420sp, String fileName, boolean append){
	    FileOutputStream fos;
		try {
			fos = new FileOutputStream(fileName, append);
			fos.write(yuv420sp);
			fos.close();
		} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    
	}	
	
	

	private static Uri getLastImageUriFromContentResolver(
			ContentResolver resolver) {
		Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;

		Uri query = baseUri.buildUpon().appendQueryParameter("limit", "1")
				.build();
		String[] projection = new String[] { ImageColumns._ID, MediaColumns.DATA,
				ImageColumns.ORIENTATION, ImageColumns.DATE_TAKEN };
		String selection = ImageColumns.MIME_TYPE + "='image/jpeg' AND "
				+ ImageColumns.BUCKET_ID + '='+ String.valueOf(DIRECTORY.toLowerCase().hashCode());
		String order = ImageColumns.DATE_TAKEN + " DESC," + ImageColumns._ID
				+ " DESC";

		Cursor cursor = null;
		try {
			cursor = resolver.query(query, projection, selection, null, order);
			if (cursor != null && cursor.moveToFirst()) {
				long id = cursor.getLong(0);
				filePath = cursor.getString(1);
				return ContentUris.withAppendedId(baseUri, id);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}
	
	class MyByteBufferQueue{		
		private int queueLength = 1;
		private int bufferSize  = 1;
		List<MyByteBuffer> bufferList   = null;
		List<MyByteBuffer> freeBuffer,lockedBuffer,processedBuffer;
		
		MyByteBufferQueue(int queueLength, int bufferSize)  {	
			this.queueLength = queueLength;
			this.bufferSize  = bufferSize;				
			freeBuffer=new ArrayList<MyByteBuffer>();
			
			for(int i=0; i < this.queueLength; i++){			
				freeBuffer.add(i, new MyByteBuffer( this.bufferSize));	
		    }
		}		

		MyByteBuffer getFreeBuffer(){
			if(freeBuffer.size()>0){
				MyByteBuffer bb=freeBuffer.remove(0);
				return bb;
			}
			return null;
		}
		
		
		void unLockBuffer(MyByteBuffer buffer){
			freeBuffer.add(buffer);
		}
		
	}
	
	
	class MyByteBuffer{
		byte[] bufferData = null;     
		
		MyByteBuffer(int bufferLength){			
			this.bufferData = new byte[bufferLength];
		}		
		
		public byte[] getByteBuffer(){
			return bufferData;
		}		
	}	
	
}
