package com.android.camera.effects.gl;

/**Interface for all GL stuff*/
public interface IGLStuff {
    /**To attach this GL-Stuff onto EGLContext.*/
    public void attachToGL();

    /**To detach this GL-Stuff onto EGLContext.*/
    public void detachFromGL();
}
