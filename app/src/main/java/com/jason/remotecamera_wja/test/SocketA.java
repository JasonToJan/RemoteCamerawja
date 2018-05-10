package com.jason.remotecamera_wja.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jason.remotecamera_wja.InitApp;
import com.jason.remotecamera_wja.R;
import com.jason.remotecamera_wja.app.Constant;
import com.jason.remotecamera_wja.util.NetworkUtil;
import com.jason.remotecamera_wja.util.ToastUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class SocketA extends AppCompatActivity {

    public static ServerSocket serverSocket = null;
    public static TextView mTextView, textView1;
    private String IP = "";
    String buffer = "";

    public static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==0x11) {
                Bundle bundle = msg.getData();
                 mTextView.setText(bundle.getString("msg"));
                ToastUtil.showToast(InitApp.AppContext,mTextView.getText().toString());
            }
        };
    };

    public static void launch(String flag) {
        InitApp.AppContext.startActivity(new Intent(InitApp.AppContext, SocketA.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        mTextView = (TextView) findViewById(R.id.textsss);
        textView1 = (TextView) findViewById(R.id.textView1);
        IP = NetworkUtil.getlocalip();
        textView1.setText("IP addresss:"+IP);
        new Thread() {
            public void run() {
                Bundle bundle = new Bundle();
                bundle.clear();
                OutputStream output;
                String str = "hello hehe";
                try {
                    serverSocket = new ServerSocket(Constant.DEFAULT_PORT);
                    while (true) {
                        Message msg = new Message();
                        msg.what = 0x11;
                        try {
                            Socket socket = serverSocket.accept();
                            output = socket.getOutputStream();
                            output.write(str.getBytes("utf-8"));
                            output.flush();
                            socket.shutdownOutput();
                            BufferedReader bff = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String line = "";
                            while ((line = bff.readLine())!=null) {
                                buffer = line + buffer;
                            }
                            bundle.putString("msg", buffer);
                            msg.setData(bundle);
                            mHandler.sendMessage(msg);
                            bff.close();
                            output.close();
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }.start();
    }

}
