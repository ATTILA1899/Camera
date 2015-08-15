package com.android.camera.manager;

public class DialogRunnable implements Runnable{
	private int position=-1;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	public void setIndex(int position){
		this.position = position;
	}
	
	public int getIndex(){
		return position;
	}
}