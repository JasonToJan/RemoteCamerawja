package com.jason.remotecamera_wja;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.jason.remotecamera_wja.parta.PartAMain;
import com.jason.remotecamera_wja.partb.PartBMain;

/**
 * APP开始的主活动页面，这里可以选择进入A端或者B端
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button Abtn;
    private Button Bbtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    private void initView(){
        Abtn=findViewById(R.id.main_A_btn);
        Bbtn=findViewById(R.id.main_B_btn);


        Abtn.setOnClickListener(this);
        Bbtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.main_A_btn:
                PartAMain.launch("partA");
                //SocketA.launch("socketA");
                break;
            case R.id.main_B_btn:
                PartBMain.launch("partB");
                //SocketB.launch("socketB");
                break;

            default:
                break;
        }
    }

}
