package com.android.camera.effects.filter;

import java.util.HashSet;

import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptC;
import android.renderscript.Type;

import com.android.camera.effects.filter.EffectFilter.IImageFilter;

/** Class definition for abstract RenderScript image filter.*/
public abstract class AbstractRSImageFilter extends ScriptC implements IImageFilter {
    /** The Recycled 1D Allocations.*/
    private static HashSet<Allocation> mTempAlloc1D = new HashSet<Allocation>();

    /** The Recycled 2D Allocations.*/
    private static HashSet<Allocation> mTempAlloc2D = new HashSet<Allocation>();

    /** RenderScript for this RenderScript filter.*/
    protected RenderScript mRS;

    protected AbstractRSImageFilter(String name, RenderScript rs) {
        super(rs,
                rs.getApplicationContext().getResources(),
                rs.getApplicationContext().getResources().getIdentifier(
                        name, "raw", rs.getApplicationContext().getPackageName()));
        mRS = rs;
    }

    /** Get 1D Allocation.
     * @param len : length of the 1D Allocation.
     * @return The 1D Allocation with given length.*/
    protected Allocation get1DAlloc(int len) {
        // Try to get a length-matched Allocation from template 1D Allocation list.
        synchronized (mTempAlloc1D) {
            for (Allocation alloc : mTempAlloc1D) {
                Type type = alloc.getType();
                if (type.getX() == len) {
                    mTempAlloc1D.remove(alloc);
                    return alloc;
                }
            }
        }

        // If no Allocation in the template list matches the given length,
        // just generate a new one.
        Allocation alloc = Allocation
                .createSized(mRS, Element.I32(mRS), len, Allocation.USAGE_SCRIPT);
        int[] ids = new int[len];
        for (int i = 0; i < len; i++) ids[i] = i;
        alloc.copyFrom(ids);
        return alloc;
    }

    /** Get 2D Allocation.
     * @param width : width of the 2D Allocation.
     * @param height : height of the 2D Allocation.
     * @return The 2D Allocation with given size.*/
    protected Allocation get2DAlloc(int width, int height) {
        // Try to get a size-matched Allocation from template 2D Allocation list.
        synchronized (mTempAlloc2D) {
            for (Allocation alloc : mTempAlloc2D) {
                Type type = alloc.getType();
                if (type.getX() == width && type.getY() == height) {
                    mTempAlloc2D.remove(alloc);
                    return alloc;
                }
            }
        }
        // If no Allocation in the template list matches the given size,
        // just generate a new one.
        Type.Builder builder = new Type.Builder(mRS, Element.RGBA_8888(mRS));
        builder.setX(width).setY(height);
        Allocation alloc =
                Allocation.createTyped(mRS, builder.create(), Allocation.USAGE_SCRIPT);
        return alloc;
    }

    /** Recycle the Allocation.
     * @param alloc : The Allocation to be recycled.*/
    protected void recycleAlloc(Allocation alloc) {
        if (alloc.getType().getY() == 0) {
            synchronized (mTempAlloc1D) {mTempAlloc1D.add(alloc);}
        } else {
            synchronized (mTempAlloc2D) {mTempAlloc2D.add(alloc);}
        }
    }

    @Override
    public void apply(Bitmap image) {
        // Get a 2D Allocation with specific width and height.
        Allocation src = get2DAlloc(image.getWidth(), image.getHeight());
        // Copy bitmap image to the src Allocation
        src.copyFrom(image);
        // Process the src Allocation of the input image
        Allocation ret = process(src, image.getWidth(), image.getHeight());
        // Copy result Allocation into input image.
        ret.copyTo(image);
        // recycle Allocations
        recycleAlloc(src);
        recycleAlloc(ret);
    }

    /** The subclass must implement this function to
     * process the Allocation of input image.
     * @param src : The Allocation of the input image.
     * @param width : The width of the input image.
     * @param height : The height of the input image.
     * @return The Allocation of the processed image*/
    protected abstract Allocation process(Allocation src, int width, int height);
}
