package com.android.camera.effects.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.android.camera.ui.RotateImageView;

public final class LiveFilterSmartShutterButton extends RotateImageView
        implements View.OnLongClickListener{
    private boolean m3ALocked = false;
    private OnShutterButtonListener mListener;

    /**Interface definition for listener of the smart shutter button.*/
    public interface OnShutterButtonListener {
        /** Called when get a locking 3A operation.*/
        public void onLock3A();

        /** Called when get a unlocking 3A operation.*/
        public void onUnlock3A();

        /** Called when the shutter button is pressed down.*/
        public void onShutterButtonDown();

        /** Called when get a actual capture operation.*/
        public void onCapture();

        /** Called after short tap and remove.*/
        public void onLoseFocus();
    }

    public LiveFilterSmartShutterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnLongClickListener(this);
    }

    public void setShutterButtonListener(OnShutterButtonListener listener) {
        mListener = listener;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        final boolean pressed = isPressed();
        if (pressed) mListener.onShutterButtonDown();
        else {
            if (m3ALocked) {
                m3ALocked = false;
                mListener.onUnlock3A();
            } else {
                mListener.onLoseFocus();
            }
        }
    }

    @Override
    public boolean performClick() {
        boolean result = super.performClick();
        mListener.onCapture();
        return result;
    }

    @Override
    public boolean onLongClick(View useless) {
        m3ALocked = true;
        mListener.onLock3A();
        return false;
    }
}
