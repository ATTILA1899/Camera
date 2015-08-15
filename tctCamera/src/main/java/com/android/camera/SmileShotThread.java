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
/* 11/14/2013|       hui.shen       |       CR-553221      |Add smile mode    */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.camera;

import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.util.Log;

import org.codeaurora.camera.ExtendedFace;

public class SmileShotThread extends Thread{

    private static final int SMILE_DEGREE = 52;
    private static final int BLINK_DEGREE = 25;
    private static final int SMILE_DEGREE_T = 20;
    private static final int BLINK_DEGREE_T = 35;
    private static final int MIN_SMILE_DEGREE = 10;
    //[FEATURE]-Mod-BEGIN by TCTNB(hui.shen), CR553221, add smile mode
    //private static final int COUNT_NUMBER = 2;
    private static final int COUNT_NUMBER = 3;
    //[FEATURE]-Mod-END by TCTNB(hui.shen), CR553221, add smile mode
    private int mCount = 0;

    private static SmileShotThread thread;
    private boolean stop;
    private Runnable mCallback;
    private Face[] mFace;
    private CameraActivity mActivity;
    private SmileShotThread(CameraActivity activity, Runnable callback){
        mActivity = activity;
        mCallback = callback;
    }
    public void run() {
        Log.i("LMC","SmileShotThread::Running...");
        try { Thread.sleep(500); } catch (Exception e) {}
        while(!stop) {
            try { Thread.sleep(50); } catch (Exception e) {}
            if (mFace != null && mFace.length > 0) {
                for(Face face : mFace) {
                    // [BUGFIX]-MOD-BEGIN by TCTNB.Shangyong.Zhang, 2013/08/16, PR486693, PR483054.
                    //if (mActivity.getCameraState() == CameraActivity.STATE_SWITCHING_CAMERA) {
                    //    stop = true;
                    //}
                    //one face is smiling.
                    if (face instanceof ExtendedFace) {
                        int smileDegree = ((ExtendedFace)face).getSmileDegree();
                        int leftEye = ((ExtendedFace)face).getLeftEyeBlinkDegree();
                        int rightEye = ((ExtendedFace)face).getRightEyeBlinkDegree();

                        if (smileDegree < MIN_SMILE_DEGREE) {
                            mCount = 0;
                            continue;
                        }
                        if ((smileDegree > SMILE_DEGREE && (leftEye > BLINK_DEGREE || rightEye > BLINK_DEGREE) && !stop)||
                            (smileDegree > SMILE_DEGREE_T &&(leftEye > BLINK_DEGREE_T || rightEye > BLINK_DEGREE_T) && !stop)) {
                            ++mCount;
                            Log.i("LMC","SmileShotThread::mCount = " + mCount);
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                            }
                            CameraInfo info = CameraHolder.instance().getCameraInfo()[mActivity.getCameraId()];
//                            if (info.facing == CameraInfo.CAMERA_FACING_FRONT
//                                /*|| mActivity.getCameraState() == CameraActivity.STATE_SWITCHING_CAMERA*/) {
//                                stop = true;
//                                break;
//                            }
                            if (mCount == COUNT_NUMBER) {
                                Log.i("LMC","SmileShotThread::call back!!");
                                mCount = 0;
                                stop = true;
                                mActivity.runOnUiThread(mCallback);
                                break;
                            }
                        }
                    }
                    // [BUGFIX]-MOD-END by TCTNB.Shangyong.Zhang.
                }
            } else {
                mCount = 0;
            }
        }
    }

    public synchronized static void detect(CameraActivity activity, Runnable callback) {
        Log.i("LMC","SmileShotThread::detect start!!");
        stopDetect();
        thread = new SmileShotThread(activity, callback);
        thread.start();
    }

    public static void stopDetect() {
        if (thread != null) {
            Log.i("LMC","SmileShotThread::thread != null stopDetect!!");
            thread.stop = true;
            thread = null;
        }
    }

    public synchronized static void updateFace(Face[] face) {
        if (thread != null) {
            synchronized(thread) {
                thread.mFace = face;
            }
        }
    }
}
