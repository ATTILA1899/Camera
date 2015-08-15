/******************************************************************************/
/*                                                                Date:05/2013*/
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/* Author :                                                                   */
/* Email  :                                                                   */
/* Role   :                                                                   */
/* Reference documents :                                                      */
/* -------------------------------------------------------------------------- */
/* Comments :                                                                 */
/* File     :                                                                 */
/* Labels   :                                                                 */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* ----------|----------------------|----------------------|----------------- */
/*    date   |        Author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.camera.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;

import com.tct.camera.R;

public class MaxLinearLayout extends LinearLayout {
    private static final String TAG = "MaxLinearLayout";
    private static final boolean LOG = true;

    private int mMaxHeight;
    private final int mMaxWidth;

    public MaxLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.MaxLinearLayout, 0, 0);
        mMaxHeight = a.getDimensionPixelSize(R.styleable.MaxLinearLayout_maxHeight, Integer.MAX_VALUE);
        mMaxWidth = a.getDimensionPixelSize(R.styleable.MaxLinearLayout_maxWidth, Integer.MAX_VALUE);
        if (LOG) {
            Log.i(TAG, "MaxLinearLayout() mMaxHeight=" + mMaxHeight + ", mMaxWidth=" + mMaxWidth);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(buildMeasureSpec(widthMeasureSpec, mMaxWidth),
                buildMeasureSpec(heightMeasureSpec, mMaxHeight));
    }

    private int buildMeasureSpec(int spec, int max) {
        int specMode = MeasureSpec.getMode(spec);
        int specSize = MeasureSpec.getSize(spec);
        specSize = max < specSize ? max : specSize;
        int result = MeasureSpec.makeMeasureSpec(specMode, specSize);
        return result;
    }
    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
    }
}
