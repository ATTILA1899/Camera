/******************************************************************************/
/*                                                                Date:05/2013*/
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

import java.util.ArrayList;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.GC;
import com.android.camera.PhotoModule;
import com.android.camera.ui.GridViewForScrollView;
import com.android.camera.ui.ModeSettingListLayout;
import com.android.camera.ui.RotateLayout;
import com.jrdcamera.modeview.ItemOnClick;
import com.tct.camera.R;
//import com.android.camera.FeatureSwitcher;
//import com.android.camera.Util;

//import com.android.camera.FeatureSwitcher;
//import com.android.camera.Util;


public class PhotoMenuManager extends ViewManager implements GridView.OnItemClickListener,ModeSettingListLayout.Listener,
                                    GridView.OnItemLongClickListener,ItemOnClick{
    private static final String TAG = "YUHUAN_PhotoMenuManager";
    private static final boolean LOG = true;

    public static interface onSharedPreferenceChangedListener {
        void onSharedPreferenceChanged();
    }

    public int selectId = -1;
    public ViewHold viewhold;
    public List<Picture> mPictures;
    private View view;
    private View viewMode;
    private View mArrowView;
    private GridView mGridView;
    private View mDriverView;
    private LinearLayout mPhotoMenuManagerVisible;
    private RotateLayout mPhotoMenuManagerRota;
    private ModeSettingListLayout mModeSettingListLayout;
    private TextView mOkBtn;
    private LinearLayout mPhotoMenuTitle;
    private ImageView mTitleArrowImg;

    private int mModeTimer=0;
    private int mCurrentTimerMode=0;
    private int mPosition = -1;
    private int mCameraIdStored = 0;
    private PictureAdapter adapter;
    private onSharedPreferenceChangedListener mSharedPreferenceChangedListener;

    private int mOrientation = -1;
    private boolean needUpdateLayout = false;
    private ScrollView mSettingScrollView;

    public PhotoMenuManager(CameraActivity context) {
        super(context, VIEW_LAYER_SETTING);
    }



    public void setCurrentMode(int mode) {
        int realmode = GC.getModeIndex(mode);
        if (mGC.getCurrentMode() != realmode) {
            mContext.setCurIntent(Intent.ACTION_MAIN);
            Log.i(TAG,"LMCH0415 realmode:"+realmode);
            mGC.setCurrentMode(realmode);
            if (mContext != null) {
                mContext.notifyModeChanged(mGC.getCurrentMode());
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void setSharedPreferenceChangedListener(onSharedPreferenceChangedListener l) {
        mSharedPreferenceChangedListener = l;
    }

    @Override
    protected View getView() {
    	Log.d(TAG,"###YUHUAN###getView");
        viewMode = inflate(R.layout.mode_picker_list, VIEW_LAYER_SETTING);
        mDriverView = (View) viewMode.findViewById(R.id.diverView);
        mPhotoMenuTitle=(LinearLayout)viewMode.findViewById(R.id.setting_menu_title);
        mTitleArrowImg=(ImageView)viewMode.findViewById(R.id.setting_title_arrow);
        mPhotoMenuManagerVisible = (LinearLayout) viewMode.findViewById(R.id.mode_picker_visible);
        mPhotoMenuManagerRota = (RotateLayout) viewMode.findViewById(R.id.mode_picker_rota);//change by biao.luo
        mModeSettingListLayout = (ModeSettingListLayout) viewMode.findViewById(R.id.modeSettingLayout);//change by jiankun.liu
        mGridView = (GridView) viewMode.findViewById(R.id.mode_picker);

        mSettingScrollView=(ScrollView)viewMode.findViewById(R.id.photo_basicsetting_scrollview);
        mSettingScrollView.setBackgroundResource(R.drawable.setting_bg);
        mOkBtn = (TextView) viewMode.findViewById(R.id.setting_ok_btn);
        mOkBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                ((CameraActivity)mContext).setViewState(GC.VIEW_STATE_HIDE_SET);
            }
        });

        adapter = new PictureAdapter(GC.MODE_NAMES, GC.MODE_ICONS_NORMAL, getContext());
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnItemLongClickListener(this);
        viewMode.setVisibility(View.VISIBLE);

        mPhotoMenuManagerVisible.setVisibility(View.VISIBLE);
        mGridView.setVisibility(View.VISIBLE);
        mModeSettingListLayout.setVisibility(View.VISIBLE);
        ListHolder holder;
        boolean addrestore;
        if(mContext.isImageCaptureIntent()) {
            if(getContext().getCameraId() == GC.CAMERAID_FRONT) {
                mGridView.setNumColumns(GC.GRIDVIEW_COLUMN_FRONT);
                addrestore = false;
                holder = new ListHolder(GC.SETTING_GROUP_COMMON_MMS_FRONT);
            } else {
                mGridView.setNumColumns(GC.GRIDVIEW_COLUMN_BACK);
                holder = new ListHolder(GC.SETTING_GROUP_COMMON_FOR_MMS_SETTING);
                addrestore = false;
            }
        } else {
            if(getContext().getCameraId() == GC.CAMERAID_FRONT)
            {
                mGridView.setNumColumns(GC.GRIDVIEW_COLUMN_FRONT);
                addrestore = false;
               //[FEATURE]-Mod-BEGIN by TCTNB.(Liu Jie),12/18/2013,550519,VoiceShooting
               if(true /*wxt getContext().getResources().getString(R.string.def_camera_voiceshootingmode_value).equals("disable")*/){
                    holder = new ListHolder(GC.SETTING_GROUP_COMMON_SETTING_FRONT);
               }else{
                   holder = new ListHolder(GC.SETTING_GROUP_COMMON_SETTING_FRONT_WITH_VOICE);
               }
               //[FEATURE]-Mod-END by TCTNB.(Liu Jie)
            } else {
                mGridView.setNumColumns(GC.GRIDVIEW_COLUMN_BACK);
                switch (mGC.getCurrentMode()) {
                    case GC.MODE_PANORAMA:
                        holder = new ListHolder(GC.SETTING_GROUP_COMMON_FOR_PANORAMA_MODE_SETTING);
                        addrestore = true;
                        break;
                    default:
                        //[FEATURE]-Mod-BEGIN by TCTNB.(Liu Jie),12/18/2013,550519,VoiceShooting
                        if(true /* getContext().getResources().getString(R.string.def_camera_voiceshootingmode_value).equals("disable")*/){
                            holder = new ListHolder(GC.SETTING_GROUP_COMMON_FOR_MODE_SETTING);
                        }else{
                            holder = new ListHolder(GC.SETTING_GROUP_COMMON_SETTING_WITH_VOICE);
                        }
                        //[FEATURE]-Mod-END by TCTNB.(Liu Jie)
                        addrestore = true;
                }
            }
        }

        if(holder.mSettingKeys.length == 0) {
            mDriverView.setVisibility(View.GONE);
        }

        mModeSettingListLayout.initialize(GC.getSettingKeys(holder.mSettingKeys), addrestore);

        //[BUGFIX]-ADD-BEGIN by xuan.zhou, PR-631802, 2014-4-2
        mGridView.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (visibleItemCount < totalItemCount) {
                    if (LOG) {
                        Log.d(TAG, "zx3974, scroll missing" + visibleItemCount
                                                       + " < " + totalItemCount);
                    }
                    ((PictureAdapter) mGridView.getAdapter()).notifyDataSetChanged();
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

        });
        //[BUGFIX]-ADD-END by xuan.zhou, PR-631802, 2014-4-2
        return viewMode;
    }


    @Override
    protected void onRelease() {
    }

    @Override
    public void resetToDefault() {
        // Cancel the highlight state.
        for(int i=0; i<adapter.getCount();i++){
            Picture pic = (Picture)adapter.getItem(i);
            pic.setSelected(false);
        }
        // Reset to photo mode.
        setCurrentMode(GC.MODE_PHOTO);
        // Reload preference to update UI.
        mModeSettingListLayout.reloadPreference();
    }

    private int[] getModeArray() {
    	Log.d(TAG,"###YUHUAN###getModeArray");
        int[] modeArray = null;
        int cameraId = getContext().getCameraId();
        if(getContext().isImageCaptureIntent()){
            switch (cameraId) {
            case GC.CAMERAID_BACK:
                modeArray = GC.MODE_SET_BACK_MMS;
                break;
            case GC.CAMERAID_FRONT:
                modeArray = GC.MODE_SET_FRONT_MMS;
                break;
            }
        } else {
            switch (cameraId) {
            case GC.CAMERAID_BACK:
                modeArray = GC.MODE_SET_BACK;
                break;
            case GC.CAMERAID_FRONT:
                modeArray = GC.MODE_SET_FRONT;
                break;
            }
        }
        // [BUGFIX]-MOD-BEGIN by TCTNB(Shangyong.Zhang), 2014/01/02, PR579818.
        // filter disabled mode.
        int count = 0;
        for (int i=0; i<modeArray.length; i++) {
            if (mGC.MODE_ENABLE[modeArray[i]])
                count ++;
        }
        int[] array = new int[count];
        for(int i=0, last = 0; i<array.length; i++) {
            for(int j=last; j<modeArray.length; j++) {
                if (mGC.MODE_ENABLE[modeArray[j]]) {
                    Log.d(TAG, "zsy0102 j = " + j + " modeArray[j]= " + modeArray[j]);
                    array[i] = modeArray[j];
                    last = j + 1;
                    break;
                }
            }
        }
        return array;
        // [BUGFIX]-MOD-END by TCTNB(Shangyong.Zhang), 2014/01/02.
    }

    private int getPositionFromMode(int mode) {
        int[] array = getModeArray();
        for (int i=0; i<array.length; i++) {
            if (array[i] == mode)
                return i;
        }
        return UNKNOWN;
    }

    private void updateCameraSettingMode(int position) {
    	Log.d(TAG,"###YUHUAN###updateCameraSettingMode#position=" + position);
        int modeId = getModeArray()[position];
        Log.d(TAG,"###YUHUAN###updateCameraSettingMode#modeId=" + modeId);
        if (modeId == GC.MODE_MANUAL) {
            //do:goto the manual manu
            hide();
            //mContext.setViewState(GC.VIEW_STATE_MANUAL_MANU);
            // set new mode
            selectId = modeId;
            mModeTimer = modeId;
            mPosition = position;
            Picture pic = (Picture)adapter.getItem(mPosition);
            pic.setSelected(true);
            setCurrentMode(modeId);
            //mContext.getCameraViewManager().getCameraManualModeManager().show();
            mContext.setViewState(GC.VIEW_STATE_HIDE_SET);
            return;
        }
        if (modeId == mGC.getCurrentMode()) {
            // cancel selected mode
            // mCurrentMode=-1 add for zsl mode functional behavior
            if (position==2 && mPosition==2)
                mGC.setCurrentMode(GC.UNKNOWN);
            selectId = GC.MODE_PHOTO;
            setCurrentMode(GC.MODE_PHOTO);
            mModeTimer = GC.MODE_PHOTO;
            mPosition = UNKNOWN;
        } else {
            // set new mode
            selectId = modeId;
            mModeTimer = modeId;
            mPosition = position;
            Picture pic = (Picture)adapter.getItem(mPosition);
            pic.setSelected(true);
            setCurrentMode(modeId);
        }
        mContext.setViewState(GC.VIEW_STATE_HIDE_SET);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        for(int i=0; i<adapter.getCount();i++){
            Picture pic = (Picture)adapter.getItem(i);
            pic.setSelected(false);
        }
        switch(position){
        case 6:
            mContext.setViewState(GC.VIEW_STATE_HIDE_SET);
        	getContext().startActivity(new Intent().setClassName(getContext(),"com.tct.nightshot.NightShotActivity"));
        	break;
        case 7:
            mContext.setViewState(GC.VIEW_STATE_HIDE_SET);
        	getContext().startActivity(new Intent().setClassName(getContext(),"com.tct.beautyface.BeautyFaceActivity"));
        	break;
        case 8:
            mContext.setViewState(GC.VIEW_STATE_HIDE_SET);
        	getContext().startActivity(new Intent().setClassName(getContext(),"com.tct.scenedetector.SceneDetectorActivity"));
        	break;
        	default:
        		 updateCameraSettingMode(position);
        		break;
        }
       
    }

    @Override
    public void onRefresh() {
        //mPosition == -1 means that the current mode is photo
        if (mGC.getCurrentMode() == GC.MODE_PHOTO && adapter != null) {
            for (int i=0; i<adapter.getCount(); i++) {
                Picture pic = (Picture)adapter.getItem(i);
                pic.setSelected(false);
            }
            adapter.notifyDataSetChanged();
            mPosition = -1;
        }
        needUpdateLayout=true;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        return false;
    }

    public void show() {
        super.show();
        if(mOrientation==-1){
            updateLayout(0);
        }else{
            updateLayout(mOrientation);
        }
        mModeSettingListLayout.setSettingChangedListener(PhotoMenuManager.this);
    }



    class PictureAdapter extends BaseAdapter {
        private LayoutInflater mInflater;


        public PictureAdapter(int[] titles, int[] images, Context context) {
            super();
            mPictures = new ArrayList<Picture>();
            mInflater = LayoutInflater.from(context);
            int cameraId = getContext().getCameraId();
            int[] cameraMode = getModeArray();
            for (int j = 0; j < cameraMode.length; j++) {
                for (int i = 0; i < images.length; i++) {
                    if (i == cameraMode[j]) {
                        String title = getContext().getString(titles[i]);
                        if(title.contains("Micro"))
                        	title = "ASD";
                        Picture pic = new Picture(title, images[i]);
                        int index = GC.getModeIndex(mGC.getCurrentMode());
                        if (i == index) {
                            pic.setSelected(true);
                        }
                        mPictures.add(pic);
                    }
                }
            }
        }

        @Override
        public int getCount() {
            if (mPictures != null) {
                return mPictures.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return mPictures.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.mode_item_setting, null);
                viewhold = new ViewHold();
                viewhold.mImageView = (ImageView) convertView.findViewById(R.id.photo_icon);
                viewhold.mTextView = (TextView) convertView.findViewById(R.id.description_view);
                viewhold.mItemRightline=(View)convertView.findViewById(R.id.item_rightline);
                convertView.setTag(viewhold);
            } else {
                viewhold = (ViewHold) convertView.getTag();
            }
            Picture picture = mPictures.get(position);
            if (picture.isSelected()) {
                // [BUGFIX]-MOD-BEGIN by TCTNB(Shangyong.Zhang), 2014/01/02, PR579818.
                //viewhold.mImageView.setImageResource(GC.MODE_ICONS_HIGHTLIGHT[getModeArray()[position]]);
                viewhold.mImageView.setImageResource(GC.MODE_ICONS_NORMAL[getModeArray()[position]]);
                viewhold.mImageView.setSelected(true);
                viewhold.mTextView.setSelected(true);
            } else {
                viewhold.mImageView.setImageResource(GC.MODE_ICONS_NORMAL[getModeArray()[position]]);
                viewhold.mImageView.setSelected(false);
                viewhold.mTextView.setSelected(false);
            }
            int cameraId = getContext().getCameraId();
            if(cameraId==GC.CAMERAID_BACK){
                if((position+1)%GC.GRIDVIEW_COLUMN_BACK==0){
                    viewhold.mItemRightline.setVisibility(View.GONE);
                }
            }else if(cameraId==GC.CAMERAID_FRONT){
                if((position+1)%GC.GRIDVIEW_COLUMN_FRONT==0){
                    viewhold.mItemRightline.setVisibility(View.GONE);
                }
            }

            viewhold.mTextView.setText(mPictures.get(position).mTitle);
            //viewhold.mTextView.setTextColor(Color.WHITE);
            convertView.setEnabled(true);
            convertView.setClickable(false);
            convertView.setFocusable(false);
            convertView.setAlpha(1f);
            return convertView;
        }

        public ViewHold getHold() {
            return viewhold;
        }
    }



    class Picture {
        private String mTitle;
        private int mImageId;
        private boolean selected = false;

        public Picture(String title, int imageid) {
            super();
            this.mTitle = title;
            this.mImageId = imageid;

        }

        public String getTitle(){
            return mTitle;
        }

        public void setTitle(String title) {
            this.mTitle = title;
        }

        public int getImageId() {
            return mImageId;
        }

        public void setImageId(int imageid) {
            this.mImageId = imageid;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }


    }

    class ViewHold {
        public ImageView mImageView;
        public TextView mTextView;
        public View mItemRightline;
    }

    private class ListHolder {
        int[] mSettingKeys;
        public ListHolder(int[] keys) {
            mSettingKeys = keys;
        }
    }

    @Override
    public void onModeSettingChanged(ModeSettingListLayout settingList) {
        if (mSharedPreferenceChangedListener != null) {
            mSharedPreferenceChangedListener.onSharedPreferenceChanged();
        }
        refresh();
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (mOrientation != orientation || needUpdateLayout) {
            needUpdateLayout = false;
            mOrientation = orientation;
            updateLayout(mOrientation);
        }
    }

    private void updateLayout(int orientation) {
    	DisplayMetrics dm = new DisplayMetrics();
    	mContext.getWindowManager().getDefaultDisplay()
        .getMetrics(dm);
    	float scaledDensity = dm.scaledDensity;
        if(mSettingScrollView==null || mTitleArrowImg == null){
            return;
        }
        if (orientation == 0 ) {
            mTitleArrowImg.setBackgroundResource(R.drawable.setting_title_arrow_port);
        }else if(orientation == 180){
            mTitleArrowImg.setBackgroundResource(R.drawable.setting_title_arrow_land_green);
        }else {
            mTitleArrowImg.setBackgroundResource(R.drawable.setting_title_arrow_land);
        }
        LayoutParams lp= (LayoutParams) mSettingScrollView.getLayoutParams();
        int cameraId = mContext.getCameraId();
        if(cameraId==GC.CAMERAID_BACK){
            if (orientation == 0 || orientation == 180) {
                if(mGC.getCurrentMode() == GC.MODE_PANORAMA) {
                    lp.height = (int) (144 * scaledDensity);
                }else{
                    lp.height= (int) (266 * scaledDensity);
                }
                mSettingScrollView.setLayoutParams(lp);
            }else{
                if(mGC.getCurrentMode() == GC.MODE_PANORAMA) {
                    lp.height = (int) (144 * scaledDensity);
                }else{
                    lp.height= (int) (245 * scaledDensity);
                }
                mSettingScrollView.setLayoutParams(lp);
                mSettingScrollView.smoothScrollTo(0, 0);
            }
        }else{
            lp.height = (int) (122 * scaledDensity);
            mSettingScrollView.setLayoutParams(lp);
        }
    }

//Add by fei.gao for Camera_animation dev -begin

	@Override
	public void HDRClick() {
		// TODO Auto-generated method stub
		Log.d(TAG,"###YUHUAN###HDRClick");
		functionForClick(GC.MODE_HDR);
	}



	@Override
	public void panoramaClick() {
		// TODO Auto-generated method stub
		Log.d(TAG,"###YUHUAN###panoramaClick");
		functionForClick(GC.MODE_PANORAMA);
	}



	@Override
	public void modeClick() {
		// TODO Auto-generated method stub
		Log.d(TAG,"###YUHUAN###modeClick");
		functionForClick(GC.MODE_MANUAL);
	}



	@Override
	public void timeLapseClick() {
		// TODO Auto-generated method stub
		
		Log.d(TAG,"###YUHUAN###timeLapseClick");
//		try{
//			Intent intent = new Intent();
//			intent.setAction("com.muvee.timelapse.TIME_LAPSE_CAMERA");
//			//intent.putExtra(Constants.KEY_TIMELAPSE_INTERVAL, 1.0f);
//			///intent.putExtra(Constants.KEY_OUTPUT_RESOLUTION, "1280x720");
//			//intent.putExtra(Constants.KEY_OUTPUT_QUALITY, "MEDIUM");
//			//intent.putExtra("com.muvee.timelapse.output.resolution", "1920x1080");
//			//intent.putExtra(Constants.KEY_OUTPUT_PATH, "/storage/emulated/0/DCIM/Camera/video.mp4");
//			mContext.startActivity(intent);	
//		}catch(ActivityNotFoundException ex){
//			ex.printStackTrace();
//		}
		if(mContext.getGC().getCurrentMode() == GC.MODE_MANUAL){
    		//reset settings
    		mContext.getCameraViewManager().getCameraManualModeManager().resetISOETimeFocus();
    	}
		mContext.switchTimelapse();
		
	}



	@Override
	public void scanerClick() {
		// TODO Auto-generated method stub
		
		Log.d(TAG,"###YUHUAN###scanerClick");
		functionForClick(GC.MODE_QRCODE);
	}


	@Override
	public void photoClick() {
		// TODO Auto-generated method stub
		
		Log.d(TAG,"###YUHUAN###photoClick");
		functionForClick(GC.MODE_PHOTO);
	}

	@Override
	public void faceBeautyClick() {
		functionForClick(GC.MODE_ARCSOFT_FACEBEAUTY);
		// TODO Auto-generated method stub
//		mContext.setViewState(GC.VIEW_STATE_HIDE_SET);
////    	getContext().startActivity(new Intent().setClassName(getContext(),"com.tct.beautyface.BeautyFaceActivity"));
//    	if(getContext().getCurrentModule() instanceof PhotoModule){
//    		if(!mContext.gIsFaceBeauty){
//    			((PhotoModule)getContext().getCurrentModule()).startFB();
//    		}else{
//    			((PhotoModule)getContext().getCurrentModule()).stopFB();
//    		}
//    	}
	}
	
	@Override
	public void polaroidClick() {
		// TODO Auto-generated method stub
		functionForClick(GC.MODE_POLAROID);
	}
	
	
	@Override
	public void nightModeClick() {
		// TODO Auto-generated method stub
		 functionForClick(GC.MODE_ARCSOFT_NIGHT);
	}

	
	public void functionForClick(int modeId) {
		Log.d(TAG, "###YUHUAN###functionForClick#modeId=" + modeId);

		int curMode = mContext.getGC().getCurrentMode();
		Log.d(TAG, "###YUHUAN###functionForClick#curMode=" + curMode);

		if (modeId == GC.MODE_MANUAL) {
			// do:goto the manual manu

			// due to init prewview so show cameramodemanager again
			if (curMode == GC.MODE_PANORAMA || curMode == GC.MODE_TIMELAPSE) {
				mContext.getCameraViewManager().getTopViewManager()
						.setInitAnimationForMenu(false);
			}

			hide();
			// mContext.setViewState(GC.VIEW_STATE_MANUAL_MANU);
			// set new mode
			selectId = modeId;
			mModeTimer = modeId;
			mContext.getCameraViewManager().getTopViewManager()
					.setCurrentMode(modeId);
			if (!mContext.getCameraViewManager().getTopViewManager().mhasShowManualMode) {
				mContext.getCameraViewManager().getCameraManualModeManager()
						.show();
			}
			mContext.setViewState(GC.VIEW_STATE_HIDE_SET);

			// if infomanger is showing ,night mode and back light toast is
			// showing
			if (mContext.getCameraViewManager().getInfoManager().isShowing()) {
				mContext.getCameraViewManager().getInfoManager().hideInfo();
			}
			return;
		}
		// if (modeId == mGC.getCurrentMode()) {
		// selectId = GC.MODE_PHOTO;
		// mModeTimer = GC.MODE_PHOTO;
		// mContext.getCameraViewManager().getTopViewManager().setCurrentMode(GC.MODE_PHOTO);
		//
		// }else{

		if (mContext.getCameraViewManager().getCameraManualModeManager()
				.isShowing()) {
			mContext.getCameraViewManager().getCameraManualModeManager().hide();
		}
		if (curMode == GC.MODE_MANUAL) {
			// reset settings
			mContext.getCameraViewManager().getCameraManualModeManager()
					.resetISOETimeFocus();
		}
		mContext.setViewState(GC.VIEW_STATE_NORMAL);
		// set new mode
		selectId = modeId;
		mModeTimer = modeId;
		Log.d(TAG, "###YUHUAN###functionForClick#selectId=" + selectId);
		mContext.getCameraViewManager().getTopViewManager()
				.setCurrentMode(modeId);
		// }
		mContext.setViewState(GC.VIEW_STATE_HIDE_SET);

	}
	// Add by fei.gao for Camera_animation dev -end





	
}
