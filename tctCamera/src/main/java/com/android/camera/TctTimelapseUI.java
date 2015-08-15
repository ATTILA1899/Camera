package com.android.camera;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.android.camera.ui.CameraRootView;
import com.android.camera.ui.FocusIndicatorRotateLayout;
import com.android.camera.ui.ShutterButton;
import com.android.camera.util.CameraUtil;
import com.tct.camera.R;

public class TctTimelapseUI implements 
TextureView.SurfaceTextureListener,
ShutterButton.OnShutterButtonListener,
CameraRootView.MyDisplayListener,
View.OnLayoutChangeListener{
	
	private static final String TAG = "TctTimelapseUI";
	
	private CameraActivity mActivity;
    private View mRootView;
    private TextureView mTextureView;
    private TctTimelapeController mController;
    private SurfaceTexture mSurfaceTexture;
//	private ShutterButton mShutterButton;
    private FocusIndicatorRotateLayout mFocusAreaIndicator;

	public TctTimelapseUI(CameraActivity activity, TctTimelapeController controller, View parent) {
		mActivity = activity;
		mController = controller;
		mRootView = parent;
		mActivity.getLayoutInflater().inflate(R.layout.timelapse_module, (ViewGroup) mRootView, true);
		mTextureView = (TextureView) mRootView.findViewById(R.id.preview_content);
		mTextureView.setSurfaceTextureListener(this);
        mFocusAreaIndicator = (FocusIndicatorRotateLayout) mRootView.findViewById(R.id.focus_indicator_rotate_layout);
	}

	@Override
	public void onLayoutChange(View v, int left, int top, int right,
			int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisplayChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onShutterButtonFocus(boolean pressed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onShutterButtonClick() {
		Log.i(TAG, "onShutterButtonClick");		
	}

	@Override
	public void onShutterButtonLongClick() {
		// TODO Auto-generated method stub
		
	}

	 public void setSwipingEnabled(boolean enable) {
	        mActivity.setSwipingEnabled(enable);
	    }
	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		mSurfaceTexture = surface;
		mController.onPreviewUIReady();		
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		 		
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		mSurfaceTexture = null;
		mController.onPreviewUIDestroyed();
		return true;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		// TODO Auto-generated method stub
		
	}
	
	public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

	public void onSingleTapUp(int x, int y) {
		Log.i(TAG, "onSingleTapUp XY = " + x + "," + y);

		int previewWidth = mTextureView.getWidth();
		int previewHeight = mTextureView.getHeight(); 
		startFocus(previewWidth, previewHeight, x, y);

		float xCord = (float)x/previewWidth;
		float yCord = (float)y/previewHeight;
		mController.onFocusChanged(xCord, yCord);
	}
	
	public void updateUI(boolean recording){
		if(recording){
			mActivity.getCameraViewManager().getShutterManager().mShutterButton.setImageResource(R.drawable.ic_timelapse_active);
		}else{			
			mActivity.getCameraViewManager().getShutterManager().mShutterButton.setImageResource(R.drawable.btn_shutter_timelapse);	
		}		
	}

//	public void setShutterButton(ShutterButton mShutterButton) {
//		this.mShutterButton = mShutterButton;
//	}
	
	public void onAutoFocusCompleted(final boolean success) {	
		Log.i(TAG, "onAutoFocusCompleted success = "+ success);
		mFocusAreaIndicator.post(new Runnable(){
			@Override
			public void run() {
				mFocusAreaIndicator.showSuccess(false);
				mFocusAreaIndicator.setVisibility(View.INVISIBLE);
			}
		});	
	}
	
	public void startFocus(int previewWidth, int previewHeight, int x, int y) {
        int focusWidth = 0;
        int focusHeight = 0;
        if(mFocusAreaIndicator != null){
        	mFocusAreaIndicator.setVisibility(View.VISIBLE);
            focusWidth = mFocusAreaIndicator.getWidth();
            focusHeight = mFocusAreaIndicator.getHeight();
            if (focusWidth == 0 || focusHeight == 0) {
                Log.i(TAG, "UI Component not initialized, cancel this touch");
                return;
            }
            RelativeLayout.LayoutParams p =
                    (RelativeLayout.LayoutParams) mFocusAreaIndicator.getLayoutParams();
            int left = CameraUtil.clamp(x - focusWidth / 2, 0, previewWidth - focusWidth);
            int top = CameraUtil.clamp(y - focusHeight / 2, 0, previewHeight - focusHeight);
            p.setMargins(left, top, 0, 0);

            int[] rules = p.getRules();
            rules[RelativeLayout.CENTER_IN_PARENT] = 0;
            mFocusAreaIndicator.requestLayout();
            mFocusAreaIndicator.showStart();
        }
    }

}
