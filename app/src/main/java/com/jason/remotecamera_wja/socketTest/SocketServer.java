package com.jason.remotecamera_wja.socketTest;

import android.content.Context;
import android.os.Handler;

import com.jason.remotecamera_wja.app.Constant;
import com.jason.remotecamera_wja.util.ToastUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by jasonjan on 2018/5/6.
 */

public class SocketServer {
    /**
     * 用来保存不同的客户端
     */
    private static Map<String, Socket> mClients = new LinkedHashMap<>();

    public static void makeAServer(final Context context, final Handler handler) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //创建服务器
                    ServerSocket serverSocket=null;
                    try{
                        serverSocket = new ServerSocket(Constant.DEFAULT_PORT);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(context,"ServerSocket已经创建！");
                            }
                        });
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    while (true) {
                        //获取客户端的连接
                        final Socket socket;
                        try{
                            socket = serverSocket.accept();
                            //读取从客户端发送过来的数据
                            InputStream inputStream = socket.getInputStream();
                            byte[] buffer = new byte[1024];
                            int len = -1;
                            while ((len = inputStream.read(buffer)) != -1) {
                                String data = new String(buffer, 0, len);

                                //先认证客户端
                                if (data.startsWith("#")) {
                                    mClients.put(data, socket);
                                } else {
                                    //将数据发送给指定的客户端
                                    String[] split = data.split("#");
                                    Socket c = mClients.get("#" + split[0]);
                                    OutputStream outputStream = c.getOutputStream();
                                    outputStream.write(split[1].getBytes());

                                }
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
}
