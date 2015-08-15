package com.jrdcamera.modeview;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.android.camera.CameraActivity;
import com.android.camera.PhotoModule;
import com.android.camera.PhotoUI;
import com.android.camera.ui.RotateImageView;
import com.tct.camera.R;

public class PolaroidView extends RotateImageView implements OnTouchListener ,SettingPolaroid{
	private Bitmap mCropBitmap = null;
	private Paint mPaint = null;
	private Matrix mMatrix = null;
	private CameraActivity mContext= null;
	
    private    int POLAR_COUNT = 2;
    public final static int POLAR_ONE = 1;
    public final static int POLAR_TWO = 2;
    public final static int POLAR_FOUR = 4;
    public final static int POLAR_NONE = -1;
    
    private final static String miniType = "image/*";
    
    private  HashMap<String, Bitmap> hasBitmap=new HashMap<String, Bitmap>();
    private String[] hasBitmapKey = new String[]{"first","second","thirst","forist","result"};
    private int mPicCount = -2;
    
    private boolean mDoWithLast =false;

    
    private int mWidth = 0;
    private int mHeight = 0;
    
    private float kDis = 0; // line k
    private float bDis = 0; // line b
    
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private int mDisWidHei = 0;
    
    private final static int START_CROP = 1001;
    private final static int START_SYNTHESIS = 1002;
    public final static String POLAROID_ACTION = "action_polaroid_edit";
    

    private final static int X_LINE = 0;
    private final static int Y_LINE = 1;
    private int mLineState = Y_LINE;
    private float[][] endPoint = new float[2][2];
    private float[][] mCirclePoint = new float[2][2];
    private final static int DISCROLLROTATE = 135;
    private Path mPath = null;
    
    private float[][] mScreenEndPoint = new float[4][2];
    
    private float mRotateDegree = 0;
    private float[][] mCurrentPoint = new float[1][2];
    private boolean mChangePosition  =false;
    
    private  ExecutorService mExecutorService = null;
    
    private int mCounter = 0;
    
    private PhotoUI mPhotoUI;
    
	public PolaroidView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public PolaroidView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = (CameraActivity)context;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mMatrix = new Matrix();
		mMatrix.setScale(1.0f, 1.0f);
		mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
		mScreenHeight =mContext.getResources().getDisplayMetrics().heightPixels;
		mDisWidHei = mScreenHeight - mScreenWidth ; 
		kDis = 1 ; //default value
		bDis = (float)mDisWidHei/2; // default value
		mWidth = mScreenWidth;
		mHeight = mScreenWidth;;
		initPoint();
		((CameraActivity)context).getCameraViewManager().getBackButtonManager().setCallPolaroid(this);
	}

	/**
	 * get instance
	 */
	
	public void setPhotoUI(PhotoUI mUI){
		mPhotoUI = mUI;
	}
	
	
	@Override
	protected int getDegree() {
		// TODO Auto-generated method stub
		return super.getDegree();
	}
	
	

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void  onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if(mDoWithLast && null != mCropBitmap){
			Matrix matrix = new Matrix();
			if(mCropBitmap.getWidth() > mScreenWidth){
				matrix.setScale(((float)mScreenWidth)/mCropBitmap.getWidth(), ((float)mScreenWidth)/mCropBitmap.getWidth());
			}
			canvas.drawBitmap(mCropBitmap, matrix, mPaint);
			mDoWithLast = false;
		}
		if(POLAR_COUNT ==POLAR_TWO && null != mCropBitmap){
			Op type = null;
			switch(mPicCount){
			case -1:
				type = Region.Op.REPLACE;
				break;
			case 0:
				type = Region.Op.DIFFERENCE;
				break;
				default:
					type = Region.Op.DIFFERENCE;
					break;
			}
			mPath = getPath();
			Bitmap drawBit = cropTriangleBitmap(type);
			Matrix matrix = new Matrix();
			if(drawBit.getWidth() > mScreenWidth){
				matrix.setScale(((float)mScreenWidth)/drawBit.getWidth(), ((float)mScreenWidth)/drawBit.getWidth());
			}
			canvas.drawBitmap(drawBit, matrix, mPaint);
			mPaint.setColor(Color.WHITE);
			mPaint.setStrokeWidth(5);
			canvas.drawLine(endPoint[0][0]*mScreenWidth, endPoint[0][1]*mScreenWidth, endPoint[1][0]*mScreenWidth, endPoint[1][1]*mScreenWidth, mPaint);
			canvas.drawCircle(mScreenWidth/2,mScreenWidth/2, 10, mPaint);
			if(!drawBit.isRecycled()){
				drawBit.recycle();
			}
		}else if(POLAR_COUNT == POLAR_FOUR){
			if(null != mCropBitmap &&  !mCropBitmap.isRecycled()){
				Matrix matrix = new Matrix();
				if(mWidth > mScreenWidth){
					matrix.setScale(((float)mScreenWidth)/mCropBitmap.getWidth(), ((float)mScreenWidth)/mCropBitmap.getWidth());
				}
				canvas.drawBitmap(mCropBitmap, matrix, mPaint);
			}
			
		}
		super.onDraw(canvas);
	}
	
	
	@Override
	public void setImageBitmap(Bitmap bm) {
		// TODO Auto-generated method stub
		synchronized(hasBitmap){
		if(null == bm){
			return;
		}
		if(mWidth < bm.getWidth()){
			mWidth = bm.getWidth();
			mHeight = bm.getHeight();
		}
		if(POLAR_COUNT == 1){
			mPicCount++;
			new SaveSingleBitmapTask().execute(bm);
			return ;
		}
			
			if(POLAR_COUNT == 2){
				mPicCount++;
				mCropBitmap = bm;
				if(mPicCount == 1){
					//intent to polaractivity
					mExecutorService.execute(new CropTraingleBitmapTask());
					
				}else{
					invalidate();
				}
			}
		if(POLAR_COUNT ==4){
			//scale bitmap 
			if(mPicCount == -2){
				mCropBitmap = bm;
				invalidate();
				mPicCount = -1;;
				return; 
			}
			mPicCount++;
			new CropRectBitmapTask().execute(bm , mPicCount);
			
		}
		
		}
		}
	
	
	/**
	 * init for four pic 
	 */
	public void setPicCount(int count){
		mPicCount = count;
	}
	
	public int getPicCount(){
		return mPicCount;
	}

	/**
	 * ontouch event
	 * 
	 */
	
	private boolean allowTouch =false;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if(isTouch(event)){
			allowTouch = true;
		}
		if(allowTouch){
			switch (event.getAction()){
			case MotionEvent.ACTION_DOWN:
				float disUpRotate = getCrossRotation(mCurrentPoint[0]);
				mCurrentPoint[0][0] = event.getX();
				mCurrentPoint[0][1] = event.getY();
				float disDownrotate = getCrossRotation(mCurrentPoint[0]);
				if(Math.abs(disDownrotate - disUpRotate) >120 && Math.abs(disDownrotate - disUpRotate) < 240){
					mChangePosition = true;
				}else{
					mChangePosition = false;
				}
				return true;
			case MotionEvent.ACTION_UP:
				allowTouch = false;
				return false;
			case MotionEvent.ACTION_CANCEL:
				allowTouch = false;
				return false;
			case MotionEvent.ACTION_MOVE:
				//do get line
				mCurrentPoint[0][0] = event.getX();
				mCurrentPoint[0][1] = event.getY();
				if(mPicCount == -1){
					mExecutorService.execute(new EndPointRunnable(mCurrentPoint[0]));
				}
				return true;
			default:
				return false;
			}
		}else{
			return false;
		}
		
	}
	
	
	
	/**
	 * change point to other side
	 */
	
	private float[] changPoint(float[] point){
		float[] result = new float[2];
		result[0] = mWidth - point[0];
		result[1] = mHeight - point[1];
		return result;
	}
	
	/**
	 * get event
	 */
	
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(getPolarCount() == 2 && mPicCount == -1){
			return onTouch(this, event);
		}else{
			return false;	
		}
		
	}
	
	/**
	 * get line according two point
	 */
	
	private synchronized float[][] getEndPoint(float[] touchPoint){
		float[][] endPoint = new float[2][2];
		//y = k x + b
		float k=((touchPoint[1]-mHeight/2)/(touchPoint[0]-mWidth/2));
		//b= y-kx
		float b = mHeight/2 - ((touchPoint[1]-mHeight/2)/(touchPoint[0]-mWidth/2))*mWidth/2;
		switch (inWhichLine(touchPoint)){
		case Y_LINE:
			//x=0 || x = mWith
			endPoint[0][0] = 0;
			endPoint[0][1] = b/mWidth;
			endPoint[1][0] = 1;
			endPoint[1][1] = k+b/mWidth;
			//circle
			//y = -(1/k)x+a
			float a = mHeight/2+(1/k)*mWidth/2;
			//cross x line
			//x = a/(1/k);
			if(a/(1/k) > mWidth/2){
				mCirclePoint[0][0] = (a/(1/k) - mWidth/2)/2+mWidth/2;
			}else{
				mCirclePoint[0][0] = (mWidth/2 -  (a/(1/k)))/2 + a/(1/k);
			}
			
			mCirclePoint[0][1] = mHeight/4;
			
			if(mCirclePoint[0][0] > mWidth/2){
				mCirclePoint[1][0] =mWidth/2 -  (mCirclePoint[0][0] - mWidth/2);
			}else{
				mCirclePoint[1][0] = mWidth/2 + (mWidth/2 - mCirclePoint[0][0]);
			}
			
			mCirclePoint[1][1] = 3*mHeight/4 ;
			
			
			break;
		case X_LINE:
			//y=0 || y = mHeight
			endPoint[0][0] = (-b)/(k*mHeight);
			endPoint[0][1] = 0;
			endPoint[1][0] = (1-b/mHeight)/k;
			endPoint[1][1] = 1;
			
			//circle
			//y = -(1/k)x+c
			float c = mHeight/2+(1/k)*mWidth/2;
			//cross x line
			//x = a/(1/k);
			mCirclePoint[0][0] =mWidth/4;
			
			if(c > mHeight/2){
				mCirclePoint[0][1] = (c - mHeight/2)/2+mHeight/2;
			}else{
				mCirclePoint[0][1] =  (mHeight/2 - c)/2+c;
			}
			if(mCirclePoint[0][1] > mHeight/2){
				mCirclePoint[1][1] =mHeight/2 -  (mCirclePoint[0][1] - mHeight/2);
			}else{
				mCirclePoint[1][1] = mHeight/2 + (mHeight/2 - mCirclePoint[0][1]);
			}
			
			mCirclePoint[1][0] = 3*mWidth/4 ;
			
			break;
		}
		return endPoint;
		
	}
	
	/**
	 * get cross line
	 */
	private int inWhichLine(float[] touchPoint){
		float xDistance = touchPoint[0]-mScreenWidth/2;
		float yDistance = touchPoint[1]-mScreenWidth/2;
		if(Math.abs(xDistance) >= Math.abs(yDistance)){
			mLineState = Y_LINE;
		}else{
			mLineState = X_LINE;
		}
		return mLineState;
	}

	
	/**
	 * 
	 * get rotate
	 */
	
	private float getCrossRotation(float[] point) {
		double delta_x = (point[0] - mScreenWidth/2);
		double delta_y = (point[1] -  mScreenWidth/2);
		double radians = Math.atan2(delta_y, delta_x);
		return (float) Math.toDegrees(radians) + DISCROLLROTATE;
	}
	
	/**
	 * 
	 * Do with bitmap
	 */
	  public Bitmap cropTriangleBitmap( Op cropType){
		  Bitmap output = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
		  Canvas canvas = new Canvas(output);
	        canvas.clipPath(mPath, cropType);
	        Matrix max = new Matrix();
	        float dis = (float)mWidth/mCropBitmap.getWidth();
	        max.setScale(dis, dis);
	        canvas.drawBitmap(mCropBitmap,max, mPaint);
	        if(POLAR_COUNT == 2 && mPicCount == -1 ){
	        	drawTriangleCircleText(canvas , getPolaroidOrientation());
	        }
	        if(mPicCount < 0){
	        	return output;
	        }
	        hasBitmap.put(hasBitmapKey[mPicCount], output);
	        return output;	
	    	
	    }

		  private Bitmap scaleBitmap(Bitmap bitmap,int picCount){
			  Bitmap output = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
			  Canvas canvas = new Canvas(output);
			    Matrix matrix = new Matrix();
			    float xDis = (float)(mWidth/2) / bitmap.getWidth() ; 
				matrix.setScale(xDis,xDis);	
			    canvas.drawBitmap(bitmap, matrix, mPaint);
			    hasBitmap.put(hasBitmapKey[picCount], output);
			    if(!bitmap.isRecycled()){
			    	bitmap.recycle();
			    }
			    return output;
		  }
	    private  Bitmap synthesisBitmap(Bitmap bitmap , int picCount){
	    	
	    	Bitmap output =Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);;
	    	Canvas canvas = new Canvas(output);
	    	for(int x = 0 ;x < hasBitmapKey.length ; x++){
	    		scaleSmallBitmap(x);
	    	}
	    	if(POLAR_COUNT == 2){
	    		if(null != hasBitmap.get(hasBitmapKey[0])){
		    		canvas.drawBitmap(hasBitmap.get(hasBitmapKey[0]),new Matrix(), mPaint);
		    	}
	    		if(null != hasBitmap.get(hasBitmapKey[1])){
		    		canvas.drawBitmap(hasBitmap.get(hasBitmapKey[1]),new Matrix(), mPaint);
		    	}
	    		mPaint.setAntiAlias(true);
	    		mPaint.setColor(Color.BLACK);
				mPaint.setStrokeWidth(3);
				canvas.drawLine(endPoint[0][0]*mWidth, endPoint[0][1]*mWidth, endPoint[1][0]*mWidth, endPoint[1][1]*mWidth, mPaint);
	    	}else if(POLAR_COUNT ==4){
	    		switch (picCount){
			    case 0:
			    	drawClearCircleText(canvas,2);
			    	drawRectCircleText(canvas, mScreenEndPoint[2], "3" , true);
			    	drawRectCircleText(canvas, mScreenEndPoint[3], "4" ,true);
			    	break;
			    case 1:
			    	//do with 3 
			    	drawClearCircleText(canvas,3);
			    	drawRectCircleText(canvas, mScreenEndPoint[3], "4" ,true);
			    	break;
			    case 2:
			    	//do with 4
			    	drawClearCircleText(canvas ,4);
			    	break;
			    }
	    		if(null != hasBitmap.get(hasBitmapKey[0])){
		    		canvas.drawBitmap(hasBitmap.get(hasBitmapKey[0]), 0,0, mPaint);
		    	}
		    	if(null != hasBitmap.get(hasBitmapKey[1])){
		        	canvas.drawBitmap(hasBitmap.get(hasBitmapKey[1]),mWidth/2 , 0, mPaint);
		    	}
		    	if(null != hasBitmap.get(hasBitmapKey[2])){
		        	canvas.drawBitmap(hasBitmap.get(hasBitmapKey[2]), 0 , mHeight/2, mPaint);
		    	}
		    	if(null != hasBitmap.get(hasBitmapKey[3])){
		    		canvas.drawBitmap(hasBitmap.get(hasBitmapKey[3]), mWidth/2, mHeight/2, mPaint);
		    	}
		    	mPaint.setColor(Color.BLACK);
		    	mPaint.setStrokeWidth(3);
		    	canvas.drawLine(0, mWidth/2, mWidth, mWidth/2, mPaint);
		    	canvas.drawLine(mWidth/2, 0, mWidth/2, mWidth, mPaint);
	    	}
	    	hasBitmap.put(hasBitmapKey[4], output);
	    	return output;
	    }
	    
	    private void scaleSmallBitmap(int key){
	    	if(null != hasBitmap.get(hasBitmapKey[key]) && hasBitmap.get(hasBitmapKey[key]).getWidth() < mWidth){
	    		hasBitmap.put(hasBitmapKey[key], zoomBitmap(hasBitmap.get(hasBitmapKey[key])));
	    	}
	    }
	    
	    private Bitmap zoomBitmap(Bitmap bitmap){
	    	Bitmap zoomBit = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
	    	Canvas canvas = new Canvas(zoomBit);
	    	Matrix matrix = new Matrix();
	    	float dis = (float)mWidth/bitmap.getWidth();
	    	matrix.setScale(dis, dis);
	    	canvas.drawBitmap(bitmap, matrix, mPaint);
	    	return zoomBit;
	    }
	    public void resetPolaroidView(){
	    	for(int x=0; x < hasBitmapKey.length ; x++){
	    		if(null != hasBitmap.get(hasBitmapKey[x]) && !hasBitmap.get(hasBitmapKey[x]).isRecycled()){
	    			hasBitmap.get(hasBitmapKey[x]).recycle();
	    		}
	    	}
	    	hasBitmap.clear();
	    	if(null != mCropBitmap && !mCropBitmap.isRecycled()){
	    		mCropBitmap.recycle();
	    	}
	    	mRotateDegree = 0;
	    	mWidth = mScreenWidth ;
	    	mHeight = mScreenWidth;
	    	kDis = 1 ; //default value
			bDis = (float)mDisWidHei/2; // default value
	    	initPoint();
	    	mCurrentPoint[0] = endPoint[0]; // (0,0)
	    	mChangePosition =false;
	    	if(mExecutorService != null){
	    		mExecutorService.shutdown();
	    	}
	    	if(mPhotoUI.getTextureView().getScaleX() == 0.5f){
	    		mPhotoUI.getTextureView().animate().scaleX(1.0f).setStartDelay(100).start();
	    		mPhotoUI.getTextureView().animate().scaleY(1.0f).setStartDelay(100).start();
	    	}
	    	mDoWithLast = false;
	    	mPicCount = -2;
	    	mCounter = 0;
	    	mPath = null;
	    }
	    
	    @Override
	    public void setVisibility(int visibility) {
	    	// TODO Auto-generated method stub
	    	if(visibility == View.VISIBLE){
	    		mExecutorService = Executors.newCachedThreadPool();
	    	}else 	if(visibility == View.GONE){
	    		//reset and gone
	    		resetPolaroidView();
	    	}else if (visibility == PhotoUI.MARKID){
	    		//only gone
	    		visibility = View.GONE;
	    	}
	    	super.setVisibility(visibility);
	    }
	    
	    
	    public void initPoint(){
	    	endPoint[0][0] = 0;endPoint[0][1] = 0;
	    	endPoint[1][0] = 1;endPoint[1][1] = 1;
	    	mCirclePoint[0][0] = mWidth/4 ; mCirclePoint[0][1] = 3*mHeight/4;
	    	mCirclePoint[1][0] = 3*mWidth/4 ; mCirclePoint[1][1] = mHeight/4;
	    	mScreenEndPoint[0][0] = 0;mScreenEndPoint[0][1] = 0;
	    	mScreenEndPoint[1][0] = (float)1/2;mScreenEndPoint[1][1] = 0;
	    	mScreenEndPoint[2][0] = 0;mScreenEndPoint[2][1] = (float)1/2;
	    	mScreenEndPoint[3][0] = (float)1/2;mScreenEndPoint[3][1] = (float)1/2;
	    }

	    public int getBitmapSize(){
	    	return hasBitmap.size();
	    }
	    
	    class EndPointRunnable implements Runnable{

	    	private float[] params ; 
	    	public EndPointRunnable(float[] params) {
				// TODO Auto-generated constructor stub
	    		this.params=params;
			}
	    	
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(mChangePosition){
					params = changPoint(params);
					mCurrentPoint[0] = params;
				}else{
					
				}
				mRotateDegree =  getCrossRotation(params);
				synchronized(endPoint){
					endPoint = getEndPoint(params);
				}
				mHandler.sendEmptyMessage(START_CROP);
			}
	    	
		};
		
	    class CropTraingleBitmapTask implements Runnable{
	    	
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mRotateDegree =0;
				Bitmap cropBitmap = cropTriangleBitmap(Region.Op.REPLACE);
				//display
				Bitmap bit =synthesisBitmap(cropBitmap, mPicCount);
				mDoWithLast = true;
				mCropBitmap = bit;
				mHandler.sendEmptyMessage(START_CROP);
				//save big pic
				Uri uri = null;
				if(mContext.getCurrentModule() instanceof PhotoModule){
					uri = ((PhotoModule)(mContext.getCurrentModule())).savePicForPolaroid(bit);	
				}
				Message msg = new Message();
				msg.what = START_SYNTHESIS;
				msg.obj = uri;
				mHandler.sendMessage(msg);
				
			}
	    	
		};
		
		class CropFirstTraingleBitmapTask extends AsyncTask<Void, Void, Bitmap>{

			@Override
			protected Bitmap doInBackground(Void... params) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			protected void onPostExecute(Bitmap result) {
				// TODO Auto-generated method stub
				super.onPostExecute(result);
			}
		}
		
		private Handler mHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what){
				case START_CROP:
					invalidate();
					break;
				case START_SYNTHESIS:
					try {
						Intent intent = new Intent(POLAROID_ACTION);
				          Uri uri = (Uri)msg.obj;
				          intent.setDataAndType(uri, miniType).setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				          mContext.startActivity(intent);
					} catch (Exception e) {
						// TODO: handle exception
						Intent intent = new Intent(Intent.ACTION_EDIT);
				          Uri uri = (Uri)msg.obj;
				          intent.setDataAndType(uri, miniType);
				          mContext.startActivity(intent);
					}
			          
			          if(mContext.getCameraViewManager().getRotateProgress().isShowing()){
			        	  mContext.getCameraViewManager().getRotateProgress().hide();
			        	  mContext.getCameraViewManager().getShutterManager().getShutterButton().enableTouch(true);
			          }
			          setVisibility(View.GONE);
			          mContext.resetPolaroid(true);
					break;
				}
				super.handleMessage(msg);
			}
			
		};
		
		
		private Path getPath(){
			Path temPath = new Path();
			  temPath.moveTo(endPoint[0][0]*mWidth, endPoint[0][1]*mWidth);
		    	int x = (int)(mRotateDegree / 180);
		    	switch(mLineState){
		    	case X_LINE:
		    		if((mRotateDegree / 180)+1 >= 2*x+1 && (mRotateDegree / 180)+1 < 2*x+2){
		    			temPath.lineTo(0, 0);
		    			temPath.lineTo(0, mHeight);
		    			temPath.lineTo(endPoint[1][0]*mWidth, endPoint[1][1]*mWidth);
		    		}else{
		    			temPath.lineTo(mWidth, 0);
		    			temPath.lineTo(mWidth, mHeight);
		    			temPath.lineTo(endPoint[1][0]*mWidth, endPoint[1][1]*mWidth);	
		    		}
		    		
		    		break;
		    	case Y_LINE:
		    		if((mRotateDegree / 180)+1 >2*x+1 && (mRotateDegree / 180)+1 <2*x+2){
		    			temPath.lineTo(0, 0);
		    			temPath.lineTo(mWidth, 0);
			    		temPath.lineTo(endPoint[1][0]*mWidth, endPoint[1][1]*mWidth);
		    		}else{
		    			temPath.lineTo(0, mHeight);
		    			temPath.lineTo(mWidth, mHeight);
			    		temPath.lineTo(endPoint[1][0]*mWidth, endPoint[1][1]*mWidth);	
		    		}
		    		
		    		break;
		    	}
		    	
		    	temPath.addPath(temPath , mMatrix);
		        return temPath;
		}
		
		class CropRectBitmapTask extends AsyncTask<Object, Void, Bitmap>{
			private int mCurrentPicCount = 0;

			@Override
			protected Bitmap doInBackground(Object... params) {
				// TODO Auto-generated method stub
				synchronized(params[1]){
					mCurrentPicCount = (int)params[1];
					Bitmap scaleBit = scaleBitmap((Bitmap)params[0],(int)params[1]);
					if((int)params[1] < 3 ){
						return synthesisBitmap(scaleBit,(int)params[1]);
					}else{
						Bitmap bitmap = synthesisBitmap(scaleBit,(int)params[1]);
						
						mDoWithLast = true;
						mCropBitmap = bitmap;
						mHandler.sendEmptyMessage(START_CROP);
						
						Uri uri = null;
						if(mContext.getCurrentModule() instanceof PhotoModule){
							uri = ((PhotoModule)(mContext.getCurrentModule())).savePicForPolaroid(bitmap);	
						}
						Message msg = new Message();
						msg.what = START_SYNTHESIS;
						msg.obj = uri;
						mHandler.sendMessage(msg);

						
						
						return bitmap;
					}
				}
			}
			@Override
			protected void onPostExecute(Bitmap result) {
				// TODO Auto-generated method stub
				if(mCurrentPicCount < 3){
					mCropBitmap = result;
					invalidate();
					changeTextureViewSize(mCurrentPicCount);
				}
				
				super.onPostExecute(result);
			}
			
		}
		
		/**
		 * change surface view
		 */

		public void changeTextureViewSize(int position){
			TextureView mView =mPhotoUI.getTextureView(); 
			int x = position+1;
			mView.setPivotX(0);
			mView.setPivotY(0);
			if(mView.getScaleX() != 0.5f){
				mView.animate().scaleX(0.5f).start();
				mView.animate().scaleY(0.5f).start();
			}
			mView.animate().translationX(mScreenEndPoint[x][0]*mScreenWidth).setDuration(400).start();
			mView.animate().translationY(mScreenEndPoint[x][1]*mScreenWidth).setDuration(400).start();
		}
		
		/**
		 * Draw circle and text
		 */
		private void drawTriangleCircleText(Canvas canvas , int orientation){
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.polaroid_num_2);
			Matrix matrix = new Matrix();
			matrix.setRotate(orientation);
			matrix.preScale(1.8f, 1.8f);
			Bitmap tem = Bitmap.createBitmap(bmp,  0, 0, bmp.getWidth() , bmp.getHeight() , matrix ,false);
			canvas.drawBitmap(tem ,mCirclePoint[0][0]-tem.getWidth()/2 , mCirclePoint[0][1]-tem.getHeight()/2, mPaint );
			canvas.drawBitmap(tem ,mCirclePoint[1][0]-tem.getWidth()/2 , mCirclePoint[1][1]-tem.getHeight()/2, mPaint );
			if(null != bmp && !bmp.isRecycled()){
				bmp.recycle();
			}
			if(null != tem && !tem.isRecycled()){
				tem.recycle();
			}
//			mPaint.setStrokeWidth(2);
//			mPaint.setColor(getResources().getColor(R.color.polaroid_circle));
//        	canvas.drawCircle(mCirclePoint[0][0], mCirclePoint[0][1], getResources().getDimension(R.dimen.circle_r), mPaint);
//        	mPaint.setColor(getResources().getColor(R.color.polaroid_circle_text));
//        	mPaint.setTextSize(getResources().getInteger(R.integer.polaroid_circle_text_size));
//        	canvas.drawText((mPicCount+3)+"", mCirclePoint[0][0]-30, mCirclePoint[0][1]+30, mPaint);
//        	mPaint.setColor(getResources().getColor(R.color.polaroid_circle));
//        	canvas.drawCircle(mCirclePoint[1][0], mCirclePoint[1][1], getResources().getDimension(R.dimen.circle_r), mPaint);
//        	mPaint.setColor(getResources().getColor(R.color.polaroid_circle_text));
//        	mPaint.setTextSize(getResources().getInteger(R.integer.polaroid_circle_text_size));
//        	canvas.drawText((mPicCount+3)+"", mCirclePoint[1][0]-30, mCirclePoint[1][1]+30, mPaint);
        	
		}
		
		public void drawRectCircleText(Canvas canvas , float[] midpoint,String text , boolean isPic){
			
			float value = 0 ;
			float Proportion = 0;
			if(isPic){
				value = mWidth;
				Proportion = (float)mWidth/mScreenWidth;
			}else{
				value = 1 ;
				Proportion =1;
			}
			Bitmap bmp = null;
			switch(text){
			case "2":
				bmp = BitmapFactory.decodeResource(getResources(), R.drawable.polaroid_num_2);
				break;
			case "3":
				bmp = BitmapFactory.decodeResource(getResources(), R.drawable.polaroid_num_3);
				break;
			case "4":
				bmp = BitmapFactory.decodeResource(getResources(), R.drawable.polaroid_num_4);
				break;
			}
			Matrix matrix = new Matrix();
			matrix.setRotate(getPolaroidOrientation());
			matrix.preScale(1.8f*Proportion, 1.8f*Proportion);
			Bitmap tem = Bitmap.createBitmap(bmp,  0, 0, bmp.getWidth() , bmp.getHeight() , matrix ,false);
			canvas.drawBitmap(tem ,midpoint[0]*value+mWidth/4-tem.getWidth()/2 , midpoint[1]*value+mWidth/4-tem.getHeight()/2, mPaint );
			if(null != bmp && !bmp.isRecycled()){
				bmp.recycle();
			}
			if(null != tem && !tem.isRecycled()){
				tem.recycle();
			}
//			mPaint.setStrokeWidth(2);
//			mPaint.setColor(getResources().getColor(R.color.polaroid_circle));
//        	canvas.drawCircle(midpoint[0]*value+mWidth/4, midpoint[1]*value+mHeight/4, getResources().getDimension(R.dimen.circle_r)*Proportion, mPaint);
//        	mPaint.setColor(getResources().getColor(R.color.polaroid_circle_text));
//        	mPaint.setTextSize(getResources().getInteger(R.integer.polaroid_circle_text_size)*Proportion);
//        	canvas.drawText(text, midpoint[0]*value+mWidth/4-30*Proportion, midpoint[1]*value+mHeight/4+30*Proportion, mPaint);
		}
		
		private void drawClearCircleText(Canvas canvas , int x){
			Path path = new Path();
			switch (x){
			case 1:
				// do with 2
				path.moveTo(0, 0);
				path.lineTo(mWidth/2,0);
				path.lineTo(mWidth/2, mWidth/2);
				path.lineTo(0,mWidth/2);
				canvas.clipPath(path,Op.DIFFERENCE);
				break;
			case 2:
				// do with 2
				path.moveTo(mWidth/2, 0);
				path.lineTo(mWidth,0);
				path.lineTo(mWidth, mWidth/2);
				path.lineTo(mWidth/2,mWidth/2);
				canvas.clipPath(path,Op.DIFFERENCE);
				break;
			case 3:
				//do with 3 
				path.moveTo(0, mWidth/2);
				path.lineTo(mWidth/2,mWidth/2);
				path.lineTo(mWidth/2, mWidth);
				path.lineTo(0,mWidth);
				canvas.clipPath(path,Op.DIFFERENCE);
			break;
			case 4:
				//do with 4 
				path.moveTo(mWidth/2, mWidth/2);
				path.lineTo(mWidth,mWidth/2);
				path.lineTo(mWidth, mWidth);
				path.lineTo(mWidth/2,mWidth);
				canvas.clipPath(path,Op.DIFFERENCE);
			break;
			}
			canvas.drawColor(Color.parseColor("#1A1A1A"));
		}
		public void setInitValue(boolean value){
			
		}

		@Override
		public void changePolarCount(int picCount) {
			// TODO Auto-generated method stub
			if(POLAR_COUNT == picCount){
				return;
			}
			POLAR_COUNT = picCount;
			mPhotoUI.initPolaroidView(true);
		}
		
		public int getPolarCount(){
			return POLAR_COUNT;
		}
		
		class SaveSingleBitmapTask extends AsyncTask<Bitmap, Void, Void>{

			@Override
			protected Void doInBackground(Bitmap... params) {
				// TODO Auto-generated method stub
				mDoWithLast = true;
				mCropBitmap = params[0];
				mHandler.sendEmptyMessage(START_CROP);
				Uri uri = null;
				if(mContext.getCurrentModule() instanceof PhotoModule){
					uri = ((PhotoModule)(mContext.getCurrentModule())).savePicForPolaroid(params[0]);	
				}
				Message msg = new Message();
				msg.what = START_SYNTHESIS;
				msg.obj = uri;
				mHandler.sendMessage(msg);
				return null;
			}
		}
		public boolean counterConout(){
			mCounter ++ ;
			switch (POLAR_COUNT){
			case POLAR_FOUR:
				if(mCounter > POLAR_FOUR-1){
					mCounter = 0;
					mContext.getCameraViewManager().getShutterManager().getShutterButton().enableTouch(false);
					mContext.getCameraViewManager().getRotateProgress().showProgress(getResources().getString(R.string.pano_review_saving_indication_str), mContext.getOrientation());
					return true;
				}else{
					return false;
				}
			case POLAR_TWO:
				if(mCounter > POLAR_TWO-1){
					mCounter = 0;
					mContext.getCameraViewManager().getShutterManager().getShutterButton().enableTouch(false);
					mContext.getCameraViewManager().getRotateProgress().showProgress(getResources().getString(R.string.pano_review_saving_indication_str), mContext.getOrientation());
					return true;
				}else{
					return false;
				}
			case POLAR_ONE:
				if(mCounter > POLAR_ONE-1){
					mCounter = 0;
					mContext.getCameraViewManager().getShutterManager().getShutterButton().enableTouch(false);
					mContext.getCameraViewManager().getRotateProgress().showProgress(getResources().getString(R.string.pano_review_saving_indication_str), mContext.getOrientation());
					return true;
				}else{
					return false;
				}
			}
			return false;
		}
		/**
		 * touch in line  2in1
		 * @param event
		 * @return
		 */
		private boolean  isTouch(MotionEvent event){
			// in line aera
			if(event.getY() > (float)mDisWidHei/2 ||  event.getY() < (mScreenHeight - (float)mDisWidHei/2)){
				float[] touchPoint = new float[2];
				touchPoint[0] = event.getX();
				touchPoint[1] = event.getY();
				if(touchInLine(touchPoint)){
					return true;
				}
			}
			return false;
		}
		
		private boolean touchInLine(float touchPoint[]){
			if(Math.abs(endPoint[1][0]-endPoint[0][0]) < 0.045){  // near very big value  ,judge x
				if(Math.abs(endPoint[1][1] - endPoint[0][0]) < 120 && Math.abs(touchPoint[0] - (Math.abs(endPoint[1][0] - endPoint[0][0]) / 2 + Math.min(endPoint[0][0], endPoint[1][0]))*mScreenWidth) < 120 ){
					return true;
				}else{
					return false;
				}
				
			}else{
				//judge y = kx +b
				kDis = (endPoint[1][1]-endPoint[0][1])/(endPoint[1][0]-endPoint[0][0]);
				bDis=(endPoint[1][1]- kDis*endPoint[1][0])*mScreenWidth;
				if(Math.abs((touchPoint[0]*kDis + bDis) - touchPoint[1]) <120){
					return true;
				}else{
					return false;
				}
			}
			
			//y = kx+b
			
		}
		
		public void rotateCircleFour(){
			new AsyncTask<Void, Void, Void>(){

				@Override
				protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub
					Bitmap bitmap =Bitmap.createBitmap(mCropBitmap.getWidth(), mCropBitmap.getHeight(), Config.ARGB_8888);
					Canvas canvas = new Canvas(bitmap);
					switch (mPicCount){
					case -1:
						drawClearCircleText(canvas,1);
						drawRectCircleText(canvas, mScreenEndPoint[1], "2" , true);
						drawRectCircleText(canvas, mScreenEndPoint[2], "3" , true);
				    	drawRectCircleText(canvas, mScreenEndPoint[3], "4" ,true);
						break;
					case 0:
				    	drawClearCircleText(canvas,2);
				    	drawRectCircleText(canvas, mScreenEndPoint[2], "3" , true);
				    	drawRectCircleText(canvas, mScreenEndPoint[3], "4" ,true);
				    	break;
				    case 1:
				    	//do with 3 
				    	drawClearCircleText(canvas,3);
				    	drawRectCircleText(canvas, mScreenEndPoint[3], "4" ,true);
				    	break;
				    case 2:
				    	//do with 4
				    	drawClearCircleText(canvas ,4);
				    	break;
				    }
					if(null != hasBitmap.get(hasBitmapKey[0])){
			    		canvas.drawBitmap(hasBitmap.get(hasBitmapKey[0]), 0,0, mPaint);
			    	}
			    	if(null != hasBitmap.get(hasBitmapKey[1])){
			        	canvas.drawBitmap(hasBitmap.get(hasBitmapKey[1]),mWidth/2 , 0, mPaint);
			    	}
			    	if(null != hasBitmap.get(hasBitmapKey[2])){
			        	canvas.drawBitmap(hasBitmap.get(hasBitmapKey[2]), 0 , mHeight/2, mPaint);
			    	}
			    	if(null != hasBitmap.get(hasBitmapKey[3])){
			    		canvas.drawBitmap(hasBitmap.get(hasBitmapKey[3]), mWidth/2, mHeight/2, mPaint);
			    	}
			    	mPaint.setColor(Color.BLACK);
			    	mPaint.setStrokeWidth(3);
			    	canvas.drawLine(0, mWidth/2, mWidth, mWidth/2, mPaint);
			    	canvas.drawLine(mWidth/2, 0, mWidth/2, mWidth, mPaint);
					mCropBitmap = bitmap;
					return null;
				}
				
				@Override
				protected void onPostExecute(Void result) {
					// TODO Auto-generated method stub
					invalidate();
					super.onPostExecute(result);
				}
			}.execute();
			
			
		}
		
		private int getPolaroidOrientation(){
			int orientation = mContext.getOrientation();
			switch(orientation){
			case 270 :
				orientation = 90;
				break;
			case 90 :
				orientation = 270;
				break;
			}
			return orientation;
		}
}
