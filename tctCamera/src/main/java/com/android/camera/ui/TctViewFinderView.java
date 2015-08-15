/******************************************************************************/
/*                                                               Date:09/2013 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  (hao.wang)                                                      */
/*  Email  :  hao.wang@tcl-mobile.com                                         */
/*  Role   :                                                                  */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments :                                                                */
/*  File     :                                                                */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
package com.android.camera.ui;

import com.android.camera.manager.TctPanoramaProcessManager;
import com.android.camera.manager.TctParameter;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class TctViewFinderView extends ImageView {
    private RelativeLayout.LayoutParams mLP;

    public TctViewFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init() {
        mLP = (RelativeLayout.LayoutParams) this.getLayoutParams();
        mLP.leftMargin = TctParameter.Config.VIEWFINDER_LEFT;
        mLP.topMargin = TctParameter.Config.VIEWFINDER_TOP;
        mLP.width = TctParameter.Config.VIEWFINDER_WIDTH;
        mLP.height = TctParameter.Config.VIEWFINDER_HEIGHT;

        this.setScaleType(ScaleType.FIT_XY);
        this.setVisibility(View.INVISIBLE);
        this.setImageBitmap(null);
        this.setLayoutParams(mLP);
    }

    public void reset() {
        this.setImageBitmap(null);
        this.setVisibility(View.INVISIBLE);
    }

    public void calculateProcess(int direction, float currentMaxProcess) {
        setProcess(direction, currentMaxProcess);
    }

    private void setProcess(int direction, float currentMaxProcess) {

        switch (direction) {
        case TctPanoramaProcessManager.DIRECTION_LEFT:
            mLP.width = Math.round(currentMaxProcess);
            mLP.leftMargin = TctParameter.getDisplayWidth()
                    - (TctParameter.getDisplayWidth()
                    - TctParameter.Config.VIEWFINDER_WIDTH - TctParameter.Config.VIEWFINDER_LEFT)
                    - mLP.width;
            break;
        case TctPanoramaProcessManager.DIRECTION_RIGHT:
            mLP.width = Math.round(currentMaxProcess);
            break;
        case TctPanoramaProcessManager.DIRECTION_UP:
            mLP.height = Math.round(currentMaxProcess);
            mLP.topMargin = TctParameter.getDisplayHeight()
                    - (TctParameter.getDisplayHeight()
                    - TctParameter.Config.VIEWFINDER_HEIGHT - TctParameter.Config.VIEWFINDER_TOP)
                    - mLP.height;
            break;
        case TctPanoramaProcessManager.DIRECTION_DOWN:
            mLP.height = Math.round(currentMaxProcess);
            break;
        default:
            break;
        }

        this.setLayoutParams(mLP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

}
