/******************************************************************************/
/*                                                               Date:09/2013 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  (hao.wang)                                                      */
/*  Email  :  hao.wang@tcl-mobile.com                                         */
/*  Role   :                                                                  */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments :                                                                */
/*  File     : tct_src/com/android/camera/manager/TctPanoramaProcessManager.- */
/*             java                                                           */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
package com.android.camera.manager;

import com.android.camera.CameraActivity;

import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

public class TctParameter {
    private static TctParameter INSTANCE = new TctParameter();
    private static DisplayMetrics sDisplayMetrics;
    private static final int DEFAULT_DISPLAY_WIDTH_PX=720;
    private static final int DEFAULT_DISPLAY_HEIGHT_PX=1280;
    private static Point mPoint;

    public TctParameter getInstance() {
        return INSTANCE;
    }

    private TctParameter() {
        sDisplayMetrics = new DisplayMetrics();
    }

    /**************************/
    public static int getDisplayWidth() {
        Log.d("TCTWH","getDisplayWidth:"+sDisplayMetrics.widthPixels);
        if (mPoint==null) {
            return DEFAULT_DISPLAY_WIDTH_PX;
        }
        return mPoint.x;
    }

    public static int getDisplayHeight() {
        Log.d("TCTWH","getDisplayHeight:"+sDisplayMetrics.heightPixels);
        //return sDisplayMetrics.heightPixels;
        if (mPoint==null) {
            return DEFAULT_DISPLAY_HEIGHT_PX;
        }
        return mPoint.y;
    }

    public static void init(CameraActivity activity, int direction) {
        //modify by minghui.hua for PR912018
        Display display =  activity.getWindowManager().getDefaultDisplay();
        mPoint = new Point();
        display.getRealSize(mPoint);
        display.getMetrics(sDisplayMetrics);

        Config.ARROWL_HEIGHT = (int) (Config.ARROW_HEIGHT_DP * sDisplayMetrics.scaledDensity);
        Config.ARROWL_WIDTH = (int) (Config.ARROW_WIDTH_DP * sDisplayMetrics.scaledDensity);
        Config.ARROWR_HEIGHT = Config.ARROWL_HEIGHT;
        Config.ARROWR_WIDTH = Config.ARROWL_WIDTH;
        if (direction==TctPanoramaProcessManager.DIRECTION_LEFT
                ||direction==TctPanoramaProcessManager.DIRECTION_RIGHT) {// Vertical

            Config.ARROWL_LEFT = (int) (Vertical.ARROWL_LEFT_DP * sDisplayMetrics.scaledDensity);
            Config.ARROWL_TOP = (int) (getDisplayHeight() / 2 - Config.ARROWL_HEIGHT / 2);

            Config.ARROWR_LEFT = (int) (getDisplayWidth() - Config.ARROWR_WIDTH - Config.ARROWL_LEFT);
            Config.ARROWR_TOP = (int) (getDisplayHeight() / 2 - Config.ARROWR_HEIGHT / 2);

            Config.STATIC_RECT_LEFT = (int) (Vertical.STATIC_RECT_LEFT_DP * sDisplayMetrics.scaledDensity);
            Config.STATIC_RECT_RIGHT = (int) (Vertical.STATIC_RECT_RIGHT_DP * sDisplayMetrics.scaledDensity);
            Config.STATIC_RECT_HEIGHT = (int) (Config.STATIC_RECT_SECOND_DP * sDisplayMetrics.scaledDensity);
            Config.STATIC_RECT_TOP = (int) ((getDisplayHeight() - Config.STATIC_RECT_HEIGHT) / 2);
            Config.STATIC_RECT_WIDTH = (int) (getDisplayWidth() - Config.STATIC_RECT_LEFT -Config.STATIC_RECT_RIGHT);

            Config.VIEWFINDER_LEFT = (int) (Vertical.VIEWFINDER_LEFT_DP * sDisplayMetrics.scaledDensity);
            Config.VIEWFINDER_HEIGHT = (int) (Config.VIEWFINDER_SECOND_DP * sDisplayMetrics.scaledDensity);
            Config.VIEWFINDER_TOP = (int) ((getDisplayHeight() - Config.VIEWFINDER_HEIGHT) / 2);
            Config.VIEWFINDER_WIDTH = (int) (getDisplayWidth() - Config.VIEWFINDER_LEFT * 2);

            Config.ARROWL_ROTATE=0;
            Config.ARROWR_ROTATE=0;

        } else {// horizonal
            Config.ARROWL_LEFT = (int) (getDisplayWidth() /2  - Config.ARROWL_WIDTH / 2);
            Config.ARROWL_TOP = (int) (Horizontal.ARROWL_TOP_DP * sDisplayMetrics.scaledDensity - Horizontal.STATIC_RECT_WID_DEVIATION);

            Config.ARROWR_LEFT = (int) (getDisplayWidth() /2  - Config.ARROWL_WIDTH / 2);
            Config.ARROWR_TOP = (int) (getDisplayHeight() - Config.ARROWR_HEIGHT - Config.ARROWL_TOP  -Horizontal.STATIC_RECT_WID_DEVIATION);



            Config.STATIC_RECT_TOP = (int) (Horizontal.STATIC_RECT_TOP_DP * sDisplayMetrics.scaledDensity);
            Config.STATIC_RECT_WIDTH = (int) (Config.STATIC_RECT_SECOND_DP * sDisplayMetrics.scaledDensity);
            Config.STATIC_RECT_HEIGHT = (int) ( getDisplayHeight()-Horizontal.STATIC_RECT_WID_DEVIATION - Config.STATIC_RECT_TOP * 2);
            Config.STATIC_RECT_LEFT = (int) ((getDisplayWidth() - Config.STATIC_RECT_WIDTH) / 2);

            Config.VIEWFINDER_TOP = (int) (Horizontal.VIEWFINDER_TOP_DP * sDisplayMetrics.scaledDensity);
            Config.VIEWFINDER_WIDTH = (int) (Config.VIEWFINDER_SECOND_DP * sDisplayMetrics.scaledDensity);
            Config.VIEWFINDER_HEIGHT = (int) (getDisplayHeight() -Horizontal.STATIC_RECT_WID_DEVIATION- Config.VIEWFINDER_TOP * 2);
            Config.VIEWFINDER_LEFT = (int) ((getDisplayWidth() - Config.VIEWFINDER_WIDTH) / 2);

            Config.ARROWL_ROTATE=90;
            Config.ARROWR_ROTATE=90;
        }
        Config.PROCESS_RECT_OFFEST_RATE=(float)(1-51.84/Config.PROCESS_RECT_DP);
        Config.PROCESS_RECT = (int) (Config.PROCESS_RECT_DP * sDisplayMetrics.scaledDensity);
    }

    /**************************/

    public static class Config {
        public static int STATIC_LINE_TOP;
        public static int STATIC_LINE_HEIGHT;
        public static int ARROWL_LEFT;
        public static int ARROWL_TOP;
        public static int ARROWL_WIDTH;
        public static int ARROWL_HEIGHT;
        public static float ARROWL_ROTATE;

        public static int ARROWR_LEFT;
        public static int ARROWR_TOP;
        public static int ARROWR_WIDTH;
        public static int ARROWR_HEIGHT;
        public static float ARROWR_ROTATE;

        public static int STATIC_RECT_LEFT;
        public static int STATIC_RECT_RIGHT;
        public static int STATIC_RECT_TOP;
        public static int STATIC_RECT_WIDTH;
        public static int STATIC_RECT_HEIGHT;

        public static float STATIC_LINE_ROTATE;

        public static int VIEWFINDER_LEFT;
        public static int VIEWFINDER_TOP;
        public static int VIEWFINDER_WIDTH;
        public static int VIEWFINDER_HEIGHT;
        public static float VIEWFINDER_ROTATE;

        public static float PROCESS_RECT_OFFEST_RATE;
        public static int PROCESS_RECT;

        public static final int STATIC_RECT_SECOND_DP = 102;
        public static final int VIEWFINDER_SECOND_DP = 96;
        public static final int PROCESS_RECT_DP = 101;
        public static final int ARROW_WIDTH_DP = 72;
        public static final int ARROW_HEIGHT_DP = 72;

    }

    static class Vertical extends Config {
        /**
         * Arrow left
         */
        public static final int ARROWL_LEFT_DP = 6;

        /**
         * rect
         */
        public static final int STATIC_RECT_LEFT_DP = -3;
        
        /**
         * rect
         */
        public static final int STATIC_RECT_RIGHT_DP  = -3;

        /**
         * viewfinder image
         */
        public static final int VIEWFINDER_LEFT_DP = 0;

    }

    static class Horizontal extends Config {
        /**
         * Arrow left
         */
        public static final int ARROWL_TOP_DP = 90;

        /**
         * rect
         */
        public static final int STATIC_RECT_TOP_DP = 80;
        /**
         * wid position deviation
         */
        public static final int STATIC_RECT_WID_DEVIATION=70;


        /**
         * viewfinder image
         */
        public static final int VIEWFINDER_TOP_DP = 83;


    }
}
