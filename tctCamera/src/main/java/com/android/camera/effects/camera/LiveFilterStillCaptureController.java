/**********************************************************************************************************/
/*     Modifications on Features list / Changes Request / Problems Report                                 */
/**********************************************************************************************************/
/*    date   |        author        |         Key          |     comment                                  */
/************|**********************|**********************|***********************************************/
/* 08/15/2014|       jian.zhang1    |   PR759360,759369    |Add OrientationEventListener for filter labels*/
/************|**********************|**********************|***********************************************/
package com.android.camera.effects.camera;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.CameraInfo;
import android.view.View;
import android.view.OrientationEventListener;

import com.android.camera.util.CameraUtil;
import com.android.camera.effects.EffectsUtils.StateMachine;
import com.android.camera.effects.EffectsUtils.StateMachine.SMStateCallback;
import com.android.camera.effects.EffectsUtils.StateMachine.StateHolder;
import com.android.camera.effects.EffectsUtils;
import com.android.camera.effects.FocusWindowView;
import com.android.camera.effects.camera.Components.PauseResumeAble;
import com.android.camera.effects.camera.LiveFilterCameraConfigurator.ICameraSwitchObserver;
import com.android.camera.effects.camera.LiveFilterSmartShutterButton.OnShutterButtonListener;
import com.android.camera.effects.camera.Components.I3ACallback;
import com.android.camera.effects.camera.Components.I3AControl;
import com.android.camera.effects.camera.Components.IPlatformCameraHolder;
import com.android.camera.effects.gl.GLEffectsRender;
import com.android.camera.effects.gl.GLEffectsRender.RenderViewTapListener;

/** Class of controlling the picture taken process. To manager the Auto focus,
 * Face detect to focus or touch to focus, and take picture.*/
public final class LiveFilterStillCaptureController implements PauseResumeAble,
        ICameraSwitchObserver {
    private static final String TAG = "CaptureController";

    private static final String KEY_3A_COORD_X = "3A_x-key";
    private static final String KEY_3A_COORD_Y = "3A_y-key";
    private static final String KEY_FOCUS_SUCCESS = "focus_success-key";

    private long mLastFocusTime = 0;
    private View mSnapshotBorder;
    private Point mScreenSize, mScreenCenter = new Point();
    private Matrix mTransformMatrix;
    private I3AControl m3AControl;
    private List<Area> mFocusArea = new ArrayList<Camera.Area>();
    private List<Area> mMeteringArea = new ArrayList<Camera.Area>();
    private StateMachine mStateMachine = new StateMachine();
    private FocusWindowView mFocusWindow;

    //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 Begin
    private OrientationEventListener mOrientationEventListener;
    //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 End

    //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-19,PR753337 Begin
    private boolean isAfSupport = false;
    //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-19,PR753337 End

    private final StateMachineEvents EV = new StateMachineEvents();

    private final class StateMachineEvents {
        public final int
                CAF_START = mStateMachine.newEventID("CAF start"),
                CAF_DONE = mStateMachine.newEventID("CAF done"),
                SHORT_TAP = mStateMachine.newEventID("Short tap"),
                LONG_TAP = mStateMachine.newEventID("Long tap"),
                AF_DONE = mStateMachine.newEventID("AF done"),
                BTN_DOWN = mStateMachine.newEventID("Button down"),
                BTN_LOCK3A = mStateMachine.newEventID("Button lock 3A"),
                BTN_UNLOCK3A = mStateMachine.newEventID("Button unlock 3A"),
                BTN_SNAP = mStateMachine.newEventID("Button snap"),
                SNAP_DONE = mStateMachine.newEventID("Snap done"),
                OV_BTN = mStateMachine.newEventID("Overview button pressed"),
                TILE_TAP = mStateMachine.newEventID("tile tap"),
                CAMSW_START = mStateMachine.newEventID("switch camera start"),
                CAMSW_DONE = mStateMachine.newEventID("switch camera done"),
                ON_PAUSE = mStateMachine.newEventID("on pause"),
                ON_RESUME = mStateMachine.newEventID("on resume"),
                VM_CHANGED_OV = mStateMachine.newEventID("View mode changed to Overview"),
                VM_CHANGED_SV = mStateMachine.newEventID("View mode changed to SingleView"),
                //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-19,PR753337 Begin
                AF_DISABLE = mStateMachine.newEventID("AF Disable");
                //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-19,PR753337 End
    }

    private final SMStateCallback mAFCallback = new SMStateCallback() {
        @Override
        public void onStateChanged(int state, int lastState, ContentValues values) {
            if (mAFToLock3AState.isState(lastState)) {
                mFocusWindow.setWindowLocked(false);
                return;
            }
            if (!mSingleViewState.isState(lastState)) {
                m3AControl.cancelAF();
            }
            recalculate3A(values.getAsInteger(KEY_3A_COORD_X),
                    values.getAsInteger(KEY_3A_COORD_Y));
            mFocusWindow.showStart(values.getAsInteger(KEY_3A_COORD_X),
                    values.getAsInteger(KEY_3A_COORD_Y));
        }
    };

    private final SMStateCallback mSnapCallback = new SMStateCallback() {
        @Override
        public void onStateChanged(int state, int lastState, ContentValues values) {
            //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-19,PR753337 Begin
            /*
            if (mAFToSnapState.isState(lastState) || mCAFToSnapState.isState(lastState)) {
            */
            if (mAFToSnapState.isState(lastState) || mCAFToSnapState.isState(lastState) || !isAfSupport) {
                mFocusWindow.showEnd(values.getAsBoolean(KEY_FOCUS_SUCCESS), false);
            }
            //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-19,PR753337 End
            //[BUGFIX]-Del by TCTNB,shidong.hou, 2014-08-11,PR759392 Begin
            //mSnapshotBorder.setVisibility(View.VISIBLE);
            //[BUGFIX]-Del by TCTNB,shidong.hou, 2014-08-11,PR759392 End
            m3AControl.snap();
        }
    };

    private final SMStateCallback mLock3ACallback = new SMStateCallback() {
        @Override
        public void onStateChanged(int state, int lastState, ContentValues values) {
            if (mAFToLock3AState.isState(lastState) ||
                    mCAFToLock3AState.isState(lastState)) {
                m3AControl.lock3A();
                mFocusWindow.showEnd(values.getAsBoolean(KEY_FOCUS_SUCCESS), true);
            } else if (mSingleViewState.isState(lastState)) {
                mFocusWindow.setWindowLocked(true);
                m3AControl.lock3A();
            }
        }
    };

    private final SMStateCallback mCAFCallback = new SMStateCallback() {
        @Override
        public void onStateChanged(int state, int lastState, ContentValues values) {
            if (mSingleViewState.isState(lastState)) {
                mFocusWindow.showStart(values.getAsInteger(KEY_3A_COORD_X),
                        values.getAsInteger(KEY_3A_COORD_Y));
            } else if (mCAFToLock3AState.isState(lastState)) {
                mFocusWindow.setWindowLocked(false);
            }
        }
    };

    private final SMStateCallback mAFToSnapCallback = new SMStateCallback() {
        @Override
        public void onStateChanged(int state, int lastState, ContentValues values) {
            if (mSingleViewState.isState(lastState)) {
                mFocusWindow.showStart(values.getAsInteger(KEY_3A_COORD_X),
                        values.getAsInteger(KEY_3A_COORD_Y));
                recalculate3A(values.getAsInteger(KEY_3A_COORD_X),
                        values.getAsInteger(KEY_3A_COORD_Y));
            } else if (mCAFState.isState(lastState)) {
                m3AControl.cancelAF();
                mFocusWindow.showStart(values.getAsInteger(KEY_3A_COORD_X),
                        values.getAsInteger(KEY_3A_COORD_Y));
                recalculate3A(values.getAsInteger(KEY_3A_COORD_X),
                        values.getAsInteger(KEY_3A_COORD_Y));
            } // Do nothing : mAFToLock3AState, mAFState
        }
    };

    private final SMStateCallback mOverviewCallback = new SMStateCallback() {
        @Override
        public void onStateChanged(int state, int lastState, ContentValues values) {
            //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 Begin
            if(mOrientationEventListener != null){
                mOrientationEventListener.enable();
            }
            //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 End
        }
    };

    private final SMStateCallback mCAFToSnapCallback = new SMStateCallback() {
        @Override
        public void onStateChanged(int state, int lastState, ContentValues values) {}
    };

    private final SMStateCallback mOV_PausedCallback = new SMStateCallback() {
        @Override
        public void onStateChanged(int state, int lastState, ContentValues values) {}
    };

    private final SMStateCallback mSV_PausedCallback = new SMStateCallback() {
        @Override
        public void onStateChanged(int state, int lastState, ContentValues values) {
            mFocusWindow.reset();
            //[BUGFIX]-Del by TCTNB,shidong.hou, 2014-08-11,PR759392 Begin
            //mSnapshotBorder.setVisibility(View.GONE);
            //[BUGFIX]-Del by TCTNB,shidong.hou, 2014-08-11,PR759392 End
        }
    };

    private final SMStateCallback mAFToLock3ACallback = new SMStateCallback() {
        @Override
        public void onStateChanged(int state, int lastState, ContentValues values) {
            mFocusWindow.setWindowLocked(true);
        }
    };

    private final SMStateCallback mSingleViewCallback = new SMStateCallback() {
        @Override
        public void onStateChanged(int state, int lastState, ContentValues values) {
            if (mAFState.isState(lastState)) {
                mFocusWindow.showEnd(values.getAsBoolean(KEY_FOCUS_SUCCESS), true);
            } else if (mCAFState.isState(lastState)) {
                mFocusWindow.showEnd(values.getAsBoolean(KEY_FOCUS_SUCCESS), false);
            } else if (mLock3AState.isState(lastState)) {
                mFocusWindow.reset();
                m3AControl.unlock3A();
            } else if (mSnapState.isState(lastState)) {
                //[BUGFIX]-Del by TCTNB,shidong.hou, 2014-08-11,PR759392 Begin
                //mSnapshotBorder.setVisibility(View.GONE);
                //[BUGFIX]-Del by TCTNB,shidong.hou, 2014-08-11,PR759392 End
                mFocusWindow.reset();
            } // Do nothing : mOverViewState
        }
    };

    private final SMStateCallback mCAFToLock3ACallback = new SMStateCallback() {
        @Override
        public void onStateChanged(int state, int lastState, ContentValues values) {
            mFocusWindow.setWindowLocked(true);
        }
    };

    private final SMStateCallback mOV_CamSwitchCallback = new SMStateCallback() {
        @Override
        public void onStateChanged(int state, int lastState, ContentValues values) {}
    };

    private final SMStateCallback mSV_CamSwitchCallback = new SMStateCallback() {
        @Override
        public void onStateChanged(int state, int lastState, ContentValues values) {
            if (mCAFState.isState(lastState) || mAFState.isState(lastState)) {
                mFocusWindow.reset();
            }
        }
    };

    private final SMStateCallback mViewModeSwitchCallback = new SMStateCallback() {
        @Override
        public void onStateChanged(int state, int lastState, ContentValues values) {
            mFocusWindow.reset();
        }
    };

    private final StateMachine.State mAFState =
            mStateMachine.newState(mAFCallback, "AF state");

    private final StateMachine.State mCAFState =
            mStateMachine.newState(mCAFCallback, "CAF state");

    private final StateMachine.State mSnapState =
            mStateMachine.newState(mSnapCallback, "Snap state");

    private final StateMachine.State mLock3AState =
            mStateMachine.newState(mLock3ACallback, "Lock 3A state");

    private final StateMachine.State mAFToSnapState =
            mStateMachine.newState(mAFToSnapCallback, "AF to snap state");

    private final StateMachine.State mOverViewState =
            mStateMachine.newState(mOverviewCallback, "OverView state");

    private final StateMachine.State mCAFToSnapState =
            mStateMachine.newState(mCAFToSnapCallback, "CAF to snap state");

    private final StateMachine.State mOV_PausedState =
            mStateMachine.newState(mOV_PausedCallback, "OverView paused state");

    private final StateMachine.State mSV_PausedState =
            mStateMachine.newState(mSV_PausedCallback, "SingleView paused state");

    private final StateMachine.State mAFToLock3AState =
            mStateMachine.newState(mAFToLock3ACallback, "AF to lock 3A state");

    private final StateMachine.State mSingleViewState =
            mStateMachine.newState(mSingleViewCallback, "SingleView state");

    private final StateMachine.State mCAFToLock3AState =
            mStateMachine.newState(mCAFToLock3ACallback, "CAF to lock 3A state");

    private final StateMachine.State mOV_CamSwitchState =
            mStateMachine.newState(mOV_CamSwitchCallback, "OverView cam-switch state");

    private final StateMachine.State mSV_CamSwitchState =
            mStateMachine.newState(mSV_CamSwitchCallback, "SingleView cam-switch state");

    private final StateMachine.State mViewModeSwitchState =
            mStateMachine.newState(mViewModeSwitchCallback, "ViewMode switch state");

    private OnShutterButtonListener mShutterBtnLis = new OnShutterButtonListener() {
        private StateHolder holder;

        @Override
        public void onLock3A() {
            handleEvent(EV.BTN_LOCK3A);
        }

        @Override
        public void onUnlock3A() {
            handleEvent(EV.BTN_UNLOCK3A);
        }

        @Override
        public void onCapture() {
            //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-19,PR753337 Begin
            /*
            handleEvent(EV.BTN_SNAP);
            */
            EffectsUtils.LogV(TAG, "onCapture isAfSupport: " + isAfSupport);
            if(!isAfSupport) {
                holder = mStateMachine.handleEvent(EV.AF_DISABLE);
                if (holder != null) {
                    // Set 3A result for this 3A done event
                    holder.values.put(KEY_FOCUS_SUCCESS, true);
                    // commit the state.
                    holder.commit();
                }
            } else {
                   handleEvent(EV.BTN_SNAP);
            }
            //[BUGFIX]-Mod by TCTNB,shidong.hou, 2014-08-19,PR753337 End
        }

        @Override
        public void onShutterButtonDown() {
            if (System.currentTimeMillis() - mLastFocusTime >
                    FocusWindowView.FOCUS_TIMEOUT) {
                holder = mStateMachine.handleEvent(EV.BTN_DOWN);
                if (holder != null) {
                    // Set AF window position for this button down event
                    holder.values.put(KEY_3A_COORD_X, mScreenCenter.x);
                    holder.values.put(KEY_3A_COORD_Y, mScreenCenter.y);
                    // commit the state.
                    holder.commit();
                }
            }
        }

        @Override
        public void onLoseFocus() {}
    };

    private RenderViewTapListener mRenderViewTapLis = new RenderViewTapListener() {
        @Override
        public void onRenderViewShortTap(Point pos) {
            StateHolder holder = mStateMachine.handleEvent(EV.SHORT_TAP);
            if (holder != null) {
                // Set tap position for this short tap event
                holder.values.put(KEY_3A_COORD_X, pos.x);
                holder.values.put(KEY_3A_COORD_Y, pos.y);
                // commit the state.
                holder.commit();
            }
        }

        @Override
        public void onRenderViewLongTap(Point pos) {
            StateHolder holder = mStateMachine.handleEvent(EV.LONG_TAP);
            if (holder != null) {
                // Set tap position for this long tap event
                holder.values.put(KEY_3A_COORD_X, pos.x);
                holder.values.put(KEY_3A_COORD_Y, pos.y);
                // commit the state.
                holder.commit();
            }
        }

        @Override
        public boolean onTileTapped() {
            return handleEvent(EV.TILE_TAP);
        }
    };

    private I3ACallback m3ACallback = new I3ACallback() {
        private StateHolder holder;

        @Override
        public void on3ADone(boolean success) {
            if (success) mLastFocusTime = System.currentTimeMillis();
            holder = mStateMachine.handleEvent(EV.AF_DONE);
            if (holder != null) {
                // Set 3A result for this 3A done event
                holder.values.put(KEY_FOCUS_SUCCESS, success);
                // commit the state.
                holder.commit();
            }
        }

        @Override
        public void onCAFState(int state) {
            switch (state) {
            case LiveFilterCameraDevice.CAF_START:
                holder = mStateMachine.handleEvent(EV.CAF_START);
                if (holder != null) {
                    // Set AF window position for this CAF state event
                    holder.values.put(KEY_3A_COORD_X, mScreenCenter.x);
                    holder.values.put(KEY_3A_COORD_Y, mScreenCenter.y);
                    // commit the state.
                    holder.commit();
                }
                break;
            case LiveFilterCameraDevice.CAF_SUCCESS:
                mLastFocusTime = System.currentTimeMillis();
            case LiveFilterCameraDevice.CAF_FAIL:
                holder = mStateMachine.handleEvent(EV.CAF_DONE);
                if (holder != null) {
                    // Set CAF result for this CAF done event
                    holder.values.put(KEY_FOCUS_SUCCESS,
                            state == LiveFilterCameraDevice.CAF_SUCCESS);
                    // commit the state.
                    holder.commit();
                }
                break;
            }
        }

        @Override
        public void onSnapDone() {
            handleEvent(EV.SNAP_DONE);
        }

        @Override
        public void onDeviceChanged(IPlatformCameraHolder holder) {
            Matrix matrix = new Matrix();
            int displayOri;
            int cameraid = holder.getCameraId();

            if (cameraid == CameraInfo.CAMERA_FACING_FRONT) {
                displayOri = (360 - holder.getCameraInfo().orientation % 360) % 360;
            } else {
                displayOri = (holder.getCameraInfo().orientation + 360) % 360;
            }

            CameraUtil.prepareMatrix(matrix, cameraid == CameraInfo.CAMERA_FACING_FRONT,
                    displayOri, mScreenSize.x, mScreenSize.y);
            matrix.invert(mTransformMatrix);
        }
    };

    public LiveFilterStillCaptureController() {
        try {
            initStateMachine();
        } catch (Exception e) {
            EffectsUtils.LogE(TAG, e.toString());
        }
        mTransformMatrix = new Matrix();
        mFocusArea.add(new Area(new Rect(), 1));
        mMeteringArea.add(new Area(new Rect(), 1));
    }

    /** Thread-Safely handling event of State machine.
     * @param event : The event to be handled by the state machine.
     * @return True if this event is accepted by the state machine, otherwise False*/
    private synchronized boolean handleEvent(int event) {
        StateHolder holder = mStateMachine.handleEvent(event);
        if (holder == null) return false;
        holder.commit();
        return true;
    }

    /** Create rule of state the machine.
     * @throws Exception */
    private void initStateMachine() throws Exception {
/*            .---overview btn---------->ViewMode switch state
 *            |---AF done--------------->SingleView state
 *            |---btn snap-------------->AF to snap state
 *  AF state--|---btn lock3A------------>AF to lock 3A
 *            |---short tap------------->AF state
 *            |---camera switch start--->Camera switch state(SingleView)
 *            |---on pause-------------->Paused state(SingleView)
 *            '---diable af------------->Snap state*/
        mAFState.addNextState(mViewModeSwitchState, EV.OV_BTN);
        mAFState.addNextState(mSingleViewState, EV.AF_DONE);
        mAFState.addNextState(mAFToSnapState, EV.BTN_SNAP);
        mAFState.addNextState(mAFToLock3AState, EV.BTN_LOCK3A);
        mAFState.addNextState(mAFState, EV.SHORT_TAP);
        mAFState.addNextState(mSV_CamSwitchState, EV.CAMSW_START);
        mAFState.addNextState(mSV_PausedState, EV.ON_PAUSE);
        //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-19,PR753337 Begin
        mAFState.addNextState(mSnapState, EV.AF_DISABLE);
        //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-19,PR753337 End

/*            .---btn snap-------------->CAF to snap state
 *            |---CAF done-------------->SingleView state
 *            |---overview btn---------->ViewMode switch state
 * CAF state--|---short tap------------->AF state
 *            |---long tap-------------->AF to snap state
 *            |---btn lock3A------------>CAF to lock 3A
 *            |---camera switch start--->Camera switch state(SingleView)
 *            '---on pause-------------->Paused state(SingleView)*/
        mCAFState.addNextState(mCAFToSnapState, EV.BTN_SNAP);
        mCAFState.addNextState(mSingleViewState, EV.CAF_DONE);
        mCAFState.addNextState(mViewModeSwitchState, EV.OV_BTN);
        mCAFState.addNextState(mAFState, EV.SHORT_TAP);
        mCAFState.addNextState(mAFToSnapState, EV.LONG_TAP);
        mCAFState.addNextState(mCAFToLock3AState, EV.BTN_LOCK3A);
        mCAFState.addNextState(mSV_CamSwitchState, EV.CAMSW_START);
        mCAFState.addNextState(mSV_PausedState, EV.ON_PAUSE);

/*             .---snap done------------->SingleView state
 * Snap state--|
 *             '---on pause-------------->Paused state(SingleView)*/
        mSnapState.addNextState(mSingleViewState, EV.SNAP_DONE);
        mSnapState.addNextState(mSV_PausedState, EV.ON_PAUSE);

/*               .---btn unlock3A---------->SingleView state
 * Lock3A state--|---btn snap-------------->Snap state
 *               '---on pause-------------->Paused state(SingleView)*/
        mLock3AState.addNextState(mSingleViewState, EV.BTN_UNLOCK3A);
        mLock3AState.addNextState(mSnapState, EV.BTN_SNAP);
        mLock3AState.addNextState(mSV_PausedState, EV.ON_PAUSE);

/*                    .---AF done--------------->Snap state
 *  AF to snap state--|
 *                    '---on pause-------------->Paused state(SingleView)*/
        mAFToSnapState.addNextState(mSnapState, EV.AF_DONE);
        mAFToSnapState.addNextState(mSV_PausedState, EV.ON_PAUSE);

/*                 .---tile tap-------------->Viewmode switch state
 * OverView state--|---camera switch start--->Camera switch state(OverView)
 *                 '---on pause-------------->Paused state(OverView)*/
        mOverViewState.addNextState(mViewModeSwitchState, EV.TILE_TAP);
        mOverViewState.addNextState(mOV_CamSwitchState, EV.CAMSW_START);
        mOverViewState.addNextState(mOV_PausedState, EV.ON_PAUSE);

/*                    .---CAF done------------->Snap state
 * CAF to snap state--|
 *                    '---on pause------------->Paused state(SingleView)*/
        mCAFToSnapState.addNextState(mSnapState, EV.CAF_DONE);
        mCAFToSnapState.addNextState(mSV_PausedState, EV.ON_PAUSE);

/* OverView paused state-----on resume------------->OverView state */
        mOV_PausedState.addNextState(mOverViewState, EV.ON_RESUME);

/* SingleView paused state-----on resume------------->SingleView state */
        mSV_PausedState.addNextState(mSingleViewState, EV.ON_RESUME);

/*
 *                      .---btn unlock3A-------------->AF state
 * AF to lock 3A state--|---btn snap------------------>AF to snap state
 *                      |---AF done------------------->Lock3A state
 *                      '---on pause------------------>Paused state(SingleView)
 */
        mAFToLock3AState.addNextState(mAFState, EV.BTN_UNLOCK3A);
        mAFToLock3AState.addNextState(mAFToSnapState, EV.BTN_SNAP);
        mAFToLock3AState.addNextState(mLock3AState, EV.AF_DONE);
        mAFToLock3AState.addNextState(mSV_PausedState, EV.ON_PAUSE);

/*
 *                   .---overview btn---------->ViewMode switch state
 *                   |---CAF start------------->CAF state
 *                   |---btn lock3A------------>Lock3A state
 *                   |---btn snap-------------->Snap state
 * SingleView state--|---long tap-------------->AF to snap state
 *                   |---short tap,btn down---->AF state
 *                   |---camera switch start--->Camera switch state(SingleView)
 *                   '---on pause-------------->Paused state(SingleView)
 */
        mSingleViewState.addNextState(mViewModeSwitchState, EV.OV_BTN);
        mSingleViewState.addNextState(mCAFState, EV.CAF_START);
        mSingleViewState.addNextState(mLock3AState, EV.BTN_LOCK3A);
        mSingleViewState.addNextState(mSnapState, EV.BTN_SNAP);
        mSingleViewState.addNextState(mAFToSnapState, EV.LONG_TAP);
        mSingleViewState.addNextState(mAFState, EV.SHORT_TAP, EV.BTN_DOWN);
        mSingleViewState.addNextState(mSV_CamSwitchState, EV.CAMSW_START);
        mSingleViewState.addNextState(mSV_PausedState, EV.ON_PAUSE);

/*
 *                       .---btn snap------------------>CAF to snap state
 * CAF to lock 3A state--|---btn unlock3A-------------->CAF state
 *                       |---CAF done------------------>Lock3A state
 *                       '---on pause------------------>Paused state(SingleView)
 */
        mCAFToLock3AState.addNextState(mCAFToSnapState, EV.BTN_SNAP);
        mCAFToLock3AState.addNextState(mCAFState, EV.BTN_UNLOCK3A);
        mCAFToLock3AState.addNextState(mLock3AState, EV.CAF_DONE);
        mCAFToLock3AState.addNextState(mSV_PausedState, EV.ON_PAUSE);

/*                                .---camera switch done------------->OverView state
 * Camera switch state(OverView)--|
 *                                '---on pause------------->Paused state(OverView)*/
        mOV_CamSwitchState.addNextState(mOverViewState, EV.CAMSW_DONE);
        mOV_CamSwitchState.addNextState(mOV_PausedState, EV.ON_PAUSE);

/*                                  .---camera switch done------------->SingleView state
 * Camera switch state(SingleView)--|
 *                                  '---on pause------------->Paused state(SingleView)*/
        mSV_CamSwitchState.addNextState(mSingleViewState, EV.CAMSW_DONE);
        mSV_CamSwitchState.addNextState(mSV_PausedState, EV.ON_PAUSE);

/*                        .---viewMode changed to OverView-- --->OverView state
 * ViewMode switch state--|
 *                        '---viewMode changed to SingleView---->SingleView state*/
        mViewModeSwitchState.addNextState(mOverViewState, EV.VM_CHANGED_OV);
        mViewModeSwitchState.addNextState(mSingleViewState, EV.VM_CHANGED_SV);
    }

    @Override
    public void onPause() {
        handleEvent(EV.ON_PAUSE);
    }

    @Override
    public void onResume() {
        handleEvent(EV.ON_RESUME);
    }

    @Override
    public boolean onStartSwitch() {
        return handleEvent(EV.CAMSW_START);
    }

    @Override
    public void onSwitchDone() {
        handleEvent(EV.CAMSW_DONE);
    }

    /** Called when view mode is changed.
     * @param viewmode : View mode the renderview has changed to.*/
    public void onViewModeChanged(int viewmode) {
        handleEvent(viewmode ==
                GLEffectsRender.MODE_SINGVIEW ? EV.VM_CHANGED_SV : EV.VM_CHANGED_OV);
    }

    /** Called when the OvierView button is clicked.
     * @return Return true if this operation is accepted, otherwise false.*/
    public boolean onOverViewButtonClicked() {return handleEvent(EV.OV_BTN);}

    /** Get {@link OnShutterButtonListener} from the
     * {@link LiveFilterStillCaptureController}.
     * @return {@link OnShutterButtonListener} to listen on the event of shutter button.*/
    public OnShutterButtonListener getShutterButtonListener() {
        return mShutterBtnLis;
    }

    /** Get {@link RenderViewTapListener} from the
     * {@link LiveFilterStillCaptureController}.
     * @return {@link RenderViewTapListener} to listen on the tap event of RenderView.*/
    public RenderViewTapListener getRenderViewTapListener() {
        return mRenderViewTapLis;
    }

    /** Set screen size to this {@link LiveFilterStillCaptureController}.
     * @param screenSize : Screen size of preview window.*/
    public void setScreenSize(Point screenSize) {
        mScreenSize = screenSize;
        mScreenCenter.x = mScreenSize.x / 2;
        mScreenCenter.y = mScreenSize.y / 2;
    }

    /** Set {@link FocusWindowView} for AF/CAF/T2F.
     * @param focuswindow : The {@link FocusWindowView} to be shown at focusing.*/
    public void setFocusWindow(FocusWindowView focuswindow) {
        mFocusWindow = focuswindow;
    }

    /** Set snap shot border view to be shown at snapping..
     * @param border : The border view to be shown at snapping.*/
    public void setSnapshotBorder(View border) {
        mSnapshotBorder = border;
    }

    /** Set {@link I3AControl} control interface of the 3A device.
     * @param control : 3A control interface.*/
    public void set3AControl(I3AControl control) {
        m3AControl = control;
        m3AControl.set3ACallback(m3ACallback);
        mStateMachine.swithToState(mOV_PausedState);
    }

    //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-19,PR753337 Begin
    public void setAfSupport(boolean support){
        EffectsUtils.LogV(TAG, "setAfSupport support: " + support);
        isAfSupport = support;
    }
    //[BUGFIX]-Add by TCTNB,shidong.hou, 2014-08-19,PR753337 End

    /** To recalculate 3A, you need to make sure it is under AF or CAF mode.
     * @param x : Tap area coordinate X.
     * @param y : Tap area coordinate Y.*/
    private void recalculate3A(int x, int y) {
        calculateTapArea(1, x, y, mFocusArea.get(0).rect);
        calculateTapArea(1.5f, x, y, mMeteringArea.get(0).rect);
        m3AControl.recalculate3A(mMeteringArea, mFocusArea);
    }

    /** Calculate tap area to camera CCD area.
     * @param areaMultiple : area size.
     * @param x : touch position X.
     * @param y : touch position Y.
     * @param rect : Rect to handle the result area.*/
    private void calculateTapArea(float areaMultiple, int x, int y, Rect rect) {
        int areaWidth = (int) (mFocusWindow.getWidth() * areaMultiple);
        int areaHeight = (int) (mFocusWindow.getHeight() * areaMultiple);
        int left = CameraUtil.clamp(x - areaWidth / 2, 0, mScreenSize.x - areaWidth);
        int top = CameraUtil.clamp(y - areaHeight / 2, 0, mScreenSize.y - areaHeight);

        RectF rectF = new RectF(left, top, left + areaWidth, top + areaHeight);
        mTransformMatrix.mapRect(rectF);
        CameraUtil.rectFToRect(rectF, rect);
    }

    //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 Begin
    public void setOrientationEventListener(OrientationEventListener listener){
        mOrientationEventListener = listener;
    }
    //[BUGFIX]-Added by jian.zhang1 for PR759360,759369 2014.08.15 End
}
