package cn.edu.scut.adapters;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.edu.items.ActionsListener;
import cn.edu.scut.R;
import cn.edu.scut.data.Album;

/**
 * Created by dnld on 1/7/16.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    private List<Album> albums;

    private int selectedCount = 0;

    private ActionsListener actionsListener;

    private boolean isSelecting;

    public AlbumsAdapter(Context context, ActionsListener actionsListener) {
        super();
        albums = new ArrayList<>();
        this.actionsListener = actionsListener;
    }

    public AlbumsAdapter(Context context) {
        super();
        albums = new ArrayList<>();
    }

    public List<String> getAlbumsPaths() {
        ArrayList<String> list = new ArrayList<>();

        for (Album album : albums) {
            list.add(album.getPath());
        }

        return list;
    }

    public Album get(int pos) {
        return albums.get(pos);
    }

    public void notifyItemChanaged(Album album) {
        notifyItemChanged(albums.indexOf(album));
    }

    public List<Album> getSelectedAlbums() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return albums.stream().filter(Album::isSelected).collect(Collectors.toList());
        } else {
            ArrayList<Album> arrayList = new ArrayList<>(selectedCount);
            for (Album album : albums)
                if (album.isSelected())
                    arrayList.add(album);
            return arrayList;
        }
    }

    public Album getFirstSelectedAlbum() {
        if (selectedCount > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                return albums.stream().filter(Album::isSelected).findFirst().orElse(null);
            else
                for (Album album : albums)
                    if (album.isSelected())
                        return album;
        }
        return null;
    }

    private void startSelection() {
        isSelecting = true;
        actionsListener.onSelectMode(true);
    }

    private void stopSelection() {
        isSelecting = false;
        actionsListener.onSelectMode(false);
    }

    public int getSelectedCount() {
        return selectedCount;
    }

    public void selectAll() {
        for (int i = 0; i < albums.size(); i++)
            if (albums.get(i).setSelected(true))
                notifyItemChanged(i);
        selectedCount = albums.size();
        startSelection();
    }

    public void removeSelectedAlbums(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            albums.removeIf(Album::isSelected);
        else {
            Iterator<Album> iter = albums.iterator();

            while (iter.hasNext()) {
                Album album = iter.next();

                if (album.isSelected())
                    iter.remove();
            }
        }
        selectedCount = 0;
        notifyDataSetChanged();
    }

    public void removeAlbumsThatStartsWith(String path){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            albums.removeIf(album -> album.getPath().startsWith(path));
        else {
            Iterator<Album> iter = albums.iterator();

            while (iter.hasNext()) {
                Album album = iter.next();

                if (album.getPath().startsWith(path))
                    iter.remove();
            }
        }

        notifyDataSetChanged();
    }

    public void removeAlbum(Album album) {
        int i = albums.indexOf(album);
        albums.remove(i);
        notifyItemRemoved(i);

    }

    public void invalidateSelectedCount() {
        int c = 0;
        for (Album m : this.albums) {
            c += m.isSelected() ? 1 : 0;
        }

        this.selectedCount = c;

        if (this.selectedCount == 0) stopSelection();
        else {
            this.actionsListener.onSelectionCountChanged(selectedCount, albums.size());
        }
    }

    public boolean clearSelected() {

        boolean changed = true;
        for (int i = 0; i < albums.size(); i++) {
            boolean b = albums.get(i).setSelected(false);
            if (b)
                notifyItemChanged(i);
            changed &= b;
        }

        selectedCount = 0;
        stopSelection();
        return changed;
    }

    public void forceSelectedCount(int count) {
        selectedCount = count;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_album, parent, false);
        return new ViewHolder(v);
    }

    private void notifySelected(boolean increase) {
        selectedCount += increase ? 1 : -1;
        actionsListener.onSelectionCountChanged(selectedCount, getItemCount());

        if (selectedCount == 0 && isSelecting) stopSelection();
        else if (selectedCount > 0 && !isSelecting) startSelection();
    }

    public boolean selecting() {
        return isSelecting;
    }

    @Override
    public void onBindViewHolder(final AlbumsAdapter.ViewHolder holder, int position) {
        // TODO Calvin: Major Refactor - No business logic here.
        Album a = albums.get(position);

        RequestOptions options = new RequestOptions()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .centerCrop()
                .error(R.drawable.ic_error)
                //.animate(R.anim.fade_in)//TODO:DONT WORK WELL
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

        Log.v("path",a.getCover());
        Glide.with(holder.cover.getContext())
                .load(a.getCover())
                .apply(options)
                .into(holder.cover);


//        holder.llCount.setVisibility(View.VISIBLE);
//        holder.name.setText(a.getName());
//        holder.nMedia.setText(String.valueOf(a.getCount()));
//        holder.path.setVisibility(View.VISIBLE);
//        holder.path.setText(a.getPath());

        //START Animation MAKES BUG ON FAST TAP ON CARD
        //Animation anim;
        //anim = AnimationUtils.loadAnimation(holder.albumCard.getContext(), R.anim.slide_fade_card);
        //holder.albumCard.startAnimation(anim);
        //ANIMS
        //holder.card.animate().alpha(1).setDuration(250);

        holder.cover.setOnClickListener(v -> {
            if (selecting()) {
                notifySelected(a.toggleSelected());
                notifyItemChanged(position);
            } else
                actionsListener.onItemSelected(position);
        });
//
//        holder.card.setOnLongClickListener(v -> {
//            notifySelected(a.toggleSelected());
//            notifyItemChanged(position);
//            return true;
//        });
    }

    public void clear() {
        albums.clear();
        notifyDataSetChanged();
    }

    public int add(Album album) {
//        int i = Collections.binarySearch(
//                albums, album, AlbumsComparators.getComparator(sortingMode, sortingOrder));
//        if (i < 0) i = ~i;
        Log.v("album",album.getPath());
        int i = 0;
        albums.add(i, album);
        notifyItemInserted(i);
        //int finalI = i;
        //((ThemedActivity) context).runOnUiThread(() -> notifyItemInserted(finalI));
        return i;

    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cover)
        ImageView cover;
//        @BindView(R.id.album_card)
//        CardView card;
//        @BindView(R.id.album_preview)
//        ImageView picture;
//        @BindView(R.id.ll_album_info)
//        View footer;
//        @BindView(R.id.ll_media_count)
//        View llCount;
//        @BindView(R.id.album_name)
//        TextView name;
//        @BindView(R.id.album_media_count)
//        TextView nMedia;
//        @BindView(R.id.album_media_label)
//        TextView mediaLabel;
//        @BindView(R.id.album_path)
//        TextView path;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}