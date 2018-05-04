package com.jason.remotecamera_wja.camera;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.R;
import com.jason.remotecamera_wja.parta.pictures.PicturesAll;
import com.jason.remotecamera_wja.util.DensityUtil;
import com.jason.remotecamera_wja.util.DisplayUtil;
import com.jason.remotecamera_wja.view.RectImageView;


public class CameraActivity extends Activity {

    private static final String TAG = "CameraActivity";
    private final static int PHOTO_REQUEST_TAKEPHOTO=101;
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
    int DST_CENTER_RECT_WIDTH = 200;
    int DST_CENTER_RECT_HEIGHT = 200;
    Point rectPictureSize = null;
    private boolean isRectPhoto=false;

    public static void launch(String flag) {
        InitApp.AppContext.startActivity(new Intent(InitApp.AppContext, CameraActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initCamera();
        initView();
        initViewParams();
        setListener();

    }

    public void initView(){
        settings = findViewById(R.id.button_settings);
        takePhoto=findViewById(R.id.button_tokephoto);
        priviewPhoto=findViewById(R.id.photo_preview);
        rectPhoto=findViewById(R.id.button_rectphoto);
        rectImageView=findViewById(R.id.camera_rect);
        camera_menu=findViewById(R.id.camera_menu);
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
                    /*if(rectPictureSize == null){
                        rectPictureSize = createCenterPictureRect(DisplayUtil.dip2px(CameraActivity.this, DST_CENTER_RECT_WIDTH)
                                ,DisplayUtil.dip2px(CameraActivity.this, DST_CENTER_RECT_HEIGHT));
                    }*/
                    int rect_x=rectImageView.getmFristPointX();
                    int rect_y=rectImageView.getmFristPointY();

                    mPreview.takePicture(priviewPhoto,
                            rect_x, rect_y,
                            DensityUtil.dip2px(CameraActivity.this,RectImageView.RECTRADIU),
                            DensityUtil.dip2px(CameraActivity.this,RectImageView.RECTRADIU));
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

    public void makeARect(boolean isRectPhoto){
        if(rectImageView != null){
            Rect screenCenterRect = createCenterScreenRect(DisplayUtil.dip2px(this, DST_CENTER_RECT_WIDTH)
                    ,DisplayUtil.dip2px(this, DST_CENTER_RECT_HEIGHT));
            rectImageView.setCenterRect(screenCenterRect);
        }
        if(isRectPhoto){
            rectImageView.setVisibility(View.VISIBLE);
        }else{
            rectImageView.setVisibility(View.GONE);
        }

    }

    private Point createCenterPictureRect(int w, int h){

        int wScreen = DisplayUtil.getScreenMetrics(this).x;
        int hScreen = DisplayUtil.getScreenMetrics(this).y;
        int wSavePicture = mPreview.doGetPrictureSize().y;
        int hSavePicture = mPreview.doGetPrictureSize().x;
        float wRate = (float)(wSavePicture) / (float)(wScreen);
        float hRate = (float)(hSavePicture) / (float)(hScreen);
        float rate = (wRate <= hRate) ? wRate : hRate;

        int wRectPicture = (int)( w * wRate);
        int hRectPicture = (int)( h * hRate);
        return new Point(wRectPicture, hRectPicture);

    }

    private Rect createCenterScreenRect(int w, int h){
        int x1 = DisplayUtil.getScreenMetrics(this).x / 2 - w / 2;
        int y1 = DisplayUtil.getScreenMetrics(this).y / 2 - h / 2;
        int x2 = x1 + w;
        int y2 = y1 + h;
        return new Rect(x1, y1, x2, y2);
    }

    public void initCamera(){
        mPreview = new CameraPreview(this);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        SettingsFragment.passCamera(mPreview.getCameraInstance());
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SettingsFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this));
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
