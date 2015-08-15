package com.android.camera.effects.gl;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.android.camera.effects.EffectsUtils;
import com.android.camera.effects.LiveFilterInstanceHolder;
import com.android.camera.effects.camera.Components;
import com.android.camera.effects.camera.Components.IPreviewDataProducer;
import com.android.camera.effects.camera.Components.PauseResumeAble;
import com.android.camera.effects.filter.EffectFilter;
import com.android.camera.effects.filter.FilterSource;
import com.android.camera.effects.gl.GLEffectTile.ZoomListener;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.tct.camera.R;

public final class GLEffectsRender implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener, PauseResumeAble {
    /**Single view with single tile.*/
    public static final int MODE_SINGVIEW = 0;

    /**Over view with 9 tiles.*/
    public static final int MODE_OVERVIEW = 1;

    private static final String TAG = "GLPreviewRender";

    /**The current view mode. Possible values: MODE_SINGVIEW:0 or MODE_OVERVIEW:1. */
    private int mViewMode = MODE_OVERVIEW;

    /**Aspect ratio of preview frame.*/
    private float mAspectRatio;

    /**The ratio of each pixel's width and height and preview frame's width and height.*/
    private float mPixWf, mPixHf;

    /**Bitmaps for GLBitmapTextures.*/
    private Bitmap mBmpYellowing, mBmpCharcoal;

    private boolean mInAnimation = false;

    /**The size of preview frame.*/
    private Point mPreviewFrameSize = new Point();

    /**The flag indicates whether new preview frame is available.*/
    private boolean mFrameAvailable = false;

    /**OES texture to hold preview frame and display.*/
    private GLTextureOES mOESTex = new GLTextureOES();

    /**Live effect preview tiles*/
    private GLEffectTile[] mEffectTiles = new GLEffectTile[9];

    /**Effect textures*/
    private GLBitmapTexture mYellowingTexture, mCharcoalTexture;

    /**SurfaceTexture for camera device to fill preview frame data in.*/
    private SurfaceTexture mSurfaceTexture;

    /**Observer to observe the changes or events.*/
    private EffectsRenderObserver mObserver;

    /**Listener used to dispatch tap events.*/
    private RenderViewTapListener mTapListener;

    /**Preview data producer for producing the preview frames.*/
    private IPreviewDataProducer mPreviewDataProducer;

    /**The target GLSurfaceView*/
    private GLPreviewRootSurfaceView mRootView;

    /**Preview frame transform matrix.*/
    private final float[] mPreviewTransform = new float[16];

    /**Observer to observe the changes or events.*/
    public interface EffectsRenderObserver {
        /**Called at the end of the zoom-in/zoom-out animation.
         * @param mode : current mode (MODE_OVERVIEW or MODE_SINGVIEW)*/
        public void onViewModeChanged(int mode);

        /**Called at when start to change the view mode.
         * @param toMode : The change mode (MODE_OVERVIEW or MODE_SINGVIEW)*/
        public void onStartChangeViewMode(int toMode);

        /** Called when the no-effect tile is clicked.*/
        public void onBackToNormalCamera();
    }

    /**Interface definition for a callback to be invoked when a view is tapped*/
    public interface RenderViewTapListener {
        /** Notified when a tap occurs on the render view
         * with the up MotionEvent that triggered it.
         * @param pos : Coordinate of the tap.*/
        public void onRenderViewShortTap(Point pos);

        /** Notified when a long press occurs on the render view
         * with the initial on down MotionEvent that triggered it.
         * @param pos : Coordinate of the tap.*/
        public void onRenderViewLongTap(Point pos);

        /** Called when one of preview tiles is tapped.
         * @return Return true if this operation is accepted, otherwise false.*/
        public boolean onTileTapped();
    }

    private ZoomListener mZoonInListener = new ZoomListener() {
        @Override
        public void onAnimationDone() {
            mViewMode = MODE_SINGVIEW;
            mInAnimation = false;
            mObserver.onViewModeChanged(mViewMode);
        }
    };

    private ZoomListener mZoonOutListener = new ZoomListener() {
        @Override
        public void onAnimationDone() {
            mViewMode = MODE_OVERVIEW;
            mInAnimation = false;
            mObserver.onViewModeChanged(mViewMode);
        }
    };

    private GLEffectTile.FilterConfigurer mPixSizeConfigurer1 =
            new GLEffectTile.FilterConfigurer() {
        private int mPixSizeLoc;
        @Override
        public void onBindTextures() {}
        @Override
        public void onLocateVariables(EffectFilter filter) {
            mPixSizeLoc = filter.getUniformLocation("uPixelSize");
        }
        @Override
        public void onUpdateVariables() {
            GLES20.glUniform2f(mPixSizeLoc, mPixWf, mPixHf);
        }
    };

    private GLEffectTile.FilterConfigurer mPixSizeConfigurer2 =
            new GLEffectTile.FilterConfigurer() {
        private int mPixSizeLoc;
        @Override
        public void onBindTextures() {}
        @Override
        public void onLocateVariables(EffectFilter filter) {
            mPixSizeLoc = filter.getUniformLocation("uPixelSize");
        }
        @Override
        public void onUpdateVariables() {
            GLES20.glUniform2f(mPixSizeLoc, mPixWf, mPixHf);
        }
    };

    private GLEffectTile.FilterConfigurer mCharcoalConfigurer =
            new GLEffectTile.FilterConfigurer() {
        private int mTexLoc_yellowing, mTexLoc_charcoal;
        @Override
        public void onBindTextures() {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mCharcoalTexture.getTexName());
            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYellowingTexture.getTexName());
        }
        @Override
        public void onLocateVariables(EffectFilter filter) {
            mTexLoc_yellowing = filter.getUniformLocation("tex_yellowing");
            mTexLoc_charcoal = filter.getUniformLocation("tex_charcoal");
        }
        @Override
        public void onUpdateVariables() {
            GLES20.glUniform1i(mTexLoc_charcoal, 1);
            GLES20.glUniform1i(mTexLoc_yellowing, 2);
        }
    };

    /** Register a callback to be invoked when the state of render is changed.*/
    public void setObserver(EffectsRenderObserver observer) {
        mObserver = observer;
        mViewMode = MODE_OVERVIEW;
        GLEffectTile.reset();
    }

    /** Register a callback to be invoked when GLSurfaceView is tapped.*/
    public void setTapListener(RenderViewTapListener listener) {
        mTapListener = listener;
    }

    /** Set the preview data producer to this preview frame render.*/
    public void setPreviewDataProducer(IPreviewDataProducer producer) {
        if (mPreviewDataProducer != null) mPreviewDataProducer.onLosePreviewTexture();
        mPreviewDataProducer = producer;
    }

    /** Set the target GLSurfaceView for this GLRender.*/
    public void setRootView(GLPreviewRootSurfaceView root) {
        mRootView = root;
    }

    public void prepare() {
        GLEffectTile.prepare(mZoonInListener, mZoonOutListener);
        loadBitmapRes();
    }

    private void loadBitmapRes() {
        Resources res = LiveFilterInstanceHolder.getIntance().getContext().getResources();
        mBmpYellowing = BitmapFactory.decodeResource(res, R.drawable.yellowing);
        mBmpCharcoal = BitmapFactory.decodeResource(res, R.drawable.charcoal);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mFrameAvailable = true;
        mRootView.requestRender();
    }

    @Override
    public void onDrawFrame(GL10 useless) {
        GLES20.glClearColor(0, 0, 0, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        if (mFrameAvailable) {
            mFrameAvailable = false;
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mPreviewTransform);
            GLEffectTile.setOrientation(mPreviewTransform[12] == 1);
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOESTex.getTexName());
        mEffectTiles[0].draw();
        mEffectTiles[1].draw();
        mEffectTiles[2].draw();
        mEffectTiles[3].draw();
        mEffectTiles[4].draw();
        mEffectTiles[5].draw();
        mEffectTiles[6].draw();
        mEffectTiles[7].draw();
        mEffectTiles[8].draw();
        GLEffectTile.updateZoomState();
        if (mInAnimation) mRootView.requestRender();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mPreviewFrameSize.x = width;
        mPreviewFrameSize.y = height;
        mPixWf = 1 / (float)width;
        mPixHf = 1 / (float)height;
        mAspectRatio = GLEffectTile.setTileSize(width, height);
        if (mSurfaceTexture == null) {
            mSurfaceTexture = new SurfaceTexture(mOESTex.getTexName());
            mSurfaceTexture.setOnFrameAvailableListener(this);
            mPreviewDataProducer.onPreviewTextureAvalible(mSurfaceTexture);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLAbstractTexture.reset();
        mOESTex.attachToGL();
        SurfaceTexture tempST = new SurfaceTexture(mOESTex.getTexName());
        tempST.setOnFrameAvailableListener(this);
        mPreviewDataProducer.onPreviewTextureAvalible(tempST);
        prepareTiles();
        mYellowingTexture = new GLBitmapTexture(mBmpYellowing);
        mCharcoalTexture = new GLBitmapTexture(mBmpCharcoal);
        mCharcoalTexture.attachToGL();
        mYellowingTexture.attachToGL();
        mEffectTiles[0].attachToGL();
        mEffectTiles[1].attachToGL();
        mEffectTiles[2].attachToGL();
        mEffectTiles[3].attachToGL();
        mEffectTiles[4].attachToGL();
        mEffectTiles[5].attachToGL();
        mEffectTiles[6].attachToGL();
        mEffectTiles[7].attachToGL();
        mEffectTiles[8].attachToGL();
        synchronized(GLEffectsRender.class){//PR834296-sichao.hu added, in case this happening during updateTexture
	        if (mSurfaceTexture != null) {
	            mSurfaceTexture.setOnFrameAvailableListener(null);
	            mSurfaceTexture.release();
	        }
	        mSurfaceTexture = tempST;
        }
    }

    /** Get id of top tile effect.
     * @return effect id.*/
    public int getCurrentEffect() {
        return GLEffectTile.getTopTileId();
    }

    /** Apply filter with specific window position to a bitmap image data.
     * @param effect : The effect that will be implemented.
     * @param image : The bitmap image data to apply effect on.*/
    public void applyFilter(int effect, Bitmap image) {
        mEffectTiles[effect].getFilter().apply(image);
    }

    private void prepareTiles() {
        mEffectTiles[0] = new GLEffectTile(null,
                new EffectFilter(FilterSource.P_LT, FilterSource.T_SHADER_SEPIA));
        mEffectTiles[1] = new GLEffectTile(null,
                new EffectFilter(FilterSource.P_CT, FilterSource.T_SHADER_ANSEL));
        mEffectTiles[2] = new GLEffectTile(null,
                new EffectFilter(FilterSource.P_RT, FilterSource.T_SHADER_RETRO));
        mEffectTiles[3] = new GLEffectTile(null,
                new EffectFilter(FilterSource.P_LC, FilterSource.T_SHADER_FISHEYE));
        mEffectTiles[4] = new GLEffectTile(null,
                new EffectFilter(FilterSource.P_CC, FilterSource.T_SHADER_NONE));
        mEffectTiles[5] = new GLEffectTile(null,
                new EffectFilter(FilterSource.P_RC, FilterSource.T_SHADER_NEGATIVE));
        mEffectTiles[6] = new GLEffectTile(mPixSizeConfigurer1,
                new EffectFilter(FilterSource.P_LB, FilterSource.T_SHADER_RELIEF));
        mEffectTiles[7] = new GLEffectTile(mCharcoalConfigurer,
                new EffectFilter(FilterSource.P_CB, FilterSource.T_SHADER_CHARCOAL));
        mEffectTiles[8] = new GLEffectTile(mPixSizeConfigurer2,
                new EffectFilter(FilterSource.P_RB, FilterSource.T_SHADER_EDGE));
    }

    public void switchToOverview() {
        if (mViewMode == MODE_SINGVIEW) {
            mObserver.onStartChangeViewMode(MODE_OVERVIEW);
            mInAnimation = GLEffectTile.startZoomOutAnimation();
        }
    }

    public void onSingleTap(Point pos) {
        if (mViewMode == MODE_OVERVIEW) {
            if (GLEffectTile.getTileAtPos(pos) == 4) {
                mObserver.onBackToNormalCamera();
                return;
            }
            if (!mTapListener.onTileTapped()) return;
            mObserver.onStartChangeViewMode(MODE_SINGVIEW);
            mInAnimation = GLEffectTile.startZoomInAnimation(pos);
        } else {
            mTapListener.onRenderViewShortTap(pos);
        }
    }

    public void onLongPress(Point pos) {
        if (mInAnimation) return;
        if (mViewMode == MODE_SINGVIEW) mTapListener.onRenderViewLongTap(pos);
    }

    @Override
    public void onPause() {
        mRootView.onPause();
        if (mSurfaceTexture != null) {
        	synchronized(GLEffectsRender.class){//PR834296-sichao.hu added, in case this happening during updateTexture
	            mPreviewDataProducer.onLosePreviewTexture();
	            mSurfaceTexture.release();
	            mSurfaceTexture = null;
        	}
        }
    }

    @Override
    public void onResume() {
        mRootView.onResume();
    }
}
