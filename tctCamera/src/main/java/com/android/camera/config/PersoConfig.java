package com.android.camera.config;

import java.lang.reflect.Field;

import com.tct.camera.R;

import android.content.Context;
import android.media.CamcorderProfile;

public class PersoConfig {
	
	public enum CustomSetting{
		INTEROP,
		SHUTTER_SOUND_VISIBLE,
		SHUTTER_SOUND_ON_OFF,
	}
	
	public static boolean queryPerso(Context context,CustomSetting setting){
		boolean value=false;
		int resId=-1;
		String persoId="";
		switch(setting){
		case INTEROP:
			persoId="interpolation";
			break;
		case SHUTTER_SOUND_VISIBLE:
			persoId="shutter_sound_visible";
			break;
		case SHUTTER_SOUND_ON_OFF:
			persoId="shutter_sound_on";
			break;
		default:
			break;
		}
		
		
		try {
			Field f;
			f = R.bool.class.getDeclaredField(persoId);
			f.setAccessible(true);
	        resId = f.getInt(null);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(resId!=R.bool.no_find_flag){
        	value=context.getResources().getBoolean(resId);
        }
		return value;
	}
}
