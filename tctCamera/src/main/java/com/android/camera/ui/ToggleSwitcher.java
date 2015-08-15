package com.android.camera.ui;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import com.tct.camera.R;


public class ToggleSwitcher extends View {
	
	public interface IToggleSwitcherListener{
		public void toggleSwitched(boolean on);
	}

	private int mBtnOnSrc;
	private int mBtnOffSrc;
	private int mOnSrc;
	private int mOffSrc;
	private GestureDetector mDetector;
	private float mWidth;
	private float mHeight;
	private float mMetric;
	public ToggleSwitcher(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.toggle_switcher);
		mBtnOnSrc=a.getColor(R.styleable.toggle_switcher_buttonOnSrc,0xff48D1CC);
		mBtnOffSrc=a.getColor(R.styleable.toggle_switcher_buttonOffSrc,0xffffffff);
		mOnSrc=a.getColor(R.styleable.toggle_switcher_onSrc,0xff838B8B);//gray
		mOffSrc=a.getColor(R.styleable.toggle_switcher_offSrc,0xff458B74);//cyan
		mOn=a.getBoolean(R.styleable.toggle_switcher_defaultOn, false);
		a.recycle();

		mDetector=new  GestureDetector(context,new GestureListener());
		this.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(!mEnabled){
					return;
				}
				
				if(mIsAnimating){
					return;
				}
				ToggleSwitcher.this.setSwitcherOn(!ToggleSwitcher.this.isSwitcherOn());
			}
			
		});
	}
	
	
	private IToggleSwitcherListener mListener;
	public void setToggleSwitcherListener(IToggleSwitcherListener listener){
		mListener=listener;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mWidth=this.getWidth();
		mWidth=this.getMeasuredWidth();
		mHeight=this.getMeasuredHeight();
		mMetric=4*mWidth/ANIMATION_FRAME_RATE;
		setSwitcherOn(mOn);
	}
	
	private boolean mIsScrolling=false;
	class GestureListener extends SimpleOnGestureListener{

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			
			if(e2.getHistorySize()>0){
				distanceX=(e2.getX()-e2.getHistoricalX(0))*(-1);
			}
			mIsScrolling=true;
			float radius=ToggleSwitcher.this.getHeight()/2;
			float width=ToggleSwitcher.this.getWidth();
			mCenterX-=distanceX;
			if(mCenterX<radius+1){
				mCenterX=radius+1;
				mOn=false;
				if(mListener!=null){
					mListener.toggleSwitched(mOn);
				}
			}
			if(mCenterX>width-radius-1){
				mCenterX=width-radius-1;
				mOn=true;
				if(mListener!=null){
					mListener.toggleSwitched(mOn);
				}
			}
			invalidate();
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if(mOn){
				Message.obtain(mHandler,TOGGLE_ANIMATION_OFF).sendToTarget();
			}else{
				Message.obtain(mHandler,TOGGLE_ANIMATION_ON).sendToTarget();
			}
			invalidate();
			return super.onSingleTapUp(e);
		}

		
		
	}
	
	private final static int VIEW_STATE_STILL=0;
	private final static int VIEW_STATE_ANIMATING=VIEW_STATE_STILL+1;
	private final static int VIEW_STATE_DISABLE=VIEW_STATE_ANIMATING+1;
	private int mViewState=VIEW_STATE_STILL;
	
	
	
	private boolean mIsAnimating=false;
	
	private static final int ANIMATION_FRAME_RATE=20;
	private static final int TOGGLE_ANIMATION_ON=0x0711ffcc;
	private static final int TOGGLE_ANIMATION_OFF=TOGGLE_ANIMATION_ON+1;
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			mIsAnimating=true;
			float radius=ToggleSwitcher.this.getHeight()/2;
			if(msg.what==TOGGLE_ANIMATION_ON){
				mCenterX+=mMetric;
				if(mCenterX>mWidth-radius-1){
					mCenterX=mWidth-radius-1;
					mOn=true;
					if(mListener!=null){
						mListener.toggleSwitched(mOn);
					}
					invalidate();
					mIsAnimating=false;
					return;
				}
			}else{
				mCenterX-=mMetric;
				if(mCenterX<radius+1){
					mCenterX=radius+1;
					mOn=false;
					if(mListener!=null){
						mListener.toggleSwitched(mOn);
					}
					invalidate();
					mIsAnimating=false;
					return;
				}
			}
			invalidate();
			mHandler.sendMessageDelayed(Message.obtain(mHandler, msg.what), 1000/ANIMATION_FRAME_RATE);
		}
		
	};
	
	
	
	private boolean mEnabled=true;;
	@Override
	public void setEnabled(boolean enabled) {
		mEnabled=enabled;
		super.setEnabled(enabled);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(!mEnabled){
			return false;
		}
		
		if(mIsAnimating){
			return false;
		}
		
		mDetector.onTouchEvent(event);
		if(event.getAction()==MotionEvent.ACTION_UP||event.getAction()==MotionEvent.ACTION_CANCEL){
			if(mIsScrolling){
				mIsScrolling=false;
				if(mOn){
					if(mCenterX<mWidth*2/3){
						Message.obtain(mHandler, TOGGLE_ANIMATION_OFF).sendToTarget();
					}else{
						Message.obtain(mHandler, TOGGLE_ANIMATION_ON).sendToTarget();
					}
				}else{
					if(mCenterX>mWidth/3){
						Message.obtain(mHandler, TOGGLE_ANIMATION_ON).sendToTarget();
					}else{
						Message.obtain(mHandler, TOGGLE_ANIMATION_OFF).sendToTarget();
					}
				}
			}
		}
		return true;
	}
	

	
	
	private void setSwitcherOn(boolean on){
		mOn=on;
		if(mListener!=null){
			mListener.toggleSwitched(mOn);
		}
		float radius=mHeight/2;
		mCenterX=mOn?mWidth-radius-1:radius+1;
		invalidate();
	}
	
	public void setSwitcherState(boolean on){
		if(mOn!=on){
			mOn=on;
			if(mListener!=null){
				mListener.toggleSwitched(mOn);
			}
			invalidate();
		}
	}
	
	private float mCenterX;
	private boolean mOn;
	
	
	public boolean isSwitcherOn(){
		return mOn;
	}
	
	
	RectF leftRect=null;
	RectF rightRect=null;
	RectF offRect=null;
	RectF onRect=null;
	Paint onPaint=new Paint();
	Paint offPaint=new Paint();
	Paint btnPaint=new Paint();
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
//		float width=canvas.getWidth();
		float width=mWidth;
//		float height=canvas.getHeight();
		float height=mHeight;
		onPaint.setAntiAlias(true);
		onPaint.setColor(mOnSrc);
		onPaint.setAlpha(255);
		offPaint.setColor(mOffSrc);
		offPaint.setAlpha(255);
		offPaint.setAntiAlias(true);
		btnPaint.setAntiAlias(true);
		btnPaint.setColor(mOn?mBtnOnSrc:mBtnOffSrc);
		
		//0 degree maps to 3 o'clock clockwise
		//We define the arc sweep from 120 to 240 degree , 
		//so the width of the bound of the oval is half of the radius which is the height of this view 
		
		float radius=height*35f/100;
		float margin=(((float)height)/2)-radius;
		
		float marginLeft=2.0f;
		if(leftRect==null){
			leftRect=new RectF(marginLeft,margin,
					radius+radius+marginLeft,margin+radius+radius);
		}
		if(rightRect==null){
			rightRect=new RectF(width-radius-radius-marginLeft,margin,
					width-marginLeft,margin+radius+radius);
		}
		
		if(onRect==null){
			onRect=new RectF(radius+marginLeft,margin,mCenterX,height-margin);
		}
		onRect.set(radius+marginLeft,margin,mCenterX,margin+radius+radius);
		if(offRect==null){
			offRect=new RectF(mCenterX,margin,width-radius-marginLeft,height-margin);
		}
		offRect.set(mCenterX,margin,width-radius-marginLeft,margin+radius+radius);
//		canvas.drawArc(leftRect, 90, 180, false, offPaint);
//		canvas.drawArc(leftRect, 270f, 180f, false, offPaint);
		canvas.drawCircle(marginLeft+radius, margin+radius, radius, onPaint);
//		canvas.drawArc(rightRect, 270, 180, false, onPaint);
//		canvas.drawArc(rightRect, 90f, 180f, false, onPaint);
		canvas.drawCircle(width-marginLeft-radius, margin+radius, radius, offPaint);
		canvas.drawRect(onRect, onPaint);
		canvas.drawRect(offRect, offPaint);
		
		canvas.drawCircle(mCenterX, height/2, height/2, btnPaint);
		
	}
}

