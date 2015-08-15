package com.android.camera.effects.filter;

import android.renderscript.Allocation;
import android.renderscript.RenderScript;

/**Render script image filter of fish eye effect.*/
public final class FisheyeRSImageFilter extends AbstractRSImageFilter {
    private final static int mExportVarIdx_pixn = 0;
    private final static int mExportVarIdx_rowc = 1;
    private final static int mExportVarIdx_gDst = 2;
    private final static int mExportVarIdx_gSrc = 3;
    private final static int mExportForEachIdx_root = 0;

    protected FisheyeRSImageFilter(String name, RenderScript rs) {
        super(name, rs);
    }

    @Override
    protected Allocation process(Allocation src, int width, int height) {
        Allocation ids = get1DAlloc(height);
        Allocation dst = get2DAlloc(width, height);
        setVar(mExportVarIdx_pixn, width);
        setVar(mExportVarIdx_rowc, height);
        bindAllocation(dst, mExportVarIdx_gDst);
        bindAllocation(src, mExportVarIdx_gSrc);
        forEach(mExportForEachIdx_root, ids, null, null);
        recycleAlloc(ids);
        return dst;
    }
}
