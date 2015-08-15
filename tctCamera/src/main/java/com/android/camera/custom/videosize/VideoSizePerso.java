package com.android.camera.custom.videosize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.util.Log;

import com.android.camera.GC;
import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.custom.CustomFields;
import com.android.camera.custom.CustomUtil;
import com.android.jrd.externel.CamcorderProfileEx;

public class VideoSizePerso {
	public static String TAG = "VideoSizePerso";
	
	public static class VideoSize{
		public VideoSize(int w,int h){
			width=w;
			height=h;
		}
		public int width;
		public int height;
		@Override
		public boolean equals(Object o) {
			VideoSize other=(VideoSize)o;
			return (other.width==this.width&&other.height==this.height);
		}
		@Override
		public String toString() {
			return width+"x"+height+" ";
		}
		
		
	}
	
	private CustomUtil mCuzUtil;
	private boolean[] mIsInitialized=new boolean[2];
	private static VideoSizePerso mPerso;
	private ArrayList<List<String>> mSupportedQuality=null;
	private ArrayList<List<VideoSize>> mCameraSupportedSize=null;
	private ArrayList<List<String>> mConfigSettings;
	
	private ArrayList<List<String>> mTitleLists;
	private List<Size> mSupportedSizes;
	
	private static final String[] CUZ_SIZE_REAR;
	private static final String[] CUZ_SIZE_FRONT;
	
	private static final String[] CUZ_SIZE_TITLES_REAR;
	private static final String[] CUZ_SIZE_TITLES_FRONT;
	
	private VideoSizePerso(){
		mCuzUtil=CustomUtil.getInstance();
		mIsInitialized[0]=false;
		mIsInitialized[1]=false;
		
		mSupportedQuality=new ArrayList<List<String>>();
		mSupportedQuality.add(new ArrayList<String>());//rear camera
		mSupportedQuality.add(new ArrayList<String>());//front camera
		
		mCameraSupportedSize=new ArrayList<List<VideoSize>>();
		mCameraSupportedSize.add(new ArrayList<VideoSize>());//rear camera
		mCameraSupportedSize.add(new ArrayList<VideoSize>());//front camera
		
		mConfigSettings=new ArrayList<List<String>>();
		mConfigSettings.add(new ArrayList<String>());//rear camera
		mConfigSettings.add(new ArrayList<String>());//front camera
		
		mTitleLists=new ArrayList<List<String>>();
		mTitleLists.add(new ArrayList<String>());//rear camera
		mTitleLists.add(new ArrayList<String>());//front camera
	}
	
	public static VideoSizePerso getInstance(){
		if(mPerso==null)
			mPerso=new VideoSizePerso();
		return mPerso;
	}
	
	private Object PERSO_LOCK=new Object();

	public void init(CameraProxy camera,int id){
		if(camera==null){
			return;
		}
		if(id>1){
			return;
		}
		Parameters params=camera.getParameters();
		synchronized(PERSO_LOCK){
			readConfigSettings(params,id);
			filterUnsupportedConfigSettings(id);
		}
		mIsInitialized[id]=true;
		
	}
	
	public boolean isInitialized(int cameraId){
		return mIsInitialized[cameraId];
	}

	
	
	/**
	 *
	 * Size Equal Method:
	 * public boolean equals(Object obj) {
     *      if (!(obj instanceof Size)) {
     *          return false;
     *      }
     *      Size s = (Size) obj;
     *      return width == s.width && height == s.height;
     *  }
	 */
	
	private void readConfigSettings(Parameters param,int cameraId){
		List<String> qualityList=null;
		List<String> titleList=null;
		List<VideoSize> cameraSupportedSizes=null;
		String[] configTitleArray;
		String[] configSizeArray;
		
		if(cameraId==0){
			configTitleArray=CUZ_SIZE_TITLES_REAR;
			configSizeArray=CUZ_SIZE_REAR;
			
		}else{
			configTitleArray=CUZ_SIZE_TITLES_FRONT;
			configSizeArray=CUZ_SIZE_FRONT;
		}
		
		titleList=mTitleLists.get(cameraId);
		cameraSupportedSizes=mCameraSupportedSize.get(cameraId);
		qualityList=mConfigSettings.get(cameraId);
		titleList.clear();
		qualityList.clear();
		cameraSupportedSizes.clear();
		
		for(int i=0;i<configTitleArray.length;i++){//Reading Titles
			String sizeTitle=mCuzUtil.getString(configTitleArray[i], "VGA");
			if(sizeTitle.equalsIgnoreCase("null")){
				continue;
			}
			titleList.add(sizeTitle);
		}
		
		for(int i=0;i<configSizeArray.length;i++){//Reading Sizes
			String quality=mCuzUtil.getString(configSizeArray[i], "QUALITY_VGA");
			if(quality.equalsIgnoreCase("null")){
				continue;
			}
			qualityList.add(quality);
		}
		
		mSupportedSizes=param.getSupportedVideoSizes();
		for(int i=0;i<mSupportedSizes.size();i++){//Reading Camera Sizes
			VideoSize size=parse2VideoSize(mSupportedSizes.get(i));
			cameraSupportedSizes.add(size);
			Log.i(TAG, "Supported Video Size is "+size.toString());
		}
	}
	
	static{
		CUZ_SIZE_REAR=new String[]{
			CustomFields.DEF_VIDEO_SIZE_ONE,
			CustomFields.DEF_VIDEO_SIZE_TWO,
			CustomFields.DEF_VIDEO_SIZE_THREE,
			CustomFields.DEF_VIDEO_SIZE_FOUR,
			CustomFields.DEF_VIDEO_SIZE_FIVE,
			CustomFields.DEF_VIDEO_SIZE_SIX,
			CustomFields.DEF_VIDEO_SIZE_SEVEN,
			CustomFields.DEF_VIDEO_SIZE_EIGHT,
			CustomFields.DEF_VIDEO_SIZE_NINE,
			CustomFields.DEF_VIDEO_SIZE_TEN,
		};
		
		CUZ_SIZE_FRONT=new String[]{
			CustomFields.DEF_VIDEO_SIZE_FRONT_ONE,
			CustomFields.DEF_VIDEO_SIZE_FRONT_TWO,
			CustomFields.DEF_VIDEO_SIZE_FRONT_THREE,
			CustomFields.DEF_VIDEO_SIZE_FRONT_FOUR,
			CustomFields.DEF_VIDEO_SIZE_FRONT_FIVE,
		};
		
		CUZ_SIZE_TITLES_REAR=new String[]{
				CustomFields.DEF_VIDEOSIZE_TITLE_ONE,
				CustomFields.DEF_VIDEOSIZE_TITLE_TWO,
				CustomFields.DEF_VIDEOSIZE_TITLE_THREE,
				CustomFields.DEF_VIDEOSIZE_TITLE_FOUR,
				CustomFields.DEF_VIDEOSIZE_TITLE_FIVE,
				CustomFields.DEF_VIDEOSIZE_TITLE_SIX,
				CustomFields.DEF_VIDEOSIZE_TITLE_SEVEN,
				CustomFields.DEF_VIDEOSIZE_TITLE_EIGHT,
				CustomFields.DEF_VIDEOSIZE_TITLE_NINE,
				CustomFields.DEF_VIDEOSIZE_TITLE_TEN,
		};
		CUZ_SIZE_TITLES_FRONT=new String[]{
				CustomFields.DEF_VIDEOSIZE_TITLE_FRONT_ONE,
				CustomFields.DEF_VIDEOSIZE_TITLE_FRONT_TWO,
				CustomFields.DEF_VIDEOSIZE_TITLE_FRONT_THREE,
				CustomFields.DEF_VIDEOSIZE_TITLE_FRONT_FOUR,
				CustomFields.DEF_VIDEOSIZE_TITLE_FRONT_FIVE,
		};
	}
	
	private void filterUnsupportedConfigSettings(int cameraId){
		if(mConfigSettings==null||mConfigSettings.get(cameraId)==null||mConfigSettings.get(cameraId).size()==0){
			return;
		}
		mSupportedQuality.get(cameraId).clear();
		
		/*
		 * Records is to record all the titles need to be removed from the final output
		 */
		
		Iterator<String> it=mTitleLists.get(cameraId).iterator();
		for(int i=0;i<mConfigSettings.get(cameraId).size();i++){
			if(it!=null&&it.hasNext()){
				it.next();
			}else{
				it=null;
			}
			String s=mConfigSettings.get(cameraId).get(i);
			
			if(isQualityValid(s,cameraId)){
				mSupportedQuality.get(cameraId).add(s);
			}else{
				if(it!=null)
					it.remove();//Remove unsupported titles
			}
		}

        if (mSupportedQuality.get(cameraId).size() == 0) {
        	if(cameraId == GC.CAMERAID_FRONT) {
        		mSupportedQuality.get(cameraId).add("QUALITY_VGA");
            	mTitleLists.get(cameraId).add("VGA");
        	}
        	else {
        		mSupportedQuality.get(cameraId).add("QUALITY_720P");
            	mTitleLists.get(cameraId).add("HD 720p");
        	}
        }

//        Locale locale = Locale.getDefault();
//        if ("ru".equalsIgnoreCase(locale.getLanguage())) {
//            List<String> titleList = mTitleLists.get(cameraId);
//            for (int j = 0; j < titleList.size(); j++) {
//                titleList.set(j, replaceSizeMP(titleList.get(j)));
//            }
//        }
	}
	
	private boolean isSizeValid(VideoSize s,int id){
		if(mCameraSupportedSize.get(id).size()==0){
			return false;
		}
		for(VideoSize size : mCameraSupportedSize.get(id)){
			if(size.equals(s)){
				return true;
			}
		}
		return false;
	}
	
	private boolean isQualityValid(String quality,int id){
		CamcorderProfile profile = CamcorderProfileEx.getProfile(id, CamcorderProfileEx.getQualityNum(quality));
		if(profile == null) return false;
		for(Size size : mSupportedSizes) {
			if((size.width == profile.videoFrameWidth) && (size.height == profile.videoFrameHeight)) return true;
		}
		return false;
	}

    // Just for Russian
    private String replaceSizeMP(String sizeStr) {
        if (sizeStr == null) {
            return sizeStr;
        }
        // add a space between value and unit
        if (sizeStr.contains("MP")) {
            return sizeStr.replace("MP", " Мпикс");
        }
        if (sizeStr.contains("M")) {
            return sizeStr.replace("M", " Мпикс");
        }
        return sizeStr;
    }

	private String[] mRearQualities;
	private String[] mFrontQualities;
	public String[] getPersoSupportedQualities(int id){
		switch(id){
		case 0:
			if(mRearQualities==null){
				mRearQualities=new String[mSupportedQuality.get(id).size()];
				mRearQualities=mSupportedQuality.get(id).toArray(mRearQualities);
			}
			return mRearQualities;
		case 1:
			if(mFrontQualities==null){
				mFrontQualities=new String[mSupportedQuality.get(id).size()];
				mFrontQualities=mSupportedQuality.get(id).toArray(mFrontQualities);
			}
			return mFrontQualities;
		default:
			return null;
		}
	}
	
	public List<String> getPersoSupportedQualityList(int id){
		return mSupportedQuality.get(id);
	}
	
	private String[] mRearSizeTitles;
	private String[] mFrontSizeTitles;
	public String[] getPersoSupporteTitle(int id){
		switch(id){
		case 0:
			if(mRearSizeTitles==null){
				mRearSizeTitles=new String[mTitleLists.get(id).size()];
				mRearSizeTitles=mTitleLists.get(id).toArray(mRearSizeTitles);
			}
			return mRearSizeTitles;
		case 1:
			if(mFrontSizeTitles==null){
				mFrontSizeTitles=new String[mTitleLists.get(id).size()];
				mFrontSizeTitles=mTitleLists.get(id).toArray(mFrontSizeTitles);
			}
			return mFrontSizeTitles;
		default:
			return null;
		}
	}
	
	public int getDefaultValue(int id) {
		int defaultValue = 0;
		switch(id){
			case 0:
				int indexB = Integer.parseInt(mCuzUtil.getString(CustomFields.DEF_VIDEOSIZE_FLAG_DEFAULT, "0"));
				defaultValue = mSupportedQuality.get(id).contains(mConfigSettings.get(id).get(indexB)) ?
						mSupportedQuality.get(id).indexOf(mConfigSettings.get(id).get(indexB)) : 0;
				break;
			case 1:
				int indexF = Integer.parseInt(mCuzUtil.getString(CustomFields.DEF_VIDEOSIZE_FLAG_DEFAULT_FRONT, "0"));
				defaultValue = mSupportedQuality.get(id).contains(mConfigSettings.get(id).get(indexF)) ?
						mSupportedQuality.get(id).indexOf(mConfigSettings.get(id).get(indexF)) : 0;
				break;
			default:
				defaultValue = 0;
		}
		return defaultValue;
	}
	
	public static VideoSize parse2VideoSize(Size size){
		VideoSize videoSize=new VideoSize(size.width,size.height);
		return videoSize;
	}
}
