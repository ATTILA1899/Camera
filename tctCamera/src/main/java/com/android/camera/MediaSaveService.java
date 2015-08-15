/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore.Video;
import android.util.Log;
import com.android.camera.PhotoModule;
import java.util.LinkedList;
import java.util.Queue;
import com.android.camera.exif.ExifInterface;

import java.io.File;

/*
 * Service for saving images in the background thread.
 */
public class MediaSaveService extends Service {
    public static final String VIDEO_BASE_URI = "content://media/external/video/media";

    // The memory limit for unsaved image is 50MB.
    private static final int SAVE_TASK_MEMORY_LIMIT = 60 * 1024 * 1024;
    private static final String TAG = "YUHUAN_" + MediaSaveService.class.getSimpleName();

    private final IBinder mBinder = new LocalBinder();
    private Listener mListener;
    // Memory used by the total queued save request, in bytes.
    private long mMemoryUse;
    private final int SAVE_DONE = 1;
    private final Handler mHandler = new MainHandler();
    /**
     * This Handler is used to post message back onto the main thread of the
     * application
     */
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SAVE_DONE: {
                    //[FEATURE]-Mod by TCTNB.(hao.wang),11/22/2013,558699,[BT-Phone]Camera control
                    //this here just send when the burstshot pics are saved.
                    Log.d(TAG,"send TCT_SAVE_DONE_ACTION to cameraActivity");
                    Intent intent = new Intent(CameraActivity.TCT_SAVE_DONE_ACTION);
                    sendBroadcast(intent);
                    break;
                }
            }
        }
    }

    public interface Listener {
        public void onQueueStatus(boolean full);
    }

    public interface OnMediaSavedListener {
        public void onMediaSaved(Uri uri);
    }

    class LocalBinder extends Binder {
        public MediaSaveService getService() {
            return MediaSaveService.this;
        }
    }

    // [BUGFIX]-ADD-BEGIN by TCTNB(Shangyong.Zhang), 2014/01/24, PR586582, PR552251.
    private final static int BURSTSHOT_MAX_TASK_NUM = PhotoModule.BURST_NUM_MAX;
    private boolean isNeed2CheckBurstshot = false; // true that is capture done.
    private int mCapturedCount4Burstshot = 0; // sync the number that user seeing.
    private int mMaxCapturedCount4Burstshot = 0; // the max num when capture done.
    private int mImageSavedCount = 0;
    private Queue<ImageSaveTask> mTaskQueue = new LinkedList<ImageSaveTask>();
    private int mImageSavingCount = 0; // the count of the picture witch is saving.
    private BurstshotThread mBurstshotThread = null;
    private class BurstshotThread extends Thread {
        private boolean stopRequested = false;
        private int autoStopCount = 0; // this count is used to auto stop thread.
        public void stopRequest() {
            stopRequested = true;
            if (this.isAlive()) {
                this.interrupt();
            }
        }

        @Override
        public void run() {
        	Log.d(TAG,"###YUHUAN###BurstshotThread#stopRequested=" + stopRequested);
        	Log.d(TAG,"###YUHUAN###BurstshotThread#mImageSavedCount=" + mImageSavedCount);
        	Log.d(TAG,"###YUHUAN###BurstshotThread#mImageSavingCount=" + mImageSavingCount);
            while (!stopRequested && (mImageSavedCount + mImageSavingCount)<=BURSTSHOT_MAX_TASK_NUM) {
            	Log.d(TAG,"###YUHUAN###BurstshotThread#autoStopCount=" + autoStopCount);
            	Log.d(TAG,"###YUHUAN###BurstshotThread#isNeed2CheckBurstshot=" + isNeed2CheckBurstshot);
            	Log.d(TAG,"###YUHUAN###BurstshotThread#stopRequested=" + stopRequested);
                if (autoStopCount>50||(isNeed2CheckBurstshot && (mImageSavedCount
                        + mImageSavingCount)==mMaxCapturedCount4Burstshot)) {
                    mHandler.sendEmptyMessage(SAVE_DONE);
                    break;
                }
                // mImageSavingCount < 1, the 1 is the max saving number, you can change it to others.
                if (mMemoryUse > 0 && (mImageSavedCount + mImageSavingCount)
                    <mCapturedCount4Burstshot && mImageSavingCount<=BURSTSHOT_MAX_TASK_NUM) {
                    synchronized(mTaskQueue) {
                        ImageSaveTask t = mTaskQueue.poll();
                        if (t != null) {
                            t.execute();
                            mImageSavingCount++;
                            autoStopCount = 0;
                        }
                    }
                } else {
                    try{
                        Thread.sleep(100);
                        autoStopCount++;
                    } catch (Exception e) {
                        Log.e(TAG,"zsy0122 BurstshotThread sleep error. maybe interrupt");
                    }
                }
            }
        }
    }

    public void init4BurstshotStart(boolean isNeedThread) {
        Log.i(TAG,"zsy0122 init4BurstshotStart");
        mImageSavedCount = 0;
        mMaxCapturedCount4Burstshot = 0;
        mCapturedCount4Burstshot = 0;

        if(!isNeedThread) return;
        synchronized(mTaskQueue) { mTaskQueue.clear();}
        mImageSavedCount = 0;
        mImageSavingCount = 0;
        isNeed2CheckBurstshot = false;
        synchronized(this){//PR914866-sichao.hu , to make sure the burstshotthread is working
	        if(mBurstshotThread != null&&mBurstshotThread.isAlive()){
	            mBurstshotThread.stopRequest();
	            try{
	                mBurstshotThread.join();
	            }catch(Exception e){}
	            mBurstshotThread=null;
	        }
        }
        mBurstshotThread = new BurstshotThread();
        mBurstshotThread.start();
    }

    public void capturedDone4Burstshot(int maxCapturedCount) {
    	Log.d(TAG,"###YUHUAN###capturedDone4Burstshot#maxCapturedCount=" + maxCapturedCount);
        if (mMemoryUse <= 0) {
            mHandler.sendEmptyMessage(SAVE_DONE);
            return;
        }
        isNeed2CheckBurstshot = true;
        mMaxCapturedCount4Burstshot = maxCapturedCount;
        Log.i(TAG,"zsy0122 capturedDone4Burstshot");
    }

    public void updateShowNumber4Bustshot(int showNum) {
    	Log.d(TAG,"###YUHUAN###updateShowNumber4Bustshot#showNum=" + showNum);
        mCapturedCount4Burstshot = showNum;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mImageSavedCount = 0;
        isNeed2CheckBurstshot = false;
        mMaxCapturedCount4Burstshot = 0;
    }

    @Override
    public void onCreate() {
        mMemoryUse = 0;
        mImageSavedCount = 0;
        isNeed2CheckBurstshot = false;
        mMaxCapturedCount4Burstshot = 0;
    }

    public boolean isQueueFull() {
        return (mMemoryUse >= SAVE_TASK_MEMORY_LIMIT);
    }
    public void addImage4BurstShot(final byte[] data, String title, long date, Location loc,
            int width, int height, int orientation, ExifInterface exif,
            OnMediaSavedListener l, ContentResolver resolver, String pictureFormat) {
    	Log.d(TAG,"###YUHUAN###addImage4BurstShot into ");
        if (isQueueFull()) {
            Log.e(TAG, "Cannot add image when the queue is full");
            return;
        }
        if (mImageSavedCount >= BURSTSHOT_MAX_TASK_NUM) {
            Log.i(TAG,"mImageSavedCount >= BURSTSHOT_MAX_TASK_NUM");
            return;
        }
        ImageSaveTask t = new ImageSaveTask(data, title, date,
                (loc == null) ? null : new Location(loc),
                width, height, orientation, exif, resolver, l, pictureFormat);

        mMemoryUse += data.length;
        if (isQueueFull()) {
            onQueueFull();
        }
        // modify by minghui.hua for PR1003310
        // t.execute();
        t.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public void addImage(final byte[] data, String title, long date, Location loc,
            int width, int height, int orientation, ExifInterface exif,
            OnMediaSavedListener l, ContentResolver resolver, String pictureFormat) {
        if (isQueueFull()) {
            Log.e(TAG, "Cannot add image when the queue is full");
            return;
        }
        ImageSaveTask t = new ImageSaveTask(data, title, date,
                (loc == null) ? null : new Location(loc),
                width, height, orientation, exif, resolver, l, pictureFormat);

        mMemoryUse += data.length;
        if (isQueueFull()) {
            onQueueFull();
        }
        t.execute();
    }

    public void addImage(final byte[] data, String title, long date, Location loc,
                         int orientation, ExifInterface exif,
                         OnMediaSavedListener l, ContentResolver resolver) {
        // When dimensions are unknown, pass 0 as width and height,
        // and decode image for width and height later in a background thread
        addImage(data, title, date, loc, 0, 0, orientation, exif, l, resolver,
                 PhotoModule.PIXEL_FORMAT_JPEG);
    }
    public void addImage(final byte[] data, String title, Location loc,
            int width, int height, int orientation, ExifInterface exif,
            OnMediaSavedListener l, ContentResolver resolver) {
        addImage(data, title, System.currentTimeMillis(), loc, width, height,
                orientation, exif, l, resolver,PhotoModule.PIXEL_FORMAT_JPEG);
    }

    public void addVideo(String path, long duration, ContentValues values,
            OnMediaSavedListener l, ContentResolver resolver) {
        // We don't set a queue limit for video saving because the file
        // is already in the storage. Only updating the database.
        new VideoSaveTask(path, duration, values, l, resolver).execute();
    }

    public void setListener(Listener l) {
        mListener = l;
        if (l == null) return;
        l.onQueueStatus(isQueueFull());
    }

    private void onQueueFull() {
        if (mListener != null) mListener.onQueueStatus(true);
    }

    private void onQueueAvailable() {
        if (mListener != null) mListener.onQueueStatus(false);
    }

    private class ImageSaveTask extends AsyncTask <Void, Void, Uri> {
        private byte[] data;
        private String title;
        private long date;
        private Location loc;
        private int width, height;
        private int orientation;
        private ExifInterface exif;
        private ContentResolver resolver;
        private OnMediaSavedListener listener;
        private String pictureFormat;

        public ImageSaveTask(byte[] data, String title, long date, Location loc,
                             int width, int height, int orientation, ExifInterface exif,
                             ContentResolver resolver, OnMediaSavedListener listener, String pictureFormat) {
            this.data = data;
            this.title = title;
            this.date = date;
            this.loc = loc;
            this.width = width;
            this.height = height;
            this.orientation = orientation;
            this.exif = exif;
            this.resolver = resolver;
            this.listener = listener;
            this.pictureFormat = pictureFormat;
        }

        @Override
        protected void onPreExecute() {
            // do nothing.
        }

        @Override
        protected Uri doInBackground(Void... v) {
        	Log.d(TAG,"###YUHUAN###doInBackground into ");
            if (width == 0 || height == 0) {
                // Decode bounds
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bmp=BitmapFactory.decodeByteArray(data, 0, data.length, options);
                width = options.outWidth;
                height = options.outHeight;
                if(bmp!=null)	bmp.recycle();//PR935621-sichao.hu added
            }
            return Storage.addImage(
                    resolver, title, date, loc, orientation, exif, data, width, height, pictureFormat);
        }

        @Override
        protected void onPostExecute(Uri uri) {
        	Log.d(TAG, "onPostExecute uri="+uri); 
            if (listener != null) listener.onMediaSaved(uri);
            boolean previouslyFull = isQueueFull();
            mImageSavedCount++;
            mImageSavingCount--;
            Log.d(TAG,"###YUHUAN###ImageSaveTask#onPostExecute#isNeed2CheckBurstshot=" + isNeed2CheckBurstshot);
            Log.d(TAG,"###YUHUAN###ImageSaveTask#onPostExecute#mImageSavedCount=" + mImageSavedCount);
            if (isNeed2CheckBurstshot && mImageSavedCount == mMaxCapturedCount4Burstshot) {
                isNeed2CheckBurstshot = false;
                mHandler.sendEmptyMessage(SAVE_DONE);
            }
            mMemoryUse -= data.length;
            if (isQueueFull() != previouslyFull) onQueueAvailable();
        }
    }

    private class VideoSaveTask extends AsyncTask <Void, Void, Uri> {
        private String path;
        private long duration;
        private ContentValues values;
        private OnMediaSavedListener listener;
        private ContentResolver resolver;

        public VideoSaveTask(String path, long duration, ContentValues values,
                OnMediaSavedListener l, ContentResolver r) {
            this.path = path;
            this.duration = duration;
            this.values = new ContentValues(values);
            this.listener = l;
            this.resolver = r;
        }

        @Override
        protected Uri doInBackground(Void... v) {
            values.put(Video.Media.SIZE, new File(path).length());
            values.put(Video.Media.DURATION, duration);
            Uri uri = null;
            try {
                Uri videoTable = Uri.parse(VIDEO_BASE_URI);
                uri = resolver.insert(videoTable, values);

                // Rename the video file to the final name. This avoids other
                // apps reading incomplete data.  We need to do it after we are
                // certain that the previous insert to MediaProvider is completed.
                String finalName = values.getAsString(
                        Video.Media.DATA);
                if (new File(path).renameTo(new File(finalName))) {
                    path = finalName;
                }

                resolver.update(uri, values, null, null);
            } catch (Exception e) {
                // We failed to insert into the database. This can happen if
                // the SD card is unmounted.
                Log.e(TAG, "failed to add video to media store", e);
                uri = null;
            } finally {
                Log.v(TAG, "Current video URI: " + uri);
            }
            return uri;
        }

        @Override
        protected void onPostExecute(Uri uri) {
            if (listener != null) listener.onMediaSaved(uri);
        }
    }
}
