package com.android.camera.ui;

import com.android.camera.GC;
import com.android.camera.PhotoModule;
import com.android.camera.manager.CameraManualModeManager;
import com.jrdcamera.modeview.MenuDrawable;
import com.jrdcamera.modeview.ModeListView;
import com.tct.camera.R;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class ManualModeSeekBar extends LinearLayout {
	
	private LayoutInflater mInflater;
	private View mView;
	private TextView seekbarValue;
	private SeekBar seekBarSoften;
	
	
	private int mSeekBarWid=0;
	private float seekbarPosition_y=0;
	private int maxValue=0;
	private int minValue=0;
	private int defaultValue=0;
//	private BeautyFaceActivity context;
	private int[] locations = new int[2];
	private final static int LEFT_DEVIATION=10;
	private int RIGHT_DEVIATION=0;
	
	public final static String[] isoString=new String[]{"0","100","200","250","400","600","800","1200","1600","2400"};
	
	private CameraManualModeManager mCameraManualModeManager;
	
	public ManualModeSeekBar(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}



	public ManualModeSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub

		mInflater = LayoutInflater.from(context);
		mView = mInflater.inflate(R.layout.manual_mode_seekbar, this);
		seekbarValue = (TextView)mView.findViewById(R.id.manualseekbarvalue);
		seekBarSoften = (SeekBar)mView.findViewById(R.id.manualseekbar);
//		seekBarSoften.getProgressDrawable().setColorFilter(Color.parseColor("#0DB4FF"), Mode.MULTIPLY);
		switch (context.getResources().getDisplayMetrics().widthPixels){
		case 1080:
			RIGHT_DEVIATION=context.getResources().getInteger(R.integer.manual_seekbar_right_deviation_xlarge);
			break;
		case 720:
			RIGHT_DEVIATION=context.getResources().getInteger(R.integer.manual_seekbar_right_deviation);
			break;
			default:
				RIGHT_DEVIATION=context.getResources().getInteger(R.integer.manual_seekbar_right_deviation);
				break;
			
		}
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return seekBarSoften.onTouchEvent(event);
	}
	public void setCameraManualModeManager(CameraManualModeManager mCameraManualModeManager){
		this.mCameraManualModeManager=mCameraManualModeManager;
	}
	
	@SuppressLint("NewApi")
	public void initData(int defaultValue,int minValues,int maxValues,final int mode){
//		this.context=context;
//		if(mode==GC.MODE_MANUAL_EXPOSURE){
//			this.maxValue=exposureTimeString.length-1;
//			this.minValue=0;
//			this.defaultValue=defaultValue;
//		}else 
		if(mode==GC.MODE_MANUAL_ISO){
			this.maxValue=isoString.length-1;
			this.minValue=0;
			this.defaultValue=defaultValue;
		}else{
			if(defaultValue<minValues){
			}else{
				this.defaultValue=minValue;
				this.defaultValue=defaultValue;
			}
			this.maxValue=maxValues;
			this.minValue=minValues;
			
		}
		seekBarSoften.setMax(maxValue-minValues);
		seekBarSoften.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				seekbarValue.setVisibility(View.GONE);
				seekBar.setThumb(getResources().getDrawable(R.drawable.seekbar_thumb));
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				mSeekBarWid=seekBar.getWidth()-RIGHT_DEVIATION;
				startAnimation(getPositionX());
				seekbarValue.setVisibility(View.VISIBLE);
//				if(mode==GC.MODE_MANUAL_EXPOSURE){
//					if(seekBar.getProgress()==0){
//						seekbarValue.setText("auto");
//					}else{
//						seekbarValue.setText(exposureTimeString[seekBar.getProgress()]);
//					}
//				}else 
				if(mode==GC.MODE_MANUAL_ISO){
					if(seekBar.getProgress()==0){
                        seekbarValue.setText(R.string.manualseekbar_up_auto);//Modify by zhiming.yu for PR936763
					}else{
						seekbarValue.setText(isoString[seekBar.getProgress()]);
					}
				}
				seekBar.setThumb(getResources().getDrawable(R.color.Settings_switch_color));
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
//				if(mode==GC.MODE_MANUAL_EXPOSURE){
//					if(progress==0){
//						seekbarValue.setText("auto");
//					}else{
//						seekbarValue.setText(exposureTimeString[progress]);
//					}
//					mCameraManualModeManager.saveManualProgressBarValues(progress+"");
//				}else 
				if(mode==GC.MODE_MANUAL_ISO){
					if(progress==0){
                        //seekbarValue.setText("auto");
                        seekbarValue.setText(R.string.manualseekbar_up_auto);//Modify by zhiming.yu for PR936763
					}else{
						seekbarValue.setText(isoString[progress]);
					}
					mCameraManualModeManager.saveManualProgressBarValues(progress+"");
				}else if(mode==GC.MODE_MANUAL_FOCUS_VALUE){
					if(progress==minValue){
                        //seekbarValue.setText("auto");
                        seekbarValue.setText(R.string.manualseekbar_up_auto);//Modify by zhiming.yu for PR936763
					}else if(progress==maxValue){
						seekbarValue.setText("∞");
					}else{
						seekbarValue.setText(progress+minValue+"");
					}
					mCameraManualModeManager.saveManualProgressBarValues(progress+minValue+"");
				}else{
					
					if(progress==0){
                        //seekbarValue.setText("auto");
                        seekbarValue.setText(R.string.manualseekbar_up_auto);//Modify by zhiming.yu for PR936763
					}else{
						seekbarValue.setText(progress+minValue+"");
					}
					mCameraManualModeManager.saveManualProgressBarValues(progress+minValue+"");
				}
				
				startAnimation(getPositionX());
			}
		});
		seekBarSoften.setProgress(defaultValue-minValue);
		if(defaultValue==minValue){
            //seekbarValue.setText("auto");
            seekbarValue.setText(R.string.manualseekbar_up_auto);//Modify by zhiming.yu for PR936763 
		}else{
			seekbarValue.setText(defaultValue+"");
		}
	}

//	
	@SuppressLint("NewApi")
	public void startAnimation(float positionX){
		Path path=new Path();
		path.moveTo(positionX,seekbarPosition_y);
		ObjectAnimator mAnimator = ObjectAnimator.ofFloat(seekbarValue, "x", "y", path);
	 	mAnimator.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				if(animation.isRunning()){
					animation.end();
				}
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
	 		mAnimator.setDuration(0);
			mAnimator.setStartDelay(0);
	        mAnimator.setInterpolator(new LinearInterpolator());
	        mAnimator.start();
	}
	
	private float getPositionX(){
		float positionX=((float)seekBarSoften.getProgress()/(maxValue-minValue))*mSeekBarWid;
		return positionX+LEFT_DEVIATION;
	}
	
	
	
	public void onWindowFocusChanged(boolean hasFocus) {
	    // TODO Auto-generated method stub
	    super.onWindowFocusChanged(hasFocus);
    	seekbarValue.getLocationOnScreen(locations);
	    seekbarPosition_y=seekbarValue.getY();
	    startAnimation(getPositionX());
	    }
	
	public void resetUI(){
		initData(0,0, 0, 0);
	}
}
