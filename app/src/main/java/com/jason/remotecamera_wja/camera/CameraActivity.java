package com.jason.remotecamera_wja.camera;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
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
import com.jason.remotecamera_wja.socketTest.ClientConnector;
import com.jason.remotecamera_wja.util.DebugUtil;
import com.jason.remotecamera_wja.util.DensityUtil;
import com.jason.remotecamera_wja.util.DisplayUtil;
import com.jason.remotecamera_wja.util.NetworkUtil;
import com.jason.remotecamera_wja.util.StringUtils;
import com.jason.remotecamera_wja.util.ToastUtil;
import com.jason.remotecamera_wja.view.RectImageView;

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

    private HandlerThread mHandlerThread;
    private ClientConnector mConnector;
    private String mDstName = Constant.ServiceAddress;
    private int mDstPort = Constant.DEFAULT_PORT;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
           if(msg.what==0x01){
               ToastUtil.showToast(InitApp.AppContext, NetworkUtil.getlocalip());
           }else if(msg.what==0x11) {
                Bundle bundle = msg.getData();
                ToastUtil.showToast(InitApp.AppContext,bundle.getString("msg"));
                String flag=bundle.getString("msg");
                switch (flag){
                    case Constant.TOKEPHOTO:
                        tokeAPhoto();
                        break;
                    case Constant.AREA_TOKEPHOTO:
                        makeAreaTokePhoto();
                        break;
                }

            }
        }
    };


    public static void launch(String flag) {
        InitApp.AppContext.startActivity(new Intent(InitApp.AppContext, CameraActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AppUtil.fullScreen(this);
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
                    dos.writeShort(Integer.valueOf(Constant.TOKEPHOTO));
                    dos.write(data);

                    //dos.close();
                    fis.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void sendMessage(final int flag,final String message){
        new Thread() {
            public void run() {
                Message msg = new Message();
                msg.what = 0x11;
                try {
                    OutputStream output = socket.getOutputStream();
                    DataOutputStream dos=new DataOutputStream(output);
                    dos.writeInt(StringUtils.strToByteArray(message).length);                    dos.writeShort(flag);
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

    private Rect createCenterScreenRect(int w, int h){
        int x1 = DensityUtil.getScreenWidth(InitApp.AppContext)/2-w/2;
        int y1 = DensityUtil.getScreenHeight(InitApp.AppContext)/2-h/2;
        int x2 = x1 + w;
        int y2 = y1 + h;
        return new Rect(x1, y1, x2, y2);
    }

    public void initCamera(){
        mPreview = new CameraPreview(this);
        SettingsFragment.passCamera(mPreview,mPreview.getCameraInstance(),this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SettingsFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this));
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
        DebugUtil.debug("update中： height="+height+" \nwidth="+width+" \nmpreview_width="+mpreview_width+" \nmpreview_height="+mpreview_height);
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
