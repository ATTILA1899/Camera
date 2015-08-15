/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.graphics.Matrix;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import com.android.camera.util.CameraUtil;

import java.io.FileDescriptor;
import android.os.ParcelFileDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Thumbnail {
    //[FEATURE]-Add BEGIN by TCTNB,xutao.wu, 2014-07-24, PR635962
    private static final String TAG = "Thumbnail.java";
    private static final boolean LOG = false;
    private static final String LAST_THUMB_FILENAME = "last_thumb";
    private static final int BUFSIZE = 4096;

    private Uri mUri;
    private Bitmap mBitmap;
    // whether this thumbnail is read from file
    private boolean mFromFile = false;

    // Camera, VideoCamera, and Panorama share the same thumbnail. Use sLock
    // to serialize the storage access.
    private static Object sLock = new Object();

    private Thumbnail(Uri uri, Bitmap bitmap, int orientation) {
        mUri = uri;
        mBitmap = rotateImage(bitmap, orientation);
/*just add some log
        Log.i(TAG, " Thumbnail   mBitmap:"+mBitmap);

        if (mBitmap == null) {
            Log.e(TAG, " thumbnail mBitmap is null ???????????????????????????????????");
        }
        else{
            if(mBitmap.isRecycled()){
                Log.i(TAG, " thumbnail  mBitmap has benn recycled nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
            }
        }
*/
    }

    public Uri getUri() {
        return mUri;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setFromFile(boolean fromFile) {
        mFromFile = fromFile;
    }

    public boolean fromFile() {
        return mFromFile;
    }

    private static Bitmap rotateImage(Bitmap bitmap, int orientation) {
        //Log.e(TAG, "WXT  rotateImage  orientation:"+orientation );
        if (orientation != 0) {
            // We only rotate the thumbnail once even if we get OOM.
            Matrix m = new Matrix();
            m.setRotate(orientation, bitmap.getWidth() * 0.5f,
                    bitmap.getHeight() * 0.5f);

            try {
                Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                // If the rotated bitmap is the original bitmap, then it
                // should not be recycled.
                //Log.e(TAG, "WXT  rotateImage   mBitmap:"+bitmap  +"  rotated:"+rotated);
                if (rotated != bitmap) {
                    //Log.e(TAG, "WXT  rotateImage   mBitmap:"+bitmap  +" will recycle");
                    bitmap.recycle();
                }
                //Log.e(TAG, "WXT  rotateImage   return  rotated:"+rotated);
                return rotated;
            } catch (Throwable t) {
                Log.w(TAG, "Failed to rotate thumbnail", t);
            }
        }
        //Log.e(TAG, "WXT  rotateImage  return mBitmap:"+bitmap );
        return bitmap;
    }
    //[FEATURE]-Add END by TCTNB,xutao.wu, 2014-07-24, PR635962
    public static Bitmap createVideoThumbnailBitmap(FileDescriptor fd, int targetWidth) {
        return createVideoThumbnailBitmap(null, fd, targetWidth);
    }

    public static Bitmap createVideoThumbnailBitmap(String filePath, int targetWidth) {
        return createVideoThumbnailBitmap(filePath, null, targetWidth);
    }

    private static Bitmap createVideoThumbnailBitmap(String filePath, FileDescriptor fd,
            int targetWidth) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            if (filePath != null) {
                retriever.setDataSource(filePath);
            } else {
                retriever.setDataSource(fd);
            }
            bitmap = retriever.getFrameAtTime(-1);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (bitmap == null) return null;

        // Scale down the bitmap if it is bigger than we need.
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > targetWidth) {
            float scale = (float) targetWidth / width;
            int w = Math.round(scale * width);
            int h = Math.round(scale * height);
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
        }
        return bitmap;
    }

        //[FEATURE]-Add BEGIN by TCTNB,xutao.wu, 2014-07-24, PR635962
    public static Bitmap getLastThumbnailFromContentResolver(ContentResolver resolver, Thumbnail[] result) {
        Media image = getLastImageThumbnail(resolver);
        Media video = getLastVideoThumbnail(resolver);
        if (image == null && video == null) return null;

        Bitmap bitmap = null;
        Media lastMedia;
        // If there is only image or video, get its thumbnail. If both exist,
        // get the thumbnail of the one that is newer.
        if (image != null && (video == null || image.dateTaken >= video.dateTaken)) {
            bitmap = Images.Thumbnails.getThumbnail(resolver, image.id,
                    Images.Thumbnails.MINI_KIND, null);
            lastMedia = image;
        } else {
            bitmap = Video.Thumbnails.getThumbnail(resolver, video.id,
                    Video.Thumbnails.MINI_KIND, null);
            lastMedia = video;
        }

        // Ensure database and storage are in sync.
        if (CameraUtil.isUriValid(lastMedia.uri, resolver)) {
            result[0] = createThumbnail(lastMedia.uri, bitmap, lastMedia.orientation);
            return bitmap;
        }
        return null;
    }

    public static Uri getLastMediaUriFromContentResolver(ContentResolver resolver, Thumbnail[] result) {
        Media image = getLastImageThumbnail(resolver);
        Media video = getLastVideoThumbnail(resolver);
        if (image == null && video == null) return null;

        Bitmap bitmap = null;
        Media lastMedia;
        // If there is only image or video, get its thumbnail. If both exist,
        // get the thumbnail of the one that is newer.
        if (image != null && (video == null || image.dateTaken >= video.dateTaken)) {
            bitmap = Images.Thumbnails.getThumbnail(resolver, image.id,
                    Images.Thumbnails.MINI_KIND, null);
            lastMedia = image;
        } else {
            bitmap = Video.Thumbnails.getThumbnail(resolver, video.id,
                    Video.Thumbnails.MINI_KIND, null);
            lastMedia = video;
        }

        // Ensure database and storage are in sync.
        if (isUriValid(lastMedia.uri, resolver)) {
            //result[0] = createThumbnail(lastMedia.uri, bitmap, lastMedia.orientation);
            return lastMedia.uri;
        }
        return null;
    }

    private static Media getLastImageThumbnail(ContentResolver resolver) {
        Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;

        Uri query = baseUri.buildUpon().appendQueryParameter("limit", "1").build();
        String[] projection = new String[] {ImageColumns._ID, ImageColumns.ORIENTATION,
                ImageColumns.DATE_TAKEN};
        String selection = ImageColumns.MIME_TYPE + "='image/jpeg' AND " +
                ImageColumns.BUCKET_ID + '=' + Storage.BUCKET_ID;
        String order = ImageColumns.DATE_TAKEN + " DESC," + ImageColumns._ID + " DESC";

        Cursor cursor = null;
        try {
            cursor = resolver.query(query, projection, selection, null, order);
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(0);
                return new Media(id, cursor.getInt(1), cursor.getLong(2),
                                 ContentUris.withAppendedId(baseUri, id));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private static Media getLastVideoThumbnail(ContentResolver resolver) {
        Uri baseUri = Video.Media.EXTERNAL_CONTENT_URI;

        Uri query = baseUri.buildUpon().appendQueryParameter("limit", "1").build();
        String[] projection = new String[] {VideoColumns._ID, MediaColumns.DATA,
                VideoColumns.DATE_TAKEN};
        String selection = VideoColumns.BUCKET_ID + '=' + Storage.BUCKET_ID;
        String order = VideoColumns.DATE_TAKEN + " DESC," + VideoColumns._ID + " DESC";

        Cursor cursor = null;
        try {
            cursor = resolver.query(query, projection, selection, null, order);
            if (cursor != null && cursor.moveToFirst()) {
                //Log.d(TAG, "getLastVideoThumbnail: " + cursor.getString(1));
                long id = cursor.getLong(0);
                return new Media(id, 0, cursor.getLong(2),
                        ContentUris.withAppendedId(baseUri, id));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private static class Media {
        public Media(long id, int orientation, long dateTaken, Uri uri) {
            this.id = id;
            this.orientation = orientation;
            this.dateTaken = dateTaken;
            this.uri = uri;
        }

        public final long id;
        public final int orientation;
        public final long dateTaken;
        public final Uri uri;
    }

    public static boolean isUriValid(Uri uri, ContentResolver resolver) {
        if (uri == null) return false;

        try {
            ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
            if (pfd == null) {
                android.util.Log.e(TAG, "Fail to open URI. URI=" + uri);
                return false;
            }
            pfd.close();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public static Thumbnail createThumbnail(Uri uri, Bitmap bitmap, int orientation) {
        if (bitmap == null) {
            //Log.i(TAG, "WXT  Failed to create thumbnail from null bitmap");
            return null;
        }
/* just add some log
        if(uri == null){
            Log.i("TAG", "WXT   createThumbnail   uri is null ?????????????????????????????OOOOOOOOOOOOOOOOOOOO");
        }
        else{
            Log.i("TAG", "WXT   createThumbnail  ********************** uri :"+uri);
        }

        if (bitmap == null) {
            Log.i(TAG, "WXT   createThumbnail bitmap is null ???????????????????????????????????");
        }
        else{
            if(bitmap.isRecycled())
                Log.i(TAG, "WXT   createThumbnail bitmap has benn recycled nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
        }
*/
        return new Thumbnail(uri, bitmap, orientation);
    }

    // Stores the bitmap to the specified file.
    public void saveLastThumbnailToFile(File filesDir) {
        File file = new File(filesDir, LAST_THUMB_FILENAME);
        FileOutputStream f = null;
        BufferedOutputStream b = null;
        DataOutputStream d = null;
        synchronized (sLock) {
            try {
                f = new FileOutputStream(file);
                b = new BufferedOutputStream(f, BUFSIZE);
                d = new DataOutputStream(b);

                //if(mUri==null)
               // {
                //    Log.i(TAG, "WXT saveLastThumbnailToFile m uri is null");
                //}
                d.writeUTF(mUri.toString());
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, d);
                d.close();
            } catch (IOException e) {
                Log.e(TAG, "Fail to store bitmap. path=" + file.getPath(), e);
            } finally {
                CameraUtil.closeSilently(f);
                CameraUtil.closeSilently(b);
                CameraUtil.closeSilently(d);
            }
        }
    }
    // Loads the data from the specified file.
    // Returns null if failure or the Uri is invalid.
    public static Thumbnail getLastThumbnailFromFile(File filesDir, ContentResolver resolver) {
        File file = new File(filesDir, LAST_THUMB_FILENAME);
        Uri uri = null;
        Bitmap bitmap = null;
        FileInputStream f = null;
        BufferedInputStream b = null;
        DataInputStream d = null;
        synchronized (sLock) {
            try {
                f = new FileInputStream(file);
                b = new BufferedInputStream(f, BUFSIZE);
                d = new DataInputStream(b);
                uri = Uri.parse(d.readUTF());
                if (!CameraUtil.isUriValid(uri, resolver)) {
                    d.close();
                    return null;
                }
                bitmap = BitmapFactory.decodeStream(d);
                d.close();
            } catch (IOException e) {
                Log.i(TAG, "Fail to load bitmap. " + e);
                return null;
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "loadFrom file fail", e);
                return null;
            } finally {
                CameraUtil.closeSilently(f);
                CameraUtil.closeSilently(b);
                CameraUtil.closeSilently(d);
            }
        }
        Thumbnail thumbnail = createThumbnail(uri, bitmap, 0);
        if (thumbnail != null) thumbnail.setFromFile(true);
        return thumbnail;
    }
    //[FEATURE]-Add END by TCTNB,xutao.wu, 2014-07-24, PR635962
}
