package com.android.camera.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class BitmapUtil {
    
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx, boolean roundTL, boolean roundTR, boolean roundBL, boolean roundBR)
    {
        try
        {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);
            //final float roundPx = convertDipToPixel(roundDip, context);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// draw round
                                                                    // 4Corner

            if (!roundTL)
            {
                Rect rectTL = new Rect(0, 0, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
                canvas.drawRect(rectTL, paint);
            }
            if (!roundTR)
            {
                Rect rectTR = new Rect(bitmap.getWidth() / 2, 0, bitmap.getWidth(), bitmap.getHeight() / 2);
                canvas.drawRect(rectTR, paint);
            }
            if (!roundBR)
            {
                Rect rectBR = new Rect(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth(), bitmap.getHeight());
                canvas.drawRect(rectBR, paint);
            }
            if (!roundBL)
            {
                Rect rectBL = new Rect(0, bitmap.getHeight() / 2, bitmap.getWidth() / 2, bitmap.getHeight());
                canvas.drawRect(rectBL, paint);
            }

            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            return output;
        } catch (Exception e)
        {
        }
        return bitmap;
    }
}
