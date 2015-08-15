package com.android.camera.effects.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.android.camera.effects.filter.EffectFilter;

import android.graphics.Point;
import android.opengl.GLES20;

/**Class of tile for displaying preview effect.*/
public final class GLEffectTile implements IGLStuff {
    /** Texture coordinates of camera preview frame: (0, 1) (1, 1) (0, 0) (1, 0) */
    private static final byte TEX_COORDS[] = {-1, 1, 1, 1, -1, 0, 1, 0};

    /** Duration of in/out animation.*/
    private static final float ANI_DURATION = 300.0f;

    /**Transform Matrix for the zoom-in/zoom-out animation of tiles.*/
    private static final float[] mAnimation = {1.0f, 1f, -1f, -1f};

    /**Points coordinates of tile : (-1/3, 1/3) (-1/3, -1/3) (1/3, 1/3) (1/3, -1/3) */
    private static final float[] TILE_COORDS = {-0.3f, 0.3f, -0.3f, -0.3f, 0.3f, 0.3f, 0.3f, -0.3f};

    /** Width and height of each tiles.*/
    private static int mTileW, mTileH;

    /** The index of top tile.*/
    private static int mTopTileIndex = 0;

    /** Launch time of the in/out animation.*/
    private static long mAniLaunchTime = -1;

    /** Zoom step of zoom in/out animation.*/
    private static float mZoomStep = 2.3333333333f;

    /** Aspect ratio of surface view.*/
    private static float mAspectRatio;

    /** Base scale value of tiles.*/
    private static float mZoomBaseValue = 1;

    /** Indicates whether the animation have done.*/
    private static boolean mInAnimation = false;

    /** FloatBuffer of point coordinate.*/
    private static FloatBuffer mPositionBuff = null;

    /** ByteBuffs of texture coordinate.*/
    private static ByteBuffer mTexCoordBuf = null;

    /** Zoom-in&Zoom-out listener for zoom animation.*/
    private static ZoomListener mZoonInListener, mZoomOutListener;

    /** Locations a attribute or uniform variables in GLSL shader.*/
    private int mAPositionLoc, mATexCoordLoc, mUAnimationLoc, mUAspectRatioVLoc;

    /** Filter for this tile.*/
    private EffectFilter mFilter;

    /** Effect configurer for this tile.*/
    private FilterConfigurer mConfigurer;

    /**Interface definition for a preview frame GLSL shader.*/
    protected interface FilterConfigurer {
        /** Called when the shader should bind its textures.*/
        public void onBindTextures();

        /** Called when the shader should locate its variables.*/
        public void onLocateVariables(EffectFilter filter);

        /** Called when the shader should update its variables.*/
        public void onUpdateVariables();
    }

    /** The listener that is used to notify when zoom animation is done.*/
    protected interface ZoomListener {
        /**Called when the zoom animation is done.*/
        public void onAnimationDone();
    }

    /** Reset scale animation.*/
    protected static void reset() {
        mInAnimation = false;
        mAnimation[0] = 1;
    }

    /** To get the index of top tile*/
    protected static int getTopTileId() {return mTopTileIndex;}

    /**Set preview orientation for front or back camera.
     * @param front : Whether it is the front camera.*/
    protected static void setOrientation(boolean front) {mAnimation[3] = front ? -1 : 1;}

    /**Set the size of each tiles.
     * @param width : width of each tiles
     * @param height : height of each tiles
     * @return aspect ratio of each tiles.*/
    protected static float setTileSize(int width, int height) {
        mAspectRatio = width / (float) height;
        mTileW = width / 3;
        mTileH = height / 3;
        return mAspectRatio;
    }

    protected static void updateZoomState() {
        if (!mInAnimation) return;
        long current = System.currentTimeMillis();
        mAnimation[0] = mZoomBaseValue + mZoomStep * (current - mAniLaunchTime) / ANI_DURATION;

        if (mZoomStep > 0 && mAnimation[0] > 3.333333333f) {
            mAnimation[0] = 3.333333333f;
            mInAnimation = false;
            mZoonInListener.onAnimationDone();
        } else if (mZoomStep < 0 && mAnimation[0] < 1) {
            mAnimation[0] = 1;
            mInAnimation = false;
            mZoomOutListener.onAnimationDone();
        }
    }

    /** Get the tile index at the screen position.
     * @param pos : screen position.
     * @return The tile index at the screen position.*/
    protected static int getTileAtPos(Point pos) {
        int x = pos.x / mTileW;
        int y = pos.y / mTileH;
        return y * 3 + x;
    }

    /** To start zoom-in animation base at the specific tap position.
     * @param pos : The tap position.*/
    protected static boolean startZoomInAnimation(Point pos) {
        if (mInAnimation || mTileW == 0) return false;
        mAniLaunchTime = System.currentTimeMillis();
        mZoomBaseValue = 1;
        mZoomStep = 2.3333333333f;
        int x = pos.x / mTileW;
        int y = pos.y / mTileH;
        mInAnimation = true;
        mAnimation[1] = 1 - x;
        mAnimation[2] = y - 1;
        mTopTileIndex = y * 3 + x;
        return true;
    }

    /** To start zoom-out animation.*/
    protected static boolean startZoomOutAnimation() {
        if (mInAnimation) return false;
        mAniLaunchTime = System.currentTimeMillis();
        mZoomBaseValue = 3.33333333333f;
        mZoomStep = -2.3333333333f;
        mInAnimation = true;
        return true;
    }

    /** To prepare data for all tiles.
     * @param in : The zoom-in listener.
     * @param out : The zoom-out listener.*/
    protected static void prepare(ZoomListener in, ZoomListener out) {
        mZoonInListener = in;
        mZoomOutListener = out;
        if (mPositionBuff == null) {
            // float has 4 bytes
            ByteBuffer bbuff = ByteBuffer.allocateDirect(32); // 4 * 2 * 4
            bbuff.order(ByteOrder.nativeOrder());
            mPositionBuff = bbuff.asFloatBuffer();
            mPositionBuff.put(TILE_COORDS).position(0);
        }
        if (mTexCoordBuf == null) {
            mTexCoordBuf = ByteBuffer.allocateDirect(8); // 4 * 2
            mTexCoordBuf.put(TEX_COORDS).position(0);
        }
    }

    public GLEffectTile(FilterConfigurer configurer, EffectFilter filter) {
        mFilter = filter;
        mConfigurer = configurer;
    }

    /** To get the effect filter of this tile.*/
    protected EffectFilter getFilter() {return mFilter;}

    /** Render this tile on the GLSurfaceView in onDrawFrame callback.*/
    protected void draw() {
        // Bind the texture of this effect needs.
        if (mConfigurer != null) mConfigurer.onBindTextures();
        // Apply the filter to the following operating.
        mFilter.apply();
        // Update transform vector for tiles animation
        GLES20.glUniform4f(mUAnimationLoc,
                mAnimation[0], mAnimation[1], mAnimation[2], mAnimation[3]);
        GLES20.glUniform1f(mUAspectRatioVLoc, mAspectRatio);
        // Update variables of the filter.
        if (mConfigurer != null) mConfigurer.onUpdateVariables();
        // Draw texture rectangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    public void attachToGL() {
        mUAspectRatioVLoc = mFilter.getUniformLocation("uAspectRatioV");
        mATexCoordLoc = mFilter.getAttribLocation("aTexCoord");
        mAPositionLoc = mFilter.getAttribLocation("aPosition");
        mUAnimationLoc = mFilter.getUniformLocation("uAnimation");
        GLES20.glVertexAttribPointer(mAPositionLoc, 2, GLES20.GL_FLOAT, false, 0, mPositionBuff);
        GLES20.glEnableVertexAttribArray(mAPositionLoc);
        GLES20.glVertexAttribPointer(mATexCoordLoc, 2, GLES20.GL_BYTE, false, 0, mTexCoordBuf);
        GLES20.glEnableVertexAttribArray(mATexCoordLoc);
        if (mConfigurer != null) mConfigurer.onLocateVariables(mFilter);
    }

    @Override
    public void detachFromGL() {
        GLES20.glDisableVertexAttribArray(mAPositionLoc);
        GLES20.glDisableVertexAttribArray(mATexCoordLoc);
    }
}
