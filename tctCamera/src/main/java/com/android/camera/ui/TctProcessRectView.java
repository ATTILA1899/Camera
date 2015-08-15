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

public class TctProcessRectView extends ImageView {
    private RelativeLayout.LayoutParams mLP;
    private final String TAG="TctProcessRectView";
    //private static final int LEFT_OFFSET=-2;
    //private static final int UP_OFFSET=-2;

    public TctProcessRectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void init() {
        mLP = (RelativeLayout.LayoutParams) this.getLayoutParams();
        mLP.rightMargin = -300;
        this.setLayoutParams(mLP);
        this.setVisibility(View.INVISIBLE);
        this.bringToFront();
    }

    public void reset() {
        this.setVisibility(View.INVISIBLE);
    }

    /**
     * coor[0] is main direction coor[1] is second direction
     *
     * @param coor
     */
    public void setProcess(int[] coor,int dirction) {
        switch (dirction) {
        case TctPanoramaProcessManager.DIRECTION_LEFT:
            mLP.leftMargin = (int) (TctParameter.getDisplayWidth() + coor[0]
                    - (1-TctParameter.Config.PROCESS_RECT_OFFEST_RATE)
                    * this.getWidth()/*+LEFT_OFFSET*/);
            if(mLP.leftMargin>TctParameter.getDisplayWidth()-this.getWidth()){
                mLP.leftMargin=TctParameter.getDisplayWidth()-this.getWidth();
            }
            mLP.topMargin = coor[1];
            break;
        case TctPanoramaProcessManager.DIRECTION_RIGHT:
            mLP.leftMargin = (int) (coor[0] - TctParameter.Config.PROCESS_RECT_OFFEST_RATE
                    * this.getWidth());
            if(mLP.leftMargin<0){
                mLP.leftMargin=0;
            }

            mLP.topMargin = coor[1];
            break;
        case TctPanoramaProcessManager.DIRECTION_UP:
            mLP.topMargin = (int) (TctParameter.getDisplayHeight()+ coor[0]
                    - (1-TctParameter.Config.PROCESS_RECT_OFFEST_RATE)
                    * this.getWidth()/*+UP_OFFSET*/);
            if(mLP.topMargin>TctParameter.getDisplayHeight()-this.getWidth()){
                mLP.topMargin=TctParameter.getDisplayHeight()-this.getWidth();
            }
            mLP.leftMargin = coor[1];
            break;
        case TctPanoramaProcessManager.DIRECTION_DOWN:
            mLP.topMargin = (int) (coor[0] - TctParameter.Config.PROCESS_RECT_OFFEST_RATE
                    * this.getWidth());
            if(mLP.topMargin<20){
                mLP.topMargin=20;
            }

            mLP.leftMargin = coor[1];
            break;
        default:
            break;
        }
        this.setLayoutParams(mLP);
    }

}
