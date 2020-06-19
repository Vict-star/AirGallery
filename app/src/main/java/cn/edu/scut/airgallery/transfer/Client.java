package cn.edu.scut.airgallery.transfer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Client {
    private static Socket client;
    private String ip;
    private int port;
    private Handler handler = null;

    public Client(String ip,int port,Handler handler) {
        this.ip = ip;
        this.port = port;
        this.handler = handler;
    }

    public static void checkConnect(String ip,int port) throws IOException {
        client = new Socket(ip,port);
        client.close();
    }

    public void startSender(final ArrayList<String> filePath){
        Thread SenderThread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    for(String path:filePath){
                        Log.v("Client","Connecting to " + ip + " on port " + port);
                        client = new Socket(ip,port);
                        Log.v("Client","========Just connected to " + client.getRemoteSocketAddress()+"========");
                        sendFile(path);
                        client.close();
                    }
                    SendMessage(0, "所有文件发送完成……");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        SenderThread.start();
    }

    void SendMessage(int what, Object obj){
        if (handler != null){
            Message.obtain(handler, what, obj).sendToTarget();
        }
    }


    //向服务端传输文件
    public  void sendFile(String path) throws IOException {
        File file=new File(path);
        FileInputStream fis = null;
        DataOutputStream dos = null;
        try {
            Log.v("path",path);
            fis = new FileInputStream(file);
            //BufferedInputStream bi=new BufferedInputStream(new InputStreamReader(new FileInputStream(file),"GBK"));
            dos = new DataOutputStream(client.getOutputStream());//client.getOutputStream()
            Log.v("name",file.getName());
            dos.writeUTF(file.getName());
            dos.flush();
            dos.writeLong(file.length());
            dos.flush();
            long fileLength = file.length();
            Log.v("Client","fileSize-----------------"+fileLength+" Byte");
            // 开始传输文件
            Log.v("Client","======== Transfer begin ========");
            SendMessage(0, "正在发送:" + file.getName()+" 文件大小："+fileLength+" Byte");
            byte[] bytes = new byte[1024];
            int length = 0;
            long times = (fileLength+1023)/1024;
            int round = 1;
            while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                dos.write(bytes, 0, length);
                Log.v("Client","progress"+"-----------------"+(round*100/times)+"%\t");
                round ++;
                dos.flush();
            }
            Log.v("Client","\n======== Transfer success! ========");
            SendMessage(0, file.getName() + "发送完成");
        }catch(IOException e){
            e.printStackTrace();
            Log.v("Client","Client exception");
            SendMessage(0, "发送错误:\n" + e.getMessage());
        }finally{
            if(fis != null)
                fis.close();
            if(dos != null)
                dos.close();
        }
    }
}
