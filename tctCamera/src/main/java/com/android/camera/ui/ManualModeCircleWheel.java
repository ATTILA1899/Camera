package com.android.camera.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import android.util.Log;

public class ManualModeCircleWheel extends ImageView implements View.OnTouchListener {
    private static final String TAG = "ManualModeCircleWheel";

    private Bitmap imageOriginal, imageScaled;     //variables for original and re-sized image
    private Matrix matrix;                         //Matrix used to perform rotations
    private int wheelHeight, wheelWidth;           //height and width of the view
    private int mHalfWheelHeight, mHalfWheelWidth;
    private int top;                               //the current top of the wheel (calculated in wheel divs)
    private double totalRotation;                  //variable that counts the total rotation
    // during a given rotation of the wheel by the user (from ACTION_DOWN to ACTION_UP)
    private int divCount;                          //no of divisions in the wheel
    private int divAngle;                          //angle of each division
    private int selectedPosition;                  //the section currently selected by the user.
    private Context context;
    private WheelChangeListener wheelChangeListener;

    private double mStartAngle;

    private static String[] MANUAL_MODE_VALUES;

    public interface WheelChangeListener {
        public void onSelectionChange(String newValue);
    }

    public ManualModeCircleWheel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setSupportValues(String[] values) {
        MANUAL_MODE_VALUES = values;
    }

    //initializations
    private void init(Context context) {
        this.context = context;
        this.setScaleType(ScaleType.MATRIX);
        selectedPosition = 0;

        // initialize the matrix only once
        if (matrix == null) {
            matrix = new Matrix();
        } else {
            matrix.reset();
        }

        this.setOnTouchListener(this);
    }

    public void setWheelChangeListener(WheelChangeListener wheelChangeListener) {
        this.wheelChangeListener = wheelChangeListener;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setDivCount(int divCount) {
        this.divCount = divCount;

        divAngle = 360 / divCount;
        totalRotation = -1 * (divAngle / 2);
    }

    public void setAlternateTopDiv(int newTopDiv) {
        if (newTopDiv < 0 || newTopDiv >= divCount)
            return;
        else
            top = newTopDiv;
        // ajust the wheel when update the topDiv
        if (wheelHeight != 0 && wheelWidth != 0)
            updateLayoutWheel(newTopDiv);

        selectedPosition = top;
    }

    public void setWheelImage(int drawableId) {
        imageOriginal = BitmapFactory.decodeResource(context.getResources(), drawableId);
    }

    private void updateLayoutWheel(int newTopDiv) {
        if (matrix == null)
            return;
        matrix.postRotate((float) divAngle * (selectedPosition - newTopDiv), mHalfWheelWidth, mHalfWheelHeight);
        ManualModeCircleWheel.this.setImageMatrix(matrix);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // method called multiple times but initialized just once
        if (wheelHeight == 0 || wheelWidth == 0) {
            wheelHeight = h;
            wheelWidth = w;
            mHalfWheelHeight = h / 2;
            mHalfWheelWidth = w / 2;
            // resize the image
            Matrix resize = new Matrix();
            resize.postScale((float) Math.min(wheelWidth, wheelHeight) / (float) imageOriginal
                    .getWidth(), (float) Math.min(wheelWidth,
                    wheelHeight) / (float) imageOriginal.getHeight());
            imageScaled = Bitmap.createBitmap(imageOriginal, 0, 0, imageOriginal.getWidth(),
                    imageOriginal.getHeight(), resize, false);
            // translate the matrix to the image view's center
            float translateX = mHalfWheelWidth - imageScaled.getWidth() / 2;
            float translateY = mHalfWheelHeight - imageScaled.getHeight() / 2;
            matrix.postTranslate(translateX, translateY);

            // negative degree is clockwise
            matrix.postRotate((float) -1 * divAngle * selectedPosition, mHalfWheelWidth, mHalfWheelHeight);
            ManualModeCircleWheel.this.setImageBitmap(imageScaled);
            ManualModeCircleWheel.this.setImageMatrix(matrix);
        }
    }

    private double getAngle(double x, double y) {
        x = x - mHalfWheelWidth;
        y = mHalfWheelHeight - y;

        switch (getQuadrant(x, y)) {
            case 1:
                return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 2:
                return 180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 3:
                return 180 + (-1 * Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
            case 4:
                return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            default:
                return 0;
        }
    }

    private static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }

    private void rotateWheel(float degrees) {
        matrix.postRotate(degrees, mHalfWheelWidth, mHalfWheelHeight);
        ManualModeCircleWheel.this.setImageMatrix(matrix);

        //add the rotation to the total rotation
        totalRotation = totalRotation + degrees;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                //get the start angle for the current move event
                mStartAngle = getAngle(event.getX(), event.getY());
                break;


            case MotionEvent.ACTION_MOVE:
                //get the current angle for the current move event
                double currentAngle = getAngle(event.getX(), event.getY());

                //rotate the wheel by the difference
                rotateWheel((float) (mStartAngle - currentAngle));

                //current angle becomes start angle for the next motion
                mStartAngle = currentAngle;
                break;


            case MotionEvent.ACTION_UP:
                //get the total angle rotated in 360 degrees
                totalRotation = totalRotation % 360;

                //represent total rotation in positive value
                if (totalRotation < 0) {
                    totalRotation = 360 + totalRotation;
                }

                //calculate the no of divs the rotation has crossed
                int no_of_divs_crossed = (int) ((totalRotation) / divAngle);

                //calculate current top
                top = (divCount + top - no_of_divs_crossed) % divCount;

                //for next rotation, the initial total rotation will be the no of degrees
                // inside the current top
                totalRotation = totalRotation % divAngle;

                //calculate the angle to be rotated to reach the top's center.
                double leftover = divAngle / 2 - totalRotation;

                rotateWheel((float) (leftover));

                //re-initialize total rotation
                totalRotation = divAngle / 2;

                //set the currently selected option
                if (top == 0) {
                    selectedPosition = divCount - 1;//loop around the array
                } else {
                    selectedPosition = top - 1;
                }

                if (wheelChangeListener != null) {
                    wheelChangeListener.onSelectionChange(MANUAL_MODE_VALUES[selectedPosition]);
                }

                break;
        }

        return true;
    }

}

