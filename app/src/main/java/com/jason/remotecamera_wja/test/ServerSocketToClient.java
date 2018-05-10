package com.jason.remotecamera_wja.test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by jasonjan on 2018/5/6.
 */

public class ServerSocketToClient {

    public static void byteToClient(final byte[] data,final Socket socket) {
        if(socket!=null&&!socket.isClosed()){
            try{
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        new Thread(){
            OutputStream output;
            public void run(){
                try{
                    output = socket.getOutputStream();
                    output.write(data);
                    output.flush();
                    output.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
