package com.android.jrd.externel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.hardware.Camera.Parameters;
import android.util.Log;

public class ExtendParameters {
    private static Parameters mParameters;

    private static ExtendParameters mInstance;

    public synchronized static ExtendParameters getInstance(Parameters param) {
        if (mInstance == null || param != mParameters) {
            mInstance = new ExtendParameters(param);
        }
        return mInstance;
    }

    private HashMap<String, Method> mParamMethodMap = new HashMap<String, Method>();

    private static final String TAG = "ExtendParameters";

    private ExtendParameters(Parameters parameters) {
        mParameters = parameters;
        Class<Parameters> paramClass = Parameters.class;
        Method[] methods = paramClass.getMethods();
        for (Method method : methods) {
            mParamMethodMap.put(method.getName(), method);
        }
    }

    private void set(String method, Object value) {
        if (value == null) {
            return;
        }
        Method setMethod = mParamMethodMap.get(method);

        if (setMethod != null) {
            try {
                setMethod.setAccessible(true);
                setMethod.invoke(mParameters, value);
            } catch (Exception e) {
                Log.d(TAG,"set parameter failed!method:"+method.toString()+",value:"+value);
                e.printStackTrace();
            }
        }
    }

    private List<String> query(String method) {
        Method queryMethod = mParamMethodMap.get(method);
        if (queryMethod != null) {
            try {
                queryMethod.setAccessible(true);
                Object obj = queryMethod.invoke(mParameters, null);
                return (List<String>) obj;
            } catch (Exception e) {
                Log.d(TAG,"query failed!method:"+method.toString());
                e.printStackTrace();
            }
        }

        return new ArrayList<String>();
    }

    // //////////////////////////ISO Associate Setting Begin
    // /////////////////////////////////////

    /*
     * This method is used to convert ISO value automatically from Qualcomm
     * format to MTK ,and vice versa
     */

    public boolean isISOModeSupport() {
        if (mParamMethodMap.containsKey("getSupportedISOSpeed")
                || mParamMethodMap.containsKey("getSupportedIsoValues")) {
            return true;
        }
        return false;
    }

    public String parseISOValueFormat(String value) {
        boolean isQualcomm = false;
        if (mParamMethodMap.containsKey("getSupportedIsoValues")) {
            isQualcomm = true;
        }
        if("auto".equalsIgnoreCase(value)){
            return value;
        }

        String realValue = "";
        String reg = "[\\d]+";
        Pattern pat = Pattern.compile(reg);
        Matcher mat = pat.matcher(value);
        if (mat.find()) {
            realValue = mat.group();
        }
        if (isQualcomm) {
            return "ISO" + realValue;
        } else {
            return realValue;
        }
    }

    public List<String> getSupportedISOValues() {
        if (mParamMethodMap.containsKey("getSupportedISOSpeed")) {
            return query("getSupportedZSDMode");
        } else if (mParamMethodMap.containsKey("getSupportedIsoValues")) {
            return query("getSupportedIsoValues");
        }
        return new ArrayList<String>();
    }

    public void setISOValue(String value) {
        if (mParamMethodMap.containsKey("setISOSpeed")) {
            set("setISOSpeed", value);
        } else if (mParamMethodMap.containsKey("setISOValue")) {
            set("setISOValue", value);
        }
    }
    
    public void setContinuousISOValue(int value){
    	if(!"iso".equals(mParameters.get("iso"))){
    		mParameters.set("iso", "manual");
    	}
    	mParameters.set("continuous-iso", value);
    }

    // //////////////////////////ISO Associate Setting
    // End////////////////////////////////////

    /*************************** ZSL Associate Setting *********************************/
    public String parseZSLValueFormat(String value) {
        // if (value == null || value.length() < 2) {
        // return null;
        // }
        // boolean isQcom = false;
        // if (mParamMethodMap.containsKey("setZSLMode")) {
        // isQcom = true;
        // }
        //
        // if (isQcom) {
        // value = value.toLowerCase();
        // } else {
        // value = value.replaceFirst(value.substring(0, 1),
        // value.substring(0, 1).toUpperCase());
        // }

        return value;
    }

    public List<String> getSupportedZSLValues() {
        if (mParamMethodMap.containsKey("getSupportedZSDMode")) {
            return query("getSupportedZSDMode");

        } else if (mParamMethodMap.containsKey("getSupportedZSLModes")) {
            return query("getSupportedZSLModes");
        }
        return new ArrayList<String>();
    }

    public void setZSLMode(String value) {
        if (value == null) {
            return;
        }
        boolean isQcom = false;
        if (mParamMethodMap.containsKey("setZSDMode")) {
            set("setZSDMode", value);
        } else if (mParamMethodMap.containsKey("setZSLMode")) {
            set("setZSLMode", value);
            isQcom = true;
        }

        // if platform is qcom,then set camera-mode
        if (isQcom && mParamMethodMap.containsKey("setCameraMode")) {
            int cameraModeValue = 0;
            if (value.equalsIgnoreCase("on")) {
                cameraModeValue = 1;
            }
            set("setCameraMode", cameraModeValue);
        }
    }

    /*************************** Face Detection Associate Setting ********************/
    // This function is used by qcom
    public List<String> getSupportedFDModeValues() {
        return query("getSupportedFaceDetectionModes");
    }

    // This function is used by qcom
    public void setFaceDetectionMode(String value) {
        set("setFaceDetectionMode", value);
    }

    /*************************** Set Brightness Associate Setting ********************/
    // Set Brightness.
    public void SetBrightness(String value) {
        // TODO
        mParameters.set("luma-adaptation", value);
    }

    /*************************** TouchAf Associate Setting ********************/
    public List<String> getSupportedTouchAfAec() {
        // TODO
        return query("getSupportedTouchAfAec");
    }

    public void setTouchAfAec(String value) {
        // TODO
        set("setTouchAfAec", value);
    }

    /****************** Selectable Zone Af Associate Setting ********************/
    public List<String> getSupportedSelectableZoneAf() {
        // TODO
        return query("getSupportedSelectableZoneAf");
    }

    public void setSelectableZoneAf(String value) {
        // TODO
        set("setSelectableZoneAf", value);
    }

    /****************** wavelet denoise Associate Setting ********************/
    public List<String> getSupportedDenoiseModes() {
        // TODO
        return query("getSupportedDenoiseModes");
    }

    public void setDenoise(String value) {
        // TODO
        set("setDenoise", value);
    }

    /****************** Redeye Reduction Associate Setting ********************/
    public List<String> getSupportedRedeyeReductionModes() {
        // TODO
        return query("getSupportedRedeyeReductionModes");
    }

    public void setRedeyeReductionMode(String value) {
        // TODO
        set("setRedeyeReductionMode", value);
    }

    /****************** Set Saturation Associate Setting ********************/
    public void setSaturation(String value) {
        // TODO
        set("setSaturation", Integer.valueOf(value));
    }

    /****************** Set Saturation Associate Setting ********************/
    public void setContrast(String value) {
        // TODO
        set("setContrast", Integer.valueOf(value));
    }

    /****************** Set sharpness Associate Setting ********************/
    public void setSharpness(String value) {
        // TODO
        set("setSharpness", Integer.valueOf(value));
    }

    /****************** Set auto exposure Associate Setting ********************/
    public List<String> getSupportedAutoexposure() {
        // TODO
        return query("getSupportedAutoexposure");
    }

    public void setAutoExposure(String value) {
        // TODO
        set("setAutoExposure", value);
    }

    /********************set Video hdr**************************************************/
    public List<String> getSupportedVideoHDRModes(){
        //TODO;
        return query("getSupportedVideoHDRModes");
    }

    public void setVideoHDRMode(String value){
        //TODO
        set("setVideoHDRMode", value);
    }
    
    /*******************set burst shot in mtk *****************************************/
    private void setCaptureNum(int num){
    	set("setBurstShotNum",num);
    }

    /*******************set tintless*************************************************************/
    public void setTintless(String value){//value:"enable"/"disable"
        //TODO
        mParameters.set("tintless",value);
    }

    private static final String CAPTURE_MODE_CONTINUOUS_SHOT="continuousshot";
    private static final String CAPTURE_MODE_NORMAL="normal";
    public void setBurstShot(boolean enable){
    	final String setCaptureMode="setCaptureMode"; 
    	if(enable){
    		setCaptureNum(40);
    		set(setCaptureMode,CAPTURE_MODE_CONTINUOUS_SHOT);
    	}else{
    		setCaptureNum(1);
    		set(setCaptureMode,CAPTURE_MODE_NORMAL);
    	}
    }
}
