package com.jason.remotecamera_wja.socketTest;

import android.os.Handler;
import android.os.Message;

import com.jason.remotecamera_wja.app.Constant;
import com.jason.remotecamera_wja.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;


//接收消息线程
public class ReceiveThread extends Thread {

    private Socket socket;
    private Handler handler;
    private int STATE_FROM_SERVER_OK=0;

    public ReceiveThread(Socket socket, Handler handler){
        this.socket=socket;
        this.handler=handler;
    }
    @Override
    public void run() {

        while (true) {
            try {
                String flag = "";
                if (socket == null || socket.isClosed()){
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(Constant.ServiceAddress, Constant.DEFAULT_PORT), 3000);
                }
                InputStream in = socket.getInputStream();
                byte[] bytes = new byte[in.available()];
                in.read(bytes);
                //读出B端下发的指令
                flag = StringUtils.byteArrayToStr(bytes);
                if (flag != null && !flag.equals("")) {
                    Message msg = Message.obtain();
                    msg.obj = flag;
                    msg.what = STATE_FROM_SERVER_OK;
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
