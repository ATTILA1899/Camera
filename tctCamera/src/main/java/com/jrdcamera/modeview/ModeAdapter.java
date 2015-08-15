package com.jrdcamera.modeview;


import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.camera.GC;
import com.android.camera.ui.RotateImageView;
import com.jrdcamera.modeview.ModeListView.ListAttr;
import com.tct.camera.R;


public class ModeAdapter extends BaseAdapter{
	private List<ListAttr> listString;
	private LayoutInflater inflate;
	
	private GC mGC;
	private Context mContext;
	private ModeListView mModeListView;

	
	public ModeAdapter(List<ListAttr> list,Context context ,ModeListView modeList) {
		super();
		// TODO Auto-generated constructor stub
		this.listString=list;
		this.mModeListView=modeList;
		this.mContext = context;
		inflate=LayoutInflater.from(context);
	}
	


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(mGC==null){
			mGC=mModeListView.getGC();
		}
		return listString.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return listString.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressLint("ResourceAsColor")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHodler viewHodler=new ViewHodler();
		if(convertView==null){
			convertView=inflate.inflate(R.layout.mode_listview_item,null);
		}
			
			viewHodler.view=(ListViewItemView)convertView.findViewById(R.id.mode_item);
			viewHodler.image=(RotateImageView)convertView.findViewById(R.id.mode_image);
			viewHodler.text=(TextView)convertView.findViewById(R.id.mode_text);
			viewHodler.text.setText(listString.get(position).getMenuName());
			
			if(mGC !=null){
				if(mGC.getCurrentMode()==listString.get(position).getMode()){
					viewHodler.image.setImageResource(listString.get(position).getMenuPic_selected());	
					viewHodler.text.setTextColor(mContext.getResources().getColor(R.color.menu_drawer_mode_list_text_select));
				}else{
						
						viewHodler.image.setImageResource(listString.get(position).getMenuPic());
						viewHodler.text.setTextColor(mContext.getResources().getColor(R.color.menu_drawer_mode_list_text_normal));
				}
				
				if(mModeListView.isFrontCamera()&& (listString.get(position).getMode()==GC.MODE_HDR || listString.get(position).getMode()==GC.MODE_PANORAMA ||listString.get(position).getMode()==GC.MODE_MANUAL ||listString.get(position).getMode()==GC.MODE_TIMELAPSE ||listString.get(position).getMode()==GC.MODE_QRCODE)){
					listString.get(position).setEnable(false);
				}else{
					listString.get(position).setEnable(listString.get(position).isOriginalEnable());
				}
				
					if(listString.get(position).isEnable()){
						convertView.setEnabled(true);
					}else{
						convertView.setEnabled(false);
						viewHodler.text.setTextColor(mContext.getResources().getColor(R.color.menu_text_disable_color));
					}
			}else{
				viewHodler.image.setImageResource(listString.get(position).getMenuPic());
			}
			convertView.setTag(position);
			
		return convertView;
	}
	
	
	
	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		this.listString=mModeListView.getList();
		super.notifyDataSetChanged();
	}
		
	
	class ViewHodler{
		private ListViewItemView view;
		private RotateImageView image;
		private TextView text;
	}

	
	
}
