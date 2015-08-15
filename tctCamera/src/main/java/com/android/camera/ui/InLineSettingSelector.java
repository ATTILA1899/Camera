package com.android.camera.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import android.util.Log;

import com.android.camera.CameraActivity;
import com.android.camera.ListPreference;
import com.android.camera.manager.DialogRunnable;
import com.android.camera.manager.RotateListDialog;
import com.tct.camera.R;

public class InLineSettingSelector extends InLineSettingItem{

	private static final String TAG = "InLineSettingSelector";
	
	private Context mContext;
	public InLineSettingSelector(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext=context;
	}
	

	private TextView mSelectedContent;
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		mSelectedContent=(TextView)findViewById(R.id.content);
		this.setOnClickListener(mOnClickListener);
	}


	

	@Override
	public void initialize(ListPreference preference) {
		super.initialize(preference);
		
		Log.d(TAG,"###YUHUAN###initialize into ");
		updateView();
	}


	@Override
	protected void updateView() {
		if(mPreference==null){
			return;
		}
		mOverrideValue = mPreference.getOverrideValue();
		Log.d(TAG,"###YUHUAN###updateView#mOverrideValue=" + mOverrideValue);
		
		int index =-1;
        if (mOverrideValue == null) {
        	index=mIndex;
        }else{
        	index= mPreference.findIndexOfValue(mOverrideValue);
        }
        if(index==-1){
            return;
        }
        mSelectedContent.setText(mPreference.getEntries()[index]);
	}
	
	private OnClickListener mOnClickListener=new OnClickListener(){

		@Override
		public void onClick(View v) {
			Log.d(TAG,"###YUHUAN###onClick#mPreference=" + mPreference);
//			AlertDialog.Builder builder=new AlertDialog.Builder(mContext,R.style.AlaterDialogTheme);
//		    builder.setTitle(mPreference.getTitle());
			if(mPreference==null){
        		return;
        	}
		    int index =-1;
		    
		    Log.d(TAG,"###YUHUAN###onClick#mOverrideValue=" + mOverrideValue);
	        if (mOverrideValue == null) {
	        	index=mIndex;
	        }else{
	        	index= mPreference.findIndexOfValue(mOverrideValue);
	        }
//		    builder.setSingleChoiceItems(mPreference.getEntries(), index,new DialogInterface.OnClickListener() {
//				
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					changeIndex(which);
//					dialog.dismiss();
//				}
//		    });
//		    
//		    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//				
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					dialog.cancel();
//				}
//			});
//		    AlertDialog dialog=builder.create();
//		    dialog.show();
	        
	        
	        DialogRunnable runnable = new DialogRunnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					super.run();
					Log.d(TAG,"###YUHUAN###onClick#getIndex=" + getIndex());
					changeIndex(getIndex());
				}
	        	
	        };
	        RotateListDialog dialog=((CameraActivity) mContext).getCameraViewManager().getRotateListDialog();
	        Log.d(TAG,"###YUHUAN###onClick#dialog=" + dialog);
	        Log.d(TAG,"###YUHUAN###onClick#index=" + index);
            Log.d(TAG,"###YUHUAN###onClick#" + mPreference.getEntries());
	        dialog.setData(mPreference.getEntries(), index);
	        dialog.showAlertDialog(mPreference.getTitle(),
                    mContext.getString(R.string.location_service_dialog_cancel), runnable,false);
	        
		}
		
	};

	
	
}
