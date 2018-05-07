package com.jason.remotecamera_wja.util;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class DisplayUtil {

	private static final String TAG = "DisplayUtil";

	public static int dip2px(Context context, float dipValue){            
		final float scale = context.getResources().getDisplayMetrics().density;                 
		return (int)(dipValue * scale + 0.5f);         
	}     

	public static int px2dip(Context context, float pxValue){                
		final float scale = context.getResources().getDisplayMetrics().density;                 
		return (int)(pxValue / scale + 0.5f);         
	} 

	public static Point getScreenMetrics(Context context){
		DisplayMetrics dm =context.getResources().getDisplayMetrics();
		int w_screen = dm.widthPixels;
		int h_screen = dm.heightPixels;
		Log.i(TAG, "Screen---Width = " + w_screen + " Height = " + h_screen + " densityDpi = " + dm.densityDpi);
		return new Point(w_screen, h_screen);
		
	}

	public static float getScreenRate(Context context){
		Point P = getScreenMetrics(context);
		float H = P.y;
		float W = P.x;
		return (H/W);
	}

	/**
	 * 获取分辨率
	 *
	 * @param context
	 */
	public static int getScreenWidth(Context context) {
		int reWidth = 0;
		try {
			DisplayMetrics metrics = new DisplayMetrics();
			WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			window.getDefaultDisplay().getMetrics(metrics);
			int width = metrics.widthPixels;
			int height = metrics.heightPixels;
			int temp;
			if (width > height) {
				temp = width;
				width = height;
				height = temp;
			}
			reWidth = width;
		} catch (Exception e2) {
			Log.e(TAG, "read resolution fail");

		}
		return reWidth;
	}

	/**
	 * 获取分辨率
	 *
	 * @param context
	 */
	public static int getScreenHeight(Context context) {
		int reHeight = 0;
		try {
			DisplayMetrics metrics = new DisplayMetrics();
			WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			window.getDefaultDisplay().getMetrics(metrics);
			int width = metrics.widthPixels;
			int height = metrics.heightPixels;
			int temp;
			if (width > height) {
				temp = width;
				width = height;
				height = temp;
			}
			reHeight = height;
		} catch (Exception e2) {
			Log.e(TAG, "read resolution fail");

		}
		return reHeight;
	}
}
