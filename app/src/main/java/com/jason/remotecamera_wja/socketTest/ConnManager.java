package com.jason.remotecamera_wja.socketTest;

/**
 * Created by jasonjan on 2018/5/6.
 */

import android.os.Handler;
import android.os.Message;

import com.jason.remotecamera_wja.app.Constant;
import com.jason.remotecamera_wja.util.DebugUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * @描述 使用socket实现长连接
 */
public class ConnManager {

    protected static final int STATE_FROM_SERVER_OK = 0;
    private String dsName = Constant.ServiceAddress;
    private static int dstPort = Constant.DEFAULT_PORT;
    private Socket socket;

    private static ConnManager instance;

    private ConnManager() {
    }

    public static ConnManager getInstance() {
        if (instance == null) {
            synchronized (ConnManager.class) {
                if (instance == null) {
                    instance = new ConnManager();
                }
            }
        }
        return instance;
    }

    /**
     * 连接
     *
     * @return
     */
    public boolean connect(final Handler handler) {

        //从shared文件中获取host地址
        //dsName= (String)SharePreferencesUtil.getParam(InitApp.AppContext,"host",Constant.ServiceAddress);

        if (socket == null || socket.isClosed()) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(Constant.ServiceAddress, Constant.DEFAULT_PORT), 1000);
                        DebugUtil.debug("\ndsName="+dsName+"\ndstPort="+dstPort);
                        // 输入流，为了获取A端发送的消息
                        BufferedReader bff = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String line = "";
                        String data="";
                        while ((line = bff.readLine())!=null) {
                            data +=line;
                        }
                        Message msg = Message.obtain();
                        msg.obj = data;
                        msg.what = STATE_FROM_SERVER_OK;
                        handler.sendMessage(msg);
                    } catch (IOException e) {
                        throw new RuntimeException("错误: " + e.getMessage());
                    }

                }
            }).start();
        }

        return true;
    }

    /**
     * 连接
     *
     * @return
     */
    public void connect() {
        if (socket == null || socket.isClosed()) {
            try {
                // 输入流，为了获取客户端发送的数据
                socket = new Socket(dsName, dstPort);
            } catch (IOException e) {
                throw new RuntimeException("getInputStream错误: " + e.getMessage());
            }
        }
    }

    /**
     * 发送信息
     *
     * @param content
     */
    public void sendMessage(String content) {
        OutputStream os = null;
        try {
            if (socket != null) {
                os = socket.getOutputStream();
                os.write(content.getBytes());
                os.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("发送失败:" + e.getMessage());
        }
    }

    /**
     * 发送信息
     *
     * @param messageCode
     */
    public void sendCode(final String messageCode) {

        if(socket!=null&&!socket.isClosed()) {
            try{
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    //连接服务器 并设置连接超时为1秒
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(Constant.ServiceAddress, Constant.DEFAULT_PORT), 1000);
                    //获取输入输出流
                    OutputStream ou = socket.getOutputStream();
                    BufferedReader bff = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));
                    //向服务器发送信息
                    ou.write(messageCode.getBytes("utf-8"));
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
        }).start();
    }

    /**
     * 发送信息
     *
     * @param messageCode
     */
    public void sendCode2(final String messageCode) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //获取输入输出流
                    OutputStream ou = socket.getOutputStream();
                    //向服务器发送信息
                    ou.write(messageCode.getBytes("utf-8"));
                    //关闭各种输入输出流
                    ou.flush();
                } catch (SocketTimeoutException aa) {
                    aa.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 接收消息
     *
     * @param
     */
    public void receiveFromServer(final Handler handler) {

        if(socket!=null&&!socket.isClosed()) {
            try{
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //连接服务器 并设置连接超时为1秒
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(Constant.ServiceAddress, Constant.DEFAULT_PORT), 1000);
                    //获取输入输出流

                    InputStream is = socket.getInputStream();
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    while ((len = is.read(buffer)) != -1) {
                        final String result = new String(buffer, 0, len);

                        Message msg = Message.obtain();
                        msg.obj = result;
                        msg.what = STATE_FROM_SERVER_OK;
                        handler.sendMessage(msg);
                    }
                    is.close();
                    socket.close();
                } catch (SocketTimeoutException aa) {
                    aa.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 关闭连接
     */
    public void disClose() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException("关闭异常:" + e.getMessage());
            }
        }
    }

    public  interface  ConnectionListener{
        void pushData(String str);
    }

    private ConnectionListener mListener;

    public void setConnectionListener(ConnectionListener listener){
        this.mListener = listener;
    }
}
