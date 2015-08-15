package com.android.camera.effects.filter;

import android.content.Context;
import android.renderscript.RenderScript;

/**Class of manage GlslShader source.*/
public final class FilterSource {
    public static final int T_SHADER_NONE = 0;
    /**Effect Type of film negative. Configurer: null. */
    public static final int T_SHADER_NEGATIVE = 1;
    /**Effect Type of relief painting. Configurer: PixSizeConfigurer. */
    public static final int T_SHADER_RELIEF = 2;
    /**Effect Type of fish eye. Configurer: null */
    public static final int T_SHADER_FISHEYE = 3;
    /**Effect Type of charcoal painting. Configurer:CharcoalConfigurer. */
    public static final int T_SHADER_CHARCOAL = 4;
    /**Effect Type of edge. Configurer: PixSizeConfigurer. */
    public static final int T_SHADER_EDGE = 5;
    /**Effect Type of retro. Configurer: null. */
    public static final int T_SHADER_RETRO = 6;
    /**Effect Type of ansel. Configurer: null. */
    public static final int T_SHADER_ANSEL = 7;
    /**Effect Type of georgia. Configurer: null. */
    public static final int T_SHADER_GEORGIA = 8;
    /**Effect Type of sepia. Configurer: null. */
    public static final int T_SHADER_SEPIA = 9;

    /**left top*/
    public static final int P_LT = 0;
    /**center top*/
    public static final int P_CT = 1;
    /**right top*/
    public static final int P_RT = 2;
    /**left center*/
    public static final int P_LC = 3;
    /**center center*/
    public static final int P_CC = 4;
    /**right center*/
    public static final int P_RC = 5;
    /**left bottom*/
    public static final int P_LB = 6;
    /**center bottom*/
    public static final int P_CB = 7;
    /**right bottom*/
    public static final int P_RB = 8;

    private static final String[] VERTEXPROGS = {
        GLSLSource.Vertex.LEFT_TOP,GLSLSource.Vertex.CENTER_TOP, GLSLSource.Vertex.RIGHT_TOP,
        GLSLSource.Vertex.LEFT_CENTER, GLSLSource.Vertex.CENTER, GLSLSource.Vertex.RIGHT_CENTER,
        GLSLSource.Vertex.LEFT_BOTTOM, GLSLSource.Vertex.CENTER_BOTTOM, GLSLSource.Vertex.RIGHT_BOTTOM};

    private static final String[] FRAGMENTPROGS = {
        GLSLSource.Fragment.NONE, GLSLSource.Fragment.NEGATIVE, GLSLSource.Fragment.RELIEF,
        GLSLSource.Fragment.FISH_EYE, GLSLSource.Fragment.CHARCOAL, GLSLSource.Fragment.EDGE,
        GLSLSource.Fragment.RETRO, GLSLSource.Fragment.ANSEL, GLSLSource.Fragment.GEORGIA,
        GLSLSource.Fragment.SEPIA};

    /**Array of GlslShader vertex source name.*/
    private static final String[] VS_NAMES = {
        "vs_lt",
        "vs_ct",
        "vs_rt",
        "vs_lc",
        "vs_cc",
        "vs_rc",
        "vs_lb",
        "vs_cb",
        "vs_rb"
    };

    /**Array of GlslShader fragment source name.*/
    private static final String[] FS_NAMES = {
        "fs_none",
        "fs_negative",
        "fs_relief",
        "fs_fisheye",
        "fs_charcoal",
        "fs_edge",
        "fs_retro",
        "fs_ansel",
        "fs_georgia",
        "fs_sepia"
    };

    /**Array of renderscript source name.*/
    private static final String[] RS_FILE_NAMES = {
        "filter_none",
        "filter_negative",
        "filter_relief",
        "filter_fisheye",
        "filter_charcoal",
        "filter_edge",
        "filter_retro",
        "filter_ansel",
        "filter_georgia",
        "filter_sepia"
    };

    private Context mContext;
    private RenderScript mRenderScript;

    public static String getRSName(int filter) {return RS_FILE_NAMES[filter];}

    public static String getVertexSourceName(int position) {return VS_NAMES[position];}

    public static String getFragmentSourceName(int type) {return FS_NAMES[type];}

    //Singleton mode
    private static FilterSource mInstance = null;
    public static FilterSource createInstance(Context context) {
        if (mInstance == null) mInstance = new FilterSource(context);
        return mInstance;
    }

    public static FilterSource getInstance() {
        return mInstance;
    }

    /**Class of manage GlslShader source.*/
    private FilterSource(Context context) {
        mContext = context;
    }

    public void prepare() {
        // Create RenderScript for photo image processing.
        mRenderScript = RenderScript.create(mContext);
    }

    public Context getContext() {return mContext;}

    /**Get RenderScipt instance.*/
    public RenderScript getRenderScript() {return mRenderScript;}

    /** To get the vertex source specified by position.
     * @param position : The position of the tile.
     * @return The GlslShader vertex program source.*/
    public String getVertexSource(int position) {
        return VERTEXPROGS[position];
    }

    /** To get the fragment source specified by effect type.
     * @param type : Type of the effect.
     * @return The GlslShader fragment program source.*/
    public String getFragmentSource(int type) {
        return FRAGMENTPROGS[type];
    }
}
