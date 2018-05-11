package com.jason.remotecamera_wja.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * 图片处理工具类
 */
public class ImageUtil {

	/**
	 * 旋转图片
	 * @param b 图片
	 * @param rotateDegree 旋转角度
	 * @return
	 */
	public static Bitmap getRotateBitmap(Bitmap b, float rotateDegree){
		Matrix matrix = new Matrix();
		matrix.postRotate((float)rotateDegree);
		Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
		return rotaBitmap;
	}
}
