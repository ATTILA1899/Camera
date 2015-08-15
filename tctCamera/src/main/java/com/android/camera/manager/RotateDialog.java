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

package com.android.camera.manager;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.tct.camera.R;
import com.android.camera.SettingUtils;
import com.android.camera.ui.RotateLayout;
import android.widget.CheckBox;

public class RotateDialog extends ViewManager {
    @SuppressWarnings("unused")
    private static final String TAG = "RotateDialog";
    
    private RotateLayout mRotateDialog;
    private View mRotateDialogTitleLayout;
    private View mRotateDialogButtonLayout;
    private TextView mRotateDialogTitle;
    private TextView mRotateDialogText;
    private TextView mRotateDialogButton1;
    private TextView mRotateDialogButton2;
    private View mRotateDialogTitleDivider;
    
    private String mTitle;
    private String mMessage;
    private String mButton1;
    private String mButton2;
    private Runnable mRunnable1;
    private Runnable mRunnable2;
    // CR576491 ming.zhang modify begin
    private View mNeverShowAgainLayout;
    private CheckBox mNeverShowAgainCheckBox;
    private boolean mHasNeverShowAgainBox;// CR576491 ming.zhang modify
    // CR576491 ming.zhang modify end
    public RotateDialog(CameraActivity context) {
        super(context, VIEW_LAYER_NORMAL);
    }
    
    @Override
    protected View getView() {
        View v = inflate(R.layout.rotate_dialog, getViewLayer());
        mRotateDialog = (RotateLayout) v.findViewById(R.id.rotate_dialog_layout);
        mRotateDialogTitleLayout = v.findViewById(R.id.rotate_dialog_title_layout);
        mRotateDialogButtonLayout = v.findViewById(R.id.rotate_dialog_button_layout);
        mRotateDialogTitle = (TextView) v.findViewById(R.id.rotate_dialog_title);
        mRotateDialogText = (TextView) v.findViewById(R.id.rotate_dialog_text);
        mRotateDialogButton1 = (Button) v.findViewById(R.id.rotate_dialog_button1);
        mRotateDialogButton2 = (Button) v.findViewById(R.id.rotate_dialog_button2);
        //mRotateDialogTitleDivider = (View) v.findViewById(R.id.rotate_dialog_title_divider);
        // CR576491 ming.zhang modify begin
        //mNeverShowAgainLayout = v.findViewById(R.id.rotate_dialog_never_show_again_layout);
        //mNeverShowAgainCheckBox = (CheckBox) v.findViewById(R.id.never_show_again_ckbox);
        // CR576491 ming.zhang modify end
        return v;
    }

    public CheckBox getNeverBox() {
        return mNeverShowAgainCheckBox;
    }

    private void resetRotateDialog() {
        //inflateDialogLayout();
        mRotateDialogTitleLayout.setVisibility(View.GONE);
        mRotateDialogButton1.setVisibility(View.GONE);
        mRotateDialogButton2.setVisibility(View.GONE);
        mRotateDialogButtonLayout.setVisibility(View.GONE);
    }
    
    private void resetValues() {
        mTitle = null;
        mMessage = null;
        mButton1 = null;
        mButton2 = null;
        mRunnable1 = null;
        mRunnable2 = null;
    }

    @Override
    protected void onRefresh() {
        resetRotateDialog();
        if (mTitle != null && mRotateDialogTitle != null) {
            mRotateDialogTitle.setTextColor(SettingUtils.getMainColor(getContext()));
            mRotateDialogTitle.setText(mTitle);
            if (mRotateDialogTitleLayout != null) {
                mRotateDialogTitleLayout.setVisibility(View.VISIBLE);
            }
            if (mRotateDialogTitleDivider != null) {
                mRotateDialogTitleDivider.setBackgroundColor(SettingUtils.getMainColor(getContext()));
            }
        }
        if (mRotateDialogText != null) {
            mRotateDialogText.setText(mMessage);
        }
        if (mButton1 != null) {
            mRotateDialogButton1.setText(mButton1);
            mRotateDialogButton1.setContentDescription(mButton1);
            mRotateDialogButton1.setVisibility(View.VISIBLE);
            mRotateDialogButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRunnable1 != null) {
                        mRunnable1.run();
                    }
                    hide();
                }
            });
            mRotateDialogButtonLayout.setVisibility(View.VISIBLE);
        }
        if (mButton2 != null) {
            mRotateDialogButton2.setText(mButton2);
            mRotateDialogButton2.setContentDescription(mButton2);
            mRotateDialogButton2.setVisibility(View.VISIBLE);
            mRotateDialogButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRunnable2 != null) {
                        mRunnable2.run();
                    }
                    hide();
                }
            });
            mRotateDialogButtonLayout.setVisibility(View.VISIBLE);
        }
        // CR576491 ming.zhang modify begin
        if (mNeverShowAgainLayout != null) {
            if (mHasNeverShowAgainBox) {
                mNeverShowAgainLayout.setVisibility(View.VISIBLE);
            } else {
                mNeverShowAgainLayout.setVisibility(View.GONE);
            }
        }
        // CR576491 ming.zhang modify end
        android.util.Log.d(TAG, "onRefresh() mTitle=" + mTitle + ", mMessage=" + mMessage + ", mButton1="
                + mButton1 + ", mButton2=" + mButton2 + ", mRunnable1=" + mRunnable1
                + ", mRunnable2=" + mRunnable2);
    }
        // CR576491 ming.zhang modify begin
    public void showAlertDialog(String title, String msg, String button1Text,
                final Runnable r1, String button2Text, final Runnable r2) {
        showAlertDialog(title, msg, button1Text, r1, button2Text, r2, false);
    }
    public void showAlertDialog(String title, String msg, String button1Text,
                final Runnable r1, String button2Text, final Runnable r2, boolean hasNeverShowAgainBox) {
        resetValues();
        mTitle = title;
        mMessage = msg;
        mButton1 = button1Text;
        mButton2 = button2Text;
        mRunnable1 = r1;
        mRunnable2 = r2;
        mHasNeverShowAgainBox = hasNeverShowAgainBox;
        show();
    }
    
    @Override
    public boolean collapse(boolean force) {
        if (isShowing()) {
            hide();
            return true;
        }
        return super.collapse(force);
    }
    
    @Override
    protected Animation getFadeInAnimation() {
        return AnimationUtils.loadAnimation(getContext(),
                R.anim.setting_popup_grow_fade_in);
    }
    
    @Override
    protected Animation getFadeOutAnimation() {
        return AnimationUtils.loadAnimation(getContext(),
                R.anim.setting_popup_shrink_fade_out);
    }
    
    private Animation mDialogFadeIn;
    private Animation mDialogFadeOut;
    protected void fadeIn() {
        if (getShowAnimationEnabled()) {
            if (mDialogFadeIn == null) {
                mDialogFadeIn = getFadeInAnimation();
            }
            if (mDialogFadeIn != null && mRotateDialog != null) {
                mRotateDialog.startAnimation(mDialogFadeIn);
            }
        }
    }

    protected void fadeOut() {
        if (getHideAnimationEnabled()) {
            if (mDialogFadeOut == null) {
                mDialogFadeOut = getFadeOutAnimation();
            }
            if (mDialogFadeOut != null && mRotateDialog != null) {
                mRotateDialog.startAnimation(mDialogFadeOut);
            }
        }
    }
}
