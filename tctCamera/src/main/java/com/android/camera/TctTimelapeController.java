package com.android.camera;

public interface TctTimelapeController {
	
	// Callbacks for camera preview UI events.
    public void onPreviewUIReady();
    public void onPreviewUIDestroyed();
    public void onFocusChanged(float xCord, float yCord);
}
