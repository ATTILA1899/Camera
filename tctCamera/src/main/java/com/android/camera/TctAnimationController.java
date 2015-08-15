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
package com.android.camera;

import com.android.camera.manager.TctPanoramaProcessManager;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

public class TctAnimationController {
    private static final String TAG = "TctAnimationController";
    public static final int ANIM_DURATION = 180;
    private ImageView mArrowOffest;
    private ImageView mArrowForward;
    private ImageView mArrowL;
    private ImageView mArrowR;

    private Runnable mArrowOffestAnim = new Runnable() {
        public void run() {
            AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.0f);
            alpha.setDuration(ANIM_DURATION * 4);
            alpha.setRepeatCount(Animation.INFINITE);

            mArrowOffest.startAnimation(alpha);
            alpha.startNow();
        }
    };

    private Runnable mArrowForwardAnim = new Runnable() {
        public void run() {
            AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.0f);
            alpha.setDuration(ANIM_DURATION * 4);
            alpha.setRepeatCount(Animation.INFINITE);

            mArrowForward.startAnimation(alpha);
            alpha.startNow();
        }
    };

    private Runnable mArrowLAnim = new Runnable() {
        public void run() {
            AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.0f);
            alpha.setDuration(ANIM_DURATION * 8);
            alpha.setRepeatCount(Animation.INFINITE);

            mArrowL.startAnimation(alpha);
            alpha.startNow();
        }
    };

    private Runnable mArrowRAnim = new Runnable() {
        public void run() {
            AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.0f);
            alpha.setDuration(ANIM_DURATION * 8);
            alpha.setRepeatCount(Animation.INFINITE);

            mArrowR.startAnimation(alpha);
            alpha.startNow();
        }
    };

    private boolean mDownAnimationIsRunning = false;
    private boolean mRightAnimationIsRunning = false;

    public TctAnimationController(ImageView arrowOffest, ImageView arrowForward,ImageView arrowL,ImageView arrowR) {
        mArrowOffest = arrowOffest;
        mArrowForward = arrowForward;
        mArrowL=arrowL;
        mArrowR=arrowR;
    }

    public void startArrowAnimation(){
        mArrowL.setVisibility(View.VISIBLE);
        mArrowLAnim.run();
        mArrowR.setVisibility(View.VISIBLE);
        mArrowRAnim.run();
    }

    public void stopArrowAnimation(){
        mArrowL.clearAnimation();
        mArrowL.setVisibility(View.INVISIBLE);
        mArrowR.clearAnimation();
        mArrowR.setVisibility(View.INVISIBLE);
    }

    public void startArrowOffestAnimation(int direction) {
        if (mDownAnimationIsRunning) {
            return;
        }
        switch (direction) {
        case TctPanoramaProcessManager.DIRECTION_LEFT:
            mArrowOffest.setRotation(90);
            break;
        case TctPanoramaProcessManager.DIRECTION_RIGHT:
            mArrowOffest.setRotation(-90);
            break;
        case TctPanoramaProcessManager.DIRECTION_UP:
            mArrowOffest.setRotation(180);
            break;
        case TctPanoramaProcessManager.DIRECTION_DOWN:
            mArrowOffest.setRotation(0);
            break;
        default:
            break;
        }
        mArrowOffest.setVisibility(View.VISIBLE);
        mArrowOffestAnim.run();
        mDownAnimationIsRunning = true;
    }

    public void stopSecondAnimation() {
        mArrowOffest.clearAnimation();
        mDownAnimationIsRunning = false;
        mArrowOffest.setVisibility(View.INVISIBLE);
    }

    public void startArrowForwardAnimation(int direction) {
        if (mRightAnimationIsRunning) {
            return;
        }
        switch (direction) {
        case TctPanoramaProcessManager.DIRECTION_LEFT:
            mArrowForward.setRotation(90);
            break;
        case TctPanoramaProcessManager.DIRECTION_RIGHT:
            mArrowForward.setRotation(-90);
            break;
        case TctPanoramaProcessManager.DIRECTION_UP:
            mArrowForward.setRotation(180);
            break;
        case TctPanoramaProcessManager.DIRECTION_DOWN:
            mArrowForward.setRotation(0);
            break;
        default:
            break;
        }
        mArrowForward.setVisibility(View.VISIBLE);
        mArrowForwardAnim.run();
        mRightAnimationIsRunning = true;
    }

    public void stopMainAnimation() {
        mArrowForward.clearAnimation();
        mRightAnimationIsRunning = false;
        mArrowForward.setVisibility(View.INVISIBLE);
    }

    public void reset() {
        stopSecondAnimation();
        stopMainAnimation();
        stopArrowAnimation();
    }

}
