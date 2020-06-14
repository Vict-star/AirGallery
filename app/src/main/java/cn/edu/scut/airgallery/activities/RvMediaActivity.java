package cn.edu.scut.airgallery.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.edu.scut.airgallery.adapters.MediaAdapter;
import cn.edu.scut.airgallery.data.Album;
import cn.edu.scut.airgallery.data.Media;
import cn.edu.scut.airgallery.data.provider.CPHelper;
import cn.edu.scut.airgallery.interfaces.MediaClickListener;
import cn.edu.scut.airgallery.items.ActionsListener;
import cn.edu.scut.airgallery.R;
import cn.edu.scut.airgallery.util.Measure;
import cn.edu.scut.airgallery.views.GridSpacingItemDecoration;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RvMediaActivity extends AppCompatActivity implements ActionsListener, MediaClickListener{

    public static final String TAG = "RvMediaActivity";
    public static final String BUNDLE_ALBUM = "album";

    @BindView(R.id.medias) RecyclerView rv;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private MediaAdapter adapter;
    private GridSpacingItemDecoration spacingDecoration;
    private Album album;

    @Override
    public void onResume() {
        super.onResume();
        setUpColumns();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv_media);
        ButterKnife.bind(this);

        album = getIntent().getParcelableExtra(BUNDLE_ALBUM);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(album.getName());
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        //设置rv的item间隔
        int spanCount = columnsCount();
        spacingDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getApplicationContext()), true);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(spacingDecoration);
        rv.setLayoutManager(new GridLayoutManager(getApplicationContext(), spanCount));

        adapter = new MediaAdapter(getApplicationContext(), this);
        rv.setAdapter(adapter);

        displayMedias(album);
    }

    private void displayMedias(Album album) {
        adapter.clear();
        CPHelper.getMedia(getApplicationContext(), album)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        media -> adapter.add(media),
                        throwable -> {
                            throwable.printStackTrace();
                        });
    }

    public void setUpColumns() {
        int columnsCount = columnsCount();

        if (columnsCount != ((GridLayoutManager) rv.getLayoutManager()).getSpanCount()) {
            rv.removeItemDecoration(spacingDecoration);
            spacingDecoration = new GridSpacingItemDecoration(columnsCount, Measure.pxToDp(3, getApplicationContext()), true);
            rv.addItemDecoration(spacingDecoration);
            rv.setLayoutManager(new GridLayoutManager(getApplicationContext(), columnsCount));
        }
    }

    public int columnsCount() {
        return 3;
    }

        @Override
    public void onItemSelected(int position) {
        onMediaClick(RvMediaActivity.this.album, adapter.getMedia(), position);
    }

    @Override
    public void onSelectMode(boolean selectMode) {

    }

    @Override
    public void onSelectionCountChanged(int selectionCount, int totalCount) {

    }

    @Override
    public void onMediaClick(Album album, ArrayList<Media> media, int position){
        Intent intent = new Intent(getApplicationContext(), SingleMediaActivity.class);
        intent.putExtra(SingleMediaActivity.EXTRA_ARGS_ALBUM, album);
        try{
            intent.putExtra(SingleMediaActivity.EXTRA_ARGS_MEDIA, media);
            intent.putExtra(SingleMediaActivity.EXTRA_ARGS_POSITION, position);
            startActivity(intent);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
