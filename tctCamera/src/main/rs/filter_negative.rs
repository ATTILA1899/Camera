#pragma version(1)
#pragma rs java_package_name(com.android.camera.effects.RSFilter)

void root(uchar4* v_color) {
    float4 color = 1.0f - rsUnpackColor8888(*v_color);
    color.a = 1.0f;
    *v_color = rsPackColorTo8888(color);
}
