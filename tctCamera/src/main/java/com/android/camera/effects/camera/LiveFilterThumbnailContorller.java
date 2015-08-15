package com.android.camera.effects.camera;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageView;

import com.android.camera.CameraActivity;
import com.tct.camera.R;
import com.android.camera.Storage;
import com.android.camera.Thumbnail;
import com.android.camera.effects.EffectsUtils;
import com.android.camera.effects.EffectsUtils.MainMessage;
import com.android.camera.effects.camera.Components.PauseResumeAble;
import com.android.camera.effects.camera.Components.ThumbnailHandler;
import com.android.camera.ui.RotateImageView;

import com.android.camera.effects.LiveFilterCamera;
//import com.android.gallery3d.app.Gallery;

public final class LiveFilterThumbnailContorller
        implements ThumbnailHandler, PauseResumeAble {
    private static final String TAG = "LiveFilterThumbnailContorller";

    private Context mContext;
    private LiveFilterCamera mFilterContext;
    private Handler mRefreshHandler;

    private RotateImageView mThumbnailView;

    private Thumbnail mThumb = null, mTempThumb = null;

    private IntentFilter mDelFilter =
            new IntentFilter("com.android.gallery3d.action.DELETE_PICTURE");

    private AsyncTask<Void, Void, Thumbnail> mLoadTask;

    private BroadcastReceiver mDeleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            EffectsUtils.LogV(TAG, "onReceive(" + intent + ")");
        }
    };

    private class SaveTask extends AsyncTask<Thumbnail, Void, Void> {
        @Override
        protected Void doInBackground(Thumbnail... params) {
            final int n = params.length;
            final File filesDir = mContext.getFilesDir();
            //EffectsUtils.LogI(TAG, "WXT SaveTask doInBackground  save to : " + filesDir.getPath() +" para len:"+n);
            for (int i = 0; i < n; i++) {
                params[i].saveLastThumbnailToFile(filesDir);
            }
            return null;
        }
    }

    private class LoadTask extends AsyncTask<Void, Void, Thumbnail> {
        private boolean mLookAtCache;

        public LoadTask(boolean lookAtCache) {mLookAtCache = lookAtCache;}
/*
        @Override
        protected Thumbnail doInBackground(Void... params) {
            ContentResolver resolver = mContext.getContentResolver();
            Thumbnail t = null;
            if (mLookAtCache) {
                t = Thumbnail.getLastThumbnailFromFile(mContext.getFilesDir(), resolver);
                EffectsUtils.LogV(TAG, "Load thumbnail from cache file. " + t);
            }

            if (isCancelled()) {return null;}

            if (t == null) {
                Thumbnail result[] = new Thumbnail[1];
                // Load the thumbnail from the media provider.
                int rt = Thumbnail.getLastThumbnailFromContentResolver(resolver, result);
                EffectsUtils.LogV(TAG, "Load thumbnail from media provider.");
                switch (rt) {
                case Thumbnail.THUMBNAIL_FOUND:
                    return result[0];
                case Thumbnail.THUMBNAIL_NOT_FOUND:
                    return null;
                case Thumbnail.THUMBNAIL_DELETED:
                    cancel(true);
                    return null;
                default:
                    return null;
                }
            }
            return t;
        }
*/
    @Override
    protected Thumbnail doInBackground(Void... params) {
        ContentResolver resolver = mContext.getContentResolver();
        //Thumbnail t = null;
       // if (mLookAtCache) {
            //t = Thumbnail.getLastThumbnailFromFile(mContext.getFilesDir(), resolver);
            //Log.i("Liu", "Load thumbnail from cache file. " + t);
        //}
        Log.i("Liu"," Load Task doInBackground");
        if (isCancelled()) {return null;}
        if (true) {
            Thumbnail result[] = new Thumbnail[1];
            // Load the thumbnail from the media provider.
           if(Thumbnail.getLastThumbnailFromContentResolver(resolver, result) != null){
                Log.i("Liu"," Load Task doInBackground result[0] "+result[0]);
                return result[0]; 
            }else{
                Log.i("Liu"," Load Task doInBackground return null 1");
                return null;
            }
        }
	  Log.i("Liu","WXT Load Task doInBackground  return null 2");
        return null;
    }

        @Override
        protected void onPostExecute(Thumbnail thumbnail) {
            //Log.i("Liu","WXT Load Task onPostExecute");
            if (isCancelled()) {Log.i("Liu"," Load Task onPostExecute return"); return;}
/* just add some log
            if (thumbnail == null) {
			EffectsUtils.LogE(TAG, " Load Task onPostExecute thumbnail is null ???????????????????????????????????");
        	}
		else{
                if(thumbnail.getBitmap().isRecycled())
                    EffectsUtils.LogE(TAG, " Load Task onPostExecute  thumbbitmap has benn recycled nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
            }
*/
            onNewThumbnailArrived(thumbnail);
        }
    }

    public LiveFilterThumbnailContorller(Context context) {mContext = context;}

    public void setThumbnailImage(RotateImageView view, Handler handler) {
        mThumbnailView = view;
        mRefreshHandler = handler;
    }

    public void setFilterContext(LiveFilterCamera context) {
        mFilterContext = context;
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDeleteReceiver);
        if (mLoadTask != null) {
            mLoadTask.cancel(true);
            mLoadTask = null;
        }
        trySaveThumbnailToFile();
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(mContext)
                .registerReceiver(mDeleteReceiver, mDelFilter);
        mLoadTask = new LoadTask(true).execute();
    }

    private void trySaveThumbnailToFile() {
        EffectsUtils.LogV(TAG, "Save thumbnail to file : " + mThumb);
        if (mThumb != null && !mThumb.fromFile()) {
            new SaveTask().execute(mThumb);
        }
    }

    public void refreshThumbnail() {

        if (mTempThumb == null) {
            //EffectsUtils.LogD(TAG, "refreshThumbnail  mTempThumb is null");
            if (mThumb != null) {
                //EffectsUtils.LogD(TAG, "refreshThumbnail  mThumb is not null,  :"+ mThumb + " ^^^^^^^^^^^^^^  will recyle");
                mThumb.getBitmap().recycle();
                mThumb = null;
            }
            mThumbnailView.setScaleType(ImageView.ScaleType.FIT_XY);
            mThumbnailView.setImageResource(R.drawable.default_thumbnail);
            //EffectsUtils.LogD(TAG, " refreshThumbnail   return");
            return;
        }else
		EffectsUtils.LogD(TAG, " refreshThumbnail mTempThumb:"+mTempThumb);
		
        mThumbnailView.setScaleType(ImageView.ScaleType.CENTER);
/* just add some log
		if(mTempThumb.getBitmap() == null)
		{
                   EffectsUtils.LogD(TAG, " refreshThumbnail   mTempThumb bitmap is null >>>>>>>>>>>>>>>>>>>>>..");
		}else{
			if(mTempThumb.getBitmap().isRecycled())
                      EffectsUtils.LogD(TAG, " refreshThumbnail   mTempThumb bitmap has benn recycled ,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,.");
    	       }
*/
        mThumbnailView.setBitmap(mTempThumb.getBitmap());
        if (mThumb != null) {
            //EffectsUtils.LogD(TAG, " refreshThumbnail   mThumb:"+ mThumb + " will ^^^^^^^^^^^^^^^^^^^^^^^^^^ recycle");
            mThumb.getBitmap().recycle();
        }

        mThumb = mTempThumb;
    }

    public void onThumbnailClicked(Context context) {
        if (mThumb == null) return;
        mFilterContext.gotoGallery();
    }

    @Override
    public int getExpectedThumbnailSize() {return mThumbnailView.getWidth();}

    @Override
    public void onNewThumbnailArrived(Thumbnail thumb) {
/* just add some log
        EffectsUtils.LogD(TAG, " onNewThumbnailArrived  ");
        if (thumb == null) {
            EffectsUtils.LogD(TAG, " onNewThumbnailArrived Thumb is null ???????????????????????????????????");
        }
        else{
            EffectsUtils.LogD(TAG, " onNewThumbnailArrived thumb:"+thumb);
            EffectsUtils.LogD(TAG, " onNewThumbnailArrived thumb. bitmap:"+thumb.getBitmap());
            if(thumb.getBitmap().isRecycled())
                EffectsUtils.LogD(TAG, " onNewThumbnailArrived  thumbbitmap has benn recycled nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
        }
*/
        mTempThumb = thumb;
        mRefreshHandler.sendEmptyMessage(MainMessage.MSG_REFRESH_THUMBNAIL);
    }
}
