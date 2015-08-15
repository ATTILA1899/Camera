package com.android.camera.effects.gl;

import android.graphics.Point;
import android.opengl.GLES20;

/**Base Class of all texture.*/
public abstract class GLAbstractTexture implements IGLStuff {
    private static final int UNUSED = -1;
    private static final int RETAINED = -2;
    protected static final int[] mTexIds = {UNUSED, UNUSED, UNUSED, UNUSED, UNUSED, UNUSED};

    private int mTexId = -1;
    protected Point mSize = new Point();

    public static void reset() {
        for (int i = 0; i < mTexIds.length; i++) {
            if (mTexIds[i] != UNUSED && mTexIds[i] != RETAINED) {
                GLES20.glDeleteTextures(1, mTexIds, i);
            }
            mTexIds[i] = UNUSED;
        }
    }

    /**To assign a texture ID for this texture.
     * @return Return false if the total texture count reached the MAX_TEXTURE_COUNT.*/
    protected boolean requestTexId() {
        synchronized (mTexIds) {
            for (int i = 0; i < mTexIds.length; i++) {
                if (mTexIds[i] == UNUSED) {
                    mTexIds[i] = RETAINED; // Mark as to be used.
                    mTexId = i;
                    return true;
                }
            }
            return false;
        }
    }

    /**To get size of this texture.
     * @return Size of the texture.*/
    public Point getSize() {
        return mSize;
    }

    /**To get the texture name of this texture.
     * @return The name of this texture in EGLContext.*/
    public int getTexName() {
        return mTexIds[mTexId];
    }

    /**To attach this texture onto EGLContext By SubClass.
     * @param index : Index of this context in array of mTexIds.*/
    protected abstract void doAttachToGL(int index);

    @Override
    public void attachToGL() {
        requestTexId();
        doAttachToGL(mTexId);
    }

    @Override
    public void detachFromGL() {
        synchronized (mTexIds) {
            do {
                if (mTexId == -1) break;
                if (mTexIds[mTexId] == RETAINED || mTexIds[mTexId] == UNUSED) break;
                GLES20.glDeleteTextures(1, mTexIds, mTexId);
            } while (true);
            mTexIds[mTexId] = UNUSED;
            mTexId = -1;
        }
    }
}