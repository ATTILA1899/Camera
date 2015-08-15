#pragma version(1)
#pragma rs java_package_name(com.android.camera.effects.RSFilter)

static const float2 vc = {0.5f, 0.5f};
static const float theta = 1.570796327f;
static const float R = 0.707106781f;
static const float A = 1.110720734f;
static const float a_A = 0.636619773f;

int32_t pixn;
int32_t rowc;
uchar4 *gDst;
const uchar4 *gSrc;

void root(const int32_t *v_in) {
    int32_t i, j = *v_in - rowc / 2, w_2 = pixn / 2, x, y, base = *v_in * pixn;
    float fw = pixn, fh = rowc, l, L;
    float2 vp;
    for (i = -w_2; i < w_2; i++) {
        vp.x = i / fw;
        vp.y = j / fh;
        l = length(vp);
        L = A * asin(l / R) / theta;
        vp = vp * (a_A * L / l) + vc;
        x = pixn * vp.x;
        y = rowc * vp.y;
        gDst[base + i + w_2] = gSrc[y * pixn + x];
    }
}
