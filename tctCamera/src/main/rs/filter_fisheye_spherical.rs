#pragma version(1)
#pragma rs java_package_name(com.android.camera.effects.RSFilter)

static const float PI = 3.14159265359;

void apply(const rs_allocation src, rs_allocation dst) {
    int w_2, h_2, i, j, x, y;
    int w = rsAllocationGetDimX(src);
    int h = rsAllocationGetDimY(src);
    w_2 = w / 2;
    h_2 = h / 2;
    float fw_2 = w_2 / 1.7777778, fh_2 = h_2;

    for (i = -w_2; i < w_2; i++) {
        for (j = -h_2; j < h_2; j++) {
            float2 vp = {i / fw_2, j / fh_2};
            float l = length(vp);
            uchar4* colorValue = (uchar4*)rsGetElementAt(dst, i + w_2, j + h_2);
            if (l > 1) {
                *colorValue = rsPackColorTo8888(0, 0, 0, 1.0f);
            } else {
                float ratio = asin(l) / PI / l;
                vp = vp * ratio + 0.5f;
                x = vp.x * w;
                y = vp.y * h;
                *colorValue = *(uchar4*)rsGetElementAt(src, x, y);
            }
        }
    }
}
