package com.jason.remotecamera_wja.camera;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.jason.remotecamera_wja.util.StringUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 服务器处理客户端会话的线程
 */
public class ServerThread implements Runnable{

    Socket socket = null;
    int flag;
    String message;
    Handler handler;


    public ServerThread(Socket socket, Handler handler){
        this.socket = socket;
        this.handler=handler;

    }

    @Override
    public void run() {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = socket.getInputStream();
            out  = socket.getOutputStream();
            //使用循环的方式，不停的与客户端交互会话
            while(true){

                //处理客户端发来的数据
                doRead(in);
                doSomeThing(flag,message,handler);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void doSomeThing(int flag,String message,Handler handler){

        if(message!=null){
            Message msg = new Message();
            Bundle bundle=new Bundle();
            msg.what = flag;
            bundle.putString("msg", message);
            msg.setData(bundle);
            handler.sendMessage(msg);//显示在界面上

        }
    }

    /**
     * 读取数据
     * @param in
     * @return
     */
    public boolean doRead(InputStream in){
        //引用关系，不要在此处关闭流
        try {
            //读出B端下发的指令
            DataInputStream dis=new DataInputStream(in);
            int size=dis.readInt();
            flag=dis.readShort();
            byte[] data=new byte[size];
            int len=0;
            while(len<size){
                len+=dis.read(data,len,size-len);
            }
            message= StringUtil.byteArrayToStr(data);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

}
