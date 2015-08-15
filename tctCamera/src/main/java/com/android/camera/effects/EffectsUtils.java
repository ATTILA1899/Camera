package com.android.camera.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.camera.util.CameraUtil;
import com.android.camera.effects.camera.Components.PauseResumeAble;
import com.android.camera.ui.Rotatable;

import android.content.ContentValues;
import android.content.Context;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.opengl.GLES20;
import android.util.Log;
import android.view.OrientationEventListener;

//[BUGFIX]-Add-BEGIN by hongyuan-wang,08/06/2014,PR-750322
import android.graphics.Point;
//[BUGFIX]-Add-END by hongyuan-wang,07/06/2014,PR-750322

public final class EffectsUtils {
    private static final String BASE_TAG = "LiveFilter";

    /** Send an ERROR log message.
     * @param TAG : Used to identify the source of a log message.
     * @param msg : The message you would like logged.*/
    public static void LogE(String TAG, String msg) {
        Log.e(BASE_TAG, TAG + ": " + msg);
    }

    /** Send a VERBOSE log message.
     * @param TAG : Used to identify the source of a log message.
     * @param msg : The message you would like logged.*/
    public static void LogV(String TAG, String msg) {
        Log.v(BASE_TAG, TAG + ": " + msg);
    }

    /** Send a DEBUG log message.
     * @param TAG : Used to identify the source of a log message.
     * @param msg : The message you would like logged.*/
    public static void LogD(String TAG, String msg) {
        Log.d(BASE_TAG, TAG + ": " + msg);
    }

    /** Send a WARN log message.
     * @param TAG : Used to identify the source of a log message.
     * @param msg : The message you would like logged.*/
    public static void LogW(String TAG, String msg) {
        Log.w(BASE_TAG, TAG + ": " + msg);
    }

    public static boolean isLiveFilterSupported() {
        return true;
    }

    /** Time stamp helper class.*/
    public final static class TimeStamp {
        private static long baseTime;

        /** Reset base time to current time to start calculate time elapse.*/
        public static void reset() {baseTime = System.currentTimeMillis();}

        /** Get time elapse base on the base time.
         * @param log : The message you would like logged.
         * @return Time elapse base on the time you reset the {@link TimeStamp}.*/
        public static long elapse(String log) {
            long e = (System.currentTimeMillis() - baseTime);
            LogD("TimeStamp", log + ": " + e);
            return e;
        }
    }

    /** Get Optimal preview/picture size from a size list with given aspect ratio.
     * @param sizes : A given size list.
     * @param targetRatio : The aspect ratio.
     * @return The Optimal preview/picture size.*/
    public static Size getOptimalSize(List<Size> sizes, float aspectRatio) {
        Size maxSize = null;
        int pix, maxPixel = 0;
        for (Size size : sizes) {
//[BUGFIX]-Mod-BEGIN by hongyuan-wang,08/06/2014,PR-750322
            if (Math.abs((size.width / (float)size.height - aspectRatio)) < 1e-2) {
//[BUGFIX]-Mod-END by hongyuan-wang,08/06/2014,PR-750322
                pix = size.width * size.height;
                if (pix > maxPixel) {
                    maxPixel = pix;
                    maxSize = size;
                }
            }
        }
        if (maxSize == null) maxSize = sizes.get(sizes.size() - 1);
        return maxSize;
    }

//[BUGFIX]-Add-BEGIN by hongyuan-wang,08/06/2014,PR-750322
    /** Get Optimal preview size from a size list with given aspect ratio.
     * @param sizes : A given size list.
     * @param targetRatio : The aspect ratio.
     * @return The Optimal preview size.*/
    public static Size getOptimalPreviewSize(List<Size> sizes, Point screenSize) {
        Size maxSize = null;
        int pix, maxPixel = 0;
        float ratio = screenSize.y / (float)screenSize.x;
        for (Size size : sizes) {
            if((size.width<=screenSize.y) && (size.height<=screenSize.x)){
                if (Math.abs((size.width / (float)size.height - ratio)) < 1e-2) {
                     pix = size.width * size.height;
                     if (pix > maxPixel) {
                           maxPixel = pix;
                           maxSize = size;
                     }
                 }
             }
        }
        if (maxSize == null) maxSize = sizes.get(sizes.size() - 1);
        return maxSize;
    }
//[BUGFIX]-Add-END by hongyuan-wang,08/06/2014,PR-750322

    /** Helper method for compiling a shader.
     * @param shaderType : Type of shader to compile.
     * @param source : String presentation for shader.
     * @return id for compiled shader.
     * @exception Exception */
    public static int loadShader(int shaderType, String source) throws Exception {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                String error = GLES20.glGetShaderInfoLog(shader);
                GLES20.glDeleteShader(shader);
                EffectsUtils.LogE("loadShader", shaderType == GLES20.GL_VERTEX_SHADER ?
                        "Vertex program err!" : "Fragment program err!");
                throw new Exception(error);
            }
        }
        return shader;
    }

    /** Class defined for MainMessage types*/
    public static final class MainMessage {
        /** Main message of view mode changed.*/
        public static final int MSG_RENDER_VIEWMODE_CHANGED = 0;

        /** Main message of refresh thumbnail.*/
        public static final int MSG_REFRESH_THUMBNAIL = 1;

        /** Main message of thumbnail is loading.*/
        public static final int MSG_THUMBNAIL_LOADING = 2;

        /** Main message of thumbnail is loaded.*/
        public static final int MSG_THUMBNAIL_DONE = 3;
    }

    /** Class defined for camera device message types.*/
    public static final class CameraDevMessage {
        /** Message of start continuous auto focus.*/
        public static final int MSG_START_CAF = 0;

        /** Message of unlock 3A and start start continuous auto focus.*/
        public static final int MSG_UNLOCK_3A_START_CAF = 1;
    }

    /** Class of observing Activity state change events.*/
    public static final class ActivityStateObserver implements PauseResumeAble {
        /** List of {@link PauseResumeAble}s to be notified when Activity state changes.*/
        private ArrayList<PauseResumeAble> mPauseResumeAbles =
                new ArrayList<PauseResumeAble>();

        /** Add {@link PauseResumeAble}s to this Observer. Then they will
         * be notified when Activity state changes.
         * @param items : The {@link PauseResumeAble}s to be added.*/
        public void addFollowers(PauseResumeAble... items) {
            for (PauseResumeAble item : items) {
                mPauseResumeAbles.add(item);
            }
        }

        /** Remove all {@link PauseResumeAble}s from this Observer.*/
        public void removeAll() {
            mPauseResumeAbles.clear();
        }

        @Override
        public void onPause() {
            for (PauseResumeAble item : mPauseResumeAbles) {
                item.onPause();
            }
        }

        @Override
        public void onResume() {
            for (PauseResumeAble item : mPauseResumeAbles) {
                item.onResume();
            }
        }
    }

    /** Class of observing orientation change events.*/
    public static final class OrientationObserver extends OrientationEventListener
            implements PauseResumeAble {
        private int mOrientation = 0;

        /** List of Rotatable to be rotated when orientation changes.*/
        private final ArrayList<Rotatable> mRotatables = new ArrayList<Rotatable>();

        public OrientationObserver(Context context) {
            super(context);
        }

        /** Add Rotatables to this Observer.
         * Then they will be rotated when orientation changes.
         * @param rotatables : The Rotatables to be added.*/
        public void addFollowers(Rotatable... rotatables) {
            for (Rotatable item : rotatables) {
                mRotatables.add(item);
            }
        }

        /** Remove all Rotatables from this Observer.*/
        public void removeAll() {
            mRotatables.clear();
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == ORIENTATION_UNKNOWN) return;
            int newOrientation = CameraUtil.roundOrientation(orientation, mOrientation);
            if (mOrientation != newOrientation) {
                // Notify all Rotatable in notification list of orientation changes.
                for (Rotatable ro : mRotatables) {
                    ro.setOrientation(newOrientation, true);
                }
            }
            mOrientation = newOrientation;
        }

        /** Get camera picture orientation.
         * @param info : The CameraInfo of specific Camera.*/
        public int getCameraOrientation(CameraInfo info) {
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                return (info.orientation - mOrientation) % 360;
            } else {
                return (info.orientation + mOrientation) % 360;
            }
        }

        @Override
        public void onPause() {
            disable();
        }

        @Override
        public void onResume() {
            enable();
        }
    }

    /** Class of state machine.*/
    public static final class StateMachine {

        public static final String KEY_EVENT_ID = "eventID-key";

        /** Current state of this state machine.*/
        private State mCurrState;

        /** State holder for this state machine.*/
        private StateHolder mStateHolder;

        private ArrayList<String> mEventNames = new ArrayList<String>();

        private ArrayList<String> mStateNames = new ArrayList<String>();

        /** Class of StateHolder to hold a state to be changed.*/
        public static final class StateHolder {
            /** values to be shared between states.*/
            public ContentValues values = new ContentValues();

            /** The trigger event.*/
            private int event;

            /** The instance of StateMachine.*/
            private StateMachine mMS;

            /** commit to change state.*/
            public void commit() {
                mMS.swithToState(mMS.mCurrState.getNextState(event)
                        .trigger(mMS.mCurrState.mStateID, values));
            }
        }

        /** Interface definition for state change callback.*/
        public static interface SMStateCallback {
            /** Called when state changed.
             * @param state : current state.
             * @param lastState : last state.
             * @param values : share data to be shared between states.*/
            public void onStateChanged(int state, int lastState, ContentValues values);
        }

        /** Class of State for State Machine.*/
        public static final class State {
            /** State Id of this state.*/
            private int mStateID;

            /** Callback for this State.*/
            private SMStateCallback mCallback;

            /** The supported next state and its trigger.*/
            private Map<Integer, State> mNextStates = new HashMap<Integer, State>();

            /** Construct a State with an given state id and a callback.
             * @param id : The state id.
             * @param cb : The state callback.*/
            private State(int id, SMStateCallback cb) {
                mStateID = id;
                mCallback = cb;
            }

            /** Trigger this state, to active its callback.
             * @param lastState : The last state to trigger this state.
             * @param values : The share data, share by last state.
             * @return The state is triggered.*/
            private State trigger(int lastState, ContentValues values) {
                mCallback.onStateChanged(mStateID, lastState, values);
                return this;
            }

            /** Check whether the state can be accepted by this state.
             * @param event : The specific event.
             * @param Return true if the event is acceptable otherwise false*/
            private boolean isAcceptable(int event) {return mNextStates.containsKey(event);}

            /** Get the next state when a specific event arrives.
             * Must check the event by {@link State#isAcceptable(int)} before call this.
             * @param event : The specific event.
             * @return The next state when the given event arrives.*/
            private State getNextState(int event) {return mNextStates.get(event);}

            /** Check a state id.
             * @param state : The state id.
             * @return true if the given state id is equal to this state id.*/
            public boolean isState(int state) {
                return state == mStateID;
            }

            /** Add a supported next state and triggers.
             * @param nextState : The possible next state.
             * @param events : Triggers to make the state machine change from this state to the
             * next state.*/
            public void addNextState(State nextState, int... events) throws Exception {
                for (int event : events) {
                    if (mNextStates.containsKey(event)) {
                        throw new Exception("Duplicate event id : " + event);
                    }
                    mNextStates.put(event, nextState);
                }
            }
        }

        public StateMachine() {
            mStateHolder = new StateHolder();
            mStateHolder.mMS = this;
        }

        /** Get a instance of a new state form a instance of state machine.
         * @param : cb : State change callback for this new State.
         * @param : name : State name.
         * @return New state in this state machine.*/
        public State newState(SMStateCallback cb, String name) {
            mStateNames.add(name);
            return new State(mStateNames.size() - 1, cb);
        }

        /** Force switch state machine to a given state.*/
        public void swithToState(State initState) {mCurrState = initState;}

        /** Get a new event id.
         * @param name : name of this event.
         * @return new event id*/
        public int newEventID(String name) {
            mEventNames.add(name);
            return mEventNames.size() - 1;
        }

        /** Get current state.
         * @return Id of current state.*/
        public int currentState() {return mCurrState.mStateID;}

        /** Send a event to this state machine.
         * @param event : The specific event id. This id must return by newEventID().
         * @return StateHolder to hold the state. call holder.commit() to change current state.*/
        public StateHolder handleEvent(int event) {
            EffectsUtils.LogV("State Machine", "event \'" + mEventNames.get(event) +
                    "\' Under state \'" + mStateNames.get(mCurrState.mStateID) + "\'");
            if (mCurrState.isAcceptable(event)) {
                mStateHolder.values.clear();
                mStateHolder.event = event;
                mStateHolder.values.put(KEY_EVENT_ID, event);
                return mStateHolder;
            } else {
                EffectsUtils.LogE("State Machine", "Unacceptable event \'" + mEventNames.get(event) + "\'");
                return null;
            }
        }
    }

    /** Thread definition for handle task queue.*/
    public static final class TaskQueueThread<T> extends Thread {
        private ITaskHandler<T> mHandler;
        private TaskObserver mObserver = null;
        private ArrayList<T> mTaskQueue = new ArrayList<T>();

        /** Interface definition for task handler for the {@link TaskQueueThread}.*/
        public interface ITaskHandler<T> {
            /** Called by the {@link TaskQueueThread} with task thread, to handle the task.*/
            public void handleTask(T task);
        }

        /** Observer to observe the state of this {@link TaskQueueThread}.*/
        public interface TaskObserver {
            /** Notified when new task is added into this {@link TaskQueueThread}.
             * @param n : Current task number of this {@link TaskQueueThread}.*/
            public void onTask(int n);

            /** Notified when the {@link TaskQueueThread} become idle.*/
            public void onIdle();
        }

        /** Set {@link ITaskHandler} for this {@link TaskQueueThread}.*/
        public void setTaskHandler(ITaskHandler<T> handler) {
            mHandler = handler;
        }

        /** Set {@link TaskObserver} for this {@link TaskQueueThread}.*/
        public void setTaskObserver(TaskObserver observer) {
            mObserver = observer;
        }

        /** Add task to this {@link TaskQueueThread}.
         * @param task : The task to be added to this {@link TaskQueueThread}..*/
        public void addTask(T task) {
            synchronized (this) {
                mTaskQueue.add(task);
                if (mObserver != null) {
                    // Notify the TaskObserver of new task added to this TaskQueueThread.
                    mObserver.onTask(mTaskQueue.size());
                }
                notifyAll();
            }
        }

        /** Remove the task from this {@link TaskQueueThread}.
         * @param task : The task to be removed from this {@link TaskQueueThread}.*/
        public void removeTask(T task) {
            synchronized (mTaskQueue) {
                mTaskQueue.remove(task);
            }
        }

        @Override
        public void run() {
            if (mHandler == null) {
                LogE("TaskQueueThread", "Unspecified task handler.");
                return;
            }
            while (true) {
                T task;
                synchronized (this) {
                    if (mTaskQueue.isEmpty()) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            LogE("TaskQueueThread", e.toString());
                        }
                        continue;
                    }
                    task = mTaskQueue.remove(0);
                }
                mHandler.handleTask(task);
                if (mObserver != null) {
                    if (mTaskQueue.isEmpty()) {
                        mObserver.onIdle();
                    }
                }
            }
        }
    }
}
