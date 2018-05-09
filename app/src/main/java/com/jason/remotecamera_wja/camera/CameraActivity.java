package com.jason.remotecamera_wja.camera;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.R;
import com.jason.remotecamera_wja.app.Constant;
import com.jason.remotecamera_wja.parta.pictures.PicturesAll;
import com.jason.remotecamera_wja.util.AppUtil;
import com.jason.remotecamera_wja.util.DebugUtil;
import com.jason.remotecamera_wja.util.DensityUtil;
import com.jason.remotecamera_wja.util.DisplayUtil;
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


public class CameraActivity extends Activity implements SettingsFragment.UpdatepreviewListener{

    private static final String TAG = "CameraActivity";
    private boolean isClickSetting=false;
    private CameraPreview mPreview;
    private Button settings;
    private Button takePhoto;
    private ImageView priviewPhoto;
    private Button rectPhoto;
    private RectImageView rectImageView;
    private Fragment settingFragment;
    private FrameLayout camera_menu;
    float previewRate = -1f;
    int DST_CENTER_RECT_WIDTH = 600;
    int DST_CENTER_RECT_HEIGHT = 600;
    private boolean isRectPhoto=false;
    private RelativeLayout preview;
    private int mpreview_width;
    private int mpreview_height;

    public static ServerSocket serverSocket = null;
    public Socket socket=null;

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
        initViewParams();
        setListener();
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
                    int size=fis.available();
                    byte[] data = new byte[size];
                    fis.read(data);

                    //DebugUtil.debug("图片大小为"+size+"\n图片byte数组为："+ StringUtils.byteArrayToStr(data));

                    dos.writeInt(size);
                    dos.writeShort(Constant.RESPONSE_TOKEPHOTO);
                    dos.write(data);

                    //dos.close();
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
               /* Message msg = new Message();
                msg.what = 0x11;*/
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
    }

    public void initViewParams(){
        //设置外部整体布局参数
        ViewGroup.LayoutParams params=mPreview.getLayoutParams();
        Point p= DisplayUtil.getScreenMetrics(this);
        params.width=p.x;
        params.height=p.y;
        Log.i(TAG, "screen: w = " + p.x + " y = " + p.y);
        previewRate = DisplayUtil.getScreenRate(this);
        mPreview.setLayoutParams(params);

    }

    /**
     * 设置点击事件
     */
    public void setListener(){
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });
        takePhoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!isRectPhoto){
                    mPreview.takePicture(priviewPhoto);
                }else{
                    int rect_x=rectImageView.getmFristPointX();
                    int rect_y=rectImageView.getmFristPointY();
                    mPreview.takePicture(priviewPhoto, mpreview_width, mpreview_height,
                            rect_x,rect_y);
                }
            }
        });
        priviewPhoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                PicturesAll.launch("pictures");
            }
        });
        rectPhoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                if(!isRectPhoto){
                    isRectPhoto=true;
                    makeARect(isRectPhoto);
                }else{
                    isRectPhoto=false;
                    makeARect(isRectPhoto);
                }
            }
        });
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
     * 创建一个矩形，接收到B端消息后，A端创建一个矩形区域
     */
    public void makeAreaTokePhoto(){
        if(mPreview!=null&&priviewPhoto!=null&&rectImageView!=null){
            if(!isRectPhoto){
                isRectPhoto=true;
                makeARect(isRectPhoto);
                //发送设置成功事件
                sendMessage(Constant.AREA_TOKEPHOTO_SUCCESS,"已设置区域拍照！");
            }else{
                isRectPhoto=false;
                makeARect(isRectPhoto);
                //发送设置成功事件
                sendMessage(Constant.AREA_TOKEPHOTO_SUCCESS,"已取消区域拍照！");
            }

        }
    }

    /**
     * 获取A端相机参数，以json字符串的形式发送给B端
     */
    public void getParamsFromA(){
        int height=SettingsFragment.mCamera.getParameters().getPictureSize().height;
        int width=SettingsFragment.mCamera.getParameters().getPictureSize().width;
        String picture_size=width+"x"+height;

        String flash_mode=SettingsFragment.mCamera.getParameters().getFlashMode();
        String focus_mode=SettingsFragment.mCamera.getParameters().getFocusMode();
        String white_balance=SettingsFragment.mCamera.getParameters().getWhiteBalance();
        String exposure_compensation=String.valueOf(SettingsFragment.mCamera.getParameters().getExposureCompensation());
        String jpeg_quality=String.valueOf(SettingsFragment.mCamera.getParameters().getJpegQuality());

        JSONObject jsonObject=new JSONObject();
        try{
            jsonObject.put("picture_size",picture_size);
            jsonObject.put("flash_mode",flash_mode);
            jsonObject.put("focus_mode",focus_mode);
            jsonObject.put("white_balance",white_balance);
            jsonObject.put("exposure_compensation",exposure_compensation);
            jsonObject.put("jpeg_quality",jpeg_quality);
        }catch (JSONException e){
            e.printStackTrace();
        }
        String paramsAll=jsonObject.toString();
        sendMessage(Constant.RESPONSE_PARAMS,paramsAll);
    }

    /**
     * A段自己创建一个矩形
     * @param isRectPhoto 是否已经是区域拍照
     */
    public void makeARect(boolean isRectPhoto){
        if(rectImageView != null){
            Rect screenCenterRect = createCenterScreenRect(DST_CENTER_RECT_WIDTH, DST_CENTER_RECT_HEIGHT);
            rectImageView.setCenterRect(screenCenterRect);
        }
        if(isRectPhoto){
            rectImageView.setVisibility(View.VISIBLE);
        }else{
            rectImageView.setVisibility(View.GONE);
        }

    }

    /**
     * 根据长宽来新建一个矩形
     * @param w
     * @param h
     * @return
     */
    private Rect createCenterScreenRect(int w, int h){
        int x1 = DensityUtil.getScreenWidth(InitApp.AppContext)/2-w/2;
        int y1 = DensityUtil.getScreenHeight(InitApp.AppContext)/2-h/2;
        int x2 = x1 + w;
        int y2 = y1 + h;
        return new Rect(x1, y1, x2, y2);
    }

    /**
     * 初始化相机
     */
    public void initCamera(){
        mPreview = new CameraPreview(this);
        SettingsFragment.passCamera(mPreview,mPreview.getCameraInstance(),this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences_a, false);
        //SettingsFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));

        //获取相机预览分辨率，高度：宽度
        int height=SettingsFragment.mCamera.getParameters().getPreviewSize().height;
        int width=SettingsFragment.mCamera.getParameters().getPreviewSize().width;
        mpreview_width=DensityUtil.getScreenWidth(this);
        mpreview_height=(int)((float)width/(float)height*mpreview_width);
        DebugUtil.debug(" height="+height+" \nwidth="+width+" \nmpreview_width="+mpreview_width+" \nmpreview_height="+mpreview_height);

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
        sendMessage(Constant.RESPONSE_PICTURE,"已成功设置照片分辨率！");
        SettingsFragment.mCamera.setParameters(SettingsFragment.mParameters);
        SettingsFragment.setUpdate(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));

    }

    /**
     * 设置闪光灯
     */
    public void setFlashMode(String value){
        SettingsFragment.setFlashMode(value);
        sendMessage(Constant.RESPONSE_FLASH,"已成功设置闪光灯！");
        SettingsFragment.mCamera.setParameters(SettingsFragment.mParameters);
        SettingsFragment.setUpdate(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));

    }

    /**
     * 设置对焦模式
     */
    public void setFocusMode(String value){
        SettingsFragment.setFocusMode(value);
        sendMessage(Constant.RESPONSE_FOCUS,"已成功设置对焦模式！");
        SettingsFragment.mCamera.setParameters(SettingsFragment.mParameters);
        SettingsFragment.setUpdate(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));

    }

    /**
     * 设置白平衡
     */
    public void setWhiteMode(String value){
        SettingsFragment.setWhiteBalance(value);
        sendMessage(Constant.RESPONSE_WHITE,"已成功设置白平衡！");
        SettingsFragment.mCamera.setParameters(SettingsFragment.mParameters);
        SettingsFragment.setUpdate(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));

    }

    /**
     * 设置曝光补偿
     */
    public void setExposMode(String value){
        SettingsFragment.setExposComp(value);
        sendMessage(Constant.RESPONSE_EXPOS,"已成功设置曝光补偿！");
        SettingsFragment.mCamera.setParameters(SettingsFragment.mParameters);
        SettingsFragment.setUpdate(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));

    }

    /**
     * 设置照片品质
     */
    public void setJpegMode(String value){
        SettingsFragment.setJpegQuality(value);
        sendMessage(Constant.RESPONSE_JPEG,"已成功设置照片品质！");
        SettingsFragment.mCamera.setParameters(SettingsFragment.mParameters);
        SettingsFragment.setUpdate(PreferenceManager.getDefaultSharedPreferences(this));
        SettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));

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
        super.onDestroy();
        mPreview=null;
    }

}
