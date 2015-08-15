package com.android.camera.effects.filter;

public final class GLSLSource {
    public static final class Vertex {
        public static final String LEFT_TOP =
            "uniform vec4 uAnimation;" +
            "uniform float uAspectRatioV;" +
            "attribute vec2 aTexCoord;" +
            "attribute vec2 aPosition;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "    float dy = 0.1 * uAspectRatioV;" +
            "    gl_Position = vec4(aPosition.x - 0.65, aPosition.y + 0.5 + dy, 0.0, 1.0);" +
            "    gl_Position.x = gl_Position.x * uAnimation.x + (uAnimation.y - 0.07) * (uAnimation.x - 1.0);" +
            "    gl_Position.y = gl_Position.y * uAnimation.x + (uAnimation.z + 0.205) * (uAnimation.x - 1.0);" +
            "    vTextureCoord.y = aTexCoord.y;" +
            "    vTextureCoord.x = (aTexCoord.x * uAnimation.w + 1.0) / 2.0;" +
            "}";

        public static final String CENTER_TOP =
            "uniform vec4 uAnimation;" +
            "uniform float uAspectRatioV;" +
            "attribute vec2 aTexCoord;" +
            "attribute vec2 aPosition;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "    float dy = 0.1 * uAspectRatioV;" +
            "    gl_Position = vec4(aPosition.x, aPosition.y + 0.5 + dy, 0.0, 1.0);" +
            "    gl_Position.x = gl_Position.x * uAnimation.x + uAnimation.y * (uAnimation.x - 1.0);" +
            "    gl_Position.y = gl_Position.y * uAnimation.x + (uAnimation.z + 0.205) * (uAnimation.x - 1.0);" +
            "    vTextureCoord.y = aTexCoord.y;" +
            "    vTextureCoord.x = (aTexCoord.x * uAnimation.w + 1.0) / 2.0;" +
            "}";

        public static final String RIGHT_TOP =
            "uniform vec4 uAnimation;" +
            "uniform float uAspectRatioV;" +
            "attribute vec2 aTexCoord;" +
            "attribute vec2 aPosition;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "    float dy = 0.1 * uAspectRatioV;" +
            "    gl_Position = vec4(aPosition.x + 0.65, aPosition.y + 0.5 + dy, 0.0, 1.0);" +
            "    gl_Position.x = gl_Position.x * uAnimation.x + (uAnimation.y + 0.071) * (uAnimation.x - 1.0);" +
            "    gl_Position.y = gl_Position.y * uAnimation.x + (uAnimation.z + 0.205) * (uAnimation.x - 1.0);" +
            "    vTextureCoord.y = aTexCoord.y;" +
            "    vTextureCoord.x = (aTexCoord.x * uAnimation.w + 1.0) / 2.0;" +
            "}";

        public static final String LEFT_CENTER =
            "uniform vec4 uAnimation;" +
            "uniform float uAspectRatioV;" +
            "attribute vec2 aTexCoord;" +
            "attribute vec2 aPosition;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "    float dy = 0.05 * uAspectRatioV;" +
            "    gl_Position = vec4(aPosition.x - 0.65, aPosition.y - 0.1 + dy, 0.0, 1.0);" +
            "    gl_Position.x = gl_Position.x * uAnimation.x + (uAnimation.y - 0.07) * (uAnimation.x - 1.0);" +
            "    gl_Position.y = gl_Position.y * uAnimation.x + (uAnimation.z + 0.102) * (uAnimation.x - 1.0);" +
            "    vTextureCoord.y = aTexCoord.y;" +
            "    vTextureCoord.x = (aTexCoord.x * uAnimation.w + 1.0) / 2.0;" +
            "}";

        public static final String CENTER =
            "uniform vec4 uAnimation;" +
            "uniform float uAspectRatioV;" +
            "attribute vec2 aTexCoord;" +
            "attribute vec2 aPosition;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "    float dy = 0.05 * uAspectRatioV;" +
            "    gl_Position = vec4(aPosition.x, aPosition.y - 0.1 + dy, 0.0, 1.0);" +
            "    gl_Position.x = gl_Position.x * uAnimation.x + uAnimation.y * (uAnimation.x - 1.0);" +
            "    gl_Position.y = gl_Position.y * uAnimation.x + (uAnimation.z + 0.102) * (uAnimation.x - 1.0);" +
            "    vTextureCoord.y = aTexCoord.y;" +
            "    vTextureCoord.x = (aTexCoord.x * uAnimation.w + 1.0) / 2.0;" +
            "}";

        public static final String RIGHT_CENTER =
            "uniform vec4 uAnimation;" +
            "uniform float uAspectRatioV;" +
            "attribute vec2 aTexCoord;" +
            "attribute vec2 aPosition;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "    float dy = 0.05 * uAspectRatioV;" +
            "    gl_Position = vec4(aPosition.x + 0.65, aPosition.y - 0.1 + dy, 0.0, 1.0);" +
            "    gl_Position.x = gl_Position.x * uAnimation.x + (uAnimation.y + 0.071) * (uAnimation.x - 1.0);" +
            "    gl_Position.y = gl_Position.y * uAnimation.x + (uAnimation.z + 0.102) * (uAnimation.x - 1.0);" +
            "    vTextureCoord.y = aTexCoord.y;" +
            "    vTextureCoord.x = (aTexCoord.x * uAnimation.w + 1.0) / 2.0;" +
            "}";

        public static final String LEFT_BOTTOM =
            "uniform vec4 uAnimation;" +
            "uniform float uAspectRatioV;" +
            "attribute vec2 aTexCoord;" +
            "attribute vec2 aPosition;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "    gl_Position = vec4(aPosition.x - 0.65, aPosition.y - 0.7, 0.0, 1.0);" +
            "    gl_Position.x = gl_Position.x * uAnimation.x + (uAnimation.y - 0.07) * (uAnimation.x - 1.0);" +
            "    gl_Position.y = gl_Position.y * uAnimation.x + uAnimation.z * (uAnimation.x - 1.0);" +
            "    vTextureCoord.y = aTexCoord.y;" +
            "    vTextureCoord.x = (aTexCoord.x * uAnimation.w + 1.0) / 2.0;" +
            "}";

        public static final String CENTER_BOTTOM =
            "uniform vec4 uAnimation;" +
            "uniform float uAspectRatioV;" +
            "attribute vec2 aTexCoord;" +
            "attribute vec2 aPosition;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "    gl_Position = vec4(aPosition.x, aPosition.y - 0.7, 0.0, 1.0);" +
            "    gl_Position.x = gl_Position.x * uAnimation.x + uAnimation.y * (uAnimation.x - 1.0);" +
            "    gl_Position.y = gl_Position.y * uAnimation.x + uAnimation.z * (uAnimation.x - 1.0);" +
            "    vTextureCoord.y = aTexCoord.y;" +
            "    vTextureCoord.x = (aTexCoord.x * uAnimation.w + 1.0) / 2.0;" +
            "}";

        public static final String RIGHT_BOTTOM =
            "uniform vec4 uAnimation;" +
            "uniform float uAspectRatioV;" +
            "attribute vec2 aTexCoord;" +
            "attribute vec2 aPosition;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "    gl_Position = vec4(aPosition.x + 0.65, aPosition.y - 0.7, 0.0, 1.0);" +
            "    gl_Position.x = gl_Position.x * uAnimation.x + (uAnimation.y + 0.071) * (uAnimation.x - 1.0);" +
            "    gl_Position.y = gl_Position.y * uAnimation.x + uAnimation.z * (uAnimation.x - 1.0);" +
            "    vTextureCoord.y = aTexCoord.y;" +
            "    vTextureCoord.x = (aTexCoord.x * uAnimation.w + 1.0) / 2.0;" +
            "}";
    }

    public static final class Fragment {
        public static final String NONE =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "uniform samplerExternalOES sTexture;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);" +
            "}";

        public static final String NEGATIVE =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "uniform samplerExternalOES sTexture;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "    gl_FragColor = 1.0 - texture2D(sTexture, vTextureCoord);" +
            "}";

        public static final String RELIEF =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "uniform vec2 uPixelSize;" +
            "uniform samplerExternalOES sTexture;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "    float dx = uPixelSize.x;" +
            "    float dy = uPixelSize.y;" +
            "    vec3 sa1 = texture2D(sTexture, vec2(vTextureCoord.x - dx, vTextureCoord.y + dy)).rgb;" +
            "    vec3 sa2 = texture2D(sTexture, vec2(vTextureCoord.x + dx, vTextureCoord.y - dy)).rgb;" +
            "    float val = dot(sa1, vec3(0.299, 0.587, 0.114)) - dot(sa2, vec3(0.299, 0.587, 0.114));" +
            "    gl_FragColor = vec4(val, val, val, 0.5) + 0.5;" +
            "}";

        public static final String FISH_EYE =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "uniform samplerExternalOES sTexture;" +
            "varying vec2 vTextureCoord;" +
            "const vec2 vc = vec2(0.5, 0.5);" +
            "const float theta = 1.570796327;" + // PI / 2, a = √2/2*/
            "const float R = 0.707106781;" +     // a / sin(theta)
            "const float A = 1.110720734;" +     // theta * R
            "const float a_A = 0.636619773;" +   //a / A
            "void main() {" +
            "    vec2 vcp = vTextureCoord - vc;" +
            "    float l = length(vcp);" +
            "    float L = A * asin(l / R) / theta;" +
            "    if (l < 0.01) L = l = 0.1;" +
            "    vec2 vp = vcp * (a_A * L / l) + vc;" +
            "    gl_FragColor = texture2D(sTexture, vp);" +
            "}";

        public static final String CHARCOAL =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "uniform samplerExternalOES sTexture;" +
            "varying vec2 vTextureCoord;" +
            "uniform sampler2D tex_yellowing;" +
            "uniform sampler2D tex_charcoal;" +
            "void main() {" +
            "    vec4 yellow = texture2D(tex_yellowing, vTextureCoord);" +
            "    vec4 charcoal = texture2D(tex_charcoal, vTextureCoord);" +
            "    vec4 color = texture2D(sTexture, vTextureCoord);" +
            "    float gray = dot(color, vec4(0.299, 0.587, 0.114, 0.0));" +
            "    gray = gray * gray;" +
            "    gl_FragColor = mix(mix(yellow, vec4(gray), 0.6), charcoal, 0.4);" +
            "}";

        public static final String EDGE =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "uniform vec2 uPixelSize;" +
            "uniform samplerExternalOES sTexture;" +
            "varying vec2 vTextureCoord;" +
            "const vec4 gw = vec4(0.299, 0.587, 0.114, 0.0);" +
            "float gray(vec4 color) {" +
            "    return dot(gw, color);" +
            "}" +
            "void main() {" +
            "    float dx = uPixelSize.x;" +
            "    float dy = uPixelSize.y;" +
            "    float v1 = gray(texture2D(sTexture, vec2(vTextureCoord.x - dx, vTextureCoord.y + dy)));" +
            "    float v3 = gray(texture2D(sTexture, vec2(vTextureCoord.x + dx, vTextureCoord.y + dy)));" +
            "    float v7 = gray(texture2D(sTexture, vec2(vTextureCoord.x - dx, vTextureCoord.y - dy)));" +
            "    float v9 = gray(texture2D(sTexture, vec2(vTextureCoord.x + dx, vTextureCoord.y - dy)));" +
            "    float t = v7 - v3;" +
            "    float v = t + v9 - v1;" +
            "    float h = t - v9 + v1;" +
            "    float ret = sqrt(v * v + h * h) / 2.828427125 - 1.0;" +
            "    if (ret > -0.4) ret = 0.0;" +
            "    ret = ret * ret;" +
            "    gl_FragColor = vec4(ret, ret, ret, 1.0);" +
            "}";

        public static final String RETRO =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "uniform samplerExternalOES sTexture;" +
            "varying vec2 vTextureCoord;" +
            "vec3 overlay(vec3 overlayComponent, vec3 underlayComponent, float alpha) {" +
            "    vec3 underlay = underlayComponent * alpha;" +
            "    return underlay * (underlay + (2.0 * overlayComponent * (1.0 - underlay)));" +
            "}" +
            "vec3 multiplyWithAlpha(vec3 overlayComponent, float alpha, vec3 underlayComponent) {" +
            "    return underlayComponent * overlayComponent * alpha;" +
            "}" +
            "vec3 screenPixelComponent(vec3 maskPixelComponent, float alpha, vec3 imagePixelComponent) {" +
            "    return 1.0 - (1.0 - (maskPixelComponent * alpha)) * (1.0 - imagePixelComponent);" +
            "}" +
            "void main() {" +
            "    vec3 color = texture2D(sTexture, vTextureCoord).rgb;" +
            "    float gray = dot(color, vec3(0.299, 0.587, 0.114));" +
            "    color = overlay(vec3(gray), color, 1.0);" +
            "    color = multiplyWithAlpha(vec3(0.984, 0.949, 0.639), 0.588235, color);" +
            "    color = screenPixelComponent(vec3(0.909, 0.396, 0.702), 0.2, color);" +
            "    color = screenPixelComponent(vec3(0.035, 0.286, 0.914), 0.168627, color);" +
            "    gl_FragColor = vec4(color, 1.0);" +
            "}";

        public static final String ANSEL =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "uniform samplerExternalOES sTexture;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "    float g = dot(texture2D(sTexture, vTextureCoord), vec4(0.299, 0.587, 0.114, 0.0));" +
            "    float v;" +
            "    if (g > 0.5) {" +
            "    v = 1.0 - g;" +
            "    v = 1.0 - v * v * 2.0;" +
            "    } else {" +
            "    v = 2.0 * g * g;" +
            "    }" +
            "    gl_FragColor = vec4(v, v, v, 1.0);" +
            "}";

        public static final String GEORGIA =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "uniform samplerExternalOES sTexture;" +
            "varying vec2 vTextureCoord;" +
            "vec3 brightness(vec3 color, float brightness) {" +
            "    float scaled = brightness / 2.0;" +
            "    if (scaled < 0.0) {" +
            "        return color * (1.0 + scaled);" +
            "    } else {" +
            "        return color + ((1.0 - color) * scaled);" +
            "    }" +
            "}" +
            "vec3 contrast(vec3 color, float contrast) {" +
            "    const float PI = 3.14159265;" +
            "    return min(vec3(1.0), ((color - 0.5) * (tan((contrast + 1.0) * PI / 4.0) ) + 0.5));" +
            "}" +
            "void main() {" +
            "    vec3 color = texture2D(sTexture, vTextureCoord).rgb;" +
            "    color = brightness(color, 0.4724);" +
            "    color = contrast(color, 0.3149);" +
            "    color.g = color.g * 0.87 + 0.13;" +
            "    color.b = color.b * 0.439 + 0.561;" +
            "    color *= vec3(0.981, 0.862, 0.686);" +
            "    gl_FragColor = vec4(color, 1.0);" +
            "}";

        public static final String SEPIA =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "uniform samplerExternalOES sTexture;" +
            "varying vec2 vTextureCoord;" +
            "vec3 overlay(vec3 overlayComponent, vec3 underlayComponent, float alpha) {" +
            "    vec3 underlay = underlayComponent * alpha;" +
            "    return underlay * (underlay + (2.0 * overlayComponent * (1.0 - underlay)));" +
            "}" +
            "vec3 brightness(vec3 color, float brightness) {" +
            "    float scaled = brightness / 2.0;" +
            "    if (scaled < 0.0) {" +
            "        return color * (1.0 + scaled);" +
            "    } else {" +
            "        return color + ((1.0 - color) * scaled);" +
            "    }" +
            "}" +
            "void main() {" +
            "vec3 color = texture2D(sTexture, vTextureCoord).rgb;" +
            "float luminosity = dot(color, vec3(0.21, 0.72, 0.07));" +
            "float brightGray = brightness(vec3(luminosity), 0.234375).r;" +
            "vec3 tinted = overlay(vec3(0.419, 0.259, 0.047), vec3(brightGray), 1.0);" +
            "float invertMask = 1.0 - luminosity;" +
            "float luminosity3 = pow(luminosity, 3.0);" +
            "vec3 ret = vec3(luminosity3) + (tinted * invertMask * (luminosity + 1.0));" +
            "gl_FragColor = vec4(ret, 1.0);" +
            "}";
    }
}
