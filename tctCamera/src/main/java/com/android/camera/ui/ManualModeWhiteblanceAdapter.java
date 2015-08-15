package com.android.camera.ui;


import com.android.camera.CameraActivity;
import com.android.camera.manager.CameraManualModeManager;
import com.jrdcamera.modeview.HorizontalListView;
import com.tct.camera.R;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ManualModeWhiteblanceAdapter extends  BaseAdapter{

	private TypedArray iconList;
	private TypedArray iconListSelect;
	private int onSelection=2;
	private CameraActivity mContext;
	private CameraManualModeManager mCameraManualModeManager;
	private LayoutInflater mInflater;
	
 
	
	public ManualModeWhiteblanceAdapter(CameraActivity mContext) {
		this.mContext = mContext;
		mInflater = LayoutInflater.from(mContext);
		iconList = mContext.getResources().obtainTypedArray(R.array.tct_whitebalance_icons);
    	iconListSelect = mContext.getResources().obtainTypedArray(R.array.tct_whitebalance_hightlight_icons);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return iconList.length();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return iconList.getDrawable(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHodler viewHodler=new ViewHodler();
		if(convertView==null){
			convertView=mInflater.inflate(R.layout.manual_mode_recyler_view_item,null);
		}
		viewHodler.mImageView = (RotateImageView)convertView.findViewById(R.id.item_iv);	
		
		viewHodler.mDiver = (View)convertView.findViewById(R.id.wb_diver);
		if(position+1 != getCount()){
			viewHodler.mDiver.setBackgroundColor(Color.parseColor("#18000000"));
		}else{
			viewHodler.mDiver.setBackgroundColor(Color.parseColor("#00000000"));
		}
		viewHodler.mImageView.setOrientation(mContext.getLastOrientation(), true);
	        if(onSelection == position){
	        	viewHodler.mImageView.setImageResource(iconListSelect.getResourceId(position, 0));
	        }else{
	        	viewHodler.mImageView.setImageResource(iconList.getResourceId(position, 0));
	        }
		return convertView;
	}
	
	
	public void setSelectionId(int position){
		onSelection = position;
	}
	
	
	class ViewHodler{
		private RotateImageView mImageView;
		private View mDiver;
		
	}

    
    public void setCameraManualModeManager(CameraManualModeManager mCameraManualModeManager,int onSelect){
    	this.onSelection=onSelect;
    	this.mCameraManualModeManager=mCameraManualModeManager;
    }
    
    public void resetUI(){
    	onSelection=2;//WB default value
    	notifyDataSetChanged();
    }

	
}
