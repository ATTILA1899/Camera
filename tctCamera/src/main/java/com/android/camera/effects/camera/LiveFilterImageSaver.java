package com.android.camera.effects.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Images.ImageColumns;

import com.android.camera.CameraActivity;
import com.android.camera.Exif;
import com.android.camera.Storage;
import com.android.camera.Thumbnail;
import com.android.camera.effects.EffectsUtils;
import com.android.camera.effects.EffectsUtils.TaskQueueThread;
import com.android.camera.effects.EffectsUtils.TaskQueueThread.ITaskHandler;
import com.android.camera.effects.EffectsUtils.TaskQueueThread.TaskObserver;
import com.android.camera.effects.LiveFilterInstanceHolder;
import com.android.camera.effects.camera.Components.IStillImageDataHandler;
import com.android.camera.effects.camera.Components.ThumbnailHandler;


import com.android.camera.data.CameraDataAdapter;
import com.android.camera.data.CameraPreviewData;
import com.android.camera.data.FixedFirstDataAdapter;
import com.android.camera.data.FixedLastDataAdapter;
import com.android.camera.data.InProgressDataWrapper;
import com.android.camera.data.LocalData;
import com.android.camera.data.LocalDataAdapter;
import com.android.camera.data.LocalMediaObserver;
import com.android.camera.data.MediaDetails;
import com.android.camera.data.SimpleViewData;


/** Class of camera image data filter and saver.*/
public final class LiveFilterImageSaver implements IStillImageDataHandler {
    private static final String TAG = "LiveFilterImageSaver";

    private Context mContext;

    private ContentResolver mResolver;

    private ThumbnailHandler mThumbnailHandler;

    private final BitmapFactory.Options mBmpOptions = new BitmapFactory.Options();

    private final TaskQueueThread<SaveTaskItem>
            mSaveTaskQueueThread = new TaskQueueThread<SaveTaskItem>();

    private static final class SaveTaskItem {
        int w, h, effect;
        long time;
        byte[] data;
        Location location = null;
    }

    private final ITaskHandler<SaveTaskItem> mTaskHandler =
            new ITaskHandler<SaveTaskItem>() {
        @Override
        public void handleTask(SaveTaskItem task) {
            try {
                doSave(task);
            } catch (IOException e) {EffectsUtils.LogE(TAG, e.toString());}
        }
    };

    private void doSave(SaveTaskItem task) throws IOException {
        int orientation = Exif.getOrientation(task.data);
        Bitmap bmp = BitmapFactory
                .decodeByteArray(task.data, 0, task.data.length, mBmpOptions);
        if (orientation % 180 == 0) {
            task.h = bmp.getWidth();
            task.w = bmp.getHeight();
        } else {
            task.w = bmp.getWidth();
            task.h = bmp.getHeight();
        }

        LiveFilterInstanceHolder.getIntance()
                .getEffectsRender().applyFilter(task.effect, bmp);

        // Generate file name
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(task.time);
        String pictureName = String.format(Locale.getDefault(),
                "IMG_%d%02d%02d_%02d%02d%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + (1 - Calendar.JANUARY),
                calendar.get(Calendar.DATE),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));

        File filePath = new File(Storage.generateFilepath(pictureName));

        filePath.createNewFile();

        FileOutputStream fos = new FileOutputStream(filePath);
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        fos.flush();
        fos.close();

        Bitmap thumb = null;
        // Create Thumbnail
        int multiple = Math.min(task.w, task.h)
                / mThumbnailHandler.getExpectedThumbnailSize();
        thumb = Bitmap.createScaledBitmap(
                bmp, task.h / multiple, task.w / multiple, false);
/* just add some log
        //Log.e("WXT", "LiveFilterImageSave.java   doSave  bmp:"+bmp);

        if(thumb != bmp){
	     Log.i("WXT", " LiveFilterImageSave.java   doSave  bmp and thumb is not the same,   will recycle bmp -----------------");
            bmp.recycle();
        }

        //if(bmp.isRecycled())
            //Log.i("WXT", " LiveFilterImageSave.java   doSave  bmp has benn recycled mmmmmmmmmmmmmmmmmmm  is OK");

        if (thumb == null) {
			EffectsUtils.LogE("WXT", "LiveFilterImageSave.java   doSave thumb is null ???????????????????????????????????");
        }
        else{
            //Log.i("WXT", "LiveFilterImageSave.java   doSave  thumb:"+thumb);
            if(thumb.isRecycled())
                Log.i("WXT", "LiveFilterImageSave.java   doSave  thumb has benn recycled nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
        }

*/

        ExifInterface exif = new ExifInterface(filePath.getAbsolutePath());
        exif.setAttribute(ExifInterface.TAG_ORIENTATION,
                    Integer.toString(ExifInterface.ORIENTATION_ROTATE_90));
        exif.saveAttributes();

        ContentValues v = new ContentValues();
        v.put(MediaColumns.TITLE, pictureName);
        v.put(MediaColumns.DISPLAY_NAME, pictureName);
        v.put(ImageColumns.DATE_TAKEN, calendar.getTimeInMillis());
        v.put(MediaColumns.MIME_TYPE, "image/jpeg");
        v.put(MediaColumns.DATA, filePath.getAbsolutePath());
        v.put(MediaColumns.SIZE, filePath.length());
        if (task.location != null) {
            v.put(ImageColumns.LATITUDE, task.location.getLatitude());
            v.put(ImageColumns.LONGITUDE, task.location.getLongitude());
        }
        v.put(ImageColumns.ORIENTATION, orientation);
        v.put(ImageColumns.WIDTH, task.w);
        v.put(ImageColumns.HEIGHT,task.h);
        Uri uri = mResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);

        //if(uri == null)
            //Log.i("WXT", "LiveFilterImageSave.java   doSave uri is null ?????????????????????????????OOOOOOOOOOOOOOOOOOOO");

        Thumbnail mythumbnial = Thumbnail.createThumbnail(uri, thumb, 0);
/* just add some log
        if (mythumbnial == null) {
            Log.i("WXT", "LiveFilterImageSave.java   doSave mythusmbnial is null ");
        }
        else{
            Log.i("WXT", "LiveFilterImageSave.java   doSave  mythumbnial:"+mythumbnial);
            if(mythumbnial.getBitmap().isRecycled())
                Log.i("WXT", "LiveFilterImageSave.java   doSave  mythumbnial has benn recycled");
        }
*/
        mThumbnailHandler.onNewThumbnailArrived(mythumbnial /*Thumbnail.createThumbnail(uri, thumb, 0)*/);
    }

    public LiveFilterImageSaver(Context context) {
        mContext = context;
        mBmpOptions.inDither = false;
        mBmpOptions.inPurgeable = true;
        mBmpOptions.inInputShareable = true;
        mBmpOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        mResolver = mContext.getContentResolver();
        mSaveTaskQueueThread.setTaskHandler(mTaskHandler);
        mSaveTaskQueueThread.start();
    }

    public void setThumbnailHandler(ThumbnailHandler handler) {
        mThumbnailHandler = handler;
    }

    public void setSaveTaskObserver(TaskObserver observer) {
        mSaveTaskQueueThread.setTaskObserver(observer);
    }

    @Override
    public void onJpegData(byte[] data, int effect) {
        SaveTaskItem item = new SaveTaskItem();
        item.data = data;
        item.time = System.currentTimeMillis();
        item.effect = effect;
        mSaveTaskQueueThread.addTask(item);
    }
}
