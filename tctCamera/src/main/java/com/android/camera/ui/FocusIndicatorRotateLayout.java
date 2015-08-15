/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.tct.camera.R;

// A view that indicates the focus area or the metering area.
public class FocusIndicatorRotateLayout extends RotateLayout implements FocusIndicator {
    // Sometimes continuous autofucus starts and stops several times quickly.
    // These states are used to make sure the animation is run for at least some
    // time.
    private int mState;
    private static final int STATE_IDLE = 0;
    private static final int STATE_FOCUSING = 1;
    private static final int STATE_FINISHING = 2;

    private static final int STOP_ANIMATION=1000;
    private static final int ANIMATION_DUR=2000;
    private static final int START_ANIMATION=3000;
    
    private Runnable mDisappear = new Disappear();
    private Runnable mEndAction = new EndAction();

    private static final int SCALING_UP_TIME = 130;
    private static final int SCALING_DOWN_TIME = 130;
    private static final int DISAPPEAR_TIMEOUT = 500;
    

    private AnimatorSet mAnimatorSet = new AnimatorSet();
    
    public FocusIndicatorRotateLayout(Context context, AttributeSet attrs) {
    	super(context, attrs);
        
    }

    private void setDrawable(int resid) {
        mChild.setBackgroundDrawable(getResources().getDrawable(resid));
        setPivotX(getWidth()/2);
        setPivotY(getHeight()/2);
    }
    @Override
    public void showStart() {
        Log.i("LMCH","showState1016 showStart mState:"+mState);
        if (mState != STATE_FOCUSING ) {
        	mState = STATE_FOCUSING;
        	mHandler.sendEmptyMessage(START_ANIMATION);
        }
    }
    
    private void startAnimation(){
    	if(FocusIndicatorRotateLayout.this.getVisibility()==GONE){
        	FocusIndicatorRotateLayout.this.setVisibility(VISIBLE);
        }
            setDrawable(R.drawable.ic_focus_focusing);
            if(mAnimatorSet !=null){
            	mAnimatorSet.cancel();
            }
            Animator mAnimatorA = ObjectAnimator.ofFloat(FocusIndicatorRotateLayout.this,"rotation", 0f, 600f);
            mAnimatorA.setInterpolator(new DecelerateInterpolator());
            mAnimatorA.setDuration(ANIMATION_DUR);
            
            Animator mAnimatorB = ObjectAnimator.ofFloat(FocusIndicatorRotateLayout.this,"rotation", 0f, 1800f);
            mAnimatorB.setInterpolator(new LinearInterpolator());
            mAnimatorB.setDuration(9000);
            mAnimatorB.setStartDelay(1000);
            
            mAnimatorSet.playTogether(mAnimatorA ,mAnimatorB );
            mAnimatorSet.start();
            
    }

    @Override
    public void showSuccess(boolean timeout) {
        Log.i("LMCH","showState1016 showSuccess mState:"+mState);
//        setDrawable(R.drawable.ic_focus_focused);
//        animate().withLayer().cancel();
        mState = STATE_FINISHING;
        mHandler.post(new Runnable(){
        	public void run(){
        		if (mAnimatorSet != null) {
        			mAnimatorSet.cancel();
                }
                mEndAction.run();
                if (mState == STATE_FOCUSING) {
////                    animate().withLayer().setDuration(SCALING_UP_TIME)
////                    .scaleX(1.4f).scaleY(1.4f);
//                    new Handler().postDelayed(new Runnable(){
//                        public void run(){
//                            setDrawable(R.drawable.ic_focus_focused);
//                            animate().withLayer().setDuration(SCALING_DOWN_TIME).scaleX(1f)
//                            .scaleY(1f).withEndAction(mEndAction);
//                        }
//                    },SCALING_UP_TIME);
                }
        	}
        });
        
    }

    @Override
    public void showFail(boolean timeout) {
    	mState = STATE_FINISHING;
    	mHandler.post(new Runnable(){
    		public void run(){
    			setDrawable(R.drawable.ic_focus_failed);
//    	        animate().withLayer().cancel();
    	        if (mAnimatorSet != null) {
    	        	mAnimatorSet.cancel();
    	        }
    	        mEndAction.run();
//    	        Log.i("LMCH","showState1016 showFail mState:"+mState);
    	        if (mState == STATE_FOCUSING) {
//    	            animate().withLayer().setDuration(SCALING_UP_TIME)
//    	            .scaleX(1.4f).scaleY(1.4f);
//    	            new Handler().postDelayed(new Runnable(){
//    	                public void run(){
//    	                    setDrawable(R.drawable.ic_focus_failed);
//    	                    animate().withLayer().setDuration(SCALING_DOWN_TIME).scaleX(1f)
//    	                            .scaleY(1f).withEndAction(mEndAction);
//    	                }
//    	            },SCALING_UP_TIME);
    	            
    	        }
    		}
    	});
        
    }

    @Override
    public void clear() {
//        animate().cancel();
    	mState=STATE_IDLE;
    	if (mAnimatorSet != null) {
    		mAnimatorSet.cancel();
    	}
        removeCallbacks(mDisappear);
        mDisappear.run();
//        if(getScaleX()!=1f){
//	        setScaleX(1f);
//	        setScaleY(1f);
//        }
    }

    private class EndAction implements Runnable {
        @Override
        public void run() {
            // Keep the focus indicator for some time.
            mHandler.postDelayed(mDisappear, DISAPPEAR_TIMEOUT);
        }
    }

    private class Disappear implements Runnable {
        @Override
        public void run() {
        	if(mState!=STATE_FOCUSING){
	        	FocusIndicatorRotateLayout.this.setVisibility(GONE);
	            mChild.setBackgroundDrawable(null);
	            mState = STATE_IDLE;
        	}
        }
    }

    public boolean isFocusing() {
        return mState != STATE_IDLE;
    }
    
    private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case START_ANIMATION:
				startAnimation();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
    	
    };
}
