package com.android.camera.effects.filter;

import android.renderscript.Allocation;
import android.renderscript.RenderScript;

/**Render script image filter of the root pixel is based on neighbor pixels.*/
public final class NeighborBasedRSImageFilter extends AbstractRSImageFilter {
    private final static int mExportVarIdx_radius = 0;
    private final static int mExportVarIdx_pixn = 1;
    private final static int mExportVarIdx_gDst = 2;
    private final static int mExportVarIdx_gSrc = 3;
    private final static int mExportForEachIdx_root = 0;

    protected NeighborBasedRSImageFilter(String name, RenderScript rs) {
        super(name, rs);
    }

    @Override
    protected Allocation process(Allocation src, int width, int height) {
        // If we get a large image, we set the core radius as 2.
        int radius = width > 2000 ? 2 : 1;
        setVar(mExportVarIdx_radius, radius);

        Allocation ids = get1DAlloc(height - radius * 2);
        Allocation dst = get2DAlloc(width, height);
        setVar(mExportVarIdx_pixn, width);
        bindAllocation(dst, mExportVarIdx_gDst);
        bindAllocation(src, mExportVarIdx_gSrc);
        forEach(mExportForEachIdx_root, ids, null, null);
        recycleAlloc(ids);
        return dst;
    }
}
