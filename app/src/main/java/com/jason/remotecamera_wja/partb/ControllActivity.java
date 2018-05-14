package com.jason.remotecamera_wja.partb;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.R;
import com.jason.remotecamera_wja.app.Constant;
import com.jason.remotecamera_wja.parta.camera.CameraPreview;
import com.jason.remotecamera_wja.pictures.PicturesAll;
import com.jason.remotecamera_wja.util.DebugUtil;
import com.jason.remotecamera_wja.util.DensityUtil;
import com.jason.remotecamera_wja.util.DialogUtil;
import com.jason.remotecamera_wja.util.JsonUtils;
import com.jason.remotecamera_wja.util.SharePreferencesUtil;
import com.jason.remotecamera_wja.util.StringUtils;
import com.jason.remotecamera_wja.util.ToastUtil;
import com.jason.remotecamera_wja.view.RectImageViewForB;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 控制主页面，可以控制A端拍照，控制A端区域拍照，改变A端相机参数
 */
public class ControllActivity extends AppCompatActivity implements PartBSettingsFragment.UpdateListener,View.OnClickListener{

    public static final String TAG = "CameraPreview";
    private boolean isClickSetting=false;//是否点击了参数设置
    private boolean isClickArea=false;//是否点击了区域拍照
    private boolean isClickFocus=false;//是否点击了对焦
    private Fragment settingFragment;//设置参数的片段
    private Uri outputMediaFileUri;//接收到A端发送过来的图片，保存到本地的路径
    private Button controll_take_btn;//B端拍照按钮
    private Button controll_area_btn;//B端区域拍照按钮
    private Button controll_setting_btn;//B端参数设置
    private Button controll_focus_btn;//B端触摸对焦
    private FrameLayout camera_setting;//B端相机设置视图
    private TextView camera_focus_tv;//B端触摸对焦视图
    private RectImageViewForB camera_area_iv;//B端区域拍照视图
    private TextView partb_status_tv;//B端控制状态显示
    private ImageView controll_iv;//B端显示A端发送过来的图片
    public Socket socket;//B端用来消息通信的Socket

    /**
     * B端用来接收A端发送过来的消息的handler回调
     */
    public  Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case  Constant.RESPONSE_TOKEPHOTO:{
                    partb_status_tv.setText("正在控制拍照");
                    Bundle bundle=msg.getData();
                    byte[] data= bundle.getByteArray(Constant.TOKEPHOTO);

                    File pictureFile = getOutputMediaFile();
                    try{
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.close();
                        controll_iv.setImageURI(outputMediaFileUri);
                        DialogUtil.getInstance().closeDialog();
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    break;
                }
                case Constant.STATE_FROM_SERVER_ERROR:{
                    ToastUtil.showToast(InitApp.AppContext,"A端未创建或被异常关闭!");
                    finish();
                    break;
                }
                case Constant.AREA_TOKEPHOTO_SUCCESS:{
                    Bundle bundle=msg.getData();
                    String message=bundle.getString("msg");
                    ToastUtil.showToast(InitApp.AppContext,message);
                    String[] split = message.split("x");
                    makeARect(isClickArea,split);
                    camera_area_iv.setVisibility(View.VISIBLE);
                    partb_status_tv.setText("设置区域拍照成功");
                    break;
                }
                case Constant.AREA_DELETE_SUCCESS:{
                    Bundle bundle=msg.getData();
                    String message=bundle.getString("msg");
                    ToastUtil.showToast(InitApp.AppContext,message);
                    camera_area_iv.setVisibility(View.GONE);
                    partb_status_tv.setText("取消区域拍照成功");
                    break;
                }
                case Constant.RESPONSE_MESSAGE:{
                    Bundle bundle=msg.getData();
                    String message=bundle.getString("msg");
                    ToastUtil.showToast(InitApp.AppContext,message);
                    partb_status_tv.setText("设置参数成功");
                    break;
                }
                case Constant.RESPONSE_PARAMS:{
                    partb_status_tv.setText("正在设置参数");
                    Bundle bundle=msg.getData();
                    String message=bundle.getString("msg");
                    ToastUtil.showToast(InitApp.AppContext,"正在读取A端相机参数！");
                    DebugUtil.debug("A端相机参数为："+message);
                    //存放到SharePerfences文件中
                    String picture_size=JsonUtils.getString(message,"picture_size","640x480");
                    String flash_mode=JsonUtils.getString(message,"flash_mode","auto");
                    String focus_mode=JsonUtils.getString(message,"focus_mode","auto");
                    String white_balance=JsonUtils.getString(message,"white_balance","auto");
                    String exposure_compensation=JsonUtils.getString(message,"exposure_compensation","0");
                    String iso=JsonUtils.getString(message,"iso","auto");
                    String jpeg_quality=JsonUtils.getString(message,"jpeg_quality","100");
                    SharePreferencesUtil.setParam(ControllActivity.this,"picture_size",picture_size);
                    SharePreferencesUtil.setParam(ControllActivity.this,"flash_mode",flash_mode);
                    SharePreferencesUtil.setParam(ControllActivity.this,"focus_mode",focus_mode);
                    SharePreferencesUtil.setParam(ControllActivity.this,"white_balance",white_balance);
                    SharePreferencesUtil.setParam(ControllActivity.this,"exposure_compensation",exposure_compensation);
                    SharePreferencesUtil.setParam(ControllActivity.this,"iso",iso);
                    SharePreferencesUtil.setParam(ControllActivity.this,"jpeg_quality",jpeg_quality);
                    PartBSettingsFragment.init();
                    break;
                }

            }

        };
    };



    public static void launch(String flag) {
        InitApp.AppContext.startActivity(new Intent(InitApp.AppContext, ControllActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controll);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
        initCamera();
        //开启消息接收线程
        reveiveMessage(mHandler);
    }

    /**
     * 接收A端发送过来的消息
     * @param handler 接收到消息后，通过handler进行回调，来进行界面显示
     * @return
     */
    public boolean reveiveMessage(final Handler handler) {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(Constant.ServiceAddress, Constant.DEFAULT_PORT), 3000);
                        // 输入流，为了获取A端发送的消息
                        while (true){
                            InputStream in = socket.getInputStream();
                            DataInputStream dis=new DataInputStream(in);
                            int size=dis.readInt();
                            int flag=dis.readShort();
                            byte[] data=new byte[size];
                            int len=0;
                            while(len<size){
                                len+=dis.read(data,len,size-len);
                            }
                            if(!data.equals("")){
                                switch (flag){
                                    case Constant.RESPONSE_TOKEPHOTO:{
                                        Message msg = Message.obtain();
                                        Bundle bundle=new Bundle();
                                        bundle.putByteArray(Constant.TOKEPHOTO,data);
                                        msg.setData(bundle);
                                        msg.what = Constant.RESPONSE_TOKEPHOTO;
                                        handler.sendMessage(msg);
                                        break;
                                    }
                                    case Constant.AREA_TOKEPHOTO_SUCCESS:{
                                        Message msg = Message.obtain();
                                        Bundle bundle=new Bundle();
                                        bundle.putString("msg", StringUtils.byteArrayToStr(data));
                                        msg.setData(bundle);
                                        msg.what = Constant.AREA_TOKEPHOTO_SUCCESS;
                                        handler.sendMessage(msg);
                                        break;
                                    }
                                    case Constant.AREA_DELETE_SUCCESS:{
                                        Message msg = Message.obtain();
                                        Bundle bundle=new Bundle();
                                        bundle.putString("msg", StringUtils.byteArrayToStr(data));
                                        msg.setData(bundle);
                                        msg.what = Constant.AREA_DELETE_SUCCESS;
                                        handler.sendMessage(msg);
                                        break;
                                    }
                                    case Constant.RESPONSE_PARAMS:{
                                        Message msg = Message.obtain();
                                        Bundle bundle=new Bundle();
                                        bundle.putString("msg", StringUtils.byteArrayToStr(data));
                                        msg.setData(bundle);
                                        msg.what = Constant.RESPONSE_PARAMS;
                                        handler.sendMessage(msg);
                                        break;
                                    }
                                    case Constant.RESPONSE_MESSAGE:{
                                        Message msg = Message.obtain();
                                        Bundle bundle=new Bundle();
                                        bundle.putString("msg", StringUtils.byteArrayToStr(data));
                                        msg.setData(bundle);
                                        msg.what = Constant.RESPONSE_MESSAGE;
                                        handler.sendMessage(msg);
                                        break;
                                    }
                                }

                            }
                        }
                    } catch (IOException e) {
                        Message msg = Message.obtain();
                        msg.what = Constant.STATE_FROM_SERVER_ERROR;
                        handler.sendMessage(msg);
                    }

                }
            }).start();
        return true;
    }


    public void initView(){
        controll_take_btn=findViewById(R.id.controll_take_btn);
        controll_area_btn=findViewById(R.id.controll_area_btn);
        controll_setting_btn=findViewById(R.id.controll_setting_btn);
        partb_status_tv=findViewById(R.id.partb_status_tv);
        controll_iv=findViewById(R.id.controll_iv);
        camera_setting=findViewById(R.id.camera_setting);
        controll_focus_btn=findViewById(R.id.controll_focus_btn);
        camera_focus_tv=findViewById(R.id.camera_focus_tv);
        camera_area_iv=findViewById(R.id.camera_area_iv);
        partb_status_tv.setText("已连接");

        controll_take_btn.setOnClickListener(this);
        controll_area_btn.setOnClickListener(this);
        controll_setting_btn.setOnClickListener(this);
        controll_iv.setOnClickListener(this);
        controll_focus_btn.setOnClickListener(this);
    }

    /**
     * 主要用来初始化B端的参数设置页面的初始化
     */
    public void initCamera(){
        PartBSettingsFragment.passCamera(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences_b, false);
    }

    /**
     * B端进行参数设置后，用来给A端发送相应的指令
     * @param flag 给A端的指令
     * @param value 设置的详细信息
     */
    @Override
    public void updateSetting(int flag,String value){
        //这里通过B端设置了某些属性，然后这里需要上传给A端，自己来设置属性
        new SendThread(socket,flag,value).start();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.controll_take_btn:{
                DialogUtil.getInstance().tokeAPhoto(ControllActivity.this);
                if(isClickSetting){
                    isClickSetting=false;
                    getFragmentManager().beginTransaction().hide(settingFragment
                    ).commit();
                    camera_setting.setVisibility(View.GONE);
                }
                camera_area_iv.setVisibility(View.GONE);
                camera_focus_tv.setVisibility(View.GONE);
                new SendThread(socket,Constant.TOKEPHONTFLAG,"接收到B端拍照请求！").start();
                break;
            }
            case R.id.controll_area_btn:{
                if(isClickSetting){
                    isClickSetting=false;
                    getFragmentManager().beginTransaction().hide(settingFragment
                    ).commit();
                    camera_setting.setVisibility(View.GONE);
                }
                camera_focus_tv.setVisibility(View.GONE);
                if(!isClickArea){
                    isClickArea=true;
                    camera_area_iv.setVisibility(View.VISIBLE);
                    new SendThread(socket,Constant.AREAFLAG,"接收到B端区域拍照请求！").start();
                }else{
                    isClickArea=false;
                    camera_area_iv.setVisibility(View.GONE);
                    new SendThread(socket,Constant.AREAFLAG,"接收到B端取消区域拍照请求！").start();
                }

                break;
            }
            case R.id.controll_focus_btn:{
                if(isClickSetting){
                    isClickSetting=false;
                    getFragmentManager().beginTransaction().hide(settingFragment
                    ).commit();
                    camera_setting.setVisibility(View.GONE);
                }
                camera_area_iv.setVisibility(View.GONE);
                if(!isClickFocus){
                    isClickFocus=true;
                    ToastUtil.showToast(this,"触摸下方视图中某一处进行对焦！");
                    camera_focus_tv.setVisibility(View.VISIBLE);
                    camera_focus_tv.setOnTouchListener(new FocusOnTouchListener());
                }else{
                    isClickFocus=false;
                    camera_focus_tv.setVisibility(View.GONE);
                }
                break;
            }
            case R.id.controll_setting_btn:{
                if(!isClickSetting){
                    isClickSetting=true;
                    settingFragment=new PartBSettingsFragment();
                    getFragmentManager().beginTransaction().replace(R.id.camera_setting,
                            settingFragment).commit();
                    PartBSettingsFragment.init();
                    camera_setting.setVisibility(View.VISIBLE);//fix 抖动问题
                    //请求A端相机的参数
                    new SendThread(socket,Constant.PARAMSFLAG,"接收到B端参数设置请求！").start();

                }else{
                    isClickSetting=false;
                    getFragmentManager().beginTransaction().hide(settingFragment
                    ).commit();
                    camera_setting.setVisibility(View.GONE);
                }
                break;
            }
            case R.id.controll_iv: {
                PicturesAll.launch("pictures");
                break;
            }
        }
    }

    /**
     * 触摸对焦的一个监听器，触摸结束返回对焦相对坐标点
     */
    class FocusOnTouchListener implements View.OnTouchListener  {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch(event.getAction()){

                case MotionEvent.ACTION_DOWN:
                    break;

                case MotionEvent.ACTION_MOVE:

                    break;

                case MotionEvent.ACTION_UP:
                    Point point=getFocusWidthAndHeight();
                    int focus_width=point.x;
                    int focus_height=point.y;
                    //获取触摸点
                    float focus_x=event.getX();
                    float focus_y=event.getY();
                    //计算相对比例
                    float ratio_x=focus_x/focus_width;
                    float ratio_y=focus_y/focus_height;
                    ToastUtil.showToast(ControllActivity.this,"点击的相对坐标为：\n("
                            +ratio_x+","+ratio_y+")");
                    //发送对焦的请求给A端
                    new SendThread(socket,Constant.FOUCSFALG,ratio_x+"x"+ratio_y).start();
                    break;
            }
            return true;
        }
    }

    /**
     * 接收到A端的请求后，根据比例B端自己创建一个对应的矩形
     * @param isRectPhoto 是否已经是区域拍照
     */
    public void makeARect(boolean isRectPhoto,String[] split){
        float r1x=Float.parseFloat(split[0]);
        float r1y=Float.parseFloat(split[1]);
        float r2x=Float.parseFloat(split[2]);
        float r2y=Float.parseFloat(split[3]);
        int max_widht=DensityUtil.getScreenWidth(InitApp.AppContext)
                -2*DensityUtil.dip2px(InitApp.AppContext,16);//最大的宽度
        int max_height=DensityUtil.getScreenHeight(InitApp.AppContext)-700;//最大的高度

        int x1 = (int)(max_widht*r1x);
        int y1 = (int)(max_height*r1y);
        int x2 = (int)(max_widht*r2x);
        int y2 = (int)(max_height*r2y);
        if( camera_area_iv!= null){
            DebugUtil.debug("x1="+x1+" y1="+y1+" x2="+x2+" y2="+y2);
            Rect screenCenterRect = new Rect(x1, y1, x2, y2);
            camera_area_iv.setCenterRect(screenCenterRect);
            camera_area_iv.setSocket(socket);
        }
        camera_area_iv.setVisibility(View.VISIBLE);
    }

    /**
     * 获取触摸对焦视图的宽和高，返回到一个Point中
     * @return
     */
    private Point getFocusWidthAndHeight(){
        int width=camera_focus_tv.getWidth();
        int height=camera_focus_tv.getHeight();
        return new Point(width,height);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 返回图片文件类型
     * @return 保存图片的文件
     */
    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), CameraPreview.TAG);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

        outputMediaFileUri = Uri.fromFile(mediaFile);
        DebugUtil.debug(outputMediaFileUri.toString());

        return mediaFile;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
