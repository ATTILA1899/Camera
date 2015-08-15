package com.android.camera.manager;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.camera.AboutActivity;
import com.android.camera.CameraActivity;
import com.android.camera.CameraSettings;
import com.android.camera.GC;
import com.android.camera.ListPreference;
import com.android.camera.SDCard;
import com.android.camera.custom.CustomFields;
import com.android.camera.custom.CustomUtil;
import com.android.camera.manager.PhotoMenuManager.onSharedPreferenceChangedListener;
import com.android.camera.ui.InLineSettingCheckBox;
import com.android.camera.ui.InLineSettingItem;
import com.android.camera.ui.InLineSettingItem.Listener;
import com.android.camera.ui.InLineSettingRestore;
import com.android.camera.ui.InLineSettingSelector;
import com.android.camera.ui.InLineSettingToggleSwitch;
import com.jrdcamera.modeview.DrawLayout;
import com.jrdcamera.modeview.MenuDrawable;
import com.tct.camera.R;

public class OverallMenuManager extends ViewManager{
	private static MenuDrawable drawer;
    private static final String TAG = "YUHUAN_OverallMenuManager";
	public OverallMenuManager(CameraActivity context) {
		super(context,VIEW_LAYER_SETTING);
		updateMenu();
	}
	
	public void updateMenu(){
		Log.d(TAG,"###YUHUAN###updateMenu ");
		
		for(int i=0;i<PHOTO_MENU_ID_BACK.length;i++){
			PHOTO_MENU_ID_BACK[i]=0;
		}
		
		for(int i=0;i<PHOTO_MENU_ID_FRONT.length;i++){
			PHOTO_MENU_ID_FRONT[i]=0;
		}
		
		for(int i=0;i<BACK_SETTING_ID.length;i++){
			PHOTO_MENU_ID_BACK[BACK_SETTING_ID[i]]=1;
		}
		
		for(int i=0;i<FRONT_SETTING_ID.length;i++){
			PHOTO_MENU_ID_FRONT[FRONT_SETTING_ID[i]]=1;
		}
		
		
		//DEF_TCTCAMERA_SHUTTER_SOUND_VISIBLE
		//DEF_TCTCAMERA_ZSL_VISIBLE
		//DEF_ANTIBAND_VISIBLE
		
		boolean shutterSoundVisible = CustomUtil.getInstance().getBoolean(CustomFields.DEF_TCTCAMERA_SHUTTER_SOUND_VISIBLE, false);
		Log.d(TAG,"###YUHUAN###updateMenu#shutterSoundVisible=" + shutterSoundVisible);
		if(!shutterSoundVisible){
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_SHUTTER_SOUND]=0;
			
			FRONT_SETTING_ID=new int[]{
					GC.ROW_SETTING_PICTURE_SIZE_FLAG,
					GC.ROW_SETTING_SELF_TIMER,
					GC.ROW_SETTING_GPS_TAG,
					GC.ROW_SETTING_GRID,
				};
		}
		
		boolean antibandVisible = CustomUtil.getInstance().getBoolean(CustomFields.DEF_ANTIBAND_VISIBLE, false);
		Log.d(TAG,"###YUHUAN###updateMenu#antibandVisible=" + antibandVisible);
		if(!antibandVisible){
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_ANTIBANDING]=0;
		}
        //Add by zhiming.yu for PR927592 begin
		
		boolean storageVisible = CustomUtil.getInstance().getBoolean(CustomFields.DEF_STORAGESAVEPATH_SETTING_VISIBLE, true);
		Log.d(TAG,"###YUHUAN###updateMenu#storageVisible=" + storageVisible);
        if(storageVisible){
            //Log.v(TAG,"updateMenu(),DEF_STORAGESAVEPATH is VISIBLE");
            CAMERA_MENU_ID=new int[]{
                    GC.ROW_SETTING_SAVEPATH,
                    GC.ROW_SETTING_RESTORE_TO_DEFAULT
            };
        }else{
            //Log.v(TAG,"updateMenu(),DEF_STORAGESAVEPATH is gone");
            CAMERA_MENU_ID=new int[]{
                    GC.ROW_SETTING_RESTORE_TO_DEFAULT
            };
        }
		//Add by zhiming.yu for PR927592 end
        boolean mZSLVisible = CustomUtil.getInstance().getBoolean(CustomFields.DEF_TCTCAMERA_ZSL_VISIBLE, false);
        Log.d(TAG,"###YUHUAN###updateMenu#mZSLVisible=" + mZSLVisible);
		if(!mZSLVisible){
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_ZSL]=0;
		}
		
		int currMode = mContext.getGC().getCurrentMode();
		Log.d(TAG,"###YUHUAN###updateMenu#currMode=" + currMode);
		if (currMode == GC.MODE_PANORAMA) {
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_PICTURE_SIZE_FLAG] = 0;
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_SELF_TIMER] = 0;
			// modify by minghui.hua for PR971928
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_GRID] = 0;
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_EXPOSURE] = 0;// PR1000875-sichao.hu
															// added
		}
		
		if (currMode == GC.MODE_ARCSOFT_FACEBEAUTY || currMode == GC.MODE_HDR
				|| currMode == GC.MODE_MANUAL || currMode == GC.MODE_PANORAMA) {
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_ARCSOFT_ASD] = 0;
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE] = 0;
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE] = 0;
		}
		//PR932980-sichao.hu add begin
		if (currMode == GC.MODE_TIMELAPSE) {
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_PICTURE_SIZE_FLAG] = 0;
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_SELF_TIMER] = 0;
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_GPS_TAG] = 0;
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_EXPOSURE] = 0;
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_ARCSOFT_ASD] = 0;
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE] = 0;
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE] = 0;
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_FFF] = 0;
		}
		//PR932980	-sichao.hu add end

		boolean EISEnable = CustomUtil.getInstance().getBoolean(CustomFields.DEF_VIDEO_EIS_ENABLE, true);
		Log.d(TAG,"###YUHUAN###updateMenu#EISEnable=" + EISEnable);
        if (!EISEnable) {
            VIDEO_MENU_BACK_ID = new int[]{
                    GC.ROW_SETTING_VIDEO_QUALITY,
                    GC.ROW_SETTING_SOUND_RECORDING,
                };
        }

		if(mContext.getCameraId()==0){//BACK
			PHOTO_MENU_ID=PHOTO_MENU_ID_BACK;
			PHOTO_SETTING_IDs=BACK_SETTING_ID;
			VIDEO_MENUS=VIDEO_MENU_BACK_ID;
		}else{//FRONT
			PHOTO_MENU_ID=PHOTO_MENU_ID_FRONT;
			PHOTO_SETTING_IDs=FRONT_SETTING_ID;
			VIDEO_MENUS=VIDEO_MENU_FRONT_ID;
		}
		initMenus();
	}
	
	
	private static int[] PHOTO_MENU_ID;
	
	private static int[] PHOTO_SETTING_IDs;
	
	
	
	public static int[] BACK_SETTING_ID=new int[]{
		GC.ROW_SETTING_PICTURE_SIZE_FLAG,
		GC.ROW_SETTING_SELF_TIMER,
		GC.ROW_SETTING_GPS_TAG,
		GC.ROW_SETTING_SHUTTER_SOUND,
		GC.ROW_SETTING_GRID,
		GC.ROW_SETTING_EXPOSURE,
//		GC.ROW_SETTING_ARCSOFT_ASD,
//		GC.ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE,
//		GC.ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE,
//		GC.ROW_SETTING_FFF,
		GC.ROW_SETTING_ZSL,
		GC.ROW_SETTING_ANTIBANDING,
	};
	
	public static int[] PHOTO_MENU_ID_BACK=new int[GC.SETTING_ROW_COUNT];
	
	public static int[] PHOTO_MENU_ID_FRONT=new int[GC.SETTING_ROW_COUNT];
	
	public static int[] FRONT_SETTING_ID=new int[]{
		GC.ROW_SETTING_PICTURE_SIZE_FLAG,
		GC.ROW_SETTING_SELF_TIMER,
		GC.ROW_SETTING_GPS_TAG,
		GC.ROW_SETTING_SHUTTER_SOUND,
		GC.ROW_SETTING_GRID,
	};
	
	public int[] VIDEO_MENUS;
	
	public static int[] VIDEO_MENU_BACK_ID=new int[]{
		GC.ROW_SETTING_VIDEO_QUALITY,
		GC.ROW_SETTING_SOUND_RECORDING,
		// yuhuan 20150706 comment out EIS,not support
		//GC.ROW_SETTING_EIS,
	};
	
	public static int[] VIDEO_MENU_FRONT_ID=new int[]{
		GC.ROW_SETTING_VIDEO_FRONT_QUALITY,
		GC.ROW_SETTING_SOUND_RECORDING,
	};
	
    //Modify by zhiming.yu for PR927592
    public static int[] CAMERA_MENU_ID;
    /*public static int[] CAMERA_MENU_ID=new int[]{
        GC.ROW_SETTING_SAVEPATH,
        GC.ROW_SETTING_RESTORE_TO_DEFAULT
    };*/
	
	public static int[] TOGGLE_SWITCH_ITEM=new int[]{
		GC.ROW_SETTING_GPS_TAG,
		GC.ROW_SETTING_SHUTTER_SOUND,
		GC.ROW_SETTING_ARCSOFT_NIGHT,
		GC.ROW_SETTING_ARCSOFT_ASD,
		GC.ROW_SETTING_FFF,
		GC.ROW_SETTING_GRID,
		GC.ROW_SETTING_SOUND_RECORDING,
		GC.ROW_SETTING_EIS,
		GC.ROW_SETTING_ZSL,
	};
	
	public static int[] CHECKBOX_ITEM=new int[]{
		GC.ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE,
		GC.ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE,
	};
	
	public static int[] SELECTOR_ITEM=new int[]{
		GC.ROW_SETTING_PICTURE_SIZE_FLAG,
		GC.ROW_SETTING_EXPOSURE,
		GC.ROW_SETTING_SELF_TIMER,
		GC.ROW_SETTING_VIDEO_QUALITY,
		GC.ROW_SETTING_VIDEO_FRONT_QUALITY,
		GC.ROW_SETTING_ANTIBANDING,
		GC.ROW_SETTING_SAVEPATH,
	};
	
	public static int[] CAMERA_ITEM=new int[]{
		GC.ROW_SETTING_RESTORE_TO_DEFAULT
	};
	
	enum MenuType{
		Toggle ,
		Selector,
		Slider,
		Class ,
		Camera,
		Checkbox,
	}
	class MenuItem {
		
		public final MenuType menuType;
		private final int menuId;
		public MenuItem(MenuType type,int id){
			menuType=type;
			if(type==MenuType.Class){
				menuId=-1;
			}else{
				menuId=id;
			}
		}
		
		public boolean needBorder=true;
		public boolean needDescription=false;
		
		public String menuTitle;
	}
	
	@Override
	public void show() {
		Log.d(TAG,"###YUHUAN###show into ");
		updateMenu();
		if(mAdapter!=null){
			mAdapter.notifyDataSetChanged();
		}
		setAnimationEnabled(false, false);
		super.show();
		
		if(mContext.getCameraViewManager()!=null&&mContext.getCameraViewManager().getQrcodeViewManager()!=null){
			mContext.getCameraViewManager().getQrcodeViewManager().hideMenu();
		}
		
		Log.d(TAG,"###YUHUAN###show#mAdvanceMenu= " + mAdvanceMenu);
		if(mAdvanceMenu !=null){
			showAnimation();
		}
	}
	
	@Override
	public void hide() {
		// TODO Auto-generated method stub
		Log.d(TAG,"###YUHUAN###hide#mContext.isResetToDefault()= " + mContext.isResetToDefault());
		
		if(mContext.isResetToDefault()){
			super.hide();
		}else{
			Log.d(TAG,"###YUHUAN###hide#mAdvanceMenu= " + mAdvanceMenu);
			if(mAdvanceMenu !=null){
				hideAnimation();
			}
		}
		
		Log.d(TAG,"###YUHUAN###hide#mContext.isInCamera()= " + mContext.isInCamera());
		if(mContext.isInCamera()){
			mContext.enableDrawer();	
		}
//		super.hide();
	}

	private int mOrientation=0;
	@Override
	public void onOrientationChanged(int orientation) {
		super.onOrientationChanged(orientation);
		if(mAdvanceMenu==null){
			return;
		}
//		if(orientation%90==0){
//			mAdvanceMenu.setLayoutDirection(orientation);
//			mOrientation=orientation;
//		}
//		this.invalidate();
		
	}

	
	private View mAdvanceMenu;
	private MenuAdapter mAdapter;
	@TargetApi(Build.VERSION_CODES.KITKAT)
	@SuppressLint("InlinedApi")
	public View getView(){
		Log.d(TAG,"###YUHUAN###getView into ");
		mAdvanceMenu=inflate(R.layout.advance_menu);
		
		if(supportUIRTL(mContext)){
			mAdvanceMenu.setLayoutDirection(LayoutDirection.RTL);
		}else{
			mAdvanceMenu.setLayoutDirection(LayoutDirection.LTR);
		}
		
		ListView menuList=(ListView)mAdvanceMenu.findViewById(R.id.menu_list);
		
		mAdapter=new MenuAdapter();
		
		menuList.setAdapter(mAdapter);
		
		LinearLayout backBtnParent=(LinearLayout) mAdvanceMenu.findViewById(R.id.advance_menu_parent);
		LinearLayout backBtn=(LinearLayout) backBtnParent.findViewById(R.id.advance_menu_back);
		
		backBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Log.d(TAG,"###YUHUAN###backBtn.onClick");
				mContext.setViewState(GC.VIEW_STATE_HIDE_SET);
				mContext.disableNavigationBar();
			}
			
		});
		
		ImageView info = (ImageView)backBtnParent.findViewById(R.id.advance_menu_info);
		
		info.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG,"###YUHUAN###info.onClick");
				Intent i = new Intent(mContext, AboutActivity.class);
				mContext.startActivity(i);
			}
		});
		
		//if need about pls delete the code, or about has no listener
		LinearLayout infoParent = (LinearLayout)backBtnParent.findViewById(R.id.advance_menu_info_parent);
		infoParent.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		//if need about pls delete the code, or about has no listener
		
		return mAdvanceMenu;
	}
	
	
	public void setSharedPreferenceChangedListener(onSharedPreferenceChangedListener l){
		mSharedPreferenceChangedListener=l;
	}
	
	private onSharedPreferenceChangedListener mSharedPreferenceChangedListener;
	private Listener mSettingChangedListener=new Listener(){

		@Override
		public void onSettingChanged(InLineSettingItem item) {
			if(mSharedPreferenceChangedListener!=null){
				mSharedPreferenceChangedListener.onSharedPreferenceChanged();
			}
//			mActivity
		}

		@Override
		public void onShow(InLineSettingItem item) {
			refreshValueAndView();
		}

		@Override
		public void onDismiss(InLineSettingItem item) {
			refreshValueAndView();
		}
		
	};
	
	
	private List<MenuItem> mMenus=new ArrayList<MenuItem>();
	
	private void initMenus(){
		boolean needChildViewASD=false;
		mMenus.clear();
		MenuItem photoTitle=new MenuItem(MenuType.Class,-1);
		MenuItem videoTitle=new MenuItem(MenuType.Class,-1);
		MenuItem cameraTitle=new MenuItem(MenuType.Class,-1);
		photoTitle.menuTitle=mContext.getString(R.string.advance_setting_group_photo);
		videoTitle.menuTitle=mContext.getString(R.string.advance_setting_group_video);
		cameraTitle.menuTitle=mContext.getString(R.string.advance_setting_group_camera);
		mMenus.add(photoTitle);
		
		
		if(mGC.getListPreference(GC.ROW_SETTING_ARCSOFT_ASD)!=null){
			if(mGC.getListPreference(GC.ROW_SETTING_ARCSOFT_ASD).findIndexOfValue(mGC.getListPreference(GC.ROW_SETTING_ARCSOFT_ASD).getValue())!=1 || PHOTO_MENU_ID_BACK[GC.ROW_SETTING_ARCSOFT_ASD]==0){
				needChildViewASD=false;
			}else{
				needChildViewASD=true;
			}
		}
		
		Log.d(TAG,"###YUHUAN###initMenus#needChildViewASD=" + needChildViewASD);
		if(needChildViewASD){
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE]=GC.ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE;
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE]=GC.ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE;
		}else{
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_ARCSOFT_ASD_BACK_NIGHT_MODE]=0;
			PHOTO_MENU_ID_BACK[GC.ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE]=0;
		}
		
		for(int i=0;i<PHOTO_SETTING_IDs.length;i++){
			if(PHOTO_MENU_ID[PHOTO_SETTING_IDs[i]]!=0){
				MenuItem item=new MenuItem(getMenuType(PHOTO_SETTING_IDs[i]),PHOTO_SETTING_IDs[i]);
				if((PHOTO_SETTING_IDs[i]==GC.ROW_SETTING_ARCSOFT_ASD && needChildViewASD) || PHOTO_SETTING_IDs[i]==GC.ROW_SETTING_ARCSOFT_ASD_NIGHT_MODE ){
					item.needBorder=false;
				}
				if(PHOTO_SETTING_IDs[i]==GC.ROW_SETTING_ARCSOFT_ASD && needChildViewASD){
					item.needDescription=true;
				}
				mMenus.add(item);
			}
		}
//		for(int photoId:PHOTO_MENU_ID){
//			if(PHOTO_MENU_I)
//			MenuItem item=new MenuItem(getMenuType(photoId),photoId);
//			mMenus.add(item);
//		}
		
		mMenus.add(videoTitle);
		for(int i=0;i<VIDEO_MENUS.length;i++){
			MenuItem item=new MenuItem(getMenuType(VIDEO_MENUS[i]),VIDEO_MENUS[i]);
			if(i==VIDEO_MENUS.length-1){
				item.needBorder=true;
			}
			mMenus.add(item);
		}

        // modify by minghui.hua for PR938995
		Log.d(TAG,"###YUHUAN###initMenus#isWriteable=" + SDCard.instance().isWriteable());
        if (!SDCard.instance().isWriteable()) {
            Log.w(TAG, "initMenus have no sdcard!!!");
            MenuItem item = new MenuItem(getMenuType(GC.ROW_SETTING_RESTORE_TO_DEFAULT),
                    GC.ROW_SETTING_RESTORE_TO_DEFAULT);
            item.needBorder = false;
            mMenus.add(item);
            return;
        }
        mMenus.add(cameraTitle);
        for (int i = 0; i < CAMERA_MENU_ID.length; i++) {
            MenuItem item = new MenuItem(getMenuType(CAMERA_MENU_ID[i]), CAMERA_MENU_ID[i]);
            if (i == CAMERA_MENU_ID.length - 1) {
                item.needBorder = false;
            }
            mMenus.add(item);
        }
	}
	
	private MenuType getMenuType(int id){
		for(int menuId:TOGGLE_SWITCH_ITEM){
			if(menuId==id){
				return MenuType.Toggle;
			}
		}
		
		for(int menuId:CHECKBOX_ITEM){
			if(menuId==id){
				return MenuType.Checkbox;
			}
		}
		
		
		for(int menuId:SELECTOR_ITEM){
			if(menuId==id){
				return MenuType.Selector;
			}
		}
		
		for(int menuId:CAMERA_ITEM){
			if(menuId==id){
				return MenuType.Camera;
			}
		}
		
		if(id==-1){
			return MenuType.Class;
		}else{
			return MenuType.Slider;
		}
	}
	
	private class MenuAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			Log.d(TAG,"###YUHUAN###getCount#mMenus.size()=" + mMenus.size());
			return mMenus.size();
		}

		@Override
		public Object getItem(int position) {
			Log.d(TAG,"###YUHUAN###getItem#position=" + position);
			return mMenus.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MenuItem item=mMenus.get(position);
			LayoutInflater lf=mContext.getLayoutInflater();
			View view=null;
			int id=item.menuId;
			
			String key=null;
			ListPreference pref=null;
			
			Log.d(TAG,"###YUHUAN###getView#id=" + id);
			if(id!=-1){//in case of MenuType.Class
				key=GC.getSettingKey(id);
				Log.d(TAG,"###YUHUAN###getView#key=" + key);
				pref = mGC.getListPreference(key);
			}
			View border=null;
			TextView mDescription=null;
			Log.d(TAG,"###YUHUAN###getView#item.menuType= " + item.menuType);
			
		    Log.d(TAG,"###YUHUAN###getView#convertView= " + convertView);
			
			switch(item.menuType){
			case Toggle:
				if(!(convertView instanceof InLineSettingToggleSwitch)){
					view=lf.inflate(R.layout.in_line_setting_toggle, null);
				}else{
					view=convertView;
				}
				mDescription=(TextView)view.findViewById(R.id.description_switch);
				border=view.findViewById(R.id.border);
				if(!item.needBorder){
					border.setVisibility(View.GONE);
				}else{
					border.setVisibility(View.VISIBLE);
				}
				if(item.needDescription){
					mDescription.setText(mContext.getResources().getString(mGC.getDescription(id)));
					mDescription.setVisibility(View.VISIBLE);
				}else{
					mDescription.setVisibility(View.GONE);
				}
				break;
			case Selector:
				if(!(convertView instanceof InLineSettingSelector)){
					view=lf.inflate(R.layout.in_line_setting_selector, null);
				}else{
					view=convertView;
				}
				border=view.findViewById(R.id.border);
				if(!item.needBorder){
					border.setVisibility(View.GONE);
				}
				break;
			case Slider:
				break;
			case Class:
				TextView tv=null;
				if(convertView!=null&&! (convertView instanceof InLineSettingItem)){
					view=convertView;
					tv=(TextView)convertView.getTag();
				}else{
					view=lf.inflate(R.layout.setting_title, null);
					tv=(TextView)view.findViewById(R.id.title_text);
					view.setTag(tv);
				}
				tv.setText(item.menuTitle);
				break;
			case Camera:
				if(!(convertView instanceof InLineSettingRestore)){
					view=lf.inflate(R.layout.in_line_setting_restore, null);
				}else{
					view=convertView;
				}
				border=view.findViewById(R.id.border);
				if(!item.needBorder){
					border.setVisibility(View.GONE);
				}
				break;
			case Checkbox:
				if(!(convertView instanceof InLineSettingCheckBox)){
					view=lf.inflate(R.layout.in_line_setting_check_box, null);
				}else{
					view=convertView;
				}
				border=view.findViewById(R.id.border);
				if(!item.needBorder){
					border.setVisibility(View.GONE);
				}
				break;
			default:
				break;
			}
			if(id!=-1&&pref!=null){
				((InLineSettingItem)view).initialize(pref);
				((InLineSettingItem)view).setSettingChangedListener(mSettingChangedListener);
			}
			
			if(!mGC.isSettingEnable(id)){
				view.setEnabled(false);
			}else{
				view.setEnabled(true);
			}
			return view;
		}
		
	}

	public void setDrawLayout(MenuDrawable drawer){
		this.drawer=drawer;
	}

	@Override
	public void resetToDefault() {
		// TODO Auto-generated method stub
		super.resetToDefault();
		reloadView();
	}
	public static boolean supportUIRTL(CameraActivity context) {
        String language = context.getResources().getConfiguration().locale.getLanguage();
        if ("ar".equals(language)){// || "fa".equals(language) || "iw".equals(language)) {
            return true;
        }
        return false;
    }

    public void refreshValueAndView() {
        initMenus();
        //modify by minghui.hua for PR948326
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }
    private void showAnimation(){
    	ObjectAnimator mTranceXAnimator = ObjectAnimator.ofFloat(mAdvanceMenu, "translationY", mContext.getResources().getDisplayMetrics().heightPixels,0);
		mTranceXAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		
		ObjectAnimator mAlpaAnimator =	ObjectAnimator.ofFloat(mAdvanceMenu, "alpha", 0.0f,1.0f);
		mAlpaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		
		AnimatorSet mAnimator=new AnimatorSet();
		mAnimator.playTogether(mTranceXAnimator,mAlpaAnimator);
		mAnimator.setDuration(300);
		mAnimator.start();
    }
    private void hideAnimation(){
    	ObjectAnimator mTranceXAnimator =	ObjectAnimator.ofFloat(mAdvanceMenu, "translationY", 0,mContext.getResources().getDisplayMetrics().heightPixels);
		mTranceXAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		mTranceXAnimator.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				//do super.hide
				Log.d(TAG,"###YUHUAN###hideAnimation#onAnimationEnd#mAdvanceMenu=" + mAdvanceMenu);
				Log.d(TAG,"###YUHUAN###hideAnimation#onAnimationEnd#mShowing=" + mShowing);
				if (mAdvanceMenu != null && mShowing) {
		            mShowing = false;
		            mAdvanceMenu.setVisibility(View.GONE);
		        }
			}
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		ObjectAnimator mAlpaAnimator =	ObjectAnimator.ofFloat(mAdvanceMenu, "alpha", 1.0f,0.0f);
		mAlpaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
		
		AnimatorSet mAnimator=new AnimatorSet();
		mAnimator.playTogether(mTranceXAnimator,mAlpaAnimator);
		mAnimator.setDuration(300);
		mAnimator.start();
    }
}
