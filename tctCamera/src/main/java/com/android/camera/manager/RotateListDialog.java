/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.manager;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.SettingUtils;
import com.android.camera.ui.RotateLayout;
import com.android.camera.ui.SettingDialogAdapter;
import com.jrdcamera.modeview.ModeListView;
import com.tct.camera.R;

public class RotateListDialog extends ViewManager {
    @SuppressWarnings("unused")
    private static final String TAG = "YUHUAN_RotateListDialog";
    
    private RotateLayout mRotateDialog;
    private FrameLayout mRootView;
    private View mRotateDialogTitleLayout;
    private View mRotateDialogButtonLayout;
    private TextView mRotateDialogTitle;
    private TextView mRotateDialogButton;
    private ListView mList;
    private RelativeLayout mListSettings;
    private CharSequence[] mData;
    private int index=0;
    private SettingDialogAdapter  mSettingDialogAdapter;
    private int mOritation=0;
    
    private String mTitle;
    private String mButton;
    private DialogRunnable mRunnable;
    // CR576491 ming.zhang modify begin
    private View mNeverShowAgainLayout;
    private CheckBox mNeverShowAgainCheckBox;
    private boolean mHasNeverShowAgainBox;// CR576491 ming.zhang modify
    // CR576491 ming.zhang modify end
    
    private final static int DEFAULT_CHILDCOUNT_HEIGHT=4;
    public RotateListDialog(CameraActivity context) {
        super(context, VIEW_LAYER_DIALOG);
    }
    
    @Override
    protected View getView() {
    	Log.d(TAG,"###YUHUAN###getView into ");
        View v = inflate(R.layout.rotate_list_dialog_settings, getViewLayer());
        mRootView=(FrameLayout)v.findViewById(R.id.rotate_dialog_root_layout);
        mRotateDialog = (RotateLayout) v.findViewById(R.id.rotate_dialog_layout);
        mRotateDialogTitleLayout = v.findViewById(R.id.rotate_dialog_title_layout);
        mRotateDialogButtonLayout = v.findViewById(R.id.rotate_dialog_button_layout);
        mRotateDialogTitle = (TextView) v.findViewById(R.id.rotate_dialog_title);
        mRotateDialogButton = (Button) v.findViewById(R.id.rotate_dialog_button);
        mList=new ListView(mContext);
        mList.setDivider(null);
        mListSettings =(RelativeLayout)v.findViewById(R.id.list_settings);
        
        
        
        updateData();
        updateDialogHeight();
        //mRotateDialogTitleDivider = (View) v.findViewById(R.id.rotate_dialog_title_divider);
        // CR576491 ming.zhang modify begin
        //mNeverShowAgainLayout = v.findViewById(R.id.rotate_dialog_never_show_again_layout);
        //mNeverShowAgainCheckBox = (CheckBox) v.findViewById(R.id.never_show_again_ckbox);
        // CR576491 ming.zhang modify end
        return v;
    }

    public CheckBox getNeverBox() {
        return mNeverShowAgainCheckBox;
    }
    
    public void setData(CharSequence[] mData,int index){
    	Log.d(TAG,"###YUHUAN###setData#mData.length=" + mData.length);
    	Log.d(TAG,"###YUHUAN###setData#index=" + index);
    	this.index = index;
    	this.mData=mData;
    	
    	Log.d(TAG,"###YUHUAN###setData#mSettingDialogAdapter=" + mSettingDialogAdapter);
    	if(mSettingDialogAdapter == null){
    		mSettingDialogAdapter  =  new SettingDialogAdapter(this,mContext, mData, index);
    		
    	}
    	if(mList != null){
    		mList.setAdapter(mSettingDialogAdapter);
    	}
    	mSettingDialogAdapter.notifyDataSetChanged();
    	updateDialogHeight();
    }

    private void resetRotateDialog() {
        //inflateDialogLayout();
        mRotateDialogTitleLayout.setVisibility(View.GONE);
        mRotateDialogButton.setVisibility(View.GONE);
        mRotateDialogButtonLayout.setVisibility(View.GONE);
        mList.setVisibility(View.GONE);
    }
    
    private void resetValues() {
        mTitle = null;
        mButton = null;
        mRunnable = null;
    }

    
    @Override
    protected void onRefresh() {
    	Log.d(TAG,"###YUHUAN###onRefresh#mTitle=" + mTitle);
    	Log.d(TAG,"###YUHUAN###onRefresh#mRotateDialogTitle=" + mRotateDialogTitle);
    	
        resetRotateDialog();
        if (mTitle != null && mRotateDialogTitle != null) {
            mRotateDialogTitle.setText(mTitle);
            Log.d(TAG,"###YUHUAN###onRefresh#mRotateDialogTitleLayout=" + mRotateDialogTitleLayout);
            if (mRotateDialogTitleLayout != null) {
                mRotateDialogTitleLayout.setVisibility(View.VISIBLE);
            }
        }
        
        Log.d(TAG,"###YUHUAN###onRefresh#mButton=" + mButton);
        if (mButton != null) {
            mRotateDialogButton.setText(mButton);
            mRotateDialogButton.setContentDescription(mButton);
            mRotateDialogButton.setVisibility(View.VISIBLE);
            mRotateDialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                }
            });
            mRotateDialogButtonLayout.setVisibility(View.VISIBLE);
            
            Log.d(TAG,"###YUHUAN###onRefresh#mList=" + mList);
            if(mList !=null){
            	
            	mList.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						Log.d(TAG,"###YUHUAN###onRefresh#mRunnable=" + mRunnable);
						if (mRunnable != null) {
                            mRunnable.setIndex(position);
                            mRunnable.run();
                        }
                        hide();
					}
				
            	});
            	mList.setVisibility(View.VISIBLE);
            }
        }
        // CR576491 ming.zhang modify begin
        Log.d(TAG,"###YUHUAN###onRefresh#mNeverShowAgainLayout=" + mNeverShowAgainLayout);
        if (mNeverShowAgainLayout != null) {
        	Log.d(TAG,"###YUHUAN###onRefresh#mHasNeverShowAgainBox=" + mHasNeverShowAgainBox);
            if (mHasNeverShowAgainBox) {
                mNeverShowAgainLayout.setVisibility(View.VISIBLE);
            } else {
                mNeverShowAgainLayout.setVisibility(View.GONE);
            }
        }
        // CR576491 ming.zhang modify end
        android.util.Log.d(TAG, "onRefresh() mTitle=" + mTitle + ", mMessage="  + ", mButton1="
                + mButton + ", mButton2=" + mButton + ", mRunnable1=" + mRunnable
                + ", mRunnable2=" + mRunnable);
        
        Log.d(TAG,"###YUHUAN###onRefresh#mSettingDialogAdapter=" + mSettingDialogAdapter);
        if(mSettingDialogAdapter!=null){
        	mSettingDialogAdapter.notifyDataSetChanged();
        }
        
        Log.d(TAG,"###YUHUAN###onRefresh#mRootView=" + mRootView);
        if(mRootView !=null){
        	mRootView.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					
					Log.d(TAG,"###YUHUAN###onRefresh#event.getX()=" + event.getX());
					Log.d(TAG,"###YUHUAN###onRefresh#event.getY()=" + event.getY());
					Log.d(TAG,"###YUHUAN###onRefresh#mRotateDialog.getX()=" + mRotateDialog.getX());
					Log.d(TAG,"###YUHUAN###onRefresh#mRotateDialog.getY()=" + mRotateDialog.getY());
					Log.d(TAG,"###YUHUAN###onRefresh#mRotateDialog.getWidth()=" + mRotateDialog.getWidth());
					Log.d(TAG,"###YUHUAN###onRefresh#mRotateDialog.getHeight()=" + mRotateDialog.getHeight());
					
					if ((event.getX() > (mRotateDialog.getX() + mRotateDialog
							.getWidth()) || event.getX() < mRotateDialog.getX())
							|| (event.getY() < mRotateDialog.getY() || event
									.getY() > (mRotateDialog.getY() + mRotateDialog
									.getHeight()))) {
						RotateListDialog.this.hide();
					}
					return false;
				}
			});
        }
    }
        // CR576491 ming.zhang modify begin
    public void showAlertDialog(String title, String msg, String button1Text,
                final DialogRunnable r1) {
        showAlertDialog(title, button1Text, r1, false);
    }
    public void showAlertDialog(String title,String button1Text,
                final DialogRunnable r1, boolean hasNeverShowAgainBox) {
    	Log.d(TAG,"###YUHUAN###showAlertDialog#title=" + title);
    	Log.d(TAG,"###YUHUAN###showAlertDialog#hasNeverShowAgainBox=" + hasNeverShowAgainBox);
    	
        resetValues();
        
        mTitle = title;
        mButton = button1Text;
        mRunnable = r1;
        mHasNeverShowAgainBox = hasNeverShowAgainBox;
        show();
    }
    
    @Override
    public boolean collapse(boolean force) {
    	Log.d(TAG,"###YUHUAN###collapse#force=" + force);
    	Log.d(TAG,"###YUHUAN###collapse#isShowing()=" + isShowing());
        if (isShowing()) {
            hide();
            return true;
        }
        return super.collapse(force);
    }
    
    @Override
    protected Animation getFadeInAnimation() {
        return AnimationUtils.loadAnimation(getContext(),
                R.anim.setting_popup_grow_fade_in);
    }
    
    @Override
    protected Animation getFadeOutAnimation() {
        return AnimationUtils.loadAnimation(getContext(),
                R.anim.setting_popup_shrink_fade_out);
    }
    
    private Animation mDialogFadeIn;
    private Animation mDialogFadeOut;
    protected void fadeIn() {
        if (getShowAnimationEnabled()) {
            if (mDialogFadeIn == null) {
                mDialogFadeIn = getFadeInAnimation();
            }
            if (mDialogFadeIn != null && mRotateDialog != null) {
                mRotateDialog.startAnimation(mDialogFadeIn);
            }
        }
    }

    protected void fadeOut() {
        if (getHideAnimationEnabled()) {
            if (mDialogFadeOut == null) {
                mDialogFadeOut = getFadeOutAnimation();
            }
            if (mDialogFadeOut != null && mRotateDialog != null) {
                mRotateDialog.startAnimation(mDialogFadeOut);
            }
        }
    }
    
    private void updateData(){
    	if(mData !=null){
        	mSettingDialogAdapter  =  new SettingDialogAdapter(this,mContext, mData, index);
        	mList.setAdapter(mSettingDialogAdapter);
        }
    	
    }
    public CharSequence[] getData(){
    	return mData;
    }
    public int getIndex(){
    	return index;
    }
    @Override
    public void resetToDefault() {
    	// TODO Auto-generated method stub
    	super.resetToDefault();
    	this.reloadView();
    }
    
    
    
    @Override
	public void onOrientationChanged(int orientation) {
		// TODO Auto-generated method stub
    	
    	if(mOritation != orientation){
    		mOritation=orientation;
    		updateDialogHeight();
    		
    	}
    	
		super.onOrientationChanged(orientation);
	}

	private int getListViewHeightBasedOnChildren(ListView listView) {
    	if (mSettingDialogAdapter == null) {
    	return 0;
    	}
    	int totalHeight = 0;
    	int childCount =0;
    	if(isLandscape(mOritation)){
    		childCount =DEFAULT_CHILDCOUNT_HEIGHT;
    		if(mSettingDialogAdapter.getCount() <=DEFAULT_CHILDCOUNT_HEIGHT ){
    			childCount =mSettingDialogAdapter.getCount();
    		}
    	}else{
    		childCount = mSettingDialogAdapter.getCount();
    		
    	}
    	 for (int i = 0; i < childCount; i++) {
    		   View listItem = mSettingDialogAdapter.getView(i, null, listView);
    		   listItem.measure(0, 0);
    		   totalHeight += listItem.getMeasuredHeight()+listView.getDividerHeight();
    	}
    	return totalHeight;
    	}
    
    public void updateDialogHeight(){
    	if(mListSettings == null){
    		return;
    	}
    	mListSettings.removeAllViews();
    	 RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
         lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
         lp.topMargin=50;
         lp.bottomMargin=50;
         lp.height=getListViewHeightBasedOnChildren(mList);
         mListSettings.addView(mList, lp);
    }
    private boolean isLandscape(int oritation){
    	if(oritation == 270 || oritation ==90){
    		return true;
    	}else{
    		return false;
    	}
    }
}

