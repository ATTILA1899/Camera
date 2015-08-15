package com.android.camera.ui;

import com.android.camera.manager.RotateListDialog;
import com.tct.camera.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.TextView;

public class SettingDialogAdapter extends BaseAdapter{

	private static final String TAG = "YUHUAN_SettingDialogAdapter";
	private CharSequence[] list;
	private LayoutInflater inflater;
	private int checked;
	private RotateListDialog dialog;
	 
	
	public SettingDialogAdapter(RotateListDialog dialog,Context context,CharSequence[] list,int checked) {
		super();
		// TODO Auto-generated constructor stub
		inflater=LayoutInflater.from(context);
		this.list=list;
		this.checked=checked;
		this.dialog =dialog;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		Log.d(TAG,"###YUHUAN###getCount#list.length=" + list.length);
		return list.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		Log.d(TAG,"###YUHUAN###getView#position=" + position);
		Holder holder=new Holder();
		
		Log.d(TAG,"###YUHUAN###getView#convertView=" + convertView);
		if(convertView==null){
			convertView=inflater.inflate(R.layout.setting_dialog_item, null);
		}
		
		holder.item=(TextView)convertView.findViewById(R.id.dialog_text);
		holder.radio=(RadioButton)convertView.findViewById(R.id.dialog_radio);
		holder.item.setText(list[position]);
		holder.radio.setClickable(false);
		holder.radio.setFocusable(false);
		holder.radio.setButtonTintMode(Mode.SRC_IN);
		if(position==checked){
			holder.radio.setChecked(true);
			holder.radio.setButtonTintList(getColorSelect(Color.parseColor("#0DB6FF")));
		}else{
			holder.radio.setChecked(false);
			holder.radio.setButtonTintList(getColorNormal(Color.parseColor("#FFFFFF")));
		}
		
		return convertView;
	}
	
	

	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		this.list=dialog.getData();
		this.checked =dialog.getIndex();
		super.notifyDataSetChanged();
	}

	
	private ColorStateList getColorSelect(int select) {
		int[] colors = new int[] {select};
		int[][] states = new int[1][];
		states[0] = new int[] { android.R.attr.state_checked};
		ColorStateList colorList = new ColorStateList(states, colors);
		return colorList;
	}
	private ColorStateList getColorNormal(int normal) {
		int[] colors = new int[] { normal};
		int[][] states = new int[1][];
		states[0] = new int[] { android.R.attr.state_enabled};
		ColorStateList colorList = new ColorStateList(states, colors);
		return colorList;
	}

	class Holder{
		private RadioButton radio;
		private TextView item;
		
		
		public Holder() {
			super();
		}
		public Holder(RadioButton radio, TextView item) {
			super();
			this.radio = radio;
			this.item = item;
		}
		public RadioButton getRadio() {
			return radio;
		}
		public void setRadio(RadioButton radio) {
			this.radio = radio;
		}
		public TextView getItem() {
			return item;
		}
		public void setItem(TextView item) {
			this.item = item;
		}
		
		
	}
}
