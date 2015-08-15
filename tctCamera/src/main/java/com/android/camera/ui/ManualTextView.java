
package com.android.camera.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.graphics.Color;
import android.widget.TextView;

import com.tct.camera.R;

public class ManualTextView extends TextView {
    // Position represents the index of this view in the viewgroups children
    // array
    private int position = 0;
    // The name of the view
    private String mEntry;
    private String mEntryValue;

    // the text size follow the ManualTextView's
    private static final int MANUAL_ITEM_TEXT_SIZE = 38;


    /**
     * Return the position of the view.
     * 
     * @return Returns the position of the view.
     */
    public int getPosition() {
        return position;
    }

    /**
     * Set the position of the view.
     * 
     * @param position The position to be set for the view.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Return the name of the view.
     * 
     * @return Returns the name of the view.
     */
    public String getName() {
        return mEntry;
    }
    /**
     * Return the name of the view.
     * 
     * @return Returns the name of the view.
     */
    public String getEntryValue() {
        return mEntryValue;
    }
    /**
     * Set the name of the view.
     * 
     * @param name The name to be set for the view.
     */
    public void setEntryValue(String name) {
        this.mEntryValue = name;
    }

    /**
     * Set the name of the view.
     * 
     * @param name The name to be set for the view.
     */
    public void setName(String name) {
        this.mEntry = name;
    }

    public ManualTextView(Context context) {
        this(context, null);
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, MANUAL_ITEM_TEXT_SIZE);
    }

    public ManualTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, MANUAL_ITEM_TEXT_SIZE);
    }

    public ManualTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (attrs != null) {
            TypedArray array = getContext().obtainStyledAttributes(attrs,
                        R.styleable.ManualModeCircleView);

            mEntry = array.getString(R.styleable.ManualModeCircleView_name);
        }
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, MANUAL_ITEM_TEXT_SIZE);
        this.setTextColor(Color.parseColor("#808080"));
    }
}

