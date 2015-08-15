package com.android.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.CameraSettings;
import com.android.camera.GC;
import com.android.camera.ListPreference;
import com.tct.camera.R;

public class InLineSettingToggleSwitch  extends InLineSettingItem{

	private CameraActivity mContext;
	
	public InLineSettingToggleSwitch(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext=(CameraActivity)context;
	}
	
	

	
	private Switch mSwitch;
	private TextView mDescription;
	private OnCheckedChangeListener mCheckedListener=new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			if(mPreference==null){
        		return;
        	}
			if (mPreference.getKey().equals(CameraSettings.KEY_VIDEO_LIMIT_FOR_MMS_KEY)) {
                //wxt mContext.getCameraViewManager().getTopViewManager().getVideoMenuManager().showVideoSetWhenChanged(desiredState);
            }
            changeIndex(isChecked ? 1 : 0);
            if(isChecked){
            	mSwitch.getThumbDrawable().setColorFilter(Color.parseColor("#0DB6FF"), Mode.MULTIPLY);
        		mSwitch.getTrackDrawable().setColorFilter(Color.parseColor("#00A5F7"), Mode.MULTIPLY);
            }else{
            	mSwitch.getThumbDrawable().setColorFilter(Color.parseColor("#F8F8F8"), Mode.MULTIPLY);
        		mSwitch.getTrackDrawable().setColorFilter(Color.parseColor("#221F1F"), Mode.MULTIPLY);
            }
            
            /*
			if (mPreference.getKey().equals(
					CameraSettings.KEY_CAMERA_ARCSOFT_ASD)) {
				// hide the view for checkbox
				if (isChecked) {
					mListener.onShow(null);
					mContext.getGC()
							.getListPreference(
									GC.ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE)
							.setValueIndex(1);
					mContext.getGC()
							.getListPreference(
									GC.ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE)
							.setValueIndex(1);

				} else {
					mListener.onDismiss(null);
				}

			}*/
		}
		
	};
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mSwitch=(Switch)findViewById(R.id.setting_toggle);
		mDescription=(TextView)findViewById(R.id.description_switch);
		mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(mPreference==null){
					return;
				}
				// TODO Auto-generated method stub
				if (mPreference.getKey().equals(CameraSettings.KEY_VIDEO_LIMIT_FOR_MMS_KEY)) {
	                //wxt mContext.getCameraViewManager().getTopViewManager().getVideoMenuManager().showVideoSetWhenChanged(desiredState);
	            }
	            changeIndex(isChecked ? 1 : 0);
	            if(isChecked){
	            	mSwitch.getThumbDrawable().setColorFilter(Color.parseColor("#0DB6FF"), Mode.MULTIPLY);
	        		mSwitch.getTrackDrawable().setColorFilter(Color.parseColor("#00A5F7"), Mode.MULTIPLY);
	            }else{
	            	mSwitch.getThumbDrawable().setColorFilter(Color.parseColor("#F8F8F8"), Mode.MULTIPLY);
	        		mSwitch.getTrackDrawable().setColorFilter(Color.parseColor("#221F1F"), Mode.MULTIPLY);
	            }
			}
		});
		this.setOnClickListener(mOnClickListener);
		
	}

	@Override
	public void initialize(ListPreference preference) {
		super.initialize(preference);
		reloadPreference();
		updateView();
		
	}
	
	
	private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            //[BUGFIX]-Add by TCTNB.(zsy,wh),03/02/2014,608031
            //wxt if(TctMutex.isTakingPicture()) return;
        	
        	 mSwitch.setOnCheckedChangeListener(mCheckedListener);
            if (mPreference != null && mPreference.isClickable() && mPreference.isEnabled()) {
                if (mListener != null) {
                    mListener.onShow(InLineSettingToggleSwitch.this);
                }
                if (mSwitch != null) {
                    mSwitch.performClick();
                }
            }
        }
    };
	
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mSwitch != null) {
            mSwitch.setEnabled(enabled);
        }
    }
    
	@Override
	protected void updateView() {
		mSwitch.setOnCheckedChangeListener(null);
		if(mPreference==null){
    		return;
    	}
        mOverrideValue = mPreference.getOverrideValue();
        if (mOverrideValue == null) {
            mSwitch.setChecked(mIndex==1);
            if(mIndex==1){
            	mSwitch.getThumbDrawable().setColorFilter(Color.parseColor("#0DB6FF"), Mode.MULTIPLY);
        		mSwitch.getTrackDrawable().setColorFilter(Color.parseColor("#00A5F7"), Mode.MULTIPLY);
            }else{
            	mSwitch.getThumbDrawable().setColorFilter(Color.parseColor("#F8F8F8"), Mode.MULTIPLY);
        		mSwitch.getTrackDrawable().setColorFilter(Color.parseColor("#221F1F"), Mode.MULTIPLY);
            }
        } else {
            int index = mPreference.findIndexOfValue(mOverrideValue);
            mSwitch.setChecked(index == 1);
        }
        
        mSwitch.setOnCheckedChangeListener(mCheckedListener);
	}
}
