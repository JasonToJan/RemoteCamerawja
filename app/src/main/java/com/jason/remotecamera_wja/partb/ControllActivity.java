package com.jason.remotecamera_wja.partb;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.R;
import com.jason.remotecamera_wja.app.Constant;
import com.jason.remotecamera_wja.pictures.PicturesAll;
import com.jason.remotecamera_wja.util.DebugUtil;
import com.jason.remotecamera_wja.util.DialogUtil;
import com.jason.remotecamera_wja.util.JsonUtils;
import com.jason.remotecamera_wja.util.PerfectClickListener;
import com.jason.remotecamera_wja.util.SharePreferencesUtil;
import com.jason.remotecamera_wja.util.StringUtils;
import com.jason.remotecamera_wja.util.ToastUtil;

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

public class ControllActivity extends AppCompatActivity implements PartBSettingsFragment.UpdateListener{

    public static final String TAG = "CameraPreview";
    public static final int MEDIA_TYPE_IMAGE=1;
    public static final int TEMP_IMAGE=2;
    private boolean isClickSetting=false;
    private Fragment settingFragment;
    private Uri outputMediaFileUri;
    private String outputMediaFileType;
    protected static final int STATE_FROM_SERVER_OK = 0;
    protected static final int STATE_FROM_SERVER_ERROR=1;
    public Button controll_take_btn;
    public Button controll_area_btn;
    public Button controll_setting_btn;
    public FrameLayout camera_setting;
    public static TextView partb_status_tv;
    public ImageView controll_iv;
    public Socket socket;


    public  Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case STATE_FROM_SERVER_OK:{
                    partb_status_tv.setText("正在控制拍照");
                    Bundle bundle=msg.getData();
                    byte[] data= bundle.getByteArray(Constant.TOKEPHOTO);
                    //ToastUtil.showToast(InitApp.AppContext,"获取的图片长度为："+data.length);
                    /*bmp.compress(Bitmap.CompressFormat.JPEG, 50, outPut);*/
                    File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
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
                case STATE_FROM_SERVER_ERROR:{
                    ToastUtil.showToast(InitApp.AppContext,"A端未创建或被异常关闭!");
                    finish();
                    break;
                }
                case Constant.AREA_TOKEPHOTO_SUCCESS:{
                    Bundle bundle=msg.getData();
                    String message=bundle.getString("msg");
                    ToastUtil.showToast(InitApp.AppContext,message);
                    partb_status_tv.setText(message);
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
                    String jpeg_quality=JsonUtils.getString(message,"jpeg_quality","100");
                    SharePreferencesUtil.setParam(ControllActivity.this,"picture_size",picture_size);
                    SharePreferencesUtil.setParam(ControllActivity.this,"flash_mode",flash_mode);
                    SharePreferencesUtil.setParam(ControllActivity.this,"focus_mode",focus_mode);
                    SharePreferencesUtil.setParam(ControllActivity.this,"white_balance",white_balance);
                    SharePreferencesUtil.setParam(ControllActivity.this,"exposure_compensation",exposure_compensation);
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
        setListener();

    }

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
                                        msg.what = STATE_FROM_SERVER_OK;
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
                                    case Constant.RESPONSE_PARAMS:{
                                        Message msg = Message.obtain();
                                        Bundle bundle=new Bundle();
                                        bundle.putString("msg", StringUtils.byteArrayToStr(data));
                                        msg.setData(bundle);
                                        msg.what = Constant.RESPONSE_PARAMS;
                                        handler.sendMessage(msg);
                                        break;
                                    }
                                }

                            }
                        }
                    } catch (IOException e) {
                        Message msg = Message.obtain();
                        msg.what = STATE_FROM_SERVER_ERROR;
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
        partb_status_tv.setText("已连接");
    }

    public void initCamera(){

        PartBSettingsFragment.passCamera(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences_b, false);
        //PartBSettingsFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this));
        //PartBSettingsFragment.init(PreferenceManager.getDefaultSharedPreferences(this));
    }

    @Override
    public void updateSetting(int flag,String value){
        //这里通过B端设置了某些属性，然后这里需要上传给A端，自己来设置属性
        new SendThread(socket,flag,value).start();
    }

    public void setListener(){
        controll_take_btn.setOnClickListener(new PerfectClickListener(){
            @Override
            protected void onNoDoubleClick(View v) {
                DialogUtil.getInstance().tokeAPhoto(ControllActivity.this);
                if(isClickSetting){
                    isClickSetting=false;
                    getFragmentManager().beginTransaction().hide(settingFragment
                    ).commit();
                    camera_setting.setVisibility(View.GONE);
                }
                new SendThread(socket,Constant.TOKEPHONTFLAG,"接收到B端拍照请求！").start();
            }
        });
        controll_area_btn.setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                if(isClickSetting){
                    isClickSetting=false;
                    getFragmentManager().beginTransaction().hide(settingFragment
                    ).commit();
                    camera_setting.setVisibility(View.GONE);
                }
                new SendThread(socket,Constant.AREAFLAG,"接收到B端区域拍照请求！").start();
            }
        });
        controll_setting_btn.setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {

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
                
            }
        });

        controll_iv.setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                PicturesAll.launch("pictures");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
