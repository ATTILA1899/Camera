package com.android.camera.manager;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.GC;
import com.tct.camera.R;
import android.os.Handler;
import android.os.Message;

public class InfoManager extends ViewManager {
    private static final String TAG = "InfoManager";

    private static final int MSG_SHOW_ONSCREEN_INDICATOR = 0;
    public  static final int DELAY_MSG_SHOW_ONSCREEN_VIEW = 2 * 1000;
    public static final int DELAY_MSG_SHOW_ONSCREEN_VIEW_LONG=-1;

    private TextView mInfoView;
    private TextView mUndoView;
    private String mInfoText;
    public static final int NIGHT_MODE_DISABLE=0;
    public static final int BACK_NIGHT_DISABLE=1;
    public static final int NO_DISABLE=-1;
    private int isUndoStatus=NO_DISABLE;

    public InfoManager(CameraActivity context,int viewlayer) {
        super(context,viewlayer);
    }

    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case MSG_SHOW_ONSCREEN_INDICATOR:
                hideInfo();
                break;
            }
        }
    };

    @Override
    protected View getView() {
        View view = inflate(R.layout.onscreen_info);
        mInfoView = (TextView)view.findViewById(R.id.info_view);
        mUndoView = (TextView)view.findViewById(R.id.undo_view);
        mUndoView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(mInfoView.getText().equals(mContext.getResources().getString(R.string.asd_night))){
					//night mode
					isUndoStatus=NIGHT_MODE_DISABLE;
					mGC.getListPreference(GC.ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE).setValueIndex(0);
					if(mGC.getListPreference(GC.ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE).findIndexOfValue(mGC.getListPreference(GC.ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE).getValue())!=1){
						mGC.getListPreference(GC.ROW_SETTING_ARCSOFT_ASD).setValueIndex(0);
					}
					hideInfo();
					showText(mContext.getResources().getString(R.string.disable_night_mode), DELAY_MSG_SHOW_ONSCREEN_VIEW, false);
					
				}else if(mInfoView.getText().equals(mContext.getResources().getString(R.string.asd_backlight))){
					//black night mode
					isUndoStatus=BACK_NIGHT_DISABLE;
					mGC.getListPreference(GC.ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE).setValueIndex(0);
					if(mGC.getListPreference(GC.ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE).findIndexOfValue(mGC.getListPreference(GC.ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE).getValue())!=1){
						mGC.getListPreference(GC.ROW_SETTING_ARCSOFT_ASD).setValueIndex(0);
					}
					hideInfo();
					showText(mContext.getResources().getString(R.string.disable_back_light), DELAY_MSG_SHOW_ONSCREEN_VIEW, false);
					
				}
				return false;
			}
		});
        return view;
    }

    public void showText(String text,int delayMs,final boolean needUndo) {
    	mMainHandler.removeMessages(MSG_SHOW_ONSCREEN_INDICATOR);
        mInfoText = text;
        mContext.runOnUiThread(new Runnable(){
            @Override
            public void run() {
               show();
               if(needUndo){
               	if(mUndoView !=null){
               		mUndoView.setVisibility(View.VISIBLE);
               	}
               }else{
               	if(mUndoView !=null){
               		mUndoView.setVisibility(View.GONE);
               	}
               }
            }
        });
        setDuringTime(delayMs);
    }

    private void setDuringTime(int delayMs) {
        mMainHandler.removeMessages(MSG_SHOW_ONSCREEN_INDICATOR);
        if (delayMs > 0) {
            mMainHandler.sendEmptyMessageDelayed(MSG_SHOW_ONSCREEN_INDICATOR, delayMs);
        }else if(delayMs ==-1){
        	//do nothing
        } else {
            mMainHandler.sendEmptyMessage(MSG_SHOW_ONSCREEN_INDICATOR);
        }
    }

    @Override
    protected void onRefresh() {
        if (mInfoView != null) {
            mInfoView.setText(mInfoText);
            int visibility = mInfoText != null ? View.VISIBLE : View.INVISIBLE;
            mInfoView.setVisibility(visibility);
        }
    }

    public void hideInfo() {
        mContext.runOnUiThread(new Runnable(){
            @Override
            public void run() {
            hide();
            mInfoText = null;
            }
        });
    }

    public int getUodoStatus(){
    	return isUndoStatus;
    }
    
    public void setUndoStatus(){
    	isUndoStatus=NO_DISABLE;
    }
}
