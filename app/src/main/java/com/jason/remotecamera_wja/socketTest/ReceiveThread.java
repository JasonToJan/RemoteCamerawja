package com.jason.remotecamera_wja.socketTest;

import android.util.Log;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import java.nio.CharBuffer;

//接收消息线程
public class ReceiveThread extends Thread {

    private Socket socket;

    public ReceiveThread(Socket socket){
        this.socket=socket;
    }
    @Override
    public void run(){
        while(true){
            try{
                Reader reader=new InputStreamReader(socket.getInputStream());
                CharBuffer charbuffer=CharBuffer.allocate(8192);
                int index=-1;
                while((index=reader.read(charbuffer))!=-1){
                    charbuffer.flip();//设置从0到刚刚读取到的位置
                    Log.d("测试Socket","client:"+charbuffer.toString());
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
