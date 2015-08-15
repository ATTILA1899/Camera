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

package com.android.camera.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.provider.MediaStore.Images;
import android.util.Log;

import com.android.camera.CameraActivity;
import com.android.camera.data.LocalMediaData.PhotoData;
import com.android.camera.exif.ExifInterface;
import com.android.camera.exif.ExifTag;
import com.tct.camera.R;

/**
 * RotationTask can be used to rotate a {@link LocalData} by updating the exif
 * data from jpeg file. Note that only {@link PhotoData}  can be rotated.
 */
public class RotationTask extends AsyncTask<LocalData, Void, LocalData> {
    private static final String TAG = "CAM_RotationTask";
    private final Context mContext;
    private final LocalDataAdapter mAdapter;
    private final int mCurrentDataId;
    private final boolean mClockwise;
    private ProgressDialog mProgress;

    public RotationTask(Context context, LocalDataAdapter adapter,
            int currentDataId, boolean clockwise) {
        mContext = context;
        mAdapter = adapter;
        mCurrentDataId = currentDataId;
        mClockwise = clockwise;
    }

    @Override
    protected void onPreExecute() {
        // Show a progress bar since the rotation could take long.
        mProgress = new ProgressDialog(mContext);
        int titleStringId = mClockwise ? R.string.rotate_right : R.string.rotate_left;
        mProgress.setTitle(mContext.getString(titleStringId));
        mProgress.setMessage(mContext.getString(R.string.please_wait));
        mProgress.setCancelable(false);
        mProgress.show();
    }

    @Override
    protected LocalData doInBackground(LocalData... data) {
        return rotateInJpegExif(data[0]);
    }

    //huanglin 20150207 for PR919525
    private void rotateJpg(String filepath, int rotationdegrees) {
    	Bitmap bitmap = null;
    	BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inPreferredConfig = Bitmap.Config.RGB_565;
    	options.inJustDecodeBounds = true;
    	BitmapFactory.decodeFile(filepath, options);
    	if (options.outWidth < 0 || options.outHeight < 0) {
    		return;
    	}
    	options.inJustDecodeBounds = false;
    	bitmap = BitmapFactory.decodeFile(filepath, options);
    	if (rotationdegrees > 0) {
    		Matrix matrix = new Matrix();
    		matrix.setRotate(rotationdegrees);
    		Bitmap rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, 
    				options.outWidth, options.outHeight, matrix, true);
    		if (null != rotateBitmap) {
    			bitmap.recycle();
    		}
    		bitmap = rotateBitmap;
    	}
    }

    /**
     * Rotates the image by updating the exif. Done in background thread.
     * The worst case is the whole file needed to be re-written with
     * modified exif data.
     *
     * @return A new {@link LocalData} object which containing the new info.
     */
    private LocalData rotateInJpegExif(LocalData data) {
        if (!(data instanceof PhotoData)) {
            Log.w(TAG, "Rotation can only happen on PhotoData.");
            return null;
        }

        PhotoData imageData = (PhotoData) data;
        int originRotation = imageData.getOrientation();
        int finalRotationDegrees;
        if (mClockwise) {
            finalRotationDegrees = (originRotation + 90) % 360;
        } else {
            finalRotationDegrees = (originRotation + 270) % 360;
        }

        String filePath = imageData.getPath();
        ContentValues values = new ContentValues();
        boolean success = false;
        int newOrientation = 0;
        if (imageData.getMimeType().equalsIgnoreCase(LocalData.MIME_TYPE_JPEG)) {
            ExifInterface exifInterface = new ExifInterface();
            ExifTag tag = exifInterface.buildTag(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.getOrientationValueForRotation(
                            finalRotationDegrees));
            if (tag != null) {
                exifInterface.setTag(tag);
                try {
                    // Note: This only works if the file already has some EXIF.
                    exifInterface.forceRewriteExif(filePath);
                    long fileSize = new File(filePath).length();
                    values.put(Images.Media.SIZE, fileSize);
                    newOrientation = finalRotationDegrees;
                    success = true;
                } catch (FileNotFoundException e) {
                    Log.w(TAG, "Cannot find file to set exif: " + filePath);
                } catch (IOException e) {
                    Log.w(TAG, "Cannot set exif data: " + filePath);
                    //huanglin 20150207 for PR919525
                    rotateJpg(filePath, finalRotationDegrees);
                    long fileSize = new File(filePath).length();
                    values.put(Images.Media.SIZE, fileSize);
                    newOrientation = finalRotationDegrees;
                    success = true;
                }
            } else {
                Log.w(TAG, "Cannot build tag: " + ExifInterface.TAG_ORIENTATION);
            }
        }

        PhotoData result = null;
        if (success) {
            // MediaStore using SQLite is thread safe.
            values.put(Images.Media.ORIENTATION, finalRotationDegrees);
            mContext.getContentResolver().update(imageData.getContentUri(),
                    values, null, null);
            double[] latLong = data.getLatLong();
            double latitude = 0;
            double longitude = 0;
            if (latLong != null) {
                latitude = latLong[0];
                longitude = latLong[1];
            }

            result = new PhotoData(data.getContentId(), data.getTitle(),
                    data.getMimeType(), data.getDateTaken(), data.getDateModified(),
                    data.getPath(), newOrientation, imageData.getWidth(),
                    imageData.getHeight(), data.getSizeInBytes(), latitude, longitude);
        }

        return result;
    }

    @Override
    protected void onPostExecute(LocalData result) {
        mProgress.dismiss();
        if (result != null) {
            mAdapter.updateData(mCurrentDataId, result);
            ((CameraActivity)mContext).reloadZoom();
        }
    }
}
