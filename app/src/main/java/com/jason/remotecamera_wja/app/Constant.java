package com.jason.remotecamera_wja.app;

import android.os.Environment;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.camera.CameraPreview;
import com.jason.remotecamera_wja.util.DensityUtil;

import java.io.File;

public class Constant {

    public final static String picturePath=Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES).getPath()+ File.separator+ CameraPreview.TAG+File.separator;

    public final static float CAME_SETTING_HEIGHT=70f;
    public final static int DEFAULT_HEIGHT= DensityUtil.dip2px(InitApp.AppContext,Constant.CAME_SETTING_HEIGHT);
    public final static int NOCONNECT=0;
    public final static int ISCONNECT=1;
    public final static int CONNECTTING=2;
    public final static int OFFCONNECT=3;
    public final static String ServiceAddress1= "192.168.43.70";
    public final static String ServiceAddress= "192.168.1.182";
    public final static int DEFAULT_PORT=10003;

    //B端发送的指令Key
    public final static String TOKEPHOTO="101";
    public final static String PICTURE_SIZE="103";
    public final static String FLASH_MODE="104";
    public final static String FOCUS_MODE="105";
    public final static String WHITE_BALANCE="106";
    public final static String EXPOS_COMP="108";
    public final static String JPEG_QUALITY="109";
    public final static String AREA_TOKEPHOTO="110";
    public final static String GETPARAMSFROMA="301";

    //B端发送的标志位
    public final static int TOKEPHONTFLAG=11;
    public final static int AREAFLAG=12;
    public final static int PARAMSFLAG=13;
    public final static int PICTUREFLAG=14;
    public final static int FLASHFALG=15;
    public final static int FOCUSFALG=16;
    public final static int WHITEFALG=17;
    public final static int EXPOSFALG=18;
    public final static int JPEGFALG=19;
    public final static int DISCONNECT=500;

    //A端回复B端的指令
    public final static int RESPONSE_TOKEPHOTO=101;
    public final static int AREA_TOKEPHOTO_SUCCESS=200;
    public final static int RESPONSE_PARAMS=201;
    public final static int RESPONSE_NORMAL=210;
    public final static int RESPONSE_PICTURE=202;
    public final static int RESPONSE_FLASH=203;
    public final static int RESPONSE_FOCUS=204;
    public final static int RESPONSE_WHITE=205;
    public final static int RESPONSE_EXPOS=206;
    public final static int RESPONSE_JPEG=207;
    
}
