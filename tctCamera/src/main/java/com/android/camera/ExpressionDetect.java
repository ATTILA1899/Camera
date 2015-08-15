/*******************************************************************************************************************/
/*                                                                                                                 */
/*           This material is company confidential, cannot be reproduced in any                                    */
/*           form without the written permission of JRD Communications, Inc.                                       */
/*                                                                                                                 */
/*=================================================================================================================*/
/*   Author :  Jianzhi Qiu                                                                                          */
/*   Role :    ImageDetect                                                                                         */
/*   Reference documents :                                                                                         */
/*=================================================================================================================*/
/* Comments :                                                                                                      */
/*     file    :  ../packages/apps/MusicWidget/src/com/jrdcom/musicwidget/MediaPlaybackService.java                */
/*     Labels  :                                                                                                   */
/*=================================================================================================================*/
/* Modifications   (month/day/year)                                                                                */
/*=================================================================================================================*/
/* date    | author       |FeatureID                                    |modification                              */
/*=========|==============|=============================================|==========================================*/
/*14/01/2013 | Jianzhi Qiu   |FR-222191-FL_MUSICWIDGET-001-Jianzhi Qiu  |                                          */
/*=========|==============|=============================================|==========================================*/
/*         |              |                                             |                                          */
/*=================================================================================================================*/
/* Problems Report(PR/CR)                                                                                          */
/*=================================================================================================================*/
/* date    | author       | PR #                                        |                                          */
/*=========|==============|=============================================|==========================================*/
/*         |              |                                             |                                          */
/*=================================================================================================================*/
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 07/23/2013|      long.chen       |        469710        |beauty4 snapshot  */
/*           |                      |                      |is too slow       */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/


package com.android.camera;

import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.graphics.RectF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.android.camera.util.CameraUtil;
import com.android.camera.CameraActivity;
import com.android.camera.CameraManager;

public class ExpressionDetect {
    public Context mContext;
    public Handler mHandler = null;
    public Size mPreviewSize;
    public int mSameCount = 0;
    public int mPreOrientation;
    public int mLastOrientation;
    public byte[] mPreFrameBytes;
    public CameraManager.CameraProxy mCameraDevice;
    public Rect mPreFace = new Rect();
    public RectF mTempFace = new RectF();
    public boolean mIsSavePreFrame = false;
    public boolean mIsCheckExpression = false;
    public boolean mIsFacialExpressionOk = false;

    //[BUGFIX]-Add-BEGIN by TCTNB.long.chen,07/23/2013,469710,
    //we need the preview's info to adjust to the face rect
    private int previewWidth;
    private int previewHeight;
    private CameraActivity mCamera = null;
    private ExpressionThread mExpressionThread = null;

    private final String TAG = "ExpressionDetect";
    //[BUGFIX]-Add-END by TCTNB.long.chen

    public void meLog(String text) {
        if (true) {
            Log.d(TAG, text);
        }
    }

    public ExpressionDetect(CameraManager.CameraProxy camera, Handler handler, Context context, int length) {
        mContext = context;
        mCameraDevice = camera;
        mPreFrameBytes = new byte[length];
        mHandler = handler;		
    }

    public ExpressionDetect(CameraManager.CameraProxy camera, Handler handler, CameraActivity context) {
        mContext = context;
        //[BUGFIX]-Add by TCTNB.long.chen,07/23/2013,469710,
        //remain CameraActivity to get preview's info
        mCamera = context;
        mHandler = handler;
        mCameraDevice = camera;
    }

    public ExpressionDetect(CameraManager.CameraProxy camera, CameraActivity context, Handler handler, ExpressionThread thread) {
        mContext = context;
        mCamera = context;
        mCameraDevice = camera;
        mExpressionThread = thread;
        mHandler = handler;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }
    //add by qjz for shot control 2013.2.26 begin
    public void setToCheckExpressionState() {
        mIsSavePreFrame = false;
        mIsCheckExpression = true;
    }

    public void setFacialExpressionOk() {
        mIsFacialExpressionOk = true;
    }
    //add by qjz for shot control 2013.2.26 end
    public void init() {
        //[BUGFIX]-Add-BEGIN by TCTNB.long.chen,07/23/2013,469710,get preview's info
        mPreviewSize = mCameraDevice.getParameters().getPreviewSize();
        previewHeight = mPreviewSize.width;//mCamera.getPreviewFrameWidth();
        previewWidth = mPreviewSize.height;//mCamera.getPreviewFrameHeight();
        //[BUGFIX]-Add-END by TCTNB.long.chen


        mSameCount = 0;
        mPreOrientation = 0;
        mIsSavePreFrame = false;

        //[BUGFIX]-Add by TCTNB.long.chen,07/23/2013,469710,
        //start expresss check to face change
        mIsCheckExpression = true;//false;    //add by qjz for shot control 2013.2.26
        mIsFacialExpressionOk = false; //add by qjz for shot control 2013.2.26
        meLog("ExpressionDetect init!");
    }

    public void initFrameBytes() {
        mIsSavePreFrame = false;
        int format = mCameraDevice.getParameters().getPreviewFormat();
        int bitsPerPixel = ImageFormat.getBitsPerPixel(format);
        mPreviewSize = mCameraDevice.getParameters().getPreviewSize();
        mPreFrameBytes = new byte[mPreviewSize.width * mPreviewSize.height * bitsPerPixel / 8];
    }

    public void getFrameByte(RectF[] faces, int orientation) {
        if (faces == null) {
            init();
            mTempFace = null;
            //meLog("zsy1029 getFrameByte::faces is null!");
        } else {
            mTempFace = faces[0];
            CameraUtil.dumpRect(mTempFace, "getFrameByte  mTempFace:");			
        }
        if (mPreFrameBytes == null) {
            initFrameBytes();
        }
        mLastOrientation = orientation;
//[BUGFIX]-Add-BEGIN by TCTNB.(hao.wang),09/06/2013,503665,
		if(mIsFacialExpressionOk){
			return;
		}
//[BUGFIX]-Add-END by TCTNB.(hao.wang)
        //meLog("zsy1029 getFrameByte::mCameraDevice.setOneShotPreviewCallback!");

        mCameraDevice.setOneShotPreviewCallback(mHandler, oneshotcallback/*ExpressionDetect.this*/);
    }

    public int number = 0;

    public boolean saveYUVFrame(byte[] bytes, String filePath) {
        try {
            //[BUGFIX]-Add by TCTNB.long.chen,07/23/2013,469710,for Debug
            //FileOutputStream out = new FileOutputStream("/mnt/sdcard/Yuv" + number + ".yuv");
            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();
            number++;
        } catch (Exception er) {
            meLog("Can't save the yuv picture because Error:" + er.toString());
            return false;
        }
        return true;
    }

    // facial expression
    public boolean YUVEqualsByFace(byte[] lastFrameBytes, RectF lastFace) {
        if (lastFace == null) {
            return false;
        }

        boolean isSame = false;
        Rect faceRect = new Rect(Math.round(lastFace.left), Math.round(lastFace.top),
                Math.round(lastFace.right), Math.round(lastFace.bottom));

        /*
         * meLog("AA faceRect.left = "+faceRect.left+" faceRect.right = "+faceRect
         * .right+"\n"
         * +" faceRect.top = "+faceRect.top+" faceRect.bottom = "+faceRect
         * .bottom+"\n"
         * +" faceRect.width = "+faceRect.width()+" faceRect.height = "
         * +faceRect.height()+"\n");
         */

        // Rect translation
        switch (mLastOrientation) {
            case 0:
                faceRect = new Rect(faceRect.top, mPreviewSize.height - faceRect.right,
                        faceRect.bottom, mPreviewSize.height - faceRect.left);
                break;
            case 90:
                if (faceRect.left > 0 && faceRect.right > 0
                        && faceRect.top > 0 && faceRect.bottom > 0) {
                    int left = mPreviewSize.height - faceRect.left;
                    int right = mPreviewSize.height - faceRect.right;
                    faceRect.left = faceRect.top;
                    faceRect.right = faceRect.bottom;
                    faceRect.top = left;
                    faceRect.bottom = right;
                } else if (faceRect.left > 0 && faceRect.right > 0
                        && faceRect.top < 0 && faceRect.bottom < 0) {
                    int left = mPreviewSize.width - faceRect.right;
                    int right = mPreviewSize.width - faceRect.left;
                    faceRect.left = mPreviewSize.width + faceRect.top;
                    faceRect.right = mPreviewSize.width + faceRect.bottom;
                    faceRect.top = left;
                    faceRect.bottom = right;
                } else if (faceRect.left < 0 && faceRect.right < 0
                        && faceRect.top > 0 && faceRect.bottom > 0) {
                    int left = mPreviewSize.width + faceRect.left;
                    int right = mPreviewSize.width + faceRect.right;
                    faceRect.left = mPreviewSize.width - faceRect.bottom;
                    faceRect.right = mPreviewSize.width - faceRect.top;
                    faceRect.top = left;
                    faceRect.bottom = right;
                } else if (faceRect.left < 0 && faceRect.right < 0 &&
                        faceRect.top < 0 && faceRect.bottom < 0) {
                    faceRect.left = mPreviewSize.width + faceRect.left;
                    faceRect.right = mPreviewSize.width + faceRect.right;
                    faceRect.top = mPreviewSize.width - Math.abs(faceRect.top);
                    faceRect.bottom = mPreviewSize.width - Math.abs(faceRect.bottom);
                }
                break;
            case 180:
                if (faceRect.left < 0 && faceRect.right < 0 &&
                        faceRect.top < 0 && faceRect.bottom < 0) {
                    int temp = Math.abs(faceRect.right);
                    faceRect.right = Math.abs(faceRect.left);
                    faceRect.left = temp;
                    temp = Math.abs(faceRect.top);
                    faceRect.top = Math.abs(faceRect.bottom);
                    faceRect.bottom = temp;
                }
                faceRect = new Rect(faceRect.top, mPreviewSize.height - faceRect.right,
                        faceRect.bottom, mPreviewSize.height - faceRect.left);
                break;
            case 270:
                if (faceRect.left < 0 && faceRect.right < 0
                        && faceRect.top < 0 && faceRect.bottom < 0) {
                    int left = mPreviewSize.height + faceRect.left;
                    int right = mPreviewSize.height + faceRect.right;
                    faceRect.left = Math.abs(faceRect.bottom);
                    faceRect.right = Math.abs(faceRect.top);
                    faceRect.top = left;
                    faceRect.bottom = right;
                } else if (faceRect.left < 0 && faceRect.right < 0
                        && faceRect.top > 0 && faceRect.bottom > 0) {
                    int temp = Math.abs(faceRect.left);
                    faceRect.left = Math.abs(faceRect.right);
                    faceRect.right = temp;
                    temp = mPreviewSize.height - faceRect.top;
                    faceRect.top = mPreviewSize.height - faceRect.bottom;
                    faceRect.bottom = temp;
                }
                break;
            default:
                faceRect = new Rect(faceRect.top, mPreviewSize.height - faceRect.right,
                        faceRect.bottom, mPreviewSize.height - faceRect.left);
                meLog("The orientation->" + mLastOrientation + " is wrong!");
                break;
        }

        /*
         * meLog("ZZ faceRect.left = "+faceRect.left+" faceRect.right = "+faceRect
         * .right+"\n"
         * +" faceRect.top = "+faceRect.top+" faceRect.bottom = "+faceRect
         * .bottom+"\n"
         * +" faceRect.width = "+faceRect.width()+" faceRect.height = "
         * +faceRect.height()+"\n" +" orientation = "+mLastOrientation);
         */
        if (mIsSavePreFrame && mPreOrientation == mLastOrientation) {
            meLog("1");
            int preBeginX = mPreFace.left;
            int preBeginY = mPreFace.top;
            int preWidth = mPreFace.width();
            int lastBeginX = faceRect.left;
            int lastBeginY = faceRect.top;
            int lastWidth = faceRect.width();
            int step = (int) Math.ceil(lastWidth / 16);// 6.25% step>=1

            if ((lastWidth == preWidth) || lastWidth / Math.abs(lastWidth - preWidth) > 16) {
                meLog("2");
                int count = 0;
                int diffValue = 16;
                boolean isNeedEquals = true;
                int width = lastWidth > preWidth ? preWidth : lastWidth;
                int height = width;
                int minX = lastBeginX - step >= 0 ? (-step) : (-lastBeginX);
                int maxX = 2 * step + minX;
                int minY = lastBeginY - step >= 0 ? (-step) : (-lastBeginY);
                int maxY = 2 * step + minY;
                for (int k = minX; k <= maxX; k++) {
                    if (Math.abs((int) lastFrameBytes[mPreviewSize.width * lastBeginY + lastBeginX
                            + k]
                            -
                            (int) mPreFrameBytes[mPreviewSize.width * preBeginY + preBeginX + k]) <= diffValue) {
                        count++;
                    }
                }
                if (count < 2 * step) {
                    meLog("3");
                    count = 0;
                    isNeedEquals = false;
                    for (int i = minY; i <= maxY; i++) {
                        for (int j = minX; j <= maxX; j++) {
                            if (Math.abs((int) lastFrameBytes[mPreviewSize.width * (lastBeginY + i)
                                    + lastBeginX + j]
                                    -
                                    (int) mPreFrameBytes[mPreviewSize.width * preBeginY + preBeginX]) <= diffValue) {
                                count = 0;
                                for (int k = j; k <= 2 * step + j; k++) {
                                    if (Math.abs((int) lastFrameBytes[mPreviewSize.width
                                            * (lastBeginY + i) + lastBeginX + j + k]
                                            -
                                            (int) mPreFrameBytes[mPreviewSize.width * preBeginY
                                                    + preBeginX + k]) <= diffValue) {
                                        count++;
                                    }
                                }
                                if (count >= 2 * step) {
                                    meLog("4");
                                    isNeedEquals = true;
                                    lastBeginY = lastBeginY + i;
                                    lastBeginX = lastBeginX + j;
                                    width = lastBeginX + width <= mPreviewSize.width ? width
                                            : (mPreviewSize.width - lastBeginX);
                                    height = lastBeginY + height <= mPreviewSize.height ? height
                                            : (mPreviewSize.height - lastBeginY);
                                    break;
                                }
                            }
                        }
                        if (count >= 2 * step) {
                            meLog("5");
                            break;
                        }
                    }
                }

                meLog("6");
                if (isNeedEquals) {
                    count = 0;
                    for (int i = 0; i < height; i++) {
                        for (int j = 0; j <= width; j++) {
                            if (Math.abs((int) lastFrameBytes[mPreviewSize.width * (lastBeginY + i)
                                    + lastBeginX + j]
                                    - (int) mPreFrameBytes[mPreviewSize.width * (preBeginY + i)
                                            + preBeginX + j]) <= diffValue) {
                                count++;
                            }
                        }
                    }

                    meLog("7 count:" + count + "  width * height:"
                            + (width * height));
                    if (count >= (width * height * 17 / 20)) {
                        mSameCount++;
                        isSame = true;
                        if (mSameCount >= 2) {
                            mSameCount = 0;
                            if (mExpressionThread != null) {
                                mExpressionThread.runCallbackOnUiThreadDirect();
                            }
                            //if (mHandler != null) {
                                //mHandler.sendEmptyMessage(ExpressionDetectModule.FACIAL_EXPRESSION_SHOT);
                            //}
                        }
                    }
                }
            }
        }

        if (!isSame) {
            mSameCount = 0;
        }
        meLog("8");
        mPreOrientation = mLastOrientation;
        mPreFrameBytes = lastFrameBytes;
        mIsSavePreFrame = true;
        mPreFace = faceRect;

        meLog("9");
        return isSame;
    }

    public boolean YUVEquals(byte[] lastFrameBytes, RectF lastFace) {
        if (lastFace == null || mIsFacialExpressionOk) {
            return false;
        }

        boolean isSame = false;
        int x = mPreviewSize.width / 4;
        int y = mPreviewSize.height / 5;
        Rect faceRect = new Rect(Math.round(lastFace.left),
                Math.round(lastFace.top), Math.round(lastFace.right),
                Math.round(lastFace.bottom));
        Rect equalRect = new Rect(x, y, x + mPreviewSize.width / 2, y
                + mPreviewSize.height * 3 / 5);

        if (mIsSavePreFrame && mPreOrientation == mLastOrientation) {
	    // Rect translation
	    switch (mLastOrientation) {
	        case 0:
	            faceRect = new Rect(faceRect.top, mPreviewSize.height - faceRect.right,
	                    faceRect.bottom, mPreviewSize.height - faceRect.left);
	            break;
	        case 90:
	            if (faceRect.left > 0 && faceRect.right > 0
	                    && faceRect.top > 0 && faceRect.bottom > 0) {
	                int left = mPreviewSize.height - faceRect.left;
	                int right = mPreviewSize.height - faceRect.right;
	                faceRect.left = faceRect.top;
	                faceRect.right = faceRect.bottom;
	                faceRect.top = left;
	                faceRect.bottom = right;
	            } else if (faceRect.left > 0 && faceRect.right > 0
	                    && faceRect.top < 0 && faceRect.bottom < 0) {
	                int left = mPreviewSize.width - faceRect.right;
	                int right = mPreviewSize.width - faceRect.left;
	                faceRect.left = mPreviewSize.width + faceRect.top;
	                faceRect.right = mPreviewSize.width + faceRect.bottom;
	                faceRect.top = left;
	                faceRect.bottom = right;
	            } else if (faceRect.left < 0 && faceRect.right < 0
	                    && faceRect.top > 0 && faceRect.bottom > 0) {
	                int left = mPreviewSize.width + faceRect.left;
	                int right = mPreviewSize.width + faceRect.right;
	                faceRect.left = mPreviewSize.width - faceRect.bottom;
	                faceRect.right = mPreviewSize.width - faceRect.top;
	                faceRect.top = left;
	                faceRect.bottom = right;
	            } else if (faceRect.left < 0 && faceRect.right < 0 &&
	                    faceRect.top < 0 && faceRect.bottom < 0) {
	                faceRect.left = mPreviewSize.width + faceRect.left;
	                faceRect.right = mPreviewSize.width + faceRect.right;
	                faceRect.top = mPreviewSize.width - Math.abs(faceRect.top);
	                faceRect.bottom = mPreviewSize.width - Math.abs(faceRect.bottom);
	            }
	            break;
	        case 180:
	            if (faceRect.left < 0 && faceRect.right < 0 &&
	                    faceRect.top < 0 && faceRect.bottom < 0) {
	                int temp = Math.abs(faceRect.right);
	                faceRect.right = Math.abs(faceRect.left);
	                faceRect.left = temp;
	                temp = Math.abs(faceRect.top);
	                faceRect.top = Math.abs(faceRect.bottom);
	                faceRect.bottom = temp;
	            }
	            faceRect = new Rect(faceRect.top, mPreviewSize.height - faceRect.right,
	                    faceRect.bottom, mPreviewSize.height - faceRect.left);
	            break;
	        case 270:
	            if (faceRect.left < 0 && faceRect.right < 0
	                    && faceRect.top < 0 && faceRect.bottom < 0) {
	                int left = mPreviewSize.height + faceRect.left;
	                int right = mPreviewSize.height + faceRect.right;
	                faceRect.left = Math.abs(faceRect.bottom);
	                faceRect.right = Math.abs(faceRect.top);
	                faceRect.top = left;
	                faceRect.bottom = right;
	            } else if (faceRect.left < 0 && faceRect.right < 0
	                    && faceRect.top > 0 && faceRect.bottom > 0) {
	                int temp = Math.abs(faceRect.left);
	                faceRect.left = Math.abs(faceRect.right);
	                faceRect.right = temp;
	                temp = mPreviewSize.height - faceRect.top;
	                faceRect.top = mPreviewSize.height - faceRect.bottom;
	                faceRect.bottom = temp;
	            }
	            break;
	        default:
	            faceRect = new Rect(faceRect.top, mPreviewSize.height - faceRect.right,
	                    faceRect.bottom, mPreviewSize.height - faceRect.left);
	            meLog("The orientation->" + mLastOrientation + " is wrong!");
	            break;
	    }

	    //modified by qjz for detect face 2013.2.20 begin
	    if (equalRect.left > faceRect.left && faceRect.left >= 0 && faceRect.left <= mPreviewSize.width) {
	        equalRect.left = faceRect.left;
	    }
	    if (equalRect.top > faceRect.top && faceRect.top >= 0 && faceRect.top <= mPreviewSize.height) {
	        equalRect.top = faceRect.top;
	    }
	    if (equalRect.right < faceRect.right && faceRect.right >= 0 && faceRect.right <= mPreviewSize.width) {
	        equalRect.right = faceRect.right;
	    }
	    if (equalRect.bottom < faceRect.bottom && faceRect.bottom >= 0 && faceRect.bottom <= mPreviewSize.height) {
	        equalRect.bottom = faceRect.bottom;
	    }
	     //modified by qjz for detect face 2013.2.20 end
            meLog("1");
            int beginX = equalRect.left;
            int beginY = equalRect.top;
            int preBeginX = equalRect.left;
            int preBeginY = equalRect.top;
            int width = equalRect.width();
            int height = equalRect.height();

            meLog("2");
            int count = 0;
            int diffValue = 16;
            boolean isNeedEquals = true;
            int step = (int) Math.ceil(equalRect.width() / 16);// 6.25% step>=1
            int xMin = -step;
            int xMax = step;
            int yMin = -step;
            int yMax = step;
            if (beginX -step  < 0) {
                xMin = -beginX;
                xMax = 2*step - xMin;
            }
            if (beginY - step < 0) {
                yMin = -beginY;
                yMax = 2*step - yMin;
            }
            for (int k = xMin; k <= xMax; k++) {
                if (Math.abs((int) lastFrameBytes[mPreviewSize.width * beginY
                        + beginX + k]
                        - (int) mPreFrameBytes[mPreviewSize.width * beginY
                                + beginX + k]) <= diffValue) {
                    count++;
                }
            }
            if (count < 2 * step) {
                meLog("3");
                count = 0;
                isNeedEquals = false;
                for (int i = yMin; i <= yMax; i++) {
                    for (int j = xMin; j <= xMax; j++) {
                        if (Math.abs((int) lastFrameBytes[mPreviewSize.width
                                * (beginY + i) + beginX + j]
                                - (int) mPreFrameBytes[mPreviewSize.width
                                        * preBeginY + preBeginX]) <= diffValue) {
                            count = 0;
                            for (int k = j; k <= 2 * step + j; k++) {
                                if (Math.abs((int) lastFrameBytes[mPreviewSize.width
                                        * (beginY + i) + beginX + j + k]
                                        - (int) mPreFrameBytes[mPreviewSize.width
                                                * preBeginY + preBeginX + k]) <= diffValue) {
                                    count++;
                                }
                            }
                            if (count >= 2 * step) {
                                meLog("4");
                                beginY = beginY + i;
                                beginX = beginX + j;
                                isNeedEquals = true;
                                width = beginX + width <= mPreviewSize.width ? width
                                        : (mPreviewSize.width - beginX);
                                height = beginY + height <= mPreviewSize.height ? height
                                        : (mPreviewSize.height - beginY);
                                preBeginX = preBeginX + width <= mPreviewSize.width ? width
                                        : (mPreviewSize.width - preBeginX);
                                preBeginY = preBeginY + height <= mPreviewSize.height ? height
                                        : (mPreviewSize.height - preBeginY);
                                break;
                            }
                        }
                    }
                    if (count >= 2 * step) {
                        meLog("5");
                        break;
                    }
                }
            }

            meLog("6");
            if (isNeedEquals) {
                count = 0;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j <= width; j+=2) {
                        //modified by qjz for detect face 2013.2.21 begin
                        try {
                            if (Math.abs((int) lastFrameBytes[mPreviewSize.width
                                    * (beginY + i) + beginX + j]
                                    - (int) mPreFrameBytes[mPreviewSize.width
                                            * (preBeginY + i) + preBeginX + j]) <= diffValue) {
                                count++;
                            }
                        } catch (Exception er) {
                            meLog("YUVEquals Error:" + er.toString());
                            return false;
                        }
                        //modified by qjz for detect face 2013.2.21 end
                    }
                }

                meLog("7 count:" + count*2 + "  width * height:" + (width * height));
                if (count >= (width * height * 17 / 20 / 2)) {
                    mSameCount++;
                    isSame = true;
                    if (mSameCount >= 2) {
                        mSameCount = 0;
                        mIsFacialExpressionOk = true;//add by qjz for shot control 2013.2.26
                        if (mExpressionThread != null) {
                            mExpressionThread.runCallbackOnUiThreadDirect();
                        }
                        //if (mHandler != null) {
                        //    mHandler.sendEmptyMessage(ExpressionDetectModule.FACIAL_EXPRESSION_SHOT);
                        //}
                    }
                }
            }
        }

        if (!isSame) {
            mSameCount = 0;
        }

        meLog("8");
        mPreOrientation = mLastOrientation;
        mPreFrameBytes = lastFrameBytes;
        mIsSavePreFrame = true;

        meLog("9");
        return isSame;
    }

    //[BUGFIX]-Add-BEGIN by TCTNB.long.chen,07/23/2013,469710,for Debug
    private void printRectLog(Rect rect, String flag) {
        Log.i(TAG, "printRectLog: "+flag+"("+rect.left+","+rect.top+","+rect.right+","+rect.bottom+")");
    }

    private void showRectDetectRect(byte[] frameBytes, Rect rect, String flag) {
        int block = 1;
        for(int i = rect.top-1; i < rect.bottom+1; i++) {
            for(int j = 0; j < block; j++) {
                frameBytes[mPreviewSize.width*i + j + rect.left] = 0;
            }
        }
        for(int i = rect.left-1; i < rect.right+1; i++) {
            for(int j = 0; j < block; j++) {
                frameBytes[mPreviewSize.width*(rect.top+j) +  i] = 0;
            }
        }
        for(int i = rect.left-1; i < rect.right+1; i++) {
            for(int j = 0; j < block; j++) {
                frameBytes[mPreviewSize.width*(rect.bottom+j) +  i] = 0;
            }
        }
        for(int i = rect.top-1; i < rect.bottom+1; i++) {
            for(int j = 0; j < block; j++) {
                frameBytes[mPreviewSize.width*i + j + rect.right] = 0;
            }
        }

        saveYUVFrame(frameBytes, "/mnt/sdcard/" + flag  + rect.left + "_" + rect.top + "_" + rect.right + "_" + rect.bottom + "_" + number + ".yuv");
    }
    //[BUGFIX]-Add-END by TCTNB.long.chen

    public boolean YUVCheckExpressionChange(byte[] lastFrameBytes, RectF lastFace) {
//[BUGFIX]-Mod-BEGIN by TCTNB.(hao.wang),09/06/2013,503665,
        if (lastFace == null || mIsFacialExpressionOk) {
            return false;
        }
//[BUGFIX]-Mod-END by TCTNB.(hao.wang)
        int x = mPreviewSize.width / 5;//[BUGFIX]-Add by TCTNB.Shangyong.Zhang 03/12/2014, 607040.
        int y = mPreviewSize.height / 6;//[BUGFIX]-Add by TCTNB.Shangyong.Zhang 03/12/2014, 607040.
        Rect faceRect = new Rect(Math.round(lastFace.left),
                Math.round(lastFace.top), Math.round(lastFace.right),
                Math.round(lastFace.bottom));
        Rect equalRect = new Rect(x, y, x + mPreviewSize.width * 3 / 5, y
                + mPreviewSize.height * 4 / 6);//[BUGFIX]-Add by TCTNB.Shangyong.Zhang 03/12/2014, 607040.

        if (mIsSavePreFrame  && mIsCheckExpression && mPreOrientation == mLastOrientation) {
            //[BUGFIX]-Add-BEGIN by TCTNB.long.chen,07/23/2013,469710,
            //compute to width and heigth scale to zoom the face rect
            float widthScale = (float)mPreviewSize.width / (float)(previewWidth);
            float heightScale = (float)mPreviewSize.height / (float)(previewHeight);
            //[BUGFIX]-Add-END by TCTNB.long.chen
        // Rect translation
            switch (mLastOrientation) {
                case 0:
                        //[BUGFIX]-Add-BEGIN by TCTNB.long.chen,07/23/2013,469710,the rect need reset
                        /*faceRect = new Rect(faceRect.top, mPreviewSize.height - faceRect.right,
                        faceRect.bottom, mPreviewSize.height - faceRect.left);*/

                        faceRect.bottom *= widthScale;
                        faceRect.top *= widthScale;
                        faceRect.right *= heightScale;
                        faceRect.left *= heightScale;
                        faceRect = new Rect(mPreviewSize.width - faceRect.bottom, mPreviewSize.height - faceRect.right,
                                mPreviewSize.width - faceRect.top, mPreviewSize.height -faceRect.left);
                        //[BUGFIX]-Add-END by TCTNB.long.chen
                    break;
                case 90:
                    if (faceRect.left > 0 && faceRect.right > 0
                            && faceRect.top > 0 && faceRect.bottom > 0) {
                        int left = mPreviewSize.height - faceRect.left;
                        int right = mPreviewSize.height - faceRect.right;
                        faceRect.left = faceRect.top;
                        faceRect.right = faceRect.bottom;
                        faceRect.top = left;
                        faceRect.bottom = right;
                    } else if (faceRect.left > 0 && faceRect.right > 0
                            && faceRect.top < 0 && faceRect.bottom < 0) {
                        int left = mPreviewSize.width - faceRect.right;
                        int right = mPreviewSize.width - faceRect.left;
                        faceRect.left = mPreviewSize.width + faceRect.top;
                        faceRect.right = mPreviewSize.width + faceRect.bottom;
                        faceRect.top = left;
                        faceRect.bottom = right;
                    } else if (faceRect.left < 0 && faceRect.right < 0
                            && faceRect.top > 0 && faceRect.bottom > 0) {
                        int left = mPreviewSize.width + faceRect.left;
                        int right = mPreviewSize.width + faceRect.right;
                        faceRect.left = mPreviewSize.width - faceRect.bottom;
                        faceRect.right = mPreviewSize.width - faceRect.top;
                        faceRect.top = left;
                        faceRect.bottom = right;
                    } else if (faceRect.left < 0 && faceRect.right < 0 &&
                            faceRect.top < 0 && faceRect.bottom < 0) {
                        //[BUGFIX]-Add-BEGIN by TCTNB.long.chen,07/23/2013,469710,the rect need reset
                        /*faceRect.left = mPreviewSize.width + faceRect.left;
                        faceRect.right = mPreviewSize.width + faceRect.right;
                        faceRect.top = mPreviewSize.width - Math.abs(faceRect.top);
                        faceRect.bottom = mPreviewSize.width - Math.abs(faceRect.bottom);*/

                        //adjust face rect
                        faceRect.bottom *= widthScale;
                        faceRect.top *= widthScale;
                        faceRect.right *= heightScale;
                        faceRect.left *= heightScale;

                        faceRect = new Rect(mPreviewSize.width - Math.abs(faceRect.top), mPreviewSize.height - Math.abs(faceRect.left),
                                mPreviewSize.width - Math.abs(faceRect.bottom), mPreviewSize.height - Math.abs(faceRect.right));
                        //[BUGFIX]-Add-END by TCTNB.long.chen
                    }
                    break;
                case 180:
                    if (faceRect.left < 0 && faceRect.right < 0 &&
                            faceRect.top < 0 && faceRect.bottom < 0) {
                        int temp = Math.abs(faceRect.right);
                        faceRect.right = Math.abs(faceRect.left);
                        faceRect.left = temp;
                        temp = Math.abs(faceRect.top);
                        faceRect.top = Math.abs(faceRect.bottom);
                        faceRect.bottom = temp;
                    }
                    //[BUGFIX]-Add-BEGIN by TCTNB.long.chen,07/23/2013,469710,the rect need reset
                    /*faceRect = new Rect(faceRect.top, mPreviewSize.height - faceRect.right,
                            faceRect.bottom, mPreviewSize.height - faceRect.left);*/

                     //adjust face rect
                     faceRect.bottom *= widthScale;
                     faceRect.top *= widthScale;
                     faceRect.right *= heightScale;
                     faceRect.left *= heightScale;
                     faceRect = new Rect(mPreviewSize.width - faceRect.bottom, mPreviewSize.height - faceRect.right,
                            mPreviewSize.width - faceRect.top, mPreviewSize.height - faceRect.left);
                     //[BUGFIX]-Add-END by TCTNB.long.chen
                    break;
                case 270:
                    if (faceRect.left < 0 && faceRect.right < 0
                            && faceRect.top < 0 && faceRect.bottom < 0) {
                        //[BUGFIX]-Add-BEGIN by TCTNB.long.chen,07/23/2013,469710,the rect need reset
                        /*int left = mPreviewSize.height + faceRect.left;
                        int right = mPreviewSize.height + faceRect.right;
                        faceRect.left = Math.abs(faceRect.bottom);
                        faceRect.right = Math.abs(faceRect.top);
                        faceRect.top = left;
                        faceRect.bottom = right;*/

                        //adjust face rect
                        faceRect.bottom *= widthScale;
                        faceRect.top *= widthScale;
                        faceRect.right *= heightScale;
                        faceRect.left *= heightScale;

                        faceRect = new Rect(mPreviewSize.width - Math.abs(faceRect.top), mPreviewSize.height - Math.abs(faceRect.left),
                            mPreviewSize.width - Math.abs(faceRect.bottom), mPreviewSize.height - Math.abs(faceRect.right));
                        //[BUGFIX]-Add-END by TCTNB.long.chen
                    } else if (faceRect.left < 0 && faceRect.right < 0
                            && faceRect.top > 0 && faceRect.bottom > 0) {
                        int temp = Math.abs(faceRect.left);
                        faceRect.left = Math.abs(faceRect.right);
                        faceRect.right = temp;
                        temp = mPreviewSize.height - faceRect.top;
                        faceRect.top = mPreviewSize.height - faceRect.bottom;
                        faceRect.bottom = temp;
                    }
                    break;
                default:
                    faceRect = new Rect(faceRect.top, mPreviewSize.height - faceRect.right,
                            faceRect.bottom, mPreviewSize.height - faceRect.left);
                    meLog("The orientation->" + mLastOrientation + " is wrong!");
                    break;
            }

        //[BUGFIX]-Add-BEGIN by TCTNB.long.chen,07/23/2013,469710,
        //Avoid the out of bounds through check the faceRect
            //modified by qjz for detect face 2013.2.20 begin
        /*if (equalRect.left > faceRect.left && faceRect.left >= 0 && faceRect.left <= mPreviewSize.width) {
            equalRect.left = faceRect.left;
        }
        if (equalRect.top > faceRect.top && faceRect.top >= 0 && faceRect.top <= mPreviewSize.height) {
            equalRect.top = faceRect.top;
        }
        if (equalRect.right < faceRect.right && faceRect.right >= 0 && faceRect.right <= mPreviewSize.width) {
            equalRect.right = faceRect.right;
        }
        if (equalRect.bottom < faceRect.bottom && faceRect.bottom >= 0 && faceRect.bottom <= mPreviewSize.height) {
            equalRect.bottom = faceRect.bottom;
        }*/

        //chekc rect to avoid out of bounds
        if(faceRect.left >= 0 && faceRect.left <= mPreviewSize.width) {
            equalRect.left = faceRect.left;
        } else if(faceRect.left < 0) {
                equalRect.left = 0;
        }
        if(faceRect.top >= 0 && faceRect.top <= mPreviewSize.height) {
            equalRect.top = faceRect.top;
            } else if(faceRect.top < 0) {
                equalRect.top = 0;
        }
        if(faceRect.right >= 0 && faceRect.right <= mPreviewSize.width) {
            equalRect.right = faceRect.right;
            } else if(faceRect.right < 0) {
                equalRect.right = 0;
        }
        if(faceRect.bottom >= 0 && faceRect.bottom <= mPreviewSize.height) {
            equalRect.bottom = faceRect.bottom;
            } else if(faceRect.left < 0) {
                equalRect.bottom = 0;
        }
            //modified by qjz for detect face 2013.2.20 end
        //[BUGFIX]-Add-END by TCTNB.long.chen
            meLog("1");
            int beginX = equalRect.left;
            int beginY = equalRect.top;
            int preBeginX = equalRect.left;
            int preBeginY = equalRect.top;
            int width = equalRect.width();
            int height = equalRect.height();

            meLog("2");
            int count = 0;

            //[BUGFIX]-Add by TCTNB.long.chen,07/23/2013,469710,
            //for more sensitive to detect face change
            int diffValue =8;
            boolean isNeedEquals = true;
            int step = (int) Math.ceil(equalRect.width() / 16);// 6.25% step>=1
            int xMin = -step;
            int xMax = step;
            int yMin = -step;
            int yMax = step;
            if (beginX -step  < 0) {
                xMin = -beginX;
                xMax = 2*step - xMin;
            }
            if (beginY - step < 0) {
                yMin = -beginY;
                yMax = 2*step - yMin;
            }
            for (int k = xMin; k <= xMax; k++) {
                if (Math.abs((int) lastFrameBytes[mPreviewSize.width * beginY
                        + beginX + k]
                        - (int) mPreFrameBytes[mPreviewSize.width * beginY
                                + beginX + k]) <= 5) {
                    count++;
                }
            }
            if (count < 2 * step) {
                meLog("3");
                count = 0;
                isNeedEquals = false;
                for (int i = yMin; i <= yMax; i++) {
                    for (int j = xMin; j <= xMax; j++) {
                        if (Math.abs((int) lastFrameBytes[mPreviewSize.width
                                * (beginY + i) + beginX + j]
                                - (int) mPreFrameBytes[mPreviewSize.width
                                        * preBeginY + preBeginX]) <= 5) {
                            count = 0;
                            for (int k = j; k <= 2 * step + j; k++) {
                                if (Math.abs((int) lastFrameBytes[mPreviewSize.width
                                        * (beginY + i) + beginX + k]
                                        - (int) mPreFrameBytes[mPreviewSize.width
                                                * preBeginY + preBeginX + k]) <= 5) {
                                    count++;
                                }
                            }
                            if (count >= 2 * step) {
                                meLog("4");
                                beginY = beginY + i;
                                beginX = beginX + j;
                                isNeedEquals = true;
                                width = beginX + width <= mPreviewSize.width ? width
                                        : (mPreviewSize.width - beginX);
                                height = beginY + height <= mPreviewSize.height ? height
                                        : (mPreviewSize.height - beginY);

                                //[BUGFIX]-Add-BEGIN by TCTNB.long.chen,07/23/2013,469710,
                                //when shift rect, we only need shift the last frame. why we need to shift the pre frame?
                                /*preBeginX = preBeginX + width <= mPreviewSize.width ? width
                                        : (mPreviewSize.width - preBeginX);
                                preBeginY = preBeginY + height <= mPreviewSize.height ? height
                                        : (mPreviewSize.height - preBeginY);*/
                                //[BUGFIX]-Add-END by TCTNB.long.chen
                                break;
                            }
                        }
                    }
                    if (count >= 2 * step) {
                        meLog("5");
                        break;
                    }
                }
            }

            meLog("6");
            if (isNeedEquals) {
                //[BUGFIX]-Add by TCTNB.long.chen,07/23/2013,469710,for Debug
                /*byte[] localPreFrameBytes = new byte[mPreFrameBytes.length];
                byte[] localLastFrameBytes = new byte[lastFrameBytes.length];
                //byte[] localNoChangeLastFrameBytes = new byte[lastFrameBytes.length];
                for(int i = 0; i < lastFrameBytes.length; i++) {
                    localPreFrameBytes[i] = mPreFrameBytes[i];
                    localLastFrameBytes[i] = lastFrameBytes[i];
                    //localNoChangeLastFrameBytes[i] = lastFrameBytes[i];
                }*/

                count = 0;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j <= width; j+=2) {
                        try {
                            if (Math.abs((int) lastFrameBytes[mPreviewSize.width
                                    * (beginY + i) + beginX + j]
                                    - (int) mPreFrameBytes[mPreviewSize.width
                                            * (preBeginY + i) + preBeginX + j]) <= diffValue) {
                                count++;
                            }/* else {
                                //[BUGFIX]-Add by TCTNB.long.chen,07/23/2013,469710,for Debug
                                //flag the diff pixel
                                localLastFrameBytes[mPreviewSize.width* (beginY + i) + beginX + j] = 0;
                                localPreFrameBytes[mPreviewSize.width* (preBeginY + i) + preBeginX + j] = 0;
                            }*/
                        } catch (Exception er) {
                            meLog("YUVEquals Error:" + er.toString());
                            return false;
                        }
                    }
                }

                meLog("7 count:" + count + "  width * height/2:" + (width * height)/2);
                //[BUGFIX]-Add by TCTNB.long.chen,07/23/2013,469710,
                //for more sensitive to detect face change
                if (count < (width * height * 0.3 *18 / 20 / 2)) {//[BUGFIX]-Add by TCTNB.Shangyong.Zhang 03/12/2014, 607040.
                    //[BUGFIX]-Add-BEGIN by TCTNB.long.chen,07/23/2013,469710,for Debug
                    /*showRectDetectRect(localPreFrameBytes, new Rect(preBeginX , preBeginY, preBeginX+width, preBeginY+height), "chag_pre_");
                    showRectDetectRect(localLastFrameBytes, new Rect(beginX , beginY, beginX+width, beginY+height), "chag_last_");*/
                    //[BUGFIX]-Add-END by TCTNB.long.chen
                    //meLog("zsy1029 ************mIsCheckExpression == " +mIsCheckExpression);
                    //meLog("zsy1029 $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ == " +mExpressionThread);
                    if (mIsCheckExpression) {
                        if (mExpressionThread != null) {
                            mExpressionThread.runCallbackOnUiThreadLogic();
                        }
                        //if (mHandler != null) {
                        //    mHandler.sendEmptyMessage(ExpressionDetectModule.CONTINUOUS_EXPRESSION_SHOT);
                        //}
                        mIsCheckExpression = false;
                    }
                }
            }else {
                 meLog("20");
                 /*if (mIsCheckExpression) {
                        mHandler.sendEmptyMessage(ExpressionDetectModule.CONTINUOUS_EXPRESSION_SHOT);
                        mIsCheckExpression = false;
                    }*/
                }
            }else {
            //meLog("zsy1029 ************mIsSavePreFrame &&mIsCheckExpression == " +(mIsSavePreFrame &&mIsCheckExpression));
            if (mIsSavePreFrame &&mIsCheckExpression) {
                if (mExpressionThread != null) {
                    mExpressionThread.runCallbackOnUiThreadLogic();
                }
                //if (mHandler != null) {
                //    mHandler.sendEmptyMessage(ExpressionDetectModule.CONTINUOUS_EXPRESSION_SHOT);
                //}
                mIsCheckExpression = false;
            }
        }

        meLog("8");
        mPreOrientation = mLastOrientation;
        mPreFrameBytes = lastFrameBytes;
        mIsSavePreFrame = true;

        meLog("9");
        return true;
    }


    private CameraManager.CameraPreviewDataCallback oneshotcallback = new CameraManager.CameraPreviewDataCallback(){
         @Override
        public void onPreviewFrame(final byte[] data, CameraManager.CameraProxy camera) {
               //meLog("zsy1029 callback onPreviewFrame E");
	        // TODO Auto-generated method stub
	        if (data == null) {
	            //meLog("zsy1029 callback onPreviewFrame::Preview data is null!");
	            return;
	        }
	        if (!mIsCheckExpression) {
	            //meLog("zsy1029 callback onPreviewFrame::YUVEquals");
	            YUVEquals(data, mTempFace);
	        } else {
	            //meLog("zsy1029 callback onPreviewFrame::YUVCheckExpressionChange");
	            YUVCheckExpressionChange(data, mTempFace);
	        }
        }
    };
}
