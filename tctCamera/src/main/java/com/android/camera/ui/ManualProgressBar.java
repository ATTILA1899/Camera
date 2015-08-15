package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.camera.manager.CameraManualModeManager;
import com.tct.camera.R;

public class ManualProgressBar extends ViewGroup {

	public final String TAG = "ManualProgress";
	public boolean isFirstRunning = true;
	public int mScreenWidth;
	public int mMenuTitleSize;
	public int mTextViewSize;
	public int mProgressbarHeight;
	public int mProgressbarWidth;
	public TextView mProgressText;
	public ProgressBar mProgressBar;

	private float mDownMotionX;
	protected float mLastMotionX;
	protected int mTouchState = TOUCH_STATE_REST;
	protected static final int INVALID_POINTER = -1;
	protected int mActivePointerId = INVALID_POINTER;
	protected final static int TOUCH_STATE_REST = 0;
	protected final static int TOUCH_STATE_SCROLLING = 1;
	protected float mLastMotionXRemainder;

	private String mDefaultValue;
	private String mManualTitle;
	private TextView mMenuTitle;

	private int mCurvalueIndex;
	private int mOldValueIndex;

	String[] mSupportList;
	String[] mSupportEntryList;

	public CameraManualModeManager mCameraManualModeManager;

	public ManualProgressBar(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public ManualProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public ManualProgressBar(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		mScreenWidth = getResources().getDisplayMetrics().widthPixels;
		mMenuTitleSize = dipToPx(context,75.0f);
		mTextViewSize = dipToPx(context,75.0f);
		mProgressbarHeight = dipToPx(context,75.0f);
	}

	public void initManualValue(String[] supportList, String[] supportEntryList) {
		mSupportList = supportList;
		mSupportEntryList = supportEntryList;
	}

	public void setManualDefaultValue(String value) {
		mDefaultValue = value;
	}

	public void setManualTitle(String title) {
		mManualTitle = title;
	}

	public void setCameraManualModeManager(
			CameraManualModeManager cameraManualModeManager) {
		mCameraManualModeManager = cameraManualModeManager;
	}

	private void initLayout() {
//		mMenuTitle = (TextView) findViewById(R.id.manual_menu_title);
//		mProgressText = (TextView) findViewById(R.id.progress_text);
//		mProgressBar = (ProgressBar) findViewById(R.id.progress_horizontal);
		mProgressBar.setMax(mProgressbarWidth);
		mMenuTitle.layout(0, 0, mMenuTitleSize, mMenuTitleSize);
		mMenuTitle.setText(mManualTitle);
		initDragViewTranslationDuringDrag(valueToProgress(mDefaultValue));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int parentWidth = getMeasuredWidth();
		int parentHeight = getMeasuredHeight();
		mProgressbarWidth = parentWidth - mTextViewSize - mMenuTitleSize;
		if (isFirstRunning)
			initLayout();
		mMenuTitle.layout(0, (parentHeight - mMenuTitleSize + 20),
				mMenuTitleSize, mMenuTitleSize
						+ (parentHeight - mMenuTitleSize) / 2);
		mMenuTitle.setGravity(Gravity.CENTER_HORIZONTAL);
		mProgressText.layout(mMenuTitleSize, (parentHeight - mMenuTitleSize)/2, mMenuTitleSize
				 + mTextViewSize, mMenuTitleSize + mTextViewSize);
		mProgressText.setGravity(Gravity.CENTER_HORIZONTAL);
		mProgressBar.layout(mMenuTitleSize, (parentHeight - mTextViewSize),
				mProgressbarWidth + mMenuTitleSize + mMenuTitleSize, mProgressbarHeight);
		isFirstRunning = false;
	}

	public void isTouchInTextView(int x, int y) {
		if (mProgressText.getTop() < y
				&& mProgressText.getBottom() > y
				&& mProgressText.getLeft() + mProgressText.getTranslationX() < x
				&& mProgressText.getRight() + mProgressText.getTranslationX() > x) {
			mTouchState = TOUCH_STATE_SCROLLING;
		} else {
			mTouchState = TOUCH_STATE_REST;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		final int action = event.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mDownMotionX = mLastMotionX = event.getX();
			mActivePointerId = event.getPointerId(0);
			mLastMotionXRemainder = 0;
			isTouchInTextView((int) mDownMotionX, (int) event.getY());
			break;
		case MotionEvent.ACTION_MOVE:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final int pointerIndex = event
						.findPointerIndex(mActivePointerId);
				if (pointerIndex == -1)
					return true;
				final float x = event.getX(pointerIndex);
				final float deltaX = mLastMotionX + mLastMotionXRemainder - x;
				mLastMotionX = x;
				mLastMotionXRemainder = deltaX - (int) deltaX;
				updateDragViewTranslationDuringDrag((int) deltaX);
			}
			break;
		case MotionEvent.ACTION_UP:
			mLastMotionXRemainder = 0;
			mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		return true;
	}

	public void initDragViewTranslationDuringDrag(int curProgress) {
		if (mProgressText != null) {
			mProgressText.setTranslationX(curProgress);
			mProgressBar.setProgress(curProgress);
			mProgressText.setText(getValue(curProgress));
		} else {
			initLayout();
		}
		mDownMotionX = mLastMotionX = curProgress;
	}

	void updateDragViewTranslationDuringDrag(int deltaX) {
		if (mProgressText != null) {
			int curProgress = mProgressBar.getProgress() - deltaX;
			mProgressText.setTranslationX(curProgress);
			mProgressBar.setProgress(curProgress);
			mProgressText.setText(getValue(curProgress));
			updateValueByProgress(curProgress);
		} else {
			initLayout();
		}
	}

	public void updateValueByProgress(int curProgress) {
		int level = mProgressBar.getMax() / mSupportList.length;
		mCurvalueIndex = curProgress / level;
		mCurvalueIndex = Math.min(mCurvalueIndex, mSupportEntryList.length - 1);
		if (mCurvalueIndex != mOldValueIndex && mCurvalueIndex >= 0) {
			mCameraManualModeManager.saveManualProgressBarValues(mSupportList[mCurvalueIndex]);
			mOldValueIndex = mCurvalueIndex;
		}
	}

	public String getValue(int progress) {
		if (mProgressBar == null) {
			initLayout();
		}
		int level = mProgressBar.getMax() / mSupportEntryList.length;
		int index = Math.min(progress / level, mSupportEntryList.length - 1);
		if (index < 0)
		    return mSupportEntryList[0];
		return mSupportEntryList[index];
	}

	public int valueToProgress(String value) {
		if (mProgressBar == null) {
			initLayout();
		}
		int index = 0;
		for (int i = 0; i < mSupportEntryList.length; i++) {
			String entryValue = mSupportEntryList[i];
			if(entryValue.startsWith("+")) {
				entryValue = entryValue.substring(1);
			}
			if (entryValue.equals(value)) {
				index = i;
				break;
			}
		}
		int level = mProgressBar.getMax() / mSupportEntryList.length;
		int progress = level * index;
		return progress;
	}

	public static int dipToPx(Context context, float dipValue) {
	    final float scale = context.getResources().getDisplayMetrics().density;
	    return (int) (dipValue * scale + 0.5f);
	    }
}
