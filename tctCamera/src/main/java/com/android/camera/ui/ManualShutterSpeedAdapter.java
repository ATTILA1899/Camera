package com.android.camera.ui;


import com.android.camera.CameraActivity;
import com.android.camera.manager.CameraManualModeManager;
import com.tct.camera.R;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

@SuppressLint("ResourceAsColor")
public class ManualShutterSpeedAdapter extends  BaseAdapter{

	private int onSelection=0;
	private CameraManualModeManager mCameraManualModeManager;
	private RotateLayout mRotateLayout;
	private CameraActivity mContext;
	public final static String[] exposureTimeString=new String[]{"0","1/2000","1/1000","1/500","1/250","1/125","1/60","1/30","1/15","1/8","1/4","1/2"};
	public final static String[] exposureTimeDouble=new String[]{"0","0.5","1","2","4","8","16.666666666666","33.3333333333333333","66.6666666666666667","125","250","500"};
	private LayoutInflater mInflater;
	
	public ManualShutterSpeedAdapter(CameraActivity context) {
    	this.mContext = context;
    	mInflater= LayoutInflater.from(mContext);
    }

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return exposureTimeString.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return exposureTimeString[position];
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
			convertView=mInflater.inflate(R.layout.manual_mode_recyler_textview_item,null);
		}
		viewHodler.mTextView = (TextView)convertView.findViewById(R.id.item_textview);
		viewHodler.mRotateLayout =(RotateLayout)convertView.findViewById(R.id.manual_mode_shutterspeed_rotatelayout);
		viewHodler.mDiver = (View)convertView.findViewById(R.id.sh_diver);
		if(position+1 != getCount()){
			viewHodler.mDiver.setBackgroundColor(Color.parseColor("#18000000"));
		}else{
			viewHodler.mDiver.setBackgroundColor(Color.parseColor("#00000000"));
		}
		viewHodler.mRotateLayout.setOrientation(mContext.getLastOrientation(), false);
	        if(position == 0){//Modify by zhiming.yu for PR927414
	            //viewHolder.getTextView().setText("auto");
	        	viewHodler.mTextView.setText(mContext.getString(R.string.pref_camera_iso_s_f_entry_auto));
	        }else{
	        	viewHodler.mTextView.setText(exposureTimeString[position]);
	        }
	        if(onSelection == position){
	        	viewHodler.mTextView.setTextColor(mContext.getResources().getColor(R.color.manual_mode_shutter_speed_selector));
	        }else{
	        	viewHodler.mTextView.setTextColor(mContext.getResources().getColor(R.color.manual_mode_shutter_speed_normal));
	        }
		return convertView;
	}
	
	public void setSelectionId(int position){
		onSelection = position;
	}
	
	
	
	class ViewHodler{
		private TextView mTextView;
		private RotateLayout mRotateLayout;
		private View mDiver;
		
	}
	
    
    public void setCameraManualModeManager(CameraManualModeManager mCameraManualModeManager,int onSelect){
    	this.onSelection=onSelect;
    	this.mCameraManualModeManager=mCameraManualModeManager;
    }
    
    public void resetUI(){
    	onSelection=0;//WB default value
    	notifyDataSetChanged();
    }
}
