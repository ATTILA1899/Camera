#pragma version(1)
#pragma rs java_package_name(com.android.camera.effects.RSFilter)

static const float4 gray_w = {0.299f, 0.587f, 0.114f, 0.0f};

void root(uchar4* v_color) {
    float4 color = rsUnpackColor8888(*v_color);
    float gray = dot(color, gray_w);
    if (gray > 0.5f) {
        gray = 1 - gray;
        color = 1.0f - 2.0f * gray * gray;
    } else {
        color = 2.0f * gray * gray;
    }

    color.a = 1.0f;

    *v_color = rsPackColorTo8888(color);
}
