#pragma version(1)
#pragma rs java_package_name(com.android.camera.effects.RSFilter)

static const float4 gray_w = {0.299f, 0.587f, 0.114f, 0.0f};

int32_t radius;
int32_t pixn;
uchar4 *gDst;
const uchar4 *gSrc;

void root(const int32_t *v_in) {
    int32_t prev = *v_in * pixn, i, rx2 = radius * 2;
    int32_t next = prev + pixn * rx2, base = prev + pixn * radius + radius;
    float sam1, sam3, sam7, sam9;
/*_____________________               _____________________
 *|      |      |      |              |      |      |      |
 *| prev |      |      |<== *v_in     | sam1 | sam2 | sam3 |
 *|______|______|______|              |______|______|______|
 *|      |      |      |              |      |      |      |
 *|      | base |      |              | sam4 | sam5 | sam6 |
 *|______|______|______|              |______|______|______|
 *|      |      |      |              |      |      |      |
 *| next |      |      |              | sam7 | sam8 | sam9 |
 *|______|______|______|              |______|______|______|
 */


    float t, v, h, ret;
    for (i = rx2; i < pixn; i++) {
        sam1 = dot(rsUnpackColor8888(gSrc[prev]), gray_w);
        sam3 = dot(rsUnpackColor8888(gSrc[prev + rx2]), gray_w);
        sam7 = dot(rsUnpackColor8888(gSrc[next]), gray_w);
        sam9 = dot(rsUnpackColor8888(gSrc[next + rx2]), gray_w);
        t = sam7 - sam3;
        v = t + sam9 - sam1;
        h = t - sam9 + sam1;
        ret = sqrt(v * v + h * h) / 2.828427125f - 1.0f;
        if (ret > -0.6f) ret = 0.0f;
        ret = ret * ret;
        gDst[base++] = rsPackColorTo8888(ret, ret, ret, 1.0f);
        prev++;
        next++;
    }
}
