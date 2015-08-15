package com.jrdcamera.modeview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOverlay;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.android.camera.CameraActivity;
import com.android.camera.GC;
import com.android.camera.custom.CustomFields;
import com.android.camera.custom.CustomUtil;
import com.tct.camera.R;

public class ModeListView  extends ListView{
	
	private static final String TAG = "YUHUAN_ModeListView" ;
	
	public final static int MODE_PHOTO_ID=0;
	public static int MODE_HDR_ID=1;
	public static int MODE_PANORAMA_ID=2;
	public static int MODE_MANUAL_ID=3;
	public static int MODE_TIMELAPSE_ID=4;
	public static int MODE_QRCODE_ID=5;
	public static int MODE_ARCSOFT_FACEBEAUTY_ID=6;
	public static int MODE_POLAROID_ID=7;
	public static int MODE_ARCSOFT_NIGHT_ID = 8;
	
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, ListViewItemView> itemView=new HashMap<Integer, ListViewItemView>();
	private  List<ListAttr> mList=new ArrayList<ListAttr>();
	private Context mContext;
	private CameraActivity mCameraActivity;
	private  ViewOverlay mOverlay;
	private ModeAdapter mModeAdapter;
	private DrawLayout mDrawLayout;
	private GC mGC;

	

	public ModeListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	
	
	public ModeListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.mContext=context;
		this.setClickable(true);
		this.setVerticalScrollBarEnabled(false);
		TypedArray mTypeArray = context.obtainStyledAttributes(attrs, R.styleable.CustomView);
        String  method = mTypeArray.getString(R.styleable.CustomView_touchmethod);
        Log.d(TAG,"###YUHUAN###ModeListView#method=" + method);
        mList=initdata(null,null,method);
        
        mModeAdapter=new ModeAdapter(mList, context,this);
        this.setAdapter(mModeAdapter);
        this.setDividerHeight(0);
        mTypeArray.recycle();
        this.setSelector(android.R.color.transparent);
	}

	
	public void setGC(GC mGc){
		this.mGC=mGc;
	}
	
	public GC getGC(){
			return mGC;
	}
	
	public void  setDrawLayout(DrawLayout drawLayout){
		this.mDrawLayout = drawLayout;
	}
	private List<ListAttr>  initdata(String menuName,String menuPic,String method){
		List<ListAttr> mListAttr=new ArrayList<ListAttr>();
		//v-nj-feiqiang.cheng begin for PR941559,941555
		if ("off".equals(mContext.getResources().getString(
				R.string.def_manualmode_feature_onoff))) {
			method = method.replace("modeClick,", "").replace(",modeClick", "");
			MODE_TIMELAPSE_ID = 3;
			MODE_QRCODE_ID = 4;
			MODE_ARCSOFT_FACEBEAUTY_ID = 5;
			MODE_POLAROID_ID = 6;
			MODE_ARCSOFT_NIGHT_ID = 7;
		}
		//v-nj-feiqiang.cheng end for PR941559,941555
		// v-nj-feiqiang.cheng begin for PR947584
		if ("off".equals(mContext.getResources().getString(
				R.string.def_timelapse_feature_onoff))) {
			method = method.replace("timeLapseClick,", "").replace(
					",timeLapseClick", "");
			MODE_QRCODE_ID = MODE_QRCODE_ID - 1;
			MODE_ARCSOFT_FACEBEAUTY_ID = MODE_ARCSOFT_FACEBEAUTY_ID - 1;
			MODE_POLAROID_ID = MODE_POLAROID_ID - 1;
			MODE_ARCSOFT_NIGHT_ID = MODE_ARCSOFT_NIGHT_ID-1;
		}
		// v-nj-feiqiang.cheng end for PR947584

        // modify by minghui.hua for PR975998 begin
        if (!mContext.getResources().getBoolean(R.bool.def_hdr_enable)) {
            method = method.replace("HDRClick,", "").replace(",HDRClick", "");
            MODE_PANORAMA_ID -= 1;
            MODE_MANUAL_ID -= 1;
            MODE_TIMELAPSE_ID -= 1;
            MODE_QRCODE_ID -= 1;
            MODE_ARCSOFT_FACEBEAUTY_ID -= 1;
            MODE_POLAROID_ID -= 1;
            MODE_ARCSOFT_NIGHT_ID -=1 ;
        }
        if (!mContext.getResources().getBoolean(R.bool.def_panorama_enable)) {
            method = method.replace("panoramaClick,", "").replace(",panoramaClick", "");
            MODE_MANUAL_ID -= 1;
            MODE_TIMELAPSE_ID -= 1;
            MODE_QRCODE_ID -= 1;
            MODE_ARCSOFT_FACEBEAUTY_ID -= 1;
            MODE_POLAROID_ID -= 1;
            MODE_ARCSOFT_NIGHT_ID -=1;
        }
        if ("off".equals(mContext.getResources().getString(
				R.string.def_arcsoft_feature_onoff))) {
			method = method.replace("faceBeautyClick,", "").replace(",faceBeautyClick", "");
			MODE_POLAROID_ID -= 1;
			MODE_ARCSOFT_NIGHT_ID -=1;
		}
        // modify by minghui.hua for PR975998 end nightClick
        Log.d(TAG, "get custom value:"+CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_POLAROID_ENABLE, false));
        if (!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_POLAROID_ENABLE, false)) {
            method = method.replace("polaroidClick,", "").replace(",polaroidClick", "");
            MODE_ARCSOFT_NIGHT_ID -=1;
        }
        
        if (!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_ARCSOFT_NIGHT_ENABLE, false)) {
            method = method.replace("nightClick,", "").replace(",nightClick", "");
        }

		String[] methodTem=method.split(",");

		//HDRClick,panoramaClick,modeClick,timeLapseClick,scanerClick,faceBeautyClick
		for(int i=0;i<methodTem.length;i++){
			int id=-1;
			int picId=0;
			int picPressedId=0;
			int picSelectedId=0;
			int nameId=0;
			int mode=0;
			boolean enable=true;
			boolean originalEnable=true;
			switch(methodTem[i]){
			case "photoClick":
				id=MODE_PHOTO_ID;
				picId=R.drawable.mode_photo_normal;
				picPressedId=R.drawable.mode_photo_pressed;
				picSelectedId=R.drawable.mode_photo_selected;
				nameId=R.string.mode_menu_photo;
				mode=GC.MODE_PHOTO;
				enable=true;
				originalEnable=true;
				break;
			case "HDRClick":
				id=MODE_HDR_ID;
				picId=R.drawable.mode_hdrs_normal;
				picPressedId=R.drawable.mode_hdrs_pressed;
				picSelectedId=R.drawable.mode_hdrs_selected;
				nameId=R.string.mode_menu_hdr;
				mode=GC.MODE_HDR;
				enable=true;
				originalEnable=true;
				break;
			case "panoramaClick":
				id=MODE_PANORAMA_ID;
				picId=R.drawable.mode_panoramas_normal;
				picPressedId=R.drawable.mode_panoramas_pressed;
				picSelectedId=R.drawable.mode_panoramas_selected;
				nameId=R.string.mode_menu_panorama;
				mode=GC.MODE_PANORAMA;
				enable=true;
				originalEnable=true;
				break;
			case "modeClick":
				id=MODE_MANUAL_ID;
				picId=R.drawable.mode_manuals_normal;
				picPressedId=R.drawable.mode_manuals_pressed;
				picSelectedId=R.drawable.mode_manuals_selected;
				nameId=R.string.mode_menu_manual;
				mode=GC.MODE_MANUAL;
				enable=true;
				originalEnable=true;
				break;
			case "timeLapseClick":
				id=MODE_TIMELAPSE_ID;
				picId=R.drawable.mode_time_normal;
				picPressedId=R.drawable.mode_time_pressed;
				picSelectedId=R.drawable.mode_time_selected;
				nameId=R.string.mode_menu_timelapse;
				mode=GC.MODE_TIMELAPSE;
				enable=true;
				originalEnable=true;
				break;
			case "scanerClick":
				id=MODE_QRCODE_ID;
				picId=R.drawable.mode_scan_normal;
				picPressedId=R.drawable.mode_scan_pressed;
				picSelectedId=R.drawable.mode_scan_selected;
				nameId=R.string.mode_menu_scaner;
				mode=GC.MODE_QRCODE;
				enable=true;
				originalEnable=true;
				break;
			case "faceBeautyClick":
				id=MODE_ARCSOFT_FACEBEAUTY_ID;
				picId=R.drawable.mode_face_normal;
				picPressedId=R.drawable.mode_face_pressed;
				picSelectedId=R.drawable.mode_face_selected;
                nameId=R.string.mode_menu_face_beauty;//Modify by zhiming.yu for RP909961
				mode=GC.MODE_ARCSOFT_FACEBEAUTY;
				enable=true;
				originalEnable=true;
				break;
			case "polaroidClick":
				id=MODE_POLAROID_ID;
				picId=R.drawable.mode_polaroid_normal;
				picPressedId=R.drawable.mode_polaroid_pressed;
				picSelectedId=R.drawable.mode_polaroid_pressed;
                nameId=R.string.mode_menu_polaroid;//Modify by zhiming.yu for RP909961
				mode=GC.MODE_POLAROID;
				enable=true;
				originalEnable=true;
				break;
			case "nightClick":
				id=MODE_ARCSOFT_NIGHT_ID;
				picId=R.drawable.mode_polaroid_normal;
				picPressedId=R.drawable.mode_polaroid_pressed;
				picSelectedId=R.drawable.mode_polaroid_pressed;
                nameId=R.string.mode_menu_night;//Modify by zhiming.yu for RP909961
				mode=GC.MODE_ARCSOFT_NIGHT;
				enable=true;
				originalEnable=true;
				break;
//			case "Night Mode":
//				picId=R.drawable.mode_face_normal;
//				break;
				default:
					break;
				
			}
			mListAttr.add(new ListAttr(i,nameId,picId,picPressedId,picSelectedId,methodTem[i],mode,enable,originalEnable));
		}
		return mListAttr;
	}
	
	 class ListAttr{
		 private int id;
		 private int menuName;
		 private int menuPic;
		 private int menuPic_pressed;
		 private int menuPic_selected;
		 private String methodName;
		 private int mode;
		 private boolean enable;
		 private boolean originalEnable;
		 
		 
		public ListAttr(int id, int menuName, int menuPic,int menuPic_pressed,int menuPic_selected,
				String methodName,int mode,boolean enable,boolean originalEnable) {
			super();
			this.id = id;
			this.menuName = menuName;
			this.menuPic = menuPic;
			this.menuPic_pressed=menuPic_pressed;
			this.menuPic_selected=menuPic_selected;
			this.methodName = methodName;
			this.mode=mode;
			this.enable=enable;
			this.originalEnable=originalEnable;
		}
		public int getMenuName() {
			return menuName;
		}
		public void setMenuName(int menuName) {
			this.menuName = menuName;
		}
		public int getMenuPic() {
			return menuPic;
		}
		public void setMenuPic(int menuPic) {
			this.menuPic = menuPic;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getMethodName() {
			return methodName;
		}
		public void setMethodName(String methodName) {
			this.methodName = methodName;
		}
		public int getMenuPic_pressed() {
			return menuPic_pressed;
		}
		public void setMenuPic_pressed(int menuPic_pressed) {
			this.menuPic_pressed = menuPic_pressed;
		}
		
		public int getMenuPic_selected() {
			return menuPic_selected;
		}
		public void setMenuPic_selected(int menuPic_selected) {
			this.menuPic_selected = menuPic_selected;
		}
		public int getMode() {
			return mode;
		}
		public void setMode(int mode) {
			this.mode = mode;
		}
		public boolean isEnable() {
			return enable;
		}
		public void setEnable(boolean enable) {
			this.enable = enable;
		}
		public boolean isOriginalEnable() {
			return originalEnable;
		}
		public void setOriginalEnable(boolean originalEnable) {
			this.originalEnable = originalEnable;
		}
		
	 }
	 
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return onTouchEvent(ev);
	}
	
	float x = 0;
	 float y = 0;
	 @SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		 
		 switch(event.getAction()){
		 case MotionEvent.ACTION_UP:
			 if(Math.abs(event.getX() - x) < Math.abs(event.getY() - y) ){
				 return super.onTouchEvent(event);
			 }else{
				 return mDrawLayout.onTouchEvent(event);
			 }
			 
		 case MotionEvent.ACTION_DOWN:
			 x = event.getX();
			 y = event.getY();
			 mDrawLayout.initDown(event);
			 return super.onTouchEvent(event);
		 case MotionEvent.ACTION_MOVE:
			 if(Math.abs(event.getX() - x) < Math.abs(event.getY() - y) ){
				 return super.onTouchEvent(event);
			 }
			 
		 }
		return mDrawLayout.mGestureDetector.onTouchEvent(event);
	}
	 
	 
	 
	 public List<ListAttr>  getMethod(){
		 return mList;
	 } 
	 
	 
	//set listview wid and heig
		 public void setListViewHeightBasedOnChildren(ListView listView) {
	   	  ListAdapter listAdapter = listView.getAdapter();
	   	  if (listAdapter == null) {
	   	   return;
	   	  }
	   	  int totalHeight = 0;
	   	  int maxWid=0;
	   	  for (int i = 0; i < listAdapter.getCount(); i++) { 
	   	   View listItem = listAdapter.getView(i, null, listView);
	   	   listItem.measure(0, 0); 
	   	   if(maxWid<listItem.getMeasuredWidth()){
	   		   maxWid=listItem.getMeasuredWidth();
	   	   }
	   	   totalHeight += listItem.getMeasuredHeight(); 
	   	  }
	   	  ViewGroup.LayoutParams params = listView.getLayoutParams();
	   	  params.height = totalHeight+dipTopx(mContext,16);
//	   	  params.width = maxWid;
	   	  listView.setLayoutParams(params);

	   	 }
		 
		 public static int dipTopx(Context context, float dipValue){
		        final float scale = context.getResources().getDisplayMetrics().density;
		        return (int)(dipValue * scale + 0.5f);
			} 
		 
		public void updateModeMenu(){
			mModeAdapter.notifyDataSetChanged();
		}

		public void  updateList(List<ListAttr> list){
			 this.mList=list;
			 
			 updateModeMenu();
		 }
		public List<ListAttr> getList(){
			return mList;
		}
		
		public boolean isFrontCamera(){
			 if(mCameraActivity.getCameraId() == GC.CAMERAID_FRONT){
				 return true;
			 }else{
				 return false;
			 }
		 }
		
		public void setContext(CameraActivity context){
			this.mCameraActivity = context;
		}
		
}
