package com.android.camera.effects.gl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**Class of bitmap texture.*/
public final class GLBitmapTexture extends GLAbstractTexture {
    private Bitmap mBitmap;

    /**Class of bitmap texture.
     * @param bmp : Bitmap for generating this bitmap texture.*/
    public GLBitmapTexture(Bitmap bmp) {
        mBitmap = bmp;
        mSize.x = bmp.getWidth();
        mSize.y = bmp.getHeight();
    }

    public void setBitmap() {}

    /**Recycle the allocated memory of this bitmap texture.*/
    public void recycle() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    @Override
    protected void doAttachToGL(int index) {
        // Generate a texture
        GLES20.glGenTextures(1, mTexIds, index);
        // Bind this texture to GL_TEXTURE_2D
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTexName());
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
    }
}