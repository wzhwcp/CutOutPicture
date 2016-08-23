package com.wcp.cutoutpicture.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by wcp on 2016/8/23.
 */
public class BitmapUtil {

    public static Bitmap compressBitmap(@NonNull Bitmap bitmap, Activity activity) {
        // 获取屏幕分辨率
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        // 图片分辨率与屏幕分辨率
        float scale = bitmap.getWidth() / (float) dm.widthPixels;

        Bitmap newBitMap = null;
        if (scale > 1) {
            newBitMap = zoomBitmap(bitmap, bitmap.getWidth() / scale,
                    bitmap.getHeight() / scale);
            bitmap.recycle();
            return newBitMap;
        }

        return bitmap;
    }

    //

    /**
     * 对分辨率较大的图片进行缩放
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    public static Bitmap zoomBitmap(@NonNull Bitmap bitmap, float width, float height) {

        int w = bitmap.getWidth();

        int h = bitmap.getHeight();

        Matrix matrix = new Matrix();

        float scaleWidth = ((float) width / w);

        float scaleHeight = ((float) height / h);

        matrix.postScale(scaleWidth, scaleHeight);// 利用矩阵进行缩放不会造成内存溢出

        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);

        return newbmp;
    }

    /**
     *
     * @param bitmap
     * @param cx
     * @param cy
     * @param halfLength   中心点到边距的长度 如果是圆则为半径
     * @param shapeType   0 输出圆  1 输出正方形
     * @return
     */
    public static Bitmap toShapeBitmap(Bitmap bitmap, float cx, float cy, float halfLength, int shapeType) {
        //要截取bitmap的大小
        Bitmap output = Bitmap.createBitmap((int)halfLength*2, (int)halfLength*2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect src = new Rect((int)(cx-halfLength), (int) (cy - halfLength), (int) (cx + halfLength), (int) (cy + halfLength));
        final Rect dst = new Rect( 0,  0, (int) halfLength*2, (int) halfLength*2);

        paint.setAntiAlias(true);// 设置画笔无锯齿
        canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas
        if (shapeType == 0) {
            canvas.drawCircle(halfLength, halfLength, halfLength, paint);
        } else {
            canvas.drawRect(0,0,halfLength * 2,halfLength * 2,paint);
        }

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452
        /**从src bitmap中取得区域放到dst output 指定区域中*/
        canvas.drawBitmap(bitmap, src, dst, paint); // 以Mode.SRC_IN模式合并bitmap和已经draw了的Circle

        return output;
    }

    /**
     * 获取当前屏幕的截图
     */
    public static Bitmap cutOutScreen(Activity activity) {
        // 1.构建Bitmap
        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int w = display.getWidth();
        int h = display.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        // 2.获取屏幕
        View decorview = activity.getWindow().getDecorView();
        decorview.setDrawingCacheEnabled(true);
        bitmap = decorview.getDrawingCache();
        return bitmap;
    }

}
