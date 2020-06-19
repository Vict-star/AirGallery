package cn.edu.scut.airgallery.ui.receive;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.edu.scut.airgallery.R;
import cn.edu.scut.airgallery.transfer.Server;


public class ReceiveFragment extends Fragment {

    @BindView(R.id.tvMsg)
    TextView tvMsg;
    @BindView(R.id.txtPort)
    EditText txtPort;
    @BindView(R.id.et)
    EditText txtEt;
    @BindView(R.id.btnReceive)
    Button btnReceive;

    private int port;
    private Handler handler;
    private String SaveFolder = "/sdcard/Shared/";;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case 0:
                        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
                        txtEt.append("\n[" + format.format(new Date()) + "]" + msg.obj.toString());
                        break;
                    case 1:
                        tvMsg.setText("本机IP：" + GetIpAddress() + " 口令:" + msg.obj.toString());
                        Toast.makeText(getContext(), "将口令"+ msg.obj.toString()+"告诉对方吧！", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(getContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_receive, container, false);
        ButterKnife.bind(this, root);
        createMkdir(SaveFolder);
        btnReceive.setOnClickListener(v -> {
            port = Integer.parseInt(txtPort.getText().toString());
            Message.obtain(handler, 1, port).sendToTarget();
            Server.startServer(port, Environment.getExternalStorageDirectory().getPath()+"/Shared/",handler);
        });
        tvMsg.setText("本机IP：" + GetIpAddress());

        return root;
    }

    public String GetIpAddress() {
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int i = wifiInfo.getIpAddress();
        return (i & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF)+ "." +
                ((i >> 24 ) & 0xFF );
    }

    public static void createMkdir(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }
}
