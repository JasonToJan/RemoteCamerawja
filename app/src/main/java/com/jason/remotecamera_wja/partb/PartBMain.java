package com.jason.remotecamera_wja.partb;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.R;
import com.jason.remotecamera_wja.parta.pictures.PicturesAll;
import com.jason.remotecamera_wja.util.PerfectClickListener;
import com.jason.remotecamera_wja.util.ToastUtil;

public class PartBMain extends AppCompatActivity{

    private Button partb_controll_btn;
    private Button partb_picture_btn;



    public static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==0x11) {
                Bundle bundle = msg.getData();
                ToastUtil.showToast(InitApp.AppContext,bundle.getString("msg"));
            }
        };
    };

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
                //DialogUtil.getInstance().showLoading(PartBMain.this);
                /*new Handler().postDelayed(new Runnable(){
                    public void run() {
                        DialogUtil.getInstance().closeDialog();

                    }
                }, 1000);*/
                ControllActivity.launch("请求控制中...");
            }
        });

        partb_picture_btn.setOnClickListener(new PerfectClickListener() {
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

}
