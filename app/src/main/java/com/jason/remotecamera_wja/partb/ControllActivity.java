package com.jason.remotecamera_wja.partb;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.R;
import com.jason.remotecamera_wja.app.Constant;
import com.jason.remotecamera_wja.socketTest.ConnManager;
import com.jason.remotecamera_wja.util.DialogUtil;
import com.jason.remotecamera_wja.util.PerfectClickListener;
import com.jason.remotecamera_wja.util.ToastUtil;

public class ControllActivity extends AppCompatActivity {

    protected static final int STATE_FROM_SERVER_OK = 0;
    public Button controll_take_btn;
    public Button controll_params_btn;
    public Button controll_pictures_btn;
    public static TextView partb_status_tv;
    public boolean socketSuccess=false;

    private ConnManager mConnManager;

    public static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==STATE_FROM_SERVER_OK) {
                partb_status_tv.setText("正在控制中");
                ToastUtil.showToast(InitApp.AppContext,msg.obj.toString());
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
        mConnManager = ConnManager.getInstance();
        //mConnManager.connect(mHandler);//B端连接
        setListener();

    }


    public void initView(){
        controll_take_btn=findViewById(R.id.controll_take_btn);
        controll_params_btn=findViewById(R.id.controll_params_btn);
        controll_pictures_btn=findViewById(R.id.controll_pictures_btn);
        partb_status_tv=findViewById(R.id.partb_status_tv);
    }

    public void setListener(){
        controll_take_btn.setOnClickListener(new PerfectClickListener(){
            @Override
            protected void onNoDoubleClick(View v) {
                DialogUtil.getInstance().tokeAPhoto(ControllActivity.this);
                mConnManager.sendCode(Constant.TOKEPHOTO);
                //mConnManager.receiveFromServer(mHandler);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtil.getInstance().closeDialog();
                    }
                },1000);
            }
        });
        controll_params_btn.setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                mConnManager.sendCode("B端点击了参数设置！");
            }
        });
        controll_pictures_btn.setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                mConnManager.sendCode("B端点击了相册！");
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
}
