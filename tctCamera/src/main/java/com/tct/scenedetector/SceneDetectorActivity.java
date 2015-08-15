package com.tct.scenedetector;

import java.io.File;
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
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
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

import com.android.camera.ui.RotateImageView;
import com.tct.camera.R;
import com.tct.jni.SceneDetectorLib;
//import com.tct.Camera;

public class SceneDetectorActivity extends Activity  implements SurfaceHolder.Callback, OnClickListener{
	
	public static final String TAG       = "SceneDetectorActivity";
	public static final boolean isDebug = true;
	
	public Camera mCamera;
	public Parameters mParameters;
	public SurfaceHolder mHolder;
	public SurfaceView mPreview;
	private TextView   mText; 
	private ImageButton   mShutterBtn;
	private RotateImageView mThumbnail;
	private View mThumbLayout, mThumbnailLoadingView;
	Uri uri = null;
	static String fileName ;
	static String filePath ;
    public static final String DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
    public static final String DIRECTORY = DCIM + "/Camera";	
    
	private int mPreviewWidth = 0;
	private int mPreviewHeight= 0;	
	private int mPictureWidth = 0;
	private int mPictureHeight= 0;
	private boolean isCapturing 		 = false;
	private boolean isThumbnailLoading = false;

	/*ArcSoft Typed Scene Mode */
	public final int auto 			= 0;
	public final int landscape 	= 1;
	public final int portrait 		= 2;
	public final int night    		= 4;
	public final int backlit 		= 6;
	public final int gourmet 		= 19;	
	
	private List<String> mSceneModes;
	
    public static final int MSG_THUMBNAIL_LOADING 		   = 0x0010;
    public static final int MSG_THUMBNAIL_DONE	   		   = 0x0011;
    public static final int MSG_INTENT_IMAGE_VIEW       	   = 0x0012;	

	private Handler mHandler = new Handler() {		 
        @Override 
        public void handleMessage(Message msg) {
        	
                 switch (msg.what)
                 {
                 case auto:             
                	 mText.setText("auto" ); 	 
	               	 if(mSceneModes.contains(Parameters.SCENE_MODE_AUTO)){
	            		 mParameters.setSceneMode(Parameters.SCENE_MODE_AUTO);
	            		 if(null != mCamera)
	            			 mCamera.setParameters(mParameters);
	            	 }
                	 break;
                 case landscape: 
                	 mText.setText("landscape");
                	 if(mSceneModes.contains(Parameters.SCENE_MODE_LANDSCAPE)){
                		 mParameters.setSceneMode(Parameters.SCENE_MODE_LANDSCAPE);
                		 if(null != mCamera)
                			 mCamera.setParameters(mParameters);
                	 }
                	 break;
                 case portrait: 
                	 mText.setText("portrait");
                	 if(mSceneModes.contains(Parameters.SCENE_MODE_PORTRAIT)){
                		 mParameters.setSceneMode(Parameters.SCENE_MODE_PORTRAIT);
                		 if(null != mCamera)
                		     mCamera.setParameters(mParameters);
                	 }
                	 break;
                 case night: 
                	 mText.setText("night" );
                	 if(mSceneModes.contains(Parameters.SCENE_MODE_NIGHT)){
                		 mParameters.setSceneMode(Parameters.SCENE_MODE_NIGHT);             		 
                		 if(null != mCamera)
                			 mCamera.setParameters(mParameters);
                	 }
                	 
                	 
                	 break;
                 case backlit: 
                	 mText.setText("backlit");
                	 if(mSceneModes.contains("backlight")){
                		 mParameters.setSceneMode("backlight");
                		 if(null != mCamera)
                			 mCamera.setParameters(mParameters);
                	 }
                	 break;
                 case gourmet: 
                	 mText.setText("gourmet");
//                	 if(mSceneModes.contains(Parameters.SCENE_MODE_MARCO)){
//                		 mParameters.setSceneMode(Parameters.SCENE_MODE_LANDSCAPE);
//                	 }
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
    



	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);	
		setContentView(R.layout.sd_preview);		
		
		mPreview = (SurfaceView) findViewById(R.id.camera_preview);	
		mText    = (TextView)findViewById(R.id.scene);
		mShutterBtn    = (ImageButton)findViewById(R.id.shutter);
		mShutterBtn.setOnClickListener(this);	
		
        mThumbLayout = findViewById(R.id.lay_thumb);
        mThumbnailLoadingView = findViewById(R.id.probar_processing);
        mThumbnail = (RotateImageView)findViewById(R.id.rimgv_thumb);
        mThumbnail.setOnClickListener(this);
		
		mHolder = mPreview.getHolder(); 
		mHolder.setKeepScreenOn(true);
		mPreview.setFocusable(true);  
		mPreview.setBackgroundColor(TRIM_MEMORY_BACKGROUND); 
		mHolder.addCallback(this); 	
   
	}	 
  
	@Override	
	public void onResume() {
		super.onResume();
		uri = getLastImageUriFromContentResolver(SceneDetectorActivity.this.getContentResolver());
		mThumbnail.setImageURI(uri);
	}
	
	@Override	
	public void onPause() {
		if(mCamera != null) { 
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
		SceneDetectorLib.uninit();
		super.onPause();
	}	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.shutter:
			mShutterBtn.setVisibility(View.INVISIBLE);
			mHandler.sendEmptyMessage(MSG_THUMBNAIL_LOADING);
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
	
	public void surfaceCreated(SurfaceHolder holder) {  		
	    mCamera = Camera.open();	    
	    try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.setPreviewCallback(mPreviewCallback);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	    
	    mParameters = mCamera.getParameters();	    
	    mSceneModes=mParameters.getSupportedSceneModes();	
		
		List<Size> sizes=mParameters.getSupportedPreviewSizes();
		Size largestSize=sizes.get(2);
		
		mPreviewWidth=SceneDetectorActivity.this.getWindow().getDecorView().getWidth();
		mPreviewHeight=SceneDetectorActivity.this.getWindow().getDecorView().getHeight();
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
		Size pictureSize = 	pictureSizes.get(0);	
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

		mCamera.startPreview();			
		log(String.format("mPreviewWidth = %d   mPreviewHeight = %d",  mPreviewWidth, mPreviewHeight));	
		SceneDetectorLib.init(mPreviewWidth, mPreviewWidth);
     }  
	
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,int arg3) {		
	} 
        
	
    public void surfaceDestroyed(SurfaceHolder holder) {  
     } 
          


	private PreviewCallback mPreviewCallback = new PreviewCallback() {
		
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub;	
			int res = 0; 			
			res = SceneDetectorLib.process(mPreviewWidth, mPreviewHeight, data);	
			mHandler.sendEmptyMessage(res);
		}
	};

	byte [] tempData = null;
	private PictureCallback mPictureCallback = new PictureCallback() {	
	    public 	void onPictureTaken(byte[] data, Camera camera){	
	    	restartPreview();
	    	tempData = data;
	    	fileName = String.format("%d.jpg", System.currentTimeMillis());
	    	filePath = String.format("%s/%s", DIRECTORY, fileName);
	    	new Thread(){
	    		public void run(){
	    			saveFile(tempData, filePath, false); 
					ContentValues v = new ContentValues();
			        v.put(MediaColumns.TITLE, fileName);
			        v.put(MediaColumns.DISPLAY_NAME, fileName);
			        v.put(ImageColumns.DATE_TAKEN, System.currentTimeMillis());
			        v.put(MediaColumns.MIME_TYPE, "image/jpeg");
			        v.put(MediaColumns.DATA, filePath);
			        v.put(MediaColumns.SIZE, filePath.length());				
			        v.put(ImageColumns.WIDTH, mPictureWidth);
			        v.put(ImageColumns.HEIGHT,mPictureHeight);
					
			        uri  = SceneDetectorActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
	    			mHandler.sendEmptyMessageDelayed(MSG_THUMBNAIL_DONE, 1000);
	    		}
	    	}.start();
 				
	    }
	};


	public void restartPreview(){
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
