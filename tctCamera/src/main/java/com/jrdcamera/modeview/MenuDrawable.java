package com.jrdcamera.modeview;


import java.util.HashMap;
import java.util.List;

import com.android.camera.CameraActivity;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ui.RotateLayout;
import com.android.camera.util.CameraUtil;
import com.tct.camera.R;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Path;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;


public class MenuDrawable extends DrawerLayout{
	private static final String TAG = "MenuDrawable";
	private CameraActivity mContext;
	private ModeListView mModeListView;
	private ListViewItemView mListViewItemView;
	private DrawLayout mDrawLayout;
	public int touchItemId = -1;
	private int mOriginTouchItemId =-1;
	private final static int INITPOSITION=-300;//animation init position
	private final static int ANIMATION_DUR=100;
	private final static float NOT_DOCHANGE_ALPHA_DRAWER=0.3f;
	private final static float NOT_DOCHANGE_ALPHA_MODELISTVIEW=0.5f;

	private final static int delay=40;
	private boolean isDrawerOpen=false;
	
	
	public MenuDrawable(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@SuppressLint("ClickableViewAccessibility")
	public MenuDrawable(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub

		this.setDrawerListener(new DrawerListener() {
			
			@Override
			public void onDrawerStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onDrawerSlide(View arg0, float dis) {
				// TODO Auto-generated method stub
				//touchItemId do nothing ,others to start animation
				doAnimation(arg0);
				//setapha
				if(dis<NOT_DOCHANGE_ALPHA_DRAWER){
					arg0.setAlpha(NOT_DOCHANGE_ALPHA_DRAWER);
				}else{
					arg0.setAlpha(dis);
				}
				if(dis<NOT_DOCHANGE_ALPHA_MODELISTVIEW){
					mModeListView.setAlpha(0);
				}else{
					mModeListView.setAlpha((dis-NOT_DOCHANGE_ALPHA_MODELISTVIEW)*(1f/(1f-NOT_DOCHANGE_ALPHA_MODELISTVIEW)));
				}
				
			}
			
			@Override
			public void onDrawerOpened(View arg0) {
				// TODO Auto-generated method stub
				if(mContext.getCameraViewManager().getTopViewManager().mShowingFaceBeautyMenu){
					mContext.getCameraViewManager().getTopViewManager().hideFaceBeautyMenu();
				}
				if(mContext.getCameraViewManager().getTopViewManager().mShowingManualModel){
					mContext.getCameraViewManager().getCameraManualModeManager().hideManualMenu(mContext.getCameraViewManager().getTopViewManager().getViewForHideManualModeMenu());
				}
				isDrawerOpen =true;
			}
			
			@Override
			public void onDrawerClosed(View arg0) {
				// TODO Auto-generated method stub
				isDrawerOpen =false;
			}
		});
	}

	
	 @Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		 
		 switch(event.getAction()){
		 case MotionEvent.ACTION_DOWN:
			 
			 if(touchItemId != -1 && mOriginTouchItemId !=touchItemId){
				 mOriginTouchItemId = touchItemId;
			 }
			 touchItemId=mDrawLayout.getPosition(event.getY());
			 if(mOriginTouchItemId == -1){
				 mOriginTouchItemId = touchItemId;
			 }
			 
			 Log.d(TAG,"###YUHUAN###onInterceptTouchEvent#touchItemId=" + touchItemId);
			 mListViewItemView=(ListViewItemView) mModeListView.getChildAt(touchItemId);
			 if(!isDrawerOpen(Gravity.LEFT)){
				 initItemPosition();
				 //update ui
				 updateUI();
			 }
			 break;
			 default :
				 break;
		 }
		return super.onInterceptTouchEvent(event);
	}
	 

	 public void setDrawerLayout(CameraActivity mContext,DrawLayout mDrawLayout,ModeListView mModeListView){
		 this.mContext = mContext;
		 this.mModeListView=mModeListView;
		 this.mDrawLayout=mDrawLayout;
		 mModeListView.setContext(mContext);
	 }
	 
	 @SuppressLint("NewApi")
	private void startAnimator(final ListViewItemView view,Path path,int delay){
		 	ObjectAnimator mAnimator = ObjectAnimator.ofFloat(view, "x", null, path);
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
		 		mAnimator.setDuration(ANIMATION_DUR);
				mAnimator.setStartDelay(delay);
		        mAnimator.setInterpolator(new MenuDrawablerInterpolator(2.0f));
		        mAnimator.start();
	 }
	 
	 private void initItemPosition(){
		 for(int x=0;x<mModeListView.getChildCount();x++){
			 if(mOriginTouchItemId !=-1 && x!=mOriginTouchItemId){
				 Path pathTem=new Path();
				 pathTem.moveTo(INITPOSITION, mModeListView.getChildAt(x).getY());
				 startAnimator((ListViewItemView)mModeListView.getChildAt(x), pathTem, 0);
			 }
		 }
	 }
	 
	 public void setOrientation(int orientation,boolean animation){
		 for(int listItem=0;listItem<mModeListView.getChildCount();listItem++){
			 RotateImageView imageItem=(RotateImageView)(mModeListView.getChildAt(listItem)).findViewById(R.id.mode_image);
			 RotateLayout textParent=(RotateLayout)(mModeListView.getChildAt(listItem)).findViewById(R.id.mode_text_parent);
			 imageItem.setOrientation(orientation, true);
			 textParent.setOrientation(orientation, true);
			 
		 }
	 }
	 
	 
	 private void doAnimation(View view){
		 if(touchItemId>=0 && touchItemId <=mModeListView.getChildCount()-1){
				for(int x=0;x<mModeListView.getChildCount();){
					
					Path sTraversalPath_current = new Path();
					sTraversalPath_current.moveTo(mModeListView.getChildAt(touchItemId).getX(), mModeListView.getChildAt(touchItemId).getY());
					sTraversalPath_current.lineTo(view.getX(), mModeListView.getChildAt(touchItemId).getY());
					startAnimator((ListViewItemView)(mModeListView.getChildAt(touchItemId)),sTraversalPath_current,0);
					
					x++;
					
					int up=touchItemId+x;
					
					if(up<=mModeListView.getChildCount()-1){
					Path sTraversalPath = new Path();
					sTraversalPath.moveTo(mModeListView.getChildAt(up).getX(), mModeListView.getChildAt(up).getY());
					sTraversalPath.lineTo(view.getX(), mModeListView.getChildAt(up).getY());
					startAnimator((ListViewItemView)(mModeListView.getChildAt(up)),sTraversalPath,delay*x);
					}
					
					int down=touchItemId-x;
					if(down>=0){
					Path sTraversalPath = new Path();
					sTraversalPath.moveTo(mModeListView.getChildAt(down).getX(), mModeListView.getChildAt(down).getY());
					sTraversalPath.lineTo(view.getX(), mModeListView.getChildAt(down).getY());
					startAnimator((ListViewItemView)(mModeListView.getChildAt(down)),sTraversalPath,delay*x);
					}
				}
			}else{
				//donothing
			}
	 }
	 
	 private void updateUI(){
		 if(mModeListView!=null){
			 mModeListView.updateModeMenu();
		 }
	 }
	 public boolean isDrawerOpened(){
		 return isDrawerOpen;
	 }
}