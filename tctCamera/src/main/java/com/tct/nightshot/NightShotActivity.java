package com.tct.nightshot;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.camera.effects.RSHelper;
import com.android.camera.ui.RotateImageView;
import com.android.jrd.externel.ExtendCamera;
import com.android.jrd.externel.ExtendParameters;
import com.tct.camera.R;
import com.tct.jni.NightShotLib;
 

public class NightShotActivity extends Activity  implements SurfaceHolder.Callback, OnClickListener, AutoFocusCallback{
	
	public static final String TAG       = "NightShotActivity";
	public static final boolean isDebug = true;
	
	public Camera mCamera;
	public Parameters mParameters;
	public ExtendParameters mExtParameters;
	public ExtendCamera  mExtCamera;
	public SurfaceHolder mHolder;
	public SurfaceView mPreview;
	private ImageButton   mShutterBtn;
	private TextView      mTextView;
	private RotateImageView mThumbnail;
	private View mThumbLayout, mThumbnailLoadingView;
	Uri uri = null;
	static String fileName ;
	static String filePath ;
 
    public static final String DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
    public static final String DIRECTORY = DCIM + "/Camera";	
	
	private int mPictureWidth = 0;
	private int mPictureHeight= 0;	
	private boolean isCapturing 		 = false;
	private boolean isThumbnailLoading = false;
	byte []srcYuvBuff;	
	byte []dstYuvBuff;	
	int  MAX_INPUT_FRAME = 6;
	int  sedIndex = 0;
	int  rcvIndex = 0;
	boolean needResnap = false;

	RSHelper rs =null;
	private final int MSG_CAPTURE      = 0x001;
	private final int MSG_DO_NIGHTSHOT = 0x002;
	private final int MSG_FRESH_UI     = 0x003;
	
	public static final int MSG_START_PREVIEW				   = 0x0009;
    public static final int MSG_THUMBNAIL_LOADING 		   = 0x0010;
    public static final int MSG_THUMBNAIL_DONE	   		   = 0x0011;
    public static final int MSG_INTENT_IMAGE_VIEW       	   = 0x0012;	

    
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CAPTURE:
				mCamera.takePicture(mShutterBtnCallback, mRawCallback,
						mPictureCallback);
				break;
				
			case MSG_DO_NIGHTSHOT:
		    	fileName = String.format("%d.jpg", System.currentTimeMillis());
		    	filePath = String.format("%s/%s", DIRECTORY, fileName);  
		    	
				for (int i = 0; i < MAX_INPUT_FRAME; i++) {
					log(String.format(" i = %d ", i));
//					saveFile(bbArr[i], String.format("/sdcard/ns%d.jpg", i),
//							false);
//					log(String.format(" decodeByteArray rcvIndex = %d ", i));
//					Bitmap bmp = BitmapFactory.decodeByteArray(bbArr[i], 0,
//							bbArr[i].length);
//					log(String.format("BitmapFactory.decodeByteArray  rcvIndex = %d  time = %dms ",
//									i, System.currentTimeMillis() - start));
//					start = System.currentTimeMillis();
//					log(String.format(" rs.decodeBMPToYUV422 rcvIndex = %d ", i));
//					rs = RSHelper.getInstance(NightShotActivity.this);
//					byte[] temp = rs.decodeBMPToYUV422(bmp);
//					log(String.format(" rs.decodeBMPToYUV422  rcvIndex = %d, time = %dms ", i,
//							System.currentTimeMillis() - start));
//					if(null != bmp)
//					{
//						bmp.recycle();
//						bmp = null;
//					}
					start = System.currentTimeMillis();
					NightShotLib.process_jpeg(mPictureWidth, mPictureHeight, bbArr[i], bbArr[i].length, dstYuvBuff, i);
					log(String.format("NightShotLib.process_jpeg  rcvIndex = %d, time = %dms ", i,
							System.currentTimeMillis() - start));
//					if(null != temp)
//						temp = null;
//					System.gc();
				}

				start = System.currentTimeMillis();
				YuvImage yuvimage = new YuvImage(dstYuvBuff, ImageFormat.NV21,
						mPictureWidth, mPictureHeight, null);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				yuvimage.compressToJpeg(new Rect(0, 0, mPictureWidth, mPictureHeight), 100,
						baos);
				try {
					FileOutputStream fos = new FileOutputStream(new File(filePath));

					fos.write(baos.toByteArray());
					fos.flush();
					fos.close();
					baos.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				log(String.format("YuvImage  time = %dms ",			System.currentTimeMillis() - start));
				
				ContentValues v = new ContentValues();
		        v.put(MediaColumns.TITLE, fileName);
		        v.put(MediaColumns.DISPLAY_NAME, fileName);
		        v.put(ImageColumns.DATE_TAKEN, System.currentTimeMillis());
		        v.put(MediaColumns.MIME_TYPE, "image/jpeg");
		        v.put(MediaColumns.DATA, filePath);
		        v.put(MediaColumns.SIZE, filePath.length());				
		        v.put(ImageColumns.WIDTH, mPictureWidth);
		        v.put(ImageColumns.HEIGHT,mPictureHeight);
				
		        uri  = NightShotActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
				if(needResnap){
					mHandler.sendEmptyMessageDelayed(MSG_THUMBNAIL_DONE, 0);
				}
				break;
				
			case MSG_FRESH_UI:
				mShutterBtn.setVisibility(View.VISIBLE);
				mTextView.setVisibility(View.INVISIBLE);
				break;
				
			case MSG_START_PREVIEW:
				restartPreview();
				if(!needResnap){
					mHandler.sendEmptyMessageDelayed(MSG_THUMBNAIL_DONE, 0);
				}
				break;
				
            case MSG_THUMBNAIL_DONE:
                if (mThumbnailLoadingView != null) {
                    mThumbnailLoadingView.setVisibility(View.GONE);
                    mThumbnail.setImageURI(uri);
                    mShutterBtn.setVisibility(View.VISIBLE);
                    isCapturing = !isCapturing;
                    isThumbnailLoading = !isThumbnailLoading;
                    mTextView.setVisibility(View.INVISIBLE);
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



	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);	
		setContentView(R.layout.ns_preview);		
		
		mPreview 	= (SurfaceView) findViewById(R.id.camera_preview);	
		mShutterBtn    = (ImageButton)findViewById(R.id.shutter);
		mTextView 	= (TextView)findViewById(R.id.index);
		
		mHolder = mPreview.getHolder(); 
		mHolder.setKeepScreenOn(true);
		mPreview.setFocusable(true);  
		mPreview.setBackgroundColor(TRIM_MEMORY_BACKGROUND); 
		mHolder.addCallback(this); 		
		mShutterBtn.setOnClickListener(this);	
		
        mThumbLayout = findViewById(R.id.lay_thumb);
        mThumbnailLoadingView = findViewById(R.id.probar_processing);
        mThumbnail = (RotateImageView)findViewById(R.id.rimgv_thumb);
        mThumbnail.setOnClickListener(this);

	}	 
  
	
	public void onResume() {
		super.onResume();
		uri = getLastImageUriFromContentResolver(NightShotActivity.this.getContentResolver());
		mThumbnail.setImageURI(uri);
	}
		
	public void onPause() {
		if(mCamera != null) { 
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
		NightShotLib.uninit();
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	
	
	public void surfaceCreated(SurfaceHolder holder) {  
	    mCamera = Camera.open();	
	    mExtCamera = ExtendCamera.getInstance(mCamera);
	    needResnap = mExtCamera.needTakePicturePerShotDuringBurst();
	    log(String.format("  needResnap = %b", needResnap));
	    try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.setPreviewCallback(mPreviewCallback);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	    
	    mParameters = mCamera.getParameters();	
//	    log(mParameters.flatten());
	    
		List<Size> pictureSizes = mParameters.getSupportedPictureSizes();
		int index=0;
//		for(int i=0;i<pictureSizes.size();i++){
//			if(pictureSizes.get(i).width>3000){
//				continue;
//			}
//			index=i;
//			break;
//		}
		
		Size pictureSize = 	pictureSizes.get(index);	
		mPictureWidth = pictureSize.width;
		mPictureHeight= pictureSize.height;		
		mParameters.setPictureSize(mPictureWidth, mPictureHeight);
		
		mExtParameters = ExtendParameters.getInstance(mParameters);
		List<String> mList = mExtParameters.getSupportedZSLValues();
		if(!mList.isEmpty()){
			for(int i=0; i<mList.size(); i++)
				log( String.format("mList %d = %s ",i, mList.get(i) ));
		}
		else{
			log( String.format(" null == mList.isEmpty() "));
		}
//		mExtParameters.setZSLMode("on");
////		mExtParameters.setBurstShot(true);
////		mParameters.set("zsl","on");		
//		mExtCamera.setLongshot(true, mParameters);
//		
////		mParameters.set("snapshot-burst-num","6");
		mCamera.setParameters(mParameters);
		
		mParameters = mCamera.getParameters();
		mPictureWidth = mParameters.getPreviewSize().width;
		mPictureHeight= mParameters.getPreviewSize().height;		
		log( String.format("getPreviewSize mPictureWidth = %d   mPictureHeight = %d",  mPictureWidth, mPictureHeight));
		
		mParameters = mCamera.getParameters();
		mPictureWidth = mParameters.getPictureSize().width;
		mPictureHeight= mParameters.getPictureSize().height;	
		mCamera.startPreview();			
		log( String.format("getPictureSize mPictureWidth = %d   mPictureHeight = %d",  mPictureWidth, mPictureHeight));
		dstYuvBuff = new byte[mPictureWidth*mPictureHeight*3/2];
		NightShotLib.init(mPictureWidth, mPictureHeight);
     }  	 
	 
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,int arg3) {		
	} 
        
	@Override
    public void surfaceDestroyed(SurfaceHolder holder) { 
    } 
       
	 

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub		
		switch(v.getId())
		{
		case R.id.shutter:
			mExtParameters.setZSLMode("on");
//			mExtParameters.setBurstShot(true);
//			mParameters.set("zsl","on");		
			mExtCamera.setLongshot(true, mParameters);
			
//			mParameters.set("snapshot-burst-num","6");
			mCamera.setParameters(mParameters);
			sedIndex = 0;
			rcvIndex = 0;
			mShutterBtn.setVisibility(View.INVISIBLE);
			mHandler.sendEmptyMessage(MSG_THUMBNAIL_LOADING);
			mTextView.setVisibility(View.VISIBLE);
			mTextView.setText(null);
			if(!isCapturing){
				isCapturing = !isCapturing;
				mHandler.sendEmptyMessage(MSG_CAPTURE);
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
	public void onAutoFocus(boolean success, Camera camera) {
		// TODO Auto-generated method stub	
	}
	
	private ShutterCallback mShutterBtnCallback = new ShutterCallback(){

		@Override
		public void onShutter() {
			// TODO Auto-generated method stub
			sedIndex++;
			log(String.format(" mShutterBtnCallback  sedIndex = %d", sedIndex));
			if(!needResnap)
				return;
    		if(sedIndex < MAX_INPUT_FRAME)
//	    		mHandler.sendEmptyMessage(MSG_CAPTURE);
    			mCamera.takePicture(mShutterBtnCallback, mRawCallback,
						mPictureCallback);
		}		
	};
	
	private PictureCallback mRawCallback = new PictureCallback() {	
	    public 	void onPictureTaken(byte[] data, Camera camera){
	    	log(String.format(" mRawCallback  sedIndex = %d", sedIndex));    
	    }
	};

	long start = 0, end = 0;
	byte [][]bbArr = new byte[MAX_INPUT_FRAME][];
	private PictureCallback mPictureCallback = new PictureCallback() {	
	    public 	void onPictureTaken(byte[] data, Camera camera){
	    	log(String.format(" mPictureCallback  rcvIndex = %d", rcvIndex));  		

	    	switch(rcvIndex){
	    		case 0:
		    	case 1:
		    	case 2:
		    	case 3:
		    	case 4:
		    	case 5:	
		    		mTextView.setText(String.format("%d/%d", rcvIndex+1, MAX_INPUT_FRAME));
		    		bbArr[rcvIndex] = data;	
		    	break;
		    	default:
		    		break;	    	
	    	}
	    	rcvIndex++;
	    	if(rcvIndex ==MAX_INPUT_FRAME)
	    	{  
	    		mExtParameters.setZSLMode("off");
//	    		mExtParameters.setBurstShot(true);
//	    		mParameters.set("zsl","on");		
	    		mExtCamera.setLongshot(false, mParameters);
	    		
//	    		mParameters.set("snapshot-burst-num","6");
	    		mCamera.setParameters(mParameters);
	    		mTextView.setText(String.format("Night Shot processing..."));
	    		mHandler.sendEmptyMessage(MSG_DO_NIGHTSHOT);
	    		mHandler.sendEmptyMessage(MSG_START_PREVIEW);
	    	}
	    	if(!needResnap && rcvIndex == 40){
	    		mHandler.sendEmptyMessage(MSG_START_PREVIEW);
	    	}
		}

	};
	
	private PreviewCallback mPreviewCallback= new PreviewCallback() {	
	    public 	void onPreviewFrame(byte[] data, Camera camera){
			
		}
	};
	 
	public void restartPreview(){
		mCamera.stopPreview();
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.setPreviewCallback(mPreviewCallback);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		mCamera.startPreview();		
	}

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
	
	public void readFile(byte[] yuv420sp, String fileName){
		BufferedInputStream bis;	
		try {
			bis = new BufferedInputStream(new FileInputStream(fileName));
			bis.read(yuv420sp);
			bis.close();
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
		
}
