package com.android.camera.effects.gl;

import com.android.camera.effects.LiveFilterInstanceHolder;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

public final class GLPreviewRootSurfaceView extends GLSurfaceView {
    private final GestureDetector mGestureDetector;
    private Point mGesturePosition = new Point();
    private GLEffectsRender mRender;

    public GLPreviewRootSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, mGestureListener, null, true);
        setPreserveEGLContextOnPause(true);
        setEGLContextClientVersion(2);
        mRender = LiveFilterInstanceHolder.getIntance().getEffectsRender();
        mRender.setRootView(this);
        setRenderer(mRender);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    private GestureDetector.SimpleOnGestureListener mGestureListener =
            new GestureDetector.SimpleOnGestureListener() {
        @Override
        public void onLongPress(MotionEvent e) {
            mGesturePosition.x = (int)e.getX();
            mGesturePosition.y = (int)e.getY();
            mRender.onLongPress(mGesturePosition);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mGesturePosition.x = (int)e.getX();
            mGesturePosition.y = (int)e.getY();
            mRender.onSingleTap(mGesturePosition);
            return true;
        }
    };
}