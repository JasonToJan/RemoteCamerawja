package com.jason.remotecamera_wja.partb;

import com.jason.remotecamera_wja.util.StringUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SendThread extends Thread{

    private Socket socket;
    private String message;
    private int flag;
    public SendThread(Socket socket,int flag,String message){
        this.socket=socket;
        this.flag=flag;
        this.message=message;
    }
    @Override
    public void run(){

        try {

            OutputStream output = socket.getOutputStream();
            DataOutputStream dos=new DataOutputStream(output);
            dos.writeInt(StringUtils.strToByteArray(message).length);
            dos.writeShort(flag);
            dos.write(StringUtils.strToByteArray(message));

        } catch (SocketTimeoutException aa) {
            aa.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
