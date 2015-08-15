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

package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.camera.CameraActivity;
import com.tct.camera.R;

/**
 * Shows controls at the bottom of the screen for editing, viewing a photo
 * sphere image and creating a tiny planet from a photo sphere image.
 */
public class FilmstripBottomControls extends RelativeLayout
    implements CameraActivity.OnActionBarVisibilityListener {

    /**
     * Classes implementing this interface can listen for events on the bottom
     * controls.
     */
    public static interface BottomControlsListener {
        /**
         * Called when the user pressed the "view photosphere" button.
        public void onViewPhotoSphere();
         */

        /**
         * Called when the user pressed the "edit" button.
         */
        public void onEdit();
        public void onShare();
        public void onDelete();

        /**
         * Called when the user pressed the "tiny planet" button.
        public void onTinyPlanet();
         */
    }

    private BottomControlsListener mListener;
    private LinearLayout mBottomLayout;
    private ImageButton mEditButton;
    private ImageButton mShareButton;
    private ImageButton mDelButton;
//    private ImageButton mViewPhotoSphereButton;
//    private ImageButton mTinyPlanetButton;
    private View mLine;
    
    private CameraActivity mContext;

    public FilmstripBottomControls(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext =(CameraActivity)context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mBottomLayout = (LinearLayout)findViewById(R.id.filmstrip_bottom_layout);
        mLine = findViewById(R.id.filmstrip_bottom_control_line);
        mEditButton = (ImageButton)findViewById(R.id.filmstrip_bottom_control_edit);
        mEditButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onEdit();
                }
            }
        });
        mShareButton = (ImageButton)findViewById(R.id.filmstrip_bottom_control_share);
        mShareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onShare();
                }
            }
        });
        mDelButton = (ImageButton)findViewById(R.id.filmstrip_bottom_control_del);
        mDelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onDelete();
                }
            }
        });
    }

    /**
     * Sets a new or replaces an existing listener for bottom control events.
     */
    public void setListener(BottomControlsListener listener) {
        mListener = listener;
    }

    /**
     * Sets the visibility of buttons.
     * modify by minghui.hua for PR919347
     */
    public void setEditButtonVisibility(boolean visible, boolean isPhoto) {
    	boolean isShow = visible &&  !mContext.isDownInCameraPreView(); //Add by fei.gao for PR964866
    	setVisibility(mBottomLayout, isShow);
        setVisibility(mEditButton, isShow && isPhoto);
        setVisibility(mShareButton, isShow);
        setVisibility(mDelButton, isShow);
        setVisibility(mLine, isShow && mContext.checkDeviceHasNavigationBar());//Modify by fei.gao for PR988884 
    }

    /**
     * Sets the visibility of the view-photosphere button.
     */
    public void setViewPhotoSphereButtonVisibility(boolean visible) {
//        setVisibility(mViewPhotoSphereButton, visible);
    }

    /**
     * Sets the visibility of the tiny-planet button.
     */
    public void setTinyPlanetButtonVisibility(final boolean visible) {
//        setVisibility(mTinyPlanetButton, visible);
    }

    /**
     * Sets the visibility of the given view.
     */
    private static void setVisibility(final View view, final boolean visible) {
        view.post(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(visible ? View.VISIBLE
                        : View.GONE);
            }
        });
    }

    @Override
    public void onActionBarVisibilityChanged(boolean isVisible) {
        // TODO: Fade in and out
        setVisibility(isVisible ? VISIBLE : GONE);
    }
}
