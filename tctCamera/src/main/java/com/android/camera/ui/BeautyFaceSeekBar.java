package com.android.camera.ui;

import com.android.camera.PhotoModule;
import com.tct.camera.R;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class BeautyFaceSeekBar extends LinearLayout {
	
	private LayoutInflater mInflater;
	private View mView;
	private TextView seekbarValue;
	private SeekBar seekBar;
	private int mSeekBarWid=0;
	private float seekbarPosition_y=0;
	private int maxValue=100;
	private int mDefaultValue=0;
	private boolean isBright=false;
//	private BeautyFaceActivity context;
	private int[] locations = new int[2];
	private final static int LEFT_DEVIATION=10;
	private int RIGHT_DEVIATION=0;
	private   PhotoModule mPhotoMode;
	public final static int SOFTEN_TYPE=0;
	public final static int BRIGHT_TYPE=1;
	
	public BeautyFaceSeekBar(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}



	public BeautyFaceSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub

		mInflater = LayoutInflater.from(context);
		mView = mInflater.inflate(R.layout.beauty_face_seekar, this);
		seekbarValue = (TextView)mView.findViewById(R.id.seekbarvalue);
		seekBar = (SeekBar)mView.findViewById(R.id.seekbar);
//		seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#0DB4FF"), Mode.MULTIPLY);
		seekbarValue.setVisibility(View.GONE);
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

	
	
	public void initData(boolean isBrights,int defaultValue){
//		this.context=context;
		this.mDefaultValue=defaultValue;
		this.isBright=isBrights;
		seekBar.setMax(maxValue);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
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
				seekBar.setThumb(getResources().getDrawable(R.color.Settings_switch_color));
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				updateValues(progress);
				seekbarValue.setText(progress+"");
				startAnimation(getPositionX());
			}
		});
		
		seekBar.setProgress(mDefaultValue);
		updateValues(mDefaultValue);
		seekbarValue.setText(mDefaultValue+"");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return seekBar.onTouchEvent(event);
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
		float positionX=((float)seekBar.getProgress()/maxValue)*mSeekBarWid;
		return positionX+LEFT_DEVIATION;
	}
	
	
	public void onWindowFocusChanged(boolean hasFocus) {
	    // TODO Auto-generated method stub
	    super.onWindowFocusChanged(hasFocus);
    	seekbarValue.getLocationOnScreen(locations);
	    seekbarPosition_y=seekbarValue.getY();
	    startAnimation(getPositionX());
	    }
	
	public void setCallBack(PhotoModule calls){
		mPhotoMode=calls;
	}
	
	private void updateValues(int progress){
		if(mPhotoMode !=null){
			if(isBright){
				mPhotoMode.updateFaceBeautySetting(progress, BRIGHT_TYPE);
			}else{
				mPhotoMode.updateFaceBeautySetting(progress, SOFTEN_TYPE);
			}
		}
	}
}