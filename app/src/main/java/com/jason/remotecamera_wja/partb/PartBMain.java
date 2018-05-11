package com.jason.remotecamera_wja.partb;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.R;
import com.jason.remotecamera_wja.pictures.PicturesAll;
import com.jason.remotecamera_wja.util.AppUtil;
import com.jason.remotecamera_wja.util.PerfectClickListener;

/**
 * B端首页，可以选择控制A端或者进入B端的相册
 */
public class PartBMain extends AppCompatActivity{

    private static final String TAG = "PartBMain";
    private Button partb_controll_btn;//B端请求控制A端按钮
    private Button partb_picture_btn;//B端相册按钮
    private AlertDialog mPermissionDialog;//B端权限对话框

    public static void launch(String flag) {
        InitApp.AppContext.startActivity(new Intent(InitApp.AppContext, PartBMain.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part_bmain);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
        setListener();
    }

    public void initView(){
        partb_controll_btn=findViewById(R.id.partb_controll_btn);
        partb_picture_btn=findViewById(R.id.partb_picture_btn);
    }

    public void setListener(){
        partb_controll_btn.setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                checkPermission(1);
            }
        });

        partb_picture_btn.setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                checkPermission(2);
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

    /**
     * 检查权限，两个按钮检查不一样的权限，故用了flag来标识
     * @param flag
     */
    public void checkPermission(int flag){
        switch (flag){
            case 1:
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    //权限还没有授予，需要在这里写申请权限的代码
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }else {
                    //权限已经被授予，在这里直接写要执行的相应方法即可
                    ControllActivity.launch("请求控制中...");
                }
                break;
            case 2:
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    //权限还没有授予，需要在这里写申请权限的代码
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                }else {
                    //权限已经被授予，在这里直接写要执行的相应方法即可
                    PicturesAll.launch("pictures");
                }
                break;

        }
    }

    /**
     * 请求权限后的回调，根据用户选择进行相应的操作
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                for(int i=0;i<permissions.length;i++){
                    if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(PartBMain.this, permissions[0]);
                        if (showRequestPermission) {
                            checkPermission(1);
                            return;
                        } else {
                            showPermissionDialog();
                        }
                    }else if(i==permissions.length){
                        ControllActivity.launch("请求控制中...");
                    }
                }
                break;
            case 2:
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    //判断是否勾选禁止后不再询问
                    boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(PartBMain.this, permissions[0]);
                    if (showRequestPermission) {
                        checkPermission(2);
                        return;
                    } else {
                        showPermissionDialog();
                    }
                }else{
                    PicturesAll.launch("pictures");
                }
                break;
        }
    }

    /**
     * 不再提示权限 时的展示对话框
     */
    private void showPermissionDialog() {
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(this)
                    .setMessage("已禁用权限，请手动授予")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mPermissionDialog.cancel();
                            Uri packageURI = Uri.parse("package:" + AppUtil.getAppPackageName(InitApp.AppContext));
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mPermissionDialog.cancel();
                        }
                    })
                    .create();
        }
        mPermissionDialog.show();
    }

}
