package cn.edu.scut.airgallery.transfer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server extends Thread {
    private static int ServerTime = 0;
    private static String folderPath;
    private ServerSocket serverSocket;
    private static int port;
    private Handler handler = null;

    public Server(int port, Handler handler) throws IOException {
        this.port = port;
        this.handler = handler;
        Log.v("Server", String.valueOf(port));
        serverSocket = new ServerSocket(port);
        if(ServerTime!=0){
            serverSocket.setSoTimeout(ServerTime*1000);
        }
    }

    public void run() {
        // 等待连接，连接上就建立线程接收文件
        Log.v("Server","========Waiting for client on port " +
                serverSocket.getLocalPort() + "...========");
        while(true) {
            try {
                // 等待连接，连接上后接收文件
                Socket server = serverSocket.accept();
                Log.v("Server","========Just connected to " + server.getRemoteSocketAddress()+"========");
                new Thread(new ReceiveFile(server),"thread1").start(); // 每接收到一个Socket就建立一个新的线程来处理它
                Log.v("Server","thread-----------------"+Thread.currentThread().getName());

            } catch (SocketTimeoutException s) {
                Log.v("Server","========Socket timed out!========");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    // 接收文件类
    class ReceiveFile implements Runnable{
        private DataInputStream dis;
        private FileOutputStream fos;
        private Socket server;

        public ReceiveFile(Socket server) {
            this.server = server;
        }

        @Override
        public void run() {
            // 文件传输
            try {
                dis = new DataInputStream(server.getInputStream());
                // 文件名和长度
                String fileName = dis.readUTF();
                long fileLength = dis.readLong();
                fos = new FileOutputStream(folderPath+fileName,false);


                Log.v("Server","======== Start receiving ========");
                Log.v("Server","filePath-----------------"+folderPath);
                Log.v("Server","fileName-----------------"+fileName);
                Log.v("Server","fileSize-----------------"+fileLength+" Byte");
                SendMessage(0, "正在接收:" + fileName+" 文件大小"+fileLength+" Byte");
                // 1024bytes为传输单位
                long times = (fileLength+1023)/1024;
                byte[] bytes = new byte[1024];
                int length = 0;
                int round = 1;
                while((length = dis.read(bytes, 0, bytes.length)) != -1) {
                    fos.write(bytes, 0, length);
                    // 进度显示
                    System.out.print("progress"+"-----------------"+(round*100/times)+"%\t");
                    round ++;
                    fos.flush();
                }

                Log.v("Server","\n======== received successfully ========\n\n");
                SendMessage(0, fileName + "接收完成");
                Log.v("Server","========Waiting for client on port " +
                        serverSocket.getLocalPort() + "...========");
            } catch (Exception e) {
                e.printStackTrace();
                SendMessage(0, "接收错误:\n" + e.getMessage());
            } finally {
                try {
                    if(fos != null)
                        fos.close();
                    if(dis != null)
                        dis.close();
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void SendMessage(int what, Object obj){
        if (handler != null){
            Message.obtain(handler, what, obj).sendToTarget();
        }
    }

    public static void startServer(int port,String savePath,Handler handler) {
        folderPath = savePath;
        ServerTime = 0;
        try {
            Thread t = new Server(port,handler);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}