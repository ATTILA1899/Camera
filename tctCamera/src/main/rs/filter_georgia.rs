#pragma version(1)
#pragma rs java_package_name(com.android.camera.effects.RSFilter)

static const float3 COLOR_MULT = {0.981, 0.862, 0.686};

static float3 brightness(float3 color, float brightness) {
    float scaled = brightness / 2.0;
    if (scaled < 0.0) {
        return color * (1.0f + scaled);
    } else {
        return color + ((1.0f - color) * scaled);
    }
}

static float3 contrast(float3 color, float contrast) {
    const float PI_PER_4 = M_PI / 4.0f;
    return min(1.0f, ((color - 0.5f) * (tan((contrast + 1.0f) * PI_PER_4) ) + 0.5f));
}

void root(uchar4* v_color) {
    float3 color = rsUnpackColor8888(*v_color).rgb;

    color = brightness(color, 0.4724f);
    color = contrast(color, 0.3149f);

    color.g = color.g * 0.87f + 0.13f;
    color.b = color.b * 0.439f + 0.561f;

    color *= COLOR_MULT;

    // Finally store color value back to allocation.
    color = clamp(color, 0.0f, 1.0f);
    *v_color = rsPackColorTo8888(color);
}
