package com.jason.remotecamera_wja.test;

import com.jason.remotecamera_wja.app.Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SendThread extends Thread{

    private Socket socket;
    private String message;
    public SendThread(Socket socket,String message){
        this.socket=socket;
        this.message=message;
    }
    @Override
    public void run(){
        /*try{
            if(socket==null||socket.isClosed()) socket = new Socket();
            socket.connect(new InetSocketAddress(Constant.ServiceAddress, Constant.DEFAULT_PORT), 3000);
        }catch (IOException e){
            e.printStackTrace();
        }
        while(true){
            try{
                if(message!=null&&!message.equals("")){
                    String send=message;
                    PrintWriter pw=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    pw.write(send);
                    pw.flush();
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally {
                try{
                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }*/
        try {
            //连接服务器 并设置连接超时为1秒
            socket = new Socket();
            socket.connect(new InetSocketAddress(Constant.ServiceAddress, Constant.DEFAULT_PORT), 1000);
            //获取输入输出流
            OutputStream ou = socket.getOutputStream();
            BufferedReader bff = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            //向服务器发送信息
            ou.write(message.getBytes("utf-8"));
            ou.flush();
            //关闭各种输入输出流
            bff.close();
            ou.close();
            socket.close();
        } catch (SocketTimeoutException aa) {
            aa.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
