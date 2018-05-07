package com.jason.remotecamera_wja.socketTest;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SendThread extends Thread{

    private Socket socket;
    public SendThread(Socket socket){
        this.socket=socket;
    }
    @Override
    public void run(){
        while(true){
            try{
                Thread.sleep(1000);
                String send="<SOAP-ENV:Envelope>"+System.currentTimeMillis()+"</SOAP-ENV:Envelope>";
                PrintWriter pw=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                pw.write(send);
                pw.flush();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

}
