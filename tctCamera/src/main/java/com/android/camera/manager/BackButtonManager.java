package com.android.camera.manager;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

import com.android.camera.CameraActivity;
import com.android.camera.GC;
import com.android.camera.ui.RotateImageView;
import com.jrdcamera.modeview.FloatingActionButton;
import com.jrdcamera.modeview.FloatingActionsMenu;
import com.jrdcamera.modeview.PolaroidView;
import com.jrdcamera.modeview.SettingPolaroid;
import com.tct.camera.R;

public class BackButtonManager extends ViewManager{
	
	private SettingPolaroid mCallPolaroid ; 

	public BackButtonManager(CameraActivity context) {
		super(context);
	}
	
	public BackButtonManager(CameraActivity context,int layer) {
		super(context,layer);
	}

	RotateImageView mSwitcherBack;
	FloatingActionButton mPolaroidOne;
	FloatingActionButton mPolaroidTwo ;
	FloatingActionButton mPolaroidFour;
	FloatingActionsMenu mPolaroidMenu;
	@Override
	protected View getView() {
		View view = inflate(R.layout.camera_back);
		mSwitcherBack=(RotateImageView)view.findViewById(R.id.camera_back_switch_icon);
		mPolaroidMenu=(FloatingActionsMenu)view.findViewById(R.id.multiple_actions);
		mPolaroidTwo =(FloatingActionButton)view.findViewById(R.id.polaroid_two);
		mPolaroidFour =(FloatingActionButton)view.findViewById(R.id.polaroid_four);
		mPolaroidOne =(FloatingActionButton)view.findViewById(R.id.polaroid_one);
		mPolaroidMenu.setIcon(R.drawable.polaroid_two ,PolaroidView.POLAR_TWO);
		return view;
	}
	
	@Override
	protected void onRefresh() {
		// TODO Auto-generated method stub
		if(mGC.getCurrentMode() == GC.MODE_POLAROID){
			mPolaroidMenu.setVisibility(View.VISIBLE);
			mSwitcherBack.setVisibility(View.GONE);
			mPolaroidOne.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mCallPolaroid.changePolarCount(PolaroidView.POLAR_ONE);
					mPolaroidMenu.toggle();
					mPolaroidMenu.setIcon(R.drawable.polaroid_one ,PolaroidView.POLAR_ONE);
				}
			});
			mPolaroidTwo.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mCallPolaroid.changePolarCount(PolaroidView.POLAR_TWO);
					mPolaroidMenu.toggle();
					mPolaroidMenu.setIcon(R.drawable.polaroid_two ,PolaroidView.POLAR_TWO);
					
				}
			});
			mPolaroidFour.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mCallPolaroid.changePolarCount(PolaroidView.POLAR_FOUR);
					mPolaroidMenu.toggle();
					mPolaroidMenu.setIcon(R.drawable.polaroid_four , PolaroidView.POLAR_FOUR);
				}
			});
		}else{
			mPolaroidMenu.setVisibility(View.GONE);
			mSwitcherBack.setVisibility(View.VISIBLE);
			mSwitcherBack.setImageResource(R.drawable.btn_shutter_back_normal);
	        mSwitcherBack.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					
					switch(event.getAction()){
					case MotionEvent.ACTION_DOWN:
						mSwitcherBack.setImageResource(R.drawable.btn_shutter_back_pressed);
						break;
					case MotionEvent.ACTION_UP:
						mSwitcherBack.setImageResource(R.drawable.btn_shutter_back_normal);
						mContext.onBackPressed();
			            
						break;
						default:
							break;
					}
					return true;
				}
			});
		}
		super.onRefresh();
	}
	
public void setCallPolaroid(SettingPolaroid mPolaroid){
	mCallPolaroid = mPolaroid;
}

public void collepsePolaroidMenu(){
	if(null != mPolaroidMenu && mPolaroidMenu.mExpanded){
		mPolaroidMenu.toggle();
	}
}

public boolean isInMenuArea(MotionEvent event){
	if(null == mPolaroidMenu){
		return false;
	}
	int[] location = new int[2];
	mPolaroidMenu.getLocationOnScreen(location);
	if(event.getX() > location[0] && event.getX() < (location[0]+mPolaroidMenu.getWidth()) && event.getY() > location[1] && event.getY() < (location[1]+mPolaroidMenu.getHeight())){
		return true;
	}else{
		return false;
	}
}
}


