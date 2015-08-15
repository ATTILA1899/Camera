package com.android.camera.effects.filter;

import com.android.camera.effects.EffectsUtils;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.renderscript.RenderScript;

/** Class of using GlslShader and RenderScript to apply a specific effect
 * on preview frame or photo image data.*/
public final class EffectFilter {
    private static final String TAG = "GLEffectShader";

    private int mType = FilterSource.T_SHADER_NONE;

    // Handle of GLSL shader program.
    private int mProgram = 0;
    // Handle of fragment and vertex shader
    private int mFShader = 0, mVShader = 0;

    /** Bitmap image data filter for this EffectFilter.*/
    private IImageFilter mImageFilter = null;

    /**Interface definition for a bitmap image data filter.*/
    public interface IImageFilter {
        /**To apply the effect on the given bitmap image data.
         * @param image : the given bitmap image data.*/
        public void apply(Bitmap image);
    }

    /** Class of using GlslShader and RenderScript to apply a specific effect
     * on preview frame or photo image data.
     * @param pos: The position of 9-tile preview surface.
     * @param type: The effect type of this filter.
     * @see FilterSource*/
    public EffectFilter(int pos, int type) {
        mType = type;
        //Get FilterSource as singleton mode.
        FilterSource src = FilterSource.getInstance();
        try {
            // Create GLSL shader program with specific
            // vertex shader and fragment shader source.
            EffectsUtils.LogD(TAG, "createProgram:" + FilterSource.getVertexSourceName(pos) + "|" +
                 FilterSource.getFragmentSourceName(type));
            createProgram(src.getVertexSource(pos), src.getFragmentSource(type));
        } catch (Exception e) {
            EffectsUtils.LogE(TAG, "Error creating GLSL shader:" + e.toString());
        }
    }

    private void loadImageFilter() {
        FilterSource src = FilterSource.getInstance();
        RenderScript rs = src.getRenderScript();
        switch (mType) {
        case FilterSource.T_SHADER_ANSEL:
        case FilterSource.T_SHADER_GEORGIA:
        case FilterSource.T_SHADER_NEGATIVE:
        case FilterSource.T_SHADER_RETRO:
        case FilterSource.T_SHADER_SEPIA:
            mImageFilter = new SimpleRSImageFilter(FilterSource.getRSName(mType), rs);
            break;
        case FilterSource.T_SHADER_EDGE:
        case FilterSource.T_SHADER_RELIEF:
            mImageFilter = new NeighborBasedRSImageFilter(FilterSource.getRSName(mType), rs);
            break;
        case FilterSource.T_SHADER_FISHEYE:
            mImageFilter = new FisheyeRSImageFilter(FilterSource.getRSName(mType), rs);
            break;
        case FilterSource.T_SHADER_CHARCOAL:
            mImageFilter = new CharcoalRSImageFilter(FilterSource.getRSName(mType), rs);
            break;
        case FilterSource.T_SHADER_NONE:
            mImageFilter = new IImageFilter() {
                @Override
                public void apply(Bitmap image) {/*Do nothing for none effect*/}
            };
            break;
        }
    }

    /** To delete a GlslShader program.*/
    public void deleteProgram() {
        GLES20.glDeleteShader(mFShader);
        GLES20.glDeleteShader(mVShader);
        GLES20.glDeleteProgram(mProgram);
        mProgram = mFShader = mVShader = 0;
    }

    /** Get location for a given GlslShader uniform name
     * @param name : Name of GlslShader uniform.
     * @return Id for given GlslShader uniform or -1 if not found.*/
    public int getUniformLocation(String name) {
        return GLES20.glGetUniformLocation(mProgram, name);
    }

    /** Get location for a given GlslShader attribute name
     * @param name : Name of GlslShader attribute.
     * @return Id for given GlslShader attribute or -1 if not found.*/
    public int getAttribLocation(String name) {
        return GLES20.glGetAttribLocation(mProgram, name);
    }

    /** Compiles vertex and fragment shader and links them into a program one
     * can use for rendering. Once OpenGL context is lost and onSurfaceCreated
     * is called, there is no need to reset existing GlslShader objects but one
     * can simply reload shader.
     * @param vertexSource : String presentation for vertex shader
     * @param fragmentSource : String presentation for fragment shader
     * @throws Exception */
    public void createProgram(String vertexSource, String fragmentSource) throws Exception {
        mVShader = EffectsUtils.loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        mFShader = EffectsUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, mVShader);
            GLES20.glAttachShader(program, mFShader);
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                String error = GLES20.glGetProgramInfoLog(program);
                deleteProgram();
                throw new Exception(error);
            }
        }
        mProgram = program;
    }

    /** Apply this filter on following preview frame. */
    public void apply() {
        GLES20.glUseProgram(mProgram);
    }

    /** Apply this filter on a given bitmap image.
     * @param image : the bitmap image to apply filter on.*/
    public void apply(Bitmap image) {
        if (mImageFilter == null) loadImageFilter();
        EffectsUtils.TimeStamp.reset();
        mImageFilter.apply(image);
        EffectsUtils.TimeStamp.elapse(FilterSource.getRSName(mType) + " cost");
    }
}
