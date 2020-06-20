package cn.edu.scut.airgallery.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import cn.edu.scut.airgallery.R;
import cn.edu.scut.airgallery.data.Album;
import cn.edu.scut.airgallery.ui.albums.AlbumsFragment;
import cn.edu.scut.airgallery.util.PermissionUtils;

public class MainActivity extends AppCompatActivity implements AlbumsFragment.AlbumClickListener {

    private AppBarConfiguration mAppBarConfiguration;
    private final int EXTERNAL_STORAGE_PERMISSIONS = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        //设置浮动按钮
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(view -> Snackbar.make(view, "Coming soon!", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show());

        if (PermissionUtils.isStoragePermissionsGranted(this)) {
        } else{
            PermissionUtils.requestPermissions(this, EXTERNAL_STORAGE_PERMISSIONS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_albums, R.id.nav_receive, R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.content);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_PERMISSIONS:
                boolean gotPermission = grantResults.length > 0;
                for (int result : grantResults) {
                    gotPermission &= result == PackageManager.PERMISSION_GRANTED;
                }
                if (gotPermission) {
                    Toast.makeText(this, "请点击“本地目录”标签以刷新", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "app需要读取权限才能正常运行哦", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.content);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onAlbumClick(Album album) {
        Intent intent = new Intent(getApplicationContext(), RvMediaActivity.class);
        intent.putExtra(RvMediaActivity.BUNDLE_ALBUM, album);
        startActivity(intent);
    }
}
