package com.android.camera.manager;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import com.tct.camera.R;
import com.android.camera.CameraActivity;
import com.android.camera.GC;
import com.android.camera.SettingUtils;
import com.android.camera.util.CameraUtil;


public class ViewManager{
    private static final String TAG = "YUHUAN_ViewManager";
    private static final boolean LOG = true;//GC.LOG;

    public static final int VIEW_LAYER_ROOT = 0;
    public static final int VIEW_LAYER_BOTTOM = 1;
    public static final int VIEW_LAYER_NORMAL = 2;
    public static final int VIEW_LAYER_TOP = 3;
    public static final int VIEW_LAYER_SETTING = 4;
    public static final int VIEW_LAYER_PREVIEW = 5;
    public static final int VIEW_LAYER_DIALOG = 6;

    public static final int UNKNOWN = -1;

    protected CameraActivity mContext;
    protected GC mGC;
    private ViewGroup mViewLayerRoot;
    private ViewGroup mViewLayerBottom;
    private ViewGroup mViewLayerNormal;
    private ViewGroup mViewLayerTop;
    private ViewGroup mViewLayerSetting;
    private ViewGroup mViewLayerExpression;
    private ViewGroup mViewLayerPreview;

    private View mView;
    private final int mViewLayer;
    protected boolean mShowing;
    private int mOrientation;
    private boolean mEnabled = true;
    private boolean mFilter = true;
    private Animation mFadeIn;
    private Animation mFadeOut;
    private boolean mShowAnimationEnabled = true;
    private boolean mHideAnimationEnabled = true;
    private int mConfigOrientation = UNKNOWN;
    private View mCameraModuleRootView;

    public ViewManager(CameraActivity context, int layer) {
        mContext = context;
        mViewLayer = layer;
        mGC = context.getGC();
    }

    public ViewManager(CameraActivity context) {
        this(context, VIEW_LAYER_ROOT);
    }

    public final CameraActivity getContext() {
        return mContext;
    }

    public void setFileter(boolean filter) {
        mFilter = filter;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public boolean isShowing() {
        return mShowing;
    }

    public boolean isEnabled() {
        return mEnabled;
    }
    
    public void invalidate(){
    	if(mView!=null){
    		mView.invalidate();
    	}
    }

    public void setAnimationEnabled(boolean showAnimationEnabled, boolean hideAnimationEnabled) {
        mShowAnimationEnabled = showAnimationEnabled;
        mHideAnimationEnabled = hideAnimationEnabled;
    }

    public boolean getShowAnimationEnabled() {
        return mShowAnimationEnabled;
    }

    public boolean getHideAnimationEnabled() {
        return mHideAnimationEnabled;
    }

    public void show() {
        if (LOG) {
            Log.i(TAG, "show() " + this);
        }
        
        Log.d(TAG,"###YUHUAN###show#mView=" + mView);
        if (mView == null) {
            //mConfigOrientation = mContext.getResources().getConfiguration().orientation;
            mView = getView();
            Log.d(TAG,"###YUHUAN###show#mView2=" + mView);
            if (mView == null) return;
            addView(mView, mViewLayer);
            //Util.setOrientation(mView, mOrientation, false);
        }
        Log.d(TAG,"###YUHUAN###show#mView3=" + mView);
        Log.d(TAG,"###YUHUAN###show#mShowing=" + mShowing);
        if (mView != null && !mShowing) {
            mShowing = true;
            //setEnabled(mEnabled);
            refresh();// refresh view state
            fadeIn();
            mView.setVisibility(View.VISIBLE);
        } else if (mShowing) {
            refresh();
        }
    }

    public void getViewWithoutShow() {
        if (LOG) {
            Log.i(TAG, "getViewWithoutShow() " + this);
        }
        if (mView == null) {
            mView = getView();
            if (mView == null) return;
            addView(mView, mViewLayer);
        }
    }

    protected void hideView(View v) {
        if (v == null) return;
        v.setVisibility(View.GONE);
    }

    protected void showView(View v) {
        if (v == null) return;
        v.setVisibility(View.VISIBLE);
    }

    public void checkConfiguration() {
        /*int newConfigOrientation = mContext.getResources().getConfiguration().orientation;
        if (Log) {
            Log.i(TAG, "checkConfiguration() mConfigOrientation=" + mConfigOrientation
                    + ", newConfigOrientation=" + newConfigOrientation + ", this=" + this);
        }
        if (mConfigOrientation != UNKNOWN && newConfigOrientation != mConfigOrientation) {
            reInflate();
        }*/
    }

    protected void fadeIn() {
        if (mShowAnimationEnabled) {
            if (mFadeIn == null) {
                mFadeIn = getFadeInAnimation();
            }
            if (mFadeIn != null) {
                mView.startAnimation(mFadeIn);
            } else {
                CameraUtil.fadeIn(mView);
            }
        }
    }

    public void hide() {
		Log.i(TAG, "###YUHUAN###hide#mView" + mView + " mShowing:" + mShowing);
        if (mView != null && mShowing) {
            mShowing = false;
            mView.setVisibility(View.GONE);
            fadeOut();
        }
    }

    public void hideswift() {
        if (LOG) {
            Log.i(TAG,"hideswift() mView"+mView+" mShowing:"+mShowing);
        }
        if (mView != null && mShowing) {
            mShowing = false;
            mView.setVisibility(View.GONE);
        }
    }

    public void setViewVisibility(int visibility) {
        if (mView != null) {
            mView.setVisibility(visibility);
        }
    }

    protected void fadeOut() {
    	Log.d(TAG,"###YUHUAN###fadeOut#mHideAnimationEnabled=" + mHideAnimationEnabled);
        if (mHideAnimationEnabled) {
            if (mFadeOut == null) {
                mFadeOut = getFadeOutAnimation();
            }
            if (mFadeOut != null) {
                mView.startAnimation(mFadeOut);
            } else {
                CameraUtil.fadeOut(mView);
            }
        }
    }

    public final void reInflate() {
        boolean showing = mShowing;
        Log.d(TAG,"###YUHUAN###reInflate#showing=" + showing);
        hide();
        if (mView != null) {
            removeView(mView, mViewLayer);
        }
        onInflate();
        mView = null;
        if (showing) {
            show();
        }
    }

    public final void reInflateWithoutHide() {
        boolean showing = mShowing;
        if (mView != null) {
            removeView(mView, mViewLayer);
        }
        onInflate();
        mView = null;
        if (showing) {
            show();
        }
    }

    public final void refresh() {
    	Log.d(TAG,"###YUHUAN###refresh#mShowing=" + mShowing);
    	
        if (mShowing) {
            onRefresh();
        }
    }

    public final void release() {
        hide();
        if (mView != null) {
            removeView(mView, mViewLayer);
        }
        onRelease();
        mView = null;
        //mContext.removeViewManager(this);
        //mContext.removeOnOrientationListener(this);
    }

    public boolean collapse(boolean force) {
        return false;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
        if (mView != null) {
            mView.setEnabled(mEnabled);
            if (mFilter) {
                SettingUtils.setEnabledState(mView, mEnabled);
            }
        }
    }

    /**
     * Will be called when app call release() to unload views from view hierarchy.
     */
    protected void onRelease() {
        mContext = null;
        mViewLayerRoot= null;
        mViewLayerNormal = null;
        mViewLayerBottom = null;
        mViewLayerTop = null;
        mViewLayerSetting = null;
        mViewLayerExpression = null;
        mViewLayerPreview = null;
    }

    /**
     * Will be called when user clicked "Reset to Default" button.
     */
    public void resetToDefault() {}

    /**
     * Will be called when App call refresh and isShowing().
     */
    protected void onRefresh() {}
    protected void onInflate() {}
    public void onOrientationChanged(int orientation) {};

    /**
     * Will be called if app want to show current view which hasn't been created.
     */
    protected View getView() {
        return null;
    };

    protected Animation getFadeInAnimation() {
        return null;
    }

    protected Animation getFadeOutAnimation() {
        return null;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        return false;
    }

    /**
     * Will be called if app want to initialize something, and it's not obligatory.
     */
    public void initialize() {}

    public final View inflate(int layoutId) {
        if (LOG) {
            Log.i(TAG, "zsy927 layoutId::"+layoutId +"  mViewLayer::"+mViewLayer);
        }
        return inflate(layoutId, mViewLayer);
    }

    public View inflate(int layoutId, int layer) {
        //mViewLayerNormal, mViewLayerBottom and mViewLayerTop are same ViewGroup.
        //Here just use one to inflate child view.
        return mContext.getLayoutInflater().inflate(layoutId, mContext.getViewLayer(layer), false);
    }

    public View inflate(int layoutId, ViewGroup vgroup) {
    	Log.d(TAG,"###YUHUAN###inflate into ");
        return mContext.getLayoutInflater().inflate(layoutId, vgroup, false);
    }

    public ViewGroup getViewLayer() {
        return mContext.getViewLayer(mViewLayer);
    }

/*
    protected ViewGroup getViewLayer(int layer) {
        ViewGroup viewLayer = null;
        mCameraModuleRootView = mContext.getRootLayout();
        switch (layer) {
        case VIEW_LAYER_ROOT:
            if (mViewLayerRoot == null)
                mViewLayerRoot = (ViewGroup) mCameraModuleRootView.findViewById(R.id.camera_app_root);
            viewLayer = mViewLayerRoot;
            break;
        case VIEW_LAYER_BOTTOM:
            if (mViewLayerBottom == null)
                mViewLayerBottom = (ViewGroup) mCameraModuleRootView.findViewById(R.id.view_layer_bottom);
            viewLayer = mViewLayerBottom;
            break;
        case VIEW_LAYER_NORMAL:
            if (mViewLayerNormal == null)
                mViewLayerNormal = (ViewGroup) mCameraModuleRootView.findViewById(R.id.view_layer_normal);
            viewLayer = mViewLayerNormal;
            break;
        case VIEW_LAYER_TOP:
            if (mViewLayerTop == null)
                mViewLayerTop = (ViewGroup) mCameraModuleRootView.findViewById(R.id.view_layer_top);
            viewLayer = mViewLayerTop;
            break;
        case VIEW_LAYER_SETTING:
            if (mViewLayerSetting == null)
                mViewLayerSetting = (ViewGroup) mCameraModuleRootView.findViewById(R.id.view_layer_setting);
            viewLayer = mViewLayerSetting;
            break;
        case VIEW_LAYER_PREVIEW:
            if (mViewLayerPreview== null)
                mViewLayerPreview = (ViewGroup) mCameraModuleRootView.findViewById(R.id.view_layer_preview);
            viewLayer = mViewLayerPreview;
            break;
        default:
            throw new RuntimeException("Wrong layer:" + layer);
        }
        if (LOG) {
            Log.i(TAG, "getViewLayer(" + layer + ") return " + viewLayer);
        }
        if (viewLayer == null)
            Log.d(TAG, "zsye95 getViewLayer("+layer+") return null");
        return viewLayer;
    }
*/
    public void addView(View view, int layer) {
    	Log.d(TAG,"###YUHUAN###addView#layer=" + layer);
        ViewGroup group = mContext.getViewLayer(layer);
        Log.d(TAG,"###YUHUAN###addView#group=" + group);
        Log.d(TAG,"###YUHUAN###addView#view=" + view);
        if (group != null && view != null) {
            group.addView(view);
        }
    }

    public void removeView(View view, int layer) {
        Log.i(TAG,"###YUHUAN###removeView view:"+view+" layer:"+layer);
        ViewGroup group = mContext.getViewLayer(layer);
        if (group != null && view != null) {
            if (view instanceof ViewGroup) {
                ((ViewGroup)view).removeAllViews();
            }
            group.removeView(view);
        }
    }

    public void addView(View view) {
        addView(view, mViewLayer);
    }

    public void removeView(View view) {
        removeView(view, mViewLayer);
    }

    public void reloadView() {
        removeView(mView);
        mView = null;
        mView = getView();
        addView(mView);
        refresh();
    }

}
