package com.android.camera.effects.filter;

import android.renderscript.Allocation;
import android.renderscript.RenderScript;

/**Render script image filter of only modify the root pixel.*/
public final class SimpleRSImageFilter extends AbstractRSImageFilter {
    private final static int mExportForEachIdx_root = 0;

    protected SimpleRSImageFilter(String name, RenderScript rs) {
        super(name, rs);
    }

    @Override
    protected Allocation process(Allocation src, int width, int height) {
        forEach(mExportForEachIdx_root, (Allocation)null, src, null);
        return src;
    }
}
