package com.jason.remotecamera_wja.app;

import android.os.Environment;

import com.jason.remotecamera_wja.camera.CameraPreview;

import java.io.File;

public class Constant {

    public final static String picturePath=Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES).getPath()+ File.separator+ CameraPreview.TAG+File.separator;

}
