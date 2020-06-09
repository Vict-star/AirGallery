package cn.edu.scut.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cn.edu.scut.R;
import cn.edu.scut.adapters.AlbumsAdapter;
import cn.edu.scut.data.Album;
import cn.edu.scut.data.provider.CPHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AlbumsFragment extends Fragment {
    private AlbumsAdapter adapter;
    private AlbumsViewModel albumsViewModel;
    private AlbumClickListener listener;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        albumsViewModel =
                ViewModelProviders.of(this).get(AlbumsViewModel.class);

        View root = inflater.inflate(R.layout.fragment_albums, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
//
        albumsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        adapter = new AlbumsAdapter(getContext());
        final RecyclerView rv = root.findViewById(R.id.albums);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        displayAlbums();
        return root;
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AlbumClickListener) listener = (AlbumClickListener) context;
    }

    public interface AlbumClickListener {
        void onAlbumClick(Album album);
    }
}
