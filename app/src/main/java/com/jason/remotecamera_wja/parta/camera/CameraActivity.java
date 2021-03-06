package com.jason.remotecamera_wja.parta.camera;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.R;
import com.jason.remotecamera_wja.app.Constant;
import com.jason.remotecamera_wja.pictures.PicturesAll;
import com.jason.remotecamera_wja.util.AppUtil;
import com.jason.remotecamera_wja.util.DebugUtil;
import com.jason.remotecamera_wja.util.DensityUtil;
import com.jason.remotecamera_wja.util.NetworkUtil;
import com.jason.remotecamera_wja.util.StringUtils;
import com.jason.remotecamera_wja.util.ToastUtil;
import com.jason.remotecamera_wja.view.RectImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * A端相机主页面，实现了设置页面的一个监听器，当设置了某一个属性后，在相机主页面做相应的改变
 */
public class CameraActivity extends Activity implements SettingsFragment.UpdatepreviewListener,View.OnClickListener{

    private static final String TAG = "CameraActivity";
    private boolean isClickSetting=false;//是否点击了设置
    private CameraPreview mPreview;//相机预览的视图，自定义SurfaceView
    private Button settings;//设置按钮
    private Button takePhoto;//拍照按钮
    private ImageView priviewPhoto;//预览拍照的缩略图
    private Button rectPhoto;//区域拍照按钮
    private RectImageView rectImageView;//区域拍照视图，自定义ImageView
    private Fragment settingFragment;//设置页面的碎片
    private FrameLayout camera_menu;//用来显示相机设置的视图
    float previewRate = -1f;//预览视图的旋转角度
    private boolean isRectPhoto=false;//是否是区域拍照
    private RelativeLayout preview;//用来显示相机预览的视图
    private int mpreview_width;//预览视图的宽
    private int mpreview_height;//预览视图的高
    public static ServerSocket serverSocket = null;//A端建立的ServerSocket
    public Socket socket=null;//A端负责接收和分发信息的Socket

    /**
     * 接收到B的消息后，A这边进行的handler操作
     */
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String message=bundle.getString("msg","");
            switch (msg.what){
                case 0x01:{
                    ToastUtil.showToast(InitApp.AppContext, NetworkUtil.getlocalip());
                    break;
                }
                case Constant.TOKEPHONTFLAG:

                    ToastUtil.showToast(InitApp.AppContext,message);

                    tokeAPhoto();
                    break;
                case Constant.AREAFLAG:

                    ToastUtil.showToast(InitApp.AppContext,message);

                    makeAreaTokePhoto();
                    break;
                case Constant.PARAMSFLAG:
                    ToastUtil.showToast(InitApp.AppContext,"正在给B端发送参数信息！");
                    getParamsFromA();
                    break;
                case Constant.PICTUREFLAG:
                    ToastUtil.showToast(InitApp.AppContext,"正在设置照片分辨率！");
                    setPictureSize(message);
                    break;
                case Constant.FLASHFALG:
                    ToastUtil.showToast(InitApp.AppContext,"正在设置闪光灯！");
                    setFlashMode(message);
                    break;
                case Constant.FOCUSFALG:
                    ToastUtil.showToast(InitApp.AppContext,"正在设置对焦模式！");
                    setFocusMode(message);
                    break;
                case Constant.WHITEFALG:
                    ToastUtil.showToast(InitApp.AppContext,"正在设置白平衡！");
                    setWhiteMode(message);
                    break;
                case Constant.EXPOSFALG:
                    ToastUtil.showToast(InitApp.AppContext,"正在设置曝光补偿！");
                    setExposMode(message);
                    break;
                case Constant.JPEGFALG:
                    ToastUtil.showToast(InitApp.AppContext,"正在设置照片品质！");
                    setJpegMode(message);
                    break;
                case Constant.ISOFALG:
                    ToastUtil.showToast(InitApp.AppContext,"正在设置感光度ISO！");
                    setISO(message);
                    break;
                case Constant.FOUCSFALG:
                    String[] split = message.split("x");
                    ToastUtil.showToast(InitApp.AppContext,"接收到对焦请求！"
                            +"\n相对坐标为："+split[0]+" "+split[1]);
                    makeFocus(Float.parseFloat(split[0]),Float.parseFloat(split[1]));
                    break;
                case Constant.AREAPOINTFALG:
                    String[] split1 = message.split("x");
                    ToastUtil.showToast(InitApp.AppContext,"接收到区域移动信号！"
                            +"\n相对坐标为："+split1[0]+" "+split1[1]);
                    makeAChangeRect(Float.parseFloat(split1[0]),Float.parseFloat(split1[1]));
                    break;
                case Constant.A_ERROR:
                    ToastUtil.showToast(InitApp.AppContext,message);
                    finish();
                    break;
            }

        }
    };

    public static void launch(String flag) {
        InitApp.AppContext.startActivity(new Intent(InitApp.AppContext, CameraActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtil.fullScreen(this);
        setContentView(R.layout.activity_camera);

        initView();
        initCamera();

        try{
            serverSocket = new ServerSocket(Constant.DEFAULT_PORT);
        }catch (IOException e){
            e.printStackTrace();
        }
        receiverMessage();

    }

    /**
     * 开启一个接收消息的线程，这个线程是一直运行，内部还创建了一个服务器接收线程
     */
    public void receiverMessage(){
        new Thread() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.clear();
                Message msg1=new Message();
                msg1.what=0x01;
                mHandler.sendMessage(msg1);
                while (true) {
                    Message msg = new Message();
                    msg.what = 0x11;
                    try {
                        socket = serverSocket.accept();
                        new Thread(new ServerThread(socket,mHandler)).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
    }

    /**
     * 开启一个发送消息的线程，这个线程主要是为了上传图片
     * @param fileName 图片的path
     */
    public void sendMessage(final String fileName){
        new Thread() {
            public void run() {
                Message msg = new Message();
                msg.what = 0x11;
                try {

                    OutputStream output = socket.getOutputStream();
                    DataOutputStream dos=new DataOutputStream(output);
                    FileInputStream fis = new FileInputStream(fileName);
                    int size=fis.available();//获取文件的长度
                    byte[] data = new byte[size];
                    fis.read(data);

                    dos.writeInt(size);//将长度单独写入
                    dos.writeShort(Constant.RESPONSE_TOKEPHOTO);//将标志位单独写入
                    dos.write(data);//将图片信息以byte数组的形式写入

                    fis.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 开启一个发送消息的线程，主要向B端发送指令和消息
     * @param flag 标志位，B段接收消息的线程主要根据这个来分类
     * @param message 发送的消息内容
     */
    public void sendMessage(final int flag,final String message){
        new Thread() {
            public void run() {
                try {
                    DebugUtil.debug("发送消息："+"\n标志位："+flag+"\n消息"+message);
                    OutputStream output = socket.getOutputStream();
                    DataOutputStream dos=new DataOutputStream(output);
                    dos.writeInt(StringUtils.strToByteArray(message).length);
                    dos.writeShort(flag);
                    dos.write(StringUtils.strToByteArray(message));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void initView(){
        settings = findViewById(R.id.button_settings);
        takePhoto=findViewById(R.id.button_tokephoto);
        priviewPhoto=findViewById(R.id.photo_preview);
        rectPhoto=findViewById(R.id.button_rectphoto);
        rectImageView=findViewById(R.id.camera_rect);
        camera_menu=findViewById(R.id.camera_menu);
        preview = findViewById(R.id.camera_preview);

        settings.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        priviewPhoto.setOnClickListener(this);
        rectPhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.button_settings:
                if(!isClickSetting){
                    isClickSetting=true;
                    settingFragment=new SettingsFragment();
                    getFragmentManager().beginTransaction().replace(R.id.camera_menu,
                            settingFragment).commit();
                    camera_menu.setVisibility(View.VISIBLE);//fix 抖动问题

                }else{
                    isClickSetting=false;
                    getFragmentManager().beginTransaction().hide(settingFragment
                    ).commit();
                    camera_menu.setVisibility(View.GONE);
                }
                break;
            case R.id.button_tokephoto:
                if(!isRectPhoto){
                    mPreview.takePicture(priviewPhoto);
                }else{
                    int rect_x=rectImageView.getmFristPointX();
                    int rect_y=rectImageView.getmFristPointY();
                    mPreview.takePicture(priviewPhoto, mpreview_width, mpreview_height,
                            rect_x,rect_y);
                }
                break;
            case R.id.button_rectphoto:
                if(!isRectPhoto){
                    isRectPhoto=true;
                    makeARect(isRectPhoto);
                }else{
                    isRectPhoto=false;
                    makeARect(isRectPhoto);
                }
                break;
            case R.id.photo_preview:
                PicturesAll.launch("pictures");
                break;
        }
    }

    /**
     * B端发拍照请求后，A端接收到消息，进行拍照
     */
    public void tokeAPhoto(){
        if(mPreview!=null&&priviewPhoto!=null&&rectImageView!=null){
            if(!isRectPhoto){
                mPreview.takePicture2(priviewPhoto,new TakePhotoBackListener(){
                    @Override
                    public void uploadPictureToB(String fileName) {
                        //上传到B端
                        DebugUtil.debug("上传到B端："+fileName);

                        sendMessage(fileName);
                    }
                });
            }else{
                int rect_x=rectImageView.getmFristPointX();
                int rect_y=rectImageView.getmFristPointY();
                mPreview.takePicture2(priviewPhoto, mpreview_width, mpreview_height,
                        rect_x,rect_y,new TakePhotoBackListener(){
                            @Override
                            public void uploadPictureToB(String fileName) {
                                //上传到B端
                                DebugUtil.debug("上传到B端："+fileName);
                                sendMessage(fileName);
                            }
                        });

            }
        }
    }

    /**
     * B端发送区域请求后，A端创建一个矩形区域
     */
    public void makeAreaTokePhoto(){
        if(mPreview!=null&&priviewPhoto!=null&&rectImageView!=null){
            if(!isRectPhoto){
                isRectPhoto=true;
                makeARect(isRectPhoto);
                int point_x=rectImageView.getmFristPointX();
                int point_y=rectImageView.getmFristPointY()-rectImageView.getMin_height();
                //获取相机预览分辨率，高度：宽度
                int height=SettingsFragment.mCamera.getParameters().getPreviewSize().height;
                int width=SettingsFragment.mCamera.getParameters().getPreviewSize().width;
                mpreview_width=DensityUtil.getScreenWidth(this);
                mpreview_height=(int)((float)width/(float)height*mpreview_width);
                //发送当前区域拍照的相对坐标点
                float ratio_x=point_x/(float)mpreview_width;
                float ratio_y=point_y/(float)mpreview_height;
                float ratio2_x=(point_x+Constant.RECTWIDTH)/(float)mpreview_width;
                float ratio2_y=(point_y+Constant.RECTHEIGHT)/(float)mpreview_height;
                DebugUtil.debug("B端发送区域请求后，A端发送相对坐标点给B端"
                        +"\npoint_x="+point_x+ " point_y="+point_y
                        +" mpreview_width="+mpreview_width+" mpreview_height="+mpreview_height
                        +" ratio2_x="+ratio2_x+" ratio2_y="+ratio2_y);
                sendMessage(Constant.AREA_TOKEPHOTO_SUCCESS,ratio_x+"x"+ratio_y+"x"+ratio2_x+"x"+ratio2_y);
            }else{
                isRectPhoto=false;
                makeARect(isRectPhoto);
                //发送设置成功事件
                sendMessage(Constant.AREA_DELETE_SUCCESS,"已取消区域拍照！");
            }

        }
    }

    /**
     * B端发送参数设置请求后，获取A端相机参数，以json字符串的形式发送给B端
     */
    public void getParamsFromA(){
        int height=SettingsFragment.mCamera.getParameters().getPictureSize().height;
        int width=SettingsFragment.mCamera.getParameters().getPictureSize().width;
        String picture_size=width+"x"+height;

        String flash_mode=SettingsFragment.mCamera.getParameters().getFlashMode();
        String focus_mode=SettingsFragment.mCamera.getParameters().getFocusMode();
        String white_balance=SettingsFragment.mCamera.getParameters().getWhiteBalance();
        String exposure_compensation=String.valueOf(SettingsFragment.mCamera.getParameters().getExposureCompensation());
        String iso=SettingsFragment.mCamera.getParameters().get("iso");
        String jpeg_quality=String.valueOf(SettingsFragment.mCamera.getParameters().getJpegQuality());

        JSONObject jsonObject=new JSONObject();
        try{
            jsonObject.put("picture_size",picture_size);
            jsonObject.put("flash_mode",flash_mode);
            jsonObject.put("focus_mode",focus_mode);
            jsonObject.put("white_balance",white_balance);
            jsonObject.put("exposure_compensation",exposure_compensation);
            jsonObject.put("iso",iso);
            jsonObject.put("jpeg_quality",jpeg_quality);
        }catch (JSONException e){
            e.printStackTrace();
        }
        String paramsAll=jsonObject.toString();
        sendMessage(Constant.RESPONSE_PARAMS,paramsAll);
    }

    /**
     * A端自己创建一个矩形，位于屏幕正中心
     * @param isRectPhoto 是否已经是区域拍照
     */
    public void makeARect(boolean isRectPhoto){
        int x1 = DensityUtil.getScreenWidth(InitApp.AppContext)/2-Constant.RECTWIDTH/2;
        int y1 = (DensityUtil.getScreenHeight(InitApp.AppContext)
                -DensityUtil.dip2px(InitApp.AppContext,Constant.CAME_SETTING_HEIGHT))/2
                -Constant.RECTHEIGHT/2;
        int x2 = x1 + Constant.RECTWIDTH;
        int y2 = y1 + Constant.RECTHEIGHT;
        if(rectImageView != null){
            DebugUtil.debug("x1="+x1+" y1="+y1+" x2="+x2+" y2="+y2);
            Rect screenCenterRect = new Rect(x1, y1, x2, y2);
            rectImageView.setCenterRect(screenCenterRect);
        }
        if(isRectPhoto){
            rectImageView.setVisibility(View.VISIBLE);
        }else{
            rectImageView.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化相机,设置相机参数
     */
    public void initCamera(){
        mPreview = new CameraPreview(this);
        SettingsFragment.passCamera(mPreview,mPreview.getCameraInstance(),this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences_a, false);
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));


        //获取相机预览分辨率，高度：宽度
        int height=SettingsFragment.mCamera.getParameters().getPreviewSize().height;
        int width=SettingsFragment.mCamera.getParameters().getPreviewSize().width;
        mpreview_width=DensityUtil.getScreenWidth(this);
        mpreview_height=(int)((float)width/(float)height*mpreview_width);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.width=mpreview_width;
        lp.height=mpreview_height;
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        preview.setLayoutParams(lp);
        preview.addView(mPreview);

        rectImageView.setMax_width(mpreview_width);
        rectImageView.setMax_height(mpreview_height);
    }

    /**
     * 选择完预览屏幕分辨率后，预览的宽高也随之变化
     */
    @Override
    public void updatepreview(){
        isClickSetting=false;
        camera_menu.setVisibility(View.GONE);
        //获取相机预览分辨率，高度：宽度
        int height=SettingsFragment.mCamera.getParameters().getPreviewSize().height;
        int width=SettingsFragment.mCamera.getParameters().getPreviewSize().width;
        mpreview_width=DensityUtil.getScreenWidth(this);
        mpreview_height=(int)((float)width/(float)height*mpreview_width);
        int screenHight=DensityUtil.getScreenHeight(this);
        int maxHight=screenHight-DensityUtil.getStatusBarHeight(this)
                -DensityUtil.dip2px(this,Constant.CAME_SETTING_HEIGHT);
        DebugUtil.debug("update中： height="+height+" \nwidth="+width+" \nmpreview_width="
                +mpreview_width+" \nmpreview_height="
                +mpreview_height+"\n屏幕高度为："+screenHight+"\n最大高度为："+maxHight);
        //更新区域拍照范围
        rectImageView.setMax_width(mpreview_width);
        rectImageView.setMax_height(mpreview_height);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.width=mpreview_width;
        lp.height=mpreview_height;
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        preview.updateViewLayout(mPreview,lp);
    }

    /**
     * 设置照片分辨率
     */
    public void setPictureSize(String value){
        SettingsFragment.setPictureSize(value);
        sendMessage(Constant.RESPONSE_MESSAGE,"已成功设置照片分辨率为："+value);
        SettingsFragment.mCamera.setParameters(SettingsFragment.mParameters);
        SettingsFragment.setUpdate(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));

    }

    /**
     * 设置闪光灯
     */
    public void setFlashMode(String value){
        SettingsFragment.setFlashMode(value);
        sendMessage(Constant.RESPONSE_MESSAGE,"已成功设置闪光灯为："+value);
        SettingsFragment.mCamera.setParameters(SettingsFragment.mParameters);
        SettingsFragment.setUpdate(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));

    }

    /**
     * 设置对焦模式
     */
    public void setFocusMode(String value){
        SettingsFragment.setFocusMode(value);
        sendMessage(Constant.RESPONSE_MESSAGE,"已成功设置对焦模式为："+value);
        SettingsFragment.mCamera.setParameters(SettingsFragment.mParameters);
        SettingsFragment.setUpdate(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));

    }

    /**
     * 设置白平衡
     */
    public void setWhiteMode(String value){
        SettingsFragment.setWhiteBalance(value);
        sendMessage(Constant.RESPONSE_MESSAGE,"已成功设置白平衡为："+value);
        SettingsFragment.mCamera.setParameters(SettingsFragment.mParameters);
        SettingsFragment.setUpdate(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));

    }

    /**
     * 设置曝光补偿
     */
    public void setExposMode(String value){
        SettingsFragment.setExposComp(value);
        sendMessage(Constant.RESPONSE_MESSAGE,"已成功设置曝光补偿为："+value);
        SettingsFragment.mCamera.setParameters(SettingsFragment.mParameters);
        SettingsFragment.setUpdate(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));

    }

    /**
     * 设置感光度ISO
     */
    public void setISO(String value){
        SettingsFragment.setExposComp(value);
        sendMessage(Constant.RESPONSE_MESSAGE,"已成功设置感光度ISO为："+value);
        SettingsFragment.mCamera.setParameters(SettingsFragment.mParameters);
        SettingsFragment.setUpdate(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));
    }

    /**
     * 设置照片品质
     */
    public void setJpegMode(String value){
        SettingsFragment.setJpegQuality(value);
        sendMessage(Constant.RESPONSE_MESSAGE,"已成功设置照片品质为："+value);
        SettingsFragment.mCamera.setParameters(SettingsFragment.mParameters);
        SettingsFragment.setUpdate(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));
    }

    /**
     * 当B端选择触摸对焦后，发送相对坐标给A端，A端接收到坐标进行对焦
     * @param ratio_x 宽的比例
     * @param ratio_y 高的比例
     */
    public void makeFocus(float ratio_x,float ratio_y){
        int viewWidth=SettingsFragment.mCameraPreview.getWidth();
        int viewHeight=SettingsFragment.mCameraPreview.getHeight();
        DebugUtil.debug("宽："+viewWidth+" 高"+viewHeight);
        Rect focusRect = calculateTapArea(ratio_x*viewWidth, ratio_y*viewHeight, 1f, viewWidth, viewHeight);

        SettingsFragment.mCamera.cancelAutoFocus();
        Camera.Parameters params = SettingsFragment.mCamera.getParameters();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            DebugUtil.debug( "focus areas not supported");
        }
        final String currentFocusMode = params.getFocusMode();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        SettingsFragment.mCamera.setParameters(params);

        SettingsFragment.mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                DebugUtil.debug("聚焦成功！");
                Camera.Parameters params = camera.getParameters();
                params.setFocusMode(currentFocusMode);
                camera.setParameters(params);
            }
        });
        sendMessage(Constant.RESPONSE_MESSAGE,"已成功对焦");
    }

    /**
     * 接受触摸点的坐标，返回转换后坐标介于【-1000，1000】的矩形区域
     * @param x 触摸点的x坐标
     * @param y 触摸到的y坐标
     * @param coefficient 系数 1f
     * @param width 预览视图的宽
     * @param height 预览视图的高
     * @return
     */
    private static Rect calculateTapArea(float x, float y, float coefficient, int width, int height) {
        DebugUtil.debug("CameraActivity中 传入的x="+x+" \n传入的y="+y
                +"\n传入的width="+width+"\n传入的height="+height);
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / width * 2000 - 1000);
        int centerY = (int) (y / height * 2000 - 1000);

        int halfAreaSize = areaSize / 2;
        RectF rectF = new RectF(clamp(centerX - halfAreaSize, -1000, 1000)
                , clamp(centerY - halfAreaSize, -1000, 1000)
                , clamp(centerX + halfAreaSize, -1000, 1000)
                , clamp(centerY + halfAreaSize, -1000, 1000));
        DebugUtil.debug("矩形的坐标："+Math.round(rectF.left)+" "+Math.round(rectF.top)
                +" "+Math.round(rectF.right)+" "+Math.round(rectF.bottom));
        return new Rect(Math.round(rectF.left), Math.round(rectF.top)
                , Math.round(rectF.right), Math.round(rectF.bottom));
    }

    /**
     * 重定向x的值，规定了最大值和最小值
     * @param x
     * @param min
     * @param max
     * @return
     */
    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    /**
     * 当B端发送区域改变的信号给A端后，A端区域拍照发生相应的改变
     * @param ratio_x
     * @param ratio_y
     */
    public void makeAChangeRect(float ratio_x,float ratio_y){
        int max_width=SettingsFragment.mCameraPreview.getWidth();
        int max_height=SettingsFragment.mCameraPreview.getHeight();
        int min_height=(DensityUtil.getScreenHeight(this)
                -DensityUtil.dip2px(this,Constant.CAME_SETTING_HEIGHT))/2
                -max_height/2;
        int x1=(int)(max_width*ratio_x);
        int y1=(int)(max_height*ratio_y)+min_height;
        int x2 = x1 + Constant.RECTWIDTH;
        int y2 = y1 + Constant.RECTHEIGHT;
        rectImageView.setMax_width(max_width);
        rectImageView.setMax_height(max_height);
        if(rectImageView != null){
            DebugUtil.debug("最低高度为："+min_height+" x1="+x1+" y1="+y1+" x2="+x2+" y2="+y2);
            Rect screenCenterRect = new Rect(x1, y1, x2, y2);
            rectImageView.setCenterRect(screenCenterRect);
        }
        if(isRectPhoto){
            rectImageView.setVisibility(View.VISIBLE);
        }else{
            rectImageView.setVisibility(View.GONE);
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        if(mPreview==null){
            initCamera();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPreview=null;
    }

    @Override
    public void onDestroy() {
        if(SettingsFragment.mCamera!=null){
            SettingsFragment.mCamera.release();
            SettingsFragment.mCamera = null;
        }
        super.onDestroy();
        mPreview=null;
    }

}
