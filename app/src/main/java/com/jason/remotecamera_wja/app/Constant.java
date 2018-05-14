package com.jason.remotecamera_wja.app;

import android.os.Environment;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.parta.camera.CameraPreview;
import com.jason.remotecamera_wja.util.DensityUtil;

import java.io.File;

/**
 * 系统常量
 */
public class Constant {

    //图片保存地址
    public final static String picturePath=Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES).getPath()+ File.separator+ CameraPreview.TAG+File.separator;

    //A端相机菜单栏高度
    public final static float CAME_SETTING_HEIGHT=70f;
    public final static int DEFAULT_HEIGHT= DensityUtil.dip2px(InitApp.AppContext,Constant.CAME_SETTING_HEIGHT);

    //区域拍照设定的长宽
    public final static int RECTWIDTH=600;
    public final static int RECTHEIGHT=600;
    public final static int RECTWIDTHFORB=500;
    public final static int RECTHEIGHTFORB=500;

    //A端创建wifi的名称和密码
    public final static String WIFISSID="RemoteCamerawja";
    public final static String WIFIPASSWORD="123456789";

    //A端默认ip和端口号
    public final static String ServiceAddress1= "192.168.43.70";
    public final static String ServiceAddress2= "192.168.191.2";
    public final static String ServiceAddress3= "0.0.0.0";
    public final static String ServiceAddress4= "192.168.43.70";
    public final static String ServiceAddress= "192.168.1.182";
    public final static int DEFAULT_PORT=10004;

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
    public final static int STATE_FROM_SERVER_ERROR=10;
    public final static int TOKEPHONTFLAG=11;
    public final static int AREAFLAG=12;
    public final static int PARAMSFLAG=13;
    public final static int PICTUREFLAG=14;
    public final static int FLASHFALG=15;
    public final static int FOCUSFALG=16;
    public final static int WHITEFALG=17;
    public final static int EXPOSFALG=18;
    public final static int JPEGFALG=19;
    public final static int ISOFALG=22;
    public final static int FOUCSFALG=20;//对焦，传送坐标点，按照比例缩放
    public final static int AREAPOINTFALG=21;//区域拍照，传送坐标点，按照比例缩放

    //A端回复B端的指令
    public final static int RESPONSE_TOKEPHOTO=101;
    public final static int AREA_TOKEPHOTO_SUCCESS=200;
    public final static int AREA_DELETE_SUCCESS=211;
    public final static int RESPONSE_PARAMS=201;
    public final static int RESPONSE_MESSAGE=209;
    public final static int RESPONSE_FOCUS_SUCCESS=212;
    public final static int RESPONSE_PICTURE=202;
    public final static int RESPONSE_FLASH=203;
    public final static int RESPONSE_FOCUS=204;
    public final static int RESPONSE_WHITE=205;
    public final static int RESPONSE_EXPOS=206;
    public final static int RESPONSE_ISO=208;
    public final static int RESPONSE_JPEG=207;

    //A端其他指令
    public final static int A_ERROR=404;
}
