package com.android.camera.effects.gl;

import android.opengl.GLES20;

/** Helper class of render to texture framebuffer.*/
public class GLRTTFrameBuffer implements IGLStuff {
    private int[] mFrameBufferHandle = {-1};

    /**Bind screen buffer to an off-screen frame buffer, to enable render to texture.*/
    public void bind() {GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferHandle[0]);}

    /**Unbind screen buffer from off-screen frame buffer, to enable render to screen.*/
    public void unbind() {GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);}

    /** Set target texture.
     * @param texture : The target texture to render on.*/
    public void setTarget(int texture) {
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, texture, 0);
    }

    @Override
    public void attachToGL() {GLES20.glGenFramebuffers(1, mFrameBufferHandle, 0);}

    @Override
    public void detachFromGL() {
        GLES20.glDeleteFramebuffers(1, mFrameBufferHandle, 0);
        mFrameBufferHandle[0] = -1;
    }
}
