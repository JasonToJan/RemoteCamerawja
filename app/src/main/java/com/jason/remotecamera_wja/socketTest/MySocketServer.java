package com.jason.remotecamera_wja.socketTest;

/**
 * Created by jasonjan on 2018/5/6.
 */

import com.jason.remotecamera_wja.app.Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天室服务端
 *
 * java.net.ServerSocket
 * ServerSocket是运行在服务端的，其作用是向系统申请服务端端口，以便监听该端口，等待客户端的连接，
 * 一旦一个客户端连接，就会创建一个Socket与该客户端进行通信。
 * @author Administrator
 *
 */
public class MySocketServer {

    private ServerSocket server;//ServerSocket对象用于监听来自客户端的Socket连接，
    //ServerSocket包含一个 监听来自客户端连接请求的方法
    //存放所有客户端输出流的集合，用于广播信息
    private List<PrintWriter> allOut;
    public  MySocketServer(){

        try {
            allOut=new ArrayList<PrintWriter>();
            /*
             * 初始化ServerSocket的同时需要指定服务端口，该端口不能与当前系统使用TCP协议的
             * 其他程序申请的端口冲突，否则会抛出端口被占用异常
             */
            server = new ServerSocket(Constant.DEFAULT_PORT);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private synchronized void addOut(PrintWriter pw){
        allOut.add(pw);
    }
    private synchronized void removeOut(PrintWriter pw){
        allOut.remove(pw);
    }
    private synchronized void sendMessageToAllClient(String m){
        for(PrintWriter pw:allOut){
            pw.println(m);
        }
    }

    //服务端开始工作的方法
    public void start(){
        try {

            /*
             * Socket accept()
             * ServerSocket提供该方法用来监听打开服端口(8088),该方法是一个阻塞方法，
             * 直到一个客户端尝试连接才会解除阻塞，并创建一个Socket与刚连接的客户端进行通讯。
             *
             * accept方法每次调用都会等待一个客户端的连接，所以若希望让若干个客户端连接，就需要多次
             * 调用该方法，来分别获取对应这些客户端
             * 的socket与他们通讯。
             */

            while(true){
                //System.out.println("等待客户端连接……");
                Socket socket=server.accept();
                //System.out.println("一个客户端连接了！");
                /**
                 * 当一个客户端连接后，启动一个线程，来负责与该客户端交互
                 */
                ClientHandler handler=new ClientHandler(socket);
                new Thread(handler).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        MySocketServer server=new MySocketServer();
        server.start();
    }

    /**
     *
     * 该线程用来与一个指定的客户端进行交互
     * 每当一个客户端连接服务端后，都会启动当前线程来负责与之交互工作。
     */
    private class ClientHandler implements Runnable{
        private Socket  socket;

        //客户端地址信息
        private String host;
        public byte[] data;

        public ClientHandler(Socket socket){
            this.socket=socket;
            //通过socket可以得知远端计算机信息
            InetAddress address=socket.getInetAddress();
            host=address.getHostAddress();
        }
        public void run(){
            PrintWriter pw=null;
            try {
                /*
                 * 通过客户端的Socket获取输出流，以便将消息发送给客户端
                 */
                OutputStream out=socket.getOutputStream();
                OutputStreamWriter osw=new OutputStreamWriter(out,"UTF-8");
                pw=new PrintWriter(osw,true);

                //共享该客户端的输出流
                addOut(pw);

                //广播该用户上线
                sendMessageToAllClient("");

                /*
                 * InputStream getInputStream()
                 * Socket提供的该方法用来获取输入流，读取远端计算机发送过来的数据
                 */
                InputStream in=socket.getInputStream();
                InputStreamReader isr=new InputStreamReader(in,"utf-8");
                BufferedReader br=new BufferedReader(isr);

                String message=null;
                    /*
                     * 当我们使用BufferedReader读取来自远端计算机发送过来的内容时，由于远端计算机的操作系统不同，
                     * 当他们断开连接时，这里readline方法的结果也不同：
                     * 当远端计算机操作系统是Windows时，若断开连接，这里的readline方法直接会抛出异常。
                     * 当远端计算机操作系统是Linux时，若断开连接，这里的readline方法返回NULL。
                     */
               /* while((message=br.readLine())!=null){
                    sendMessageToAllClient(host+"说："+message);
                }*/


            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                /*
                 * 当该客户端与服务端断开时，应当将该客户端的输出流从共享集合删除。
                 */
                allOut.remove(pw);

                /**
                 * 无论是Linux的客户端还是Windows的客户端，当与服务器断开连
                 * 接后，都应当将与该客户端交互的 socket关闭，来释放底层资源。
                 */
                if(socket!=null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
