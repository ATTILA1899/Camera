/*
 * Copyright (C) 2012 The Android Open Source Project
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

import java.util.Locale;

import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tct.camera.R;
import com.android.camera.CameraActivity;
import com.android.camera.GC;
import com.android.camera.SoundClips;
import com.android.camera.manager.ViewManager;

public class CountDownView extends ViewManager {

    private static final String TAG = "CAM_CountDownView";
    private static final int SET_TIMER_TEXT = 1;
    private static final int CANCEL_COUNTDOWN = 2;
    private TextView mRemainingSecondsView;
    private int mRemainingSecs = 0;
    private OnCountDownFinishedListener mListener;
    private Animation mCountDownAnim;
    private SoundPool mSoundPool;
    private int mBeepTwice;
    private int mBeepOnce;
    private boolean mPlaySound;
    private final Handler mHandler = new MainHandler();
    private CameraActivity mCameraActivity;
    private RotateLayout mRotateLayout;
    private View mCountDownView;

    // modify by minghui.hua for PR939691
    public CountDownView(CameraActivity context, int viewlayer) {
        super(context, viewlayer);
        mCameraActivity = context;
    }

    @Override
    protected View getView() {
        View view = inflate(R.layout.count_down_to_capture);
        mRotateLayout = (RotateLayout)view.findViewById(R.id.rotate_layout);
        mCountDownView = view.findViewById(R.id.count_down_to_capture);
        mRemainingSecondsView = (TextView) view.findViewById(R.id.remaining_seconds);
        mCountDownAnim = AnimationUtils.loadAnimation(mCameraActivity, R.anim.count_down_exit);
        // Load the beeps
        //mSoundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
        mSoundPool = new SoundPool(1, SoundClips.getAudioTypeForSoundPool(), 0);
        mBeepOnce = mSoundPool.load(mCameraActivity, R.raw.beep_once, 1);
        mBeepTwice = mSoundPool.load(mCameraActivity, R.raw.beep_twice, 1);
        return view;
    }

    @Override
    protected void onRefresh() {
    }

    public void setRotation(int rotation) {
        mRotateLayout.setOrientation(rotation, false);
    }

    public boolean isCountingDown() {
        return mRemainingSecs > 0;
    }

    public interface OnCountDownFinishedListener {
        public void onCountDownFinished();
        public void onCountDownCanceled();
    }

    private void remainingSecondsChanged(int newVal) {
        mRemainingSecs = newVal;
        if (newVal == 0) {
            // Countdown has finished
            hide();
            mListener.onCountDownFinished();
        } else {
            Locale locale = mCameraActivity.getResources().getConfiguration().locale;
            String localizedValue = String.format(locale, "%d", newVal);
            mRemainingSecondsView.requestLayout();//Add by fei.gao for PR997343
            mRemainingSecondsView.setText(localizedValue);
            // Fade-out animation
            mCountDownAnim.reset();
            // mRemainingSecondsView.clearAnimation();
            // mRemainingSecondsView.startAnimation(mCountDownAnim);
            mCountDownView.clearAnimation();
            mCountDownView.startAnimation(mCountDownAnim);

            // Play sound effect for the last 3 seconds of the countdown
            if (mPlaySound) {
                if (newVal == 1) {
                    mSoundPool.play(mBeepTwice, 1.0f, 1.0f, 0, 0, 1.0f);
                } else if (newVal <= 3) {
                    mSoundPool.play(mBeepOnce, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            }
            // Schedule the next remainingSecondsChanged() call in 1 second
            mHandler.sendEmptyMessageDelayed(SET_TIMER_TEXT, 1000);
        }
    }

    public void setCountDownFinishedListener(OnCountDownFinishedListener listener) {
        mListener = listener;
    }

    public void startCountDown(int sec, boolean playSound) {
        if (sec <= 0) {
            Log.w(TAG, "Invalid input for countdown timer: " + sec + " seconds");
            return;
        }
        if (mHandler.hasMessages(CANCEL_COUNTDOWN)) {
            mHandler.removeMessages(CANCEL_COUNTDOWN);
        }
        show();
        mPlaySound = playSound;
        remainingSecondsChanged(sec);
        if (mCameraActivity != null) {
            mCameraActivity.setViewState(GC.VIEW_STATE_COUNTDOWN_START);
        }
    }

    public void cancelCountDown() {
        if (mRemainingSecs > 0) {
            mRemainingSecs = 0;
            mHandler.removeMessages(SET_TIMER_TEXT);
            hide();
            mHandler.sendEmptyMessageDelayed(CANCEL_COUNTDOWN, 50);
        }
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            if (message.what == SET_TIMER_TEXT) {
                remainingSecondsChanged(mRemainingSecs -1);
            }
            else if (message.what == CANCEL_COUNTDOWN) {
                mListener.onCountDownCanceled();
                if (mCameraActivity != null &&
                    GC.MODE_EXPRESSION4 != mCameraActivity.getGC().getCurrentMode()) {
                    mCameraActivity.setViewState(GC.VIEW_STATE_COUNTDOWN_CANCEL);
                }
            }
        }
    }
}