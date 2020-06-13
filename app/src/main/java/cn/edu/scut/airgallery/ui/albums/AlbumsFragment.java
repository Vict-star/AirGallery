package cn.edu.scut.airgallery.ui.albums;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.edu.scut.airgallery.R;
import cn.edu.scut.airgallery.adapters.AlbumsAdapter;
import cn.edu.scut.airgallery.data.Album;
import cn.edu.scut.airgallery.data.provider.CPHelper;
import cn.edu.scut.airgallery.items.ActionsListener;
import cn.edu.scut.airgallery.views.GridSpacingItemDecoration;
import cn.edu.scut.airgallery.util.Measure;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class AlbumsFragment extends Fragment implements ActionsListener{
    public static final String TAG = "AlbumsFragment";

    @BindView(R.id.albums) RecyclerView rv;

    private AlbumsAdapter adapter;
    private GridSpacingItemDecoration spacingDecoration;
    private AlbumClickListener listener;

    public interface AlbumClickListener {
        void onAlbumClick(Album album);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AlbumClickListener) listener = (AlbumClickListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpColumns();
    }

    private void displayAlbums() {
        adapter.clear();
        CPHelper.getAlbums(getContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        album -> adapter.add(album),
                        throwable -> {
                            throwable.printStackTrace();
                        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        displayAlbums();
    }

    public void setUpColumns() {
        int columnsCount = columnsCount();

        if (columnsCount != ((GridLayoutManager) rv.getLayoutManager()).getSpanCount()) {
            rv.removeItemDecoration(spacingDecoration);
            spacingDecoration = new GridSpacingItemDecoration(columnsCount, Measure.pxToDp(3, getContext()), true);
            rv.addItemDecoration(spacingDecoration);
            rv.setLayoutManager(new GridLayoutManager(getContext(), columnsCount));
        }
    }

    public int columnsCount() {
        return 2;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_albums, container, false);
        ButterKnife.bind(this, root);

        //设置rv的item间隔
        int spanCount = columnsCount();
        spacingDecoration = new GridSpacingItemDecoration(spanCount, Measure.pxToDp(3, getContext()), true);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(spacingDecoration);
        rv.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        adapter = new AlbumsAdapter(getContext(), this);
        rv.setAdapter(adapter);

        return root;
    }

    @Override
    public void onItemSelected(int position) {
        if (listener != null) listener.onAlbumClick(adapter.get(position));
    }

    @Override
    public void onSelectMode(boolean selectMode) {

    }

    @Override
    public void onSelectionCountChanged(int selectionCount, int totalCount) {

    }

}