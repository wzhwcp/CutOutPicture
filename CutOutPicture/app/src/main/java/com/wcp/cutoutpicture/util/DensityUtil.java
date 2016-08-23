package com.wcp.cutoutpicture.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.lang.reflect.Field;

/**
 * @author supernan 屏幕适配工具
 */
public class DensityUtil {
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		if (density==0) {
			density = context.getResources().getDisplayMetrics().density;
		}
		return (int) (dpValue * density + 0.5f);
	}
	private static float density;
	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static float px2dip(Context context, float pxValue) {
		if (density==0) {
			density = context.getResources().getDisplayMetrics().density;
		}
		return pxValue / density + 0.5f;
	}

	private static int statusBarHeight;
	// 获取手机状态栏高度 2
	public static int getStatusBarHeight(Context context) {
		if (statusBarHeight!=0) return statusBarHeight;
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, statusBarHeight = 0; 
		try { 
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance(); 
			field = c.getField("status_bar_height"); 
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = context.getResources().getDimensionPixelSize(x); 
			} catch (Exception e1) {
				e1.printStackTrace(); 
				} 
		return statusBarHeight; 
		}
	
	public static DisplayMetrics getScreenSize (Activity activity) {
		WindowManager wm = activity.getWindowManager();
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		return dm;
	}
}
