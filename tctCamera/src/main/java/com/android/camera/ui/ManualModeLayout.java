
package com.android.camera.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
//import android.content.res.TypedArray;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
//import android.view.View.MeasureSpec;
import com.tct.camera.R;

public class ManualModeLayout extends ViewGroup {
    private static final String TAG = "ManualModeLayout";
    
    private OnItemSelectedListener mOnItemSelectedListener = null;

    private int mSelectedPosId = 0;

    private int mChildWidth;
    private int mChildHeight;
    private final int MAX_CHILD_COUNT = 5;
    private final int LEFT_PADDING = 15;
    private final int RIGHT_PADDING = 15;
    // 0 - textview, 1 - imageview
    private int mCurrentViewType = 0;
    public static final int MANUAL_MODE_CIRCLE_TEXTVIEW_TYPE = 0;
    public static final int MANUAL_MODE_CIRCLE_IMAGEVIEW_TYPE = 1;
    // circle view type maybe have others, use the start and end var to clamp the type
    public static final int MANUAL_MODE_CIRCLE_VIEW_TYPE_START = MANUAL_MODE_CIRCLE_TEXTVIEW_TYPE;
    public static final int MANUAL_MODE_CIRCLE_VIEW_TYPE_END = MANUAL_MODE_CIRCLE_IMAGEVIEW_TYPE;
    
    
    public ManualModeLayout(Context context) {
        this(context, null);
    }

    public ManualModeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ManualModeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int densityDpi = (int) getResources().getDisplayMetrics().density;
//        mChildWidth = mChildHeight = (screenWidth - (LEFT_PADDING + RIGHT_PADDING) * densityDpi)
//                / MAX_CHILD_COUNT;
        mChildWidth = 115;
        mChildHeight=108;
    }

    public void setManualModeCircleViewType(int viewType) {
        if (viewType >= MANUAL_MODE_CIRCLE_VIEW_TYPE_START
                && viewType <= MANUAL_MODE_CIRCLE_VIEW_TYPE_END) {
            mCurrentViewType = viewType;
        } /*else {
            Log.e(TAG, "WARNING! the given viewtype=" + viewType
                    + "is not supproted ! SO, set the defalut type!");
            mCurrentViewType = MANUAL_MODE_CIRCLE_TEXTVIEW_TYPE;
        }*/
    }

    public View getSelectedItem() {
        return (mSelectedPosId >= 0) ? getChildAt(mSelectedPosId) : null;
    }

    public void initSelectedItem(String value, String entry) {
        for (int i = 0; i < getChildCount(); i ++ ) {
            View child = getChildAt(i);
            if(child instanceof ManualImageView) {
                ManualImageView ctv = (ManualImageView)child;
                if(value.equalsIgnoreCase(ctv.getName())) {
                    mSelectedPosId = i;
                    break;
                }
            }
//            else if (child instanceof ManualTextView){
//                ManualTextView ctv = (ManualTextView)child;
//                String name = ctv.getName();
//                if(name.indexOf("+") == 0) {
//                    name = name.substring(1, name.length());
//                }
//                if(entry.equalsIgnoreCase(name)) {
//                    mSelectedPosId = i;
//                    break;
//                }
//            }
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - mChildWidth
                * Math.min(getChildCount(), MAX_CHILD_COUNT)) / 2;
        int left = paddingLeft;
        int top =0;
        Log.i("ManualImage",TAG +" onLayout paddingLeft = " + paddingLeft + ", top = " + top + ", paddingRight = " + getPaddingRight());
        for (int i = 0; i < getChildCount(); i++) {
            ManualImageView child = (ManualImageView)getChildAt(i);
            child.layout(left, top, left + mChildWidth, top + mChildHeight);
            left += mChildWidth;
            if ((i + 1) % MAX_CHILD_COUNT == 0) {
                left = paddingLeft;
                top = top + mChildHeight;
            }
        }
        doOnLayout();
    }

    private void doOnLayout() {
        for (int i = 0; i < getChildCount(); i ++ ) {
//            if(mCurrentViewType == MANUAL_MODE_CIRCLE_TEXTVIEW_TYPE) {
//                final ManualTextView child = (ManualTextView) getChildAt(i);
//                if (child.getVisibility() == GONE) {
//                    continue;
//                }
//                child.setPosition(i);
//
//                if(mSelectedPosId != child.getPosition()) {
//                    mSelectedPosId = child.getPosition();
//
//
//                child.setOnClickListener(new OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        ManualTextView ctv = (ManualTextView)v;
//                        if (mOnItemSelectedListener != null) {
//                            mOnItemSelectedListener.onSelectionChange(ctv, ctv.getName(), ctv.getEntryValue());
//                        }
//                    }
//                });
//                }
//            } else {
                final ManualImageView child = (ManualImageView) getChildAt(i);
                if (child.getVisibility() == GONE) {
                    continue;
                }
                child.setPosition(i);
                if(mSelectedPosId != child.getPosition()) {
                    mSelectedPosId = child.getPosition();
                    
                    child.setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            ManualImageView ctv = (ManualImageView)v;
                            if (mOnItemSelectedListener != null) {
                                mOnItemSelectedListener.onSelectionChange(ctv, ctv.getName(), ctv.getName());
                            }
                        }
                    });
                }

            }
            
//        }
    
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        return false;
    }
    
    public interface OnItemSelectedListener {
        void onSelectionChange(View view, String name, String entryValue);
        void onSpecialRegionAngleUpdate(float angle, boolean clockwise);
    }

    public void setOnItemSelectedListener(
            OnItemSelectedListener onItemSelectedListener) {
        this.mOnItemSelectedListener = onItemSelectedListener;
    }

}
