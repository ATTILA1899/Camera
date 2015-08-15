package com.android.camera.custom.picturesize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;

import com.android.camera.CameraManager.CameraProxy;
import com.android.camera.custom.CustomFields;
import com.android.camera.custom.CustomUtil;

public class PictureSizePerso {
	
	private static final String TAG = "YUHUAN_PictureSizePerso";
	
	public static class PictureSize{
		public PictureSize(int w,int h){
			width=w;
			height=h;
		}
		public int width;
		public int height;
		@Override
		public boolean equals(Object o) {
			PictureSize other=(PictureSize)o;
			return (other.width==this.width&&other.height==this.height);
		}
		@Override
		public String toString() {
			return width+"x"+height+" ";
		}
		
		
	}
	
	private CustomUtil mCuzUtil;
	private boolean[] mIsInitialized=new boolean[2];
	private static PictureSizePerso mPerso;
	private ArrayList<List<PictureSize>> mSupportedSize=null;
	private ArrayList<List<PictureSize>> mCameraSupportedSize=null;
	private ArrayList<List<PictureSize>> mConfigSettings;
	
	private ArrayList<List<String>> mTitleLists;
	
	private static final String[] CUZ_SIZE_REAR;
	private static final String[] CUZ_SIZE_FRONT;
	
	private static final String[] CUZ_SIZE_TITLES_REAR;
	private static final String[] CUZ_SIZE_TITLES_FRONT;
	
	private PictureSizePerso(){
		mCuzUtil=CustomUtil.getInstance();
		mIsInitialized[0]=false;
		mIsInitialized[1]=false;
		
		mSupportedSize=new ArrayList<List<PictureSize>>();
		mSupportedSize.add(new ArrayList<PictureSize>());//rear camera
		mSupportedSize.add(new ArrayList<PictureSize>());//front camera
		
		mCameraSupportedSize=new ArrayList<List<PictureSize>>();
		mCameraSupportedSize.add(new ArrayList<PictureSize>());//rear camera
		mCameraSupportedSize.add(new ArrayList<PictureSize>());//front camera
		
		mConfigSettings=new ArrayList<List<PictureSize>>();
		mConfigSettings.add(new ArrayList<PictureSize>());//rear camera
		mConfigSettings.add(new ArrayList<PictureSize>());//front camera
		
		mTitleLists=new ArrayList<List<String>>();
		mTitleLists.add(new ArrayList<String>());//rear camera
		mTitleLists.add(new ArrayList<String>());//front camera
	}
	
	public static PictureSizePerso getInstance(){
		if(mPerso==null)
			mPerso=new PictureSizePerso();
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
		List<PictureSize> sizeList=null;
		List<String> titleList=null;
		List<PictureSize> cameraSupportedSizes=null;
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
		sizeList=mConfigSettings.get(cameraId);
		titleList.clear();
		sizeList.clear();
		cameraSupportedSizes.clear();
		
		for(int i=0;i<configTitleArray.length;i++){//Reading Titles
			String sizeTitle=mCuzUtil.getString(configTitleArray[i], "13M");
			Log.d(TAG,"###YUHUAN###readConfigSettings#sizeTitle=" + sizeTitle);
			if(sizeTitle.equalsIgnoreCase("null")){
				continue;
			}
			titleList.add(sizeTitle);
			
			// yuhuan 20150703 for test ===begin
			for(int n = 0;n < titleList.size();n++) {
				String mTitle = titleList.get(n);
				Log.d(TAG,"###YUHUAN###readConfigSettings#titleList[" + n + "]=" + mTitle);
			}
			// yuhuan 20150703 for test ===end
		}
		
		for(int i=0;i<configSizeArray.length;i++){//Reading Sizes
			String size=mCuzUtil.getString(configSizeArray[i], "4160x3120");
			Log.d(TAG,"###YUHUAN###readConfigSettings#size=" + size);
			if(size.equalsIgnoreCase("null")){
				continue;
			}
			String[] measure=size.split("x");
			int width=Integer.parseInt(measure[0].trim());
			int height=Integer.parseInt(measure[1].trim());
			if(width==0||height==0){
				continue;
			}
			PictureSize picSize=new PictureSize(width,height);
			sizeList.add(picSize);
			
			// yuhuan 20150703 for test ===begin
			for(int m = 0;m < sizeList.size();m++) {
				PictureSize mPicSize = sizeList.get(m);
				Log.d(TAG,"###YUHUAN###readConfigSettings#sizeList[" + m + "]=" + mPicSize.width + "x" + mPicSize.height);
			}
			// yuhuan 20150703 for test ===end
		}
		
		List<Size> cSizes=param.getSupportedPictureSizes();
		for(int i=0;i<cSizes.size();i++){//Reading Camera Sizes
			PictureSize size=parse2PicSize(cSizes.get(i));
			cameraSupportedSizes.add(size);
			Log.w(TAG, "###YUHUAN###readConfigSettings#Supported Pictuer Size is "+size.toString());
		}
	}
	
	static{
		CUZ_SIZE_REAR=new String[]{
			CustomFields.DEF_PICTURE_SIZE_ONE,
			CustomFields.DEF_PICTURE_SIZE_TWO,
			CustomFields.DEF_PICTURE_SIZE_THREE,
			CustomFields.DEF_PICTURE_SIZE_FOUR,
			CustomFields.DEF_PICTURE_SIZE_FIVE,
			CustomFields.DEF_PICTURE_SIZE_SIX,
			CustomFields.DEF_PICTURE_SIZE_SEVEN,
			CustomFields.DEF_PICTURE_SIZE_EIGHT,
			CustomFields.DEF_PICTURE_SIZE_NINE,
			CustomFields.DEF_PICTURE_SIZE_TEN,
		};
		
		CUZ_SIZE_FRONT=new String[]{
			CustomFields.DEF_PICTURE_SIZE_FRONT_ONE,
			CustomFields.DEF_PICTURE_SIZE_FRONT_TWO,
			CustomFields.DEF_PICTURE_SIZE_FRONT_THREE,
			CustomFields.DEF_PICTURE_SIZE_FRONT_FOUR,
			CustomFields.DEF_PICTURE_SIZE_FRONT_FIVE,
		};
		
		CUZ_SIZE_TITLES_REAR=new String[]{
				CustomFields.DEF_PICTURESIZE_TITLE_ONE,
				CustomFields.DEF_PICTURESIZE_TITLE_TWO,
				CustomFields.DEF_PICTURESIZE_TITLE_THREE,
				CustomFields.DEF_PICTURESIZE_TITLE_FOUR,
				CustomFields.DEF_PICTURESIZE_TITLE_FIVE,
				CustomFields.DEF_PICTURESIZE_TITLE_SIX,
				CustomFields.DEF_PICTURESIZE_TITLE_SEVEN,
				CustomFields.DEF_PICTURESIZE_TITLE_EIGHT,
				CustomFields.DEF_PICTURESIZE_TITLE_NINE,
				CustomFields.DEF_PICTURESIZE_TITLE_TEN,
		};
		CUZ_SIZE_TITLES_FRONT=new String[]{
				CustomFields.DEF_PICTURESIZE_TITLE_FRONT_ONE,
				CustomFields.DEF_PICTURESIZE_TITLE_FRONT_TWO,
				CustomFields.DEF_PICTURESIZE_TITLE_FRONT_THREE,
				CustomFields.DEF_PICTURESIZE_TITLE_FRONT_FOUR,
				CustomFields.DEF_PICTURESIZE_TITLE_FRONT_FIVE,
		};
	}
	
	private void filterUnsupportedConfigSettings(int cameraId){
		if(mConfigSettings==null||mConfigSettings.get(cameraId)==null||mConfigSettings.get(cameraId).size()==0){
			return;
		}
		mSupportedSize.get(cameraId).clear();
		
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
			PictureSize s=mConfigSettings.get(cameraId).get(i);
			Log.d(TAG,"###YUHUAN###filterUnsupportedConfigSettings#s=" + s.width + "x" + s.height);
			
			if(isSizeValid(s,cameraId)){
				mSupportedSize.get(cameraId).add(s);
			}else{
				if(it!=null)
					it.remove();//Remove unsupported titles
			}
		}

        if (mSupportedSize.get(cameraId).size() == 0) {
            mSupportedSize.get(cameraId).add(mCameraSupportedSize.get(cameraId).get(0));
            mTitleLists.get(cameraId).add(mCameraSupportedSize.get(cameraId).get(0).toString());

            if(mCameraSupportedSize.get(cameraId).size()>1){
                mSupportedSize.get(cameraId).add(mCameraSupportedSize.get(cameraId).get(1));
                mTitleLists.get(cameraId).add(mCameraSupportedSize.get(cameraId).get(1).toString());
            }else{
                mSupportedSize.get(cameraId).add(mCameraSupportedSize.get(cameraId).get(0));
                mTitleLists.get(cameraId).add(mCameraSupportedSize.get(cameraId).get(0).toString());
            }
        }

        Locale locale = Locale.getDefault();
        if ("ru".equalsIgnoreCase(locale.getLanguage())) {
            List<String> titleList = mTitleLists.get(cameraId);
            for (int j = 0; j < titleList.size(); j++) {
                titleList.set(j, replaceSizeMP(titleList.get(j)));
            }
        }
	}
	
	private boolean isSizeValid(PictureSize s,int id){
		if(mCameraSupportedSize.get(id).size()==0){
			return false;
		}
		for(PictureSize size : mCameraSupportedSize.get(id)){
			if(size.equals(s)){
				return true;
			}
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

	public List<PictureSize> getPersoSupportedSizes(int id){
		if(id>1){
			return null;
		}
		
		// yuhuan 20150703 for test picture size ===begin
		List<PictureSize> picSizeList = mSupportedSize.get(id);
		for(int i =0;i< picSizeList.size();i++) {
			PictureSize picSize = picSizeList.get(i);
			Log.d(TAG,"###YUHUAN###getPersoSupportedSizes#picSizeList[" + i + "]="+ picSize.width + "x" + picSize.height);
		}
		return picSizeList;
		// yuhuan 20150703 for test picture size ===end
		//return mSupportedSize.get(id);
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
	
	
	public static PictureSize parse2PicSize(Size size){
		PictureSize picSize=new PictureSize(size.width,size.height);
		return picSize;
	}
}
