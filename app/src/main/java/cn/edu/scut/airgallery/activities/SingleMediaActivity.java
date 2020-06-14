package cn.edu.scut.airgallery.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.edu.scut.airgallery.R;
import cn.edu.scut.airgallery.data.Album;
import cn.edu.scut.airgallery.data.Media;
import cn.edu.scut.airgallery.data.StorageHelper;
import cn.edu.scut.airgallery.util.StringUtils;
import cn.hzw.doodle.DoodleActivity;
import cn.hzw.doodle.DoodleParams;
import cn.hzw.doodle.DoodleView;

public class SingleMediaActivity extends AppCompatActivity {
    public static final String TAG = "SingleMediaActivity";

    public static final int REQ_CODE_DOODLE = 1000;

    public static final String EXTRA_ARGS_ALBUM = "args_album";
    public static final String EXTRA_ARGS_MEDIA = "args_media";
    public static final String EXTRA_ARGS_POSITION = "args_position";

    @BindView(R.id.PhotoPager_Layout)
    RelativeLayout activityBackground;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.button)
    Button button;

    private int position;

    private Album album;
    private ArrayList<Media> media;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_media);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        toolbar.setNavigationOnClickListener(v -> goBackToMedias());
        button.setOnClickListener(v -> doodle());

        loadAlbum(getIntent());
    }

    private void loadAlbum(Intent intent) {
        album = intent.getParcelableExtra(EXTRA_ARGS_ALBUM);
        position = intent.getIntExtra(EXTRA_ARGS_POSITION, 0);
        media = intent.getParcelableArrayListExtra(EXTRA_ARGS_MEDIA);
    }

    public void doodle() {
        DoodleParams params = initDoodelParams();
        DoodleActivity.startActivityForResult(SingleMediaActivity.this, params, REQ_CODE_DOODLE);
    }

    public DoodleParams initDoodelParams(){
        DoodleParams params = new DoodleParams();
        params.mImagePath = getCurrentMedia().getPath();
        params.mIsFullScreen = true;
        params.mPaintUnitSize = DoodleView.DEFAULT_SIZE;
        params.mPaintColor = Color.RED;
        params.mSupportScaleItem = true;
        return params;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_CODE_DOODLE:
                    final String imagePath = data.getStringExtra(DoodleActivity.KEY_IMAGE_PATH);
                    if (imagePath != null) {
                        try {
                            if (StorageHelper.copyFile(getApplicationContext(), new File(imagePath), new File(this.album.getPath()))) {
                                Toast.makeText(this, R.string.new_file_created, Toast.LENGTH_SHORT).show();
                                String cachePath = "/storage/emulated/0/DCIM/Doodle";
                                StorageHelper.deleteFilesInFolder(getApplicationContext(),new File(cachePath));
                            }
                        } catch (Exception e) {
                            Log.e("ERROS - Dooldle", imagePath, e);
                        }


                    } else
                        StringUtils.showToast(getApplicationContext(), "errori random");
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }
    }

    public void goBackToMedias() {
        startActivity(new Intent(getApplicationContext(), RvMediaActivity.class));
        finish();
    }

    public Media getCurrentMedia() {
        return media.get(position);
    }

}
