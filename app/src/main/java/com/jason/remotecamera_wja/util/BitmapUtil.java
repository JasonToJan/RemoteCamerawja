package com.jason.remotecamera_wja.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public final class BitmapUtil {


    public BitmapUtil() {

    }

    public static Bitmap getBitmapThumbnail(String path,int width,int height){
        Bitmap bitmap = null;
        //这里可以按比例缩小图片：
            /*BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 4;//宽和高都是原来的1/4
            bitmap = BitmapFactory.decodeFile(path, opts); */

            /*进一步的，
                如何设置恰当的inSampleSize是解决该问题的关键之一。BitmapFactory.Options提供了另一个成员inJustDecodeBounds。
               设置inJustDecodeBounds为true后，decodeFile并不分配空间，但可计算出原始图片的长度和宽度，即opts.width和opts.height。
               有了这两个参数，再通过一定的算法，即可得到一个恰当的inSampleSize。*/
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        opts.inSampleSize = Math.max((int)(opts.outHeight / (float) height), (int)(opts.outWidth / (float) width));
        opts.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(path, opts);
        return bitmap;
    }

    public static Bitmap getBitmapThumbnail(Bitmap bmp,int width,int height){
        Bitmap bitmap = null;
        if(bmp != null ){
            int bmpWidth = bmp.getWidth();
            int bmpHeight = bmp.getHeight();
            if(width != 0 && height !=0){
                Matrix matrix = new Matrix();
                float scaleWidth = ((float) width / bmpWidth);
                float scaleHeight = ((float) height / bmpHeight);
                matrix.postScale(scaleWidth, scaleHeight);
                bitmap = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight, matrix, true);
            }else{
                bitmap = bmp;
            }
        }
        return bitmap;
    }

    /**
     * 加载本地图片
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 通过uri获取图片并进行压缩
     *
     * @param uri
     */
    public static Bitmap getBitmapFormUri(Context context, Uri uri) throws FileNotFoundException, IOException {

        InputStream input = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        //图片分辨率以480x800为标准
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        //比例压缩
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//设置缩放比例
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return compressImage(bitmap);//再进行质量压缩
    }

    /**
     * 质量压缩方法
     *
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * uri转path
     */
    public static String getRealFilePath(final Context context, final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * path转uri
     */
    public static Uri getUri(Context context,String path){
        Uri uri = null;
        if (path != null) {
            path = Uri.decode(path);
            ContentResolver cr = context.getContentResolver();
            StringBuffer buff = new StringBuffer();
            buff.append("(")
                    .append(MediaStore.Images.ImageColumns.DATA)
                    .append("=")
                    .append("'" + path + "'")
                    .append(")");
            Cursor cur = cr.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[] { MediaStore.Images.ImageColumns._ID },
                    buff.toString(), null, null);
            int index = 0;
            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                index = cur.getInt(index);
            }
            if (index == 0) {

            } else {
                Uri uri_temp = Uri.parse("content://media/external/images/media/" + index);
                if (uri_temp != null) {
                    uri = uri_temp;
                }
            }
        }
        return uri;
    }

    /**
     *  将图片从本地读到内存时,进行压缩 ,即图片从File形式变为Bitmap形式
     *  特点: 通过设置采样率, 减少图片的像素, 达到对内存中的Bitmap进行压缩
     * @param srcPath
     * @return
     */
    public static Bitmap compressImageFromFile(String srcPath, float pixWidth, float pixHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;// 只读边,不读内容
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, options);

        options.inJustDecodeBounds = false;
        int w = options.outWidth;
        int h = options.outHeight;
        int scale = 1;
        if (w > h && w > pixWidth) {
            scale = (int) (options.outWidth / pixWidth);
        } else if (w < h && h > pixHeight) {
            scale = (int) (options.outHeight / pixHeight);
        }
        if (scale <= 0)
            scale = 1;
        options.inSampleSize = scale;// 设置采样率

        options.inPreferredConfig = Bitmap.Config.ARGB_8888;// 该模式是默认的,可不设
        options.inPurgeable = true;// 同时设置才会有效
        options.inInputShareable = true;// 。当系统内存不够时候图片自动被回收

        bitmap = BitmapFactory.decodeFile(srcPath, options);
        // return compressBmpFromBmp(bitmap);//原来的方法调用了这个方法企图进行二次压缩
        // 其实是无效的,大家尽管尝试
        return bitmap;
    }

    /* 读取照片exif信息中的旋转角度
     * @param path 照片路径
     * @return角度
     */
    public static int readPictureDegree(String path) {
        int degree  = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm
     *            需要旋转的图片
     * @param degree
     *            旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 调整Bitmap大小
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    public static Bitmap resizeBitmap(Bitmap bitmap,int width,int height){
        // 获得相片的原始大小
        int oriWidth = bitmap.getWidth();
        int oriHeight = bitmap.getHeight();
        // 获得相片的缩放比例
        float scaleWidth = (float)width/oriWidth;
        float scaleHeight = (float)height/oriHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);
        Bitmap mBitmap = Bitmap.createBitmap(bitmap,0,0,
                oriWidth,oriHeight,matrix,true);
        // 记得将原来的bitmap对象回收
        bitmap.recycle();
        return mBitmap;
    }

    public static Bitmap Bytes2Bimap(byte[] b) {
       if (b.length != 0) {
          return BitmapFactory.decodeByteArray(b, 0, b.length);
       } else {
          return null;
       }
    }

    /**
     * bitmap转为base64
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}

