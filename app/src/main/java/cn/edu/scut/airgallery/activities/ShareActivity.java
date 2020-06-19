package cn.edu.scut.airgallery.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.edu.scut.airgallery.R;
import cn.edu.scut.airgallery.transfer.Client;
import cn.edu.scut.airgallery.transfer.PermissionUtils;
import cn.edu.scut.airgallery.transfer.ScanDeviceTool;
import static cn.edu.scut.airgallery.transfer.Client.checkConnect;

public class ShareActivity extends AppCompatActivity {

    private final int EXTERNAL_STORAGE_PERMISSIONS = 12;
    public static final String ARGS_MEDIA_PATH = "args_media_path";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txtIP)
    EditText txtIP;
    @BindView(R.id.txtPort)
    EditText txtPort;
    @BindView(R.id.et)
    EditText txtEt;
    @BindView(R.id.btnScan)
    Button btnScan;
    @BindView(R.id.btnSend)
    Button btnSend;

    private Handler handler;
    private ScanDeviceTool scanDeviceTool;
    private String ip;
    private int port;
    private String media_path;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @SuppressLint({"HandlerLeak", "ResourceAsColor"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        media_path = getIntent().getStringExtra(ARGS_MEDIA_PATH);

        if (PermissionUtils.isStoragePermissionsGranted(this)) {
        } else
            PermissionUtils.requestPermissions(this, EXTERNAL_STORAGE_PERMISSIONS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

        btnScan.setOnClickListener(v -> ScanDevice());
        btnSend.setOnClickListener(v -> {
            final String ipAddress = txtIP.getText().toString();
            final int port = Integer.parseInt(txtPort.getText().toString());
            ArrayList<String> paths = new ArrayList<>();
            paths.add(media_path);
            Client client = new Client(ipAddress,port,handler);
            client.startSender(paths);
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case 0:
                        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
                        txtEt.append("\n[" + format.format(new Date()) + "]" + msg.obj.toString());
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        setIp(msg.obj.toString());
                        txtIP.setText(getIp());
                        break;
                }
            }
        };
        //ScanDevice();
    }

    private void ScanDevice() {
        txtEt.append(". . .");
        new Thread(){
            @Override
            public void run() {
                scanDeviceTool = new ScanDeviceTool();
                List<String> pList = scanDeviceTool.scan();
                if(pList != null && pList.size() >0) {
                    Message.obtain(handler, 0, "扫描成功，发现"+pList.size()+"个设备：").sendToTarget();
                    for (final String ip : pList) {/*TODO:如果找到连接就应该终止尝试的*/
                        Message.obtain(handler, 0, "尝试连接:"+ip+"……").sendToTarget();
                        /*TODO:连接逻辑*/
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Log.d("connect", "-------->connecting: "+ip);
                                    checkConnect(ip,port);
                                    Log.d("connect", "-------->connected: "+ip);
                                    Message.obtain(handler, 0, "连接成功，IP为"+ip+"的设备").sendToTarget();
                                    Message.obtain(handler, 3, ip).sendToTarget();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                    Message.obtain(handler, 0, "连接失败,请和对方在同一局域网环境下再次尝试!或手动输入对方的ip地址。").sendToTarget();
                }
            }
        }.start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}


