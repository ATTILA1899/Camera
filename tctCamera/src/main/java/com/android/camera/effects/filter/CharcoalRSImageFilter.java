package com.android.camera.effects.filter;

import com.tct.camera.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.FieldPacker;
import android.renderscript.RenderScript;
import android.renderscript.ScriptC;

import com.android.camera.effects.filter.EffectFilter.IImageFilter;

/**Render script image filter of charcoal effect.*/
public final class CharcoalRSImageFilter extends AbstractRSImageFilter {
    private final static int mExportVarIdx_pixn = 0;
    private final static int mExportVarIdx_gSrc = 1;
    private final static int mExportVarIdx_tex_size = 2;
    private final static int mExportVarIdx_ratio_w = 3;
    private final static int mExportVarIdx_ratio_h = 4;
    private final static int mExportVarIdx_gYel = 5;
    private final static int mExportVarIdx_gPen = 6;
    private final static int mExportForEachIdx_root = 0;

    private int mTexSize = 0;
    private Allocation mAllocCharcoal;
    private Allocation mAllocYellowing;

    protected CharcoalRSImageFilter(String name, RenderScript rs) {
        super(name, rs);
        initialize();
    }

    private void initialize() {
        // Setup Bitmap decode Options.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        // Decode texture images from resource.
        Bitmap yellowing = BitmapFactory.decodeResource(
                mRS.getApplicationContext().getResources(), R.drawable.yellowing, options);
        Bitmap charcoal = BitmapFactory.decodeResource(
                mRS.getApplicationContext().getResources(), R.drawable.charcoal, options);

        // create yellowing and charcoal texture Allocations.
        mAllocYellowing = Allocation.createFromBitmap(mRS, yellowing,
                Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        mAllocCharcoal = Allocation.createFromBitmap(mRS, charcoal,
                Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        mTexSize = yellowing.getWidth();
        yellowing.recycle();
        charcoal.recycle();

        // Set texture size for RenderScript.
        setVar(mExportVarIdx_tex_size, mTexSize);

        // Bind yellowing and charcoal textures to RenderScript.
        bindAllocation(mAllocYellowing, mExportVarIdx_gYel);
        bindAllocation(mAllocCharcoal, mExportVarIdx_gPen);
    }

    @Override
    protected Allocation process(Allocation src, int width, int height) {
        // Bind input src Allocation to RenderScript
        bindAllocation(src, mExportVarIdx_gSrc);
        // Set the parameter of pixn in RenderScript as image width.
        setVar(mExportVarIdx_pixn, width);
        // Set the parameter of width and height ratio of texture for RenderScript.
        setVar(mExportVarIdx_ratio_w, mTexSize / (float) width);
        setVar(mExportVarIdx_ratio_h, mTexSize / (float) height);
        Allocation ids = get1DAlloc(height);
        forEach(mExportForEachIdx_root, ids, null, null);
        // Recycle the Allocation.
        recycleAlloc(ids);
        // Return the result Allocation.
        return src;
    }
}
