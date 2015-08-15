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
/*  Comments : panorama ergo manager                                          */
/*  File     : tct_src/com/android/camera/manager/TctPanoramaProcessManager.- */
/*             java                                                           */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
package com.android.camera.manager;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;

import com.android.camera.TctAnimationController;
import com.android.camera.WideAnglePanoramaModule;
import com.android.camera.ui.TctProcessRectView;
import com.android.camera.ui.TctViewFinderView;
import com.tct.camera.R;

public class TctPanoramaProcessManager {
    public static final int DIRECTION_RIGHT = 0;
    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_UP = 2;
    public static final int DIRECTION_DOWN = 3;
    public static final int DIRECTION_UNKNOWN = 4;

    public static final int MSG_TOO_FAST = 10;
    public static final int MSG_MOVE_UP = 11;
    public static final int MSG_MOVE_DOWN = 12;

    public static final int MAIN_DIRECTION_ANGLE = 8;
    public static final int MAIN_IGNORE_ERROR_ANGLE = 20;

    public static final float SECOND_SHOW_OFFEST = 6;
    private static final float MAIN_SHOW_OFFEST = 50;
    private static final float SECOND_MAX_OFFEST = 60;
    private static final int SECOND_MAX_ANGLE = WideAnglePanoramaModule.DEFAULT_SWEEP_ANGLE >> 1;
    private int mSecondOffest;

    private int mDeviceOrientation;
    private WideAnglePanoramaModule mPanoramaActor;
    private TctAnimationController mTctAnimationController = null;

    private ImageView mTctArrowLView = null;
    private ImageView mTctArrowRView = null;
    private TctViewFinderView mViewFinderView = null;
    private TctProcessRectView mProcessRectView = null;
    private ImageView mStaticLineViewPort = null;
    private ImageView mStaticLineViewLand = null;
    private ImageView mStaticRectView = null;
    private ImageView mArrowOffestShowView = null;
    private ImageView mArrowForwardShowView = null;

    private TextView mTipsPromptV = null;
    private TextView mTipsPromptH = null;
    private TextView mTipsPrompt = null;

    private RelativeLayout.LayoutParams mTctArrowLViewLP = null;
    private RelativeLayout.LayoutParams mTctArrowRViewLP = null;
    private RelativeLayout.LayoutParams mViewFinderViewLP = null;
    private RelativeLayout.LayoutParams mProcessRectViewLP = null;
    private RelativeLayout.LayoutParams mStaticLineViewLP = null;
    private RelativeLayout.LayoutParams mStaticRectViewLP = null;
    private RelativeLayout.LayoutParams mArrowOffestShowViewLP = null;
    private RelativeLayout.LayoutParams mArrowForwardShowViewLP = null;
    private RelativeLayout.LayoutParams mTipsPromptLP = null;

    private float mProcess;
    private int mMaxProcess;
    private float mCurrentMaxProcess = 0.0f;

    private View mRootView;

    private String TAG = "YUHUAN_TctPanoramaProcessManager";

    private int mDirection;
    private int mSecondMaxOffest;

    private String mSlowDownString = null;
    private String mMoveUpString = null;
    private String mMoveDownString = null;
    private boolean isFast;

    public float getCurrentMaxProcess() {
        return mCurrentMaxProcess;
    }

    public WideAnglePanoramaModule getPanoramaActor() {
        return mPanoramaActor;
    }

    public TctPanoramaProcessManager(WideAnglePanoramaModule panoramaActor,
            TctViewFinderView tctPanoramaProcessView, View rootView) {
        this.mPanoramaActor = panoramaActor;
        this.mViewFinderView = tctPanoramaProcessView;
        this.mRootView = rootView;
    }

    public void init(int deviceOrientation) {/* TODO */
        Log.i(TAG,"init");
        mProcess = 0.0f;
        mCurrentMaxProcess = 0.0f;
        mDeviceOrientation = deviceOrientation;
        Log.d(TAG,"mDeviceOrientation :"+mDeviceOrientation);
        if(deviceOrientation % 180 == 0) {// Vertical
            mDirection = DIRECTION_RIGHT;
        }else{
            mDirection = DIRECTION_DOWN;
        }
        if(mSlowDownString == null)
           mSlowDownString = mPanoramaActor.getContext().getString(R.string.pano_slow_down_prompt);

        if(mMoveUpString == null)
           mMoveUpString = mPanoramaActor.getContext().getString(R.string.pano_move_up_prompt);

        if(mMoveDownString == null)
           mMoveDownString = mPanoramaActor.getContext().getString(R.string.pano_move_down_prompt);

        getView();
        getViewLP();
        SetViewParams();
        initView();
        initAnimation();
    }

    private void getView() {
        Log.i(TAG,"getView");
        if(mProcessRectView == null)
           mProcessRectView = (TctProcessRectView) mRootView
                .findViewById(R.id.pano_rect);

        if(mStaticLineViewPort == null)
           mStaticLineViewPort = (ImageView) mRootView
                .findViewById(R.id.tct_static_center_indicator_port);

        if(mStaticLineViewLand == null)
           mStaticLineViewLand = (ImageView) mRootView
                .findViewById(R.id.tct_static_center_indicator_land);

        if(mStaticRectView == null)
           mStaticRectView = (ImageView) mRootView
                .findViewById(R.id.tct_static_rect);

        if(mTctArrowLView == null)
           mTctArrowLView = (ImageView) mRootView
                .findViewById(R.id.tct_static_left_indicator);

        if(mTctArrowRView == null)
           mTctArrowRView = (ImageView) mRootView
                .findViewById(R.id.tct_static_right_indicator);

        if(mArrowOffestShowView == null)
           mArrowOffestShowView = (ImageView) mRootView
                .findViewById(R.id.tct_pano_arrows_down);

        if(mArrowForwardShowView == null)
           mArrowForwardShowView = (ImageView) mRootView
                .findViewById(R.id.tct_pano_arrows_right);

        if(mTipsPromptV == null)
           mTipsPromptV = (TextView) mRootView.findViewById(R.id.pano_capture_tips_textview_v);

        if(mTipsPromptH == null)
           mTipsPromptH = (TextView) mRootView.findViewById(R.id.pano_capture_tips_textview_h);
    }

    private void getViewLP() {
        Log.d(TAG,"getViewLP");

        if(mViewFinderViewLP == null)
           mViewFinderViewLP = (RelativeLayout.LayoutParams) mViewFinderView
                .getLayoutParams();

        if(mProcessRectViewLP == null)
           mProcessRectViewLP = (RelativeLayout.LayoutParams) mProcessRectView
                .getLayoutParams();

        //mStaticLineViewLP = (RelativeLayout.LayoutParams) mStaticLineView
        //        .getLayoutParams();
        if(mStaticRectViewLP == null)
           mStaticRectViewLP = (RelativeLayout.LayoutParams) mStaticRectView
                .getLayoutParams();

        if(mTctArrowLViewLP == null)
           mTctArrowLViewLP = (RelativeLayout.LayoutParams) mTctArrowLView
                .getLayoutParams();

        if(mTctArrowRViewLP == null)
           mTctArrowRViewLP = (RelativeLayout.LayoutParams) mTctArrowRView
                .getLayoutParams();

        if(mArrowForwardShowViewLP == null)
           mArrowForwardShowViewLP = (RelativeLayout.LayoutParams) mArrowForwardShowView
                .getLayoutParams();

        if(mArrowOffestShowViewLP == null)
           mArrowOffestShowViewLP = (RelativeLayout.LayoutParams) mArrowOffestShowView
                .getLayoutParams();
    }

    private void initView() {
        Log.i(TAG,"initView");
        mTipsPrompt = null;
        mArrowOffestShowView.setVisibility(View.INVISIBLE);
        mArrowForwardShowView.setVisibility(View.INVISIBLE);
        mArrowForwardShowView.setVisibility(View.GONE);
    }

    private void SetViewParams() {
        Log.i(TAG,"SetViewParams");
        TctParameter.init(mPanoramaActor.getContext(), mDirection);

        mMaxProcess = Math.max(TctParameter.Config.VIEWFINDER_WIDTH,
                TctParameter.Config.VIEWFINDER_HEIGHT);

        mSecondMaxOffest = Math.min(TctParameter.getDisplayHeight(),
                TctParameter.getDisplayWidth());

        mTctArrowLView.setRotation(TctParameter.Config.ARROWL_ROTATE);
        mTctArrowRView.setRotation(TctParameter.Config.ARROWR_ROTATE);

        mTctArrowLViewLP.leftMargin = TctParameter.Config.ARROWL_LEFT;
        mTctArrowLViewLP.topMargin = TctParameter.Config.ARROWL_TOP;
        mTctArrowLViewLP.width = TctParameter.Config.ARROWL_WIDTH;
        mTctArrowLViewLP.height = TctParameter.Config.ARROWL_HEIGHT;
        mTctArrowLView.setLayoutParams(mTctArrowLViewLP);

        mTctArrowRViewLP.leftMargin = TctParameter.Config.ARROWR_LEFT;
        mTctArrowRViewLP.topMargin = TctParameter.Config.ARROWR_TOP;
        mTctArrowRViewLP.width = TctParameter.Config.ARROWR_WIDTH;
        mTctArrowRViewLP.height = TctParameter.Config.ARROWR_HEIGHT;
        mTctArrowRView.setLayoutParams(mTctArrowRViewLP);

        mStaticRectViewLP.leftMargin = TctParameter.Config.STATIC_RECT_LEFT;
        mStaticRectViewLP.rightMargin = TctParameter.Config.STATIC_RECT_RIGHT;
        mStaticRectViewLP.topMargin = TctParameter.Config.STATIC_RECT_TOP;
        mStaticRectViewLP.width = TctParameter.Config.STATIC_RECT_WIDTH;
        mStaticRectViewLP.height = TctParameter.Config.STATIC_RECT_HEIGHT;
        mStaticRectView.setLayoutParams(mStaticRectViewLP);

        mViewFinderViewLP.leftMargin = TctParameter.Config.VIEWFINDER_LEFT;
        mViewFinderViewLP.topMargin = TctParameter.Config.VIEWFINDER_TOP;
        mViewFinderViewLP.width = TctParameter.Config.VIEWFINDER_WIDTH;
        mViewFinderViewLP.height = TctParameter.Config.VIEWFINDER_HEIGHT;
        mViewFinderView.setLayoutParams(mViewFinderViewLP);

        mProcessRectViewLP.height = TctParameter.Config.PROCESS_RECT;
        mProcessRectViewLP.width = mProcessRectViewLP.height;
        mProcessRectView.setLayoutParams(mProcessRectViewLP);

        mViewFinderView.init();
        mProcessRectView.init();
    }

    private void initAnimation() {
        Log.i(TAG,"initAnimation");
        if(mTctAnimationController == null)
           mTctAnimationController = new TctAnimationController(
                mArrowOffestShowView, mArrowForwardShowView,mTctArrowLView,mTctArrowRView);

        mTctAnimationController.startArrowAnimation();

    }

    public int setDirection(int progress, float x, float y) {
        int direction = DIRECTION_UNKNOWN;
        if (progress > MAIN_DIRECTION_ANGLE) {// r/d
            if (x > y) {// r
                direction = DIRECTION_RIGHT;
            } else {// d
                direction = DIRECTION_DOWN;
            }
        } else if (progress < -MAIN_DIRECTION_ANGLE) {// l/u
            if (x > y) {// u
                direction = DIRECTION_UP;
            } else {// l
                direction = DIRECTION_LEFT;
            }
        }
        setDirection(direction);
        return direction;
    }

    private void setDirection(int direction) {
        Log.i(TAG,"setDirection");
        switch (direction) {
        case DIRECTION_LEFT:
            mProcessRectView.setRotation(180);
            break;
        case DIRECTION_RIGHT:
            mProcessRectView.setRotation(0);
            break;
        case DIRECTION_UP:
            mProcessRectView.setRotation(-90);
            break;
        case DIRECTION_DOWN:
            mProcessRectView.setRotation(90);
            break;
        case DIRECTION_UNKNOWN:
            return;
        }

        mDirection = direction;
        mCurrentMaxProcess = 0.0f;
        SetViewParams();


        if(mDirection == DIRECTION_LEFT || mDirection == DIRECTION_RIGHT){
            mTipsPrompt=mTipsPromptV;
            mTipsPromptLP = (RelativeLayout.LayoutParams) mTipsPrompt
                .getLayoutParams();

            mTipsPromptLP.topMargin = mStaticRectViewLP.topMargin + mViewFinderViewLP.height + 30;
        }else{
            mTipsPrompt=mTipsPromptH;
            mTipsPromptLP = (RelativeLayout.LayoutParams) mTipsPrompt
                .getLayoutParams();

            mTipsPromptLP.leftMargin = mStaticRectViewLP.leftMargin - mViewFinderViewLP.width;
        }
        mTipsPrompt.setLayoutParams(mTipsPromptLP);
        mTipsPrompt.setRotation((360-mDeviceOrientation)%360);

        mTctAnimationController.stopArrowAnimation();
        mProcessRectView.setVisibility(View.VISIBLE);
        mStaticRectView.setVisibility(View.VISIBLE);
        mViewFinderView.setVisibility(View.VISIBLE);
        showCenterLine();
    }

    private void showCenterLine(){
        Log.i(TAG,"showCenterLine");

       if(mDirection == DIRECTION_LEFT || mDirection == DIRECTION_RIGHT){//v
           mStaticLineViewLP = (RelativeLayout.LayoutParams) mStaticLineViewPort.getLayoutParams();
           mStaticLineViewLP.width = TctParameter.Config.STATIC_RECT_WIDTH;
           mStaticLineViewPort.setLayoutParams(mStaticLineViewLP);

           mStaticLineViewPort.setVisibility(View.VISIBLE);
           mStaticLineViewLand.setVisibility(View.INVISIBLE);
       }else{
           mStaticLineViewLP = (RelativeLayout.LayoutParams) mStaticLineViewLand.getLayoutParams();
           // modify by minghui.hua for PR912018
           mStaticLineViewLP.height = TctParameter.Config.STATIC_RECT_HEIGHT-5;
           mStaticLineViewLP.topMargin = mStaticRectViewLP.topMargin+5;
           mStaticLineViewLand.setLayoutParams(mStaticLineViewLP);

           mStaticLineViewPort.setVisibility(View.INVISIBLE);
           mStaticLineViewLand.setVisibility(View.VISIBLE);
       }
    }

    private void hideCenterLine(){
        Log.i(TAG,"hideCenterLine");
        mStaticLineViewPort.setVisibility(View.INVISIBLE);
        mStaticLineViewLand.setVisibility(View.INVISIBLE);
    }

    private boolean checkValid(float mainAngle, float secondAngle){//PR1000919-sichao.hu modified , in this method returns true , it means the Panorama is not working properly
         if((Math.abs(mainAngle) > MAIN_IGNORE_ERROR_ANGLE && Math.abs(mSecondOffest) > SECOND_MAX_OFFEST)
            || (int)mainAngle == 0){
            return true;
         }

         return false;
    }
    public boolean setProcess(float mainAngle, float secondAngle) {/*TODO*/
        int[] coor = realTimeCalculateProcess(mainAngle, secondAngle);
        mProcessRectView.setProcess(coor, mDirection);

        if (checkValid(mainAngle,secondAngle)) {
            abortPanorama();
            return false;
        }

        if (Math.abs(mSecondOffest) > SECOND_SHOW_OFFEST) {
            showSecondAnimation(secondAngle);
        }else{
            mTctAnimationController.stopSecondAnimation();
            hideTipsIndication();
        }

        if (Math.abs(mCurrentMaxProcess) - Math.abs(mProcess) > MAIN_SHOW_OFFEST) {
            showMainAnimation();
        }else{
            mTctAnimationController.stopMainAnimation();
        }

        // update bitmap
        if (Math.abs(mProcess) > Math.abs(mCurrentMaxProcess)) {
            mCurrentMaxProcess = mProcess;
            return true;
        }
        return false;
    }

    private int[] realTimeCalculateProcess(float mainAngle, float secondAngle) {
        mProcess = mainAngle / WideAnglePanoramaModule.DEFAULT_SWEEP_ANGLE
                * mMaxProcess;
        int[] rect = new int[2];
        calculateSecond(mainAngle,secondAngle);
        switch (mDirection) {
        case DIRECTION_LEFT:
            rect[0] = -(mStaticRectViewLP.rightMargin - Math.round(mProcess));
            rect[1] = mStaticRectViewLP.topMargin
                    + mSecondOffest;
            break;
        case DIRECTION_RIGHT:
            rect[0] = mStaticRectViewLP.leftMargin + Math.round(mProcess);
            rect[1] = mStaticRectViewLP.topMargin
                    + mSecondOffest;
            break;
        case DIRECTION_UP:
            rect[0] = -(TctParameter.getDisplayHeight()
                    - mStaticRectViewLP.topMargin - mStaticRectViewLP.height - Math
                    .round(mProcess));
            rect[1] = TctParameter.getDisplayWidth() / 2
                    - mProcessRectViewLP.width / 2
                    + mSecondOffest;
            break;
        case DIRECTION_DOWN:
            rect[0] = mStaticRectViewLP.topMargin + Math.round(mProcess);
            rect[1] = TctParameter.getDisplayWidth() / 2
                    - mProcessRectViewLP.width / 2
                    + mSecondOffest;
            break;
        default:
            break;
        }

        return rect;
    }

    private void calculateSecond(float mainAngle,float secondAngle) {
        double rate= secondAngle / SECOND_MAX_ANGLE;
        double process = Math.abs(mainAngle) / WideAnglePanoramaModule.DEFAULT_SWEEP_ANGLE;
        //rate = rate * rate * rate * (4*(1.9-process*process*process));
        //rate =(Math.abs(rate)) > 0.9f ? 0.9f : rate;
        //mSecondOffest = (int)(rate * mSecondMaxOffest);//Math.round
        if(process<0.2){
            rate= rate*rate*rate * 0.55;
        }else if(process<0.4){
            rate= rate*rate*rate * 0.5;
        }else if(process<0.6){
            rate= rate*rate*rate * 0.4;
        }else if(process<0.8){
            rate= rate*rate*rate * 0.3;
        }else if(process<=1){
            rate= rate*rate*rate * 0.2;
        }
        mSecondOffest = (int)(rate * mSecondMaxOffest);//Math.round
    }

    private void abortPanorama() {
        Log.d(TAG,"savePanorama");
        /** TODO */
//        mPanoramaActor.stopCapture(false);
        mPanoramaActor.stopCapture(true);//PR1000919-sichao.hu added ,  need to abort pano saving in this case
    }

    private void showMainAnimation() {
        Log.d(TAG,"showMainAnimation");
        int arrowDirectioin = mDirection;

        int processRectViewMax = mProcessRectView.getWidth() > mProcessRectView
                .getHeight() ? mProcessRectView.getWidth() : mProcessRectView
                .getHeight();

        int processRectViewMin = mProcessRectView.getWidth() < mProcessRectView
                .getHeight() ? mProcessRectView.getWidth() : mProcessRectView
                .getHeight();

        int processRectMainMargin;
        int processRectSecondMargin;
        int arrowForwardMain;
        int arrowForwardSecond;
        int mainMargin;
        int secondMargin;

        int sOffest=0;
        int mOffest=0;

        if (mDirection == DIRECTION_LEFT || mDirection == DIRECTION_RIGHT) {
            processRectMainMargin = mProcessRectViewLP.leftMargin;
            processRectSecondMargin = mProcessRectViewLP.topMargin;
            arrowForwardMain = mArrowForwardShowView.getWidth();
            arrowForwardSecond = mArrowForwardShowView.getHeight();
        } else {
            processRectMainMargin = mProcessRectViewLP.topMargin;
            processRectSecondMargin = mProcessRectViewLP.leftMargin;
            arrowForwardMain = mArrowForwardShowView.getHeight();
            arrowForwardSecond = mArrowForwardShowView.getWidth();
        }

        if (mDirection == DIRECTION_LEFT || mDirection == DIRECTION_UP) {
            mainMargin = Math.round(processRectMainMargin
                    + (1 - TctParameter.Config.PROCESS_RECT_OFFEST_RATE)
                    * processRectViewMax - arrowForwardMain);
            secondMargin = processRectSecondMargin + (processRectViewMin) / 2
                    - arrowForwardSecond / 2;
        } else {
            mainMargin = Math.round(processRectMainMargin
                    + (0.1f + TctParameter.Config.PROCESS_RECT_OFFEST_RATE)
                    * processRectViewMax);

            secondMargin = Math.round(processRectSecondMargin
                    + (float) processRectViewMin / 2
                    - (float) arrowForwardSecond / 2);

        }
        switch(arrowDirectioin){
            case DIRECTION_LEFT:
                sOffest=-1;
                break;
            case DIRECTION_RIGHT:
                mOffest = -20;
                break;
            case DIRECTION_UP:
                mOffest = -20;
                break;
            case DIRECTION_DOWN:
                break;
        }
        if (mDirection == DIRECTION_LEFT || mDirection == DIRECTION_RIGHT) {
            mArrowForwardShowViewLP.leftMargin = mainMargin + mOffest;
            mArrowForwardShowViewLP.topMargin = secondMargin + sOffest;
        } else {
            mArrowForwardShowViewLP.topMargin = mainMargin + mOffest;
            mArrowForwardShowViewLP.leftMargin = secondMargin + sOffest;
        }
        mArrowForwardShowView.setLayoutParams(mArrowForwardShowViewLP);
        mTctAnimationController.startArrowForwardAnimation(arrowDirectioin);

    }

    private void showSecondAnimation(float secondAngle) {
        Log.d(TAG,"showSecondAnimation");
        int processRectViewMax = mProcessRectView.getWidth() > mProcessRectView
                .getHeight() ? mProcessRectView.getWidth() : mProcessRectView
                .getHeight();

        int processRectViewMin = mProcessRectView.getWidth() < mProcessRectView
                .getHeight() ? mProcessRectView.getWidth() : mProcessRectView
                .getHeight();

        int arrowDirectioin = DIRECTION_UNKNOWN;
        int processRectMainMargin;
        int processRectSecondMargin;
        int arrowOffestMain;
        int arrowOffestSecond;
        int mainMargin;
        int secondMargin;

        int sOffest = 0;
        int mOffest = 0;

        if (mDirection == DIRECTION_LEFT || mDirection == DIRECTION_RIGHT) {
            processRectMainMargin = mProcessRectViewLP.leftMargin;
            processRectSecondMargin = mProcessRectViewLP.topMargin;
            arrowOffestMain = mArrowOffestShowView.getWidth();
            arrowOffestSecond = mArrowOffestShowView.getHeight();

            if (secondAngle > 0) {
                arrowDirectioin = DIRECTION_UP;
                showTipsIndication(mDeviceOrientation == 0?MSG_MOVE_UP:MSG_MOVE_DOWN);
            } else {
                arrowDirectioin = DIRECTION_DOWN;
                showTipsIndication(mDeviceOrientation == 0?MSG_MOVE_DOWN:MSG_MOVE_UP);
            }
        } else {
            processRectMainMargin = mProcessRectViewLP.topMargin;
            processRectSecondMargin = mProcessRectViewLP.leftMargin;
            arrowOffestMain = mArrowOffestShowView.getHeight();
            arrowOffestSecond = mArrowOffestShowView.getWidth();

            if (secondAngle > 0) {
                arrowDirectioin = DIRECTION_LEFT;
                showTipsIndication(mDeviceOrientation == 270?MSG_MOVE_DOWN:MSG_MOVE_UP);
            } else {
                arrowDirectioin = DIRECTION_RIGHT;
                showTipsIndication(mDeviceOrientation == 270?MSG_MOVE_UP:MSG_MOVE_DOWN);
            }
        }

        if (mDirection == DIRECTION_LEFT || mDirection == DIRECTION_UP) {
            mainMargin = processRectMainMargin + processRectViewMax - arrowOffestMain;
        } else {
            mainMargin = processRectMainMargin;
        }

        if (secondAngle > 0) {
            secondMargin = processRectSecondMargin - arrowOffestSecond;
        } else {
            secondMargin = processRectSecondMargin + processRectViewMin;
        }

        switch(arrowDirectioin){
            case DIRECTION_LEFT:
                sOffest = 30;
                break;
            case DIRECTION_RIGHT:
                sOffest = -30;
                break;
            case DIRECTION_UP:
                break;
            case DIRECTION_DOWN:
                break;
        }

        if (mDirection == DIRECTION_LEFT || mDirection == DIRECTION_RIGHT) {
            mArrowOffestShowViewLP.leftMargin = mainMargin + mOffest;
            mArrowOffestShowViewLP.topMargin = secondMargin + sOffest;
        } else {
            mArrowOffestShowViewLP.topMargin = mainMargin + mOffest;
            mArrowOffestShowViewLP.leftMargin = secondMargin + sOffest;
        }

        mArrowOffestShowView.setLayoutParams(mArrowOffestShowViewLP);
        mTctAnimationController.startArrowOffestAnimation(arrowDirectioin);
    }

    public void showTipsIndication(int Msg) {
        if(mTipsPrompt == null){
           return;
        }
        switch(Msg){
            case MSG_TOO_FAST:
                 isFast = true;
                 mTipsPrompt.setText(mSlowDownString);
                 break;
            case MSG_MOVE_UP:
                 mTipsPrompt.setText(mMoveUpString);
                 break;
            case MSG_MOVE_DOWN:
                 mTipsPrompt.setText(mMoveDownString);
                 break;
        }
        mTipsPrompt.setVisibility(View.VISIBLE);
    }
    public void setIsFast(boolean value){
        isFast = value;
    }

    public void hideTipsIndication() {
        if(mTipsPrompt != null && !isFast){
           mTipsPrompt.setVisibility(View.GONE);
        }
    }


    public void reset() {
        isFast = false;
        hideTipsIndication();
        mTctArrowLView.setVisibility(View.INVISIBLE);
        mTctArrowRView.setVisibility(View.INVISIBLE);
        mStaticRectView.setVisibility(View.INVISIBLE);
        hideCenterLine();
        mViewFinderView.reset();
        mProcessRectView.reset();
        mTctAnimationController.reset();
    }

    public static Bitmap rotate(Bitmap b, int degrees, boolean isDeleteOld) {
        Bitmap rs = null;
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2,
                    (float) b.getHeight() / 2);
            try {
                rs = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(),
                        m, true);
                if (isDeleteOld) {
                    if (b != null) {
                        b.recycle();
                        b = null;
                    }
                }
            } catch (OutOfMemoryError ex) {
                ex.printStackTrace();
            }
        }
        return rs;
    }

}
