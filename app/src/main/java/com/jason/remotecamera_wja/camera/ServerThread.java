package com.jason.remotecamera_wja.camera;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.jason.remotecamera_wja.app.Constant;
import com.jason.remotecamera_wja.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 服务器处理客户端会话的线程
 */
public class ServerThread implements Runnable{

    Socket socket = null;
    String flag;
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
                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                //处理客户端发来的数据
                doRead(in);
                doSomeThing(flag,handler);
                //发送数据回客户端
                //doWrite(out);
                if(flag.equals(Constant.TOKEPHOTO)){
                    //如果是拍照，需要传递本机相册给B端

                }
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

    public void doSomeThing(String flag,Handler handler){
        Message msg = new Message();
        msg.what = 0x11;
        Bundle bundle=new Bundle();
        if(flag!=null&&!flag.equals("")){
            bundle.putString("msg", flag);
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
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            //读出B端下发的指令
            flag= StringUtil.byteArrayToStr(bytes);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public  boolean doWrite(OutputStream out,byte[] data){
        //引用关系，不要在此处关闭流
        try {
            out.write(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

}
