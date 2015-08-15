package com.android.camera.manager;

import com.android.camera.util.BitmapUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.ExpressionThread;
import android.widget.ImageView;
import com.android.camera.ui.RotateImageView;
import com.tct.camera.R;

public class ExpressionThumbnailManager extends ViewManager implements OnClickListener {
    private static final String TAG = "ExpressionThumbnailManager";
    private static final boolean LOG = true;
    private Bitmap[] bitmaps = new Bitmap[ExpressionThread.MAXNUM];
    private ImageView[] mThumbnailViews = new ImageView[ExpressionThread.MAXNUM];
    public static int [] mThumbnailId= { R.id.expression1, R.id.expression2, R.id.expression3, R.id.expression4};
    private TableLayout mTableLayout;
    private int mLocalThumbnailIndex = -1; // used to check & sync the index with ExpressionThread.

    //the index of thumbnail
    private TextView[] mThumbnailTextViews = new TextView[ExpressionThread.MAXNUM];
    public static int [] mThumbnailTextId= { R.id.expression1_text,
        R.id.expression2_text, R.id.expression3_text, R.id.expression4_text};

    private static int [] mImageText={R.string.expression_text_1,
                   R.string.expression_text_2,
                   R.string.expression_text_3,
                   R.string.expression_text_4};

    //the order of image show in GUI
    private int [] mImageShowOrder={0,1,2,3};
    private int [] mImageShowOrder0= new int [] {0,1,2,3};
    private int [] mImageShowOrder90= new int [] {3,0,1,2};
    private int [] mImageShowOrder180= new int [] {2,3,0,1};
    private int [] mImageShowOrder270= new int [] {1,2,3,0};

    //the order of imgae split
    private int [] mImageCollageOrder={0,1,2,3};
    private int [] mImageCollageOrder0= new int [] {0,1,2,3};
    private int [] mImageCollageOrder90= new int [] {1,2,3,0};
    private int [] mImageCollageOrder180= new int [] {2,3,0,1};
    private int [] mImageCollageOrder270= new int [] {3,0,1,2};

    private int mOrientation = -1;
    private boolean needUpdateLayout = false;

    public ExpressionThumbnailManager(CameraActivity context) {
        super(context);
    }

    public ExpressionThumbnailManager(CameraActivity context, int layer) {
        super(context, layer);
    }

    @Override
    protected View getView() {
        View view = inflate(R.layout.expression_thumbnail);
        mTableLayout = (TableLayout) view.findViewById(R.id.expression_tablelayout);
        for (int i=0; i<ExpressionThread.MAXNUM; i++) {
            mThumbnailViews[i] = (ImageView) view.findViewById(mThumbnailId[i]);
            mThumbnailViews[i].setClickable(true);
            mThumbnailViews[i].setOnClickListener(this);

            mThumbnailTextViews[i] = (TextView) view.findViewById(mThumbnailTextId[i]);
        }
        if(mOrientation == -1){
            updateLayout(0);
        }else{
            updateLayout(mOrientation);
        }
        return view;
    }

    @Override
    protected void onRefresh() {
        if (mGC.getCurrentMode() != mGC.MODE_EXPRESSION4) {
            resetThumbnail2Null();
            hide();
        }
        needUpdateLayout=true;
    }

    @Override
    protected void onRelease() {
        mLocalThumbnailIndex = -1;
        for (int index=0; index<ExpressionThread.MAXNUM; index++) {
            if (bitmaps[index] != null && !bitmaps[index].isRecycled()) {
                Log.i(TAG,"zsy1204 bitmaps recycle index = " + index);
                bitmaps[index].recycle();
            }
        }
    }

    public void resetThumbnail2Null() {
        mLocalThumbnailIndex = -1;
        for (int i=0; i<ExpressionThread.MAXNUM; i++) {
            if (mThumbnailViews[i] != null) {
                //mThumbnailViews[i].setBitmap(null);
                mThumbnailViews[i].setImageBitmap(null);
            }
        }
    }

    // [BUGFIX]-ADD-BEGIN by TCTNB(Shangyong.Zhang), 2014/01/06, PR583294.
    // return true means current capture is failed and need to back the index.
    public boolean checkExpressionThumbnail(int index) {
        if (index < 0) return false;
        Log.i(TAG,"zsy1031 checkExpressionThumbnail index = " + index);
        Log.i(TAG,"zsy1031 checkExpressionThumbnail mLocalThumbnailIndex = " + mLocalThumbnailIndex);
        if (index - mLocalThumbnailIndex == 1)
            return true;
        else
            return false;
    }
    // [BUGFIX]-ADD-END by TCTNB(Shangyong.Zhang), 2014/01/06.

    public void refreshThumbnail(Bitmap bitmap) {
        //mThumbnailViews[0].setBitmap(bitmap);
        if (!ExpressionThread.isExpressionIndexOk()) {
            return;
        }
        int index = ExpressionThread.getExpressionIndex();
        index =mImageShowOrder[index];
        if (mThumbnailViews[index] != null) {
            if (isShowing()) {
                if (bitmaps[index] != null && !bitmaps[index].isRecycled()) {
                    Log.i(TAG,"zsy1204 bitmaps recycle index = " + index);
                    bitmaps[index].recycle();
                }
                // [BUGFIX]-ADD-BEGIN by TCTNB(Shangyong.Zhang), 2014/01/06, PR583294.
                mLocalThumbnailIndex ++;
                int wMargin = 0;
                int hMargin = 0;
                if (index == 0) { // do this in first capture.
                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();
                    for(int i=0; i<ExpressionThread.MAXNUM; i++) {
                        LinearLayout.LayoutParams para = (LinearLayout.LayoutParams)mThumbnailViews[i].getLayoutParams();
                        para.height = (int)(para.width*h/w);
                        wMargin += para.leftMargin;
                        hMargin += para.topMargin;
                        mThumbnailViews[i].setLayoutParams(para);
                    }
                    LayoutParams para = mTableLayout.getLayoutParams();
                    LayoutParams para1 = mThumbnailViews[0].getLayoutParams();
                    para.width = para1.width*2 + wMargin;
                    para.height = para1.height*2 + hMargin;
                    mTableLayout.setLayoutParams(para);
                } else if (index == (ExpressionThread.MAXNUM - 1)) {
                    mLocalThumbnailIndex = -1;
                }
                // [BUGFIX]-ADD-END by TCTNB(Shangyong.Zhang), 2014/01/06.
                //bitmaps[index] = bitmap;
                switch(index){
                case 0:
                    bitmaps[index]=BitmapUtil.getRoundedCornerBitmap(bitmap, 60, true, false, false, false);
                    break;
                case 1:
                    bitmaps[index]=BitmapUtil.getRoundedCornerBitmap(bitmap, 60, false, true, false, false);
                    break;
                case 2:
                    bitmaps[index]=BitmapUtil.getRoundedCornerBitmap(bitmap, 60, false, false, false, true);
                    break;
                case 3:
                    bitmaps[index]=BitmapUtil.getRoundedCornerBitmap(bitmap, 60, false, false, true, false);
                    break;
                }
                mThumbnailViews[index].setImageBitmap(bitmaps[index]);
                mThumbnailViews[index].setVisibility(View.VISIBLE);
            } else {
                mThumbnailViews[index].setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        getContext().gotoGallery();
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (mOrientation != orientation || needUpdateLayout) {
            needUpdateLayout = false;
            mOrientation = orientation;
            updateLayout(mOrientation);
        }
    }

    private void updateLayout(int orientation) {
       for(int i=0;i<ExpressionThread.MAXNUM;i++){
           if(mThumbnailTextViews[i]==null){
               return;
           }
           //Do not change the image order as capture is in process
           if(mThumbnailViews[i].isShown()){
               return;
           }
       }
       switch(orientation){
       case 0:
           mImageShowOrder=mImageShowOrder0;
           mImageCollageOrder=mImageCollageOrder0;
           break;
       case 90:
           mImageShowOrder=mImageShowOrder90;
           mImageCollageOrder=mImageCollageOrder90;
           break;
       case 180:
           mImageShowOrder=mImageShowOrder180;
           mImageCollageOrder=mImageCollageOrder180;
           break;
       case 270:
           mImageShowOrder=mImageShowOrder270;
           mImageCollageOrder=mImageCollageOrder270;
           break;
       }
       for(int i=0;i<ExpressionThread.MAXNUM;i++){
           mThumbnailTextViews[i].setText(mImageText[mImageCollageOrder[i]]);
       }
    }

    //get the split order of all images
    public int[] getImageOrder(){
        return mImageCollageOrder;
    }
}
