
package com.android.camera.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.tct.camera.R;

public class ManualModeCircleCtlLayout extends ViewGroup {
    private static final String TAG = "ManualModeCircleCtlLayout";

    // Event listeners
    private OnItemSelectedListener mOnItemSelectedListener = null;

    // Background image
    private Bitmap imageOriginal, mImageScaled;
    private Matrix mMatrix;

    private int mSelectedPosId = 0;

    // Child sizes
    private int mMaxChildWidth = 0;
    private int mMaxChildHeight = 0;

    // Sizes of the ViewGroup
    private int mRadius = 0;
    private int mCircleViewHeight, mCircleViewWidth;
    // decrease the division operation (DIV 2)
    private int mCircleViewHalfHeight, mCircleViewHalfWidth;

    private double mStartAngle;

    // Settings of the ViewGroup
    private boolean mAllowRotating = true;
    private float mAngle = 90;
    private float firstChildPos = 90;
    private boolean isRotating = true;
    private int speed = 75;
    private float deceleration = 1 + (5f / speed);
    private float mRotatedDegrees = 0f;

    private int mChildCount = 0;
    private int[] mChildRealWidth;

    // 0 - textview, 1 - imageview
    private int mCurrentViewType = 0;
    public static final int MANUAL_MODE_CIRCLE_TEXTVIEW_TYPE = 0;
    public static final int MANUAL_MODE_CIRCLE_IMAGEVIEW_TYPE = 1;
    //circle view type maybe have others, use the start and end var to clamp the type
    public static final int MANUAL_MODE_CIRCLE_VIEW_TYPE_START = MANUAL_MODE_CIRCLE_TEXTVIEW_TYPE;
    public static final int MANUAL_MODE_CIRCLE_VIEW_TYPE_END = MANUAL_MODE_CIRCLE_IMAGEVIEW_TYPE;


    public ManualModeCircleCtlLayout(Context context) {
        this(context, null);
    }

    public ManualModeCircleCtlLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ManualModeCircleCtlLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(attrs);
    }

    private void updateFirstChildAngle() {
        mAngle = firstChildPos;
    }

    public void setManualModeCircleViewType(int viewType) {
        if (viewType >= MANUAL_MODE_CIRCLE_VIEW_TYPE_START 
            && viewType <= MANUAL_MODE_CIRCLE_VIEW_TYPE_END) {
            mCurrentViewType = viewType;
        } else {
            Log.e(TAG, "WARNING! the given viewtype=" + viewType + "is not supproted ! SO, set the defalut type!");
            mCurrentViewType = MANUAL_MODE_CIRCLE_TEXTVIEW_TYPE;
        }
        mStartSpecialDegree = false;
        updateFirstChildAngle();
    }

    protected void initialize(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs,
                    R.styleable.ManualModeCircleViewLayout);

            // The angle where the first menu item will be drawn
            mAngle = a.getInt(R.styleable.ManualModeCircleViewLayout_firstChildPosition, 270);
            firstChildPos = mAngle;

            isRotating = a.getBoolean(R.styleable.ManualModeCircleViewLayout_isRotating, true);
            speed = a.getInt(R.styleable.ManualModeCircleViewLayout_speed, 75);
            deceleration = 1 + (5f / speed);
            mCurrentViewType = a.getInt(R.styleable.ManualModeCircleViewLayout_viewType, 0);
            if (imageOriginal == null) {
                int circleBgResId = a.getResourceId(
                        R.styleable.ManualModeCircleViewLayout_circleBackground, -1);

                // If a background image was set as an attribute,
                // retrieve the image
                if (circleBgResId != -1) {
                    imageOriginal = BitmapFactory.decodeResource(
                            getResources(), circleBgResId);
                }
            }

            a.recycle();

            // initialize the matrix only once
            if (mMatrix == null) {
                mMatrix = new Matrix();
            } else {
                // not needed, you can also post the matrix immediately to
                // restore the old state
                mMatrix.reset();
            }

            // Needed for the ViewGroup to be drawn
            setWillNotDraw(false);
        }
    }

    public View getSelectedItem() {
        return (mSelectedPosId >= 0) ? getChildAt(mSelectedPosId) : null;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mImageScaled != null) {
            // Move the background to the center
            int cx = (mCircleViewWidth - mImageScaled.getWidth()) / 2;
            int cy = (mCircleViewHeight - mImageScaled.getHeight()) / 2;

            canvas.rotate(0, mCircleViewHalfWidth, mCircleViewHalfHeight);
            canvas.drawBitmap(mImageScaled, cx, cy, null);

        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mCircleViewHeight == 0 || mCircleViewWidth == 0) {
            mCircleViewHeight = h;
            mCircleViewWidth = w;

            mCircleViewHalfWidth = w / 2;
            mCircleViewHalfHeight = h / 2;

            float sx = (float) Math.min(mCircleViewWidth, mCircleViewHeight)
                    / (float) imageOriginal.getWidth();
            float sy = (float) Math.min(mCircleViewWidth, mCircleViewHeight)
                    / (float) imageOriginal.getHeight();
            mMatrix.postScale(sx, sy);
            mImageScaled = Bitmap.createBitmap(imageOriginal, 0, 0, imageOriginal.getWidth(),
                    imageOriginal.getHeight(), mMatrix, false);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMaxChildWidth = 0;
        mMaxChildHeight = 0;
        // onMeasure will be called firstly, so update the childcount here
        mChildCount = getChildCount(); 
        if (mChildRealWidth == null || mChildRealWidth.length != mChildCount) {
            mChildRealWidth = new int[mChildCount];
        }

        // Measure once to find the maximum child size.
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST);
        for (int i = 0; i < mChildCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

            mMaxChildWidth = Math.max(mMaxChildWidth, child.getMeasuredWidth());
            mMaxChildHeight = Math.max(mMaxChildHeight,
                    child.getMeasuredHeight());
        }

        // Measure again for each child to be exactly the same size.
        childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxChildWidth,
                MeasureSpec.EXACTLY);
        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxChildHeight,
                MeasureSpec.EXACTLY);

        for (int i = 0; i < mChildCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            mChildRealWidth[i] = mCurrentViewType == MANUAL_MODE_CIRCLE_TEXTVIEW_TYPE ? child.getMeasuredWidth() :
                    (int) (child.getMeasuredWidth() / 1.3);
        }

        setMeasuredDimension(resolveSize(mMaxChildWidth, widthMeasureSpec),
                resolveSize(mMaxChildHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int layoutWidth = r - l;
        int layoutHeight = b - t;
        mRadius = (layoutWidth <= layoutHeight) ? layoutWidth / 2 : layoutHeight / 2;

        doOnLayout();
    }

    private boolean mStartSpecialDegree = false;
    
    private void doOnLayout() {
        int left, top, halfChildWidth;
        float r;
        float angleDelay = 360.0f / mChildCount;

        for (int i = 0; i < mChildCount; i++) {
            if (mCurrentViewType == MANUAL_MODE_CIRCLE_TEXTVIEW_TYPE) {
                final CircleTextView child = (CircleTextView) getChildAt(i);
                if (child.getVisibility() == GONE) {
                    continue;
                }

                if (mAngle > 360) {
                    mAngle -= 360;
                } else if (mAngle < 0) {
                    mAngle += 360;
                }

                child.setAngle(mAngle);

                child.setRotation(mAngle + 90);
                halfChildWidth = mChildRealWidth[i] / 2;
                child.setPosition(i);

                r = mRadius - halfChildWidth / 0.8f;
                left = Math.round((float) ((mCircleViewHalfWidth - halfChildWidth) + r
                        * Math.cos(Math.toRadians(mAngle))));
                top = Math.round((float) ((mCircleViewHalfHeight - halfChildWidth) + r
                        * Math.sin(Math.toRadians(mAngle))));
                if (Math.abs(mAngle - firstChildPos) < (angleDelay / 2)
                        && mSelectedPosId != child.getPosition()) {
                    mSelectedPosId = child.getPosition();
                    // the placeholder view is just for space occupying, not showing
                    if (" ".equalsIgnoreCase(child.getName())) {
                        mStartSpecialDegree = true;
                    } else {
                        mStartSpecialDegree = false;
                    }

                    // ignore the placeholder
                    if (mOnItemSelectedListener != null && !mStartSpecialDegree) {
                        mOnItemSelectedListener.onSelectionChange(child, child.getName(), child.getEntryValue());
                    }
                }
                child.layout(left, top, left + mChildRealWidth[i], top + mChildRealWidth[i]);
            } else {
                final CircleImageView child = (CircleImageView) getChildAt(i);
                if (child.getVisibility() == GONE) {
                    continue;
                }

                if (mAngle > 360) {
                    mAngle -= 360;
                } else if (mAngle < 0) {
                    mAngle += 360;
                }

                child.setAngle(mAngle);

                child.setRotation(mAngle + 90);
                halfChildWidth = mChildRealWidth[i] / 2;
                child.setPosition(i);
                // tuning the fator will change the distance between imageview pos and center point
                // the higher fator is, more farther will the distance be
                r = mRadius - halfChildWidth / 1.2f;
                left = Math.round((float) ((mCircleViewHalfWidth - halfChildWidth) + r
                        * Math.cos(Math.toRadians(mAngle))));
                top = Math.round((float) ((mCircleViewHalfHeight - halfChildWidth) + r
                        * Math.sin(Math.toRadians(mAngle))));
                if (Math.abs(mAngle - firstChildPos) < (angleDelay / 2)
                        && mSelectedPosId != child.getPosition()) {
                    mSelectedPosId = child.getPosition();

                    if (mOnItemSelectedListener != null) {
                        //this here just because the entry is not for view,so I just use the entry to the value
                        mOnItemSelectedListener.onSelectionChange(child, child.getName(), child.getName());
                    }
                }
                child.layout(left, top, left + mChildRealWidth[i], top + mChildRealWidth[i]);
            }

            mAngle += angleDelay;
        }
    }

    private void rotateCircleViews(float degrees) {
        mAngle += degrees;
        if (mStartSpecialDegree) {
            if (Math.abs(degrees) < 100) {
                mRotatedDegrees += degrees;
                mRotatedDegrees = mRotatedDegrees % 360;
                if (mOnItemSelectedListener != null) {
                    mOnItemSelectedListener.onSpecialRegionAngleUpdate(degrees, degrees > 0);
                }
            }
        } else {
            mRotatedDegrees = 0f;
        }
        doOnLayout();
    }

    private double getAngle(float xTouch, float yTouch) {
        float deltaX = xTouch - mCircleViewHalfWidth;
        float deltaY = mCircleViewHalfHeight - yTouch;

        double radians = Math.atan2(deltaY, deltaX);

        return Math.toDegrees(radians);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            if (isRotating) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mAllowRotating = false;

                        mStartAngle = getAngle(event.getX(), event.getY());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        double currentAngle = getAngle(event.getX(), event.getY());
                        rotateCircleViews((float) (mStartAngle - currentAngle));
                        mStartAngle = currentAngle;
                        break;
                    case MotionEvent.ACTION_UP:
                        mAllowRotating = true;
                        if (mCurrentViewType == MANUAL_MODE_CIRCLE_TEXTVIEW_TYPE) {
                            CircleTextView ctv = (CircleTextView)getChildAt(mSelectedPosId);
                            if (ctv != null && !" ".equalsIgnoreCase(ctv.getName())) {
                                ajustViewRemainingRotation(ctv, false);
                            }
                        } else {
                            ajustViewRemainingRotation((CircleImageView) getChildAt(mSelectedPosId), false);
                        }
                        break;
                }
            }

            return true;
        }
        return false;
    }


    private void ajustViewRemainingRotation(CircleImageView view, boolean fromRunnable) {
        float velocityTemp = 1;
        float destAngle = (float) (firstChildPos - view.getAngle());
        float startAngle = 0;
        int reverser = 1;

        if (destAngle < 0) {
            destAngle += 360;
        }

        if (destAngle > 180) {
            reverser = -1;
            destAngle = 360 - destAngle;
        }

        while (startAngle < destAngle) {
            velocityTemp *= deceleration;
            startAngle += velocityTemp / speed;
        }

        ManualModeCircleCtlLayout.this.post(new FlingRunnable(reverser * velocityTemp,
                    !fromRunnable));
    }

    private void ajustViewRemainingRotation(CircleTextView view, boolean fromRunnable) {
        float velocityTemp = 1;
        float destAngle = (float) (firstChildPos - view.getAngle());
        float startAngle = 0;
        int reverser = 1;

        if (destAngle < 0) {
            destAngle += 360;
        }

        if (destAngle > 180) {
            reverser = -1;
            destAngle = 360 - destAngle;
        }

        while (startAngle < destAngle) {
            velocityTemp *= deceleration;
            startAngle += velocityTemp / speed;
        }

        ManualModeCircleCtlLayout.this.post(new FlingRunnable(reverser * velocityTemp,
                    !fromRunnable));
    }

    /**
     * A {@link Runnable} for animating the menu rotation.
     */
    private class FlingRunnable implements Runnable {

        private float velocity;
        private float angleDelay;
        private boolean isFirstForwarding = true;

        public FlingRunnable(float velocity, boolean isFirst) {
            this.velocity = velocity;
            this.angleDelay = 360.0f / mChildCount;
            this.isFirstForwarding = isFirst;

        }

        public void run() {
            if (mAllowRotating) {
                if (Math.abs(velocity) > 1) {
                    if (!(Math.abs(velocity) < 200 && (Math.abs(mAngle
                                - firstChildPos)
                                % angleDelay < 2))) {
                        rotateCircleViews(velocity / speed);
                        velocity /= deceleration;

                        ManualModeCircleCtlLayout.this.post(this);
                    }
                } else {
                    if (isFirstForwarding) {
                        isFirstForwarding = false;

                        if (mCurrentViewType == MANUAL_MODE_CIRCLE_TEXTVIEW_TYPE) {
                            ManualModeCircleCtlLayout.this.ajustViewRemainingRotation(
                                    (CircleTextView) getChildAt(mSelectedPosId),
                                    true);
                        } else {
                            ManualModeCircleCtlLayout.this.ajustViewRemainingRotation(
                                    (CircleImageView) getChildAt(mSelectedPosId),
                                    true);
                        }
                    }
                }
            }
        }
    }

    public interface OnItemSelectedListener {
        void onSelectionChange(View view, String name, String entryValue);
        void onSpecialRegionAngleUpdate(float angle, boolean clockwise);
    }

    public void setOnItemSelectedListener(
            OnItemSelectedListener onItemSelectedListener) {
        this.mOnItemSelectedListener = onItemSelectedListener;
    }
}

