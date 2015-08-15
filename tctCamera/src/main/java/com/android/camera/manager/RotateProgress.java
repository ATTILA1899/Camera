package com.android.camera.manager;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.ui.RotateLayout;
import com.tct.camera.R;

public class RotateProgress extends ViewManager {
    private static final String TAG = "RotateProgress";

    private ProgressBar mRotateDialogSpinner;
    private TextView mRotateDialogText;

    private String mMessage;

    //[BUGFIX]-ADD-BEGIN by xuan.zhou, PR-611831, 2014-3-12
    private RotateLayout mRotateDialog;
    private int orientation;
    //[BUGFIX]-ADD-END by xuan.zhou, PR-611831, 2014-3-12
    private boolean spinnerVisible = true;

    public RotateProgress(CameraActivity context, int viewlayer) {
        super(context, viewlayer);
    }

    @Override
    protected View getView() {
        View v = inflate(R.layout.rotate_progress);
        //[BUGFIX]-ADD-BEGIN by xuan.zhou, PR-611831, 2014-3-12
        mRotateDialog = (RotateLayout) v
                .findViewById(R.id.rotate_dialog_layout);
        //[BUGFIX]-ADD-END by xuan.zhou, PR-611831, 2014-3-12
        mRotateDialogSpinner = (ProgressBar) v
                .findViewById(R.id.rotate_dialog_spinner);
        mRotateDialogText = (TextView) v.findViewById(R.id.rotate_dialog_text);
        return v;
    }

    @Override
    protected void onRefresh() {
        //[BUGFIX]-ADD-BEGIN by xuan.zhou, PR-611831, 2014-3-12
        mRotateDialog.setOrientation(orientation, false);
        //[BUGFIX]-ADD-END by xuan.zhou, PR-611831, 2014-3-12
        mRotateDialogText.setText(mMessage);
        mRotateDialogText.setVisibility(View.VISIBLE);
      //[BUGFIX]-ADD-BEGIN by tiexing.xie PR-887150  2015-01-21
        if(spinnerVisible){
            mRotateDialogSpinner.setVisibility(View.VISIBLE);
        }else{
            mRotateDialogSpinner.setVisibility(View.GONE);
            spinnerVisible = true;
        }
      //[BUGFIX]-ADD-END by tiexing.xie PR-887150  2015-01-21
    }

    public void showProgress(String msg) {
        mMessage = msg;
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reInflate();
                show();
            }
        });
    }

    //[BUGFIX]-ADD-BEGIN by xuan.zhou, PR-611831, 2014-3-12
    public void showProgress(String msg, int ori) {
        mMessage = msg;
        orientation = ori;
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reInflate();
                show();
            }
        });
    }
    //[BUGFIX]-ADD-END by xuan.zhou, PR-611831, 2014-3-12

    //[BUGFIX]-ADD-BEGIN by tiexing.xie PR-887150  2015-01-21
    public void showProgress(String msg, int ori, boolean spinner){
        mMessage = msg;
        orientation = ori;
        spinnerVisible = spinner;
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reInflate();
                show();
            }
        });
    }
  //[BUGFIX]-ADD-END by tiexing.xie PR-887150  2015-01-21
}
