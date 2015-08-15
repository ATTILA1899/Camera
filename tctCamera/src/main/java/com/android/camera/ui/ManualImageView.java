
package com.android.camera.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.tct.camera.R;

public class ManualImageView extends ImageView {
    // Position represents the index of this view in the viewgroups children
    // array
    private int position = 0;
    // The name of the view
    private String mEntry;
    private String mEntryValue;
    private int highlightId;
    private int normalId;

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

    public void setHighlightId(int resid) {
        this.highlightId = resid;
    }

    public int getHighLightId() {
        return highlightId;
    }

    public void setNormalId(int resid) {
        this.normalId = resid;
    }

    public int getNormalId() {
        return normalId;
    }

    public ManualImageView(Context context) {
        this(context, null);
    }

    public ManualImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ManualImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

}

