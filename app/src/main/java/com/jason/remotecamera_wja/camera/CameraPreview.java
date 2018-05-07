package com.jason.remotecamera_wja.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.app.Constant;
import com.jason.remotecamera_wja.util.BitmapUtil;
import com.jason.remotecamera_wja.util.DebugUtil;
import com.jason.remotecamera_wja.util.DensityUtil;
import com.jason.remotecamera_wja.util.ImageUtil;
import com.jason.remotecamera_wja.view.RectImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    public static final String TAG = "CameraPreview";
    public static final int MEDIA_TYPE_IMAGE=1;
    public static final int TEMP_IMAGE=2;

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Uri outputMediaFileUri;
    private String outputMediaFileType;
    private float oldDist = 1f;
    private List<Camera.Size> previewSizes;
    private List<Camera.Size> pictureSizes;

    public CameraPreview(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    //接受触摸点的坐标，返回转换后坐标介于【-1000，1000】的矩形区域
    private static Rect calculateTapArea(float x, float y, float coefficient, int width, int height) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / width * 2000 - 1000);
        int centerY = (int) (y / height * 2000 - 1000);

        int halfAreaSize = areaSize / 2;
        RectF rectF = new RectF(clamp(centerX - halfAreaSize, -1000, 1000)
                , clamp(centerY - halfAreaSize, -1000, 1000)
                , clamp(centerX + halfAreaSize, -1000, 1000)
                , clamp(centerY + halfAreaSize, -1000, 1000));
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    //设置对焦区域
    private  void handleFocus(MotionEvent event, Camera camera) {

        int viewWidth=getWidth();
        int viewHeight=getHeight();
        DebugUtil.debug("宽："+viewWidth,"高"+viewHeight);
        Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f, viewWidth, viewHeight);

        camera.cancelAutoFocus();
        Camera.Parameters params = camera.getParameters();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            Log.i(TAG, "focus areas not supported");
        }
        final String currentFocusMode = params.getFocusMode();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        camera.setParameters(params);

        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Camera.Parameters params = camera.getParameters();
                params.setFocusMode(currentFocusMode);
                camera.setParameters(params);
            }
        });
    }

    //捕获触摸事件，获取手指数目，只有一个手指时触发对焦
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            handleFocus(event, mCamera);
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if (newDist > oldDist) {
                        handleZoom(true, mCamera);
                    } else if (newDist < oldDist) {
                        handleZoom(false, mCamera);
                    }
                    oldDist = newDist;
                    break;
            }
        }
        return true;
    }

    //进行拍照,图片保存路径为系统的picture下，名称为IMG_当前时间.jpg
    public void takePicture(final ImageView view){
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d(TAG, "Error creating media file, check storage permissions");
                    return;
                }
                try {
                    if(null != data) {
                        Bitmap b = null;
                        b = BitmapFactory.decodeByteArray(data, 0, data.length);
                        mCamera.stopPreview();
                    }
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    DebugUtil.debug("拍摄的照片旋转角度为："+BitmapUtil.readPictureDegree(pictureFile.getPath()));
                    Bitmap bitmap=BitmapUtil.rotateBitmapByDegree(MediaStore.Images.Media.getBitmap(InitApp.AppContext.getContentResolver(),outputMediaFileUri),
                            BitmapUtil.readPictureDegree(pictureFile.getPath()));
                    view.setImageBitmap(bitmap);//显示到小预览图中
                    camera.startPreview();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }
            }
        });
    }

    //B端进行拍照
    public void takePicture2(final ImageView view,final TakePhotoBackListener listener){
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d(TAG, "Error creating media file, check storage permissions");
                    return;
                }
                try {
                    if(null != data) {
                        Bitmap b = null;
                        b = BitmapFactory.decodeByteArray(data, 0, data.length);
                        mCamera.stopPreview();
                    }
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    DebugUtil.debug("拍摄的照片旋转角度为："+BitmapUtil.readPictureDegree(pictureFile.getPath()));
                    Bitmap bitmap=BitmapUtil.rotateBitmapByDegree(MediaStore.Images.Media.getBitmap(InitApp.AppContext.getContentResolver(),outputMediaFileUri),
                            BitmapUtil.readPictureDegree(pictureFile.getPath()));
                    view.setImageBitmap(bitmap);//显示到小预览图中
                    listener.uploadPictureToB(data);
                    camera.startPreview();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }
            }
        });
    }

    //进行拍照,图片保存路径为系统的picture下，名称为IMG_当前时间.jpg
    public void takePicture(final ImageView view,final int w,final int h,final int x,final int y){
        final int DST_RECT_WIDTH= RectImageView.RECTRADIU;
        final int DST_RECT_HEIGHT=RectImageView.RECTRADIU;
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.i(TAG, "myJpegCallback:onPictureTaken...");
                Bitmap b = null;
                if(null != data){
                    b = BitmapFactory.decodeByteArray(data, 0, data.length);
                    mCamera.stopPreview();
                }
                if(null != b) {
                    Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 0.0f);
                    Bitmap bitmap=BitmapUtil.resizeBitmap(rotaBitmap,w,h);//缩放成屏幕大小

                    int new_y= y-(DensityUtil.getScreenHeight(InitApp.AppContext)- Constant.DEFAULT_HEIGHT-h-DensityUtil.getStatusBarHeight(InitApp.AppContext))/2;
                    Log.i(TAG, "w="+w+" h="+h+"Bitmap.getWidth() = " + bitmap.getWidth()
                            + " \nBitmap.getHeight() = " + bitmap.getHeight()+" new_y="+new_y+
                            " 屏幕高度为："+DensityUtil.getScreenHeight(InitApp.AppContext));
                    //开始裁剪图片
                    int crop_width=DST_RECT_WIDTH;
                    int crop_height=DST_RECT_HEIGHT;
                    if((x+DST_RECT_WIDTH)>bitmap.getWidth()) crop_width=bitmap.getWidth()-x;
                    if((new_y+DST_RECT_HEIGHT)>bitmap.getHeight()) crop_height=bitmap.getHeight()-y;
                    Log.i(TAG, "x="+x+" new_y="+new_y+"crop_width = " + crop_width
                            + " \ncrop_height = " + crop_height+
                            " 屏幕高度为："+DensityUtil.getScreenHeight(InitApp.AppContext));
                    Bitmap rectBitmap = Bitmap.createBitmap(bitmap, x,new_y, crop_width, crop_height);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    rectBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] datas = baos.toByteArray();
                    File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                    if (pictureFile == null) {
                        Log.d(TAG, "Error creating media file, check storage permissions");
                        return;
                    }
                    try {
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(datas);//存入裁剪后的图片
                        fos.close();
                        DebugUtil.debug("拍摄的照片旋转角度为："+BitmapUtil.readPictureDegree(pictureFile.getPath()));
                        view.setImageBitmap(BitmapUtil.rotateBitmapByDegree(MediaStore.Images.Media.getBitmap(InitApp.AppContext.getContentResolver(),outputMediaFileUri),
                                BitmapUtil.readPictureDegree(pictureFile.getPath())));

                        camera.startPreview();
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d(TAG, "Error accessing file: " + e.getMessage());
                    }
                    if(rotaBitmap.isRecycled()){
                        rotaBitmap.recycle();
                        rotaBitmap = null;
                    }
                    if(rectBitmap.isRecycled()){
                        rectBitmap.recycle();
                        rectBitmap = null;
                    }
                }
                if(!b.isRecycled()){
                    b.recycle();
                    b = null;
                }
            }
        });
    }

    //B端进行区域拍照
    public void takePicture2(final ImageView view,final int w,final int h,final int x,final int y,final TakePhotoBackListener listener){
        final int DST_RECT_WIDTH= RectImageView.RECTRADIU;
        final int DST_RECT_HEIGHT=RectImageView.RECTRADIU;
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.i(TAG, "myJpegCallback:onPictureTaken...");
                Bitmap b = null;
                if(null != data){
                    b = BitmapFactory.decodeByteArray(data, 0, data.length);
                    mCamera.stopPreview();
                }
                if(null != b) {
                    Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 0.0f);
                    Bitmap bitmap=BitmapUtil.resizeBitmap(rotaBitmap,w,h);//缩放成屏幕大小

                    int new_y= y-(DensityUtil.getScreenHeight(InitApp.AppContext)- Constant.DEFAULT_HEIGHT-h-DensityUtil.getStatusBarHeight(InitApp.AppContext))/2;
                    Log.i(TAG, "w="+w+" h="+h+"Bitmap.getWidth() = " + bitmap.getWidth()
                            + " \nBitmap.getHeight() = " + bitmap.getHeight()+" new_y="+new_y+
                            " 屏幕高度为："+DensityUtil.getScreenHeight(InitApp.AppContext));
                    //开始裁剪图片
                    int crop_width=DST_RECT_WIDTH;
                    int crop_height=DST_RECT_HEIGHT;
                    if((x+DST_RECT_WIDTH)>bitmap.getWidth()) crop_width=bitmap.getWidth()-x;
                    if((new_y+DST_RECT_HEIGHT)>bitmap.getHeight()) crop_height=bitmap.getHeight()-y;
                    Log.i(TAG, "x="+x+" new_y="+new_y+"crop_width = " + crop_width
                            + " \ncrop_height = " + crop_height+
                            " 屏幕高度为："+DensityUtil.getScreenHeight(InitApp.AppContext));
                    Bitmap rectBitmap = Bitmap.createBitmap(bitmap, x,new_y, crop_width, crop_height);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    rectBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] datas = baos.toByteArray();
                    File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                    if (pictureFile == null) {
                        Log.d(TAG, "Error creating media file, check storage permissions");
                        return;
                    }
                    try {
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(datas);//存入裁剪后的图片
                        fos.close();
                        DebugUtil.debug("拍摄的照片旋转角度为："+BitmapUtil.readPictureDegree(pictureFile.getPath()));
                        view.setImageBitmap(BitmapUtil.rotateBitmapByDegree(MediaStore.Images.Media.getBitmap(InitApp.AppContext.getContentResolver(),outputMediaFileUri),
                                BitmapUtil.readPictureDegree(pictureFile.getPath())));
                        listener.uploadPictureToB(datas);
                        camera.startPreview();
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d(TAG, "Error accessing file: " + e.getMessage());
                    }
                    if(rotaBitmap.isRecycled()){
                        rotaBitmap.recycle();
                        rotaBitmap = null;
                    }
                    if(rectBitmap.isRecycled()){
                        rectBitmap.recycle();
                        rectBitmap = null;
                    }
                }
                if(!b.isRecycled()){
                    b.recycle();
                    b = null;
                }
            }
        });
    }

    //调整显示的旋转角度
    private void adjustDisplayRatio(int rotation) {
        ViewGroup parent = ((ViewGroup) getParent());
        Rect rect = new Rect();
        parent.getLocalVisibleRect(rect);
        int width = rect.width();
        int height = rect.height();
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        int previewWidth;
        int previewHeight;
        if (rotation == 90 || rotation == 270) {
            previewWidth = previewSize.height;
            previewHeight = previewSize.width;
        } else {
            previewWidth = previewSize.width;
            previewHeight = previewSize.height;
        }

        if (width * previewHeight > height * previewWidth) {
            final int scaledChildWidth = previewWidth * height / previewHeight;

            layout((width - scaledChildWidth) / 2, 0,
                    (width + scaledChildWidth) / 2, height);
        } else {
            final int scaledChildHeight = previewHeight * width / previewWidth;
            layout(0, (height - scaledChildHeight) / 2,
                    width, (height + scaledChildHeight) / 2);
        }
    }

    //获取显示的旋转角度
    public int getDisplayOrientation() {

        Camera.CameraInfo camInfo =
                new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);


        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = (camInfo.orientation - degrees + 360) % 360;
        return result;
    }

    //返回一个图片文件类型
    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), TAG);
        File mediaStorageDir2 = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "temp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
            outputMediaFileType = "image/*";
        } else if(type==TEMP_IMAGE){
            mediaFile = new File(mediaStorageDir2.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
            outputMediaFileType = "image/*";
        }else{
            return null;
        }
        outputMediaFileUri = Uri.fromFile(mediaFile);
        DebugUtil.debug(outputMediaFileUri.toString());

        return mediaFile;
    }

    //手指间距
    private static float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    //设置缩放
    private void handleZoom(boolean isZoomIn, Camera camera) {
        Camera.Parameters params = camera.getParameters();
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            params.setZoom(zoom);
            camera.setParameters(params);
        } else {
            Log.i(TAG, "zoom not supported");
        }
    }

    public Uri getOutputMediaFileUri() {
        return outputMediaFileUri;
    }

    public Camera getCameraInstance() {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
                int rotation=getDisplayOrientation();
                mCamera.setDisplayOrientation(rotation);
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setRotation(rotation);
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                Log.d(TAG, "camera is not available");
            }
        }
        return mCamera;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        getCameraInstance();
        try {
            mCamera.setPreviewDisplay(holder);
            int rotation=getDisplayOrientation();
            mCamera.setDisplayOrientation(rotation);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setRotation(rotation);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder.removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
       /* int rotation=getDisplayOrientation();
        mCamera.setDisplayOrientation(rotation);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(rotation);
        mCamera.setParameters(parameters);*/
        //adjustDisplayRatio(rotation);
    }

}
