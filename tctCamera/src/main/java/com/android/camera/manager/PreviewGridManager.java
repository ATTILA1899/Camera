package com.android.camera.manager;

import android.view.View;

import com.android.camera.CameraActivity;
import com.tct.camera.R;

public class PreviewGridManager extends ViewManager{

	public PreviewGridManager(CameraActivity context, int layer) {
		super(context, layer);
	}

	@Override
	protected View getView() {
		View view = inflate(R.layout.camera_grid);
		return view;
	}
	
	

}
