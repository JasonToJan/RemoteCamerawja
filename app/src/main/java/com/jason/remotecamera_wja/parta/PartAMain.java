package com.jason.remotecamera_wja.parta;

import android.Manifest;
import android.app.Activity;
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
import com.jason.remotecamera_wja.camera.CameraActivity;
import com.jason.remotecamera_wja.parta.pictures.PicturesAll;
import com.jason.remotecamera_wja.util.AppUtil;

public class PartAMain extends AppCompatActivity implements View.OnClickListener{

    private Button parta_photo_btn;
    private Button parta_picture_btn;
    private AlertDialog mPermissionDialog;

    public static void launch(String flag) {
        InitApp.AppContext.startActivity(new Intent(InitApp.AppContext, PartAMain.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part_amain);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
    }

    public void initView(){
        parta_photo_btn=findViewById(R.id.parta_photo_btn);
        parta_picture_btn=findViewById(R.id.parta_picture_btn);

        parta_photo_btn.setOnClickListener(this);
        parta_picture_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.parta_photo_btn:
                checkPermission(1);
                break;
            case R.id.parta_picture_btn:
                checkPermission(2);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkPermission(int flag){
        switch (flag){
            case 1:
                if (ContextCompat.checkSelfPermission(PartAMain.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        &&ContextCompat.checkSelfPermission(PartAMain.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //同时询问两个权限
                    ActivityCompat.requestPermissions((Activity) PartAMain.this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, flag);

                } else {
                   CameraActivity.launch("拍照");
                }
                break;
            case 2:
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    //权限还没有授予，需要在这里写申请权限的代码
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                }else {
                    //权限已经被授予，在这里直接写要执行的相应方法即可
                    PicturesAll.launch("相册");
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                for(int i=0;i<permissions.length;i++){
                    if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(PartAMain.this, permissions[0]);
                        if (showRequestPermission) {
                            checkPermission(1);
                            return;
                        } else {
                            showPermissionDialog();
                        }
                    }else if(i==permissions.length){
                        CameraActivity.launch("拍照");
                    }
                }
                break;
            case 2:
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    //判断是否勾选禁止后不再询问
                    boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(PartAMain.this, permissions[0]);
                    if (showRequestPermission) {
                        checkPermission(2);
                        return;
                    } else {
                        showPermissionDialog();
                    }
                }else{
                    PicturesAll.launch("相册");
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
