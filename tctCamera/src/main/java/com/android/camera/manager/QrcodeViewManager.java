/******************************************************************************/
/*                                                                Date:06/2013*/
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/* Author :                                                                   */
/* Email  :                                                                   */
/* Role   :                                                                   */
/* Reference documents :                                                      */
/* -------------------------------------------------------------------------- */
/* Comments :                                                                 */
/* File     :                                                                 */
/* Labels   :                                                                 */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* ----------|----------------------|----------------------|----------------- */
/*    date   |        Author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.camera.manager;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.GC;
import com.android.camera.qrcode.QRCodeManager;
import com.android.camera.ui.RotateLayout;
import com.tct.camera.R;

public class QrcodeViewManager extends ViewManager{
    private static final String TAG = "QrcodeViewManager";
    private static final boolean LOG = GC.LOG;

    private View mMenuItemView;
    private View mScanningLine;
    private View mQRCodeArrow;
    private View mQRCodeTitleImage;
    private ScanningLineThread mScanningThread = null;
    private final static long SCANNING_DELAY = 25;
    private final static long SCANNING_STEPS = 2;
    private final static long SCANNING_MAX = 180;
    OnClickListener listenerForMenu;
    OnClickListener listener;
    OnClickListener itemListener;
  //[BUGFIX]-ADD-BEGIN by kechao.chen, PR-765640, 2014-8-27
    private PopupMenu popup;
    private boolean isShowing;
  //[BUGFIX]-ADD-BEGIN by kechao.chen, PR-765640, 2014-8-27
    private View menuLayout;
    public RotateLayout scanItem;
    private TextView scanFromPhoto;
    private TextView scanHistory;
//    private TextView scanSettings;
    private LayoutInflater inflate;
    private CameraActivity mContext;
    public LinearLayout scan;
    
    private final Handler mHandler = new MainHandler();

    /**
     * This Handler is used to post message back onto the main thread of the
     * application
     */
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        }
    }

    public QrcodeViewManager(CameraActivity context) {
        super(context);
		inflate=LayoutInflater.from(context);
		mContext =context;
        setFileter(false);
    }

    public QrcodeViewManager(CameraActivity context, int layer) {
        super(context, layer);
        inflate=LayoutInflater.from(context);
        mContext =context;
        setFileter(false);
    }


    public void hideMenu(){
        if(scanItem != null){
            if(scanItem.getVisibility() == View.VISIBLE){
                scanItem.setVisibility(View.GONE);
            }
        }
    }

	@Override
    protected View getView() {
        View view = inflate(R.layout.qrcode_view);
        mMenuItemView = view.findViewById(R.id.QRCode_MenuImage);
        
        menuLayout =inflate.inflate(R.layout.codescan_item,mContext.getViewLayer(VIEW_LAYER_DIALOG));
        scanItem =(RotateLayout)menuLayout.findViewById(R.id.codescanitem);
        scan = (LinearLayout) menuLayout.findViewById(R.id.scan);
        scanFromPhoto = (TextView) menuLayout.findViewById(R.id.scanfromphotos);
        scanHistory = (TextView) menuLayout.findViewById(R.id.scanhistory);
//        scanSettings = (TextView) menuLayout.findViewById(R.id.scansettings);
        scanFromPhoto.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				scanItem.setVisibility(View.GONE);
				mContext.pickImage();
			}
        });
        scanHistory.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		scanItem.setVisibility(View.GONE);
        		Intent intentHistory = new Intent(mContext, com.android.camera.QRCodeHistoryActivity.class);
                mContext.startActivity(intentHistory);
        	}
        });
//        scanSettings.setOnClickListener(new OnClickListener(){
//        	public void onClick(View v){
//        		scanItem.setVisibility(View.GONE);
//        		Intent intentSettings = new Intent(mContext, com.android.camera.QRCodeSettingsActivity.class);
//                mContext.startActivity(intentSettings);
//        	}
//        });
        
      //[BUGFIX]-ADD-BEGIN by kechao.chen, PR-765640, 2014-8-27
        popup = new PopupMenu(mContext, mMenuItemView);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.qrcode, popup.getMenu());
        popup.setOnMenuItemClickListener(new OnMenuItemClickListener(){
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.qrcode_scanfromphotos) {
                    mContext.pickImage();
                } else if(id == R.id.qrcode_scanhistory) {
                    Intent intent = new Intent(mContext, com.android.camera.QRCodeHistoryActivity.class);
                    mContext.startActivity(intent);
                } else if(id == R.id.qrcode_settings) {
                    Intent intent = new Intent(mContext, com.android.camera.QRCodeSettingsActivity.class);
                    mContext.startActivity(intent);
                }
                return true;
            }
        });
        popup.setOnDismissListener(new OnDismissListener(){
			@Override
			public void onDismiss(PopupMenu menu) {
                isShowing = false;				
			}
        	
        });
        
        listenerForMenu = new OnClickListener(){
            public void onClick(View view) {
            	isShowing = true;
//                popup.show();
            	scanItem.setVisibility(View.VISIBLE);
            	
            }
        };
      //[BUGFIX]-ADD-BEGIN by kechao.chen, PR-765640, 2014-8-27
        mScanningLine = view.findViewById(R.id.QRCode_ScanningLine);
        mQRCodeArrow = view.findViewById(R.id.QRCode_Arrow);
        mQRCodeTitleImage = view.findViewById(R.id.QRCode_TitleImage);
        listener = new OnClickListener() {
            public void onClick(View view) {
                mContext.getCameraViewManager().getTopViewManager().setCurrentMode(GC.MODE_PHOTO);
            }
        };

        Log.i(TAG,"LMCH04181 qrcode view:"+view);
        return view;
    }
    
  //[BUGFIX]-ADD-BEGIN by kechao.chen, PR-765640, 2014-8-27
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_MENU:
        	if(scanItem.getVisibility() == View.VISIBLE){
        		scanItem.setVisibility(View.GONE);
        	}else{
        		scanItem.setVisibility(View.VISIBLE);
        	}
        	return true;
        }
        return false;
    }
  //[BUGFIX]-ADD-BEGIN by kechao.chen, PR-765640, 2014-8-27
    
    @Override
    protected void onRelease() {
    }

    @Override
    protected void onRefresh() {
        applylisteners();
        if(mScanningThread != null) {
            mScanningThread.shutdown();
            mScanningThread = null;
        }
        mScanningThread = new ScanningLineThread();
        mScanningThread.start();
    }

    private void applylisteners() {
        if (mMenuItemView != null)
            mMenuItemView.setOnClickListener(listenerForMenu);
        if (mQRCodeArrow != null)
            mQRCodeArrow.setOnClickListener(listener);
        if (mQRCodeTitleImage != null)
            mQRCodeTitleImage.setOnClickListener(listener);
    }
    @Override
    public void setEnabled(boolean enabled) {

    }
        //To display the Scanning Line
    private class ScanningLineThread extends Thread {
        private boolean needStop = false;

        public void shutdown(){
            needStop = true;
        }

        public void run() {
            while(!needStop) {
                if(mScanningLine == null) {
                    needStop = true;
                    break;
                }
                //It will show/hide ScanningLine according to User Settings
                if(QRCodeManager.showCrossHairs(mContext)) {
                    final RelativeLayout.LayoutParams params =
                        (RelativeLayout.LayoutParams) mScanningLine.getLayoutParams();
                    params.topMargin += SCANNING_STEPS;
                    int max = (int) (mContext.getResources().getDisplayMetrics().density * SCANNING_MAX);
                    if(params.topMargin >= max) {
                        params.topMargin -= max;
                    }
                    mHandler.post(new Runnable(){
                        public void run() {
                            mScanningLine.setLayoutParams(params);
                            mScanningLine.setVisibility(View.VISIBLE);
                        }
                    });
                }
                else {
                    mHandler.post(new Runnable() {
                        public void run() {
                             mScanningLine.setVisibility(View.GONE);
                        }
                    });
                }
                try {
                    Thread.sleep(SCANNING_DELAY);
                } catch (Exception e) {
                }
            }
        }
    }
}
