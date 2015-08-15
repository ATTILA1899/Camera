#pragma version(1)
#pragma rs java_package_name(com.android.camera.effects.RSFilter)

static const float4 gray_w = {0.299f, 0.587f, 0.114f, 0.0f};

int32_t pixn;
uchar4 *gSrc;

int32_t tex_size;
float ratio_w;
float ratio_h;
const uchar4 *gYel;
const uchar4 *gPen;

void root(const int32_t *v_in) {
    int32_t row = *v_in, i, im_offset, tex_offset;
    float gray;
    float4 yell, penc, color;

    int imbase = row * pixn;
    int sam_row = row * ratio_h;
    int texbase = sam_row * tex_size;

    for (i = 0; i < pixn; i++) {
        im_offset = imbase + i;
        tex_offset = texbase + i * ratio_w;
        gray = dot(rsUnpackColor8888(gSrc[im_offset]), gray_w);
        yell = rsUnpackColor8888(gYel[tex_offset]);
        penc = rsUnpackColor8888(gPen[tex_offset]);
        color = gray;
        color = mix(yell, color, 0.5f);
        color = mix(color, penc, 1 - gray);
        gSrc[im_offset] = rsPackColorTo8888(color);
    }
}
