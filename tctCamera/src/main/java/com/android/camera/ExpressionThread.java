
package com.android.camera;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera.Size;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import com.android.camera.util.CameraUtil;
import android.os.Handler;
import android.util.Log;
import android.graphics.RectF;
import com.android.camera.CameraManager.CameraProxy;
import java.io.ByteArrayOutputStream;

import org.codeaurora.camera.ExtendedFace;//[BUGFIX]-Add by TCTNB.Shangyong.Zhang 03/12/2014, 607040.

public class ExpressionThread extends Thread{
    private static final String TAG = "ExpressionThread";
    public static final int MAXNUM = 4;
    private ExpressionDetect mExpressionDetect;
    private RectF[] mFacesRectF = null;
    private int mShotCount = MAXNUM;
    private int mShotCountChecker = MAXNUM;
    private int mThumbnailIndex = -1;
    private CameraProxy mCameraDevice;
    private static int mOrientation;
    private Bitmap[] bitmaps = new Bitmap[MAXNUM];

    private int mWidth;
    private int mHeight;
    private boolean stop;
    private boolean jobDone;
    private Runnable mCallback;
    private Face[] mFace;
    private ExtendedFace mLastQCFace = null;//[BUGFIX]-Add by TCTNB.Shangyong.Zhang 03/12/2014, 607040.
    private CameraActivity mActivity;
    private static ExpressionThread thread;
    private final Handler mHandler;
    private ExpressionThread(CameraActivity activity, Handler handler, int orientation,
                                CameraProxy cameraProxy, Runnable callback) {
        mActivity = activity;
        mOrientation = orientation;
        mCameraDevice = cameraProxy;
        mCallback = callback;
        mHandler = handler;
    }

    public void run() {
        Log.i(TAG,"zsy0311 run E");
        if (mExpressionDetect == null) {
            mExpressionDetect = new ExpressionDetect(mCameraDevice, mActivity, mHandler, thread);
        }
        mExpressionDetect.init();
        stop = false;
        //[BUGFIX]-Add by TCTNB.Shangyong.Zhang 03/12/2014, 607040.
        /*while (!stop) {
            try {
                Thread.sleep(252);
                if(stop) { break; }
                if (mActivity != null) {
                    if (mFacesRectF == null) {
                        Thread.sleep(48);
                    }
                    if(stop){ break; }
                    mExpressionDetect.getFrameByte(mFacesRectF, mOrientation);
                } else {
                    stop = true;
                }
            } catch (Exception ex) {
                Log.e(TAG, "ERROR:" + ex.toString());
            }
        }*/
        //try { Thread.sleep(500); } catch (Exception e) {}
        while(!stop) {
            try {
                Thread.sleep(252);
                if(stop) { break; }
                if (mActivity != null) {
                    if (mFacesRectF == null) {
                        Thread.sleep(48);
                    }
                    if(stop){ break; }
                    mExpressionDetect.getFrameByte(mFacesRectF, mOrientation);
                } else {
                    stop = true;
                }
            } catch (Exception e) {}
            if (mFace != null && mFace.length > 0) {
                Face face = mFace[0];
                if (face instanceof ExtendedFace) {
                    if (mLastQCFace == null) {
                        mLastQCFace = ((ExtendedFace)face);
                        continue;
                    }
                    if (!stop) {
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                        }
                        CameraInfo info = CameraHolder.instance().getCameraInfo()[mActivity.getCameraId()];
                        if (info.facing == CameraInfo.CAMERA_FACING_BACK
                            /*|| mActivity.getCameraState() == CameraActivity.STATE_SWITCHING_CAMERA*/) {
                            stop = true;
                            break;
                        }
                        if (isExpressionChanged((ExtendedFace)face)) {
                            Log.i("zsy0311","call back!!");
                            if(stop){ break; }
                            mLastQCFace = null;
                            runCallbackOnUiThreadLogic();
                            try {
                                Thread.sleep(500);
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            }
        }
        Log.i(TAG,"zsy0311 run X");
    }

    private final int smile_threashold_little_change = 3;
    private final int smile_threashold_no_smile = 30;
    private final int smile_threashold_small_smile = 60;
    private final int blink_threashold = 10;
    private final int gaze_degree_threashold = 10;

    private boolean isExpressionChanged(ExtendedFace face) {
        if (mLastQCFace == null) return false;
        int positiveCount = 0;
        int negativeCount = 0;

        //Log.i(TAG, "zsy0311 old blink: (" + face.getLeftEyeBlinkDegree()+ ", " +
        //      face.getRightEyeBlinkDegree() + ")");
        //Log.i(TAG, "zsy0311 blink: (" + face.getLeftEyeBlinkDegree()+ ", " +
        //      face.getRightEyeBlinkDegree() + ")");
        if (face.leftEye != null && mLastQCFace.leftEye != null) {
            Log.i(TAG,"zsy0311 run 1");
            //Log.i(TAG,"zsy0311 face.leftEye (" + face.leftEye.x + "," +face.leftEye.y +")");
            //Log.i(TAG,"zsy0311 mLastQCFace.leftEye (" + mLastQCFace.leftEye.x + "," +mLastQCFace.leftEye.y +")");
            if ((face.leftEye.x-mLastQCFace.leftEye.x) > face.rect.width()/2
                || (face.leftEye.y-mLastQCFace.leftEye.y) > face.rect.height()/2) {
                negativeCount++;
            } else if (Math.abs(face.getLeftEyeBlinkDegree() - mLastQCFace.getLeftEyeBlinkDegree()) >= blink_threashold) {
                positiveCount++;
            } else {
                negativeCount++;
            }
        }
        if (face.rightEye != null && mLastQCFace.rightEye != null) {
            Log.i(TAG,"zsy0311 run 2");
            //Log.i(TAG,"zsy0311 face.rightEye (" + face.rightEye.x + "," +face.rightEye.y +")");
            //Log.i(TAG,"zsy0311 mLastQCFace.rightEye (" + mLastQCFace.rightEye.x + "," +mLastQCFace.rightEye.y +")");
            if ((face.rightEye.x-mLastQCFace.rightEye.x) > face.rect.width()/2
                || (face.rightEye.y-mLastQCFace.rightEye.y) > face.rect.height()/2) {
                negativeCount++;
            } else if (Math.abs(face.getRightEyeBlinkDegree() - mLastQCFace.getRightEyeBlinkDegree()) >= blink_threashold) {
                positiveCount++;
            } else {
                negativeCount++;
            }
        }

        //Log.i(TAG,"zsy0311 face.getTopBottomGazeDegree() = " + face.getTopBottomGazeDegree());
        //Log.i(TAG,"zsy0311 mLastQCFace.getTopBottomGazeDegree() = " + mLastQCFace.getTopBottomGazeDegree());
        //Log.i(TAG,"zsy0311 face.getLeftRightGazeDegree() = " + face.getLeftRightGazeDegree());
        //Log.i(TAG,"zsy0311 mLastQCFace.getLeftRightGazeDegree() = " + mLastQCFace.getLeftRightGazeDegree());
        if (Math.abs(face.getTopBottomGazeDegree()-mLastQCFace.getTopBottomGazeDegree()) >= gaze_degree_threashold) {
            positiveCount++;
        } else {
            negativeCount++;
        }
        if (Math.abs(face.getLeftRightGazeDegree()-mLastQCFace.getLeftRightGazeDegree()) >= gaze_degree_threashold) {
            positiveCount++;
        } else {
            negativeCount++;
        }
        if (face.mouth != null && mLastQCFace.mouth != null) {
            Log.i(TAG, "zsy0311 smile: " + face.getSmileDegree() + "," + face.getSmileScore());
            if (face.getSmileDegree() < smile_threashold_little_change) {
                if (mLastQCFace.getSmileDegree() >= smile_threashold_little_change) {
                    Log.i(TAG,"zsy0311 run 50");
                    positiveCount++;
                    negativeCount--;
                } else {
                    positiveCount--;
                    negativeCount++;
                }
            } else if (face.getSmileDegree() < smile_threashold_no_smile) {
                if (mLastQCFace.getSmileDegree() < smile_threashold_little_change
                    ||mLastQCFace.getSmileDegree() >= smile_threashold_no_smile) {
                    Log.i(TAG,"zsy0311 run 5");
                    positiveCount++;
                    negativeCount--;
                } else {
                    positiveCount--;
                    negativeCount++;
                }
            } else if (face.getSmileDegree() < smile_threashold_small_smile) {
                if (mLastQCFace.getSmileDegree() < smile_threashold_no_smile ||
                    mLastQCFace.getSmileDegree() >= smile_threashold_small_smile) {
                    Log.i(TAG,"zsy0311 run 6");
                    positiveCount++;
                    negativeCount--;
                } else {
                    positiveCount--;
                    negativeCount++;
                }
            } else {
                if (mLastQCFace.getSmileDegree() <= smile_threashold_small_smile) {
                    Log.i(TAG,"zsy0311 run 7");
                    positiveCount++;
                    negativeCount--;
                } else {
                    positiveCount--;
                    negativeCount++;
                }
            }
        } else {
            Log.i(TAG,"zsy0311 run 8");
            negativeCount++;
            positiveCount--;
        }

        Log.i(TAG,"zsy0311 positiveCount = " + positiveCount);
        Log.i(TAG,"zsy0311 negativeCount = " + negativeCount);
        if(positiveCount >= negativeCount)
            return true;
        return false;
    }
    //[BUGFIX]-Add-END by TCTNB.Shangyong.Zhang.

    public void runCallbackOnUiThreadDirect() {
        Log.i(TAG,"zsy1029 runCallbackOnUiThreadDirect E");
        if (mActivity != null && mCallback != null) {
            if(mShotCount > 0) {// 4, 3, 2, 1
                mActivity.runOnUiThread(mCallback);
            }
        }
    }

    public void runCallbackOnUiThreadLogic() {
        Log.i(TAG,"zsy1029 runCallbackOnUiThreadLogic E");
        if (mActivity != null && mCallback != null) {
            Log.i(TAG,"zsy1029 runCallbackOnUiThreadLogic::mShotCount = " + mShotCount);
            if(mShotCount > 0) {// 4, 3, 2, 1
                mActivity.runOnUiThread(mCallback);
            }
        }
    }

    public static void onPictureTaken(Bitmap bitmap, int width, int height) {
        if (thread == null) return;
        Log.i(TAG,"zsy1029 onPictureTaken E");
        thread.mWidth = width;
        thread.mHeight = height;
        thread.mShotCountChecker--;
        Log.i(TAG,"zsy1031 thread.mThumbnailIndex = " +thread.mThumbnailIndex);
        Log.i(TAG,"zsy1031 bitmap = "+bitmap);
        thread.bitmaps[thread.mThumbnailIndex] = bitmap;
        if (thread.mShotCount <= 0) {
            thread.stop = true;
        }
    }

    public static void onShutterButtonClick() {
        if (thread == null) return;
        Log.i(TAG,"zsy1031 onShutterButtonClick EX");
        thread.mThumbnailIndex++;
        thread.mShotCount--;
    }

    // [BUGFIX]-ADD-BEGIN by TCTNB(Shangyong.Zhang), 2014/01/06, PR583294.
    public static void backForCheck() {
        if (thread == null) return;
        Log.i(TAG,"zsy1031 backForCheck EX");
        thread.mThumbnailIndex--;
        thread.mShotCount++;
    }
    // [BUGFIX]-ADD-END by TCTNB(Shangyong.Zhang), 2014/01/06.

    // This func is used to generate final picture for expression4 mode.
    // If MAXNUM isn't 4, must recheck this func.
    public static byte[] getFinalImage(int [] collageOrder) {
        if (thread == null) return null;
        Log.i(TAG,"zsy1031 save4Pic E ");
        for (int i=0; i<MAXNUM; i++) {
            if (thread.bitmaps[i] == null) {
                Log.d(TAG,"zsy1031 bitmaps "+ i +" is null!");
                return null;
            }
        }
        int w = thread.mWidth;
        int h = thread.mHeight;
        // 1.The thread.bitmaps[]'s images are portrait, so generate the portrait
        // final image first, then rotate the final image to landscape/portrait.
        if (w>h) {
            w = thread.mHeight;
            h = thread.mWidth;
        }
        Log.d(TAG,"zsy1031 bitmaps mWidth:"+w+" mHeight:"+h);
        Bitmap[] bitmaps = new Bitmap[MAXNUM];
        bitmaps[0] = Bitmap.createScaledBitmap(thread.bitmaps[0], w/2, h/2, true);
        bitmaps[1] = Bitmap.createScaledBitmap(thread.bitmaps[1], w/2, h/2, true);
        bitmaps[2] = Bitmap.createScaledBitmap(thread.bitmaps[2], w/2, h/2, true);
        bitmaps[3] = Bitmap.createScaledBitmap(thread.bitmaps[3], w/2, h/2, true);
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas cv = new Canvas(bmp);
        cv.drawBitmap(bitmaps[collageOrder[0]], 0, 0, null);
        cv.drawBitmap(bitmaps[collageOrder[1]], w/2, 0, null);
        cv.drawBitmap(bitmaps[collageOrder[2]], w/2, h/2, null);
        cv.drawBitmap(bitmaps[collageOrder[3]], 0, h/2, null);

        // 2.Rotate the final image to right orientation.
        bmp = CameraUtil.rotate(bmp, mOrientation);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        // Bitmap.createScaledBitmap return the new scaled bitmap or the source
        // bitmap if no scaling is required. And don't to recycle the source bitmap.
        for(int i=0;i<MAXNUM;i++){
            if (bitmaps[i] != thread.bitmaps[i]) {
                bitmaps[i].recycle();
             }
        }
        bmp.recycle();

        stopDetect();

        return baos.toByteArray();
    }

    public static int getExpressionIndex() {
        if (thread == null) return -1;
        return thread.mThumbnailIndex;
    }

    public static boolean isExpressionIndexOk() {
        if (thread == null) return false;
        return thread.mThumbnailIndex >= 0 && thread.mThumbnailIndex <= (MAXNUM -1);
    }

    public static boolean isExpressionThreadActive() {
        if (thread == null) return false;
        return !(thread.stop || thread.jobDone);
    }

    // Generate final picture job done.
    public static boolean isExpressionJobDone() {
        return thread.jobDone;
    }

    // Just complete all picture taken, but not generate final picture
    public static boolean isTakePictureJobDone() {
        return thread.mShotCount <= 0;
    }

    public static boolean canTakePictureAgain() {
        return thread != null && !thread.jobDone && thread.mShotCount > 0;
    }

    public static boolean canGenerateFinalPicture() {
        return thread != null && thread.mShotCount <= 0
            && thread.mShotCount == thread.mShotCountChecker;
    }


    public static void dumpStates(String tag) {
        if (thread == null) {
            Log.i(TAG,"dumpexp thread == null!!");
            return;
        }
        Log.i(TAG,tag + " thread.stop = " + thread.stop +
            "\n "+ tag + " thread.jobDone = " + thread.jobDone +
            "\n "+ tag + " thread.mShotCount = " + thread.mShotCount +
            "\n "+ tag + " thread.mThumbnailIndex = " + thread.mThumbnailIndex +
            "\n "+ tag + " thread.mWidth = " + thread.mWidth +
            "\n "+ tag + " thread.mHeight = " + thread.mHeight +
            "\n "+ tag + " thread.mOrientation = " + thread.mOrientation);
    }


    public synchronized static void detect(CameraActivity activity, Handler handler, int orientation,
                                CameraProxy cameraProxy, Runnable callback) {
        Log.i(TAG,"zsy1031 detect start!!  orientation:"+orientation);
        stopDetect();
        thread = new ExpressionThread(activity, handler, orientation, cameraProxy, callback);
        thread.jobDone = false;
        thread.start();
    }

    public static void stopDetect() {
        Log.i(TAG,"zsy1031 stopDetect E ");
        if (thread != null) {
            Log.i(TAG,"zsy1031 thread != null stopDetect!!");
            thread.mShotCount = 0;
            thread.stop = true;
            thread.jobDone = true;
            for (int i=0; i<MAXNUM; i++) {
                if (thread.bitmaps[i] != null) {
                    //thread.bitmaps[i].recycle();
                    thread.bitmaps[i] = null;
                }
            }
            thread.interrupt();
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

    public synchronized static void updateRectF(RectF[] rectF) {
        if (thread != null && !thread.stop) {
            synchronized(thread) {
                thread.mFacesRectF = rectF;
            }
        }
    }

    public static void onOrientationChanged(int orientation) {
        mOrientation = orientation;
    }
}
